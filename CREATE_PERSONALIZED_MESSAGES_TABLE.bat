@echo off
echo ============================================
echo Creating personalized_messages table...
echo ============================================
sqlite3 nutrilife.db < CREATE_PERSONALIZED_MESSAGES_TABLE.sql
echo.
echo Table created successfully!
echo.
pause
