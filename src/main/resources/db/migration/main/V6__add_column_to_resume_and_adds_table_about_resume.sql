ALTER TABLE resume
    ADD COLUMN is_newcomer BIT NOT NULL DEFAULT TRUE;

ALTER TABLE resume MODIFY COLUMN title VARCHAR(30) NULL;

CREATE TABLE career
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    resume_id        BIGINT        NOT NULL,
    joined_at        DATE          NOT NULL,
    quit_at          DATE          NOT NULL,
    is_working       BIT           NOT NULL,
    company_name     VARCHAR(100)  NOT NULL,
    work_performance VARCHAR(2000) NOT NULL,
    FOREIGN KEY (resume_id) REFERENCES resume (id) ON DELETE CASCADE
);

CREATE TABLE education
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    resume_id   BIGINT       NOT NULL,
    level       VARCHAR(20)  NOT NULL,
    major_field VARCHAR(225) NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    FOREIGN KEY (resume_id) REFERENCES resume (id) ON DELETE CASCADE
);

CREATE TABLE skill
(
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    resume_id BIGINT       NOT NULL,
    content   VARCHAR(255) NOT NULL,
    FOREIGN KEY (resume_id) REFERENCES resume (id) ON DELETE CASCADE
);

CREATE TABLE skill_word
(
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    content VARCHAR(50) NOT NULL,
    FULLTEXT(content)
);