-- ========================================
-- DEMO DATA FOR CODE QUALITY PLATFORM
-- ========================================

-- Insert Demo Users (password: 'password123' hashed)
INSERT INTO users (username, email, password_hash, role, enabled, created_at) VALUES
('john_dev', 'john@example.com', '$2a$10$slYQmyNdGzin7olVeolvFONFsxChF.V5PlHkGBarca7MHpjHN7LO', 'USER', TRUE, NOW()),
('jane_admin', 'jane@example.com', '$2a$10$slYQmyNdGzin7olVeolvFONFsxChF.V5PlHkGBarca7MHpjHN7LO', 'ADMIN', TRUE, NOW()),
('alice_lead', 'alice@example.com', '$2a$10$slYQmyNdGzin7olVeolvFONFsxChF.V5PlHkGBarca7MHpjHN7LO', 'USER', TRUE, NOW());

-- Insert Demo Repositories
INSERT INTO code_repositories (owner, name, url, default_branch, description, active, registered_by, created_at) VALUES
('anthropics', 'claude-api', 'https://github.com/anthropics/claude-api', 'main', 'SDK for Claude API integration', TRUE, 1, NOW()),
('opf', 'openproject', 'https://github.com/opf/openproject', 'dev', 'Project management platform', TRUE, 1, NOW()),
('vercel', 'commerce', 'https://github.com/vercel/commerce', 'main', 'Next.js e-commerce template', TRUE, 2, NOW()),
('spring-projects', 'spring-security-samples', 'https://github.com/spring-projects/spring-security-samples', 'main', 'Spring Security examples', TRUE, 3, NOW());

-- Insert Demo Scans with COMPLETED status
INSERT INTO scans (repository_id, triggered_by, status, branch, commit_sha, files_scanned, started_at, completed_at, created_at) VALUES
(1, 1, 'COMPLETED', 'main', 'abc123def456', 12, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 2 DAY),
(2, 1, 'COMPLETED', 'dev', 'xyz789uvw456', 18, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 12 HOUR, NOW() - INTERVAL 1 DAY),
(3, 2, 'COMPLETED', 'main', 'pqr321lmn654', 8, NOW() - INTERVAL 6 HOUR, NOW() - INTERVAL 5 HOUR, NOW() - INTERVAL 6 HOUR),
(4, 3, 'RUNNING', 'main', NULL, 0, NOW() - INTERVAL 5 MINUTE, NULL, NOW() - INTERVAL 5 MINUTE),
(1, 3, 'COMPLETED', 'main', 'def789ghi123', 12, NOW() - INTERVAL 12 HOUR, NOW() - INTERVAL 11 HOUR, NOW() - INTERVAL 12 HOUR);

-- Insert Quality Scores
INSERT INTO quality_scores (scan_id, overall_score, maintainability, reliability, security, coverage_estimate, grade) VALUES
(1, 82.5, 80.0, 85.0, 78.0, 75.0, 'A'),
(2, 76.3, 74.0, 78.0, 72.0, 68.0, 'B'),
(3, 88.7, 90.0, 87.0, 89.0, 92.0, 'A'),
(5, 71.2, 68.0, 72.0, 65.0, 70.0, 'C');

-- Insert Code Issues
INSERT INTO code_issues (scan_id, file_path, line_number, severity, category, title, description, suggestion) VALUES
-- Scan 1 Issues
(1, 'src/main/java/ApiClient.java', 45, 'HIGH', 'SECURITY', 'SQL Injection Vulnerability', 'User input is directly concatenated into SQL query', 'Use prepared statements with parameterized queries'),
(1, 'src/main/java/ApiClient.java', 67, 'MEDIUM', 'PERFORMANCE', 'N+1 Query Problem', 'Loading data in a loop causes excessive database queries', 'Use JOIN or batch loading instead of loops'),
(1, 'src/main/java/Utils.java', 23, 'LOW', 'CODE_QUALITY', 'Unused Import', 'Import statement is not used in this file', 'Remove the unused import: import java.util.Optional;'),
(1, 'src/main/java/Controller.java', 89, 'MEDIUM', 'MAINTAINABILITY', 'Long Method', 'Method exceeds 100 lines of code', 'Break this method into smaller, focused methods'),
(1, 'src/main/java/Service.java', 12, 'HIGH', 'RELIABILITY', 'Null Pointer Risk', 'Variable may be null but no null check exists', 'Add null check: if (response != null) { ... }'),

