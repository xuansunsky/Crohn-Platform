ALTER TABLE `crohn_paper_boat`
  ADD COLUMN `user_id` BIGINT NULL COMMENT '匿名放飞者ID，仅用于限额与避开自己捞自己' AFTER `id`,
  ADD KEY `idx_paper_boat_user_day` (`user_id`, `created_at`);

CREATE TABLE IF NOT EXISTS `crohn_paper_boat_scoop` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `boat_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_scoop_user_day` (`user_id`, `created_at`),
  KEY `idx_scoop_boat` (`boat_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='纸船每日打捞记录';

CREATE TABLE IF NOT EXISTS `crohn_paper_boat_response` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `boat_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `response_type` VARCHAR(20) NOT NULL COMMENT 'reply / gift',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '私密回声',
  `gift_type` VARCHAR(40) DEFAULT NULL COMMENT '礼物类型',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_boat_user_type` (`boat_id`, `user_id`, `response_type`),
  KEY `idx_response_boat` (`boat_id`),
  KEY `idx_response_user_day` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='纸船私密回应与礼物';
