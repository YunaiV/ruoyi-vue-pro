-- ============================================================
-- V002：初始化活动相关表
-- 来源：docs/5-数据库设计.md §3.1 + §3.2
-- 注：SPATIAL KEY（地理位置空间索引）暂未加，因 MySQL 8 要求字段是 POINT/GEOMETRY，
-- 而 venue_lat/venue_lng 当前是 DECIMAL；Sprint 1 第 4 周做"附近发现"时再改造。
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_activity` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `short_code` VARCHAR(8) NOT NULL COMMENT '活动短码（6位，分享链接用）',
  `title` VARCHAR(128) NOT NULL,
  `creator_id` BIGINT UNSIGNED NOT NULL COMMENT '召集人用户 ID',
  `club_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '可选关联球队',
  `venue_name` VARCHAR(128) NOT NULL COMMENT '场馆名',
  `venue_address` VARCHAR(255) DEFAULT NULL,
  `venue_lat` DECIMAL(10,6) DEFAULT NULL,
  `venue_lng` DECIMAL(10,6) DEFAULT NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NOT NULL,
  `court_count` INT NOT NULL DEFAULT 1 COMMENT '场地数',
  `planned_count` INT NOT NULL DEFAULT 6 COMMENT '计划人数',
  `current_count` INT NOT NULL DEFAULT 0 COMMENT '当前已报名',
  `mode` VARCHAR(16) NOT NULL DEFAULT 'MIXED' COMMENT 'MEN_DOUBLES/WOMEN_DOUBLES/MIXED/FREE',
  `rotation` VARCHAR(16) NOT NULL DEFAULT 'ROTATE' COMMENT 'FIXED/ROTATE',
  `match_duration` INT NOT NULL DEFAULT 15 COMMENT '单场分钟',
  `aa_amount` DECIMAL(8,2) DEFAULT NULL COMMENT 'AA 金额',
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_public` TINYINT NOT NULL DEFAULT 0 COMMENT 'v1.1 是否对外公开',
  `visible_radius_km` INT NOT NULL DEFAULT 3 COMMENT 'v1.1 可见半径（1/3/5/10）',
  `accept_strangers` TINYINT NOT NULL DEFAULT 1 COMMENT 'v1.1 是否接受陌生人申请',
  `need_review` TINYINT NOT NULL DEFAULT 1 COMMENT 'v1.1 申请是否需审核',
  `limit_levels` VARCHAR(128) DEFAULT NULL COMMENT 'v1.1 段位限制 JSON 数组，如 ["C1","C2","B1"]',
  `limit_gender` TINYINT DEFAULT 0 COMMENT 'v1.1 性别限制 0不限 1只缺男 2只缺女',
  `limit_strangers_count` INT DEFAULT 1 COMMENT 'v1.1 通过陌生申请最多多少人',
  `accepted_strangers_count` INT DEFAULT 0 COMMENT 'v1.1 已通过陌生申请人数',
  `status` VARCHAR(16) NOT NULL DEFAULT 'RECRUITING' COMMENT 'RECRUITING/MATCHED/PLAYING/FINISHED/CANCELLED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_short_code` (`short_code`),
  KEY `idx_creator_status` (`creator_id`, `status`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_club` (`club_id`),
  KEY `idx_public_status` (`is_public`, `status`, `start_time`) COMMENT 'v1.1 发现页查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动';


CREATE TABLE IF NOT EXISTS `t_activity_member` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `activity_id` BIGINT UNSIGNED NOT NULL,
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '注册用户ID',
  `placeholder_name` VARCHAR(32) DEFAULT NULL COMMENT '临时占位人名（未注册）',
  `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '0未知 1男 2女',
  `level_at_join` VARCHAR(8) DEFAULT NULL COMMENT '报名时的段位（用于排对阵）',
  `join_source` VARCHAR(16) NOT NULL DEFAULT 'INVITE' COMMENT 'INVITE/MANUAL/DISCOVERY',
  `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` VARCHAR(16) NOT NULL DEFAULT 'JOINED' COMMENT 'JOINED/WAITING/QUIT/REMOVED/NO_SHOW',
  `note` VARCHAR(255) DEFAULT NULL COMMENT '备注，如 30 分钟后到',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
  KEY `idx_activity` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动参与者';
