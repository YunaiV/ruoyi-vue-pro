/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1 PostgreSQL
 Source Server Type    : PostgreSQL
 Source Server Version : 140002
 Source Host           : 127.0.0.1:5432
 Source Catalog        : ruoyi-vue-pro
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 140002
 File Encoding         : 65001

 Date: 28/07/2022 23:48:10
*/


-- ----------------------------
-- Sequence structure for bpm_form_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "bpm_form_seq";
CREATE SEQUENCE "bpm_form_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for bpm_oa_leave_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "bpm_oa_leave_seq";
CREATE SEQUENCE "bpm_oa_leave_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for bpm_process_definition_ext_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "bpm_process_definition_ext_seq";
CREATE SEQUENCE "bpm_process_definition_ext_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for bpm_process_instance_ext_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "bpm_process_instance_ext_seq";
CREATE SEQUENCE "bpm_process_instance_ext_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for bpm_task_assign_rule_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "bpm_task_assign_rule_seq";
CREATE SEQUENCE "bpm_task_assign_rule_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for bpm_task_ext_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "bpm_task_ext_seq";
CREATE SEQUENCE "bpm_task_ext_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for bpm_user_group_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "bpm_user_group_seq";
CREATE SEQUENCE "bpm_user_group_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for infra_api_access_log_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "infra_api_access_log_seq";
CREATE SEQUENCE "infra_api_access_log_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for infra_api_error_log_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "infra_api_error_log_seq";
CREATE SEQUENCE "infra_api_error_log_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for infra_codegen_column_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "infra_codegen_column_seq";
CREATE SEQUENCE "infra_codegen_column_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for infra_codegen_table_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "infra_codegen_table_seq";
CREATE SEQUENCE "infra_codegen_table_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for infra_config_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "infra_config_seq";
CREATE SEQUENCE "infra_config_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for infra_data_source_config_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "infra_data_source_config_seq";
CREATE SEQUENCE "infra_data_source_config_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for infra_file_config_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "infra_file_config_seq";
CREATE SEQUENCE "infra_file_config_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for infra_file_content_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "infra_file_content_seq";
CREATE SEQUENCE "infra_file_content_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for infra_file_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "infra_file_seq";
CREATE SEQUENCE "infra_file_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for infra_job_log_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "infra_job_log_seq";
CREATE SEQUENCE "infra_job_log_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for infra_job_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "infra_job_seq";
CREATE SEQUENCE "infra_job_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for infra_test_demo_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "infra_test_demo_seq";
CREATE SEQUENCE "infra_test_demo_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for member_user_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "member_user_seq";
CREATE SEQUENCE "member_user_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for pay_app_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "pay_app_seq";
CREATE SEQUENCE "pay_app_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for pay_channel_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "pay_channel_seq";
CREATE SEQUENCE "pay_channel_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for pay_merchant_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "pay_merchant_seq";
CREATE SEQUENCE "pay_merchant_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for pay_notify_log_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "pay_notify_log_seq";
CREATE SEQUENCE "pay_notify_log_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for pay_notify_task_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "pay_notify_task_seq";
CREATE SEQUENCE "pay_notify_task_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for pay_order_extension_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "pay_order_extension_seq";
CREATE SEQUENCE "pay_order_extension_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for pay_order_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "pay_order_seq";
CREATE SEQUENCE "pay_order_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for pay_refund_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "pay_refund_seq";
CREATE SEQUENCE "pay_refund_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_dept_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_dept_seq";
CREATE SEQUENCE "system_dept_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_dict_data_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_dict_data_seq";
CREATE SEQUENCE "system_dict_data_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_dict_type_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_dict_type_seq";
CREATE SEQUENCE "system_dict_type_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_error_code_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_error_code_seq";
CREATE SEQUENCE "system_error_code_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_login_log_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_login_log_seq";
CREATE SEQUENCE "system_login_log_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_menu_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_menu_seq";
CREATE SEQUENCE "system_menu_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_notice_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_notice_seq";
CREATE SEQUENCE "system_notice_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_oauth2_access_token_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_oauth2_access_token_seq";
CREATE SEQUENCE "system_oauth2_access_token_seq"
    INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for system_oauth2_approve_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_oauth2_approve_seq";
CREATE SEQUENCE "system_oauth2_approve_seq"
    INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for system_oauth2_client_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_oauth2_client_seq";
CREATE SEQUENCE "system_oauth2_client_seq"
    INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for system_oauth2_code_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_oauth2_code_seq";
CREATE SEQUENCE "system_oauth2_code_seq"
    INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for system_oauth2_refresh_token_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_oauth2_refresh_token_seq";
CREATE SEQUENCE "system_oauth2_refresh_token_seq"
    INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for system_operate_log_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_operate_log_seq";
CREATE SEQUENCE "system_operate_log_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_post_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_post_seq";
CREATE SEQUENCE "system_post_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_role_menu_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_role_menu_seq";
CREATE SEQUENCE "system_role_menu_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_role_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_role_seq";
CREATE SEQUENCE "system_role_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_sensitive_word_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_sensitive_word_seq";
CREATE SEQUENCE "system_sensitive_word_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_sms_channel_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_sms_channel_seq";
CREATE SEQUENCE "system_sms_channel_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_sms_code_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_sms_code_seq";
CREATE SEQUENCE "system_sms_code_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_sms_log_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_sms_log_seq";
CREATE SEQUENCE "system_sms_log_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_sms_template_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_sms_template_seq";
CREATE SEQUENCE "system_sms_template_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_social_user_bind_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_social_user_bind_seq";
CREATE SEQUENCE "system_social_user_bind_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_social_user_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_social_user_seq";
CREATE SEQUENCE "system_social_user_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_tenant_package_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_tenant_package_seq";
CREATE SEQUENCE "system_tenant_package_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_tenant_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_tenant_seq";
CREATE SEQUENCE "system_tenant_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_user_post_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_user_post_seq";
CREATE SEQUENCE "system_user_post_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_user_role_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_user_role_seq";
CREATE SEQUENCE "system_user_role_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_user_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_user_seq";
CREATE SEQUENCE "system_user_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_mail_account_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_mail_account_seq";
CREATE SEQUENCE "system_mail_account_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_mail_log_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_mail_log_seq";
CREATE SEQUENCE "system_mail_log_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_mail_template_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_mail_template_seq";
CREATE SEQUENCE "system_mail_template_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_notify_message_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_notify_message_seq";
CREATE SEQUENCE "system_notify_message_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_notify_template_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_notify_template_seq";
CREATE SEQUENCE "system_notify_template_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Sequence structure for system_user_session_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "system_user_session_seq";
CREATE SEQUENCE "system_user_session_seq"
    INCREMENT 1
MAXVALUE 9223372036854775807
CACHE 1;

