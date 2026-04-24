package tn.esprit.projet.models;

import java.time.LocalDateTime;
import java.time.LocalDate;

public class Complaint {
    private int id;
    private int userId;
    private String title;
    private String description;
    private String phoneNumber;
    private int rate;
    private LocalDateTime dateOfComplaint;
    private LocalDate incidentDate;
    private String status;
    private ComplaintResponse responseObj;
    private String userName;
    private String userEmail;
    private String imagePath;

    public Complaint() {
        this.status = "PENDING";
        this.dateOfComplaint = LocalDateTime.now();
    }

    public Complaint(int userId, String title, String description, String phoneNumber, int rate) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.phoneNumber = phoneNumber;
        this.rate = rate;
        this.status = "PENDING";
        this.dateOfComplaint = LocalDateTime.now();
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPhoneNumber() { return phoneNumber; }
    public int getRate() { return rate; }
    public LocalDateTime getDateOfComplaint() { return dateOfComplaint; }
    public LocalDate getIncidentDate() { return incidentDate; }
    public String getStatus() { return status; }
    public String getAdminResponse() { return responseObj != null ? responseObj.getResponseContent() : null; }
    public ComplaintResponse getResponseObj() { return responseObj; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getImagePath() { return imagePath; }

    public java.util.List<String> getImagePathsList() {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return java.util.Arrays.asList(imagePath.split(";"));
    }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setRate(int rate) { this.rate = rate; }
    public void setDateOfComplaint(LocalDateTime dateOfComplaint) { this.dateOfComplaint = dateOfComplaint; }
    public void setIncidentDate(LocalDate incidentDate) { this.incidentDate = incidentDate; }
    public void setStatus(String status) { this.status = status; }
    public void setAdminResponse(String adminResponse) {
        if (adminResponse == null) {
            this.responseObj = null;
        } else {
            if (this.responseObj == null) {
                this.responseObj = new ComplaintResponse();
                this.responseObj.setComplaintId(this.id);
            }
            this.responseObj.setResponseContent(adminResponse);
        }
    }
    public void setResponseObj(ComplaintResponse responseObj) {
        this.responseObj = responseObj;
    }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}
