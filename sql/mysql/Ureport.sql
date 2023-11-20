-- 菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           'Ureport2报表管理', '', 2, 0, 1281,
           'ureport-file', '', 'report/ureport/index', 0, 'UreportFile'
       );

-- 按钮父菜单ID
-- 暂时只支持 MySQL。如果你是 Oracle、PostgreSQL、SQLServer 的话，需要手动修改 @parentId 的部分的代码
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           'Ureport2报表查询', 'report:ureport-file:query', 3, 1, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           'Ureport2报表创建', 'report:ureport-file:create', 3, 2, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           'Ureport2报表更新', 'report:ureport-file:update', 3, 3, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           'Ureport2报表删除', 'report:ureport-file:delete', 3, 4, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           'Ureport2报表导出', 'report:ureport-file:export', 3, 5, @parentId,
           '', '', '', 0
       );


DROP TABLE IF EXISTS `ureport_file`;
CREATE TABLE `ureport_file`  (
     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
     `file_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件名称',
     `status` tinyint(4) NOT NULL COMMENT '状态',
     `file_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '文件内容',
     `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
     `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
     `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
     PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Ureport2报表' ROW_FORMAT = Dynamic;
