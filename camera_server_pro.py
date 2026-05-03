#!/usr/bin/env python3
"""
Professional Camera Server - Enterprise Grade
High-quality frame capture with face detection overlay
Real-time quality feedback and eye tracking
"""

import sys
import os
import socket
import threading
import json
import base64
import time
import numpy as np

try:
    import cv2
    CV2_OK = True
except ImportError:
    CV2_OK = False

PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 7654
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))

# ── Camera Configuration ───────────────────────────────────────────────────────

FRAME_WIDTH = 1280
FRAME_HEIGHT = 720
FPS = 30
JPEG_QUALITY = 90

# ── Shared State ───────────────────────────────────────────────────────────────

_cap = None
_cap_lock = threading.Lock()
_last_frame = None
_frame_lock = threading.Lock()
_running = True

# Face detection cascades
_face_cascade = None
_eye_cascade = None
_cascades_loaded = False

# ── Face Detection & Quality ───────────────────────────────────────────────────

def _load_cascades():
    """Load Haar cascades for face and eye detection."""
    global _face_cascade, _eye_cascade, _cascades_loaded
    
    if _cascades_loaded:
        return True
    
    try:
        _face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')
        _eye_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_eye.xml')
        _cascades_loaded = True
        print("[CamServer] Face detection cascades loaded", flush=True)
        return True
    except Exception as e:
        print(f"[CamServer] Failed to load cascades: {e}", flush=True)
        return False


def _detect_face_and_eyes(frame):
    """
    Detect face and eyes in frame.
    Returns (face_rect, eyes_list, quality_score, feedback_message)
    """
    if not _cascades_loaded:
        return None, [], 0, "Initializing..."
    
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    
    # Detect faces
    faces = _face_cascade.detectMultiScale(
        gray, 
        scaleFactor=1.1, 
        minNeighbors=5, 
        minSize=(100, 100),
        flags=cv2.CASCADE_SCALE_IMAGE
    )
    
    if len(faces) == 0:
        return None, [], 0, "❌ No face detected"
    
    if len(faces) > 1:
        return None, [], 0, "⚠️ Multiple faces detected"
    
    # Get the face
    face = faces[0]
    x, y, w, h = face
    
    # Quality checks
    quality_score = 100
    feedback = []
    
    # 1. Face size check
    face_area = w * h
    frame_area = frame.shape[0] * frame.shape[1]
    face_ratio = face_area / frame_area
    
    if face_ratio < 0.08:
        feedback.append("Move closer")
        quality_score -= 30
    elif face_ratio > 0.5:
        feedback.append("Move back")
        quality_score -= 20
    
    # 2. Face position check (should be centered)
    face_center_x = x + w // 2
    face_center_y = y + h // 2
    frame_center_x = frame.shape[1] // 2
    frame_center_y = frame.shape[0] // 2
    
    offset_x = abs(face_center_x - frame_center_x)
    offset_y = abs(face_center_y - frame_center_y)
    
    if offset_x > frame.shape[1] * 0.15:
        feedback.append("Center horizontally")
        quality_score -= 15
    
    if offset_y > frame.shape[0] * 0.15:
        feedback.append("Center vertically")
        quality_score -= 15
    
    # 3. Brightness check
    face_roi = gray[y:y+h, x:x+w]
    brightness = np.mean(face_roi)
    
    if brightness < 60:
        feedback.append("Too dark")
        quality_score -= 25
    elif brightness > 200:
        feedback.append("Too bright")
        quality_score -= 20
    
    # 4. Sharpness check
    laplacian = cv2.Laplacian(face_roi, cv2.CV_64F)
    sharpness = laplacian.var()
    
    if sharpness < 100:
        feedback.append("Image blurry")
        quality_score -= 30
    
    # 5. Eye detection (critical!)
    face_gray = gray[y:y+h, x:x+w]
    eyes = _eye_cascade.detectMultiScale(
        face_gray,
        scaleFactor=1.1,
        minNeighbors=5,
        minSize=(20, 20)
    )
    
    # Convert eye coordinates to frame coordinates
    eyes_abs = [(x + ex, y + ey, ew, eh) for ex, ey, ew, eh in eyes]
    
    if len(eyes) < 2:
        feedback.append("⚠️ Eyes not clear")
        quality_score -= 40
    else:
        # Check eye distance (should be reasonable)
        if len(eyes) >= 2:
            eye1, eye2 = eyes[0], eyes[1]
            eye_dist = np.sqrt((eye1[0] - eye2[0])**2 + (eye1[1] - eye2[1])**2)
            if eye_dist < 30:
                feedback.append("Face too far")
                quality_score -= 20
    
    # Generate feedback message
    if quality_score >= 85:
        msg = "✅ Perfect! Ready to capture"
    elif quality_score >= 70:
        msg = "✓ Good - " + ", ".join(feedback[:2]) if feedback else "Good"
    elif quality_score >= 50:
        msg = "⚠️ " + ", ".join(feedback[:2])
    else:
        msg = "❌ " + ", ".join(feedback[:3])
    
    return face, eyes_abs, max(0, quality_score), msg


