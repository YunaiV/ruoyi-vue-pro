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
