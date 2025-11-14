/*
 Navicat Premium Data Transfer

 Source Server         : mysql5.7
 Source Server Type    : MySQL
 Source Server Version : 50738
 Source Host           : 127.0.0.1:3306
 Source Schema         : jimureport

 Target Server Type    : PostgreSQL
 Target Server Version : 16
 File Encoding         : 65001

 Date: 08/07/2024 22:09:05
*/

-- PostgreSQL uses UTF8 by default
-- PostgreSQL does not need FOREIGN_KEY_CHECKS

-- ----------------------------
-- Table structure for jimu_dict
-- ----------------------------
DROP TABLE IF EXISTS "jimu_dict" CASCADE;
CREATE TABLE "jimu_dict" (
  "id" varchar(32) NOT NULL,
  "dict_name" varchar(100) NULL DEFAULT NULL,
  "dict_code" varchar(100) NULL DEFAULT NULL,
  "description" varchar(255) NULL DEFAULT NULL,
  "del_flag" INT4 NULL DEFAULT NULL,
  "create_by" varchar(32) NULL DEFAULT NULL,
  "create_time" TIMESTAMP NULL DEFAULT NULL,
  "update_by" varchar(32) NULL DEFAULT NULL,
  "update_time" TIMESTAMP NULL DEFAULT NULL,
  "type" INT8 NULL DEFAULT 0,
  "tenant_id" varchar(10) NULL DEFAULT NULL,
  CONSTRAINT "pk_jimu_dict" PRIMARY KEY ("id")
);


-- Table structure for jimu_dict_item
-- ----------------------------
DROP TABLE IF EXISTS "jimu_dict_item" CASCADE;
CREATE TABLE "jimu_dict_item" (
  "id" varchar(32) NOT NULL,
  "dict_id" varchar(32) NULL DEFAULT NULL,
  "item_text" varchar(100) NULL DEFAULT NULL,
  "item_value" varchar(100) NULL DEFAULT NULL,
  "description" varchar(255) NULL DEFAULT NULL,
  "sort_order" INT4 NULL DEFAULT NULL,
  "status" INT4 NULL DEFAULT NULL,
  "create_by" varchar(32) NULL DEFAULT NULL,
  "create_time" TIMESTAMP NULL DEFAULT NULL,
  "update_by" varchar(32) NULL DEFAULT NULL,
  "update_time" TIMESTAMP NULL DEFAULT NULL,
  CONSTRAINT "pk_jimu_dict_item" PRIMARY KEY ("id")
);


-- Table structure for jimu_report
-- ----------------------------
DROP TABLE IF EXISTS "jimu_report" CASCADE;
CREATE TABLE "jimu_report" (
  "id" varchar(32) NOT NULL,
  "code" varchar(50) NULL DEFAULT NULL,
  "name" varchar(50) NULL DEFAULT NULL,
  "note" varchar(255) NULL DEFAULT NULL,
  "status" varchar(10) NULL DEFAULT NULL,
  "type" varchar(10) NULL DEFAULT NULL,
  "json_str" TEXT NULL,
  "api_url" varchar(255) NULL DEFAULT NULL,
  "thumb" text NULL,
  "create_by" varchar(50) NULL DEFAULT NULL,
  "create_time" TIMESTAMP NULL DEFAULT NULL,
  "update_by" varchar(50) NULL DEFAULT NULL,
  "update_time" TIMESTAMP NULL DEFAULT NULL,
  "del_flag" INT2 NULL DEFAULT NULL,
  "api_method" varchar(255) NULL DEFAULT NULL,
  "api_code" varchar(255) NULL DEFAULT NULL,
  "template" INT2 NULL DEFAULT NULL,
  "view_count" INT8 NULL DEFAULT 0,
  "css_str" text NULL,
  "js_str" text NULL,
  "py_str" text NULL,
  "tenant_id" varchar(10) NULL DEFAULT NULL,
  CONSTRAINT "pk_jimu_report" PRIMARY KEY ("id")
);


-- Table structure for jimu_report_data_source
-- ----------------------------
DROP TABLE IF EXISTS "jimu_report_data_source" CASCADE;
CREATE TABLE "jimu_report_data_source" (
  "id" varchar(36) NOT NULL,
  "name" varchar(100) NULL DEFAULT NULL,
  "report_id" varchar(100) NULL DEFAULT NULL,
  "code" varchar(100) NULL DEFAULT NULL,
  "remark" varchar(200) NULL DEFAULT NULL,
  "db_type" varchar(10) NULL DEFAULT NULL,
  "db_driver" varchar(100) NULL DEFAULT NULL,
  "db_url" varchar(500) NULL DEFAULT NULL,
  "db_username" varchar(100) NULL DEFAULT NULL,
  "db_password" varchar(100) NULL DEFAULT NULL,
  "create_by" varchar(50) NULL DEFAULT NULL,
  "create_time" TIMESTAMP NULL DEFAULT NULL,
  "update_by" varchar(50) NULL DEFAULT NULL,
  "update_time" TIMESTAMP NULL DEFAULT NULL,
  "connect_times" INT4 NULL DEFAULT 0,
  "tenant_id" varchar(10) NULL DEFAULT NULL,
  "type" varchar(10) NULL DEFAULT NULL,
  CONSTRAINT "pk_jimu_report_data_source" PRIMARY KEY ("id")
);


