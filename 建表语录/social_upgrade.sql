-- ============================================================
-- SocialTab 全功能改造 · 建表脚本
-- 全部使用 CREATE TABLE IF NOT EXISTS，不影响现有数据。
-- 现有 users / friendships / moments / moment_likes / messages 不改结构。
-- 直接整段执行即可。
-- ============================================================

-- ----------------------------
-- 1. 动态评论
-- ----------------------------
CREATE TABLE IF NOT EXISTS `social_moment_comments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `moment_id` BIGINT NOT NULL COMMENT '所属动态ID',
  `user_id` BIGINT NOT NULL COMMENT '评论者ID',
  `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_moment` (`moment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态评论表';

-- ----------------------------
-- 2. 小队 / 圈子（避开 MySQL 保留字 groups，命名 chat_groups）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `chat_groups` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '小队名称',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '小队头像',
  `notice` VARCHAR(500) DEFAULT NULL COMMENT '小队公告',
  `owner_id` BIGINT NOT NULL COMMENT '队长（创建者）ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_owner` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小队/圈子表';

-- ----------------------------
-- 3. 小队成员
-- ----------------------------
CREATE TABLE IF NOT EXISTS `group_members` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `group_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `role` VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT 'OWNER / MEMBER',
  `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_id`, `user_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小队成员表';

-- ----------------------------
-- 4. 群聊消息
-- ----------------------------
CREATE TABLE IF NOT EXISTS `group_messages` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `group_id` BIGINT NOT NULL,
  `sender_id` BIGINT NOT NULL,
  `content` TEXT,
  `type` VARCHAR(20) NOT NULL DEFAULT 'text' COMMENT 'text / image',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_group` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群聊消息表';

-- ----------------------------
-- 5. 小队任务（共同打卡看板）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `squad_tasks` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `group_id` BIGINT NOT NULL,
  `label` VARCHAR(200) NOT NULL COMMENT '任务内容',
  `owner_id` BIGINT DEFAULT NULL COMMENT '负责人ID（可空=全员）',
  `assignee_id` BIGINT DEFAULT NULL COMMENT '认领人/被指派人ID（可空=全员）',
  `priority` VARCHAR(20) NOT NULL DEFAULT '日常' COMMENT '日常/必做/提醒/安排',
  `done` TINYINT NOT NULL DEFAULT 0 COMMENT '0未完成 1已完成',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_group` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小队任务表';

-- 升级老库：给已存在的 squad_tasks 补 assignee_id 字段（新库忽略此句）
-- ALTER TABLE `squad_tasks` ADD COLUMN `assignee_id` BIGINT DEFAULT NULL COMMENT '认领人/被指派人ID' AFTER `owner_id`;

-- ----------------------------
-- 6. 队友动态流
-- ----------------------------
CREATE TABLE IF NOT EXISTS `squad_activities` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `group_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `action` VARCHAR(300) NOT NULL COMMENT '动作描述，如：打卡了低渣早餐',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_group` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队友动态流表';
_group` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队友动态流表';

_group` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队友动态流表';
;

_group` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队友动态流表';
';
;

_group` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队友动态流表';
�';
';
;

_group` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队友动态流表';
';
�';
';
;

_group` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='队友动态流表';
