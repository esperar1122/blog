@echo off
echo ========================================
echo     Blog System Setup Assistant
echo ========================================
echo.

echo [1/5] Checking current environment...
echo.

:: Check Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java not found in PATH
    echo.
    echo Please install Java 17+ first:
    echo 1. Visit https://adoptium.net/temurin/releases/?version=17
    echo 2. Download and install Windows x64 version
    echo 3. Restart this script
    pause
    exit /b 1
) else (
    echo ✅ Java found
    java -version
    echo.
)

:: Check Maven
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven not found in PATH
    echo.
    echo Please install Maven:
    echo Option 1 - Using Chocolatey (recommended):
    echo   choco install maven -y
    echo.
    echo Option 2 - Manual:
    echo   1. Download from https://maven.apache.org/download.cgi
    echo   2. Extract to C:\Program Files\Apache\maven
    echo   3. Add C:\Program Files\Apache\maven\bin to PATH
    echo.
    pause
    exit /b 1
) else (
    echo ✅ Maven found
    mvn -version | findstr /C:"Apache Maven"
    echo.
)

echo [2/5] Checking Node.js dependencies...
npm list --depth=0 >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js dependencies not installed
    echo Installing...
    npm run install:all
    if %errorlevel% neq 0 (
        echo ❌ Failed to install dependencies
        pause
        exit /b 1
    )
) else (
    echo ✅ Node.js dependencies already installed
)

echo [3/5] Checking MySQL connection...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  MySQL not found in expected location
    echo Please ensure MySQL 8.0 is installed and running
) else (
    echo ✅ MySQL found
)

echo.
echo [4/5] Database setup required:
echo Please run these SQL commands manually in MySQL:
echo.
echo   CREATE DATABASE blog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
echo.
echo Then edit blog-backend/src/main/resources/application.yml
echo Set your MySQL password in the datasource section
echo.

echo [5/5] Environment variables setup:
echo Create a .env file in the project root with:
echo   DB_PASSWORD=your_mysql_password
echo   JWT_SECRET=your_jwt_secret_key
echo.

echo ========================================
echo            Setup Summary
echo ========================================
echo.
echo ✅ Environment checks completed
echo ✅ Dependencies installed
echo ⚠️  Manual setup required:
echo   1. Configure database
echo   2. Set environment variables
echo.
echo After completing manual setup, run:
echo   npm run dev
echo.
echo Access the application at:
echo   Frontend: http://localhost:5173
echo   Backend:  http://localhost:8080/api
echo.

pause