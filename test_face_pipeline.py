"""Full pipeline test: enroll + verify same person + reject different person."""
import cv2, numpy as np, base64, json, subprocess, sys, os, time

SCRIPT = os.path.join(os.path.dirname(__file__), 'face_recognition_service.py')
PY = sys.executable

def run(req):
    r = subprocess.run([PY, SCRIPT], input=json.dumps(req),
                       capture_output=True, text=True, timeout=30)
    for line in reversed(r.stdout.strip().split('\n')):
        if line.strip().startswith('{'):
            return json.loads(line.strip())
    return {"success": False, "error": "No JSON: " + r.stdout[:100]}

def make_face(seed=1, brightness=0, noise=3):
    """Generate a realistic face image."""
    np.random.seed(seed)
    img = np.ones((480, 640, 3), dtype=np.uint8) * 200
    cx, cy = 320, 240
    skin = (int(180+seed%30), int(140+seed%20), int(100+seed%15))
    cv2.ellipse(img, (cx, cy), (110, 140), 0, 0, 360, skin, -1)
    cv2.ellipse(img, (cx, cy-60), (100, 80), 0, 180, 360, skin, -1)
    eye_c = (int(40+seed%40), int(30+seed%30), int(20+seed%20))
    cv2.ellipse(img, (cx-38, cy-35), (22, 13), 0, 0, 360, (240,220,200), -1)
    cv2.ellipse(img, (cx+38, cy-35), (22, 13), 0, 0, 360, (240,220,200), -1)
    cv2.circle(img, (cx-38, cy-35), 9, eye_c, -1)
    cv2.circle(img, (cx+38, cy-35), 9, eye_c, -1)
    cv2.line(img, (cx-58, cy-58), (cx-18, cy-52), eye_c, 3)
    cv2.line(img, (cx+18, cy-52), (cx+58, cy-58), eye_c, 3)
    nose_c = tuple(max(0, c-20) for c in skin)
    cv2.ellipse(img, (cx, cy+10), (15, 20), 0, 0, 360, nose_c, -1)
    lip_c = (int(150+seed%30), int(80+seed%20), int(80+seed%20))
    cv2.ellipse(img, (cx, cy+60), (35, 12), 0, 0, 180, lip_c, -1)
    hair_c = (int(30+seed%60), int(20+seed%40), int(10+seed%20))
    cv2.ellipse(img, (cx, cy-100), (115, 80), 0, 180, 360, hair_c, -1)
    img = np.clip(img.astype(np.int32) + brightness, 0, 255).astype(np.uint8)
    if noise > 0:
        img = np.clip(img.astype(np.int32) + np.random.randint(-noise, noise, img.shape), 0, 255).astype(np.uint8)
    return img

def b64(img):
    _, buf = cv2.imencode('.jpg', img, [cv2.IMWRITE_JPEG_QUALITY, 90])
    return base64.b64encode(buf.tobytes()).decode()

print("=" * 55)
print("FACE RECOGNITION PIPELINE TEST")
print("=" * 55)

# Enroll
t0 = time.time()
r = run({"command": "encode", "image": b64(make_face(seed=1))})
print(f"\n[ENROLL] {time.time()-t0:.1f}s | OK={r.get('success')} | face={r.get('face_detected')} | {len(r.get('embedding',[]))}D")
if not r['success']:
    print("ERROR:", r.get('error')); sys.exit(1)
stored = r['embedding']

tests = [
    ("Same person, normal",        make_face(seed=1, brightness=0),   True),
    ("Same person, +20 brightness", make_face(seed=1, brightness=20),  True),
    ("Same person, -25 brightness", make_face(seed=1, brightness=-25), True),
    ("Same person, noise",          make_face(seed=1, noise=15),       True),
    ("DIFFERENT person (seed=99)",  make_face(seed=99),                False),
    ("DIFFERENT person (seed=50)",  make_face(seed=50),                False),
]

passed = 0
for name, img, expected in tests:
    t0 = time.time()
    r = run({"command": "compare", "stored": stored, "image": b64(img)})
    elapsed = time.time() - t0
    match = r.get('match', False)
    ok = match == expected
    if ok: passed += 1
    icon = "✅" if ok else "❌"
    print(f"\n  {icon} {name} ({elapsed:.1f}s)")
    print(f"     match={match} | cosine={r.get('cosine',0):.4f} | sim={r.get('similarity')}%")

print(f"\n{'='*55}")
print(f"RESULT: {passed}/{len(tests)} tests passed")
print(f"{'='*55}")
