"""Test DeepFace pipeline timing."""
import cv2, numpy as np, base64, json, subprocess, sys, time, os

script = os.path.join(os.path.dirname(__file__), 'face_recognition_service.py')

# Create a realistic face image
img = np.ones((480, 640, 3), dtype=np.uint8) * 180
cv2.ellipse(img, (320, 240), (120, 150), 0, 0, 360, (200, 170, 140), -1)
cv2.circle(img, (280, 210), 20, (60, 40, 20), -1)
cv2.circle(img, (360, 210), 20, (60, 40, 20), -1)
cv2.ellipse(img, (320, 290), (40, 15), 0, 0, 180, (120, 80, 70), -1)
_, buf = cv2.imencode('.jpg', img, [cv2.IMWRITE_JPEG_QUALITY, 90])
b64 = base64.b64encode(buf.tobytes()).decode()

req = json.dumps({"command": "encode", "image": b64})
print(f"Sending request ({len(req)} bytes)...")
t0 = time.time()
r = subprocess.run([sys.executable, script], input=req, capture_output=True, text=True, timeout=180)
elapsed = time.time() - t0
print(f"Time: {elapsed:.1f}s")
print(f"STDOUT: {r.stdout[:400]}")
if r.stderr:
    print(f"STDERR (last 400): {r.stderr[-400:]}")