-- Table structure for jimu_report_db
-- ----------------------------
DROP TABLE IF EXISTS "jimu_report_db" CASCADE;
CREATE TABLE "jimu_report_db" (
  "id" varchar(36) NOT NULL,
  "jimu_report_id" varchar(32) NULL DEFAULT NULL,
  "create_by" varchar(50) NULL DEFAULT NULL,
  "update_by" varchar(50) NULL DEFAULT NULL,
  "create_time" TIMESTAMP NULL DEFAULT NULL,
  "update_time" TIMESTAMP NULL DEFAULT NULL,
  "db_code" varchar(32) NULL DEFAULT NULL,
  "db_ch_name" varchar(50) NULL DEFAULT NULL,
  "db_type" varchar(32) NULL DEFAULT NULL,
  "db_table_name" varchar(32) NULL DEFAULT NULL,
  "db_dyn_sql" TEXT NULL,
  "db_key" varchar(32) NULL DEFAULT NULL,
  "tb_db_key" varchar(32) NULL DEFAULT NULL,
  "tb_db_table_name" varchar(32) NULL DEFAULT NULL,
  "java_type" varchar(32) NULL DEFAULT NULL,
  "java_value" varchar(255) NULL DEFAULT NULL,
  "api_url" varchar(255) NULL DEFAULT NULL,
  "api_method" varchar(255) NULL DEFAULT NULL,
  "is_list" varchar(10) NULL DEFAULT '0',
  "is_page" varchar(10) NULL DEFAULT NULL,
  "db_source" varchar(255) NULL DEFAULT NULL,
  "db_source_type" varchar(50) NULL DEFAULT NULL,
  "json_data" text NULL,
  "api_convert" varchar(255) NULL DEFAULT NULL,
  CONSTRAINT "pk_jimu_report_db" PRIMARY KEY ("id")
);


-- Table structure for jimu_report_db_field
-- ----------------------------
DROP TABLE IF EXISTS "jimu_report_db_field" CASCADE;
CREATE TABLE "jimu_report_db_field" (
  "id" varchar(36) NOT NULL,
  "create_by" varchar(50) NULL DEFAULT NULL,
  "create_time" TIMESTAMP NULL DEFAULT NULL,
  "update_by" varchar(50) NULL DEFAULT NULL,
  "update_time" TIMESTAMP NULL DEFAULT NULL,
  "jimu_report_db_id" varchar(32) NULL DEFAULT NULL,
  "field_name" varchar(80) NULL DEFAULT NULL,
  "field_text" varchar(50) NULL DEFAULT NULL,
  "widget_type" varchar(50) NULL DEFAULT NULL,
  "widget_width" INT4 NULL DEFAULT NULL,
  "order_num" INT4 NULL DEFAULT NULL,
  "search_flag" INT4 NULL DEFAULT 0,
  "search_mode" INT4 NULL DEFAULT NULL,
  "dict_code" varchar(255) NULL DEFAULT NULL,
  "search_value" varchar(100) NULL DEFAULT NULL,
  "search_format" varchar(50) NULL DEFAULT NULL,
  "ext_json" text NULL,
  CONSTRAINT "pk_jimu_report_db_field" PRIMARY KEY ("id")
);


-- Table structure for jimu_report_db_param
-- ----------------------------
DROP TABLE IF EXISTS "jimu_report_db_param" CASCADE;
CREATE TABLE "jimu_report_db_param" (
  "id" varchar(36) NOT NULL,
  "jimu_report_head_id" varchar(36) NOT NULL,
  "param_name" varchar(32) NOT NULL,
  "param_txt" varchar(32) NULL DEFAULT NULL,
  "param_value" varchar(1000) NULL DEFAULT NULL,
  "order_num" INT4 NULL DEFAULT NULL,
  "create_by" varchar(50) NULL DEFAULT NULL,
  "create_time" TIMESTAMP NULL DEFAULT NULL,
  "update_by" varchar(50) NULL DEFAULT NULL,
  "update_time" TIMESTAMP NULL DEFAULT NULL,
  "search_flag" INT4 NULL DEFAULT NULL,
  "widget_type" varchar(50) NULL DEFAULT NULL,
  "search_mode" INT4 NULL DEFAULT NULL,
  "dict_code" varchar(255) NULL DEFAULT NULL,
  "search_format" varchar(50) NULL DEFAULT NULL,
  "ext_json" text NULL,
  CONSTRAINT "pk_jimu_report_db_param" PRIMARY KEY ("id")
);


-- Table structure for jimu_report_export_log
-- ----------------------------
DROP TABLE IF EXISTS "jimu_report_export_log" CASCADE;
CREATE TABLE "jimu_report_export_log" (
  "id" varchar(32) NOT NULL,
  "batch_no" varchar(50) NULL DEFAULT NULL,
  "export_channel" varchar(20) NULL DEFAULT NULL,
  "export_type" varchar(10) NULL DEFAULT NULL,
  "report_id" text NULL,
  "download_path" varchar(255) NULL DEFAULT NULL,
  "status" varchar(15) NULL DEFAULT NULL,
  "create_by" varchar(32) NULL DEFAULT NULL,
  "create_time" TIMESTAMP NULL DEFAULT NULL,
  "update_time" TIMESTAMP NULL DEFAULT NULL,
  "tenant_id" varchar(10) NULL DEFAULT NULL,
  CONSTRAINT "pk_jimu_report_export_log" PRIMARY KEY ("id")
);


-- Table structure for jimu_report_link
-- ----------------------------
DROP TABLE IF EXISTS "jimu_report_link" CASCADE;
CREATE TABLE "jimu_report_link" (
  "id" varchar(32) NOT NULL,
  "report_id" varchar(32) NULL DEFAULT NULL,
  "parameter" text NULL,
  "eject_type" varchar(1) NULL DEFAULT NULL,
  "link_name" varchar(255) NULL DEFAULT NULL,
  "api_method" varchar(1) NULL DEFAULT NULL,
  "link_type" varchar(1) NULL DEFAULT NULL,
  "api_url" varchar(1000) NULL DEFAULT NULL,
  "link_chart_id" varchar(50) NULL DEFAULT NULL,
  "expression" varchar(255) NULL DEFAULT NULL,
  "requirement" varchar(255) NULL DEFAULT NULL,
  CONSTRAINT "pk_jimu_report_link" PRIMARY KEY ("id")
);


-- Table structure for jimu_report_map
-- ----------------------------
DROP TABLE IF EXISTS "jimu_report_map" CASCADE;
CREATE TABLE "jimu_report_map" (
  "id" varchar(64) NOT NULL,
  "label" varchar(125) NULL DEFAULT NULL,
  "name" varchar(125) NULL DEFAULT NULL,
  "data" TEXT NULL,
  "create_by" varchar(32) NULL DEFAULT NULL,
  "create_time" TIMESTAMP NULL DEFAULT NULL,
  "update_by" varchar(32) NULL DEFAULT NULL,
  "update_time" TIMESTAMP NULL DEFAULT NULL,
  "del_flag" varchar(1) NULL DEFAULT NULL,
  "sys_org_code" varchar(64) NULL DEFAULT NULL,
  CONSTRAINT "pk_jimu_report_map" PRIMARY KEY ("id")
);


