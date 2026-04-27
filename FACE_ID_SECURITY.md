# Face ID Security - Duplicate Detection

## 🔒 Security Feature: One Face = One Account

### Overview
The application enforces a strict security policy: **Each face can only be registered to ONE account**. This prevents:
- Identity fraud
- Multiple account creation with same face
- Account impersonation

### How It Works

#### 1. **During Registration (Face ID Sign Up)**
When a user creates an account with Face ID:
1. User fills in registration form (email, password, name, etc.)
2. User clicks "Face ID Sign Up"
3. **IMPORTANT:** Account is NOT created yet
4. Camera opens and captures 3 face images
5. System generates face embedding
6. **SECURITY CHECK:** Compares with ALL existing faces in database
7. If similarity > 60% with any existing face:
   - ❌ **REJECT** - Account creation is CANCELLED
   - Show alert: "This face is already registered to [User Name]"
   - User must use different registration method
8. If face is unique:
   - ✅ **ALLOW** - Create account AND save Face ID
   - User can now login with Face ID

#### 2. **During Face ID Enrollment (Existing Account)**
When a user adds Face ID to existing account:
1. User opens Face ID enrollment
2. Camera captures 3 face images
3. System generates face embedding
4. **SECURITY CHECK:** Compares with ALL existing faces
5. If similarity > 60% (excluding same user):
   - ❌ **REJECT** - Enrollment cancelled
   - Show alert with existing user name
6. If unique OR same user (re-enrollment):
   - ✅ **ALLOW** - Save Face ID

### Security Benefits

✅ **Prevents Account Fraud**
- One person cannot create multiple accounts with same face
- Even if they use different emails/passwords

✅ **Registration Protection**
- Account is NOT created if face already exists
- No orphan accounts with duplicate faces

✅ **Enrollment Protection**
- Cannot add someone else's face to your account
- Cannot steal someone's Face ID

✅ **Transparency**
- System shows which user already has the face
- Clear security alerts

### Technical Implementation

#### Files Created/Modified:

1. **FaceIdRegisterController.java** (NEW)
   - Handles Face ID registration
   - Verifies face uniqueness BEFORE creating account
   - Only creates account if face is unique

2. **FaceIdEnrollController.java** (MODIFIED)
   - Added duplicate detection for existing accounts
   - Allows re-enrollment for same user

3. **RegisterController.java** (MODIFIED)
   - Uses FaceIdRegisterController for Face ID sign up
   - Passes pending user data (not saved yet)

4. **FaceEmbeddingRepository.java** (MODIFIED)
   - Added `findAllActiveEmbeddings()` method
   - Returns all enrolled faces for comparison

5. **face_id_register.fxml** (NEW)
   - FXML for registration flow
   - Uses FaceIdRegisterController

### User Experience

#### Registration with Duplicate Face:
```
Step 1: User fills registration form
Step 2: Clicks "Face ID Sign Up"
Step 3: Camera captures face
Step 4: System detects duplicate

❌ This face is already registered!

⚠️ SECURITY ALERT: This face is already registered to another account (John Doe).
Each face can only be used for ONE account. Account creation cancelled.

[Window closes after 3 seconds]
[User returns to registration form]
[Account was NOT created]
```

#### Enrollment with Duplicate Face:
```
❌ Security Alert: This face is already registered!

⚠️ This face is already enrolled for another account (Jane Smith).
Each face can only be used for one account.

[Can retry with different face]
```

### Configuration

#### Adjusting Similarity Threshold:
To change the duplicate detection sensitivity, modify this line in `FaceIdEnrollController.java`:

```java
if (similarity > 0.6) {  // Change 0.6 to your desired threshold
```

**Recommended values:**
- `0.5` - Very strict (may reject similar-looking people)
- `0.6` - Balanced (recommended)
- `0.7` - Lenient (may allow same person twice)

### Testing

#### Test Scenarios:
1. ✅ Enroll User A with Face X → Success
2. ❌ Enroll User B with Face X → Rejected (duplicate)
3. ✅ Re-enroll User A with Face X → Success (same user)
4. ✅ Enroll User B with Face Y → Success (different face)

### Logging

The system logs duplicate detection attempts:
```
[Security] Could not decrypt embedding for user 123: ...
```

### Future Enhancements

Potential improvements:
- [ ] Log all duplicate detection attempts to database
- [ ] Send email alerts on duplicate attempts
- [ ] Add admin dashboard for security events
- [ ] Implement rate limiting for enrollment attempts
- [ ] Add CAPTCHA for repeated failed enrollments

### Compliance

This feature helps with:
- **GDPR**: Prevents unauthorized biometric data collection
- **Security Standards**: One face = one account principle
- **Fraud Prevention**: Reduces identity theft risk

---

**Last Updated:** April 25, 2026
**Version:** 1.0
