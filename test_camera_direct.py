#!/usr/bin/env python3
"""
Test direct de la caméra - FORCE l'ouverture
"""
import cv2
import time
import sys

print("=== TEST CAMERA DIRECT ===")
print()

# Test 1: DirectShow (Windows)
print("1. Test avec DirectShow...")
cap = cv2.VideoCapture(0, cv2.CAP_DSHOW)
if cap.isOpened():
    print("   ✅ DirectShow OK")
    cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
    cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
    
    # Warm-up
    for i in range(10):
        ret, frame = cap.read()
        if ret:
            print(f"   ✅ Frame {i+1}: {frame.shape}")
        else:
            print(f"   ❌ Frame {i+1}: FAILED")
        time.sleep(0.1)
    
    cap.release()
else:
    print("   ❌ DirectShow FAILED")

print()

# Test 2: Default backend
print("2. Test avec backend par défaut...")
cap = cv2.VideoCapture(0)
if cap.isOpened():
    print("   ✅ Default backend OK")
    for i in range(5):
        ret, frame = cap.read()
        if ret:
            print(f"   ✅ Frame {i+1}: {frame.shape}")
            # Sauvegarder la première image
            if i == 0:
                cv2.imwrite("test_frame.jpg", frame)
                print("   💾 Image sauvée: test_frame.jpg")
        else:
            print(f"   ❌ Frame {i+1}: FAILED")
        time.sleep(0.1)
    cap.release()
else:
    print("   ❌ Default backend FAILED")

print()

# Test 3: Tous les indices
print("3. Test de tous les indices de caméra...")
for idx in range(5):
    cap = cv2.VideoCapture(idx, cv2.CAP_DSHOW)
    if cap.isOpened():
        ret, frame = cap.read()
        if ret and frame is not None:
            print(f"   ✅ Caméra {idx}: {frame.shape}")
        else:
            print(f"   ⚠️  Caméra {idx}: ouverte mais pas de frame")
        cap.release()
    else:
        print(f"   ❌ Caméra {idx}: fermée")

print()
print("=== FIN DU TEST ===")

# Lancer le serveur caméra
print()
print("4. Lancement du serveur caméra...")
import subprocess
try:
    subprocess.Popen([sys.executable, "camera_server.py"], 
                    creationflags=subprocess.CREATE_NEW_CONSOLE if sys.platform == "win32" else 0)
    print("   ✅ Serveur lancé!")
except Exception as e:
    print(f"   ❌ Erreur: {e}")