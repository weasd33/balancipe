-- Flyway 도입 이전, Hibernate ddl-auto가 생성해 오던 users 테이블을 그대로 베이스라인으로 등록한다.
-- User.java(com.beokay.balancipe.user.domain.User) 매핑과 1:1 대응.
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(30) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    birth_year INTEGER NOT NULL,
    profile_image_url VARCHAR(500),
    role VARCHAR(10) NOT NULL,
    status VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_nickname UNIQUE (nickname)
);
