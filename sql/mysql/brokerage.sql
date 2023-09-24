-- 增加配置表
create table trade_config
(
    id                             bigint auto_increment comment '自增主键' primary key,
    brokerage_enabled              bit           default 1                 not null comment '是否启用分佣',
    brokerage_enabled_condition    tinyint       default 1                 not null comment '分佣模式：1-人人分销 2-指定分销',
    brokerage_bind_mode            tinyint       default 1                 not null comment '分销关系绑定模式: 1-没有推广人，2-新用户, 3-扫码覆盖',
    brokerage_post_urls            varchar(2000) default ''                null comment '分销海报图地址数组',
    brokerage_first_percent        int           default 0                 not null comment '一级返佣比例',
    brokerage_second_percent       int           default 0                 not null comment '二级返佣比例',
    brokerage_withdraw_min_price   int           default 0                 not null comment '用户提现最低金额',
    brokerage_withdraw_fee_percent int           default 0                 not null comment '提现手续费百分比',
    brokerage_bank_names           varchar(200)  default ''                not null comment '提现银行（字典类型=brokerage_bank_name）',
    brokerage_frozen_days          int           default 7                 not null comment '佣金冻结时间(天)',
    brokerage_withdraw_type        varchar(32)   default '1,2,3,4'         not null comment '提现方式：1-钱包；2-银行卡；3-微信；4-支付宝',
    creator                        varchar(64)   default ''                null comment '创建者',
    create_time                    datetime      default CURRENT_TIMESTAMP not null comment '创建时间',
    updater                        varchar(64)   default ''                null comment '更新者',
    update_time                    datetime      default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted                        bit           default b'0'              not null comment '是否删除',
    tenant_id                      bigint        default 0                 not null comment '租户编号'
) comment '交易中心配置';

# alter table trade_config
#     add brokerage_withdraw_fee_percent int default 0 not null comment '提现手续费百分比' after brokerage_withdraw_min_price;

# alter table trade_brokerage_user
#     add level int not null default 1 comment '等级' after frozen_price;
# alter table trade_brokerage_user
#     add path varchar(2000) null comment '路径' after level;


-- 增加分销用户扩展表
create table trade_brokerage_user
(
    id                bigint auto_increment comment '用户编号' primary key,
    bind_user_id      bigint                                null comment '推广员编号',
    bind_user_time    datetime                              null comment '推广员绑定时间',
    brokerage_enabled bit         default 1                 not null comment '是否成为推广员',
    brokerage_time    datetime                              null comment '成为分销员时间',
    price             int         default 0                 not null comment '可用佣金',
    frozen_price      int         default 0                 not null comment '冻结佣金',
    creator           varchar(64) default ''                null comment '创建者',
    create_time       datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updater           varchar(64) default ''                null comment '更新者',
    update_time       datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted           bit         default b'0'              not null comment '是否删除',
    tenant_id         bigint      default 0                 not null comment '租户编号'
)
    comment '分销用户';

create index idx_invite_user_id on trade_brokerage_user (bind_user_id) comment '推广员编号';
create index idx_agent on trade_brokerage_user (brokerage_enabled) comment '是否成为推广员';


create table trade_brokerage_record
(
    id                int auto_increment comment '编号'
        primary key,
    user_id           bigint                                 not null comment '用户编号',
    biz_id            varchar(64)  default ''                not null comment '业务编号',
    biz_type          tinyint      default 0                 not null comment '业务类型：1-订单，2-提现',
    title             varchar(64)  default ''                not null comment '标题',
    price             int          default 0                 not null comment '金额',
    total_price       int          default 0                 not null comment '当前总佣金',
    description       varchar(500) default ''                not null comment '说明',
    status            tinyint      default 0                 not null comment '状态：0-待结算，1-已结算，2-已取消',
    frozen_days       int          default 0                 not null comment '冻结时间（天）',
    unfreeze_time     datetime                               null comment '解冻时间',
    source_user_level int                                    not null comment '来源用户等级',
    source_user_id    bigint                                 not null comment '来源用户编号',
    creator           varchar(64)  default ''                null comment '创建者',
    create_time       datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updater           varchar(64)  default ''                null comment '更新者',
    update_time       datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted           bit          default b'0'              not null comment '是否删除',
    tenant_id         bigint       default 0                 not null comment '租户编号'
)
    comment '佣金记录';

