/*
 Yudao Database Transfer Tool

 Source Server Type    : MySQL
 Target Server Type    : PostgreSQL

 Module: BPM (Business Process Management)
 Date: 2025-11-13
*/

-- ----------------------------
-- Sequence structure for bpm_category
-- ----------------------------
DROP SEQUENCE IF EXISTS bpm_category_seq;
CREATE SEQUENCE bpm_category_seq
    START 1
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- ----------------------------
-- Table structure for bpm_category
-- ----------------------------
DROP TABLE IF EXISTS bpm_category;
CREATE TABLE bpm_category
(
    id          int8        NOT NULL,
    name        varchar(30) NULL     DEFAULT '',
    code        varchar(30) NULL     DEFAULT '',
    description varchar(255) NOT NULL DEFAULT '',
    status      int2        NULL     DEFAULT NULL,
    sort        int4        NULL     DEFAULT NULL,
    creator     varchar(64) NULL     DEFAULT '',
    create_time timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64) NULL     DEFAULT '',
    update_time timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     int2        NOT NULL DEFAULT 0,
    tenant_id   int8        NOT NULL DEFAULT 0
);

ALTER TABLE bpm_category
    ADD CONSTRAINT pk_bpm_category PRIMARY KEY (id);

-- Business indexes for bpm_category
CREATE INDEX idx_bpm_category_code ON bpm_category (code);
CREATE INDEX idx_bpm_category_status ON bpm_category (status);
CREATE INDEX idx_bpm_category_sort ON bpm_category (sort);

COMMENT ON COLUMN bpm_category.id IS '分类编号';
COMMENT ON COLUMN bpm_category.name IS '分类名';
COMMENT ON COLUMN bpm_category.code IS '分类标志';
COMMENT ON COLUMN bpm_category.description IS '分类描述';
COMMENT ON COLUMN bpm_category.status IS '分类状态';
COMMENT ON COLUMN bpm_category.sort IS '分类排序';
COMMENT ON COLUMN bpm_category.creator IS '创建者';
COMMENT ON COLUMN bpm_category.create_time IS '创建时间';
COMMENT ON COLUMN bpm_category.updater IS '更新者';
COMMENT ON COLUMN bpm_category.update_time IS '更新时间';
COMMENT ON COLUMN bpm_category.deleted IS '是否删除';
COMMENT ON COLUMN bpm_category.tenant_id IS '租户编号';
COMMENT ON TABLE bpm_category IS 'BPM 流程分类';

-- ----------------------------
-- Sequence structure for bpm_form
-- ----------------------------
DROP SEQUENCE IF EXISTS bpm_form_seq;
CREATE SEQUENCE bpm_form_seq
    START 1
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- ----------------------------
-- Table structure for bpm_form
-- ----------------------------
DROP TABLE IF EXISTS bpm_form;
CREATE TABLE bpm_form
(
    id          int8         NOT NULL,
    name        varchar(64)  NOT NULL,
    status      int2         NOT NULL,
    conf        varchar(1000) NOT NULL,
    fields      varchar(5000) NOT NULL,
    remark      varchar(255) NULL     DEFAULT NULL,
    creator     varchar(64)  NULL     DEFAULT '',
    create_time timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64)  NULL     DEFAULT '',
    update_time timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     int2         NOT NULL DEFAULT 0,
    tenant_id   int8         NOT NULL DEFAULT 0
);

ALTER TABLE bpm_form
    ADD CONSTRAINT pk_bpm_form PRIMARY KEY (id);

