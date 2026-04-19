INSERT IGNORE INTO resources (name, type, capacity, location, availability, status)
VALUES
  ('Lecture Hall A', 'ROOM', 100, 'Main Building', 'AVAILABLE', 'ACTIVE'),
  ('Meeting Room 1', 'ROOM', 10, 'Admin Block', 'AVAILABLE', 'ACTIVE'),
  ('Computer Lab 1', 'LAB', 40, 'Engineering Faculty', 'AVAILABLE', 'ACTIVE'),
  ('Projector', 'EQUIPMENT', 1, 'AV Store', 'AVAILABLE', 'ACTIVE'),
  ('Camera', 'EQUIPMENT', 1, 'Media Unit', 'AVAILABLE', 'ACTIVE');

INSERT IGNORE INTO users (first_name, last_name, email, role, active)
VALUES
  ('Demo', 'User', 'demo.user@smartcampus.local', 'USER', true),
  ('Demo', 'Admin', 'demo.admin@smartcampus.local', 'ADMIN', true);
