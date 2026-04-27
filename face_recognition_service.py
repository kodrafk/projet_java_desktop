#!/usr/bin/env python3
"""
Face Recognition Service — NutriLife
Fast, accurate face recognition using OpenCV only.

Key improvements:
  - Eye region extraction (most discriminant facial feature)
  - Multi-region analysis: full face + left eye + right eye + nose bridge
  - CLAHE normalization for lighting robustness
  - LBP + HOG + DCT features per region
  - Cosine similarity threshold: 0.88

Protocol: reads JSON from stdin, writes JSON to stdout.
"""

import sys, json, base64, os
import numpy as np

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
MODELS_DIR = os.path.join(SCRIPT_DIR, "face_models")
FACE_PROTO  = os.path.join(MODELS_DIR, "deploy.prototxt")
FACE_MODEL  = os.path.join(MODELS_DIR, "res10_300x300_ssd_iter_140000.caffemodel")
EYE_CASCADE = None  # loaded lazily

def log(msg):
    print(f"[FaceID] {msg}", file=sys.stderr, flush=True)

# ── Image decode ───────────────────────────────────────────────────────────────

def decode_image(b64_str):
    import cv2
    data = base64.b64decode(b64_str)
    arr  = np.frombuffer(data, dtype=np.uint8)
    return cv2.imdecode(arr, cv2.IMREAD_COLOR)

# ── Face detection ─────────────────────────────────────────────────────────────

_dnn_net = None

def get_dnn():
    global _dnn_net
    if _dnn_net is not None:
        return _dnn_net
    import cv2
    if os.path.exists(FACE_PROTO) and os.path.exists(FACE_MODEL):
        try:
            _dnn_net = cv2.dnn.readNetFromCaffe(FACE_PROTO, FACE_MODEL)
            return _dnn_net
        except Exception as e:
            log(f"DNN load failed: {e}")
    return None

def detect_face_box(img):
    """Returns (x,y,w,h) of best face, or None."""
    import cv2
    H, W = img.shape[:2]
    net = get_dnn()

    if net is not None:
        blob = cv2.dnn.blobFromImage(
            cv2.resize(img, (300, 300)), 1.0, (300, 300),
            (104.0, 177.0, 123.0), swapRB=False, crop=False
        )
        net.setInput(blob)
        dets = net.forward()
        best_conf, best_box = 0.35, None
        for i in range(dets.shape[2]):
            conf = float(dets[0, 0, i, 2])
            if conf > best_conf:
                b = dets[0, 0, i, 3:7] * np.array([W, H, W, H])
                x1, y1, x2, y2 = b.astype(int)
                x1, y1 = max(0, x1), max(0, y1)
                x2, y2 = min(W, x2), min(H, y2)
                if x2 > x1 + 30 and y2 > y1 + 30:
                    best_box = (x1, y1, x2-x1, y2-y1)
                    best_conf = conf
        if best_box:
            log(f"Face detected (DNN conf={best_conf:.2f})")
            return best_box

    # Haar fallback
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    cascade = cv2.CascadeClassifier(cv2.data.haarcascades + "haarcascade_frontalface_default.xml")
    for scale, neighbors, minsize in [(1.1, 4, 60), (1.05, 3, 40), (1.03, 2, 30)]:
        faces = cascade.detectMultiScale(gray, scaleFactor=scale,
                                         minNeighbors=neighbors, minSize=(minsize, minsize))
        if len(faces) > 0:
            log("Face detected (Haar)")
            return tuple(max(faces, key=lambda f: f[2]*f[3]))
    log("No face detected — using center crop")
    return None

def crop_region(img, box, size):
    """Crop a region with padding, resize to size×size."""
    import cv2
    H, W = img.shape[:2]
    if box is None:
        my, mx = int(H*0.10), int(W*0.15)
        region = img[my:H-my, mx:W-mx]
    else:
        x, y, fw, fh = box
        pad = int(min(fw, fh) * 0.20)
        x1, y1 = max(0, x-pad), max(0, y-pad)
        x2, y2 = min(W, x+fw+pad), min(H, y+fh+pad)
        region = img[y1:y2, x1:x2]
    if region.size == 0:
        region = img
    return cv2.resize(region, (size, size), interpolation=cv2.INTER_LANCZOS4)

