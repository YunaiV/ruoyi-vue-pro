-- 支付模块-商户中心-菜单SQL

INSERT INTO `sys_menu`(
    `name`, `permission`, `menu_type`, `sort`, `parent_id`,
    `path`, `icon`, `component`, `status`
)
VALUES (
    '支付商户信息管理', '', 2, 0, ${table.parentMenuId},
    'merchant', '', 'pay/merchant/index', 0
);

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
INSERT INTO `sys_menu`(
    `name`, `permission`, `menu_type`, `sort`, `parent_id`,
    `path`, `icon`, `component`, `status`
)
VALUES (
    '支付商户信息查询', 'pay:merchant:query', 3, 1, @parentId,
    '', '', '', 0
);
INSERT INTO `sys_menu`(
    `name`, `permission`, `menu_type`, `sort`, `parent_id`,
    `path`, `icon`, `component`, `status`
)
VALUES (
    '支付商户信息创建', 'pay:merchant:create', 3, 2, @parentId,
    '', '', '', 0
);
INSERT INTO `sys_menu`(
    `name`, `permission`, `menu_type`, `sort`, `parent_id`,
    `path`, `icon`, `component`, `status`
)
VALUES (
    '支付商户信息更新', 'pay:merchant:update', 3, 3, @parentId,
    '', '', '', 0
);
INSERT INTO `sys_menu`(
    `name`, `permission`, `menu_type`, `sort`, `parent_id`,
    `path`, `icon`, `component`, `status`
)
VALUES (
    '支付商户信息删除', 'pay:merchant:delete', 3, 4, @parentId,
    '', '', '', 0
);
INSERT INTO `sys_menu`(
    `name`, `permission`, `menu_type`, `sort`, `parent_id`,
    `path`, `icon`, `component`, `status`
)
VALUES (
    '支付商户信息导出', 'pay:merchant:export', 3, 5, @parentId,
    '', '', '', 0
);
