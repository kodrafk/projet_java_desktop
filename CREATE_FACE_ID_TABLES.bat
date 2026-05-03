@echo off
echo Creating Face ID tables...
sqlite3 nutrilife.db < CREATE_FACE_ID_TABLES.sql
echo Done!
pause
