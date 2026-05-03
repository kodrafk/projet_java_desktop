#!/usr/bin/env python3
"""
Professional Face Recognition System - Enterprise Grade
Uses DeepFace with ArcFace model + advanced face detection
Focus on eyes, facial landmarks, and quality validation
"""

import os
import sys
import json
import base64
import numpy as np
from io import BytesIO

try:
    from deepface import DeepFace
    from deepface.detectors import FaceDetector
    import cv2
    from PIL import Image
    DEPS_OK = True
except ImportError as e:
    DEPS_OK = False
    IMPORT_ERROR = str(e)

# ── Configuration ──────────────────────────────────────────────────────────────

MODEL_NAME = "ArcFace"  # State-of-the-art face recognition
DETECTOR_BACKEND = "retinaface"  # Best detector for facial landmarks
EMBEDDING_SIZE = 512

# Quality thresholds
MIN_FACE_SIZE = 80  # pixels
MIN_EYE_DISTANCE = 40  # pixels between eyes
MAX_YAW = 30  # degrees head rotation
MAX_PITCH = 25  # degrees head tilt
MIN_BRIGHTNESS = 40
MAX_BRIGHTNESS = 220
MIN_SHARPNESS = 100  # Laplacian variance

# ── Face Quality Analysis ──────────────────────────────────────────────────────

def analyze_face_quality(img, face_region):
    """
    Analyze face quality for professional recognition.
    Returns (is_valid, quality_score, issues)
    """
    issues = []
    quality_score = 100.0
    
    x, y, w, h = face_region['x'], face_region['y'], face_region['w'], face_region['h']
    
    # Extract face region
    face_img = img[y:y+h, x:x+w]
    if face_img.size == 0:
        return False, 0, ["Face region empty"]
    
    # 1. Face size check
    if w < MIN_FACE_SIZE or h < MIN_FACE_SIZE:
        issues.append(f"Face too small ({w}x{h}px)")
        quality_score -= 30
    
    # 2. Brightness check
    gray = cv2.cvtColor(face_img, cv2.COLOR_BGR2GRAY) if len(face_img.shape) == 3 else face_img
    brightness = np.mean(gray)
    if brightness < MIN_BRIGHTNESS:
        issues.append(f"Too dark (brightness: {brightness:.0f})")
        quality_score -= 25
    elif brightness > MAX_BRIGHTNESS:
        issues.append(f"Too bright (brightness: {brightness:.0f})")
        quality_score -= 20
    
    # 3. Sharpness check (Laplacian variance)
    laplacian = cv2.Laplacian(gray, cv2.CV_64F)
    sharpness = laplacian.var()
    if sharpness < MIN_SHARPNESS:
        issues.append(f"Blurry image (sharpness: {sharpness:.0f})")
        quality_score -= 30
    
    # 4. Eye detection (critical for recognition)
    try:
        eye_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_eye.xml')
        eyes = eye_cascade.detectMultiScale(gray, 1.1, 5, minSize=(20, 20))
        
        if len(eyes) < 2:
            issues.append("Both eyes not clearly visible")
            quality_score -= 40
        else:
            # Check eye distance
            eye1, eye2 = eyes[0], eyes[1]
            eye_dist = np.sqrt((eye1[0] - eye2[0])**2 + (eye1[1] - eye2[1])**2)
            if eye_dist < MIN_EYE_DISTANCE:
                issues.append("Face too far from camera")
                quality_score -= 20
    except Exception:
        issues.append("Eye detection failed")
        quality_score -= 30
    
    # 5. Pose estimation (head rotation)
    if 'confidence' in face_region and face_region['confidence'] < 0.95:
        issues.append("Face detection confidence low")
        quality_score -= 15
    
    is_valid = quality_score >= 50 and len([i for i in issues if "eyes" in i.lower()]) == 0
    
    return is_valid, max(0, quality_score), issues


