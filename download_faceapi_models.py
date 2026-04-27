#!/usr/bin/env python3
"""Download face-api.js model weights for NutriLife Face ID."""
import urllib.request, os, sys

out_dir = os.path.join(os.path.dirname(__file__), "src", "main", "resources", "faceapi", "models")
os.makedirs(out_dir, exist_ok=True)

base = "https://github.com/justadudewhohacks/face-api.js/raw/master/weights"
files = [
    ("ssd_mobilenetv1_model-weights_manifest.json", 1000),
    ("ssd_mobilenetv1_model-shard1", 100000),
    ("face_landmark_68_model-weights_manifest.json", 1000),
    ("face_landmark_68_model-shard1", 100000),
    ("face_recognition_model-weights_manifest.json", 1000),
    ("face_recognition_model-shard1", 100000),
    ("face_recognition_model-shard2", 100000),
]

print("Downloading face-api.js models...")
for fname, min_size in files:
    path = os.path.join(out_dir, fname)
    if os.path.exists(path) and os.path.getsize(path) > min_size:
        print(f"  SKIP {fname} ({os.path.getsize(path):,} bytes)")
        continue
    print(f"  DL   {fname} ...", end=" ", flush=True)
    try:
        req = urllib.request.Request(f"{base}/{fname}", headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req, timeout=120) as r, open(path, "wb") as f:
            f.write(r.read())
        print(f"OK ({os.path.getsize(path):,} bytes)")
    except Exception as e:
        print(f"FAIL: {e}")

print("\nAll done!")
print("Files in models dir:")
for f in os.listdir(out_dir):
    print(f"  {f}: {os.path.getsize(os.path.join(out_dir, f)):,} bytes")