COMMENT ON COLUMN bpm_form.id IS '编号';
COMMENT ON COLUMN bpm_form.name IS '表单名';
COMMENT ON COLUMN bpm_form.status IS '开启状态';
COMMENT ON COLUMN bpm_form.conf IS '表单的配置';
COMMENT ON COLUMN bpm_form.fields IS '表单项的数组';
COMMENT ON COLUMN bpm_form.remark IS '备注';
COMMENT ON COLUMN bpm_form.creator IS '创建者';
COMMENT ON COLUMN bpm_form.create_time IS '创建时间';
COMMENT ON COLUMN bpm_form.updater IS '更新者';
COMMENT ON COLUMN bpm_form.update_time IS '更新时间';
COMMENT ON COLUMN bpm_form.deleted IS '是否删除';
COMMENT ON COLUMN bpm_form.tenant_id IS '租户编号';
COMMENT ON TABLE bpm_form IS 'BPM 表单定义表';

-- ----------------------------
-- Sequence structure for bpm_oa_leave
-- ----------------------------
DROP SEQUENCE IF EXISTS bpm_oa_leave_seq;
CREATE SEQUENCE bpm_oa_leave_seq
    START 1
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- ----------------------------
-- Table structure for bpm_oa_leave
-- ----------------------------
DROP TABLE IF EXISTS bpm_oa_leave;
CREATE TABLE bpm_oa_leave
(
    id                   int8        NOT NULL,
    user_id              int8        NOT NULL,
    type                 varchar(50) NOT NULL,
    reason               varchar(200) NOT NULL,
    start_time           timestamp   NOT NULL,
    end_time             timestamp   NOT NULL,
    day                  int8        NOT NULL,
    status               int2        NOT NULL,
    process_instance_id  varchar(64) NULL     DEFAULT NULL,
    creator              varchar(64) NULL     DEFAULT '',
    create_time          timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater              varchar(64) NULL     DEFAULT '',
    update_time          timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted              int2        NOT NULL DEFAULT 0,
    tenant_id            int8        NOT NULL DEFAULT 0
);

ALTER TABLE bpm_oa_leave
    ADD CONSTRAINT pk_bpm_oa_leave PRIMARY KEY (id);

COMMENT ON COLUMN bpm_oa_leave.id IS '请假表单主键';
COMMENT ON COLUMN bpm_oa_leave.user_id IS '申请人的用户编号';
COMMENT ON COLUMN bpm_oa_leave.type IS '请假类型';
COMMENT ON COLUMN bpm_oa_leave.reason IS '请假原因';
COMMENT ON COLUMN bpm_oa_leave.start_time IS '开始时间';
COMMENT ON COLUMN bpm_oa_leave.end_time IS '结束时间';
COMMENT ON COLUMN bpm_oa_leave.day IS '请假天数';
COMMENT ON COLUMN bpm_oa_leave.status IS '审批结果';
COMMENT ON COLUMN bpm_oa_leave.process_instance_id IS '流程实例的编号';
COMMENT ON COLUMN bpm_oa_leave.creator IS '创建者';
COMMENT ON COLUMN bpm_oa_leave.create_time IS '创建时间';
COMMENT ON COLUMN bpm_oa_leave.updater IS '更新者';
COMMENT ON COLUMN bpm_oa_leave.update_time IS '更新时间';
COMMENT ON COLUMN bpm_oa_leave.deleted IS '是否删除';
COMMENT ON COLUMN bpm_oa_leave.tenant_id IS '租户编号';
COMMENT ON TABLE bpm_oa_leave IS 'OA 请假申请表';

