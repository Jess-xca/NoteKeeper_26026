@echo off
echo ============================================
echo Stopping NoteKeeper Backend (Port 8080)
echo ============================================

REM Find process on port 8080
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do (
    set PID=%%a
)

if defined PID (
    echo Found process on port 8080 with PID: %PID%
    taskkill /F /PID %PID%
    echo Backend stopped successfully!
) else (
    echo No process found on port 8080
)

echo.
pause
