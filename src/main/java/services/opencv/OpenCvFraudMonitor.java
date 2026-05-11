package services.opencv;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;

/**
 * Haar faces plus motion heuristics: {@code NO_FACE_DETECTED}, {@code MULTIPLE_FACES},
 * {@code MOTION_ANOMALY_NO_FACE}, {@code OBJECT_NEAR_FACE}, {@code EXCESSIVE_HEAD_TURNS},
 * {@code PHONE_IN_FRAME}. Processing runs on each {@link #offerFrame} (no fixed-delay scheduler).
 */
public final class OpenCvFraudMonitor {

    private static final long NO_FACE_DURATION_MS = 6_000L;
    private static final long NO_FACE_START_GRACE_MS = 4_000L;
    private static final int MULTI_FACE_CONSECUTIVE_TICKS = 12;

    /** Mean abs-diff (0–255) on full 320×240 frame: movement while Haar sees no face. */
    private static final double FULL_FRAME_MOTION_MEAN_THRESH = 27.0;
    /** Mean abs-diff in ring around face(s): object / hand entering near head. */
    private static final double NEAR_FACE_RING_MOTION_MEAN_THRESH = 24.0;
    /** Ring must exceed full-frame mean by this factor (localised motion near face). */
    private static final double NEAR_FACE_RING_VS_FULL_RATIO = 1.22;
    /** Minimum full-frame motion so a static camera does not trigger near-face from noise. */
    private static final double NEAR_FACE_MIN_FULL_MEAN = 14.0;
    private static final int MOTION_ANOMALY_STREAK = 10;
    private static final int OBJECT_NEAR_FACE_STREAK = 11;
    private static final long MOTION_GRACE_MS = 4_000L;
    /**
     * DNN runs every N-th processed frame (same thread as capture) to limit CPU and avoid UI stalls.
     * Override: {@code -Deducore.phone.dnnEveryNFrames=3}
     */
    private static final int PHONE_DNN_EVERY_N_FRAMES = parsePositiveInt("educore.phone.dnnEveryNFrames", 3);
    /** Strict consecutive positive DNN evaluations (not camera frames) before notifying the policy layer. */
    private static final int PHONE_CONSECUTIVE_DNN_HITS = 3;
    /** Heuristic path: require this many consecutive camera frames (legacy fallback when DNN unavailable). */
    private static final int PHONE_HEURISTIC_CONSECUTIVE_FRAMES = 3;
    /**
     * Ignore SSD “phone” boxes whose IoU with an expanded face exceeds this (face mislabeled as phone).
     * {@code -Deducore.phone.maxFaceIou=0.22}
     */
    private static final double PHONE_MAX_IOU_WITH_FACE = parseDouble01("educore.phone.maxFaceIou", 0.22);
    /**
     * Also ignore if this fraction of the phone box intersects the expanded face.
     * {@code -Deducore.phone.maxOverlapFracOnFace=0.38}
     */
    private static final double PHONE_MAX_AREA_FRAC_ON_FACE = parseDouble01("educore.phone.maxOverlapFracOnFace", 0.38);
    /** Expand Haar face before overlap test (phones partly occluding cheek still overlap face). */
    private static final double PHONE_FACE_EXPAND_RATIO = 1.12;

    private static final double NEAR_FACE_OUTER_SCALE = 1.75;

    /**
     * Haar face box shifts sideways when the head yaws. Thresholds scale with {@code face.width}
     * (320×240 analysis space) so different face sizes behave similarly.
     * <p>
     * <strong>Hysteresis:</strong> “Away” when {@code |cx-mid| > offFrac·width} (confirmed {@code N} frames);
     * “Centered” again when {@code |cx-mid| < onFrac·width} (confirmed {@code M} frames), with {@code offFrac > onFrac}.
     */
    private static final double HEAD_TURN_OFF_FRAC = parseHeadTurnFrac("educore.headTurn.offFrac", 0.20);
    private static final double HEAD_TURN_ON_FRAC = parseHeadTurnFrac("educore.headTurn.onFrac", 0.095);
    private static final int HEAD_TURN_OFF_MIN_PX = parseHeadTurnMarginPx("educore.headTurn.offMinPx", 14);
    private static final int HEAD_TURN_ON_MIN_PX = parseHeadTurnMarginPx("educore.headTurn.onMinPx", 8);
    /** Frames {@code |dx|} must stay beyond “off” before we count a look-away (reduces jitter). */
    private static final int HEAD_TURN_AWAY_CONFIRM_FRAMES = parsePositiveInt("educore.headTurn.awayConfirmFrames", 2);
    /** Frames {@code |dx|} must stay inside “on” before we accept facing forward again. */
    private static final int HEAD_TURN_CENTER_CONFIRM_FRAMES = parsePositiveInt("educore.headTurn.centerConfirmFrames", 3);
    /**
     * Fraud after this many distinct look-away episodes (centered → away, after debounce).
     * Default {@code 5} ⇒ only after <strong>more than four</strong> turns (fifth episode triggers).
     * Override: {@code -Deducore.headTurn.eventsBeforeFraud=5}
     */
    private static final int HEAD_TURN_EVENTS_BEFORE_FRAUD = parsePositiveInt("educore.headTurn.eventsBeforeFraud", 5);

