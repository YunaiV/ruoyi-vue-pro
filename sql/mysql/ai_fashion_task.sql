-- ============================================================
-- 服装设计多模型流水线任务表
-- 数据库：MySQL 8.x
-- 版本：v1.0.0  2026-04-25
-- ============================================================

-- ----------------------------
-- 主任务表
-- ----------------------------
CREATE TABLE `ai_fashion_task`  (
  `id`               bigint        NOT NULL AUTO_INCREMENT COMMENT '任务编号',
  `user_id`          bigint        NOT NULL COMMENT '用户编号',
  `prompt`           varchar(2000) NOT NULL COMMENT '正向提示词',
  `negative_prompt`  varchar(2000) NULL     DEFAULT NULL COMMENT '负向提示词',
  `width`            int           NOT NULL DEFAULT 768   COMMENT '生成宽度（像素）',
  `height`           int           NOT NULL DEFAULT 1024  COMMENT '生成高度（像素）',
  `seed`             bigint        NULL     DEFAULT -1    COMMENT '随机种子，-1 表示随机',
  `quality_preset`   varchar(50)   NULL     DEFAULT NULL  COMMENT '质量预设（HIGH/MEDIUM/LOW）',
  `model_checkpoint` varchar(255)  NULL     DEFAULT NULL  COMMENT 'SDXL checkpoint 名称（可覆盖服务器默认值）',
  `pose_image_url`   varchar(1000) NULL     DEFAULT NULL  COMMENT 'ControlNet 姿势参考图片地址',
  `fabric_ref_url`   varchar(1000) NULL     DEFAULT NULL  COMMENT '面料参考图片地址',
  `fabric_strength`  decimal(4,2)  NULL     DEFAULT 0.70  COMMENT '面料转换强度（0~1）',
  `upscale`          bit(1)        NOT NULL DEFAULT b'1'  COMMENT '是否执行超分辨率',
  `upscale_factor`   int           NULL     DEFAULT 2     COMMENT '超分倍数',
  `upscaler_name`    varchar(100)  NULL     DEFAULT 'R-ESRGAN 4x+' COMMENT '超分模型名称',
  `status`           tinyint       NOT NULL DEFAULT 10    COMMENT '任务状态（10进行中 20已完成 30已失败）',
  `trace_id`         varchar(64)   NULL     DEFAULT NULL  COMMENT '链路追踪 ID（用于日志 MDC）',
  `final_pic_url`    varchar(1000) NULL     DEFAULT NULL  COMMENT '最终产出图片地址',
  `error_message`    varchar(2000) NULL     DEFAULT NULL  COMMENT '失败原因',
  `start_time`       datetime      NULL     DEFAULT NULL  COMMENT '开始执行时间',
  `finish_time`      datetime      NULL     DEFAULT NULL  COMMENT '完成/失败时间',
  `creator`          varchar(64)   NOT NULL DEFAULT ''    COMMENT '创建者',
  `create_time`      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`          varchar(64)   NOT NULL DEFAULT ''    COMMENT '更新者',
  `update_time`      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`          bit(1)        NOT NULL DEFAULT b'0'  COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_status`  (`status`)  USING BTREE,
  INDEX `idx_create_time` (`create_time`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'AI 服装设计流水线任务';

-- ----------------------------
-- 步骤表
-- ----------------------------
CREATE TABLE `ai_fashion_task_step`  (
  `id`              bigint        NOT NULL AUTO_INCREMENT COMMENT '步骤编号',
  `task_id`         bigint        NOT NULL               COMMENT '所属任务编号',
  `step_order`      tinyint       NOT NULL DEFAULT 0     COMMENT '步骤顺序（0-based）',
  `step_type`       varchar(20)   NOT NULL               COMMENT '步骤类型（SDXL/POSE/FABRIC/UPSCALE/THREE_D）',
  `status`          tinyint       NOT NULL DEFAULT 10    COMMENT '步骤状态（10进行中 20已完成 30已失败 40已跳过）',
  `input_pic_url`   varchar(1000) NULL     DEFAULT NULL  COMMENT '输入图片地址',
  `output_pic_url`  varchar(1000) NULL     DEFAULT NULL  COMMENT '输出图片地址',
  `input_options`   json          NULL     DEFAULT NULL  COMMENT '输入参数（JSON）',
  `output_options`  json          NULL     DEFAULT NULL  COMMENT '输出元信息（JSON，如 seed、model hash）',
  `model_name`      varchar(255)  NULL     DEFAULT NULL  COMMENT '本步骤使用的模型名称',
  `duration_ms`     bigint        NULL     DEFAULT NULL  COMMENT '本步骤耗时（毫秒）',
  `retry_count`     int           NOT NULL DEFAULT 0     COMMENT '重试次数',
  `error_message`   varchar(2000) NULL     DEFAULT NULL  COMMENT '失败原因',
  `creator`         varchar(64)   NOT NULL DEFAULT ''    COMMENT '创建者',
  `create_time`     datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`         varchar(64)   NOT NULL DEFAULT ''    COMMENT '更新者',
  `update_time`     datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         bit(1)        NOT NULL DEFAULT b'0'  COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_id`    (`task_id`) USING BTREE,
  INDEX `idx_step_type`  (`step_type`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'AI 服装设计流水线任务步骤';