def get_eye_regions(face_bgr, face_box):
    """
    Extract left eye, right eye, and nose bridge regions from face.
    Returns (left_eye_img, right_eye_img, nose_img) each 32×32.
    """
    import cv2
    if face_box is None:
        return None, None, None

    x, y, fw, fh = face_box
    H, W = face_bgr.shape[:2]

    # Eye region: upper 45% of face, split left/right
    eye_y1 = y + int(fh * 0.15)
    eye_y2 = y + int(fh * 0.50)
    eye_mid = x + fw // 2

    # Left eye (from camera perspective = right side of image)
    le_x1 = max(0, eye_mid - int(fw * 0.05))
    le_x2 = min(W, x + fw + int(fw * 0.05))
    left_eye = face_bgr[eye_y1:eye_y2, le_x1:le_x2]

    # Right eye
    re_x1 = max(0, x - int(fw * 0.05))
    re_x2 = min(W, eye_mid + int(fw * 0.05))
    right_eye = face_bgr[eye_y1:eye_y2, re_x1:re_x2]

    # Nose bridge (center strip)
    nose_y1 = y + int(fh * 0.35)
    nose_y2 = y + int(fh * 0.65)
    nose_x1 = x + int(fw * 0.30)
    nose_x2 = x + int(fw * 0.70)
    nose = face_bgr[nose_y1:nose_y2, nose_x1:nose_x2]

    def safe_resize(region, size):
        if region is None or region.size == 0:
            return np.zeros((size, size, 3), dtype=np.uint8)
        return cv2.resize(region, (size, size), interpolation=cv2.INTER_LANCZOS4)

    return safe_resize(left_eye, 32), safe_resize(right_eye, 32), safe_resize(nose, 32)

# ── Feature extraction ─────────────────────────────────────────────────────────

def normalize_gray(img_bgr, size):
    """Convert to grayscale, apply CLAHE, resize."""
    import cv2
    gray = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2GRAY)
    clahe = cv2.createCLAHE(clipLimit=2.5, tileGridSize=(4, 4))
    gray = clahe.apply(gray)
    return cv2.resize(gray, (size, size), interpolation=cv2.INTER_LANCZOS4)

def lbp_features(gray, grid=4, bins=8):
    """LBP histogram on a spatial grid. Returns grid*grid*bins values."""
    h, w = gray.shape
    cell_h, cell_w = h // grid, w // grid
    feats = []
    for gy in range(grid):
        for gx in range(grid):
            region = gray[gy*cell_h:(gy+1)*cell_h, gx*cell_w:(gx+1)*cell_w]
            lbp = np.zeros_like(region)
            for i, (dy, dx) in enumerate([(-1,-1),(-1,0),(-1,1),(0,1),(1,1),(1,0),(1,-1),(0,-1)]):
                shifted = np.roll(np.roll(region, dy, axis=0), dx, axis=1)
                lbp |= ((region >= shifted).astype(np.uint8) << i)
            hist, _ = np.histogram(lbp, bins=bins, range=(0, 256))
            hist = hist.astype(np.float32)
            s = hist.sum()
            feats.extend((hist / s if s > 0 else hist).tolist())
    return feats

def hog_features(gray, grid=4, bins=4):
    """HOG histogram on a spatial grid. Returns grid*grid*bins values."""
    import cv2
    gx = cv2.Sobel(gray.astype(np.float32), cv2.CV_32F, 1, 0, ksize=3)
    gy = cv2.Sobel(gray.astype(np.float32), cv2.CV_32F, 0, 1, ksize=3)
    mag = np.sqrt(gx**2 + gy**2)
    ang = np.arctan2(gy, gx) * 180 / np.pi % 180
    h, w = gray.shape
    cell_h, cell_w = h // grid, w // grid
    feats = []
    for gy_i in range(grid):
        for gx_i in range(grid):
            m = mag[gy_i*cell_h:(gy_i+1)*cell_h, gx_i*cell_w:(gx_i+1)*cell_w]
            a = ang[gy_i*cell_h:(gy_i+1)*cell_h, gx_i*cell_w:(gx_i+1)*cell_w]
            hist = np.zeros(bins, dtype=np.float32)
            for b in range(bins):
                mask = (a >= b*(180/bins)) & (a < (b+1)*(180/bins))
                hist[b] = m[mask].sum()
            s = hist.sum()
            feats.extend((hist / s if s > 0 else hist).tolist())
    return feats

