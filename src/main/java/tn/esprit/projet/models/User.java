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
    private double        weight;
    private double        height;
    // Phone (integration fields)
    private String        phoneNumber;
    private boolean       phoneVerified;
    // Photo & welcome
    private String        photoFilename;
    private String        welcomeMessage;
    // Face ID (gestion_user fields)
    private String        faceDescriptor;
    private LocalDateTime faceIdEnrolledAt;
    // Google OAuth
    private String        googleId;
    // Password reset / verification
    private String        resetToken;
    private LocalDateTime resetTokenExpiresAt;
    private String        verificationCode;
    private LocalDateTime verificationCodeExpiresAt;
    // Gallery & phone alias
    private boolean       galleryAccessEnabled = false;
    private String        phone;

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

    /** Alias for gestion_user compatibility */
    public String getRole() { return roles; }
    public void setRole(String role) { setRoles(role); }

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

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    // Phone (integration style)
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public boolean isPhoneVerified() { return phoneVerified; }
    public void setPhoneVerified(boolean phoneVerified) { this.phoneVerified = phoneVerified; }

    // Phone alias (gestion_user style)
    public String getPhone() { return phone != null ? phone : phoneNumber; }
    public void setPhone(String phone) { this.phone = phone; this.phoneNumber = phone; }

    public String getPhotoFilename() { return photoFilename; }
    public void setPhotoFilename(String photoFilename) { this.photoFilename = photoFilename; }

    public String getWelcomeMessage() { return welcomeMessage; }
    public void setWelcomeMessage(String welcomeMessage) { this.welcomeMessage = welcomeMessage; }

    // Face ID
    public String getFaceDescriptor() { return faceDescriptor; }
    public void setFaceDescriptor(String faceDescriptor) { this.faceDescriptor = faceDescriptor; }

    public LocalDateTime getFaceIdEnrolledAt() { return faceIdEnrolledAt; }
    public void setFaceIdEnrolledAt(LocalDateTime t) { this.faceIdEnrolledAt = t; }

    // Google OAuth
    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    // Password reset
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public LocalDateTime getResetTokenExpiresAt() { return resetTokenExpiresAt; }
    public void setResetTokenExpiresAt(LocalDateTime t) { this.resetTokenExpiresAt = t; }

    // Email verification
    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }

    public LocalDateTime getVerificationCodeExpiresAt() { return verificationCodeExpiresAt; }
    public void setVerificationCodeExpiresAt(LocalDateTime t) { this.verificationCodeExpiresAt = t; }

    // Gallery
    public boolean isGalleryAccessEnabled() { return galleryAccessEnabled; }
    public void setGalleryAccessEnabled(boolean galleryAccessEnabled) { this.galleryAccessEnabled = galleryAccessEnabled; }

    // ── Computed ───────────────────────────────────────────────────────────────

    public String getFullName() {
        String fn = firstName != null ? firstName : "";
        String ln = lastName  != null ? lastName  : "";
        return (fn + " " + ln).trim();
    }

    public double getBmi() {
        if (height <= 0 || weight <= 0) return 0;
        double hm = height / 100.0;
        double result = weight / (hm * hm);
        return Math.round(result * 100.0) / 100.0;
    }

    public String getBmiCategory() {
        double bmi = getBmi();
        if (bmi <= 0)   return "—";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal weight";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    public int getAge() {
        if (birthday == null) return 0;
        return Period.between(birthday, LocalDate.now()).getYears();
    }

    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(roles);
    }

    public boolean hasFaceId() {
        return faceDescriptor != null && !faceDescriptor.isEmpty();
    }
}
