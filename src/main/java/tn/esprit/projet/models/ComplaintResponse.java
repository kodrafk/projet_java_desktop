package tn.esprit.projet.models;

import java.time.LocalDateTime;

public class ComplaintResponse {
    private int id;
    private int complaintId;
    private String responseContent;
    private LocalDateTime responseDate;
    
    public ComplaintResponse() {
        this.responseDate = LocalDateTime.now();
    }
    
    public ComplaintResponse(int complaintId, String responseContent) {
        this.complaintId = complaintId;
        this.responseContent = responseContent;
        this.responseDate = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(int complaintId) {
        this.complaintId = complaintId;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public void setResponseContent(String responseContent) {
        this.responseContent = responseContent;
    }

    public LocalDateTime getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(LocalDateTime responseDate) {
        this.responseDate = responseDate;
    }
}
