# Trident Code Quality Platform - Architecture & Flow Diagrams

---

## 1. System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                        FRONTEND / CLIENT                             │
│                    (Browser, Mobile, CLI)                            │
└─────────────────────────────┬───────────────────────────────────────┘
                              │ HTTPS REST API
                              ▼
┌──────────────────────────────────────────────────────────────────────┐
│                                                                        │
│                    SPRING BOOT APPLICATION (Port 8080)               │
│                                                                        │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                      WEB LAYER (Controllers)                    │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐ │  │
│  │  │AuthController│  │RepositoryCon │  │ScanController │      │ │  │
│  │  │              │  │      troller  │  │DashboardCtrl │      │ │  │
│  │  └──────────────┘  └──────────────┘  └──────────────────────┘ │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                              ▲                                         │
│                              │ Injection                              │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                    SERVICE LAYER (Business Logic)              │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌─────────────────────┐ │  │
│  │  │AuthService   │  │RepositorySvc │  │ScanService   │       │ │  │
│  │  │              │  │               │  │ScanExecutor  │ @Async│ │  │
│  │  └──────────────┘  └──────────────┘  └─────────────────────┘ │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌─────────────────────┐ │  │
│  │  │DashboardSvc  │  │GitHubService │  │AiAnalysisService    │ │  │
│  │  │              │  │(GitHub REST) │  │(OpenAI/Azure REST)  │ │  │
│  │  └──────────────┘  └──────────────┘  └─────────────────────┘ │  │
│  │                                                                  │  │
│  │  ┌──────────────────────────────────────────────────────────┐  │  │
│  │  │          QualityScoringService (Metrics)                │  │  │
│  │  └──────────────────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                              ▲                                         │
│                              │ Injection                              │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │               REPOSITORY LAYER (Data Access)                   │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌─────────────────────┐ │  │
│  │  │UserRepository│  │CodeRepoRepo  │  │ScanRepository │     │ │  │
│  │  └──────────────┘  └──────────────┘  └─────────────────────┘ │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌─────────────────────┐ │  │
│  │  │CodeIssueRepo │  │GeneratedTest │  │ComplianceCheckRepo  │ │  │
│  │  │              │  │    Repo      │  │QualityScoreRepo     │ │  │
│  │  └──────────────┘  └──────────────┘  └─────────────────────┘ │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                              ▲                                         │
│                              │ SQL                                    │
└──────────────────────────────┼────────────────────────────────────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │  PostgreSQL Database │
                    │  (codequality DB)    │
                    │  ┌────────────────┐  │
                    │  │ users          │  │
                    │  │ code_repos     │  │
                    │  │ scans          │  │
                    │  │ code_issues    │  │
                    │  │ generated_tests│  │
                    │  │ compliance_chks│  │
                    │  │ quality_scores │  │
                    │  └────────────────┘  │
                    └──────────────────────┘
```

---

## 2. Request Flow Diagram

### User Registration & Login Flow

```
┌─────────┐
│ Client  │
└────┬────┘
     │ POST /api/v1/auth/register
     │ {username, email, password}
     ▼
┌──────────────────────┐      ┌─────────────────────────┐
│  AuthController      │─────▶│  AuthService            │
│  (POST /register)    │      │  ├─ Hash password(BCrypt)
└──────────────────────┘      │  ├─ Save User entity    │
                              │  └─ Return JWT tokens  │
                              └──────────┬──────────────┘
                                         │
                                         ▼
                              ┌─────────────────────┐
                              │  UserRepository     │
                              │  (JPA)              │
                              └──────────┬──────────┘
                                         │ SQL INSERT
                                         ▼
                              ┌─────────────────────┐
                              │  PostgreSQL - users │
                              └─────────────────────┘
                                         │
                    ┌────────────────────┘
                    │
                    ▼
         ┌────────────────────────┐
         │ AuthResponse:          │
         │ {                      │
         │   accessToken: "...",  │
         │   refreshToken: "..." │
         │ }                      │
         └────────────────────────┘
```

### Scan Execution Flow

```
┌────────────┐
│  Client    │
└─────┬──────┘
      │ POST /api/v1/repositories/{id}/scan
      │ Authorization: Bearer JWT
      ▼
