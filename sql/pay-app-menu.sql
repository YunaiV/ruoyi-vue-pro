-- 菜单 SQL
INSERT INTO `sys_menu`(
    `name`, `permission`, `menu_type`, `sort`, `parent_id`,
    `path`, `icon`, `component`, `status`
)
VALUES (
           '支付应用信息管理', '', 2, 0, 1117,
           'app', '', 'pay/app/index', 0
       );

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
INSERT INTO `sys_menu`(
    `name`, `permission`, `menu_type`, `sort`, `parent_id`,
    `path`, `icon`, `component`, `status`
)
VALUES (
           '支付应用信息查询', 'pay:app:query', 3, 1, @parentId,
           '', '', '', 0
       );
INSERT INTO `sys_menu`(
    `name`, `permission`, `menu_type`, `sort`, `parent_id`,
    `path`, `icon`, `component`, `status`
)
VALUES (
           '支付应用信息创建', 'pay:app:create', 3, 2, @parentId,
           '', '', '', 0
       );
INSERT INTO `sys_menu`(
    `name`, `permission`, `menu_type`, `sort`, `parent_id`,
    `path`, `icon`, `component`, `status`
)
VALUES (
           '支付应用信息更新', 'pay:app:update', 3, 3, @parentId,
           '', '', '', 0
       );
INSERT INTO `sys_menu`(
    `name`, `permission`, `menu_type`, `sort`, `parent_id`,
    `path`, `icon`, `component`, `status`
)
VALUES (
           '支付应用信息删除', 'pay:app:delete', 3, 4, @parentId,
           '', '', '', 0
       );
INSERT INTO `sys_menu`(
    `name`, `permission`, `menu_type`, `sort`, `parent_id`,
    `path`, `icon`, `component`, `status`
)
VALUES (
           '支付应用信息导出', 'pay:app:export', 3, 5, @parentId,
           '', '', '', 0
       );


INSERT INTO `sys_menu` (
    `name`, `permission`, `menu_type`, `sort`, `parent_id`,
    `path`, `icon`, `component`, `status`, `creator`, `create_time`,
    `updater`, `update_time`, `deleted`
) VALUES (
    '秘钥解析', 'pay:channel:parsing', 3, 6, 1129, '', '', '', 0, '1',
    '2021-11-08 15:15:47', '1', '2021-11-08 15:15:47', b'0'
    );
