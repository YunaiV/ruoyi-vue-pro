-- ============================================================
-- B2B 智能电商升级 — 数据库迁移脚本
-- 模块：product_share（安全分享记录表）
-- 作者：deepay
-- 说明：支持 SecureSharingService + IntelligentSharingSystem
-- ============================================================

CREATE TABLE IF NOT EXISTS `product_share`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',

    -- 分享标识
    `token`         varchar(64)  NOT NULL COMMENT '唯一分享令牌（UUID，32 位十六进制）',

    -- 资源信息
    `resource_type` varchar(32)  NOT NULL COMMENT '资源类型：product / order / contract / blockchain_proof',
    `resource_id`   varchar(64)  NOT NULL COMMENT '资源编号（如商品 SPU 编号 或 合同编号）',

    -- 创建者
    `user_id`       bigint       NOT NULL COMMENT '创建分享的用户编号',

    -- 平台与权限
    `platform`      varchar(32)  DEFAULT 'general' COMMENT '目标平台：wechat / weibo / general',
    `permissions`   json         DEFAULT NULL COMMENT '权限配置（JSON）：view/download/sign/verify/maxViews/watermark/passwordProtected/expiresIn',

    -- 访问控制
    `view_count`    int          NOT NULL DEFAULT 0 COMMENT '已查看次数',
    `max_views`     int          NOT NULL DEFAULT 10 COMMENT '最大查看次数（0=不限）',
    `password_hash` varchar(128) DEFAULT NULL COMMENT '访问密码 SHA-256 哈希（null 表示无密码）',
    `watermark`     tinyint(1)   NOT NULL DEFAULT 1 COMMENT '是否加水印（1=是 0=否）',
    `expires_at`    datetime     NOT NULL COMMENT '过期时间',
    `status`        tinyint      NOT NULL DEFAULT 1 COMMENT '状态：1=活跃 0=禁用',

    -- 访问追踪
    `tracking_data` json         DEFAULT NULL COMMENT '访问记录列表（JSON 数组：ip/userAgent/accessTime）',

    -- 公共字段
    `creator`       varchar(64)  DEFAULT '' COMMENT '创建者',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`       varchar(64)  DEFAULT '' COMMENT '更新者',
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除（逻辑删除）',
    `tenant_id`     bigint       NOT NULL DEFAULT 0 COMMENT '租户编号',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token` (`token`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_resource` (`resource_type`, `resource_id`),
    KEY `idx_expires_at` (`expires_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '商品/订单安全分享记录';
