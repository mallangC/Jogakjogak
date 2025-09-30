CREATE TABLE event
(
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT      NOT NULL,
    code      VARCHAR(10) NOT NULL,
    type      VARCHAR(15) NOT NULL,
    is_first  BIT         NOT NULL DEFAULT TRUE,
    FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE

);