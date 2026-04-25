-- ============================================================
-- AI 服装 3D 资产表
-- 数据库：MySQL 8.x
-- 版本：v1.0.0  2026-04-25
-- ============================================================

CREATE TABLE `ai_fashion_3d_asset` (
  `id`                bigint        NOT NULL AUTO_INCREMENT COMMENT '资产编号',
  `user_id`           bigint        NOT NULL               COMMENT '用户编号',
  `task_id`           bigint        NULL     DEFAULT NULL  COMMENT '来源设计任务ID（可选）',
  `source_image_url`  varchar(1000) NULL     DEFAULT NULL  COMMENT '来源 2D 图片地址',
  `depth_map_url`     varchar(1000) NULL     DEFAULT NULL  COMMENT '生成的深度图地址',
  `obj_file_url`      varchar(1000) NULL     DEFAULT NULL  COMMENT 'OBJ 网格文件地址',
  `mtl_file_url`      varchar(1000) NULL     DEFAULT NULL  COMMENT 'MTL 材质文件地址',
  `texture_url`       varchar(1000) NULL     DEFAULT NULL  COMMENT '纹理图片地址',
  `preview_urls_json` text          NULL                   COMMENT '多角度预览图 JSON（{"angle_0":"url", ...}）',
  `rotation_gif_url`  varchar(1000) NULL     DEFAULT NULL  COMMENT '旋转合成图/GIF 地址',
  `vertices_count`    int           NULL     DEFAULT NULL  COMMENT '网格顶点数',
  `faces_count`       int           NULL     DEFAULT NULL  COMMENT '网格面片数',
  `grid_resolution`   int           NULL     DEFAULT 64    COMMENT '网格精度（每个维度的采样点数）',
  `color_change`      varchar(20)   NULL     DEFAULT NULL  COMMENT '颜色变换 Hex（如 #FF0000）',
  `output_format`     varchar(20)   NULL     DEFAULT 'OBJ' COMMENT '输出格式（OBJ/GLTF/STL）',
  `status`            varchar(20)   NOT NULL DEFAULT 'PROCESSING' COMMENT '处理状态（PROCESSING/SUCCESS/FAIL）',
  `error_message`     varchar(2000) NULL     DEFAULT NULL  COMMENT '失败原因',
  `duration_ms`       bigint        NULL     DEFAULT NULL  COMMENT '处理总耗时（毫秒）',
  `creator`           varchar(64)   NOT NULL DEFAULT ''    COMMENT '创建者',
  `create_time`       datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`           varchar(64)   NOT NULL DEFAULT ''    COMMENT '更新者',
  `update_time`       datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`           bit(1)        NOT NULL DEFAULT b'0'  COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_task_id` (`task_id`) USING BTREE,
  INDEX `idx_status`  (`status`)  USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'AI 服装 3D 资产';
