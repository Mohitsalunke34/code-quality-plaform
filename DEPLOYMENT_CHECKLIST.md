# Trident Code Quality Platform - Deployment Checklist

**Generated:** September 28, 2026  
**For:** Deployment Team  
**Overall Status:** ✅ Ready for Configuration

---

## 📋 Pre-Deployment Checklist

### Phase 1: Prerequisites Installation (30 minutes)

**Database Setup**
- [ ] PostgreSQL 14+ installed
- [ ] PostgreSQL service running
- [ ] psql command-line tool available
- [ ] Network connectivity to PostgreSQL verified

**Development Tools**
- [ ] Java 21 JDK installed
- [ ] Maven 3.9+ installed
- [ ] Git installed
- [ ] Text editor/IDE (VS Code, IntelliJ, etc.)

**External Accounts**
- [ ] GitHub account created
- [ ] GitHub Personal Access Token generated
  - [ ] Scopes: `repo` (full control of repositories)
  - [ ] Token format: `ghp_xxxxxxxxxxxxxxxxxxxxxxxx`
- [ ] OpenAI OR Azure account created
  - [ ] OpenAI: API key obtained (`sk-proj-xxxxxxxx`)
  - [ ] Azure: OpenAI service created + model deployed

**Command Verification**
```bash
# Test each command returns expected output
java -version              # Should show Java 21
mvn --version              # Should show Maven 3.9+
git --version              # Should show git 2.x+
psql --version             # Should show psql 14+
```

---

### Phase 2: Database Configuration (15 minutes)

**Create Database User**
```bash
# [ ] Open PostgreSQL terminal
psql -U postgres

# [ ] Execute in psql:
CREATE USER codequality WITH ENCRYPTED PASSWORD 'choose-strong-password';
CREATE DATABASE codequality OWNER codequality;
GRANT ALL PRIVILEGES ON DATABASE codequality TO codequality;
ALTER ROLE codequality WITH CREATEDB;
\q
```

**Verify Connection**
```bash
# [ ] Test connection
psql -h localhost -U codequality -d codequality -c "SELECT NOW();"

# [ ] Expected: 2026-09-28 14:30:45.123456+00
```

**Database Configuration Values**
```
[ ] DB_URL = jdbc:postgresql://localhost:5432/codequality
[ ] DB_USERNAME = codequality
[ ] DB_PASSWORD = _________________ (secure password)
[ ] DB_PORT = 5432 (default)
```

---

### Phase 3: Security Configuration (10 minutes)

**JWT Secret Generation**
```bash
# [ ] Generate 256-bit secret
openssl rand -base64 32

# [ ] Result should be ~44 characters
# [ ] Store securely (DON'T commit to git)
# [ ] Example: ABC123def45ghi6789jkl0mno1pqrstu/vwx==

JWT_SECRET = ________________________________
```

**GitHub Token Setup**
```bash
# [ ] Go to: https://github.com/settings/tokens
# [ ] Click: "Generate new token (classic)" or "Generate new token"
# [ ] Name: "Trident Code Quality Platform"
# [ ] Expiration: 90 days
# [ ] Scopes: 
#     [ ] repo (full control of repositories)
#     [ ] read:user (read user information)
#     [ ] admin:repo_hook (manage hooks)
# [ ] Copy token (appears only once)
# [ ] Format: ghp_xxxxxxxxxxxxxxxxxxxxxxxx

GITHUB_TOKEN = ________________________________
```

**Configuration Values Checklist**
```
[ ] JWT_SECRET (256+ bits, base64 encoded)
[ ] JWT_EXPIRATION_MS = 3600000 (1 hour)
[ ] JWT_REFRESH_EXPIRATION_MS = 604800000 (7 days)
[ ] GITHUB_TOKEN = ghp_xxxxxxxx
[ ] GITHUB_API_BASE_URL = https://api.github.com
```

---

### Phase 4: AI Provider Setup (10 minutes)

**⚠️ CRITICAL - This is the ONLY blocking item**

**Option A: OpenAI (RECOMMENDED)**
```bash
# [ ] Go to: https://platform.openai.com/account/api-keys
# [ ] Click: "Create new secret key"
# [ ] Copy key (appears only once)
# [ ] Format: sk-proj-xxxxxxxxxxxxxxxxxxxxxxxx
# [ ] SAVE SECURELY - cannot be retrieved later

AI_PROVIDER = openai
AI_API_KEY = ________________________________
AI_MODEL = gpt-4o-mini
AI_BASE_URL = https://api.openai.com/v1
AI_PROVIDER_TIMEOUT_SECONDS = 120
```