    private final OpenCvVisionAnalyzer analyzer;
    private final BiConsumer<String, String> fraudNotifier;
    private final BooleanSupplier sessionStillActive;
    private final DnnPhoneDetector dnnPhone;
    private final DoubleConsumer phoneBurstConfirmed;

    private final AtomicReference<BufferedImage> latestFrame = new AtomicReference<>();
    private final AtomicBoolean schedulerRunning = new AtomicBoolean(false);

    private Mat prevGrayEq;
    private long lastFaceSeenAtMs = System.currentTimeMillis();
    private long monitorStartedAtMs;
    private int multiFaceStreak;
    private int fullMotionNoFaceStreak;
    private int nearFaceObjectStreak;
    /** Logical state: student is considered facing the camera (after hysteresis + debounce). */
    private boolean headFacingCenter = true;
    private int headTurnAwayCount;
    private int headAwayStreak;
    private int headCenterStreak;
    private int phonePositiveStreak;
    private int phoneDnnFrameCounter;
    private int phoneConsecutiveDnnHits;
    /** Max model confidence across the current consecutive DNN hit streak (for logging / policy). */
    private double phonePendingMaxConfidence;

    public OpenCvFraudMonitor(
            BiConsumer<String, String> fraudNotifier,
            BooleanSupplier sessionStillActive,
            DnnPhoneDetector dnnPhone,
            DoubleConsumer phoneBurstConfirmed
    ) throws Exception {
        if (!OpenCvLoader.ensureLoaded()) {
            Throwable cause = OpenCvLoader.getLoadError();
            throw cause != null
                    ? new IllegalStateException("OpenCV native library not loaded", cause)
                    : new IllegalStateException("OpenCV native library not loaded");
        }
        this.fraudNotifier = fraudNotifier;
        this.sessionStillActive = sessionStillActive;
        this.dnnPhone = dnnPhone;
        this.phoneBurstConfirmed = phoneBurstConfirmed != null ? phoneBurstConfirmed : c -> { };
        this.analyzer = new OpenCvVisionAnalyzer();
    }

    private static int parsePositiveInt(String key, int def) {
        try {
            String s = System.getProperty(key);
            if (s != null && !s.isBlank()) {
                int v = Integer.parseInt(s.trim());
                return v >= 1 ? v : def;
            }
        } catch (NumberFormatException ignored) {
        }
        return def;
    }

    /** Margin 1–160 px on 320-wide analysis frame. */
    private static int parseHeadTurnMarginPx(String key, int def) {
        int v = parsePositiveInt(key, def);
        return Math.min(160, Math.max(4, v));
    }

    /** Fraction of face width for head-turn bands (0.05–0.55). */
    private static double parseHeadTurnFrac(String key, double def) {
        try {
            String s = System.getProperty(key);
            if (s != null && !s.isBlank()) {
                double v = Double.parseDouble(s.trim());
                return Math.min(0.55, Math.max(0.05, v));
            }
        } catch (NumberFormatException ignored) {
        }
        return def;
    }

    private static double parseDouble01(String key, double def) {
        try {
            String s = System.getProperty(key);
            if (s != null && !s.isBlank()) {
                return Double.parseDouble(s.trim());
            }
        } catch (NumberFormatException ignored) {
        }
        return def;
    }

    /**
     * Each camera frame runs vision + phone checks immediately on the calling thread (no scheduler delay).
     */
    public void offerFrame(BufferedImage rgbSnapshot) {
        if (rgbSnapshot == null || !schedulerRunning.get()) {
            return;
        }
        latestFrame.set(rgbSnapshot);
        tick();
    }

