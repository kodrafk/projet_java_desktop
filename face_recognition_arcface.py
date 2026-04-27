#!/usr/bin/env python3
"""
Professional Face Recognition Service using DeepFace + ArcFace
99.83% accuracy on LFW benchmark

This service provides state-of-the-art face recognition using:
- Model: ArcFace (512-dimensional embeddings)
- Detector: RetinaFace (best face detector)
- Backend: OpenCV
- Threshold: 0.50 (cosine similarity)

Protocol: reads JSON from stdin, writes JSON to stdout.
"""

import sys
import json
import base64
import numpy as np

def log(msg):
    print(f"[ArcFace] {msg}", file=sys.stderr, flush=True)

# ── Lazy imports ───────────────────────────────────────────────────────────────

_deepface = None
_cv2 = None

def get_deepface():
    global _deepface
    if _deepface is None:
        try:
            from deepface import DeepFace
            _deepface = DeepFace
            log("DeepFace loaded successfully")
        except ImportError:
            log("ERROR: DeepFace not installed. Run: pip install deepface")
            sys.exit(1)
    return _deepface

def get_cv2():
    global _cv2
    if _cv2 is None:
        try:
            import cv2
            _cv2 = cv2
            log("OpenCV loaded successfully")
        except ImportError:
            log("ERROR: OpenCV not installed. Run: pip install opencv-python")
            sys.exit(1)
    return _cv2

# ── Image decode ───────────────────────────────────────────────────────────────

def decode_image(b64_str):
    """Decode base64 string to OpenCV image."""
    cv2 = get_cv2()
    try:
        data = base64.b64decode(b64_str)
        arr = np.frombuffer(data, dtype=np.uint8)
        img = cv2.imdecode(arr, cv2.IMREAD_COLOR)
        if img is None:
            raise ValueError("Failed to decode image")
        return img
    except Exception as e:
        log(f"Image decode error: {e}")
        return None

# ── Face encoding with ArcFace ─────────────────────────────────────────────────

def encode_face(image_base64):
    """
    Extract 512-dimensional ArcFace embedding from base64 image.
    
    Returns:
        dict: {
            "success": bool,
            "embedding": list[float] (512D),
            "dimensions": int,
            "model": str,
            "detector": str,
            "error": str (if failed)
        }
    """
    DeepFace = get_deepface()
    
    try:
        img = decode_image(image_base64)
        if img is None:
            return {"success": False, "error": "Could not decode image"}
        
        log("Extracting ArcFace embedding...")
        
        # Extract embedding using ArcFace model with RetinaFace detector
        result = DeepFace.represent(
            img_path=img,
            model_name='ArcFace',
            detector_backend='retinaface',
            enforce_detection=True,
            align=True,
            normalization='ArcFace'
        )
        
        embedding = result[0]['embedding']  # 512D vector
        
        # L2 normalize for cosine similarity
        emb_array = np.array(embedding, dtype=np.float64)
        norm = np.linalg.norm(emb_array)
        if norm > 1e-7:
            emb_array = emb_array / norm
        
        log(f"Embedding extracted successfully ({len(emb_array)}D)")
        
        return {
            "success": True,
            "embedding": emb_array.tolist(),
            "dimensions": len(emb_array),
            "model": "ArcFace",
            "detector": "RetinaFace"
        }
        
    except ValueError as e:
        # No face detected
        error_msg = str(e)
        if "Face could not be detected" in error_msg:
            log("No face detected in image")
            return {"success": False, "error": "No face detected. Please ensure your face is visible and well-lit."}
        else:
            log(f"ValueError: {error_msg}")
            return {"success": False, "error": error_msg}
    
    except Exception as e:
        log(f"Encoding error: {e}")
        import traceback
        traceback.print_exc(file=sys.stderr)
        return {"success": False, "error": str(e)}

# ── Face verification ──────────────────────────────────────────────────────────