**Option B: Azure OpenAI**
```bash
# [ ] Go to: https://portal.azure.com
# [ ] Create Resource Group (e.g., trident-rg)
# [ ] Create "Cognitive Services" > "OpenAI"
# [ ] Deploy GPT model (gpt-4o-mini)
# [ ] Get API key from "Keys and Endpoint"
# [ ] Format: 32-character hex string

AI_PROVIDER = azure
AI_API_KEY = ________________________________
AI_BASE_URL = https://your-resource.openai.azure.com
AI_AZURE_DEPLOYMENT = codequality-deployment
AI_AZURE_API_VERSION = 2024-02-15-preview
AI_PROVIDER_TIMEOUT_SECONDS = 120
```

**Choose One and Document**
```
Selected Provider: [ ] OpenAI  [ ] Azure OpenAI

Provider-Specific Values:
[ ] AI_API_KEY = ___________________________
[ ] AI_BASE_URL = ___________________________
[ ] (If Azure) AI_AZURE_DEPLOYMENT = _______
```

---

### Phase 5: Application Configuration (15 minutes)

**Create Environment File**
```bash
# [ ] Create file: .env (in project root)
# [ ] DO NOT commit this file to git
# [ ] Add to .gitignore: echo ".env" >> .gitignore

# [ ] Copy content below and fill in blanks:
```

**Environment File Template (.env)**
```properties
# ============================================================================
# DATABASE
# ============================================================================
DB_URL=jdbc:postgresql://localhost:5432/codequality
DB_USERNAME=codequality
DB_PASSWORD=<YOUR_DB_PASSWORD>

# ============================================================================
# SECURITY - JWT
# ============================================================================
JWT_SECRET=<YOUR_256_BIT_SECRET>
JWT_EXPIRATION_MS=3600000
JWT_REFRESH_EXPIRATION_MS=604800000

# ============================================================================
# GITHUB
# ============================================================================
GITHUB_TOKEN=<YOUR_GITHUB_TOKEN>
GITHUB_API_BASE_URL=https://api.github.com

# ============================================================================
# AI (CRITICAL - Choose OpenAI or Azure)
# ============================================================================
AI_PROVIDER=openai
AI_API_KEY=<YOUR_AI_API_KEY>
AI_MODEL=gpt-4o-mini
AI_BASE_URL=https://api.openai.com/v1
AI_PROVIDER_TIMEOUT_SECONDS=120

# For Azure, use instead:
# AI_PROVIDER=azure
# AI_BASE_URL=https://your-resource.openai.azure.com
# AI_AZURE_DEPLOYMENT=codequality-deployment
# AI_AZURE_API_VERSION=2024-02-15-preview

# ============================================================================
# APPLICATION
# ============================================================================
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
```

**Validation Checklist**
```bash
# [ ] File .env created in project root
# [ ] All required variables filled in
# [ ] No empty values (except commented lines)
# [ ] File permissions: readable only by user
# [ ] NOT in git (verify: git status | grep .env)
```

---

### Phase 6: Build & Package (20 minutes)

**Build Application**
```bash
# [ ] Navigate to project directory
cd /path/to/code-quality-platform

# [ ] Clean previous builds
mvn clean

# [ ] Build application (creates JAR file)
mvn package -DskipTests

# [ ] Expected output: BUILD SUCCESS
# [ ] JAR file location: target/code-quality-platform-1.0.0.jar
# [ ] JAR file size: ~30-50 MB
```

**Build Verification**
```bash
# [ ] JAR file exists
ls -lh target/code-quality-platform-1.0.0.jar

# [ ] JAR is executable
file target/code-quality-platform-1.0.0.jar

# [ ] Expected: "JAR archive data"
```

---

### Phase 7: Database Migration (5 minutes)

**Automatic via Flyway**
```bash
# Flyway migrations run automatically on startup
# Schema will be created during first application run

# [ ] Flyway configuration in pom.xml verified
# [ ] Migration file: src/main/resources/db/migration/V1__init.sql
# [ ] Table count: 7 tables
#     [ ] users
#     [ ] code_repositories
#     [ ] scans
#     [ ] code_issues
#     [ ] generated_tests
#     [ ] compliance_checks
#     [ ] quality_scores
```

