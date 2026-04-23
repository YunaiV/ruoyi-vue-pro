-- =====================================================================
-- Deepay Phase 6-7 — 选款引导层（数据库迁移脚本）
-- 执行环境：MySQL 5.7 / 8.0
-- =====================================================================

-- ----------------------------
-- 1. deepay_customer_profile 补充 crowd 字段
-- ----------------------------
ALTER TABLE `deepay_customer_profile`
    ADD COLUMN IF NOT EXISTS `crowd` VARCHAR(32) DEFAULT NULL
        COMMENT '目标客群：男装 / 少女 / 中老年 / 运动'
        AFTER `category_level2`;

-- ----------------------------
-- 2. 趋势款池（爆款层 + 设计层）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `deepay_trend_pool`
(
    `id`         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `source`     VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '来源：1688 / tiktok / shein / brand / runway',
    `category`   VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '品类（外套 / 内裤 / 连衣裙 …）',
    `style`      VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '风格标签（工装 / 极简 / 性感 …）',
    `crowd`      VARCHAR(32)            DEFAULT NULL COMMENT '客群：男装 / 少女 / 中老年 / 运动',
    `image_url`  VARCHAR(512)  NOT NULL DEFAULT '' COMMENT '参考图 URL',
    `score`      DECIMAL(5, 2)          DEFAULT 0.00 COMMENT '热度评分（0～100）',
    `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_category_style` (`category`, `style`) USING BTREE,
    INDEX `idx_crowd` (`crowd`) USING BTREE,
    INDEX `idx_score` (`score` DESC) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Deepay 趋势款池（Phase 6-7 爆款层 + 设计层）';

-- ----------------------------
-- 3. 客户选款记录（驱动记忆进化）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `deepay_selection_log`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `customer_id` BIGINT       NOT NULL COMMENT '客户 ID',
    `image_url`   VARCHAR(512) NOT NULL DEFAULT '' COMMENT '参考图 URL',
    `category`    VARCHAR(64)           DEFAULT NULL COMMENT '品类',
    `style`       VARCHAR(64)           DEFAULT NULL COMMENT '风格',
    `crowd`       VARCHAR(32)           DEFAULT NULL COMMENT '客群',
    `is_selected` TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '1=选中 / 0=跳过',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_customer_id` (`customer_id`) USING BTREE,
    INDEX `idx_customer_category` (`customer_id`, `category`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Deepay 客户选款记录（Phase 6-7 记忆进化）';

-- ----------------------------
-- 4. deepay_style_chain 补齐字段（如旧版本未迁移）
-- ----------------------------
ALTER TABLE `deepay_style_chain`
    ADD COLUMN IF NOT EXISTS `keyword`          VARCHAR(128)  DEFAULT NULL COMMENT '原始关键词'          AFTER `chain_code`,
    ADD COLUMN IF NOT EXISTS `selected_image`   VARCHAR(512)  DEFAULT NULL COMMENT '选中设计图'          AFTER `image_url`,
    ADD COLUMN IF NOT EXISTS `decision_reason`  VARCHAR(1024) DEFAULT NULL COMMENT 'AI 决策原因'         AFTER `selected_image`,
    ADD COLUMN IF NOT EXISTS `pattern_file`     VARCHAR(256)  DEFAULT NULL COMMENT '打版文件路径'        AFTER `decision_reason`,
    ADD COLUMN IF NOT EXISTS `context_snapshot` MEDIUMTEXT    DEFAULT NULL COMMENT 'Context 快照(JSON)' AFTER `pattern_file`,
    ADD COLUMN IF NOT EXISTS `status`           VARCHAR(32)   DEFAULT NULL COMMENT '状态'               AFTER `context_snapshot`;

-- ----------------------------
-- 5. 预置种子数据（各品类各客群各风格各一张示例图，方便本地调试）
-- ----------------------------
INSERT IGNORE INTO `deepay_trend_pool` (`source`, `category`, `style`, `crowd`, `image_url`, `score`) VALUES
-- 外套
('1688',    '外套', '工装',   '男装',   'img/trend/coat_workwear_male_1.jpg',    90.00),
('tiktok',  '外套', '极简',   '男装',   'img/trend/coat_minimal_male_1.jpg',     85.00),
('shein',   '外套', '街头',   '少女',   'img/trend/coat_street_girl_1.jpg',      88.00),
('brand',   '外套', '轻奢',   '男装',   'img/trend/coat_luxury_male_1.jpg',      80.00),
('runway',  '外套', '性感',   '少女',   'img/trend/coat_sexy_girl_1.jpg',        75.00),
-- 内裤 / 内衣
('1688',    '内裤', '性感',   '少女',   'img/trend/underwear_sexy_girl_1.jpg',   92.00),
('shein',   '内裤', '极简',   '少女',   'img/trend/underwear_minimal_girl_1.jpg',82.00),
('brand',   '内衣', '轻奢',   '少女',   'img/trend/lingerie_luxury_girl_1.jpg',  78.00),
-- 连衣裙
('tiktok',  '连衣裙', '性感', '少女',   'img/trend/dress_sexy_girl_1.jpg',       95.00),
('shein',   '连衣裙', '休闲', '少女',   'img/trend/dress_casual_girl_1.jpg',     88.00),
('runway',  '连衣裙', '轻奢', '中老年', 'img/trend/dress_luxury_elder_1.jpg',    72.00),
-- 裤子
('1688',    '裤子', '工装',   '男装',   'img/trend/pants_workwear_male_1.jpg',   87.00),
('brand',   '裤子', '极简',   '男装',   'img/trend/pants_minimal_male_1.jpg',    80.00),
('shein',   '裤子', '休闲',   '少女',   'img/trend/pants_casual_girl_1.jpg',     83.00),
-- 上衣 / T恤
('tiktok',  '上衣', '街头',   '运动',   'img/trend/top_street_sport_1.jpg',      89.00),
('1688',    '上衣', '休闲',   '少女',   'img/trend/top_casual_girl_1.jpg',       86.00),
('brand',   'T恤',  '极简',   '男装',   'img/trend/tshirt_minimal_male_1.jpg',   84.00);
