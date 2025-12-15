# Development Workflow

## Local Development Setup

### Prerequisites

```bash
# Install required tools
# Java 17
curl -s "https://get.sdkman.io" | bash
sdk install java 17.0.9-tem

# Node.js 18+
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
nvm install 18

# Docker
# Follow instructions at https://docs.docker.com/get-docker/

# Maven 3.9+
# Typically installed with Java SDK or use system package manager
```

### Initial Setup

```bash
# Clone repository
git clone <repository-url>
cd blog-system

# Setup backend
cd blog-backend
./mvnw clean install

# Setup frontend
cd ../blog-frontend
npm install

# Setup shared package
cd ../blog-shared
npm install

# Start development services
docker-compose -f docker-compose.dev.yml up -d mysql redis
```

### Development Commands

```bash
# Start all services
npm run dev:all

# Start backend only
cd blog-backend && ./mvnw spring-boot:run

# Start frontend only
cd blog-frontend && npm run dev

# Run tests
npm run test:all
```

## Environment Configuration

### Required Environment Variables

```bash
# Frontend (.env.local)
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_APP_TITLE=大学生博客系统
VITE_OSS_ENDPOINT=https://oss-cn-hangzhou.aliyuncs.com
VITE_UPLOAD_MAX_SIZE=5242880

# Backend (.env)
# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/blog_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=password

# Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
SPRING_DATA_REDIS_PASSWORD=

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# OSS
ALIBABA_OSS_ENDPOINT=https://oss-cn-hangzhou.aliyuncs.com
ALIBABA_OSS_ACCESS_KEY_ID=your-access-key
ALIBABA_OSS_ACCESS_KEY_SECRET=your-secret
ALIBABA_OSS_BUCKET_NAME=blog-system-files

# Shared
NODE_ENV=development
```