┌──────────────────────┐
│ ScanController       │
│ (POST /scan)         │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐      ┌───────────────────────┐
│ ScanService          │─────▶│ ScanRepository        │
│ ├─ Create Scan       │      │ ├─ Save Scan with    │
│ │  (PENDING status)  │      │ │  PENDING status    │
│ └─ Trigger async     │      └─────────┬─────────────┘
│    execution         │                 │ SQL INSERT
└──────────┬───────────┘                 ▼
           │                  ┌──────────────────────┐
           └─────────────────▶│ PostgreSQL - scans   │
           (Transaction      └──────────────────────┘
            completion)
           │
           ▼ (After @Commit)
┌──────────────────────────────┐
│ TransactionSynchronization   │
│ - Registers afterCommit()    │
│ - Triggers ScanExecutor      │
└──────────┬───────────────────┘
           │
           ▼
┌──────────────────────────────────────┐
│ @Async ScanExecutor.execute()        │
│ (Separate thread)                    │
│                                      │
│ 1. Update Scan status: RUNNING       │
│ 2. Call GitHubService                │
│    - Resolve commit SHA              │
│    - Fetch source files (max 25)     │
│ 3. For each file:                    │
│    │                                 │
│    ├─▶ Call AiAnalysisService       │
│    │   - Send to OpenAI/Azure        │
│    │   - Parse JSON response         │
│    │   - Extract issues, tests,      │
│    │     compliance, quality         │
│    │                                 │
│    ├─▶ Create CodeIssue entities    │
│    ├─▶ Create GeneratedTest entities│
│    └─▶ Create ComplianceCheck       │
│                                      │
│ 4. Calculate QualityScore            │
│ 5. Update Scan status: COMPLETED     │
│ 6. Persist all results               │
└──────────┬───────────────────────────┘
           │
           ▼
   ┌──────────────────────────┐
   │ Database Operations:     │
   │ ├─ UPDATE scans SET ... │
   │ ├─ INSERT code_issues   │
   │ ├─ INSERT generated_tests
   │ ├─ INSERT compliance..  │
   │ └─ INSERT quality_scores│
   └──────────────────────────┘
           │
           ▼
   ┌──────────────────────────┐
   │ Client Poll              │
   │ GET /api/v1/scans/{id}   │
   │ ──────────────────────▶  │
   │  Returns COMPLETED       │
   │  + all results           │
   └──────────────────────────┘
```

---

## 3. Data Model Relationships

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                   │
│  ┌──────────────┐                 ┌─────────────────────────┐   │
│  │ User         │◀────────────────│ CodeRepository          │   │
│  ├─ id (PK)    │   registered_by │ ├─ id (PK)              │   │
│  ├─ username   │                  │ ├─ owner (GitHub)      │   │
│  ├─ email      │                  │ ├─ name (repo name)    │   │
│  ├─ password   │                  │ ├─ url                  │   │
│  ├─ role       │                  │ ├─ default_branch      │   │
│  └─ created_at │                  │ ├─ active (boolean)    │   │
│                │                  │ └─ created_at          │   │
│                │                  └────────────┬────────────┘   │
│                │                               │                 │
│                │                               │ 1:N            │
│  ┌─────────────┴──────────────┐                ▼                │
│  │                            │        ┌────────────────┐       │
│  │ triggered_by               │        │ Scan           │       │
│  │  (user who ran scan)       │        ├─ id (PK)       │       │
│  │                            │        ├─ status       │       │
│  │                            │        ├─ commit_sha    │       │
│  │                            │        ├─ branch        │       │
│  │                            │        ├─ files_scanned │       │
│  │                            │        ├─ started_at    │       │
│  │                            │        ├─ completed_at  │       │
│  │                            │        └─ created_at    │       │
│  │                            │                 │        │       │
│  │                            └────────────────┤        │       │
│  │                                             │        │       │
│  └─────────────────────────────────────────────┼───────┼────┐  │
│                                                │        │    │  │
│                                  ┌─────────────▼─────┬──┴─┐  │  │
│                                  │                   │    │  │  │
│                                  ▼                   ▼    ▼  ▼  │
│                         ┌───────────────────────┐                │
│                         │ QualityScore (1:1)    │                │
│                         ├─ overall_score       │                │
│                         ├─ maintainability     │ ┌────────────┐ │
│                         ├─ reliability         │ │CodeIssue   │ │
│                         ├─ security            │ ├─ severity  │ │
│                         ├─ coverage_estimate   │ ├─ category  │ │
│                         └─ grade (A-F)         │ ├─ title     │ │
│                                                │ ├─ line_num  │ │
│                                                │ └─ file_path │ │
│                         ┌────────────────────┐ └────────────┘  │
│                         │GeneratedTest (1:N) │                 │
│                         ├─ source_file_path  │ ┌──────────────┐│
│                         ├─ test_file_path    │ │ComplianceCheck
│                         ├─ framework         │ ├─ standard    ││
│                         ├─ test_code (TEXT)  │ ├─ rule        ││
│                         └─ description       │ ├─ status      ││
│                                              │ └─ details     ││
│                                              └──────────────┘│
└──────────────────────────────────────────────────────────────────┘

Legend:
  PK = Primary Key
  FK = Foreign Key (implied by arrows)
  1:1 = One-to-one relationship
  1:N = One-to-many relationship
```

