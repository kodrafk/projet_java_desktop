#!/usr/bin/env python3
"""
Camera server for NutriLife Face ID.
Opens the webcam with OpenCV and serves JPEG frames via a local TCP socket.

Protocol (newline-delimited):
  Client → "FRAME\n"       Server → "<length>\n<jpeg_bytes>"
  Client → "STATUS\n"      Server → JSON {"camera": true/false} + "\n"
  Client → "RETRY\n"       Server → JSON {"camera": true/false} + "\n"
  Client → JSON + "\n"     Server → JSON result + "\n"  (face recognition)

Usage: python camera_server.py [port]
Default port: 7654
"""

import sys
import os
import socket
import threading
import json
import base64
import time

try:
    import cv2
    import numpy as np
    CV2_OK = True
except ImportError:
    CV2_OK = False

PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 7654
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))

# ── Shared camera state ────────────────────────────────────────────────────────

_cap       = None          # cv2.VideoCapture
_cap_lock  = threading.Lock()
_last_frame = None
_frame_lock = threading.Lock()
_running    = True

# ── Camera management ──────────────────────────────────────────────────────────

def _open_camera():
    """Try to open the first available camera using DirectShow (Windows)."""
    global _cap
    # On Windows, always use DirectShow — it works even when MSMF is busy
    use_dshow = hasattr(cv2, 'CAP_DSHOW')

    for idx in range(4):
        try:
            backend = cv2.CAP_DSHOW if use_dshow else cv2.CAP_ANY
            c = cv2.VideoCapture(idx, backend)
            if not c.isOpened():
                c.release()
                continue
            c.set(cv2.CAP_PROP_FRAME_WIDTH,  640)
            c.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
            c.set(cv2.CAP_PROP_FPS, 30)
            # Give camera time to initialize, then do warm-up reads
            time.sleep(0.3)
            for _ in range(10):
                c.read()
            ret, frame = c.read()
            if ret and frame is not None and frame.size > 0:
                _cap = c
                print(f"[CamServer] Camera opened: index={idx} DirectShow={use_dshow}", flush=True)
                return True
            c.release()
        except Exception as e:
            print(f"[CamServer] idx={idx} error: {e}", flush=True)

    print("[CamServer] No camera found!", flush=True)
    return False

def _close_camera():
    global _cap
    if _cap is not None:
        try:
            _cap.release()
        except Exception:
            pass
        _cap = None

def _grab_loop():
    """Background thread: continuously grab frames into _last_frame."""
    global _last_frame, _running
    while _running:
        with _cap_lock:
            local_cap = _cap
        if local_cap is not None and local_cap.isOpened():
            try:
                ret, frame = local_cap.read()
                if ret and frame is not None and frame.size > 0:
                    with _frame_lock:
                        _last_frame = frame.copy()
            except Exception:
                pass
        time.sleep(0.033)  # ~30 fps

def _get_jpeg(quality=85):
    """Return latest frame as JPEG bytes, or None."""
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
    with _cap_lock:
        return _cap is not None and _cap.isOpened()

# ── Face recognition (lazy-loaded) ────────────────────────────────────────────

_frs = None
_frs_lock = threading.Lock()

def _get_frs():
    global _frs
    with _frs_lock:
        if _frs is None:
            import importlib.util
            path = os.path.join(SCRIPT_DIR, "face_recognition_service.py")
            spec = importlib.util.spec_from_file_location("frs", path)
            mod  = importlib.util.module_from_spec(spec)
            spec.loader.exec_module(mod)
            _frs = mod
        return _frs

# ── Client handler ─────────────────────────────────────────────────────────────

def _handle_client(conn, addr):
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
                    try:
                        req = json.loads(cmd)
                        frs = _get_frs()
                        c = req.get("command", "")
                        if c == "encode":
                            result = frs.cmd_encode(req)
                        elif c == "compare":
                            result = frs.cmd_compare(req)
                        elif c == "encode_live":
                            jpeg = _get_jpeg(quality=90)
                            if jpeg is None:
                                result = {"success": False, "error": "No camera frame"}
                            else:
                                b64 = base64.b64encode(jpeg).decode()
                                result = frs.cmd_encode({"command": "encode", "image": b64})
                        else:
                            result = {"success": False, "error": f"Unknown command: {c}"}
                    except Exception as e:
                        import traceback
                        result = {"success": False, "error": str(e), "trace": traceback.format_exc()}
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
    global _running

    if not CV2_OK:
        print("[CamServer] ERROR: opencv-python not installed.", flush=True)
        print("[CamServer] Run: pip install opencv-python", flush=True)
        sys.exit(1)

    print(f"[CamServer] Starting on port {PORT}...", flush=True)

    with _cap_lock:
        cam_ok = _open_camera()

    if cam_ok:
        t = threading.Thread(target=_grab_loop, daemon=True)
        t.start()

    srv = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    srv.bind(("127.0.0.1", PORT))
    srv.listen(20)
    print(f"[CamServer] Ready on 127.0.0.1:{PORT} | camera={'OK' if cam_ok else 'UNAVAILABLE'}", flush=True)
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

if __name__ == "__main__":
    main()
