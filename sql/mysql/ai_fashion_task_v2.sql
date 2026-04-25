-- ============================================================
-- AI 服装任务表 v2 升级脚本
-- 新增字段：路由策略、工作流模式、总耗时、3D需求、色板
-- 数据库：MySQL 8.x
-- 版本：v2.0.0  2026-04-25
-- ============================================================

ALTER TABLE `ai_fashion_task`
    ADD COLUMN `routing_strategy` varchar(20)   NULL COMMENT '路由策略（QUALITY_FIRST/COST_FIRST/SPEED_FIRST/BALANCED）' AFTER `quality_preset`,
    ADD COLUMN `workflow_mode`    varchar(20)   NULL COMMENT '工作流模式（BASIC/STANDARD/PROFESSIONAL/FULL）'              AFTER `routing_strategy`,
    ADD COLUMN `total_duration_ms` bigint       NULL COMMENT '总耗时(毫秒)'                                               AFTER `finish_time`,
    ADD COLUMN `require_3d`       bit(1)        NOT NULL DEFAULT b'0' COMMENT '是否需要3D转换'                             AFTER `total_duration_ms`,
    ADD COLUMN `color_palette`    varchar(500)  NULL COMMENT '色板(JSON，List<String>)'                                   AFTER `require_3d`;
