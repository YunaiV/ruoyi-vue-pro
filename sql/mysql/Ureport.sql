-- 菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           'Ureport2报表', '', 2, 0, 1281,
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


-- 创建数据库表
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


-- 插入测试报表
INSERT INTO `ureport_file` (`id`, `file_name`, `status`, `file_content`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES (7, '角色对应的用户列表.ureport.xml', 0, '<?xml version=\"1.0\" encoding=\"UTF-8\"?><ureport><cell expand=\"None\" name=\"A1\" row=\"1\" col=\"1\"><cell-style font-size=\"10\" font-family=\"黑体\" bold=\"true\" align=\"center\" valign=\"middle\"><left-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><right-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><top-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><bottom-border width=\"1\" style=\"solid\" color=\"0,0,0\"/></cell-style><simple-value><![CDATA[角色名称]]></simple-value></cell><cell expand=\"None\" name=\"B1\" row=\"1\" col=\"2\"><cell-style font-size=\"10\" font-family=\"黑体\" bold=\"true\" align=\"center\" valign=\"middle\"><left-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><right-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><top-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><bottom-border width=\"1\" style=\"solid\" color=\"0,0,0\"/></cell-style><simple-value><![CDATA[角色编码]]></simple-value></cell><cell expand=\"None\" name=\"C1\" row=\"1\" col=\"3\"><cell-style font-size=\"10\" font-family=\"黑体\" bold=\"true\" align=\"center\" valign=\"middle\"><left-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><right-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><top-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><bottom-border width=\"1\" style=\"solid\" color=\"0,0,0\"/></cell-style><simple-value><![CDATA[昵称]]></simple-value></cell><cell expand=\"None\" name=\"D1\" row=\"1\" col=\"4\"><cell-style font-size=\"10\" font-family=\"黑体\" bold=\"true\" align=\"center\" valign=\"middle\"><left-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><right-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><top-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><bottom-border width=\"1\" style=\"solid\" color=\"0,0,0\"/></cell-style><simple-value><![CDATA[邮箱]]></simple-value></cell><cell expand=\"None\" name=\"E1\" row=\"1\" col=\"5\"><cell-style font-size=\"9\" forecolor=\"0,0,0\" font-family=\"黑体\" bold=\"true\" align=\"center\" valign=\"middle\"><left-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><right-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><top-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><bottom-border width=\"1\" style=\"solid\" color=\"0,0,0\"/></cell-style><simple-value><![CDATA[手机]]></simple-value></cell><cell expand=\"None\" name=\"F1\" row=\"1\" col=\"6\"><cell-style font-size=\"9\" forecolor=\"0,0,0\" font-family=\"宋体\" align=\"center\" valign=\"middle\"></cell-style><simple-value><![CDATA[]]></simple-value></cell><cell expand=\"Down\" name=\"A2\" row=\"2\" col=\"1\"><cell-style font-size=\"10\" font-family=\"黑体\" align=\"center\" valign=\"middle\"><left-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><right-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><top-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><bottom-border width=\"1\" style=\"solid\" color=\"0,0,0\"/></cell-style><dataset-value dataset-name=\"sys_role\" aggregate=\"group\" property=\"role_name\" order=\"none\" mapping-type=\"simple\"></dataset-value></cell><cell expand=\"Down\" name=\"B2\" row=\"2\" col=\"2\"><cell-style font-size=\"10\" font-family=\"黑体\" align=\"center\" valign=\"middle\"><left-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><right-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><top-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><bottom-border width=\"1\" style=\"solid\" color=\"0,0,0\"/></cell-style><dataset-value dataset-name=\"sys_role\" aggregate=\"group\" property=\"role_code\" order=\"none\" mapping-type=\"simple\"></dataset-value></cell><cell expand=\"Down\" name=\"C2\" row=\"2\" col=\"3\"><cell-style font-size=\"10\" font-family=\"黑体\" align=\"center\" valign=\"middle\"><left-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><right-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><top-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><bottom-border width=\"1\" style=\"solid\" color=\"0,0,0\"/></cell-style><dataset-value dataset-name=\"sys_role\" aggregate=\"group\" property=\"nickname\" order=\"none\" mapping-type=\"simple\"></dataset-value></cell><cell expand=\"Down\" name=\"D2\" row=\"2\" col=\"4\"><cell-style font-size=\"10\" font-family=\"黑体\" align=\"center\" valign=\"middle\"><left-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><right-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><top-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><bottom-border width=\"1\" style=\"solid\" color=\"0,0,0\"/></cell-style><dataset-value dataset-name=\"sys_role\" aggregate=\"group\" property=\"email\" order=\"none\" mapping-type=\"simple\"></dataset-value></cell><cell expand=\"Down\" name=\"E2\" row=\"2\" col=\"5\"><cell-style font-size=\"9\" forecolor=\"0,0,0\" font-family=\"黑体\" align=\"center\" valign=\"middle\"><left-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><right-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><top-border width=\"1\" style=\"solid\" color=\"0,0,0\"/><bottom-border width=\"1\" style=\"solid\" color=\"0,0,0\"/></cell-style><dataset-value dataset-name=\"sys_role\" aggregate=\"group\" property=\"mobile\" order=\"none\" mapping-type=\"simple\"></dataset-value></cell><cell expand=\"None\" name=\"F2\" row=\"2\" col=\"6\"><cell-style font-size=\"9\" forecolor=\"0,0,0\" font-family=\"宋体\" align=\"center\" valign=\"middle\"></cell-style><simple-value><![CDATA[]]></simple-value></cell><cell expand=\"None\" name=\"A3\" row=\"3\" col=\"1\"><cell-style font-size=\"10\" align=\"center\" valign=\"middle\"></cell-style><simple-value><![CDATA[]]></simple-value></cell><cell expand=\"None\" name=\"B3\" row=\"3\" col=\"2\"><cell-style font-size=\"10\" align=\"center\" valign=\"middle\"></cell-style><simple-value><![CDATA[]]></simple-value></cell><cell expand=\"None\" name=\"C3\" row=\"3\" col=\"3\"><cell-style font-size=\"10\" align=\"center\" valign=\"middle\"></cell-style><simple-value><![CDATA[]]></simple-value></cell><cell expand=\"None\" name=\"D3\" row=\"3\" col=\"4\"><cell-style font-size=\"10\" align=\"center\" valign=\"middle\"></cell-style><simple-value><![CDATA[]]></simple-value></cell><cell expand=\"None\" name=\"E3\" row=\"3\" col=\"5\"><cell-style font-size=\"9\" forecolor=\"0,0,0\" font-family=\"宋体\" align=\"center\" valign=\"middle\"></cell-style><simple-value><![CDATA[]]></simple-value></cell><cell expand=\"None\" name=\"F3\" row=\"3\" col=\"6\"><cell-style font-size=\"9\" forecolor=\"0,0,0\" font-family=\"宋体\" align=\"center\" valign=\"middle\"></cell-style><simple-value><![CDATA[]]></simple-value></cell><row row-number=\"1\" height=\"18\"/><row row-number=\"2\" height=\"18\"/><row row-number=\"3\" height=\"18\"/><column col-number=\"1\" width=\"80\"/><column col-number=\"2\" width=\"80\"/><column col-number=\"3\" width=\"80\"/><column col-number=\"4\" width=\"80\"/><column col-number=\"5\" width=\"74\"/><column col-number=\"6\" width=\"74\"/><datasource name=\"UreportDataSource\" type=\"buildin\"><dataset name=\"sys_role\" type=\"sql\"><sql><![CDATA[SELECT sr.name as role_name ,sr.code as role_code,su.username ,su.nickname ,su.email,su.mobile \nFROM system_role sr\nLEFT JOIN system_user_role sur ON sr.id = sur.role_id\nLEFT JOIN system_users su ON sur.user_id = su.id]]></sql><field name=\"role_name\"/><field name=\"role_code\"/><field name=\"username\"/><field name=\"nickname\"/><field name=\"email\"/><field name=\"mobile\"/></dataset></datasource><paper type=\"A4\" left-margin=\"90\" right-margin=\"90\"\n    top-margin=\"72\" bottom-margin=\"72\" paging-mode=\"fitpage\" fixrows=\"0\"\n    width=\"595\" height=\"842\" orientation=\"portrait\" html-report-align=\"left\" bg-image=\"\" html-interval-refresh-value=\"0\" column-enabled=\"false\"></paper></ureport>', NULL, NULL, '2023-11-22 22:02:44', NULL, '2023-11-22 22:06:51', b'0', 0);
