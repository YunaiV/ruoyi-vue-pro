/*
 Yudao Database Transfer Tool

 Source Server Type    : MySQL
 Source Server Version : 8.2.0
 Source Schema         : ruoyi-vue-pro
 Source File           : pay-2025-05-12.sql

 Target Server Type    : PostgreSQL
 Target Database       : ruoyi-vue-pro

 Date: 2025-11-14

 Description: 支付模块(Pay Module)数据库初始化脚本
 包含14个表: pay_app, pay_channel, pay_demo_order, pay_demo_withdraw, pay_notify_log,
            pay_notify_task, pay_order, pay_order_extension, pay_refund, pay_transfer,
            pay_wallet, pay_wallet_recharge, pay_wallet_recharge_package, pay_wallet_transaction
*/


-- ----------------------------
-- Sequence for pay_app
-- ----------------------------
DROP SEQUENCE IF EXISTS pay_app_seq;
CREATE SEQUENCE pay_app_seq START 1;

-- ----------------------------
-- Table structure for pay_app
-- ----------------------------
DROP TABLE IF EXISTS pay_app;
CREATE TABLE pay_app
(
    id                   int8         NOT NULL,
    app_key              varchar(64)  NOT NULL,
    name                 varchar(64)  NOT NULL,
    status               int2         NOT NULL,
    remark               varchar(255) NULL     DEFAULT NULL,
    order_notify_url     varchar(1024) NOT NULL,
    refund_notify_url    varchar(1024) NOT NULL,
    transfer_notify_url  varchar(1024) NULL     DEFAULT NULL,
    creator              varchar(64)  NULL     DEFAULT '',
    create_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater              varchar(64)  NULL     DEFAULT '',
    update_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted boolean NOT NULL DEFAULT false,
    tenant_id            int8         NOT NULL DEFAULT 0
);

ALTER TABLE pay_app ADD CONSTRAINT pk_pay_app PRIMARY KEY (id);

COMMENT ON COLUMN pay_app.id IS '应用编号';
COMMENT ON COLUMN pay_app.app_key IS '应用标识';
COMMENT ON COLUMN pay_app.name IS '应用名';
COMMENT ON COLUMN pay_app.status IS '开启状态';
COMMENT ON COLUMN pay_app.remark IS '备注';
COMMENT ON COLUMN pay_app.order_notify_url IS '支付结果的回调地址';
COMMENT ON COLUMN pay_app.refund_notify_url IS '退款结果的回调地址';
COMMENT ON COLUMN pay_app.transfer_notify_url IS '转账结果的回调地址';
COMMENT ON COLUMN pay_app.creator IS '创建者';
COMMENT ON COLUMN pay_app.create_time IS '创建时间';
COMMENT ON COLUMN pay_app.updater IS '更新者';
COMMENT ON COLUMN pay_app.update_time IS '更新时间';
COMMENT ON COLUMN pay_app.deleted IS '是否删除';
COMMENT ON COLUMN pay_app.tenant_id IS '租户编号';
COMMENT ON TABLE pay_app IS '支付应用信息';


-- ----------------------------
-- Sequence for pay_channel
-- ----------------------------
DROP SEQUENCE IF EXISTS pay_channel_seq;
CREATE SEQUENCE pay_channel_seq START 1;

-- ----------------------------
-- Table structure for pay_channel
-- ----------------------------
DROP TABLE IF EXISTS pay_channel;
CREATE TABLE pay_channel
(
    id          int8          NOT NULL,
    code        varchar(32)   NOT NULL,
    status      int2          NOT NULL,
    remark      varchar(255)  NULL     DEFAULT NULL,
    fee_rate    float8        NOT NULL DEFAULT 0,
    app_id      int8          NOT NULL,
    config      text          NOT NULL,
    creator     varchar(64)   NULL     DEFAULT '',
    create_time timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64)   NULL     DEFAULT '',
    update_time timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted boolean NOT NULL DEFAULT false,
    tenant_id   int8          NOT NULL DEFAULT 0
);

ALTER TABLE pay_channel ADD CONSTRAINT pk_pay_channel PRIMARY KEY (id);

COMMENT ON COLUMN pay_channel.id IS '商户编号';
COMMENT ON COLUMN pay_channel.code IS '渠道编码';
COMMENT ON COLUMN pay_channel.status IS '开启状态';
COMMENT ON COLUMN pay_channel.remark IS '备注';
COMMENT ON COLUMN pay_channel.fee_rate IS '渠道费率，单位：百分比';
COMMENT ON COLUMN pay_channel.app_id IS '应用编号';
COMMENT ON COLUMN pay_channel.config IS '支付渠道配置';
COMMENT ON COLUMN pay_channel.creator IS '创建者';
COMMENT ON COLUMN pay_channel.create_time IS '创建时间';
COMMENT ON COLUMN pay_channel.updater IS '更新者';
COMMENT ON COLUMN pay_channel.update_time IS '更新时间';
COMMENT ON COLUMN pay_channel.deleted IS '是否删除';
COMMENT ON COLUMN pay_channel.tenant_id IS '租户编号';
COMMENT ON TABLE pay_channel IS '支付渠道';


-- ----------------------------
-- Sequence for pay_demo_order
-- ----------------------------
DROP SEQUENCE IF EXISTS pay_demo_order_seq;
CREATE SEQUENCE pay_demo_order_seq START 1;