-- Table structure for jimu_report_share
-- ----------------------------
DROP TABLE IF EXISTS "jimu_report_share" CASCADE;
CREATE TABLE "jimu_report_share" (
  "id" varchar(32) NOT NULL,
  "report_id" varchar(32) NULL DEFAULT NULL,
  "preview_url" varchar(1000) NULL DEFAULT NULL,
  "preview_lock" varchar(4) NULL DEFAULT NULL,
  "last_update_time" TIMESTAMP NULL DEFAULT NULL,
  "term_of_validity" varchar(1) NULL DEFAULT NULL,
  "status" varchar(1) NULL DEFAULT NULL,
  "preview_lock_status" varchar(1) NULL DEFAULT NULL,
  "share_token" varchar(50) NULL DEFAULT NULL,
  CONSTRAINT "pk_jimu_report_share" PRIMARY KEY ("id")
);


-- Table structure for onl_drag_comp
-- ----------------------------
DROP TABLE IF EXISTS "onl_drag_comp" CASCADE;
CREATE TABLE "onl_drag_comp" (
  "id" varchar(32) NOT NULL,
  "parent_id" varchar(32) NULL DEFAULT NULL,
  "comp_name" varchar(50) NULL DEFAULT NULL,
  "comp_type" varchar(20) NULL DEFAULT NULL,
  "icon" varchar(50) NULL DEFAULT NULL,
  "order_num" INT4 NULL DEFAULT NULL,
  "type_id" INT4 NULL DEFAULT NULL,
  "comp_config" TEXT NULL,
  "status" varchar(2) NULL DEFAULT '0',
  "create_by" varchar(50) NULL DEFAULT NULL,
  "create_time" TIMESTAMP NULL DEFAULT NULL,
  "update_by" varchar(50) NULL DEFAULT NULL,
  "update_time" TIMESTAMP NULL DEFAULT NULL,
  CONSTRAINT "pk_onl_drag_comp" PRIMARY KEY ("id")
);


-- Table structure for onl_drag_dataset_head
-- ----------------------------
DROP TABLE IF EXISTS "onl_drag_dataset_head" CASCADE;
CREATE TABLE "onl_drag_dataset_head" (
  "id" varchar(32) NOT NULL,
  "name" varchar(100) NOT NULL,
  "code" varchar(36) NULL DEFAULT NULL,
  "parent_id" varchar(36) NULL DEFAULT NULL,
  "db_source" varchar(100) NULL DEFAULT NULL,
  "query_sql" varchar(5000) NULL DEFAULT '0',
  "content" varchar(1000) NULL DEFAULT NULL,
  "iz_agent" varchar(10) NULL DEFAULT '0',
  "data_type" varchar(50) NULL DEFAULT NULL,
  "api_method" varchar(10) NULL DEFAULT NULL,
  "create_time" TIMESTAMP NULL DEFAULT NULL,
  "create_by" varchar(50) NULL DEFAULT NULL,
  "update_time" TIMESTAMP NULL DEFAULT NULL,
  "update_by" varchar(50) NULL DEFAULT NULL,
  "low_app_id" varchar(32) NULL DEFAULT NULL,
  "tenant_id" INT4 NULL DEFAULT NULL,
  CONSTRAINT "pk_onl_drag_dataset_head" PRIMARY KEY ("id")
);


-- Table structure for onl_drag_dataset_item
-- ----------------------------
DROP TABLE IF EXISTS "onl_drag_dataset_item" CASCADE;
CREATE TABLE "onl_drag_dataset_item" (
  "id" varchar(32) NOT NULL,
  "head_id" varchar(36) NOT NULL,
  "field_name" varchar(36) NULL DEFAULT NULL,
  "field_txt" varchar(1000) NULL DEFAULT NULL,
  "field_type" varchar(10) NULL DEFAULT NULL,
  "widget_type" varchar(30) NULL DEFAULT NULL,
  "dict_code" varchar(500) NULL DEFAULT NULL,
  "iz_show" varchar(5) NULL DEFAULT NULL,
  "iz_search" varchar(10) NULL DEFAULT NULL,
  "iz_total" varchar(5) NULL DEFAULT NULL,
  "search_mode" varchar(10) NULL DEFAULT NULL,
  "order_num" INT4 NULL DEFAULT NULL,
  "create_by" varchar(32) NULL DEFAULT NULL,
  "create_time" TIMESTAMP NULL DEFAULT NULL,
  "update_by" varchar(32) NULL DEFAULT NULL,
  "update_time" TIMESTAMP NULL DEFAULT NULL,
  CONSTRAINT "pk_onl_drag_dataset_item" PRIMARY KEY ("id")
);


-- Table structure for onl_drag_dataset_param
-- ----------------------------
DROP TABLE IF EXISTS "onl_drag_dataset_param" CASCADE;
CREATE TABLE "onl_drag_dataset_param" (
  "id" varchar(36) NOT NULL,
  "head_id" varchar(36) NOT NULL,
  "param_name" varchar(32) NOT NULL,
  "param_txt" varchar(32) NULL DEFAULT NULL,
  "param_value" varchar(1000) NULL DEFAULT NULL,
  "order_num" INT4 NULL DEFAULT NULL,
  "iz_search" INT4 NULL DEFAULT NULL,
  "widget_type" varchar(50) NULL DEFAULT NULL,
  "search_mode" INT4 NULL DEFAULT NULL,
  "dict_code" varchar(255) NULL DEFAULT NULL,
  "create_by" varchar(50) NULL DEFAULT NULL,
  "create_time" TIMESTAMP NULL DEFAULT NULL,
  "update_by" varchar(50) NULL DEFAULT NULL,
  "update_time" TIMESTAMP NULL DEFAULT NULL,
  CONSTRAINT "pk_onl_drag_dataset_param" PRIMARY KEY ("id")
);


