@echo off
echo ========================================
echo     Fix Environment Variables
echo ========================================
echo.

echo Current Java installation found:
echo "C:\Users\x8440\.jdks\ms-17.0.17\bin\java.exe"
echo.
echo This is Java 17.0.17 Microsoft OpenJDK - Perfect!
echo.

echo Setting JAVA_HOME to correct path...
setx JAVA_HOME "C:\Users\x8440\.jdks\ms-17.0.17"
echo ✅ JAVA_HOME updated

echo.
echo Adding Java to PATH if not already present...
echo Current PATH entries for Java:
echo %PATH% | findstr /C:"ms-17.0.17" >nul
if %errorlevel% equ 0 (
    echo ✅ Java already in PATH
) else (
    echo Adding Java bin directory to PATH...
    setx PATH "%PATH%;C:\Users\x8440\.jdks\ms-17.0.17\bin"
    echo ✅ Java added to PATH
)

echo.
echo Checking for Maven...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven not found in PATH
    echo.
    echo Please install Maven:
    echo.
    echo Option 1 - Chocolatey (recommended):
    echo   choco install maven -y
    echo.
    echo Option 2 - Manual:
    echo   1. Download Apache Maven from: https://maven.apache.org/download.cgi
    echo   2. Extract to: C:\Program Files\Apache\maven
    echo   3. Add to PATH: C:\Program Files\Apache\maven\bin
    echo.
    echo After installing Maven, run this script again to verify.
) else (
    echo ✅ Maven found
    mvn -version | findstr /C:"Apache Maven"
)

echo.
echo ========================================
echo         Environment Status
echo ========================================
echo.
echo Testing Java installation...
"C:\Users\x8440\.jdks\ms-17.0.17\bin\java.exe" -version
echo.

echo Next steps:
echo 1. Close and reopen this terminal/PowerShell
echo 2. Run: npm run dev
echo.
echo If Maven is not installed, install it first, then restart your terminal.

pause