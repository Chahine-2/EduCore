package services;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import services.opencv.DnnPhoneDetector;
import services.opencv.OpenCvFraudMonitor;
import services.opencv.OpenCvLoader;
import services.opencv.PhoneDetectorConfig;

import javax.swing.SwingUtilities;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Anti-cheat: window focus / fullscreen, plus optional webcam proctoring (OpenCV Haar + optional
 * MobileNet-SSD phone DNN).
 * <p>
 * Phone signals: {@link OpenCvFraudMonitor} only notifies this class after consecutive inferences;
 * {@link #onPhoneBurstConfirmed(double)} adds a <em>suspicion score</em> and applies a cooldown.
 * {@code PHONE_IN_FRAME} is reported only when the score reaches {@link #PHONE_SUSPICION_ESCALATE_THRESHOLD}.
 * <p>
 * With no webcam, leave {@link #ENABLE_VISION_ANTICHEAT} {@code false} so the quiz only enforces
 * focus/fullscreen. Turn vision on when machines have cameras; use {@link #WEBCAM_REQUIRED_FOR_EXAM}
 * if missing hardware should end the attempt ({@code CAMERA_OFF}).
 */
public class FraudeService {

    private static final boolean ENFORCE_FOCUS_FULLSCREEN_ANTI_CHEAT = true;
    /** When false, no OpenCV/webcam is used (no {@code NO_FACE_DETECTED} / {@code MULTIPLE_FACES} / {@code CAMERA_OFF}). */
    private static final boolean ENABLE_VISION_ANTICHEAT = true;
    /** If true, failing to open the camera or losing the video stream ends the attempt ({@code CAMERA_OFF}). */
    private static final boolean WEBCAM_REQUIRED_FOR_EXAM = false;

    /** Target max time between frames; actual gap may be shorter (see adaptive sleep in capture loop). */
    private static final int CAPTURE_INTERVAL_MS = 33;
    /** Avg luma (0–255) below this on a grid sample ⇒ lens covered / cap on camera. */
    private static final int BLOCKED_CAMERA_MAX_MEAN_LUMA = 10;
    /** Skip “black frame” checks while the sensor auto-exposes (does not apply to null frames). */
    private static final long BLOCKED_LUMA_GRACE_AFTER_CAPTURE_START_MS = 1_500L;

    /** UI / wiring: hide webcam preview when vision anti-cheat is compiled off. */
    public static boolean isVisionAnticheatEnabled() {
        return ENABLE_VISION_ANTICHEAT;
    }

    @FunctionalInterface
    public interface FraudeHandler {
        void onFraudeDetected(String type, String description);
    }

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean triggered = new AtomicBoolean(false);

    private Stage stage;
    private FraudeHandler fraudeHandler;
    private ImageView webcamPreview;

    private ChangeListener<Boolean> stageFocusListener;
    private ChangeListener<Boolean> stageIconifiedListener;
    private ChangeListener<Boolean> stageFullScreenListener;

    private volatile long ignoreUiFraudUntilMs;

    private ExecutorService captureExecutor;
    private VideoCapture openCvCapture;
    private Webcam sarxosWebcam;
    private OpenCvFraudMonitor openCvMonitor;
    private int consecutiveNullFrames;
    private int consecutiveDarkFrames;

    private final Object phonePolicyLock = new Object();
    private int phoneSuspicionScore;
    private long phoneCooldownUntilMs;

    /** Weight added per confirmed burst (3 consecutive DNN hits). {@code -Deducore.phone.suspicionDelta=3} */
    private static final int PHONE_SUSPICION_DELTA = readIntProp("educore.phone.suspicionDelta", 3);
    /** Cumulative score before {@code PHONE_IN_FRAME} is reported. {@code -Deducore.phone.suspicionThreshold=9} */
    private static final int PHONE_SUSPICION_ESCALATE_THRESHOLD = readIntProp("educore.phone.suspicionThreshold", 9);
    /** After a burst is counted, ignore further bursts for this long. {@code -Deducore.phone.cooldownMs=5000} */
    private static final long PHONE_SUSPICION_COOLDOWN_MS = readLongProp("educore.phone.cooldownMs", 5_000L);

    private static int readIntProp(String key, int def) {
        try {
            String s = System.getProperty(key);
            if (s != null && !s.isBlank()) {
                return Integer.parseInt(s.trim());
            }
        } catch (NumberFormatException ignored) {
        }
        return def;
    }

    private static long readLongProp(String key, long def) {
        try {
            String s = System.getProperty(key);
            if (s != null && !s.isBlank()) {
                return Long.parseLong(s.trim());
            }
        } catch (NumberFormatException ignored) {
        }
        return def;
    }

    public void initialize(Stage stage, FraudeHandler handler) {
        initialize(stage, handler, null);
    }

    public void initialize(Stage stage, FraudeHandler handler, ImageView webcamPreview) {
        stop();

        this.stage = stage;
        this.fraudeHandler = handler;
        this.webcamPreview = webcamPreview;
        this.running.set(true);
        this.triggered.set(false);
        this.ignoreUiFraudUntilMs = System.currentTimeMillis() + 8_000L;
        this.consecutiveNullFrames = 0;
        this.consecutiveDarkFrames = 0;
        synchronized (phonePolicyLock) {
            this.phoneSuspicionScore = 0;
            this.phoneCooldownUntilMs = 0;
        }

        if (!stage.isFullScreen()) {
            stage.setFullScreen(true);
        }

        if (ENFORCE_FOCUS_FULLSCREEN_ANTI_CHEAT) {
            stageFocusListener = (obs, oldVal, focused) -> {
                if (!Boolean.FALSE.equals(focused)) {
                    return;
                }
                Platform.runLater(() -> Platform.runLater(() -> {
                    if (!running.get() || triggered.get()) {
                        return;
                    }
                    long now = System.currentTimeMillis();
                    if (now < ignoreUiFraudUntilMs) {
                        return;
                    }
                    if (!stage.isShowing() || stage.isIconified()) {
                        return;
                    }
                    if (stage.isFocused()) {
                        return;
                    }
                    notifyFraude("WINDOW_FOCUS_LOST", "Exam window lost focus (alt-tab/app switch/minimize).");
                }));
            };
            stage.focusedProperty().addListener(stageFocusListener);

            stageIconifiedListener = (obs, oldVal, iconified) -> {
                if (Boolean.TRUE.equals(iconified)) {
                    long now = System.currentTimeMillis();
                    if (now < ignoreUiFraudUntilMs) {
                        return;
                    }
                    notifyFraude("WINDOW_FOCUS_LOST", "Exam window was minimized.");
                }
            };
            stage.iconifiedProperty().addListener(stageIconifiedListener);

            stageFullScreenListener = (obs, wasFull, isFull) -> {
                if (Boolean.TRUE.equals(wasFull) && Boolean.FALSE.equals(isFull)) {
                    notifyFraude("FULLSCREEN_EXIT", "Fullscreen mode was exited.");
                }
            };
            stage.fullScreenProperty().addListener(stageFullScreenListener);
        }

        if (ENABLE_VISION_ANTICHEAT) {
            startVisionPipelineAsync();
        }
    }

    private void startVisionPipelineAsync() {
        ExecutorService starter = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "exam-vision-start");
            t.setDaemon(true);
            return t;
        });
        starter.execute(() -> {
            try {
                startVisionPipelineBlocking();
            } finally {
                starter.shutdown();
            }
        });
    }

    private void startVisionPipelineBlocking() {
        if (!running.get() || triggered.get()) {
            return;
        }
        if (!OpenCvLoader.ensureLoaded()) {
            if (WEBCAM_REQUIRED_FOR_EXAM) {
                notifyFraude("CAMERA_OFF",
                        "Exam proctoring could not start (vision engine unavailable). Check that the app can load native libraries.");
            }
            return;
        }

        ensureAwtToolkitForWebcam();

        /* Sarxos first: on many Windows PCs DirectShow via webcam-capture opens reliably; OpenCV MSMF/DSHOW often fails or returns black frames. */
        boolean opened = tryOpenSarxosWebcam();
        if (!opened) {
            opened = tryOpenCvCapture();
        }
        if (!opened) {
            if (WEBCAM_REQUIRED_FOR_EXAM) {
                notifyFraude("CAMERA_OFF",
                        "Camera disconnected, off, or not found. Connect a webcam and allow camera access, then restart the quiz.");
            }
            return;
        }

        try {
            PhoneDetectorConfig phoneCfg = PhoneDetectorConfig.fromSystemProperties();
            DnnPhoneDetector dnnPhone = DnnPhoneDetector.tryLoad(phoneCfg);
            openCvMonitor = new OpenCvFraudMonitor(
                    this::notifyFraude,
                    () -> running.get() && !triggered.get(),
                    dnnPhone,
                    this::onPhoneBurstConfirmed
            );
            openCvMonitor.start();
        } catch (Exception e) {
            releaseCapture();
            if (WEBCAM_REQUIRED_FOR_EXAM) {
                notifyFraude("CAMERA_OFF",
                        "Camera proctoring failed to start: " + e.getMessage());
            }
            return;
        }

        captureExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "exam-webcam-capture");
            t.setDaemon(true);
            return t;
        });
        captureExecutor.execute(this::captureLoop);
    }

    private static void ensureAwtToolkitForWebcam() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        try {
            java.awt.Toolkit.getDefaultToolkit();
        } catch (Throwable ignored) {
        }
    }

    private static List<Webcam> discoverWebcams() {
        List<Webcam> out = new ArrayList<>();
        try {
            out.addAll(Webcam.getWebcams(10_000));
        } catch (TimeoutException | WebcamException ignored) {
        }
        if (out.isEmpty()) {
            try {
                out.addAll(Webcam.getWebcams());
            } catch (WebcamException ignored) {
            }
        }
        Webcam def = Webcam.getDefault();
        if (def != null && out.stream().noneMatch(w -> Objects.equals(w.getName(), def.getName()))) {
            out.add(0, def);
        }
        return out;
    }

    /**
     * Prefer 640×480 or 1280×720 when advertised; otherwise the largest mode at or below 720p.
     * Small modes (e.g. 176×144) make DNN phone detection unreliable.
     */
    private static Dimension pickViewSize(Webcam w) {
        Dimension[] sizes = w.getViewSizes();
        if (sizes == null || sizes.length == 0) {
            return null;
        }
        Dimension vga = WebcamResolution.VGA.getSize();
        for (Dimension d : sizes) {
            if (d.width == vga.width && d.height == vga.height) {
                return d;
            }
        }
        Dimension hd = WebcamResolution.HD.getSize();
        for (Dimension d : sizes) {
            if (d.width == hd.width && d.height == hd.height) {
                return d;
            }
        }
        Dimension best = null;
        int bestArea = -1;
        for (Dimension d : sizes) {
            if (d.width > 0 && d.height > 0 && d.width <= 1280 && d.height <= 720) {
                int area = d.width * d.height;
                if (area > bestArea) {
                    bestArea = area;
                    best = d;
                }
            }
        }
        if (best != null) {
            return best;
        }
        for (Dimension d : sizes) {
            int area = d.width * d.height;
            if (area > bestArea) {
                bestArea = area;
                best = d;
            }
        }
        return best;
    }

    private void openSarxosOnEdt(Webcam w) throws Exception {
        if (w.isOpen()) {
            w.close();
        }
        Dimension sz = pickViewSize(w);
        if (sz != null) {
            w.setViewSize(sz);
        }
        w.open();
    }

    private boolean tryOpenSarxosWebcam() {
        List<Webcam> cams = discoverWebcams();
        for (Webcam w : cams) {
            boolean openedHere = false;
            try {
                if (!GraphicsEnvironment.isHeadless()) {
                    final boolean[] ok = {false};
                    SwingUtilities.invokeAndWait(() -> {
                        try {
                            openSarxosOnEdt(w);
                            ok[0] = true;
                        } catch (Exception e) {
                            ok[0] = false;
                        }
                    });
                    openedHere = ok[0];
                } else {
                    try {
                        openSarxosOnEdt(w);
                        openedHere = true;
                    } catch (Exception e) {
                        openedHere = false;
                    }
                }
                if (!openedHere) {
                    continue;
                }
                Thread.sleep(450);
                BufferedImage probe = w.getImage();
                if (probe != null && probe.getWidth() > 0 && probe.getHeight() > 0) {
                    this.sarxosWebcam = w;
                    return true;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ignored) {
            }
            closeSarxosQuiet(w);
        }
        return false;
    }

    private static void closeSarxosQuiet(Webcam w) {
        try {
            if (!GraphicsEnvironment.isHeadless()) {
                SwingUtilities.invokeAndWait(() -> {
                    if (w.isOpen()) {
                        w.close();
                    }
                });
            } else if (w.isOpen()) {
                w.close();
            }
        } catch (Exception ignored) {
        }
    }

    private boolean tryOpenCvCapture() {
        int[] apis = new int[]{Videoio.CAP_DSHOW, Videoio.CAP_MSMF, 0};
        for (int index = 0; index < 3; index++) {
            for (int api : apis) {
                VideoCapture cap = new VideoCapture(index, api);
                if (!cap.isOpened()) {
                    cap.release();
                    continue;
                }
                cap.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
                cap.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
                Mat test = new Mat();
                try {
                    if (cap.read(test) && !test.empty() && test.rows() > 2 && test.cols() > 2) {
                        this.openCvCapture = cap;
                        return true;
                    }
                } finally {
                    test.release();
                }
                cap.release();
            }
        }
        return false;
    }

    private void captureLoop() {
        final long captureStartedAt = System.currentTimeMillis();
        while (running.get() && !triggered.get()) {
            long loopStart = System.currentTimeMillis();
            BufferedImage raw = grabFrame();
            if (raw == null) {
                consecutiveNullFrames++;
                consecutiveDarkFrames = 0;
                if (consecutiveNullFrames >= 4) {
                    notifyFraude("CAMERA_BLOCKED",
                            "The camera feed stopped (disconnected, off, covered, or in use by another app).");
                    break;
                }
                if (!sleepCaptureRemainder(loopStart)) {
                    break;
                }
                continue;
            }
            consecutiveNullFrames = 0;
            if (System.currentTimeMillis() - captureStartedAt >= BLOCKED_LUMA_GRACE_AFTER_CAPTURE_START_MS
                    && isFrameNearlyBlack(raw)) {
                consecutiveDarkFrames++;
                if (consecutiveDarkFrames >= 2) {
                    notifyFraude("CAMERA_BLOCKED",
                            "The camera image is nearly black — the lens may be covered or blocked.");
                    break;
                }
            } else {
                consecutiveDarkFrames = 0;
            }
            BufferedImage forVision = ensureRgbCopy(raw);
            if (openCvMonitor != null && forVision != null) {
                openCvMonitor.offerFrame(forVision);
            }
            if (webcamPreview != null && forVision != null) {
                BufferedImage snap = ensureRgbCopy(forVision);
                Platform.runLater(() -> {
                    if (!running.get() || triggered.get() || webcamPreview == null || snap == null) {
                        return;
                    }
                    webcamPreview.setImage(SwingFXUtils.toFXImage(snap, null));
                });
            }
            if (!sleepCaptureRemainder(loopStart)) {
                break;
            }
        }
    }

    /** @return false if sleep was interrupted */
    private boolean sleepCaptureRemainder(long loopStartMs) {
        long elapsed = System.currentTimeMillis() - loopStartMs;
        long nap = CAPTURE_INTERVAL_MS - elapsed;
        if (nap > 0) {
            try {
                Thread.sleep(nap);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return true;
    }

    private BufferedImage grabFrame() {
        if (openCvCapture != null) {
            Mat m = new Mat();
            try {
                if (openCvCapture.read(m) && !m.empty()) {
                    return matBgrToBufferedRgb(m);
                }
            } finally {
                m.release();
            }
            return null;
        }
        if (sarxosWebcam != null) {
            try {
                return sarxosWebcam.getImage();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static BufferedImage matBgrToBufferedRgb(Mat bgr) {
        int w = bgr.cols();
        int h = bgr.rows();
        int ch = bgr.channels();
        if (ch < 3) {
            return null;
        }
        int sz = (int) (bgr.total() * bgr.elemSize());
        byte[] data = new byte[sz];
        bgr.get(0, 0, data);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = (y * w + x) * ch;
                int blue = data[i] & 0xff;
                int green = data[i + 1] & 0xff;
                int red = data[i + 2] & 0xff;
                pixels[y * w + x] = (red << 16) | (green << 8) | blue;
            }
        }
        img.setRGB(0, 0, w, h, pixels, 0, w);
        return img;
    }

    /**
     * Fast check: covered lens / capped camera usually yields very low brightness across the frame.
     */
    private static boolean isFrameNearlyBlack(BufferedImage rgb) {
        int w = rgb.getWidth();
        int h = rgb.getHeight();
        if (w < 2 || h < 2) {
            return true;
        }
        int stepX = Math.max(1, w / 40);
        int stepY = Math.max(1, h / 40);
        long sum = 0;
        int n = 0;
        for (int y = 0; y < h; y += stepY) {
            for (int x = 0; x < w; x += stepX) {
                int p = rgb.getRGB(x, y);
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;
                int yL = (r * 30 + g * 59 + b * 11) / 100;
                sum += yL;
                n++;
            }
        }
        if (n == 0) {
            return true;
        }
        return (sum / n) <= BLOCKED_CAMERA_MAX_MEAN_LUMA;
    }

    private static BufferedImage ensureRgbCopy(BufferedImage src) {
        if (src == null) {
            return null;
        }
        BufferedImage c = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = c.createGraphics();
        try {
            g.drawImage(src, 0, 0, null);
        } finally {
            g.dispose();
        }
        return c;
    }

    public void stop() {
        running.set(false);
        if (openCvMonitor != null) {
            openCvMonitor.close();
            openCvMonitor = null;
        }
        if (captureExecutor != null) {
            captureExecutor.shutdownNow();
            try {
                captureExecutor.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            captureExecutor = null;
        }
        releaseCapture();
        removeStageAntiCheatListeners();
        Platform.runLater(() -> {
            if (webcamPreview != null) {
                webcamPreview.setImage(null);
            }
        });
    }

    private void releaseCapture() {
        if (openCvCapture != null) {
            openCvCapture.release();
            openCvCapture = null;
        }
        if (sarxosWebcam != null) {
            try {
                if (sarxosWebcam.isOpen()) {
                    sarxosWebcam.close();
                }
            } catch (Exception ignored) {
            }
            sarxosWebcam = null;
        }
    }

    private void removeStageAntiCheatListeners() {
        Stage s = stage;
        if (s == null) {
            return;
        }
        if (stageFocusListener != null) {
            s.focusedProperty().removeListener(stageFocusListener);
            stageFocusListener = null;
        }
        if (stageIconifiedListener != null) {
            s.iconifiedProperty().removeListener(stageIconifiedListener);
            stageIconifiedListener = null;
        }
        if (stageFullScreenListener != null) {
            s.fullScreenProperty().removeListener(stageFullScreenListener);
            stageFullScreenListener = null;
        }
    }

    /**
     * Vision pipeline: after {@code OpenCvFraudMonitor} confirms several consecutive phone detections
     * (DNN or heuristic), we add suspicion instead of failing the exam immediately. Only when the
     * cumulative score reaches {@link #PHONE_SUSPICION_ESCALATE_THRESHOLD} do we call {@link #notifyFraude}.
     */
    private void onPhoneBurstConfirmed(double maxModelConfidence) {
        synchronized (phonePolicyLock) {
            if (!running.get() || triggered.get()) {
                return;
            }
            long now = System.currentTimeMillis();
            if (now < phoneCooldownUntilMs) {
                return;
            }
            phoneSuspicionScore += PHONE_SUSPICION_DELTA;
            phoneCooldownUntilMs = now + PHONE_SUSPICION_COOLDOWN_MS;
            if (phoneSuspicionScore >= PHONE_SUSPICION_ESCALATE_THRESHOLD) {
                notifyFraude(
                        "PHONE_IN_FRAME",
                        "Escalated after repeated phone detections (suspicion score "
                                + phoneSuspicionScore
                                + ", model confidence up to "
                                + String.format(java.util.Locale.US, "%.2f", maxModelConfidence)
                                + ")."
                );
            }
        }
    }

    private void notifyFraude(String type, String description) {
        Platform.runLater(() -> {
            long now = System.currentTimeMillis();
            if (("WINDOW_FOCUS_LOST".equals(type) || "FULLSCREEN_EXIT".equals(type))
                    && now < ignoreUiFraudUntilMs) {
                return;
            }
            if (!running.get() || !triggered.compareAndSet(false, true)) {
                return;
            }
            stop();
            if (fraudeHandler != null) {
                fraudeHandler.onFraudeDetected(type, description);
            }
        });
    }
}