---

## 4. Authentication & Security Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                   │
│  Step 1: LOGIN                                                   │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ Client: POST /api/v1/auth/login                           │ │
│  │         {username, password}                              │ │
│  │                                                            │ │
│  │ AuthService:                                              │ │
│  │ ├─ Find user by username                                 │ │
│  │ ├─ Compare password with BCrypt hash                     │ │
│  │ ├─ If match: Generate JWT tokens (HMAC256)              │ │
│  │ └─ Return {accessToken, refreshToken}                   │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                   │
│  Step 2: API REQUEST with Token                                 │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ Client: GET /api/v1/repositories                         │ │
│  │         Headers: Authorization: Bearer eyJhbGciOi... │ │
│  │                                                            │ │
│  │ JwtAuthenticationFilter (runs for every request):         │ │
│  │ ├─ Extract token from Authorization header               │ │
│  │ ├─ Validate token signature (HMAC256 check)              │ │
│  │ ├─ Check expiration time                                 │ │
│  │ ├─ Decode claims: {userId, username, roles}             │ │
│  │ └─ Set SecurityContextHolder with authentication         │ │
│  │                                                            │ │
│  │ Controller:                                               │ │
│  │ ├─ @Secured("ROLE_USER") enforced                       │ │
│  │ ├─ Permission check passed                               │ │
│  │ └─ Execute business logic                                │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                   │
│  Step 3: TOKEN REFRESH                                          │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ Client: POST /api/v1/auth/refresh                        │ │
│  │         {refreshToken}                                   │ │
│  │                                                            │ │
│  │ AuthService:                                              │ │
│  │ ├─ Validate refresh token                                │ │
│  │ ├─ Generate new access token (short-lived)              │ │
│  │ └─ Return {newAccessToken, refreshToken}                │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                   │
│  Security Configuration:                                        │
│  ├─ JWT Secret: 256+ bits (environment variable)             │ │
│  ├─ Access Token: Expires in 1 hour                          │ │
│  ├─ Refresh Token: Expires in 7 days                         │ │
│  ├─ Signing Algorithm: HMAC256                               │ │
│  └─ Password Encoding: BCrypt with salt                      │ │
└─────────────────────────────────────────────────────────────────┘
```

---

## 5. Async Scan Processing

```
                    ┌─────────────────────────────┐
                    │  ScanService.startScan()    │
                    │  (Main Thread)              │
                    └──────────────┬──────────────┘
                                   │
                  ┌────────────────┼────────────────┐
                  │                                 │
                  ▼                                 ▼
        ┌──────────────────┐          ┌─────────────────────┐
        │ 1. Create Scan   │          │ 2. Register        │
        │    entity        │          │    Transaction      │
        │    (PENDING)     │          │    Callback         │
        └────────┬─────────┘          └────────────┬────────┘
                 │                                  │
                 ▼                                  ▼
        ┌──────────────────┐          ┌─────────────────────┐
        │ 2. Save to DB    │          │ 3. On transaction   │
        │    (committed)   │          │    commit:          │
        │                  │          │    Invoke @Async    │
        └────────┬─────────┘          │    method           │
                 │                    └────────────┬────────┘
                 │                                 │
                 └────────────────┬────────────────┘
                                  │
                    ┌─────────────▼────────────────┐
                    │  SEPARATE THREAD (Thread     │
                    │  Pool / Spring @Async)       │
                    │                              │
                    │  ScanExecutor.execute(id)    │
                    │  ├─ Fetch Scan from DB       │
                    │  ├─ Set Status: RUNNING      │
                    │  ├─ Call GitHubService       │
                    │  ├─ Call AiAnalysisService   │
                    │  ├─ Persist Issues           │
                    │  ├─ Persist Tests            │
                    │  ├─ Persist Compliance       │
                    │  ├─ Calculate Quality Score  │
                    │  ├─ Set Status: COMPLETED    │
                    │  └─ Save to DB               │
                    │                              │
                    │  (Non-blocking to client)    │
                    └──────────────────────────────┘
                    
