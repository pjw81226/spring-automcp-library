-- Sample schema for demonstrating the DatabaseSchemaTool

CREATE TABLE users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    username   VARCHAR(50)  NOT NULL,
    role       VARCHAR(20)  DEFAULT 'USER',
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE articles (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    content      TEXT,
    author_id    BIGINT NOT NULL,
    published    BOOLEAN DEFAULT FALSE,
    published_at TIMESTAMP,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id)
);

CREATE TABLE comments (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    body       VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES articles(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Sample data
INSERT INTO users (email, username, role) VALUES
('admin@example.com', 'admin', 'ADMIN'),
('user1@example.com', 'alice', 'USER'),
('user2@example.com', 'bob', 'USER');

INSERT INTO articles (title, content, author_id, published, published_at) VALUES
('Getting Started with MCP', 'Model Context Protocol is...', 1, TRUE, CURRENT_TIMESTAMP),
('Spring Boot Best Practices', 'When building Spring Boot apps...', 2, TRUE, CURRENT_TIMESTAMP);