create index idx_user_id on trade_brokerage_record (user_id) comment '用户编号';
create index idx_biz on trade_brokerage_record (biz_type, biz_id) comment '业务';
create index idx_status on trade_brokerage_record (status) comment '状态';


create table trade_brokerage_withdraw
(
    id                  bigint auto_increment comment '编号'
        primary key,
    user_id             bigint                                not null comment '用户编号',
    price               int         default 0                 not null comment '提现金额',
    fee_price           int         default 0                 not null comment '提现手续费',
    total_price         int         default 0                 not null comment '当前总佣金',
    type                tinyint     default 0                 not null comment '提现类型：1-钱包；2-银行卡；3-微信；4-支付宝',
    name                varchar(64)                           null comment '真实姓名',
    account_no          varchar(64)                           null comment '账号',
    bank_name           varchar(100)                          null comment '银行名称',
    bank_address        varchar(200)                          null comment '开户地址',
    account_qr_code_url varchar(512)                          null comment '收款码',
    status              tinyint(2)  default 0                 not null comment '状态：0-审核中，10-审核通过 20-审核不通过；预留：11 - 提现成功；21-提现失败',
    audit_reason        varchar(128)                          null comment '审核驳回原因',
    audit_time          datetime                              null comment '审核时间',
    remark              varchar(500)                          null comment '备注',
    creator             varchar(64) default ''                null comment '创建者',
    create_time         datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updater             varchar(64) default ''                null comment '更新者',
    update_time         datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted             bit         default b'0'              not null comment '是否删除',
    tenant_id           bigint      default 0                 not null comment '租户编号'
)
    comment '佣金提现';

create index idx_user_id on trade_brokerage_withdraw (user_id) comment '用户编号';
create index idx_audit_status on trade_brokerage_withdraw (status) comment '状态';

-- 增加字典
insert into system_dict_type(type, name)
values ('brokerage_enabled_condition', '分佣模式');
insert into system_dict_data(dict_type, label, value, sort, remark)
values ('brokerage_enabled_condition', '人人分销', 1, 1, '所有用户都可以分销'),
       ('brokerage_enabled_condition', '指定分销', 2, 2, '仅可后台手动设置推广员');

insert into system_dict_type(type, name)
values ('brokerage_bind_mode', '分销关系绑定模式');
insert into system_dict_data(dict_type, label, value, sort, remark)
values ('brokerage_bind_mode', '没有推广人', 1, 1, '只要用户没有推广人，随时都可以绑定推广关系'),
       ('brokerage_bind_mode', '新用户', 2, 2, '仅新用户注册时才能绑定推广关系'),
       ('brokerage_bind_mode', '扫码覆盖', 3, 3, '如果用户已经有推广人，推广人会被变更');

insert into system_dict_type(type, name)
values ('brokerage_withdraw_type', '佣金提现类型');
insert into system_dict_data(dict_type, label, value, sort)
values ('brokerage_withdraw_type', '钱包', 1, 1),
       ('brokerage_withdraw_type', '银行卡', 2, 2),
       ('brokerage_withdraw_type', '微信', 3, 3),
       ('brokerage_withdraw_type', '支付宝', 4, 4);

insert into system_dict_type(type, name)
values ('brokerage_record_biz_type', '佣金记录业务类型');
insert into system_dict_data(dict_type, label, value, sort)
values ('brokerage_record_biz_type', '订单返佣', 1, 1),
       ('brokerage_record_biz_type', '申请提现', 2, 2),
       ('brokerage_record_biz_type', '申请提现驳回', 3, 3);

insert into system_dict_type(type, name)
values ('brokerage_record_status', '佣金记录状态');
insert into system_dict_data(dict_type, label, value, sort)
values ('brokerage_record_status', '待结算', 0, 0),
       ('brokerage_record_status', '已结算', 1, 1),
       ('brokerage_record_status', '已取消', 2, 2);

insert into system_dict_type(type, name)
values ('brokerage_withdraw_status', '佣金提现状态');
insert into system_dict_data(dict_type, label, value, sort, color_type)
values ('brokerage_withdraw_status', '审核中', 0, 0, ''),
       ('brokerage_withdraw_status', '审核通过', 10, 10, 'success'),
       ('brokerage_withdraw_status', '提现成功', 11, 11, 'success'),
       ('brokerage_withdraw_status', '审核不通过', 20, 20, 'danger'),
       ('brokerage_withdraw_status', '提现失败', 21, 21, 'danger');

