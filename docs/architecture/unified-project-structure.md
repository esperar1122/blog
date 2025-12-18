# Unified Project Structure

```
blog-system/
├── .github/                    # CI/CD workflows
│   └── workflows/
│       ├── ci.yaml
│       └── deploy.yaml
├── blog-backend/               # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/blog/
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   ├── dto/
│   │   │   │   ├── config/
│   │   │   │   ├── security/
│   │   │   │   ├── exception/
│   │   │   │   └── util/
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/
│   │   └── test/
│   ├── Dockerfile
│   ├── pom.xml
│   └── .mvn/
├── blog-frontend/              # Vue3 frontend
│   ├── src/
│   │   ├── components/
│   │   ├── views/
│   │   ├── composables/
│   │   ├── stores/
│   │   ├── services/
│   │   ├── utils/
│   │   ├── router/
│   │   ├── assets/
│   │   ├── App.vue
│   │   └── main.ts
│   ├── public/
│   ├── tests/
│   ├── Dockerfile
│   ├── package.json
│   ├── vite.config.ts
│   └── tsconfig.json
├── blog-shared/                # Shared types and utilities
│   ├── src/
│   │   ├── types/
│   │   │   ├── models.ts
│   │   │   ├── api.ts
│   │   │   └── common.ts
│   │   ├── constants/
│   │   └── utils/
│   ├── package.json
│   └── tsconfig.json
├── infrastructure/             # Infrastructure as Code
│   ├── docker/
│   │   ├── docker-compose.yml
│   │   ├── docker-compose.prod.yml
│   │   └── nginx/
│   ├── kubernetes/
│   │   ├── backend-deployment.yaml
│   │   ├── frontend-deployment.yaml
│   │   └── ingress.yaml
│   └── terraform/
├── scripts/                    # Build and deployment scripts
│   ├── build.sh
│   ├── deploy.sh
│   └── setup-dev.sh
├── docs/                       # Documentation
│   ├── prd.md
│   ├── front-end-spec.md
│   └── architecture.md
├── .env.example                # Environment template
├── package.json                # Root package.json for workspace
├── docker-compose.yml          # Development environment
├── .gitignore
└── README.md
```