-- ----------------------------
-- Table structure for pay_demo_order
-- ----------------------------
DROP TABLE IF EXISTS pay_demo_order;
CREATE TABLE pay_demo_order
(
    id                             int8         NOT NULL,
    user_id                        int8         NOT NULL,
    spu_id                         int8         NOT NULL,
    spu_name                       varchar(255) NOT NULL,
    price                          int4         NOT NULL,
    pay_status                     int2         NOT NULL DEFAULT 0,
    pay_order_id                   int8         NULL     DEFAULT NULL,
    pay_channel_code               varchar(16)  NULL     DEFAULT NULL,
    pay_time                       timestamp    NULL     DEFAULT NULL,
    pay_refund_id                  int8         NULL     DEFAULT NULL,
    refund_price                   int4         NOT NULL DEFAULT 0,
    refund_time                    timestamp    NULL     DEFAULT NULL,
    transfer_channel_package_info  varchar(2048) NULL    DEFAULT NULL,
    creator                        varchar(64)  NULL     DEFAULT '',
    create_time                    timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater                        varchar(64)  NULL     DEFAULT '',
    update_time                    timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted boolean NOT NULL DEFAULT false,
    tenant_id                      int8         NOT NULL DEFAULT 0
);

ALTER TABLE pay_demo_order ADD CONSTRAINT pk_pay_demo_order PRIMARY KEY (id);

COMMENT ON COLUMN pay_demo_order.id IS '订单编号';
COMMENT ON COLUMN pay_demo_order.user_id IS '用户编号';
COMMENT ON COLUMN pay_demo_order.spu_id IS '商品编号';
COMMENT ON COLUMN pay_demo_order.spu_name IS '商品名字';
COMMENT ON COLUMN pay_demo_order.price IS '价格，单位：分';
COMMENT ON COLUMN pay_demo_order.pay_status IS '是否已支付：[0:未支付 1:已经支付过]';
COMMENT ON COLUMN pay_demo_order.pay_order_id IS '支付订单编号';
COMMENT ON COLUMN pay_demo_order.pay_channel_code IS '支付成功的支付渠道';
COMMENT ON COLUMN pay_demo_order.pay_time IS '订单支付时间';
COMMENT ON COLUMN pay_demo_order.pay_refund_id IS '退款订单编号';
COMMENT ON COLUMN pay_demo_order.refund_price IS '退款金额，单位：分';
COMMENT ON COLUMN pay_demo_order.refund_time IS '退款时间';
COMMENT ON COLUMN pay_demo_order.transfer_channel_package_info IS '渠道 package 信息';
COMMENT ON COLUMN pay_demo_order.creator IS '创建者';
COMMENT ON COLUMN pay_demo_order.create_time IS '创建时间';
COMMENT ON COLUMN pay_demo_order.updater IS '更新者';
COMMENT ON COLUMN pay_demo_order.update_time IS '更新时间';
COMMENT ON COLUMN pay_demo_order.deleted IS '是否删除';
COMMENT ON COLUMN pay_demo_order.tenant_id IS '租户编号';
COMMENT ON TABLE pay_demo_order IS '示例订单';


-- ----------------------------
-- Sequence for pay_demo_withdraw
-- ----------------------------
DROP SEQUENCE IF EXISTS pay_demo_withdraw_seq;
CREATE SEQUENCE pay_demo_withdraw_seq START 1;

-- ----------------------------
-- Table structure for pay_demo_withdraw
-- ----------------------------
DROP TABLE IF EXISTS pay_demo_withdraw;
CREATE TABLE pay_demo_withdraw
(
    id                     int8         NOT NULL,
    subject                varchar(32)  NOT NULL,
    price                  int4         NOT NULL,
    user_account           varchar(64)  NOT NULL,
    user_name              varchar(64)  NULL     DEFAULT NULL,
    type                   int2         NOT NULL,
    status                 int2         NOT NULL,
    pay_transfer_id        int8         NULL     DEFAULT NULL,
    transfer_channel_code  varchar(16)  NULL     DEFAULT NULL,
    transfer_time          timestamp    NULL     DEFAULT NULL,
    transfer_error_msg     varchar(4096) NULL    DEFAULT '',
    creator                varchar(64)  NULL     DEFAULT '',
    create_time            timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater                varchar(64)  NULL     DEFAULT '',
    update_time            timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted boolean NOT NULL DEFAULT false,
    tenant_id              int8         NOT NULL DEFAULT 0
);

ALTER TABLE pay_demo_withdraw ADD CONSTRAINT pk_pay_demo_withdraw PRIMARY KEY (id);

COMMENT ON COLUMN pay_demo_withdraw.id IS '提现单编号';
COMMENT ON COLUMN pay_demo_withdraw.subject IS '提现标题';
COMMENT ON COLUMN pay_demo_withdraw.price IS '提现金额，单位：分';
COMMENT ON COLUMN pay_demo_withdraw.user_account IS '收款人账号';
COMMENT ON COLUMN pay_demo_withdraw.user_name IS '收款人姓名';
COMMENT ON COLUMN pay_demo_withdraw.type IS '提现方式';
COMMENT ON COLUMN pay_demo_withdraw.status IS '提现状态';
COMMENT ON COLUMN pay_demo_withdraw.pay_transfer_id IS '转账订单编号';
COMMENT ON COLUMN pay_demo_withdraw.transfer_channel_code IS '转账渠道';
COMMENT ON COLUMN pay_demo_withdraw.transfer_time IS '转账支付时间';
COMMENT ON COLUMN pay_demo_withdraw.transfer_error_msg IS '转账错误提示';
COMMENT ON COLUMN pay_demo_withdraw.creator IS '创建者';
COMMENT ON COLUMN pay_demo_withdraw.create_time IS '创建时间';
COMMENT ON COLUMN pay_demo_withdraw.updater IS '更新者';
COMMENT ON COLUMN pay_demo_withdraw.update_time IS '更新时间';
COMMENT ON COLUMN pay_demo_withdraw.deleted IS '是否删除';
COMMENT ON COLUMN pay_demo_withdraw.tenant_id IS '租户编号';
COMMENT ON TABLE pay_demo_withdraw IS '示例业务提现单';


