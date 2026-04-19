INSERT INTO resources (name, type, capacity, location, status, created_at)
VALUES
  ('Lecture Hall A', 'ROOM', 100, 'Main Building', 'ACTIVE', NOW()),
  ('Meeting Room 1', 'ROOM', 10, 'Admin Block', 'ACTIVE', NOW()),
  ('Computer Lab 1', 'LAB', 40, 'Engineering Faculty', 'ACTIVE', NOW()),
  ('Projector', 'EQUIPMENT', 1, 'AV Store', 'ACTIVE', NOW()),
  ('Camera', 'EQUIPMENT', 1, 'Media Unit', 'ACTIVE', NOW());

INSERT IGNORE INTO users (name, email, role, google_id, profile_picture, created_at)
VALUES
  ('Demo User', 'demo.user@smartcampus.local', 'USER', NULL, NULL, NOW()),
  ('Demo Admin', 'demo.admin@smartcampus.local', 'ADMIN', NULL, NULL, NOW());

