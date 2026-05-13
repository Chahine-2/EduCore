package services.opencv;

import org.opencv.core.Rect;

/**
 * One raw SSD detection for a COCO {@code cell phone} class. Bounding box is in
 * <strong>source image pixel coordinates</strong> (same as the {@link org.opencv.core.Mat} passed to
 * {@link DnnPhoneDetector#detectPhones(org.opencv.core.Mat, org.opencv.core.Rect[])}).
 */
public record PhoneDetection(Rect bounds, double confidence, int classId) {
}
