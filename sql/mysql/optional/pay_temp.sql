-- ----------------------------
-- 支付-会员钱包表
-- ----------------------------
DROP TABLE IF EXISTS `pay_member_wallet`;
CREATE TABLE `pay_member_wallet`
(
    `id`             bigint   NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id`        bigint   NOT NULL COMMENT '用户 id',
    `balance`        int      NOT NULL DEFAULT 0 COMMENT '余额, 单位分',
    `total_spending` int      NOT NULL DEFAULT 0 COMMENT '累计支出, 单位分',
    `total_top_up`   int      NOT NULL DEFAULT 0 COMMENT '累计充值, 单位分',
    `creator`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
    `create_time`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
    `update_time`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        bit(1)   NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`      bigint   NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='会员钱包表';

-- ----------------------------
-- 支付-会员钱包明细表
-- ----------------------------
DROP TABLE IF EXISTS `pay_member_wallet_transaction`;
CREATE TABLE `pay_member_wallet_transaction`
(
    `id`               bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    `wallet_id`        bigint      NOT NULL COMMENT '会员钱包 id',
    `user_id`          bigint      NOT NULL COMMENT '用户 id',
    `trade_no`         varchar(64) COMMENT '交易单号',
    `category`         tinyint     NOT NULL COMMENT '交易大类',
    `operate_type`     tinyint     NOT NULL COMMENT '操作分类',
    `operate_desc`     varchar(64) NOT NULL COMMENT '操作说明',
    `amount`           int         NOT NULL COMMENT '交易金额, 单位分',
    `balance`          int         NOT NULL COMMENT '余额, 单位分',
    `mark`             varchar(512) COMMENT '备注',
    `transaction_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
    `create_time`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`          varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
    `update_time`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          bit(1)      NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`        bigint      NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='会员钱包明细表';