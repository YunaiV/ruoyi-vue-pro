-- JimuReport PostgreSQL 补丁脚本
-- 用于补充 jimureport.sql 中缺失的表

-- ----------------------------
-- Table structure for jimu_report_category
-- ----------------------------
DROP TABLE IF EXISTS jimu_report_category CASCADE;
CREATE TABLE jimu_report_category (
    id varchar(32) NOT NULL,
    name varchar(100) NOT NULL,
    parent_id varchar(32) NULL DEFAULT NULL,
    iz_leaf int2 NULL DEFAULT NULL,
    source_type varchar(10) NULL DEFAULT NULL,
    create_by varchar(32) NULL DEFAULT NULL,
    create_time timestamp NULL DEFAULT NULL,
    update_by varchar(32) NULL DEFAULT NULL,
    update_time timestamp NULL DEFAULT NULL,
    tenant_id varchar(11) NULL DEFAULT NULL,
    del_flag int2 NULL DEFAULT NULL,
    sort_no int4 NULL DEFAULT NULL,
    CONSTRAINT pk_jimu_report_category PRIMARY KEY (id)
);

COMMENT ON TABLE jimu_report_category IS '分类';
COMMENT ON COLUMN jimu_report_category.id IS '主键';
COMMENT ON COLUMN jimu_report_category.name IS '分类名称';
COMMENT ON COLUMN jimu_report_category.parent_id IS '父级id';
COMMENT ON COLUMN jimu_report_category.iz_leaf IS '是否为叶子节点(0 否 1是)';
COMMENT ON COLUMN jimu_report_category.source_type IS '来源类型( report 积木报表 screen 大屏  drag 仪表盘)';
COMMENT ON COLUMN jimu_report_category.create_by IS '创建人';
COMMENT ON COLUMN jimu_report_category.create_time IS '创建时间';
COMMENT ON COLUMN jimu_report_category.update_by IS '更新人';
COMMENT ON COLUMN jimu_report_category.update_time IS '更新时间';
COMMENT ON COLUMN jimu_report_category.tenant_id IS '租户id';
COMMENT ON COLUMN jimu_report_category.del_flag IS '删除状态(0未删除，1已删除，2临时删除)';
COMMENT ON COLUMN jimu_report_category.sort_no IS '排序';

-- 初始化分类数据
INSERT INTO jimu_report_category VALUES ('984272091947253760', '数据报表', '0', 1, 'report', 'admin', '2024-08-15 11:52:44', 'admin', '2024-12-19 15:11:12', '1', 0, 0);
INSERT INTO jimu_report_category VALUES ('984302991393210368', '打印设计', '0', 1, 'report', 'admin', '2024-08-16 13:55:31', 'admin', '2024-12-19 15:11:16', '1', 0, 1);
INSERT INTO jimu_report_category VALUES ('984302961118724096', '图形报表', '0', 1, 'report', 'admin', '2024-08-16 13:55:24', 'admin', '2024-09-09 14:18:57', '1', 0, 2);
INSERT INTO jimu_report_category VALUES ('1011126161407836160', '填报报表', '0', 1, 'report', 'admin', '2024-10-29 14:21:13', NULL, NULL, '1', 0, 3);
INSERT INTO jimu_report_category VALUES ('988299668956545024', '仪表盘设计', '0', 1, 'drag', 'admin', '2024-08-27 00:00:00', 'admin', '2024-10-31 15:59:47', '1', 0, 0);
INSERT INTO jimu_report_category VALUES ('988299695309357056', '门户设计', '0', 1, 'drag', 'admin', '2024-08-27 00:00:00', 'admin', '2024-08-27 00:00:00', '1', 0, 0);
INSERT INTO jimu_report_category VALUES ('1023810558598676480', '测试大屏', '0', 1, 'screen', 'admin', '2024-12-03 14:24:29', NULL, '2024-12-03 14:24:29', '1', 0, NULL);