-- ----------------------------
-- Sequence for pay_notify_log
-- ----------------------------
DROP SEQUENCE IF EXISTS pay_notify_log_seq;
CREATE SEQUENCE pay_notify_log_seq START 1;

-- ----------------------------
-- Table structure for pay_notify_log
-- ----------------------------
DROP TABLE IF EXISTS pay_notify_log;
CREATE TABLE pay_notify_log
(
    id           int8         NOT NULL,
    task_id      int8         NOT NULL,
    notify_times int2         NOT NULL,
    response     varchar(2048) NOT NULL,
    status       int2         NOT NULL,
    creator      varchar(64)  NULL     DEFAULT '',
    create_time  timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater      varchar(64)  NULL     DEFAULT '',
    update_time  timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted boolean NOT NULL DEFAULT false,
    tenant_id    int8         NOT NULL DEFAULT 0
);

ALTER TABLE pay_notify_log ADD CONSTRAINT pk_pay_notify_log PRIMARY KEY (id);

COMMENT ON COLUMN pay_notify_log.id IS '日志编号';
COMMENT ON COLUMN pay_notify_log.task_id IS '通知任务编号';
COMMENT ON COLUMN pay_notify_log.notify_times IS '第几次被通知';
COMMENT ON COLUMN pay_notify_log.response IS '请求参数';
COMMENT ON COLUMN pay_notify_log.status IS '通知状态';
COMMENT ON COLUMN pay_notify_log.creator IS '创建者';
COMMENT ON COLUMN pay_notify_log.create_time IS '创建时间';
COMMENT ON COLUMN pay_notify_log.updater IS '更新者';
COMMENT ON COLUMN pay_notify_log.update_time IS '更新时间';
COMMENT ON COLUMN pay_notify_log.deleted IS '是否删除';
COMMENT ON COLUMN pay_notify_log.tenant_id IS '租户编号';
COMMENT ON TABLE pay_notify_log IS '支付通知 App 的日志';


-- ----------------------------
-- Sequence for pay_notify_task
-- ----------------------------
DROP SEQUENCE IF EXISTS pay_notify_task_seq;
CREATE SEQUENCE pay_notify_task_seq START 1;

-- ----------------------------
-- Table structure for pay_notify_task
-- ----------------------------
DROP TABLE IF EXISTS pay_notify_task;
CREATE TABLE pay_notify_task
(
    id                int8         NOT NULL,
    app_id            int8         NOT NULL,
    type              int2         NOT NULL,
    data_id           int8         NOT NULL,
    status            int2         NOT NULL,
    merchant_order_id varchar(64)  NOT NULL,
    next_notify_time  timestamp    NOT NULL,
    last_execute_time timestamp    NULL     DEFAULT NULL,
    notify_times      int2         NOT NULL DEFAULT 0,
    max_notify_times  int2         NOT NULL,
    notify_url        varchar(1024) NOT NULL,
    creator           varchar(64)  NULL     DEFAULT '',
    create_time       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater           varchar(64)  NULL     DEFAULT '',
    update_time       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted boolean NOT NULL DEFAULT false,
    tenant_id         int8         NOT NULL DEFAULT 0
);

ALTER TABLE pay_notify_task ADD CONSTRAINT pk_pay_notify_task PRIMARY KEY (id);

COMMENT ON COLUMN pay_notify_task.id IS '任务编号';
COMMENT ON COLUMN pay_notify_task.app_id IS '应用编号';
COMMENT ON COLUMN pay_notify_task.type IS '通知类型';
COMMENT ON COLUMN pay_notify_task.data_id IS '数据编号';
COMMENT ON COLUMN pay_notify_task.status IS '通知状态';
COMMENT ON COLUMN pay_notify_task.merchant_order_id IS '商户订单编号';
COMMENT ON COLUMN pay_notify_task.next_notify_time IS '下一次通知时间';
COMMENT ON COLUMN pay_notify_task.last_execute_time IS '最后一次执行时间';
COMMENT ON COLUMN pay_notify_task.notify_times IS '当前通知次数';
COMMENT ON COLUMN pay_notify_task.max_notify_times IS '最大可通知次数';
COMMENT ON COLUMN pay_notify_task.notify_url IS '异步通知地址';
COMMENT ON COLUMN pay_notify_task.creator IS '创建者';
COMMENT ON COLUMN pay_notify_task.create_time IS '创建时间';
COMMENT ON COLUMN pay_notify_task.updater IS '更新者';
COMMENT ON COLUMN pay_notify_task.update_time IS '更新时间';
COMMENT ON COLUMN pay_notify_task.deleted IS '是否删除';
COMMENT ON COLUMN pay_notify_task.tenant_id IS '租户编号';
COMMENT ON TABLE pay_notify_task IS '支付通知任务';


