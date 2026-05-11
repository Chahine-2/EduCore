package services.opencv;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * MobileNet-SSD (TensorFlow) COCO — detects {@code cell phone} only.
 * <p>
 * This class performs inference and geometric filtering only. It does <strong>not</strong> decide
 * fraud, suspicion, or cooldowns; that belongs in {@link services.FraudeService} /
 * {@link OpenCvFraudMonitor}.
 * <p>
 * Model files are not bundled (large). Resolution order:
 * <ol>
 *   <li>{@code System.getProperty("educore.dnn.pb")} and {@code educore.dnn.pbtxt}</li>
 *   <li>Environment {@code EDUCORE_DNN_PB} and {@code EDUCORE_DNN_PBTXT}</li>
 *   <li>Classpath {@code /opencv/models/ssd_mobilenet_v1_coco.pb} and {@code .pbtxt} if packaged</li>
 * </ol>
 * See {@code src/main/resources/opencv/models/README.txt} for download links.
 */
public final class DnnPhoneDetector implements AutoCloseable {

    /** MS COCO category id for "cell phone" (1-based in label map used by TF SSD COCO). */
    public static final int COCO_CELL_PHONE_CLASS_ID = 77;

    private final Net net;
    private final PhoneDetectorConfig config;

    private DnnPhoneDetector(Net net, PhoneDetectorConfig config) {
        this.net = net;
        this.config = config;
    }

    /**
     * Attempts to load the network from disk / classpath. Returns {@code null} if files are missing
     * or OpenCV cannot parse the model (caller keeps heuristic fallback).
     */
    public static DnnPhoneDetector tryLoad(PhoneDetectorConfig config) {
        Path pb = resolvePb();
        Path pbtxt = resolvePbtxt();
        if (pb == null || pbtxt == null || !Files.isRegularFile(pb) || !Files.isRegularFile(pbtxt)) {
            return null;
        }
        try {
            Net n = Dnn.readNetFromTensorflow(pb.toAbsolutePath().toString(), pbtxt.toAbsolutePath().toString());
            if (n.empty()) {
                return null;
            }
            return new DnnPhoneDetector(n, config);
        } catch (Throwable t) {
            return null;
        }
    }

    private static Path resolvePb() {
        Path p = firstPath(
                propPath("educore.dnn.pb"),
                envPath("EDUCORE_DNN_PB"),
                extractResourceIfPresent("/opencv/models/ssd_mobilenet_v1_coco.pb", ".pb")
        );
        return p;
    }

    private static Path resolvePbtxt() {
        return firstPath(
                propPath("educore.dnn.pbtxt"),
                envPath("EDUCORE_DNN_PBTXT"),
                extractResourceIfPresent("/opencv/models/ssd_mobilenet_v1_coco.pbtxt", ".pbtxt")
        );
    }

    private static Path firstPath(Path... paths) {
        if (paths == null) {
            return null;
        }
        for (Path p : paths) {
            if (p != null) {
                return p;
            }
        }
        return null;
    }

    private static Path propPath(String key) {
        String v = System.getProperty(key);
        if (v == null || v.isBlank()) {
            return null;
        }
        return Paths.get(v.trim());
    }

