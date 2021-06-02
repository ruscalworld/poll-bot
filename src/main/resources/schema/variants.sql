CREATE TABLE `poll_variants`
(
    `id`          INT PRIMARY KEY AUTOINCREMENT,
    `poll_id`     INT NOT NULL,
    `name`        VARCHAR(50),
    `sign`        VARCHAR(50),
    `title`       VARCHAR(100),
    `description` TEXT
);