package services.opencv;

/**
 * Loads OpenCV native libraries once (org.openpnp:opencv bundles platform DLLs via nu.pattern.OpenCV).
 */
public final class OpenCvLoader {
    private static volatile boolean loaded;
    private static volatile Throwable loadError;

    private OpenCvLoader() {
    }

    public static boolean ensureLoaded() {
        if (loaded) {
            return true;
        }
        if (loadError != null) {
            return false;
        }
        synchronized (OpenCvLoader.class) {
            if (loaded) {
                return true;
            }
            if (loadError != null) {
                return false;
            }
            try {
                nu.pattern.OpenCV.loadLocally();
                loaded = true;
                return true;
            } catch (Throwable t) {
                loadError = t;
                return false;
            }
        }
    }

    public static Throwable getLoadError() {
        return loadError;
    }
}
