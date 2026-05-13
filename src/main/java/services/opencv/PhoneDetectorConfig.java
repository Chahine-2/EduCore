package services.opencv;

/**
 * Tunable parameters for {@link DnnPhoneDetector}. Prefer setting via system properties
 * (see {@link #fromSystemProperties()}) so deployments can adjust without recompiling.
 */
public record PhoneDetectorConfig(
        double minConfidence,
        int dnnInputSize,
        double nmsConfidenceThreshold,
        int minInferenceWidth,
        int minInferenceHeight,
        double maxInferenceUpscale
) {
    public static final double DEFAULT_MIN_CONFIDENCE = 0.65;
    public static final int DEFAULT_DNN_INPUT_SIZE = 300;
    /** Used only when applying optional NMS across multiple raw boxes (SSD usually one per class). */
    public static final double DEFAULT_NMS_CONFIDENCE = 0.45;
    /** If the webcam frame is smaller than this, upscale before DNN (helps tiny 320×240 feeds). */
    public static final int DEFAULT_MIN_INFERENCE_WIDTH = 640;
    public static final int DEFAULT_MIN_INFERENCE_HEIGHT = 480;
    /** Cap upscale factor so 160×120 does not become enormous. */
    public static final double DEFAULT_MAX_INFERENCE_UPSCALE = 3.0;

    public PhoneDetectorConfig {
        if (minConfidence < 0.05 || minConfidence > 0.99) {
            throw new IllegalArgumentException("minConfidence out of range");
        }
        if (dnnInputSize < 128 || dnnInputSize > 512) {
            throw new IllegalArgumentException("dnnInputSize out of range");
        }
        if (minInferenceWidth < 160 || minInferenceHeight < 120) {
            throw new IllegalArgumentException("min inference dimensions out of range");
        }
        if (maxInferenceUpscale < 1.0 || maxInferenceUpscale > 5.0) {
            throw new IllegalArgumentException("maxInferenceUpscale out of range");
        }
    }

    public static PhoneDetectorConfig defaultConfig() {
        return new PhoneDetectorConfig(
                DEFAULT_MIN_CONFIDENCE,
                DEFAULT_DNN_INPUT_SIZE,
                DEFAULT_NMS_CONFIDENCE,
                DEFAULT_MIN_INFERENCE_WIDTH,
                DEFAULT_MIN_INFERENCE_HEIGHT,
                DEFAULT_MAX_INFERENCE_UPSCALE
        );
    }

    /**
     * Optional overrides:
     * <ul>
     *   <li>{@code educore.phone.minConfidence} — default 0.65</li>
     *   <li>{@code educore.phone.dnnInputSize} — default 300 (MobileNet-SSD)</li>
     *   <li>{@code educore.dnn.minInferWidth} / {@code educore.dnn.minInferHeight} — upscale threshold (default 640×480)</li>
     *   <li>{@code educore.dnn.maxUpscale} — default 3.0</li>
     * </ul>
     */
    public static PhoneDetectorConfig fromSystemProperties() {
        double conf = DEFAULT_MIN_CONFIDENCE;
        int size = DEFAULT_DNN_INPUT_SIZE;
        int minW = DEFAULT_MIN_INFERENCE_WIDTH;
        int minH = DEFAULT_MIN_INFERENCE_HEIGHT;
        double maxUp = DEFAULT_MAX_INFERENCE_UPSCALE;
        try {
            String c = System.getProperty("educore.phone.minConfidence");
            if (c != null && !c.isBlank()) {
                conf = Double.parseDouble(c.trim());
            }
        } catch (NumberFormatException ignored) {
        }
        try {
            String s = System.getProperty("educore.phone.dnnInputSize");
            if (s != null && !s.isBlank()) {
                size = Integer.parseInt(s.trim());
            }
        } catch (NumberFormatException ignored) {
        }
        try {
            String s = System.getProperty("educore.dnn.minInferWidth");
            if (s != null && !s.isBlank()) {
                minW = Integer.parseInt(s.trim());
            }
        } catch (NumberFormatException ignored) {
        }
        try {
            String s = System.getProperty("educore.dnn.minInferHeight");
            if (s != null && !s.isBlank()) {
                minH = Integer.parseInt(s.trim());
            }
        } catch (NumberFormatException ignored) {
        }
        try {
            String s = System.getProperty("educore.dnn.maxUpscale");
            if (s != null && !s.isBlank()) {
                maxUp = Double.parseDouble(s.trim());
            }
        } catch (NumberFormatException ignored) {
        }
        return new PhoneDetectorConfig(conf, size, DEFAULT_NMS_CONFIDENCE, minW, minH, maxUp);
    }
}