def detect_facial_landmarks(img, face_region):
    """
    Detect key facial landmarks (eyes, nose, mouth).
    Returns dict with landmark coordinates.
    """
    x, y, w, h = face_region['x'], face_region['y'], face_region['w'], face_region['h']
    face_img = img[y:y+h, x:x+w]
    
    landmarks = {
        'left_eye': None,
        'right_eye': None,
        'nose': None,
        'mouth': None
    }
    
    try:
        gray = cv2.cvtColor(face_img, cv2.COLOR_BGR2GRAY) if len(face_img.shape) == 3 else face_img
        
        # Detect eyes
        eye_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_eye.xml')
        eyes = eye_cascade.detectMultiScale(gray, 1.1, 5, minSize=(20, 20))
        
        if len(eyes) >= 2:
            # Sort eyes by x coordinate (left to right)
            eyes_sorted = sorted(eyes, key=lambda e: e[0])
            landmarks['left_eye'] = (x + eyes_sorted[0][0] + eyes_sorted[0][2]//2, 
                                     y + eyes_sorted[0][1] + eyes_sorted[0][3]//2)
            landmarks['right_eye'] = (x + eyes_sorted[1][0] + eyes_sorted[1][2]//2,
                                      y + eyes_sorted[1][1] + eyes_sorted[1][3]//2)
        
        # Detect nose (approximate center-bottom of face)
        landmarks['nose'] = (x + w//2, y + int(h*0.6))
        
        # Detect mouth (approximate lower third)
        landmarks['mouth'] = (x + w//2, y + int(h*0.8))
        
    except Exception as e:
        print(f"[Landmarks] Detection error: {e}", file=sys.stderr)
    
    return landmarks


def align_face(img, landmarks):
    """
    Align face based on eye positions for better recognition.
    Returns aligned image.
    """
    if landmarks['left_eye'] is None or landmarks['right_eye'] is None:
        return img
    
    try:
        left_eye = np.array(landmarks['left_eye'])
        right_eye = np.array(landmarks['right_eye'])
        
        # Calculate angle
        dY = right_eye[1] - left_eye[1]
        dX = right_eye[0] - left_eye[0]
        angle = np.degrees(np.arctan2(dY, dX))
        
        # Only align if rotation is significant but not extreme
        if abs(angle) > 2 and abs(angle) < 30:
            # Get rotation matrix
            eye_center = ((left_eye[0] + right_eye[0]) // 2, (left_eye[1] + right_eye[1]) // 2)
            M = cv2.getRotationMatrix2D(eye_center, angle, 1.0)
            
            # Apply rotation
            aligned = cv2.warpAffine(img, M, (img.shape[1], img.shape[0]), 
                                     flags=cv2.INTER_CUBIC, 
                                     borderMode=cv2.BORDER_REPLICATE)
            return aligned
    except Exception as e:
        print(f"[Align] Error: {e}", file=sys.stderr)
    
    return img


def enhance_face_region(img, face_region, landmarks):
    """
    Enhance face region for better recognition:
    - Focus on eye region (most discriminative)
    - Histogram equalization
    - Noise reduction
    """
    x, y, w, h = face_region['x'], face_region['y'], face_region['w'], face_region['h']
    
    # Extract face with margin
    margin = int(w * 0.2)
    x1 = max(0, x - margin)
    y1 = max(0, y - margin)
    x2 = min(img.shape[1], x + w + margin)
    y2 = min(img.shape[0], y + h + margin)
    
    face_img = img[y1:y2, x1:x2].copy()
    
    # Apply CLAHE (Contrast Limited Adaptive Histogram Equalization)
    if len(face_img.shape) == 3:
        lab = cv2.cvtColor(face_img, cv2.COLOR_BGR2LAB)
        l, a, b = cv2.split(lab)
        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8,8))
        l = clahe.apply(l)
        face_img = cv2.cvtColor(cv2.merge([l, a, b]), cv2.COLOR_LAB2BGR)
    
    # Denoise
    face_img = cv2.fastNlMeansDenoisingColored(face_img, None, 10, 10, 7, 21)
    
    # Sharpen (focus on edges - important for eyes)
    kernel = np.array([[-1,-1,-1], [-1, 9,-1], [-1,-1,-1]])
    face_img = cv2.filter2D(face_img, -1, kernel)
    
    return face_img


# ── Core Recognition Functions ─────────────────────────────────────────────────

def extract_embedding_professional(img_array):
    """
    Extract face embedding with professional quality checks.
    Returns (embedding, quality_info, landmarks)
    """
    if not DEPS_OK:
        raise Exception(f"Dependencies missing: {IMPORT_ERROR}")
    
    # Detect face with RetinaFace (best detector)
    try:
        face_objs = DeepFace.extract_faces(
            img_path=img_array,
            detector_backend=DETECTOR_BACKEND,
            enforce_detection=True,
            align=True
        )
    except Exception as e:
        raise Exception(f"No face detected: {str(e)}")
    
    if not face_objs:
        raise Exception("No face detected in image")
    
    # Get best face (highest confidence)
    face_obj = max(face_objs, key=lambda f: f.get('confidence', 0))
    face_region = face_obj['facial_area']
    
    # Quality analysis
    is_valid, quality_score, issues = analyze_face_quality(img_array, face_region)
    
    if not is_valid:
        raise Exception(f"Face quality insufficient: {', '.join(issues)}")
    
    # Detect landmarks
    landmarks = detect_facial_landmarks(img_array, face_region)
    
    # Align face based on eyes
    aligned_img = align_face(img_array, landmarks)
    
    # Enhance face region
    enhanced_img = enhance_face_region(aligned_img, face_region, landmarks)
    
    # Extract embedding with ArcFace
    try:
        embedding_objs = DeepFace.represent(
            img_path=enhanced_img,
            model_name=MODEL_NAME,
            detector_backend=DETECTOR_BACKEND,
            enforce_detection=False,  # Already detected
            align=True,
            normalization='ArcFace'
        )
        
        if not embedding_objs:
            raise Exception("Failed to extract embedding")
        
        embedding = np.array(embedding_objs[0]['embedding'])
        
        # L2 normalize
        norm = np.linalg.norm(embedding)
        if norm > 0:
            embedding = embedding / norm
        
        quality_info = {
            'quality_score': quality_score,
            'issues': issues,
            'face_size': (face_region['w'], face_region['h']),
            'confidence': face_obj.get('confidence', 0),
            'eyes_detected': landmarks['left_eye'] is not None and landmarks['right_eye'] is not None
        }
        
        return embedding.tolist(), quality_info, landmarks
        
    except Exception as e:
        raise Exception(f"Embedding extraction failed: {str(e)}")


def compare_embeddings_professional(emb1, emb2):
    """
    Compare two embeddings with professional metrics.
    Returns (similarity, distance, match_confidence)
    """
    emb1 = np.array(emb1)
    emb2 = np.array(emb2)
    
    # Cosine similarity (primary metric for ArcFace)
    cosine_sim = np.dot(emb1, emb2) / (np.linalg.norm(emb1) * np.linalg.norm(emb2))
    
    # Euclidean distance (secondary metric)
    euclidean_dist = np.linalg.norm(emb1 - emb2)
    
    # Match confidence (0-100%)
    # ArcFace threshold: 0.68 for same person
    match_confidence = max(0, min(100, (cosine_sim - 0.5) * 200))
    
    return {
        'cosine_similarity': float(cosine_sim),
        'euclidean_distance': float(euclidean_dist),
        'match_confidence': float(match_confidence),
        'is_match': cosine_sim >= 0.68
    }


# ── API Commands ───────────────────────────────────────────────────────────────

def cmd_encode(request):
    """Encode face from base64 image."""
    try:
        b64_img = request.get('image', '')
        if not b64_img:
            return {'success': False, 'error': 'No image provided'}
        
        # Decode image
        img_bytes = base64.b64decode(b64_img)
        img_array = np.array(Image.open(BytesIO(img_bytes)))
        
        # Convert RGB to BGR for OpenCV
        if len(img_array.shape) == 3 and img_array.shape[2] == 3:
            img_array = cv2.cvtColor(img_array, cv2.COLOR_RGB2BGR)
        
        # Extract embedding
        embedding, quality_info, landmarks = extract_embedding_professional(img_array)
        
        return {
            'success': True,
            'embedding': embedding,
            'quality': quality_info,
            'landmarks': {k: list(v) if v else None for k, v in landmarks.items()}
        }
        
    except Exception as e:
        return {'success': False, 'error': str(e)}


def cmd_compare(request):
    """Compare two embeddings."""
    try:
        emb1 = request.get('embedding1')
        emb2 = request.get('embedding2')
        
        if not emb1 or not emb2:
            return {'success': False, 'error': 'Missing embeddings'}
        
        result = compare_embeddings_professional(emb1, emb2)
        result['success'] = True
        
        return result
        
    except Exception as e:
        return {'success': False, 'error': str(e)}


def cmd_verify(request):
    """Verify face against stored embedding."""
    try:
        b64_img = request.get('image', '')
        stored_emb = request.get('stored_embedding')
        
        if not b64_img or not stored_emb:
            return {'success': False, 'error': 'Missing image or stored embedding'}
        
        # Decode and extract embedding
        img_bytes = base64.b64decode(b64_img)
        img_array = np.array(Image.open(BytesIO(img_bytes)))
        
        if len(img_array.shape) == 3 and img_array.shape[2] == 3:
            img_array = cv2.cvtColor(img_array, cv2.COLOR_RGB2BGR)
        
        live_emb, quality_info, landmarks = extract_embedding_professional(img_array)
        
        # Compare
        comparison = compare_embeddings_professional(live_emb, stored_emb)
        
        return {
            'success': True,
            'match': comparison['is_match'],
            'confidence': comparison['match_confidence'],
            'similarity': comparison['cosine_similarity'],
            'quality': quality_info,
            'landmarks': {k: list(v) if v else None for k, v in landmarks.items()}
        }
        
    except Exception as e:
        return {'success': False, 'error': str(e)}


# ── Main ───────────────────────────────────────────────────────────────────────

def main():
    """Process JSON commands from stdin."""
    if not DEPS_OK:
        print(json.dumps({'success': False, 'error': f'Dependencies missing: {IMPORT_ERROR}'}))
        return
    
    for line in sys.stdin:
        line = line.strip()
        if not line:
            continue
        
        try:
            request = json.loads(line)
            command = request.get('command', '')
            
            if command == 'encode':
                result = cmd_encode(request)
            elif command == 'compare':
                result = cmd_compare(request)
            elif command == 'verify':
                result = cmd_verify(request)
            else:
                result = {'success': False, 'error': f'Unknown command: {command}'}
            
            print(json.dumps(result), flush=True)
            
        except Exception as e:
            print(json.dumps({'success': False, 'error': str(e)}), flush=True)


if __name__ == '__main__':
    main()
