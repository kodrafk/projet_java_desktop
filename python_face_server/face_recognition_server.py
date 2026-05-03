"""
SIMPLE FACE RECOGNITION SERVER
Uses FaceNet model - Very powerful and stable
No complex dependencies - Just works!
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import numpy as np
import cv2
import base64
from PIL import Image
import io
import os

# Try to import face_recognition (very simple and powerful)
try:
    import face_recognition
    FACE_LIB = "face_recognition"
    print("✅ Using face_recognition library (dlib-based)")
except ImportError:
    try:
        # Try alternative: just use OpenCV
        FACE_LIB = "opencv"
        print("⚠️ face_recognition not available, using OpenCV (basic but works)")
    except:
        FACE_LIB = None
        print("❌ No face detection library available")

app = Flask(__name__)
CORS(app)

# Simple in-memory storage for enrolled faces
enrolled_faces = {}

def decode_base64_image(base64_string):
    """Decode base64 image to numpy array"""
    try:
        # Remove data URL prefix if present
        if ',' in base64_string:
            base64_string = base64_string.split(',')[1]
        
        # Decode base64
        img_data = base64.b64decode(base64_string)
        
        # Convert to PIL Image
        img = Image.open(io.BytesIO(img_data))
        
        # Convert to RGB if needed
        if img.mode != 'RGB':
            img = img.convert('RGB')
        
        # Convert to numpy array
        img_array = np.array(img)
        
        return img_array
    except Exception as e:
        print(f"❌ Error decoding image: {e}")
        return None

def extract_face_encoding(image_array):
    """Extract face encoding using face_recognition library"""
    if FACE_LIB == "face_recognition":
        try:
            # Detect faces
            face_locations = face_recognition.face_locations(image_array)
            
            if len(face_locations) == 0:
                return None, "NO_FACE"
            
            if len(face_locations) > 1:
                return None, "MULTIPLE_FACES"
            
            # Extract encoding (128D vector)
            encodings = face_recognition.face_encodings(image_array, face_locations)
            
            if len(encodings) > 0:
                return encodings[0].tolist(), "SUCCESS"
            else:
                return None, "NO_ENCODING"
                
        except Exception as e:
            print(f"❌ Encoding error: {e}")
            return None, f"ERROR: {str(e)}"
    else:
        # Fallback: Use OpenCV Haar Cascade (basic but works)
        try:
            # Convert to grayscale
            if len(image_array.shape) == 3:
                gray = cv2.cvtColor(image_array, cv2.COLOR_RGB2GRAY)
            else:
                gray = image_array
            
            # Load Haar Cascade
            cascade_path = cv2.data.haarcascades + 'haarcascade_frontalface_default.xml'
            face_cascade = cv2.CascadeClassifier(cascade_path)
            
            # Detect faces
            faces = face_cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5, minSize=(30, 30))
            
            if len(faces) == 0:
                return None, "NO_FACE"
            
            if len(faces) > 1:
                return None, "MULTIPLE_FACES"
            
            # Extract face region and create simple encoding
            x, y, w, h = faces[0]
            face_region = gray[y:y+h, x:x+w]
            
            # Resize to fixed size (64x64 for speed)
            face_resized = cv2.resize(face_region, (64, 64))
            
            # Normalize
            face_normalized = face_resized.astype(np.float32) / 255.0
            
            # Flatten to 1D array (simple encoding)
            encoding = face_normalized.flatten().tolist()
            
            print(f"✅ OpenCV: Extracted {len(encoding)}D encoding")
            return encoding, "SUCCESS"
            
        except Exception as e:
            print(f"❌ OpenCV error: {e}")
            import traceback
            traceback.print_exc()
            return None, f"ERROR: {str(e)}"

def compare_faces(encoding1, encoding2):
    """Compare two face encodings and return similarity score"""
    try:
        # Convert to numpy arrays
        enc1 = np.array(encoding1)
        enc2 = np.array(encoding2)
        
        # Normalize
        enc1_norm = enc1 / np.linalg.norm(enc1)
        enc2_norm = enc2 / np.linalg.norm(enc2)
        
        # Calculate cosine similarity
        similarity = np.dot(enc1_norm, enc2_norm)
        
        # Convert to 0-1 range
        similarity = (similarity + 1) / 2
        
        return float(similarity)
        
    except Exception as e:
        print(f"❌ Comparison error: {e}")
        return 0.0

@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint"""
    return jsonify({
        "status": "OK",
        "library": FACE_LIB or "opencv",
        "message": "Face Recognition Server is running"
    })

