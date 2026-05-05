package tn.esprit.projet.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

public class User {
    private int           id;
    private String        email;
    private String        password;
    private String        roles = "ROLE_USER";
    private boolean       isActive;
    private LocalDateTime createdAt;
    private String        firstName;
    private String        lastName;
    private LocalDate     birthday;
    private float         weight;
    private float         height;
    private String        phoneNumber;
    private boolean       phoneVerified;
    private String        photoFilename;
    private String        welcomeMessage;

    // ── Extended fields ────────────────────────────────────────────────────────
    private String        googleId;
    private boolean       galleryAccessEnabled = false;
    private String        faceDescriptor;
    private LocalDateTime faceIdEnrolledAt;
    private String        resetToken;
    private LocalDateTime resetTokenExpiresAt;
    private String        verificationCode;
    private LocalDateTime verificationCodeExpiresAt;

    public User() {}

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRoles() { return roles; }
    public void setRoles(String roles) {
        this.roles = (roles != null && !roles.isBlank()) ? roles : "ROLE_USER";
    }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }

    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }
    public void setWeight(double weight) { this.weight = (float) weight; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }
    public void setHeight(double height) { this.height = (float) height; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public boolean isPhoneVerified() { return phoneVerified; }
    public void setPhoneVerified(boolean phoneVerified) { this.phoneVerified = phoneVerified; }

    public String getPhotoFilename() { return photoFilename; }
    public void setPhotoFilename(String photoFilename) { this.photoFilename = photoFilename; }

    public String getWelcomeMessage() { return welcomeMessage; }
    public void setWelcomeMessage(String welcomeMessage) { this.welcomeMessage = welcomeMessage; }

    // ── Computed ───────────────────────────────────────────────────────────────

    public String getFullName() {
        String fn = firstName != null ? firstName : "";
        String ln = lastName  != null ? lastName  : "";
        return (fn + " " + ln).trim();
    }

    public double getBmi() {
        if (height <= 0 || weight <= 0) return 0;
        double hm = height / 100.0;
        return weight / (hm * hm);
    }

    public int getAge() {
        if (birthday == null) return 0;
        return Period.between(birthday, LocalDate.now()).getYears();
    }

    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(roles);
    }

    // ── Aliases for compatibility ──────────────────────────────────────────────

    /** Alias for getRoles() */
    public String getRole() { return roles; }
    /** Alias for setRoles() */
    public void setRole(String role) { setRoles(role); }

    /** Alias for getPhoneNumber() */
    public String getPhone() { return phoneNumber; }
    /** Alias for setPhoneNumber() */
    public void setPhone(String phone) { this.phoneNumber = phone; }

    // ── FaceID ────────────────────────────────────────────────────────────────

    public boolean hasFaceId() { return faceDescriptor != null && !faceDescriptor.isBlank(); }

    public String getFaceDescriptor() { return faceDescriptor; }
    public void setFaceDescriptor(String faceDescriptor) { this.faceDescriptor = faceDescriptor; }

    public LocalDateTime getFaceIdEnrolledAt() { return faceIdEnrolledAt; }
    public void setFaceIdEnrolledAt(LocalDateTime faceIdEnrolledAt) { this.faceIdEnrolledAt = faceIdEnrolledAt; }

    // ── Google Auth ───────────────────────────────────────────────────────────

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    // ── Gallery ───────────────────────────────────────────────────────────────

    public boolean isGalleryAccessEnabled() { return galleryAccessEnabled; }
    public void setGalleryAccessEnabled(boolean galleryAccessEnabled) { this.galleryAccessEnabled = galleryAccessEnabled; }

    // ── Password Reset ────────────────────────────────────────────────────────

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public LocalDateTime getResetTokenExpiresAt() { return resetTokenExpiresAt; }
    public void setResetTokenExpiresAt(LocalDateTime resetTokenExpiresAt) { this.resetTokenExpiresAt = resetTokenExpiresAt; }

    // ── Verification Code ─────────────────────────────────────────────────────

    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }

    public LocalDateTime getVerificationCodeExpiresAt() { return verificationCodeExpiresAt; }
    public void setVerificationCodeExpiresAt(LocalDateTime verificationCodeExpiresAt) { this.verificationCodeExpiresAt = verificationCodeExpiresAt; }

    // ── BMI Category ──────────────────────────────────────────────────────────

    public String getBmiCategory() {
        double bmi = getBmi();
        if (bmi <= 0)   return "Unknown";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }
}
