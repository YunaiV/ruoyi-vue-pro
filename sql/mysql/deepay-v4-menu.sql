-- =====================================================================
-- Deepay 第四阶段 —— 菜单清理 + Deepay 菜单注册
--
-- 步骤 1：软删除所有非 Deepay 的旧菜单（deleted = 1）
-- 步骤 2：插入 Deepay 完整菜单树
--
-- 执行环境：MySQL 5.7 / 8.0
-- 注意：使用 ID 从 10000 起，避免与已有数据冲突
-- =====================================================================

-- ----------------------------
-- Step 1: 软删除所有现有菜单（清空旧若依菜单）
-- ----------------------------
UPDATE `system_menu`
SET `deleted`     = b'1',
    `update_time` = NOW()
WHERE `deleted` = b'0';

-- ----------------------------
-- Step 2: 插入 Deepay 菜单树
--
-- 菜单树结构：
--   10000  Deepay 智能生产（根目录）
--   ├── 10001  设计与生成（子目录）
--   │   ├── 10002  商品生成（页面）
--   │   └── 10003  生产链（页面）
--   ├── 10004  商品管理（页面）
--   ├── 10005  订单管理（页面）
--   └── 10006  数据复盘（页面）
-- ----------------------------

-- 根目录：Deepay 智能生产
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`,
     `icon`, `component`, `component_name`,
     `status`, `visible`, `keep_alive`, `always_show`,
     `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (10000, 'Deepay 智能生产', '', 1, 1, 0, '/deepay',
        'ep:magic-stick', NULL, NULL,
        0, b'1', b'1', b'1',
        'admin', NOW(), 'admin', NOW(), b'0');

-- 子目录：设计与生成
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`,
     `icon`, `component`, `component_name`,
     `status`, `visible`, `keep_alive`, `always_show`,
     `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (10001, '设计与生成', '', 1, 1, 10000, 'design',
        'ep:picture', NULL, NULL,
        0, b'1', b'1', b'1',
        'admin', NOW(), 'admin', NOW(), b'0');

-- 页面：商品生成（POST /api/create-product）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`,
     `icon`, `component`, `component_name`,
     `status`, `visible`, `keep_alive`, `always_show`,
     `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (10002, '商品生成', 'deepay:product:create', 2, 1, 10001, 'create-product',
        'ep:add', 'deepay/product/create', 'DeepayCreateProduct',
        0, b'1', b'1', b'1',
        'admin', NOW(), 'admin', NOW(), b'0');

-- 页面：生产链（POST /deepay/run-pipeline）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`,
     `icon`, `component`, `component_name`,
     `status`, `visible`, `keep_alive`, `always_show`,
     `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (10003, '生产链', 'deepay:pipeline:run', 2, 2, 10001, 'pipeline',
        'ep:connection', 'deepay/pipeline/index', 'DeepayPipeline',
        0, b'1', b'1', b'1',
        'admin', NOW(), 'admin', NOW(), b'0');

-- 页面：商品管理（deepay_product 列表）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`,
     `icon`, `component`, `component_name`,
     `status`, `visible`, `keep_alive`, `always_show`,
     `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (10004, '商品管理', 'deepay:product:list', 2, 2, 10000, 'products',
        'ep:goods', 'deepay/product/index', 'DeepayProduct',
        0, b'1', b'1', b'1',
        'admin', NOW(), 'admin', NOW(), b'0');

-- 页面：订单管理（deepay_order 列表）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`,
     `icon`, `component`, `component_name`,
     `status`, `visible`, `keep_alive`, `always_show`,
     `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (10005, '订单管理', 'deepay:order:list', 2, 3, 10000, 'orders',
        'ep:list', 'deepay/order/index', 'DeepayOrder',
        0, b'1', b'1', b'1',
        'admin', NOW(), 'admin', NOW(), b'0');

-- 页面：数据复盘（deepay_metrics 列表）
INSERT INTO `system_menu`
    (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`,
     `icon`, `component`, `component_name`,
     `status`, `visible`, `keep_alive`, `always_show`,
     `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (10006, '数据复盘', 'deepay:metrics:list', 2, 4, 10000, 'metrics',
        'ep:data-analysis', 'deepay/metrics/index', 'DeepayMetrics',
        0, b'1', b'1', b'1',
        'admin', NOW(), 'admin', NOW(), b'0');

-- ----------------------------
-- Step 3: 将 Deepay 所有菜单权限授予超级管理员角色（role_id=1）
-- （若 system_role_menu 表已有冲突行则忽略）
-- ----------------------------
INSERT IGNORE INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
    (1, 10000, 'admin', NOW(), 'admin', NOW(), b'0'),
    (1, 10001, 'admin', NOW(), 'admin', NOW(), b'0'),
    (1, 10002, 'admin', NOW(), 'admin', NOW(), b'0'),
    (1, 10003, 'admin', NOW(), 'admin', NOW(), b'0'),
    (1, 10004, 'admin', NOW(), 'admin', NOW(), b'0'),
    (1, 10005, 'admin', NOW(), 'admin', NOW(), b'0'),
    (1, 10006, 'admin', NOW(), 'admin', NOW(), b'0');