-- Table structure for onl_drag_page
-- ----------------------------
DROP TABLE IF EXISTS "onl_drag_page" CASCADE;
CREATE TABLE "onl_drag_page" (
  "id" varchar(50) NOT NULL,
  "name" varchar(100) NULL DEFAULT NULL,
  "path" varchar(100) NULL DEFAULT NULL,
  "background_color" varchar(10) NULL DEFAULT NULL,
  "background_image" varchar(255) NULL DEFAULT NULL,
  "design_type" INT4 NULL DEFAULT NULL,
  "theme" varchar(10) NULL DEFAULT NULL,
  "style" varchar(20) NULL DEFAULT NULL,
  "cover_url" varchar(500) NULL DEFAULT NULL,
  "template" TEXT NULL,
  "protection_code" varchar(32) NULL DEFAULT NULL,
  "type" varchar(10) NULL DEFAULT NULL,
  "iz_template" varchar(10) NULL DEFAULT '0',
  "create_by" varchar(50) NULL DEFAULT NULL,
  "create_time" TIMESTAMP NULL DEFAULT NULL,
  "update_by" varchar(50) NULL DEFAULT NULL,
  "update_time" TIMESTAMP NULL DEFAULT NULL,
  "low_app_id" varchar(50) NULL DEFAULT NULL,
  "tenant_id" INT4 NULL DEFAULT NULL,
  "update_count" INT4 NULL DEFAULT 1,
  "visits_num" INT4 NULL DEFAULT NULL,
  CONSTRAINT "pk_onl_drag_page" PRIMARY KEY ("id")
);


-- Table structure for onl_drag_page_comp
-- ----------------------------
DROP TABLE IF EXISTS "onl_drag_page_comp" CASCADE;
CREATE TABLE "onl_drag_page_comp" (
  "id" varchar(32) NOT NULL,
  "parent_id" varchar(32) NULL DEFAULT NULL,
  "page_Id" varchar(50) NULL DEFAULT NULL,
  "comp_id" varchar(32) NULL DEFAULT NULL,
  "component" varchar(50) NULL DEFAULT NULL,
  "config" TEXT NULL,
  "create_by" varchar(50) NULL DEFAULT NULL,
  "create_time" TIMESTAMP NULL DEFAULT NULL,
  "update_by" varchar(50) NULL DEFAULT NULL,
  "update_time" TIMESTAMP NULL DEFAULT NULL,
  CONSTRAINT "pk_onl_drag_page_comp" PRIMARY KEY ("id")
);


-- Table structure for onl_drag_table_relation
-- ----------------------------
DROP TABLE IF EXISTS "onl_drag_table_relation" CASCADE;
CREATE TABLE "onl_drag_table_relation" (
  "id" varchar(50) NOT NULL,
  "aggregation_name" varchar(100) NULL DEFAULT NULL,
  "aggregation_desc" varchar(100) NULL DEFAULT NULL,
  "relation_forms" TEXT NULL,
  "filter_condition" TEXT NULL,
  "header_fields" TEXT NULL,
  "calculate_fields" TEXT NULL,
  "validate_info" TEXT NULL,
  "del_flag" INT2 NULL DEFAULT NULL,
  "low_app_id" varchar(50) NULL DEFAULT NULL,
  "tenant_id" INT4 NULL DEFAULT NULL,
  "create_by" varchar(50) NULL DEFAULT NULL,
  "create_time" TIMESTAMP NULL DEFAULT NULL,
  "update_by" varchar(50) NULL DEFAULT NULL,
  "update_time" TIMESTAMP NULL DEFAULT NULL,
  CONSTRAINT "pk_onl_drag_table_relation" PRIMARY KEY ("id")
);


-- PostgreSQL does not need FOREIGN_KEY_CHECKS

-- ----------------------------
-- Table and Column Comments
-- ----------------------------
COMMENT ON COLUMN "jimu_dict"."dict_name" IS '字典名称';
COMMENT ON COLUMN "jimu_dict"."dict_code" IS '字典编码';
COMMENT ON COLUMN "jimu_dict"."description" IS '描述';
COMMENT ON COLUMN "jimu_dict"."del_flag" IS '删除状态';
COMMENT ON COLUMN "jimu_dict"."create_by" IS '创建人';
COMMENT ON COLUMN "jimu_dict"."create_time" IS '创建时间';
COMMENT ON COLUMN "jimu_dict"."update_by" IS '更新人';
COMMENT ON COLUMN "jimu_dict"."update_time" IS '更新时间';
COMMENT ON COLUMN "jimu_dict"."type" IS '字典类型0为string,1为number';
COMMENT ON COLUMN "jimu_dict"."tenant_id" IS '多租户标识';

COMMENT ON COLUMN "jimu_dict_item"."dict_id" IS '字典id';
COMMENT ON COLUMN "jimu_dict_item"."item_text" IS '字典项文本';
COMMENT ON COLUMN "jimu_dict_item"."item_value" IS '字典项值';
COMMENT ON COLUMN "jimu_dict_item"."description" IS '描述';
COMMENT ON COLUMN "jimu_dict_item"."sort_order" IS '排序';
COMMENT ON COLUMN "jimu_dict_item"."status" IS '状态（1启用 0不启用）';

COMMENT ON TABLE "jimu_report" IS '在线excel设计器';
COMMENT ON COLUMN "jimu_report"."id" IS '主键';
COMMENT ON COLUMN "jimu_report"."code" IS '编码';
COMMENT ON COLUMN "jimu_report"."name" IS '名称';
COMMENT ON COLUMN "jimu_report"."note" IS '说明';
COMMENT ON COLUMN "jimu_report"."status" IS '状态';
COMMENT ON COLUMN "jimu_report"."type" IS '类型';
COMMENT ON COLUMN "jimu_report"."json_str" IS 'json字符串';
COMMENT ON COLUMN "jimu_report"."api_url" IS '请求地址';
COMMENT ON COLUMN "jimu_report"."thumb" IS '缩略图';
COMMENT ON COLUMN "jimu_report"."create_by" IS '创建人';
COMMENT ON COLUMN "jimu_report"."create_time" IS '创建时间';
COMMENT ON COLUMN "jimu_report"."update_by" IS '修改人';
COMMENT ON COLUMN "jimu_report"."update_time" IS '修改时间';
COMMENT ON COLUMN "jimu_report"."del_flag" IS '删除标识0-正常,1-已删除';
COMMENT ON COLUMN "jimu_report"."api_method" IS '请求方法0-get,1-post';
COMMENT ON COLUMN "jimu_report"."api_code" IS '请求编码';
COMMENT ON COLUMN "jimu_report"."template" IS '是否是模板 0-是,1-不是';
COMMENT ON COLUMN "jimu_report"."view_count" IS '浏览次数';
COMMENT ON COLUMN "jimu_report"."css_str" IS 'css增强';
COMMENT ON COLUMN "jimu_report"."js_str" IS 'js增强';
COMMENT ON COLUMN "jimu_report"."py_str" IS 'py增强';
COMMENT ON COLUMN "jimu_report"."tenant_id" IS '多租户标识';

