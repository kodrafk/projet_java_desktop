package tn.esprit.projet.models;

import java.time.LocalDateTime;

public class UserBadge {
    private int           id;
    private int           userId;
    private Badge         badge;
    private boolean       unlocked;
    private LocalDateTime unlockedAt;
    private int           currentValue;
    private boolean       isVitrine;

    public int           getId()                       { return id; }
    public void          setId(int id)                 { this.id = id; }
    public int           getUserId()                   { return userId; }
    public void          setUserId(int userId)         { this.userId = userId; }
    public Badge         getBadge()                    { return badge; }
    public void          setBadge(Badge badge)         { this.badge = badge; }
    public boolean       isUnlocked()                  { return unlocked; }
    public void          setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    public LocalDateTime getUnlockedAt()               { return unlockedAt; }
    public void          setUnlockedAt(LocalDateTime t){ this.unlockedAt = t; }
    public int           getCurrentValue()             { return currentValue; }
    public void          setCurrentValue(int v)        { this.currentValue = v; }
    public boolean       isVitrine()                   { return isVitrine; }
    public void          setVitrine(boolean v)         { this.isVitrine = v; }

    /** Returns 0–100 progress percentage */
    public int getProgression() {
        if (badge == null || badge.getConditionValue() <= 0) return unlocked ? 100 : 0;
        return Math.min(100, (int) Math.round((currentValue / (double) badge.getConditionValue()) * 100));
    }
}