-- ----------------------------
-- Sequence for pay_order
-- ----------------------------
DROP SEQUENCE IF EXISTS pay_order_seq;
CREATE SEQUENCE pay_order_seq START 1;

-- ----------------------------
-- Table structure for pay_order
-- ----------------------------
DROP TABLE IF EXISTS pay_order;
CREATE TABLE pay_order
(
    id                 int8          NOT NULL,
    app_id             int8          NOT NULL,
    channel_id         int8          NULL     DEFAULT NULL,
    channel_code       varchar(32)   NULL     DEFAULT NULL,
    user_id            int8          NULL     DEFAULT NULL,
    user_type          int4          NULL     DEFAULT NULL,
    merchant_order_id  varchar(64)   NOT NULL,
    subject            varchar(32)   NOT NULL,
    body               varchar(128)  NOT NULL,
    notify_url         varchar(1024) NOT NULL,
    price              int8          NOT NULL,
    channel_fee_rate   float8        NULL     DEFAULT 0,
    channel_fee_price  int8          NULL     DEFAULT 0,
    status             int2          NOT NULL,
    user_ip            varchar(50)   NOT NULL,
    expire_time        timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    success_time       timestamp     NULL     DEFAULT CURRENT_TIMESTAMP,
    extension_id       int8          NULL     DEFAULT NULL,
    no                 varchar(64)   NULL     DEFAULT NULL,
    refund_price       int8          NOT NULL,
    channel_user_id    varchar(255)  NULL     DEFAULT NULL,
    channel_order_no   varchar(64)   NULL     DEFAULT NULL,
    creator            varchar(64)   NULL     DEFAULT '',
    create_time        timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater            varchar(64)   NULL     DEFAULT '',
    update_time        timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted boolean NOT NULL DEFAULT false,
    tenant_id          int8          NOT NULL DEFAULT 0
);

ALTER TABLE pay_order ADD CONSTRAINT pk_pay_order PRIMARY KEY (id);

COMMENT ON COLUMN pay_order.id IS '支付订单编号';
COMMENT ON COLUMN pay_order.app_id IS '应用编号';
COMMENT ON COLUMN pay_order.channel_id IS '渠道编号';
COMMENT ON COLUMN pay_order.channel_code IS '渠道编码';
COMMENT ON COLUMN pay_order.user_id IS '用户编号';
COMMENT ON COLUMN pay_order.user_type IS '用户类型';
COMMENT ON COLUMN pay_order.merchant_order_id IS '商户订单编号';
COMMENT ON COLUMN pay_order.subject IS '商品标题';
COMMENT ON COLUMN pay_order.body IS '商品描述';
COMMENT ON COLUMN pay_order.notify_url IS '异步通知地址';
COMMENT ON COLUMN pay_order.price IS '支付金额，单位：分';
COMMENT ON COLUMN pay_order.channel_fee_rate IS '渠道手续费，单位：百分比';
COMMENT ON COLUMN pay_order.channel_fee_price IS '渠道手续金额，单位：分';
COMMENT ON COLUMN pay_order.status IS '支付状态';
COMMENT ON COLUMN pay_order.user_ip IS '用户 IP';
COMMENT ON COLUMN pay_order.expire_time IS '订单失效时间';
COMMENT ON COLUMN pay_order.success_time IS '订单支付成功时间';
COMMENT ON COLUMN pay_order.extension_id IS '支付成功的订单拓展单编号';
COMMENT ON COLUMN pay_order.no IS '支付订单号';
COMMENT ON COLUMN pay_order.refund_price IS '退款总金额，单位：分';
COMMENT ON COLUMN pay_order.channel_user_id IS '渠道用户编号';
COMMENT ON COLUMN pay_order.channel_order_no IS '渠道订单号';
COMMENT ON COLUMN pay_order.creator IS '创建者';
COMMENT ON COLUMN pay_order.create_time IS '创建时间';
COMMENT ON COLUMN pay_order.updater IS '更新者';
COMMENT ON COLUMN pay_order.update_time IS '更新时间';
COMMENT ON COLUMN pay_order.deleted IS '是否删除';
COMMENT ON COLUMN pay_order.tenant_id IS '租户编号';
COMMENT ON TABLE pay_order IS '支付订单';


-- ----------------------------
-- Sequence for pay_order_extension
-- ----------------------------
DROP SEQUENCE IF EXISTS pay_order_extension_seq;
CREATE SEQUENCE pay_order_extension_seq START 1;

-- ----------------------------
-- Table structure for pay_order_extension
-- ----------------------------
DROP TABLE IF EXISTS pay_order_extension;
CREATE TABLE pay_order_extension
(
    id                   int8         NOT NULL,
    no                   varchar(64)  NOT NULL,
    order_id             int8         NOT NULL,
    channel_id           int8         NOT NULL,
    channel_code         varchar(32)  NOT NULL,
    user_ip              varchar(50)  NOT NULL,
    status               int2         NOT NULL,
    channel_extras       text         NULL     DEFAULT NULL,
    channel_error_code   varchar(128) NULL     DEFAULT NULL,
    channel_error_msg    varchar(256) NULL     DEFAULT NULL,
    channel_notify_data  varchar(4096) NULL    DEFAULT NULL,
    creator              varchar(64)  NULL     DEFAULT '',
    create_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater              varchar(64)  NULL     DEFAULT '',
    update_time          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted boolean NOT NULL DEFAULT false,
    tenant_id            int8         NOT NULL DEFAULT 0
);