**Manual Verification (Optional)**
```bash
# [ ] Connect to database after first run
psql -U codequality -d codequality -c "\dt"

# [ ] Expected output: 7 tables listed
# [ ] Verify indexes: \di
# [ ] Expected: Multiple indexes on foreign keys
```

---

### Phase 8: Local Startup Test (10 minutes)

**Start Application Locally**
```bash
# [ ] Load environment variables
source .env  # Linux/Mac
$env:var = value  # Windows PowerShell

# [ ] Run application
java -jar target/code-quality-platform-1.0.0.jar

# [ ] Watch for startup logs
# [ ] Expected: "Tomcat initialized with port(s): 8080"
# [ ] Expected: "Started CodeQualityPlatformApplication in X.XXX seconds"
```

**Health Check**
```bash
# [ ] In separate terminal, test health endpoint
curl http://localhost:8080/actuator/health

# [ ] Expected response:
# {
#   "status": "UP",
#   "components": {
#     "db": { "status": "UP" },
#     ...
#   }
# }
```

**API Documentation**
```bash
# [ ] Open browser: http://localhost:8080/swagger-ui.html
# [ ] Expected: Swagger UI loads
# [ ] Expected: 20+ API endpoints listed
# [ ] Try it out button works
```

---

### Phase 9: Functional Testing (30 minutes)

**Test Authentication Flow**
```bash
# [ ] Register new user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testadmin",
    "email": "admin@test.com",
    "password": "TestPass123!"
  }'

# [ ] Expected: 201 Created
# [ ] Response contains: accessToken, refreshToken

# [ ] Save token for next tests
export TOKEN="<accessToken_from_response>"
```

**Test Repository Registration**
```bash
# [ ] Register a test repository
curl -X POST http://localhost:8080/api/v1/repositories \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "owner": "spring-projects",
    "name": "spring-boot",
    "url": "https://github.com/spring-projects/spring-boot",
    "description": "Spring Boot framework"
  }'

# [ ] Expected: 201 Created
# [ ] Response contains: repositoryId

# [ ] Save ID for next test
export REPO_ID=1
```

**Test Scan Initiation**
```bash
# [ ] Start scan (may fail if no AI key, but should create scan record)
curl -X POST http://localhost:8080/api/v1/repositories/$REPO_ID/scan \
  -H "Authorization: Bearer $TOKEN"

# [ ] Expected: 200 or 202
# [ ] Response contains: scanId, status: "PENDING"

# [ ] Save scan ID
export SCAN_ID=1
```

**Test Scan Status Retrieval**
```bash
# [ ] Get scan details
curl -X GET http://localhost:8080/api/v1/scans/$SCAN_ID \
  -H "Authorization: Bearer $TOKEN"

# [ ] Expected: 200 OK
# [ ] Response contains: scan details
# [ ] If AI key configured: issues, tests, scores
```

**Test Dashboard**
```bash
# [ ] Get dashboard summary
curl -X GET http://localhost:8080/api/v1/dashboard/summary \
  -H "Authorization: Bearer $TOKEN"

# [ ] Expected: 200 OK
# [ ] Response contains: metrics object
```

---

### Phase 10: Security Verification (15 minutes)

**JWT Token Validation**
```bash
# [ ] Try without token
curl -X GET http://localhost:8080/api/v1/repositories

# [ ] Expected: 401 Unauthorized

# [ ] Try with invalid token
curl -X GET http://localhost:8080/api/v1/repositories \
  -H "Authorization: Bearer invalid.token.here"

# [ ] Expected: 401 Unauthorized

# [ ] Try with expired token (use admin token, wait until expired)
# [ ] Expected: 401 Unauthorized
```

**Password Security**
```bash
# [ ] Attempt login with wrong password
curl -X POST http://localhost:8080/api/v1/auth/login \
  -d '{"username":"testadmin","password":"WrongPassword"}'

# [ ] Expected: 401 Unauthorized (not 400 or 500)
```

**Input Validation**
```bash
# [ ] Try to register with invalid email
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test",
    "email": "invalid-email",
    "password": "Pass123!"
  }'

# [ ] Expected: 400 Bad Request (validation error)
```

