-- IoT 设备影子表
CREATE TABLE IF NOT EXISTS `iot_device_shadow`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `device_id`   bigint       NOT NULL COMMENT '设备编号',
    `product_id`  bigint       NOT NULL COMMENT '产品编号',
    `product_key` varchar(64)  NOT NULL COMMENT '产品标识',
    `device_name` varchar(255) NOT NULL COMMENT '设备名称',
    `reported`    json                  DEFAULT NULL COMMENT '上报状态',
    `desired`     json                  DEFAULT NULL COMMENT '期望状态',
    `metadata`    json                  DEFAULT NULL COMMENT '元数据',
    `version`     bigint       NOT NULL DEFAULT 0 COMMENT '版本号',
    `shadow_time` bigint       NOT NULL DEFAULT 0 COMMENT '文档最后更新时间',
    `status`      tinyint      NOT NULL DEFAULT 0 COMMENT '状态',
    `creator`     varchar(64)           DEFAULT '' COMMENT '创建者',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`     varchar(64)           DEFAULT '' COMMENT '更新者',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`   bigint       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_id` (`device_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='IoT 设备影子';

-- 菜单权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
                           `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`,
                           `updater`, `update_time`, `deleted`)
VALUES (4056, '设备影子查询', 'iot:device:shadow-query', 3, 13, 4008, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(),
        '1', NOW(), b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
                           `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`,
                           `updater`, `update_time`, `deleted`)
VALUES (4057, '设备影子更新', 'iot:device:shadow-update', 3, 14, 4008, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(),
        '1', NOW(), b'0');
