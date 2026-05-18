-- ============================================================
-- V001：初始化 C 端用户表 t_user
-- 来源：docs/5-数据库设计.md §2.1
-- 与 system_users（运营后台用户）完全分离
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `openid` VARCHAR(64) NOT NULL COMMENT '微信 openid',
  `unionid` VARCHAR(64) DEFAULT NULL COMMENT '微信 unionid',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(512) DEFAULT NULL COMMENT '头像 URL',
  `gender` TINYINT DEFAULT 0 COMMENT '0未知 1男 2女',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号（实名时获取）',
  `is_real_name_verified` TINYINT DEFAULT 0 COMMENT 'v1.1 是否实名（绑定phone）',
  `current_level` VARCHAR(8) DEFAULT NULL COMMENT '当前段位 D1/D2/.../S',
  `current_score` INT DEFAULT 0 COMMENT '当前评分（0-100）',
  `total_games` INT DEFAULT 0 COMMENT '总比赛场次',
  `wins` INT DEFAULT 0 COMMENT '总胜场',
  `total_organized` INT DEFAULT 0 COMMENT 'v1.1 已组织活动数（召集人成长等级）',
  `credit_score` INT DEFAULT 100 COMMENT 'v1.1 信用分（默认100，范围0-150）',
  `last_known_lng` DECIMAL(10,6) DEFAULT NULL COMMENT 'v1.1 最近位置经度（脱敏到街道）',
  `last_known_lat` DECIMAL(10,6) DEFAULT NULL COMMENT 'v1.1 最近位置纬度（脱敏到街道）',
  `region_code` VARCHAR(16) DEFAULT NULL COMMENT 'v1.1 行政区划代码（如310104）',
  `region_name` VARCHAR(64) DEFAULT NULL COMMENT 'v1.1 行政区名（如上海·徐汇区）',
  `last_location_at` DATETIME DEFAULT NULL COMMENT 'v1.1 最近位置更新时间',
  `female_visibility` TINYINT DEFAULT 0 COMMENT 'v1.1 女性可见设置 0仅活动内 1完全公开',
  `discover_enabled` TINYINT DEFAULT 1 COMMENT 'v1.1 是否参与附近发现（个人可关闭）',
  `status` TINYINT DEFAULT 1 COMMENT '1正常 0封禁',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_unionid` (`unionid`),
  KEY `idx_region` (`region_code`),
  KEY `idx_credit` (`credit_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='C端用户表（与若依sys_user分离）';
