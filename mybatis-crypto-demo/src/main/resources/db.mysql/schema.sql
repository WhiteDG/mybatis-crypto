CREATE
DATABASE `mybatis_crypto` CHARACTER
SET utf8mb4 COLLATE utf8mb4_general_ci;

DROP TABLE IF EXISTS `t_user`;

CREATE TABLE `t_user`
(
    `id`           INT          NOT NULL AUTO_INCREMENT,
    `name`         VARCHAR(255) NULL     DEFAULT NULL,
    `email`        VARCHAR(255) NULL     DEFAULT NULL,
    `password`     VARCHAR(255) NULL     DEFAULT NULL,
    `id_card_no`   VARCHAR(255) NULL     DEFAULT NULL,
    `created_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE `t_address`
(
    `id`      INT          NOT NULL AUTO_INCREMENT,
    `user_id` INT          NOT NULL,
    `address` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`)
);
