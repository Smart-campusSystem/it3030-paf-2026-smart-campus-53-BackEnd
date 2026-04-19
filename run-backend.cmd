@echo off
cd /d "%~dp0"
set MYSQL_PASSWORD=1234
echo.
echo === Smart Campus API ===
echo Starting Spring Boot on http://127.0.0.1:8080
echo Leave this window OPEN while you use the React app or Postman.
echo Do NOT press Ctrl+C until you are finished testing.
echo.
call mvnw.cmd spring-boot:run
pause
