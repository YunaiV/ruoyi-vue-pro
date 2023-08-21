-- 会员表增加字段
alter table member_user add column experience int not null default 0 comment '经验';
alter table member_user add column level_id bigint comment '等级编号';

-- 增加3张表
create table member_level
(
    id             bigint auto_increment comment '编号' primary key,
    name           varchar(30)  default ''                not null comment '等级名称',
    experience     int          default 0                 not null comment '升级经验',
    level          int          default 0                 not null comment '等级',
    discount       tinyint      default 100               not null comment '享受折扣',
    icon           varchar(255) default ''                not null comment '等级图标',
    background_url varchar(255) default ''                not null comment '等级背景图',
    status         tinyint      default 0                 not null comment '状态',
    creator        varchar(64)  default ''                null comment '创建者',
    create_time    datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updater        varchar(64)  default ''                null comment '更新者',
    update_time    datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted        bit          default b'0'              not null comment '是否删除',
    tenant_id      bigint       default 0                 not null comment '租户编号'
)
    comment '会员等级';

create table member_level_log
(
    id              bigint auto_increment comment '编号' primary key,
    user_id         bigint       default 0                 not null comment '用户编号',
    level_id        bigint       default 0                 not null comment '等级编号',
    level           int          default 0                 not null comment '会员等级',
    discount        tinyint      default 100               not null comment '享受折扣',
    experience      int          default 0                 not null comment '升级经验',
    user_experience int          default 0                 not null comment '会员此时的经验',
    remark          varchar(255) default ''                not null comment '备注',
    description     varchar(255) default ''                not null comment '描述',
    creator         varchar(64)  default ''                null comment '创建者',
    create_time     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updater         varchar(64)  default ''                null comment '更新者',
    update_time     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted         bit          default b'0'              not null comment '是否删除',
    tenant_id       bigint       default 0                 not null comment '租户编号'
)
    comment '会员等级记录';

create index idx_user_id on member_level_log (user_id) comment '会员等级记录-用户编号';

create table member_experience_log
(
    id               bigint auto_increment comment '编号' primary key,
    user_id          bigint       default 0                 not null comment '用户编号',
    biz_id           varchar(64)  default ''                not null comment '业务编号',
    biz_type         tinyint      default 0                 not null comment '业务类型',
    title            varchar(30)  default ''                not null comment '标题',
    experience       int          default 0                 not null comment '经验',
    total_experience int          default 0                 not null comment '变更后的经验',
    description      varchar(512) default ''                not null comment '描述',
    creator          varchar(64)  default ''                null comment '创建者',
    create_time      datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updater          varchar(64)  default ''                null comment '更新者',
    update_time      datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted          bit          default b'0'              not null comment '是否删除',
    tenant_id        bigint       default 0                 not null comment '租户编号'
)
    comment '会员经验记录';

create index idx_user_id on member_experience_log (user_id) comment '会员经验记录-用户编号';
create index idx_user_biz_type on member_experience_log (user_id, biz_type) comment '会员经验记录-用户业务类型';

-- 增加字典
insert system_dict_type(name, type) values ('会员经验业务类型', 'member_experience_biz_type');
insert system_dict_data(dict_type, label, value, sort) values ('member_experience_biz_type', '管理员调整', '0', 0);
insert system_dict_data(dict_type, label, value, sort) values ('member_experience_biz_type', '邀新奖励', '1', 1);
insert system_dict_data(dict_type, label, value, sort) values ('member_experience_biz_type', '下单奖励', '2', 2);
insert system_dict_data(dict_type, label, value, sort) values ('member_experience_biz_type', '退单扣除', '3', 3);
insert system_dict_data(dict_type, label, value, sort) values ('member_experience_biz_type', '签到奖励', '4', 4);
insert system_dict_data(dict_type, label, value, sort) values ('member_experience_biz_type', '抽奖奖励', '5', 5);

-- 菜单 SQL
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('会员等级', '', 2, 3, 2262, 'level', '', 'member/level/index', 0, 'MemberLevel');

-- 按钮父菜单ID
-- 暂时只支持 MySQL。如果你是 Oracle、PostgreSQL、SQLServer 的话，需要手动修改 @parentId 的部分的代码
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('会员等级查询', 'member:level:query', 3, 1, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('会员等级创建', 'member:level:create', 3, 2, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('会员等级更新', 'member:level:update', 3, 3, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('会员等级删除', 'member:level:delete', 3, 4, @parentId, '', '', '', 0);