-- ----------------------------
-- Sequence structure for bpm_process_definition_info
-- ----------------------------
DROP SEQUENCE IF EXISTS bpm_process_definition_info_seq;
CREATE SEQUENCE bpm_process_definition_info_seq
    START 1
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- ----------------------------
-- Table structure for bpm_process_definition_info
-- ----------------------------
DROP TABLE IF EXISTS bpm_process_definition_info;
CREATE TABLE bpm_process_definition_info
(
    id                              int8          NOT NULL,
    process_definition_id           varchar(64)   NOT NULL,
    model_id                        varchar(64)   NOT NULL,
    model_type                      int2          NOT NULL DEFAULT 10,
    category                        varchar(64)   NOT NULL,
    icon                            varchar(512)  NULL     DEFAULT NULL,
    description                     varchar(255)  NULL     DEFAULT NULL,
    form_type                       int2          NOT NULL,
    form_id                         int8          NULL     DEFAULT NULL,
    form_conf                       varchar(1000) NULL     DEFAULT NULL,
    form_fields                     varchar(5000) NULL     DEFAULT NULL,
    form_custom_create_path         varchar(255)  NULL     DEFAULT NULL,
    form_custom_view_path           varchar(255)  NULL     DEFAULT NULL,
    simple_model                    text          NULL,
    sort                            int8          NULL     DEFAULT 0,
    visible                         int2          NOT NULL DEFAULT 1,
    start_user_ids                  varchar(256)  NULL     DEFAULT NULL,
    start_dept_ids                  varchar(256)  NULL     DEFAULT NULL,
    manager_user_ids                varchar(256)  NULL     DEFAULT NULL,
    allow_cancel_running_process    int2          NOT NULL DEFAULT 1,
    allow_withdraw_task             int2          NOT NULL DEFAULT 1,
    process_id_rule                 varchar(255)  NULL     DEFAULT NULL,
    auto_approval_type              int2          NOT NULL DEFAULT 0,
    title_setting                   varchar(512)  NULL     DEFAULT NULL,
    summary_setting                 varchar(512)  NULL     DEFAULT NULL,
    process_before_trigger_setting  varchar(1024) NULL     DEFAULT NULL,
    process_after_trigger_setting   varchar(1024) NULL     DEFAULT NULL,
    task_before_trigger_setting     varchar(1024) NULL     DEFAULT NULL,
    task_after_trigger_setting      varchar(1024) NULL     DEFAULT NULL,
    print_template_setting          varchar(512)  NULL     DEFAULT NULL,
    creator                         varchar(64)   NULL     DEFAULT '',
    create_time                     timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater                         varchar(64)   NULL     DEFAULT '',
    update_time                     timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted                         int2          NOT NULL DEFAULT 0,
    tenant_id                       int8          NOT NULL DEFAULT 0
);

ALTER TABLE bpm_process_definition_info
    ADD CONSTRAINT pk_bpm_process_definition_info PRIMARY KEY (id);

-- Business indexes for bpm_process_definition_info
CREATE UNIQUE INDEX idx_bpm_pdi_process_definition_id ON bpm_process_definition_info (process_definition_id);
CREATE INDEX idx_bpm_pdi_model_id ON bpm_process_definition_info (model_id);