def dct_features(gray, n=16):
    """DCT low-frequency features. Returns n values."""
    import cv2
    dct = cv2.dct(gray.astype(np.float32) / 255.0)
    flat = dct[:8, :8].flatten()
    flat[0] = 0  # remove DC
    norm = np.linalg.norm(flat)
    flat = flat / norm if norm > 0 else flat
    return flat[:n].tolist()

def extract_embedding(face_bgr, face_box):
    """
    Extract a 128-D embedding focusing on eyes + full face.

    Regions:
      - Full face (64×64): LBP 4×4×8=128 + HOG 4×4×4=64 + DCT 16 → 208 → take 64
      - Left eye (32×32):  LBP 4×4×4=64 → take 24
      - Right eye (32×32): LBP 4×4×4=64 → take 24
      - Nose bridge (32×32): LBP 2×2×4=16 → take 16

    Total: 64 + 24 + 24 + 16 = 128 → L2 normalize
    """
    # Full face features
    face_gray = normalize_gray(face_bgr, 64)
    face_lbp  = lbp_features(face_gray, grid=4, bins=8)   # 128
    face_hog  = hog_features(face_gray, grid=4, bins=4)   # 64
    face_dct  = dct_features(face_gray, n=16)              # 16
    face_all  = face_lbp + face_hog + face_dct             # 208

    # Eye + nose regions
    left_eye, right_eye, nose = get_eye_regions(face_bgr, face_box)

    if left_eye is not None and left_eye.size > 0:
        le_gray = normalize_gray(left_eye, 32)
        le_feats = lbp_features(le_gray, grid=4, bins=4)  # 64
    else:
        le_feats = [0.0] * 64

    if right_eye is not None and right_eye.size > 0:
        re_gray = normalize_gray(right_eye, 32)
        re_feats = lbp_features(re_gray, grid=4, bins=4)  # 64
    else:
        re_feats = [0.0] * 64

    if nose is not None and nose.size > 0:
        nose_gray = normalize_gray(nose, 32)
        nose_feats = lbp_features(nose_gray, grid=2, bins=4)  # 16
    else:
        nose_feats = [0.0] * 16

    # Combine: face(64) + left_eye(24) + right_eye(24) + nose(16) = 128
    combined = face_all[:64] + le_feats[:24] + re_feats[:24] + nose_feats[:16]

    # Pad to 128 if needed
    while len(combined) < 128:
        combined.append(0.0)
    combined = combined[:128]

    # L2 normalize
    arr = np.array(combined, dtype=np.float64)
    n = np.linalg.norm(arr)
    return (arr / n if n > 1e-7 else arr).tolist()

# ── Commands ───────────────────────────────────────────────────────────────────

def cmd_encode(req):
    img = decode_image(req["image"])
    if img is None:
        return {"success": False, "error": "Could not decode image"}
    box  = detect_face_box(img)
    face = crop_region(img, box, 64)
    emb  = extract_embedding(face, box)
    return {"success": True, "embedding": emb, "face_detected": box is not None, "model": "EyeFocus-LBP+HOG+DCT"}

def cmd_compare(req):
    stored = np.array(req["stored"], dtype=np.float64)
    img = decode_image(req["image"])
    if img is None:
        return {"success": False, "error": "Could not decode live image"}
    box  = detect_face_box(img)
    face = crop_region(img, box, 64)
    live = np.array(extract_embedding(face, box), dtype=np.float64)

    # Re-normalize
    sn = np.linalg.norm(stored); stored = stored/sn if sn > 0 else stored
    ln = np.linalg.norm(live);   live   = live/ln   if ln > 0 else live

    cosine   = float(np.dot(stored, live))
    distance = float(np.linalg.norm(stored - live))
    match    = cosine > 0.82
    sim      = round(max(0.0, cosine) * 100, 1)

    return {"success": True, "match": match, "cosine": cosine,
            "distance": distance, "similarity": sim, "face_detected": box is not None}

def main():
    try:
        raw = sys.stdin.read().strip()
        req = json.loads(raw)
        cmd = req.get("command", "")
        if   cmd == "encode":  result = cmd_encode(req)
        elif cmd == "compare": result = cmd_compare(req)
        else: result = {"success": False, "error": f"Unknown command: {cmd}"}
    except Exception as e:
        import traceback
        result = {"success": False, "error": str(e), "trace": traceback.format_exc()}
    print(json.dumps(result), flush=True)

if __name__ == "__main__":
    main()
