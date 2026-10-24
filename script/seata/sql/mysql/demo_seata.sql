-- ----------------------------
-- 分布式事务示例：2 个独立的演示库，各自包含一张业务表 + Seata AT 模式的 undo_log 表
--
-- 1. yudao_seata_order：订单库，对应 seata_order 数据源（单体：yudao-server；微服务：system-server 发起方）
--    yudao_seata_account：账户库，对应 seata_account 数据源（单体：yudao-server；微服务：infra-server 参与方）
-- 2. 本脚本使用「库名.表名」，可以在同一个 MySQL 实例上直接执行，可重复执行；
--    如果两个库在不同的 MySQL 实例上，则分别执行对应库的语句
-- 3. undo_log 必须和业务表在同一个库中，Seata Server 不需要这张表
-- 4. 不会修改 master 主库，也不会删除已有的库、表、数据
-- ----------------------------

CREATE DATABASE IF NOT EXISTS `yudao_seata_order` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `yudao_seata_account` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ----------------------------
-- 订单库：yudao_seata_order
-- ----------------------------
CREATE TABLE IF NOT EXISTS `yudao_seata_order`.`demo_seata_order` (
  `id` bigint NOT NULL COMMENT '订单编号（由调用方传入，作为幂等主键）',
  `user_id` bigint NOT NULL COMMENT '用户编号',
  `amount` int NOT NULL COMMENT '订单金额，单位：分',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '分布式事务示例订单表';

-- ----------------------------
-- 账户库：yudao_seata_account
-- ----------------------------
CREATE TABLE IF NOT EXISTS `yudao_seata_account`.`demo_seata_account` (
  `id` bigint NOT NULL COMMENT '账户编号',
  `user_id` bigint NOT NULL COMMENT '用户编号',
  `balance` int NOT NULL DEFAULT 0 COMMENT '余额，单位：分',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_user` (`tenant_id`, `user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '分布式事务示例账户表';

-- 演示账户：租户 1、用户 1，初始余额 100 分；已存在时不覆盖（INSERT IGNORE），只影响本演示库
INSERT IGNORE INTO `yudao_seata_account`.`demo_seata_account` (`id`, `user_id`, `balance`, `tenant_id`) VALUES (1, 1, 100, 1);

-- ----------------------------
-- undo_log：AT 模式的回滚日志，两个演示库各一张
--
-- 表结构来自 Apache Seata 2.6.0 的 script/client/at/db/mysql.sql：
-- https://github.com/apache/incubator-seata/blob/v2.6.0/script/client/at/db/mysql.sql
-- Licensed to the Apache Software Foundation (ASF) under the Apache License, Version 2.0
-- （http://www.apache.org/licenses/LICENSE-2.0）。
-- 与官方脚本的差异：官方使用单独的 ALTER TABLE 添加 ix_log_created 索引，这里写入 CREATE TABLE，便于重复执行；字段、索引保持一致。
-- ----------------------------
CREATE TABLE IF NOT EXISTS `yudao_seata_order`.`undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`),
    KEY `ix_log_created` (`log_created`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';

CREATE TABLE IF NOT EXISTS `yudao_seata_account`.`undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`),
    KEY `ix_log_created` (`log_created`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';
