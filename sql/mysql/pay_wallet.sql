-- ----------------------------
-- 会员钱包表
-- ----------------------------
DROP TABLE IF EXISTS `pay_wallet`;
CREATE TABLE `pay_wallet`
(
    `id`                   bigint   NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id`              bigint   NOT NULL COMMENT '用户编号',
    `user_type`            tinyint  NOT NULL DEFAULT 0 COMMENT '用户类型',
    `balance`              int      NOT NULL DEFAULT 0 COMMENT '余额，单位分',
    `total_expense`        int      NOT NULL DEFAULT 0 COMMENT '累计支出，单位分',
    `total_recharge`       int      NOT NULL DEFAULT 0 COMMENT '累计充值，单位分',
    `creator`              varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
    `create_time`          datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`              varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
    `update_time`          datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`              bit(1)   NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`            bigint   NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='会员钱包表';

-- ----------------------------
-- 会员钱包流水表
-- ----------------------------
DROP TABLE IF EXISTS `pay_wallet_transaction`;
CREATE TABLE `pay_wallet_transaction`
(
    `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `wallet_id`        bigint       NOT NULL COMMENT '会员钱包 id',
    `biz_type`         tinyint      NOT NULL COMMENT '关联类型',
    `biz_id`           varchar(64)  NOT NULL COMMENT '关联业务编号',
    `no`               varchar(64)  NOT NULL COMMENT '流水号',
    `title`            varchar(128) NOT NULL COMMENT '流水标题',
    `price`            int          NOT NULL COMMENT '交易金额, 单位分',
    `balance`          int          NOT NULL COMMENT '余额, 单位分',
    `creator`          varchar(64)  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`          varchar(64)  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`        bigint       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='会员钱包流水表';

-- ----------------------------
-- 会员钱包充值
-- ----------------------------
DROP TABLE IF EXISTS `pay_wallet_recharge`;
CREATE TABLE `pay_wallet_recharge`
(
    `id`                            bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    `wallet_id`                     bigint      NOT NULL COMMENT '会员钱包 id',
    `price`                         int         NOT NULL COMMENT '用户实际到账余额，例如充 100 送 20，则该值是 120',
    `pay_price`                     int         NOT NULL COMMENT '实际支付金额',
    `wallet_bonus`                  int         NOT NULL COMMENT '钱包赠送金额',
    `pay_status`                    bit(1)      NOT NULL DEFAULT b'0' COMMENT '是否已支付：[0:未支付 1:已经支付过]',
    `pay_order_id`                  bigint      NULL COMMENT '支付订单编号',
    `pay_channel_code`              varchar(16) NULL COMMENT '支付成功的支付渠道',
    `pay_time`                      datetime    NULL COMMENT '订单支付时间',
    `wallet_transaction_id`         bigint      NULL COMMENT '充值钱包流水',
    `pay_refund_id`                 bigint      NULL COMMENT '支付退款单编号',
    `refund_price`                  int         NOT NULL DEFAULT 0 COMMENT '退款金额，包含赠送金额',
    `refund_pay_price`              int         NOT NULL DEFAULT 0 COMMENT '退款支付金额',
    `refund_wallet_bonus`           int         NOT NULL DEFAULT 0 COMMENT '退款钱包赠送金额',
    `refund_wallet_transaction_id`  bigint      NULL COMMENT '充值退款钱包流水',
    `creator`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
    `create_time`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
    `update_time`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        bit(1)   NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`      bigint   NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='会员钱包充值';