COMMENT ON COLUMN "jimu_report_data_source"."name" IS '数据源名称';
COMMENT ON COLUMN "jimu_report_data_source"."report_id" IS '报表_id';
COMMENT ON COLUMN "jimu_report_data_source"."code" IS '编码';
COMMENT ON COLUMN "jimu_report_data_source"."remark" IS '备注';
COMMENT ON COLUMN "jimu_report_data_source"."db_type" IS '数据库类型';
COMMENT ON COLUMN "jimu_report_data_source"."db_driver" IS '驱动类';
COMMENT ON COLUMN "jimu_report_data_source"."db_url" IS '数据源地址';
COMMENT ON COLUMN "jimu_report_data_source"."db_username" IS '用户名';
COMMENT ON COLUMN "jimu_report_data_source"."db_password" IS '密码';
COMMENT ON COLUMN "jimu_report_data_source"."create_by" IS '创建人';
COMMENT ON COLUMN "jimu_report_data_source"."create_time" IS '创建日期';
COMMENT ON COLUMN "jimu_report_data_source"."update_by" IS '更新人';
COMMENT ON COLUMN "jimu_report_data_source"."update_time" IS '更新日期';
COMMENT ON COLUMN "jimu_report_data_source"."connect_times" IS '连接失败次数';
COMMENT ON COLUMN "jimu_report_data_source"."tenant_id" IS '多租户标识';
COMMENT ON COLUMN "jimu_report_data_source"."type" IS '类型(report:报表;drag:仪表盘)';

COMMENT ON COLUMN "jimu_report_db"."id" IS 'id';
COMMENT ON COLUMN "jimu_report_db"."jimu_report_id" IS '主键字段';
COMMENT ON COLUMN "jimu_report_db"."create_by" IS '创建人登录名称';
COMMENT ON COLUMN "jimu_report_db"."update_by" IS '更新人登录名称';
COMMENT ON COLUMN "jimu_report_db"."create_time" IS '创建日期';
COMMENT ON COLUMN "jimu_report_db"."update_time" IS '更新日期';
COMMENT ON COLUMN "jimu_report_db"."db_code" IS '数据集编码';
COMMENT ON COLUMN "jimu_report_db"."db_ch_name" IS '数据集名字';
COMMENT ON COLUMN "jimu_report_db"."db_type" IS '数据源类型';
COMMENT ON COLUMN "jimu_report_db"."db_table_name" IS '数据库表名';
COMMENT ON COLUMN "jimu_report_db"."db_dyn_sql" IS '动态查询SQL';
COMMENT ON COLUMN "jimu_report_db"."db_key" IS '数据源KEY';
COMMENT ON COLUMN "jimu_report_db"."tb_db_key" IS '填报数据源';
COMMENT ON COLUMN "jimu_report_db"."tb_db_table_name" IS '填报数据表';
COMMENT ON COLUMN "jimu_report_db"."java_type" IS 'java类数据集  类型（spring:springkey,class:java类名）';
COMMENT ON COLUMN "jimu_report_db"."java_value" IS 'java类数据源  数值（bean key/java类名）';
COMMENT ON COLUMN "jimu_report_db"."api_url" IS '请求地址';
COMMENT ON COLUMN "jimu_report_db"."api_method" IS '请求方法0-get,1-post';
COMMENT ON COLUMN "jimu_report_db"."is_list" IS '是否是列表0否1是 默认0';
COMMENT ON COLUMN "jimu_report_db"."is_page" IS '是否作为分页,0:不分页，1:分页';
COMMENT ON COLUMN "jimu_report_db"."db_source" IS '数据源';
COMMENT ON COLUMN "jimu_report_db"."db_source_type" IS '数据库类型 MYSQL ORACLE SQLSERVER';
COMMENT ON COLUMN "jimu_report_db"."json_data" IS 'json数据，直接解析json内容';
COMMENT ON COLUMN "jimu_report_db"."api_convert" IS 'api转换器';

COMMENT ON COLUMN "jimu_report_db_field"."id" IS 'id';
COMMENT ON COLUMN "jimu_report_db_field"."create_by" IS '创建人登录名称';
COMMENT ON COLUMN "jimu_report_db_field"."create_time" IS '创建日期';
COMMENT ON COLUMN "jimu_report_db_field"."update_by" IS '更新人登录名称';
COMMENT ON COLUMN "jimu_report_db_field"."update_time" IS '更新日期';
COMMENT ON COLUMN "jimu_report_db_field"."jimu_report_db_id" IS '数据源ID';
COMMENT ON COLUMN "jimu_report_db_field"."field_name" IS '字段名';
COMMENT ON COLUMN "jimu_report_db_field"."field_text" IS '字段文本';
COMMENT ON COLUMN "jimu_report_db_field"."widget_type" IS '控件类型';
COMMENT ON COLUMN "jimu_report_db_field"."widget_width" IS '控件宽度';
COMMENT ON COLUMN "jimu_report_db_field"."order_num" IS '排序';
COMMENT ON COLUMN "jimu_report_db_field"."search_flag" IS '查询标识0否1是 默认0';
COMMENT ON COLUMN "jimu_report_db_field"."search_mode" IS '查询模式1简单2范围';
COMMENT ON COLUMN "jimu_report_db_field"."dict_code" IS '字典编码支持从表中取数据';
COMMENT ON COLUMN "jimu_report_db_field"."search_value" IS '查询默认值';
COMMENT ON COLUMN "jimu_report_db_field"."search_format" IS '查询时间格式化表达式';
COMMENT ON COLUMN "jimu_report_db_field"."ext_json" IS '参数配置';