-- Scan 2 Issues
(2, 'app/models/User.ts', 34, 'MEDIUM', 'SECURITY', 'Weak Password Validation', 'Password validation only checks length', 'Require uppercase, lowercase, numbers, and special characters'),
(2, 'app/services/auth.ts', 56, 'HIGH', 'SECURITY', 'Hardcoded API Key', 'API key is hardcoded in source code', 'Move to environment variables and use .env file'),
(2, 'app/components/Form.tsx', 102, 'LOW', 'CODE_QUALITY', 'Missing PropTypes', 'React component missing prop validation', 'Add PropTypes or use TypeScript interface'),
(2, 'app/utils/helpers.ts', 15, 'MEDIUM', 'PERFORMANCE', 'Inefficient Array Filter', 'Using filter followed by map could be combined', 'Combine operations: items.reduce((acc, item) => { ... })'),

-- Scan 3 Issues
(3, 'pages/checkout.js', 78, 'HIGH', 'SECURITY', 'XSS Vulnerability', 'User input rendered without sanitization', 'Use DOMPurify.sanitize() before rendering user content'),
(3, 'components/Product.jsx', 45, 'LOW', 'CODE_QUALITY', 'Console.log in Production', 'Debug console.log statement left in code', 'Remove or use logger.debug() for production');

-- Insert Generated Tests
INSERT INTO generated_tests (scan_id, source_file_path, test_file_path, framework, test_code, description) VALUES
(1, 'src/main/java/ApiClient.java', 'src/test/java/ApiClientTest.java', 'JUnit5',
'@Test
public void testApiClientConnectionSuccess() {
    ApiClient client = new ApiClient("https://api.example.com");
    Response response = client.connect();
    assertEquals(200, response.getStatusCode());
    assertNotNull(response.getBody());
}', 'Test successful API connection'),

(1, 'src/main/java/Service.java', 'src/test/java/ServiceTest.java', 'JUnit5',
'@Test
public void testServiceWithNullInput() {
    Service service = new Service();
    assertThrows(NullPointerException.class, () -> {
        service.process(null);
    });
}', 'Test null input handling'),

(2, 'app/services/auth.ts', 'app/services/auth.test.ts', 'Jest',
'describe("Auth Service", () => {
  it("should validate strong password", () => {
    const result = validatePassword("SecurePass123!");
    expect(result).toBe(true);
  });

  it("should reject weak password", () => {
    const result = validatePassword("weak");
    expect(result).toBe(false);
  });
});', 'Test password validation'),

(3, 'pages/checkout.js', 'pages/__tests__/checkout.test.js', 'Jest',
'describe("Checkout Page", () => {
  it("should sanitize user input", () => {
    const userInput = "<script>alert(\"XSS\")</script>";
    const sanitized = sanitizeInput(userInput);
    expect(sanitized).not.toContain("<script>");
  });
});', 'Test XSS protection');

-- Insert Compliance Checks
INSERT INTO compliance_checks (scan_id, standard, rule, status, details) VALUES
-- Scan 1 Compliance
(1, 'OWASP', 'A06:2021 - Vulnerable Components', 'FAILED', 'Dependency library has known vulnerability CVE-2024-1234'),
(1, 'GDPR', 'Data Protection Impact Assessment', 'PASSED', 'Proper data anonymization implemented'),
(1, 'SOC2', 'Access Control', 'PASSED', 'Role-based access control properly configured'),
(1, 'CWE', 'CWE-89: Improper Neutralization of Special Elements', 'FAILED', 'SQL Injection detected in ApiClient.java:45'),

-- Scan 2 Compliance
(2, 'OWASP', 'A02:2021 - Cryptographic Failures', 'FAILED', 'Hardcoded API key found'),
(2, 'GDPR', 'User Consent Management', 'PASSED', 'Cookie consent banner implemented'),
(2, 'PCI-DSS', 'Secure Development Practices', 'WARNING', 'No encryption for sensitive data in transit'),

-- Scan 3 Compliance
(3, 'OWASP', 'A07:2021 - Cross-Site Scripting', 'FAILED', 'XSS vulnerability in checkout.js'),
(3, 'GDPR', 'Data Processing Agreement', 'PASSED', 'Vendor compliance verified'),
(3, 'SOC2', 'Availability', 'PASSED', 'System availability meets SLA requirements'),

-- Scan 5 Compliance
(5, 'OWASP', 'A03:2021 - Injection', 'WARNING', 'Potential injection risk in query builder');
