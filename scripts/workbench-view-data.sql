-- Run in MySQL Workbench after the Spring Boot app has started at least once with the "mysql" profile.
-- Connection must match application-mysql.properties: host localhost:3306, database smartcampus.

USE smartcampus;

SHOW TABLES;

-- Sample users (seeded when app.seed.sample-users=true — default SamplePass12 for LOCAL logins)
SELECT id, email, first_name, last_name, role, active, created_at
FROM users
ORDER BY id
LIMIT 20;

-- Tickets created from the web app
SELECT id, category, status, priority, contact_name, contact_email, created_at
FROM tickets
ORDER BY id DESC
LIMIT 20;
