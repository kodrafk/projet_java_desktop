@echo off
echo Adding phone column to user table...
mysql -u root nutrilife_db < ADD_PHONE_COLUMN.sql
echo Done!
pause