ALTER TABLE pay_order_extension ADD CONSTRAINT pk_pay_order_extension PRIMARY KEY (id);

COMMENT ON COLUMN pay_order_extension.id IS '支付订单编号';
COMMENT ON COLUMN pay_order_extension.no IS '支付订单号';
COMMENT ON COLUMN pay_order_extension.order_id IS '支付订单编号';
COMMENT ON COLUMN pay_order_extension.channel_id IS '渠道编号';
COMMENT ON COLUMN pay_order_extension.channel_code IS '渠道编码';
COMMENT ON COLUMN pay_order_extension.user_ip IS '用户 IP';
COMMENT ON COLUMN pay_order_extension.status IS '支付状态';
COMMENT ON COLUMN pay_order_extension.channel_extras IS '支付渠道的额外参数';
COMMENT ON COLUMN pay_order_extension.channel_error_code IS '渠道调用报错时，错误码';
COMMENT ON COLUMN pay_order_extension.channel_error_msg IS '渠道调用报错时，错误信息';
COMMENT ON COLUMN pay_order_extension.channel_notify_data IS '支付渠道异步通知的内容';
COMMENT ON COLUMN pay_order_extension.creator IS '创建者';
COMMENT ON COLUMN pay_order_extension.create_time IS '创建时间';
COMMENT ON COLUMN pay_order_extension.updater IS '更新者';
COMMENT ON COLUMN pay_order_extension.update_time IS '更新时间';
COMMENT ON COLUMN pay_order_extension.deleted IS '是否删除';
COMMENT ON COLUMN pay_order_extension.tenant_id IS '租户编号';
COMMENT ON TABLE pay_order_extension IS '支付订单';


-- ----------------------------
-- Sequence for pay_refund
-- ----------------------------
DROP SEQUENCE IF EXISTS pay_refund_seq;
CREATE SEQUENCE pay_refund_seq START 1;

-- ----------------------------
-- Table structure for pay_refund
-- ----------------------------
DROP TABLE IF EXISTS pay_refund;
CREATE TABLE pay_refund
(
    id                   int8          NOT NULL,
    no                   varchar(64)   NOT NULL,
    app_id               int8          NOT NULL,
    channel_id           int8          NOT NULL,
    channel_code         varchar(32)   NOT NULL,
    order_id             int8          NOT NULL,
    order_no             varchar(64)   NOT NULL,
    user_id              int8          NULL     DEFAULT NULL,
    user_type            int4          NULL     DEFAULT NULL,
    merchant_order_id    varchar(64)   NOT NULL,
    merchant_refund_id   varchar(64)   NOT NULL,
    notify_url           varchar(1024) NOT NULL,
    status               int2          NOT NULL,
    pay_price            int8          NOT NULL,
    refund_price         int8          NOT NULL,
    reason               varchar(256)  NOT NULL,
    user_ip              varchar(50)   NULL     DEFAULT NULL,
    channel_order_no     varchar(64)   NOT NULL,
    channel_refund_no    varchar(64)   NULL     DEFAULT NULL,
    success_time         timestamp     NULL     DEFAULT NULL,
    channel_error_code   varchar(128)  NULL     DEFAULT NULL,
    channel_error_msg    varchar(256)  NULL     DEFAULT NULL,
    channel_notify_data  varchar(4096) NULL     DEFAULT NULL,
    creator              varchar(64)   NULL     DEFAULT '',
    create_time          timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater              varchar(64)   NULL     DEFAULT '',
    update_time          timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted boolean NOT NULL DEFAULT false,
    tenant_id            int8          NOT NULL DEFAULT 0
);

ALTER TABLE pay_refund ADD CONSTRAINT pk_pay_refund PRIMARY KEY (id);

COMMENT ON COLUMN pay_refund.id IS '支付退款编号';
COMMENT ON COLUMN pay_refund.no IS '退款单号';
COMMENT ON COLUMN pay_refund.app_id IS '应用编号';
COMMENT ON COLUMN pay_refund.channel_id IS '渠道编号';
COMMENT ON COLUMN pay_refund.channel_code IS '渠道编码';
COMMENT ON COLUMN pay_refund.order_id IS '支付订单编号 pay_order 表id';
COMMENT ON COLUMN pay_refund.order_no IS '支付订单 no';
COMMENT ON COLUMN pay_refund.user_id IS '用户编号';
COMMENT ON COLUMN pay_refund.user_type IS '用户类型';
COMMENT ON COLUMN pay_refund.merchant_order_id IS '商户订单编号（商户系统生成）';
COMMENT ON COLUMN pay_refund.merchant_refund_id IS '商户退款订单号（商户系统生成）';
COMMENT ON COLUMN pay_refund.notify_url IS '异步通知商户地址';
COMMENT ON COLUMN pay_refund.status IS '退款状态';
COMMENT ON COLUMN pay_refund.pay_price IS '支付金额,单位分';
COMMENT ON COLUMN pay_refund.refund_price IS '退款金额,单位分';
COMMENT ON COLUMN pay_refund.reason IS '退款原因';
COMMENT ON COLUMN pay_refund.user_ip IS '用户 IP';
COMMENT ON COLUMN pay_refund.channel_order_no IS '渠道订单号，pay_order 中的 channel_order_no 对应';
COMMENT ON COLUMN pay_refund.channel_refund_no IS '渠道退款单号，渠道返回';
COMMENT ON COLUMN pay_refund.success_time IS '退款成功时间';
COMMENT ON COLUMN pay_refund.channel_error_code IS '渠道调用报错时，错误码';
COMMENT ON COLUMN pay_refund.channel_error_msg IS '渠道调用报错时，错误信息';
COMMENT ON COLUMN pay_refund.channel_notify_data IS '支付渠道异步通知的内容';
COMMENT ON COLUMN pay_refund.creator IS '创建者';
COMMENT ON COLUMN pay_refund.create_time IS '创建时间';
COMMENT ON COLUMN pay_refund.updater IS '更新者';
COMMENT ON COLUMN pay_refund.update_time IS '更新时间';
COMMENT ON COLUMN pay_refund.deleted IS '是否删除';
COMMENT ON COLUMN pay_refund.tenant_id IS '租户编号';
COMMENT ON TABLE pay_refund IS '退款订单';