Main Thread Returns Immediately:
┌────────────────────────────────────┐
│ Client:                            │
│ POST /api/v1/scan                  │
│   ──────────────────────────────▶  │
│   Returns HTTP 201:                │
│   {scanId: 123, status: PENDING}   │
│                                    │
│ Client polls:                      │
│ GET /api/v1/scans/123 (every 5s)   │
│   ──────────────────────────────▶  │
│   Returns status: RUNNING          │
│   Returns status: COMPLETED ✓      │
│   + issues, tests, scores          │
└────────────────────────────────────┘
```

---

## 6. AI Analysis Integration

```
┌────────────────────────────────────────────────────────────────┐
│                                                                  │
│  ScanExecutor (in Async thread)                               │
│  └─ For each SourceFile:                                      │
│     │                                                          │
│     ▼                                                          │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │ AiAnalysisService.analyzeFile(SourceFile)              │ │
│  │                                                         │ │
│  │ Build Request:                                          │ │
│  │ ├─ Model: "gpt-4o-mini"                                │ │
│  │ ├─ Temperature: 0.1 (deterministic)                    │ │
│  │ ├─ Response Format: JSON                               │ │
│  │ ├─ System Prompt: Analysis instructions               │ │
│  │ └─ User Message: "File: path/to/file.java\n\n```\n[code]\n```" │
│  └──────────────────────┬──────────────────────────────────┘ │
│                         │                                      │
│                         ▼                                      │
│            ┌────────────────────────┐                         │
│            │ External API Call      │                         │
│            │ (over HTTPS)           │                         │
│            │                        │                         │
│            │ Option 1:              │                         │
│            │ POST https://api.openai.com/v1/chat/completions │
│            │                        │                         │
│            │ Option 2:              │                         │
│            │ POST https://your-resource.openai.azure.com/... │
│            │ Headers:               │                         │
│            │ Authorization: Bearer sk-xxx or api-key: xxx    │
│            └────────────────┬───────────────────────────────┘ │
│                             │                                  │
│                             ▼                                  │
│            ┌────────────────────────────────────────────────┐ │
│            │ AI Service Processes:                          │ │
│            │ ├─ Analyzes code for bugs                     │ │
│            │ ├─ Identifies security issues                 │ │
│            │ ├─ Generates unit tests                       │ │
│            │ ├─ Assesses compliance                        │ │
│            │ └─ Calculates quality metrics                 │ │
│            │                                                │ │
│            │ Returns JSON:                                  │ │
│            │ {                                              │ │
│            │   "issues": [{...}, {...}],                   │ │
│            │   "tests": [{...}, {...}],                    │ │
│            │   "quality": {                                │ │
│            │     "maintainability": 85,                    │ │
│            │     "reliability": 78,                        │ │
│            │     "security": 92,                           │ │
│            │     "coverageEstimate": 65                    │ │
│            │   },                                           │ │
│            │   "compliance": [{...}, {...}]                │ │
│            │ }                                              │ │
│            └────────────┬───────────────────────────────────┘ │
│                         │                                      │
│                         ▼                                      │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │ Parse & Apply Results:                                  │ │
│  │ ├─ Create CodeIssue entities                           │ │
│  │ ├─ Create GeneratedTest entities                       │ │
│  │ ├─ Create ComplianceCheck entities                     │ │
│  │ ├─ Build QualityScore entity                           │ │
│  │ └─ Persist all to database                             │ │
│  └──────────────────────┬──────────────────────────────────┘ │
│                         │                                      │
│                         ▼                                      │
│              Database Update Complete                        │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

