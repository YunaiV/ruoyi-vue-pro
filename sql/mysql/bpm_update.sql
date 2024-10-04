-- ----------------------------
-- 流程抄送表新加流程活动编号
-- ----------------------------
ALTER TABLE `pro-test`.`bpm_process_instance_copy`
    ADD COLUMN `activity_id` varchar(64) NULL COMMENT '流程活动编号' AFTER `category`,
    MODIFY COLUMN `task_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '任务编号' AFTER `category`;

ALTER TABLE `pro-test`.`bpm_process_definition_info`
    ADD COLUMN `model_type` tinyint NOT NULL DEFAULT 10 COMMENT '流程模型的类型' AFTER `model_id`,
    ADD COLUMN `simple_model` json NULL COMMENT 'SIMPLE 设计器模型数据' AFTER `form_custom_view_path`,
    ADD COLUMN `visible` bit(1) NOT NULL DEFAULT 1 COMMENT '是否可见' AFTER `simple_model`;