-- ----------------------------
-- Sequence for pay_transfer
-- ----------------------------
DROP SEQUENCE IF EXISTS pay_transfer_seq;
CREATE SEQUENCE pay_transfer_seq START 1;

-- ----------------------------
-- Table structure for pay_transfer
-- ----------------------------
DROP TABLE IF EXISTS pay_transfer;
CREATE TABLE pay_transfer
(
    id                     int8          NOT NULL,
    no                     varchar(64)   NOT NULL,
    app_id                 int8          NOT NULL,
    channel_id             int8          NOT NULL,
    channel_code           varchar(32)   NOT NULL,
    merchant_transfer_id   varchar(64)   NOT NULL,
    status                 int2          NOT NULL,
    success_time           timestamp     NULL     DEFAULT NULL,
    price                  int4          NOT NULL,
    subject                varchar(512)  NOT NULL,
    user_name              varchar(64)   NULL     DEFAULT NULL,
    user_account           varchar(64)   NOT NULL,
    notify_url             varchar(1024) NOT NULL,
    user_ip                varchar(50)   NOT NULL,
    channel_extras         varchar(512)  NULL     DEFAULT NULL,
    channel_transfer_no    varchar(64)   NULL     DEFAULT NULL,
    channel_error_code     varchar(128)  NULL     DEFAULT NULL,
    channel_error_msg      varchar(256)  NULL     DEFAULT NULL,
    channel_notify_data    varchar(4096) NULL     DEFAULT NULL,
    channel_package_info   varchar(2048) NULL     DEFAULT NULL,
    creator                varchar(64)   NULL     DEFAULT '',
    create_time            timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater                varchar(64)   NULL     DEFAULT '',
    update_time            timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted boolean NOT NULL DEFAULT false,
    tenant_id              int8          NOT NULL DEFAULT 0
);

ALTER TABLE pay_transfer ADD CONSTRAINT pk_pay_transfer PRIMARY KEY (id);

COMMENT ON COLUMN pay_transfer.id IS '编号';
COMMENT ON COLUMN pay_transfer.no IS '转账单号';
COMMENT ON COLUMN pay_transfer.app_id IS '应用编号';
COMMENT ON COLUMN pay_transfer.channel_id IS '转账渠道编号';
COMMENT ON COLUMN pay_transfer.channel_code IS '转账渠道编码';
COMMENT ON COLUMN pay_transfer.merchant_transfer_id IS '商户转账单编号';
COMMENT ON COLUMN pay_transfer.status IS '转账状态';
COMMENT ON COLUMN pay_transfer.success_time IS '转账成功时间';
COMMENT ON COLUMN pay_transfer.price IS '转账金额，单位：分';
COMMENT ON COLUMN pay_transfer.subject IS '转账标题';
COMMENT ON COLUMN pay_transfer.user_name IS '收款人姓名';
COMMENT ON COLUMN pay_transfer.user_account IS '收款人账号';
COMMENT ON COLUMN pay_transfer.notify_url IS '异步通知商户地址';
COMMENT ON COLUMN pay_transfer.user_ip IS '用户 IP';
COMMENT ON COLUMN pay_transfer.channel_extras IS '渠道的额外参数';
COMMENT ON COLUMN pay_transfer.channel_transfer_no IS '渠道转账单号';
COMMENT ON COLUMN pay_transfer.channel_error_code IS '调用渠道的错误码';
COMMENT ON COLUMN pay_transfer.channel_error_msg IS '调用渠道的错误提示';
COMMENT ON COLUMN pay_transfer.channel_notify_data IS '渠道的同步/异步通知的内容';
COMMENT ON COLUMN pay_transfer.channel_package_info IS '渠道 package 信息';
COMMENT ON COLUMN pay_transfer.creator IS '创建者';
COMMENT ON COLUMN pay_transfer.create_time IS '创建时间';
COMMENT ON COLUMN pay_transfer.updater IS '更新者';
COMMENT ON COLUMN pay_transfer.update_time IS '更新时间';
COMMENT ON COLUMN pay_transfer.deleted IS '是否删除';
COMMENT ON COLUMN pay_transfer.tenant_id IS '租户编号';
COMMENT ON TABLE pay_transfer IS '转账单表';


-- ----------------------------
-- Sequence for pay_wallet
-- ----------------------------
DROP SEQUENCE IF EXISTS pay_wallet_seq;
CREATE SEQUENCE pay_wallet_seq START 1;

