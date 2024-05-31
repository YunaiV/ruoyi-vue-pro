DROP TABLE IF EXISTS `promotion_kefu_conversation`;
CREATE TABLE `promotion_kefu_conversation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` BIGINT NOT NULL COMMENT '会话所属用户',
    `last_message_time` DATETIME NOT NULL COMMENT '最后聊天时间',
    `last_message_content` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '最后聊天内容',
    `last_message_content_type` INT NOT NULL COMMENT '最后发送的消息类型',
    `admin_pinned` BIT(1) NOT NULL DEFAULT b'0'  COMMENT '管理端置顶',
    `user_deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '用户是否可见',
    `admin_deleted` BIT(1) NOT NULL DEFAULT b'0'  COMMENT '管理员是否可见',
    `admin_unread_message_count` INT NOT NULL COMMENT '管理员未读消息数',
    `creator` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '客服会话' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `promotion_kefu_message`;
CREATE TABLE `promotion_kefu_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
    `conversation_id` BIGINT NOT NULL COMMENT '会话编号',
    `sender_id` BIGINT NOT NULL COMMENT '发送人编号',
    `sender_type` INT NOT NULL COMMENT '发送人类型',
    `receiver_id` BIGINT NOT NULL COMMENT '接收人编号',
    `receiver_type` INT NOT NULL COMMENT '接收人类型',
    `content_type` INT NOT NULL COMMENT '消息类型',
    `content` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息',
    `read_status` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否已读',
    `creator` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '客服消息' ROW_FORMAT = Dynamic;
