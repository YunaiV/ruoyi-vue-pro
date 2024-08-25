-- ----------------------------
-- 流程抄送表新加流程活动编号
-- ----------------------------
ALTER TABLE `pro-test`.`bpm_process_instance_copy`
    ADD COLUMN `activity_id` varchar(64) NULL COMMENT '流程活动编号' AFTER `category`,
    MODIFY COLUMN `task_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '任务编号' AFTER `category`;