insert into system_dict_type(type, name)
values ('brokerage_bank_name', '佣金提现银行');
insert into system_dict_data(dict_type, label, value, sort)
values ('brokerage_bank_name', '工商银行', 0, 0),
       ('brokerage_bank_name', '建设银行', 1, 1),
       ('brokerage_bank_name', '农业银行', 2, 2),
       ('brokerage_bank_name', '中国银行', 3, 3),
       ('brokerage_bank_name', '交通银行', 4, 4),
       ('brokerage_bank_name', '招商银行', 5, 5);


-- 交易中心配置：菜单 SQL
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('交易中心配置', '', 2, 0, 2072, 'config', 'ep:setting', 'trade/config/index', 0, 'TradeConfig');
-- 按钮父菜单ID
-- 暂时只支持 MySQL。如果你是 Oracle、PostgreSQL、SQLServer 的话，需要手动修改 @parentId 的部分的代码
SELECT @parentId := LAST_INSERT_ID();
-- 按钮 SQL
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('交易中心配置查询', 'trade:config:query', 3, 1, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('交易中心配置保存', 'trade:config:save', 3, 2, @parentId, '', '', '', 0);


-- 增加菜单：分销
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('分销', '', 1, 5, 2072, 'brokerage', 'fa-solid:project-diagram', '', 0, '');
-- 按钮父菜单ID
SELECT @brokerageMenuId := LAST_INSERT_ID();

-- 增加菜单：分销员
-- 菜单 SQL
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('分销用户', '', 2, 0, @brokerageMenuId, 'brokerage-user', 'fa-solid:user-tie', 'trade/brokerage/user/index', 0,
        'TradeBrokerageUser');
-- 按钮父菜单ID
-- 暂时只支持 MySQL。如果你是 Oracle、PostgreSQL、SQLServer 的话，需要手动修改 @parentId 的部分的代码
SELECT @parentId := LAST_INSERT_ID();
-- 按钮 SQL
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('分销用户查询', 'trade:brokerage-user:query', 3, 1, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('分销用户推广人查询', 'trade:brokerage-user:user-query', 3, 2, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('分销用户推广订单查询', 'trade:brokerage-user:order-query', 3, 3, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('分销用户修改推广资格', 'trade:brokerage-user:update-brokerage-enable', 3, 4, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('分销用户修改推广员', 'trade:brokerage-user:update-bind-user', 3, 5, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('分销用户清除推广员', 'trade:brokerage-user:clear-bind-user', 3, 6, @parentId, '', '', '', 0);

-- 增加菜单：佣金记录
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('佣金记录', '', 2, 1, @brokerageMenuId, 'brokerage-record', 'fa:money', 'trade/brokerage/record/index', 0,
        'TradeBrokerageRecord');
-- 按钮父菜单ID
-- 暂时只支持 MySQL。如果你是 Oracle、PostgreSQL、SQLServer 的话，需要手动修改 @parentId 的部分的代码
SELECT @parentId := LAST_INSERT_ID();
-- 按钮 SQL
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('佣金记录查询', 'trade:brokerage-record:query', 3, 1, @parentId, '', '', '', 0);

-- 增加菜单：佣金提现
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('佣金提现', '', 2, 2, @brokerageMenuId, 'brokerage-withdraw', 'fa:credit-card',
        'trade/brokerage/withdraw/index', 0, 'TradeBrokerageWithdraw');
-- 按钮父菜单ID
-- 暂时只支持 MySQL。如果你是 Oracle、PostgreSQL、SQLServer 的话，需要手动修改 @parentId 的部分的代码
SELECT @parentId := LAST_INSERT_ID();
-- 按钮 SQL
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('佣金提现查询', 'trade:brokerage-withdraw:query', 3, 1, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('佣金提现审核', 'trade:brokerage-withdraw:audit', 3, 2, @parentId, '', '', '', 0);

-- 站内信模板
INSERT INTO `ruoyi-vue-pro`.system_notify_template (name, code, nickname, content, type, params, status)
VALUES
    ('佣金提现（审核通过）', 'brokerage_withdraw_audit_approve', 'system', '您在{createTime}提现￥{price}元的申请已通过审核', 2, '["createTime","price"]', 0),
    ('佣金提现（审核不通过）', 'brokerage_withdraw_audit_reject', 'system', '您在{createTime}提现￥{price}元的申请未通过审核，原因：{reason}', 2, '["createTime","price","reason"]', 0);