-- ----------------------------
-- Table structure for jimu_report_export_job
-- ----------------------------
DROP TABLE IF EXISTS jimu_report_export_job CASCADE;
CREATE TABLE jimu_report_export_job (
    id varchar(32) NOT NULL,
    code varchar(64) NULL DEFAULT NULL,
    name varchar(100) NULL DEFAULT NULL,
    request_url varchar(500) NULL DEFAULT NULL,
    request_param text NULL DEFAULT NULL,
    status int2 NULL DEFAULT NULL,
    cron_expression varchar(200) NULL DEFAULT NULL,
    start_time timestamp NULL DEFAULT NULL,
    complete_time timestamp NULL DEFAULT NULL,
    create_by varchar(50) NULL DEFAULT NULL,
    create_time timestamp NULL DEFAULT NULL,
    update_by varchar(50) NULL DEFAULT NULL,
    update_time timestamp NULL DEFAULT NULL,
    del_flag int2 NULL DEFAULT 0,
    tenant_id varchar(10) NULL DEFAULT NULL,
    remarks varchar(500) NULL DEFAULT NULL,
    CONSTRAINT pk_jimu_report_export_job PRIMARY KEY (id)
);

COMMENT ON TABLE jimu_report_export_job IS '定时导出任务';

-- ----------------------------
-- Table structure for onl_drag_share
-- ----------------------------
DROP TABLE IF EXISTS onl_drag_share CASCADE;
CREATE TABLE onl_drag_share (
    id varchar(32) NOT NULL,
    page_id varchar(255) NULL DEFAULT NULL,
    page_key varchar(255) NULL DEFAULT NULL,
    share_token varchar(255) NULL DEFAULT NULL,
    page_type varchar(10) NULL DEFAULT NULL,
    expire_time timestamp NULL DEFAULT NULL,
    create_by varchar(50) NULL DEFAULT NULL,
    create_time timestamp NULL DEFAULT NULL,
    update_by varchar(50) NULL DEFAULT NULL,
    update_time timestamp NULL DEFAULT NULL,
    del_flag int2 NULL DEFAULT 0,
    tenant_id varchar(10) NULL DEFAULT NULL,
    CONSTRAINT pk_onl_drag_share PRIMARY KEY (id)
);

COMMENT ON TABLE onl_drag_share IS '仪表盘分享表';

-- ----------------------------
-- 示例数据表（可选，用于报表演示）
-- ----------------------------

-- huiyuan_age - 会员年龄分布
DROP TABLE IF EXISTS huiyuan_age CASCADE;
CREATE TABLE huiyuan_age (
    id varchar(36) NOT NULL,
    name varchar(50) NULL DEFAULT NULL,
    value varchar(20) NULL DEFAULT NULL,
    CONSTRAINT pk_huiyuan_age PRIMARY KEY (id)
);

INSERT INTO huiyuan_age VALUES ('1339875613023969282', '25岁以下', '1500');
INSERT INTO huiyuan_age VALUES ('1339875692078211073', '26~30岁', '800');
INSERT INTO huiyuan_age VALUES ('1339875766099288066', '31~35岁', '1200');
INSERT INTO huiyuan_age VALUES ('1339875822382653442', '36~40岁', '1200');
INSERT INTO huiyuan_age VALUES ('1339875892473667586', '41~45岁', '1800');
INSERT INTO huiyuan_age VALUES ('1339875949587505153', '45~50岁', '1800');
INSERT INTO huiyuan_age VALUES ('1339876015857508354', '50岁以上', '2000');

-- huiyuan_sex - 会员性别分布
DROP TABLE IF EXISTS huiyuan_sex CASCADE;
CREATE TABLE huiyuan_sex (
    id varchar(36) NOT NULL,
    name varchar(50) NULL DEFAULT NULL,
    value varchar(20) NULL DEFAULT NULL,
    CONSTRAINT pk_huiyuan_sex PRIMARY KEY (id)
);

INSERT INTO huiyuan_sex VALUES ('1339872878782357506', '女', '8800');
INSERT INTO huiyuan_sex VALUES ('1339872907911798785', '男', '5000');