COMMENT ON COLUMN bpm_process_definition_info.id IS '编号';
COMMENT ON COLUMN bpm_process_definition_info.process_definition_id IS '流程定义的编号';
COMMENT ON COLUMN bpm_process_definition_info.model_id IS '流程模型的编号';
COMMENT ON COLUMN bpm_process_definition_info.model_type IS '流程模型的类型';
COMMENT ON COLUMN bpm_process_definition_info.category IS '流程分类的编码';
COMMENT ON COLUMN bpm_process_definition_info.icon IS '图标';
COMMENT ON COLUMN bpm_process_definition_info.description IS '描述';
COMMENT ON COLUMN bpm_process_definition_info.form_type IS '表单类型';
COMMENT ON COLUMN bpm_process_definition_info.form_id IS '表单编号';
COMMENT ON COLUMN bpm_process_definition_info.form_conf IS '表单的配置';
COMMENT ON COLUMN bpm_process_definition_info.form_fields IS '表单项的数组';
COMMENT ON COLUMN bpm_process_definition_info.form_custom_create_path IS '自定义表单的提交路径';
COMMENT ON COLUMN bpm_process_definition_info.form_custom_view_path IS '自定义表单的查看路径';
COMMENT ON COLUMN bpm_process_definition_info.simple_model IS 'SIMPLE 设计器模型数据 JSON 格式';
COMMENT ON COLUMN bpm_process_definition_info.sort IS '排序值';
COMMENT ON COLUMN bpm_process_definition_info.visible IS '是否可见';
COMMENT ON COLUMN bpm_process_definition_info.start_user_ids IS '可发起用户编号数组';
COMMENT ON COLUMN bpm_process_definition_info.start_dept_ids IS '可发起部门编号数组';
COMMENT ON COLUMN bpm_process_definition_info.manager_user_ids IS '可管理用户编号数组';
COMMENT ON COLUMN bpm_process_definition_info.allow_cancel_running_process IS '是否允许撤销审批中的申请';
COMMENT ON COLUMN bpm_process_definition_info.allow_withdraw_task IS '是否允许审批人撤回任务';
COMMENT ON COLUMN bpm_process_definition_info.process_id_rule IS '流程 ID 规则';
COMMENT ON COLUMN bpm_process_definition_info.auto_approval_type IS '自动去重类型';
COMMENT ON COLUMN bpm_process_definition_info.title_setting IS '标题设置';
COMMENT ON COLUMN bpm_process_definition_info.summary_setting IS '摘要设置';
COMMENT ON COLUMN bpm_process_definition_info.process_before_trigger_setting IS '流程前置通知设置';
COMMENT ON COLUMN bpm_process_definition_info.process_after_trigger_setting IS '流程后置通知设置';
COMMENT ON COLUMN bpm_process_definition_info.task_before_trigger_setting IS '任务前置通知设置';
COMMENT ON COLUMN bpm_process_definition_info.task_after_trigger_setting IS '任务后置通知设置';
COMMENT ON COLUMN bpm_process_definition_info.print_template_setting IS '自定义打印模板设置';
COMMENT ON COLUMN bpm_process_definition_info.creator IS '创建者';
COMMENT ON COLUMN bpm_process_definition_info.create_time IS '创建时间';
COMMENT ON COLUMN bpm_process_definition_info.updater IS '更新者';
COMMENT ON COLUMN bpm_process_definition_info.update_time IS '更新时间';
COMMENT ON COLUMN bpm_process_definition_info.deleted IS '是否删除';
COMMENT ON COLUMN bpm_process_definition_info.tenant_id IS '租户编号';
COMMENT ON TABLE bpm_process_definition_info IS 'BPM 流程定义的信息表';

-- ----------------------------
-- Sequence structure for bpm_process_expression
-- ----------------------------
DROP SEQUENCE IF EXISTS bpm_process_expression_seq;
CREATE SEQUENCE bpm_process_expression_seq
    START 1
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- ----------------------------
-- Table structure for bpm_process_expression
-- ----------------------------
DROP TABLE IF EXISTS bpm_process_expression;
CREATE TABLE bpm_process_expression
(
    id          int8          NOT NULL,
    name        varchar(64)   NOT NULL DEFAULT '',
    status      int2          NOT NULL,
    expression  varchar(1024) NOT NULL,
    creator     varchar(64)   NULL     DEFAULT '',
    create_time timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64)   NULL     DEFAULT '',
    update_time timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     int2          NOT NULL DEFAULT 0,
    tenant_id   int8          NOT NULL DEFAULT 0
);

ALTER TABLE bpm_process_expression
    ADD CONSTRAINT pk_bpm_process_expression PRIMARY KEY (id);

COMMENT ON COLUMN bpm_process_expression.id IS '编号';
COMMENT ON COLUMN bpm_process_expression.name IS '表达式名字';
COMMENT ON COLUMN bpm_process_expression.status IS '表达式状态';
COMMENT ON COLUMN bpm_process_expression.expression IS '表达式';
COMMENT ON COLUMN bpm_process_expression.creator IS '创建者';
COMMENT ON COLUMN bpm_process_expression.create_time IS '创建时间';
COMMENT ON COLUMN bpm_process_expression.updater IS '更新者';
COMMENT ON COLUMN bpm_process_expression.update_time IS '更新时间';
COMMENT ON COLUMN bpm_process_expression.deleted IS '是否删除';
COMMENT ON COLUMN bpm_process_expression.tenant_id IS '租户编号';
COMMENT ON TABLE bpm_process_expression IS 'BPM 流程表达式表';

