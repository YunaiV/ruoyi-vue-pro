-- ----------------------------
-- 会员钱包表
-- ----------------------------
DROP TABLE IF EXISTS `pay_wallet`;
CREATE TABLE `pay_wallet`
(
    `id`             bigint   NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id`        bigint   NOT NULL COMMENT '用户编号',
    `user_type`      tinyint  NOT NULL DEFAULT 0 COMMENT '用户类型',
    `balance`        int      NOT NULL DEFAULT 0 COMMENT '余额，单位分',
    `total_expense`  int      NOT NULL DEFAULT 0 COMMENT '累计支出，单位分',
    `total_recharge` int      NOT NULL DEFAULT 0 COMMENT '累计充值，单位分',
    `creator`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
    `create_time`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
    `update_time`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        bit(1)   NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`      bigint   NOT NULL DEFAULT 0 COMMENT '租户编号',
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
