@echo off
REM ====================================================================
REM SMART E-COMMERCE BACKEND ENGINE - DATABASE SETUP SCRIPT
REM ====================================================================

echo ===================================================
echo   Setting up Database: smart_ecommerce
echo ===================================================
echo.
echo Please enter your MySQL root password when prompted.
echo.

set "MYSQL_BIN=mysql"
where mysql >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
        set "MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    ) else (
        echo [ERROR] mysql.exe not found in PATH or standard install location.
        echo Please ensure MySQL 8 is installed.
        pause
        exit /b 1
    )
)

echo [1/2] Executing database\schema.sql...
"%MYSQL_BIN%" -u root -p < database\schema.sql
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Failed to execute schema.sql. Please verify your password.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [2/2] Executing database\sample_data.sql (500+ products)...
"%MYSQL_BIN%" -u root -p < database\sample_data.sql
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Failed to execute sample_data.sql. Please verify your password.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [OK] Database 'smart_ecommerce' created and populated successfully!
pause
