CREATE TABLE member
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(50) NOT NULL UNIQUE,
    email         VARCHAR(50) NOT NULL UNIQUE,
    password      VARCHAR(50) NOT NULL,
    name          VARCHAR(50) NOT NULL,
    nickname      VARCHAR(50) NOT NULL,
    phone_number  VARCHAR(11) NOT NULL,
    role          VARCHAR(50) NOT NULL,
    registered_at DATETIME    NOT NULL,
    last_login_at DATETIME    NOT NULL
);

CREATE TABLE refresh_token
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    username   VARCHAR(50)  NOT NULL,
    token      VARCHAR(255) NOT NULL,
    expiration DATETIME     NOT NULL
);

CREATE TABLE oauth2_info
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id   BIGINT      NOT NULL,
    provider    VARCHAR(10) NOT NULL,
    provider_id VARCHAR(50) NOT NULL,
    FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE TABLE resume
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id  BIGINT        NOT NULL,
    title      VARCHAR(30)   NOT NULL,
    content    VARCHAR(5000) NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE TABLE job_description
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id    BIGINT        NOT NULL,
    title        VARCHAR(30)   NOT NULL,
    company_name VARCHAR(30)   NOT NULL,
    job          VARCHAR(30)   NOT NULL,
    content      VARCHAR(4000) NOT NULL,
    jd_url       VARCHAR(1000) NOT NULL,
    `memo`       VARCHAR(1000) NOT NULL,
    is_bookmark  BIT           NOT NULL,
    is_alarm_on  BIT           NOT NULL,
    apply_at     DATETIME,
    ended_at     DATETIME,
    created_at   DATETIME,
    updated_at   DATETIME,
    FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE TABLE to_do_list
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    jd_id      BIGINT        NOT NULL,
    category   VARCHAR(40)   NOT NULL,
    title      VARCHAR(15)   NOT NULL,
    content    VARCHAR(1000) NOT NULL,
    `memo`     VARCHAR(255)  NOT NULL,
    is_done    BIT           NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (jd_id) REFERENCES job_description (id)
);

CREATE TABLE notification
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL ,
    created_at DATETIME,
    FOREIGN KEY (member_id) REFERENCES member(id)
);