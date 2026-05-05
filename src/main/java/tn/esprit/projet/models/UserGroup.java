package tn.esprit.projet.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserGroup {
    private int           id;
    private String        name;
    private String        description;
    private int           adminId;
    private String        color;
    private boolean       pinned;
    private LocalDateTime createdAt;
    private List<User>    members = new ArrayList<>();

    public UserGroup() { this.createdAt = LocalDateTime.now(); }

    public UserGroup(String name, String description, int adminId, String color) {
        this();
        this.name = name;
        this.description = description;
        this.adminId = adminId;
        this.color = color;
    }

    public int getId()                        { return id; }
    public void setId(int id)                 { this.id = id; }
    public String getName()                   { return name; }
    public void setName(String v)             { this.name = v; }
    public String getDescription()            { return description; }
    public void setDescription(String v)      { this.description = v; }
    public int getAdminId()                   { return adminId; }
    public void setAdminId(int v)             { this.adminId = v; }
    public String getColor()                  { return color != null ? color : "#2E7D5A"; }
    public void setColor(String v)            { this.color = v; }
    public boolean isPinned()                 { return pinned; }
    public void setPinned(boolean v)          { this.pinned = v; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public List<User> getMembers()            { return members; }
    public void setMembers(List<User> v)      { this.members = v; }
    public int getMemberCount()               { return members.size(); }
}
