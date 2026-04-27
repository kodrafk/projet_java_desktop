-- Add phone column to user table for Twilio SMS
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `phone` VARCHAR(20) DEFAULT NULL;