COMMENT ON COLUMN "jimu_report_db_param"."jimu_report_head_id" IS '动态报表ID';
COMMENT ON COLUMN "jimu_report_db_param"."param_name" IS '参数字段';
COMMENT ON COLUMN "jimu_report_db_param"."param_txt" IS '参数文本';
COMMENT ON COLUMN "jimu_report_db_param"."param_value" IS '参数默认值';
COMMENT ON COLUMN "jimu_report_db_param"."order_num" IS '排序';
COMMENT ON COLUMN "jimu_report_db_param"."create_by" IS '创建人登录名称';
COMMENT ON COLUMN "jimu_report_db_param"."create_time" IS '创建日期';
COMMENT ON COLUMN "jimu_report_db_param"."update_by" IS '更新人登录名称';
COMMENT ON COLUMN "jimu_report_db_param"."update_time" IS '更新日期';
COMMENT ON COLUMN "jimu_report_db_param"."search_flag" IS '查询标识0否1是 默认0';
COMMENT ON COLUMN "jimu_report_db_param"."widget_type" IS '查询控件类型';
COMMENT ON COLUMN "jimu_report_db_param"."search_mode" IS '查询模式1简单2范围';
COMMENT ON COLUMN "jimu_report_db_param"."dict_code" IS '字典';
COMMENT ON COLUMN "jimu_report_db_param"."search_format" IS '查询时间格式化表达式';
COMMENT ON COLUMN "jimu_report_db_param"."ext_json" IS '参数配置';

COMMENT ON TABLE "jimu_report_export_log" IS '积木报表自动导出记录表';
COMMENT ON COLUMN "jimu_report_export_log"."batch_no" IS '批次编号';
COMMENT ON COLUMN "jimu_report_export_log"."export_channel" IS '导出渠道';
COMMENT ON COLUMN "jimu_report_export_log"."export_type" IS '导出类型';
COMMENT ON COLUMN "jimu_report_export_log"."report_id" IS '报表id';
COMMENT ON COLUMN "jimu_report_export_log"."download_path" IS '下载路径';
COMMENT ON COLUMN "jimu_report_export_log"."status" IS '状态';
COMMENT ON COLUMN "jimu_report_export_log"."create_by" IS '创建人';
COMMENT ON COLUMN "jimu_report_export_log"."create_time" IS '创建时间';
COMMENT ON COLUMN "jimu_report_export_log"."update_time" IS '更新时间';
COMMENT ON COLUMN "jimu_report_export_log"."tenant_id" IS '多租户标识';

COMMENT ON TABLE "jimu_report_link" IS '超链接配置表';
COMMENT ON COLUMN "jimu_report_link"."id" IS '主键id';
COMMENT ON COLUMN "jimu_report_link"."report_id" IS '积木设计器id';
COMMENT ON COLUMN "jimu_report_link"."parameter" IS '参数';
COMMENT ON COLUMN "jimu_report_link"."eject_type" IS '弹出方式（0 当前页面 1 新窗口）';
COMMENT ON COLUMN "jimu_report_link"."link_name" IS '链接名称';
COMMENT ON COLUMN "jimu_report_link"."api_method" IS '请求方法0-get,1-post';
COMMENT ON COLUMN "jimu_report_link"."link_type" IS '链接方式(0 网络报表 1 网络连接 2 图表联动)';
COMMENT ON COLUMN "jimu_report_link"."api_url" IS '外网api';
COMMENT ON COLUMN "jimu_report_link"."link_chart_id" IS '联动图表的ID';
COMMENT ON COLUMN "jimu_report_link"."expression" IS '表达式';
COMMENT ON COLUMN "jimu_report_link"."requirement" IS '条件';

COMMENT ON TABLE "jimu_report_map" IS '地图配置表';
COMMENT ON COLUMN "jimu_report_map"."id" IS '主键';
COMMENT ON COLUMN "jimu_report_map"."label" IS '地图名称';
COMMENT ON COLUMN "jimu_report_map"."name" IS '地图编码';
COMMENT ON COLUMN "jimu_report_map"."data" IS '地图数据';
COMMENT ON COLUMN "jimu_report_map"."create_by" IS '创建人';
COMMENT ON COLUMN "jimu_report_map"."create_time" IS '创建时间';
COMMENT ON COLUMN "jimu_report_map"."update_by" IS '修改人';
COMMENT ON COLUMN "jimu_report_map"."update_time" IS '修改时间';
COMMENT ON COLUMN "jimu_report_map"."del_flag" IS '0表示未删除,1表示删除';
COMMENT ON COLUMN "jimu_report_map"."sys_org_code" IS '所属部门';

COMMENT ON TABLE "jimu_report_share" IS '积木报表预览权限表';
COMMENT ON COLUMN "jimu_report_share"."id" IS '主键';
COMMENT ON COLUMN "jimu_report_share"."report_id" IS '在线excel设计器id';
COMMENT ON COLUMN "jimu_report_share"."preview_url" IS '预览地址';
COMMENT ON COLUMN "jimu_report_share"."preview_lock" IS '密码锁';
COMMENT ON COLUMN "jimu_report_share"."last_update_time" IS '最后更新时间';
COMMENT ON COLUMN "jimu_report_share"."term_of_validity" IS '有效期(0:永久有效，1:1天，2:7天)';
COMMENT ON COLUMN "jimu_report_share"."status" IS '是否过期(0未过期，1已过期)';
COMMENT ON COLUMN "jimu_report_share"."preview_lock_status" IS '密码锁状态(0不存在密码锁，1存在密码锁)';
COMMENT ON COLUMN "jimu_report_share"."share_token" IS '分享token';