-- ----------------------------
-- Table structure for pay_wallet
-- ----------------------------
DROP TABLE IF EXISTS pay_wallet;
CREATE TABLE pay_wallet
(
    id             int8        NOT NULL,
    user_id        int8        NOT NULL,
    user_type      int2        NOT NULL DEFAULT 0,
    balance        int4        NOT NULL DEFAULT 0,
    total_expense  int4        NOT NULL DEFAULT 0,
    total_recharge int4        NOT NULL DEFAULT 0,
    freeze_price   int4        NOT NULL DEFAULT 0,
    creator        varchar(64) NULL     DEFAULT '',
    create_time    timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater        varchar(64) NULL     DEFAULT '',
    update_time    timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted boolean NOT NULL DEFAULT false,
    tenant_id      int8        NOT NULL DEFAULT 0
);

ALTER TABLE pay_wallet ADD CONSTRAINT pk_pay_wallet PRIMARY KEY (id);

COMMENT ON COLUMN pay_wallet.id IS '编号';
COMMENT ON COLUMN pay_wallet.user_id IS '用户编号';
COMMENT ON COLUMN pay_wallet.user_type IS '用户类型';
COMMENT ON COLUMN pay_wallet.balance IS '余额，单位分';
COMMENT ON COLUMN pay_wallet.total_expense IS '累计支出，单位分';
COMMENT ON COLUMN pay_wallet.total_recharge IS '累计充值，单位分';
COMMENT ON COLUMN pay_wallet.freeze_price IS '冻结金额，单位分';
COMMENT ON COLUMN pay_wallet.creator IS '创建者';
COMMENT ON COLUMN pay_wallet.create_time IS '创建时间';
COMMENT ON COLUMN pay_wallet.updater IS '更新者';
COMMENT ON COLUMN pay_wallet.update_time IS '更新时间';
COMMENT ON COLUMN pay_wallet.deleted IS '是否删除';
COMMENT ON COLUMN pay_wallet.tenant_id IS '租户编号';
COMMENT ON TABLE pay_wallet IS '会员钱包表';


-- ----------------------------
-- Sequence for pay_wallet_recharge
-- ----------------------------
DROP SEQUENCE IF EXISTS pay_wallet_recharge_seq;
CREATE SEQUENCE pay_wallet_recharge_seq START 1;

-- ----------------------------
-- Table structure for pay_wallet_recharge
-- ----------------------------
DROP TABLE IF EXISTS pay_wallet_recharge;
CREATE TABLE pay_wallet_recharge
(
    id                    int8        NOT NULL,
    wallet_id             int8        NOT NULL,
    total_price           int4        NOT NULL,
    pay_price             int4        NOT NULL,
    bonus_price           int4        NOT NULL,
    package_id            int8        NULL     DEFAULT NULL,
    pay_status            int2        NOT NULL DEFAULT 0,
    pay_order_id          int8        NULL     DEFAULT NULL,
    pay_channel_code      varchar(16) NULL     DEFAULT NULL,
    pay_time              timestamp   NULL     DEFAULT NULL,
    pay_refund_id         int8        NULL     DEFAULT NULL,
    refund_total_price    int4        NOT NULL DEFAULT 0,
    refund_pay_price      int4        NOT NULL DEFAULT 0,
    refund_bonus_price    int4        NOT NULL DEFAULT 0,
    refund_time           timestamp   NULL     DEFAULT NULL,
    refund_status         int4        NOT NULL DEFAULT 0,
    creator               varchar(64) NULL     DEFAULT '',
    create_time           timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater               varchar(64) NULL     DEFAULT '',
    update_time           timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted boolean NOT NULL DEFAULT false,
    tenant_id             int8        NOT NULL DEFAULT 0
);

ALTER TABLE pay_wallet_recharge ADD CONSTRAINT pk_pay_wallet_recharge PRIMARY KEY (id);

COMMENT ON COLUMN pay_wallet_recharge.id IS '编号';
COMMENT ON COLUMN pay_wallet_recharge.wallet_id IS '会员钱包 id';
COMMENT ON COLUMN pay_wallet_recharge.total_price IS '用户实际到账余额，例如充 100 送 20，则该值是 120';
COMMENT ON COLUMN pay_wallet_recharge.pay_price IS '实际支付金额';
COMMENT ON COLUMN pay_wallet_recharge.bonus_price IS '钱包赠送金额';
COMMENT ON COLUMN pay_wallet_recharge.package_id IS '充值套餐编号';
COMMENT ON COLUMN pay_wallet_recharge.pay_status IS '是否已支付：[0:未支付 1:已经支付过]';
COMMENT ON COLUMN pay_wallet_recharge.pay_order_id IS '支付订单编号';
COMMENT ON COLUMN pay_wallet_recharge.pay_channel_code IS '支付成功的支付渠道';
COMMENT ON COLUMN pay_wallet_recharge.pay_time IS '订单支付时间';
COMMENT ON COLUMN pay_wallet_recharge.pay_refund_id IS '支付退款单编号';
COMMENT ON COLUMN pay_wallet_recharge.refund_total_price IS '退款金额，包含赠送金额';
COMMENT ON COLUMN pay_wallet_recharge.refund_pay_price IS '退款支付金额';
COMMENT ON COLUMN pay_wallet_recharge.refund_bonus_price IS '退款钱包赠送金额';
COMMENT ON COLUMN pay_wallet_recharge.refund_time IS '退款时间';
COMMENT ON COLUMN pay_wallet_recharge.refund_status IS '退款状态';
COMMENT ON COLUMN pay_wallet_recharge.creator IS '创建者';
COMMENT ON COLUMN pay_wallet_recharge.create_time IS '创建时间';
COMMENT ON COLUMN pay_wallet_recharge.updater IS '更新者';
COMMENT ON COLUMN pay_wallet_recharge.update_time IS '更新时间';
COMMENT ON COLUMN pay_wallet_recharge.deleted IS '是否删除';
COMMENT ON COLUMN pay_wallet_recharge.tenant_id IS '租户编号';
COMMENT ON TABLE pay_wallet_recharge IS '会员钱包充值';


