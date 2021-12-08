-- 菜单 SQL
INSERT INTO `sys_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`,
                       `path`, `icon`, `component`, `status`)
VALUES ('支付订单管理', '', 2, 0, ${table.parentMenuId},
        'order', '', 'pay/order/index', 0);

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
INSERT INTO `sys_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`,
                       `path`, `icon`, `component`, `status`)
VALUES ('支付订单查询', 'pay:order:query', 3, 1, @parentId, '', '', '', 0);

INSERT INTO `sys_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`,
                       `path`, `icon`, `component`, `status`)
VALUES ('支付订单创建', 'pay:order:create', 3, 2, @parentId,
        '', '', '', 0);
INSERT INTO `sys_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`,
                       `path`, `icon`, `component`, `status`)
VALUES ('支付订单更新', 'pay:order:update', 3, 3, @parentId,
        '', '', '', 0);
INSERT INTO `sys_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`,
                       `path`, `icon`, `component`, `status`)
VALUES ('支付订单删除', 'pay:order:delete', 3, 4, @parentId,
        '', '', '', 0);
INSERT INTO `sys_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`,
                       `path`, `icon`, `component`, `status`)
VALUES ('支付订单导出', 'pay:order:export', 3, 5, @parentId,
        '', '', '', 0);