    public void start() {
        if (!schedulerRunning.compareAndSet(false, true)) {
            return;
        }
        long now = System.currentTimeMillis();
        monitorStartedAtMs = now;
        lastFaceSeenAtMs = now;
        multiFaceStreak = 0;
        fullMotionNoFaceStreak = 0;
        nearFaceObjectStreak = 0;
        headFacingCenter = true;
        headTurnAwayCount = 0;
        headAwayStreak = 0;
        headCenterStreak = 0;
        phonePositiveStreak = 0;
        phoneDnnFrameCounter = 0;
        phoneConsecutiveDnnHits = 0;
        phonePendingMaxConfidence = 0;
        if (prevGrayEq != null) {
            prevGrayEq.release();
            prevGrayEq = null;
        }
        analyzer.resetState();
    }

    public void stop() {
        schedulerRunning.set(false);
        latestFrame.set(null);
        if (prevGrayEq != null) {
            prevGrayEq.release();
            prevGrayEq = null;
        }
        analyzer.resetState();
    }

    public void close() {
        stop();
        if (dnnPhone != null) {
            dnnPhone.close();
        }
        analyzer.close();
    }

    private void tick() {
        if (!sessionStillActive.getAsBoolean()) {
            return;
        }
        BufferedImage frame = latestFrame.getAndSet(null);
        if (frame == null) {
            return;
        }

        int faceCount;
        Rect[] rects;
        Mat currGray = null;
        Mat bgrSmall = null;
        try {
            OpenCvVisionAnalyzer.VisionFrameResult r = analyzer.analyze(frame);
            faceCount = r.faceCount();
            rects = r.faceRects();
            currGray = r.grayEqualizedSmall();
            bgrSmall = r.bgrSmall();
        } catch (Exception ex) {
            faceCount = 0;
            rects = new Rect[0];
            if (prevGrayEq != null) {
                prevGrayEq.release();
                prevGrayEq = null;
            }
        }

        long now = System.currentTimeMillis();

        try {
            if (faceCount > 1) {
                multiFaceStreak++;
                if (multiFaceStreak >= MULTI_FACE_CONSECUTIVE_TICKS) {
                    fraudNotifier.accept(
                            "MULTIPLE_FACES",
                            "More than one face detected in the exam webcam frame."
                    );
                    if (currGray != null) {
                        currGray.release();
                    }
                    return;
                }
            } else {
                multiFaceStreak = 0;
            }

            if (faceCount >= 1 && currGray != null) {
                int fw = frame.getWidth();
                int fh = frame.getHeight();
                Rect[] facesFull = OpenCvVisionAnalyzer.scaleFaceRectsToFrame(rects, fw, fh);

                if (dnnPhone != null) {
                    phoneDnnFrameCounter++;
                    if (phoneDnnFrameCounter % PHONE_DNN_EVERY_N_FRAMES == 0) {
                        double bestConf = evaluateDnnPhone(frame, facesFull);
                        if (bestConf > 0) {
                            phonePendingMaxConfidence = Math.max(phonePendingMaxConfidence, bestConf);
                            phoneConsecutiveDnnHits++;
                            if (phoneConsecutiveDnnHits >= PHONE_CONSECUTIVE_DNN_HITS) {
                                phoneBurstConfirmed.accept(phonePendingMaxConfidence);
                                phoneConsecutiveDnnHits = 0;
                                phonePendingMaxConfidence = 0;
                            }
                        } else {
                            phoneConsecutiveDnnHits = 0;
                            phonePendingMaxConfidence = 0;
                        }
                    }
                } else if (bgrSmall != null) {
                    if (OpenCvPhoneHeuristicDetector.phoneLikeObjectNearFace(bgrSmall, faceCount, rects)) {
                        phonePositiveStreak++;
                        if (phonePositiveStreak >= PHONE_HEURISTIC_CONSECUTIVE_FRAMES) {
                            phonePositiveStreak = 0;
                            phoneBurstConfirmed.accept(0.75);
                        }
                    } else {
                        phonePositiveStreak = 0;
                    }
                }
            } else {
                phonePositiveStreak = 0;
                phoneConsecutiveDnnHits = 0;
                phonePendingMaxConfidence = 0;
            }

            if (prevGrayEq != null && currGray != null) {
                boolean motionFraud = evaluateMotionAnomalies(prevGrayEq, currGray, faceCount, rects, now);
                prevGrayEq.release();
                prevGrayEq = null;
                if (motionFraud) {
                    currGray.release();
                    return;
                }
            }
            if (currGray != null) {
                prevGrayEq = currGray;
            }

            if (faceCount >= 1) {
                lastFaceSeenAtMs = now;
            }

            if (checkExcessiveHeadTurns(rects, faceCount)) {
                return;
            }

            if (faceCount == 0
                    && now - monitorStartedAtMs >= NO_FACE_START_GRACE_MS
                    && now - lastFaceSeenAtMs >= NO_FACE_DURATION_MS) {
                fraudNotifier.accept(
                        "NO_FACE_DETECTED",
                        "No face detected by the proctoring camera for "
                                + (NO_FACE_DURATION_MS / 1000)
                                + " seconds (after warm-up). Stay centered and ensure your face is lit."
                );
            }
        } finally {
            if (bgrSmall != null) {
                bgrSmall.release();
            }
        }
    }

