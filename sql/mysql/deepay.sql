-- =====================================================================
-- Deepay 全量数据库初始化脚本（MySQL 5.7 / 8.0 兼容）
-- 合并来源：deepay.sql + deepay-v2-ima.sql + deepay-v3-inventory.sql
--           deepay-v3-payment.sql + deepay-v3-product-fields.sql
--           deepay-phase67.sql + deepay-phase10.sql
--           deepay-selection-v2.sql + deepay-fx.sql
--           deepay-feature-config.sql + deepay-v4-menu.sql
-- 说明：所有列已写入 CREATE TABLE，无 ALTER TABLE，幂等安全
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. deepay_style_chain — 设计链（所有字段终态）
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_style_chain` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code`       VARCHAR(64)   NOT NULL DEFAULT ''    COMMENT '6位随机大写链码，全局唯一',
    `image_url`        VARCHAR(512)  NOT NULL DEFAULT ''    COMMENT '最终选中的设计图片URL',
    `status`           VARCHAR(32)   NOT NULL DEFAULT ''    COMMENT '状态：CREATED / PUBLISHED / SOLD',
    `keyword`          VARCHAR(256)           DEFAULT NULL  COMMENT '用户输入关键词',
    `title`            VARCHAR(255)           DEFAULT NULL  COMMENT '商品标题',
    `description`      TEXT                   DEFAULT NULL  COMMENT '商品描述',
    `price`            DECIMAL(10,2)          DEFAULT NULL  COMMENT '商品价格（欧元）',
    `selected_image`   VARCHAR(512)           DEFAULT NULL  COMMENT 'AI 决策选中的图片',
    `decision_reason`  VARCHAR(1024)          DEFAULT NULL  COMMENT 'AI 决策原因（可解释）',
    `pattern_file`     VARCHAR(512)           DEFAULT NULL  COMMENT '打版文件路径（.dxf）',
    `context_snapshot` MEDIUMTEXT             DEFAULT NULL  COMMENT 'Context 快照（JSON）',
    `ima_kb_id`        VARCHAR(128)           DEFAULT NULL  COMMENT 'ima 知识库 ID，同步失败时为 NULL',
    `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_chain_code` (`chain_code`) COMMENT '链码唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 样式链码表';

-- ============================================================
-- 2. deepay_inventory — 库存管理表
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_inventory` (
    `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code`   VARCHAR(64) NOT NULL DEFAULT ''    COMMENT '商品链码，关联 deepay_style_chain',
    `stock`        INT         NOT NULL DEFAULT 0     COMMENT '当前可用库存',
    `locked_stock` INT         NOT NULL DEFAULT 0     COMMENT '锁定库存（已下单未支付）',
    `status`       VARCHAR(16) NOT NULL DEFAULT 'NORMAL' COMMENT '库存状态：NORMAL / LOW / OUT',
    `updated_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_chain_code` (`chain_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 库存管理表';

