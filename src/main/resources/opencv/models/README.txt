EDUCORE DNN phone detection (optional)
======================================

Place MobileNet-SSD TensorFlow COCO files here to enable OpenCV DNN phone detection:

  ssd_mobilenet_v1_coco.pb
  ssd_mobilenet_v1_coco.pbtxt

Or set absolute paths (no bundling):

  -Deducore.dnn.pb=C:/path/to/frozen_inference_graph.pb
  -Deducore.dnn.pbtxt=C:/path/to/ssd_mobilenet_v1_coco_2017_11_17.pbtxt

Or environment variables:

  EDUCORE_DNN_PB
  EDUCORE_DNN_PBTXT

Official OpenCV extra includes a compatible pbtxt; the TensorFlow detection model zoo ships
the frozen graph. Search for: "ssd_mobilenet_v1_coco" + OpenCV dnn tutorial.

Tuning (JVM system properties):
--------------------------------
  educore.phone.minConfidence      default 0.65
  educore.phone.dnnInputSize       default 300
  educore.dnn.minInferWidth        default 640 (upscale if frame narrower)
  educore.dnn.minInferHeight       default 480 (upscale if frame shorter)
  educore.dnn.maxUpscale           default 3.0 (cap when webcam is tiny)
  educore.phone.dnnEveryNFrames    default 3
  educore.phone.maxFaceIoU         default 0.22
  educore.phone.maxOverlapFracOnFace default 0.38
  educore.phone.suspicionDelta     default 3 (score added per burst)
  educore.phone.suspicionThreshold default 9 (escalate to PHONE_IN_FRAME)
  educore.phone.cooldownMs         default 5000

If model files are missing, the app falls back to the lightweight Canny/heuristic detector.
