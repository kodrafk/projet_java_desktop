#!/usr/bin/env python3
"""
Serveur caméra FORCE - Version simplifiée qui FORCE l'ouverture
"""
import cv2
import socket
import threading
import time
import sys

PORT = 7654

# Variables globales
cap = None
cap_lock = threading.Lock()
last_frame = None
frame_lock = threading.Lock()

def force_open_camera():
    """FORCE l'ouverture de la caméra avec tous les moyens possibles"""
    global cap
    
    print("[FORCE] Tentative d'ouverture FORCÉE de la caméra...")
    
    # Méthode 1: DirectShow avec index 0
    try:
        cap = cv2.VideoCapture(0, cv2.CAP_DSHOW)
        if cap.isOpened():
            cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
            cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
            cap.set(cv2.CAP_PROP_FPS, 30)
            
            # WARM-UP AGRESSIF
            print("[FORCE] Warm-up agressif...")
            for i in range(30):
                ret, frame = cap.read()
                if ret and frame is not None:
                    print(f"[FORCE] ✅ Frame {i+1} OK: {frame.shape}")
                    return True
                time.sleep(0.05)
            
            cap.release()
    except Exception as e:
        print(f"[FORCE] DirectShow failed: {e}")
    
    # Méthode 2: Backend par défaut
    try:
        cap = cv2.VideoCapture(0)
        if cap.isOpened():
            cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
            cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
            
            for i in range(20):
                ret, frame = cap.read()
                if ret and frame is not None:
                    print(f"[FORCE] ✅ Default backend OK: {frame.shape}")
                    return True
                time.sleep(0.1)
            
            cap.release()
    except Exception as e:
        print(f"[FORCE] Default backend failed: {e}")
    
    # Méthode 3: Essayer tous les indices
    for idx in range(5):
        try:
            cap = cv2.VideoCapture(idx, cv2.CAP_DSHOW)
            if cap.isOpened():
                ret, frame = cap.read()
                if ret and frame is not None:
                    print(f"[FORCE] ✅ Caméra {idx} OK: {frame.shape}")
                    return True
                cap.release()
        except Exception:
            pass
    
    print("[FORCE] ❌ AUCUNE CAMÉRA TROUVÉE!")
    return False

def grab_loop():
    """Boucle de capture continue"""
    global last_frame
    while True:
        if cap and cap.isOpened():
            try:
                ret, frame = cap.read()
                if ret and frame is not None:
                    with frame_lock:
                        last_frame = frame.copy()
            except Exception:
                pass
        time.sleep(0.033)  # 30 FPS

def handle_client(conn, addr):
    """Gestion des clients"""
    try:
        while True:
            data = conn.recv(1024)
            if not data:
                break
            
            cmd = data.decode().strip()
            
            if cmd == "FRAME":
                with frame_lock:
                    frame = last_frame
                
                if frame is not None:
                    ret, jpeg = cv2.imencode('.jpg', frame)
                    if ret:
                        jpeg_bytes = jpeg.tobytes()
                        conn.sendall(f"{len(jpeg_bytes)}\n".encode() + jpeg_bytes)
                    else:
                        conn.sendall(b"0\n")
                else:
                    conn.sendall(b"0\n")
            
            elif cmd == "STATUS":
                ok = cap is not None and cap.isOpened()
                conn.sendall(f'{{"camera": {str(ok).lower()}}}\n'.encode())
            
            elif cmd == "RETRY":
                with cap_lock:
                    if cap:
                        cap.release()
                    ok = force_open_camera()
                conn.sendall(f'{{"camera": {str(ok).lower()}}}\n'.encode())
    
    except Exception as e:
        print(f"[FORCE] Client error: {e}")
    finally:
        conn.close()

def main():
    print("[FORCE] ========================================")
    print("[FORCE]    SERVEUR CAMÉRA FORCE v1.0")
    print("[FORCE] ========================================")
    
    # FORCE l'ouverture de la caméra
    if not force_open_camera():
        print("[FORCE] ERREUR: Impossible d'ouvrir la caméra!")
        sys.exit(1)
    
    # Démarrer la boucle de capture
    threading.Thread(target=grab_loop, daemon=True).start()
    
    # Serveur TCP
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind(("127.0.0.1", PORT))
    server.listen(10)
    
    print(f"[FORCE] ✅ Serveur prêt sur port {PORT}")
    print(f"[FORCE] ✅ Caméra: {'OK' if cap and cap.isOpened() else 'ERREUR'}")
    
    try:
        while True:
            conn, addr = server.accept()
            threading.Thread(target=handle_client, args=(conn, addr), daemon=True).start()
    except KeyboardInterrupt:
        pass
    finally:
        if cap:
            cap.release()
        server.close()

if __name__ == "__main__":
    main()