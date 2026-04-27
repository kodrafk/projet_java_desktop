package tn.esprit.projet.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

public class User {

    private int           id;
    private String        email;
    private String        password;
    private String        role = "ROLE_USER";
    private boolean       isActive;
    private LocalDateTime createdAt;
    private String        resetToken;
    private LocalDateTime resetTokenExpiresAt;
    private String        verificationCode;
    private LocalDateTime verificationCodeExpiresAt;
    private String        faceDescriptor;
    private LocalDateTime faceIdEnrolledAt;
    private String        welcomeMessage;
    private String        googleId;
    private String        photoFilename;
    private String        firstName;
    private String        lastName;
    private LocalDate     birthday;
    private double        weight;
    private double        height;
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

    public String getRole() { return role; }
    public void setRole(String role) { this.role = (role != null && !role.isBlank()) ? role : "ROLE_USER"; }

    /** Alias used by legacy code that calls getRoles() */
    public String getRoles() { return role; }
    public void setRoles(String r) { setRole(r); }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public LocalDateTime getResetTokenExpiresAt() { return resetTokenExpiresAt; }
    public void setResetTokenExpiresAt(LocalDateTime t) { this.resetTokenExpiresAt = t; }

    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }

    public LocalDateTime getVerificationCodeExpiresAt() { return verificationCodeExpiresAt; }
    public void setVerificationCodeExpiresAt(LocalDateTime t) { this.verificationCodeExpiresAt = t; }

    public String getFaceDescriptor() { return faceDescriptor; }
    public void setFaceDescriptor(String faceDescriptor) { this.faceDescriptor = faceDescriptor; }

    public LocalDateTime getFaceIdEnrolledAt() { return faceIdEnrolledAt; }
    public void setFaceIdEnrolledAt(LocalDateTime t) { this.faceIdEnrolledAt = t; }

    public String getWelcomeMessage() { return welcomeMessage; }
    public void setWelcomeMessage(String welcomeMessage) { this.welcomeMessage = welcomeMessage; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    public String getPhotoFilename() { return photoFilename; }
    public void setPhotoFilename(String photoFilename) { this.photoFilename = photoFilename; }

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

    public boolean isGalleryAccessEnabled() { return galleryAccessEnabled; }
    public void setGalleryAccessEnabled(boolean galleryAccessEnabled) { this.galleryAccessEnabled = galleryAccessEnabled; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // ── Computed ───────────────────────────────────────────────────────────────

    public String getFullName() {
        String fn = firstName != null ? firstName : "";
        String ln = lastName  != null ? lastName  : "";
        return (fn + " " + ln).trim();
    }

    public int getAge() {
        if (birthday == null) return 0;
        return Period.between(birthday, LocalDate.now()).getYears();
    }

    public double getBmi() {
        if (height <= 0 || weight <= 0) return 0;
        double result = weight / Math.pow(height / 100.0, 2);
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

    public boolean hasFaceId() {
        return faceDescriptor != null && !faceDescriptor.isEmpty();
    }

    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(role);
    }
}
