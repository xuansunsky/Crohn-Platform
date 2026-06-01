-- ============================================================
-- 战友状态墙 / 无声关心 / 微信扫码转款 建表脚本
-- 隐私红线：绝不存储银行卡号、身份证等隐私，只存用户自愿上传的收款码图片链接
-- ============================================================

-- 1. 每人当前状态（user_id 主键，保证一人一条，配合 ON DUPLICATE KEY 做 upsert）
CREATE TABLE IF NOT EXISTS `crohn_user_status` (
  `user_id`     BIGINT      NOT NULL COMMENT '用户ID',
  `emoji`       VARCHAR(16)  DEFAULT NULL COMMENT '状态表情',
  `text`        VARCHAR(64)  DEFAULT NULL COMMENT '状态文案',
  `description` VARCHAR(128) DEFAULT NULL COMMENT '状态描述',
  `accent`      VARCHAR(16)  DEFAULT 'slate' COMMENT '主题色键',
  `zone`        VARCHAR(8)   DEFAULT 'green' COMMENT '档位 green/yellow/red',
  `updated_at`  DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='战友状态';

-- 2. 无声关心记录（用于统计每人收到多少份关心）
CREATE TABLE IF NOT EXISTS `crohn_status_reaction` (
  `id`             BIGINT      NOT NULL AUTO_INCREMENT,
  `target_user_id` BIGINT      NOT NULL COMMENT '被关心的人',
  `sender_id`      BIGINT      NOT NULL COMMENT '送关心的人',
  `reaction_type`  VARCHAR(24)  DEFAULT NULL COMMENT '关心类型 hug/punch/...',
  `created_at`     DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_user_id`),
  KEY `idx_sender_day` (`sender_id`, `target_user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='状态关心回应';

-- 3. 微信收款码（只存图片链接，user_id 主键，一人一码）
CREATE TABLE IF NOT EXISTS `crohn_user_paycode` (
  `user_id`                 BIGINT       NOT NULL COMMENT '用户ID',
  `wechat_receive_code_url` VARCHAR(512) DEFAULT NULL COMMENT '微信收款码图片链接',
  `updated_at`              DATETIME     DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信收款码（仅图片链接，无任何隐私）';

-- 4. IBD 战友身份认证（user_id 主键，一人一条；重新提交回到 PENDING）
CREATE TABLE IF NOT EXISTS `crohn_user_verification` (
  `user_id`         BIGINT       NOT NULL COMMENT '用户ID',
  `proof_image_url` VARCHAR(512) DEFAULT NULL COMMENT '证明材料图片链接（确诊单/肠镜/药单，可涂黑隐私）',
  `status`          VARCHAR(16)  DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  `created_at`      DATETIME     DEFAULT NULL COMMENT '提交时间',
  `reviewed_at`     DATETIME     DEFAULT NULL COMMENT '审核时间',
  PRIMARY KEY (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IBD 战友身份认证';

-- 5. 经验金库封面图列（若 experience_posts 已存在则补列）
ALTER TABLE `experience_posts` ADD COLUMN `cover_image` VARCHAR(512) DEFAULT NULL COMMENT '故事配图链接';
