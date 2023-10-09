-- 交易统计表
CREATE TABLE trade_statistics
(
    id                         bigint AUTO_INCREMENT COMMENT '编号，主键自增'
        PRIMARY KEY,
    time                       datetime                              NOT NULL COMMENT '统计日期',
    order_create_count         int         DEFAULT 0                 NOT NULL COMMENT '创建订单数',
    order_pay_count            int         DEFAULT 0                 NOT NULL COMMENT '支付订单商品数',
    order_pay_price            int         DEFAULT 0                 NOT NULL COMMENT '总支付金额，单位：分',
    order_wallet_pay_price     int         DEFAULT 0                 NOT NULL COMMENT '总支付金额（余额），单位：分',
    after_sale_count           int         DEFAULT 0                 NOT NULL COMMENT '退款订单数',
    after_sale_refund_price    int         DEFAULT 0                 NOT NULL COMMENT '总退款金额，单位：分',
    brokerage_settlement_price int         DEFAULT 0                 NOT NULL COMMENT '佣金金额（已结算），单位：分',
    recharge_pay_count         int         DEFAULT 0                 NOT NULL COMMENT '充值订单数',
    recharge_pay_price         int         DEFAULT 0                 NOT NULL COMMENT '充值金额，单位：分',
    recharge_refund_count      int         DEFAULT 0                 NOT NULL COMMENT '充值退款订单数',
    recharge_refund_price      int         DEFAULT 0                 NOT NULL COMMENT '充值退款金额，单位：分',
    creator                    varchar(64) DEFAULT ''                NULL COMMENT '创建者',
    create_time                datetime    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updater                    varchar(64) DEFAULT ''                NULL COMMENT '更新者',
    update_time                datetime    DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                    bit         DEFAULT b'0'              NOT NULL COMMENT '是否删除',
    tenant_id                  bigint      DEFAULT 0                 NOT NULL COMMENT '租户编号'
)
    COMMENT '交易统计表';

CREATE INDEX trade_statistics_time_index
    ON trade_statistics (time);

-- 菜单
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name)
VALUES ('统计管理', '', 1, 4, 0, '/statistics', 'ep:data-line', '', '');
SELECT @parentId := LAST_INSERT_ID();
-- 交易统计
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name)
VALUES ('交易统计', '', 2, 1, @parentId, 'trade', 'fa-solid:credit-card', 'statistics/trade/index', 'TradeStatistics');
SELECT @parentId := LAST_INSERT_ID();
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('交易统计查询', 'statistics:trade:query', 3, 1, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('交易统计导出', 'statistics:trade:export', 3, 2, @parentId, '', '', '', 0);
-- 会员统计
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name)
VALUES ('会员统计', '', 2, 2, @parentId, 'member', 'ep:avatar', 'statistics/member/index', 'MemberStatistics');
SELECT @parentId := LAST_INSERT_ID();
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('会员统计查询', 'statistics:member:query', 3, 1, @parentId, '', '', '', 0);


