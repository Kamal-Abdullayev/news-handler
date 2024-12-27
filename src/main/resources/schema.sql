CREATE TABLE IF NOT EXISTS source (
                                      source_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      id VARCHAR(80),
                                      name VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS news (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    author VARCHAR(100),
                                    title VARCHAR(200),
                                    description TEXT,
                                    url VARCHAR(255),
                                    url_to_image VARCHAR(255),
                                    local_url_to_image VARCHAR(255),
                                    published_at TIMESTAMP,
                                    content TEXT,
                                    source_id BIGINT,
                                    FOREIGN KEY (source_id) REFERENCES source(source_id) ON DELETE CASCADE
);

CREATE INDEX idx_news_title ON news(title);



