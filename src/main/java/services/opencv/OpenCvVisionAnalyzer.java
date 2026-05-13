package services.opencv;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Haar frontal-face detection on downscaled gray (equalized) for exam anti-cheat.
 */
public final class OpenCvVisionAnalyzer {

    static final int ANALYSIS_WIDTH = 320;
    static final int ANALYSIS_HEIGHT = 240;

    private final CascadeClassifier faceCascade;

    public OpenCvVisionAnalyzer() throws Exception {
        this.faceCascade = new CascadeClassifier();
        Path cascadeFile = extractCascadeResource("/opencv/haarcascade_frontalface_default.xml");
        if (!faceCascade.load(cascadeFile.toAbsolutePath().toString())) {
            throw new IllegalStateException("Failed to load Haar cascade from " + cascadeFile);
        }
    }

    private static Path extractCascadeResource(String classpathLocation) throws Exception {
        try (InputStream in = OpenCvVisionAnalyzer.class.getResourceAsStream(classpathLocation)) {
            if (in == null) {
                throw new IllegalStateException("Missing resource: " + classpathLocation);
            }
            Path tmp = Files.createTempFile("haarcascade_face", ".xml");
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            tmp.toFile().deleteOnExit();
            return tmp;
        }
    }

    public VisionFrameResult analyze(BufferedImage rgbCopy) {
        Mat bgr = bgrMatFromBufferedImageScaled(rgbCopy, ANALYSIS_WIDTH, ANALYSIS_HEIGHT);
        Mat grayEq = new Mat();
        try {
            Mat gray = new Mat();
            Imgproc.cvtColor(bgr, gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(gray, grayEq);
            gray.release();

            MatOfRect faces = new MatOfRect();
            /* Slightly coarser pyramid = faster; still fine at 320×240. */
            faceCascade.detectMultiScale(
                    grayEq,
                    faces,
                    1.12,
                    3,
                    0,
                    new Size(24, 24),
                    new Size()
            );
            Rect[] faceRects = faces.toArray();
            faces.release();
            return new VisionFrameResult(faceRects.length, faceRects, grayEq, bgr);
        } catch (RuntimeException e) {
            grayEq.release();
            bgr.release();
            throw e;
        }
    }

    public void resetState() {
    }

    /** Full-res BGR mat (avoid for HD frames; prefer {@link #bgrMatFromBufferedImageScaled}). */
    public static Mat bgrMatFromBufferedImage(BufferedImage bi) {
        return bufferedImageToBgr(bi);
    }

    /**
     * Bilinear downscale then BGR mat — matches analysis size (fast phone heuristic + no extra resize).
     */
    public static Mat bgrMatFromBufferedImageScaled(BufferedImage bi, int targetW, int targetH) {
        if (bi.getWidth() == targetW && bi.getHeight() == targetH) {
            return bufferedImageToBgr(bi);
        }
        BufferedImage scaled = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(bi, 0, 0, targetW, targetH, null);
        } finally {
            g.dispose();
        }
        return bufferedImageToBgr(scaled);
    }

    private static Mat bufferedImageToBgr(BufferedImage bi) {
        int w = bi.getWidth();
        int h = bi.getHeight();
        Mat mat = new Mat(h, w, CvType.CV_8UC3);
        int[] argb = new int[w * h];
        bi.getRGB(0, 0, w, h, argb, 0, w);
        byte[] bgr = new byte[w * h * 3];
        int i = 0;
        for (int pixel : argb) {
            int b = pixel & 0xff;
            int g = (pixel >> 8) & 0xff;
            int r = (pixel >> 16) & 0xff;
            bgr[i++] = (byte) b;
            bgr[i++] = (byte) g;
            bgr[i++] = (byte) r;
        }
        mat.put(0, 0, bgr);
        return mat;
    }

    public void close() {
        resetState();
    }

    public record VisionFrameResult(int faceCount, Rect[] faceRects, Mat grayEqualizedSmall, Mat bgrSmall) {
    }

    /**
     * Maps Haar rectangles from {@link #ANALYSIS_WIDTH}×{@link #ANALYSIS_HEIGHT} space to full webcam frame pixels.
     */
    public static Rect[] scaleFaceRectsToFrame(Rect[] analysisRects, int frameW, int frameH) {
        if (analysisRects == null || analysisRects.length == 0) {
            return new Rect[0];
        }
        double sx = frameW / (double) ANALYSIS_WIDTH;
        double sy = frameH / (double) ANALYSIS_HEIGHT;
        Rect[] out = new Rect[analysisRects.length];
        for (int i = 0; i < analysisRects.length; i++) {
            Rect r = analysisRects[i];
            int x = (int) Math.round(r.x * sx);
            int y = (int) Math.round(r.y * sy);
            int w = (int) Math.round(r.width * sx);
            int h = (int) Math.round(r.height * sy);
            out[i] = clampRect(new Rect(x, y, w, h), frameW, frameH);
        }
        return out;
    }

    /** Expand rectangle about its center (for “heavily overlaps face” tests). */
    public static Rect expandRectProportional(Rect r, double scale, int maxW, int maxH) {
        if (r.width <= 0 || r.height <= 0) {
            return r;
        }
        int nw = (int) Math.round(r.width * scale);
        int nh = (int) Math.round(r.height * scale);
        nw = Math.max(1, nw);
        nh = Math.max(1, nh);
        int cx = r.x + r.width / 2;
        int cy = r.y + r.height / 2;
        int nx = cx - nw / 2;
        int ny = cy - nh / 2;
        return clampRect(new Rect(nx, ny, nw, nh), maxW, maxH);
    }

    public static Rect clampRect(Rect r, int maxW, int maxH) {
        int x = Math.max(0, Math.min(r.x, Math.max(0, maxW - 1)));
        int y = Math.max(0, Math.min(r.y, Math.max(0, maxH - 1)));
        int w = Math.max(1, Math.min(r.width, maxW - x));
        int h = Math.max(1, Math.min(r.height, maxH - y));
        return new Rect(x, y, w, h);
    }

    public static double intersectionOverUnion(Rect a, Rect b) {
        int x1 = Math.max(a.x, b.x);
        int y1 = Math.max(a.y, b.y);
        int x2 = Math.min(a.x + a.width, b.x + b.width);
        int y2 = Math.min(a.y + a.height, b.y + b.height);
        int w = x2 - x1;
        int h = y2 - y1;
        if (w <= 0 || h <= 0) {
            return 0;
        }
        int inter = w * h;
        int uni = a.width * a.height + b.width * b.height - inter;
        return uni <= 0 ? 0 : inter / (double) uni;
    }

    public static double intersectionAreaPx(Rect a, Rect b) {
        int x1 = Math.max(a.x, b.x);
        int y1 = Math.max(a.y, b.y);
        int x2 = Math.min(a.x + a.width, b.x + b.width);
        int y2 = Math.min(a.y + a.height, b.y + b.height);
        int w = x2 - x1;
        int h = y2 - y1;
        if (w <= 0 || h <= 0) {
            return 0;
        }
        return w * (double) h;
    }
}
