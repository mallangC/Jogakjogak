ALTER TABLE job_description
ADD COLUMN is_created_with_resume boolean
NOT NULL
DEFAULT FALSE;

UPDATE job_description
SET is_created_with_resume = true;