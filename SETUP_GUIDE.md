# Trident Code Quality Platform - Configuration & Deployment Guide

## Table of Contents
1. [Quick Start](#quick-start)
2. [Environment Configuration](#environment-configuration)
3. [AI Provider Setup](#ai-provider-setup)
4. [Database Setup](#database-setup)
5. [Local Development](#local-development)
6. [Docker Deployment](#docker-deployment)
7. [Kubernetes Deployment](#kubernetes-deployment)
8. [Troubleshooting](#troubleshooting)

---

## Quick Start

### Prerequisites
- Java 21 JDK
- Maven 3.9+
- PostgreSQL 14+
- Git

### 5-Minute Setup
```bash
# Clone repository
git clone <repo-url>
cd code-quality-platform

# Create .env file with minimum configuration
cat > .env << 'EOF'
# Database
DB_URL=jdbc:postgresql://localhost:5432/codequality
DB_USERNAME=codequality
DB_PASSWORD=your-secure-password

# GitHub
GITHUB_TOKEN=ghp_your_github_token_here

# JWT (generate with: openssl rand -base64 32)
JWT_SECRET=your-256-bit-base64-secret-here
JWT_EXPIRATION_MS=3600000
JWT_REFRESH_EXPIRATION_MS=604800000

# AI API (CRITICAL - see AI Provider Setup section)
AI_PROVIDER=openai
AI_API_KEY=sk-your-openai-api-key
AI_MODEL=gpt-4o-mini
AI_BASE_URL=https://api.openai.com/v1

# Application
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
EOF

# Load environment variables
source .env  # On Windows: $env:variable = "value" in PowerShell

# Build
mvn clean package

# Run
java -jar target/code-quality-platform-1.0.0.jar

# Test
curl http://localhost:8080/actuator/health
open http://localhost:8080/swagger-ui.html
```

---

## Environment Configuration

### Complete Environment Variables

```bash
# ============================================================================
# DATABASE CONFIGURATION
# ============================================================================
DB_URL=jdbc:postgresql://localhost:5432/codequality
DB_USERNAME=codequality
DB_PASSWORD=secure_password_here
DB_POOL_SIZE=10
DB_CONNECTION_TIMEOUT=30000

# ============================================================================
# SECURITY - JWT
# ============================================================================
JWT_SECRET=abc123def456ghi789jkl012mno345pqr567stu890  # Minimum 256 bits
JWT_EXPIRATION_MS=3600000                               # 1 hour in milliseconds
JWT_REFRESH_EXPIRATION_MS=604800000                      # 7 days in milliseconds

# ============================================================================
# EXTERNAL INTEGRATION - GITHUB
# ============================================================================
GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx       # GitHub Personal Access Token
GITHUB_API_BASE_URL=https://api.github.com

# ============================================================================
# EXTERNAL INTEGRATION - AI (CRITICAL)
# ============================================================================
AI_PROVIDER=openai                                       # Options: openai, azure
AI_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx         # OpenAI API Key
AI_MODEL=gpt-4o-mini                                   # Model name
AI_BASE_URL=https://api.openai.com/v1                 # OpenAI endpoint
AI_PROVIDER_TIMEOUT_SECONDS=120                        # Request timeout

# For Azure OpenAI instead:
# AI_PROVIDER=azure
# AI_API_KEY=your-azure-api-key
# AI_BASE_URL=https://your-resource-name.openai.azure.com
# AI_AZURE_DEPLOYMENT=your-deployment-name
# AI_AZURE_API_VERSION=2024-02-15-preview

# ============================================================================
# APPLICATION
# ============================================================================
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod                            # Options: dev, test, prod
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics

# ============================================================================
# LOGGING
# ============================================================================
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_TRIDENT_CODEQUALITY=DEBUG
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=DEBUG
LOGGING_PATTERN_CONSOLE=%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n
```

### Generate Secure JWT Secret

```bash
# Linux/Mac
openssl rand -base64 32

# Windows PowerShell
[Convert]::ToBase64String((Get-Random -Count 32 -InputObject (0..255) -SetSeed (Get-Date).Ticks | ForEach-Object {[byte]$_}))

# Online (for testing only - NEVER in production)
# https://www.base64encode.org/ (paste 32 random bytes)
```

---

## AI Provider Setup

### ⚠️ CRITICAL: This is the ONLY blocking item for full functionality

### Option 1: OpenAI (Recommended for Quick Start)

#### Step 1: Create OpenAI Account & API Key
1. Go to https://platform.openai.com/account/api-keys
2. Click "Create new secret key"
3. Copy the key (starts with `sk-proj-`)
4. Set it in your `.env` file

#### Step 2: Configure Environment
```bash
export AI_PROVIDER=openai
export AI_API_KEY=sk-proj-xxxxxxxxxxxxxxxxxxxxxxxx
export AI_MODEL=gpt-4o-mini
export AI_BASE_URL=https://api.openai.com/v1
export AI_PROVIDER_TIMEOUT_SECONDS=120
```

#### Step 3: Set Usage Limits (Optional but Recommended)
1. Navigate to https://platform.openai.com/account/billing/overview
2. Set monthly budget limit (e.g., $100)
3. Enable low balance notifications

#### Cost Estimation
- **gpt-4o-mini:** ~$0.15-0.30 per scan (depending on code size)
- **Estimated:** ~50-150 scans/month = $50-300/month
- **Optimization:** Limit files per scan (currently 25 files max)

### Option 2: Azure OpenAI

#### Step 1: Create Azure OpenAI Service
```bash
# Create resource group
az group create --name trident-rg --location eastus

# Create Azure OpenAI service
az cognitiveservices account create \
  --name trident-openai \
  --resource-group trident-rg \
  --kind OpenAI \
  --sku s0 \
  --location eastus

# Get API key
az cognitiveservices account keys list \
  --name trident-openai \
  --resource-group trident-rg
```

#### Step 2: Deploy GPT Model
1. Go to Azure Portal → Azure OpenAI Studio
2. Click "Deployments" → "Create new deployment"
3. Select model: "gpt-4o-mini"
4. Name: "codequality-deployment"
5. Click "Create"

#### Step 3: Configure Environment
```bash
export AI_PROVIDER=azure
export AI_API_KEY=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
export AI_BASE_URL=https://trident-openai.openai.azure.com
export AI_AZURE_DEPLOYMENT=codequality-deployment
export AI_AZURE_API_VERSION=2024-02-15-preview
export AI_PROVIDER_TIMEOUT_SECONDS=120
```

#### Azure Cost Estimation
- **Pricing:** Pay-as-you-go (more expensive than OpenAI)
- **Reserved Capacity:** Available for predictable workloads
- **Integration:** Easier with Azure ecosystem

---

## Database Setup

### PostgreSQL Installation

#### Mac (Homebrew)
```bash
brew install postgresql@14
brew services start postgresql@14
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

#### Windows
1. Download from https://www.postgresql.org/download/windows/
2. Run installer and follow prompts
3. Remember the password for `postgres` user
4. Start PostgreSQL service

### Create Database & User

```bash
# Connect to PostgreSQL
psql -U postgres

# Create user and database
CREATE USER codequality WITH ENCRYPTED PASSWORD 'your-secure-password';
CREATE DATABASE codequality OWNER codequality;

# Grant privileges
GRANT ALL PRIVILEGES ON DATABASE codequality TO codequality;

# Connect to new database
\c codequality

# Grant schema privileges
GRANT ALL ON SCHEMA public TO codequality;

# Exit
\q
```

### Verify Connection

```bash
# Test connection
psql -h localhost -U codequality -d codequality -c "SELECT NOW();"
```

### Database Configuration in application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/codequality
    username: codequality
    password: your-secure-password
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000
      idle-timeout: 600000
      
  jpa:
    hibernate:
      ddl-auto: validate  # Don't auto-create schema
    properties:
      hibernate:
        jdbc:
          time_zone: UTC
          
  flyway:
    enabled: true
    baseline-on-migrate: true
    out-of-order: false
```

---

## Local Development

### Development Profile Configuration

Create `application-dev.yml`:

```yaml
spring:
  application:
    name: code-quality-platform
  datasource:
    url: jdbc:postgresql://localhost:5432/codequality
    username: codequality
    password: password  # Use simple password for local dev
    hikari:
      maximum-pool-size: 5
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        jdbc:
          time_zone: UTC

server:
  port: 8080
  error:
    include-message: always
    include-stacktrace: on_param

security:
  jwt:
    secret: dev-secret-minimum-256-bits-long-for-testing
    expiration-ms: 86400000  # 24 hours for dev convenience
    refresh-expiration-ms: 2592000000  # 30 days

github:
  api-base-url: https://api.github.com
  token: ghp_dev_token_here

ai:
  provider: openai  # or azure
  base-url: https://api.openai.com/v1
  api-key: sk-dev-key-here
  model: gpt-4o-mini
  timeout-seconds: 120

logging:
  level:
    root: INFO
    com.trident.codequality: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### Run with Maven

```bash
# Set dev profile
export SPRING_PROFILES_ACTIVE=dev

# Run with hot reload
mvn spring-boot:run

# Or with IntelliJ/VS Code - just right-click and run
```

### IDE Setup

#### VS Code
```json
// .vscode/launch.json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "CodeQualityPlatformApplication",
      "request": "launch",
      "mainClass": "com.trident.codequality.CodeQualityPlatformApplication",
      "projectName": "code-quality-platform",
      "cwd": "${workspaceFolder}",
      "envFile": "${workspaceFolder}/.env",
      "args": "",
      "preLaunchTask": "maven: clean install"
    }
  ]
}
```

#### IntelliJ IDEA
1. Run → Edit Configurations
2. Click "+" → Maven
3. Set Working directory to project root
4. Set Command line: `spring-boot:run`
5. Set Environment variables from `.env`
6. Click Run

### Testing Locally

```bash
# 1. Register a user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "SecurePass123!"
  }'

# Response: { "accessToken": "...", "refreshToken": "..." }

# 2. Save token for subsequent requests
TOKEN="<accessToken from response>"

# 3. Register a repository
curl -X POST http://localhost:8080/api/v1/repositories \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "owner": "spring-projects",
    "name": "spring-boot",
    "url": "https://github.com/spring-projects/spring-boot",
    "description": "Spring Boot framework"
  }'

# 4. Start a scan
REPO_ID=1
curl -X POST http://localhost:8080/api/v1/repositories/$REPO_ID/scan \
  -H "Authorization: Bearer $TOKEN"

# 5. Get scan results
SCAN_ID=1
curl -X GET http://localhost:8080/api/v1/scans/$SCAN_ID \
  -H "Authorization: Bearer $TOKEN"

# 6. Dashboard
curl -X GET http://localhost:8080/api/v1/dashboard/summary \
  -H "Authorization: Bearer $TOKEN"
```

---

## Docker Deployment

### Dockerfile

Create `Dockerfile` in project root:

```dockerfile
# Multi-stage build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests

# Runtime
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /build/target/code-quality-platform-*.jar app.jar

# Non-root user
RUN addgroup -g 1000 spring && adduser -D -u 1000 -G spring spring
USER spring

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD java -cp app.jar org.springframework.boot.loader.JarLauncher \
  org.springframework.boot.actuate.health.HealthEndpoint

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14-alpine
    container_name: codequality-db
    environment:
      POSTGRES_USER: codequality
      POSTGRES_PASSWORD: codequality
      POSTGRES_DB: codequality
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U codequality"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    build: .
    container_name: codequality-app
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_URL: jdbc:postgresql://postgres:5432/codequality
      DB_USERNAME: codequality
      DB_PASSWORD: codequality
      GITHUB_TOKEN: ${GITHUB_TOKEN}
      JWT_SECRET: ${JWT_SECRET}
      AI_PROVIDER: ${AI_PROVIDER:-openai}
      AI_API_KEY: ${AI_API_KEY}
      AI_MODEL: ${AI_MODEL:-gpt-4o-mini}
      AI_BASE_URL: ${AI_BASE_URL:-https://api.openai.com/v1}
      SERVER_PORT: 8080
    ports:
      - "8080:8080"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    profiles:
      - prod

volumes:
  postgres-data:

networks:
  default:
    name: trident-network
```

### Run with Docker Compose

```bash
# Create .env for docker-compose
cat > .env.docker << 'EOF'
GITHUB_TOKEN=ghp_xxxx
JWT_SECRET=xxxx
AI_API_KEY=sk-xxxx
EOF

# Build and run
docker-compose --env-file .env.docker up --build

# Access
curl http://localhost:8080/swagger-ui.html

# Stop
docker-compose down
```

---

## Kubernetes Deployment

### Namespace & Secrets

```bash
# Create namespace
kubectl create namespace trident

# Create secrets
kubectl create secret generic trident-secrets \
  --from-literal=github-token=ghp_xxxx \
  --from-literal=jwt-secret=xxxx \
  --from-literal=ai-api-key=sk-xxxx \
  -n trident

# Create database secrets
kubectl create secret generic postgres-secrets \
  --from-literal=username=codequality \
  --from-literal=password=xxxx \
  -n trident
```

### ConfigMap

Create `configmap.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: trident-config
  namespace: trident
data:
  SPRING_PROFILES_ACTIVE: "prod"
  SERVER_PORT: "8080"
  DB_URL: "jdbc:postgresql://postgres:5432/codequality"
  GITHUB_API_BASE_URL: "https://api.github.com"
  AI_PROVIDER: "openai"
  AI_MODEL: "gpt-4o-mini"
  AI_BASE_URL: "https://api.openai.com/v1"
  AI_PROVIDER_TIMEOUT_SECONDS: "120"
```

### Deployment

Create `deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: code-quality-platform
  namespace: trident
spec:
  replicas: 2
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: code-quality-platform
  template:
    metadata:
      labels:
        app: code-quality-platform
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      serviceAccountName: trident-sa
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        fsGroup: 1000
      containers:
      - name: app
        image: your-registry/trident-codequality:1.0.0
        imagePullPolicy: IfNotPresent
        ports:
        - name: http
          containerPort: 8080
          protocol: TCP
        env:
        - name: SPRING_PROFILES_ACTIVE
          valueFrom:
            configMapKeyRef:
              name: trident-config
              key: SPRING_PROFILES_ACTIVE
        - name: DB_URL
          valueFrom:
            configMapKeyRef:
              name: trident-config
              key: DB_URL
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: postgres-secrets
              key: username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: postgres-secrets
              key: password
        - name: GITHUB_TOKEN
          valueFrom:
            secretKeyRef:
              name: trident-secrets
              key: github-token
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: trident-secrets
              key: jwt-secret
        - name: AI_API_KEY
          valueFrom:
            secretKeyRef:
              name: trident-secrets
              key: ai-api-key
        - name: AI_PROVIDER
          valueFrom:
            configMapKeyRef:
              name: trident-config
              key: AI_PROVIDER
        - name: AI_MODEL
          valueFrom:
            configMapKeyRef:
              name: trident-config
              key: AI_MODEL
        - name: AI_BASE_URL
          valueFrom:
            configMapKeyRef:
              name: trident-config
              key: AI_BASE_URL
        
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: http
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: http
          initialDelaySeconds: 30
          periodSeconds: 5
          timeoutSeconds: 5
          failureThreshold: 3
        
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        
        securityContext:
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          capabilities:
            drop:
            - ALL
```

### Service

Create `service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: code-quality-platform
  namespace: trident
  labels:
    app: code-quality-platform
spec:
  type: LoadBalancer  # or ClusterIP for internal
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
    name: http
  selector:
    app: code-quality-platform
```

### Deploy to Kubernetes

```bash
# Apply configurations
kubectl apply -f configmap.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml

# Check status
kubectl get pods -n trident
kubectl logs -n trident -f deployment/code-quality-platform

# Port forward (for local testing)
kubectl port-forward -n trident svc/code-quality-platform 8080:80

# Scale
kubectl scale deployment code-quality-platform --replicas=3 -n trident
```

---

## Troubleshooting

### Issue: AI API Key Not Working

```bash
# 1. Verify key format
echo $AI_API_KEY

# 2. Test OpenAI API directly
curl https://api.openai.com/v1/models \
  -H "Authorization: Bearer $AI_API_KEY"

# 3. Check logs
tail -f logs/application.log | grep -i "ai\|openai\|external"

# 4. Verify configuration
curl -s http://localhost:8080/actuator/env | grep -i ai
```

### Issue: Database Connection Timeout

```bash
# 1. Verify PostgreSQL is running
psql -h localhost -U codequality -d codequality -c "SELECT NOW();"

# 2. Check connection string
echo $DB_URL

# 3. Verify credentials
psql -h ${DB_HOST} -U ${DB_USERNAME} -d ${DB_NAME}

# 4. Check network connectivity
nc -zv localhost 5432

# 5. Review application logs
grep -i "database\|connection\|postgres" logs/application.log
```

### Issue: GitHub Token Invalid

```bash
# 1. Verify token format (should start with ghp_)
echo $GITHUB_TOKEN

# 2. Test GitHub API
curl -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/user

# 3. Check token scope
curl -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/user/repos

# 4. Rotate token if leaked
# GitHub Settings → Developer settings → Personal access tokens
```

### Issue: JWT Secret Invalid

```bash
# 1. Verify secret length (minimum 256 bits = 32 bytes)
echo $JWT_SECRET | base64 -d | wc -c

# 2. Regenerate if invalid
openssl rand -base64 32

# 3. Update and restart application
```

### Issue: Scan Hangs in RUNNING Status

```bash
# 1. Check application logs
tail -f logs/application.log | grep -i "scan\|executor"

# 2. Verify AI API is accessible
curl $AI_BASE_URL/chat/completions \
  -H "Authorization: Bearer $AI_API_KEY" \
  -d '{"messages":[{"role":"user","content":"test"}]}'

# 3. Check database for locked records
psql -c "SELECT * FROM scans WHERE status = 'RUNNING';"

# 4. Kill stuck scan manually if necessary
psql -c "UPDATE scans SET status = 'FAILED', completed_at = NOW() WHERE id = <scan_id>;"
```

### Issue: Out of Memory

```bash
# Increase heap size
export _JAVA_OPTIONS="-Xms512m -Xmx1024m"

# Or in Docker
docker run -e JAVA_TOOL_OPTIONS="-Xms512m -Xmx1024m" ...

# Check current usage
jps -lv
jmap -heap <pid>
```

### Issue: Slow Queries

```bash
# Enable slow query logging
export LOGGING_LEVEL_ORG_HIBERNATE_SQL=DEBUG
export LOGGING_LEVEL_ORG_HIBERNATE_TYPE_DESCRIPTOR_SQL_BASICBINDER=TRACE

# Check indexes
psql -c "\d+ scans"
psql -c "SELECT * FROM pg_indexes WHERE tablename = 'scans';"

# Analyze query plan
EXPLAIN ANALYZE SELECT * FROM scans WHERE repository_id = 1;
```

---

## Support & Next Steps

### Getting Help
- 📖 [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- 🤖 [OpenAI API Docs](https://platform.openai.com/docs/api-reference)
- 🐘 [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- 🐙 [GitHub API Docs](https://docs.github.com/en/rest)

### When You're Ready to Go Live

**Checklist:**
- [ ] Obtain AI API key (blocking item)
- [ ] Configure production environment variables
- [ ] Set up PostgreSQL database
- [ ] Configure GitHub token with appropriate scopes
- [ ] Generate strong JWT secret (256+ bits)
- [ ] Set up monitoring & logging
- [ ] Run load tests
- [ ] Perform security audit
- [ ] Document runbooks for ops team
- [ ] Set up backup & disaster recovery

**Deployment Command:**
```bash
java -jar target/code-quality-platform-1.0.0.jar \
  --spring.profiles.active=prod \
  --server.port=8080
```

---

**Happy Deploying! 🚀**