---

## 7. Database Schema Diagram

```
┌─────────────────┐
│     users       │
├─────────────────┤
│ id (PK)         │
│ username (U)    │◀─────┐
│ email (U)       │      │
│ password_hash   │      │ registered_by
│ role            │      │
│ enabled         │      │
│ created_at      │      │
└─────────────────┘      │
                         │
                         │
    ┌────────────────────┴──────────────────┐
    │                                       │
    ▼                                       │
┌─────────────────────────────────┐         │
│   code_repositories             │         │
├─────────────────────────────────┤         │
│ id (PK)                         │         │
│ owner                           │         │
│ name                            │         │ triggered_by
│ url                             │         │
│ default_branch                  │         │
│ description                     │         │
│ active                          │         │
│ registered_by (FK) ─────────────┴────────▶│
│ created_at                      │         │
└──────────────┬──────────────────┘         │
               │                            │
               │ (1:N)                      │
               │                            │
               ▼                            ▼
    ┌──────────────────┐          ┌─────────────────┐
    │     scans        │          │  └─ (reference) │
    ├──────────────────┤          └─────────────────┘
    │ id (PK)          │
    │ repository_id(FK)├─┐
    │ triggered_by(FK) │ └──────────────────┐
    │ status           │                     │
    │ branch           │                     │
    │ commit_sha       │                     │
    │ files_scanned    │                     │
    │ error_message    │                     │
    │ started_at       │                     │
    │ completed_at     │                     │
    │ created_at       │                     │
    └────────┬─────────┘                     │
             │                               │
  ┌──────────┼──────────┬──────────────┐    │
  │          │          │              │    │
  ▼          ▼          ▼              ▼    ▼
┌──────┐  ┌──────┐  ┌──────┐  ┌───────────┐
│Code  │  │Gen   │  │Compl │  │Quality    │
│Issue │  │Test  │  │Check │  │Score (1:1)│
├──────┤  ├──────┤  ├──────┤  ├───────────┤
│id(PK)│  │id(PK)│  │id(PK)│  │id(PK)     │
│scan  │  │scan  │  │scan  │  │scan_id(FK)│
│_id   │  │_id   │  │_id   │  │overall    │
│(FK)  │  │(FK)  │  │(FK)  │  │_score     │
│file  │  │src   │  │stnd  │  │maintain   │
│_path │  │_file │  │ard   │  │ability    │
│line  │  │test  │  │rule  │  │reliab     │
│_num  │  │_file │  │staus │  │ility      │
│sever │  │frame │  │detai │  │security   │
│ity   │  │work  │  │ls    │  │coverage   │
│categ │  │test  │  └──────┘  │grade      │
│ory   │  │_code │             └───────────┘
│title │  │desc  │
│desc  │  └──────┘
│sugg  │
└──────┘

CASCADE delete on Scan:
- All CodeIssues deleted
- All GeneratedTests deleted
- All ComplianceChecks deleted
- QualityScore deleted

Indexes:
- scans(repository_id)
- scans(status)
- code_issues(scan_id)
- code_issues(severity)
- generated_tests(scan_id)
- compliance_checks(scan_id)
```

---

## 8. API Endpoint Topology

