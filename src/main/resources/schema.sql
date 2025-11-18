-- Drop tables if they exist to avoid conflicts
DROP TABLE IF EXISTS users CASCADE;

-- create table for PostgreSQL
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

-- index the email for faster search
CREATE INDEX idx_users_email ON users(email);
