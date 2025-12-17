# Maven 3.9.11 Installation Options

## Option 1: Automated Installation (Recommended)
Run the provided batch script as Administrator:
```cmd
install-maven-3.9.11.bat
```

## Option 2: Chocolatey (Easiest if Chocolatey is installed)
```powershell
# As Administrator
choco install maven --version=3.9.11 -y
```

## Option 3: Manual Installation

### Step 1: Download Maven
- URL: https://archive.apache.org/dist/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.zip
- File: `apache-maven-3.9.11-bin.zip` (approx. 8.7 MB)

### Step 2: Extract Maven
1. Create folder: `C:\Program Files\Apache\maven`
2. Extract the zip file to this folder
3. You should have: `C:\Program Files\Apache\maven\bin\mvn.exe`

### Step 3: Set Environment Variables
1. **MAVEN_HOME**:
   - Variable name: `MAVEN_HOME`
   - Variable value: `C:\Program Files\Apache\maven`

2. **PATH** (add to existing PATH):
   - Add: `C:\Program Files\Apache\maven\bin`

### Step 4: Verify Installation
Open new terminal and run:
```bash
mvn -version
```

Expected output should show:
```
Apache Maven 3.9.11 (etc...)
Java version: 17.0.17, vendor: Microsoft, etc...
```

## Option 4: PowerShell Installation
```powershell
# As Administrator
# Create directory
New-Item -ItemType Directory -Force -Path "C:\Program Files\Apache\maven"

# Download and extract
Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.zip" -OutFile "$env:TEMP\maven.zip"
Expand-Archive -Path "$env:TEMP\maven.zip" -DestinationPath "C:\Program Files\Apache" -Force

# Rename folder (if needed)
Get-ChildItem "C:\Program Files\Apache" | Where-Object {$_.Name -like "apache-maven*"} | Rename-Item -NewName "maven"

# Set environment variables
[Environment]::SetEnvironmentVariable("MAVEN_HOME", "C:\Program Files\Apache\maven", "Machine")
[Environment]::SetEnvironmentVariable("PATH", [Environment]::GetEnvironmentVariable("PATH", "Machine") + ";C:\Program Files\Apache\maven\bin", "Machine")

Write-Host "Maven 3.9.11 installed successfully!"
```

## After Installation
1. **Close and reopen** your terminal/PowerShell
2. **Test the installation**:
   ```bash
   mvn -version
   ```
3. **If successful**, you should see Maven 3.9.11 with Java 17

## Troubleshooting
- **'mvn' not recognized**: Close/reopen terminal or check PATH
- **Access denied**: Run installation as Administrator
- **Download fails**: Check internet connection or try manual download
- **Already installed**: Update environment variables to point to 3.9.11

## Verification Commands
```bash
# Check Maven version
mvn -version

# Check Maven help
mvn --help

# Test Maven with Java
mvn archetype:generate -DgroupId=com.test -DartifactId=test-app
```