ALTER TABLE member
ADD COLUMN is_notification_enabled boolean
NOT NULL
DEFAULT FALSE;

UPDATE member
SET name = nickname
WHERE name IS NULL;

UPDATE member
SET nickname = NULL;