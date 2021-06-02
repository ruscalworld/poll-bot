CREATE TABLE `polls`
(
    `id`                    INT PRIMARY KEY AUTOINCREMENT,
    `name`                  VARCHAR(50),
    `title`                 VARCHAR(200)     DEFAULT NULL,
    `description`           TEXT             DEFAULT NULL,
    `ends_at`               TIMESTAMP        DEFAULT NULL,
    `message_id`            VARCHAR(100)     DEFAULT NULL,
    `channel_id`            VARCHAR(100)     DEFAULT NULL,
    `guild_id`              VARCHAR(100)     DEFAULT NULL,
    `allow_revote`          TINYINT NOT NULL DEFAULT 0,
    `allow_multiple_choice` TINYINT NOT NULL DEFAULT 0,
    `is_anonymous`          TINYINT NOT NULL DEFAULT 0
);