-- ----------------------------
-- Table structure for bpm_form
-- ----------------------------
DROP TABLE IF EXISTS "bpm_form";
CREATE TABLE "bpm_form"
(
    "id"          int8                                         NOT NULL,
    "name"        varchar(64) COLLATE "pg_catalog"."default"   NOT NULL,
    "status"      int2                                         NOT NULL,
    "conf"        varchar(1000) COLLATE "pg_catalog"."default" NOT NULL,
    "fields"      varchar(5000) COLLATE "pg_catalog"."default" NOT NULL,
    "remark"      varchar(255) COLLATE "pg_catalog"."default",
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                                 NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                                 NOT NULL,
    "deleted"     int2                                         NOT NULL DEFAULT 0,
    "tenant_id"   int8                                         NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "bpm_form"."id" IS '编号';
COMMENT
ON COLUMN "bpm_form"."name" IS '表单名';
COMMENT
ON COLUMN "bpm_form"."status" IS '开启状态';
COMMENT
ON COLUMN "bpm_form"."conf" IS '表单的配置';
COMMENT
ON COLUMN "bpm_form"."fields" IS '表单项的数组';
COMMENT
ON COLUMN "bpm_form"."remark" IS '备注';
COMMENT
ON COLUMN "bpm_form"."creator" IS '创建者';
COMMENT
ON COLUMN "bpm_form"."create_time" IS '创建时间';
COMMENT
ON COLUMN "bpm_form"."updater" IS '更新者';
COMMENT
ON COLUMN "bpm_form"."update_time" IS '更新时间';
COMMENT
ON COLUMN "bpm_form"."deleted" IS '是否删除';
COMMENT
ON COLUMN "bpm_form"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "bpm_form" IS '工作流的表单定义';

-- ----------------------------
-- Records of bpm_form
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for bpm_oa_leave
-- ----------------------------
DROP TABLE IF EXISTS "bpm_oa_leave";
CREATE TABLE "bpm_oa_leave"
(
    "id"                  int8                                        NOT NULL,
    "user_id"             int8                                        NOT NULL,
    "type"                int2                                        NOT NULL,
    "reason"              varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "start_time"          timestamp(6)                                NOT NULL,
    "end_time"            timestamp(6)                                NOT NULL,
    "day"                 int2                                        NOT NULL,
    "result"              int2                                        NOT NULL,
    "process_instance_id" varchar(64) COLLATE "pg_catalog"."default",
    "creator"             varchar(64) COLLATE "pg_catalog"."default"           DEFAULT '':: character varying,
    "create_time"         timestamp(6)                                NOT NULL,
    "updater"             varchar(64) COLLATE "pg_catalog"."default"           DEFAULT '':: character varying,
    "update_time"         timestamp(6)                                NOT NULL,
    "deleted"             int2                                        NOT NULL DEFAULT 0,
    "tenant_id"           int8                                        NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "bpm_oa_leave"."id" IS '请假表单主键';
COMMENT
ON COLUMN "bpm_oa_leave"."user_id" IS '申请人的用户编号';
COMMENT
ON COLUMN "bpm_oa_leave"."type" IS '请假类型';
COMMENT
ON COLUMN "bpm_oa_leave"."reason" IS '请假原因';
COMMENT
ON COLUMN "bpm_oa_leave"."start_time" IS '开始时间';
COMMENT
ON COLUMN "bpm_oa_leave"."end_time" IS '结束时间';
COMMENT
ON COLUMN "bpm_oa_leave"."day" IS '请假天数';
COMMENT
ON COLUMN "bpm_oa_leave"."result" IS '请假结果';
COMMENT
ON COLUMN "bpm_oa_leave"."process_instance_id" IS '流程实例的编号';
COMMENT
ON COLUMN "bpm_oa_leave"."creator" IS '创建者';
COMMENT
ON COLUMN "bpm_oa_leave"."create_time" IS '创建时间';
COMMENT
ON COLUMN "bpm_oa_leave"."updater" IS '更新者';
COMMENT
ON COLUMN "bpm_oa_leave"."update_time" IS '更新时间';
COMMENT
ON COLUMN "bpm_oa_leave"."deleted" IS '是否删除';
COMMENT
ON COLUMN "bpm_oa_leave"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "bpm_oa_leave" IS 'OA 请假申请表';

-- ----------------------------
-- Records of bpm_oa_leave
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for bpm_process_definition_ext
-- ----------------------------
DROP TABLE IF EXISTS "bpm_process_definition_ext";
CREATE TABLE "bpm_process_definition_ext"
(
    "id"                      int8                                       NOT NULL,
    "process_definition_id"   varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "model_id"                varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "description"             varchar(255) COLLATE "pg_catalog"."default",
    "form_type"               int2                                       NOT NULL,
    "form_id"                 int8,
    "form_conf"               varchar(1000) COLLATE "pg_catalog"."default",
    "form_fields"             varchar(5000) COLLATE "pg_catalog"."default",
    "form_custom_create_path" varchar(255) COLLATE "pg_catalog"."default",
    "form_custom_view_path"   varchar(255) COLLATE "pg_catalog"."default",
    "creator"                 varchar(64) COLLATE "pg_catalog"."default",
    "create_time"             timestamp(6)                               NOT NULL,
    "updater"                 varchar(64) COLLATE "pg_catalog"."default",
    "update_time"             timestamp(6)                               NOT NULL,
    "deleted"                 int2                                       NOT NULL DEFAULT 0,
    "tenant_id"               int8                                       NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "bpm_process_definition_ext"."id" IS '编号';
COMMENT
ON COLUMN "bpm_process_definition_ext"."process_definition_id" IS '流程定义的编号';
COMMENT
ON COLUMN "bpm_process_definition_ext"."model_id" IS '流程模型的编号';
COMMENT
ON COLUMN "bpm_process_definition_ext"."description" IS '描述';
COMMENT
ON COLUMN "bpm_process_definition_ext"."form_type" IS '表单类型';
COMMENT
ON COLUMN "bpm_process_definition_ext"."form_id" IS '表单编号';
COMMENT
ON COLUMN "bpm_process_definition_ext"."form_conf" IS '表单的配置';
COMMENT
ON COLUMN "bpm_process_definition_ext"."form_fields" IS '表单项的数组';
COMMENT
ON COLUMN "bpm_process_definition_ext"."form_custom_create_path" IS '自定义表单的提交路径';
COMMENT
ON COLUMN "bpm_process_definition_ext"."form_custom_view_path" IS '自定义表单的查看路径';
COMMENT
ON COLUMN "bpm_process_definition_ext"."creator" IS '创建者';
COMMENT
ON COLUMN "bpm_process_definition_ext"."create_time" IS '创建时间';
COMMENT
ON COLUMN "bpm_process_definition_ext"."updater" IS '更新者';
COMMENT
ON COLUMN "bpm_process_definition_ext"."update_time" IS '更新时间';
COMMENT
ON COLUMN "bpm_process_definition_ext"."deleted" IS '是否删除';
COMMENT
ON COLUMN "bpm_process_definition_ext"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "bpm_process_definition_ext" IS 'Bpm 流程定义的拓展表
';

-- ----------------------------
-- Records of bpm_process_definition_ext
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for bpm_process_instance_ext
-- ----------------------------
DROP TABLE IF EXISTS "bpm_process_instance_ext";
CREATE TABLE "bpm_process_instance_ext"
(
    "id"                    int8                                       NOT NULL,
    "start_user_id"         int8                                       NOT NULL,
    "name"                  varchar(64) COLLATE "pg_catalog"."default",
    "process_instance_id"   varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "process_definition_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "category"              varchar(64) COLLATE "pg_catalog"."default",
    "status"                int2                                       NOT NULL,
    "result"                int2                                       NOT NULL,
    "end_time"              timestamp(6),
    "form_variables"        varchar(5000) COLLATE "pg_catalog"."default",
    "creator"               varchar(64) COLLATE "pg_catalog"."default",
    "create_time"           timestamp(6)                               NOT NULL,
    "updater"               varchar(64) COLLATE "pg_catalog"."default",
    "update_time"           timestamp(6)                               NOT NULL,
    "deleted"               int2                                       NOT NULL DEFAULT 0,
    "tenant_id"             int8                                       NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "bpm_process_instance_ext"."id" IS '编号';
COMMENT
ON COLUMN "bpm_process_instance_ext"."start_user_id" IS '发起流程的用户编号';
COMMENT
ON COLUMN "bpm_process_instance_ext"."name" IS '流程实例的名字';
COMMENT
ON COLUMN "bpm_process_instance_ext"."process_instance_id" IS '流程实例的编号';
COMMENT
ON COLUMN "bpm_process_instance_ext"."process_definition_id" IS '流程定义的编号';
COMMENT
ON COLUMN "bpm_process_instance_ext"."category" IS '流程分类';
COMMENT
ON COLUMN "bpm_process_instance_ext"."status" IS '流程实例的状态';
COMMENT
ON COLUMN "bpm_process_instance_ext"."result" IS '流程实例的结果';
COMMENT
ON COLUMN "bpm_process_instance_ext"."end_time" IS '结束时间';
COMMENT
ON COLUMN "bpm_process_instance_ext"."form_variables" IS '表单值';
COMMENT
ON COLUMN "bpm_process_instance_ext"."creator" IS '创建者';
COMMENT
ON COLUMN "bpm_process_instance_ext"."create_time" IS '创建时间';
COMMENT
ON COLUMN "bpm_process_instance_ext"."updater" IS '更新者';
COMMENT
ON COLUMN "bpm_process_instance_ext"."update_time" IS '更新时间';
COMMENT
ON COLUMN "bpm_process_instance_ext"."deleted" IS '是否删除';
COMMENT
ON COLUMN "bpm_process_instance_ext"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "bpm_process_instance_ext" IS '工作流的流程实例的拓展';

-- ----------------------------
-- Records of bpm_process_instance_ext
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for bpm_task_assign_rule
-- ----------------------------
DROP TABLE IF EXISTS "bpm_task_assign_rule";
CREATE TABLE "bpm_task_assign_rule"
(
    "id"                    int8                                         NOT NULL,
    "model_id"              varchar(64) COLLATE "pg_catalog"."default"   NOT NULL,
    "process_definition_id" varchar(64) COLLATE "pg_catalog"."default"   NOT NULL,
    "task_definition_key"   varchar(64) COLLATE "pg_catalog"."default"   NOT NULL,
    "type"                  int2                                         NOT NULL,
    "options"               varchar(1024) COLLATE "pg_catalog"."default" NOT NULL,
    "creator"               varchar(64) COLLATE "pg_catalog"."default",
    "create_time"           timestamp(6)                                 NOT NULL,
    "updater"               varchar(64) COLLATE "pg_catalog"."default",
    "update_time"           timestamp(6)                                 NOT NULL,
    "deleted"               int2                                         NOT NULL DEFAULT 0,
    "tenant_id"             int8                                         NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "bpm_task_assign_rule"."id" IS '编号';
COMMENT
ON COLUMN "bpm_task_assign_rule"."model_id" IS '流程模型的编号';
COMMENT
ON COLUMN "bpm_task_assign_rule"."process_definition_id" IS '流程定义的编号';
COMMENT
ON COLUMN "bpm_task_assign_rule"."task_definition_key" IS '流程任务定义的 key';
COMMENT
ON COLUMN "bpm_task_assign_rule"."type" IS '规则类型';
COMMENT
ON COLUMN "bpm_task_assign_rule"."options" IS '规则值，JSON 数组';
COMMENT
ON COLUMN "bpm_task_assign_rule"."creator" IS '创建者';
COMMENT
ON COLUMN "bpm_task_assign_rule"."create_time" IS '创建时间';
COMMENT
ON COLUMN "bpm_task_assign_rule"."updater" IS '更新者';
COMMENT
ON COLUMN "bpm_task_assign_rule"."update_time" IS '更新时间';
COMMENT
ON COLUMN "bpm_task_assign_rule"."deleted" IS '是否删除';
COMMENT
ON COLUMN "bpm_task_assign_rule"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "bpm_task_assign_rule" IS 'Bpm 任务规则表';

-- ----------------------------
-- Records of bpm_task_assign_rule
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for bpm_task_ext
-- ----------------------------
DROP TABLE IF EXISTS "bpm_task_ext";
CREATE TABLE "bpm_task_ext"
(
    "id"                    int8                                       NOT NULL,
    "assignee_user_id"      int8,
    "name"                  varchar(64) COLLATE "pg_catalog"."default",
    "task_id"               varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "result"                int2                                       NOT NULL,
    "reason"                varchar(255) COLLATE "pg_catalog"."default",
    "end_time"              timestamp(6),
    "process_instance_id"   varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "process_definition_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "creator"               varchar(64) COLLATE "pg_catalog"."default",
    "create_time"           timestamp(6)                               NOT NULL,
    "updater"               varchar(64) COLLATE "pg_catalog"."default",
    "update_time"           timestamp(6)                               NOT NULL,
    "deleted"               int2                                       NOT NULL DEFAULT 0,
    "tenant_id"             int8                                       NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "bpm_task_ext"."id" IS '编号';
COMMENT
ON COLUMN "bpm_task_ext"."assignee_user_id" IS '任务的审批人';
COMMENT
ON COLUMN "bpm_task_ext"."name" IS '任务的名字';
COMMENT
ON COLUMN "bpm_task_ext"."task_id" IS '任务的编号';
COMMENT
ON COLUMN "bpm_task_ext"."result" IS '任务的结果';
COMMENT
ON COLUMN "bpm_task_ext"."reason" IS '审批建议';
COMMENT
ON COLUMN "bpm_task_ext"."end_time" IS '任务的结束时间';
COMMENT
ON COLUMN "bpm_task_ext"."process_instance_id" IS '流程实例的编号';
COMMENT
ON COLUMN "bpm_task_ext"."process_definition_id" IS '流程定义的编号';
COMMENT
ON COLUMN "bpm_task_ext"."creator" IS '创建者';
COMMENT
ON COLUMN "bpm_task_ext"."create_time" IS '创建时间';
COMMENT
ON COLUMN "bpm_task_ext"."updater" IS '更新者';
COMMENT
ON COLUMN "bpm_task_ext"."update_time" IS '更新时间';
COMMENT
ON COLUMN "bpm_task_ext"."deleted" IS '是否删除';
COMMENT
ON COLUMN "bpm_task_ext"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "bpm_task_ext" IS '工作流的流程任务的拓展表';

-- ----------------------------
-- Records of bpm_task_ext
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for bpm_user_group
-- ----------------------------
DROP TABLE IF EXISTS "bpm_user_group";
CREATE TABLE "bpm_user_group"
(
    "id"              int8                                         NOT NULL,
    "name"            varchar(30) COLLATE "pg_catalog"."default"   NOT NULL,
    "description"     varchar(255) COLLATE "pg_catalog"."default"  NOT NULL,
    "member_user_ids" varchar(1024) COLLATE "pg_catalog"."default" NOT NULL,
    "status"          int2                                         NOT NULL,
    "creator"         varchar(64) COLLATE "pg_catalog"."default",
    "create_time"     timestamp(6)                                 NOT NULL,
    "updater"         varchar(64) COLLATE "pg_catalog"."default",
    "update_time"     timestamp(6)                                 NOT NULL,
    "deleted"         int2                                         NOT NULL DEFAULT 0,
    "tenant_id"       int8                                         NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "bpm_user_group"."id" IS '编号';
COMMENT
ON COLUMN "bpm_user_group"."name" IS '组名';
COMMENT
ON COLUMN "bpm_user_group"."description" IS '描述';
COMMENT
ON COLUMN "bpm_user_group"."member_user_ids" IS '成员编号数组';
COMMENT
ON COLUMN "bpm_user_group"."status" IS '状态（0正常 1停用）';
COMMENT
ON COLUMN "bpm_user_group"."creator" IS '创建者';
COMMENT
ON COLUMN "bpm_user_group"."create_time" IS '创建时间';
COMMENT
ON COLUMN "bpm_user_group"."updater" IS '更新者';
COMMENT
ON COLUMN "bpm_user_group"."update_time" IS '更新时间';
COMMENT
ON COLUMN "bpm_user_group"."deleted" IS '是否删除';
COMMENT
ON COLUMN "bpm_user_group"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "bpm_user_group" IS '用户组';

-- ----------------------------
-- Records of bpm_user_group
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for dual
-- ----------------------------
DROP TABLE IF EXISTS "dual";
CREATE TABLE "dual"
(

)
;

-- ----------------------------
-- Records of dual
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for infra_api_access_log
-- ----------------------------
DROP TABLE IF EXISTS "infra_api_access_log";
CREATE TABLE "infra_api_access_log"
(
    "id"               int8                                         NOT NULL,
    "trace_id"         varchar(64) COLLATE "pg_catalog"."default"   NOT NULL,
    "user_id"          int8                                         NOT NULL DEFAULT 0,
    "user_type"        int2                                         NOT NULL DEFAULT 0,
    "application_name" varchar(50) COLLATE "pg_catalog"."default"   NOT NULL,
    "request_method"   varchar(16) COLLATE "pg_catalog"."default"   NOT NULL,
    "request_url"      varchar(255) COLLATE "pg_catalog"."default"  NOT NULL,
    "request_params"   varchar(8000) COLLATE "pg_catalog"."default" NOT NULL,
    "user_ip"          varchar(50) COLLATE "pg_catalog"."default"   NOT NULL,
    "user_agent"       varchar(512) COLLATE "pg_catalog"."default"  NOT NULL,
    "begin_time"       timestamp(6)                                 NOT NULL,
    "end_time"         timestamp(6)                                 NOT NULL,
    "duration"         int4                                         NOT NULL,
    "result_code"      int4                                         NOT NULL,
    "result_msg"       varchar(512) COLLATE "pg_catalog"."default",
    "creator"          varchar(64) COLLATE "pg_catalog"."default",
    "create_time"      timestamp(6)                                 NOT NULL,
    "updater"          varchar(64) COLLATE "pg_catalog"."default",
    "update_time"      timestamp(6)                                 NOT NULL,
    "deleted"          int2                                         NOT NULL DEFAULT 0,
    "tenant_id"        int8                                         NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "infra_api_access_log"."id" IS '日志主键';
COMMENT
ON COLUMN "infra_api_access_log"."trace_id" IS '链路追踪编号';
COMMENT
ON COLUMN "infra_api_access_log"."user_id" IS '用户编号';
COMMENT
ON COLUMN "infra_api_access_log"."user_type" IS '用户类型';
COMMENT
ON COLUMN "infra_api_access_log"."application_name" IS '应用名';
COMMENT
ON COLUMN "infra_api_access_log"."request_method" IS '请求方法名';
COMMENT
ON COLUMN "infra_api_access_log"."request_url" IS '请求地址';
COMMENT
ON COLUMN "infra_api_access_log"."request_params" IS '请求参数';
COMMENT
ON COLUMN "infra_api_access_log"."user_ip" IS '用户 IP';
COMMENT
ON COLUMN "infra_api_access_log"."user_agent" IS '浏览器 UA';
COMMENT
ON COLUMN "infra_api_access_log"."begin_time" IS '开始请求时间';
COMMENT
ON COLUMN "infra_api_access_log"."end_time" IS '结束请求时间';
COMMENT
ON COLUMN "infra_api_access_log"."duration" IS '执行时长';
COMMENT
ON COLUMN "infra_api_access_log"."result_code" IS '结果码';
COMMENT
ON COLUMN "infra_api_access_log"."result_msg" IS '结果提示';
COMMENT
ON COLUMN "infra_api_access_log"."creator" IS '创建者';
COMMENT
ON COLUMN "infra_api_access_log"."create_time" IS '创建时间';
COMMENT
ON COLUMN "infra_api_access_log"."updater" IS '更新者';
COMMENT
ON COLUMN "infra_api_access_log"."update_time" IS '更新时间';
COMMENT
ON COLUMN "infra_api_access_log"."deleted" IS '是否删除';
COMMENT
ON COLUMN "infra_api_access_log"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "infra_api_access_log" IS 'API 访问日志表';

-- ----------------------------
-- Records of infra_api_access_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for infra_api_error_log
-- ----------------------------
DROP TABLE IF EXISTS "infra_api_error_log";
CREATE TABLE "infra_api_error_log"
(
    "id"                           int4                                         NOT NULL,
    "trace_id"                     varchar(64) COLLATE "pg_catalog"."default"   NOT NULL,
    "user_id"                      int4                                         NOT NULL DEFAULT 0,
    "user_type"                    int2                                         NOT NULL DEFAULT 0,
    "application_name"             varchar(50) COLLATE "pg_catalog"."default"   NOT NULL,
    "request_method"               varchar(16) COLLATE "pg_catalog"."default"   NOT NULL,
    "request_url"                  varchar(255) COLLATE "pg_catalog"."default"  NOT NULL,
    "request_params"               varchar(8000) COLLATE "pg_catalog"."default" NOT NULL,
    "user_ip"                      varchar(50) COLLATE "pg_catalog"."default"   NOT NULL,
    "user_agent"                   varchar(512) COLLATE "pg_catalog"."default"  NOT NULL,
    "exception_time"               timestamp(6)                                 NOT NULL,
    "exception_name"               varchar(128) COLLATE "pg_catalog"."default"  NOT NULL,
    "exception_message"            text COLLATE "pg_catalog"."default"          NOT NULL,
    "exception_root_cause_message" text COLLATE "pg_catalog"."default"          NOT NULL,
    "exception_stack_trace"        text COLLATE "pg_catalog"."default"          NOT NULL,
    "exception_class_name"         varchar(512) COLLATE "pg_catalog"."default"  NOT NULL,
    "exception_file_name"          varchar(512) COLLATE "pg_catalog"."default"  NOT NULL,
    "exception_method_name"        varchar(512) COLLATE "pg_catalog"."default"  NOT NULL,
    "exception_line_number"        int4                                         NOT NULL,
    "process_status"               int2                                         NOT NULL,
    "process_time"                 timestamp(6),
    "process_user_id"              int4,
    "creator"                      varchar(64) COLLATE "pg_catalog"."default",
    "create_time"                  timestamp(6)                                 NOT NULL,
    "updater"                      varchar(64) COLLATE "pg_catalog"."default",
    "update_time"                  timestamp(6)                                 NOT NULL,
    "deleted"                      int2                                         NOT NULL DEFAULT 0,
    "tenant_id"                    int8                                         NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "infra_api_error_log"."id" IS '编号';
COMMENT
ON COLUMN "infra_api_error_log"."trace_id" IS '链路追踪编号
     *
     * 一般来说，通过链路追踪编号，可以将访问日志，错误日志，链路追踪日志，logger 打印日志等，结合在一起，从而进行排错。';
COMMENT
ON COLUMN "infra_api_error_log"."user_id" IS '用户编号';
COMMENT
ON COLUMN "infra_api_error_log"."user_type" IS '用户类型';
COMMENT
ON COLUMN "infra_api_error_log"."application_name" IS '应用名
     *
     * 目前读取 spring.application.name';
COMMENT
ON COLUMN "infra_api_error_log"."request_method" IS '请求方法名';
COMMENT
ON COLUMN "infra_api_error_log"."request_url" IS '请求地址';
COMMENT
ON COLUMN "infra_api_error_log"."request_params" IS '请求参数';
COMMENT
ON COLUMN "infra_api_error_log"."user_ip" IS '用户 IP';
COMMENT
ON COLUMN "infra_api_error_log"."user_agent" IS '浏览器 UA';
COMMENT
ON COLUMN "infra_api_error_log"."exception_time" IS '异常发生时间';
COMMENT
ON COLUMN "infra_api_error_log"."exception_name" IS '异常名
     *
     * {@link Throwable#getClass()} 的类全名';
COMMENT
ON COLUMN "infra_api_error_log"."exception_message" IS '异常导致的消息
     *
     * {@link cn.iocoder.common.framework.util.ExceptionUtil#getMessage(Throwable)}';
COMMENT
ON COLUMN "infra_api_error_log"."exception_root_cause_message" IS '异常导致的根消息
     *
     * {@link cn.iocoder.common.framework.util.ExceptionUtil#getRootCauseMessage(Throwable)}';
COMMENT
ON COLUMN "infra_api_error_log"."exception_stack_trace" IS '异常的栈轨迹
     *
     * {@link cn.iocoder.common.framework.util.ExceptionUtil#getServiceException(Exception)}';
COMMENT
ON COLUMN "infra_api_error_log"."exception_class_name" IS '异常发生的类全名
     *
     * {@link StackTraceElement#getClassName()}';
COMMENT
ON COLUMN "infra_api_error_log"."exception_file_name" IS '异常发生的类文件
     *
     * {@link StackTraceElement#getFileName()}';
COMMENT
ON COLUMN "infra_api_error_log"."exception_method_name" IS '异常发生的方法名
     *
     * {@link StackTraceElement#getMethodName()}';
COMMENT
ON COLUMN "infra_api_error_log"."exception_line_number" IS '异常发生的方法所在行
     *
     * {@link StackTraceElement#getLineNumber()}';
COMMENT
ON COLUMN "infra_api_error_log"."process_status" IS '处理状态';
COMMENT
ON COLUMN "infra_api_error_log"."process_time" IS '处理时间';
COMMENT
ON COLUMN "infra_api_error_log"."process_user_id" IS '处理用户编号';
COMMENT
ON COLUMN "infra_api_error_log"."creator" IS '创建者';
COMMENT
ON COLUMN "infra_api_error_log"."create_time" IS '创建时间';
COMMENT
ON COLUMN "infra_api_error_log"."updater" IS '更新者';
COMMENT
ON COLUMN "infra_api_error_log"."update_time" IS '更新时间';
COMMENT
ON COLUMN "infra_api_error_log"."deleted" IS '是否删除';
COMMENT
ON COLUMN "infra_api_error_log"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "infra_api_error_log" IS '系统异常日志';

-- ----------------------------
-- Records of infra_api_error_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for infra_codegen_column
-- ----------------------------
DROP TABLE IF EXISTS "infra_codegen_column";
CREATE TABLE "infra_codegen_column"
(
    "id"                       int8                                        NOT NULL,
    "table_id"                 int8                                        NOT NULL,
    "column_name"              varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "data_type"                varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
    "column_comment"           varchar(500) COLLATE "pg_catalog"."default" NOT NULL,
    "nullable"                 bool                                        NOT NULL,
    "primary_key"              bool                                        NOT NULL,
    "auto_increment"           bool                                        NOT NULL,
    "ordinal_position"         int4                                        NOT NULL,
    "java_type"                varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "java_field"               varchar(64) COLLATE "pg_catalog"."default"  NOT NULL,
    "dict_type"                varchar(200) COLLATE "pg_catalog"."default",
    "example"                  varchar(255) COLLATE "pg_catalog"."default",
    "create_operation"         bool                                        NOT NULL,
    "update_operation"         bool                                        NOT NULL,
    "list_operation"           bool                                        NOT NULL,
    "list_operation_condition" varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "list_operation_result"    bool                                        NOT NULL,
    "html_type"                varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "creator"                  varchar(64) COLLATE "pg_catalog"."default",
    "create_time"              timestamp(6)                                NOT NULL,
    "updater"                  varchar(64) COLLATE "pg_catalog"."default",
    "update_time"              timestamp(6)                                NOT NULL,
    "deleted"                  int2                                        NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "infra_codegen_column"."id" IS '编号';
COMMENT
ON COLUMN "infra_codegen_column"."table_id" IS '表编号';
COMMENT
ON COLUMN "infra_codegen_column"."column_name" IS '字段名';
COMMENT
ON COLUMN "infra_codegen_column"."data_type" IS '字段类型';
COMMENT
ON COLUMN "infra_codegen_column"."column_comment" IS '字段描述';
COMMENT
ON COLUMN "infra_codegen_column"."nullable" IS '是否允许为空';
COMMENT
ON COLUMN "infra_codegen_column"."primary_key" IS '是否主键';
COMMENT
ON COLUMN "infra_codegen_column"."auto_increment" IS '是否自增';
COMMENT
ON COLUMN "infra_codegen_column"."ordinal_position" IS '排序';
COMMENT
ON COLUMN "infra_codegen_column"."java_type" IS 'Java 属性类型';
COMMENT
ON COLUMN "infra_codegen_column"."java_field" IS 'Java 属性名';
COMMENT
ON COLUMN "infra_codegen_column"."dict_type" IS '字典类型';
COMMENT
ON COLUMN "infra_codegen_column"."example" IS '数据示例';
COMMENT
ON COLUMN "infra_codegen_column"."create_operation" IS '是否为 Create 创建操作的字段';
COMMENT
ON COLUMN "infra_codegen_column"."update_operation" IS '是否为 Update 更新操作的字段';
COMMENT
ON COLUMN "infra_codegen_column"."list_operation" IS '是否为 List 查询操作的字段';
COMMENT
ON COLUMN "infra_codegen_column"."list_operation_condition" IS 'List 查询操作的条件类型';
COMMENT
ON COLUMN "infra_codegen_column"."list_operation_result" IS '是否为 List 查询操作的返回字段';
COMMENT
ON COLUMN "infra_codegen_column"."html_type" IS '显示类型';
COMMENT
ON COLUMN "infra_codegen_column"."creator" IS '创建者';
COMMENT
ON COLUMN "infra_codegen_column"."create_time" IS '创建时间';
COMMENT
ON COLUMN "infra_codegen_column"."updater" IS '更新者';
COMMENT
ON COLUMN "infra_codegen_column"."update_time" IS '更新时间';
COMMENT
ON COLUMN "infra_codegen_column"."deleted" IS '是否删除';
COMMENT
ON TABLE "infra_codegen_column" IS '代码生成表字段定义';

-- ----------------------------
-- Records of infra_codegen_column
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for infra_codegen_table
-- ----------------------------
DROP TABLE IF EXISTS "infra_codegen_table";
CREATE TABLE "infra_codegen_table"
(
    "id"                    int8                                        NOT NULL,
    "data_source_config_id" int8                                        NOT NULL,
    "scene"                 int2                                        NOT NULL,
    "table_name"            varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "table_comment"         varchar(500) COLLATE "pg_catalog"."default" NOT NULL,
    "remark"                varchar(500) COLLATE "pg_catalog"."default",
    "module_name"           varchar(30) COLLATE "pg_catalog"."default"  NOT NULL,
    "business_name"         varchar(30) COLLATE "pg_catalog"."default"  NOT NULL,
    "class_name"            varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
    "class_comment"         varchar(50) COLLATE "pg_catalog"."default"  NOT NULL,
    "author"                varchar(50) COLLATE "pg_catalog"."default"  NOT NULL,
    "template_type"         int2                                        NOT NULL,
    "parent_menu_id"        int8,
    "creator"               varchar(64) COLLATE "pg_catalog"."default",
    "create_time"           timestamp(6)                                NOT NULL,
    "updater"               varchar(64) COLLATE "pg_catalog"."default",
    "update_time"           timestamp(6)                                NOT NULL,
    "deleted"               int2                                        NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "infra_codegen_table"."id" IS '编号';
COMMENT
ON COLUMN "infra_codegen_table"."data_source_config_id" IS '数据源配置的编号';
COMMENT
ON COLUMN "infra_codegen_table"."scene" IS '生成场景';
COMMENT
ON COLUMN "infra_codegen_table"."table_name" IS '表名称';
COMMENT
ON COLUMN "infra_codegen_table"."table_comment" IS '表描述';
COMMENT
ON COLUMN "infra_codegen_table"."remark" IS '备注';
COMMENT
ON COLUMN "infra_codegen_table"."module_name" IS '模块名';
COMMENT
ON COLUMN "infra_codegen_table"."business_name" IS '业务名';
COMMENT
ON COLUMN "infra_codegen_table"."class_name" IS '类名称';
COMMENT
ON COLUMN "infra_codegen_table"."class_comment" IS '类描述';
COMMENT
ON COLUMN "infra_codegen_table"."author" IS '作者';
COMMENT
ON COLUMN "infra_codegen_table"."template_type" IS '模板类型';
COMMENT
ON COLUMN "infra_codegen_table"."parent_menu_id" IS '父菜单编号';
COMMENT
ON COLUMN "infra_codegen_table"."creator" IS '创建者';
COMMENT
ON COLUMN "infra_codegen_table"."create_time" IS '创建时间';
COMMENT
ON COLUMN "infra_codegen_table"."updater" IS '更新者';
COMMENT
ON COLUMN "infra_codegen_table"."update_time" IS '更新时间';
COMMENT
ON COLUMN "infra_codegen_table"."deleted" IS '是否删除';
COMMENT
ON TABLE "infra_codegen_table" IS '代码生成表定义';

ALTER TABLE infra_codegen_table
    ADD front_type int4 NOT NULL;
COMMENT
ON COLUMN  infra_codegen_table.front_type IS '前端类型';

-- ----------------------------
-- Records of infra_codegen_table
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for infra_config
-- ----------------------------
DROP TABLE IF EXISTS "infra_config";
CREATE TABLE "infra_config"
(
    "id"          int4                                        NOT NULL,
    "category"    varchar(50) COLLATE "pg_catalog"."default"  NOT NULL,
    "type"        int2                                        NOT NULL,
    "name"        varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
    "config_key"  varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
    "value"       varchar(500) COLLATE "pg_catalog"."default" NOT NULL,
    "visible"     varchar(5) COLLATE "pg_catalog"."default"   NOT NULL,
    "remark"      varchar(500) COLLATE "pg_catalog"."default",
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                                NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                                NOT NULL,
    "deleted"     int2                                        NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "infra_config"."id" IS '参数主键';
COMMENT
ON COLUMN "infra_config"."category" IS '参数分组';
COMMENT
ON COLUMN "infra_config"."type" IS '参数类型';
COMMENT
ON COLUMN "infra_config"."name" IS '参数名称';
COMMENT
ON COLUMN "infra_config"."config_key" IS '参数键名';
COMMENT
ON COLUMN "infra_config"."value" IS '参数键值';
COMMENT
ON COLUMN "infra_config"."visible" IS '是否可见';
COMMENT
ON COLUMN "infra_config"."remark" IS '备注';
COMMENT
ON COLUMN "infra_config"."creator" IS '创建者';
COMMENT
ON COLUMN "infra_config"."create_time" IS '创建时间';
COMMENT
ON COLUMN "infra_config"."updater" IS '更新者';
COMMENT
ON COLUMN "infra_config"."update_time" IS '更新时间';
COMMENT
ON COLUMN "infra_config"."deleted" IS '是否删除';
COMMENT
ON TABLE "infra_config" IS '参数配置表';

-- ----------------------------
-- Records of infra_config
-- ----------------------------
BEGIN;
INSERT INTO "infra_config" ("id", "category", "type", "name", "config_key", "value", "visible", "remark", "creator",
                            "create_time", "updater", "update_time", "deleted")
VALUES (1, 'ui', 1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', '0',
        '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow', 'admin',
        '2021-01-05 17:03:48', '1', '2022-03-26 23:10:31', 0);
INSERT INTO "infra_config" ("id", "category", "type", "name", "config_key", "value", "visible", "remark", "creator",
                            "create_time", "updater", "update_time", "deleted")
VALUES (2, 'biz', 1, '用户管理-账号初始密码', 'sys.user.init-password', '123456', '0', '初始化密码 123456', 'admin',
        '2021-01-05 17:03:48', '1', '2022-03-20 02:25:51', 0);
INSERT INTO "infra_config" ("id", "category", "type", "name", "config_key", "value", "visible", "remark", "creator",
                            "create_time", "updater", "update_time", "deleted")
VALUES (3, 'ui', 1, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', '0',
        '深色主题theme-dark，浅色主题theme-light', 'admin', '2021-01-05 17:03:48', '', '2021-01-19 03:05:21', 0);
INSERT INTO "infra_config" ("id", "category", "type", "name", "config_key", "value", "visible", "remark", "creator",
                            "create_time", "updater", "update_time", "deleted")
VALUES (4, '1', 2, 'xxx', 'demo.test', '10', '0', '5', '', '2021-01-19 03:10:26', '', '2021-01-20 09:25:55', 0);
INSERT INTO "infra_config" ("id", "category", "type", "name", "config_key", "value", "visible", "remark", "creator",
                            "create_time", "updater", "update_time", "deleted")
VALUES (5, 'xxx', 2, 'xxx', 'xxx', 'xxx', '1', 'xxx', '', '2021-02-09 20:06:47', '', '2021-02-09 20:06:47', 0);
INSERT INTO "infra_config" ("id", "category", "type", "name", "config_key", "value", "visible", "remark", "creator",
                            "create_time", "updater", "update_time", "deleted")
VALUES (6, 'biz', 2, '登陆验证码的开关', 'yudao.captcha.enable', 'true', '1', NULL, '1', '2022-02-17 00:03:11', '1',
        '2022-04-04 12:51:40', 0);
COMMIT;

-- ----------------------------
-- Table structure for infra_data_source_config
-- ----------------------------
DROP TABLE IF EXISTS "infra_data_source_config";
CREATE TABLE "infra_data_source_config"
(
    "id"          int8                                         NOT NULL,
    "name"        varchar(100) COLLATE "pg_catalog"."default"  NOT NULL,
    "url"         varchar(1024) COLLATE "pg_catalog"."default" NOT NULL,
    "username"    varchar(255) COLLATE "pg_catalog"."default"  NOT NULL,
    "password"    varchar(255) COLLATE "pg_catalog"."default"  NOT NULL,
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                                 NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                                 NOT NULL,
    "deleted"     int2                                         NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "infra_data_source_config"."id" IS '主键编号';
COMMENT
ON COLUMN "infra_data_source_config"."name" IS '参数名称';
COMMENT
ON COLUMN "infra_data_source_config"."url" IS '数据源连接';
COMMENT
ON COLUMN "infra_data_source_config"."username" IS '用户名';
COMMENT
ON COLUMN "infra_data_source_config"."password" IS '密码';
COMMENT
ON COLUMN "infra_data_source_config"."creator" IS '创建者';
COMMENT
ON COLUMN "infra_data_source_config"."create_time" IS '创建时间';
COMMENT
ON COLUMN "infra_data_source_config"."updater" IS '更新者';
COMMENT
ON COLUMN "infra_data_source_config"."update_time" IS '更新时间';
COMMENT
ON COLUMN "infra_data_source_config"."deleted" IS '是否删除';
COMMENT
ON TABLE "infra_data_source_config" IS '数据源配置表';

-- ----------------------------
-- Records of infra_data_source_config
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for infra_file
-- ----------------------------
DROP TABLE IF EXISTS "infra_file";
CREATE TABLE "infra_file"
(
    "id"          int8                                         NOT NULL DEFAULT 0,
    "config_id"   int8,
    "path"        varchar(512) COLLATE "pg_catalog"."default"  NOT NULL,
    "url"         varchar(1024) COLLATE "pg_catalog"."default" NOT NULL,
    "type"        varchar(127) COLLATE "pg_catalog"."default",
    "size"        int4                                         NOT NULL,
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                                 NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                                 NOT NULL,
    "deleted"     int2                                         NOT NULL DEFAULT 0,
    "name"        varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT
ON COLUMN "infra_file"."id" IS '文件编号';
COMMENT
ON COLUMN "infra_file"."config_id" IS '配置编号';
COMMENT
ON COLUMN "infra_file"."path" IS '文件路径';
COMMENT
ON COLUMN "infra_file"."url" IS '文件 URL';
COMMENT
ON COLUMN "infra_file"."type" IS '文件类型';
COMMENT
ON COLUMN "infra_file"."size" IS '文件大小';
COMMENT
ON COLUMN "infra_file"."creator" IS '创建者';
COMMENT
ON COLUMN "infra_file"."create_time" IS '创建时间';
COMMENT
ON COLUMN "infra_file"."updater" IS '更新者';
COMMENT
ON COLUMN "infra_file"."update_time" IS '更新时间';
COMMENT
ON COLUMN "infra_file"."deleted" IS '是否删除';
COMMENT
ON COLUMN "infra_file"."name" IS '文件名';
COMMENT
ON TABLE "infra_file" IS '文件表';

-- ----------------------------
-- Records of infra_file
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for infra_file_config
-- ----------------------------
DROP TABLE IF EXISTS "infra_file_config";
CREATE TABLE "infra_file_config"
(
    "id"          int8                                         NOT NULL,
    "name"        varchar(63) COLLATE "pg_catalog"."default"   NOT NULL,
    "storage"     int2                                         NOT NULL,
    "remark"      varchar(255) COLLATE "pg_catalog"."default",
    "master"      bool                                         NOT NULL,
    "config"      varchar(4096) COLLATE "pg_catalog"."default" NOT NULL,
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                                 NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                                 NOT NULL,
    "deleted"     int2                                         NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "infra_file_config"."id" IS '编号';
COMMENT
ON COLUMN "infra_file_config"."name" IS '配置名';
COMMENT
ON COLUMN "infra_file_config"."storage" IS '存储器';
COMMENT
ON COLUMN "infra_file_config"."remark" IS '备注';
COMMENT
ON COLUMN "infra_file_config"."master" IS '是否为主配置';
COMMENT
ON COLUMN "infra_file_config"."config" IS '存储配置';
COMMENT
ON COLUMN "infra_file_config"."creator" IS '创建者';
COMMENT
ON COLUMN "infra_file_config"."create_time" IS '创建时间';
COMMENT
ON COLUMN "infra_file_config"."updater" IS '更新者';
COMMENT
ON COLUMN "infra_file_config"."update_time" IS '更新时间';
COMMENT
ON COLUMN "infra_file_config"."deleted" IS '是否删除';
COMMENT
ON TABLE "infra_file_config" IS '文件配置表';

-- ----------------------------
-- Records of infra_file_config
-- ----------------------------
BEGIN;
INSERT INTO "infra_file_config" ("id", "name", "storage", "remark", "master", "config", "creator", "create_time",
                                 "updater", "update_time", "deleted")
VALUES (4, '数据库', 1, '我是数据库', 'f',
        '{"@class":"cn.iocoder.yudao.framework.file.core.client.db.DBFileClientConfig","domain":"http://127.0.0.1:48080"}',
        '1', '2022-03-15 23:56:24', '1', '2022-03-26 21:39:26', 0);
INSERT INTO "infra_file_config" ("id", "name", "storage", "remark", "master", "config", "creator", "create_time",
                                 "updater", "update_time", "deleted")
VALUES (5, '本地磁盘', 10, '测试下本地存储', 'f',
        '{"@class":"cn.iocoder.yudao.framework.file.core.client.local.LocalFileClientConfig","basePath":"/Users/yunai/file_test","domain":"http://127.0.0.1:48080"}',
        '1', '2022-03-15 23:57:00', '1', '2022-03-26 21:39:26', 0);
INSERT INTO "infra_file_config" ("id", "name", "storage", "remark", "master", "config", "creator", "create_time",
                                 "updater", "update_time", "deleted")
VALUES (11, 'S3 - 七牛云', 20, NULL, 't',
        '{"@class":"cn.iocoder.yudao.framework.file.core.client.s3.S3FileClientConfig","endpoint":"s3-cn-south-1.qiniucs.com","domain":"http://test.yudao.iocoder.cn","bucket":"ruoyi-vue-pro","accessKey":"b7yvuhBSAGjmtPhMFcn9iMOxUOY_I06cA_p0ZUx8","accessSecret":"kXM1l5ia1RvSX3QaOEcwI3RLz3Y2rmNszWonKZtP"}',
        '1', '2022-03-19 18:00:03', '1', '2022-05-26 00:03:47.17', 0);
COMMIT;

-- ----------------------------
-- Table structure for infra_file_content
-- ----------------------------
DROP TABLE IF EXISTS "infra_file_content";
CREATE TABLE "infra_file_content"
(
    "id"          int8                                        NOT NULL,
    "config_id"   int8                                        NOT NULL,
    "path"        varchar(512) COLLATE "pg_catalog"."default" NOT NULL,
    "content"     bytea                                       NOT NULL,
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                                NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                                NOT NULL,
    "deleted"     int2                                        NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "infra_file_content"."id" IS '编号';
COMMENT
ON COLUMN "infra_file_content"."config_id" IS '配置编号';
COMMENT
ON COLUMN "infra_file_content"."path" IS '文件路径';
COMMENT
ON COLUMN "infra_file_content"."content" IS '文件内容';
COMMENT
ON COLUMN "infra_file_content"."creator" IS '创建者';
COMMENT
ON COLUMN "infra_file_content"."create_time" IS '创建时间';
COMMENT
ON COLUMN "infra_file_content"."updater" IS '更新者';
COMMENT
ON COLUMN "infra_file_content"."update_time" IS '更新时间';
COMMENT
ON COLUMN "infra_file_content"."deleted" IS '是否删除';
COMMENT
ON TABLE "infra_file_content" IS '文件表';

-- ----------------------------
-- Records of infra_file_content
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for infra_job
-- ----------------------------
DROP TABLE IF EXISTS "infra_job";
CREATE TABLE "infra_job"
(
    "id"              int8                                       NOT NULL,
    "name"            varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
    "status"          int2                                       NOT NULL,
    "handler_name"    varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "handler_param"   varchar(255) COLLATE "pg_catalog"."default",
    "cron_expression" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
    "retry_count"     int4                                       NOT NULL,
    "retry_interval"  int4                                       NOT NULL,
    "monitor_timeout" int4                                       NOT NULL,
    "creator"         varchar(64) COLLATE "pg_catalog"."default",
    "create_time"     timestamp(6)                               NOT NULL,
    "updater"         varchar(64) COLLATE "pg_catalog"."default",
    "update_time"     timestamp(6)                               NOT NULL,
    "deleted"         int2                                       NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "infra_job"."id" IS '任务编号';
COMMENT
ON COLUMN "infra_job"."name" IS '任务名称';
COMMENT
ON COLUMN "infra_job"."status" IS '任务状态';
COMMENT
ON COLUMN "infra_job"."handler_name" IS '处理器的名字';
COMMENT
ON COLUMN "infra_job"."handler_param" IS '处理器的参数';
COMMENT
ON COLUMN "infra_job"."cron_expression" IS 'CRON 表达式';
COMMENT
ON COLUMN "infra_job"."retry_count" IS '重试次数';
COMMENT
ON COLUMN "infra_job"."retry_interval" IS '重试间隔';
COMMENT
ON COLUMN "infra_job"."monitor_timeout" IS '监控超时时间';
COMMENT
ON COLUMN "infra_job"."creator" IS '创建者';
COMMENT
ON COLUMN "infra_job"."create_time" IS '创建时间';
COMMENT
ON COLUMN "infra_job"."updater" IS '更新者';
COMMENT
ON COLUMN "infra_job"."update_time" IS '更新时间';
COMMENT
ON COLUMN "infra_job"."deleted" IS '是否删除';
COMMENT
ON TABLE "infra_job" IS '定时任务表';

-- ----------------------------
-- Records of infra_job
-- ----------------------------
BEGIN;
INSERT INTO "infra_job" ("id", "name", "status", "handler_name", "handler_param", "cron_expression", "retry_count",
                         "retry_interval", "monitor_timeout", "creator", "create_time", "updater", "update_time",
                         "deleted")
VALUES (5, '支付通知 Job', 2, 'payNotifyJob', NULL, '* * * * * ?', 0, 0, 0, '1', '2021-10-27 08:34:42', '1',
        '2022-04-03 20:35:25', 0);
COMMIT;

-- ----------------------------
-- Table structure for infra_job_log
-- ----------------------------
DROP TABLE IF EXISTS "infra_job_log";
CREATE TABLE "infra_job_log"
(
    "id"            int8                                       NOT NULL,
    "job_id"        int8                                       NOT NULL,
    "handler_name"  varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "handler_param" varchar(255) COLLATE "pg_catalog"."default",
    "execute_index" int2                                       NOT NULL,
    "begin_time"    timestamp(6)                               NOT NULL,
    "end_time"      timestamp(6),
    "duration"      int4,
    "status"        int2                                       NOT NULL,
    "result"        varchar(4000) COLLATE "pg_catalog"."default",
    "creator"       varchar(64) COLLATE "pg_catalog"."default",
    "create_time"   timestamp(6)                               NOT NULL,
    "updater"       varchar(64) COLLATE "pg_catalog"."default",
    "update_time"   timestamp(6)                               NOT NULL,
    "deleted"       int2                                       NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "infra_job_log"."id" IS '日志编号';
COMMENT
ON COLUMN "infra_job_log"."job_id" IS '任务编号';
COMMENT
ON COLUMN "infra_job_log"."handler_name" IS '处理器的名字';
COMMENT
ON COLUMN "infra_job_log"."handler_param" IS '处理器的参数';
COMMENT
ON COLUMN "infra_job_log"."execute_index" IS '第几次执行';
COMMENT
ON COLUMN "infra_job_log"."begin_time" IS '开始执行时间';
COMMENT
ON COLUMN "infra_job_log"."end_time" IS '结束执行时间';
COMMENT
ON COLUMN "infra_job_log"."duration" IS '执行时长';
COMMENT
ON COLUMN "infra_job_log"."status" IS '任务状态';
COMMENT
ON COLUMN "infra_job_log"."result" IS '结果数据';
COMMENT
ON COLUMN "infra_job_log"."creator" IS '创建者';
COMMENT
ON COLUMN "infra_job_log"."create_time" IS '创建时间';
COMMENT
ON COLUMN "infra_job_log"."updater" IS '更新者';
COMMENT
ON COLUMN "infra_job_log"."update_time" IS '更新时间';
COMMENT
ON COLUMN "infra_job_log"."deleted" IS '是否删除';
COMMENT
ON TABLE "infra_job_log" IS '定时任务日志表';

-- ----------------------------
-- Records of infra_job_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for infra_test_demo
-- ----------------------------
DROP TABLE IF EXISTS "infra_test_demo";
CREATE TABLE "infra_test_demo"
(
    "id"          int8                                        NOT NULL,
    "name"        varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
    "status"      int2                                        NOT NULL,
    "type"        int2                                        NOT NULL,
    "category"    int2                                        NOT NULL,
    "remark"      varchar(500) COLLATE "pg_catalog"."default",
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                                NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                                NOT NULL,
    "deleted"     int2                                        NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "infra_test_demo"."id" IS '编号';
COMMENT
ON COLUMN "infra_test_demo"."name" IS '名字';
COMMENT
ON COLUMN "infra_test_demo"."status" IS '状态';
COMMENT
ON COLUMN "infra_test_demo"."type" IS '类型';
COMMENT
ON COLUMN "infra_test_demo"."category" IS '分类';
COMMENT
ON COLUMN "infra_test_demo"."remark" IS '备注';
COMMENT
ON COLUMN "infra_test_demo"."creator" IS '创建者';
COMMENT
ON COLUMN "infra_test_demo"."create_time" IS '创建时间';
COMMENT
ON COLUMN "infra_test_demo"."updater" IS '更新者';
COMMENT
ON COLUMN "infra_test_demo"."update_time" IS '更新时间';
COMMENT
ON COLUMN "infra_test_demo"."deleted" IS '是否删除';
COMMENT
ON TABLE "infra_test_demo" IS '字典类型表';

-- ----------------------------
-- Records of infra_test_demo
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for member_user
-- ----------------------------
DROP TABLE IF EXISTS "member_user";
CREATE TABLE "member_user"
(
    "id"          int8                                        NOT NULL,
    "nickname"    varchar(30) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT '',
    "avatar"      varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "status"      int2                                        NOT NULL,
    "mobile"      varchar(11) COLLATE "pg_catalog"."default"  NOT NULL,
    "password"    varchar(100) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "register_ip" varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "login_ip"    varchar(50) COLLATE "pg_catalog"."default"           DEFAULT '',
    "login_date"  timestamp(6),
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                                NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                                NOT NULL,
    "deleted"     int2                                        NOT NULL DEFAULT 0,
    "tenant_id"   int8                                        NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "member_user"."id" IS '编号';
COMMENT
ON COLUMN "member_user"."nickname" IS '用户昵称';
COMMENT
ON COLUMN "member_user"."avatar" IS '头像';
COMMENT
ON COLUMN "member_user"."status" IS '状态';
COMMENT
ON COLUMN "member_user"."mobile" IS '手机号';
COMMENT
ON COLUMN "member_user"."password" IS '密码';
COMMENT
ON COLUMN "member_user"."register_ip" IS '注册 IP';
COMMENT
ON COLUMN "member_user"."login_ip" IS '最后登录IP';
COMMENT
ON COLUMN "member_user"."login_date" IS '最后登录时间';
COMMENT
ON COLUMN "member_user"."creator" IS '创建者';
COMMENT
ON COLUMN "member_user"."create_time" IS '创建时间';
COMMENT
ON COLUMN "member_user"."updater" IS '更新者';
COMMENT
ON COLUMN "member_user"."update_time" IS '更新时间';
COMMENT
ON COLUMN "member_user"."deleted" IS '是否删除';
COMMENT
ON COLUMN "member_user"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "member_user" IS '用户';

-- ----------------------------
-- Records of member_user
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for pay_app
-- ----------------------------
DROP TABLE IF EXISTS "pay_app";
CREATE TABLE "pay_app"
(
    "id"                int8                                         NOT NULL,
    "name"              varchar(64) COLLATE "pg_catalog"."default"   NOT NULL,
    "status"            int2                                         NOT NULL,
    "remark"            varchar(255) COLLATE "pg_catalog"."default",
    "pay_notify_url"    varchar(1024) COLLATE "pg_catalog"."default" NOT NULL,
    "refund_notify_url" varchar(1024) COLLATE "pg_catalog"."default" NOT NULL,
    "merchant_id"       int8                                         NOT NULL,
    "creator"           varchar(64) COLLATE "pg_catalog"."default",
    "create_time"       timestamp(6)                                 NOT NULL,
    "updater"           varchar(64) COLLATE "pg_catalog"."default",
    "update_time"       timestamp(6)                                 NOT NULL,
    "deleted"           int2                                         NOT NULL DEFAULT 0,
    "tenant_id"         int8                                         NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "pay_app"."id" IS '应用编号';
COMMENT
ON COLUMN "pay_app"."name" IS '应用名';
COMMENT
ON COLUMN "pay_app"."status" IS '开启状态';
COMMENT
ON COLUMN "pay_app"."remark" IS '备注';
COMMENT
ON COLUMN "pay_app"."pay_notify_url" IS '支付结果的回调地址';
COMMENT
ON COLUMN "pay_app"."refund_notify_url" IS '退款结果的回调地址';
COMMENT
ON COLUMN "pay_app"."merchant_id" IS '商户编号';
COMMENT
ON COLUMN "pay_app"."creator" IS '创建者';
COMMENT
ON COLUMN "pay_app"."create_time" IS '创建时间';
COMMENT
ON COLUMN "pay_app"."updater" IS '更新者';
COMMENT
ON COLUMN "pay_app"."update_time" IS '更新时间';
COMMENT
ON COLUMN "pay_app"."deleted" IS '是否删除';
COMMENT
ON COLUMN "pay_app"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "pay_app" IS '支付应用信息';

-- ----------------------------
-- Records of pay_app
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for pay_channel
-- ----------------------------
DROP TABLE IF EXISTS "pay_channel";
CREATE TABLE "pay_channel"
(
    "id"          int8                                         NOT NULL,
    "code"        varchar(32) COLLATE "pg_catalog"."default"   NOT NULL,
    "status"      int2                                         NOT NULL,
    "remark"      varchar(255) COLLATE "pg_catalog"."default",
    "fee_rate"    float8                                       NOT NULL,
    "merchant_id" int8                                         NOT NULL,
    "app_id"      int8                                         NOT NULL,
    "config"      varchar(4096) COLLATE "pg_catalog"."default" NOT NULL,
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                                 NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                                 NOT NULL,
    "deleted"     int2                                         NOT NULL DEFAULT 0,
    "tenant_id"   int8                                         NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "pay_channel"."id" IS '商户编号';
COMMENT
ON COLUMN "pay_channel"."code" IS '渠道编码';
COMMENT
ON COLUMN "pay_channel"."status" IS '开启状态';
COMMENT
ON COLUMN "pay_channel"."remark" IS '备注';
COMMENT
ON COLUMN "pay_channel"."fee_rate" IS '渠道费率，单位：百分比';
COMMENT
ON COLUMN "pay_channel"."merchant_id" IS '商户编号';
COMMENT
ON COLUMN "pay_channel"."app_id" IS '应用编号';
COMMENT
ON COLUMN "pay_channel"."config" IS '支付渠道配置';
COMMENT
ON COLUMN "pay_channel"."creator" IS '创建者';
COMMENT
ON COLUMN "pay_channel"."create_time" IS '创建时间';
COMMENT
ON COLUMN "pay_channel"."updater" IS '更新者';
COMMENT
ON COLUMN "pay_channel"."update_time" IS '更新时间';
COMMENT
ON COLUMN "pay_channel"."deleted" IS '是否删除';
COMMENT
ON COLUMN "pay_channel"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "pay_channel" IS '支付渠道
';

-- ----------------------------
-- Records of pay_channel
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for pay_merchant
-- ----------------------------
DROP TABLE IF EXISTS "pay_merchant";
CREATE TABLE "pay_merchant"
(
    "id"          int8                                       NOT NULL,
    "no"          varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
    "name"        varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "short_name"  varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "status"      int2                                       NOT NULL,
    "remark"      varchar(255) COLLATE "pg_catalog"."default",
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                               NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                               NOT NULL,
    "deleted"     int2                                       NOT NULL DEFAULT 0,
    "tenant_id"   int8                                       NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "pay_merchant"."id" IS '商户编号';
COMMENT
ON COLUMN "pay_merchant"."no" IS '商户号';
COMMENT
ON COLUMN "pay_merchant"."name" IS '商户全称';
COMMENT
ON COLUMN "pay_merchant"."short_name" IS '商户简称';
COMMENT
ON COLUMN "pay_merchant"."status" IS '开启状态';
COMMENT
ON COLUMN "pay_merchant"."remark" IS '备注';
COMMENT
ON COLUMN "pay_merchant"."creator" IS '创建者';
COMMENT
ON COLUMN "pay_merchant"."create_time" IS '创建时间';
COMMENT
ON COLUMN "pay_merchant"."updater" IS '更新者';
COMMENT
ON COLUMN "pay_merchant"."update_time" IS '更新时间';
COMMENT
ON COLUMN "pay_merchant"."deleted" IS '是否删除';
COMMENT
ON COLUMN "pay_merchant"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "pay_merchant" IS '支付商户信息';

-- ----------------------------
-- Records of pay_merchant
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for pay_notify_log
-- ----------------------------
DROP TABLE IF EXISTS "pay_notify_log";
CREATE TABLE "pay_notify_log"
(
    "id"           int8                                         NOT NULL,
    "task_id"      int8                                         NOT NULL,
    "notify_times" int2                                         NOT NULL,
    "response"     varchar(2048) COLLATE "pg_catalog"."default" NOT NULL,
    "status"       int2                                         NOT NULL,
    "creator"      varchar(64) COLLATE "pg_catalog"."default",
    "create_time"  timestamp(6)                                 NOT NULL,
    "updater"      varchar(64) COLLATE "pg_catalog"."default",
    "update_time"  timestamp(6)                                 NOT NULL,
    "deleted"      int2                                         NOT NULL DEFAULT 0,
    "tenant_id"    int8                                         NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "pay_notify_log"."id" IS '日志编号';
COMMENT
ON COLUMN "pay_notify_log"."task_id" IS '通知任务编号';
COMMENT
ON COLUMN "pay_notify_log"."notify_times" IS '第几次被通知';
COMMENT
ON COLUMN "pay_notify_log"."response" IS '请求参数';
COMMENT
ON COLUMN "pay_notify_log"."status" IS '通知状态';
COMMENT
ON COLUMN "pay_notify_log"."creator" IS '创建者';
COMMENT
ON COLUMN "pay_notify_log"."create_time" IS '创建时间';
COMMENT
ON COLUMN "pay_notify_log"."updater" IS '更新者';
COMMENT
ON COLUMN "pay_notify_log"."update_time" IS '更新时间';
COMMENT
ON COLUMN "pay_notify_log"."deleted" IS '是否删除';
COMMENT
ON COLUMN "pay_notify_log"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "pay_notify_log" IS '支付通知 App 的日志';

-- ----------------------------
-- Records of pay_notify_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for pay_notify_task
-- ----------------------------
DROP TABLE IF EXISTS "pay_notify_task";
CREATE TABLE "pay_notify_task"
(
    "id"                int8                                         NOT NULL,
    "merchant_id"       int8                                         NOT NULL,
    "app_id"            int8                                         NOT NULL,
    "type"              int2                                         NOT NULL,
    "data_id"           int8                                         NOT NULL,
    "status"            int2                                         NOT NULL,
    "merchant_order_id" varchar(64) COLLATE "pg_catalog"."default"   NOT NULL,
    "next_notify_time"  timestamp(6)                                 NOT NULL,
    "last_execute_time" timestamp(6)                                 NOT NULL,
    "notify_times"      int2                                         NOT NULL,
    "max_notify_times"  int2                                         NOT NULL,
    "notify_url"        varchar(1024) COLLATE "pg_catalog"."default" NOT NULL,
    "creator"           varchar(64) COLLATE "pg_catalog"."default",
    "create_time"       timestamp(6)                                 NOT NULL,
    "updater"           varchar(64) COLLATE "pg_catalog"."default",
    "update_time"       timestamp(6)                                 NOT NULL,
    "deleted"           int2                                         NOT NULL DEFAULT 0,
    "tenant_id"         int8                                         NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "pay_notify_task"."id" IS '任务编号';
COMMENT
ON COLUMN "pay_notify_task"."merchant_id" IS '商户编号';
COMMENT
ON COLUMN "pay_notify_task"."app_id" IS '应用编号';
COMMENT
ON COLUMN "pay_notify_task"."type" IS '通知类型';
COMMENT
ON COLUMN "pay_notify_task"."data_id" IS '数据编号';
COMMENT
ON COLUMN "pay_notify_task"."status" IS '通知状态';
COMMENT
ON COLUMN "pay_notify_task"."merchant_order_id" IS '商户订单编号';
COMMENT
ON COLUMN "pay_notify_task"."next_notify_time" IS '下一次通知时间';
COMMENT
ON COLUMN "pay_notify_task"."last_execute_time" IS '最后一次执行时间';
COMMENT
ON COLUMN "pay_notify_task"."notify_times" IS '当前通知次数';
COMMENT
ON COLUMN "pay_notify_task"."max_notify_times" IS '最大可通知次数';
COMMENT
ON COLUMN "pay_notify_task"."notify_url" IS '异步通知地址';
COMMENT
ON COLUMN "pay_notify_task"."creator" IS '创建者';
COMMENT
ON COLUMN "pay_notify_task"."create_time" IS '创建时间';
COMMENT
ON COLUMN "pay_notify_task"."updater" IS '更新者';
COMMENT
ON COLUMN "pay_notify_task"."update_time" IS '更新时间';
COMMENT
ON COLUMN "pay_notify_task"."deleted" IS '是否删除';
COMMENT
ON COLUMN "pay_notify_task"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "pay_notify_task" IS '商户支付、退款等的通知
';

-- ----------------------------
-- Records of pay_notify_task
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for pay_order
-- ----------------------------
DROP TABLE IF EXISTS "pay_order";
CREATE TABLE "pay_order"
(
    "id"                   int8                                         NOT NULL,
    "merchant_id"          int8                                         NOT NULL,
    "app_id"               int8                                         NOT NULL,
    "channel_id"           int8,
    "channel_code"         varchar(32) COLLATE "pg_catalog"."default",
    "merchant_order_id"    varchar(64) COLLATE "pg_catalog"."default"   NOT NULL,
    "subject"              varchar(32) COLLATE "pg_catalog"."default"   NOT NULL,
    "body"                 varchar(128) COLLATE "pg_catalog"."default"  NOT NULL,
    "notify_url"           varchar(1024) COLLATE "pg_catalog"."default" NOT NULL,
    "notify_status"        int2                                         NOT NULL,
    "amount"               int8                                         NOT NULL,
    "channel_fee_rate"     float8,
    "channel_fee_amount"   int8,
    "status"               int2                                         NOT NULL,
    "user_ip"              varchar(50) COLLATE "pg_catalog"."default"   NOT NULL,
    "expire_time"          timestamp(6)                                 NOT NULL,
    "success_time"         timestamp(6),
    "notify_time"          timestamp(6),
    "success_extension_id" int8,
    "refund_status"        int2                                         NOT NULL,
    "refund_times"         int2                                         NOT NULL,
    "refund_amount"        int8                                         NOT NULL,
    "channel_user_id"      varchar(255) COLLATE "pg_catalog"."default",
    "channel_order_no"     varchar(64) COLLATE "pg_catalog"."default",
    "creator"              varchar(64) COLLATE "pg_catalog"."default",
    "create_time"          timestamp(6)                                 NOT NULL,
    "updater"              varchar(64) COLLATE "pg_catalog"."default",
    "update_time"          timestamp(6)                                 NOT NULL,
    "deleted"              int2                                         NOT NULL DEFAULT 0,
    "tenant_id"            int8                                         NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "pay_order"."id" IS '支付订单编号';
COMMENT
ON COLUMN "pay_order"."merchant_id" IS '商户编号';
COMMENT
ON COLUMN "pay_order"."app_id" IS '应用编号';
COMMENT
ON COLUMN "pay_order"."channel_id" IS '渠道编号';
COMMENT
ON COLUMN "pay_order"."channel_code" IS '渠道编码';
COMMENT
ON COLUMN "pay_order"."merchant_order_id" IS '商户订单编号';
COMMENT
ON COLUMN "pay_order"."subject" IS '商品标题';
COMMENT
ON COLUMN "pay_order"."body" IS '商品描述';
COMMENT
ON COLUMN "pay_order"."notify_url" IS '异步通知地址';
COMMENT
ON COLUMN "pay_order"."notify_status" IS '通知商户支付结果的回调状态';
COMMENT
ON COLUMN "pay_order"."amount" IS '支付金额，单位：分';
COMMENT
ON COLUMN "pay_order"."channel_fee_rate" IS '渠道手续费，单位：百分比';
COMMENT
ON COLUMN "pay_order"."channel_fee_amount" IS '渠道手续金额，单位：分';
COMMENT
ON COLUMN "pay_order"."status" IS '支付状态';
COMMENT
ON COLUMN "pay_order"."user_ip" IS '用户 IP';
COMMENT
ON COLUMN "pay_order"."expire_time" IS '订单失效时间';
COMMENT
ON COLUMN "pay_order"."success_time" IS '订单支付成功时间';
COMMENT
ON COLUMN "pay_order"."notify_time" IS '订单支付通知时间';
COMMENT
ON COLUMN "pay_order"."success_extension_id" IS '支付成功的订单拓展单编号';
COMMENT
ON COLUMN "pay_order"."refund_status" IS '退款状态';
COMMENT
ON COLUMN "pay_order"."refund_times" IS '退款次数';
COMMENT
ON COLUMN "pay_order"."refund_amount" IS '退款总金额，单位：分';
COMMENT
ON COLUMN "pay_order"."channel_user_id" IS '渠道用户编号';
COMMENT
ON COLUMN "pay_order"."channel_order_no" IS '渠道订单号';
COMMENT
ON COLUMN "pay_order"."creator" IS '创建者';
COMMENT
ON COLUMN "pay_order"."create_time" IS '创建时间';
COMMENT
ON COLUMN "pay_order"."updater" IS '更新者';
COMMENT
ON COLUMN "pay_order"."update_time" IS '更新时间';
COMMENT
ON COLUMN "pay_order"."deleted" IS '是否删除';
COMMENT
ON COLUMN "pay_order"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "pay_order" IS '支付订单
';

-- ----------------------------
-- Records of pay_order
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for pay_order_extension
-- ----------------------------
DROP TABLE IF EXISTS "pay_order_extension";
CREATE TABLE "pay_order_extension"
(
    "id"                  int8                                       NOT NULL,
    "no"                  varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "order_id"            int8                                       NOT NULL,
    "channel_id"          int8                                       NOT NULL,
    "channel_code"        varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
    "user_ip"             varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
    "status"              int2                                       NOT NULL,
    "channel_extras"      varchar(256) COLLATE "pg_catalog"."default",
    "channel_notify_data" varchar(1024) COLLATE "pg_catalog"."default",
    "creator"             varchar(64) COLLATE "pg_catalog"."default",
    "create_time"         timestamp(6)                               NOT NULL,
    "updater"             varchar(64) COLLATE "pg_catalog"."default",
    "update_time"         timestamp(6)                               NOT NULL,
    "deleted"             int2                                       NOT NULL DEFAULT 0,
    "tenant_id"           int8                                       NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "pay_order_extension"."id" IS '支付订单编号';
COMMENT
ON COLUMN "pay_order_extension"."no" IS '支付订单号';
COMMENT
ON COLUMN "pay_order_extension"."order_id" IS '支付订单编号';
COMMENT
ON COLUMN "pay_order_extension"."channel_id" IS '渠道编号';
COMMENT
ON COLUMN "pay_order_extension"."channel_code" IS '渠道编码';
COMMENT
ON COLUMN "pay_order_extension"."user_ip" IS '用户 IP';
COMMENT
ON COLUMN "pay_order_extension"."status" IS '支付状态';
COMMENT
ON COLUMN "pay_order_extension"."channel_extras" IS '支付渠道的额外参数';
COMMENT
ON COLUMN "pay_order_extension"."channel_notify_data" IS '支付渠道异步通知的内容';
COMMENT
ON COLUMN "pay_order_extension"."creator" IS '创建者';
COMMENT
ON COLUMN "pay_order_extension"."create_time" IS '创建时间';
COMMENT
ON COLUMN "pay_order_extension"."updater" IS '更新者';
COMMENT
ON COLUMN "pay_order_extension"."update_time" IS '更新时间';
COMMENT
ON COLUMN "pay_order_extension"."deleted" IS '是否删除';
COMMENT
ON COLUMN "pay_order_extension"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "pay_order_extension" IS '支付订单
';

-- ----------------------------
-- Records of pay_order_extension
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for pay_refund
-- ----------------------------
DROP TABLE IF EXISTS "pay_refund";
CREATE TABLE "pay_refund"
(
    "id"                 int8                                         NOT NULL,
    "merchant_id"        int8                                         NOT NULL,
    "app_id"             int8                                         NOT NULL,
    "channel_id"         int8                                         NOT NULL,
    "channel_code"       varchar(32) COLLATE "pg_catalog"."default"   NOT NULL,
    "order_id"           int8                                         NOT NULL,
    "trade_no"           varchar(64) COLLATE "pg_catalog"."default"   NOT NULL,
    "merchant_order_id"  varchar(64) COLLATE "pg_catalog"."default"   NOT NULL,
    "merchant_refund_no" varchar(64) COLLATE "pg_catalog"."default"   NOT NULL,
    "notify_url"         varchar(1024) COLLATE "pg_catalog"."default" NOT NULL,
    "notify_status"      int2                                         NOT NULL,
    "status"             int2                                         NOT NULL,
    "type"               int2                                         NOT NULL,
    "pay_amount"         int8                                         NOT NULL,
    "refund_amount"      int8                                         NOT NULL,
    "reason"             varchar(256) COLLATE "pg_catalog"."default"  NOT NULL,
    "user_ip"            varchar(50) COLLATE "pg_catalog"."default",
    "channel_order_no"   varchar(64) COLLATE "pg_catalog"."default"   NOT NULL,
    "channel_refund_no"  varchar(64) COLLATE "pg_catalog"."default",
    "channel_error_code" varchar(128) COLLATE "pg_catalog"."default",
    "channel_error_msg"  varchar(256) COLLATE "pg_catalog"."default",
    "channel_extras"     varchar(1024) COLLATE "pg_catalog"."default",
    "expire_time"        timestamp(6),
    "success_time"       timestamp(6),
    "notify_time"        timestamp(6),
    "creator"            varchar(64) COLLATE "pg_catalog"."default",
    "create_time"        timestamp(6)                                 NOT NULL,
    "updater"            varchar(64) COLLATE "pg_catalog"."default",
    "update_time"        timestamp(6)                                 NOT NULL,
    "deleted"            int2                                         NOT NULL DEFAULT 0,
    "tenant_id"          int8                                         NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "pay_refund"."id" IS '支付退款编号';
COMMENT
ON COLUMN "pay_refund"."merchant_id" IS '商户编号';
COMMENT
ON COLUMN "pay_refund"."app_id" IS '应用编号';
COMMENT
ON COLUMN "pay_refund"."channel_id" IS '渠道编号';
COMMENT
ON COLUMN "pay_refund"."channel_code" IS '渠道编码';
COMMENT
ON COLUMN "pay_refund"."order_id" IS '支付订单编号 pay_order 表id';
COMMENT
ON COLUMN "pay_refund"."trade_no" IS '交易订单号 pay_extension 表no 字段';
COMMENT
ON COLUMN "pay_refund"."merchant_order_id" IS '商户订单编号（商户系统生成）';
COMMENT
ON COLUMN "pay_refund"."merchant_refund_no" IS '商户退款订单号（商户系统生成）';
COMMENT
ON COLUMN "pay_refund"."notify_url" IS '异步通知商户地址';
COMMENT
ON COLUMN "pay_refund"."notify_status" IS '通知商户退款结果的回调状态';
COMMENT
ON COLUMN "pay_refund"."status" IS '退款状态';
COMMENT
ON COLUMN "pay_refund"."type" IS '退款类型(部分退款，全部退款)';
COMMENT
ON COLUMN "pay_refund"."pay_amount" IS '支付金额,单位分';
COMMENT
ON COLUMN "pay_refund"."refund_amount" IS '退款金额,单位分';
COMMENT
ON COLUMN "pay_refund"."reason" IS '退款原因';
COMMENT
ON COLUMN "pay_refund"."user_ip" IS '用户 IP';
COMMENT
ON COLUMN "pay_refund"."channel_order_no" IS '渠道订单号，pay_order 中的channel_order_no 对应';
COMMENT
ON COLUMN "pay_refund"."channel_refund_no" IS '渠道退款单号，渠道返回';
COMMENT
ON COLUMN "pay_refund"."channel_error_code" IS '渠道调用报错时，错误码';
COMMENT
ON COLUMN "pay_refund"."channel_error_msg" IS '渠道调用报错时，错误信息';
COMMENT
ON COLUMN "pay_refund"."channel_extras" IS '支付渠道的额外参数';
COMMENT
ON COLUMN "pay_refund"."expire_time" IS '退款失效时间';
COMMENT
ON COLUMN "pay_refund"."success_time" IS '退款成功时间';
COMMENT
ON COLUMN "pay_refund"."notify_time" IS '退款通知时间';
COMMENT
ON COLUMN "pay_refund"."creator" IS '创建者';
COMMENT
ON COLUMN "pay_refund"."create_time" IS '创建时间';
COMMENT
ON COLUMN "pay_refund"."updater" IS '更新者';
COMMENT
ON COLUMN "pay_refund"."update_time" IS '更新时间';
COMMENT
ON COLUMN "pay_refund"."deleted" IS '是否删除';
COMMENT
ON COLUMN "pay_refund"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "pay_refund" IS '退款订单';

-- ----------------------------
-- Records of pay_refund
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_blob_triggers
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_blob_triggers";
CREATE TABLE "qrtz_blob_triggers"
(
    "sched_name"    varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
    "trigger_name"  varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "blob_data"     bytea
)
;

-- ----------------------------
-- Records of qrtz_blob_triggers
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_calendars";
CREATE TABLE "qrtz_calendars"
(
    "sched_name"    varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
    "calendar_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "calendar"      bytea                                       NOT NULL
)
;

-- ----------------------------
-- Records of qrtz_calendars
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_cron_triggers
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_cron_triggers";
CREATE TABLE "qrtz_cron_triggers"
(
    "sched_name"      varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
    "trigger_name"    varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "trigger_group"   varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "cron_expression" varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
    "time_zone_id"    varchar(80) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of qrtz_cron_triggers
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_fired_triggers";
CREATE TABLE "qrtz_fired_triggers"
(
    "sched_name"        varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
    "entry_id"          varchar(95) COLLATE "pg_catalog"."default"  NOT NULL,
    "trigger_name"      varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "trigger_group"     varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "instance_name"     varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "fired_time"        int8                                        NOT NULL,
    "sched_time"        int8                                        NOT NULL,
    "priority"          int4                                        NOT NULL,
    "state"             varchar(16) COLLATE "pg_catalog"."default"  NOT NULL,
    "job_name"          varchar(200) COLLATE "pg_catalog"."default",
    "job_group"         varchar(200) COLLATE "pg_catalog"."default",
    "is_nonconcurrent"  bool,
    "requests_recovery" bool
)
;

-- ----------------------------
-- Records of qrtz_fired_triggers
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_job_details
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_job_details";
CREATE TABLE "qrtz_job_details"
(
    "sched_name"        varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
    "job_name"          varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "job_group"         varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "description"       varchar(250) COLLATE "pg_catalog"."default",
    "job_class_name"    varchar(250) COLLATE "pg_catalog"."default" NOT NULL,
    "is_durable"        bool                                        NOT NULL,
    "is_nonconcurrent"  bool                                        NOT NULL,
    "is_update_data"    bool                                        NOT NULL,
    "requests_recovery" bool                                        NOT NULL,
    "job_data"          bytea
)
;

-- ----------------------------
-- Records of qrtz_job_details
-- ----------------------------
BEGIN;
INSERT INTO "qrtz_job_details" ("sched_name", "job_name", "job_group", "description", "job_class_name", "is_durable",
                                "is_nonconcurrent", "is_update_data", "requests_recovery", "job_data")
VALUES ('schedulerName', 'userSessionTimeoutJob', 'DEFAULT', NULL,
        'cn.iocoder.yudao.framework.quartz.core.handler.JobHandlerInvoker', 'f', 't', 't', 'f',
         '\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\002t\\000\\006JOB_IDsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\002t\\000\\020JOB_HANDLER_NAMEt\\000\\025userSessionTimeoutJobx\\000');
COMMIT;

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_locks";
CREATE TABLE "qrtz_locks"
(
    "sched_name" varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
    "lock_name"  varchar(40) COLLATE "pg_catalog"."default"  NOT NULL
)
;

-- ----------------------------
-- Records of qrtz_locks
-- ----------------------------
BEGIN;
INSERT INTO "qrtz_locks" ("sched_name", "lock_name")
VALUES ('schedulerName', 'TRIGGER_ACCESS');
INSERT INTO "qrtz_locks" ("sched_name", "lock_name")
VALUES ('schedulerName', 'STATE_ACCESS');
COMMIT;

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_paused_trigger_grps";
CREATE TABLE "qrtz_paused_trigger_grps"
(
    "sched_name"    varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
    "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL
)
;

-- ----------------------------
-- Records of qrtz_paused_trigger_grps
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_scheduler_state";
CREATE TABLE "qrtz_scheduler_state"
(
    "sched_name"        varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
    "instance_name"     varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "last_checkin_time" int8                                        NOT NULL,
    "checkin_interval"  int8                                        NOT NULL
)
;

-- ----------------------------
-- Records of qrtz_scheduler_state
-- ----------------------------
BEGIN;
INSERT INTO "qrtz_scheduler_state" ("sched_name", "instance_name", "last_checkin_time", "checkin_interval")
VALUES ('schedulerName', 'Yunai.local1651328569660', 1651328650075, 15000);
COMMIT;

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_simple_triggers";
CREATE TABLE "qrtz_simple_triggers"
(
    "sched_name"      varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
    "trigger_name"    varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "trigger_group"   varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "repeat_count"    int8                                        NOT NULL,
    "repeat_interval" int8                                        NOT NULL,
    "times_triggered" int8                                        NOT NULL
)
;

-- ----------------------------
-- Records of qrtz_simple_triggers
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_simprop_triggers
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_simprop_triggers";
CREATE TABLE "qrtz_simprop_triggers"
(
    "sched_name"    varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
    "trigger_name"  varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "trigger_group" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "str_prop_1"    varchar(512) COLLATE "pg_catalog"."default",
    "str_prop_2"    varchar(512) COLLATE "pg_catalog"."default",
    "str_prop_3"    varchar(512) COLLATE "pg_catalog"."default",
    "int_prop_1"    int4,
    "int_prop_2"    int4,
    "long_prop_1"   int8,
    "long_prop_2"   int8,
    "dec_prop_1"    numeric(13, 4),
    "dec_prop_2"    numeric(13, 4),
    "bool_prop_1"   bool,
    "bool_prop_2"   bool
)
;

-- ----------------------------
-- Records of qrtz_simprop_triggers
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_triggers
-- ----------------------------
DROP TABLE IF EXISTS "qrtz_triggers";
CREATE TABLE "qrtz_triggers"
(
    "sched_name"     varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
    "trigger_name"   varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "trigger_group"  varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "job_name"       varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "job_group"      varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
    "description"    varchar(250) COLLATE "pg_catalog"."default",
    "next_fire_time" int8,
    "prev_fire_time" int8,
    "priority"       int4,
    "trigger_state"  varchar(16) COLLATE "pg_catalog"."default"  NOT NULL,
    "trigger_type"   varchar(8) COLLATE "pg_catalog"."default"   NOT NULL,
    "start_time"     int8                                        NOT NULL,
    "end_time"       int8,
    "calendar_name"  varchar(200) COLLATE "pg_catalog"."default",
    "misfire_instr"  int2,
    "job_data"       bytea
)
;

-- ----------------------------
-- Records of qrtz_triggers
-- ----------------------------
BEGIN;
INSERT INTO "qrtz_triggers" ("sched_name", "trigger_name", "trigger_group", "job_name", "job_group", "description",
                             "next_fire_time", "prev_fire_time", "priority", "trigger_state", "trigger_type",
                             "start_time", "end_time", "calendar_name", "misfire_instr", "job_data")
VALUES ('schedulerName', 'userSessionTimeoutJob', 'DEFAULT', 'userSessionTimeoutJob', 'DEFAULT', NULL, 1651328700000,
        1651328640000, 5, 'WAITING', 'CRON', 1651328526000, 0, NULL, 0,
         '\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\003t\\000\\021JOB_HANDLER_PARAMpt\\000\\022JOB_RETRY_INTERVALsr\\000\\021java.lang.Integer\\022\\342\\240\\244\\367\\201\\2078\\002\\000\\001I\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\007\\320t\\000\\017JOB_RETRY_COUNTsq\\000~\\000\\011\\000\\000\\000\\003x\\000');
COMMIT;

-- ----------------------------
-- Table structure for system_dept
-- ----------------------------
DROP TABLE IF EXISTS "system_dept";
CREATE TABLE "system_dept"
(
    "id"             int8                                       NOT NULL,
    "name"           varchar(30) COLLATE "pg_catalog"."default" NOT NULL,
    "parent_id"      int8                                       NOT NULL,
    "sort"           int4                                       NOT NULL,
    "leader_user_id" int8,
    "phone"          varchar(11) COLLATE "pg_catalog"."default",
    "email"          varchar(50) COLLATE "pg_catalog"."default",
    "status"         int2                                       NOT NULL,
    "creator"        varchar(64) COLLATE "pg_catalog"."default",
    "create_time"    timestamp(6)                               NOT NULL,
    "updater"        varchar(64) COLLATE "pg_catalog"."default",
    "update_time"    timestamp(6)                               NOT NULL,
    "deleted"        int2                                       NOT NULL DEFAULT 0,
    "tenant_id"      int8                                       NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_dept"."id" IS '部门id';
COMMENT
ON COLUMN "system_dept"."name" IS '部门名称';
COMMENT
ON COLUMN "system_dept"."parent_id" IS '父部门id';
COMMENT
ON COLUMN "system_dept"."sort" IS '显示顺序';
COMMENT
ON COLUMN "system_dept"."leader_user_id" IS '负责人';
COMMENT
ON COLUMN "system_dept"."phone" IS '联系电话';
COMMENT
ON COLUMN "system_dept"."email" IS '邮箱';
COMMENT
ON COLUMN "system_dept"."status" IS '部门状态（0正常 1停用）';
COMMENT
ON COLUMN "system_dept"."creator" IS '创建者';
COMMENT
ON COLUMN "system_dept"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_dept"."updater" IS '更新者';
COMMENT
ON COLUMN "system_dept"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_dept"."deleted" IS '是否删除';
COMMENT
ON COLUMN "system_dept"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "system_dept" IS '部门表';

-- ----------------------------
-- Records of system_dept
-- ----------------------------
BEGIN;
INSERT INTO "system_dept" ("id", "name", "parent_id", "sort", "leader_user_id", "phone", "email", "status", "creator",
                           "create_time", "updater", "update_time", "deleted", "tenant_id")
VALUES (100, '芋道源码', 0, 0, 1, '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '103',
        '2022-01-14 01:04:05', 0, 1);
INSERT INTO "system_dept" ("id", "name", "parent_id", "sort", "leader_user_id", "phone", "email", "status", "creator",
                           "create_time", "updater", "update_time", "deleted", "tenant_id")
VALUES (101, '深圳总公司', 100, 1, 104, '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '1',
        '2022-02-22 19:47:48', 0, 1);
INSERT INTO "system_dept" ("id", "name", "parent_id", "sort", "leader_user_id", "phone", "email", "status", "creator",
                           "create_time", "updater", "update_time", "deleted", "tenant_id")
VALUES (102, '长沙分公司', 100, 2, NULL, '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '',
        '2021-12-15 05:01:40', 0, 1);
INSERT INTO "system_dept" ("id", "name", "parent_id", "sort", "leader_user_id", "phone", "email", "status", "creator",
                           "create_time", "updater", "update_time", "deleted", "tenant_id")
VALUES (103, '研发部门', 101, 1, 104, '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '103',
        '2022-01-14 01:04:14', 0, 1);
INSERT INTO "system_dept" ("id", "name", "parent_id", "sort", "leader_user_id", "phone", "email", "status", "creator",
                           "create_time", "updater", "update_time", "deleted", "tenant_id")
VALUES (104, '市场部门', 101, 2, NULL, '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '',
        '2021-12-15 05:01:38', 0, 1);
INSERT INTO "system_dept" ("id", "name", "parent_id", "sort", "leader_user_id", "phone", "email", "status", "creator",
                           "create_time", "updater", "update_time", "deleted", "tenant_id")
VALUES (105, '测试部门', 101, 3, NULL, '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '',
        '2021-12-15 05:01:37', 0, 1);
INSERT INTO "system_dept" ("id", "name", "parent_id", "sort", "leader_user_id", "phone", "email", "status", "creator",
                           "create_time", "updater", "update_time", "deleted", "tenant_id")
VALUES (106, '财务部门', 101, 4, 103, '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '103',
        '2022-01-15 21:32:22', 0, 1);
INSERT INTO "system_dept" ("id", "name", "parent_id", "sort", "leader_user_id", "phone", "email", "status", "creator",
                           "create_time", "updater", "update_time", "deleted", "tenant_id")
VALUES (107, '运维部门', 101, 5, NULL, '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '',
        '2021-12-15 05:01:33', 0, 1);
INSERT INTO "system_dept" ("id", "name", "parent_id", "sort", "leader_user_id", "phone", "email", "status", "creator",
                           "create_time", "updater", "update_time", "deleted", "tenant_id")
VALUES (108, '市场部门', 102, 1, NULL, '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '1',
        '2022-02-16 08:35:45', 0, 1);
INSERT INTO "system_dept" ("id", "name", "parent_id", "sort", "leader_user_id", "phone", "email", "status", "creator",
                           "create_time", "updater", "update_time", "deleted", "tenant_id")
VALUES (109, '财务部门', 102, 2, NULL, '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '',
        '2021-12-15 05:01:29', 0, 1);
INSERT INTO "system_dept" ("id", "name", "parent_id", "sort", "leader_user_id", "phone", "email", "status", "creator",
                           "create_time", "updater", "update_time", "deleted", "tenant_id")
VALUES (110, '新部门', 0, 1, NULL, NULL, NULL, 0, '110', '2022-02-23 20:46:30', '110', '2022-02-23 20:46:30', 0, 121);
INSERT INTO "system_dept" ("id", "name", "parent_id", "sort", "leader_user_id", "phone", "email", "status", "creator",
                           "create_time", "updater", "update_time", "deleted", "tenant_id")
VALUES (111, '顶级部门', 0, 1, NULL, NULL, NULL, 0, '113', '2022-03-07 21:44:50', '113', '2022-03-07 21:44:50', 0, 122);
COMMIT;

-- ----------------------------
-- Table structure for system_dict_data
-- ----------------------------
DROP TABLE IF EXISTS "system_dict_data";
CREATE TABLE "system_dict_data"
(
    "id"          int8                                        NOT NULL,
    "sort"        int4                                        NOT NULL,
    "label"       varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
    "value"       varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
    "dict_type"   varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
    "status"      int2                                        NOT NULL,
    "color_type"  varchar(100) COLLATE "pg_catalog"."default",
    "css_class"   varchar(100) COLLATE "pg_catalog"."default",
    "remark"      varchar(500) COLLATE "pg_catalog"."default",
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                                NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                                NOT NULL,
    "deleted"     int2                                        NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_dict_data"."id" IS '字典编码';
COMMENT
ON COLUMN "system_dict_data"."sort" IS '字典排序';
COMMENT
ON COLUMN "system_dict_data"."label" IS '字典标签';
COMMENT
ON COLUMN "system_dict_data"."value" IS '字典键值';
COMMENT
ON COLUMN "system_dict_data"."dict_type" IS '字典类型';
COMMENT
ON COLUMN "system_dict_data"."status" IS '状态（0正常 1停用）';
COMMENT
ON COLUMN "system_dict_data"."color_type" IS '颜色类型';
COMMENT
ON COLUMN "system_dict_data"."css_class" IS 'css 样式';
COMMENT
ON COLUMN "system_dict_data"."remark" IS '备注';
COMMENT
ON COLUMN "system_dict_data"."creator" IS '创建者';
COMMENT
ON COLUMN "system_dict_data"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_dict_data"."updater" IS '更新者';
COMMENT
ON COLUMN "system_dict_data"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_dict_data"."deleted" IS '是否删除';
COMMENT
ON TABLE "system_dict_data" IS '字典数据表';

-- ----------------------------
-- Records of system_dict_data
-- ----------------------------
BEGIN;
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1, 1, '男', '1', 'system_user_sex', 0, 'default', 'A', '性别男', 'admin', '2021-01-05 17:03:48', '1',
        '2022-03-29 00:14:39', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (2, 2, '女', '2', 'system_user_sex', 1, 'success', '', '性别女', 'admin', '2021-01-05 17:03:48', '1',
        '2022-02-16 01:30:51', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (8, 1, '正常', '1', 'infra_job_status', 0, 'success', '', '正常状态', 'admin', '2021-01-05 17:03:48', '1',
        '2022-02-16 19:33:38', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (9, 2, '暂停', '2', 'infra_job_status', 0, 'danger', '', '停用状态', 'admin', '2021-01-05 17:03:48', '1',
        '2022-02-16 19:33:45', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (12, 1, '系统内置', '1', 'infra_config_type', 0, 'danger', '', '参数类型 - 系统内置', 'admin',
        '2021-01-05 17:03:48', '1', '2022-02-16 19:06:02', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (13, 2, '自定义', '2', 'infra_config_type', 0, 'primary', '', '参数类型 - 自定义', 'admin',
        '2021-01-05 17:03:48', '1', '2022-02-16 19:06:07', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (14, 1, '通知', '1', 'system_notice_type', 0, 'success', '', '通知', 'admin', '2021-01-05 17:03:48', '1',
        '2022-02-16 13:05:57', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (15, 2, '公告', '2', 'system_notice_type', 0, 'info', '', '公告', 'admin', '2021-01-05 17:03:48', '1',
        '2022-02-16 13:06:01', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (16, 0, '其它', '0', 'system_operate_type', 0, 'default', '', '其它操作', 'admin', '2021-01-05 17:03:48', '1',
        '2022-02-16 09:32:46', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (17, 1, '查询', '1', 'system_operate_type', 0, 'info', '', '查询操作', 'admin', '2021-01-05 17:03:48', '1',
        '2022-02-16 09:33:16', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (18, 2, '新增', '2', 'system_operate_type', 0, 'primary', '', '新增操作', 'admin', '2021-01-05 17:03:48', '1',
        '2022-02-16 09:33:13', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (19, 3, '修改', '3', 'system_operate_type', 0, 'warning', '', '修改操作', 'admin', '2021-01-05 17:03:48', '1',
        '2022-02-16 09:33:22', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (20, 4, '删除', '4', 'system_operate_type', 0, 'danger', '', '删除操作', 'admin', '2021-01-05 17:03:48', '1',
        '2022-02-16 09:33:27', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (22, 5, '导出', '5', 'system_operate_type', 0, 'default', '', '导出操作', 'admin', '2021-01-05 17:03:48', '1',
        '2022-02-16 09:33:32', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (23, 6, '导入', '6', 'system_operate_type', 0, 'default', '', '导入操作', 'admin', '2021-01-05 17:03:48', '1',
        '2022-02-16 09:33:35', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (27, 1, '开启', '0', 'common_status', 0, 'primary', '', '开启状态', 'admin', '2021-01-05 17:03:48', '1',
        '2022-02-16 08:00:39', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (28, 2, '关闭', '1', 'common_status', 0, 'info', '', '关闭状态', 'admin', '2021-01-05 17:03:48', '1',
        '2022-02-16 08:00:44', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (29, 1, '目录', '1', 'system_menu_type', 0, '', '', '目录', 'admin', '2021-01-05 17:03:48', '',
        '2022-02-01 16:43:45', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (30, 2, '菜单', '2', 'system_menu_type', 0, '', '', '菜单', 'admin', '2021-01-05 17:03:48', '',
        '2022-02-01 16:43:41', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (31, 3, '按钮', '3', 'system_menu_type', 0, '', '', '按钮', 'admin', '2021-01-05 17:03:48', '',
        '2022-02-01 16:43:39', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (32, 1, '内置', '1', 'system_role_type', 0, 'danger', '', '内置角色', 'admin', '2021-01-05 17:03:48', '1',
        '2022-02-16 13:02:08', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (33, 2, '自定义', '2', 'system_role_type', 0, 'primary', '', '自定义角色', 'admin', '2021-01-05 17:03:48', '1',
        '2022-02-16 13:02:12', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (34, 1, '全部数据权限', '1', 'system_data_scope', 0, '', '', '全部数据权限', 'admin', '2021-01-05 17:03:48', '',
        '2022-02-01 16:47:17', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (35, 2, '指定部门数据权限', '2', 'system_data_scope', 0, '', '', '指定部门数据权限', 'admin',
        '2021-01-05 17:03:48', '', '2022-02-01 16:47:18', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (36, 3, '本部门数据权限', '3', 'system_data_scope', 0, '', '', '本部门数据权限', 'admin', '2021-01-05 17:03:48',
        '', '2022-02-01 16:47:16', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (37, 4, '本部门及以下数据权限', '4', 'system_data_scope', 0, '', '', '本部门及以下数据权限', 'admin',
        '2021-01-05 17:03:48', '', '2022-02-01 16:47:21', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (38, 5, '仅本人数据权限', '5', 'system_data_scope', 0, '', '', '仅本人数据权限', 'admin', '2021-01-05 17:03:48',
        '', '2022-02-01 16:47:23', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (39, 0, '成功', '0', 'system_login_result', 0, 'success', '', '登陆结果 - 成功', '', '2021-01-18 06:17:36', '1',
        '2022-02-16 13:23:49', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (40, 10, '账号或密码不正确', '10', 'system_login_result', 0, 'primary', '', '登陆结果 - 账号或密码不正确', '',
        '2021-01-18 06:17:54', '1', '2022-02-16 13:24:27', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (41, 20, '用户被禁用', '20', 'system_login_result', 0, 'warning', '', '登陆结果 - 用户被禁用', '',
        '2021-01-18 06:17:54', '1', '2022-02-16 13:23:57', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (42, 30, '验证码不存在', '30', 'system_login_result', 0, 'info', '', '登陆结果 - 验证码不存在', '',
        '2021-01-18 06:17:54', '1', '2022-02-16 13:24:07', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (43, 31, '验证码不正确', '31', 'system_login_result', 0, 'info', '', '登陆结果 - 验证码不正确', '',
        '2021-01-18 06:17:54', '1', '2022-02-16 13:24:11', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (44, 100, '未知异常', '100', 'system_login_result', 0, 'danger', '', '登陆结果 - 未知异常', '',
        '2021-01-18 06:17:54', '1', '2022-02-16 13:24:23', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (45, 1, '是', 'true', 'infra_boolean_string', 0, 'danger', '', 'Boolean 是否类型 - 是', '',
        '2021-01-19 03:20:55', '1', '2022-03-15 23:01:45', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (46, 1, '否', 'false', 'infra_boolean_string', 0, 'info', '', 'Boolean 是否类型 - 否', '', '2021-01-19 03:20:55',
        '1', '2022-03-15 23:09:45', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (47, 1, '永不超时', '1', 'infra_redis_timeout_type', 0, 'primary', '', 'Redis 未设置超时的情况', '',
        '2021-01-26 00:53:17', '1', '2022-02-16 19:03:35', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (48, 1, '动态超时', '2', 'infra_redis_timeout_type', 0, 'info', '', '程序里动态传入超时时间，无法固定', '',
        '2021-01-26 00:55:00', '1', '2022-02-16 19:03:41', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (49, 3, '固定超时', '3', 'infra_redis_timeout_type', 0, 'success', '', 'Redis 设置了过期时间', '',
        '2021-01-26 00:55:26', '1', '2022-02-16 19:03:45', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (50, 1, '单表（增删改查）', '1', 'infra_codegen_template_type', 0, '', '', NULL, '', '2021-02-05 07:09:06', '',
        '2022-03-10 16:33:15', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (51, 2, '树表（增删改查）', '2', 'infra_codegen_template_type', 0, '', '', NULL, '', '2021-02-05 07:14:46', '',
        '2022-03-10 16:33:19', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (53, 0, '初始化中', '0', 'infra_job_status', 0, 'primary', '', NULL, '', '2021-02-07 07:46:49', '1',
        '2022-02-16 19:33:29', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (57, 0, '运行中', '0', 'infra_job_log_status', 0, 'primary', '', 'RUNNING', '', '2021-02-08 10:04:24', '1',
        '2022-02-16 19:07:48', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (58, 1, '成功', '1', 'infra_job_log_status', 0, 'success', '', NULL, '', '2021-02-08 10:06:57', '1',
        '2022-02-16 19:07:52', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (59, 2, '失败', '2', 'infra_job_log_status', 0, 'warning', '', '失败', '', '2021-02-08 10:07:38', '1',
        '2022-02-16 19:07:56', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (60, 1, '会员', '1', 'user_type', 0, 'primary', '', NULL, '', '2021-02-26 00:16:27', '1', '2022-02-16 10:22:19',
        0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (61, 2, '管理员', '2', 'user_type', 0, 'success', '', NULL, '', '2021-02-26 00:16:34', '1',
        '2022-02-16 10:22:22', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (62, 0, '未处理', '0', 'infra_api_error_log_process_status', 0, 'primary', '', NULL, '', '2021-02-26 07:07:19',
        '1', '2022-02-16 20:14:17', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (63, 1, '已处理', '1', 'infra_api_error_log_process_status', 0, 'success', '', NULL, '', '2021-02-26 07:07:26',
        '1', '2022-02-16 20:14:08', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (64, 2, '已忽略', '2', 'infra_api_error_log_process_status', 0, 'danger', '', NULL, '', '2021-02-26 07:07:34',
        '1', '2022-02-16 20:14:14', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (66, 2, '阿里云', 'ALIYUN', 'system_sms_channel_code', 0, 'primary', '', NULL, '1', '2021-04-05 01:05:26', '1',
        '2022-02-16 10:09:52', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (67, 1, '验证码', '1', 'system_sms_template_type', 0, 'warning', '', NULL, '1', '2021-04-05 21:50:57', '1',
        '2022-02-16 12:48:30', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (68, 2, '通知', '2', 'system_sms_template_type', 0, 'primary', '', NULL, '1', '2021-04-05 21:51:08', '1',
        '2022-02-16 12:48:27', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (69, 0, '营销', '3', 'system_sms_template_type', 0, 'danger', '', NULL, '1', '2021-04-05 21:51:15', '1',
        '2022-02-16 12:48:22', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (70, 0, '初始化', '0', 'system_sms_send_status', 0, 'primary', '', NULL, '1', '2021-04-11 20:18:33', '1',
        '2022-02-16 10:26:07', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (71, 1, '发送成功', '10', 'system_sms_send_status', 0, 'success', '', NULL, '1', '2021-04-11 20:18:43', '1',
        '2022-02-16 10:25:56', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (72, 2, '发送失败', '20', 'system_sms_send_status', 0, 'danger', '', NULL, '1', '2021-04-11 20:18:49', '1',
        '2022-02-16 10:26:03', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (73, 3, '不发送', '30', 'system_sms_send_status', 0, 'info', '', NULL, '1', '2021-04-11 20:19:44', '1',
        '2022-02-16 10:26:10', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (74, 0, '等待结果', '0', 'system_sms_receive_status', 0, 'primary', '', NULL, '1', '2021-04-11 20:27:43', '1',
        '2022-02-16 10:28:24', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (75, 1, '接收成功', '10', 'system_sms_receive_status', 0, 'success', '', NULL, '1', '2021-04-11 20:29:25', '1',
        '2022-02-16 10:28:28', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (76, 2, '接收失败', '20', 'system_sms_receive_status', 0, 'danger', '', NULL, '1', '2021-04-11 20:29:31', '1',
        '2022-02-16 10:28:32', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (77, 0, '调试(钉钉)', 'DEBUG_DING_TALK', 'system_sms_channel_code', 0, 'info', '', NULL, '1',
        '2021-04-13 00:20:37', '1', '2022-02-16 10:10:00', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (78, 1, '自动生成', '1', 'system_error_code_type', 0, 'warning', '', NULL, '1', '2021-04-21 00:06:48', '1',
        '2022-02-16 13:57:20', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (79, 2, '手动编辑', '2', 'system_error_code_type', 0, 'primary', '', NULL, '1', '2021-04-21 00:07:14', '1',
        '2022-02-16 13:57:24', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (80, 100, '账号登录', '100', 'system_login_type', 0, 'primary', '', '账号登录', '1', '2021-10-06 00:52:02', '1',
        '2022-02-16 13:11:34', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (81, 101, '社交登录', '101', 'system_login_type', 0, 'info', '', '社交登录', '1', '2021-10-06 00:52:17', '1',
        '2022-02-16 13:11:40', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (83, 200, '主动登出', '200', 'system_login_type', 0, 'primary', '', '主动登出', '1', '2021-10-06 00:52:58', '1',
        '2022-02-16 13:11:49', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (85, 202, '强制登出', '202', 'system_login_type', 0, 'danger', '', '强制退出', '1', '2021-10-06 00:53:41', '1',
        '2022-02-16 13:11:57', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (86, 0, '病假', '1', 'bpm_oa_leave_type', 0, 'primary', '', NULL, '1', '2021-09-21 22:35:28', '1',
        '2022-02-16 10:00:41', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (87, 1, '事假', '2', 'bpm_oa_leave_type', 0, 'info', '', NULL, '1', '2021-09-21 22:36:11', '1',
        '2022-02-16 10:00:49', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (88, 2, '婚假', '3', 'bpm_oa_leave_type', 0, 'warning', '', NULL, '1', '2021-09-21 22:36:38', '1',
        '2022-02-16 10:00:53', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (98, 1, 'v2', 'v2', 'pay_channel_wechat_version', 0, '', '', 'v2版本', '1', '2021-11-08 17:00:58', '1',
        '2021-11-08 17:00:58', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (99, 2, 'v3', 'v3', 'pay_channel_wechat_version', 0, '', '', 'v3版本', '1', '2021-11-08 17:01:07', '1',
        '2021-11-08 17:01:07', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (108, 1, 'RSA2', 'RSA2', 'pay_channel_alipay_sign_type', 0, '', '', 'RSA2', '1', '2021-11-18 15:39:29', '1',
        '2021-11-18 15:39:29', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (109, 1, '公钥模式', '1', 'pay_channel_alipay_mode', 0, '', '', '公钥模式：privateKey + alipayPublicKey', '1',
        '2021-11-18 15:45:23', '1', '2021-11-18 15:45:23', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (110, 2, '证书模式', '2', 'pay_channel_alipay_mode', 0, '', '',
        '证书模式：appCertContent + alipayPublicCertContent + rootCertContent', '1', '2021-11-18 15:45:40', '1',
        '2021-11-18 15:45:40', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (111, 1, '线上', 'https://openapi.alipay.com/gateway.do', 'pay_channel_alipay_server_type', 0, '', '',
        '网关地址 - 线上', '1', '2021-11-18 16:59:32', '1', '2021-11-21 17:37:29', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (112, 2, '沙箱', 'https://openapi.alipaydev.com/gateway.do', 'pay_channel_alipay_server_type', 0, '', '',
        '网关地址 - 沙箱', '1', '2021-11-18 16:59:48', '1', '2021-11-21 17:37:39', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (113, 1, '微信 JSAPI 支付', 'wx_pub', 'pay_channel_code_type', 0, '', '', '微信 JSAPI（公众号） 支付', '1',
        '2021-12-03 10:40:24', '1', '2021-12-04 16:41:00', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (114, 2, '微信小程序支付', 'wx_lite', 'pay_channel_code_type', 0, '', '', '微信小程序支付', '1',
        '2021-12-03 10:41:06', '1', '2021-12-03 10:41:06', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (115, 3, '微信 App 支付', 'wx_app', 'pay_channel_code_type', 0, '', '', '微信 App 支付', '1',
        '2021-12-03 10:41:20', '1', '2021-12-03 10:41:20', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (116, 4, '支付宝 PC 网站支付', 'alipay_pc', 'pay_channel_code_type', 0, '', '', '支付宝 PC 网站支付', '1',
        '2021-12-03 10:42:09', '1', '2021-12-03 10:42:09', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (117, 5, '支付宝 Wap 网站支付', 'alipay_wap', 'pay_channel_code_type', 0, '', '', '支付宝 Wap 网站支付', '1',
        '2021-12-03 10:42:26', '1', '2021-12-03 10:42:26', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (118, 6, '支付宝App 支付', 'alipay_app', 'pay_channel_code_type', 0, '', '', '支付宝App 支付', '1',
        '2021-12-03 10:42:55', '1', '2021-12-03 10:42:55', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (119, 7, '支付宝扫码支付', 'alipay_qr', 'pay_channel_code_type', 0, '', '', '支付宝扫码支付', '1',
        '2021-12-03 10:43:10', '1', '2021-12-03 10:43:10', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (120, 1, '通知成功', '10', 'pay_order_notify_status', 0, 'success', '', '通知成功', '1', '2021-12-03 11:02:41',
        '1', '2022-02-16 13:59:13', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (121, 2, '通知失败', '20', 'pay_order_notify_status', 0, 'danger', '', '通知失败', '1', '2021-12-03 11:02:59',
        '1', '2022-02-16 13:59:17', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (122, 3, '未通知', '0', 'pay_order_notify_status', 0, 'info', '', '未通知', '1', '2021-12-03 11:03:10', '1',
        '2022-02-16 13:59:23', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (123, 1, '支付成功', '10', 'pay_order_status', 0, 'success', '', '支付成功', '1', '2021-12-03 11:18:29', '1',
        '2022-02-16 15:24:25', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (124, 2, '支付关闭', '20', 'pay_order_status', 0, 'danger', '', '支付关闭', '1', '2021-12-03 11:18:42', '1',
        '2022-02-16 15:24:31', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (125, 3, '未支付', '0', 'pay_order_status', 0, 'info', '', '未支付', '1', '2021-12-03 11:18:18', '1',
        '2022-02-16 15:24:35', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (126, 1, '未退款', '0', 'pay_order_refund_status', 0, '', '', '未退款', '1', '2021-12-03 11:30:35', '1',
        '2021-12-03 11:34:05', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (127, 2, '部分退款', '10', 'pay_order_refund_status', 0, '', '', '部分退款', '1', '2021-12-03 11:30:44', '1',
        '2021-12-03 11:34:10', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (128, 3, '全部退款', '20', 'pay_order_refund_status', 0, '', '', '全部退款', '1', '2021-12-03 11:30:52', '1',
        '2021-12-03 11:34:14', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1117, 1, '退款订单生成', '0', 'pay_refund_order_status', 0, 'primary', '', '退款订单生成', '1',
        '2021-12-10 16:44:44', '1', '2022-02-16 14:05:24', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1118, 2, '退款成功', '1', 'pay_refund_order_status', 0, 'success', '', '退款成功', '1', '2021-12-10 16:44:59',
        '1', '2022-02-16 14:05:28', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1119, 3, '退款失败', '2', 'pay_refund_order_status', 0, 'danger', '', '退款失败', '1', '2021-12-10 16:45:10',
        '1', '2022-02-16 14:05:34', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1124, 8, '退款关闭', '99', 'pay_refund_order_status', 0, 'info', '', '退款关闭', '1', '2021-12-10 16:46:26',
        '1', '2022-02-16 14:05:40', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1125, 0, '默认', '1', 'bpm_model_category', 0, 'primary', '', '流程分类 - 默认', '1', '2022-01-02 08:41:11',
        '1', '2022-02-16 20:01:42', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1126, 0, 'OA', '2', 'bpm_model_category', 0, 'success', '', '流程分类 - OA', '1', '2022-01-02 08:41:22', '1',
        '2022-02-16 20:01:50', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1127, 0, '进行中', '1', 'bpm_process_instance_status', 0, 'primary', '', '流程实例的状态 - 进行中', '1',
        '2022-01-07 23:47:22', '1', '2022-02-16 20:07:49', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1128, 2, '已完成', '2', 'bpm_process_instance_status', 0, 'success', '', '流程实例的状态 - 已完成', '1',
        '2022-01-07 23:47:49', '1', '2022-02-16 20:07:54', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1129, 1, '处理中', '1', 'bpm_process_instance_result', 0, 'primary', '', '流程实例的结果 - 处理中', '1',
        '2022-01-07 23:48:32', '1', '2022-02-16 09:53:26', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1130, 2, '通过', '2', 'bpm_process_instance_result', 0, 'success', '', '流程实例的结果 - 通过', '1',
        '2022-01-07 23:48:45', '1', '2022-02-16 09:53:31', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1131, 3, '不通过', '3', 'bpm_process_instance_result', 0, 'danger', '', '流程实例的结果 - 不通过', '1',
        '2022-01-07 23:48:55', '1', '2022-02-16 09:53:38', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1132, 4, '已取消', '4', 'bpm_process_instance_result', 0, 'info', '', '流程实例的结果 - 撤销', '1',
        '2022-01-07 23:49:06', '1', '2022-02-16 09:53:42', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1133, 10, '流程表单', '10', 'bpm_model_form_type', 0, '', '', '流程的表单类型 - 流程表单', '103',
        '2022-01-11 23:51:30', '103', '2022-01-11 23:51:30', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1134, 20, '业务表单', '20', 'bpm_model_form_type', 0, '', '', '流程的表单类型 - 业务表单', '103',
        '2022-01-11 23:51:47', '103', '2022-01-11 23:51:47', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1135, 10, '角色', '10', 'bpm_task_assign_rule_type', 0, 'info', '', '任务分配规则的类型 - 角色', '103',
        '2022-01-12 23:21:22', '1', '2022-02-16 20:06:14', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1136, 20, '部门的成员', '20', 'bpm_task_assign_rule_type', 0, 'primary', '', '任务分配规则的类型 - 部门的成员',
        '103', '2022-01-12 23:21:47', '1', '2022-02-16 20:05:28', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1137, 21, '部门的负责人', '21', 'bpm_task_assign_rule_type', 0, 'primary', '',
        '任务分配规则的类型 - 部门的负责人', '103', '2022-01-12 23:33:36', '1', '2022-02-16 20:05:31', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1138, 30, '用户', '30', 'bpm_task_assign_rule_type', 0, 'info', '', '任务分配规则的类型 - 用户', '103',
        '2022-01-12 23:34:02', '1', '2022-02-16 20:05:50', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1139, 40, '用户组', '40', 'bpm_task_assign_rule_type', 0, 'warning', '', '任务分配规则的类型 - 用户组', '103',
        '2022-01-12 23:34:21', '1', '2022-02-16 20:05:57', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1140, 50, '自定义脚本', '50', 'bpm_task_assign_rule_type', 0, 'danger', '', '任务分配规则的类型 - 自定义脚本',
        '103', '2022-01-12 23:34:43', '1', '2022-02-16 20:06:01', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1141, 22, '岗位', '22', 'bpm_task_assign_rule_type', 0, 'success', '', '任务分配规则的类型 - 岗位', '103',
        '2022-01-14 18:41:55', '1', '2022-02-16 20:05:39', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1142, 10, '流程发起人', '10', 'bpm_task_assign_script', 0, '', '', '任务分配自定义脚本 - 流程发起人', '103',
        '2022-01-15 00:10:57', '103', '2022-01-15 21:24:10', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1143, 20, '流程发起人的一级领导', '20', 'bpm_task_assign_script', 0, '', '',
        '任务分配自定义脚本 - 流程发起人的一级领导', '103', '2022-01-15 21:24:31', '103', '2022-01-15 21:24:31', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1144, 21, '流程发起人的二级领导', '21', 'bpm_task_assign_script', 0, '', '',
        '任务分配自定义脚本 - 流程发起人的二级领导', '103', '2022-01-15 21:24:46', '103', '2022-01-15 21:24:57', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1145, 1, '管理后台', '1', 'infra_codegen_scene', 0, '', '', '代码生成的场景枚举 - 管理后台', '1',
        '2022-02-02 13:15:06', '1', '2022-03-10 16:32:59', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1146, 2, '用户 APP', '2', 'infra_codegen_scene', 0, '', '', '代码生成的场景枚举 - 用户 APP', '1',
        '2022-02-02 13:15:19', '1', '2022-03-10 16:33:03', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1147, 0, '未退款', '0', 'pay_refund_order_type', 0, 'info', '', '退款类型 - 未退款', '1', '2022-02-16 14:09:01',
        '1', '2022-02-16 14:09:01', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1148, 10, '部分退款', '10', 'pay_refund_order_type', 0, 'success', '', '退款类型 - 部分退款', '1',
        '2022-02-16 14:09:25', '1', '2022-02-16 14:11:38', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1149, 20, '全部退款', '20', 'pay_refund_order_type', 0, 'warning', '', '退款类型 - 全部退款', '1',
        '2022-02-16 14:11:33', '1', '2022-02-16 14:11:33', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1150, 1, '数据库', '1', 'infra_file_storage', 0, 'default', '', NULL, '1', '2022-03-15 00:25:28', '1',
        '2022-03-15 00:25:28', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1151, 10, '本地磁盘', '10', 'infra_file_storage', 0, 'default', '', NULL, '1', '2022-03-15 00:25:41', '1',
        '2022-03-15 00:25:56', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1152, 11, 'FTP 服务器', '11', 'infra_file_storage', 0, 'default', '', NULL, '1', '2022-03-15 00:26:06', '1',
        '2022-03-15 00:26:10', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1153, 12, 'SFTP 服务器', '12', 'infra_file_storage', 0, 'default', '', NULL, '1', '2022-03-15 00:26:22', '1',
        '2022-03-15 00:26:22', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1154, 20, 'S3 对象存储', '20', 'infra_file_storage', 0, 'default', '', NULL, '1', '2022-03-15 00:26:31', '1',
        '2022-03-15 00:26:45', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1155, 103, '短信登录', '103', 'system_login_type', 0, 'default', '', NULL, '1', '2022-05-09 23:57:58', '1',
        '2022-05-09 23:58:09', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1156, 1, 'password', 'password', 'system_oauth2_grant_type', 0, 'default', '', '密码模式', '1',
        '2022-05-12 00:22:05', '1', '2022-05-11 16:26:01', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1157, 2, 'authorization_code', 'authorization_code', 'system_oauth2_grant_type', 0, 'primary', '', '授权码模式',
        '1', '2022-05-12 00:22:59', '1', '2022-05-11 16:26:02', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1158, 3, 'implicit', 'implicit', 'system_oauth2_grant_type', 0, 'success', '', '简化模式', '1',
        '2022-05-12 00:23:40', '1', '2022-05-11 16:26:05', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1159, 4, 'client_credentials', 'client_credentials', 'system_oauth2_grant_type', 0, 'default', '', '客户端模式',
        '1', '2022-05-12 00:23:51', '1', '2022-05-11 16:26:08', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1160, 5, 'refresh_token', 'refresh_token', 'system_oauth2_grant_type', 0, 'info', '', '刷新模式', '1',
        '2022-05-12 00:24:02', '1', '2022-05-11 16:26:11', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1161, 4, 'Vue 3 Vben', '30', 'infra_codegen_front_type', 0, '', '', '', '1', '2023-06-14 15:24:37.447', '1',
        '2023-06-14 15:24:37.447', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1162, 3, 'Vue 3 Schema', '21', 'infra_codegen_front_type', 0, '', '', 'Vue 3 Element Plus Schema', '1',
        '2023-06-14 15:24:18.714', '1', '2023-06-14 15:36:40.317', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1163, 2, 'Vue 3', '20', 'infra_codegen_front_type', 0, '', '', 'Vue 3 Element Plus', '1',
        '2023-06-14 15:24:05.654', '1', '2023-06-14 15:24:05.654', 0);
INSERT INTO "system_dict_data" ("id", "sort", "label", "value", "dict_type", "status", "color_type", "css_class",
                                "remark", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (1164, 1, 'Vue 2', '10', 'infra_codegen_front_type', 0, '', '', 'Vue 2', '1', '2023-06-14 15:23:12.211', '1',
        '2023-06-14 15:23:57.816', 0);
COMMIT;

-- ----------------------------
-- Table structure for system_dict_type
-- ----------------------------
DROP TABLE IF EXISTS "system_dict_type";
CREATE TABLE "system_dict_type"
(
    "id"           int8                                        NOT NULL,
    "name"         varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
    "type"         varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
    "status"       int2                                        NOT NULL,
    "remark"       varchar(500) COLLATE "pg_catalog"."default",
    "creator"      varchar(64) COLLATE "pg_catalog"."default",
    "create_time"  timestamp(6)                                NOT NULL,
    "updater"      varchar(64) COLLATE "pg_catalog"."default",
    "update_time"  timestamp(6)                                NOT NULL,
    "deleted_time" timestamp(6),
    "deleted"      int2                                        NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_dict_type"."id" IS '字典主键';
COMMENT
ON COLUMN "system_dict_type"."name" IS '字典名称';
COMMENT
ON COLUMN "system_dict_type"."type" IS '字典类型';
COMMENT
ON COLUMN "system_dict_type"."status" IS '状态（0正常 1停用）';
COMMENT
ON COLUMN "system_dict_type"."remark" IS '备注';
COMMENT
ON COLUMN "system_dict_type"."creator" IS '创建者';
COMMENT
ON COLUMN "system_dict_type"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_dict_type"."updater" IS '更新者';
COMMENT
ON COLUMN "system_dict_type"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_dict_type"."deleted_time" IS '删除时间';
COMMENT
ON COLUMN "system_dict_type"."deleted" IS '是否删除';
COMMENT
ON TABLE "system_dict_type" IS '字典类型表';

-- ----------------------------
-- Records of system_dict_type
-- ----------------------------
BEGIN;
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (1, '用户性别', 'system_user_sex', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2022-02-01 16:30:31', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (6, '参数类型', 'infra_config_type', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2022-02-01 16:36:54', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (7, '通知类型', 'system_notice_type', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2022-02-01 16:35:26', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (9, '操作类型', 'system_operate_type', 0, NULL, 'admin', '2021-01-05 17:03:48', '1', '2022-02-16 09:32:21', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (10, '系统状态', 'common_status', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2022-02-01 16:21:28', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (11, 'Boolean 是否类型', 'infra_boolean_string', 0, 'boolean 转是否', '', '2021-01-19 03:20:08', '',
        '2022-02-01 16:37:10', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (104, '登陆结果', 'system_login_result', 0, '登陆结果', '', '2021-01-18 06:17:11', '', '2022-02-01 16:36:00', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (105, 'Redis 超时类型', 'infra_redis_timeout_type', 0, 'RedisKeyDefine.TimeoutTypeEnum', '',
        '2021-01-26 00:52:50', '', '2022-02-01 16:50:29', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (106, '代码生成模板类型', 'infra_codegen_template_type', 0, NULL, '', '2021-02-05 07:08:06', '',
        '2022-03-10 16:33:42', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (107, '定时任务状态', 'infra_job_status', 0, NULL, '', '2021-02-07 07:44:16', '', '2022-02-01 16:51:11', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (108, '定时任务日志状态', 'infra_job_log_status', 0, NULL, '', '2021-02-08 10:03:51', '', '2022-02-01 16:50:43',
        0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (109, '用户类型', 'user_type', 0, NULL, '', '2021-02-26 00:15:51', '', '2021-02-26 00:15:51', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (110, 'API 异常数据的处理状态', 'infra_api_error_log_process_status', 0, NULL, '', '2021-02-26 07:07:01', '',
        '2022-02-01 16:50:53', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (111, '短信渠道编码', 'system_sms_channel_code', 0, NULL, '1', '2021-04-05 01:04:50', '1', '2022-02-16 02:09:08',
        0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (112, '短信模板的类型', 'system_sms_template_type', 0, NULL, '1', '2021-04-05 21:50:43', '1',
        '2022-02-01 16:35:06', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (113, '短信发送状态', 'system_sms_send_status', 0, NULL, '1', '2021-04-11 20:18:03', '1', '2022-02-01 16:35:09',
        0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (114, '短信接收状态', 'system_sms_receive_status', 0, NULL, '1', '2021-04-11 20:27:14', '1',
        '2022-02-01 16:35:14', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (115, '错误码的类型', 'system_error_code_type', 0, NULL, '1', '2021-04-21 00:06:30', '1', '2022-02-01 16:36:49',
        0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (116, '登陆日志的类型', 'system_login_type', 0, '登陆日志的类型', '1', '2021-10-06 00:50:46', '1',
        '2022-02-01 16:35:56', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (117, 'OA 请假类型', 'bpm_oa_leave_type', 0, NULL, '1', '2021-09-21 22:34:33', '1', '2022-01-22 10:41:37', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (122, '支付渠道微信版本', 'pay_channel_wechat_version', 0, '支付渠道微信版本', '1', '2021-11-08 17:00:26', '1',
        '2021-11-08 17:00:26', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (127, '支付渠道支付宝算法类型', 'pay_channel_alipay_sign_type', 0, '支付渠道支付宝算法类型', '1',
        '2021-11-18 15:39:09', '1', '2021-11-18 15:39:09', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (128, '支付渠道支付宝公钥类型', 'pay_channel_alipay_mode', 0, '支付渠道支付宝公钥类型', '1',
        '2021-11-18 15:44:28', '1', '2021-11-18 15:44:28', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (129, '支付宝网关地址', 'pay_channel_alipay_server_type', 0, '支付宝网关地址', '1', '2021-11-18 16:58:55', '1',
        '2021-11-18 17:01:34', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (130, '支付渠道编码类型', 'pay_channel_code_type', 0, '支付渠道的编码', '1', '2021-12-03 10:35:08', '1',
        '2021-12-03 10:35:08', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (131, '支付订单回调状态', 'pay_order_notify_status', 0, '支付订单回调状态', '1', '2021-12-03 10:53:29', '1',
        '2021-12-03 10:53:29', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (132, '支付订单状态', 'pay_order_status', 0, '支付订单状态', '1', '2021-12-03 11:17:50', '1',
        '2021-12-03 11:17:50', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (133, '支付订单退款状态', 'pay_order_refund_status', 0, '支付订单退款状态', '1', '2021-12-03 11:27:31', '1',
        '2021-12-03 11:27:31', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (134, '退款订单状态', 'pay_refund_order_status', 0, '退款订单状态', '1', '2021-12-10 16:42:50', '1',
        '2021-12-10 16:42:50', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (135, '退款订单类别', 'pay_refund_order_type', 0, '退款订单类别', '1', '2021-12-10 17:14:53', '1',
        '2021-12-10 17:14:53', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (138, '流程分类', 'bpm_model_category', 0, '流程分类', '1', '2022-01-02 08:40:45', '1', '2022-01-02 08:40:45',
        0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (139, '流程实例的状态', 'bpm_process_instance_status', 0, '流程实例的状态', '1', '2022-01-07 23:46:42', '1',
        '2022-01-07 23:46:42', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (140, '流程实例的结果', 'bpm_process_instance_result', 0, '流程实例的结果', '1', '2022-01-07 23:48:10', '1',
        '2022-01-07 23:48:10', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (141, '流程的表单类型', 'bpm_model_form_type', 0, '流程的表单类型', '103', '2022-01-11 23:50:45', '103',
        '2022-01-11 23:50:45', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (142, '任务分配规则的类型', 'bpm_task_assign_rule_type', 0, '任务分配规则的类型', '103', '2022-01-12 23:21:04',
        '103', '2022-01-12 15:46:10', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (143, '任务分配自定义脚本', 'bpm_task_assign_script', 0, '任务分配自定义脚本', '103', '2022-01-15 00:10:35',
        '103', '2022-01-15 00:10:35', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (144, '代码生成的场景枚举', 'infra_codegen_scene', 0, '代码生成的场景枚举', '1', '2022-02-02 13:14:45', '1',
        '2022-03-10 16:33:46', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (145, '角色类型', 'system_role_type', 0, '角色类型', '1', '2022-02-16 13:01:46', '1', '2022-02-16 13:01:46', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (146, '文件存储器', 'infra_file_storage', 0, '文件存储器', '1', '2022-03-15 00:24:38', '1',
        '2022-03-15 00:24:38', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (147, 'OAuth 2.0 授权类型', 'system_oauth2_grant_type', 0, 'OAuth 2.0 授权类型（模式）', '1',
        '2022-05-12 00:20:52', '1', '2022-05-11 16:25:49', 0);
INSERT INTO "system_dict_type" ("id", "name", "type", "status", "remark", "creator", "create_time", "updater",
                                "update_time", "deleted")
VALUES (148, '生成前端代码类型', 'infra_codegen_front_type', 0, '生成前端代码类型', '1', '2023-6-14 16:07:35', '1',
        '2023-6-14 16:07:39', 0);
COMMIT;

-- ----------------------------
-- Table structure for system_error_code
-- ----------------------------
DROP TABLE IF EXISTS "system_error_code";
CREATE TABLE "system_error_code"
(
    "id"               int8                                        NOT NULL,
    "type"             int2                                        NOT NULL,
    "application_name" varchar(50) COLLATE "pg_catalog"."default"  NOT NULL,
    "code"             int4                                        NOT NULL,
    "message"          varchar(512) COLLATE "pg_catalog"."default" NOT NULL,
    "memo"             varchar(512) COLLATE "pg_catalog"."default",
    "creator"          varchar(64) COLLATE "pg_catalog"."default",
    "create_time"      timestamp(6)                                NOT NULL,
    "updater"          varchar(64) COLLATE "pg_catalog"."default",
    "update_time"      timestamp(6)                                NOT NULL,
    "deleted"          int2                                        NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_error_code"."id" IS '错误码编号';
COMMENT
ON COLUMN "system_error_code"."type" IS '错误码类型';
COMMENT
ON COLUMN "system_error_code"."application_name" IS '应用名';
COMMENT
ON COLUMN "system_error_code"."code" IS '错误码编码';
COMMENT
ON COLUMN "system_error_code"."message" IS '错误码错误提示';
COMMENT
ON COLUMN "system_error_code"."memo" IS '备注';
COMMENT
ON COLUMN "system_error_code"."creator" IS '创建者';
COMMENT
ON COLUMN "system_error_code"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_error_code"."updater" IS '更新者';
COMMENT
ON COLUMN "system_error_code"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_error_code"."deleted" IS '是否删除';
COMMENT
ON TABLE "system_error_code" IS '错误码表';

-- ----------------------------
-- Records of system_error_code
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for system_login_log
-- ----------------------------
DROP TABLE IF EXISTS "system_login_log";
CREATE TABLE "system_login_log"
(
    "id"          int8                                        NOT NULL,
    "log_type"    int8                                        NOT NULL,
    "trace_id"    varchar(64) COLLATE "pg_catalog"."default"  NOT NULL,
    "user_id"     int8                                        NOT NULL DEFAULT 0,
    "user_type"   int2                                        NOT NULL,
    "username"    varchar(50) COLLATE "pg_catalog"."default"  NOT NULL,
    "result"      int2                                        NOT NULL,
    "user_ip"     varchar(50) COLLATE "pg_catalog"."default"  NOT NULL,
    "user_agent"  varchar(512) COLLATE "pg_catalog"."default" NOT NULL,
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                                NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                                NOT NULL,
    "deleted"     int2                                        NOT NULL DEFAULT 0,
    "tenant_id"   int8                                        NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_login_log"."id" IS '访问ID';
COMMENT
ON COLUMN "system_login_log"."log_type" IS '日志类型';
COMMENT
ON COLUMN "system_login_log"."trace_id" IS '链路追踪编号';
COMMENT
ON COLUMN "system_login_log"."user_id" IS '用户编号';
COMMENT
ON COLUMN "system_login_log"."user_type" IS '用户类型';
COMMENT
ON COLUMN "system_login_log"."username" IS '用户账号';
COMMENT
ON COLUMN "system_login_log"."result" IS '登陆结果';
COMMENT
ON COLUMN "system_login_log"."user_ip" IS '用户 IP';
COMMENT
ON COLUMN "system_login_log"."user_agent" IS '浏览器 UA';
COMMENT
ON COLUMN "system_login_log"."creator" IS '创建者';
COMMENT
ON COLUMN "system_login_log"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_login_log"."updater" IS '更新者';
COMMENT
ON COLUMN "system_login_log"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_login_log"."deleted" IS '是否删除';
COMMENT
ON COLUMN "system_login_log"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "system_login_log" IS '系统访问记录';

-- ----------------------------
-- Records of system_login_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for system_menu
-- ----------------------------
DROP TABLE IF EXISTS "system_menu";
CREATE TABLE "system_menu"
(
    "id"             int8                                        NOT NULL DEFAULT nextval('system_menu_seq'::regclass),
    "name"           varchar(50) COLLATE "pg_catalog"."default"  NOT NULL,
    "permission"     varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
    "type"           int2                                        NOT NULL,
    "sort"           int4                                        NOT NULL,
    "parent_id"      int8                                        NOT NULL,
    "path"           varchar(200) COLLATE "pg_catalog"."default",
    "icon"           varchar(100) COLLATE "pg_catalog"."default",
    "component"      varchar(255) COLLATE "pg_catalog"."default",
    "status"         int2                                        NOT NULL,
    "visible"        bool                                        NOT NULL DEFAULT true,
    "keep_alive"     bool                                        NOT NULL DEFAULT false,
    "creator"        varchar(64) COLLATE "pg_catalog"."default",
    "create_time"    timestamp(6)                                NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    "updater"        varchar(64) COLLATE "pg_catalog"."default",
    "update_time"    timestamp(6)                                NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    "deleted"        int2                                        NOT NULL DEFAULT 0,
    "component_name" varchar(255) COLLATE "pg_catalog"."default",
    "always_show"    bool                                        NOT NULL DEFAULT false
)
;

ALTER TABLE "system_menu"
    ADD COLUMN query VARCHAR(200) COLLATE "pg_catalog"."default";

COMMENT
ON COLUMN "system_menu"."id" IS '菜单ID';
COMMENT
ON COLUMN "system_menu"."name" IS '菜单名称';
COMMENT
ON COLUMN "system_menu"."permission" IS '权限标识';
COMMENT
ON COLUMN "system_menu"."type" IS '菜单类型';
COMMENT
ON COLUMN "system_menu"."sort" IS '显示顺序';
COMMENT
ON COLUMN "system_menu"."parent_id" IS '父菜单ID';
COMMENT
ON COLUMN "system_menu"."path" IS '路由地址';
COMMENT
ON COLUMN "system_menu"."icon" IS '菜单图标';
COMMENT
ON COLUMN "system_menu"."component" IS '组件路径';
COMMENT
ON COLUMN "system_menu"."status" IS '菜单状态';
COMMENT
ON COLUMN "system_menu"."visible" IS '是否可见';
COMMENT
ON COLUMN "system_menu"."keep_alive" IS '是否缓存';
COMMENT
ON COLUMN "system_menu"."creator" IS '创建者';
COMMENT
ON COLUMN "system_menu"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_menu"."updater" IS '更新者';
COMMENT
ON COLUMN "system_menu"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_menu"."deleted" IS '是否删除';
COMMENT
ON COLUMN "system_menu"."component_name" IS '组件名称';
COMMENT
ON COLUMN "system_menu"."always_show" IS '是否总是显示';
COMMENT
ON TABLE "system_menu" IS '菜单权限表';

ALTER TABLE system_menu
    ALTER COLUMN permission SET DEFAULT '';
-- ----------------------------
-- Records of system_menu
-- ----------------------------
BEGIN;
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1, '系统管理', '', 1, 10, 0, '/system', 'system', NULL, 0, 't', 't', 'admin', '2021-01-05 17:03:48', '1',
        '2022-05-13 01:02:57.073', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (2, '基础设施', '', 1, 20, 0, '/infra', 'monitor', NULL, 0, 't', 't', 'admin', '2021-01-05 17:03:48', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (5, 'OA 示例', '', 1, 40, 1185, 'oa', 'people', NULL, 0, 't', 't', 'admin', '2021-09-20 16:26:19', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (100, '用户管理', 'system:user:list', 2, 1, 1, 'user', 'user', 'system/user/index', 0, 't', 't', 'admin',
        '2021-01-05 17:03:48', '', '2022-04-20 17:03:10', 0, 'User', '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (101, '角色管理', '', 2, 2, 1, 'role', 'peoples', 'system/role/index', 0, 't', 't', 'admin',
        '2021-01-05 17:03:48', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (102, '菜单管理', '', 2, 3, 1, 'menu', 'tree-table', 'system/menu/index', 0, 't', 't', 'admin',
        '2021-01-05 17:03:48', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (103, '部门管理', '', 2, 4, 1, 'dept', 'tree', 'system/dept/index', 0, 't', 't', 'admin', '2021-01-05 17:03:48',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (104, '岗位管理', '', 2, 5, 1, 'post', 'post', 'system/post/index', 0, 't', 't', 'admin', '2021-01-05 17:03:48',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (105, '字典管理', '', 2, 6, 1, 'dict', 'dict', 'system/dict/index', 0, 't', 't', 'admin', '2021-01-05 17:03:48',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (106, '配置管理', '', 2, 6, 2, 'config', 'edit', 'infra/config/index', 0, 't', 't', 'admin',
        '2021-01-05 17:03:48', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (107, '通知公告', '', 2, 8, 1, 'notice', 'message', 'system/notice/index', 0, 't', 't', 'admin',
        '2021-01-05 17:03:48', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (108, '审计日志', '', 1, 9, 1, 'log', 'log', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (109, '令牌管理', '', 2, 2, 1261, 'token', 'online', 'system/oauth2/token/index', 0, 't', 't', 'admin',
        '2021-01-05 17:03:48', '1', '2022-05-11 23:31:42', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (110, '定时任务', '', 2, 12, 2, 'job', 'job', 'infra/job/index', 0, 't', 't', 'admin', '2021-01-05 17:03:48',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (111, 'MySQL 监控', '', 2, 9, 2, 'druid', 'druid', 'infra/druid/index', 0, 't', 't', 'admin',
        '2021-01-05 17:03:48', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (112, 'Java 监控', '', 2, 11, 2, 'admin-server', 'server', 'infra/server/index', 0, 't', 't', 'admin',
        '2021-01-05 17:03:48', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (113, 'Redis 监控', '', 2, 10, 2, 'redis', 'redis', 'infra/redis/index', 0, 't', 't', 'admin',
        '2021-01-05 17:03:48', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (114, '表单构建', 'infra:build:list', 2, 2, 2, 'build', 'build', 'infra/build/index', 0, 't', 't', 'admin',
        '2021-01-05 17:03:48', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (115, '代码生成', 'infra:codegen:query', 2, 1, 2, 'codegen', 'code', 'infra/codegen/index', 0, 't', 't', 'admin',
        '2021-01-05 17:03:48', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (116, '系统接口', 'infra:swagger:list', 2, 3, 2, 'swagger', 'swagger', 'infra/swagger/index', 0, 't', 't',
        'admin', '2021-01-05 17:03:48', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (500, '操作日志', '', 2, 1, 108, 'operate-log', 'form', 'system/operatelog/index', 0, 't', 't', 'admin',
        '2021-01-05 17:03:48', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (501, '登录日志', '', 2, 2, 108, 'login-log', 'logininfor', 'system/loginlog/index', 0, 't', 't', 'admin',
        '2021-01-05 17:03:48', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1001, '用户查询', 'system:user:query', 3, 1, 100, '', '#', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1002, '用户新增', 'system:user:create', 3, 2, 100, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1003, '用户修改', 'system:user:update', 3, 3, 100, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1004, '用户删除', 'system:user:delete', 3, 4, 100, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1005, '用户导出', 'system:user:export', 3, 5, 100, '', '#', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1006, '用户导入', 'system:user:import', 3, 6, 100, '', '#', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1007, '重置密码', 'system:user:update-password', 3, 7, 100, '', '', '', 0, 't', 't', 'admin',
        '2021-01-05 17:03:48', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1008, '角色查询', 'system:role:query', 3, 1, 101, '', '#', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1009, '角色新增', 'system:role:create', 3, 2, 101, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1010, '角色修改', 'system:role:update', 3, 3, 101, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1011, '角色删除', 'system:role:delete', 3, 4, 101, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1012, '角色导出', 'system:role:export', 3, 5, 101, '', '#', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1013, '菜单查询', 'system:menu:query', 3, 1, 102, '', '#', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1014, '菜单新增', 'system:menu:create', 3, 2, 102, '', '#', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1015, '菜单修改', 'system:menu:update', 3, 3, 102, '', '#', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1016, '菜单删除', 'system:menu:delete', 3, 4, 102, '', '#', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1017, '部门查询', 'system:dept:query', 3, 1, 103, '', '#', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1018, '部门新增', 'system:dept:create', 3, 2, 103, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1019, '部门修改', 'system:dept:update', 3, 3, 103, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1020, '部门删除', 'system:dept:delete', 3, 4, 103, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1021, '岗位查询', 'system:post:query', 3, 1, 104, '', '#', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1022, '岗位新增', 'system:post:create', 3, 2, 104, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1023, '岗位修改', 'system:post:update', 3, 3, 104, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1024, '岗位删除', 'system:post:delete', 3, 4, 104, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1025, '岗位导出', 'system:post:export', 3, 5, 104, '', '#', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1026, '字典查询', 'system:dict:query', 3, 1, 105, '#', '#', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1027, '字典新增', 'system:dict:create', 3, 2, 105, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1028, '字典修改', 'system:dict:update', 3, 3, 105, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1029, '字典删除', 'system:dict:delete', 3, 4, 105, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1030, '字典导出', 'system:dict:export', 3, 5, 105, '#', '#', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48',
        '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1031, '配置查询', 'infra:config:query', 3, 1, 106, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1032, '配置新增', 'infra:config:create', 3, 2, 106, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1033, '配置修改', 'infra:config:update', 3, 3, 106, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1034, '配置删除', 'infra:config:delete', 3, 4, 106, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1035, '配置导出', 'infra:config:export', 3, 5, 106, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1036, '公告查询', 'system:notice:query', 3, 1, 107, '#', '#', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48',
        '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1037, '公告新增', 'system:notice:create', 3, 2, 107, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1038, '公告修改', 'system:notice:update', 3, 3, 107, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1039, '公告删除', 'system:notice:delete', 3, 4, 107, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1040, '操作查询', 'system:operate-log:query', 3, 1, 500, '', '', '', 0, 't', 't', 'admin',
        '2021-01-05 17:03:48', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1042, '日志导出', 'system:operate-log:export', 3, 2, 500, '', '', '', 0, 't', 't', 'admin',
        '2021-01-05 17:03:48', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1043, '登录查询', 'system:login-log:query', 3, 1, 501, '#', '#', '', 0, 't', 't', 'admin',
        '2021-01-05 17:03:48', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1045, '日志导出', 'system:login-log:export', 3, 3, 501, '#', '#', '', 0, 't', 't', 'admin',
        '2021-01-05 17:03:48', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1046, '令牌列表', 'system:oauth2-token:page', 3, 1, 109, '', '', '', 0, 't', 't', 'admin',
        '2021-01-05 17:03:48', '1', '2022-05-09 23:54:42', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1048, '令牌删除', 'system:oauth2-token:delete', 3, 2, 109, '', '', '', 0, 't', 't', 'admin',
        '2021-01-05 17:03:48', '1', '2022-05-09 23:54:53', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1050, '任务新增', 'infra:job:create', 3, 2, 110, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1051, '任务修改', 'infra:job:update', 3, 3, 110, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1052, '任务删除', 'infra:job:delete', 3, 4, 110, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1053, '状态修改', 'infra:job:update', 3, 5, 110, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1054, '任务导出', 'infra:job:export', 3, 7, 110, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1056, '生成修改', 'infra:codegen:update', 3, 2, 115, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1057, '生成删除', 'infra:codegen:delete', 3, 3, 115, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1058, '导入代码', 'infra:codegen:create', 3, 2, 115, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1059, '预览代码', 'infra:codegen:preview', 3, 4, 115, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1060, '生成代码', 'infra:codegen:download', 3, 5, 115, '', '', '', 0, 't', 't', 'admin', '2021-01-05 17:03:48',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1063, '设置角色菜单权限', 'system:permission:assign-role-menu', 3, 6, 101, '', '', '', 0, 't', 't', '',
        '2021-01-06 17:53:44', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1064, '设置角色数据权限', 'system:permission:assign-role-data-scope', 3, 7, 101, '', '', '', 0, 't', 't', '',
        '2021-01-06 17:56:31', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1065, '设置用户角色', 'system:permission:assign-user-role', 3, 8, 101, '', '', '', 0, 't', 't', '',
        '2021-01-07 10:23:28', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1066, '获得 Redis 监控信息', 'infra:redis:get-monitor-info', 3, 1, 113, '', '', '', 0, 't', 't', '',
        '2021-01-26 01:02:31', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1067, '获得 Redis Key 列表', 'infra:redis:get-key-list', 3, 2, 113, '', '', '', 0, 't', 't', '',
        '2021-01-26 01:02:52', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1070, '代码生成示例', 'infra:test-demo:query', 2, 1, 2, 'test-demo', 'validCode', 'infra/testDemo/index', 0,
        't', 't', '', '2021-02-06 12:42:49', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1071, '测试示例表创建', 'infra:test-demo:create', 3, 1, 1070, '', '', '', 0, 't', 't', '',
        '2021-02-06 12:42:49', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1072, '测试示例表更新', 'infra:test-demo:update', 3, 2, 1070, '', '', '', 0, 't', 't', '',
        '2021-02-06 12:42:49', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1073, '测试示例表删除', 'infra:test-demo:delete', 3, 3, 1070, '', '', '', 0, 't', 't', '',
        '2021-02-06 12:42:49', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1074, '测试示例表导出', 'infra:test-demo:export', 3, 4, 1070, '', '', '', 0, 't', 't', '',
        '2021-02-06 12:42:49', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1075, '任务触发', 'infra:job:trigger', 3, 8, 110, '', '', '', 0, 't', 't', '', '2021-02-07 13:03:10', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1076, '数据库文档', '', 2, 4, 2, 'db-doc', 'table', 'infra/dbDoc/index', 0, 't', 't', '', '2021-02-08 01:41:47',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1077, '监控平台', '', 2, 13, 2, 'skywalking', 'eye-open', 'infra/skywalking/index', 0, 't', 't', '',
        '2021-02-08 20:41:31', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1078, '访问日志', '', 2, 1, 1083, 'api-access-log', 'log', 'infra/apiAccessLog/index', 0, 't', 't', '',
        '2021-02-26 01:32:59', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1082, '日志导出', 'infra:api-access-log:export', 3, 2, 1078, '', '', '', 0, 't', 't', '', '2021-02-26 01:32:59',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1083, 'API 日志', '', 2, 8, 2, 'log', 'log', NULL, 0, 't', 't', '', '2021-02-26 02:18:24', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1084, '错误日志', 'infra:api-error-log:query', 2, 2, 1083, 'api-error-log', 'log', 'infra/apiErrorLog/index', 0,
        't', 't', '', '2021-02-26 07:53:20', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1085, '日志处理', 'infra:api-error-log:update-status', 3, 2, 1084, '', '', '', 0, 't', 't', '',
        '2021-02-26 07:53:20', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1086, '日志导出', 'infra:api-error-log:export', 3, 3, 1084, '', '', '', 0, 't', 't', '', '2021-02-26 07:53:20',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1087, '任务查询', 'infra:job:query', 3, 1, 110, '', '', '', 0, 't', 't', '1', '2021-03-10 01:26:19', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1088, '日志查询', 'infra:api-access-log:query', 3, 1, 1078, '', '', '', 0, 't', 't', '1', '2021-03-10 01:28:04',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1089, '日志查询', 'infra:api-error-log:query', 3, 1, 1084, '', '', '', 0, 't', 't', '1', '2021-03-10 01:29:09',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1090, '文件列表', '', 2, 5, 1243, 'file', 'upload', 'infra/file/index', 0, 't', 't', '', '2021-03-12 20:16:20',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1091, '文件查询', 'infra:file:query', 3, 1, 1090, '', '', '', 0, 't', 't', '', '2021-03-12 20:16:20', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1092, '文件删除', 'infra:file:delete', 3, 4, 1090, '', '', '', 0, 't', 't', '', '2021-03-12 20:16:20', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1093, '短信管理', '', 1, 11, 1, 'sms', 'validCode', NULL, 0, 't', 't', '1', '2021-04-05 01:10:16', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1094, '短信渠道', '', 2, 0, 1093, 'sms-channel', 'phone', 'system/sms/channel', 0, 't', 't', '',
        '2021-04-01 11:07:15', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1095, '短信渠道查询', 'system:sms-channel:query', 3, 1, 1094, '', '', '', 0, 't', 't', '',
        '2021-04-01 11:07:15', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1096, '短信渠道创建', 'system:sms-channel:create', 3, 2, 1094, '', '', '', 0, 't', 't', '',
        '2021-04-01 11:07:15', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1097, '短信渠道更新', 'system:sms-channel:update', 3, 3, 1094, '', '', '', 0, 't', 't', '',
        '2021-04-01 11:07:15', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1098, '短信渠道删除', 'system:sms-channel:delete', 3, 4, 1094, '', '', '', 0, 't', 't', '',
        '2021-04-01 11:07:15', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1100, '短信模板', '', 2, 1, 1093, 'sms-template', 'phone', 'system/sms/template', 0, 't', 't', '',
        '2021-04-01 17:35:17', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1101, '短信模板查询', 'system:sms-template:query', 3, 1, 1100, '', '', '', 0, 't', 't', '',
        '2021-04-01 17:35:17', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1102, '短信模板创建', 'system:sms-template:create', 3, 2, 1100, '', '', '', 0, 't', 't', '',
        '2021-04-01 17:35:17', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1103, '短信模板更新', 'system:sms-template:update', 3, 3, 1100, '', '', '', 0, 't', 't', '',
        '2021-04-01 17:35:17', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1104, '短信模板删除', 'system:sms-template:delete', 3, 4, 1100, '', '', '', 0, 't', 't', '',
        '2021-04-01 17:35:17', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1105, '短信模板导出', 'system:sms-template:export', 3, 5, 1100, '', '', '', 0, 't', 't', '',
        '2021-04-01 17:35:17', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1106, '发送测试短信', 'system:sms-template:send-sms', 3, 6, 1100, '', '', '', 0, 't', 't', '1',
        '2021-04-11 00:26:40', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1107, '短信日志', '', 2, 2, 1093, 'sms-log', 'phone', 'system/sms/log', 0, 't', 't', '', '2021-04-11 08:37:05',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1108, '短信日志查询', 'system:sms-log:query', 3, 1, 1107, '', '', '', 0, 't', 't', '', '2021-04-11 08:37:05',
        '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1109, '短信日志导出', 'system:sms-log:export', 3, 5, 1107, '', '', '', 0, 't', 't', '', '2021-04-11 08:37:05',
        '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1110, '错误码管理', '', 2, 12, 1, 'error-code', 'code', 'system/errorCode/index', 0, 't', 't', '',
        '2021-04-13 21:46:42', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1111, '错误码查询', 'system:error-code:query', 3, 1, 1110, '', '', '', 0, 't', 't', '', '2021-04-13 21:46:42',
        '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1112, '错误码创建', 'system:error-code:create', 3, 2, 1110, '', '', '', 0, 't', 't', '', '2021-04-13 21:46:42',
        '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1113, '错误码更新', 'system:error-code:update', 3, 3, 1110, '', '', '', 0, 't', 't', '', '2021-04-13 21:46:42',
        '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1114, '错误码删除', 'system:error-code:delete', 3, 4, 1110, '', '', '', 0, 't', 't', '', '2021-04-13 21:46:42',
        '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1115, '错误码导出', 'system:error-code:export', 3, 5, 1110, '', '', '', 0, 't', 't', '', '2021-04-13 21:46:42',
        '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1117, '支付管理', '', 1, 11, 0, '/pay', 'money', NULL, 0, 't', 't', '1', '2021-12-25 16:43:41', '1',
        '2022-05-13 01:02:25.244', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1118, '请假查询', '', 2, 0, 5, 'leave', 'user', 'bpm/oa/leave/index', 0, 't', 't', '', '2021-09-20 08:51:03',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1119, '请假申请查询', 'bpm:oa-leave:query', 3, 1, 1118, '', '', '', 0, 't', 't', '', '2021-09-20 08:51:03', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1120, '请假申请创建', 'bpm:oa-leave:create', 3, 2, 1118, '', '', '', 0, 't', 't', '', '2021-09-20 08:51:03',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1126, '应用信息', '', 2, 1, 1117, 'app', 'table', 'pay/app/index', 0, 't', 't', '', '2021-11-10 01:13:30', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1127, '支付应用信息查询', 'pay:app:query', 3, 1, 1126, '', '', '', 0, 't', 't', '', '2021-11-10 01:13:31', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1128, '支付应用信息创建', 'pay:app:create', 3, 2, 1126, '', '', '', 0, 't', 't', '', '2021-11-10 01:13:31', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1129, '支付应用信息更新', 'pay:app:update', 3, 3, 1126, '', '', '', 0, 't', 't', '', '2021-11-10 01:13:31', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1130, '支付应用信息删除', 'pay:app:delete', 3, 4, 1126, '', '', '', 0, 't', 't', '', '2021-11-10 01:13:31', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1131, '支付应用信息导出', 'pay:app:export', 3, 5, 1126, '', '', '', 0, 't', 't', '', '2021-11-10 01:13:31', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1132, '秘钥解析', 'pay:channel:parsing', 3, 6, 1129, '', '', '', 0, 't', 't', '1', '2021-11-08 15:15:47', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1133, '支付商户信息查询', 'pay:merchant:query', 3, 1, 1132, '', '', '', 0, 't', 't', '', '2021-11-10 01:13:41',
        '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1134, '支付商户信息创建', 'pay:merchant:create', 3, 2, 1132, '', '', '', 0, 't', 't', '', '2021-11-10 01:13:41',
        '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1135, '支付商户信息更新', 'pay:merchant:update', 3, 3, 1132, '', '', '', 0, 't', 't', '', '2021-11-10 01:13:41',
        '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1136, '支付商户信息删除', 'pay:merchant:delete', 3, 4, 1132, '', '', '', 0, 't', 't', '', '2021-11-10 01:13:41',
        '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1137, '支付商户信息导出', 'pay:merchant:export', 3, 5, 1132, '', '', '', 0, 't', 't', '', '2021-11-10 01:13:41',
        '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1138, '租户列表', '', 2, 0, 1224, 'list', 'peoples', 'system/tenant/index', 0, 't', 't', '',
        '2021-12-14 12:31:43', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1139, '租户查询', 'system:tenant:query', 3, 1, 1138, '', '', '', 0, 't', 't', '', '2021-12-14 12:31:44', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1140, '租户创建', 'system:tenant:create', 3, 2, 1138, '', '', '', 0, 't', 't', '', '2021-12-14 12:31:44', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1141, '租户更新', 'system:tenant:update', 3, 3, 1138, '', '', '', 0, 't', 't', '', '2021-12-14 12:31:44', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1142, '租户删除', 'system:tenant:delete', 3, 4, 1138, '', '', '', 0, 't', 't', '', '2021-12-14 12:31:44', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1143, '租户导出', 'system:tenant:export', 3, 5, 1138, '', '', '', 0, 't', 't', '', '2021-12-14 12:31:44', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1150, '秘钥解析', '', 3, 6, 1129, '', '', '', 0, 't', 't', '1', '2021-11-08 15:15:47', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1161, '退款订单', '', 2, 3, 1117, 'refund', 'order', 'pay/refund/index', 0, 't', 't', '', '2021-12-25 08:29:07',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1162, '退款订单查询', 'pay:refund:query', 3, 1, 1161, '', '', '', 0, 't', 't', '', '2021-12-25 08:29:07', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1163, '退款订单创建', 'pay:refund:create', 3, 2, 1161, '', '', '', 0, 't', 't', '', '2021-12-25 08:29:07', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1164, '退款订单更新', 'pay:refund:update', 3, 3, 1161, '', '', '', 0, 't', 't', '', '2021-12-25 08:29:07', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1165, '退款订单删除', 'pay:refund:delete', 3, 4, 1161, '', '', '', 0, 't', 't', '', '2021-12-25 08:29:07', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1166, '退款订单导出', 'pay:refund:export', 3, 5, 1161, '', '', '', 0, 't', 't', '', '2021-12-25 08:29:07', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1173, '支付订单', '', 2, 2, 1117, 'order', 'pay', 'pay/order/index', 0, 't', 't', '', '2021-12-25 08:49:43',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1174, '支付订单查询', 'pay:order:query', 3, 1, 1173, '', '', '', 0, 't', 't', '', '2021-12-25 08:49:43', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1175, '支付订单创建', 'pay:order:create', 3, 2, 1173, '', '', '', 0, 't', 't', '', '2021-12-25 08:49:43', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1176, '支付订单更新', 'pay:order:update', 3, 3, 1173, '', '', '', 0, 't', 't', '', '2021-12-25 08:49:43', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1177, '支付订单删除', 'pay:order:delete', 3, 4, 1173, '', '', '', 0, 't', 't', '', '2021-12-25 08:49:43', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1178, '支付订单导出', 'pay:order:export', 3, 5, 1173, '', '', '', 0, 't', 't', '', '2021-12-25 08:49:43', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1179, '商户信息', '', 2, 0, 1117, 'merchant', 'merchant', 'pay/merchant/index', 0, 't', 't', '',
        '2021-12-25 09:01:44', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1180, '支付商户信息查询', 'pay:merchant:query', 3, 1, 1179, '', '', '', 0, 't', 't', '', '2021-12-25 09:01:44',
        '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1181, '支付商户信息创建', 'pay:merchant:create', 3, 2, 1179, '', '', '', 0, 't', 't', '', '2021-12-25 09:01:44',
        '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1182, '支付商户信息更新', 'pay:merchant:update', 3, 3, 1179, '', '', '', 0, 't', 't', '', '2021-12-25 09:01:44',
        '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1183, '支付商户信息删除', '', 3, 4, 1179, '', '', '', 0, 't', 't', '', '2021-12-25 09:01:44', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1184, '支付商户信息导出', 'pay:merchant:export', 3, 5, 1179, '', '', '', 0, 't', 't', '', '2021-12-25 09:01:44',
        '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1185, '工作流程', '', 1, 50, 0, '/bpm', 'tool', NULL, 0, 't', 't', '1', '2021-12-30 20:26:36', '103',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1186, '流程管理', '', 1, 10, 1185, 'manager', 'nested', NULL, 0, 't', 't', '1', '2021-12-30 20:28:30', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1187, '流程表单', '', 2, 0, 1186, 'form', 'form', 'bpm/form/index', 0, 't', 't', '', '2021-12-30 12:38:22', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1188, '表单查询', 'bpm:form:query', 3, 1, 1187, '', '', '', 0, 't', 't', '', '2021-12-30 12:38:22', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1189, '表单创建', 'bpm:form:create', 3, 2, 1187, '', '', '', 0, 't', 't', '', '2021-12-30 12:38:22', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1190, '表单更新', 'bpm:form:update', 3, 3, 1187, '', '', '', 0, 't', 't', '', '2021-12-30 12:38:22', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1191, '表单删除', 'bpm:form:delete', 3, 4, 1187, '', '', '', 0, 't', 't', '', '2021-12-30 12:38:22', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1192, '表单导出', 'bpm:form:export', 3, 5, 1187, '', '', '', 0, 't', 't', '', '2021-12-30 12:38:22', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1193, '流程模型', '', 2, 5, 1186, 'model', 'guide', 'bpm/model/index', 0, 't', 't', '1', '2021-12-31 23:24:58',
        '103', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1194, '模型查询', 'bpm:model:query', 3, 1, 1193, '', '', '', 0, 't', 't', '1', '2022-01-03 19:01:10', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1195, '模型创建', 'bpm:model:create', 3, 2, 1193, '', '', '', 0, 't', 't', '1', '2022-01-03 19:01:24', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1196, '模型导入', 'bpm:model:import', 3, 3, 1193, '', '', '', 0, 't', 't', '1', '2022-01-03 19:01:35', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1197, '模型更新', 'bpm:model:update', 3, 4, 1193, '', '', '', 0, 't', 't', '1', '2022-01-03 19:02:28', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1198, '模型删除', 'bpm:model:delete', 3, 5, 1193, '', '', '', 0, 't', 't', '1', '2022-01-03 19:02:43', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1199, '模型发布', 'bpm:model:deploy', 3, 6, 1193, '', '', '', 0, 't', 't', '1', '2022-01-03 19:03:24', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1200, '任务管理', '', 1, 20, 1185, 'task', 'cascader', NULL, 0, 't', 't', '1', '2022-01-07 23:51:48', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1201, '我的流程', '', 2, 0, 1200, 'my', 'people', 'bpm/processInstance/index', 0, 't', 't', '',
        '2022-01-07 15:53:44', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1202, '流程实例的查询', 'bpm:process-instance:query', 3, 1, 1201, '', '', '', 0, 't', 't', '',
        '2022-01-07 15:53:44', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1207, '待办任务', '', 2, 10, 1200, 'todo', 'eye-open', 'bpm/task/todo', 0, 't', 't', '1', '2022-01-08 10:33:37',
        '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1208, '已办任务', '', 2, 20, 1200, 'done', 'eye', 'bpm/task/done', 0, 't', 't', '1', '2022-01-08 10:34:13', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1209, '用户分组', '', 2, 2, 1186, 'user-group', 'people', 'bpm/group/index', 0, 't', 't', '',
        '2022-01-14 02:14:20', '103', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1210, '用户组查询', 'bpm:user-group:query', 3, 1, 1209, '', '', '', 0, 't', 't', '', '2022-01-14 02:14:20', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1211, '用户组创建', 'bpm:user-group:create', 3, 2, 1209, '', '', '', 0, 't', 't', '', '2022-01-14 02:14:20', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1212, '用户组更新', 'bpm:user-group:update', 3, 3, 1209, '', '', '', 0, 't', 't', '', '2022-01-14 02:14:20', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1213, '用户组删除', 'bpm:user-group:delete', 3, 4, 1209, '', '', '', 0, 't', 't', '', '2022-01-14 02:14:20', '',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1215, '流程定义查询', 'bpm:process-definition:query', 3, 10, 1193, '', '', '', 0, 't', 't', '1',
        '2022-01-23 00:21:43', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1216, '流程任务分配规则查询', 'bpm:task-assign-rule:query', 3, 20, 1193, '', '', '', 0, 't', 't', '1',
        '2022-01-23 00:26:53', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1217, '流程任务分配规则创建', 'bpm:task-assign-rule:create', 3, 21, 1193, '', '', '', 0, 't', 't', '1',
        '2022-01-23 00:28:15', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1218, '流程任务分配规则更新', 'bpm:task-assign-rule:update', 3, 22, 1193, '', '', '', 0, 't', 't', '1',
        '2022-01-23 00:28:41', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1219, '流程实例的创建', 'bpm:process-instance:create', 3, 2, 1201, '', '', '', 0, 't', 't', '1',
        '2022-01-23 00:36:15', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1220, '流程实例的取消', 'bpm:process-instance:cancel', 3, 3, 1201, '', '', '', 0, 't', 't', '1',
        '2022-01-23 00:36:33', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1221, '流程任务的查询', 'bpm:task:query', 3, 1, 1207, '', '', '', 0, 't', 't', '1', '2022-01-23 00:38:52', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1222, '流程任务的更新', 'bpm:task:update', 3, 2, 1207, '', '', '', 0, 't', 't', '1', '2022-01-23 00:39:24', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1224, '租户管理', '', 2, 0, 1, 'tenant', 'peoples', NULL, 0, 't', 't', '1', '2022-02-20 01:41:13', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1225, '租户套餐', '', 2, 0, 1224, 'package', 'eye', 'system/tenantPackage/index', 0, 't', 't', '',
        '2022-02-19 17:44:06', '1', '2022-04-21 01:21:25', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1226, '租户套餐查询', 'system:tenant-package:query', 3, 1, 1225, '', '', '', 0, 't', 't', '',
        '2022-02-19 17:44:06', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1227, '租户套餐创建', 'system:tenant-package:create', 3, 2, 1225, '', '', '', 0, 't', 't', '',
        '2022-02-19 17:44:06', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1228, '租户套餐更新', 'system:tenant-package:update', 3, 3, 1225, '', '', '', 0, 't', 't', '',
        '2022-02-19 17:44:06', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1229, '租户套餐删除', 'system:tenant-package:delete', 3, 4, 1225, '', '', '', 0, 't', 't', '',
        '2022-02-19 17:44:06', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1237, '文件配置', '', 2, 0, 1243, 'file-config', 'config', 'infra/fileConfig/index', 0, 't', 't', '',
        '2022-03-15 14:35:28', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1238, '文件配置查询', 'infra:file-config:query', 3, 1, 1237, '', '', '', 0, 't', 't', '', '2022-03-15 14:35:28',
        '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1239, '文件配置创建', 'infra:file-config:create', 3, 2, 1237, '', '', '', 0, 't', 't', '',
        '2022-03-15 14:35:28', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1240, '文件配置更新', 'infra:file-config:update', 3, 3, 1237, '', '', '', 0, 't', 't', '',
        '2022-03-15 14:35:28', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1241, '文件配置删除', 'infra:file-config:delete', 3, 4, 1237, '', '', '', 0, 't', 't', '',
        '2022-03-15 14:35:28', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1242, '文件配置导出', 'infra:file-config:export', 3, 5, 1237, '', '', '', 0, 't', 't', '',
        '2022-03-15 14:35:28', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1243, '文件管理', '', 2, 5, 2, 'file', 'download', NULL, 0, 't', 't', '1', '2022-03-16 23:47:40', '1',
        '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1247, '敏感词管理', '', 2, 13, 1, 'sensitive-word', 'education', 'system/sensitiveWord/index', 0, 't', 't', '',
        '2022-04-07 16:55:03', '1', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1248, '敏感词查询', 'system:sensitive-word:query', 3, 1, 1247, '', '', '', 0, 't', 't', '',
        '2022-04-07 16:55:03', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1249, '敏感词创建', 'system:sensitive-word:create', 3, 2, 1247, '', '', '', 0, 't', 't', '',
        '2022-04-07 16:55:03', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1250, '敏感词更新', 'system:sensitive-word:update', 3, 3, 1247, '', '', '', 0, 't', 't', '',
        '2022-04-07 16:55:03', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1251, '敏感词删除', 'system:sensitive-word:delete', 3, 4, 1247, '', '', '', 0, 't', 't', '',
        '2022-04-07 16:55:03', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1252, '敏感词导出', 'system:sensitive-word:export', 3, 5, 1247, '', '', '', 0, 't', 't', '',
        '2022-04-07 16:55:03', '', '2022-04-20 17:03:10', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1254, '作者动态', '', 1, 0, 0, 'https://www.iocoder.cn', 'people', NULL, 0, 't', 't', '1',
        '2022-04-23 01:03:15', '1', '2022-04-23 01:03:15', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1255, '数据源配置', '', 2, 1, 2, 'data-source-config', 'rate', 'infra/dataSourceConfig/index', 0, 't', 't', '',
        '2022-04-27 14:37:32', '1', '2022-04-27 22:42:06', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1256, '数据源配置查询', 'infra:data-source-config:query', 3, 1, 1255, '', '', '', 0, 't', 't', '',
        '2022-04-27 14:37:32', '', '2022-04-27 14:37:32', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1257, '数据源配置创建', 'infra:data-source-config:create', 3, 2, 1255, '', '', '', 0, 't', 't', '',
        '2022-04-27 14:37:32', '', '2022-04-27 14:37:32', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1258, '数据源配置更新', 'infra:data-source-config:update', 3, 3, 1255, '', '', '', 0, 't', 't', '',
        '2022-04-27 14:37:32', '', '2022-04-27 14:37:32', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1259, '数据源配置删除', 'infra:data-source-config:delete', 3, 4, 1255, '', '', '', 0, 't', 't', '',
        '2022-04-27 14:37:32', '', '2022-04-27 14:37:32', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1260, '数据源配置导出', 'infra:data-source-config:export', 3, 5, 1255, '', '', '', 0, 't', 't', '',
        '2022-04-27 14:37:32', '', '2022-04-27 14:37:32', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1261, 'OAuth 2.0', '', 1, 10, 1, 'oauth2', 'people', NULL, 0, 't', 't', '1', '2022-05-09 23:38:17', '1',
        '2022-05-11 23:51:46', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1263, '应用管理', '', 2, 0, 1261, 'oauth2/application', 'tool', 'system/oauth2/client/index', 0, 't', 't', '',
        '2022-05-10 16:26:33', '1', '2022-05-11 23:31:36', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1264, '客户端查询', 'system:oauth2-client:query', 3, 1, 1263, '', '', '', 0, 't', 't', '',
        '2022-05-10 16:26:33', '1', '2022-05-11 00:31:06', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1265, '客户端创建', 'system:oauth2-client:create', 3, 2, 1263, '', '', '', 0, 't', 't', '',
        '2022-05-10 16:26:33', '1', '2022-05-11 00:31:23', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1266, '客户端更新', 'system:oauth2-client:update', 3, 3, 1263, '', '', '', 0, 't', 't', '',
        '2022-05-10 16:26:33', '1', '2022-05-11 00:31:28', 0, NULL, '1');
INSERT INTO "system_menu" ("id", "name", "permission", "type", "sort", "parent_id", "path", "icon", "component",
                           "status", "visible", "keep_alive", "creator", "create_time", "updater", "update_time",
                           "deleted", "component_name", "always_show")
VALUES (1267, '客户端删除', 'system:oauth2-client:delete', 3, 4, 1263, '', '', '', 0, 't', 't', '',
        '2022-05-10 16:26:33', '1', '2022-05-11 00:31:33', 0, NULL, '1');
COMMIT;

-- ----------------------------
-- Table structure for system_notice
-- ----------------------------
DROP TABLE IF EXISTS "system_notice";
CREATE TABLE "system_notice"
(
    "id"          int8                                       NOT NULL,
    "title"       varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
    "content"     text COLLATE "pg_catalog"."default"        NOT NULL,
    "type"        int2                                       NOT NULL,
    "status"      int2                                       NOT NULL,
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                               NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                               NOT NULL,
    "deleted"     int2                                       NOT NULL DEFAULT 0,
    "tenant_id"   int8                                       NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_notice"."id" IS '公告ID';
COMMENT
ON COLUMN "system_notice"."title" IS '公告标题';
COMMENT
ON COLUMN "system_notice"."content" IS '公告内容';
COMMENT
ON COLUMN "system_notice"."type" IS '公告类型（1通知 2公告）';
COMMENT
ON COLUMN "system_notice"."status" IS '公告状态（0正常 1关闭）';
COMMENT
ON COLUMN "system_notice"."creator" IS '创建者';
COMMENT
ON COLUMN "system_notice"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_notice"."updater" IS '更新者';
COMMENT
ON COLUMN "system_notice"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_notice"."deleted" IS '是否删除';
COMMENT
ON COLUMN "system_notice"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "system_notice" IS '通知公告表';

-- ----------------------------
-- Records of system_notice
-- ----------------------------
BEGIN;
INSERT INTO "system_notice" ("id", "title", "content", "type", "status", "creator", "create_time", "updater",
                             "update_time", "deleted", "tenant_id")
VALUES (1, '芋道的公众', '<p>新版本内容133</p>', 1, 0, 'admin', '2021-01-05 17:03:48', '1', '2022-05-04 21:00:20', 0,
        1);
INSERT INTO "system_notice" ("id", "title", "content", "type", "status", "creator", "create_time", "updater",
                             "update_time", "deleted", "tenant_id")
VALUES (2, '维护通知：2018-07-01 若依系统凌晨维护',
        '<p><img src="http://test.yudao.iocoder.cn/b7cb3cf49b4b3258bf7309a09dd2f4e5.jpg">维护内容</p>', 2, 1, 'admin',
        '2021-01-05 17:03:48', '1', '2022-05-11 12:34:24', 0, 1);
INSERT INTO "system_notice" ("id", "title", "content", "type", "status", "creator", "create_time", "updater",
                             "update_time", "deleted", "tenant_id")
VALUES (4, '我是测试标题', '<p>哈哈哈哈123</p>', 1, 0, '110', '2022-02-22 01:01:25', '110', '2022-02-22 01:01:46', 0,
        121);
COMMIT;

-- ----------------------------
-- Table structure for system_oauth2_access_token
-- ----------------------------
DROP TABLE IF EXISTS "system_oauth2_access_token";
CREATE TABLE "system_oauth2_access_token"
(
    "id"            int8                                        NOT NULL,
    "user_id"       int8                                        NOT NULL,
    "access_token"  varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "refresh_token" varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "user_type"     int2                                        NOT NULL,
    "client_id"     varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "expires_time"  timestamp(6)                                NOT NULL,
    "creator"       varchar(64) COLLATE "pg_catalog"."default",
    "create_time"   timestamp(6)                                NOT NULL,
    "updater"       varchar(64) COLLATE "pg_catalog"."default",
    "update_time"   timestamp(6)                                NOT NULL,
    "deleted"       int2                                        NOT NULL DEFAULT 0,
    "tenant_id"     int8                                        NOT NULL DEFAULT 0,
    "scopes"        varchar(255) COLLATE "pg_catalog"."default"          DEFAULT '':: character varying
)
;
COMMENT
ON COLUMN "system_oauth2_access_token"."id" IS '编号';
COMMENT
ON COLUMN "system_oauth2_access_token"."user_id" IS '用户编号';
COMMENT
ON COLUMN "system_oauth2_access_token"."access_token" IS '访问令牌';
COMMENT
ON COLUMN "system_oauth2_access_token"."refresh_token" IS '刷新令牌';
COMMENT
ON COLUMN "system_oauth2_access_token"."user_type" IS '用户类型';
COMMENT
ON COLUMN "system_oauth2_access_token"."client_id" IS '客户端编号';
COMMENT
ON COLUMN "system_oauth2_access_token"."expires_time" IS '过期时间';
COMMENT
ON COLUMN "system_oauth2_access_token"."creator" IS '创建者';
COMMENT
ON COLUMN "system_oauth2_access_token"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_oauth2_access_token"."updater" IS '更新者';
COMMENT
ON COLUMN "system_oauth2_access_token"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_oauth2_access_token"."deleted" IS '是否删除';
COMMENT
ON COLUMN "system_oauth2_access_token"."tenant_id" IS '租户编号';
COMMENT
ON COLUMN "system_oauth2_access_token"."scopes" IS '授权范围';
COMMENT
ON TABLE "system_oauth2_access_token" IS '刷新令牌';

-- ----------------------------
-- Records of system_oauth2_access_token
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for system_oauth2_approve
-- ----------------------------
DROP TABLE IF EXISTS "system_oauth2_approve";
CREATE TABLE "system_oauth2_approve"
(
    "id"           int8                                        NOT NULL,
    "user_id"      int8                                        NOT NULL,
    "user_type"    int2                                        NOT NULL,
    "client_id"    varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "scope"        varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "approved"     bool                                        NOT NULL,
    "expires_time" timestamp(6)                                NOT NULL,
    "creator"      varchar(64) COLLATE "pg_catalog"."default",
    "create_time"  timestamp(6)                                NOT NULL,
    "updater"      varchar(64) COLLATE "pg_catalog"."default",
    "update_time"  timestamp(6)                                NOT NULL,
    "deleted"      int2                                        NOT NULL DEFAULT 0,
    "tenant_id"    int8                                        NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_oauth2_approve"."id" IS '编号';
COMMENT
ON COLUMN "system_oauth2_approve"."user_id" IS '用户编号';
COMMENT
ON COLUMN "system_oauth2_approve"."user_type" IS '用户类型';
COMMENT
ON COLUMN "system_oauth2_approve"."client_id" IS '客户端编号';
COMMENT
ON COLUMN "system_oauth2_approve"."scope" IS '授权范围';
COMMENT
ON COLUMN "system_oauth2_approve"."approved" IS '是否接受';
COMMENT
ON COLUMN "system_oauth2_approve"."expires_time" IS '过期时间';
COMMENT
ON COLUMN "system_oauth2_approve"."creator" IS '创建者';
COMMENT
ON COLUMN "system_oauth2_approve"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_oauth2_approve"."updater" IS '更新者';
COMMENT
ON COLUMN "system_oauth2_approve"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_oauth2_approve"."deleted" IS '是否删除';
COMMENT
ON COLUMN "system_oauth2_approve"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "system_oauth2_approve" IS 'OAuth2 批准表';

-- ----------------------------
-- Records of system_oauth2_approve
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for system_oauth2_client
-- ----------------------------
DROP TABLE IF EXISTS "system_oauth2_client";
CREATE TABLE "system_oauth2_client"
(
    "id"                             int8                                        NOT NULL,
    "client_id"                      varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "secret"                         varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "name"                           varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "logo"                           varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "description"                    varchar(255) COLLATE "pg_catalog"."default",
    "status"                         int2                                        NOT NULL,
    "access_token_validity_seconds"  int4                                        NOT NULL,
    "refresh_token_validity_seconds" int4                                        NOT NULL,
    "redirect_uris"                  varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "authorized_grant_types"         varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "scopes"                         varchar(255) COLLATE "pg_catalog"."default",
    "authorities"                    varchar(255) COLLATE "pg_catalog"."default",
    "resource_ids"                   varchar(255) COLLATE "pg_catalog"."default",
    "additional_information"         varchar(4096) COLLATE "pg_catalog"."default",
    "creator"                        varchar(64) COLLATE "pg_catalog"."default",
    "create_time"                    timestamp(6)                                NOT NULL,
    "updater"                        varchar(64) COLLATE "pg_catalog"."default",
    "update_time"                    timestamp(6)                                NOT NULL,
    "deleted"                        int2                                        NOT NULL DEFAULT 0,
    "auto_approve_scopes"            varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT
ON COLUMN "system_oauth2_client"."id" IS '编号';
COMMENT
ON COLUMN "system_oauth2_client"."client_id" IS '客户端编号';
COMMENT
ON COLUMN "system_oauth2_client"."secret" IS '客户端密钥';
COMMENT
ON COLUMN "system_oauth2_client"."name" IS '应用名';
COMMENT
ON COLUMN "system_oauth2_client"."logo" IS '应用图标';
COMMENT
ON COLUMN "system_oauth2_client"."description" IS '应用描述';
COMMENT
ON COLUMN "system_oauth2_client"."status" IS '状态';
COMMENT
ON COLUMN "system_oauth2_client"."access_token_validity_seconds" IS '访问令牌的有效期';
COMMENT
ON COLUMN "system_oauth2_client"."refresh_token_validity_seconds" IS '刷新令牌的有效期';
COMMENT
ON COLUMN "system_oauth2_client"."redirect_uris" IS '可重定向的 URI 地址';
COMMENT
ON COLUMN "system_oauth2_client"."authorized_grant_types" IS '授权类型';
COMMENT
ON COLUMN "system_oauth2_client"."scopes" IS '授权范围';
COMMENT
ON COLUMN "system_oauth2_client"."authorities" IS '权限';
COMMENT
ON COLUMN "system_oauth2_client"."resource_ids" IS '资源';
COMMENT
ON COLUMN "system_oauth2_client"."additional_information" IS '附加信息';
COMMENT
ON COLUMN "system_oauth2_client"."creator" IS '创建者';
COMMENT
ON COLUMN "system_oauth2_client"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_oauth2_client"."updater" IS '更新者';
COMMENT
ON COLUMN "system_oauth2_client"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_oauth2_client"."deleted" IS '是否删除';
COMMENT
ON COLUMN "system_oauth2_client"."auto_approve_scopes" IS '自动通过的授权范围';
COMMENT
ON TABLE "system_oauth2_client" IS 'OAuth2 客户端表';

-- ----------------------------
-- Records of system_oauth2_client
-- ----------------------------
BEGIN;
INSERT INTO "system_oauth2_client" ("id", "client_id", "secret", "name", "logo", "description", "status",
                                    "access_token_validity_seconds", "refresh_token_validity_seconds", "redirect_uris",
                                    "authorized_grant_types", "scopes", "authorities", "resource_ids",
                                    "additional_information", "creator", "create_time", "updater", "update_time",
                                    "deleted", "auto_approve_scopes")
VALUES (1, 'default', 'admin123', '芋道源码', 'http://test.yudao.iocoder.cn/a5e2e244368878a366b516805a4aabf1.png',
        '我是描述', 0, 180, 8640, '["https://www.iocoder.cn","https://doc.iocoder.cn"]',
        '["password","authorization_code","implicit","refresh_token"]', '["user.read","user.write"]',
        '["system:user:query"]', '[]', '{}', '1', '2022-05-11 21:47:12', '1', '2022-05-12 01:00:20', 0, NULL);
INSERT INTO "system_oauth2_client" ("id", "client_id", "secret", "name", "logo", "description", "status",
                                    "access_token_validity_seconds", "refresh_token_validity_seconds", "redirect_uris",
                                    "authorized_grant_types", "scopes", "authorities", "resource_ids",
                                    "additional_information", "creator", "create_time", "updater", "update_time",
                                    "deleted", "auto_approve_scopes")
VALUES (40, 'test', 'test2', 'biubiu', 'http://test.yudao.iocoder.cn/277a899d573723f1fcdfb57340f00379.png', NULL, 0,
        1800, 43200, '["https://www.iocoder.cn"]', '["password","authorization_code","implicit"]', '[]', '[]', '[]',
        '{}', '1', '2022-05-12 00:28:20', '1', '2022-05-25 23:45:33.005', 0, '[]');
COMMIT;

-- ----------------------------
-- Table structure for system_oauth2_code
-- ----------------------------
DROP TABLE IF EXISTS "system_oauth2_code";
CREATE TABLE "system_oauth2_code"
(
    "id"           int8                                        NOT NULL,
    "user_id"      int8                                        NOT NULL,
    "user_type"    int2                                        NOT NULL,
    "code"         varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "client_id"    varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "scopes"       varchar(255) COLLATE "pg_catalog"."default",
    "expires_time" timestamp(6)                                NOT NULL,
    "redirect_uri" varchar(255) COLLATE "pg_catalog"."default",
    "state"        varchar(255) COLLATE "pg_catalog"."default",
    "creator"      varchar(64) COLLATE "pg_catalog"."default",
    "create_time"  timestamp(6)                                NOT NULL,
    "updater"      varchar(64) COLLATE "pg_catalog"."default",
    "update_time"  timestamp(6)                                NOT NULL,
    "deleted"      int2                                        NOT NULL DEFAULT 0,
    "tenant_id"    int8                                        NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_oauth2_code"."id" IS '编号';
COMMENT
ON COLUMN "system_oauth2_code"."user_id" IS '用户编号';
COMMENT
ON COLUMN "system_oauth2_code"."user_type" IS '用户类型';
COMMENT
ON COLUMN "system_oauth2_code"."code" IS '授权码';
COMMENT
ON COLUMN "system_oauth2_code"."client_id" IS '客户端编号';
COMMENT
ON COLUMN "system_oauth2_code"."scopes" IS '授权范围';
COMMENT
ON COLUMN "system_oauth2_code"."expires_time" IS '过期时间';
COMMENT
ON COLUMN "system_oauth2_code"."redirect_uri" IS '可重定向的 URI 地址';
COMMENT
ON COLUMN "system_oauth2_code"."state" IS '状态';
COMMENT
ON COLUMN "system_oauth2_code"."creator" IS '创建者';
COMMENT
ON COLUMN "system_oauth2_code"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_oauth2_code"."updater" IS '更新者';
COMMENT
ON COLUMN "system_oauth2_code"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_oauth2_code"."deleted" IS '是否删除';
COMMENT
ON COLUMN "system_oauth2_code"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "system_oauth2_code" IS 'OAuth2 授权码表';

-- ----------------------------
-- Records of system_oauth2_code
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for system_oauth2_refresh_token
-- ----------------------------
DROP TABLE IF EXISTS "system_oauth2_refresh_token";
CREATE TABLE "system_oauth2_refresh_token"
(
    "id"            int8                                        NOT NULL,
    "user_id"       int8                                        NOT NULL,
    "refresh_token" varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "user_type"     int2                                        NOT NULL,
    "client_id"     varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "expires_time"  timestamp(6)                                NOT NULL,
    "creator"       varchar(64) COLLATE "pg_catalog"."default",
    "create_time"   timestamp(6)                                NOT NULL,
    "updater"       varchar(64) COLLATE "pg_catalog"."default",
    "update_time"   timestamp(6)                                NOT NULL,
    "deleted"       int2                                        NOT NULL DEFAULT 0,
    "tenant_id"     int8                                        NOT NULL DEFAULT 0,
    "scopes"        varchar(255) COLLATE "pg_catalog"."default"          DEFAULT '':: character varying
)
;
COMMENT
ON COLUMN "system_oauth2_refresh_token"."id" IS '编号';
COMMENT
ON COLUMN "system_oauth2_refresh_token"."user_id" IS '用户编号';
COMMENT
ON COLUMN "system_oauth2_refresh_token"."refresh_token" IS '刷新令牌';
COMMENT
ON COLUMN "system_oauth2_refresh_token"."user_type" IS '用户类型';
COMMENT
ON COLUMN "system_oauth2_refresh_token"."client_id" IS '客户端编号';
COMMENT
ON COLUMN "system_oauth2_refresh_token"."expires_time" IS '过期时间';
COMMENT
ON COLUMN "system_oauth2_refresh_token"."creator" IS '创建者';
COMMENT
ON COLUMN "system_oauth2_refresh_token"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_oauth2_refresh_token"."updater" IS '更新者';
COMMENT
ON COLUMN "system_oauth2_refresh_token"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_oauth2_refresh_token"."deleted" IS '是否删除';
COMMENT
ON COLUMN "system_oauth2_refresh_token"."tenant_id" IS '租户编号';
COMMENT
ON COLUMN "system_oauth2_refresh_token"."scopes" IS '授权范围';
COMMENT
ON TABLE "system_oauth2_refresh_token" IS '刷新令牌';

-- ----------------------------
-- Records of system_oauth2_refresh_token
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for system_operate_log
-- ----------------------------
DROP TABLE IF EXISTS "system_operate_log";
CREATE TABLE "system_operate_log"
(
    "id"               int8                                         NOT NULL,
    "trace_id"         varchar(64) COLLATE "pg_catalog"."default"   NOT NULL,
    "user_id"          int8                                         NOT NULL,
    "user_type"        int2                                         NOT NULL,
    "module"           varchar(50) COLLATE "pg_catalog"."default"   NOT NULL,
    "name"             varchar(50) COLLATE "pg_catalog"."default"   NOT NULL,
    "type"             int8                                         NOT NULL,
    "content"          varchar(2000) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '':: character varying,
    "exts"             varchar(512) COLLATE "pg_catalog"."default"  NOT NULL DEFAULT '':: character varying,
    "request_method"   varchar(16) COLLATE "pg_catalog"."default",
    "request_url"      varchar(255) COLLATE "pg_catalog"."default",
    "user_ip"          varchar(50) COLLATE "pg_catalog"."default",
    "user_agent"       varchar(200) COLLATE "pg_catalog"."default",
    "java_method"      varchar(512) COLLATE "pg_catalog"."default"  NOT NULL,
    "java_method_args" varchar(8000) COLLATE "pg_catalog"."default",
    "start_time"       timestamp(6)                                 NOT NULL,
    "duration"         int4                                         NOT NULL,
    "result_code"      int4                                         NOT NULL,
    "result_msg"       varchar(512) COLLATE "pg_catalog"."default",
    "result_data"      varchar(4000) COLLATE "pg_catalog"."default",
    "creator"          varchar(64) COLLATE "pg_catalog"."default",
    "create_time"      timestamp(6)                                 NOT NULL,
    "updater"          varchar(64) COLLATE "pg_catalog"."default",
    "update_time"      timestamp(6)                                 NOT NULL,
    "deleted"          int2                                         NOT NULL DEFAULT 0,
    "tenant_id"        int8                                         NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_operate_log"."id" IS '日志主键';
COMMENT
ON COLUMN "system_operate_log"."trace_id" IS '链路追踪编号';
COMMENT
ON COLUMN "system_operate_log"."user_id" IS '用户编号';
COMMENT
ON COLUMN "system_operate_log"."user_type" IS '用户类型';
COMMENT
ON COLUMN "system_operate_log"."module" IS '模块标题';
COMMENT
ON COLUMN "system_operate_log"."name" IS '操作名';
COMMENT
ON COLUMN "system_operate_log"."type" IS '操作分类';
COMMENT
ON COLUMN "system_operate_log"."content" IS '操作内容';
COMMENT
ON COLUMN "system_operate_log"."exts" IS '拓展字段';
COMMENT
ON COLUMN "system_operate_log"."request_method" IS '请求方法名';
COMMENT
ON COLUMN "system_operate_log"."request_url" IS '请求地址';
COMMENT
ON COLUMN "system_operate_log"."user_ip" IS '用户 IP';
COMMENT
ON COLUMN "system_operate_log"."user_agent" IS '浏览器 UA';
COMMENT
ON COLUMN "system_operate_log"."java_method" IS 'Java 方法名';
COMMENT
ON COLUMN "system_operate_log"."java_method_args" IS 'Java 方法的参数';
COMMENT
ON COLUMN "system_operate_log"."start_time" IS '操作时间';
COMMENT
ON COLUMN "system_operate_log"."duration" IS '执行时长';
COMMENT
ON COLUMN "system_operate_log"."result_code" IS '结果码';
COMMENT
ON COLUMN "system_operate_log"."result_msg" IS '结果提示';
COMMENT
ON COLUMN "system_operate_log"."result_data" IS '结果数据';
COMMENT
ON COLUMN "system_operate_log"."creator" IS '创建者';
COMMENT
ON COLUMN "system_operate_log"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_operate_log"."updater" IS '更新者';
COMMENT
ON COLUMN "system_operate_log"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_operate_log"."deleted" IS '是否删除';
COMMENT
ON COLUMN "system_operate_log"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "system_operate_log" IS '操作日志记录';

-- ----------------------------
-- Records of system_operate_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for system_post
-- ----------------------------
DROP TABLE IF EXISTS "system_post";
CREATE TABLE "system_post"
(
    "id"          int8                                       NOT NULL DEFAULT 0,
    "code"        varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "name"        varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
    "sort"        int4                                       NOT NULL,
    "status"      int2                                       NOT NULL,
    "remark"      varchar(500) COLLATE "pg_catalog"."default",
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                               NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                               NOT NULL,
    "deleted"     int2                                       NOT NULL DEFAULT 0,
    "tenant_id"   int8                                       NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_post"."id" IS '岗位ID';
COMMENT
ON COLUMN "system_post"."code" IS '岗位编码';
COMMENT
ON COLUMN "system_post"."name" IS '岗位名称';
COMMENT
ON COLUMN "system_post"."sort" IS '显示顺序';
COMMENT
ON COLUMN "system_post"."status" IS '状态（0正常 1停用）';
COMMENT
ON COLUMN "system_post"."remark" IS '备注';
COMMENT
ON COLUMN "system_post"."creator" IS '创建者';
COMMENT
ON COLUMN "system_post"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_post"."updater" IS '更新者';
COMMENT
ON COLUMN "system_post"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_post"."deleted" IS '是否删除';
COMMENT
ON COLUMN "system_post"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "system_post" IS '岗位信息表';

-- ----------------------------
-- Records of system_post
-- ----------------------------
BEGIN;
INSERT INTO "system_post" ("id", "code", "name", "sort", "status", "remark", "creator", "create_time", "updater",
                           "update_time", "deleted", "tenant_id")
VALUES (1, 'ceo', '董事长', 1, 0, '', 'admin', '2021-01-06 17:03:48', '1', '2022-04-19 16:53:39', 0, 1);
INSERT INTO "system_post" ("id", "code", "name", "sort", "status", "remark", "creator", "create_time", "updater",
                           "update_time", "deleted", "tenant_id")
VALUES (2, 'se', '项目经理', 2, 0, '', 'admin', '2021-01-05 17:03:48', '1', '2021-12-12 10:47:47', 0, 1);
INSERT INTO "system_post" ("id", "code", "name", "sort", "status", "remark", "creator", "create_time", "updater",
                           "update_time", "deleted", "tenant_id")
VALUES (4, 'user', '普通员工', 4, 0, '111', 'admin', '2021-01-05 17:03:48', '1', '2022-05-04 22:46:35', 0, 1);
COMMIT;

-- ----------------------------
-- Table structure for system_role
-- ----------------------------
DROP TABLE IF EXISTS "system_role";
CREATE TABLE "system_role"
(
    "id"                  int8                                        NOT NULL,
    "name"                varchar(30) COLLATE "pg_catalog"."default"  NOT NULL,
    "code"                varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
    "sort"                int4                                        NOT NULL,
    "data_scope"          int2                                        NOT NULL,
    "data_scope_dept_ids" varchar(500) COLLATE "pg_catalog"."default" NOT NULL DEFAULT '',
    "status"              int2                                        NOT NULL,
    "type"                int2                                        NOT NULL,
    "remark"              varchar(500) COLLATE "pg_catalog"."default",
    "creator"             varchar(64) COLLATE "pg_catalog"."default",
    "create_time"         timestamp(6)                                NOT NULL,
    "updater"             varchar(64) COLLATE "pg_catalog"."default",
    "update_time"         timestamp(6)                                NOT NULL,
    "deleted"             int2                                        NOT NULL DEFAULT 0,
    "tenant_id"           int8                                        NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_role"."id" IS '角色ID';
COMMENT
ON COLUMN "system_role"."name" IS '角色名称';
COMMENT
ON COLUMN "system_role"."code" IS '角色权限字符串';
COMMENT
ON COLUMN "system_role"."sort" IS '显示顺序';
COMMENT
ON COLUMN "system_role"."data_scope" IS '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）';
COMMENT
ON COLUMN "system_role"."data_scope_dept_ids" IS '数据范围(指定部门数组)';
COMMENT
ON COLUMN "system_role"."status" IS '角色状态（0正常 1停用）';
COMMENT
ON COLUMN "system_role"."type" IS '角色类型';
COMMENT
ON COLUMN "system_role"."remark" IS '备注';
COMMENT
ON COLUMN "system_role"."creator" IS '创建者';
COMMENT
ON COLUMN "system_role"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_role"."updater" IS '更新者';
COMMENT
ON COLUMN "system_role"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_role"."deleted" IS '是否删除';
COMMENT
ON COLUMN "system_role"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "system_role" IS '角色信息表';

-- ----------------------------
-- Records of system_role
-- ----------------------------
BEGIN;
INSERT INTO "system_role" ("id", "name", "code", "sort", "data_scope", "data_scope_dept_ids", "status", "type",
                           "remark", "creator", "create_time", "updater", "update_time", "deleted", "tenant_id")
VALUES (1, '超级管理员', 'super_admin', 1, 1, '', 0, 1, '超级管理员', 'admin', '2021-01-05 17:03:48', '',
        '2022-02-22 05:08:21', 0, 1);
INSERT INTO "system_role" ("id", "name", "code", "sort", "data_scope", "data_scope_dept_ids", "status", "type",
                           "remark", "creator", "create_time", "updater", "update_time", "deleted", "tenant_id")
VALUES (2, '普通角色', 'common', 2, 2, '', 0, 1, '普通角色', 'admin', '2021-01-05 17:03:48', '', '2022-02-22 05:08:20',
        0, 1);
INSERT INTO "system_role" ("id", "name", "code", "sort", "data_scope", "data_scope_dept_ids", "status", "type",
                           "remark", "creator", "create_time", "updater", "update_time", "deleted", "tenant_id")
VALUES (101, '测试账号', 'test', 0, 1, '[]', 0, 2, '132', '', '2021-01-06 13:49:35', '1', '2022-04-01 21:37:13', 0, 1);
INSERT INTO "system_role" ("id", "name", "code", "sort", "data_scope", "data_scope_dept_ids", "status", "type",
                           "remark", "creator", "create_time", "updater", "update_time", "deleted", "tenant_id")
VALUES (109, '租户管理员', 'tenant_admin', 0, 1, '', 0, 1, '系统自动生成', '1', '2022-02-22 00:56:14', '1',
        '2022-02-22 00:56:14', 0, 121);
INSERT INTO "system_role" ("id", "name", "code", "sort", "data_scope", "data_scope_dept_ids", "status", "type",
                           "remark", "creator", "create_time", "updater", "update_time", "deleted", "tenant_id")
VALUES (110, '测试角色', 'test', 0, 1, '[]', 0, 2, '嘿嘿', '110', '2022-02-23 00:14:34', '110', '2022-02-23 13:14:58',
        0, 121);
INSERT INTO "system_role" ("id", "name", "code", "sort", "data_scope", "data_scope_dept_ids", "status", "type",
                           "remark", "creator", "create_time", "updater", "update_time", "deleted", "tenant_id")
VALUES (111, '租户管理员', 'tenant_admin', 0, 1, '', 0, 1, '系统自动生成', '1', '2022-03-07 21:37:58', '1',
        '2022-03-07 21:37:58', 0, 122);
COMMIT;

-- ----------------------------
-- Table structure for system_role_menu
-- ----------------------------
DROP TABLE IF EXISTS "system_role_menu";
CREATE TABLE "system_role_menu"
(
    "id"          int8         NOT NULL,
    "role_id"     int8         NOT NULL,
    "menu_id"     int8         NOT NULL,
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6) NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6) NOT NULL,
    "deleted"     int2         NOT NULL DEFAULT 0,
    "tenant_id"   int8         NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_role_menu"."id" IS '自增编号';
COMMENT
ON COLUMN "system_role_menu"."role_id" IS '角色ID';
COMMENT
ON COLUMN "system_role_menu"."menu_id" IS '菜单ID';
COMMENT
ON COLUMN "system_role_menu"."creator" IS '创建者';
COMMENT
ON COLUMN "system_role_menu"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_role_menu"."updater" IS '更新者';
COMMENT
ON COLUMN "system_role_menu"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_role_menu"."deleted" IS '是否删除';
COMMENT
ON COLUMN "system_role_menu"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "system_role_menu" IS '角色和菜单关联表';

-- ----------------------------
-- Records of system_role_menu
-- ----------------------------
BEGIN;
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (263, 109, 1, '1', '2022-02-22 00:56:14', '1', '2022-02-22 00:56:14', 0, 121);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (434, 2, 1, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (454, 2, 1093, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (455, 2, 1094, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (460, 2, 1100, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (467, 2, 1107, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (470, 2, 1110, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (476, 2, 1117, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (477, 2, 100, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (478, 2, 101, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (479, 2, 102, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (480, 2, 1126, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (481, 2, 103, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (483, 2, 104, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (485, 2, 105, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (488, 2, 107, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (490, 2, 108, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (492, 2, 109, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (498, 2, 1138, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (523, 2, 1224, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (524, 2, 1225, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (541, 2, 500, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (543, 2, 501, '1', '2022-02-22 13:09:12', '1', '2022-02-22 13:09:12', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (675, 2, 2, '1', '2022-02-22 13:16:57', '1', '2022-02-22 13:16:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (689, 2, 1077, '1', '2022-02-22 13:16:57', '1', '2022-02-22 13:16:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (690, 2, 1078, '1', '2022-02-22 13:16:57', '1', '2022-02-22 13:16:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (692, 2, 1083, '1', '2022-02-22 13:16:57', '1', '2022-02-22 13:16:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (693, 2, 1084, '1', '2022-02-22 13:16:57', '1', '2022-02-22 13:16:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (699, 2, 1090, '1', '2022-02-22 13:16:57', '1', '2022-02-22 13:16:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (703, 2, 106, '1', '2022-02-22 13:16:57', '1', '2022-02-22 13:16:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (704, 2, 110, '1', '2022-02-22 13:16:57', '1', '2022-02-22 13:16:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (705, 2, 111, '1', '2022-02-22 13:16:57', '1', '2022-02-22 13:16:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (706, 2, 112, '1', '2022-02-22 13:16:57', '1', '2022-02-22 13:16:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (707, 2, 113, '1', '2022-02-22 13:16:57', '1', '2022-02-22 13:16:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1296, 110, 1, '110', '2022-02-23 00:23:55', '110', '2022-02-23 00:23:55', 0, 121);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1486, 109, 103, '1', '2022-02-23 19:32:14', '1', '2022-02-23 19:32:14', 0, 121);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1487, 109, 104, '1', '2022-02-23 19:32:14', '1', '2022-02-23 19:32:14', 0, 121);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1489, 1, 1, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1490, 1, 2, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1494, 1, 1077, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1495, 1, 1078, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1496, 1, 1083, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1497, 1, 1084, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1498, 1, 1090, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1499, 1, 1093, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1500, 1, 1094, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1501, 1, 1100, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1502, 1, 1107, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1503, 1, 1110, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1505, 1, 1117, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1506, 1, 100, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1507, 1, 101, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1508, 1, 102, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1509, 1, 1126, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1510, 1, 103, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1511, 1, 104, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1512, 1, 105, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1513, 1, 106, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1514, 1, 107, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1515, 1, 108, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1516, 1, 109, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1517, 1, 110, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1518, 1, 111, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1519, 1, 112, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1520, 1, 113, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1522, 1, 1138, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1525, 1, 1224, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1526, 1, 1225, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1527, 1, 500, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1528, 1, 501, '1', '2022-02-23 20:03:57', '1', '2022-02-23 20:03:57', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1529, 109, 1024, '1', '2022-02-23 20:30:14', '1', '2022-02-23 20:30:14', 0, 121);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1530, 109, 1025, '1', '2022-02-23 20:30:14', '1', '2022-02-23 20:30:14', 0, 121);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1536, 109, 1017, '1', '2022-02-23 20:30:14', '1', '2022-02-23 20:30:14', 0, 121);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1537, 109, 1018, '1', '2022-02-23 20:30:14', '1', '2022-02-23 20:30:14', 0, 121);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1538, 109, 1019, '1', '2022-02-23 20:30:14', '1', '2022-02-23 20:30:14', 0, 121);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1539, 109, 1020, '1', '2022-02-23 20:30:14', '1', '2022-02-23 20:30:14', 0, 121);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1540, 109, 1021, '1', '2022-02-23 20:30:14', '1', '2022-02-23 20:30:14', 0, 121);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1541, 109, 1022, '1', '2022-02-23 20:30:14', '1', '2022-02-23 20:30:14', 0, 121);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1542, 109, 1023, '1', '2022-02-23 20:30:14', '1', '2022-02-23 20:30:14', 0, 121);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1576, 111, 1024, '1', '2022-03-07 21:37:58', '1', '2022-03-07 21:37:58', 0, 122);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1577, 111, 1025, '1', '2022-03-07 21:37:58', '1', '2022-03-07 21:37:58', 0, 122);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1578, 111, 1, '1', '2022-03-07 21:37:58', '1', '2022-03-07 21:37:58', 0, 122);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1584, 111, 103, '1', '2022-03-07 21:37:58', '1', '2022-03-07 21:37:58', 0, 122);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1585, 111, 104, '1', '2022-03-07 21:37:58', '1', '2022-03-07 21:37:58', 0, 122);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1587, 111, 1017, '1', '2022-03-07 21:37:58', '1', '2022-03-07 21:37:58', 0, 122);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1588, 111, 1018, '1', '2022-03-07 21:37:58', '1', '2022-03-07 21:37:58', 0, 122);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1589, 111, 1019, '1', '2022-03-07 21:37:58', '1', '2022-03-07 21:37:58', 0, 122);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1590, 111, 1020, '1', '2022-03-07 21:37:58', '1', '2022-03-07 21:37:58', 0, 122);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1591, 111, 1021, '1', '2022-03-07 21:37:58', '1', '2022-03-07 21:37:58', 0, 122);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1592, 111, 1022, '1', '2022-03-07 21:37:58', '1', '2022-03-07 21:37:58', 0, 122);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1593, 111, 1023, '1', '2022-03-07 21:37:58', '1', '2022-03-07 21:37:58', 0, 122);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1594, 109, 102, '1', '2022-03-19 18:39:13', '1', '2022-03-19 18:39:13', 0, 121);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1595, 109, 1013, '1', '2022-03-19 18:39:13', '1', '2022-03-19 18:39:13', 0, 121);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1596, 109, 1014, '1', '2022-03-19 18:39:13', '1', '2022-03-19 18:39:13', 0, 121);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1597, 109, 1015, '1', '2022-03-19 18:39:13', '1', '2022-03-19 18:39:13', 0, 121);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1598, 109, 1016, '1', '2022-03-19 18:39:13', '1', '2022-03-19 18:39:13', 0, 121);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1599, 111, 102, '1', '2022-03-19 18:39:13', '1', '2022-03-19 18:39:13', 0, 122);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1600, 111, 1013, '1', '2022-03-19 18:39:13', '1', '2022-03-19 18:39:13', 0, 122);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1601, 111, 1014, '1', '2022-03-19 18:39:13', '1', '2022-03-19 18:39:13', 0, 122);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1602, 111, 1015, '1', '2022-03-19 18:39:13', '1', '2022-03-19 18:39:13', 0, 122);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1603, 111, 1016, '1', '2022-03-19 18:39:13', '1', '2022-03-19 18:39:13', 0, 122);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1604, 101, 1216, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1605, 101, 1217, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1606, 101, 1218, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1607, 101, 1219, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1608, 101, 1220, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1609, 101, 1221, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1610, 101, 5, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1611, 101, 1222, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1612, 101, 1118, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1613, 101, 1119, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1614, 101, 1120, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1615, 101, 1185, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1616, 101, 1186, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1617, 101, 1187, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1618, 101, 1188, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1619, 101, 1189, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1620, 101, 1190, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1621, 101, 1191, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1622, 101, 1192, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1623, 101, 1193, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1624, 101, 1194, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1625, 101, 1195, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1626, 101, 1196, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1627, 101, 1197, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1628, 101, 1198, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1629, 101, 1199, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1630, 101, 1200, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1631, 101, 1201, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1632, 101, 1202, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1633, 101, 1207, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1634, 101, 1208, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1635, 101, 1209, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1636, 101, 1210, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1637, 101, 1211, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1638, 101, 1212, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1639, 101, 1213, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1640, 101, 1215, '1', '2022-03-19 21:45:52', '1', '2022-03-19 21:45:52', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1641, 101, 2, '1', '2022-04-01 22:21:24', '1', '2022-04-01 22:21:24', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1642, 101, 1031, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1643, 101, 1032, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1644, 101, 1033, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1645, 101, 1034, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1646, 101, 1035, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1647, 101, 1050, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1648, 101, 1051, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1649, 101, 1052, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1650, 101, 1053, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1651, 101, 1054, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1652, 101, 1056, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1653, 101, 1057, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1654, 101, 1058, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1655, 101, 1059, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1656, 101, 1060, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1657, 101, 1066, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1658, 101, 1067, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1659, 101, 1070, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1660, 101, 1071, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1661, 101, 1072, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1662, 101, 1073, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1663, 101, 1074, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1664, 101, 1075, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1665, 101, 1076, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1666, 101, 1077, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1667, 101, 1078, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1668, 101, 1082, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1669, 101, 1083, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1670, 101, 1084, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1671, 101, 1085, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1672, 101, 1086, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1673, 101, 1087, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1674, 101, 1088, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1675, 101, 1089, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1676, 101, 1090, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1677, 101, 1091, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1678, 101, 1092, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1679, 101, 1237, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1680, 101, 1238, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1681, 101, 1239, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1682, 101, 1240, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1683, 101, 1241, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1684, 101, 1242, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1685, 101, 1243, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1687, 101, 106, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1688, 101, 110, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1689, 101, 111, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1690, 101, 112, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1691, 101, 113, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1692, 101, 114, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1693, 101, 115, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
INSERT INTO "system_role_menu" ("id", "role_id", "menu_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1694, 101, 116, '1', '2022-04-01 22:21:37', '1', '2022-04-01 22:21:37', 0, 1);
COMMIT;

-- ----------------------------
-- Table structure for system_sensitive_word
-- ----------------------------
DROP TABLE IF EXISTS "system_sensitive_word";
CREATE TABLE "system_sensitive_word"
(
    "id"          int8                                        NOT NULL,
    "name"        varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "description" varchar(512) COLLATE "pg_catalog"."default",
    "tags"        varchar(255) COLLATE "pg_catalog"."default",
    "status"      int2                                        NOT NULL,
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                                NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                                NOT NULL,
    "deleted"     int2                                        NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_sensitive_word"."id" IS '编号';
COMMENT
ON COLUMN "system_sensitive_word"."name" IS '敏感词';
COMMENT
ON COLUMN "system_sensitive_word"."description" IS '描述';
COMMENT
ON COLUMN "system_sensitive_word"."tags" IS '标签数组';
COMMENT
ON COLUMN "system_sensitive_word"."status" IS '状态';
COMMENT
ON COLUMN "system_sensitive_word"."creator" IS '创建者';
COMMENT
ON COLUMN "system_sensitive_word"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_sensitive_word"."updater" IS '更新者';
COMMENT
ON COLUMN "system_sensitive_word"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_sensitive_word"."deleted" IS '是否删除';
COMMENT
ON TABLE "system_sensitive_word" IS '敏感词';

-- ----------------------------
-- Records of system_sensitive_word
-- ----------------------------
BEGIN;
INSERT INTO "system_sensitive_word" ("id", "name", "description", "tags", "status", "creator", "create_time", "updater",
                                     "update_time", "deleted")
VALUES (3, '土豆', '好呀', '蔬菜,短信', 0, '1', '2022-04-08 21:07:12', '1', '2022-04-09 10:28:14', 0);
INSERT INTO "system_sensitive_word" ("id", "name", "description", "tags", "status", "creator", "create_time", "updater",
                                     "update_time", "deleted")
VALUES (4, 'XXX', NULL, '短信', 0, '1', '2022-04-08 21:27:49', '1', '2022-04-08 21:27:49', 0);
COMMIT;

-- ----------------------------
-- Table structure for system_sms_channel
-- ----------------------------
DROP TABLE IF EXISTS "system_sms_channel";
CREATE TABLE "system_sms_channel"
(
    "id"           int8                                        NOT NULL,
    "signature"    varchar(12) COLLATE "pg_catalog"."default"  NOT NULL,
    "code"         varchar(63) COLLATE "pg_catalog"."default"  NOT NULL,
    "status"       int2                                        NOT NULL,
    "remark"       varchar(255) COLLATE "pg_catalog"."default",
    "api_key"      varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
    "api_secret"   varchar(128) COLLATE "pg_catalog"."default",
    "callback_url" varchar(255) COLLATE "pg_catalog"."default",
    "creator"      varchar(64) COLLATE "pg_catalog"."default",
    "create_time"  timestamp(6)                                NOT NULL,
    "updater"      varchar(64) COLLATE "pg_catalog"."default",
    "update_time"  timestamp(6)                                NOT NULL,
    "deleted"      int2                                        NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_sms_channel"."id" IS '编号';
COMMENT
ON COLUMN "system_sms_channel"."signature" IS '短信签名';
COMMENT
ON COLUMN "system_sms_channel"."code" IS '渠道编码';
COMMENT
ON COLUMN "system_sms_channel"."status" IS '开启状态';
COMMENT
ON COLUMN "system_sms_channel"."remark" IS '备注';
COMMENT
ON COLUMN "system_sms_channel"."api_key" IS '短信 API 的账号';
COMMENT
ON COLUMN "system_sms_channel"."api_secret" IS '短信 API 的秘钥';
COMMENT
ON COLUMN "system_sms_channel"."callback_url" IS '短信发送回调 URL';
COMMENT
ON COLUMN "system_sms_channel"."creator" IS '创建者';
COMMENT
ON COLUMN "system_sms_channel"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_sms_channel"."updater" IS '更新者';
COMMENT
ON COLUMN "system_sms_channel"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_sms_channel"."deleted" IS '是否删除';
COMMENT
ON TABLE "system_sms_channel" IS '短信渠道';

-- ----------------------------
-- Records of system_sms_channel
-- ----------------------------
BEGIN;
INSERT INTO "system_sms_channel" ("id", "signature", "code", "status", "remark", "api_key", "api_secret",
                                  "callback_url", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (2, 'Ballcat', 'ALIYUN', 0, '啦啦啦', 'LTAI5tCnKso2uG3kJ5gRav88', 'fGJ5SNXL7P1NHNRmJ7DJaMJGPyE55C', NULL, '',
        '2021-03-31 11:53:10', '1', '2021-04-14 00:08:37', 0);
INSERT INTO "system_sms_channel" ("id", "signature", "code", "status", "remark", "api_key", "api_secret",
                                  "callback_url", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (4, '测试渠道', 'DEBUG_DING_TALK', 0, '123', '696b5d8ead48071237e4aa5861ff08dbadb2b4ded1c688a7b7c9afc615579859',
        'SEC5c4e5ff888bc8a9923ae47f59e7ccd30af1f14d93c55b4e2c9cb094e35aeed67', NULL, '1', '2021-04-13 00:23:14', '1',
        '2022-03-27 20:29:49', 0);
INSERT INTO "system_sms_channel" ("id", "signature", "code", "status", "remark", "api_key", "api_secret",
                                  "callback_url", "creator", "create_time", "updater", "update_time", "deleted")
VALUES (6, '测试演示', 'DEBUG_DING_TALK', 0, NULL, '696b5d8ead48071237e4aa5861ff08dbadb2b4ded1c688a7b7c9afc615579859',
        'SEC5c4e5ff888bc8a9923ae47f59e7ccd30af1f14d93c55b4e2c9cb094e35aeed67', NULL, '1', '2022-04-10 23:07:59', '1',
        '2022-04-10 23:07:59', 0);
COMMIT;

-- ----------------------------
-- Table structure for system_sms_code
-- ----------------------------
DROP TABLE IF EXISTS "system_sms_code";
CREATE TABLE "system_sms_code"
(
    "id"          int8                                       NOT NULL,
    "mobile"      varchar(11) COLLATE "pg_catalog"."default" NOT NULL,
    "code"        varchar(6) COLLATE "pg_catalog"."default"  NOT NULL,
    "create_ip"   varchar(15) COLLATE "pg_catalog"."default" NOT NULL,
    "scene"       int2                                       NOT NULL,
    "today_index" int2                                       NOT NULL,
    "used"        bool                                       NOT NULL,
    "used_time"   timestamp(6),
    "used_ip"     varchar(255) COLLATE "pg_catalog"."default",
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                               NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                               NOT NULL,
    "deleted"     int2                                       NOT NULL DEFAULT 0,
    "tenant_id"   int8                                       NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_sms_code"."id" IS '编号';
COMMENT
ON COLUMN "system_sms_code"."mobile" IS '手机号';
COMMENT
ON COLUMN "system_sms_code"."code" IS '验证码';
COMMENT
ON COLUMN "system_sms_code"."create_ip" IS '创建 IP';
COMMENT
ON COLUMN "system_sms_code"."scene" IS '发送场景';
COMMENT
ON COLUMN "system_sms_code"."today_index" IS '今日发送的第几条';
COMMENT
ON COLUMN "system_sms_code"."used" IS '是否使用';
COMMENT
ON COLUMN "system_sms_code"."used_time" IS '使用时间';
COMMENT
ON COLUMN "system_sms_code"."used_ip" IS '使用 IP';
COMMENT
ON COLUMN "system_sms_code"."creator" IS '创建者';
COMMENT
ON COLUMN "system_sms_code"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_sms_code"."updater" IS '更新者';
COMMENT
ON COLUMN "system_sms_code"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_sms_code"."deleted" IS '是否删除';
COMMENT
ON COLUMN "system_sms_code"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "system_sms_code" IS '手机验证码';

-- ----------------------------
-- Records of system_sms_code
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for system_sms_log
-- ----------------------------
DROP TABLE IF EXISTS "system_sms_log";
CREATE TABLE "system_sms_log"
(
    "id"               int8                                        NOT NULL,
    "channel_id"       int8                                        NOT NULL,
    "channel_code"     varchar(63) COLLATE "pg_catalog"."default"  NOT NULL,
    "template_id"      int8                                        NOT NULL,
    "template_code"    varchar(63) COLLATE "pg_catalog"."default"  NOT NULL,
    "template_type"    int2                                        NOT NULL,
    "template_content" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "template_params"  varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "api_template_id"  varchar(63) COLLATE "pg_catalog"."default"  NOT NULL,
    "mobile"           varchar(11) COLLATE "pg_catalog"."default"  NOT NULL,
    "user_id"          int8,
    "user_type"        int2,
    "send_status"      int2                                        NOT NULL,
    "send_time"        timestamp(6),
    "send_code"        int4,
    "send_msg"         varchar(255) COLLATE "pg_catalog"."default",
    "api_send_code"    varchar(63) COLLATE "pg_catalog"."default",
    "api_send_msg"     varchar(255) COLLATE "pg_catalog"."default",
    "api_request_id"   varchar(255) COLLATE "pg_catalog"."default",
    "api_serial_no"    varchar(255) COLLATE "pg_catalog"."default",
    "receive_status"   int2                                        NOT NULL,
    "receive_time"     timestamp(6),
    "api_receive_code" varchar(63) COLLATE "pg_catalog"."default",
    "api_receive_msg"  varchar(255) COLLATE "pg_catalog"."default",
    "creator"          varchar(64) COLLATE "pg_catalog"."default",
    "create_time"      timestamp(6)                                NOT NULL,
    "updater"          varchar(64) COLLATE "pg_catalog"."default",
    "update_time"      timestamp(6)                                NOT NULL,
    "deleted"          int2                                        NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_sms_log"."id" IS '编号';
COMMENT
ON COLUMN "system_sms_log"."channel_id" IS '短信渠道编号';
COMMENT
ON COLUMN "system_sms_log"."channel_code" IS '短信渠道编码';
COMMENT
ON COLUMN "system_sms_log"."template_id" IS '模板编号';
COMMENT
ON COLUMN "system_sms_log"."template_code" IS '模板编码';
COMMENT
ON COLUMN "system_sms_log"."template_type" IS '短信类型';
COMMENT
ON COLUMN "system_sms_log"."template_content" IS '短信内容';
COMMENT
ON COLUMN "system_sms_log"."template_params" IS '短信参数';
COMMENT
ON COLUMN "system_sms_log"."api_template_id" IS '短信 API 的模板编号';
COMMENT
ON COLUMN "system_sms_log"."mobile" IS '手机号';
COMMENT
ON COLUMN "system_sms_log"."user_id" IS '用户编号';
COMMENT
ON COLUMN "system_sms_log"."user_type" IS '用户类型';
COMMENT
ON COLUMN "system_sms_log"."send_status" IS '发送状态';
COMMENT
ON COLUMN "system_sms_log"."send_time" IS '发送时间';
COMMENT
ON COLUMN "system_sms_log"."send_code" IS '发送结果的编码';
COMMENT
ON COLUMN "system_sms_log"."send_msg" IS '发送结果的提示';
COMMENT
ON COLUMN "system_sms_log"."api_send_code" IS '短信 API 发送结果的编码';
COMMENT
ON COLUMN "system_sms_log"."api_send_msg" IS '短信 API 发送失败的提示';
COMMENT
ON COLUMN "system_sms_log"."api_request_id" IS '短信 API 发送返回的唯一请求 ID';
COMMENT
ON COLUMN "system_sms_log"."api_serial_no" IS '短信 API 发送返回的序号';
COMMENT
ON COLUMN "system_sms_log"."receive_status" IS '接收状态';
COMMENT
ON COLUMN "system_sms_log"."receive_time" IS '接收时间';
COMMENT
ON COLUMN "system_sms_log"."api_receive_code" IS 'API 接收结果的编码';
COMMENT
ON COLUMN "system_sms_log"."api_receive_msg" IS 'API 接收结果的说明';
COMMENT
ON COLUMN "system_sms_log"."creator" IS '创建者';
COMMENT
ON COLUMN "system_sms_log"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_sms_log"."updater" IS '更新者';
COMMENT
ON COLUMN "system_sms_log"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_sms_log"."deleted" IS '是否删除';
COMMENT
ON TABLE "system_sms_log" IS '短信日志';

-- ----------------------------
-- Records of system_sms_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for system_sms_template
-- ----------------------------
DROP TABLE IF EXISTS "system_sms_template";
CREATE TABLE "system_sms_template"
(
    "id"              int8                                        NOT NULL,
    "type"            int2                                        NOT NULL,
    "status"          int2                                        NOT NULL,
    "code"            varchar(63) COLLATE "pg_catalog"."default"  NOT NULL,
    "name"            varchar(63) COLLATE "pg_catalog"."default"  NOT NULL,
    "content"         varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "params"          varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "remark"          varchar(255) COLLATE "pg_catalog"."default",
    "api_template_id" varchar(63) COLLATE "pg_catalog"."default"  NOT NULL,
    "channel_id"      int8                                        NOT NULL,
    "channel_code"    varchar(63) COLLATE "pg_catalog"."default"  NOT NULL,
    "creator"         varchar(64) COLLATE "pg_catalog"."default",
    "create_time"     timestamp(6)                                NOT NULL,
    "updater"         varchar(64) COLLATE "pg_catalog"."default",
    "update_time"     timestamp(6)                                NOT NULL,
    "deleted"         int2                                        NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_sms_template"."id" IS '编号';
COMMENT
ON COLUMN "system_sms_template"."type" IS '短信签名';
COMMENT
ON COLUMN "system_sms_template"."status" IS '开启状态';
COMMENT
ON COLUMN "system_sms_template"."code" IS '模板编码';
COMMENT
ON COLUMN "system_sms_template"."name" IS '模板名称';
COMMENT
ON COLUMN "system_sms_template"."content" IS '模板内容';
COMMENT
ON COLUMN "system_sms_template"."params" IS '参数数组';
COMMENT
ON COLUMN "system_sms_template"."remark" IS '备注';
COMMENT
ON COLUMN "system_sms_template"."api_template_id" IS '短信 API 的模板编号';
COMMENT
ON COLUMN "system_sms_template"."channel_id" IS '短信渠道编号';
COMMENT
ON COLUMN "system_sms_template"."channel_code" IS '短信渠道编码';
COMMENT
ON COLUMN "system_sms_template"."creator" IS '创建者';
COMMENT
ON COLUMN "system_sms_template"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_sms_template"."updater" IS '更新者';
COMMENT
ON COLUMN "system_sms_template"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_sms_template"."deleted" IS '是否删除';
COMMENT
ON TABLE "system_sms_template" IS '短信模板';

-- ----------------------------
-- Records of system_sms_template
-- ----------------------------
BEGIN;
INSERT INTO "system_sms_template" ("id", "type", "status", "code", "name", "content", "params", "remark",
                                   "api_template_id", "channel_id", "channel_code", "creator", "create_time", "updater",
                                   "update_time", "deleted")
VALUES (2, 1, 0, 'test_01', '测试验证码短信', '正在进行登录操作{operation}，您的验证码是{code}', '["operation","code"]',
        NULL, '4383920', 1, 'YUN_PIAN', '', '2021-03-31 10:49:38', '1', '2021-04-10 01:22:00', 0);
INSERT INTO "system_sms_template" ("id", "type", "status", "code", "name", "content", "params", "remark",
                                   "api_template_id", "channel_id", "channel_code", "creator", "create_time", "updater",
                                   "update_time", "deleted")
VALUES (3, 1, 0, 'test_02', '公告通知', '您的验证码{code}，该验证码5分钟内有效，请勿泄漏于他人！', '["code"]', NULL,
        'SMS_207945135', 2, 'ALIYUN', '', '2021-03-31 11:56:30', '1', '2021-04-10 01:22:02', 0);
INSERT INTO "system_sms_template" ("id", "type", "status", "code", "name", "content", "params", "remark",
                                   "api_template_id", "channel_id", "channel_code", "creator", "create_time", "updater",
                                   "update_time", "deleted")
VALUES (6, 3, 0, 'test-01', '测试模板', '哈哈哈 {name}', '["name"]', 'f哈哈哈', '4383920', 1, 'YUN_PIAN', '1',
        '2021-04-10 01:07:21', '1', '2021-04-10 01:22:05', 0);
INSERT INTO "system_sms_template" ("id", "type", "status", "code", "name", "content", "params", "remark",
                                   "api_template_id", "channel_id", "channel_code", "creator", "create_time", "updater",
                                   "update_time", "deleted")
VALUES (7, 3, 0, 'test-04', '测试下', '老鸡{name}，牛逼{code}', '["name","code"]', NULL, 'suibian', 4, 'DEBUG_DING_TALK',
        '1', '2021-04-13 00:29:53', '1', '2021-04-14 00:30:38', 0);
INSERT INTO "system_sms_template" ("id", "type", "status", "code", "name", "content", "params", "remark",
                                   "api_template_id", "channel_id", "channel_code", "creator", "create_time", "updater",
                                   "update_time", "deleted")
VALUES (8, 1, 0, 'user-sms-login', '前台用户短信登录', '您的验证码是{code}', '["code"]', NULL, '4372216', 1, 'YUN_PIAN',
        '1', '2021-10-11 08:10:00', '1', '2021-10-11 08:10:00', 0);
INSERT INTO "system_sms_template" ("id", "type", "status", "code", "name", "content", "params", "remark",
                                   "api_template_id", "channel_id", "channel_code", "creator", "create_time", "updater",
                                   "update_time", "deleted")
VALUES (9, 2, 0, 'bpm_task_assigned', '【工作流】任务被分配',
        '您收到了一条新的待办任务：{processInstanceName}-{taskName}，申请人：{startUserNickname}，处理链接：{detailUrl}',
        '["processInstanceName","taskName","startUserNickname","detailUrl"]', NULL, 'suibian', 4, 'DEBUG_DING_TALK',
        '1', '2022-01-21 22:31:19', '1', '2022-01-22 00:03:36', 0);
INSERT INTO "system_sms_template" ("id", "type", "status", "code", "name", "content", "params", "remark",
                                   "api_template_id", "channel_id", "channel_code", "creator", "create_time", "updater",
                                   "update_time", "deleted")
VALUES (10, 2, 0, 'bpm_process_instance_reject', '【工作流】流程被不通过',
        '您的流程被审批不通过：{processInstanceName}，原因：{reason}，查看链接：{detailUrl}',
        '["processInstanceName","reason","detailUrl"]', NULL, 'suibian', 4, 'DEBUG_DING_TALK', '1',
        '2022-01-22 00:03:31', '1', '2022-05-01 12:33:14', 0);
INSERT INTO "system_sms_template" ("id", "type", "status", "code", "name", "content", "params", "remark",
                                   "api_template_id", "channel_id", "channel_code", "creator", "create_time", "updater",
                                   "update_time", "deleted")
VALUES (11, 2, 0, 'bpm_process_instance_approve', '【工作流】流程被通过',
        '您的流程被审批通过：{processInstanceName}，查看链接：{detailUrl}', '["processInstanceName","detailUrl"]', NULL,
        'suibian', 4, 'DEBUG_DING_TALK', '1', '2022-01-22 00:04:31', '1', '2022-03-27 20:32:21', 0);
INSERT INTO "system_sms_template" ("id", "type", "status", "code", "name", "content", "params", "remark",
                                   "api_template_id", "channel_id", "channel_code", "creator", "create_time", "updater",
                                   "update_time", "deleted")
VALUES (12, 2, 0, 'demo', '演示模板', '我就是测试一下下', '[]', NULL, 'biubiubiu', 6, 'DEBUG_DING_TALK', '1',
        '2022-04-10 23:22:49', '1', '2022-04-10 23:22:49', 0);
INSERT INTO "system_sms_template" ("id", "type", "status", "code", "name", "content", "params", "remark",
                                   "api_template_id", "channel_id", "channel_code", "creator", "create_time", "updater",
                                   "update_time", "deleted")
VALUES (13, 1, 0, 'admin-sms-login', '后台用户短信登录', '您的验证码是{code}', '["code"]', '', '4372216', 1, 'YUN_PIAN',
        '1', '2021-10-11 08:10:00', '1', '2021-10-11 08:10:00', 0);
COMMIT;

-- ----------------------------
-- Table structure for system_social_user
-- ----------------------------
DROP TABLE IF EXISTS "system_social_user";
CREATE TABLE "system_social_user"
(
    "id"             numeric(20, 0)                               NOT NULL,
    "type"           int2                                         NOT NULL,
    "openid"         varchar(32) COLLATE "pg_catalog"."default"   NOT NULL,
    "token"          varchar(256) COLLATE "pg_catalog"."default",
    "raw_token_info" varchar(1024) COLLATE "pg_catalog"."default" NOT NULL,
    "nickname"       varchar(32) COLLATE "pg_catalog"."default"   NOT NULL,
    "avatar"         varchar(255) COLLATE "pg_catalog"."default",
    "raw_user_info"  varchar(1024) COLLATE "pg_catalog"."default" NOT NULL,
    "code"           varchar(256) COLLATE "pg_catalog"."default"  NOT NULL,
    "state"          varchar(256) COLLATE "pg_catalog"."default",
    "creator"        varchar(64) COLLATE "pg_catalog"."default",
    "create_time"    timestamp(6)                                 NOT NULL,
    "updater"        varchar(64) COLLATE "pg_catalog"."default",
    "update_time"    timestamp(6)                                 NOT NULL,
    "deleted"        int2                                         NOT NULL DEFAULT 0,
    "tenant_id"      int8                                         NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_social_user"."id" IS '主键(自增策略)';
COMMENT
ON COLUMN "system_social_user"."type" IS '社交平台的类型';
COMMENT
ON COLUMN "system_social_user"."openid" IS '社交 openid';
COMMENT
ON COLUMN "system_social_user"."token" IS '社交 token';
COMMENT
ON COLUMN "system_social_user"."raw_token_info" IS '原始 Token 数据，一般是 JSON 格式';
COMMENT
ON COLUMN "system_social_user"."nickname" IS '用户昵称';
COMMENT
ON COLUMN "system_social_user"."avatar" IS '用户头像';
COMMENT
ON COLUMN "system_social_user"."raw_user_info" IS '原始用户数据，一般是 JSON 格式';
COMMENT
ON COLUMN "system_social_user"."code" IS '最后一次的认证 code';
COMMENT
ON COLUMN "system_social_user"."state" IS '最后一次的认证 state';
COMMENT
ON COLUMN "system_social_user"."creator" IS '创建者';
COMMENT
ON COLUMN "system_social_user"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_social_user"."updater" IS '更新者';
COMMENT
ON COLUMN "system_social_user"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_social_user"."deleted" IS '是否删除';
COMMENT
ON COLUMN "system_social_user"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "system_social_user" IS '社交用户表';

-- ----------------------------
-- Records of system_social_user
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for system_social_user_bind
-- ----------------------------
DROP TABLE IF EXISTS "system_social_user_bind";
CREATE TABLE "system_social_user_bind"
(
    "id"             numeric(20, 0) NOT NULL,
    "user_id"        int8           NOT NULL,
    "user_type"      int2           NOT NULL,
    "social_type"    int2           NOT NULL,
    "social_user_id" int8           NOT NULL,
    "creator"        varchar(64) COLLATE "pg_catalog"."default",
    "create_time"    timestamp(6)   NOT NULL,
    "updater"        varchar(64) COLLATE "pg_catalog"."default",
    "update_time"    timestamp(6)   NOT NULL,
    "deleted"        int2           NOT NULL DEFAULT 0,
    "tenant_id"      int8           NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_social_user_bind"."id" IS '主键(自增策略)';
COMMENT
ON COLUMN "system_social_user_bind"."user_id" IS '用户编号';
COMMENT
ON COLUMN "system_social_user_bind"."user_type" IS '用户类型';
COMMENT
ON COLUMN "system_social_user_bind"."social_type" IS '社交平台的类型';
COMMENT
ON COLUMN "system_social_user_bind"."social_user_id" IS '社交用户的编号';
COMMENT
ON COLUMN "system_social_user_bind"."creator" IS '创建者';
COMMENT
ON COLUMN "system_social_user_bind"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_social_user_bind"."updater" IS '更新者';
COMMENT
ON COLUMN "system_social_user_bind"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_social_user_bind"."deleted" IS '是否删除';
COMMENT
ON COLUMN "system_social_user_bind"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "system_social_user_bind" IS '社交绑定表';

-- ----------------------------
-- Records of system_social_user_bind
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for system_tenant
-- ----------------------------
DROP TABLE IF EXISTS "system_tenant";
CREATE TABLE "system_tenant"
(
    "id"              int8                                       NOT NULL,
    "name"            varchar(30) COLLATE "pg_catalog"."default" NOT NULL,
    "contact_user_id" int8,
    "contact_name"    varchar(30) COLLATE "pg_catalog"."default" NOT NULL,
    "contact_mobile"  varchar(500) COLLATE "pg_catalog"."default",
    "status"          int2                                       NOT NULL,
    "website"          varchar(256) COLLATE "pg_catalog"."default",
    "package_id"      int8                                       NOT NULL,
    "expire_time"     timestamp(6)                               NOT NULL,
    "account_count"   int4                                       NOT NULL,
    "creator"         varchar(64) COLLATE "pg_catalog"."default",
    "create_time"     timestamp(6)                               NOT NULL,
    "updater"         varchar(64) COLLATE "pg_catalog"."default",
    "update_time"     timestamp(6)                               NOT NULL,
    "deleted"         int2                                       NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_tenant"."id" IS '租户编号';
COMMENT
ON COLUMN "system_tenant"."name" IS '租户名';
COMMENT
ON COLUMN "system_tenant"."contact_user_id" IS '联系人的用户编号';
COMMENT
ON COLUMN "system_tenant"."contact_name" IS '联系人';
COMMENT
ON COLUMN "system_tenant"."contact_mobile" IS '联系手机';
COMMENT
ON COLUMN "system_tenant"."status" IS '租户状态（0正常 1停用）';
COMMENT
ON COLUMN "system_tenant"."website" IS '绑定域名';
COMMENT
ON COLUMN "system_tenant"."package_id" IS '租户套餐编号';
COMMENT
ON COLUMN "system_tenant"."expire_time" IS '过期时间';
COMMENT
ON COLUMN "system_tenant"."account_count" IS '账号数量';
COMMENT
ON COLUMN "system_tenant"."creator" IS '创建者';
COMMENT
ON COLUMN "system_tenant"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_tenant"."updater" IS '更新者';
COMMENT
ON COLUMN "system_tenant"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_tenant"."deleted" IS '是否删除';
COMMENT
ON TABLE "system_tenant" IS '租户表';

-- ----------------------------
-- Records of system_tenant
-- ----------------------------
BEGIN;
INSERT INTO "system_tenant" ("id", "name", "contact_user_id", "contact_name", "contact_mobile", "status", "website",
                             "package_id", "expire_time", "account_count", "creator", "create_time", "updater",
                             "update_time", "deleted")
VALUES (1, '芋道源码', NULL, '芋艿', '17321315478', 0, 'https://www.iocoder.cn', 0, '2099-02-19 17:14:16', 9999, '1',
        '2021-01-05 17:03:47', '1', '2022-02-23 12:15:11', 0);
INSERT INTO "system_tenant" ("id", "name", "contact_user_id", "contact_name", "contact_mobile", "status", "website",
                             "package_id", "expire_time", "account_count", "creator", "create_time", "updater",
                             "update_time", "deleted")
VALUES (121, '小租户', 110, '小王2', '15601691300', 0, 'http://www.iocoder.cn', 111, '2024-03-11 00:00:00', 20, '1',
        '2022-02-22 00:56:14', '1', '2022-03-19 18:37:20', 0);
INSERT INTO "system_tenant" ("id", "name", "contact_user_id", "contact_name", "contact_mobile", "status", "website",
                             "package_id", "expire_time", "account_count", "creator", "create_time", "updater",
                             "update_time", "deleted")
VALUES (122, '测试租户', 113, '芋道', '15601691300', 0, 'https://www.iocoder.cn', 111, '2022-04-30 00:00:00', 50, '1',
        '2022-03-07 21:37:58', '1', '2022-03-07 21:37:58', 0);
COMMIT;

-- ----------------------------
-- Table structure for system_tenant_package
-- ----------------------------
DROP TABLE IF EXISTS "system_tenant_package";
CREATE TABLE "system_tenant_package"
(
    "id"          int8                                         NOT NULL,
    "name"        varchar(30) COLLATE "pg_catalog"."default"   NOT NULL,
    "status"      int2                                         NOT NULL,
    "remark"      varchar(256) COLLATE "pg_catalog"."default",
    "menu_ids"    varchar(2048) COLLATE "pg_catalog"."default" NOT NULL,
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                                 NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                                 NOT NULL,
    "deleted"     int2                                         NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_tenant_package"."id" IS '套餐编号';
COMMENT
ON COLUMN "system_tenant_package"."name" IS '套餐名';
COMMENT
ON COLUMN "system_tenant_package"."status" IS '租户状态（0正常 1停用）';
COMMENT
ON COLUMN "system_tenant_package"."remark" IS '备注';
COMMENT
ON COLUMN "system_tenant_package"."menu_ids" IS '关联的菜单编号';
COMMENT
ON COLUMN "system_tenant_package"."creator" IS '创建者';
COMMENT
ON COLUMN "system_tenant_package"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_tenant_package"."updater" IS '更新者';
COMMENT
ON COLUMN "system_tenant_package"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_tenant_package"."deleted" IS '是否删除';
COMMENT
ON TABLE "system_tenant_package" IS '租户套餐表';

-- ----------------------------
-- Records of system_tenant_package
-- ----------------------------
BEGIN;
INSERT INTO "system_tenant_package" ("id", "name", "status", "remark", "menu_ids", "creator", "create_time", "updater",
                                     "update_time", "deleted")
VALUES (111, '普通套餐', 0, '小功能',
        '[1024,1025,1,102,103,104,1013,1014,1015,1016,1017,1018,1019,1020,1021,1022,1023]', '1', '2022-02-22 00:54:00',
        '1', '2022-03-19 18:39:13', 0);
COMMIT;

-- ----------------------------
-- Table structure for system_user_post
-- ----------------------------
DROP TABLE IF EXISTS "system_user_post";
CREATE TABLE "system_user_post"
(
    "id"          int8         NOT NULL,
    "user_id"     int8         NOT NULL,
    "post_id"     int8         NOT NULL,
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6) NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6) NOT NULL,
    "tenant_id"   int8         NOT NULL,
    "deleted"     int2         NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_user_post"."id" IS 'id';
COMMENT
ON COLUMN "system_user_post"."user_id" IS '用户ID';
COMMENT
ON COLUMN "system_user_post"."post_id" IS '岗位ID';
COMMENT
ON COLUMN "system_user_post"."creator" IS '创建者';
COMMENT
ON COLUMN "system_user_post"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_user_post"."updater" IS '更新者';
COMMENT
ON COLUMN "system_user_post"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_user_post"."tenant_id" IS '租户编号';
COMMENT
ON COLUMN "system_user_post"."deleted" IS '是否删除';
COMMENT
ON TABLE "system_user_post" IS '用户岗位表';

-- ----------------------------
-- Records of system_user_post
-- ----------------------------
BEGIN;
INSERT INTO "system_user_post" ("id", "user_id", "post_id", "creator", "create_time", "updater", "update_time",
                                "tenant_id", "deleted")
VALUES (112, 1, 1, 'admin', '2022-05-02 07:25:24', 'admin', '2022-05-02 07:25:24', 1, 0);
INSERT INTO "system_user_post" ("id", "user_id", "post_id", "creator", "create_time", "updater", "update_time",
                                "tenant_id", "deleted")
VALUES (113, 100, 1, 'admin', '2022-05-02 07:25:24', 'admin', '2022-05-02 07:25:24', 1, 0);
INSERT INTO "system_user_post" ("id", "user_id", "post_id", "creator", "create_time", "updater", "update_time",
                                "tenant_id", "deleted")
VALUES (114, 114, 3, 'admin', '2022-05-02 07:25:24', 'admin', '2022-05-02 07:25:24', 1, 0);
COMMIT;

-- ----------------------------
-- Table structure for system_user_role
-- ----------------------------
DROP TABLE IF EXISTS "system_user_role";
CREATE TABLE "system_user_role"
(
    "id"          int8 NOT NULL,
    "user_id"     int8 NOT NULL,
    "role_id"     int8 NOT NULL,
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6),
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6),
    "deleted"     int2 NOT NULL DEFAULT 0,
    "tenant_id"   int8 NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_user_role"."id" IS '自增编号';
COMMENT
ON COLUMN "system_user_role"."user_id" IS '用户ID';
COMMENT
ON COLUMN "system_user_role"."role_id" IS '角色ID';
COMMENT
ON COLUMN "system_user_role"."creator" IS '创建者';
COMMENT
ON COLUMN "system_user_role"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_user_role"."updater" IS '更新者';
COMMENT
ON COLUMN "system_user_role"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_user_role"."deleted" IS '是否删除';
COMMENT
ON COLUMN "system_user_role"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "system_user_role" IS '用户和角色关联表';

-- ----------------------------
-- Records of system_user_role
-- ----------------------------
BEGIN;
INSERT INTO "system_user_role" ("id", "user_id", "role_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (1, 1, 1, '', '2022-01-11 13:19:45', '', '2022-05-12 12:35:17', 0, 1);
INSERT INTO "system_user_role" ("id", "user_id", "role_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (2, 2, 2, '', '2022-01-11 13:19:45', '', '2022-05-12 12:35:13', 0, 1);
INSERT INTO "system_user_role" ("id", "user_id", "role_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (4, 100, 101, '', '2022-01-11 13:19:45', '', '2022-05-12 12:35:13', 0, 1);
INSERT INTO "system_user_role" ("id", "user_id", "role_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (5, 100, 1, '', '2022-01-11 13:19:45', '', '2022-05-12 12:35:12', 0, 1);
INSERT INTO "system_user_role" ("id", "user_id", "role_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (6, 100, 2, '', '2022-01-11 13:19:45', '', '2022-05-12 12:35:11', 0, 1);
INSERT INTO "system_user_role" ("id", "user_id", "role_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (7, 104, 101, '', '2022-01-11 13:19:45', '', '2022-05-12 12:35:11', 0, 1);
INSERT INTO "system_user_role" ("id", "user_id", "role_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (10, 103, 1, '1', '2022-01-11 13:19:45', '1', '2022-01-11 13:19:45', 0, 1);
INSERT INTO "system_user_role" ("id", "user_id", "role_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (11, 107, 106, '1', '2022-02-20 22:59:33', '1', '2022-02-20 22:59:33', 0, 118);
INSERT INTO "system_user_role" ("id", "user_id", "role_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (12, 108, 107, '1', '2022-02-20 23:00:50', '1', '2022-02-20 23:00:50', 0, 119);
INSERT INTO "system_user_role" ("id", "user_id", "role_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (13, 109, 108, '1', '2022-02-20 23:11:50', '1', '2022-02-20 23:11:50', 0, 120);
INSERT INTO "system_user_role" ("id", "user_id", "role_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (14, 110, 109, '1', '2022-02-22 00:56:14', '1', '2022-02-22 00:56:14', 0, 121);
INSERT INTO "system_user_role" ("id", "user_id", "role_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (15, 111, 110, '110', '2022-02-23 13:14:38', '110', '2022-02-23 13:14:38', 0, 121);
INSERT INTO "system_user_role" ("id", "user_id", "role_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (16, 113, 111, '1', '2022-03-07 21:37:58', '1', '2022-03-07 21:37:58', 0, 122);
INSERT INTO "system_user_role" ("id", "user_id", "role_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (17, 114, 101, '1', '2022-03-19 21:51:13', '1', '2022-03-19 21:51:13', 0, 1);
INSERT INTO "system_user_role" ("id", "user_id", "role_id", "creator", "create_time", "updater", "update_time",
                                "deleted", "tenant_id")
VALUES (18, 1, 2, '1', '2022-05-12 20:39:29', '1', '2022-05-12 20:39:29', 0, 1);
COMMIT;

-- ----------------------------
-- Table structure for system_users
-- ----------------------------
DROP TABLE IF EXISTS "system_users";
CREATE TABLE "system_users"
(
    "id"          int8                                        NOT NULL,
    "username"    varchar(30) COLLATE "pg_catalog"."default"  NOT NULL,
    "password"    varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
    "nickname"    varchar(30) COLLATE "pg_catalog"."default"  NOT NULL,
    "remark"      varchar(500) COLLATE "pg_catalog"."default",
    "dept_id"     int8,
    "post_ids"    varchar(255) COLLATE "pg_catalog"."default",
    "email"       varchar(50) COLLATE "pg_catalog"."default",
    "mobile"      varchar(11) COLLATE "pg_catalog"."default",
    "sex"         int2,
    "avatar"      varchar(100) COLLATE "pg_catalog"."default",
    "status"      int2                                        NOT NULL,
    "login_ip"    varchar(50) COLLATE "pg_catalog"."default",
    "login_date"  timestamp(6),
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                                NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                                NOT NULL,
    "deleted"     int2                                        NOT NULL DEFAULT 0,
    "tenant_id"   int8                                        NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_users"."id" IS '用户ID';
COMMENT
ON COLUMN "system_users"."username" IS '用户账号';
COMMENT
ON COLUMN "system_users"."password" IS '密码';
COMMENT
ON COLUMN "system_users"."nickname" IS '用户昵称';
COMMENT
ON COLUMN "system_users"."remark" IS '备注';
COMMENT
ON COLUMN "system_users"."dept_id" IS '部门ID';
COMMENT
ON COLUMN "system_users"."post_ids" IS '岗位编号数组';
COMMENT
ON COLUMN "system_users"."email" IS '用户邮箱';
COMMENT
ON COLUMN "system_users"."mobile" IS '手机号码';
COMMENT
ON COLUMN "system_users"."sex" IS '用户性别';
COMMENT
ON COLUMN "system_users"."avatar" IS '头像地址';
COMMENT
ON COLUMN "system_users"."status" IS '帐号状态（0正常 1停用）';
COMMENT
ON COLUMN "system_users"."login_ip" IS '最后登录IP';
COMMENT
ON COLUMN "system_users"."login_date" IS '最后登录时间';
COMMENT
ON COLUMN "system_users"."creator" IS '创建者';
COMMENT
ON COLUMN "system_users"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_users"."updater" IS '更新者';
COMMENT
ON COLUMN "system_users"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_users"."deleted" IS '是否删除';
COMMENT
ON COLUMN "system_users"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "system_users" IS '用户信息表';

-- ----------------------------
-- Records of system_users
-- ----------------------------
BEGIN;
INSERT INTO "system_users" ("id", "username", "password", "nickname", "remark", "dept_id", "post_ids", "email",
                            "mobile", "sex", "avatar", "status", "login_ip", "login_date", "creator", "create_time",
                            "updater", "update_time", "deleted", "tenant_id")
VALUES (100, 'yudao', '$2a$10$11U48RhyJ5pSBYWSn12AD./ld671.ycSzJHbyrtpeoMeYiw31eo8a', '芋道', '不要吓我', 104, '[1]',
        'yudao@iocoder.cn', '15601691300', 1, '', 1, '', NULL, '', '2021-01-07 09:07:17', '104', '2021-12-16 09:26:10',
        0, 1);
INSERT INTO "system_users" ("id", "username", "password", "nickname", "remark", "dept_id", "post_ids", "email",
                            "mobile", "sex", "avatar", "status", "login_ip", "login_date", "creator", "create_time",
                            "updater", "update_time", "deleted", "tenant_id")
VALUES (103, 'yuanma', '$2a$10$wWoPT7sqriM2O1YXRL.je.GiL538OR6ZTN8aQZr9JAGdnpCH2tpYe', '源码', NULL, 106, NULL,
        'yuanma@iocoder.cn', '15601701300', 0, '', 0, '127.0.0.1', '2022-01-18 00:33:40', '', '2021-01-13 23:50:35',
        NULL, '2022-01-18 00:33:40', 0, 1);
INSERT INTO "system_users" ("id", "username", "password", "nickname", "remark", "dept_id", "post_ids", "email",
                            "mobile", "sex", "avatar", "status", "login_ip", "login_date", "creator", "create_time",
                            "updater", "update_time", "deleted", "tenant_id")
VALUES (104, 'test', '$2a$10$e5RpuDCC0GYSt0Hvd2.CjujIXwgGct4SnXi6dVGxdgFsnqgEryk5a', '测试号', NULL, 107, '[]',
        '111@qq.com', '15601691200', 1, '', 0, '127.0.0.1', '2022-03-19 21:46:19', '', '2021-01-21 02:13:53', NULL,
        '2022-03-19 21:46:19', 0, 1);
INSERT INTO "system_users" ("id", "username", "password", "nickname", "remark", "dept_id", "post_ids", "email",
                            "mobile", "sex", "avatar", "status", "login_ip", "login_date", "creator", "create_time",
                            "updater", "update_time", "deleted", "tenant_id")
VALUES (107, 'admin107', '$2a$10$dYOOBKMO93v/.ReCqzyFg.o67Tqk.bbc2bhrpyBGkIw9aypCtr2pm', '芋艿', NULL, NULL, NULL, '',
        '15601691300', 0, '', 0, '', NULL, '1', '2022-02-20 22:59:33', '1', '2022-02-27 08:26:51', 0, 118);
INSERT INTO "system_users" ("id", "username", "password", "nickname", "remark", "dept_id", "post_ids", "email",
                            "mobile", "sex", "avatar", "status", "login_ip", "login_date", "creator", "create_time",
                            "updater", "update_time", "deleted", "tenant_id")
VALUES (108, 'admin108', '$2a$10$y6mfvKoNYL1GXWak8nYwVOH.kCWqjactkzdoIDgiKl93WN3Ejg.Lu', '芋艿', NULL, NULL, NULL, '',
        '15601691300', 0, '', 0, '', NULL, '1', '2022-02-20 23:00:50', '1', '2022-02-27 08:26:53', 0, 119);
INSERT INTO "system_users" ("id", "username", "password", "nickname", "remark", "dept_id", "post_ids", "email",
                            "mobile", "sex", "avatar", "status", "login_ip", "login_date", "creator", "create_time",
                            "updater", "update_time", "deleted", "tenant_id")
VALUES (109, 'admin109', '$2a$10$JAqvH0tEc0I7dfDVBI7zyuB4E3j.uH6daIjV53.vUS6PknFkDJkuK', '芋艿', NULL, NULL, NULL, '',
        '15601691300', 0, '', 0, '', NULL, '1', '2022-02-20 23:11:50', '1', '2022-02-27 08:26:56', 0, 120);
INSERT INTO "system_users" ("id", "username", "password", "nickname", "remark", "dept_id", "post_ids", "email",
                            "mobile", "sex", "avatar", "status", "login_ip", "login_date", "creator", "create_time",
                            "updater", "update_time", "deleted", "tenant_id")
VALUES (110, 'admin110', '$2a$10$qYxoXs0ogPHgYllyEneYde9xcCW5hZgukrxeXZ9lmLhKse8TK6IwW', '小王', NULL, NULL, NULL, '',
        '15601691300', 0, '', 0, '127.0.0.1', '2022-02-23 19:36:28', '1', '2022-02-22 00:56:14', NULL,
        '2022-02-27 08:26:59', 0, 121);
INSERT INTO "system_users" ("id", "username", "password", "nickname", "remark", "dept_id", "post_ids", "email",
                            "mobile", "sex", "avatar", "status", "login_ip", "login_date", "creator", "create_time",
                            "updater", "update_time", "deleted", "tenant_id")
VALUES (111, 'test', '$2a$10$mExveopHUx9Q4QiLtAzhDeH3n4/QlNLzEsM4AqgxKrU.ciUZDXZCy', '测试用户', NULL, NULL, '[]', '',
        '', 0, '', 0, '', NULL, '110', '2022-02-23 13:14:33', '110', '2022-02-23 13:14:33', 0, 121);
INSERT INTO "system_users" ("id", "username", "password", "nickname", "remark", "dept_id", "post_ids", "email",
                            "mobile", "sex", "avatar", "status", "login_ip", "login_date", "creator", "create_time",
                            "updater", "update_time", "deleted", "tenant_id")
VALUES (112, 'newobject', '$2a$10$jh5MsR.ud/gKe3mVeUp5t.nEXGDSmHyv5OYjWQwHO8wlGmMSI9Twy', '新对象', NULL, NULL, '[]',
        '', '', 0, '', 0, '', NULL, '1', '2022-02-23 19:08:03', '1', '2022-02-23 19:08:03', 0, 1);
INSERT INTO "system_users" ("id", "username", "password", "nickname", "remark", "dept_id", "post_ids", "email",
                            "mobile", "sex", "avatar", "status", "login_ip", "login_date", "creator", "create_time",
                            "updater", "update_time", "deleted", "tenant_id")
VALUES (113, 'aoteman', '$2a$10$0acJOIk2D25/oC87nyclE..0lzeu9DtQ/n3geP4fkun/zIVRhHJIO', '芋道', NULL, NULL, NULL, '',
        '15601691300', 0, '', 0, '127.0.0.1', '2022-03-19 18:38:51', '1', '2022-03-07 21:37:58', NULL,
        '2022-03-19 18:38:51', 0, 122);
INSERT INTO "system_users" ("id", "username", "password", "nickname", "remark", "dept_id", "post_ids", "email",
                            "mobile", "sex", "avatar", "status", "login_ip", "login_date", "creator", "create_time",
                            "updater", "update_time", "deleted", "tenant_id")
VALUES (114, 'hrmgr', '$2a$10$TR4eybBioGRhBmDBWkqWLO6NIh3mzYa8KBKDDB5woiGYFVlRAi.fu', 'hr 小姐姐', NULL, NULL, '[3]',
        '', '', 0, '', 0, '127.0.0.1', '2022-03-19 22:15:43', '1', '2022-03-19 21:50:58', NULL, '2022-03-19 22:15:43',
        0, 1);
INSERT INTO "system_users" ("id", "username", "password", "nickname", "remark", "dept_id", "post_ids", "email",
                            "mobile", "sex", "avatar", "status", "login_ip", "login_date", "creator", "create_time",
                            "updater", "update_time", "deleted", "tenant_id")
VALUES (115, 'aotemane', '$2a$10$/WCwGHu1eq0wOVDd/u8HweJ0gJCHyLS6T7ndCqI8UXZAQom1etk2e', '1', '11', 100, '[]', '', '',
        0, '', 0, '', NULL, '1', '2022-04-30 02:55:43', '1', '2022-04-30 02:55:43', 0, 1);
INSERT INTO "system_users" ("id", "username", "password", "nickname", "remark", "dept_id", "post_ids", "email",
                            "mobile", "sex", "avatar", "status", "login_ip", "login_date", "creator", "create_time",
                            "updater", "update_time", "deleted", "tenant_id")
VALUES (1, 'admin', '$2a$10$0acJOIk2D25/oC87nyclE..0lzeu9DtQ/n3geP4fkun/zIVRhHJIO', '芋道源码', '管理员', 103, '[1]',
        'aoteman@126.com', '15612345678', 1, 'http://test.yudao.iocoder.cn/48934f2f-92d4-4250-b917-d10d2b262c6a', 0,
        '127.0.0.1', '2022-05-25 23:44:33.003', 'admin', '2021-01-05 17:03:47', NULL, '2022-05-25 23:44:33.003', 0, 1);
COMMIT;



-- ----------------------------
-- Table structure for system_mail_account
-- ----------------------------
DROP TABLE IF EXISTS "system_mail_account";
CREATE TABLE "system_mail_account"
(
    "id"          int8                                        NOT NULL,
    "mail"        varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "username"    varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "password"    varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "host"        varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
    "port"        int4                                        NOT NULL,
    "ssl_enable"  varchar(1) COLLATE "pg_catalog"."default"   NOT NULL,
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                                NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                                NOT NULL,
    "deleted"     int2                                        NOT NULL
)
;
COMMENT
ON COLUMN "system_mail_account"."id" IS '主键';
COMMENT
ON COLUMN "system_mail_account"."mail" IS '邮箱';
COMMENT
ON COLUMN "system_mail_account"."username" IS '用户名';
COMMENT
ON COLUMN "system_mail_account"."password" IS '密码';
COMMENT
ON COLUMN "system_mail_account"."host" IS 'SMTP 服务器域名';
COMMENT
ON COLUMN "system_mail_account"."port" IS 'SMTP 服务器端口';
COMMENT
ON COLUMN "system_mail_account"."ssl_enable" IS '是否开启 SSL';
COMMENT
ON COLUMN "system_mail_account"."creator" IS '创建者';
COMMENT
ON COLUMN "system_mail_account"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_mail_account"."updater" IS '更新者';
COMMENT
ON COLUMN "system_mail_account"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_mail_account"."deleted" IS '是否删除';
COMMENT
ON TABLE "system_mail_account" IS '邮箱账号表';


-- ----------------------------
-- Table structure for system_mail_log
-- ----------------------------
DROP TABLE IF EXISTS "system_mail_log";
CREATE TABLE "system_mail_log"
(
    "id"                int8                                          NOT NULL,
    "user_id"           int8,
    "user_type"         int2,
    "to_mail"           varchar(255) COLLATE "pg_catalog"."default"   NOT NULL,
    "account_id"        int8                                          NOT NULL,
    "from_mail"         varchar(255) COLLATE "pg_catalog"."default"   NOT NULL,
    "template_id"       int8                                          NOT NULL,
    "template_code"     varchar(63) COLLATE "pg_catalog"."default"    NOT NULL,
    "template_nickname" varchar(255) COLLATE "pg_catalog"."default",
    "template_title"    varchar(255) COLLATE "pg_catalog"."default"   NOT NULL,
    "template_content"  varchar(10240) COLLATE "pg_catalog"."default" NOT NULL,
    "template_params"   varchar(255) COLLATE "pg_catalog"."default"   NOT NULL,
    "send_status"       int2                                          NOT NULL,
    "send_time"         timestamp(6),
    "send_message_id"   varchar(255) COLLATE "pg_catalog"."default",
    "send_exception"    varchar(4096) COLLATE "pg_catalog"."default",
    "creator"           varchar(64) COLLATE "pg_catalog"."default",
    "create_time"       timestamp(6)                                  NOT NULL,
    "updater"           varchar(64) COLLATE "pg_catalog"."default",
    "update_time"       timestamp(6)                                  NOT NULL,
    "deleted"           int2                                          NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_mail_log"."id" IS '编号';
COMMENT
ON COLUMN "system_mail_log"."user_id" IS '用户编号';
COMMENT
ON COLUMN "system_mail_log"."user_type" IS '用户类型';
COMMENT
ON COLUMN "system_mail_log"."to_mail" IS '接收邮箱地址';
COMMENT
ON COLUMN "system_mail_log"."account_id" IS '邮箱账号编号';
COMMENT
ON COLUMN "system_mail_log"."from_mail" IS '发送邮箱地址';
COMMENT
ON COLUMN "system_mail_log"."template_id" IS '模板编号';
COMMENT
ON COLUMN "system_mail_log"."template_code" IS '模板编码';
COMMENT
ON COLUMN "system_mail_log"."template_nickname" IS '模版发送人名称';
COMMENT
ON COLUMN "system_mail_log"."template_title" IS '邮件标题';
COMMENT
ON COLUMN "system_mail_log"."template_content" IS '邮件内容';
COMMENT
ON COLUMN "system_mail_log"."template_params" IS '邮件参数';
COMMENT
ON COLUMN "system_mail_log"."send_status" IS '发送状态';
COMMENT
ON COLUMN "system_mail_log"."send_time" IS '发送时间';
COMMENT
ON COLUMN "system_mail_log"."send_message_id" IS '发送返回的消息 ID';
COMMENT
ON COLUMN "system_mail_log"."send_exception" IS '发送异常';
COMMENT
ON COLUMN "system_mail_log"."creator" IS '创建者';
COMMENT
ON COLUMN "system_mail_log"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_mail_log"."updater" IS '更新者';
COMMENT
ON COLUMN "system_mail_log"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_mail_log"."deleted" IS '是否删除';
COMMENT
ON TABLE "system_mail_log" IS '邮件日志表';


-- ----------------------------
-- Table structure for system_mail_template
-- ----------------------------
DROP TABLE IF EXISTS "system_mail_template";
CREATE TABLE "system_mail_template"
(
    "id"          int8                                          NOT NULL,
    "name"        varchar(63) COLLATE "pg_catalog"."default"    NOT NULL,
    "code"        varchar(63) COLLATE "pg_catalog"."default"    NOT NULL,
    "account_id"  int8                                          NOT NULL,
    "nickname"    varchar(255) COLLATE "pg_catalog"."default",
    "title"       varchar(255) COLLATE "pg_catalog"."default"   NOT NULL,
    "content"     varchar(10240) COLLATE "pg_catalog"."default" NOT NULL,
    "params"      varchar(255) COLLATE "pg_catalog"."default"   NOT NULL,
    "status"      int2                                          NOT NULL,
    "remark"      varchar(255) COLLATE "pg_catalog"."default",
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                                  NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                                  NOT NULL,
    "deleted"     int2                                          NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_mail_template"."id" IS '编号';
COMMENT
ON COLUMN "system_mail_template"."name" IS '模板名称';
COMMENT
ON COLUMN "system_mail_template"."code" IS '模板编码';
COMMENT
ON COLUMN "system_mail_template"."account_id" IS '发送的邮箱账号编号';
COMMENT
ON COLUMN "system_mail_template"."nickname" IS '发送人名称';
COMMENT
ON COLUMN "system_mail_template"."title" IS '模板标题';
COMMENT
ON COLUMN "system_mail_template"."content" IS '模板内容';
COMMENT
ON COLUMN "system_mail_template"."params" IS '参数数组';
COMMENT
ON COLUMN "system_mail_template"."status" IS '开启状态';
COMMENT
ON COLUMN "system_mail_template"."remark" IS '备注';
COMMENT
ON COLUMN "system_mail_template"."creator" IS '创建者';
COMMENT
ON COLUMN "system_mail_template"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_mail_template"."updater" IS '更新者';
COMMENT
ON COLUMN "system_mail_template"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_mail_template"."deleted" IS '是否删除';
COMMENT
ON TABLE "system_mail_template" IS '邮件模版表';



-- ----------------------------
-- Table structure for system_notify_message
-- ----------------------------
DROP TABLE IF EXISTS "system_notify_message";
CREATE TABLE "system_notify_message"
(
    "id"                int8                                         NOT NULL,
    "user_id"           int8                                         NOT NULL,
    "user_type"         int2                                         NOT NULL,
    "template_id"       int8                                         NOT NULL,
    "template_code"     varchar(64) COLLATE "pg_catalog"."default"   NOT NULL,
    "template_nickname" varchar(63) COLLATE "pg_catalog"."default"   NOT NULL,
    "template_content"  varchar(1024) COLLATE "pg_catalog"."default" NOT NULL,
    "template_type"     int4                                         NOT NULL,
    "template_params"   varchar(255) COLLATE "pg_catalog"."default"  NOT NULL,
    "read_status"       bool                                         NOT NULL DEFAULT false,
    "read_time"         timestamp(6),
    "creator"           varchar(64) COLLATE "pg_catalog"."default",
    "create_time"       timestamp(6)                                 NOT NULL,
    "updater"           varchar(64) COLLATE "pg_catalog"."default",
    "update_time"       timestamp(6)                                 NOT NULL,
    "deleted"           int2                                         NOT NULL DEFAULT 0,
    "tenant_id"         int8                                         NOT NULL
)
;
COMMENT
ON COLUMN "system_notify_message"."id" IS '用户ID';
COMMENT
ON COLUMN "system_notify_message"."user_id" IS '用户id';
COMMENT
ON COLUMN "system_notify_message"."user_type" IS '用户类型';
COMMENT
ON COLUMN "system_notify_message"."template_id" IS '模版编号';
COMMENT
ON COLUMN "system_notify_message"."template_code" IS '模板编码';
COMMENT
ON COLUMN "system_notify_message"."template_nickname" IS '模版发送人名称';
COMMENT
ON COLUMN "system_notify_message"."template_content" IS '模版内容';
COMMENT
ON COLUMN "system_notify_message"."template_type" IS '模版类型';
COMMENT
ON COLUMN "system_notify_message"."template_params" IS '模版参数';
COMMENT
ON COLUMN "system_notify_message"."read_status" IS '是否已读';
COMMENT
ON COLUMN "system_notify_message"."read_time" IS '阅读时间';
COMMENT
ON COLUMN "system_notify_message"."creator" IS '创建者';
COMMENT
ON COLUMN "system_notify_message"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_notify_message"."updater" IS '更新者';
COMMENT
ON COLUMN "system_notify_message"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_notify_message"."deleted" IS '是否删除';
COMMENT
ON COLUMN "system_notify_message"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "system_notify_message" IS '站内信消息表';



-- ----------------------------
-- Table structure for system_notify_template
-- ----------------------------
DROP TABLE IF EXISTS "system_notify_template";
CREATE TABLE "system_notify_template"
(
    "id"          int8                                         NOT NULL,
    "name"        varchar(63) COLLATE "pg_catalog"."default"   NOT NULL,
    "code"        varchar(64) COLLATE "pg_catalog"."default"   NOT NULL,
    "nickname"    varchar(255) COLLATE "pg_catalog"."default"  NOT NULL,
    "content"     varchar(1024) COLLATE "pg_catalog"."default" NOT NULL,
    "type"        int2                                         NOT NULL,
    "params"      varchar(255) COLLATE "pg_catalog"."default",
    "status"      int2                                         NOT NULL,
    "remark"      varchar(255) COLLATE "pg_catalog"."default",
    "creator"     varchar(64) COLLATE "pg_catalog"."default",
    "create_time" timestamp(6)                                 NOT NULL,
    "updater"     varchar(64) COLLATE "pg_catalog"."default",
    "update_time" timestamp(6)                                 NOT NULL,
    "deleted"     int2                                         NOT NULL DEFAULT 0
)
;
COMMENT
ON COLUMN "system_notify_template"."id" IS '主键';
COMMENT
ON COLUMN "system_notify_template"."name" IS '模板名称';
COMMENT
ON COLUMN "system_notify_template"."code" IS '模版编码';
COMMENT
ON COLUMN "system_notify_template"."nickname" IS '发送人名称';
COMMENT
ON COLUMN "system_notify_template"."content" IS '模版内容';
COMMENT
ON COLUMN "system_notify_template"."type" IS '类型';
COMMENT
ON COLUMN "system_notify_template"."params" IS '参数数组';
COMMENT
ON COLUMN "system_notify_template"."status" IS '状态';
COMMENT
ON COLUMN "system_notify_template"."remark" IS '备注';
COMMENT
ON COLUMN "system_notify_template"."creator" IS '创建者';
COMMENT
ON COLUMN "system_notify_template"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_notify_template"."updater" IS '更新者';
COMMENT
ON COLUMN "system_notify_template"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_notify_template"."deleted" IS '是否删除';
COMMENT
ON TABLE "system_notify_template" IS '站内信模板表';



-- ----------------------------
-- Table structure for system_user_session
-- ----------------------------
DROP TABLE IF EXISTS "system_user_session";
CREATE TABLE "system_user_session"
(
    "id"              int8                                        NOT NULL,
    "token"           varchar(32) COLLATE "pg_catalog"."default"  NOT NULL,
    "user_id"         int8                                        NOT NULL,
    "user_type"       int2                                        NOT NULL,
    "session_timeout" timestamp(6)                                NOT NULL,
    "username"        varchar(30) COLLATE "pg_catalog"."default"  NOT NULL,
    "user_ip"         varchar(50) COLLATE "pg_catalog"."default"  NOT NULL,
    "user_agent"      varchar(512) COLLATE "pg_catalog"."default" NOT NULL,
    "creator"         varchar(64) COLLATE "pg_catalog"."default",
    "create_time"     timestamp(6)                                NOT NULL,
    "updater"         varchar(64) COLLATE "pg_catalog"."default",
    "update_time"     timestamp(6)                                NOT NULL,
    "deleted"         int2                                        NOT NULL DEFAULT 0,
    "tenant_id"       int8                                        NOT NULL
)
;
COMMENT
ON COLUMN "system_user_session"."id" IS '编号';
COMMENT
ON COLUMN "system_user_session"."token" IS '会话编号';
COMMENT
ON COLUMN "system_user_session"."user_id" IS '用户编号';
COMMENT
ON COLUMN "system_user_session"."user_type" IS '用户类型';
COMMENT
ON COLUMN "system_user_session"."session_timeout" IS '会话超时时间';
COMMENT
ON COLUMN "system_user_session"."username" IS '用户账号';
COMMENT
ON COLUMN "system_user_session"."user_ip" IS '用户 IP';
COMMENT
ON COLUMN "system_user_session"."user_agent" IS '浏览器 UA';
COMMENT
ON COLUMN "system_user_session"."creator" IS '创建者';
COMMENT
ON COLUMN "system_user_session"."create_time" IS '创建时间';
COMMENT
ON COLUMN "system_user_session"."updater" IS '更新者';
COMMENT
ON COLUMN "system_user_session"."update_time" IS '更新时间';
COMMENT
ON COLUMN "system_user_session"."deleted" IS '是否删除';
COMMENT
ON COLUMN "system_user_session"."tenant_id" IS '租户编号';
COMMENT
ON TABLE "system_user_session" IS '用户在线 Session';





-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"bpm_oa_leave_seq"', 1, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"bpm_task_assign_rule_seq"', 1, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"infra_api_access_log_seq"', 537, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"infra_api_error_log_seq"', 73, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"infra_job_log_seq"', 1, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"infra_job_seq"', 2, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"system_error_code_seq"', 186, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"system_login_log_seq"', 23, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"system_oauth2_access_token_seq"', 11, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"system_oauth2_approve_seq"', 4, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"system_oauth2_client_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"system_oauth2_code_seq"', 4, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"system_oauth2_refresh_token_seq"', 1, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"system_operate_log_seq"', 44, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"system_sms_log_seq"', 1, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------

-- ----------------------------
-- Primary Key structure for table bpm_form
-- ----------------------------
ALTER TABLE "bpm_form"
    ADD CONSTRAINT "bpm_form_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table bpm_oa_leave
-- ----------------------------
ALTER TABLE "bpm_oa_leave"
    ADD CONSTRAINT "bpm_oa_leave_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table bpm_process_definition_ext
-- ----------------------------
ALTER TABLE "bpm_process_definition_ext"
    ADD CONSTRAINT "bpm_process_definition_ext_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table bpm_process_instance_ext
-- ----------------------------
ALTER TABLE "bpm_process_instance_ext"
    ADD CONSTRAINT "bpm_process_instance_ext_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table bpm_task_assign_rule
-- ----------------------------
ALTER TABLE "bpm_task_assign_rule"
    ADD CONSTRAINT "bpm_task_assign_rule_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table bpm_task_ext
-- ----------------------------
ALTER TABLE "bpm_task_ext"
    ADD CONSTRAINT "bpm_task_ext_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table bpm_user_group
-- ----------------------------
ALTER TABLE "bpm_user_group"
    ADD CONSTRAINT "bpm_user_group_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table infra_api_access_log
-- ----------------------------
ALTER TABLE "infra_api_access_log"
    ADD CONSTRAINT "infra_api_access_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table infra_api_error_log
-- ----------------------------
ALTER TABLE "infra_api_error_log"
    ADD CONSTRAINT "infra_api_error_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table infra_codegen_column
-- ----------------------------
ALTER TABLE "infra_codegen_column"
    ADD CONSTRAINT "infra_codegen_column_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table infra_codegen_table
-- ----------------------------
ALTER TABLE "infra_codegen_table"
    ADD CONSTRAINT "infra_codegen_table_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table infra_config
-- ----------------------------
ALTER TABLE "infra_config"
    ADD CONSTRAINT "infra_config_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table infra_data_source_config
-- ----------------------------
ALTER TABLE "infra_data_source_config"
    ADD CONSTRAINT "infra_data_source_config_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table infra_file
-- ----------------------------
ALTER TABLE "infra_file"
    ADD CONSTRAINT "infra_file_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table infra_file_config
-- ----------------------------
ALTER TABLE "infra_file_config"
    ADD CONSTRAINT "infra_file_config_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table infra_file_content
-- ----------------------------
ALTER TABLE "infra_file_content"
    ADD CONSTRAINT "infra_file_content_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table infra_job
-- ----------------------------
ALTER TABLE "infra_job"
    ADD CONSTRAINT "infra_job_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table infra_job_log
-- ----------------------------
ALTER TABLE "infra_job_log"
    ADD CONSTRAINT "infra_job_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table infra_test_demo
-- ----------------------------
ALTER TABLE "infra_test_demo"
    ADD CONSTRAINT "infra_test_demo_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table member_user
-- ----------------------------
CREATE UNIQUE INDEX "uk_mobile" ON "member_user" USING btree (
    "mobile" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
COMMENT
ON INDEX "uk_mobile" IS '手机号';

-- ----------------------------
-- Primary Key structure for table member_user
-- ----------------------------
ALTER TABLE "member_user"
    ADD CONSTRAINT "member_user_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table pay_app
-- ----------------------------
ALTER TABLE "pay_app"
    ADD CONSTRAINT "pay_app_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table pay_channel
-- ----------------------------
ALTER TABLE "pay_channel"
    ADD CONSTRAINT "pay_channel_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table pay_merchant
-- ----------------------------
ALTER TABLE "pay_merchant"
    ADD CONSTRAINT "pay_merchant_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table pay_notify_log
-- ----------------------------
ALTER TABLE "pay_notify_log"
    ADD CONSTRAINT "pay_notify_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table pay_notify_task
-- ----------------------------
ALTER TABLE "pay_notify_task"
    ADD CONSTRAINT "pay_notify_task_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table pay_order
-- ----------------------------
ALTER TABLE "pay_order"
    ADD CONSTRAINT "pay_order_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table pay_order_extension
-- ----------------------------
ALTER TABLE "pay_order_extension"
    ADD CONSTRAINT "pay_order_extension_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table pay_refund
-- ----------------------------
ALTER TABLE "pay_refund"
    ADD CONSTRAINT "pay_refund_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table qrtz_blob_triggers
-- ----------------------------
ALTER TABLE "qrtz_blob_triggers"
    ADD CONSTRAINT "qrtz_blob_triggers_pkey" PRIMARY KEY ("sched_name", "trigger_name", "trigger_group");

-- ----------------------------
-- Primary Key structure for table qrtz_calendars
-- ----------------------------
ALTER TABLE "qrtz_calendars"
    ADD CONSTRAINT "qrtz_calendars_pkey" PRIMARY KEY ("sched_name", "calendar_name");

-- ----------------------------
-- Primary Key structure for table qrtz_cron_triggers
-- ----------------------------
ALTER TABLE "qrtz_cron_triggers"
    ADD CONSTRAINT "qrtz_cron_triggers_pkey" PRIMARY KEY ("sched_name", "trigger_name", "trigger_group");

-- ----------------------------
-- Indexes structure for table qrtz_fired_triggers
-- ----------------------------
CREATE INDEX "idx_qrtz_ft_inst_job_req_rcvry" ON "qrtz_fired_triggers" USING btree (
    "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "instance_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "requests_recovery" "pg_catalog"."bool_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_qrtz_ft_j_g" ON "qrtz_fired_triggers" USING btree (
    "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "job_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "job_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_qrtz_ft_jg" ON "qrtz_fired_triggers" USING btree (
    "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "job_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_qrtz_ft_t_g" ON "qrtz_fired_triggers" USING btree (
    "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "trigger_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "trigger_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_qrtz_ft_tg" ON "qrtz_fired_triggers" USING btree (
    "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "trigger_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_qrtz_ft_trig_inst_name" ON "qrtz_fired_triggers" USING btree (
    "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "instance_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );

-- ----------------------------
-- Primary Key structure for table qrtz_fired_triggers
-- ----------------------------
ALTER TABLE "qrtz_fired_triggers"
    ADD CONSTRAINT "qrtz_fired_triggers_pkey" PRIMARY KEY ("sched_name", "entry_id");

-- ----------------------------
-- Indexes structure for table qrtz_job_details
-- ----------------------------
CREATE INDEX "idx_qrtz_j_grp" ON "qrtz_job_details" USING btree (
    "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "job_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_qrtz_j_req_recovery" ON "qrtz_job_details" USING btree (
    "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "requests_recovery" "pg_catalog"."bool_ops" ASC NULLS LAST
    );

-- ----------------------------
-- Primary Key structure for table qrtz_job_details
-- ----------------------------
ALTER TABLE "qrtz_job_details"
    ADD CONSTRAINT "qrtz_job_details_pkey" PRIMARY KEY ("sched_name", "job_name", "job_group");

-- ----------------------------
-- Primary Key structure for table qrtz_locks
-- ----------------------------
ALTER TABLE "qrtz_locks"
    ADD CONSTRAINT "qrtz_locks_pkey" PRIMARY KEY ("sched_name", "lock_name");

-- ----------------------------
-- Primary Key structure for table qrtz_paused_trigger_grps
-- ----------------------------
ALTER TABLE "qrtz_paused_trigger_grps"
    ADD CONSTRAINT "qrtz_paused_trigger_grps_pkey" PRIMARY KEY ("sched_name", "trigger_group");

-- ----------------------------
-- Primary Key structure for table qrtz_scheduler_state
-- ----------------------------
ALTER TABLE "qrtz_scheduler_state"
    ADD CONSTRAINT "qrtz_scheduler_state_pkey" PRIMARY KEY ("sched_name", "instance_name");

-- ----------------------------
-- Primary Key structure for table qrtz_simple_triggers
-- ----------------------------
ALTER TABLE "qrtz_simple_triggers"
    ADD CONSTRAINT "qrtz_simple_triggers_pkey" PRIMARY KEY ("sched_name", "trigger_name", "trigger_group");

-- ----------------------------
-- Primary Key structure for table qrtz_simprop_triggers
-- ----------------------------
ALTER TABLE "qrtz_simprop_triggers"
    ADD CONSTRAINT "qrtz_simprop_triggers_pkey" PRIMARY KEY ("sched_name", "trigger_name", "trigger_group");

-- ----------------------------
-- Indexes structure for table qrtz_triggers
-- ----------------------------
CREATE INDEX "idx_qrtz_t_c" ON "qrtz_triggers" USING btree (
    "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "calendar_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_qrtz_t_g" ON "qrtz_triggers" USING btree (
    "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "trigger_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_qrtz_t_j" ON "qrtz_triggers" USING btree (
    "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "job_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "job_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_qrtz_t_jg" ON "qrtz_triggers" USING btree (
    "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "job_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_qrtz_t_n_g_state" ON "qrtz_triggers" USING btree (
    "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "trigger_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "trigger_state" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_qrtz_t_n_state" ON "qrtz_triggers" USING btree (
    "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "trigger_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "trigger_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "trigger_state" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_qrtz_t_next_fire_time" ON "qrtz_triggers" USING btree (
    "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "next_fire_time" "pg_catalog"."int8_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_qrtz_t_nft_misfire" ON "qrtz_triggers" USING btree (
    "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "misfire_instr" "pg_catalog"."int2_ops" ASC NULLS LAST,
    "next_fire_time" "pg_catalog"."int8_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_qrtz_t_nft_st" ON "qrtz_triggers" USING btree (
    "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "trigger_state" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "next_fire_time" "pg_catalog"."int8_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_qrtz_t_nft_st_misfire" ON "qrtz_triggers" USING btree (
    "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "misfire_instr" "pg_catalog"."int2_ops" ASC NULLS LAST,
    "next_fire_time" "pg_catalog"."int8_ops" ASC NULLS LAST,
    "trigger_state" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_qrtz_t_nft_st_misfire_grp" ON "qrtz_triggers" USING btree (
    "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "misfire_instr" "pg_catalog"."int2_ops" ASC NULLS LAST,
    "next_fire_time" "pg_catalog"."int8_ops" ASC NULLS LAST,
    "trigger_group" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "trigger_state" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
CREATE INDEX "idx_qrtz_t_state" ON "qrtz_triggers" USING btree (
    "sched_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "trigger_state" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );

-- ----------------------------
-- Primary Key structure for table qrtz_triggers
-- ----------------------------
ALTER TABLE "qrtz_triggers"
    ADD CONSTRAINT "qrtz_triggers_pkey" PRIMARY KEY ("sched_name", "trigger_name", "trigger_group");

-- ----------------------------
-- Primary Key structure for table system_dept
-- ----------------------------
ALTER TABLE "system_dept"
    ADD CONSTRAINT "system_dept_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_dict_data
-- ----------------------------
ALTER TABLE "system_dict_data"
    ADD CONSTRAINT "system_dict_data_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table system_dict_type
-- ----------------------------
CREATE UNIQUE INDEX "dict_type" ON "system_dict_type" USING btree (
    "type" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );

-- ----------------------------
-- Primary Key structure for table system_dict_type
-- ----------------------------
ALTER TABLE "system_dict_type"
    ADD CONSTRAINT "system_dict_type_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_error_code
-- ----------------------------
ALTER TABLE "system_error_code"
    ADD CONSTRAINT "system_error_code_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_login_log
-- ----------------------------
ALTER TABLE "system_login_log"
    ADD CONSTRAINT "system_login_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_menu
-- ----------------------------
ALTER TABLE "system_menu"
    ADD CONSTRAINT "system_menu_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_notice
-- ----------------------------
ALTER TABLE "system_notice"
    ADD CONSTRAINT "system_notice_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_oauth2_access_token
-- ----------------------------
ALTER TABLE "system_oauth2_access_token"
    ADD CONSTRAINT "system_oauth2_access_token_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_oauth2_approve
-- ----------------------------
ALTER TABLE "system_oauth2_approve"
    ADD CONSTRAINT "system_oauth2_approve_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_oauth2_client
-- ----------------------------
ALTER TABLE "system_oauth2_client"
    ADD CONSTRAINT "system_oauth2_client_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_oauth2_code
-- ----------------------------
ALTER TABLE "system_oauth2_code"
    ADD CONSTRAINT "system_oauth2_code_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_oauth2_refresh_token
-- ----------------------------
ALTER TABLE "system_oauth2_refresh_token"
    ADD CONSTRAINT "system_oauth2_refresh_token_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_operate_log
-- ----------------------------
ALTER TABLE "system_operate_log"
    ADD CONSTRAINT "system_operate_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_post
-- ----------------------------
ALTER TABLE "system_post"
    ADD CONSTRAINT "system_post_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_role
-- ----------------------------
ALTER TABLE "system_role"
    ADD CONSTRAINT "system_role_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_role_menu
-- ----------------------------
ALTER TABLE "system_role_menu"
    ADD CONSTRAINT "system_role_menu_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_sensitive_word
-- ----------------------------
ALTER TABLE "system_sensitive_word"
    ADD CONSTRAINT "system_sensitive_word_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_sms_channel
-- ----------------------------
ALTER TABLE "system_sms_channel"
    ADD CONSTRAINT "system_sms_channel_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table system_sms_code
-- ----------------------------
CREATE INDEX "idx_mobile" ON "system_sms_code" USING btree (
    "mobile" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );
COMMENT
ON INDEX "idx_mobile" IS '手机号';

-- ----------------------------
-- Primary Key structure for table system_sms_code
-- ----------------------------
ALTER TABLE "system_sms_code"
    ADD CONSTRAINT "system_sms_code_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_sms_log
-- ----------------------------
ALTER TABLE "system_sms_log"
    ADD CONSTRAINT "system_sms_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_sms_template
-- ----------------------------
ALTER TABLE "system_sms_template"
    ADD CONSTRAINT "system_sms_template_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_social_user
-- ----------------------------
ALTER TABLE "system_social_user"
    ADD CONSTRAINT "system_social_user_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_social_user_bind
-- ----------------------------
ALTER TABLE "system_social_user_bind"
    ADD CONSTRAINT "system_social_user_bind_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_tenant
-- ----------------------------
ALTER TABLE "system_tenant"
    ADD CONSTRAINT "system_tenant_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_tenant_package
-- ----------------------------
ALTER TABLE "system_tenant_package"
    ADD CONSTRAINT "system_tenant_package_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_user_post
-- ----------------------------
ALTER TABLE "system_user_post"
    ADD CONSTRAINT "system_user_post_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table system_user_role
-- ----------------------------
ALTER TABLE "system_user_role"
    ADD CONSTRAINT "system_user_role_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table system_users
-- ----------------------------
CREATE UNIQUE INDEX "idx_username" ON "system_users" USING btree (
    "username" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "update_time" "pg_catalog"."timestamp_ops" ASC NULLS LAST,
    "tenant_id" "pg_catalog"."int8_ops" ASC NULLS LAST
    );

-- ----------------------------
-- Primary Key structure for table system_users
-- ----------------------------
ALTER TABLE "system_users"
    ADD CONSTRAINT "system_user_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Foreign Keys structure for table qrtz_blob_triggers
-- ----------------------------
ALTER TABLE "qrtz_blob_triggers"
    ADD CONSTRAINT "qrtz_blob_triggers_sched_name_trigger_name_trigger_group_fkey" FOREIGN KEY ("sched_name", "trigger_name", "trigger_group") REFERENCES "qrtz_triggers" ("sched_name", "trigger_name", "trigger_group") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table qrtz_cron_triggers
-- ----------------------------
ALTER TABLE "qrtz_cron_triggers"
    ADD CONSTRAINT "qrtz_cron_triggers_sched_name_trigger_name_trigger_group_fkey" FOREIGN KEY ("sched_name", "trigger_name", "trigger_group") REFERENCES "qrtz_triggers" ("sched_name", "trigger_name", "trigger_group") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table qrtz_simple_triggers
-- ----------------------------
ALTER TABLE "qrtz_simple_triggers"
    ADD CONSTRAINT "qrtz_simple_triggers_sched_name_trigger_name_trigger_group_fkey" FOREIGN KEY ("sched_name", "trigger_name", "trigger_group") REFERENCES "qrtz_triggers" ("sched_name", "trigger_name", "trigger_group") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table qrtz_simprop_triggers
-- ----------------------------
ALTER TABLE "qrtz_simprop_triggers"
    ADD CONSTRAINT "qrtz_simprop_triggers_sched_name_trigger_name_trigger_grou_fkey" FOREIGN KEY ("sched_name", "trigger_name", "trigger_group") REFERENCES "qrtz_triggers" ("sched_name", "trigger_name", "trigger_group") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table qrtz_triggers
-- ----------------------------
ALTER TABLE "qrtz_triggers"
    ADD CONSTRAINT "qrtz_triggers_sched_name_job_name_job_group_fkey" FOREIGN KEY ("sched_name", "job_name", "job_group") REFERENCES "qrtz_job_details" ("sched_name", "job_name", "job_group") ON DELETE NO ACTION ON UPDATE NO ACTION;
