@echo off
echo ========================================
echo     Verify Maven Installation
echo ========================================
echo.

:: Try to use Maven from default location
set MAVEN_BIN=C:\Program Files\Apache\maven\bin

if exist "%MAVEN_BIN%\mvn.exe" (
    echo Testing Maven from: %MAVEN_BIN%
    echo.
    "%MAVEN_BIN%\mvn" -version
    if %errorlevel% equ 0 (
        echo.
        echo ✅ Maven is working correctly!
        echo.
        echo Testing Maven with Java...
        "%MAVEN_BIN%\mvn" help:system | findstr /C:"Java Version"
        echo.
        echo Maven is ready to use with the project!
    ) else (
        echo ❌ Maven test failed
    )
) else (
    echo ❌ Maven not found at expected location
    echo Expected: C:\Program Files\Apache\maven\bin\mvn.exe
    echo.
    echo Please run install-maven-3.9.11.bat first
)

echo.
pause