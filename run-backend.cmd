@echo off
cd /d "%~dp0"
set MYSQL_PASSWORD=1234
rem Persist to MySQL (same DB as Workbench). For in-memory H2 instead: set SPRING_PROFILES_ACTIVE=dev
set SPRING_PROFILES_ACTIVE=mysql

echo.
echo === Smart Campus API ===
echo Freeing port 8080: stopping Windows service Tomcat9 if it is running...
net stop Tomcat9 >nul 2>&1
if errorlevel 1 (
  echo   Tomcat9 was not stopped ^(not installed, already stopped, or need Administrator^).
  echo   If Spring Boot still says port 8080 in use: right-click this file - Run as administrator,
  echo   or run in elevated CMD: net stop Tomcat9
) else (
  echo   Tomcat9 stopped.
)
echo.
echo Starting Spring Boot on http://127.0.0.1:8080
echo Leave this window OPEN while you use the React app or Postman.
echo Do NOT press Ctrl+C until you are finished testing.
echo.
call mvnw.cmd spring-boot:run
pause
