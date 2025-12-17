# ⚠️ Maven Installation Required

## Current Situation
I cannot download Maven automatically due to network connectivity issues. You'll need to install it manually using one of the methods below.

## Quick Installation Options

### Option 1: Manual Download (Recommended)

1. **Download Maven 3.9.11**:
   - Open your web browser
   - Go to: https://archive.apache.org/dist/maven/maven-3/3.9.11/binaries/
   - Download: `apache-maven-3.9.11-bin.zip` (about 8.7 MB)
   - Alternative mirror: https://dlcdn.apache.org/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.zip

2. **Extract the file**:
   - Right-click the downloaded zip file
   - Extract to: `C:\Program Files\Apache\`
   - Rename the extracted folder to `maven` (should be: `C:\Program Files\Apache\maven`)

3. **Set Environment Variables**:
   - Press `Win + R`, type `sysdm.cpl`, press Enter
   - Go to "Advanced" tab → "Environment Variables"
   - Under "System variables":
     - Click "New"
     - Variable name: `MAVEN_HOME`
     - Variable value: `C:\Program Files\Apache\maven`
     - Click OK
   - Find "Path" variable, click "Edit"
     - Click "New"
     - Add: `C:\Program Files\Apache\maven\bin`
     - Click OK on all windows

4. **Verify Installation**:
   - Close and reopen Command Prompt/PowerShell
   - Run: `mvn -version`
   - Should show Maven 3.9.11 with Java 17

### Option 2: Package Manager

**Using Winget (Windows 10/11)**:
```powershell
winget install Apache.Maven
```

**Using Chocolatey (if you can install it first)**:
1. Install Chocolatey: https://chocolatey.org/install
2. Run: `choco install maven`

### Option 3: Run Batch File

I've created `install-maven-3.9.11.bat` for you. Right-click it and "Run as Administrator".

## After Installation

Once Maven is installed, you can:
1. Verify with: `mvn -version`
2. Run the project: `npm run dev`

## Verification Test

Run `verify-maven.bat` to check if Maven is working correctly.

## Need Help?

If you encounter any issues:
1. Make sure Java 17 is in your PATH
2. Check that MAVEN_HOME points to the correct directory
3. Verify the PATH includes Maven's bin directory

The project is ready to run once Maven is installed!