COMMENT ON TABLE "onl_drag_comp" IS '组件库';
COMMENT ON COLUMN "onl_drag_comp"."id" IS '主键';
COMMENT ON COLUMN "onl_drag_comp"."comp_name" IS '组件名称';
COMMENT ON COLUMN "onl_drag_comp"."icon" IS '图标';
COMMENT ON COLUMN "onl_drag_comp"."order_num" IS '排序';
COMMENT ON COLUMN "onl_drag_comp"."type_id" IS '组件类型';
COMMENT ON COLUMN "onl_drag_comp"."comp_config" IS '组件配置';
COMMENT ON COLUMN "onl_drag_comp"."status" IS '状态0:无效 1:有效';
COMMENT ON COLUMN "onl_drag_comp"."create_by" IS '创建人登录名称';
COMMENT ON COLUMN "onl_drag_comp"."create_time" IS '创建日期';
COMMENT ON COLUMN "onl_drag_comp"."update_by" IS '更新人登录名称';
COMMENT ON COLUMN "onl_drag_comp"."update_time" IS '更新日期';

COMMENT ON COLUMN "onl_drag_dataset_head"."id" IS 'id';
COMMENT ON COLUMN "onl_drag_dataset_head"."name" IS '名称';
COMMENT ON COLUMN "onl_drag_dataset_head"."code" IS '编码';
COMMENT ON COLUMN "onl_drag_dataset_head"."parent_id" IS '父id';
COMMENT ON COLUMN "onl_drag_dataset_head"."db_source" IS '动态数据源';
COMMENT ON COLUMN "onl_drag_dataset_head"."query_sql" IS '查询数据SQL';
COMMENT ON COLUMN "onl_drag_dataset_head"."content" IS '描述';
COMMENT ON COLUMN "onl_drag_dataset_head"."iz_agent" IS 'iz_agent';
COMMENT ON COLUMN "onl_drag_dataset_head"."data_type" IS '数据类型';
COMMENT ON COLUMN "onl_drag_dataset_head"."api_method" IS 'api方法：get/post';
COMMENT ON COLUMN "onl_drag_dataset_head"."low_app_id" IS '应用ID';
COMMENT ON COLUMN "onl_drag_dataset_head"."tenant_id" IS '租户ID';

COMMENT ON COLUMN "onl_drag_dataset_item"."id" IS 'id';
COMMENT ON COLUMN "onl_drag_dataset_item"."head_id" IS '主表ID';
COMMENT ON COLUMN "onl_drag_dataset_item"."field_name" IS '字段名';
COMMENT ON COLUMN "onl_drag_dataset_item"."field_txt" IS '字段文本';
COMMENT ON COLUMN "onl_drag_dataset_item"."field_type" IS '字段类型';
COMMENT ON COLUMN "onl_drag_dataset_item"."widget_type" IS '控件类型';
COMMENT ON COLUMN "onl_drag_dataset_item"."dict_code" IS '字典Code';
COMMENT ON COLUMN "onl_drag_dataset_item"."iz_show" IS '是否列表显示';
COMMENT ON COLUMN "onl_drag_dataset_item"."iz_search" IS '是否查询';
COMMENT ON COLUMN "onl_drag_dataset_item"."iz_total" IS '是否计算总计（仅对数值有效）';
COMMENT ON COLUMN "onl_drag_dataset_item"."search_mode" IS '查询模式';
COMMENT ON COLUMN "onl_drag_dataset_item"."order_num" IS '排序';
COMMENT ON COLUMN "onl_drag_dataset_item"."create_by" IS '创建人';
COMMENT ON COLUMN "onl_drag_dataset_item"."create_time" IS '创建时间';
COMMENT ON COLUMN "onl_drag_dataset_item"."update_by" IS '修改人';
COMMENT ON COLUMN "onl_drag_dataset_item"."update_time" IS '修改时间';

COMMENT ON COLUMN "onl_drag_dataset_param"."head_id" IS '动态报表ID';
COMMENT ON COLUMN "onl_drag_dataset_param"."param_name" IS '参数字段';
COMMENT ON COLUMN "onl_drag_dataset_param"."param_txt" IS '参数文本';
COMMENT ON COLUMN "onl_drag_dataset_param"."param_value" IS '参数默认值';
COMMENT ON COLUMN "onl_drag_dataset_param"."order_num" IS '排序';
COMMENT ON COLUMN "onl_drag_dataset_param"."iz_search" IS '查询标识0否1是 默认0';
COMMENT ON COLUMN "onl_drag_dataset_param"."widget_type" IS '查询控件类型';
COMMENT ON COLUMN "onl_drag_dataset_param"."search_mode" IS '查询模式1简单2范围';
COMMENT ON COLUMN "onl_drag_dataset_param"."dict_code" IS '字典';
COMMENT ON COLUMN "onl_drag_dataset_param"."create_by" IS '创建人登录名称';
COMMENT ON COLUMN "onl_drag_dataset_param"."create_time" IS '创建日期';
COMMENT ON COLUMN "onl_drag_dataset_param"."update_by" IS '更新人登录名称';
COMMENT ON COLUMN "onl_drag_dataset_param"."update_time" IS '更新日期';

COMMENT ON TABLE "onl_drag_page" IS '可视化拖拽界面';
COMMENT ON COLUMN "onl_drag_page"."id" IS '主键';
COMMENT ON COLUMN "onl_drag_page"."name" IS '界面名称';
COMMENT ON COLUMN "onl_drag_page"."path" IS '访问路径';
COMMENT ON COLUMN "onl_drag_page"."background_color" IS '背景色';
COMMENT ON COLUMN "onl_drag_page"."background_image" IS '背景图';
COMMENT ON COLUMN "onl_drag_page"."design_type" IS '设计模式(1:pc,2:手机,3:平板)';
COMMENT ON COLUMN "onl_drag_page"."theme" IS '主题色';
COMMENT ON COLUMN "onl_drag_page"."style" IS '面板主题';
COMMENT ON COLUMN "onl_drag_page"."cover_url" IS '封面图';
COMMENT ON COLUMN "onl_drag_page"."template" IS '布局json';
COMMENT ON COLUMN "onl_drag_page"."protection_code" IS '保护码';
COMMENT ON COLUMN "onl_drag_page"."type" IS '分类(1:仪表盘设计 2:门户设计)';
COMMENT ON COLUMN "onl_drag_page"."iz_template" IS '是否模板(1:是；0不是)';
COMMENT ON COLUMN "onl_drag_page"."create_by" IS '创建人登录名称';
COMMENT ON COLUMN "onl_drag_page"."create_time" IS '创建日期';
COMMENT ON COLUMN "onl_drag_page"."update_by" IS '更新人登录名称';
COMMENT ON COLUMN "onl_drag_page"."update_time" IS '更新日期';
COMMENT ON COLUMN "onl_drag_page"."low_app_id" IS '应用ID';
COMMENT ON COLUMN "onl_drag_page"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "onl_drag_page"."visits_num" IS '访问次数';

