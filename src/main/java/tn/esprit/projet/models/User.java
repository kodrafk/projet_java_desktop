package tn.esprit.projet.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

public class User {
    private int           id;
    private String        email;
    private String        password;
    private String roles = "ROLE_USER";
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

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

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
}
