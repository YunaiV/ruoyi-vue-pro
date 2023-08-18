create table member_tag
(
    id          bigint auto_increment comment '编号'
        primary key,
    name        varchar(30) default ''                not null comment '标签名称',
    creator     varchar(64) default ''                null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updater     varchar(64) default ''                null comment '更新者',
    update_time datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     bit         default b'0'              not null comment '是否删除',
    tenant_id   bigint      default 0                 not null comment '租户编号'
)
    comment '会员标签';


-- 菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
    '会员标签管理', '', 2, 3, 2262,
    'tag', '', 'member/tag/index', 0, 'MemberTag'
);

-- 按钮父菜单ID
-- 暂时只支持 MySQL。如果你是 Oracle、PostgreSQL、SQLServer 的话，需要手动修改 @parentId 的部分的代码
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
    '会员标签查询', 'member:tag:query', 3, 1, @parentId,
    '', '', '', 0
);
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
    '会员标签创建', 'member:tag:create', 3, 2, @parentId,
    '', '', '', 0
);
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
    '会员标签更新', 'member:tag:update', 3, 3, @parentId,
    '', '', '', 0
);
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
    '会员标签删除', 'member:tag:delete', 3, 4, @parentId,
    '', '', '', 0
);
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
    '会员标签导出', 'member:tag:export', 3, 5, @parentId,
    '', '', '', 0
);