COMMENT ON TABLE "onl_drag_page_comp" IS '可视化拖拽页面组件';
COMMENT ON COLUMN "onl_drag_page_comp"."id" IS '主键';
COMMENT ON COLUMN "onl_drag_page_comp"."parent_id" IS '父组件ID';
COMMENT ON COLUMN "onl_drag_page_comp"."page_Id" IS '界面ID';
COMMENT ON COLUMN "onl_drag_page_comp"."comp_id" IS '组件库ID';
COMMENT ON COLUMN "onl_drag_page_comp"."component" IS '组件名称';
COMMENT ON COLUMN "onl_drag_page_comp"."config" IS '组件配置';
COMMENT ON COLUMN "onl_drag_page_comp"."create_by" IS '创建人登录名称';
COMMENT ON COLUMN "onl_drag_page_comp"."create_time" IS '创建日期';
COMMENT ON COLUMN "onl_drag_page_comp"."update_by" IS '更新人登录名称';
COMMENT ON COLUMN "onl_drag_page_comp"."update_time" IS '更新日期';

COMMENT ON TABLE "onl_drag_table_relation" IS '仪表盘聚合表';
COMMENT ON COLUMN "onl_drag_table_relation"."id" IS '主键';
COMMENT ON COLUMN "onl_drag_table_relation"."aggregation_name" IS '聚合表名称';
COMMENT ON COLUMN "onl_drag_table_relation"."aggregation_desc" IS '聚合表描述';
COMMENT ON COLUMN "onl_drag_table_relation"."relation_forms" IS '关联表单';
COMMENT ON COLUMN "onl_drag_table_relation"."filter_condition" IS '过滤条件';
COMMENT ON COLUMN "onl_drag_table_relation"."header_fields" IS '表头字段';
COMMENT ON COLUMN "onl_drag_table_relation"."calculate_fields" IS '公式字段';
COMMENT ON COLUMN "onl_drag_table_relation"."validate_info" IS '校验信息';
COMMENT ON COLUMN "onl_drag_table_relation"."del_flag" IS '删除状态(0-正常,1-已删除)';
COMMENT ON COLUMN "onl_drag_table_relation"."low_app_id" IS '应用ID';
COMMENT ON COLUMN "onl_drag_table_relation"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "onl_drag_table_relation"."create_by" IS '创建人登录名称';
COMMENT ON COLUMN "onl_drag_table_relation"."create_time" IS '创建日期';
COMMENT ON COLUMN "onl_drag_table_relation"."update_by" IS '更新人登录名称';
COMMENT ON COLUMN "onl_drag_table_relation"."update_time" IS '更新日期';

-- ----------------------------
-- Indexes
-- ----------------------------
CREATE UNIQUE INDEX "uk_sd_dict_code" ON "jimu_dict" ("dict_code");
CREATE INDEX "idx_sdi_role_dict_id" ON "jimu_dict_item" ("dict_id");
CREATE INDEX "idx_sdi_role_sort_order" ON "jimu_dict_item" ("sort_order");
CREATE INDEX "idx_sdi_status" ON "jimu_dict_item" ("status");
CREATE INDEX "idx_sdi_dict_val" ON "jimu_dict_item" ("dict_id", "item_value");
CREATE UNIQUE INDEX "uniq_jmreport_code" ON "jimu_report" ("code");
CREATE INDEX "uniq_jmreport_createby" ON "jimu_report" ("create_by");
CREATE INDEX "uniq_jmreport_delflag" ON "jimu_report" ("del_flag");
CREATE INDEX "idx_jmdatasource_report_id" ON "jimu_report_data_source" ("report_id");
CREATE INDEX "idx_jmdatasource_code" ON "jimu_report_data_source" ("code");
CREATE INDEX "idx_jmreportdb_db_key" ON "jimu_report_db" ("db_key");
CREATE INDEX "idx_jimu_report_id" ON "jimu_report_db" ("jimu_report_id");
CREATE INDEX "idx_db_source_id" ON "jimu_report_db" ("db_source");
CREATE INDEX "idx_jrdf_jimu_report_db_id" ON "jimu_report_db_field" ("jimu_report_db_id");
CREATE INDEX "idx_dbfield_order_num" ON "jimu_report_db_field" ("order_num");
CREATE INDEX "idx_jmrheadid" ON "jimu_report_db_param" ("jimu_report_head_id");
CREATE INDEX "idx_jrdp_jimu_report_head_id" ON "jimu_report_db_param" ("jimu_report_head_id");
CREATE INDEX "uniq_link_reportid" ON "jimu_report_link" ("report_id");
CREATE UNIQUE INDEX "uniq_jmreport_map_name" ON "jimu_report_map" ("name");
CREATE UNIQUE INDEX "uniq_report_id" ON "jimu_report_share" ("report_id");
CREATE UNIQUE INDEX "uniq_jrs_share_token" ON "jimu_report_share" ("share_token");
CREATE INDEX "idx_oddi_head_id" ON "onl_drag_dataset_item" ("head_id");
CREATE INDEX "idx_oddp_head_id" ON "onl_drag_dataset_param" ("head_id");
CREATE INDEX "idx_aggregation_name" ON "onl_drag_table_relation" ("aggregation_name");
CREATE INDEX "idx_del_flag" ON "onl_drag_table_relation" ("del_flag");
CREATE INDEX "idx_tenant_id" ON "onl_drag_table_relation" ("tenant_id");
CREATE INDEX "idx_create_by" ON "onl_drag_table_relation" ("create_by");
