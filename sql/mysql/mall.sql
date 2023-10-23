INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name)
VALUES ('核销订单', '', 2, 2, 2166, 'pick-up-order', 'ep:list', 'mall/trade/delivery/pickUpOrder/index', 'PickUpOrder');

CREATE TABLE promotion_diy_template
(
    id                 bigint AUTO_INCREMENT COMMENT '装修模板编号'
        PRIMARY KEY,
    name               varchar(100)                          NOT NULL COMMENT '模板名称',
    used               bit         DEFAULT b'0'              NOT NULL COMMENT '是否使用',
    used_time          datetime                              NULL COMMENT '使用时间',
    remark             varchar(255)                          NULL COMMENT '备注',
    preview_image_urls varchar(2000)                         NULL COMMENT '预览图，多个逗号分隔',
    property           varchar(2000)                         NULL COMMENT '页面属性，JSON 格式',
    creator            varchar(64) DEFAULT ''                NULL COMMENT '创建者',
    create_time        datetime    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updater            varchar(64) DEFAULT ''                NULL COMMENT '更新者',
    update_time        datetime    DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted            bit         DEFAULT b'0'              NOT NULL COMMENT '是否删除',
    tenant_id          bigint      DEFAULT 0                 NOT NULL COMMENT '租户编号'
)
    COMMENT '装修模板';

-- 菜单 SQL
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('装修模板', '', 2, 20, 2030, 'diy-template', 'fa6-solid:brush', 'mall/promotion/diy/template/index', 0, 'DiyTemplate');

-- 按钮父菜单ID
-- 暂时只支持 MySQL。如果你是 Oracle、PostgreSQL、SQLServer 的话，需要手动修改 @parentId 的部分的代码
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('装修模板查询', 'promotion:diy-template:query', 3, 1, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('装修模板创建', 'promotion:diy-template:create', 3, 2, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('装修模板更新', 'promotion:diy-template:update', 3, 3, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('装修模板删除', 'promotion:diy-template:delete', 3, 4, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('装修模板使用', 'promotion:diy-template:use', 3, 5, @parentId, '', '', '', 0);;



CREATE TABLE promotion_diy_page
(
    id                 bigint AUTO_INCREMENT COMMENT '装修页面编号'
        PRIMARY KEY,
    template_id        bigint                                NULL COMMENT '装修模板编号',
    name               varchar(100)                          NOT NULL COMMENT '页面名称',
    remark             varchar(255)                          NULL COMMENT '备注',
    preview_image_urls varchar(2000)                         NULL COMMENT '预览图，多个逗号分隔',
    property           varchar(2000)                         NULL COMMENT '页面属性，JSON 格式',
    creator            varchar(64) DEFAULT ''                NULL COMMENT '创建者',
    create_time        datetime    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updater            varchar(64) DEFAULT ''                NULL COMMENT '更新者',
    update_time        datetime    DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted            bit         DEFAULT b'0'              NOT NULL COMMENT '是否删除',
    tenant_id          bigint      DEFAULT 0                 NOT NULL COMMENT '租户编号'
)
    COMMENT '装修页面';

CREATE INDEX idx_template_id ON promotion_diy_page (template_id);

-- 菜单 SQL
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('装修页面', '', 2, 21, 2030, 'diy-page', 'foundation:page-edit', 'mall/promotion/diy/page/index', 0, 'DiyPage');

-- 按钮父菜单ID
-- 暂时只支持 MySQL。如果你是 Oracle、PostgreSQL、SQLServer 的话，需要手动修改 @parentId 的部分的代码
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('装修页面查询', 'promotion:diy-page:query', 3, 1, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('装修页面创建', 'promotion:diy-page:create', 3, 2, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('装修页面更新', 'promotion:diy-page:update', 3, 3, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('装修页面删除', 'promotion:diy-page:delete', 3, 4, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('装修页面导出', 'promotion:diy-page:export', 3, 5, @parentId, '', '', '', 0);