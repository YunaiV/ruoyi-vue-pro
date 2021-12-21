-- 菜单 SQL
INSERT INTO `sys_menu`(
    `name`, `permission`, `menu_type`, `sort`, `parent_id`,
    `path`, `icon`, `component`, `status`
)
VALUES (
           '退款订单管理', '', 2, 0, 1117,
           'refund', '', 'pay/refund/index', 0
       );

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
INSERT INTO `sys_menu`(
    `name`, `permission`, `menu_type`, `sort`, `parent_id`,
    `path`, `icon`, `component`, `status`
)
VALUES (
           '退款订单查询', 'pay:refund:query', 3, 1, @parentId,
           '', '', '', 0
       );
INSERT INTO `sys_menu`(
    `name`, `permission`, `menu_type`, `sort`, `parent_id`,
    `path`, `icon`, `component`, `status`
)
VALUES (
           '退款订单创建', 'pay:refund:create', 3, 2, @parentId,
           '', '', '', 0
       );
INSERT INTO `sys_menu`(
    `name`, `permission`, `menu_type`, `sort`, `parent_id`,
    `path`, `icon`, `component`, `status`
)
VALUES (
           '退款订单更新', 'pay:refund:update', 3, 3, @parentId,
           '', '', '', 0
       );
INSERT INTO `sys_menu`(
    `name`, `permission`, `menu_type`, `sort`, `parent_id`,
    `path`, `icon`, `component`, `status`
)
VALUES (
           '退款订单删除', 'pay:refund:delete', 3, 4, @parentId,
           '', '', '', 0
       );
INSERT INTO `sys_menu`(
    `name`, `permission`, `menu_type`, `sort`, `parent_id`,
    `path`, `icon`, `component`, `status`
)
VALUES (
           '退款订单导出', 'pay:refund:export', 3, 5, @parentId,
           '', '', '', 0
       );