    /**
     * Runs SSD on the full webcam frame (native resolution, e.g. 640×480). Returns best confidence
     * among boxes that are not discarded as face overlaps, or 0.
     */
    private double evaluateDnnPhone(BufferedImage rgb, Rect[] facesFull) {
        Mat bgr = OpenCvVisionAnalyzer.bgrMatFromBufferedImage(rgb);
        try {
            List<PhoneDetection> raw = dnnPhone.detectPhones(bgr);
            int fw = rgb.getWidth();
            int fh = rgb.getHeight();
            double best = 0;
            for (PhoneDetection p : raw) {
                if (ignorePhoneForFaceOverlap(p.bounds(), facesFull, fw, fh)) {
                    continue;
                }
                best = Math.max(best, p.confidence());
            }
            return best;
        } finally {
            bgr.release();
        }
    }

    private static boolean ignorePhoneForFaceOverlap(Rect phone, Rect[] facesFull, int frameW, int frameH) {
        if (facesFull == null || phone.width <= 0 || phone.height <= 0) {
            return false;
        }
        for (Rect f : facesFull) {
            if (f.width <= 0 || f.height <= 0) {
                continue;
            }
            Rect expanded = OpenCvVisionAnalyzer.expandRectProportional(f, PHONE_FACE_EXPAND_RATIO, frameW, frameH);
            if (OpenCvVisionAnalyzer.intersectionOverUnion(phone, expanded) >= PHONE_MAX_IOU_WITH_FACE) {
                return true;
            }
            double inter = OpenCvVisionAnalyzer.intersectionAreaPx(phone, expanded);
            double pArea = Math.max(1.0, phone.width * (double) phone.height);
            if (inter / pArea >= PHONE_MAX_AREA_FRAC_ON_FACE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Uses face bounding-box center vs frame midline as a proxy for yaw (Haar has no pose).
     * Hysteresis + multi-frame confirmation reduce flicker; width-scaled bands adapt to face size.
     *
     * @return true if fraud was reported
     */
    private boolean checkExcessiveHeadTurns(Rect[] rects, int faceCount) {
        if (faceCount != 1 || rects.length != 1) {
            headAwayStreak = 0;
            headCenterStreak = 0;
            return false;
        }
        Rect r = rects[0];
        if (r.width < 20) {
            return false;
        }
        int cx = r.x + r.width / 2;
        int mid = OpenCvVisionAnalyzer.ANALYSIS_WIDTH / 2;
        int dx = Math.abs(cx - mid);

        int offPx = Math.max(HEAD_TURN_OFF_MIN_PX, (int) Math.round(HEAD_TURN_OFF_FRAC * r.width));
        int onPx = Math.max(HEAD_TURN_ON_MIN_PX, (int) Math.round(HEAD_TURN_ON_FRAC * r.width));
        if (offPx <= onPx + 4) {
            offPx = onPx + 5;
        }

        boolean rawAway = dx > offPx;
        boolean rawCenter = dx < onPx;

        if (rawAway) {
            headAwayStreak++;
            headCenterStreak = 0;
        } else if (rawCenter) {
            headCenterStreak++;
            headAwayStreak = 0;
        } else {
            headAwayStreak = 0;
            headCenterStreak = 0;
        }

        if (headFacingCenter) {
            if (headAwayStreak >= HEAD_TURN_AWAY_CONFIRM_FRAMES) {
                headFacingCenter = false;
                headTurnAwayCount++;
                headAwayStreak = 0;
                headCenterStreak = 0;
                if (headTurnAwayCount >= HEAD_TURN_EVENTS_BEFORE_FRAUD) {
                    fraudNotifier.accept(
                            "EXCESSIVE_HEAD_TURNS",
                            "You turned your head away from the camera too many times during this exam."
                    );
                    return true;
                }
            }
        } else {
            if (headCenterStreak >= HEAD_TURN_CENTER_CONFIRM_FRAMES) {
                headFacingCenter = true;
                headCenterStreak = 0;
                headAwayStreak = 0;
            }
        }
        return false;
    }

    /**
     * @return true if fraud was reported (caller still owns {@code currGray} for chaining).
     */
    private boolean evaluateMotionAnomalies(Mat prev, Mat curr, int faceCount, Rect[] rects, long now) {
        if (now - monitorStartedAtMs < MOTION_GRACE_MS) {
            fullMotionNoFaceStreak = 0;
            nearFaceObjectStreak = 0;
            return false;
        }

        Mat diff = new Mat();
        Core.absdiff(prev, curr, diff);
        double fullMean = Core.mean(diff).val[0];

        try {
            if (faceCount == 0) {
                if (fullMean >= FULL_FRAME_MOTION_MEAN_THRESH) {
                    fullMotionNoFaceStreak++;
                    if (fullMotionNoFaceStreak >= MOTION_ANOMALY_STREAK) {
                        fraudNotifier.accept(
                                "MOTION_ANOMALY_NO_FACE",
                                "Large movement in the frame while no face is visible (possible obstruction or second person)."
                        );
                        return true;
                    }
                } else {
                    fullMotionNoFaceStreak = 0;
                }
            } else {
                fullMotionNoFaceStreak = 0;
            }

            if (faceCount >= 1 && faceCount <= 2 && rects.length >= 1) {
                Mat ringMask = buildNearFaceRingMask(rects);
                if (ringMask != null) {
                    try {
                        Scalar ringScalar = Core.mean(diff, ringMask);
                        double ringMean = ringScalar.val[0];
                        boolean localized = ringMean >= NEAR_FACE_RING_VS_FULL_RATIO * Math.max(fullMean, 1.0);
                        if (ringMean >= NEAR_FACE_RING_MOTION_MEAN_THRESH
                                && fullMean >= NEAR_FACE_MIN_FULL_MEAN
                                && localized) {
                            nearFaceObjectStreak++;
                            if (nearFaceObjectStreak >= OBJECT_NEAR_FACE_STREAK) {
                                fraudNotifier.accept(
                                        "OBJECT_NEAR_FACE",
                                        "Sudden movement or object motion detected close to your face."
                                );
                                return true;
                            }
                        } else {
                            nearFaceObjectStreak = 0;
                        }
                    } finally {
                        ringMask.release();
                    }
                } else {
                    nearFaceObjectStreak = 0;
                }
            } else {
                nearFaceObjectStreak = 0;
            }
        } finally {
            diff.release();
        }
        return false;
    }

    private static Mat buildNearFaceRingMask(Rect[] rects) {
        if (rects.length == 0) {
            return null;
        }
        Rect u = unionAll(rects);
        if (u.width <= 0 || u.height <= 0) {
            return null;
        }
        Rect outer = expandRectClamped(u, NEAR_FACE_OUTER_SCALE,
                OpenCvVisionAnalyzer.ANALYSIS_WIDTH, OpenCvVisionAnalyzer.ANALYSIS_HEIGHT);
        Mat mask = Mat.zeros(OpenCvVisionAnalyzer.ANALYSIS_HEIGHT, OpenCvVisionAnalyzer.ANALYSIS_WIDTH, CvType.CV_8UC1);
        Imgproc.rectangle(mask, outer, Scalar.all(255), -1);
        for (Rect r : rects) {
            if (r.width > 0 && r.height > 0) {
                Imgproc.rectangle(mask, r, Scalar.all(0), -1);
            }
        }
        return mask;
    }

    private static Rect unionAll(Rect[] rects) {
        int x1 = Integer.MAX_VALUE;
        int y1 = Integer.MAX_VALUE;
        int x2 = 0;
        int y2 = 0;
        for (Rect r : rects) {
            x1 = Math.min(x1, r.x);
            y1 = Math.min(y1, r.y);
            x2 = Math.max(x2, r.x + r.width);
            y2 = Math.max(y2, r.y + r.height);
        }
        if (x1 >= x2 || y1 >= y2) {
            return new Rect(0, 0, 0, 0);
        }
        return new Rect(x1, y1, x2 - x1, y2 - y1);
    }

    private static Rect expandRectClamped(Rect r, double scale, int maxW, int maxH) {
        int nw = (int) Math.round(r.width * scale);
        int nh = (int) Math.round(r.height * scale);
        nw = Math.max(nw, 1);
        nh = Math.max(nh, 1);
        int cx = r.x + r.width / 2;
        int cy = r.y + r.height / 2;
        int nx = cx - nw / 2;
        int ny = cy - nh / 2;
        nx = Math.max(0, Math.min(nx, Math.max(0, maxW - nw)));
        ny = Math.max(0, Math.min(ny, Math.max(0, maxH - nh)));
        nw = Math.min(nw, maxW - nx);
        nh = Math.min(nh, maxH - ny);
        return new Rect(nx, ny, nw, nh);
    }
}