-- ============================================================
-- 3. deepay_product — 商品表（终态：含 currency / base_price / channel）
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_product` (
    `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code`    VARCHAR(64)    NOT NULL DEFAULT ''    COMMENT '关联样式链码',
    `title`         VARCHAR(256)   NOT NULL DEFAULT ''    COMMENT '商品标题',
    `description`   TEXT                   DEFAULT NULL  COMMENT '商品卖点描述（面料+版型+人群）',
    `cover_image`   VARCHAR(512)   NOT NULL DEFAULT ''    COMMENT '商品封面图URL',
    `price`         DECIMAL(10,2)  NOT NULL DEFAULT 0.00  COMMENT '销售价格（EUR）',
    `currency`      VARCHAR(10)    NOT NULL DEFAULT 'EUR' COMMENT '货币代码（ISO 4217），全系统统一 EUR',
    `base_price`    DECIMAL(10,2)           DEFAULT NULL  COMMENT '基准价（EUR），AI 定价系统唯一输出',
    `channel`       VARCHAR(64)             DEFAULT NULL  COMMENT '发布渠道（逗号分隔：H5 / H5,1688 / H5,Shopify）',
    `status`        VARCHAR(32)    NOT NULL DEFAULT 'SELLING' COMMENT '状态：SELLING / SOLD',
    `created_at`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_chain_code` (`chain_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 商品表';

-- ============================================================
-- 4. deepay_order — 订单表（终态：含 currency / base_amount / display_amount）
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_order` (
    `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code`     VARCHAR(64)    NOT NULL DEFAULT ''    COMMENT '关联商品链码',
    `amount`         DECIMAL(10,2)  NOT NULL DEFAULT 0.00  COMMENT '支付金额',
    `currency`       VARCHAR(10)    NOT NULL DEFAULT 'EUR' COMMENT '货币代码（ISO 4217），全系统统一 EUR',
    `base_amount`    DECIMAL(10,2)           DEFAULT NULL  COMMENT '基准金额（EUR），真实结算用',
    `display_amount` DECIMAL(10,2)           DEFAULT NULL  COMMENT '展示金额（用户付款货币）',
    `status`         VARCHAR(32)    NOT NULL DEFAULT 'INIT' COMMENT '状态：INIT / PAID',
    `created_at`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_chain_code` (`chain_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 订单表';

-- ============================================================
-- 5. deepay_metrics — 复盘指标表
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_metrics` (
    `id`         BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code` VARCHAR(64)    NOT NULL DEFAULT ''    COMMENT '关联商品链码',
    `price`      DECIMAL(10,2)  NOT NULL DEFAULT 0.00  COMMENT '成交价格',
    `sold`       TINYINT        NOT NULL DEFAULT 0     COMMENT '是否已售：0=未售 1=已售',
    `created_at` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_chain_code` (`chain_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 复盘指标表';

-- ============================================================
-- 6. deepay_customer_profile — B2B 客户画像（含 crowd / purpose）
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_customer_profile` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `customer_id`      BIGINT       NOT NULL               COMMENT 'B2B 客户 ID',
    `category_level1`  VARCHAR(64)           DEFAULT NULL  COMMENT '一级品类',
    `category_level2`  VARCHAR(64)           DEFAULT NULL  COMMENT '二级品类',
    `crowd`            VARCHAR(32)           DEFAULT NULL  COMMENT '目标客群：男装 / 少女 / 中老年 / 运动',
    `style_weights`    TEXT                  DEFAULT NULL  COMMENT '风格权重 JSON',
    `price_level`      VARCHAR(16)           DEFAULT NULL  COMMENT '价格带：LOW/MID/HIGH',
    `market`           VARCHAR(16)           DEFAULT NULL  COMMENT '目标市场：CN/EU/US/ME',
    `target_age`       VARCHAR(16)           DEFAULT NULL  COMMENT '年龄段：YOUNG/MIDDLE/ELDER',
    `gender`           VARCHAR(16)           DEFAULT NULL  COMMENT '目标性别：MALE/FEMALE/UNISEX',
    `purpose`          VARCHAR(20)           DEFAULT NULL  COMMENT '用途：WHOLESALE（批发）/ RETAIL（零售）',
    `confidence_score` DECIMAL(5,2) NOT NULL DEFAULT 0.00  COMMENT '画像置信度（0~1）',
    `created_at`       DATETIME     NOT NULL               COMMENT '创建时间',
    `updated_at`       DATETIME     NOT NULL               COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_customer_id`      (`customer_id`),
    KEY        `idx_category_level1` (`category_level1`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay B2B 客户画像（CustomerProfileAgent 读写）';

-- ============================================================
-- 7. deepay_trend_pool — 趋势款池（含 brand 字段）
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_trend_pool` (
    `id`         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `source`     VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '来源：1688 / tiktok / shein / brand / runway',
    `category`   VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '品类（外套 / 内裤 / 连衣裙 …）',
    `style`      VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '风格标签（工装 / 极简 / 性感 …）',
    `crowd`      VARCHAR(32)            DEFAULT NULL COMMENT '客群：男装 / 少女 / 中老年 / 运动',
    `image_url`  VARCHAR(512)  NOT NULL DEFAULT '' COMMENT '参考图 URL',
    `brand`      VARCHAR(64)            DEFAULT NULL COMMENT '来源品牌（ZARA / H&M / Nike / SHEIN / TikTok…）',
    `score`      DECIMAL(5,2)           DEFAULT 0.00 COMMENT '热度评分（0～100）',
    `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_category_style` (`category`, `style`),
    KEY `idx_crowd`          (`crowd`),
    KEY `idx_score`          (`score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 趋势款池';

-- ============================================================
-- 8. deepay_selection_log — 客户选款记录
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_selection_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `customer_id` BIGINT       NOT NULL               COMMENT '客户 ID',
    `image_url`   VARCHAR(512) NOT NULL DEFAULT ''    COMMENT '参考图 URL',
    `category`    VARCHAR(64)           DEFAULT NULL  COMMENT '品类',
    `style`       VARCHAR(64)           DEFAULT NULL  COMMENT '风格',
    `crowd`       VARCHAR(32)           DEFAULT NULL  COMMENT '客群',
    `is_selected` TINYINT(1)   NOT NULL DEFAULT 0     COMMENT '1=选中 / 0=跳过',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_customer_id`       (`customer_id`),
    KEY `idx_customer_category` (`customer_id`, `category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 客户选款记录';

-- ============================================================
-- 9. deepay_payment_log — 支付回调日志
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_payment_log` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `payment_id`   VARCHAR(128)  NOT NULL DEFAULT '' COMMENT '支付渠道订单号',
    `order_id`     VARCHAR(32)            DEFAULT NULL COMMENT '关联 deepay_order.id',
    `status`       VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '处理状态：PAID/IDEMPOTENT/SIGN_FAIL/AMOUNT_MISMATCH/ORDER_NOT_FOUND/REFUND',
    `amount`       DECIMAL(12,2)          DEFAULT NULL COMMENT '实收金额（EUR）',
    `callback_raw` TEXT                   DEFAULT NULL COMMENT '回调原始报文（截取前2000字符）',
    `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_payment_id` (`payment_id`),
    KEY `idx_status`     (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Deepay 支付回调日志';

-- ============================================================
-- 10. deepay_design_generated — 原创设计生成记录
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_design_generated` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `chain_code` VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '链码',
    `image_url`  VARCHAR(512) NOT NULL DEFAULT '' COMMENT '生成图 URL',
    `prompt`     VARCHAR(500)          DEFAULT NULL COMMENT '生成 Prompt',
    `version`    INT          NOT NULL DEFAULT 1  COMMENT '版本号',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_chain_code` (`chain_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Deepay 原创设计生成记录';

-- ============================================================
-- 11. deepay_design_variant — 设计变体记录
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_design_variant` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `chain_code` VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '链码',
    `image_url`  VARCHAR(512) NOT NULL DEFAULT '' COMMENT '变体图 URL',
    `style_tag`  VARCHAR(32)           DEFAULT NULL COMMENT '变体风格标签',
    `score`      INT          NOT NULL DEFAULT 0  COMMENT '综合得分（0~100）',
    `selected`   TINYINT      NOT NULL DEFAULT 0  COMMENT '是否选中（1=是）',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_chain_code` (`chain_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Deepay 设计变体记录';

-- ============================================================
-- 12. deepay_design_cost — 成本估算记录
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_design_cost` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT,
    `chain_code`    VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '链码',
    `fabric_cost`   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '面料成本（EUR）',
    `labor_cost`    DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '人工成本（EUR）',
    `total_cost`    DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总成本（EUR）',
    `suggest_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '建议售价（EUR）',
    `margin`        DECIMAL(8,4)           DEFAULT NULL  COMMENT '毛利率（0~1）',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_chain_code` (`chain_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Deepay 成本估算记录';

-- ============================================================
-- 13. deepay_fx_rate — FX 汇率表（EUR 基准）
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_fx_rate` (
    `currency`   VARCHAR(10)    NOT NULL               COMMENT '货币代码（ISO 4217），主键',
    `rate`       DECIMAL(12,6)  NOT NULL               COMMENT '相对 EUR 的汇率（1 EUR = rate 目标货币）',
    `updated_at` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`currency`) USING BTREE,
    KEY `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Deepay FX 汇率表（EUR基准，FxRateScheduler 每小时更新）';

-- 预置默认汇率
INSERT INTO `deepay_fx_rate` (`currency`, `rate`, `updated_at`) VALUES
  ('EUR', 1.000000, NOW()), ('USD', 1.080000, NOW()), ('CNY', 7.800000, NOW()),
  ('GBP', 0.860000, NOW()), ('AED', 3.970000, NOW()), ('JPY', 163.000000, NOW()),
  ('KRW', 1450.000000, NOW()), ('SGD', 1.460000, NOW()), ('HKD', 8.440000, NOW()),
  ('AUD', 1.650000, NOW()), ('CAD', 1.470000, NOW()), ('CHF', 0.950000, NOW()),
  ('SEK', 11.300000, NOW()), ('NOK', 11.600000, NOW()), ('DKK', 7.460000, NOW()),
  ('PLN', 4.230000, NOW()), ('INR', 90.000000, NOW()), ('BRL', 5.450000, NOW()),
  ('MXN', 18.200000, NOW()), ('SAR', 4.050000, NOW()), ('QAR', 3.930000, NOW()),
  ('THB', 38.500000, NOW()), ('MYR', 5.070000, NOW()), ('IDR', 17300.000000, NOW()),
  ('VND', 27300.000000, NOW()), ('PHP', 61.200000, NOW()), ('TWD', 34.800000, NOW())
AS new_val ON DUPLICATE KEY UPDATE `rate` = new_val.`rate`, `updated_at` = new_val.`updated_at`;

-- ============================================================
-- 14. deepay_feature_config — 功能菜单配置表
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_feature_config` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `feature_key`  VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '功能唯一键',
    `feature_name` VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '功能显示名称',
    `icon`         VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '图标',
    `description`  VARCHAR(128) NOT NULL DEFAULT '' COMMENT '功能描述',
    `route_path`   VARCHAR(128) NOT NULL DEFAULT '' COMMENT '前端路由路径',
    `menu_group`   VARCHAR(32)  NOT NULL DEFAULT 'other' COMMENT '菜单分组：ai_design/material/commerce/ops',
    `sort_order`   INT          NOT NULL DEFAULT 99  COMMENT '组内排序',
    `enabled`      TINYINT      NOT NULL DEFAULT 1   COMMENT '是否启用：1=启用 0=禁用',
    `visible_to`   VARCHAR(16)  NOT NULL DEFAULT 'all' COMMENT '可见范围：all/vip/beta/admin',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_feature_key` (`feature_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay 功能菜单配置表';

INSERT IGNORE INTO `deepay_feature_config`
    (`feature_key`, `feature_name`, `icon`, `description`, `route_path`, `menu_group`, `sort_order`, `enabled`, `visible_to`)
VALUES
  ('ai_generate',  'AI出款生成', '🎯', '可控出款，系列生成',   '/ai/design',   'ai_design', 1, 1, 'all'),
  ('ai_season',    '整季系列',   '🌿', '一键生成 A/B/C 整季', '/ai/season',   'ai_design', 2, 1, 'all'),
  ('ai_redesign',  '改款设计',   '🔧', '参考图 → 新款',        '/redesign',    'ai_design', 3, 1, 'all'),
  ('techpack',     '技术包',     '📐', 'Tech Pack 设计稿',     '/ai/techpack', 'ai_design', 4, 1, 'all'),
  ('inspiration',  '灵感库',     '🎭', '精选秀场 + 品牌图',    '/inspiration', 'material',  1, 1, 'all'),
  ('template',     '模板库',     '📦', '快速开店模板',          '/template',    'material',  2, 1, 'all'),
  ('shop',         '我的店铺',   '🏪', '管理和分享你的店铺',   '/me',          'commerce',  1, 1, 'all'),
  ('leaderboard',  '收益排行',   '🏆', '看看你排第几名',       '/leaderboard', 'commerce',  2, 1, 'all'),
  ('invite',       '邀请好友',   '🎁', '每邀请一人得 €2',      '/invite',      'commerce',  3, 1, 'all'),
  ('share',        '收益贡献',   '💸', '购买份额，持续分红',   '/share',       'commerce',  4, 1, 'all');

-- ============================================================
-- 15. 趋势款池种子数据
-- ============================================================
INSERT IGNORE INTO `deepay_trend_pool` (`source`, `category`, `style`, `crowd`, `image_url`, `brand`, `score`) VALUES
  ('1688',   '外套',   '工装', '男装',   'img/trend/coat_workwear_male_1.jpg',     '1688',  90.00),
  ('tiktok', '外套',   '极简', '男装',   'img/trend/coat_minimal_male_1.jpg',      'TikTok',85.00),
  ('shein',  '外套',   '街头', '少女',   'img/trend/coat_street_girl_1.jpg',       'SHEIN', 88.00),
  ('brand',  '外套',   '轻奢', '男装',   'img/trend/coat_luxury_male_1.jpg',       '品牌参考',80.00),
  ('runway', '外套',   '性感', '少女',   'img/trend/coat_sexy_girl_1.jpg',         '秀场',  75.00),
  ('1688',   '内裤',   '性感', '少女',   'img/trend/underwear_sexy_girl_1.jpg',    '1688',  92.00),
  ('shein',  '内裤',   '极简', '少女',   'img/trend/underwear_minimal_girl_1.jpg', 'SHEIN', 82.00),
  ('brand',  '内衣',   '轻奢', '少女',   'img/trend/lingerie_luxury_girl_1.jpg',   '品牌参考',78.00),
  ('tiktok', '连衣裙', '性感', '少女',   'img/trend/dress_sexy_girl_1.jpg',        'TikTok',95.00),
  ('shein',  '连衣裙', '休闲', '少女',   'img/trend/dress_casual_girl_1.jpg',      'SHEIN', 88.00),
  ('runway', '连衣裙', '轻奢', '中老年', 'img/trend/dress_luxury_elder_1.jpg',     '秀场',  72.00),
  ('1688',   '裤子',   '工装', '男装',   'img/trend/pants_workwear_male_1.jpg',    '1688',  87.00),
  ('brand',  '裤子',   '极简', '男装',   'img/trend/pants_minimal_male_1.jpg',     '品牌参考',80.00),
  ('shein',  '裤子',   '休闲', '少女',   'img/trend/pants_casual_girl_1.jpg',      'SHEIN', 83.00),
  ('tiktok', '上衣',   '街头', '运动',   'img/trend/top_street_sport_1.jpg',       'TikTok',89.00),
  ('1688',   '上衣',   '休闲', '少女',   'img/trend/top_casual_girl_1.jpg',        '1688',  86.00),
  ('brand',  'T恤',    '极简', '男装',   'img/trend/tshirt_minimal_male_1.jpg',    '品牌参考',84.00);

-- ============================================================
-- 16. Deepay 菜单注册（system_menu + system_role_menu）
-- ============================================================

-- 软删除所有旧若依菜单
UPDATE `system_menu` SET `deleted` = b'1', `update_time` = NOW()
WHERE `deleted` = b'0' AND `id` < 10000;

-- 软删除旧角色-菜单绑定
UPDATE `system_role_menu` SET `deleted` = b'1', `update_time` = NOW()
WHERE `deleted` = b'0' AND `menu_id` < 10000;

-- 根目录：Deepay 智能生产
INSERT IGNORE INTO `system_menu`
    (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
     `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
VALUES (10000,'Deepay 智能生产','',1,1,0,'/deepay','ep:magic-stick',NULL,NULL,
        0,b'1',b'1',b'1','admin',NOW(),'admin',NOW(),b'0');

-- 子目录：设计与生成
INSERT IGNORE INTO `system_menu`
    (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
     `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
VALUES (10001,'设计与生成','',1,1,10000,'design','ep:picture',NULL,NULL,
        0,b'1',b'1',b'1','admin',NOW(),'admin',NOW(),b'0');

-- 页面：商品生成
INSERT IGNORE INTO `system_menu`
    (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
     `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
VALUES (10002,'商品生成','deepay:product:create',2,1,10001,'create-product','ep:add',
        'deepay/product/create','DeepayCreateProduct',0,b'1',b'1',b'1','admin',NOW(),'admin',NOW(),b'0');

-- 页面：生产链
INSERT IGNORE INTO `system_menu`
    (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
     `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
VALUES (10003,'生产链','deepay:pipeline:run',2,2,10001,'pipeline','ep:connection',
        'deepay/pipeline/index','DeepayPipeline',0,b'1',b'1',b'1','admin',NOW(),'admin',NOW(),b'0');

-- 页面：商品管理
INSERT IGNORE INTO `system_menu`
    (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
     `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
VALUES (10004,'商品管理','deepay:product:list',2,2,10000,'products','ep:goods',
        'deepay/product/index','DeepayProduct',0,b'1',b'1',b'1','admin',NOW(),'admin',NOW(),b'0');

-- 页面：订单管理
INSERT IGNORE INTO `system_menu`
    (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
     `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
VALUES (10005,'订单管理','deepay:order:list',2,3,10000,'orders','ep:list',
        'deepay/order/index','DeepayOrder',0,b'1',b'1',b'1','admin',NOW(),'admin',NOW(),b'0');

-- 页面：数据复盘
INSERT IGNORE INTO `system_menu`
    (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
     `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
VALUES (10006,'数据复盘','deepay:metrics:list',2,4,10000,'metrics','ep:data-analysis',
        'deepay/metrics/index','DeepayMetrics',0,b'1',b'1',b'1','admin',NOW(),'admin',NOW(),b'0');

-- 授权超级管理员
INSERT IGNORE INTO `system_role_menu`
    (`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
VALUES
  (1,10000,'admin',NOW(),'admin',NOW(),b'0'),
  (1,10001,'admin',NOW(),'admin',NOW(),b'0'),
  (1,10002,'admin',NOW(),'admin',NOW(),b'0'),
  (1,10003,'admin',NOW(),'admin',NOW(),b'0'),
  (1,10004,'admin',NOW(),'admin',NOW(),b'0'),
  (1,10005,'admin',NOW(),'admin',NOW(),b'0'),
  (1,10006,'admin',NOW(),'admin',NOW(),b'0');

SET FOREIGN_KEY_CHECKS = 1;
