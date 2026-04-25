-- ============================================================
-- AI 服装设计智能会话表
-- 数据库：MySQL 8.x
-- 版本：v1.0.0  2026-04-25
-- ============================================================

CREATE TABLE `ai_fashion_session` (
  `id`               bigint        NOT NULL AUTO_INCREMENT COMMENT '会话编号',
  `user_id`          bigint        NOT NULL               COMMENT '用户编号',
  `session_token`    varchar(64)   NOT NULL               COMMENT '会话令牌（UUID，由前端存储并传入）',
  `last_task_id`     bigint        NULL     DEFAULT NULL  COMMENT '最近一次任务ID',
  `last_prompt`      varchar(2000) NULL     DEFAULT NULL  COMMENT '最近一次完整 SD Prompt',
  `current_style`    varchar(500)  NULL     DEFAULT NULL  COMMENT '当前风格 SD 关键词',
  `current_colors`   varchar(500)  NULL     DEFAULT NULL  COMMENT '当前颜色 SD 关键词',
  `current_fabric`   varchar(500)  NULL     DEFAULT NULL  COMMENT '当前面料 SD 关键词',
  `current_length`   varchar(200)  NULL     DEFAULT NULL  COMMENT '当前长度 SD 关键词',
  `current_fit`      varchar(200)  NULL     DEFAULT NULL  COMMENT '当前版型 SD 关键词',
  `variant_task_ids` varchar(1000) NULL     DEFAULT NULL  COMMENT '近期批量任务ID列表（JSON 数组）',
  `creator`          varchar(64)   NOT NULL DEFAULT ''    COMMENT '创建者',
  `create_time`      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`          varchar(64)   NOT NULL DEFAULT ''    COMMENT '更新者',
  `update_time`      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`          bit(1)        NOT NULL DEFAULT b'0'  COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  UNIQUE KEY `uk_user_token` (`user_id`, `session_token`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'AI 服装设计智能会话';
