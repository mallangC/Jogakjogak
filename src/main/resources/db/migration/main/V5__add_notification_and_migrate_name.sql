ALTER TABLE member
ADD COLUMN notification_on_off boolean
NOT NULL
DEFAULT FALSE;

UPDATE member
SET name = nickname
WHERE name IS NULL;

UPDATE member
SET nickname = NULL;