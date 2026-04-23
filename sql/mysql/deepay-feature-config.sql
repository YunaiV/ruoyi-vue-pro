-- ============================================================
-- deepay-feature-config.sql
-- 功能菜单配置表 + 预置10条数据
-- 执行顺序：在 deepay.sql 之后执行
-- ============================================================

-- ── 建表 ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `deepay_feature_config`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `feature_key`  VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '功能唯一键，英文下划线，前端路由映射用',
    `feature_name` VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '功能显示名称（中文）',
    `icon`         VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '图标（emoji 或 icon class）',
    `description`  VARCHAR(128) NOT NULL DEFAULT '' COMMENT '功能描述（前端卡片副标题）',
    `route_path`   VARCHAR(128) NOT NULL DEFAULT '' COMMENT '前端路由路径，例如 /ai/design',
    `menu_group`   VARCHAR(32)  NOT NULL DEFAULT 'other' COMMENT '菜单分组：ai_design/material/commerce/ops',
    `sort_order`   INT          NOT NULL DEFAULT 99 COMMENT '组内排序（数字越小越靠前）',
    `enabled`      TINYINT      NOT NULL DEFAULT 1 COMMENT '是否启用：1=启用 0=禁用',
    `visible_to`   VARCHAR(16)  NOT NULL DEFAULT 'all' COMMENT '可见范围：all/vip/beta/admin',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_feature_key` (`feature_key`) COMMENT '功能键唯一索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = 'Deepay 功能菜单配置表';

-- ── 预置数据（10条）────────────────────────────────────────────────────
-- 避免重复执行时报错，使用 INSERT IGNORE
INSERT IGNORE INTO `deepay_feature_config`
    (`feature_key`, `feature_name`, `icon`, `description`, `route_path`, `menu_group`, `sort_order`, `enabled`, `visible_to`)
VALUES
-- 🎨 AI设计组（ai_design）
('ai_generate',  'AI出款生成', '🎯', '可控出款，系列生成',       '/ai/design',    'ai_design', 1, 1, 'all'),
('ai_season',    '整季系列',   '🌿', '一键生成 A/B/C 整季',     '/ai/season',    'ai_design', 2, 1, 'all'),
('ai_redesign',  '改款设计',   '🔧', '参考图 → 新款',           '/redesign',     'ai_design', 3, 1, 'all'),
('techpack',     '技术包',     '📐', 'Tech Pack 设计稿',        '/ai/techpack',  'ai_design', 4, 1, 'all'),

-- 🧠 设计素材组（material）
('inspiration',  '灵感库',    '🎭', '精选秀场 + 品牌图',         '/inspiration',  'material',  1, 1, 'all'),
('template',     '模板库',    '📦', '快速开店模板',              '/template',     'material',  2, 1, 'all'),

-- 💰 商业中心组（commerce）
('shop',         '我的店铺',  '🏪', '管理和分享你的店铺',         '/me',           'commerce',  1, 1, 'all'),
('leaderboard',  '收益排行',  '🏆', '看看你排第几名',            '/leaderboard',  'commerce',  2, 1, 'all'),
('invite',       '邀请好友',  '🎁', '每邀请一人得 €2',           '/invite',       'commerce',  3, 1, 'all'),
('share',        '收益贡献',  '💸', '购买份额，持续分红',         '/share',        'commerce',  4, 1, 'all');
