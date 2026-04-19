package tn.esprit.projet.models;

public class Badge {
    private int    id;
    private String name;
    private String description;
    private String conditionType;
    private int    conditionValue;
    private String icon;

    public int    getId()                      { return id; }
    public void   setId(int id)                { this.id = id; }
    public String getName()                    { return name; }
    public void   setName(String name)         { this.name = name; }
    public String getDescription()             { return description; }
    public void   setDescription(String d)     { this.description = d; }
    public String getConditionType()           { return conditionType; }
    public void   setConditionType(String t)   { this.conditionType = t; }
    public int    getConditionValue()          { return conditionValue; }
    public void   setConditionValue(int v)     { this.conditionValue = v; }
    public String getIcon()                    { return icon; }
    public void   setIcon(String icon)         { this.icon = icon; }
}