def verify_faces(embedding1, embedding2):
    """
    Compare two ArcFace embeddings using cosine similarity.
    
    Args:
        embedding1: First embedding (512D)
        embedding2: Second embedding (512D)
    
    Returns:
        dict: {
            "success": bool,
            "verified": bool,
            "similarity": float (0-100),
            "cosine": float (-1 to 1),
            "threshold": float,
            "error": str (if failed)
        }
    """
    try:
        # Convert to numpy arrays
        emb1 = np.array(embedding1, dtype=np.float64)
        emb2 = np.array(embedding2, dtype=np.float64)
        
        # L2 normalize both embeddings
        norm1 = np.linalg.norm(emb1)
        norm2 = np.linalg.norm(emb2)
        
        if norm1 > 1e-7:
            emb1 = emb1 / norm1
        if norm2 > 1e-7:
            emb2 = emb2 / norm2
        
        # Cosine similarity (dot product of normalized vectors)
        cosine = float(np.dot(emb1, emb2))
        
        # ArcFace threshold: 0.50 (more lenient than previous 0.65)
        # This is appropriate for ArcFace which has better discrimination
        THRESHOLD = 0.50
        is_match = cosine >= THRESHOLD
        
        # Convert to percentage
        similarity_pct = max(0.0, cosine) * 100
        
        log(f"Similarity: {similarity_pct:.1f}% (threshold: {THRESHOLD*100:.0f}%)")
        
        return {
            "success": True,
            "verified": is_match,
            "similarity": round(similarity_pct, 1),
            "cosine": round(cosine, 4),
            "threshold": THRESHOLD
        }
        
    except Exception as e:
        log(f"Verification error: {e}")
        return {"success": False, "error": str(e)}

# ── Commands ───────────────────────────────────────────────────────────────────

def cmd_encode(req):
    """Handle 'encode' command."""
    image_b64 = req.get("image")
    if not image_b64:
        return {"success": False, "error": "Missing 'image' field"}
    return encode_face(image_b64)

def cmd_compare(req):
    """Handle 'compare' command."""
    stored = req.get("stored")
    image_b64 = req.get("image")
    
    if not stored:
        return {"success": False, "error": "Missing 'stored' embedding"}
    if not image_b64:
        return {"success": False, "error": "Missing 'image' field"}
    
    # First encode the live image
    live_result = encode_face(image_b64)
    if not live_result.get("success"):
        return live_result
    
    # Then compare
    return verify_faces(stored, live_result["embedding"])

# ── Main service loop ──────────────────────────────────────────────────────────

def main():
    """
    Main service loop - reads JSON from stdin, writes JSON to stdout.
    
    Commands:
        {"command": "encode", "image": "<base64>"}
        {"command": "compare", "stored": [<embedding>], "image": "<base64>"}
    """
    log("ArcFace Face Recognition Service starting...")
    log("Model: ArcFace (512D embeddings)")
    log("Detector: RetinaFace")
    log("Threshold: 0.50 (50% cosine similarity)")
    
    # Pre-load models
    try:
        DeepFace = get_deepface()
        log("Building ArcFace model (this may take a moment)...")
        DeepFace.build_model("ArcFace")
        log("ArcFace model ready!")
    except Exception as e:
        log(f"Model loading error: {e}")
        log("Service will load model on first use")
    
    log("Service ready. Waiting for commands...")
    
    while True:
        try:
            line = sys.stdin.readline()
            if not line:
                break
            
            line = line.strip()
            if not line:
                continue
            
            req = json.loads(line)
            command = req.get("command")
            
            if command == "encode":
                result = cmd_encode(req)
            elif command == "compare":
                result = cmd_compare(req)
            else:
                result = {"success": False, "error": f"Unknown command: {command}"}
            
            print(json.dumps(result), flush=True)
            
        except json.JSONDecodeError as e:
            result = {"success": False, "error": f"Invalid JSON: {e}"}
            print(json.dumps(result), flush=True)
        except Exception as e:
            import traceback
            result = {
                "success": False,
                "error": str(e),
                "trace": traceback.format_exc()
            }
            print(json.dumps(result), flush=True)

if __name__ == "__main__":
    main()