-- ----------------------------
-- Sequence for pay_wallet_recharge_package
-- ----------------------------
DROP SEQUENCE IF EXISTS pay_wallet_recharge_package_seq;
CREATE SEQUENCE pay_wallet_recharge_package_seq START 1;

-- ----------------------------
-- Table structure for pay_wallet_recharge_package
-- ----------------------------
DROP TABLE IF EXISTS pay_wallet_recharge_package;
CREATE TABLE pay_wallet_recharge_package
(
    id          int8        NOT NULL,
    name        varchar(64) NOT NULL,
    pay_price   int4        NOT NULL,
    bonus_price int4        NOT NULL,
    status      int2        NOT NULL,
    creator     varchar(64) NULL     DEFAULT '',
    create_time timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64) NULL     DEFAULT '',
    update_time timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted boolean NOT NULL DEFAULT false,
    tenant_id   int8        NOT NULL DEFAULT 0
);

ALTER TABLE pay_wallet_recharge_package ADD CONSTRAINT pk_pay_wallet_recharge_package PRIMARY KEY (id);

COMMENT ON COLUMN pay_wallet_recharge_package.id IS '编号';
COMMENT ON COLUMN pay_wallet_recharge_package.name IS '套餐名';
COMMENT ON COLUMN pay_wallet_recharge_package.pay_price IS '支付金额';
COMMENT ON COLUMN pay_wallet_recharge_package.bonus_price IS '赠送金额';
COMMENT ON COLUMN pay_wallet_recharge_package.status IS '状态';
COMMENT ON COLUMN pay_wallet_recharge_package.creator IS '创建者';
COMMENT ON COLUMN pay_wallet_recharge_package.create_time IS '创建时间';
COMMENT ON COLUMN pay_wallet_recharge_package.updater IS '更新者';
COMMENT ON COLUMN pay_wallet_recharge_package.update_time IS '更新时间';
COMMENT ON COLUMN pay_wallet_recharge_package.deleted IS '是否删除';
COMMENT ON COLUMN pay_wallet_recharge_package.tenant_id IS '租户编号';
COMMENT ON TABLE pay_wallet_recharge_package IS '充值套餐表';


-- ----------------------------
-- Sequence for pay_wallet_transaction
-- ----------------------------
DROP SEQUENCE IF EXISTS pay_wallet_transaction_seq;
CREATE SEQUENCE pay_wallet_transaction_seq START 1;

-- ----------------------------
-- Table structure for pay_wallet_transaction
-- ----------------------------
DROP TABLE IF EXISTS pay_wallet_transaction;
CREATE TABLE pay_wallet_transaction
(
    id          int8         NOT NULL,
    wallet_id   int8         NOT NULL,
    biz_type    int2         NOT NULL,
    biz_id      varchar(64)  NOT NULL,
    no          varchar(64)  NOT NULL,
    title       varchar(128) NOT NULL,
    price       int4         NOT NULL,
    balance     int4         NOT NULL,
    creator     varchar(64)  NULL     DEFAULT '',
    create_time timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64)  NULL     DEFAULT '',
    update_time timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted boolean NOT NULL DEFAULT false,
    tenant_id   int8         NOT NULL DEFAULT 0
);

ALTER TABLE pay_wallet_transaction ADD CONSTRAINT pk_pay_wallet_transaction PRIMARY KEY (id);

COMMENT ON COLUMN pay_wallet_transaction.id IS '编号';
COMMENT ON COLUMN pay_wallet_transaction.wallet_id IS '会员钱包 id';
COMMENT ON COLUMN pay_wallet_transaction.biz_type IS '关联类型';
COMMENT ON COLUMN pay_wallet_transaction.biz_id IS '关联业务编号';
COMMENT ON COLUMN pay_wallet_transaction.no IS '流水号';
COMMENT ON COLUMN pay_wallet_transaction.title IS '流水标题';
COMMENT ON COLUMN pay_wallet_transaction.price IS '交易金额, 单位分';
COMMENT ON COLUMN pay_wallet_transaction.balance IS '余额, 单位分';
COMMENT ON COLUMN pay_wallet_transaction.creator IS '创建者';
COMMENT ON COLUMN pay_wallet_transaction.create_time IS '创建时间';
COMMENT ON COLUMN pay_wallet_transaction.updater IS '更新者';
COMMENT ON COLUMN pay_wallet_transaction.update_time IS '更新时间';
COMMENT ON COLUMN pay_wallet_transaction.deleted IS '是否删除';
COMMENT ON COLUMN pay_wallet_transaction.tenant_id IS '租户编号';
COMMENT ON TABLE pay_wallet_transaction IS '会员钱包流水表';