def _draw_professional_overlay(frame, face, eyes, quality_score, feedback):
    """
    Draw professional overlay on frame:
    - Face bounding box with quality color
    - Eye markers
    - Quality indicator
    - Feedback text
    """
    overlay = frame.copy()
    
    # Color based on quality
    if quality_score >= 85:
        color = (0, 255, 0)  # Green
    elif quality_score >= 70:
        color = (0, 255, 255)  # Yellow
    elif quality_score >= 50:
        color = (0, 165, 255)  # Orange
    else:
        color = (0, 0, 255)  # Red
    
    if face is not None:
        x, y, w, h = face
        
        # Draw face rectangle with rounded corners
        thickness = 3
        corner_length = 30
        
        # Top-left corner
        cv2.line(overlay, (x, y), (x + corner_length, y), color, thickness)
        cv2.line(overlay, (x, y), (x, y + corner_length), color, thickness)
        
        # Top-right corner
        cv2.line(overlay, (x + w, y), (x + w - corner_length, y), color, thickness)
        cv2.line(overlay, (x + w, y), (x + w, y + corner_length), color, thickness)
        
        # Bottom-left corner
        cv2.line(overlay, (x, y + h), (x + corner_length, y + h), color, thickness)
        cv2.line(overlay, (x, y + h), (x, y + h - corner_length), color, thickness)
        
        # Bottom-right corner
        cv2.line(overlay, (x + w, y + h), (x + w - corner_length, y + h), color, thickness)
        cv2.line(overlay, (x + w, y + h), (x + w, y + h - corner_length), color, thickness)
        
        # Draw center crosshair
        center_x = x + w // 2
        center_y = y + h // 2
        cv2.drawMarker(overlay, (center_x, center_y), color, cv2.MARKER_CROSS, 20, 2)
        
        # Draw eyes with focus circles
        for ex, ey, ew, eh in eyes[:2]:  # Only first 2 eyes
            eye_center_x = ex + ew // 2
            eye_center_y = ey + eh // 2
            cv2.circle(overlay, (eye_center_x, eye_center_y), ew // 2, (255, 255, 0), 2)
            cv2.circle(overlay, (eye_center_x, eye_center_y), 3, (255, 255, 0), -1)
        
        # Draw quality bar
        bar_width = 200
        bar_height = 20
        bar_x = frame.shape[1] - bar_width - 20
        bar_y = 20
        
        # Background
        cv2.rectangle(overlay, (bar_x, bar_y), (bar_x + bar_width, bar_y + bar_height), (50, 50, 50), -1)
        
        # Quality fill
        fill_width = int(bar_width * quality_score / 100)
        cv2.rectangle(overlay, (bar_x, bar_y), (bar_x + fill_width, bar_y + bar_height), color, -1)
        
        # Quality text
        quality_text = f"Quality: {quality_score}%"
        cv2.putText(overlay, quality_text, (bar_x, bar_y - 5), 
                    cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1, cv2.LINE_AA)
    
    # Draw feedback message
    if feedback:
        # Background rectangle for text
        text_size = cv2.getTextSize(feedback, cv2.FONT_HERSHEY_SIMPLEX, 0.8, 2)[0]
        text_x = (frame.shape[1] - text_size[0]) // 2
        text_y = frame.shape[0] - 30
        
        cv2.rectangle(overlay, 
                      (text_x - 10, text_y - text_size[1] - 10),
                      (text_x + text_size[0] + 10, text_y + 10),
                      (0, 0, 0), -1)
        
        cv2.putText(overlay, feedback, (text_x, text_y), 
                    cv2.FONT_HERSHEY_SIMPLEX, 0.8, (255, 255, 255), 2, cv2.LINE_AA)
    
    # Blend overlay with original frame
    alpha = 0.7
    result = cv2.addWeighted(overlay, alpha, frame, 1 - alpha, 0)
    
    return result


# ── Camera Management ──────────────────────────────────────────────────────────

def _open_camera():
    """Open camera with optimal settings."""
    global _cap
    
    use_dshow = hasattr(cv2, 'CAP_DSHOW')
    
    for idx in range(4):
        try:
            backend = cv2.CAP_DSHOW if use_dshow else cv2.CAP_ANY
            c = cv2.VideoCapture(idx, backend)
            
            if not c.isOpened():
                c.release()
                continue
            
            # Set high-quality capture settings
            c.set(cv2.CAP_PROP_FRAME_WIDTH, FRAME_WIDTH)
            c.set(cv2.CAP_PROP_FRAME_HEIGHT, FRAME_HEIGHT)
            c.set(cv2.CAP_PROP_FPS, FPS)
            c.set(cv2.CAP_PROP_AUTOFOCUS, 1)
            c.set(cv2.CAP_PROP_AUTO_EXPOSURE, 1)
            
            # Warm-up
            time.sleep(0.5)
            for _ in range(15):
                c.read()
            
            ret, frame = c.read()
            if ret and frame is not None and frame.size > 0:
                _cap = c
                actual_w = int(c.get(cv2.CAP_PROP_FRAME_WIDTH))
                actual_h = int(c.get(cv2.CAP_PROP_FRAME_HEIGHT))
                print(f"[CamServer] Camera opened: idx={idx} resolution={actual_w}x{actual_h} DirectShow={use_dshow}", flush=True)
                return True
            
            c.release()
            
        except Exception as e:
            print(f"[CamServer] idx={idx} error: {e}", flush=True)
    
    print("[CamServer] No camera found!", flush=True)
    return False


def _close_camera():
    """Close camera."""
    global _cap
    if _cap is not None:
        try:
            _cap.release()
        except Exception:
            pass
        _cap = None


def _grab_loop():
    """Background thread: continuously grab frames with overlay."""
    global _last_frame, _running
    
    _load_cascades()
    
    while _running:
        with _cap_lock:
            local_cap = _cap
        
        if local_cap is not None and local_cap.isOpened():
            try:
                ret, frame = local_cap.read()
                if ret and frame is not None and frame.size > 0:
                    # Detect face and eyes
                    face, eyes, quality, feedback = _detect_face_and_eyes(frame)
                    
                    # Draw professional overlay
                    frame_with_overlay = _draw_professional_overlay(frame, face, eyes, quality, feedback)
                    
                    with _frame_lock:
                        _last_frame = frame_with_overlay.copy()
            except Exception as e:
                print(f"[CamServer] Grab error: {e}", flush=True)
        
        time.sleep(0.033)  # ~30 fps


def _get_jpeg(quality=JPEG_QUALITY):
    """Get latest frame as JPEG."""
    with _frame_lock:
        frame = _last_frame
    
    if frame is None:
        return None
    
    try:
        ret, buf = cv2.imencode('.jpg', frame, [int(cv2.IMWRITE_JPEG_QUALITY), quality])
        return buf.tobytes() if ret else None
    except Exception:
        return None


def _camera_ok():
    """Check if camera is available."""
    with _cap_lock:
        return _cap is not None and _cap.isOpened()


# ── Client Handler ─────────────────────────────────────────────────────────────

def _handle_client(conn, addr):
    """Handle client connection."""
    buf = b""
    try:
        while True:
            data = conn.recv(8192)
            if not data:
                break
            
            buf += data
            while b"\n" in buf:
                line, buf = buf.split(b"\n", 1)
                cmd = line.decode("utf-8", errors="ignore").strip()
                
                if not cmd:
                    continue
                
                if cmd == "FRAME":
                    jpeg = _get_jpeg()
                    if jpeg:
                        conn.sendall(f"{len(jpeg)}\n".encode() + jpeg)
                    else:
                        conn.sendall(b"0\n")
                
                elif cmd == "STATUS":
                    ok = _camera_ok()
                    conn.sendall((json.dumps({"camera": ok}) + "\n").encode())
                
                elif cmd == "RETRY":
                    with _cap_lock:
                        _close_camera()
                    time.sleep(0.5)
                    with _cap_lock:
                        ok = _open_camera()
                    conn.sendall((json.dumps({"camera": ok}) + "\n").encode())
                
                elif cmd.startswith("{"):
                    # JSON command (face recognition)
                    try:
                        req = json.loads(cmd)
                        # Forward to face recognition service
                        result = {"success": False, "error": "Not implemented in camera server"}
                    except Exception as e:
                        result = {"success": False, "error": str(e)}
                    conn.sendall((json.dumps(result) + "\n").encode())
    
    except Exception as e:
        print(f"[CamServer] Client {addr} error: {e}", flush=True)
    finally:
        try:
            conn.close()
        except Exception:
            pass


# ── Main ───────────────────────────────────────────────────────────────────────

def main():
    """Start camera server."""
    global _running
    
    if not CV2_OK:
        print("[CamServer] ERROR: opencv-python not installed.", flush=True)
        print("[CamServer] Run: pip install opencv-python", flush=True)
        sys.exit(1)
    
    print(f"[CamServer] Professional Camera Server starting on port {PORT}...", flush=True)
    
    with _cap_lock:
        cam_ok = _open_camera()
    
    if cam_ok:
        t = threading.Thread(target=_grab_loop, daemon=True)
        t.start()
    
    srv = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    srv.bind(("127.0.0.1", PORT))
    srv.listen(20)
    
    print(f"[CamServer] ✅ Ready on 127.0.0.1:{PORT} | Camera={'OK' if cam_ok else 'UNAVAILABLE'}", flush=True)
    print(f"[CamServer] Resolution: {FRAME_WIDTH}x{FRAME_HEIGHT} @ {FPS}fps", flush=True)
    sys.stdout.flush()
    
    try:
        while True:
            conn, addr = srv.accept()
            threading.Thread(target=_handle_client, args=(conn, addr), daemon=True).start()
    except KeyboardInterrupt:
        pass
    finally:
        _running = False
        srv.close()
        with _cap_lock:
            _close_camera()
        print("[CamServer] Shutdown complete", flush=True)


if __name__ == "__main__":
    main()
