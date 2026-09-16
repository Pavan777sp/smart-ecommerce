@echo off
REM ====================================================================
REM SMART E-COMMERCE BACKEND ENGINE - BUILD & RUN SCRIPT
REM ====================================================================

echo ===================================================
echo   Compiling Smart E-Commerce Backend Engine...
echo ===================================================

if not exist target\classes mkdir target\classes

if not exist lib\mysql-connector-j-8.3.0.jar (
    echo Downloading MySQL JDBC driver...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; New-Item -ItemType Directory -Force lib | Out-Null; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar' -OutFile 'lib\mysql-connector-j-8.3.0.jar'"
)

REM Gather source files
dir /s /b src\main\java\*.java > sources.txt

javac -encoding UTF-8 -d target\classes -cp "lib\mysql-connector-j-8.3.0.jar" @sources.txt
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    del sources.txt
    pause
    exit /b %ERRORLEVEL%
)

del sources.txt

REM Copy configuration
if exist src\main\resources\db.properties (
    copy /y src\main\resources\db.properties target\classes\db.properties >nul
)

echo [OK] Build successful!
echo.
echo ===================================================
echo   Launching Application Console...
echo ===================================================

java -cp "lib\mysql-connector-j-8.3.0.jar;target\classes" com.ecommerce.Main
pause
