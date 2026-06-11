@echo off
cd /d "%~dp0"

set MYSQL_JAR=backend\lib\mysql-connector-j-9.7.0.jar

if not exist out mkdir out

:: New compile command (with UTF-8 support and error handling)
javac -encoding UTF-8 -cp "%MYSQL_JAR%" -d out backend\src\com\estatevault\**\*.java 2>nul
if errorlevel 1 (
  dir /s /b backend\src\*.java > sources.txt
  javac -encoding UTF-8 -cp "%MYSQL_JAR%" -d out @sources.txt
  del sources.txt
)

echo Starting EstateVault on http://localhost:8081 ...
:: New run command
java -cp "out;%MYSQL_JAR%" com.estatevault.Main 8081
pause
