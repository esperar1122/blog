@echo off
echo ========================================
echo     Install Maven 3.9.11
echo ========================================
echo.

echo Downloading Apache Maven 3.9.11...
echo.

:: Create temp directory
if not exist "temp" mkdir temp

:: Download Maven 3.9.11
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.zip' -OutFile 'temp\apache-maven-3.9.11-bin.zip'}"

if %errorlevel% neq 0 (
    echo ❌ Failed to download Maven
    echo Please check your internet connection
    pause
    exit /b 1
)

echo ✅ Maven 3.9.11 downloaded successfully
echo.

echo Extracting Maven to C:\Program Files\Apache\maven...
powershell -Command "Expand-Archive -Path 'temp\apache-maven-3.9.11-bin.zip' -DestinationPath 'C:\Program Files\Apache' -Force"

if %errorlevel% neq 0 (
    echo ❌ Failed to extract Maven
    echo You may need to run this script as Administrator
    pause
    exit /b 1
)

:: Rename the extracted folder to just 'maven'
ren "C:\Program Files\Apache\apache-maven-3.9.11" "maven" 2>nul

echo ✅ Maven extracted successfully
echo.

echo Setting up environment variables...
setx MAVEN_HOME "C:\Program Files\Apache\maven"
setx PATH "%PATH%;C:\Program Files\Apache\maven\bin"

echo ✅ Environment variables updated
echo.

echo Cleaning up temporary files...
rmdir /s /q temp 2>nul

echo ========================================
echo      Installation Complete!
echo ========================================
echo.
echo Maven 3.9.11 has been installed to:
echo C:\Program Files\Apache\maven
echo.
echo Environment variables set:
echo MAVEN_HOME=C:\Program Files\Apache\maven
echo PATH updated with Maven bin directory
echo.
echo IMPORTANT: Please close and reopen this terminal/PowerShell
echo to use the new environment variables.
echo.

echo Testing Maven installation...
"C:\Program Files\Apache\maven\bin\mvn" -version

pause