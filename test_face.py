"""Test face recognition pipeline using stdin/stdout JSON protocol."""
import cv2, numpy as np, base64, json, subprocess, sys, os

SCRIPT = os.path.join(os.path.dirname(__file__), 'face_recognition_service.py')
PY = sys.executable

def run(request_dict):
    r = subprocess.run(
        [PY, SCRIPT],
        input=json.dumps(request_dict),
        capture_output=True, text=True, timeout=30
    )
    if r.stderr:
        for line in r.stderr.strip().split('\n'):
            if line.strip(): print(f"  [{line}]", file=sys.stderr)
    # Find JSON line
    for line in reversed(r.stdout.strip().split('\n')):
        if line.strip().startswith('{'):
            return json.loads(line.strip())
    return {"success": False, "error": "No JSON output: " + r.stdout[:200]}

def make_face(seed=1, brightness=0, noise=3, blur=0):
    np.random.seed(seed)
    img = np.ones((480, 640, 3), dtype=np.uint8) * 200
    cx, cy = 320, 240
    skin = (int(180 + seed % 30), int(140 + seed % 20), int(100 + seed % 15))
    cv2.ellipse(img, (cx, cy), (110, 140), 0, 0, 360, skin, -1)
    cv2.ellipse(img, (cx, cy - 60), (100, 80), 0, 180, 360, skin, -1)
    eye_c = (int(40 + seed % 40), int(30 + seed % 30), int(20 + seed % 20))
    cv2.ellipse(img, (cx-38, cy-35), (22, 13), 0, 0, 360, (240,220,200), -1)
    cv2.ellipse(img, (cx+38, cy-35), (22, 13), 0, 0, 360, (240,220,200), -1)
    cv2.circle(img, (cx-38, cy-35), 9, eye_c, -1)
    cv2.circle(img, (cx+38, cy-35), 9, eye_c, -1)
    cv2.line(img, (cx-58, cy-58), (cx-18, cy-52), eye_c, 3+seed%2)
    cv2.line(img, (cx+18, cy-52), (cx+58, cy-58), eye_c, 3+seed%2)
    nose_c = tuple(max(0, c-20) for c in skin)
    cv2.ellipse(img, (cx, cy+10), (15, 20), 0, 0, 360, nose_c, -1)
    cv2.circle(img, (cx-12, cy+25), 8, nose_c, -1)
    cv2.circle(img, (cx+12, cy+25), 8, nose_c, -1)
    lip_c = (int(150+seed%30), int(80+seed%20), int(80+seed%20))
    cv2.ellipse(img, (cx, cy+60), (35, 12), 0, 0, 180, lip_c, -1)
    hair_c = (int(30+seed%60), int(20+seed%40), int(10+seed%20))
    cv2.ellipse(img, (cx, cy-100), (115, 80), 0, 180, 360, hair_c, -1)
    img = np.clip(img.astype(np.int32) + brightness, 0, 255).astype(np.uint8)
    if noise > 0:
        img = np.clip(img.astype(np.int32) + np.random.randint(-noise, noise, img.shape), 0, 255).astype(np.uint8)
    if blur > 0:
        img = cv2.GaussianBlur(img, (blur*2+1, blur*2+1), 0)
    return img

def to_b64(img):
    _, buf = cv2.imencode('.jpg', img, [cv2.IMWRITE_JPEG_QUALITY, 85])
    return base64.b64encode(buf.tobytes()).decode()

print("=" * 60)
print("FACE RECOGNITION PIPELINE TEST (stdin/stdout)")
print("=" * 60)

# Enroll
print("\n[1] Enrolling person A...")
r = run({"command": "encode", "image": to_b64(make_face(seed=1))})
print(f"    OK={r.get('success')} | face_detected={r.get('face_detected')} | emb={len(r.get('embedding',[]))}D")
if not r['success']:
    print("    ERROR:", r.get('error')); sys.exit(1)
stored = r['embedding']

tests = [
    ("Same person, +20 brightness",  make_face(seed=1, brightness=20, noise=5),  True),
    ("Same person, -30 brightness",  make_face(seed=1, brightness=-30, noise=8), True),
    ("Same person, slight blur",     make_face(seed=1, brightness=5, blur=1),    True),
    ("Same person, more noise",      make_face(seed=1, noise=15),                True),
    ("DIFFERENT person (seed=99)",   make_face(seed=99),                         False),
    ("DIFFERENT person (seed=50)",   make_face(seed=50),                         False),
]

results = []
for name, img, expected in tests:
    r = run({"command": "compare", "stored": stored, "image": to_b64(img)})
    match = r.get('match', False)
    cosine = r.get('cosine', 0)
    ok = match == expected
    results.append(ok)
    status = "✅" if ok else "❌"
    print(f"\n  {status} {name}")
    print(f"     match={match} | cosine={cosine:.4f} | similarity={r.get('similarity')}%")

print("\n" + "=" * 60)
passed = sum(results)
print(f"RESULT: {passed}/{len(results)} tests passed")
print("=" * 60)
