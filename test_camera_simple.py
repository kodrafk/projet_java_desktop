#!/usr/bin/env python3
"""
Test simple de la caméra
Affiche une fenêtre avec le flux vidéo
"""

import sys

try:
    import cv2
except ImportError:
    print("❌ OpenCV n'est pas installé")
    print("💡 Installez-le avec : pip install opencv-python")
    sys.exit(1)

print("🔍 Test de la caméra...")
print()

# Essayer différents backends
backends = [
    ("DirectShow (Windows)", cv2.CAP_DSHOW if hasattr(cv2, 'CAP_DSHOW') else None),
    ("MSMF (Windows)", cv2.CAP_MSMF if hasattr(cv2, 'CAP_MSMF') else None),
    ("Auto", cv2.CAP_ANY),
]

cap = None
backend_name = None

for name, backend in backends:
    if backend is None:
        continue
    
    print(f"Essai avec {name}...")
    cap = cv2.VideoCapture(0, backend)
    
    if cap.isOpened():
        ret, frame = cap.read()
        if ret and frame is not None and frame.size > 0:
            backend_name = name
            print(f"✅ Caméra ouverte avec {name}")
            break
        else:
            print(f"⚠️ {name} : Caméra ouverte mais pas de frame")
            cap.release()
            cap = None
    else:
        print(f"❌ {name} : Impossible d'ouvrir la caméra")
        cap.release()
        cap = None

if cap is None or not cap.isOpened():
    print()
    print("❌ ÉCHEC : Impossible d'ouvrir la caméra")
    print()
    print("💡 SOLUTIONS :")
    print("   1. Fermez Skype, Teams, Zoom, Discord")
    print("   2. Vérifiez les permissions Windows :")
    print("      Paramètres → Confidentialité → Caméra")
    print("      → Activez 'Autoriser les applications de bureau'")
    print("   3. Redémarrez votre ordinateur")
    print("   4. Vérifiez que la caméra n'est pas physiquement couverte")
    sys.exit(1)

# Obtenir les propriétés de la caméra
width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
fps = int(cap.get(cv2.CAP_PROP_FPS))

print()
print(f"📹 Caméra configurée :")
print(f"   Backend : {backend_name}")
print(f"   Résolution : {width}x{height}")
print(f"   FPS : {fps}")
print()
print("🎥 Affichage du flux vidéo...")
print("   Appuyez sur 'q' ou 'ESC' pour quitter")
print()

# Créer une fenêtre
cv2.namedWindow('Test Caméra', cv2.WINDOW_NORMAL)

frame_count = 0
while True:
    ret, frame = cap.read()
    
    if not ret or frame is None:
        print(f"⚠️ Erreur de lecture frame {frame_count}")
        break
    
    frame_count += 1
    
    # Ajouter du texte sur l'image
    text = f"Frame: {frame_count} | Backend: {backend_name}"
    cv2.putText(frame, text, (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)
    
    text2 = "Appuyez sur 'q' ou 'ESC' pour quitter"
    cv2.putText(frame, text2, (10, 60), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)
    
    # Afficher l'image
    cv2.imshow('Test Caméra', frame)
    
    # Attendre une touche
    key = cv2.waitKey(1) & 0xFF
    if key == ord('q') or key == 27:  # 'q' ou ESC
        break

# Nettoyer
cap.release()
cv2.destroyAllWindows()

print()
print(f"✅ Test terminé - {frame_count} frames capturées")
print()

if frame_count > 0:
    print("🎉 SUCCÈS : La caméra fonctionne correctement !")
    print()
    print("💡 Si la caméra reste noire dans l'application Java :")
    print("   1. Vérifiez que le serveur Python démarre (camera_server.py)")
    print("   2. Vérifiez les logs Java pour les erreurs")
    print("   3. Essayez de relancer l'application")
else:
    print("❌ ÉCHEC : Aucune frame capturée")
    print()
    print("💡 La caméra s'ouvre mais ne capture pas d'images")
    print("   Essayez de redémarrer votre ordinateur")
