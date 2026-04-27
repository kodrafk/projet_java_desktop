# Face ID Troubleshooting Guide

## 🔴 Problem: Face ID Not Recognized / Already Exists Error

### Symptoms:
1. ❌ **Login with Face ID fails** - "Face not recognized"
2. ❌ **Creating account with Face ID fails** - "This face is already registered"
3. ❌ **Enrolling Face ID fails** - "This face already exists"

### Root Cause:
This happens when there's an inconsistency in the Face ID database:
- Your face is enrolled but the system can't match it correctly
- The embedding is corrupted
- There's a duplicate enrollment
- The similarity threshold is too strict

---

## 🛠️ SOLUTION 1: Run Diagnostic Tool (Recommended)

### Step 1: Run Diagnostic
```bash
Double-click: FACE_ID_DIAGNOSTIC.bat
```

This will show you:
- ✅ All Face ID enrollments in the database
- ✅ Which users have Face ID enrolled
- ✅ If there are any corrupted or duplicate enrollments
- ✅ Recommendations for fixing issues

### Step 2: Analyze Results

**If you see:**
```
⚠️ DUPLICATE FACE DETECTED!
User 1: user1@email.com
User 2: user2@email.com
Similarity: 85%
```
→ **Action:** Delete one of the Face IDs (see Solution 2)

**If you see:**
```
Decryption: ❌ FAILED
```
→ **Action:** This enrollment is corrupted, delete it (see Solution 2)

**If you see:**
```
⚠️ Orphaned Face ID for user ID: 123 (user not found)
```
→ **Action:** Delete this orphaned enrollment (see Solution 3)

---

## 🛠️ SOLUTION 2: Delete Specific Face ID (Via Admin Dashboard)

### For Your Own Account:
1. Login as admin
2. Go to **User Management**
3. Find your user account
4. Click **Edit** (✏️ button)
5. In the **Face ID Authentication** section, click **🗑️ Delete Face ID**
6. Confirm deletion
7. Now you can re-enroll your Face ID

### For Another User's Account:
1. Login as admin
2. Go to **User Management**
3. Find the user
4. Click **Edit** (✏️ button)
5. Click **🗑️ Delete Face ID**
6. The user can now re-enroll

---

## 🛠️ SOLUTION 3: Reset Entire Face ID System

⚠️ **WARNING:** This will delete ALL Face ID enrollments for ALL users!

### When to use:
- Multiple users have Face ID issues
- Database is corrupted
- You want to start fresh

### Steps:
```bash
Double-click: FACE_ID_RESET.bat
Type: YES
Press Enter
```

This will:
- ✅ Delete all Face ID enrollments
- ✅ Clean the database
- ✅ Allow everyone to re-enroll

---

## 🛠️ SOLUTION 4: Manual Database Cleanup

### Delete ALL Face ID enrollments:
```sql
DELETE FROM face_embeddings;
```

### Delete Face ID for specific user:
```sql
DELETE FROM face_embeddings WHERE user_id = [USER_ID];
```

### Delete Face ID for specific email:
```sql
DELETE FROM face_embeddings 
WHERE user_id = (SELECT id FROM user WHERE email = 'user@email.com');
```

### Check all enrollments:
```sql
SELECT fe.id, fe.user_id, u.email, u.first_name, u.last_name
FROM face_embeddings fe
LEFT JOIN user u ON fe.user_id = u.id;
```

---

## 🛠️ SOLUTION 5: Adjust Similarity Threshold

If Face ID is **too strict** (not recognizing you):

### Edit: `FaceIdVerifyController.java`
```java
// Line ~150
if (similarity > 0.6) {  // Change to 0.5 for more lenient
```

### Edit: `FaceIdEnrollController.java`
```java
// Line ~250
if (similarity > 0.6) {  // Change to 0.7 for less strict duplicate detection
```

**Recommended values:**
- `0.5` - Very lenient (may allow different people)
- `0.6` - Balanced (recommended)
- `0.7` - Strict (may reject same person)

---

## 📋 Step-by-Step Fix for Your Specific Problem

### Your Issue:
1. ❌ Login with Face ID → Not recognized
2. ❌ Create account with Face ID → "Already exists"

### Solution:
```
STEP 1: Run diagnostic
→ Double-click FACE_ID_DIAGNOSTIC.bat
→ Check which user has your face enrolled

STEP 2: Delete the existing enrollment
Option A: Via Admin Dashboard
  → Login as admin
  → User Management → Edit user → Delete Face ID

Option B: Via Reset Tool
  → Double-click FACE_ID_RESET.bat
  → Type YES

STEP 3: Re-enroll your Face ID
→ Login with password
→ Go to Profile → Enroll Face ID
OR
→ Create new account with Face ID

STEP 4: Test
→ Logout
→ Try Face ID login
→ Should work now! ✅
```

---

## 🔍 Prevention Tips

1. **Always use good lighting** when enrolling Face ID
2. **Look directly at camera** during enrollment
3. **Don't enroll with glasses/mask** if you don't usually wear them
4. **Re-enroll if you change appearance** significantly
5. **One face = one account** - don't try to use same face for multiple accounts

---

## 📞 Still Having Issues?

### Check Logs:
Look for these messages in console:
```
[Security] Could not decrypt embedding for user X
[FaceDB] ❌ Face tables ready
[Twilio] ❌ Failed to send SMS
```

### Common Errors:

**"No face detected"**
→ Ensure good lighting, face camera directly

**"Multiple faces detected"**
→ Ensure only one person in frame

**"Face too far/close"**
→ Position face in the oval guide

**"Similarity: 0.58 (below threshold)"**
→ Your face changed or lighting is different
→ Try re-enrolling or adjust threshold

---

## 🎯 Quick Commands

### Run Diagnostic:
```bash
FACE_ID_DIAGNOSTIC.bat
```

### Reset System:
```bash
FACE_ID_RESET.bat
```

### Check Database:
```bash
mysql -u root -p nutrilife_db
SELECT * FROM face_embeddings;
```

---

**Last Updated:** April 25, 2026  
**Version:** 1.0
