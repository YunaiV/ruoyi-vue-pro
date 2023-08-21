create table member_group
(
    id          bigint auto_increment comment '编号' primary key,
    name        varchar(30)  default ''                not null comment '名称',
    remark      varchar(255) default ''                not null comment '备注',
    status      tinyint      default 0                 not null comment '状态',
    creator     varchar(64)  default ''                null comment '创建者',
    create_time datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updater     varchar(64)  default ''                null comment '更新者',
    update_time datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     bit          default b'0'              not null comment '是否删除',
    tenant_id   bigint       default 0                 not null comment '租户编号'
)
    comment '用户分组';

alter table member_user add column group_id bigint null comment '用户分组编号';

-- 菜单 SQL
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('用户分组', '', 2, 5, 2262, 'group', '', 'member/group/index', 0, 'MemberGroup');

-- 按钮父菜单ID
-- 暂时只支持 MySQL。如果你是 Oracle、PostgreSQL、SQLServer 的话，需要手动修改 @parentId 的部分的代码
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('用户分组查询', 'member:group:query', 3, 1, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('用户分组创建', 'member:group:create', 3, 2, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('用户分组更新', 'member:group:update', 3, 3, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('用户分组删除', 'member:group:delete', 3, 4, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('用户分组导出', 'member:group:export', 3, 5, @parentId, '', '', '', 0);
