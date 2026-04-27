USE nutrilife_db;

-- Supprimer les anciens badges
DELETE FROM user_badge;
DELETE FROM badge;

-- Insérer les badges
INSERT INTO badge (nom, description, condition_text, condition_type, condition_value, svg, couleur, couleur_bg, categorie, ordre, rarete) VALUES
-- Getting Started
('Welcome!', 'You joined NutriLife - Your journey begins!', 'Create your account', 'account_created', 1, '🌟', '#7C3AED', '#EDE9FE', 'Getting Started', 0, 'common'),
('Profile Ready', 'Complete your profile with all details', 'Complete your profile', 'profile_complete', 1, '✅', '#059669', '#D1FAE5', 'Getting Started', 1, 'common'),
('Face Unlocked', 'Secure your account with Face ID', 'Enroll Face ID', 'face_id_enrolled', 1, '🔐', '#1D4ED8', '#DBEAFE', 'Getting Started', 2, 'rare'),
('Say Cheese!', 'Show the world your best smile', 'Upload a profile photo', 'photo_uploaded', 1, '📸', '#D97706', '#FEF3C7', 'Getting Started', 3, 'common'),

-- Weight Tracking
('First Weigh-In', 'Your first step towards transformation', 'Log 1 weight', 'weight_logs', 1, '⚖️', '#2E7D32', '#F0FDF4', 'Weight Tracking', 4, 'common'),
('Consistent Tracker', 'Building healthy habits, one log at a time', 'Log 5 weights', 'weight_logs', 5, '📊', '#15803D', '#DCFCE7', 'Weight Tracking', 5, 'common'),
('Dedicated Logger', 'Your commitment is showing results', 'Log 10 weights', 'weight_logs', 10, '💪', '#166534', '#BBF7D0', 'Weight Tracking', 6, 'rare'),

-- Goals
('Goal Setter', 'Every journey starts with a clear goal', 'Set a weight goal', 'objective_set', 1, '🎯', '#B45309', '#FEF3C7', 'Goals', 9, 'common'),
('Goal Crusher', 'GOAL ACHIEVED! Time to celebrate!', 'Goal fully achieved!', 'objective_100pct', 1, '🏆', '#DC2626', '#FEF2F2', 'Goals', 11, 'legendary'),

-- Consistency
('On a Roll', '3 days strong - momentum is building', '3-day activity streak', 'streak_days', 3, '🔥', '#DC2626', '#FEF2F2', 'Consistency', 16, 'common'),
('Week Warrior', '7 days in a row - you are on fire!', '7-day activity streak', 'streak_days', 7, '⚡', '#B91C1C', '#FEE2E2', 'Consistency', 17, 'rare');

-- Créer les user_badge pour tous les utilisateurs existants
INSERT INTO user_badge (user_id, badge_id, unlocked, current_value, is_vitrine)
SELECT u.id, b.id, 0, 0, 0
FROM user u
CROSS JOIN badge b;

-- Débloquer le badge "Welcome!" pour tous les utilisateurs
UPDATE user_badge ub
JOIN badge b ON ub.badge_id = b.id
SET ub.unlocked = 1, ub.unlocked_at = NOW()
WHERE b.condition_type = 'account_created';

SELECT 'Badges initialized successfully!' AS Status;
SELECT COUNT(*) AS total_badges FROM badge;
SELECT COUNT(*) AS total_user_badges FROM user_badge;
