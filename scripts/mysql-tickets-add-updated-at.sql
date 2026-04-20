-- If GET /api/tickets returns 500 and the API log shows Unknown column 'updated_at' in 'field list',
-- run this once against your smartcampus database (adjust schema name if needed).
-- If the column already exists, MySQL will error — that is safe to ignore.

ALTER TABLE tickets ADD COLUMN updated_at DATETIME(6) NULL;