    private static Path envPath(String key) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            return null;
        }
        return Paths.get(v.trim());
    }

    private static Path extractResourceIfPresent(String classpath, String suffix) {
        try (InputStream in = DnnPhoneDetector.class.getResourceAsStream(classpath)) {
            if (in == null) {
                return null;
            }
            Path tmp = Files.createTempFile("educore_dnn_", suffix);
            Files.copy(in, tmp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            tmp.toFile().deleteOnExit();
            return tmp;
        } catch (Exception e) {
            return null;
        }
    }

    public PhoneDetectorConfig config() {
        return config;
    }

    /**
     * Runs SSD forward pass and returns phone boxes above {@link PhoneDetectorConfig#minConfidence()},
     * in pixel coords of {@code bgrFrame}.
     * <p>
     * Low-resolution webcams (e.g. 320×240) yield tiny objects after internal 300×300 resize; we
     * bilinearly upscale before inference when below {@link PhoneDetectorConfig#minInferenceWidth()} /
     * {@link PhoneDetectorConfig#minInferenceHeight()} and map boxes back to the original frame.
     */
    public List<PhoneDetection> detectPhones(Mat bgrFrame) {
        List<PhoneDetection> out = new ArrayList<>();
        if (bgrFrame == null || bgrFrame.empty() || bgrFrame.channels() < 3) {
            return out;
        }
        int w = bgrFrame.cols();
        int h = bgrFrame.rows();
        if (w < 32 || h < 32) {
            return out;
        }

        int minW = config.minInferenceWidth();
        int minH = config.minInferenceHeight();
        Mat inferMat = bgrFrame;
        boolean ownInfer = false;
        double sx = 1.0;
        double sy = 1.0;
        if (w < minW || h < minH) {
            double scaleW = minW / (double) w;
            double scaleH = minH / (double) h;
            double sc = Math.min(Math.max(scaleW, scaleH), config.maxInferenceUpscale());
            int nw = Math.max(minW, (int) Math.round(w * sc));
            int nh = Math.max(minH, (int) Math.round(h * sc));
            inferMat = new Mat();
            Imgproc.resize(bgrFrame, inferMat, new Size(nw, nh), 0, 0, Imgproc.INTER_LINEAR);
            ownInfer = true;
            sx = w / (double) nw;
            sy = h / (double) nh;
        }

        int iw = inferMat.cols();
        int ih = inferMat.rows();
        int d = config.dnnInputSize();
        Mat blob = Dnn.blobFromImage(
                inferMat,
                1.0 / 127.5,
                new Size(d, d),
                new Scalar(127.5, 127.5, 127.5),
                true,
                false
        );
        try {
            net.setInput(blob);
            Mat detections = net.forward();
            try {
                parseDetections(detections, iw, ih, out);
            } finally {
                detections.release();
            }
        } finally {
            blob.release();
            if (ownInfer) {
                inferMat.release();
            }
        }

        if (sx != 1.0 || sy != 1.0) {
            for (int i = 0; i < out.size(); i++) {
                PhoneDetection p = out.get(i);
                Rect r = p.bounds();
                int x = clamp((int) Math.round(r.x * sx), 0, w - 1);
                int y = clamp((int) Math.round(r.y * sy), 0, h - 1);
                int rw = Math.max(1, (int) Math.round(r.width * sx));
                int rh = Math.max(1, (int) Math.round(r.height * sy));
                rw = Math.min(rw, w - x);
                rh = Math.min(rh, h - y);
                out.set(i, new PhoneDetection(new Rect(x, y, rw, rh), p.confidence(), p.classId()));
            }
        }
        return out;
    }

    private void parseDetections(Mat detections, int frameW, int frameH, List<PhoneDetection> out) {
        if (detections == null || detections.empty()) {
            return;
        }
        long total = detections.total();
        if (total < 7 || total % 7 != 0) {
            return;
        }
        int n = (int) (total / 7);
        float[] buf = new float[(int) total];
        /* Reshape is a view; only {@code detections} is released by the caller. */
        Mat rowsMat = detections.reshape(1, n);
        rowsMat.get(0, 0, buf);
        double minConf = config.minConfidence();
        for (int i = 0; i < n; i++) {
            int o = i * 7;
            float classId = buf[o + 1];
            float score = buf[o + 2];
            if (score < minConf) {
                continue;
            }
            if (Math.round(classId) != COCO_CELL_PHONE_CLASS_ID) {
                continue;
            }
            float x1 = buf[o + 3] * frameW;
            float y1 = buf[o + 4] * frameH;
            float x2 = buf[o + 5] * frameW;
            float y2 = buf[o + 6] * frameH;
            int ix1 = clamp(Math.round(x1), 0, frameW - 1);
            int iy1 = clamp(Math.round(y1), 0, frameH - 1);
            int ix2 = clamp(Math.round(x2), 0, frameW - 1);
            int iy2 = clamp(Math.round(y2), 0, frameH - 1);
            int rw = Math.max(1, ix2 - ix1);
            int rh = Math.max(1, iy2 - iy1);
            out.add(new PhoneDetection(new Rect(ix1, iy1, rw, rh), score, COCO_CELL_PHONE_CLASS_ID));
        }
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    /**
     * Java OpenCV {@link Net} does not expose {@code release()}; clearing references is sufficient
     * for desktop exam sessions. Native teardown is tied to the process.
     */
    @Override
    public void close() {
    }
}
