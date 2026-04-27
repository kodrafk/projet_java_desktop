#!/usr/bin/env python3
"""
Download the face recognition models needed for NutriLife Face ID.
- face_recognition_model (128-D FaceNet descriptor, same as face-api.js)
- SSD face detector (already downloaded)
"""
import os, sys, urllib.request, hashlib

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
MODELS_DIR = os.path.join(SCRIPT_DIR, "face_models")
os.makedirs(MODELS_DIR, exist_ok=True)

def download(url, path, name, min_size=1000):
    if os.path.exists(path) and os.path.getsize(path) > min_size:
        print(f"  ✅ {name} already present ({os.path.getsize(path):,} bytes)")
        return True
    print(f"  ⬇️  Downloading {name}...")
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req, timeout=60) as r, open(path, "wb") as f:
            data = r.read()
            f.write(data)
        print(f"  ✅ {name} downloaded ({os.path.getsize(path):,} bytes)")
        return True
    except Exception as e:
        print(f"  ❌ Failed: {e}")
        if os.path.exists(path): os.remove(path)
        return False

print("=" * 60)
print("Downloading Face Recognition Models")
print("=" * 60)

models = [
    # OpenCV SSD face detector
    (
        "https://raw.githubusercontent.com/opencv/opencv/master/samples/dnn/face_detector/deploy.prototxt",
        os.path.join(MODELS_DIR, "deploy.prototxt"),
        "SSD face detector config",
        1000
    ),
    (
        "https://github.com/opencv/opencv_3rdparty/raw/dnn_samples_face_detector_20170830/res10_300x300_ssd_iter_140000.caffemodel",
        os.path.join(MODELS_DIR, "res10_300x300_ssd_iter_140000.caffemodel"),
        "SSD face detector weights",
        1000000
    ),
    # face-api.js compatible FaceNet model (ONNX format)
    # This is the exact same model used by face-api.js for 128-D descriptors
    (
        "https://github.com/justadudewhohacks/face-api.js/raw/master/weights/face_recognition_model-weights_manifest.json",
        os.path.join(MODELS_DIR, "face_recognition_manifest.json"),
        "FaceNet manifest",
        100
    ),
]

for url, path, name, min_size in models:
    download(url, path, name, min_size)

# Download the actual FaceNet ONNX model
# Using the MobileFaceNet ONNX from a reliable source
facenet_path = os.path.join(MODELS_DIR, "facenet128.onnx")
facenet_urls = [
    "https://github.com/onnx/models/raw/main/validated/vision/body_analysis/arcface/model/arcfaceresnet100-8.onnx",
    "https://huggingface.co/qualcomm/MobileNet-Face-Identification/resolve/main/MobileNet-Face-Identification.onnx",
]

print("\nChecking FaceNet ONNX model...")
if os.path.exists(facenet_path) and os.path.getsize(facenet_path) > 100000:
    print(f"  ✅ FaceNet ONNX already present ({os.path.getsize(facenet_path):,} bytes)")
else:
    downloaded = False
    for url in facenet_urls:
        if download(url, facenet_path, "FaceNet ONNX", 100000):
            downloaded = True
            break
    if not downloaded:
        print("  ⚠️  FaceNet ONNX not available — will use enhanced OpenCV features")

print("\nDone!")