-- ----------------------------
-- Sequence structure for bpm_process_instance_copy
-- ----------------------------
DROP SEQUENCE IF EXISTS bpm_process_instance_copy_seq;
CREATE SEQUENCE bpm_process_instance_copy_seq
    START 1
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- ----------------------------
-- Table structure for bpm_process_instance_copy
-- ----------------------------
DROP TABLE IF EXISTS bpm_process_instance_copy;
CREATE TABLE bpm_process_instance_copy
(
    id                    int8        NOT NULL,
    user_id               int8        NOT NULL DEFAULT 0,
    start_user_id         int8        NOT NULL DEFAULT 0,
    process_instance_id   varchar(64) NOT NULL DEFAULT '',
    process_instance_name varchar(64) NOT NULL DEFAULT '',
    process_definition_id varchar(64) NOT NULL,
    category              varchar(64) NOT NULL,
    activity_id           varchar(64) NOT NULL DEFAULT '',
    activity_name         varchar(64) NOT NULL,
    task_id               varchar(64) NULL     DEFAULT '',
    reason                varchar(256) NULL     DEFAULT '',
    creator               varchar(64) NULL     DEFAULT '',
    create_time           timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater               varchar(64) NULL     DEFAULT '',
    update_time           timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted               int2        NOT NULL DEFAULT 0,
    tenant_id             int8        NOT NULL DEFAULT 0
);

ALTER TABLE bpm_process_instance_copy
    ADD CONSTRAINT pk_bpm_process_instance_copy PRIMARY KEY (id);

-- Business indexes for bpm_process_instance_copy
CREATE INDEX idx_bpm_pic_user_id ON bpm_process_instance_copy (user_id);
CREATE INDEX idx_bpm_pic_process_instance_id ON bpm_process_instance_copy (process_instance_id);

COMMENT ON COLUMN bpm_process_instance_copy.id IS '编号';
COMMENT ON COLUMN bpm_process_instance_copy.user_id IS '用户编号，被抄送人';
COMMENT ON COLUMN bpm_process_instance_copy.start_user_id IS '发起流程的用户编号';
COMMENT ON COLUMN bpm_process_instance_copy.process_instance_id IS '流程实例的编号';
COMMENT ON COLUMN bpm_process_instance_copy.process_instance_name IS '流程实例的名字';
COMMENT ON COLUMN bpm_process_instance_copy.process_definition_id IS '流程定义的编号';
COMMENT ON COLUMN bpm_process_instance_copy.category IS '流程定义的分类';
COMMENT ON COLUMN bpm_process_instance_copy.activity_id IS '流程活动的编号';
COMMENT ON COLUMN bpm_process_instance_copy.activity_name IS '流程活动的名字';
COMMENT ON COLUMN bpm_process_instance_copy.task_id IS '流程任务的编号';
COMMENT ON COLUMN bpm_process_instance_copy.reason IS '抄送意见';
COMMENT ON COLUMN bpm_process_instance_copy.creator IS '创建者';
COMMENT ON COLUMN bpm_process_instance_copy.create_time IS '创建时间';
COMMENT ON COLUMN bpm_process_instance_copy.updater IS '更新者';
COMMENT ON COLUMN bpm_process_instance_copy.update_time IS '更新时间';
COMMENT ON COLUMN bpm_process_instance_copy.deleted IS '是否删除';
COMMENT ON COLUMN bpm_process_instance_copy.tenant_id IS '租户编号';
COMMENT ON TABLE bpm_process_instance_copy IS 'BPM 流程实例抄送表';

