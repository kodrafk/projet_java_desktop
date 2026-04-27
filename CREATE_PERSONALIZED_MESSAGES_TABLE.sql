-- ============================================================================
-- Table: personalized_messages
-- Description: Messages personnalisés envoyés par l'admin aux utilisateurs
--              Affichés dans la section objectives du front office
-- ============================================================================

CREATE TABLE IF NOT EXISTS personalized_messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    admin_id INTEGER NOT NULL,
    content TEXT NOT NULL,
    send_via_sms BOOLEAN DEFAULT 0,
    sms_status TEXT,
    sms_id TEXT,
    is_read BOOLEAN DEFAULT 0,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_personalized_messages_user_id ON personalized_messages(user_id);
CREATE INDEX IF NOT EXISTS idx_personalized_messages_admin_id ON personalized_messages(admin_id);
CREATE INDEX IF NOT EXISTS idx_personalized_messages_is_read ON personalized_messages(is_read);