```
┌─────────────────────────────────────────────────────────┐
│                    REST API (v1)                        │
│                /api/v1/                                 │
└──────────────────┬──────────────────────────────────────┘
                   │
        ┌──────────┼──────────┬──────────┬───────────────┐
        │          │          │          │               │
        ▼          ▼          ▼          ▼               ▼
    ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐    ┌──────────┐
    │ /auth  │ │/repos  │ │/scans  │ │/dash   │    │/swagger- │
    │        │ │itories │ │        │ │board   │    │ui.html   │
    └───┬────┘ └───┬────┘ └───┬────┘ └───┬────┘    └──────────┘
        │          │          │          │
        │          ▼          ▼          ▼
    ┌───┴─────────────────────────────────────────────────┐
    │                                                     │
    ├─ POST   /register                                  │
    ├─ POST   /login                                     │
    ├─ POST   /refresh                                   │
    │                                                     │
    ├─ POST   /repositories              [AUTHENTICATED] │
    ├─ GET    /repositories               [AUTHENTICATED] │
    ├─ GET    /repositories/{id}          [AUTHENTICATED] │
    ├─ PUT    /repositories/{id}          [AUTHENTICATED] │
    ├─ DELETE /repositories/{id}          [AUTHENTICATED] │
    │                                                     │
    ├─ POST   /repositories/{id}/scan     [AUTHENTICATED] │
    ├─ GET    /scans/{id}                 [AUTHENTICATED] │
    ├─ GET    /repositories/{id}/scans    [AUTHENTICATED] │
    │                                                     │
    ├─ GET    /dashboard/summary          [AUTHENTICATED] │
    ├─ GET    /dashboard/repositories...  [AUTHENTICATED] │
    ├─ GET    /dashboard/issues/dist...   [AUTHENTICATED] │
    ├─ GET    /dashboard/compliance...    [AUTHENTICATED] │
    │                                                     │
    ├─ GET    /v3/api-docs                [PUBLIC]       │
    ├─ GET    /swagger-ui.html            [PUBLIC]       │
    ├─ GET    /actuator/health            [PUBLIC]       │
    └─────────────────────────────────────────────────────┘
```

---

## 9. Exception Handling Flow

```
┌──────────────────────────────────────────────┐
│         HTTP Request Processing              │
└─────────────────────────┬────────────────────┘
                          │
                ┌─────────┴─────────┐
                │                   │
                ▼                   ▼
        ┌──────────────┐    ┌──────────────┐
        │ Success      │    │ Exception    │
        │ Response     │    │ Thrown       │
        │ (200, 201)   │    └──────┬───────┘
        └──────────────┘           │
                                   ▼
                    ┌──────────────────────────────┐
                    │ GlobalExceptionHandler       │
                    │ (@RestControllerAdvice)      │
                    │                              │
                    │ Catches:                     │
                    ├─ ResourceNotFound (404)     │
                    ├─ DuplicateResource (409)    │
                    ├─ ExternalService (503)      │
                    ├─ Validation (400)           │
                    ├─ Security (401, 403)        │
                    └─ All Others (500)           │
                           │                       │
                           ▼                       │
                    ┌──────────────────┐           │
                    │ Construct ApiError│          │
                    │ {                │          │
                    │   timestamp,     │          │
                    │   status,        │          │
                    │   error,         │          │
                    │   message,       │          │
                    │   path           │          │
                    │ }                │          │
                    └────────┬─────────┘          │
                             │                   │
                             ▼                   │
                    ┌──────────────────┐          │
                    │ Return JSON      │          │
                    │ With Status Code │          │
                    │ (400, 401, ...)  │          │
                    └──────────────────┘          │
                                                  │
                    ┌─────────────────────────────┘
                    │
                    ▼
         ┌────────────────────────┐
         │ Client Receives Error  │
         │ with clear message     │
         └────────────────────────┘
```

---

## Summary

This architecture provides:

✅ **Clear Separation of Concerns** - Each layer has specific responsibility  
✅ **Async Processing** - Non-blocking scan execution  
✅ **Scalability** - Stateless design allows horizontal scaling  
✅ **Security** - JWT + RBAC + input validation  
✅ **Extensibility** - Easy to add new analysis providers  
✅ **Reliability** - Comprehensive error handling  
✅ **Performance** - Indexed database queries + connection pooling  

The only external dependency is the **AI API Key** (OpenAI or Azure OpenAI) to activate bug detection, test generation, and compliance assessment features.

---

*Architecture Diagrams Generated: September 28, 2026*
