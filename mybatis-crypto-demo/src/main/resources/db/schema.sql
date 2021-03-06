DROP TABLE IF EXISTS `t_user`;

CREATE TABLE `t_user`
(
    `id`         INT          NOT NULL AUTO_INCREMENT,
    `name`       VARCHAR(255) NULL DEFAULT NULL,
    `email`      VARCHAR(255) NULL DEFAULT NULL,
    `password`   VARCHAR(255) NULL DEFAULT NULL,
    `id_card_no` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
);
