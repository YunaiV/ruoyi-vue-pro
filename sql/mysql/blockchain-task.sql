-- 区块链异步存证任务表
-- 对应 BlockchainTaskDO，隶属于 yudao-module-pay 模块

CREATE TABLE `pay_blockchain_task` (
    `id`                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id`              VARCHAR(128) NOT NULL COMMENT '任务唯一 ID（TASK_{orderId}_{ts} / DEAD_{...}）',
    `order_id`             VARCHAR(64)  NOT NULL COMMENT '关联订单号',
    `user_id`              VARCHAR(64)           COMMENT '用户编号',
    `amount`               DECIMAL(12, 2)        COMMENT '支付金额',
    `currency`             VARCHAR(8)            COMMENT '货币代码，如 CNY / EUR',
    `data_hash`            VARCHAR(64)           COMMENT '原始数据 SHA-256 哈希',
    `tx_hash`              VARCHAR(256)          COMMENT '链上或本地交易哈希',
    `public_chain_tx_hash` VARCHAR(256)          COMMENT '公链批量上传交易哈希',
    `chain_type`           VARCHAR(32)           COMMENT '链类型：JD_CHAIN / LOCAL_HASH',
    `status`               VARCHAR(32)  NOT NULL COMMENT '任务状态：PROCESSING / SUCCESS / FAILED / DEAD_LETTER',
    `error_msg`            VARCHAR(512)          COMMENT '错误信息（失败时记录）',
    `retry_count`          INT          NOT NULL DEFAULT 0 COMMENT '已重试次数',
    `batch_uploaded`       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已批量上传到公链',
    `batch_upload_time`    DATETIME              COMMENT '批量上传时间',
    `created_at`           DATETIME              COMMENT '任务创建时间',
    `completed_at`         DATETIME              COMMENT '任务完成时间',
    -- BaseDO 公共字段
    `creator`              VARCHAR(64)           DEFAULT '' COMMENT '创建者',
    `create_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`              VARCHAR(64)           DEFAULT '' COMMENT '更新者',
    `update_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`              TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否删除（0=否 1=是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_id` (`task_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '区块链存证任务表';
