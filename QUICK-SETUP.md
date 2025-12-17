# 🚀 Quick Setup Guide

## Current Status ✅
- ✅ Node.js dependencies installed
- ✅ Project structure complete
- ✅ Story 2.3 fully implemented
- ⚠️ Need: Java 17+, Maven, Database setup

## Immediate Steps to Run Project

### 1. Install Java 17+ (Required)
```powershell
# Option 1: Chocolatey (Recommended)
choco install openjdk17 -y

# Option 2: Manual Download
# Visit: https://adoptium.net/temurin/releases/?version=17
# Download Windows x64 installer
```

### 2. Install Maven (Required)
```powershell
# Option 1: Chocolatey
choco install maven -y

# Option 2: Manual
# Download from: https://maven.apache.org/download.cgi
# Extract to: C:\Program Files\Apache\maven
# Add to PATH: C:\Program Files\Apache\maven\bin
```

### 3. Verify Installation
```bash
java -version
mvn -version
```

### 4. Database Setup (5 minutes)
```sql
-- Connect to MySQL
mysql -u root -p

-- Create database
CREATE DATABASE blog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Run migration script (run this SQL file in MySQL):
SOURCE path/to/project/blog-backend/src/main/resources/db/migration/V1_6__Add_article_lifecycle_management.sql;
```

### 5. Configure Environment
```bash
# Copy template
cp .env.template .env

# Edit .env file with your values:
# - DB_PASSWORD=your_mysql_password
# - JWT_SECRET=generate_with_node_crypto
```

Generate JWT Secret:
```bash
node -e "console.log(require('crypto').randomBytes(64).toString('hex'))"
```

### 6. Run Project!
```bash
npm run dev
```

## Access Points
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080/api

## What's Included in Story 2.3
✅ Article publish/unpublish functionality
✅ Article pinning system
✅ Scheduled publishing
✅ Soft delete and restore
✅ Version control system
✅ Operation logging
✅ Complete frontend UI
✅ Database migration scripts

## Need Help?
- Check: `setup-guide.md` for detailed instructions
- Run: `setup.bat` for guided setup
- Review: `README.md` for project structure