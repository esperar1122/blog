# 🚀 Blog System Setup Guide

## Current Status ✅
- Node.js v22.12.0 ✅
- npm v10.9.0 ✅
- Project Dependencies ✅
- MySQL 8.0 ✅ (Detected)

## Remaining Tasks ⚠️
1. Install Java 17+
2. Install Maven 3.9+
3. Set up database
4. Configure environment variables

## Quick Setup Commands

### Option 1: Automatic (Windows PowerShell - Admin)

```powershell
# Install Java 17
choco install openjdk17 -y

# Install Maven
choco install maven -y

# Refresh environment
refreshenv

# Verify installations
java -version
mvn -version
```

### Option 2: Manual Installation

#### Install Java 17
1. Visit: https://adoptium.net/temurin/releases/?version=17
2. Download Windows x64 installer
3. Run installer with default settings
4. Verify: `java -version`

#### Install Maven
1. Visit: https://maven.apache.org/download.cgi
2. Download binary zip archive
3. Extract to `C:\Program Files\Apache\maven`
4. Add to PATH: `C:\Program Files\Apache\maven\bin`
5. Verify: `mvn -version`

## Database Setup

### 1. Start MySQL Service
```cmd
# Check service status
sc query mysql80

# Start if not running
net start mysql80
```

### 2. Create Database
```sql
-- Connect to MySQL
mysql -u root -p

-- Create database
CREATE DATABASE blog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Show databases
SHOW DATABASES;

-- Exit
EXIT;
```

### 3. Configure Application

Edit `blog-backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8
    username: root
    password: YOUR_MYSQL_PASSWORD  # Change this
```

## Environment Variables

Create `.env` file in project root:

```bash
# Database
DB_PASSWORD=your_mysql_password

# JWT Secret (generate a strong secret)
JWT_SECRET=your_very_long_and_secure_jwt_secret_key_here

# Redis (optional)
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# OSS (optional, for file uploads)
OSS_ENDPOINT=
OSS_ACCESS_KEY_ID=
OSS_ACCESS_KEY_SECRET=
OSS_BUCKET_NAME=
OSS_DOMAIN_NAME=
```

## Generate JWT Secret (Optional)

```bash
# Generate a secure random string
node -e "console.log(require('crypto').randomBytes(64).toString('hex'))"
```

## Run the Project

### Development Mode
```bash
# Start both frontend and backend
npm run dev
```

### Or Start Separately
```bash
# Terminal 1 - Backend
cd blog-backend
mvn spring-boot:run

# Terminal 2 - Frontend
cd blog-frontend
npm run dev
```

## Access Points

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080/api
- **API Documentation**: http://localhost:8080/api/doc.html (if SpringDoc is configured)

## Troubleshooting

### Port Conflicts
- Frontend default: 5173
- Backend default: 8080
- MySQL default: 3306

### Common Issues
1. **Java not found**: Ensure Java 17+ is in PATH
2. **Maven not found**: Ensure Maven is in PATH
3. **Database connection**: Check MySQL service and credentials
4. **Port already in use**: Kill process or change port in config

### Database Migration
The project includes Flyway migration for automatic database schema updates. The migration file is located at:
`blog-backend/src/main/resources/db/migration/V1_6__Add_article_lifecycle_management.sql`

## Verification Tests

```bash
# Test backend
cd blog-backend
mvn test

# Test frontend
cd blog-frontend
npm run test
```

## Need Help?

1. Check README.md for project structure
2. Review application.yml for configuration options
3. Check logs in `logs/blog-backend.log` for backend errors
4. Use browser dev tools for frontend debugging