---

### Phase 11: Performance Baseline (20 minutes)

**Database Response Time**
```bash
# [ ] Time a simple repository list query
time curl -s -X GET http://localhost:8080/api/v1/repositories \
  -H "Authorization: Bearer $TOKEN" > /dev/null

# [ ] Expected: < 100ms (should be instant from local DB)
```

**API Response Time (without AI)**
```bash
# [ ] Time scan detail retrieval
time curl -s -X GET http://localhost:8080/api/v1/scans/$SCAN_ID \
  -H "Authorization: Bearer $TOKEN" > /dev/null

# [ ] Expected: < 200ms
```

**Memory Usage**
```bash
# [ ] Check running process
ps aux | grep java

# [ ] Expected: Memory usage reasonable (< 500MB for single instance)
```

**Database Connections**
```bash
# [ ] Check active connections
psql -c "SELECT count(*) FROM pg_stat_activity;"

# [ ] Expected: < 10 connections for single app instance
```

---

### Phase 12: Logging Verification (10 minutes)

**Application Logs**
```bash
# [ ] Check for ERROR level logs
# [ ] Expected: Clean startup with no errors
# [ ] Expected: INFO level logs for requests
# [ ] Expected: DEBUG level logs for database (if configured)
```

**Log Files**
```bash
# [ ] Check if logs directory created: logs/
# [ ] Expected: application.log file exists
# [ ] Expected: Log rotation configured (via Spring defaults)
```

**Actuator Endpoints**
```bash
# [ ] Access health endpoint
curl http://localhost:8080/actuator/health

# [ ] Expected: UP status
# [ ] Components: db, applicationReadyEvent, livenessState, readinessState

# [ ] Access info endpoint
curl http://localhost:8080/actuator/info

# [ ] Expected: Application version information
```

---

### Phase 13: AI API Key Testing (15 minutes)

**⚠️ Only if AI key has been configured**

**Test OpenAI Connection**
```bash
# [ ] Test OpenAI API directly
curl https://api.openai.com/v1/models \
  -H "Authorization: Bearer $AI_API_KEY"

# [ ] Expected: 200 OK with model list
```

**Test Azure OpenAI Connection**
```bash
# [ ] Test Azure endpoint (if using Azure)
curl https://<your-resource>.openai.azure.com/openai/models?api-version=2024-02-15-preview \
  -H "api-key: $AI_API_KEY"

# [ ] Expected: 200 OK with model list
```

**Test with Small Analysis**
```bash
# [ ] Create test repository with small Java file
# [ ] Register it via API
# [ ] Initiate scan
# [ ] Monitor logs for AI calls
# [ ] Expected: See chat completion request/response in logs
# [ ] Expected: Scan completes with issues detected
```

**Cost Verification**
```bash
# [ ] Check OpenAI usage dashboard
# [ ] Navigate to: https://platform.openai.com/account/usage/overview
# [ ] Expected: See recent API calls (if test ran)
# [ ] Verify charges align with expectations (~$0.15-0.30 per scan)
```

---

### Phase 14: Documentation Review (10 minutes)

**Generated Documentation**
- [ ] EXECUTIVE_SUMMARY.md reviewed
- [ ] ASSESSMENT.md reviewed
- [ ] SETUP_GUIDE.md reviewed
- [ ] CODEBASE_STATUS.md reviewed
- [ ] ARCHITECTURE_DIAGRAMS.md reviewed

**Team Handoff**
- [ ] README.md updated with latest info
- [ ] Deployment instructions documented
- [ ] Troubleshooting guide created
- [ ] Contact information documented

---

### Phase 15: Pre-Production Deployment (30 minutes)

**Choose Deployment Platform**
- [ ] **Option A: Docker**
  - [ ] Docker installed
  - [ ] Docker image built
  - [ ] Container tested locally
  - [ ] docker-compose.yml prepared
  - [ ] Environment variables mapped to Docker secrets

- [ ] **Option B: Kubernetes**
  - [ ] kubectl installed
  - [ ] Kubernetes cluster accessible
  - [ ] Namespace created: trident
  - [ ] Secrets created in namespace
  - [ ] ConfigMap created
  - [ ] Deployment manifest prepared
  - [ ] Service manifest prepared

