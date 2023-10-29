-- ----------------------------
-- 合同菜单
-- ----------------------------

-- 菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
    '合同管理', '', 2, 0, 2375,
    'contract', '', 'crm/contract/index', 0, 'Contract'
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
    '合同查询', 'crm:contract:query', 3, 1, @parentId,
    '', '', '', 0
);
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
    '合同创建', 'crm:contract:create', 3, 2, @parentId,
    '', '', '', 0
);
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
    '合同更新', 'crm:contract:update', 3, 3, @parentId,
    '', '', '', 0
);
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
    '合同删除', 'crm:contract:delete', 3, 4, @parentId,
    '', '', '', 0
);
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
    '合同导出', 'crm:contract:export', 3, 5, @parentId,
    '', '', '', 0
);

-- ----------------------------
-- 线索菜单
-- ----------------------------

-- 菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
   '线索管理', '', 2, 0, 2375,
   'clue', '', 'crm/clue/index', 0, 'CrmClue'
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
   '线索查询', 'crm:clue:query', 3, 1, @parentId,
   '', '', '', 0
);
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
   '线索创建', 'crm:clue:create', 3, 2, @parentId,
   '', '', '', 0
);
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
   '线索更新', 'crm:clue:update', 3, 3, @parentId,
   '', '', '', 0
);
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
   '线索删除', 'crm:clue:delete', 3, 4, @parentId,
   '', '', '', 0
);
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
   '线索导出', 'crm:clue:export', 3, 5, @parentId,
   '', '', '', 0
);
-- 菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '联系人', '', 2, 0, ${table.parentMenuId},
           'contact', '', 'crm/contact/index', 0, 'Contact'
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
           '联系人查询', 'crm:contact:query', 3, 1, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '联系人创建', 'crm:contact:create', 3, 2, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '联系人更新', 'crm:contact:update', 3, 3, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '联系人删除', 'crm:contact:delete', 3, 4, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '联系人导出', 'crm:contact:export', 3, 5, @parentId,
           '', '', '', 0
       );

-- ----------------------------
-- 合同菜单
-- ----------------------------

-- 菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '商机管理', '', 2, 0, '',
           'business', '', 'crm/business/index', 0, 'CrmBusiness'
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
           '商机查询', 'crm:business:query', 3, 1, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '商机创建', 'crm:business:create', 3, 2, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '商机更新', 'crm:business:update', 3, 3, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '商机删除', 'crm:business:delete', 3, 4, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '商机导出', 'crm:business:export', 3, 5, @parentId,
           '', '', '', 0
       );

-- ----------------------------
-- 回款菜单
-- ----------------------------

-- 菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '回款管理', '', 2, 0, 2375,
           'receivable', '', 'crm/receivable/index', 0, 'Receivable'
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
           '回款管理查询', 'crm:receivable:query', 3, 1, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '回款管理创建', 'crm:receivable:create', 3, 2, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '回款管理更新', 'crm:receivable:update', 3, 3, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '回款管理删除', 'crm:receivable:delete', 3, 4, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '回款管理导出', 'crm:receivable:export', 3, 5, @parentId,
           '', '', '', 0
       );


-- ----------------------------
-- 回款计划菜单
-- ----------------------------

-- 菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '回款计划管理', '', 2, 0, 2375,
           'receivable-plan', '', 'crm/receivablePlan/index', 0, 'ReceivablePlan'
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
           '回款计划查询', 'crm:receivable-plan:query', 3, 1, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '回款计划创建', 'crm:receivable-plan:create', 3, 2, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '回款计划更新', 'crm:receivable-plan:update', 3, 3, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '回款计划删除', 'crm:receivable-plan:delete', 3, 4, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '回款计划导出', 'crm:receivable-plan:export', 3, 5, @parentId,
           '', '', '', 0
       );
