CREATE TABLE `poll_votes`
(
    `id`          INT PRIMARY KEY AUTOINCREMENT,
    `variant_id`  INT         NOT NULL,
    `member_id`   VARCHAR(50) NOT NULL,
    `reaction_id` VARCHAR(50) DEFAULT NULL,
    `created_at`  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
);