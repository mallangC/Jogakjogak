ALTER TABLE member
ADD COLUMN is_onboarded boolean
NOT NULL
DEFAULT FALSE;

UPDATE member
SET is_onboarded = true;