- [ ] **Option C: Cloud Platform**
  - [ ] AWS ECS / Azure Container Instances / etc.
  - [ ] Cloud credentials configured
  - [ ] IaC (Terraform/CloudFormation) prepared
  - [ ] Load balancer configured

**Staging Environment**
- [ ] Separate staging database created
- [ ] Staging environment variables configured
- [ ] Staging deployment executed
- [ ] Staging health checks passed
- [ ] Staging functional tests passed

---

## ✅ Pre-Deployment Sign-Off Checklist

### Database
- [ ] PostgreSQL installed and running
- [ ] Database 'codequality' created
- [ ] User 'codequality' created with proper privileges
- [ ] Flyway migration ready
- [ ] Connection tested and working

### Configuration
- [ ] .env file created (NOT in git)
- [ ] All required environment variables set
- [ ] JWT secret generated and strong (256+ bits)
- [ ] GitHub token obtained and verified
- [ ] AI provider configured (OpenAI or Azure)

### Security
- [ ] JWT secret stored securely (not in code)
- [ ] API keys stored securely (not in code)
- [ ] Database password strong (12+ characters)
- [ ] SSL/TLS configured (for production)
- [ ] CORS configured for frontend domain

### Build & Test
- [ ] Application builds successfully (mvn package)
- [ ] Database migrations run on startup
- [ ] Application starts without errors
- [ ] Health check endpoint responds
- [ ] API endpoints accessible
- [ ] Authentication working (register, login)
- [ ] Repository management working
- [ ] Dashboard accessible

### Documentation
- [ ] Architecture diagrams reviewed
- [ ] Setup guide reviewed
- [ ] Troubleshooting guide available
- [ ] API documentation (Swagger) verified
- [ ] Deployment procedures documented

### Ready to Deploy
- [ ] All checkboxes above: ✅ CHECKED
- [ ] Stakeholder sign-off: _____________
- [ ] Date: _____________
- [ ] Deployment Target: _______________

---

## 🚀 Deployment Commands

### Local Development
```bash
source .env
mvn spring-boot:run
# Access: http://localhost:8080/swagger-ui.html
```

### Docker
```bash
docker build -t trident-codequality:1.0.0 .
docker-compose --env-file .env.docker up
# Access: http://localhost:8080/swagger-ui.html
```

### Kubernetes
```bash
kubectl apply -f configmap.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl port-forward svc/code-quality-platform 8080:80
# Access: http://localhost:8080/swagger-ui.html
```

### Cloud Deployment (Example: Azure)
```bash
# Using your IaC tool (Bicep/Terraform)
az deployment group create \
  --resource-group trident-rg \
  --template-file infra.bicep
```

---

## ⚠️ Common Issues & Solutions

| Issue | Solution | Docs |
|-------|----------|------|
| AI API Key not working | Verify key format, check provider docs | SETUP_GUIDE.md |
| Database connection timeout | Check PostgreSQL running, verify DB_URL | SETUP_GUIDE.md |
| JWT token invalid | Regenerate secret with `openssl rand -base64 32` | SETUP_GUIDE.md |
| GitHub token insufficient | Add scopes in GitHub settings | SETUP_GUIDE.md |
| Scan hangs in RUNNING | Check AI API accessibility, review logs | SETUP_GUIDE.md |
| Out of memory | Increase JVM heap: `-Xmx1024m` | SETUP_GUIDE.md |

---

## 📞 Support Contacts

- **Architecture Questions:** See ASSESSMENT.md
- **Setup Issues:** See SETUP_GUIDE.md  
- **Code Structure:** See CODEBASE_STATUS.md
- **Deployment:** See this checklist + SETUP_GUIDE.md
- **Production Issues:** Enable DEBUG logging, check SETUP_GUIDE.md troubleshooting

---

## Summary

**Total Setup Time:** ~3-4 hours (first time)

**Critical Path:**
1. ✅ Database setup (15 min)
2. ✅ Obtain AI API key (5 min) ← BLOCKING
3. ✅ Configure environment (10 min)
4. ✅ Build application (20 min)
5. ✅ Run locally & test (30 min)
6. ✅ Deploy to staging (30 min)
7. ✅ Performance testing (20 min)

**Status After This Checklist:** 🚀 Ready for Production

---

**Checklist Version:** 1.0  
**Last Updated:** September 28, 2026  
**Next Review:** After first production deployment
