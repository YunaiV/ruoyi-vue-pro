DROP TABLE IF EXISTS `promotion_kefu_conversation`;
CREATE TABLE `promotion_kefu_conversation` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` bigint NOT NULL COMMENT '会话所属用户',
    `last_message_time` datetime NOT NULL COMMENT '最后聊天时间',
    `last_message_content` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '最后聊天内容',
    `last_message_content_type` int NOT NULL COMMENT '最后发送的消息类型',
    `admin_pinned` bit(1) NOT NULL DEFAULT b'0' COMMENT '管理端置顶',
    `user_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '用户是否可见',
    `admin_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '管理员是否可见',
    `admin_unread_message_count` int NOT NULL COMMENT '管理员未读消息数',
    `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='客服会话';

DROP TABLE IF EXISTS `promotion_kefu_message`;
CREATE TABLE `promotion_kefu_message` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `conversation_id` bigint NOT NULL COMMENT '会话编号',
    `sender_id` bigint NOT NULL COMMENT '发送人编号',
    `sender_type` int NOT NULL COMMENT '发送人类型',
    `receiver_id` bigint DEFAULT NULL COMMENT '接收人编号',
    `receiver_type` int DEFAULT NULL COMMENT '接收人类型',
    `content_type` int NOT NULL COMMENT '消息类型',
    `content` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息',
    `read_status` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否已读',
    `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='客服消息';