-- ----------------------------
-- Sequence structure for bpm_process_listener
-- ----------------------------
DROP SEQUENCE IF EXISTS bpm_process_listener_seq;
CREATE SEQUENCE bpm_process_listener_seq
    START 1
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- ----------------------------
-- Table structure for bpm_process_listener
-- ----------------------------
DROP TABLE IF EXISTS bpm_process_listener;
CREATE TABLE bpm_process_listener
(
    id          int8          NOT NULL,
    name        varchar(30)   NOT NULL DEFAULT '',
    type        varchar(255)  NOT NULL,
    status      int2          NOT NULL,
    event       varchar(30)   NOT NULL DEFAULT '',
    value_type  varchar(64)   NOT NULL DEFAULT '',
    value       varchar(1024) NOT NULL,
    creator     varchar(64)   NULL     DEFAULT '',
    create_time timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64)   NULL     DEFAULT '',
    update_time timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     int2          NOT NULL DEFAULT 0,
    tenant_id   int8          NOT NULL DEFAULT 0
);

ALTER TABLE bpm_process_listener
    ADD CONSTRAINT pk_bpm_process_listener PRIMARY KEY (id);

COMMENT ON COLUMN bpm_process_listener.id IS '编号';
COMMENT ON COLUMN bpm_process_listener.name IS '监听器名字';
COMMENT ON COLUMN bpm_process_listener.type IS '监听器类型';
COMMENT ON COLUMN bpm_process_listener.status IS '监听器状态';
COMMENT ON COLUMN bpm_process_listener.event IS '监听事件';
COMMENT ON COLUMN bpm_process_listener.value_type IS '监听器值类型';
COMMENT ON COLUMN bpm_process_listener.value IS '监听器值';
COMMENT ON COLUMN bpm_process_listener.creator IS '创建者';
COMMENT ON COLUMN bpm_process_listener.create_time IS '创建时间';
COMMENT ON COLUMN bpm_process_listener.updater IS '更新者';
COMMENT ON COLUMN bpm_process_listener.update_time IS '更新时间';
COMMENT ON COLUMN bpm_process_listener.deleted IS '是否删除';
COMMENT ON COLUMN bpm_process_listener.tenant_id IS '租户编号';
COMMENT ON TABLE bpm_process_listener IS 'BPM 流程监听器表';

-- ----------------------------
-- Sequence structure for bpm_user_group
-- ----------------------------
DROP SEQUENCE IF EXISTS bpm_user_group_seq;
CREATE SEQUENCE bpm_user_group_seq
    START 1
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

-- ----------------------------
-- Table structure for bpm_user_group
-- ----------------------------
DROP TABLE IF EXISTS bpm_user_group;
CREATE TABLE bpm_user_group
(
    id          int8          NOT NULL,
    name        varchar(30)   NOT NULL DEFAULT '',
    description varchar(255)  NOT NULL DEFAULT '',
    user_ids    varchar(1024) NULL     DEFAULT NULL,
    status      int2          NOT NULL,
    creator     varchar(64)   NULL     DEFAULT '',
    create_time timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64)   NULL     DEFAULT '',
    update_time timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     int2          NOT NULL DEFAULT 0,
    tenant_id   int8          NOT NULL DEFAULT 0
);

ALTER TABLE bpm_user_group
    ADD CONSTRAINT pk_bpm_user_group PRIMARY KEY (id);

COMMENT ON COLUMN bpm_user_group.id IS '编号';
COMMENT ON COLUMN bpm_user_group.name IS '组名';
COMMENT ON COLUMN bpm_user_group.description IS '描述';
COMMENT ON COLUMN bpm_user_group.user_ids IS '成员编号数组';
COMMENT ON COLUMN bpm_user_group.status IS '状态（0正常 1停用）';
COMMENT ON COLUMN bpm_user_group.creator IS '创建者';
COMMENT ON COLUMN bpm_user_group.create_time IS '创建时间';
COMMENT ON COLUMN bpm_user_group.updater IS '更新者';
COMMENT ON COLUMN bpm_user_group.update_time IS '更新时间';
COMMENT ON COLUMN bpm_user_group.deleted IS '是否删除';
COMMENT ON COLUMN bpm_user_group.tenant_id IS '租户编号';
COMMENT ON TABLE bpm_user_group IS 'BPM 用户组表';