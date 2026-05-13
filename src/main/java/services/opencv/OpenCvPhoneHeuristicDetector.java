package services.opencv;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Heuristic “phone in frame”: Canny contours that look like a portrait slab
 * <strong>mostly outside</strong> the Haar face box (beside the head). Overlap-based paths and
 * lateral Sobel cues were removed — they tracked face/background structure and caused “face = phone”.
 */
public final class OpenCvPhoneHeuristicDetector {

    private static final Size SMALL = new Size(OpenCvVisionAnalyzer.ANALYSIS_WIDTH, OpenCvVisionAnalyzer.ANALYSIS_HEIGHT);

    private OpenCvPhoneHeuristicDetector() {
    }

    public static boolean phoneLikeObjectNearFace(Mat bgrAnySize, int faceCount, Rect[] faces) {
        if (faceCount < 1 || faces == null || faces.length < 1 || bgrAnySize == null || bgrAnySize.empty()) {
            return false;
        }
        Mat work = bgrAnySize;
        boolean ownsResize = false;
        if (bgrAnySize.cols() != SMALL.width || bgrAnySize.rows() != SMALL.height) {
            work = new Mat();
            Imgproc.resize(bgrAnySize, work, SMALL);
            ownsResize = true;
        }
        try {
            Rect face = unionFaces(faces);
            if (face.width <= 0 || face.height <= 0) {
                return false;
            }

            Mat gray = new Mat();
            Imgproc.cvtColor(work, gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(gray, gray, new Size(3, 3), 0);
            boolean hit = collectFromEdges(gray, face, 32, 96, 3);
            gray.release();
            return hit;
        } finally {
            if (ownsResize) {
                work.release();
            }
        }
    }

    private static boolean collectFromEdges(Mat gray, Rect face, double c1, double c2, int dilateK) {
        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, c1, c2);
        if (dilateK > 0) {
            Imgproc.dilate(edges, edges, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(dilateK, dilateK)));
        }
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        edges.release();
        hierarchy.release();

        double faceDiag = Math.hypot(face.width, face.height);
        double faceArea = face.width * (double) face.height;
        int fx = face.x + face.width / 2;
        int fy = face.y + face.height / 2;

        try {
            for (MatOfPoint c : contours) {
                double a = Imgproc.contourArea(c);
                if (a < 2_650 || a > 50_000) {
                    continue;
                }
                Rect br = Imgproc.boundingRect(c);
                int bw = br.width;
                int bh = br.height;
                int brArea = Math.max(1, bw * bh);
                int shortSide = Math.min(bw, bh);
                int longSide = Math.max(bw, bh);
                double ar = (double) longSide / Math.max(1, shortSide);

                if (ar < 1.48 || ar > 2.78) {
                    continue;
                }

                int cx = br.x + bw / 2;
                int cy = br.y + bh / 2;
                double iou = intersectionOverUnion(face, br);
                double dist = Math.hypot(cx - fx, cy - fy);
                double interPx = intersectionAreaPx(face, br);
                double fracOnFace = interPx / brArea;
                double sizeRatio = brArea / Math.max(1.0, faceArea);

                if (isLikelyFaceSilhouetteOnly(face, br, iou, fracOnFace, sizeRatio, ar, shortSide)) {
                    continue;
                }

                if (matchesBesideHeadOnly(face, faceDiag, fx, fy, br, bw, bh, ar, a, iou, dist, fracOnFace,
                        interPx, brArea, cx, cy, shortSide)) {
                    return true;
                }
            }
        } finally {
            contours.clear();
        }
        return false;
    }

    /** Single blob that is mostly the Haar face box / oval — not a second object. */
    private static boolean isLikelyFaceSilhouetteOnly(
            Rect face, Rect br, double iou, double fracOnFace, double sizeRatio, double ar, int shortSide
    ) {
        if (fracOnFace > 0.72) {
            return true;
        }
        if (sizeRatio >= 0.30 && sizeRatio <= 1.30 && iou >= 0.28 && ar < 1.62 && shortSide > 0.30 * face.width) {
            return true;
        }
        if (sizeRatio >= 0.38 && sizeRatio <= 1.32 && iou >= 0.22 && fracOnFace > 0.48 && shortSide > 0.30 * face.width) {
            return true;
        }
        if (fracOnFace > 0.50 && iou >= 0.06 && iou <= 0.42 && ar >= 1.35 && ar <= 2.40 && shortSide > 0.24 * face.width) {
            return true;
        }
        return false;
    }

    /**
     * Portrait slab beside the head: lateral offset, low IoU with face, and most of the bbox
     * pixels lie <em>outside</em> the face box (rules out jaw/cheek Canny arcs).
     */
    private static boolean matchesBesideHeadOnly(
            Rect face, double faceDiag, int fx, int fy, Rect br, int bw, int bh, double ar,
            double contourArea, double iou, double dist, double fracOnFace, double interPx, int brArea,
            int cx, int cy, int shortSide
    ) {
        if (fracOnFace > 0.22) {
            return false;
        }
        if (iou > 0.14) {
            return false;
        }
        double outsideFrac = (brArea - interPx) / brArea;
        if (outsideFrac < 0.58) {
            return false;
        }
        if (interPx / Math.max(1.0, rectAreaPx(face)) > 0.11) {
            return false;
        }
        if (dist < 0.34 * faceDiag || dist > 0.76 * faceDiag) {
            return false;
        }
        if (ar < 1.50 || contourArea < 3_000) {
            return false;
        }
        if (shortSide > 0.34 * face.width) {
            return false;
        }
        if (Math.abs(cx - fx) < 0.16 * face.width) {
            return false;
        }
        if (cy < face.y - 0.20 * face.height || cy > face.y + face.height + 0.26 * face.height) {
            return false;
        }
        if (face.contains(new Point(cx, cy)) && shortSide > 0.24 * face.width) {
            return false;
        }
        return true;
    }

    private static double rectAreaPx(Rect r) {
        return r.width * (double) r.height;
    }

    private static Rect unionFaces(Rect[] faces) {
        int x1 = Integer.MAX_VALUE;
        int y1 = Integer.MAX_VALUE;
        int x2 = 0;
        int y2 = 0;
        for (Rect r : faces) {
            if (r.width <= 0 || r.height <= 0) {
                continue;
            }
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

    private static double intersectionOverUnion(Rect a, Rect b) {
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
        return uni <= 0 ? 0 : (double) inter / uni;
    }

    private static double intersectionAreaPx(Rect a, Rect b) {
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