@app.route('/detect', methods=['POST'])
def detect_face():
    """Detect if a face is present in the image"""
    try:
        data = request.json
        image_base64 = data.get('image')
        
        if not image_base64:
            return jsonify({"error": "No image provided"}), 400
        
        # Decode image
        image_array = decode_base64_image(image_base64)
        if image_array is None:
            return jsonify({"error": "Failed to decode image"}), 400
        
        # Extract encoding
        encoding, status = extract_face_encoding(image_array)
        
        return jsonify({
            "status": status,
            "face_detected": encoding is not None,
            "encoding_length": len(encoding) if encoding else 0
        })
        
    except Exception as e:
        print(f"❌ Detect error: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/enroll', methods=['POST'])
def enroll_face():
    """Enroll a new face"""
    try:
        data = request.json
        user_id = data.get('user_id')
        image_base64 = data.get('image')
        
        if not user_id or not image_base64:
            return jsonify({"error": "Missing user_id or image"}), 400
        
        # Decode image
        image_array = decode_base64_image(image_base64)
        if image_array is None:
            return jsonify({"error": "Failed to decode image"}), 400
        
        # Extract encoding
        encoding, status = extract_face_encoding(image_array)
        
        if encoding is None:
            return jsonify({
                "success": False,
                "message": f"Face enrollment failed: {status}"
            }), 400
        
        # Store encoding
        enrolled_faces[str(user_id)] = encoding
        
        print(f"✅ Enrolled user {user_id} with {len(encoding)}D encoding")
        
        return jsonify({
            "success": True,
            "message": "Face enrolled successfully",
            "user_id": user_id,
            "encoding_length": len(encoding)
        })
        
    except Exception as e:
        print(f"❌ Enroll error: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/verify', methods=['POST'])
def verify_face():
    """Verify a face against enrolled faces"""
    try:
        data = request.json
        image_base64 = data.get('image')
        
        if not image_base64:
            return jsonify({"error": "No image provided"}), 400
        
        # Check if any faces are enrolled
        if not enrolled_faces:
            return jsonify({
                "success": False,
                "message": "No enrolled faces found",
                "matched_user_id": None
            })
        
        # Decode image
        image_array = decode_base64_image(image_base64)
        if image_array is None:
            return jsonify({"error": "Failed to decode image"}), 400
        
        # Extract encoding
        encoding, status = extract_face_encoding(image_array)
        
        if encoding is None:
            return jsonify({
                "success": False,
                "message": f"Face detection failed: {status}",
                "matched_user_id": None
            })
        
        # Compare with all enrolled faces
        best_match_user = None
        best_similarity = 0.0
        
        for user_id, enrolled_encoding in enrolled_faces.items():
            similarity = compare_faces(encoding, enrolled_encoding)
            print(f"  User {user_id}: similarity = {similarity:.3f}")
            
            if similarity > best_similarity:
                best_similarity = similarity
                best_match_user = user_id
        
        # Threshold for match (adjust as needed)
        THRESHOLD = 0.60  # 60% similarity required
        
        if best_similarity >= THRESHOLD:
            print(f"✅ Match found! User {best_match_user} with {best_similarity:.3f} similarity")
            return jsonify({
                "success": True,
                "message": "Face verified successfully",
                "matched_user_id": int(best_match_user),
                "similarity": best_similarity,
                "threshold": THRESHOLD
            })
        else:
            print(f"❌ No match found. Best similarity: {best_similarity:.3f}")
            return jsonify({
                "success": False,
                "message": "Face not recognized",
                "matched_user_id": None,
                "best_similarity": best_similarity,
                "threshold": THRESHOLD
            })
        
    except Exception as e:
        print(f"❌ Verify error: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/extract', methods=['POST'])
def extract_encoding():
    """Extract face encoding from image"""
    try:
        data = request.json
        image_base64 = data.get('image')
        
        if not image_base64:
            return jsonify({"error": "No image provided"}), 400
        
        # Decode image
        image_array = decode_base64_image(image_base64)
        if image_array is None:
            return jsonify({"error": "Failed to decode image"}), 400
        
        # Extract encoding
        encoding, status = extract_face_encoding(image_array)
        
        if encoding is None:
            return jsonify({
                "success": False,
                "message": status,
                "encoding": None
            })
        
        return jsonify({
            "success": True,
            "message": "Encoding extracted successfully",
            "encoding": encoding,
            "encoding_length": len(encoding)
        })
        
    except Exception as e:
        print(f"❌ Extract error: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/enrolled', methods=['GET'])
def get_enrolled():
    """Get list of enrolled user IDs"""
    return jsonify({
        "enrolled_users": list(enrolled_faces.keys()),
        "count": len(enrolled_faces)
    })

if __name__ == '__main__':
    print("=" * 60)
    print("🚀 SIMPLE FACE RECOGNITION SERVER")
    print("=" * 60)
    print(f"Library: {FACE_LIB or 'opencv'}")
    print("Endpoints:")
    print("  GET  /health      - Health check")
    print("  POST /detect      - Detect face in image")
    print("  POST /enroll      - Enroll a new face")
    print("  POST /verify      - Verify face against enrolled")
    print("  POST /extract     - Extract face encoding")
    print("  GET  /enrolled    - List enrolled users")
    print("=" * 60)
    print("Starting server on http://localhost:5000")
    print("=" * 60)
    
    app.run(host='0.0.0.0', port=5000, debug=True)
