# Deployment Architecture

## Deployment Strategy

**Frontend Deployment:**
- **Platform:** Nginx + Docker
- **Build Command:** `npm run build`
- **Output Directory:** dist
- **CDN/Edge:** 阿里云CDN for static assets

**Backend Deployment:**
- **Platform:** Docker + 阿里云ECS
- **Build Command:** `./mvnw clean package -DskipTests`
- **Deployment Method:** Docker containers with orchestration

## CI/CD Pipeline

```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          cache: 'npm'
      - name: Run backend tests
        run: |
          cd blog-backend
          ./mvnw test
      - name: Run frontend tests
        run: |
          cd blog-frontend
          npm ci
          npm run test

  build-and-deploy:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v3
      - name: Login to Docker Hub
        uses: docker/login-action@v2
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}
      - name: Build and push backend image
        run: |
          cd blog-backend
          docker build -t blog-system/backend .
          docker push blog-system/backend
      - name: Build and push frontend image
        run: |
          cd blog-frontend
          docker build -t blog-system/frontend .
          docker push blog-system/frontend
      - name: Deploy to production
        run: |
          # Deploy to server via SSH or use Kubernetes
```

## Environments

| Environment | Frontend URL | Backend URL | Purpose |
|-------------|--------------|-------------|---------|
| Development | http://localhost:3000 | http://localhost:8080 | Local development |
| Staging | https://staging.blog.example.com | https://api-staging.blog.example.com | Pre-production testing |
| Production | https://blog.example.com | https://api.blog.example.com | Live environment |
