ALTER TABLE refresh_token ADD COLUMN user_id BIGINT;
ALTER TABLE refresh_token ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES member(id);