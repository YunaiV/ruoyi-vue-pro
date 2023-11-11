-- ----------------------------
-- 客户公海配置
-- ----------------------------
-- 菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
   '客户公海配置', '', 2, 0, 2397,
   'customer-pool-config', 'ep:data-analysis', 'crm/customerPoolConf/index', 0, 'CustomerPoolConf'
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
   '客户公海配置保存', 'crm:customer-pool-config:update', 3, 1, @parentId,
   '', '', '', 0
);




-- ----------------------------
-- 客户限制配置管理
-- ----------------------------
-- 菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
   '客户限制配置管理', '', 2, 0, 2397,
   'customer-limit-config', '', 'crm/customerLimitConfig/index', 0, 'CrmCustomerLimitConfig'
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
   '客户限制配置查询', 'crm:customer-limit-config:query', 3, 1, @parentId,
   '', '', '', 0
);
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
   '客户限制配置创建', 'crm:customer-limit-config:create', 3, 2, @parentId,
   '', '', '', 0
);
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
   '客户限制配置更新', 'crm:customer-limit-config:update', 3, 3, @parentId,
   '', '', '', 0
);
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
   '客户限制配置删除', 'crm:customer-limit-config:delete', 3, 4, @parentId,
   '', '', '', 0
);
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
   '客户限制配置导出', 'crm:customer-limit-config:export', 3, 5, @parentId,
   '', '', '', 0
);
