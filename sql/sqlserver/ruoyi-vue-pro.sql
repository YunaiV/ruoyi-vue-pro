/*
 Yudao Database Transfer Tool

 Source Server Type    : MySQL

 Target Server Type    : Microsoft SQL Server

 Date: 2024-05-08 00:22:09
 Date: 2024-05-10 22:07:48
*/


-- ----------------------------
-- Table structure for dual
-- ----------------------------
DROP TABLE IF EXISTS dual
GO
CREATE TABLE dual
(
    id int
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'数据库连接的表',
     'SCHEMA', N'dbo',
     'TABLE', N'dual'
GO

-- ----------------------------
-- Records of dual
-- ----------------------------
-- @formatter:off
INSERT INTO dual VALUES (1)
GO
-- @formatter:on

-- ----------------------------
-- Table structure for infra_api_access_log
-- ----------------------------
DROP TABLE IF EXISTS infra_api_access_log
GO
CREATE TABLE infra_api_access_log
(
    id               bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    trace_id         nvarchar(64)  DEFAULT ''                NOT NULL,
    user_id          bigint        DEFAULT 0                 NOT NULL,
    user_type        tinyint       DEFAULT 0                 NOT NULL,
    application_name nvarchar(50)                            NOT NULL,
    request_method   nvarchar(16)  DEFAULT ''                NOT NULL,
    request_url      nvarchar(255) DEFAULT ''                NOT NULL,
    request_params   nvarchar(max)                           NULL,
    response_body    nvarchar(max)                           NULL,
    user_ip          nvarchar(50)                            NOT NULL,
    user_agent       nvarchar(512)                           NOT NULL,
    operate_module   nvarchar(50)  DEFAULT NULL              NULL,
    operate_name     nvarchar(50)  DEFAULT NULL              NULL,
    operate_type     tinyint       DEFAULT 0                 NULL,
    begin_time       datetime2                               NOT NULL,
    end_time         datetime2                               NOT NULL,
    duration         int                                     NOT NULL,
    result_code      int           DEFAULT 0                 NOT NULL,
    result_msg       nvarchar(512) DEFAULT ''                NULL,
    creator          nvarchar(64)  DEFAULT ''                NULL,
    create_time      datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater          nvarchar(64)  DEFAULT ''                NULL,
    update_time      datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted          bit           DEFAULT 0                 NOT NULL,
    tenant_id        bigint        DEFAULT 0                 NOT NULL
)
GO

CREATE INDEX idx_infra_api_access_log_01 ON infra_api_access_log (create_time)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'日志主键',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'链路追踪编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'trace_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'user_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户类型',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'user_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'应用名',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'application_name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'请求方法名',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'request_method'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'请求地址',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'request_url'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'请求参数',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'request_params'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'响应结果',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'response_body'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户 IP',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'user_ip'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'浏览器 UA',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'user_agent'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'操作模块',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'operate_module'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'操作名',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'operate_name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'操作分类',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'operate_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'开始请求时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'begin_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'结束请求时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'end_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'执行时长',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'duration'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'结果码',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'result_code'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'结果提示',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'result_msg'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'API 访问日志表',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_access_log'
GO

-- ----------------------------
-- Table structure for infra_api_error_log
-- ----------------------------
DROP TABLE IF EXISTS infra_api_error_log
GO
CREATE TABLE infra_api_error_log
(
    id                           bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    trace_id                     nvarchar(64)                            NOT NULL,
    user_id                      int           DEFAULT 0                 NOT NULL,
    user_type                    tinyint       DEFAULT 0                 NOT NULL,
    application_name             nvarchar(50)                            NOT NULL,
    request_method               nvarchar(16)                            NOT NULL,
    request_url                  nvarchar(255)                           NOT NULL,
    request_params               nvarchar(4000)                          NOT NULL,
    user_ip                      nvarchar(50)                            NOT NULL,
    user_agent                   nvarchar(512)                           NOT NULL,
    exception_time               datetime2                               NOT NULL,
    exception_name               nvarchar(128) DEFAULT ''                NOT NULL,
    exception_message            nvarchar(max)                           NOT NULL,
    exception_root_cause_message nvarchar(max)                           NOT NULL,
    exception_stack_trace        nvarchar(max)                           NOT NULL,
    exception_class_name         nvarchar(512)                           NOT NULL,
    exception_file_name          nvarchar(512)                           NOT NULL,
    exception_method_name        nvarchar(512)                           NOT NULL,
    exception_line_number        int                                     NOT NULL,
    process_status               tinyint                                 NOT NULL,
    process_time                 datetime2     DEFAULT NULL              NULL,
    process_user_id              int           DEFAULT 0                 NULL,
    creator                      nvarchar(64)  DEFAULT ''                NULL,
    create_time                  datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater                      nvarchar(64)  DEFAULT ''                NULL,
    update_time                  datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted                      bit           DEFAULT 0                 NOT NULL,
    tenant_id                    bigint        DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'链路追踪编号
     *
     * 一般来说，通过链路追踪编号，可以将访问日志，错误日志，链路追踪日志，logger 打印日志等，结合在一起，从而进行排错。',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'trace_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'user_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户类型',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'user_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'应用名
     *
     * 目前读取 spring.application.name',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'application_name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'请求方法名',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'request_method'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'请求地址',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'request_url'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'请求参数',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'request_params'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户 IP',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'user_ip'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'浏览器 UA',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'user_agent'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'异常发生时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'exception_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'异常名
     *
     * {@link Throwable#getClass()} 的类全名',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'exception_name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'异常导致的消息
     *
     * {@link cn.iocoder.common.framework.util.ExceptionUtil#getMessage(Throwable)}',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'exception_message'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'异常导致的根消息
     *
     * {@link cn.iocoder.common.framework.util.ExceptionUtil#getRootCauseMessage(Throwable)}',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'exception_root_cause_message'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'异常的栈轨迹
     *
     * {@link cn.iocoder.common.framework.util.ExceptionUtil#getServiceException(Exception)}',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'exception_stack_trace'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'异常发生的类全名
     *
     * {@link StackTraceElement#getClassName()}',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'exception_class_name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'异常发生的类文件
     *
     * {@link StackTraceElement#getFileName()}',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'exception_file_name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'异常发生的方法名
     *
     * {@link StackTraceElement#getMethodName()}',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'exception_method_name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'异常发生的方法所在行
     *
     * {@link StackTraceElement#getLineNumber()}',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'exception_line_number'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'处理状态',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'process_status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'处理时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'process_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'处理用户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'process_user_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'系统异常日志',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_api_error_log'
GO

-- ----------------------------
-- Table structure for infra_codegen_column
-- ----------------------------
DROP TABLE IF EXISTS infra_codegen_column
GO
CREATE TABLE infra_codegen_column
(
    id                       bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    table_id                 bigint                                  NOT NULL,
    column_name              nvarchar(200)                           NOT NULL,
    data_type                nvarchar(100)                           NOT NULL,
    column_comment           nvarchar(500)                           NOT NULL,
    nullable                 varchar(1)                              NOT NULL,
    primary_key              varchar(1)                              NOT NULL,
    ordinal_position         int                                     NOT NULL,
    java_type                nvarchar(32)                            NOT NULL,
    java_field               nvarchar(64)                            NOT NULL,
    dict_type                nvarchar(200) DEFAULT ''                NULL,
    example                  nvarchar(64)  DEFAULT NULL              NULL,
    create_operation         varchar(1)                              NOT NULL,
    update_operation         varchar(1)                              NOT NULL,
    list_operation           varchar(1)                              NOT NULL,
    list_operation_condition nvarchar(32)  DEFAULT '='               NOT NULL,
    list_operation_result    varchar(1)                              NOT NULL,
    html_type                nvarchar(32)                            NOT NULL,
    creator                  nvarchar(64)  DEFAULT ''                NULL,
    create_time              datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater                  nvarchar(64)  DEFAULT ''                NULL,
    update_time              datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted                  bit           DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'表编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'table_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'字段名',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'column_name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'字段类型',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'data_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'字段描述',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'column_comment'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否允许为空',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'nullable'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否主键',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'primary_key'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'排序',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'ordinal_position'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'Java 属性类型',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'java_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'Java 属性名',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'java_field'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'字典类型',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'dict_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'数据示例',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'example'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否为 Create 创建操作的字段',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'create_operation'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否为 Update 更新操作的字段',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'update_operation'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否为 List 查询操作的字段',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'list_operation'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'List 查询操作的条件类型',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'list_operation_condition'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否为 List 查询操作的返回字段',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'list_operation_result'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'显示类型',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'html_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'代码生成表字段定义',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_column'
GO

-- ----------------------------
-- Table structure for infra_codegen_table
-- ----------------------------
DROP TABLE IF EXISTS infra_codegen_table
GO
CREATE TABLE infra_codegen_table
(
    id                    bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    data_source_config_id bigint                                  NOT NULL,
    scene                 tinyint       DEFAULT 1                 NOT NULL,
    table_name            nvarchar(200) DEFAULT ''                NOT NULL,
    table_comment         nvarchar(500) DEFAULT ''                NOT NULL,
    remark                nvarchar(500) DEFAULT NULL              NULL,
    module_name           nvarchar(30)                            NOT NULL,
    business_name         nvarchar(30)                            NOT NULL,
    class_name            nvarchar(100) DEFAULT ''                NOT NULL,
    class_comment         nvarchar(50)                            NOT NULL,
    author                nvarchar(50)                            NOT NULL,
    template_type         tinyint       DEFAULT 1                 NOT NULL,
    front_type            tinyint                                 NOT NULL,
    parent_menu_id        bigint        DEFAULT NULL              NULL,
    master_table_id       bigint        DEFAULT NULL              NULL,
    sub_join_column_id    bigint        DEFAULT NULL              NULL,
    sub_join_many         varchar(1)    DEFAULT NULL              NULL,
    tree_parent_column_id bigint        DEFAULT NULL              NULL,
    tree_name_column_id   bigint        DEFAULT NULL              NULL,
    creator               nvarchar(64)  DEFAULT ''                NULL,
    create_time           datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater               nvarchar(64)  DEFAULT ''                NULL,
    update_time           datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted               bit           DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'数据源配置的编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'data_source_config_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'生成场景',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'scene'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'表名称',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'table_name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'表描述',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'table_comment'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'备注',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模块名',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'module_name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'业务名',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'business_name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'类名称',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'class_name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'类描述',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'class_comment'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'作者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'author'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模板类型',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'template_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'前端类型',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'front_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'父菜单编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'parent_menu_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'主表的编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'master_table_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'子表关联主表的字段编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'sub_join_column_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'主表与子表是否一对多',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'sub_join_many'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'树表的父字段编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'tree_parent_column_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'树表的名字字段编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'tree_name_column_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'代码生成表定义',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_codegen_table'
GO

-- ----------------------------
-- Table structure for infra_config
-- ----------------------------
DROP TABLE IF EXISTS infra_config
GO
CREATE TABLE infra_config
(
    id          bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    category    nvarchar(50)                            NOT NULL,
    type        tinyint                                 NOT NULL,
    name        nvarchar(100) DEFAULT ''                NOT NULL,
    config_key  nvarchar(100) DEFAULT ''                NOT NULL,
    value       nvarchar(500) DEFAULT ''                NOT NULL,
    visible     varchar(1)                              NOT NULL,
    remark      nvarchar(500) DEFAULT NULL              NULL,
    creator     nvarchar(64)  DEFAULT ''                NULL,
    create_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64)  DEFAULT ''                NULL,
    update_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit           DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'参数主键',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_config',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'参数分组',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_config',
     'COLUMN', N'category'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'参数类型',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_config',
     'COLUMN', N'type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'参数名称',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_config',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'参数键名',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_config',
     'COLUMN', N'config_key'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'参数键值',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_config',
     'COLUMN', N'value'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否可见',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_config',
     'COLUMN', N'visible'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'备注',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_config',
     'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_config',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_config',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_config',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_config',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_config',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'参数配置表',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_config'
GO

-- ----------------------------
-- Records of infra_config
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT infra_config ON
GO
INSERT INTO infra_config (id, category, type, name, config_key, value, visible, remark, creator, create_time, updater, update_time, deleted) VALUES (2, N'biz', 1, N'用户管理-账号初始密码', N'sys.user.init-password', N'123456', N'0', N'初始化密码 123456', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-04-03 17:22:28', N'0')
GO
INSERT INTO infra_config (id, category, type, name, config_key, value, visible, remark, creator, create_time, updater, update_time, deleted) VALUES (7, N'url', 2, N'MySQL 监控的地址', N'url.druid', N'', N'1', N'', N'1', N'2023-04-07 13:41:16', N'1', N'2023-04-07 14:33:38', N'0')
GO
INSERT INTO infra_config (id, category, type, name, config_key, value, visible, remark, creator, create_time, updater, update_time, deleted) VALUES (8, N'url', 2, N'SkyWalking 监控的地址', N'url.skywalking', N'', N'1', N'', N'1', N'2023-04-07 13:41:16', N'1', N'2023-04-07 14:57:03', N'0')
GO
INSERT INTO infra_config (id, category, type, name, config_key, value, visible, remark, creator, create_time, updater, update_time, deleted) VALUES (9, N'url', 2, N'Spring Boot Admin 监控的地址', N'url.spring-boot-admin', N'', N'1', N'', N'1', N'2023-04-07 13:41:16', N'1', N'2023-04-07 14:52:07', N'0')
GO
INSERT INTO infra_config (id, category, type, name, config_key, value, visible, remark, creator, create_time, updater, update_time, deleted) VALUES (10, N'url', 2, N'Swagger 接口文档的地址', N'url.swagger', N'', N'1', N'', N'1', N'2023-04-07 13:41:16', N'1', N'2023-04-07 14:59:00', N'0')
GO
INSERT INTO infra_config (id, category, type, name, config_key, value, visible, remark, creator, create_time, updater, update_time, deleted) VALUES (11, N'ui', 2, N'腾讯地图 key', N'tencent.lbs.key', N'TVDBZ-TDILD-4ON4B-PFDZA-RNLKH-VVF6E', N'1', N'腾讯地图 key', N'1', N'2023-06-03 19:16:27', N'1', N'2023-06-03 19:16:27', N'0')
GO
INSERT INTO infra_config (id, category, type, name, config_key, value, visible, remark, creator, create_time, updater, update_time, deleted) VALUES (12, N'test2', 2, N'test3', N'test4', N'test5', N'1', N'test6', N'1', N'2023-12-03 09:55:16', N'1', N'2023-12-03 09:55:27', N'0')
GO
SET IDENTITY_INSERT infra_config OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for infra_data_source_config
-- ----------------------------
DROP TABLE IF EXISTS infra_data_source_config
GO
CREATE TABLE infra_data_source_config
(
    id          bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    name        nvarchar(100) DEFAULT ''                NOT NULL,
    url         nvarchar(1024)                          NOT NULL,
    username    nvarchar(255)                           NOT NULL,
    password    nvarchar(255) DEFAULT ''                NOT NULL,
    creator     nvarchar(64)  DEFAULT ''                NULL,
    create_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64)  DEFAULT ''                NULL,
    update_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit           DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'主键编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_data_source_config',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'参数名称',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_data_source_config',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'数据源连接',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_data_source_config',
     'COLUMN', N'url'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户名',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_data_source_config',
     'COLUMN', N'username'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'密码',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_data_source_config',
     'COLUMN', N'password'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_data_source_config',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_data_source_config',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_data_source_config',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_data_source_config',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_data_source_config',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'数据源配置表',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_data_source_config'
GO

-- ----------------------------
-- Table structure for infra_file
-- ----------------------------
DROP TABLE IF EXISTS infra_file
GO
CREATE TABLE infra_file
(
    id          bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    config_id   bigint        DEFAULT NULL              NULL,
    name        nvarchar(256) DEFAULT NULL              NULL,
    path        nvarchar(512)                           NOT NULL,
    url         nvarchar(1024)                          NOT NULL,
    type        nvarchar(128) DEFAULT NULL              NULL,
    size        int                                     NOT NULL,
    creator     nvarchar(64)  DEFAULT ''                NULL,
    create_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64)  DEFAULT ''                NULL,
    update_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit           DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'文件编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'配置编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file',
     'COLUMN', N'config_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'文件名',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'文件路径',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file',
     'COLUMN', N'path'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'文件 URL',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file',
     'COLUMN', N'url'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'文件类型',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file',
     'COLUMN', N'type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'文件大小',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file',
     'COLUMN', N'size'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'文件表',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file'
GO

-- ----------------------------
-- Table structure for infra_file_config
-- ----------------------------
DROP TABLE IF EXISTS infra_file_config
GO
CREATE TABLE infra_file_config
(
    id          bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    name        nvarchar(63)                            NOT NULL,
    storage     tinyint                                 NOT NULL,
    remark      nvarchar(255) DEFAULT NULL              NULL,
    master      varchar(1)                              NOT NULL,
    config      nvarchar(4000)                          NOT NULL,
    creator     nvarchar(64)  DEFAULT ''                NULL,
    create_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64)  DEFAULT ''                NULL,
    update_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit           DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_config',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'配置名',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_config',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'存储器',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_config',
     'COLUMN', N'storage'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'备注',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_config',
     'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否为主配置',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_config',
     'COLUMN', N'master'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'存储配置',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_config',
     'COLUMN', N'config'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_config',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_config',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_config',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_config',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_config',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'文件配置表',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_config'
GO

-- ----------------------------
-- Records of infra_file_config
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT infra_file_config ON
GO
INSERT INTO infra_file_config (id, name, storage, remark, master, config, creator, create_time, updater, update_time, deleted) VALUES (4, N'数据库', 1, N'我是数据库', N'0', N'{"@class":"cn.iocoder.yudao.module.infra.framework.file.core.client.db.DBFileClientConfig","domain":"http://127.0.0.1:48080"}', N'1', N'2022-03-15 23:56:24', N'1', N'2024-02-28 22:54:07', N'0')
GO
INSERT INTO infra_file_config (id, name, storage, remark, master, config, creator, create_time, updater, update_time, deleted) VALUES (22, N'七牛存储器', 20, N'', N'1', N'{"@class":"cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClientConfig","endpoint":"s3.cn-south-1.qiniucs.com","domain":"http://test.yudao.iocoder.cn","bucket":"ruoyi-vue-pro","accessKey":"3TvrJ70gl2Gt6IBe7_IZT1F6i_k0iMuRtyEv4EyS","accessSecret":"wd0tbVBYlp0S-ihA8Qg2hPLncoP83wyrIq24OZuY"}', N'1', N'2024-01-13 22:11:12', N'1', N'2024-04-03 19:38:34', N'0')
GO
SET IDENTITY_INSERT infra_file_config OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for infra_file_content
-- ----------------------------
DROP TABLE IF EXISTS infra_file_content
GO
CREATE TABLE infra_file_content
(
    id          bigint                                 NOT NULL PRIMARY KEY IDENTITY,
    config_id   bigint                                 NOT NULL,
    path        nvarchar(512)                          NOT NULL,
    content     varbinary(max)                         NOT NULL,
    creator     nvarchar(64) DEFAULT ''                NULL,
    create_time datetime2    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64) DEFAULT ''                NULL,
    update_time datetime2    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit          DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_content',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'配置编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_content',
     'COLUMN', N'config_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'文件路径',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_content',
     'COLUMN', N'path'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'文件内容',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_content',
     'COLUMN', N'content'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_content',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_content',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_content',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_content',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_content',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'文件表',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_file_content'
GO

-- ----------------------------
-- Table structure for infra_job
-- ----------------------------
DROP TABLE IF EXISTS infra_job
GO
CREATE TABLE infra_job
(
    id              bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    name            nvarchar(32)                            NOT NULL,
    status          tinyint                                 NOT NULL,
    handler_name    nvarchar(64)                            NOT NULL,
    handler_param   nvarchar(255) DEFAULT NULL              NULL,
    cron_expression nvarchar(32)                            NOT NULL,
    retry_count     int           DEFAULT 0                 NOT NULL,
    retry_interval  int           DEFAULT 0                 NOT NULL,
    monitor_timeout int           DEFAULT 0                 NOT NULL,
    creator         nvarchar(64)  DEFAULT ''                NULL,
    create_time     datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater         nvarchar(64)  DEFAULT ''                NULL,
    update_time     datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted         bit           DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'任务编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'任务名称',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'任务状态',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job',
     'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'处理器的名字',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job',
     'COLUMN', N'handler_name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'处理器的参数',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job',
     'COLUMN', N'handler_param'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'CRON 表达式',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job',
     'COLUMN', N'cron_expression'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'重试次数',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job',
     'COLUMN', N'retry_count'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'重试间隔',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job',
     'COLUMN', N'retry_interval'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'监控超时时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job',
     'COLUMN', N'monitor_timeout'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'定时任务表',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job'
GO

-- ----------------------------
-- Records of infra_job
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT infra_job ON
GO
INSERT INTO infra_job (id, name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval, monitor_timeout, creator, create_time, updater, update_time, deleted) VALUES (5, N'支付通知 Job', 2, N'payNotifyJob', NULL, N'* * * * * ?', 0, 0, 0, N'1', N'2021-10-27 08:34:42', N'1', N'2023-07-09 20:51:41', N'0')
GO
INSERT INTO infra_job (id, name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval, monitor_timeout, creator, create_time, updater, update_time, deleted) VALUES (17, N'支付订单同步 Job', 2, N'payOrderSyncJob', NULL, N'0 0/1 * * * ?', 0, 0, 0, N'1', N'2023-07-22 14:36:26', N'1', N'2023-07-22 15:39:08', N'0')
GO
INSERT INTO infra_job (id, name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval, monitor_timeout, creator, create_time, updater, update_time, deleted) VALUES (18, N'支付订单过期 Job', 2, N'payOrderExpireJob', NULL, N'0 0/1 * * * ?', 0, 0, 0, N'1', N'2023-07-22 15:36:23', N'1', N'2023-07-22 15:39:54', N'0')
GO
INSERT INTO infra_job (id, name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval, monitor_timeout, creator, create_time, updater, update_time, deleted) VALUES (19, N'退款订单的同步 Job', 2, N'payRefundSyncJob', NULL, N'0 0/1 * * * ?', 0, 0, 0, N'1', N'2023-07-23 21:03:44', N'1', N'2023-07-23 21:09:00', N'0')
GO
INSERT INTO infra_job (id, name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval, monitor_timeout, creator, create_time, updater, update_time, deleted) VALUES (21, N'交易订单的自动过期 Job', 2, N'tradeOrderAutoCancelJob', N'', N'0 * * * * ?', 3, 0, 0, N'1', N'2023-09-25 23:43:26', N'1', N'2023-09-26 19:23:30', N'0')
GO
INSERT INTO infra_job (id, name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval, monitor_timeout, creator, create_time, updater, update_time, deleted) VALUES (22, N'交易订单的自动收货 Job', 2, N'tradeOrderAutoReceiveJob', N'', N'0 * * * * ?', 3, 0, 0, N'1', N'2023-09-26 19:23:53', N'1', N'2023-09-26 23:38:08', N'0')
GO
INSERT INTO infra_job (id, name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval, monitor_timeout, creator, create_time, updater, update_time, deleted) VALUES (23, N'交易订单的自动评论 Job', 2, N'tradeOrderAutoCommentJob', N'', N'0 * * * * ?', 3, 0, 0, N'1', N'2023-09-26 23:38:29', N'1', N'2023-09-27 11:03:10', N'0')
GO
INSERT INTO infra_job (id, name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval, monitor_timeout, creator, create_time, updater, update_time, deleted) VALUES (24, N'佣金解冻 Job', 2, N'brokerageRecordUnfreezeJob', N'', N'0 * * * * ?', 3, 0, 0, N'1', N'2023-09-28 22:01:46', N'1', N'2023-09-28 22:01:56', N'0')
GO
INSERT INTO infra_job (id, name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval, monitor_timeout, creator, create_time, updater, update_time, deleted) VALUES (25, N'访问日志清理 Job', 2, N'accessLogCleanJob', N'', N'0 0 0 * * ?', 3, 0, 0, N'1', N'2023-10-03 10:59:41', N'1', N'2023-10-03 11:01:10', N'0')
GO
INSERT INTO infra_job (id, name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval, monitor_timeout, creator, create_time, updater, update_time, deleted) VALUES (26, N'错误日志清理 Job', 2, N'errorLogCleanJob', N'', N'0 0 0 * * ?', 3, 0, 0, N'1', N'2023-10-03 11:00:43', N'1', N'2023-10-03 11:01:12', N'0')
GO
INSERT INTO infra_job (id, name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval, monitor_timeout, creator, create_time, updater, update_time, deleted) VALUES (27, N'任务日志清理 Job', 2, N'jobLogCleanJob', N'', N'0 0 0 * * ?', 3, 0, 0, N'1', N'2023-10-03 11:01:33', N'1', N'2023-10-03 11:01:42', N'0')
GO
SET IDENTITY_INSERT infra_job OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for infra_job_log
-- ----------------------------
DROP TABLE IF EXISTS infra_job_log
GO
CREATE TABLE infra_job_log
(
    id            bigint                                   NOT NULL PRIMARY KEY IDENTITY,
    job_id        bigint                                   NOT NULL,
    handler_name  nvarchar(64)                             NOT NULL,
    handler_param nvarchar(255)  DEFAULT NULL              NULL,
    execute_index tinyint        DEFAULT 1                 NOT NULL,
    begin_time    datetime2                                NOT NULL,
    end_time      datetime2      DEFAULT NULL              NULL,
    duration      int            DEFAULT NULL              NULL,
    status        tinyint                                  NOT NULL,
    result        nvarchar(4000) DEFAULT ''                NULL,
    creator       nvarchar(64)   DEFAULT ''                NULL,
    create_time   datetime2      DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater       nvarchar(64)   DEFAULT ''                NULL,
    update_time   datetime2      DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted       bit            DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'日志编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job_log',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'任务编号',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job_log',
     'COLUMN', N'job_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'处理器的名字',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job_log',
     'COLUMN', N'handler_name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'处理器的参数',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job_log',
     'COLUMN', N'handler_param'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'第几次执行',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job_log',
     'COLUMN', N'execute_index'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'开始执行时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job_log',
     'COLUMN', N'begin_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'结束执行时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job_log',
     'COLUMN', N'end_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'执行时长',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job_log',
     'COLUMN', N'duration'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'任务状态',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job_log',
     'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'结果数据',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job_log',
     'COLUMN', N'result'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job_log',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job_log',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job_log',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job_log',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job_log',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'定时任务日志表',
     'SCHEMA', N'dbo',
     'TABLE', N'infra_job_log'
GO

-- ----------------------------
-- Table structure for system_dept
-- ----------------------------
DROP TABLE IF EXISTS system_dept
GO
CREATE TABLE system_dept
(
    id             bigint                                 NOT NULL PRIMARY KEY IDENTITY,
    name           nvarchar(30) DEFAULT ''                NOT NULL,
    parent_id      bigint       DEFAULT 0                 NOT NULL,
    sort           int          DEFAULT 0                 NOT NULL,
    leader_user_id bigint       DEFAULT NULL              NULL,
    phone          nvarchar(11) DEFAULT NULL              NULL,
    email          nvarchar(50) DEFAULT NULL              NULL,
    status         tinyint                                NOT NULL,
    creator        nvarchar(64) DEFAULT ''                NULL,
    create_time    datetime2    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater        nvarchar(64) DEFAULT ''                NULL,
    update_time    datetime2    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted        bit          DEFAULT 0                 NOT NULL,
    tenant_id      bigint       DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'部门id',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dept',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'部门名称',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dept',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'父部门id',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dept',
     'COLUMN', N'parent_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'显示顺序',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dept',
     'COLUMN', N'sort'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'负责人',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dept',
     'COLUMN', N'leader_user_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'联系电话',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dept',
     'COLUMN', N'phone'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'邮箱',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dept',
     'COLUMN', N'email'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'部门状态（0正常 1停用）',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dept',
     'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dept',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dept',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dept',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dept',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dept',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dept',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'部门表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dept'
GO

-- ----------------------------
-- Records of system_dept
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT system_dept ON
GO
INSERT INTO system_dept (id, name, parent_id, sort, leader_user_id, phone, email, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (100, N'芋道源码', 0, 0, 1, N'15888888888', N'ry@qq.com', 0, N'admin', N'2021-01-05 17:03:47', N'1', N'2023-11-14 23:30:36', N'0', 1)
GO
INSERT INTO system_dept (id, name, parent_id, sort, leader_user_id, phone, email, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (101, N'深圳总公司', 100, 1, 104, N'15888888888', N'ry@qq.com', 0, N'admin', N'2021-01-05 17:03:47', N'1', N'2023-12-02 09:53:35', N'0', 1)
GO
INSERT INTO system_dept (id, name, parent_id, sort, leader_user_id, phone, email, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (102, N'长沙分公司', 100, 2, NULL, N'15888888888', N'ry@qq.com', 0, N'admin', N'2021-01-05 17:03:47', N'', N'2021-12-15 05:01:40', N'0', 1)
GO
INSERT INTO system_dept (id, name, parent_id, sort, leader_user_id, phone, email, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (103, N'研发部门', 101, 1, 104, N'15888888888', N'ry@qq.com', 0, N'admin', N'2021-01-05 17:03:47', N'1', N'2024-03-24 20:56:04', N'0', 1)
GO
INSERT INTO system_dept (id, name, parent_id, sort, leader_user_id, phone, email, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (104, N'市场部门', 101, 2, NULL, N'15888888888', N'ry@qq.com', 0, N'admin', N'2021-01-05 17:03:47', N'', N'2021-12-15 05:01:38', N'0', 1)
GO
INSERT INTO system_dept (id, name, parent_id, sort, leader_user_id, phone, email, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (105, N'测试部门', 101, 3, NULL, N'15888888888', N'ry@qq.com', 0, N'admin', N'2021-01-05 17:03:47', N'1', N'2022-05-16 20:25:15', N'0', 1)
GO
INSERT INTO system_dept (id, name, parent_id, sort, leader_user_id, phone, email, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (106, N'财务部门', 101, 4, 103, N'15888888888', N'ry@qq.com', 0, N'admin', N'2021-01-05 17:03:47', N'103', N'2022-01-15 21:32:22', N'0', 1)
GO
INSERT INTO system_dept (id, name, parent_id, sort, leader_user_id, phone, email, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (107, N'运维部门', 101, 5, 1, N'15888888888', N'ry@qq.com', 0, N'admin', N'2021-01-05 17:03:47', N'1', N'2023-12-02 09:28:22', N'0', 1)
GO
INSERT INTO system_dept (id, name, parent_id, sort, leader_user_id, phone, email, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (108, N'市场部门', 102, 1, NULL, N'15888888888', N'ry@qq.com', 0, N'admin', N'2021-01-05 17:03:47', N'1', N'2022-02-16 08:35:45', N'0', 1)
GO
INSERT INTO system_dept (id, name, parent_id, sort, leader_user_id, phone, email, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (109, N'财务部门', 102, 2, NULL, N'15888888888', N'ry@qq.com', 0, N'admin', N'2021-01-05 17:03:47', N'', N'2021-12-15 05:01:29', N'0', 1)
GO
INSERT INTO system_dept (id, name, parent_id, sort, leader_user_id, phone, email, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (110, N'新部门', 0, 1, NULL, NULL, NULL, 0, N'110', N'2022-02-23 20:46:30', N'110', N'2022-02-23 20:46:30', N'0', 121)
GO
INSERT INTO system_dept (id, name, parent_id, sort, leader_user_id, phone, email, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (111, N'顶级部门', 0, 1, NULL, NULL, NULL, 0, N'113', N'2022-03-07 21:44:50', N'113', N'2022-03-07 21:44:50', N'0', 122)
GO
INSERT INTO system_dept (id, name, parent_id, sort, leader_user_id, phone, email, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (112, N'产品部门', 101, 100, 1, NULL, NULL, 1, N'1', N'2023-12-02 09:45:13', N'1', N'2023-12-02 09:45:31', N'0', 1)
GO
INSERT INTO system_dept (id, name, parent_id, sort, leader_user_id, phone, email, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (113, N'支持部门', 102, 3, 104, NULL, NULL, 1, N'1', N'2023-12-02 09:47:38', N'1', N'2023-12-02 09:47:38', N'0', 1)
GO
SET IDENTITY_INSERT system_dept OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for system_dict_data
-- ----------------------------
DROP TABLE IF EXISTS system_dict_data
GO
CREATE TABLE system_dict_data
(
    id          bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    sort        int           DEFAULT 0                 NOT NULL,
    label       nvarchar(100) DEFAULT ''                NOT NULL,
    value       nvarchar(100) DEFAULT ''                NOT NULL,
    dict_type   nvarchar(100) DEFAULT ''                NOT NULL,
    status      tinyint       DEFAULT 0                 NOT NULL,
    color_type  nvarchar(100) DEFAULT ''                NULL,
    css_class   nvarchar(100) DEFAULT ''                NULL,
    remark      nvarchar(500) DEFAULT NULL              NULL,
    creator     nvarchar(64)  DEFAULT ''                NULL,
    create_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64)  DEFAULT ''                NULL,
    update_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit           DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'字典编码',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_data',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'字典排序',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_data',
     'COLUMN', N'sort'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'字典标签',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_data',
     'COLUMN', N'label'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'字典键值',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_data',
     'COLUMN', N'value'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'字典类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_data',
     'COLUMN', N'dict_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'状态（0正常 1停用）',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_data',
     'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'颜色类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_data',
     'COLUMN', N'color_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'css 样式',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_data',
     'COLUMN', N'css_class'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'备注',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_data',
     'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_data',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_data',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_data',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_data',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_data',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'字典数据表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_data'
GO

-- ----------------------------
-- Records of system_dict_data
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT system_dict_data ON
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1, 1, N'男', N'1', N'system_user_sex', 0, N'default', N'A', N'性别男', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-03-29 00:14:39', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (2, 2, N'女', N'2', N'system_user_sex', 0, N'success', N'', N'性别女', N'admin', N'2021-01-05 17:03:48', N'1', N'2023-11-15 23:30:37', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (8, 1, N'正常', N'1', N'infra_job_status', 0, N'success', N'', N'正常状态', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-02-16 19:33:38', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (9, 2, N'暂停', N'2', N'infra_job_status', 0, N'danger', N'', N'停用状态', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-02-16 19:33:45', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (12, 1, N'系统内置', N'1', N'infra_config_type', 0, N'danger', N'', N'参数类型 - 系统内置', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-02-16 19:06:02', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (13, 2, N'自定义', N'2', N'infra_config_type', 0, N'primary', N'', N'参数类型 - 自定义', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-02-16 19:06:07', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (14, 1, N'通知', N'1', N'system_notice_type', 0, N'success', N'', N'通知', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-02-16 13:05:57', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (15, 2, N'公告', N'2', N'system_notice_type', 0, N'info', N'', N'公告', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-02-16 13:06:01', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (16, 0, N'其它', N'0', N'infra_operate_type', 0, N'default', N'', N'其它操作', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-03-14 12:44:19', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (17, 1, N'查询', N'1', N'infra_operate_type', 0, N'info', N'', N'查询操作', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-03-14 12:44:20', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (18, 2, N'新增', N'2', N'infra_operate_type', 0, N'primary', N'', N'新增操作', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-03-14 12:44:21', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (19, 3, N'修改', N'3', N'infra_operate_type', 0, N'warning', N'', N'修改操作', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-03-14 12:44:22', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (20, 4, N'删除', N'4', N'infra_operate_type', 0, N'danger', N'', N'删除操作', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-03-14 12:44:23', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (22, 5, N'导出', N'5', N'infra_operate_type', 0, N'default', N'', N'导出操作', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-03-14 12:44:24', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (23, 6, N'导入', N'6', N'infra_operate_type', 0, N'default', N'', N'导入操作', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-03-14 12:44:25', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (27, 1, N'开启', N'0', N'common_status', 0, N'primary', N'', N'开启状态', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-02-16 08:00:39', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (28, 2, N'关闭', N'1', N'common_status', 0, N'info', N'', N'关闭状态', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-02-16 08:00:44', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (29, 1, N'目录', N'1', N'system_menu_type', 0, N'', N'', N'目录', N'admin', N'2021-01-05 17:03:48', N'', N'2022-02-01 16:43:45', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (30, 2, N'菜单', N'2', N'system_menu_type', 0, N'', N'', N'菜单', N'admin', N'2021-01-05 17:03:48', N'', N'2022-02-01 16:43:41', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (31, 3, N'按钮', N'3', N'system_menu_type', 0, N'', N'', N'按钮', N'admin', N'2021-01-05 17:03:48', N'', N'2022-02-01 16:43:39', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (32, 1, N'内置', N'1', N'system_role_type', 0, N'danger', N'', N'内置角色', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-02-16 13:02:08', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (33, 2, N'自定义', N'2', N'system_role_type', 0, N'primary', N'', N'自定义角色', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-02-16 13:02:12', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (34, 1, N'全部数据权限', N'1', N'system_data_scope', 0, N'', N'', N'全部数据权限', N'admin', N'2021-01-05 17:03:48', N'', N'2022-02-01 16:47:17', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (35, 2, N'指定部门数据权限', N'2', N'system_data_scope', 0, N'', N'', N'指定部门数据权限', N'admin', N'2021-01-05 17:03:48', N'', N'2022-02-01 16:47:18', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (36, 3, N'本部门数据权限', N'3', N'system_data_scope', 0, N'', N'', N'本部门数据权限', N'admin', N'2021-01-05 17:03:48', N'', N'2022-02-01 16:47:16', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (37, 4, N'本部门及以下数据权限', N'4', N'system_data_scope', 0, N'', N'', N'本部门及以下数据权限', N'admin', N'2021-01-05 17:03:48', N'', N'2022-02-01 16:47:21', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (38, 5, N'仅本人数据权限', N'5', N'system_data_scope', 0, N'', N'', N'仅本人数据权限', N'admin', N'2021-01-05 17:03:48', N'', N'2022-02-01 16:47:23', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (39, 0, N'成功', N'0', N'system_login_result', 0, N'success', N'', N'登陆结果 - 成功', N'', N'2021-01-18 06:17:36', N'1', N'2022-02-16 13:23:49', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (40, 10, N'账号或密码不正确', N'10', N'system_login_result', 0, N'primary', N'', N'登陆结果 - 账号或密码不正确', N'', N'2021-01-18 06:17:54', N'1', N'2022-02-16 13:24:27', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (41, 20, N'用户被禁用', N'20', N'system_login_result', 0, N'warning', N'', N'登陆结果 - 用户被禁用', N'', N'2021-01-18 06:17:54', N'1', N'2022-02-16 13:23:57', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (42, 30, N'验证码不存在', N'30', N'system_login_result', 0, N'info', N'', N'登陆结果 - 验证码不存在', N'', N'2021-01-18 06:17:54', N'1', N'2022-02-16 13:24:07', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (43, 31, N'验证码不正确', N'31', N'system_login_result', 0, N'info', N'', N'登陆结果 - 验证码不正确', N'', N'2021-01-18 06:17:54', N'1', N'2022-02-16 13:24:11', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (44, 100, N'未知异常', N'100', N'system_login_result', 0, N'danger', N'', N'登陆结果 - 未知异常', N'', N'2021-01-18 06:17:54', N'1', N'2022-02-16 13:24:23', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (45, 1, N'是', N'true', N'infra_boolean_string', 0, N'danger', N'', N'Boolean 是否类型 - 是', N'', N'2021-01-19 03:20:55', N'1', N'2022-03-15 23:01:45', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (46, 1, N'否', N'false', N'infra_boolean_string', 0, N'info', N'', N'Boolean 是否类型 - 否', N'', N'2021-01-19 03:20:55', N'1', N'2022-03-15 23:09:45', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (50, 1, N'单表（增删改查）', N'1', N'infra_codegen_template_type', 0, N'', N'', NULL, N'', N'2021-02-05 07:09:06', N'', N'2022-03-10 16:33:15', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (51, 2, N'树表（增删改查）', N'2', N'infra_codegen_template_type', 0, N'', N'', NULL, N'', N'2021-02-05 07:14:46', N'', N'2022-03-10 16:33:19', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (53, 0, N'初始化中', N'0', N'infra_job_status', 0, N'primary', N'', NULL, N'', N'2021-02-07 07:46:49', N'1', N'2022-02-16 19:33:29', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (57, 0, N'运行中', N'0', N'infra_job_log_status', 0, N'primary', N'', N'RUNNING', N'', N'2021-02-08 10:04:24', N'1', N'2022-02-16 19:07:48', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (58, 1, N'成功', N'1', N'infra_job_log_status', 0, N'success', N'', NULL, N'', N'2021-02-08 10:06:57', N'1', N'2022-02-16 19:07:52', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (59, 2, N'失败', N'2', N'infra_job_log_status', 0, N'warning', N'', N'失败', N'', N'2021-02-08 10:07:38', N'1', N'2022-02-16 19:07:56', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (60, 1, N'会员', N'1', N'user_type', 0, N'primary', N'', NULL, N'', N'2021-02-26 00:16:27', N'1', N'2022-02-16 10:22:19', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (61, 2, N'管理员', N'2', N'user_type', 0, N'success', N'', NULL, N'', N'2021-02-26 00:16:34', N'1', N'2022-02-16 10:22:22', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (62, 0, N'未处理', N'0', N'infra_api_error_log_process_status', 0, N'primary', N'', NULL, N'', N'2021-02-26 07:07:19', N'1', N'2022-02-16 20:14:17', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (63, 1, N'已处理', N'1', N'infra_api_error_log_process_status', 0, N'success', N'', NULL, N'', N'2021-02-26 07:07:26', N'1', N'2022-02-16 20:14:08', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (64, 2, N'已忽略', N'2', N'infra_api_error_log_process_status', 0, N'danger', N'', NULL, N'', N'2021-02-26 07:07:34', N'1', N'2022-02-16 20:14:14', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (66, 2, N'阿里云', N'ALIYUN', N'system_sms_channel_code', 0, N'primary', N'', NULL, N'1', N'2021-04-05 01:05:26', N'1', N'2022-02-16 10:09:52', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (67, 1, N'验证码', N'1', N'system_sms_template_type', 0, N'warning', N'', NULL, N'1', N'2021-04-05 21:50:57', N'1', N'2022-02-16 12:48:30', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (68, 2, N'通知', N'2', N'system_sms_template_type', 0, N'primary', N'', NULL, N'1', N'2021-04-05 21:51:08', N'1', N'2022-02-16 12:48:27', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (69, 0, N'营销', N'3', N'system_sms_template_type', 0, N'danger', N'', NULL, N'1', N'2021-04-05 21:51:15', N'1', N'2022-02-16 12:48:22', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (70, 0, N'初始化', N'0', N'system_sms_send_status', 0, N'primary', N'', NULL, N'1', N'2021-04-11 20:18:33', N'1', N'2022-02-16 10:26:07', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (71, 1, N'发送成功', N'10', N'system_sms_send_status', 0, N'success', N'', NULL, N'1', N'2021-04-11 20:18:43', N'1', N'2022-02-16 10:25:56', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (72, 2, N'发送失败', N'20', N'system_sms_send_status', 0, N'danger', N'', NULL, N'1', N'2021-04-11 20:18:49', N'1', N'2022-02-16 10:26:03', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (73, 3, N'不发送', N'30', N'system_sms_send_status', 0, N'info', N'', NULL, N'1', N'2021-04-11 20:19:44', N'1', N'2022-02-16 10:26:10', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (74, 0, N'等待结果', N'0', N'system_sms_receive_status', 0, N'primary', N'', NULL, N'1', N'2021-04-11 20:27:43', N'1', N'2022-02-16 10:28:24', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (75, 1, N'接收成功', N'10', N'system_sms_receive_status', 0, N'success', N'', NULL, N'1', N'2021-04-11 20:29:25', N'1', N'2022-02-16 10:28:28', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (76, 2, N'接收失败', N'20', N'system_sms_receive_status', 0, N'danger', N'', NULL, N'1', N'2021-04-11 20:29:31', N'1', N'2022-02-16 10:28:32', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (77, 0, N'调试(钉钉)', N'DEBUG_DING_TALK', N'system_sms_channel_code', 0, N'info', N'', NULL, N'1', N'2021-04-13 00:20:37', N'1', N'2022-02-16 10:10:00', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (80, 100, N'账号登录', N'100', N'system_login_type', 0, N'primary', N'', N'账号登录', N'1', N'2021-10-06 00:52:02', N'1', N'2022-02-16 13:11:34', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (81, 101, N'社交登录', N'101', N'system_login_type', 0, N'info', N'', N'社交登录', N'1', N'2021-10-06 00:52:17', N'1', N'2022-02-16 13:11:40', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (83, 200, N'主动登出', N'200', N'system_login_type', 0, N'primary', N'', N'主动登出', N'1', N'2021-10-06 00:52:58', N'1', N'2022-02-16 13:11:49', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (85, 202, N'强制登出', N'202', N'system_login_type', 0, N'danger', N'', N'强制退出', N'1', N'2021-10-06 00:53:41', N'1', N'2022-02-16 13:11:57', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (86, 0, N'病假', N'1', N'bpm_oa_leave_type', 0, N'primary', N'', NULL, N'1', N'2021-09-21 22:35:28', N'1', N'2022-02-16 10:00:41', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (87, 1, N'事假', N'2', N'bpm_oa_leave_type', 0, N'info', N'', NULL, N'1', N'2021-09-21 22:36:11', N'1', N'2022-02-16 10:00:49', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (88, 2, N'婚假', N'3', N'bpm_oa_leave_type', 0, N'warning', N'', NULL, N'1', N'2021-09-21 22:36:38', N'1', N'2022-02-16 10:00:53', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (112, 0, N'微信 Wap 网站支付', N'wx_wap', N'pay_channel_code', 0, N'success', N'', N'微信 Wap 网站支付', N'1', N'2023-07-19 20:08:06', N'1', N'2023-07-19 20:09:08', N'0');
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (113, 1, N'微信公众号支付', N'wx_pub', N'pay_channel_code', 0, N'success', N'', N'微信公众号支付', N'1', N'2021-12-03 10:40:24', N'1', N'2023-07-19 20:08:47', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (114, 2, N'微信小程序支付', N'wx_lite', N'pay_channel_code', 0, N'success', N'', N'微信小程序支付', N'1', N'2021-12-03 10:41:06', N'1', N'2023-07-19 20:08:50', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (115, 3, N'微信 App 支付', N'wx_app', N'pay_channel_code', 0, N'success', N'', N'微信 App 支付', N'1', N'2021-12-03 10:41:20', N'1', N'2023-07-19 20:08:56', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (116, 10, N'支付宝 PC 网站支付', N'alipay_pc', N'pay_channel_code', 0, N'primary', N'', N'支付宝 PC 网站支付', N'1', N'2021-12-03 10:42:09', N'1', N'2023-07-19 20:09:12', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (117, 11, N'支付宝 Wap 网站支付', N'alipay_wap', N'pay_channel_code', 0, N'primary', N'', N'支付宝 Wap 网站支付', N'1', N'2021-12-03 10:42:26', N'1', N'2023-07-19 20:09:16', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (118, 12, N'支付宝 App 支付', N'alipay_app', N'pay_channel_code', 0, N'primary', N'', N'支付宝 App 支付', N'1', N'2021-12-03 10:42:55', N'1', N'2023-07-19 20:09:20', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (119, 14, N'支付宝扫码支付', N'alipay_qr', N'pay_channel_code', 0, N'primary', N'', N'支付宝扫码支付', N'1', N'2021-12-03 10:43:10', N'1', N'2023-07-19 20:09:28', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (120, 10, N'通知成功', N'10', N'pay_notify_status', 0, N'success', N'', N'通知成功', N'1', N'2021-12-03 11:02:41', N'1', N'2023-07-19 10:08:19', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (121, 20, N'通知失败', N'20', N'pay_notify_status', 0, N'danger', N'', N'通知失败', N'1', N'2021-12-03 11:02:59', N'1', N'2023-07-19 10:08:21', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (122, 0, N'等待通知', N'0', N'pay_notify_status', 0, N'info', N'', N'未通知', N'1', N'2021-12-03 11:03:10', N'1', N'2023-07-19 10:08:24', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (123, 10, N'支付成功', N'10', N'pay_order_status', 0, N'success', N'', N'支付成功', N'1', N'2021-12-03 11:18:29', N'1', N'2023-07-19 18:04:28', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (124, 30, N'支付关闭', N'30', N'pay_order_status', 0, N'info', N'', N'支付关闭', N'1', N'2021-12-03 11:18:42', N'1', N'2023-07-19 18:05:07', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (125, 0, N'等待支付', N'0', N'pay_order_status', 0, N'info', N'', N'未支付', N'1', N'2021-12-03 11:18:18', N'1', N'2023-07-19 18:04:15', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (600, 5, N'首页', N'1', N'promotion_banner_position', 0, N'warning', N'', N'', N'1', N'2023-10-11 07:45:24', N'1', N'2023-10-11 07:45:38', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (601, 4, N'秒杀活动页', N'2', N'promotion_banner_position', 0, N'warning', N'', N'', N'1', N'2023-10-11 07:45:24', N'1', N'2023-10-11 07:45:38', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (602, 3, N'砍价活动页', N'3', N'promotion_banner_position', 0, N'warning', N'', N'', N'1', N'2023-10-11 07:45:24', N'1', N'2023-10-11 07:45:38', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (603, 2, N'限时折扣页', N'4', N'promotion_banner_position', 0, N'warning', N'', N'', N'1', N'2023-10-11 07:45:24', N'1', N'2023-10-11 07:45:38', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (604, 1, N'满减送页', N'5', N'promotion_banner_position', 0, N'warning', N'', N'', N'1', N'2023-10-11 07:45:24', N'1', N'2023-10-11 07:45:38', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1118, 0, N'等待退款', N'0', N'pay_refund_status', 0, N'info', N'', N'等待退款', N'1', N'2021-12-10 16:44:59', N'1', N'2023-07-19 10:14:39', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1119, 20, N'退款失败', N'20', N'pay_refund_status', 0, N'danger', N'', N'退款失败', N'1', N'2021-12-10 16:45:10', N'1', N'2023-07-19 10:15:10', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1124, 10, N'退款成功', N'10', N'pay_refund_status', 0, N'success', N'', N'退款成功', N'1', N'2021-12-10 16:46:26', N'1', N'2023-07-19 10:15:00', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1127, 1, N'审批中', N'1', N'bpm_process_instance_status', 0, N'default', N'', N'流程实例的状态 - 进行中', N'1', N'2022-01-07 23:47:22', N'1', N'2024-03-16 16:11:45', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1128, 2, N'审批通过', N'2', N'bpm_process_instance_status', 0, N'success', N'', N'流程实例的状态 - 已完成', N'1', N'2022-01-07 23:47:49', N'1', N'2024-03-16 16:11:54', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1129, 1, N'审批中', N'1', N'bpm_task_status', 0, N'primary', N'', N'流程实例的结果 - 处理中', N'1', N'2022-01-07 23:48:32', N'1', N'2024-03-08 22:41:37', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1130, 2, N'审批通过', N'2', N'bpm_task_status', 0, N'success', N'', N'流程实例的结果 - 通过', N'1', N'2022-01-07 23:48:45', N'1', N'2024-03-08 22:41:38', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1131, 3, N'审批不通过', N'3', N'bpm_task_status', 0, N'danger', N'', N'流程实例的结果 - 不通过', N'1', N'2022-01-07 23:48:55', N'1', N'2024-03-08 22:41:38', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1132, 4, N'已取消', N'4', N'bpm_task_status', 0, N'info', N'', N'流程实例的结果 - 撤销', N'1', N'2022-01-07 23:49:06', N'1', N'2024-03-08 22:41:39', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1133, 10, N'流程表单', N'10', N'bpm_model_form_type', 0, N'', N'', N'流程的表单类型 - 流程表单', N'103', N'2022-01-11 23:51:30', N'103', N'2022-01-11 23:51:30', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1134, 20, N'业务表单', N'20', N'bpm_model_form_type', 0, N'', N'', N'流程的表单类型 - 业务表单', N'103', N'2022-01-11 23:51:47', N'103', N'2022-01-11 23:51:47', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1135, 10, N'角色', N'10', N'bpm_task_candidate_strategy', 0, N'info', N'', N'任务分配规则的类型 - 角色', N'103', N'2022-01-12 23:21:22', N'1', N'2024-03-06 02:53:16', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1136, 20, N'部门的成员', N'20', N'bpm_task_candidate_strategy', 0, N'primary', N'', N'任务分配规则的类型 - 部门的成员', N'103', N'2022-01-12 23:21:47', N'1', N'2024-03-06 02:53:17', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1137, 21, N'部门的负责人', N'21', N'bpm_task_candidate_strategy', 0, N'primary', N'', N'任务分配规则的类型 - 部门的负责人', N'103', N'2022-01-12 23:33:36', N'1', N'2024-03-06 02:53:18', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1138, 30, N'用户', N'30', N'bpm_task_candidate_strategy', 0, N'info', N'', N'任务分配规则的类型 - 用户', N'103', N'2022-01-12 23:34:02', N'1', N'2024-03-06 02:53:19', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1139, 40, N'用户组', N'40', N'bpm_task_candidate_strategy', 0, N'warning', N'', N'任务分配规则的类型 - 用户组', N'103', N'2022-01-12 23:34:21', N'1', N'2024-03-06 02:53:20', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1140, 60, N'流程表达式', N'60', N'bpm_task_candidate_strategy', 0, N'danger', N'', N'任务分配规则的类型 - 流程表达式', N'103', N'2022-01-12 23:34:43', N'1', N'2024-03-06 02:53:20', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1141, 22, N'岗位', N'22', N'bpm_task_candidate_strategy', 0, N'success', N'', N'任务分配规则的类型 - 岗位', N'103', N'2022-01-14 18:41:55', N'1', N'2024-03-06 02:53:21', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1145, 1, N'管理后台', N'1', N'infra_codegen_scene', 0, N'', N'', N'代码生成的场景枚举 - 管理后台', N'1', N'2022-02-02 13:15:06', N'1', N'2022-03-10 16:32:59', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1146, 2, N'用户 APP', N'2', N'infra_codegen_scene', 0, N'', N'', N'代码生成的场景枚举 - 用户 APP', N'1', N'2022-02-02 13:15:19', N'1', N'2022-03-10 16:33:03', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1150, 1, N'数据库', N'1', N'infra_file_storage', 0, N'default', N'', NULL, N'1', N'2022-03-15 00:25:28', N'1', N'2022-03-15 00:25:28', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1151, 10, N'本地磁盘', N'10', N'infra_file_storage', 0, N'default', N'', NULL, N'1', N'2022-03-15 00:25:41', N'1', N'2022-03-15 00:25:56', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1152, 11, N'FTP 服务器', N'11', N'infra_file_storage', 0, N'default', N'', NULL, N'1', N'2022-03-15 00:26:06', N'1', N'2022-03-15 00:26:10', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1153, 12, N'SFTP 服务器', N'12', N'infra_file_storage', 0, N'default', N'', NULL, N'1', N'2022-03-15 00:26:22', N'1', N'2022-03-15 00:26:22', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1154, 20, N'S3 对象存储', N'20', N'infra_file_storage', 0, N'default', N'', NULL, N'1', N'2022-03-15 00:26:31', N'1', N'2022-03-15 00:26:45', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1155, 103, N'短信登录', N'103', N'system_login_type', 0, N'default', N'', NULL, N'1', N'2022-05-09 23:57:58', N'1', N'2022-05-09 23:58:09', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1156, 1, N'password', N'password', N'system_oauth2_grant_type', 0, N'default', N'', N'密码模式', N'1', N'2022-05-12 00:22:05', N'1', N'2022-05-11 16:26:01', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1157, 2, N'authorization_code', N'authorization_code', N'system_oauth2_grant_type', 0, N'primary', N'', N'授权码模式', N'1', N'2022-05-12 00:22:59', N'1', N'2022-05-11 16:26:02', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1158, 3, N'implicit', N'implicit', N'system_oauth2_grant_type', 0, N'success', N'', N'简化模式', N'1', N'2022-05-12 00:23:40', N'1', N'2022-05-11 16:26:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1159, 4, N'client_credentials', N'client_credentials', N'system_oauth2_grant_type', 0, N'default', N'', N'客户端模式', N'1', N'2022-05-12 00:23:51', N'1', N'2022-05-11 16:26:08', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1160, 5, N'refresh_token', N'refresh_token', N'system_oauth2_grant_type', 0, N'info', N'', N'刷新模式', N'1', N'2022-05-12 00:24:02', N'1', N'2022-05-11 16:26:11', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1162, 1, N'销售中', N'1', N'product_spu_status', 0, N'success', N'', N'商品 SPU 状态 - 销售中', N'1', N'2022-10-24 21:19:47', N'1', N'2022-10-24 21:20:38', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1163, 0, N'仓库中', N'0', N'product_spu_status', 0, N'info', N'', N'商品 SPU 状态 - 仓库中', N'1', N'2022-10-24 21:20:54', N'1', N'2022-10-24 21:21:22', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1164, 0, N'回收站', N'-1', N'product_spu_status', 0, N'default', N'', N'商品 SPU 状态 - 回收站', N'1', N'2022-10-24 21:21:11', N'1', N'2022-10-24 21:21:11', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1165, 1, N'满减', N'1', N'promotion_discount_type', 0, N'success', N'', N'优惠类型 - 满减', N'1', N'2022-11-01 12:46:41', N'1', N'2022-11-01 12:50:11', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1166, 2, N'折扣', N'2', N'promotion_discount_type', 0, N'primary', N'', N'优惠类型 - 折扣', N'1', N'2022-11-01 12:46:51', N'1', N'2022-11-01 12:50:08', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1167, 1, N'固定日期', N'1', N'promotion_coupon_template_validity_type', 0, N'default', N'', N'优惠劵模板的有限期类型 - 固定日期', N'1', N'2022-11-02 00:07:34', N'1', N'2022-11-04 00:07:49', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1168, 2, N'领取之后', N'2', N'promotion_coupon_template_validity_type', 0, N'default', N'', N'优惠劵模板的有限期类型 - 领取之后', N'1', N'2022-11-02 00:07:54', N'1', N'2022-11-04 00:07:52', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1169, 1, N'通用劵', N'1', N'promotion_product_scope', 0, N'default', N'', N'营销的商品范围 - 全部商品参与', N'1', N'2022-11-02 00:28:22', N'1', N'2023-09-28 00:27:42', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1170, 2, N'商品劵', N'2', N'promotion_product_scope', 0, N'default', N'', N'营销的商品范围 - 指定商品参与', N'1', N'2022-11-02 00:28:34', N'1', N'2023-09-28 00:27:44', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1171, 1, N'未使用', N'1', N'promotion_coupon_status', 0, N'primary', N'', N'优惠劵的状态 - 已领取', N'1', N'2022-11-04 00:15:08', N'1', N'2023-10-03 12:54:38', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1172, 2, N'已使用', N'2', N'promotion_coupon_status', 0, N'success', N'', N'优惠劵的状态 - 已使用', N'1', N'2022-11-04 00:15:21', N'1', N'2022-11-04 19:16:08', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1173, 3, N'已过期', N'3', N'promotion_coupon_status', 0, N'info', N'', N'优惠劵的状态 - 已过期', N'1', N'2022-11-04 00:15:43', N'1', N'2022-11-04 19:16:12', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1174, 1, N'直接领取', N'1', N'promotion_coupon_take_type', 0, N'primary', N'', N'优惠劵的领取方式 - 直接领取', N'1', N'2022-11-04 19:13:00', N'1', N'2022-11-04 19:13:25', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1175, 2, N'指定发放', N'2', N'promotion_coupon_take_type', 0, N'success', N'', N'优惠劵的领取方式 - 指定发放', N'1', N'2022-11-04 19:13:13', N'1', N'2022-11-04 19:14:48', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1176, 10, N'未开始', N'10', N'promotion_activity_status', 0, N'primary', N'', N'促销活动的状态枚举 - 未开始', N'1', N'2022-11-04 22:54:49', N'1', N'2022-11-04 22:55:53', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1177, 20, N'进行中', N'20', N'promotion_activity_status', 0, N'success', N'', N'促销活动的状态枚举 - 进行中', N'1', N'2022-11-04 22:55:06', N'1', N'2022-11-04 22:55:20', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1178, 30, N'已结束', N'30', N'promotion_activity_status', 0, N'info', N'', N'促销活动的状态枚举 - 已结束', N'1', N'2022-11-04 22:55:41', N'1', N'2022-11-04 22:55:41', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1179, 40, N'已关闭', N'40', N'promotion_activity_status', 0, N'warning', N'', N'促销活动的状态枚举 - 已关闭', N'1', N'2022-11-04 22:56:10', N'1', N'2022-11-04 22:56:18', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1180, 10, N'满 N 元', N'10', N'promotion_condition_type', 0, N'primary', N'', N'营销的条件类型 - 满 N 元', N'1', N'2022-11-04 22:59:45', N'1', N'2022-11-04 22:59:45', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1181, 20, N'满 N 件', N'20', N'promotion_condition_type', 0, N'success', N'', N'营销的条件类型 - 满 N 件', N'1', N'2022-11-04 23:00:02', N'1', N'2022-11-04 23:00:02', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1182, 10, N'申请售后', N'10', N'trade_after_sale_status', 0, N'primary', N'', N'交易售后状态 - 申请售后', N'1', N'2022-11-19 20:53:33', N'1', N'2022-11-19 20:54:42', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1183, 20, N'商品待退货', N'20', N'trade_after_sale_status', 0, N'primary', N'', N'交易售后状态 - 商品待退货', N'1', N'2022-11-19 20:54:36', N'1', N'2022-11-19 20:58:58', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1184, 30, N'商家待收货', N'30', N'trade_after_sale_status', 0, N'primary', N'', N'交易售后状态 - 商家待收货', N'1', N'2022-11-19 20:56:56', N'1', N'2022-11-19 20:59:20', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1185, 40, N'等待退款', N'40', N'trade_after_sale_status', 0, N'primary', N'', N'交易售后状态 - 等待退款', N'1', N'2022-11-19 20:59:54', N'1', N'2022-11-19 21:00:01', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1186, 50, N'退款成功', N'50', N'trade_after_sale_status', 0, N'default', N'', N'交易售后状态 - 退款成功', N'1', N'2022-11-19 21:00:33', N'1', N'2022-11-19 21:00:33', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1187, 61, N'买家取消', N'61', N'trade_after_sale_status', 0, N'info', N'', N'交易售后状态 - 买家取消', N'1', N'2022-11-19 21:01:29', N'1', N'2022-11-19 21:01:29', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1188, 62, N'商家拒绝', N'62', N'trade_after_sale_status', 0, N'info', N'', N'交易售后状态 - 商家拒绝', N'1', N'2022-11-19 21:02:17', N'1', N'2022-11-19 21:02:17', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1189, 63, N'商家拒收货', N'63', N'trade_after_sale_status', 0, N'info', N'', N'交易售后状态 - 商家拒收货', N'1', N'2022-11-19 21:02:37', N'1', N'2022-11-19 21:03:07', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1190, 10, N'售中退款', N'10', N'trade_after_sale_type', 0, N'success', N'', N'交易售后的类型 - 售中退款', N'1', N'2022-11-19 21:05:05', N'1', N'2022-11-19 21:38:23', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1191, 20, N'售后退款', N'20', N'trade_after_sale_type', 0, N'primary', N'', N'交易售后的类型 - 售后退款', N'1', N'2022-11-19 21:05:32', N'1', N'2022-11-19 21:38:32', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1192, 10, N'仅退款', N'10', N'trade_after_sale_way', 0, N'primary', N'', N'交易售后的方式 - 仅退款', N'1', N'2022-11-19 21:39:19', N'1', N'2022-11-19 21:39:19', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1193, 20, N'退货退款', N'20', N'trade_after_sale_way', 0, N'success', N'', N'交易售后的方式 - 退货退款', N'1', N'2022-11-19 21:39:38', N'1', N'2022-11-19 21:39:49', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1194, 10, N'微信小程序', N'10', N'terminal', 0, N'default', N'', N'终端 - 微信小程序', N'1', N'2022-12-10 10:51:11', N'1', N'2022-12-10 10:51:57', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1195, 20, N'H5 网页', N'20', N'terminal', 0, N'default', N'', N'终端 - H5 网页', N'1', N'2022-12-10 10:51:30', N'1', N'2022-12-10 10:51:59', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1196, 11, N'微信公众号', N'11', N'terminal', 0, N'default', N'', N'终端 - 微信公众号', N'1', N'2022-12-10 10:54:16', N'1', N'2022-12-10 10:52:01', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1197, 31, N'苹果 App', N'31', N'terminal', 0, N'default', N'', N'终端 - 苹果 App', N'1', N'2022-12-10 10:54:42', N'1', N'2022-12-10 10:52:18', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1198, 32, N'安卓 App', N'32', N'terminal', 0, N'default', N'', N'终端 - 安卓 App', N'1', N'2022-12-10 10:55:02', N'1', N'2022-12-10 10:59:17', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1199, 0, N'普通订单', N'0', N'trade_order_type', 0, N'default', N'', N'交易订单的类型 - 普通订单', N'1', N'2022-12-10 16:34:14', N'1', N'2022-12-10 16:34:14', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1200, 1, N'秒杀订单', N'1', N'trade_order_type', 0, N'default', N'', N'交易订单的类型 - 秒杀订单', N'1', N'2022-12-10 16:34:26', N'1', N'2022-12-10 16:34:26', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1201, 2, N'拼团订单', N'2', N'trade_order_type', 0, N'default', N'', N'交易订单的类型 - 拼团订单', N'1', N'2022-12-10 16:34:36', N'1', N'2022-12-10 16:34:36', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1202, 3, N'砍价订单', N'3', N'trade_order_type', 0, N'default', N'', N'交易订单的类型 - 砍价订单', N'1', N'2022-12-10 16:34:48', N'1', N'2022-12-10 16:34:48', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1203, 0, N'待支付', N'0', N'trade_order_status', 0, N'default', N'', N'交易订单状态 - 待支付', N'1', N'2022-12-10 16:49:29', N'1', N'2022-12-10 16:49:29', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1204, 10, N'待发货', N'10', N'trade_order_status', 0, N'primary', N'', N'交易订单状态 - 待发货', N'1', N'2022-12-10 16:49:53', N'1', N'2022-12-10 16:51:17', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1205, 20, N'已发货', N'20', N'trade_order_status', 0, N'primary', N'', N'交易订单状态 - 已发货', N'1', N'2022-12-10 16:50:13', N'1', N'2022-12-10 16:51:31', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1206, 30, N'已完成', N'30', N'trade_order_status', 0, N'success', N'', N'交易订单状态 - 已完成', N'1', N'2022-12-10 16:50:30', N'1', N'2022-12-10 16:51:06', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1207, 40, N'已取消', N'40', N'trade_order_status', 0, N'danger', N'', N'交易订单状态 - 已取消', N'1', N'2022-12-10 16:50:50', N'1', N'2022-12-10 16:51:00', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1208, 0, N'未售后', N'0', N'trade_order_item_after_sale_status', 0, N'info', N'', N'交易订单项的售后状态 - 未售后', N'1', N'2022-12-10 20:58:42', N'1', N'2022-12-10 20:59:29', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1209, 1, N'售后中', N'1', N'trade_order_item_after_sale_status', 0, N'primary', N'', N'交易订单项的售后状态 - 售后中', N'1', N'2022-12-10 20:59:21', N'1', N'2022-12-10 20:59:21', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1210, 2, N'已退款', N'2', N'trade_order_item_after_sale_status', 0, N'success', N'', N'交易订单项的售后状态 - 已退款', N'1', N'2022-12-10 20:59:46', N'1', N'2022-12-10 20:59:46', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1211, 1, N'完全匹配', N'1', N'mp_auto_reply_request_match', 0, N'primary', N'', N'公众号自动回复的请求关键字匹配模式 - 完全匹配', N'1', N'2023-01-16 23:30:39', N'1', N'2023-01-16 23:31:00', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1212, 2, N'半匹配', N'2', N'mp_auto_reply_request_match', 0, N'success', N'', N'公众号自动回复的请求关键字匹配模式 - 半匹配', N'1', N'2023-01-16 23:30:55', N'1', N'2023-01-16 23:31:10', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1213, 1, N'文本', N'text', N'mp_message_type', 0, N'default', N'', N'公众号的消息类型 - 文本', N'1', N'2023-01-17 22:17:32', N'1', N'2023-01-17 22:17:39', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1214, 2, N'图片', N'image', N'mp_message_type', 0, N'default', N'', N'公众号的消息类型 - 图片', N'1', N'2023-01-17 22:17:32', N'1', N'2023-01-17 14:19:47', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1215, 3, N'语音', N'voice', N'mp_message_type', 0, N'default', N'', N'公众号的消息类型 - 语音', N'1', N'2023-01-17 22:17:32', N'1', N'2023-01-17 14:20:08', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1216, 4, N'视频', N'video', N'mp_message_type', 0, N'default', N'', N'公众号的消息类型 - 视频', N'1', N'2023-01-17 22:17:32', N'1', N'2023-01-17 14:21:08', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1217, 5, N'小视频', N'shortvideo', N'mp_message_type', 0, N'default', N'', N'公众号的消息类型 - 小视频', N'1', N'2023-01-17 22:17:32', N'1', N'2023-01-17 14:19:59', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1218, 6, N'图文', N'news', N'mp_message_type', 0, N'default', N'', N'公众号的消息类型 - 图文', N'1', N'2023-01-17 22:17:32', N'1', N'2023-01-17 14:22:54', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1219, 7, N'音乐', N'music', N'mp_message_type', 0, N'default', N'', N'公众号的消息类型 - 音乐', N'1', N'2023-01-17 22:17:32', N'1', N'2023-01-17 14:22:54', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1220, 8, N'地理位置', N'location', N'mp_message_type', 0, N'default', N'', N'公众号的消息类型 - 地理位置', N'1', N'2023-01-17 22:17:32', N'1', N'2023-01-17 14:23:51', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1221, 9, N'链接', N'link', N'mp_message_type', 0, N'default', N'', N'公众号的消息类型 - 链接', N'1', N'2023-01-17 22:17:32', N'1', N'2023-01-17 14:24:49', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1222, 10, N'事件', N'event', N'mp_message_type', 0, N'default', N'', N'公众号的消息类型 - 事件', N'1', N'2023-01-17 22:17:32', N'1', N'2023-01-17 14:24:49', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1223, 0, N'初始化', N'0', N'system_mail_send_status', 0, N'primary', N'', N'邮件发送状态 - 初始化\n', N'1', N'2023-01-26 09:53:49', N'1', N'2023-01-26 16:36:14', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1224, 10, N'发送成功', N'10', N'system_mail_send_status', 0, N'success', N'', N'邮件发送状态 - 发送成功', N'1', N'2023-01-26 09:54:28', N'1', N'2023-01-26 16:36:22', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1225, 20, N'发送失败', N'20', N'system_mail_send_status', 0, N'danger', N'', N'邮件发送状态 - 发送失败', N'1', N'2023-01-26 09:54:50', N'1', N'2023-01-26 16:36:26', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1226, 30, N'不发送', N'30', N'system_mail_send_status', 0, N'info', N'', N'邮件发送状态 -  不发送', N'1', N'2023-01-26 09:55:06', N'1', N'2023-01-26 16:36:36', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1227, 1, N'通知公告', N'1', N'system_notify_template_type', 0, N'primary', N'', N'站内信模版的类型 - 通知公告', N'1', N'2023-01-28 10:35:59', N'1', N'2023-01-28 10:35:59', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1228, 2, N'系统消息', N'2', N'system_notify_template_type', 0, N'success', N'', N'站内信模版的类型 - 系统消息', N'1', N'2023-01-28 10:36:20', N'1', N'2023-01-28 10:36:25', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1230, 13, N'支付宝条码支付', N'alipay_bar', N'pay_channel_code', 0, N'primary', N'', N'支付宝条码支付', N'1', N'2023-02-18 23:32:24', N'1', N'2023-07-19 20:09:23', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1231, 10, N'Vue2 Element UI 标准模版', N'10', N'infra_codegen_front_type', 0, N'', N'', N'', N'1', N'2023-04-13 00:03:55', N'1', N'2023-04-13 00:03:55', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1232, 20, N'Vue3 Element Plus 标准模版', N'20', N'infra_codegen_front_type', 0, N'', N'', N'', N'1', N'2023-04-13 00:04:08', N'1', N'2023-04-13 00:04:08', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1233, 21, N'Vue3 Element Plus Schema 模版', N'21', N'infra_codegen_front_type', 0, N'', N'', N'', N'1', N'2023-04-13 00:04:26', N'1', N'2023-04-13 00:04:26', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1234, 30, N'Vue3 vben 模版', N'30', N'infra_codegen_front_type', 0, N'', N'', N'', N'1', N'2023-04-13 00:04:26', N'1', N'2023-04-13 00:04:26', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1244, 0, N'按件', N'1', N'trade_delivery_express_charge_mode', 0, N'', N'', N'', N'1', N'2023-05-21 22:46:40', N'1', N'2023-05-21 22:46:40', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1245, 1, N'按重量', N'2', N'trade_delivery_express_charge_mode', 0, N'', N'', N'', N'1', N'2023-05-21 22:46:58', N'1', N'2023-05-21 22:46:58', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1246, 2, N'按体积', N'3', N'trade_delivery_express_charge_mode', 0, N'', N'', N'', N'1', N'2023-05-21 22:47:18', N'1', N'2023-05-21 22:47:18', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1335, 11, N'订单积分抵扣', N'11', N'member_point_biz_type', 0, N'', N'', N'', N'1', N'2023-06-10 12:15:27', N'1', N'2023-10-11 07:41:43', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1336, 1, N'签到', N'1', N'member_point_biz_type', 0, N'', N'', N'', N'1', N'2023-06-10 12:15:48', N'1', N'2023-08-20 11:59:53', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1341, 20, N'已退款', N'20', N'pay_order_status', 0, N'danger', N'', N'已退款', N'1', N'2023-07-19 18:05:37', N'1', N'2023-07-19 18:05:37', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1342, 21, N'请求成功，但是结果失败', N'21', N'pay_notify_status', 0, N'warning', N'', N'请求成功，但是结果失败', N'1', N'2023-07-19 18:10:47', N'1', N'2023-07-19 18:11:38', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1343, 22, N'请求失败', N'22', N'pay_notify_status', 0, N'warning', N'', NULL, N'1', N'2023-07-19 18:11:05', N'1', N'2023-07-19 18:11:27', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1344, 4, N'微信扫码支付', N'wx_native', N'pay_channel_code', 0, N'success', N'', N'微信扫码支付', N'1', N'2023-07-19 20:07:47', N'1', N'2023-07-19 20:09:03', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1345, 5, N'微信条码支付', N'wx_bar', N'pay_channel_code', 0, N'success', N'', N'微信条码支付\n', N'1', N'2023-07-19 20:08:06', N'1', N'2023-07-19 20:09:08', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1346, 1, N'支付单', N'1', N'pay_notify_type', 0, N'primary', N'', N'支付单', N'1', N'2023-07-20 12:23:17', N'1', N'2023-07-20 12:23:17', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1347, 2, N'退款单', N'2', N'pay_notify_type', 0, N'danger', N'', NULL, N'1', N'2023-07-20 12:23:26', N'1', N'2023-07-20 12:23:26', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1348, 20, N'模拟支付', N'mock', N'pay_channel_code', 0, N'default', N'', N'模拟支付', N'1', N'2023-07-29 11:10:51', N'1', N'2023-07-29 03:14:10', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1349, 12, N'订单积分抵扣（整单取消）', N'12', N'member_point_biz_type', 0, N'', N'', N'', N'1', N'2023-08-20 12:00:03', N'1', N'2023-10-11 07:42:01', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1350, 0, N'管理员调整', N'0', N'member_experience_biz_type', 0, N'', N'', NULL, N'', N'2023-08-22 12:41:01', N'', N'2023-08-22 12:41:01', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1351, 1, N'邀新奖励', N'1', N'member_experience_biz_type', 0, N'', N'', NULL, N'', N'2023-08-22 12:41:01', N'', N'2023-08-22 12:41:01', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1352, 11, N'下单奖励', N'11', N'member_experience_biz_type', 0, N'success', N'', NULL, N'', N'2023-08-22 12:41:01', N'1', N'2023-10-11 07:45:09', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1353, 12, N'下单奖励（整单取消）', N'12', N'member_experience_biz_type', 0, N'warning', N'', NULL, N'', N'2023-08-22 12:41:01', N'1', N'2023-10-11 07:45:01', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1354, 4, N'签到奖励', N'4', N'member_experience_biz_type', 0, N'', N'', NULL, N'', N'2023-08-22 12:41:01', N'', N'2023-08-22 12:41:01', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1355, 5, N'抽奖奖励', N'5', N'member_experience_biz_type', 0, N'', N'', NULL, N'', N'2023-08-22 12:41:01', N'', N'2023-08-22 12:41:01', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1356, 1, N'快递发货', N'1', N'trade_delivery_type', 0, N'', N'', N'', N'1', N'2023-08-23 00:04:55', N'1', N'2023-08-23 00:04:55', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1357, 2, N'用户自提', N'2', N'trade_delivery_type', 0, N'', N'', N'', N'1', N'2023-08-23 00:05:05', N'1', N'2023-08-23 00:05:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1358, 3, N'品类劵', N'3', N'promotion_product_scope', 0, N'default', N'', N'', N'1', N'2023-09-01 23:43:07', N'1', N'2023-09-28 00:27:47', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1359, 1, N'人人分销', N'1', N'brokerage_enabled_condition', 0, N'', N'', N'所有用户都可以分销', N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1360, 2, N'指定分销', N'2', N'brokerage_enabled_condition', 0, N'', N'', N'仅可后台手动设置推广员', N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1361, 1, N'首次绑定', N'1', N'brokerage_bind_mode', 0, N'', N'', N'只要用户没有推广人，随时都可以绑定推广关系', N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1362, 2, N'注册绑定', N'2', N'brokerage_bind_mode', 0, N'', N'', N'仅新用户注册时才能绑定推广关系', N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1363, 3, N'覆盖绑定', N'3', N'brokerage_bind_mode', 0, N'', N'', N'如果用户已经有推广人，推广人会被变更', N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1364, 1, N'钱包', N'1', N'brokerage_withdraw_type', 0, N'', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1365, 2, N'银行卡', N'2', N'brokerage_withdraw_type', 0, N'', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1366, 3, N'微信', N'3', N'brokerage_withdraw_type', 0, N'', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1367, 4, N'支付宝', N'4', N'brokerage_withdraw_type', 0, N'', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1368, 1, N'订单返佣', N'1', N'brokerage_record_biz_type', 0, N'', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1369, 2, N'申请提现', N'2', N'brokerage_record_biz_type', 0, N'', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1370, 3, N'申请提现驳回', N'3', N'brokerage_record_biz_type', 0, N'', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1371, 0, N'待结算', N'0', N'brokerage_record_status', 0, N'', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1372, 1, N'已结算', N'1', N'brokerage_record_status', 0, N'', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1373, 2, N'已取消', N'2', N'brokerage_record_status', 0, N'', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1374, 0, N'审核中', N'0', N'brokerage_withdraw_status', 0, N'', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1375, 10, N'审核通过', N'10', N'brokerage_withdraw_status', 0, N'success', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1376, 11, N'提现成功', N'11', N'brokerage_withdraw_status', 0, N'success', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1377, 20, N'审核不通过', N'20', N'brokerage_withdraw_status', 0, N'danger', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1378, 21, N'提现失败', N'21', N'brokerage_withdraw_status', 0, N'danger', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1379, 0, N'工商银行', N'0', N'brokerage_bank_name', 0, N'', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1380, 1, N'建设银行', N'1', N'brokerage_bank_name', 0, N'', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1381, 2, N'农业银行', N'2', N'brokerage_bank_name', 0, N'', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1382, 3, N'中国银行', N'3', N'brokerage_bank_name', 0, N'', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1383, 4, N'交通银行', N'4', N'brokerage_bank_name', 0, N'', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1384, 5, N'招商银行', N'5', N'brokerage_bank_name', 0, N'', N'', NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1385, 21, N'钱包', N'wallet', N'pay_channel_code', 0, N'primary', N'', N'', N'1', N'2023-10-01 21:46:19', N'1', N'2023-10-01 21:48:01', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1386, 1, N'砍价中', N'1', N'promotion_bargain_record_status', 0, N'default', N'', N'', N'1', N'2023-10-05 10:41:26', N'1', N'2023-10-05 10:41:26', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1387, 2, N'砍价成功', N'2', N'promotion_bargain_record_status', 0, N'success', N'', N'', N'1', N'2023-10-05 10:41:39', N'1', N'2023-10-05 10:41:39', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1388, 3, N'砍价失败', N'3', N'promotion_bargain_record_status', 0, N'warning', N'', N'', N'1', N'2023-10-05 10:41:57', N'1', N'2023-10-05 10:41:57', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1389, 1, N'拼团中', N'1', N'promotion_combination_record_status', 0, N'', N'', N'', N'1', N'2023-10-08 07:24:44', N'1', N'2023-10-08 07:24:44', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1390, 2, N'拼团成功', N'2', N'promotion_combination_record_status', 0, N'success', N'', N'', N'1', N'2023-10-08 07:24:56', N'1', N'2023-10-08 07:24:56', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1391, 3, N'拼团失败', N'3', N'promotion_combination_record_status', 0, N'warning', N'', N'', N'1', N'2023-10-08 07:25:11', N'1', N'2023-10-08 07:25:11', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1392, 2, N'管理员修改', N'2', N'member_point_biz_type', 0, N'default', N'', N'', N'1', N'2023-10-11 07:41:34', N'1', N'2023-10-11 07:41:34', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1393, 13, N'订单积分抵扣（单个退款）', N'13', N'member_point_biz_type', 0, N'', N'', N'', N'1', N'2023-10-11 07:42:29', N'1', N'2023-10-11 07:42:29', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1394, 21, N'订单积分奖励', N'21', N'member_point_biz_type', 0, N'default', N'', N'', N'1', N'2023-10-11 07:42:44', N'1', N'2023-10-11 07:42:44', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1395, 22, N'订单积分奖励（整单取消）', N'22', N'member_point_biz_type', 0, N'default', N'', N'', N'1', N'2023-10-11 07:42:55', N'1', N'2023-10-11 07:43:01', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1396, 23, N'订单积分奖励（单个退款）', N'23', N'member_point_biz_type', 0, N'default', N'', N'', N'1', N'2023-10-11 07:43:16', N'1', N'2023-10-11 07:43:16', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1397, 13, N'下单奖励（单个退款）', N'13', N'member_experience_biz_type', 0, N'warning', N'', N'', N'1', N'2023-10-11 07:45:24', N'1', N'2023-10-11 07:45:38', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1398, 5, N'网上转账', N'5', N'crm_receivable_return_type', 0, N'default', N'', N'', N'1', N'2023-10-18 21:55:24', N'1', N'2023-10-18 21:55:24', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1399, 6, N'支付宝', N'6', N'crm_receivable_return_type', 0, N'default', N'', N'', N'1', N'2023-10-18 21:55:38', N'1', N'2023-10-18 21:55:38', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1400, 7, N'微信支付', N'7', N'crm_receivable_return_type', 0, N'default', N'', N'', N'1', N'2023-10-18 21:55:53', N'1', N'2023-10-18 21:55:53', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1401, 8, N'其他', N'8', N'crm_receivable_return_type', 0, N'default', N'', N'', N'1', N'2023-10-18 21:56:06', N'1', N'2023-10-18 21:56:06', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1402, 1, N'IT', N'1', N'crm_customer_industry', 0, N'default', N'', N'', N'1', N'2023-10-28 23:02:15', N'1', N'2024-02-18 23:30:38', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1403, 2, N'金融业', N'2', N'crm_customer_industry', 0, N'default', N'', N'', N'1', N'2023-10-28 23:02:29', N'1', N'2024-02-18 23:30:43', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1404, 3, N'房地产', N'3', N'crm_customer_industry', 0, N'default', N'', N'', N'1', N'2023-10-28 23:02:41', N'1', N'2024-02-18 23:30:48', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1405, 4, N'商业服务', N'4', N'crm_customer_industry', 0, N'default', N'', N'', N'1', N'2023-10-28 23:02:54', N'1', N'2024-02-18 23:30:54', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1406, 5, N'运输/物流', N'5', N'crm_customer_industry', 0, N'default', N'', N'', N'1', N'2023-10-28 23:03:03', N'1', N'2024-02-18 23:31:00', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1407, 6, N'生产', N'6', N'crm_customer_industry', 0, N'default', N'', N'', N'1', N'2023-10-28 23:03:13', N'1', N'2024-02-18 23:31:08', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1408, 7, N'政府', N'7', N'crm_customer_industry', 0, N'default', N'', N'', N'1', N'2023-10-28 23:03:27', N'1', N'2024-02-18 23:31:13', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1409, 8, N'文化传媒', N'8', N'crm_customer_industry', 0, N'default', N'', N'', N'1', N'2023-10-28 23:03:37', N'1', N'2024-02-18 23:31:20', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1422, 1, N'A （重点客户）', N'1', N'crm_customer_level', 0, N'primary', N'', N'', N'1', N'2023-10-28 23:07:13', N'1', N'2023-10-28 23:07:13', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1423, 2, N'B （普通客户）', N'2', N'crm_customer_level', 0, N'info', N'', N'', N'1', N'2023-10-28 23:07:35', N'1', N'2023-10-28 23:07:35', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1424, 3, N'C （非优先客户）', N'3', N'crm_customer_level', 0, N'default', N'', N'', N'1', N'2023-10-28 23:07:53', N'1', N'2023-10-28 23:07:53', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1425, 1, N'促销', N'1', N'crm_customer_source', 0, N'default', N'', N'', N'1', N'2023-10-28 23:08:29', N'1', N'2023-10-28 23:08:29', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1426, 2, N'搜索引擎', N'2', N'crm_customer_source', 0, N'default', N'', N'', N'1', N'2023-10-28 23:08:39', N'1', N'2023-10-28 23:08:39', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1427, 3, N'广告', N'3', N'crm_customer_source', 0, N'default', N'', N'', N'1', N'2023-10-28 23:08:47', N'1', N'2023-10-28 23:08:47', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1428, 4, N'转介绍', N'4', N'crm_customer_source', 0, N'default', N'', N'', N'1', N'2023-10-28 23:08:58', N'1', N'2023-10-28 23:08:58', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1429, 5, N'线上注册', N'5', N'crm_customer_source', 0, N'default', N'', N'', N'1', N'2023-10-28 23:09:12', N'1', N'2023-10-28 23:09:12', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1430, 6, N'线上咨询', N'6', N'crm_customer_source', 0, N'default', N'', N'', N'1', N'2023-10-28 23:09:22', N'1', N'2023-10-28 23:09:22', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1431, 7, N'预约上门', N'7', N'crm_customer_source', 0, N'default', N'', N'', N'1', N'2023-10-28 23:09:39', N'1', N'2023-10-28 23:09:39', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1432, 8, N'陌拜', N'8', N'crm_customer_source', 0, N'default', N'', N'', N'1', N'2023-10-28 23:10:04', N'1', N'2023-10-28 23:10:04', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1433, 9, N'电话咨询', N'9', N'crm_customer_source', 0, N'default', N'', N'', N'1', N'2023-10-28 23:10:18', N'1', N'2023-10-28 23:10:18', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1434, 10, N'邮件咨询', N'10', N'crm_customer_source', 0, N'default', N'', N'', N'1', N'2023-10-28 23:10:33', N'1', N'2023-10-28 23:10:33', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1435, 10, N'Gitee', N'10', N'system_social_type', 0, N'', N'', N'', N'1', N'2023-11-04 13:04:42', N'1', N'2023-11-04 13:04:42', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1436, 20, N'钉钉', N'20', N'system_social_type', 0, N'', N'', N'', N'1', N'2023-11-04 13:04:54', N'1', N'2023-11-04 13:04:54', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1437, 30, N'企业微信', N'30', N'system_social_type', 0, N'', N'', N'', N'1', N'2023-11-04 13:05:09', N'1', N'2023-11-04 13:05:09', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1438, 31, N'微信公众平台', N'31', N'system_social_type', 0, N'', N'', N'', N'1', N'2023-11-04 13:05:18', N'1', N'2023-11-04 13:05:18', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1439, 32, N'微信开放平台', N'32', N'system_social_type', 0, N'', N'', N'', N'1', N'2023-11-04 13:05:30', N'1', N'2023-11-04 13:05:30', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1440, 34, N'微信小程序', N'34', N'system_social_type', 0, N'', N'', N'', N'1', N'2023-11-04 13:05:38', N'1', N'2023-11-04 13:07:16', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1441, 1, N'上架', N'1', N'crm_product_status', 0, N'success', N'', N'', N'1', N'2023-10-30 21:49:34', N'1', N'2023-10-30 21:49:34', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1442, 0, N'下架', N'0', N'crm_product_status', 0, N'success', N'', N'', N'1', N'2023-10-30 21:49:13', N'1', N'2023-10-30 21:49:13', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1443, 15, N'子表', N'15', N'infra_codegen_template_type', 0, N'default', N'', N'', N'1', N'2023-11-13 23:06:16', N'1', N'2023-11-13 23:06:16', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1444, 10, N'主表（标准模式）', N'10', N'infra_codegen_template_type', 0, N'default', N'', N'', N'1', N'2023-11-14 12:32:49', N'1', N'2023-11-14 12:32:49', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1445, 11, N'主表（ERP 模式）', N'11', N'infra_codegen_template_type', 0, N'default', N'', N'', N'1', N'2023-11-14 12:33:05', N'1', N'2023-11-14 12:33:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1446, 12, N'主表（内嵌模式）', N'12', N'infra_codegen_template_type', 0, N'', N'', N'', N'1', N'2023-11-14 12:33:31', N'1', N'2023-11-14 12:33:31', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1447, 1, N'负责人', N'1', N'crm_permission_level', 0, N'default', N'', N'', N'1', N'2023-11-30 09:53:12', N'1', N'2023-11-30 09:53:12', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1448, 2, N'只读', N'2', N'crm_permission_level', 0, N'', N'', N'', N'1', N'2023-11-30 09:53:29', N'1', N'2023-11-30 09:53:29', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1449, 3, N'读写', N'3', N'crm_permission_level', 0, N'', N'', N'', N'1', N'2023-11-30 09:53:36', N'1', N'2023-11-30 09:53:36', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1450, 0, N'未提交', N'0', N'crm_audit_status', 0, N'', N'', N'', N'1', N'2023-11-30 18:56:59', N'1', N'2023-11-30 18:56:59', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1451, 10, N'审批中', N'10', N'crm_audit_status', 0, N'', N'', N'', N'1', N'2023-11-30 18:57:10', N'1', N'2023-11-30 18:57:10', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1452, 20, N'审核通过', N'20', N'crm_audit_status', 0, N'', N'', N'', N'1', N'2023-11-30 18:57:24', N'1', N'2023-11-30 18:57:24', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1453, 30, N'审核不通过', N'30', N'crm_audit_status', 0, N'', N'', N'', N'1', N'2023-11-30 18:57:32', N'1', N'2023-11-30 18:57:32', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1454, 40, N'已取消', N'40', N'crm_audit_status', 0, N'', N'', N'', N'1', N'2023-11-30 18:57:42', N'1', N'2023-11-30 18:57:42', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1456, 1, N'支票', N'1', N'crm_receivable_return_type', 0, N'default', N'', N'', N'1', N'2023-10-18 21:54:29', N'1', N'2023-10-18 21:54:29', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1457, 2, N'现金', N'2', N'crm_receivable_return_type', 0, N'default', N'', N'', N'1', N'2023-10-18 21:54:41', N'1', N'2023-10-18 21:54:41', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1458, 3, N'邮政汇款', N'3', N'crm_receivable_return_type', 0, N'default', N'', N'', N'1', N'2023-10-18 21:54:53', N'1', N'2023-10-18 21:54:53', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1459, 4, N'电汇', N'4', N'crm_receivable_return_type', 0, N'default', N'', N'', N'1', N'2023-10-18 21:55:07', N'1', N'2023-10-18 21:55:07', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1460, 5, N'网上转账', N'5', N'crm_receivable_return_type', 0, N'default', N'', N'', N'1', N'2023-10-18 21:55:24', N'1', N'2023-10-18 21:55:24', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1461, 1, N'个', N'1', N'crm_product_unit', 0, N'', N'', N'', N'1', N'2023-12-05 23:02:26', N'1', N'2023-12-05 23:02:26', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1462, 2, N'块', N'2', N'crm_product_unit', 0, N'', N'', N'', N'1', N'2023-12-05 23:02:34', N'1', N'2023-12-05 23:02:34', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1463, 3, N'只', N'3', N'crm_product_unit', 0, N'', N'', N'', N'1', N'2023-12-05 23:02:57', N'1', N'2023-12-05 23:02:57', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1464, 4, N'把', N'4', N'crm_product_unit', 0, N'', N'', N'', N'1', N'2023-12-05 23:03:05', N'1', N'2023-12-05 23:03:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1465, 5, N'枚', N'5', N'crm_product_unit', 0, N'', N'', N'', N'1', N'2023-12-05 23:03:14', N'1', N'2023-12-05 23:03:14', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1466, 6, N'瓶', N'6', N'crm_product_unit', 0, N'', N'', N'', N'1', N'2023-12-05 23:03:20', N'1', N'2023-12-05 23:03:20', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1467, 7, N'盒', N'7', N'crm_product_unit', 0, N'', N'', N'', N'1', N'2023-12-05 23:03:30', N'1', N'2023-12-05 23:03:30', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1468, 8, N'台', N'8', N'crm_product_unit', 0, N'', N'', N'', N'1', N'2023-12-05 23:03:41', N'1', N'2023-12-05 23:03:41', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1469, 9, N'吨', N'9', N'crm_product_unit', 0, N'', N'', N'', N'1', N'2023-12-05 23:03:48', N'1', N'2023-12-05 23:03:48', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1470, 10, N'千克', N'10', N'crm_product_unit', 0, N'', N'', N'', N'1', N'2023-12-05 23:04:03', N'1', N'2023-12-05 23:04:03', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1471, 11, N'米', N'11', N'crm_product_unit', 0, N'', N'', N'', N'1', N'2023-12-05 23:04:12', N'1', N'2023-12-05 23:04:12', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1472, 12, N'箱', N'12', N'crm_product_unit', 0, N'', N'', N'', N'1', N'2023-12-05 23:04:25', N'1', N'2023-12-05 23:04:25', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1473, 13, N'套', N'13', N'crm_product_unit', 0, N'', N'', N'', N'1', N'2023-12-05 23:04:34', N'1', N'2023-12-05 23:04:34', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1474, 1, N'打电话', N'1', N'crm_follow_up_type', 0, N'', N'', N'', N'1', N'2024-01-15 20:48:20', N'1', N'2024-01-15 20:48:20', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1475, 2, N'发短信', N'2', N'crm_follow_up_type', 0, N'', N'', N'', N'1', N'2024-01-15 20:48:31', N'1', N'2024-01-15 20:48:31', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1476, 3, N'上门拜访', N'3', N'crm_follow_up_type', 0, N'', N'', N'', N'1', N'2024-01-15 20:49:07', N'1', N'2024-01-15 20:49:07', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1477, 4, N'微信沟通', N'4', N'crm_follow_up_type', 0, N'', N'', N'', N'1', N'2024-01-15 20:49:15', N'1', N'2024-01-15 20:49:15', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1478, 4, N'钱包余额', N'4', N'pay_transfer_type', 0, N'info', N'', N'', N'1', N'2023-10-28 16:28:37', N'1', N'2023-10-28 16:28:37', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1479, 3, N'银行卡', N'3', N'pay_transfer_type', 0, N'default', N'', N'', N'1', N'2023-10-28 16:28:21', N'1', N'2023-10-28 16:28:21', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1480, 2, N'微信余额', N'2', N'pay_transfer_type', 0, N'info', N'', N'', N'1', N'2023-10-28 16:28:07', N'1', N'2023-10-28 16:28:07', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1481, 1, N'支付宝余额', N'1', N'pay_transfer_type', 0, N'default', N'', N'', N'1', N'2023-10-28 16:27:44', N'1', N'2023-10-28 16:27:44', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1482, 4, N'转账失败', N'30', N'pay_transfer_status', 0, N'warning', N'', N'', N'1', N'2023-10-28 16:24:16', N'1', N'2023-10-28 16:24:16', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1483, 3, N'转账成功', N'20', N'pay_transfer_status', 0, N'success', N'', N'', N'1', N'2023-10-28 16:23:50', N'1', N'2023-10-28 16:23:50', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1484, 2, N'转账进行中', N'10', N'pay_transfer_status', 0, N'info', N'', N'', N'1', N'2023-10-28 16:23:12', N'1', N'2023-10-28 16:23:12', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1485, 1, N'等待转账', N'0', N'pay_transfer_status', 0, N'default', N'', N'', N'1', N'2023-10-28 16:21:43', N'1', N'2023-10-28 16:23:22', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1486, 10, N'其它入库', N'10', N'erp_stock_record_biz_type', 0, N'', N'', N'', N'1', N'2024-02-05 18:07:25', N'1', N'2024-02-05 18:07:43', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1487, 11, N'其它入库（作废）', N'11', N'erp_stock_record_biz_type', 0, N'danger', N'', N'', N'1', N'2024-02-05 18:08:07', N'1', N'2024-02-05 19:20:16', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1488, 20, N'其它出库', N'20', N'erp_stock_record_biz_type', 0, N'', N'', N'', N'1', N'2024-02-05 18:08:51', N'1', N'2024-02-05 18:08:51', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1489, 21, N'其它出库（作废）', N'21', N'erp_stock_record_biz_type', 0, N'danger', N'', N'', N'1', N'2024-02-05 18:09:00', N'1', N'2024-02-05 19:20:10', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1490, 10, N'未审核', N'10', N'erp_audit_status', 0, N'default', N'', N'', N'1', N'2024-02-06 00:00:21', N'1', N'2024-02-06 00:00:21', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1491, 20, N'已审核', N'20', N'erp_audit_status', 0, N'success', N'', N'', N'1', N'2024-02-06 00:00:35', N'1', N'2024-02-06 00:00:35', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1492, 30, N'调拨入库', N'30', N'erp_stock_record_biz_type', 0, N'', N'', N'', N'1', N'2024-02-07 20:34:19', N'1', N'2024-02-07 12:36:31', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1493, 31, N'调拨入库（作废）', N'31', N'erp_stock_record_biz_type', 0, N'danger', N'', N'', N'1', N'2024-02-07 20:34:29', N'1', N'2024-02-07 20:37:11', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1494, 32, N'调拨出库', N'32', N'erp_stock_record_biz_type', 0, N'', N'', N'', N'1', N'2024-02-07 20:34:38', N'1', N'2024-02-07 12:36:33', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1495, 33, N'调拨出库（作废）', N'33', N'erp_stock_record_biz_type', 0, N'danger', N'', N'', N'1', N'2024-02-07 20:34:49', N'1', N'2024-02-07 20:37:06', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1496, 40, N'盘盈入库', N'40', N'erp_stock_record_biz_type', 0, N'', N'', N'', N'1', N'2024-02-08 08:53:00', N'1', N'2024-02-08 08:53:09', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1497, 41, N'盘盈入库（作废）', N'41', N'erp_stock_record_biz_type', 0, N'danger', N'', N'', N'1', N'2024-02-08 08:53:39', N'1', N'2024-02-16 19:40:54', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1498, 42, N'盘亏出库', N'42', N'erp_stock_record_biz_type', 0, N'', N'', N'', N'1', N'2024-02-08 08:54:16', N'1', N'2024-02-08 08:54:16', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1499, 43, N'盘亏出库（作废）', N'43', N'erp_stock_record_biz_type', 0, N'danger', N'', N'', N'1', N'2024-02-08 08:54:31', N'1', N'2024-02-16 19:40:46', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1500, 50, N'销售出库', N'50', N'erp_stock_record_biz_type', 0, N'', N'', N'', N'1', N'2024-02-11 21:47:25', N'1', N'2024-02-11 21:50:40', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1501, 51, N'销售出库（作废）', N'51', N'erp_stock_record_biz_type', 0, N'danger', N'', N'', N'1', N'2024-02-11 21:47:37', N'1', N'2024-02-11 21:51:12', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1502, 60, N'销售退货入库', N'60', N'erp_stock_record_biz_type', 0, N'', N'', N'', N'1', N'2024-02-12 06:51:05', N'1', N'2024-02-12 06:51:05', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1503, 61, N'销售退货入库（作废）', N'61', N'erp_stock_record_biz_type', 0, N'danger', N'', N'', N'1', N'2024-02-12 06:51:18', N'1', N'2024-02-12 06:51:18', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1504, 70, N'采购入库', N'70', N'erp_stock_record_biz_type', 0, N'', N'', N'', N'1', N'2024-02-16 13:10:02', N'1', N'2024-02-16 13:10:02', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1505, 71, N'采购入库（作废）', N'71', N'erp_stock_record_biz_type', 0, N'danger', N'', N'', N'1', N'2024-02-16 13:10:10', N'1', N'2024-02-16 19:40:40', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1506, 80, N'采购退货出库', N'80', N'erp_stock_record_biz_type', 0, N'', N'', N'', N'1', N'2024-02-16 13:10:17', N'1', N'2024-02-16 13:10:17', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1507, 81, N'采购退货出库（作废）', N'81', N'erp_stock_record_biz_type', 0, N'danger', N'', N'', N'1', N'2024-02-16 13:10:26', N'1', N'2024-02-16 19:40:33', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1509, 3, N'审批不通过', N'3', N'bpm_process_instance_status', 0, N'danger', N'', N'', N'1', N'2024-03-16 16:12:06', N'1', N'2024-03-16 16:12:06', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1510, 4, N'已取消', N'4', N'bpm_process_instance_status', 0, N'warning', N'', N'', N'1', N'2024-03-16 16:12:22', N'1', N'2024-03-16 16:12:22', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1511, 5, N'已退回', N'5', N'bpm_task_status', 0, N'warning', N'', N'', N'1', N'2024-03-16 19:10:46', N'1', N'2024-03-08 22:41:40', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1512, 6, N'委派中', N'6', N'bpm_task_status', 0, N'primary', N'', N'', N'1', N'2024-03-17 10:06:22', N'1', N'2024-03-08 22:41:40', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1513, 7, N'审批通过中', N'7', N'bpm_task_status', 0, N'success', N'', N'', N'1', N'2024-03-17 10:06:47', N'1', N'2024-03-08 22:41:41', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1514, 0, N'待审批', N'0', N'bpm_task_status', 0, N'info', N'', N'', N'1', N'2024-03-17 10:07:11', N'1', N'2024-03-08 22:41:42', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1515, 35, N'发起人自选', N'35', N'bpm_task_candidate_strategy', 0, N'', N'', N'', N'1', N'2024-03-22 19:45:16', N'1', N'2024-03-22 19:45:16', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1516, 1, N'执行监听器', N'execution', N'bpm_process_listener_type', 0, N'primary', N'', N'', N'1', N'2024-03-23 12:54:03', N'1', N'2024-03-23 19:14:19', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1517, 1, N'任务监听器', N'task', N'bpm_process_listener_type', 0, N'success', N'', N'', N'1', N'2024-03-23 12:54:13', N'1', N'2024-03-23 19:14:24', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1526, 1, N'Java 类', N'class', N'bpm_process_listener_value_type', 0, N'primary', N'', N'', N'1', N'2024-03-23 15:08:45', N'1', N'2024-03-23 19:14:32', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1527, 2, N'表达式', N'expression', N'bpm_process_listener_value_type', 0, N'success', N'', N'', N'1', N'2024-03-23 15:09:06', N'1', N'2024-03-23 19:14:38', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1528, 3, N'代理表达式', N'delegateExpression', N'bpm_process_listener_value_type', 0, N'info', N'', N'', N'1', N'2024-03-23 15:11:23', N'1', N'2024-03-23 19:14:41', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1529, 1, N'天', N'1', N'date_interval', 0, N'', N'', N'', N'1', N'2024-03-29 22:50:26', N'1', N'2024-03-29 22:50:26', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1530, 2, N'周', N'2', N'date_interval', 0, N'', N'', N'', N'1', N'2024-03-29 22:50:36', N'1', N'2024-03-29 22:50:36', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1531, 3, N'月', N'3', N'date_interval', 0, N'', N'', N'', N'1', N'2024-03-29 22:50:46', N'1', N'2024-03-29 22:50:54', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1532, 4, N'季度', N'4', N'date_interval', 0, N'', N'', N'', N'1', N'2024-03-29 22:51:01', N'1', N'2024-03-29 22:51:01', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1533, 5, N'年', N'5', N'date_interval', 0, N'', N'', N'', N'1', N'2024-03-29 22:51:07', N'1', N'2024-03-29 22:51:07', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1534, 1, N'赢单', N'1', N'crm_business_end_status_type', 0, N'success', N'', N'', N'1', N'2024-04-13 23:26:57', N'1', N'2024-04-13 23:26:57', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1535, 2, N'输单', N'2', N'crm_business_end_status_type', 0, N'primary', N'', N'', N'1', N'2024-04-13 23:27:31', N'1', N'2024-04-13 23:27:31', N'0')
GO
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1536, 3, N'无效', N'3', N'crm_business_end_status_type', 0, N'info', N'', N'', N'1', N'2024-04-13 23:27:59', N'1', N'2024-04-13 23:27:59', N'0')
GO
SET IDENTITY_INSERT system_dict_data OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for system_dict_type
-- ----------------------------
DROP TABLE IF EXISTS system_dict_type
GO
CREATE TABLE system_dict_type
(
    id           bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    name         nvarchar(100) DEFAULT ''                NOT NULL,
    type         nvarchar(100) DEFAULT ''                NOT NULL,
    status       tinyint       DEFAULT 0                 NOT NULL,
    remark       nvarchar(500) DEFAULT NULL              NULL,
    creator      nvarchar(64)  DEFAULT ''                NULL,
    create_time  datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater      nvarchar(64)  DEFAULT ''                NULL,
    update_time  datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted      bit           DEFAULT 0                 NOT NULL,
    deleted_time datetime2     DEFAULT NULL              NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'字典主键',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_type',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'字典名称',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_type',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'字典类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_type',
     'COLUMN', N'type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'状态（0正常 1停用）',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_type',
     'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'备注',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_type',
     'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_type',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_type',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_type',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_type',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_type',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'删除时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_type',
     'COLUMN', N'deleted_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'字典类型表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_dict_type'
GO

-- ----------------------------
-- Records of system_dict_type
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT system_dict_type ON
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (1, N'用户性别', N'system_user_sex', 0, NULL, N'admin', N'2021-01-05 17:03:48', N'1', N'2022-05-16 20:29:32', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (6, N'参数类型', N'infra_config_type', 0, NULL, N'admin', N'2021-01-05 17:03:48', N'', N'2022-02-01 16:36:54', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (7, N'通知类型', N'system_notice_type', 0, NULL, N'admin', N'2021-01-05 17:03:48', N'', N'2022-02-01 16:35:26', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (9, N'操作类型', N'infra_operate_type', 0, NULL, N'admin', N'2021-01-05 17:03:48', N'1', N'2024-03-14 12:44:01', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (10, N'系统状态', N'common_status', 0, NULL, N'admin', N'2021-01-05 17:03:48', N'', N'2022-02-01 16:21:28', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (11, N'Boolean 是否类型', N'infra_boolean_string', 0, N'boolean 转是否', N'', N'2021-01-19 03:20:08', N'', N'2022-02-01 16:37:10', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (104, N'登陆结果', N'system_login_result', 0, N'登陆结果', N'', N'2021-01-18 06:17:11', N'', N'2022-02-01 16:36:00', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (106, N'代码生成模板类型', N'infra_codegen_template_type', 0, NULL, N'', N'2021-02-05 07:08:06', N'1', N'2022-05-16 20:26:50', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (107, N'定时任务状态', N'infra_job_status', 0, NULL, N'', N'2021-02-07 07:44:16', N'', N'2022-02-01 16:51:11', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (108, N'定时任务日志状态', N'infra_job_log_status', 0, NULL, N'', N'2021-02-08 10:03:51', N'', N'2022-02-01 16:50:43', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (109, N'用户类型', N'user_type', 0, NULL, N'', N'2021-02-26 00:15:51', N'', N'2021-02-26 00:15:51', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (110, N'API 异常数据的处理状态', N'infra_api_error_log_process_status', 0, NULL, N'', N'2021-02-26 07:07:01', N'', N'2022-02-01 16:50:53', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (111, N'短信渠道编码', N'system_sms_channel_code', 0, NULL, N'1', N'2021-04-05 01:04:50', N'1', N'2022-02-16 02:09:08', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (112, N'短信模板的类型', N'system_sms_template_type', 0, NULL, N'1', N'2021-04-05 21:50:43', N'1', N'2022-02-01 16:35:06', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (113, N'短信发送状态', N'system_sms_send_status', 0, NULL, N'1', N'2021-04-11 20:18:03', N'1', N'2022-02-01 16:35:09', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (114, N'短信接收状态', N'system_sms_receive_status', 0, NULL, N'1', N'2021-04-11 20:27:14', N'1', N'2022-02-01 16:35:14', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (116, N'登陆日志的类型', N'system_login_type', 0, N'登陆日志的类型', N'1', N'2021-10-06 00:50:46', N'1', N'2022-02-01 16:35:56', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (117, N'OA 请假类型', N'bpm_oa_leave_type', 0, NULL, N'1', N'2021-09-21 22:34:33', N'1', N'2022-01-22 10:41:37', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (130, N'支付渠道编码类型', N'pay_channel_code', 0, N'支付渠道的编码', N'1', N'2021-12-03 10:35:08', N'1', N'2023-07-10 10:11:39', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (131, N'支付回调状态', N'pay_notify_status', 0, N'支付回调状态（包括退款回调）', N'1', N'2021-12-03 10:53:29', N'1', N'2023-07-19 18:09:43', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (132, N'支付订单状态', N'pay_order_status', 0, N'支付订单状态', N'1', N'2021-12-03 11:17:50', N'1', N'2021-12-03 11:17:50', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (134, N'退款订单状态', N'pay_refund_status', 0, N'退款订单状态', N'1', N'2021-12-10 16:42:50', N'1', N'2023-07-19 10:13:17', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (139, N'流程实例的状态', N'bpm_process_instance_status', 0, N'流程实例的状态', N'1', N'2022-01-07 23:46:42', N'1', N'2022-01-07 23:46:42', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (140, N'流程实例的结果', N'bpm_task_status', 0, N'流程实例的结果', N'1', N'2022-01-07 23:48:10', N'1', N'2024-03-08 22:42:03', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (141, N'流程的表单类型', N'bpm_model_form_type', 0, N'流程的表单类型', N'103', N'2022-01-11 23:50:45', N'103', N'2022-01-11 23:50:45', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (142, N'任务分配规则的类型', N'bpm_task_candidate_strategy', 0, N'BPM 任务的候选人的策略', N'103', N'2022-01-12 23:21:04', N'103', N'2024-03-06 02:53:59', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (144, N'代码生成的场景枚举', N'infra_codegen_scene', 0, N'代码生成的场景枚举', N'1', N'2022-02-02 13:14:45', N'1', N'2022-03-10 16:33:46', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (145, N'角色类型', N'system_role_type', 0, N'角色类型', N'1', N'2022-02-16 13:01:46', N'1', N'2022-02-16 13:01:46', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (146, N'文件存储器', N'infra_file_storage', 0, N'文件存储器', N'1', N'2022-03-15 00:24:38', N'1', N'2022-03-15 00:24:38', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (147, N'OAuth 2.0 授权类型', N'system_oauth2_grant_type', 0, N'OAuth 2.0 授权类型（模式）', N'1', N'2022-05-12 00:20:52', N'1', N'2022-05-11 16:25:49', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (149, N'商品 SPU 状态', N'product_spu_status', 0, N'商品 SPU 状态', N'1', N'2022-10-24 21:19:04', N'1', N'2022-10-24 21:19:08', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (150, N'优惠类型', N'promotion_discount_type', 0, N'优惠类型', N'1', N'2022-11-01 12:46:06', N'1', N'2022-11-01 12:46:06', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (151, N'优惠劵模板的有限期类型', N'promotion_coupon_template_validity_type', 0, N'优惠劵模板的有限期类型', N'1', N'2022-11-02 00:06:20', N'1', N'2022-11-04 00:08:26', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (152, N'营销的商品范围', N'promotion_product_scope', 0, N'营销的商品范围', N'1', N'2022-11-02 00:28:01', N'1', N'2022-11-02 00:28:01', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (153, N'优惠劵的状态', N'promotion_coupon_status', 0, N'优惠劵的状态', N'1', N'2022-11-04 00:14:49', N'1', N'2022-11-04 00:14:49', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (154, N'优惠劵的领取方式', N'promotion_coupon_take_type', 0, N'优惠劵的领取方式', N'1', N'2022-11-04 19:12:27', N'1', N'2022-11-04 19:12:27', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (155, N'促销活动的状态', N'promotion_activity_status', 0, N'促销活动的状态', N'1', N'2022-11-04 22:54:23', N'1', N'2022-11-04 22:54:23', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (156, N'营销的条件类型', N'promotion_condition_type', 0, N'营销的条件类型', N'1', N'2022-11-04 22:59:23', N'1', N'2022-11-04 22:59:23', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (157, N'交易售后状态', N'trade_after_sale_status', 0, N'交易售后状态', N'1', N'2022-11-19 20:52:56', N'1', N'2022-11-19 20:52:56', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (158, N'交易售后的类型', N'trade_after_sale_type', 0, N'交易售后的类型', N'1', N'2022-11-19 21:04:09', N'1', N'2022-11-19 21:04:09', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (159, N'交易售后的方式', N'trade_after_sale_way', 0, N'交易售后的方式', N'1', N'2022-11-19 21:39:04', N'1', N'2022-11-19 21:39:04', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (160, N'终端', N'terminal', 0, N'终端', N'1', N'2022-12-10 10:50:50', N'1', N'2022-12-10 10:53:11', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (161, N'交易订单的类型', N'trade_order_type', 0, N'交易订单的类型', N'1', N'2022-12-10 16:33:54', N'1', N'2022-12-10 16:33:54', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (162, N'交易订单的状态', N'trade_order_status', 0, N'交易订单的状态', N'1', N'2022-12-10 16:48:44', N'1', N'2022-12-10 16:48:44', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (163, N'交易订单项的售后状态', N'trade_order_item_after_sale_status', 0, N'交易订单项的售后状态', N'1', N'2022-12-10 20:58:08', N'1', N'2022-12-10 20:58:08', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (164, N'公众号自动回复的请求关键字匹配模式', N'mp_auto_reply_request_match', 0, N'公众号自动回复的请求关键字匹配模式', N'1', N'2023-01-16 23:29:56', N'1', N'2023-01-16 23:29:56', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (165, N'公众号的消息类型', N'mp_message_type', 0, N'公众号的消息类型', N'1', N'2023-01-17 22:17:09', N'1', N'2023-01-17 22:17:09', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (166, N'邮件发送状态', N'system_mail_send_status', 0, N'邮件发送状态', N'1', N'2023-01-26 09:53:13', N'1', N'2023-01-26 09:53:13', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (167, N'站内信模版的类型', N'system_notify_template_type', 0, N'站内信模版的类型', N'1', N'2023-01-28 10:35:10', N'1', N'2023-01-28 10:35:10', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (168, N'代码生成的前端类型', N'infra_codegen_front_type', 0, N'', N'1', N'2023-04-12 23:57:52', N'1', N'2023-04-12 23:57:52', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (170, N'快递计费方式', N'trade_delivery_express_charge_mode', 0, N'用于商城交易模块配送管理', N'1', N'2023-05-21 22:45:03', N'1', N'2023-05-21 22:45:03', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (171, N'积分业务类型', N'member_point_biz_type', 0, N'', N'1', N'2023-06-10 12:15:00', N'1', N'2023-06-28 13:48:20', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (173, N'支付通知类型', N'pay_notify_type', 0, NULL, N'1', N'2023-07-20 12:23:03', N'1', N'2023-07-20 12:23:03', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (174, N'会员经验业务类型', N'member_experience_biz_type', 0, NULL, N'', N'2023-08-22 12:41:01', N'', N'2023-08-22 12:41:01', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (175, N'交易配送类型', N'trade_delivery_type', 0, N'', N'1', N'2023-08-23 00:03:14', N'1', N'2023-08-23 00:03:14', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (176, N'分佣模式', N'brokerage_enabled_condition', 0, NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (177, N'分销关系绑定模式', N'brokerage_bind_mode', 0, NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (178, N'佣金提现类型', N'brokerage_withdraw_type', 0, NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (179, N'佣金记录业务类型', N'brokerage_record_biz_type', 0, NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (180, N'佣金记录状态', N'brokerage_record_status', 0, NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (181, N'佣金提现状态', N'brokerage_withdraw_status', 0, NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (182, N'佣金提现银行', N'brokerage_bank_name', 0, NULL, N'', N'2023-09-28 02:46:05', N'', N'2023-09-28 02:46:05', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (183, N'砍价记录的状态', N'promotion_bargain_record_status', 0, N'', N'1', N'2023-10-05 10:41:08', N'1', N'2023-10-05 10:41:08', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (184, N'拼团记录的状态', N'promotion_combination_record_status', 0, N'', N'1', N'2023-10-08 07:24:25', N'1', N'2023-10-08 07:24:25', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (185, N'回款-回款方式', N'crm_receivable_return_type', 0, N'回款-回款方式', N'1', N'2023-10-18 21:54:10', N'1', N'2023-10-18 21:54:10', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (186, N'CRM 客户行业', N'crm_customer_industry', 0, N'CRM 客户所属行业', N'1', N'2023-10-28 22:57:07', N'1', N'2024-02-18 23:30:22', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (187, N'客户等级', N'crm_customer_level', 0, N'CRM 客户等级', N'1', N'2023-10-28 22:59:12', N'1', N'2023-10-28 15:11:16', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (188, N'客户来源', N'crm_customer_source', 0, N'CRM 客户来源', N'1', N'2023-10-28 23:00:34', N'1', N'2023-10-28 15:11:16', N'0', NULL)
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (600, N'Banner 位置', N'promotion_banner_position', 0, N'', N'1', N'2023-10-08 07:24:25', N'1', N'2023-11-04 13:04:02', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (601, N'社交类型', N'system_social_type', 0, N'', N'1', N'2023-11-04 13:03:54', N'1', N'2023-11-04 13:03:54', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (604, N'产品状态', N'crm_product_status', 0, N'', N'1', N'2023-10-30 21:47:59', N'1', N'2023-10-30 21:48:45', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (605, N'CRM 数据权限的级别', N'crm_permission_level', 0, N'', N'1', N'2023-11-30 09:51:59', N'1', N'2023-11-30 09:51:59', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (606, N'CRM 审批状态', N'crm_audit_status', 0, N'', N'1', N'2023-11-30 18:56:23', N'1', N'2023-11-30 18:56:23', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (607, N'CRM 产品单位', N'crm_product_unit', 0, N'', N'1', N'2023-12-05 23:01:51', N'1', N'2023-12-05 23:01:51', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (608, N'CRM 跟进方式', N'crm_follow_up_type', 0, N'', N'1', N'2024-01-15 20:48:05', N'1', N'2024-01-15 20:48:05', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (609, N'支付转账类型', N'pay_transfer_type', 0, N'', N'1', N'2023-10-28 16:27:18', N'1', N'2023-10-28 16:27:18', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (610, N'转账订单状态', N'pay_transfer_status', 0, N'', N'1', N'2023-10-28 16:18:32', N'1', N'2023-10-28 16:18:32', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (611, N'ERP 库存明细的业务类型', N'erp_stock_record_biz_type', 0, N'ERP 库存明细的业务类型', N'1', N'2024-02-05 18:07:02', N'1', N'2024-02-05 18:07:02', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (612, N'ERP 审批状态', N'erp_audit_status', 0, N'', N'1', N'2024-02-06 00:00:07', N'1', N'2024-02-06 00:00:07', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (613, N'BPM 监听器类型', N'bpm_process_listener_type', 0, N'', N'1', N'2024-03-23 12:52:24', N'1', N'2024-03-09 15:54:28', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (615, N'BPM 监听器值类型', N'bpm_process_listener_value_type', 0, N'', N'1', N'2024-03-23 13:00:31', N'1', N'2024-03-23 13:00:31', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (616, N'时间间隔', N'date_interval', 0, N'', N'1', N'2024-03-29 22:50:09', N'1', N'2024-03-29 22:50:09', N'0', N'1970-01-01 00:00:00')
GO
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted, deleted_time) VALUES (619, N'CRM 商机结束状态类型', N'crm_business_end_status_type', 0, N'', N'1', N'2024-04-13 23:23:00', N'1', N'2024-04-13 23:23:00', N'0', N'1970-01-01 00:00:00')
GO
SET IDENTITY_INSERT system_dict_type OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for system_login_log
-- ----------------------------
DROP TABLE IF EXISTS system_login_log
GO
CREATE TABLE system_login_log
(
    id          bigint                                 NOT NULL PRIMARY KEY IDENTITY,
    log_type    bigint                                 NOT NULL,
    trace_id    nvarchar(64) DEFAULT ''                NOT NULL,
    user_id     bigint       DEFAULT 0                 NOT NULL,
    user_type   tinyint      DEFAULT 0                 NOT NULL,
    username    nvarchar(50) DEFAULT ''                NOT NULL,
    result      tinyint                                NOT NULL,
    user_ip     nvarchar(50)                           NOT NULL,
    user_agent  nvarchar(512)                          NOT NULL,
    creator     nvarchar(64) DEFAULT ''                NULL,
    create_time datetime2    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64) DEFAULT ''                NULL,
    update_time datetime2    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit          DEFAULT 0                 NOT NULL,
    tenant_id   bigint       DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'访问ID',
     'SCHEMA', N'dbo',
     'TABLE', N'system_login_log',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'日志类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_login_log',
     'COLUMN', N'log_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'链路追踪编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_login_log',
     'COLUMN', N'trace_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_login_log',
     'COLUMN', N'user_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_login_log',
     'COLUMN', N'user_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户账号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_login_log',
     'COLUMN', N'username'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'登陆结果',
     'SCHEMA', N'dbo',
     'TABLE', N'system_login_log',
     'COLUMN', N'result'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户 IP',
     'SCHEMA', N'dbo',
     'TABLE', N'system_login_log',
     'COLUMN', N'user_ip'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'浏览器 UA',
     'SCHEMA', N'dbo',
     'TABLE', N'system_login_log',
     'COLUMN', N'user_agent'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_login_log',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_login_log',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_login_log',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_login_log',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_login_log',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_login_log',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'系统访问记录',
     'SCHEMA', N'dbo',
     'TABLE', N'system_login_log'
GO

-- ----------------------------
-- Table structure for system_mail_account
-- ----------------------------
DROP TABLE IF EXISTS system_mail_account
GO
CREATE TABLE system_mail_account
(
    id              bigint                                 NOT NULL PRIMARY KEY IDENTITY,
    mail            nvarchar(255)                          NOT NULL,
    username        nvarchar(255)                          NOT NULL,
    password        nvarchar(255)                          NOT NULL,
    host            nvarchar(255)                          NOT NULL,
    port            int                                    NOT NULL,
    ssl_enable      varchar(1)   DEFAULT '0'               NOT NULL,
    starttls_enable varchar(1)   DEFAULT '0'               NOT NULL,
    creator         nvarchar(64) DEFAULT ''                NULL,
    create_time     datetime2    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater         nvarchar(64) DEFAULT ''                NULL,
    update_time     datetime2    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted         bit          DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'主键',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_account',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'邮箱',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_account',
     'COLUMN', N'mail'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户名',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_account',
     'COLUMN', N'username'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'密码',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_account',
     'COLUMN', N'password'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'SMTP 服务器域名',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_account',
     'COLUMN', N'host'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'SMTP 服务器端口',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_account',
     'COLUMN', N'port'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否开启 SSL',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_account',
     'COLUMN', N'ssl_enable'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否开启 STARTTLS',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_account',
     'COLUMN', N'starttls_enable'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_account',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_account',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_account',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_account',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_account',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'邮箱账号表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_account'
GO

-- ----------------------------
-- Records of system_mail_account
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT system_mail_account ON
GO
INSERT INTO system_mail_account (id, mail, username, password, host, port, ssl_enable, starttls_enable, creator, create_time, updater, update_time, deleted) VALUES (1, N'7684413@qq.com', N'7684413@qq.com', N'1234576', N'127.0.0.1', 8080, N'0', N'0', N'1', N'2023-01-25 17:39:52', N'1', N'2024-04-24 09:13:56', N'0')
GO
INSERT INTO system_mail_account (id, mail, username, password, host, port, ssl_enable, starttls_enable, creator, create_time, updater, update_time, deleted) VALUES (2, N'ydym_test@163.com', N'ydym_test@163.com', N'WBZTEINMIFVRYSOE', N'smtp.163.com', 465, N'1', N'0', N'1', N'2023-01-26 01:26:03', N'1', N'2023-04-12 22:39:38', N'0')
GO
INSERT INTO system_mail_account (id, mail, username, password, host, port, ssl_enable, starttls_enable, creator, create_time, updater, update_time, deleted) VALUES (3, N'76854114@qq.com', N'3335', N'11234', N'yunai1.cn', 466, N'0', N'0', N'1', N'2023-01-27 15:06:38', N'1', N'2023-01-27 07:08:36', N'1')
GO
INSERT INTO system_mail_account (id, mail, username, password, host, port, ssl_enable, starttls_enable, creator, create_time, updater, update_time, deleted) VALUES (4, N'7685413x@qq.com', N'2', N'3', N'4', 5, N'1', N'0', N'1', N'2023-04-12 23:05:06', N'1', N'2023-04-12 15:05:11', N'1')
GO
SET IDENTITY_INSERT system_mail_account OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for system_mail_log
-- ----------------------------
DROP TABLE IF EXISTS system_mail_log
GO
CREATE TABLE system_mail_log
(
    id                bigint                                   NOT NULL PRIMARY KEY IDENTITY,
    user_id           bigint         DEFAULT NULL              NULL,
    user_type         tinyint        DEFAULT NULL              NULL,
    to_mail           nvarchar(255)                            NOT NULL,
    account_id        bigint                                   NOT NULL,
    from_mail         nvarchar(255)                            NOT NULL,
    template_id       bigint                                   NOT NULL,
    template_code     nvarchar(63)                             NOT NULL,
    template_nickname nvarchar(255)  DEFAULT NULL              NULL,
    template_title    nvarchar(255)                            NOT NULL,
    template_content  nvarchar(4000)                           NOT NULL,
    template_params   nvarchar(255)                            NOT NULL,
    send_status       tinyint        DEFAULT 0                 NOT NULL,
    send_time         datetime2      DEFAULT NULL              NULL,
    send_message_id   nvarchar(255)  DEFAULT NULL              NULL,
    send_exception    nvarchar(4000) DEFAULT NULL              NULL,
    creator           nvarchar(64)   DEFAULT ''                NULL,
    create_time       datetime2      DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater           nvarchar(64)   DEFAULT ''                NULL,
    update_time       datetime2      DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted           bit            DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'user_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'user_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'接收邮箱地址',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'to_mail'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'邮箱账号编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'account_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'发送邮箱地址',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'from_mail'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模板编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'template_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模板编码',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'template_code'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模版发送人名称',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'template_nickname'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'邮件标题',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'template_title'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'邮件内容',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'template_content'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'邮件参数',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'template_params'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'发送状态',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'send_status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'发送时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'send_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'发送返回的消息 ID',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'send_message_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'发送异常',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'send_exception'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'邮件日志表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_log'
GO

-- ----------------------------
-- Table structure for system_mail_template
-- ----------------------------
DROP TABLE IF EXISTS system_mail_template
GO
CREATE TABLE system_mail_template
(
    id          bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    name        nvarchar(63)                            NOT NULL,
    code        nvarchar(63)                            NOT NULL,
    account_id  bigint                                  NOT NULL,
    nickname    nvarchar(255) DEFAULT NULL              NULL,
    title       nvarchar(255)                           NOT NULL,
    content     nvarchar(4000)                          NOT NULL,
    params      nvarchar(255)                           NOT NULL,
    status      tinyint                                 NOT NULL,
    remark      nvarchar(255) DEFAULT NULL              NULL,
    creator     nvarchar(64)  DEFAULT ''                NULL,
    create_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64)  DEFAULT ''                NULL,
    update_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit           DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_template',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模板名称',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_template',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模板编码',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_template',
     'COLUMN', N'code'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'发送的邮箱账号编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_template',
     'COLUMN', N'account_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'发送人名称',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_template',
     'COLUMN', N'nickname'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模板标题',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_template',
     'COLUMN', N'title'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模板内容',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_template',
     'COLUMN', N'content'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'参数数组',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_template',
     'COLUMN', N'params'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'开启状态',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_template',
     'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'备注',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_template',
     'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_template',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_template',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_template',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_template',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_template',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'邮件模版表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_mail_template'
GO

-- ----------------------------
-- Records of system_mail_template
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT system_mail_template ON
GO
INSERT INTO system_mail_template (id, name, code, account_id, nickname, title, content, params, status, remark, creator, create_time, updater, update_time, deleted) VALUES (13, N'后台用户短信登录', N'admin-sms-login', 1, N'奥特曼', N'你猜我猜', N'<p>您的验证码是{code}，名字是{name}</p>', N'["code","name"]', 0, N'3', N'1', N'2021-10-11 08:10:00', N'1', N'2023-12-02 19:51:14', N'0')
GO
INSERT INTO system_mail_template (id, name, code, account_id, nickname, title, content, params, status, remark, creator, create_time, updater, update_time, deleted) VALUES (14, N'测试模版', N'test_01', 2, N'芋艿', N'一个标题', N'<p>你是 {key01} 吗？</p><p><br></p><p>是的话，赶紧 {key02} 一下！</p>', N'["key01","key02"]', 0, NULL, N'1', N'2023-01-26 01:27:40', N'1', N'2023-01-27 10:32:16', N'0')
GO
INSERT INTO system_mail_template (id, name, code, account_id, nickname, title, content, params, status, remark, creator, create_time, updater, update_time, deleted) VALUES (15, N'3', N'2', 2, N'7', N'4', N'<p>45</p>', N'[]', 1, N'80', N'1', N'2023-01-27 15:50:35', N'1', N'2023-01-27 16:34:49', N'0')
GO
SET IDENTITY_INSERT system_mail_template OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for system_menu
-- ----------------------------
DROP TABLE IF EXISTS system_menu
GO
CREATE TABLE system_menu
(
    id             bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    name           nvarchar(50)                            NOT NULL,
    permission     nvarchar(100) DEFAULT ''                NOT NULL,
    type           tinyint                                 NOT NULL,
    sort           int           DEFAULT 0                 NOT NULL,
    parent_id      bigint        DEFAULT 0                 NOT NULL,
    path           nvarchar(200) DEFAULT ''                NULL,
    icon           nvarchar(100) DEFAULT '#'               NULL,
    component      nvarchar(255) DEFAULT NULL              NULL,
    component_name nvarchar(255) DEFAULT NULL              NULL,
    status         tinyint       DEFAULT 0                 NOT NULL,
    visible        varchar(1)    DEFAULT '1'               NOT NULL,
    keep_alive     varchar(1)    DEFAULT '1'               NOT NULL,
    always_show    varchar(1)    DEFAULT '1'               NOT NULL,
    creator        nvarchar(64)  DEFAULT ''                NULL,
    create_time    datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater        nvarchar(64)  DEFAULT ''                NULL,
    update_time    datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted        bit           DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'菜单ID',
     'SCHEMA', N'dbo',
     'TABLE', N'system_menu',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'菜单名称',
     'SCHEMA', N'dbo',
     'TABLE', N'system_menu',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'权限标识',
     'SCHEMA', N'dbo',
     'TABLE', N'system_menu',
     'COLUMN', N'permission'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'菜单类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_menu',
     'COLUMN', N'type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'显示顺序',
     'SCHEMA', N'dbo',
     'TABLE', N'system_menu',
     'COLUMN', N'sort'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'父菜单ID',
     'SCHEMA', N'dbo',
     'TABLE', N'system_menu',
     'COLUMN', N'parent_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'路由地址',
     'SCHEMA', N'dbo',
     'TABLE', N'system_menu',
     'COLUMN', N'path'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'菜单图标',
     'SCHEMA', N'dbo',
     'TABLE', N'system_menu',
     'COLUMN', N'icon'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'组件路径',
     'SCHEMA', N'dbo',
     'TABLE', N'system_menu',
     'COLUMN', N'component'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'组件名',
     'SCHEMA', N'dbo',
     'TABLE', N'system_menu',
     'COLUMN', N'component_name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'菜单状态',
     'SCHEMA', N'dbo',
     'TABLE', N'system_menu',
     'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否可见',
     'SCHEMA', N'dbo',
     'TABLE', N'system_menu',
     'COLUMN', N'visible'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否缓存',
     'SCHEMA', N'dbo',
     'TABLE', N'system_menu',
     'COLUMN', N'keep_alive'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否总是显示',
     'SCHEMA', N'dbo',
     'TABLE', N'system_menu',
     'COLUMN', N'always_show'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_menu',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_menu',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_menu',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_menu',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_menu',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'菜单权限表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_menu'
GO

-- ----------------------------
-- Records of system_menu
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT system_menu ON
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1, N'系统管理', N'', 1, 10, 0, N'/system', N'ep:tools', NULL, NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-02-29 01:04:23', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2, N'基础设施', N'', 1, 20, 0, N'/infra', N'ep:monitor', NULL, NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-03-01 08:28:40', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (5, N'OA 示例', N'', 1, 40, 1185, N'oa', N'fa:road', NULL, NULL, 0, N'1', N'1', N'1', N'admin', N'2021-09-20 16:26:19', N'1', N'2024-02-29 12:38:13', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (100, N'用户管理', N'system:user:list', 2, 1, 1, N'user', N'ep:avatar', N'system/user/index', N'SystemUser', 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-02-29 01:02:04', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (101, N'角色管理', N'', 2, 2, 1, N'role', N'ep:user', N'system/role/index', N'SystemRole', 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-02-29 01:03:28', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (102, N'菜单管理', N'', 2, 3, 1, N'menu', N'ep:menu', N'system/menu/index', N'SystemMenu', 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-02-29 01:03:50', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (103, N'部门管理', N'', 2, 4, 1, N'dept', N'fa:address-card', N'system/dept/index', N'SystemDept', 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-02-29 01:06:28', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (104, N'岗位管理', N'', 2, 5, 1, N'post', N'fa:address-book-o', N'system/post/index', N'SystemPost', 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-02-29 01:06:39', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (105, N'字典管理', N'', 2, 6, 1, N'dict', N'ep:collection', N'system/dict/index', N'SystemDictType', 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-02-29 01:07:12', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (106, N'配置管理', N'', 2, 8, 2, N'config', N'fa:connectdevelop', N'infra/config/index', N'InfraConfig', 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-04-23 00:02:45', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (107, N'通知公告', N'', 2, 4, 2739, N'notice', N'ep:takeaway-box', N'system/notice/index', N'SystemNotice', 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-04-22 23:56:17', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (108, N'审计日志', N'', 1, 9, 1, N'log', N'ep:document-copy', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-02-29 01:08:30', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (109, N'令牌管理', N'', 2, 2, 1261, N'token', N'fa:key', N'system/oauth2/token/index', N'SystemTokenClient', 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-02-29 01:13:48', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (110, N'定时任务', N'', 2, 7, 2, N'job', N'fa-solid:tasks', N'infra/job/index', N'InfraJob', 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-02-29 08:57:36', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (111, N'MySQL 监控', N'', 2, 1, 2740, N'druid', N'fa-solid:box', N'infra/druid/index', N'InfraDruid', 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-04-23 00:05:58', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (112, N'Java 监控', N'', 2, 3, 2740, N'admin-server', N'ep:coffee-cup', N'infra/server/index', N'InfraAdminServer', 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-04-23 00:06:57', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (113, N'Redis 监控', N'', 2, 2, 2740, N'redis', N'fa:reddit-square', N'infra/redis/index', N'InfraRedis', 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-04-23 00:06:09', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (114, N'表单构建', N'infra:build:list', 2, 2, 2, N'build', N'fa:wpforms', N'infra/build/index', N'InfraBuild', 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-02-29 08:51:35', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (115, N'代码生成', N'infra:codegen:query', 2, 1, 2, N'codegen', N'ep:document-copy', N'infra/codegen/index', N'InfraCodegen', 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-02-29 08:51:06', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (116, N'API 接口', N'infra:swagger:list', 2, 3, 2, N'swagger', N'fa:fighter-jet', N'infra/swagger/index', N'InfraSwagger', 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-04-23 00:01:24', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (500, N'操作日志', N'', 2, 1, 108, N'operate-log', N'ep:position', N'system/operatelog/index', N'SystemOperateLog', 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-02-29 01:09:59', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (501, N'登录日志', N'', 2, 2, 108, N'login-log', N'ep:promotion', N'system/loginlog/index', N'SystemLoginLog', 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2024-02-29 01:10:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1001, N'用户查询', N'system:user:query', 3, 1, 100, N'', N'#', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1002, N'用户新增', N'system:user:create', 3, 2, 100, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1003, N'用户修改', N'system:user:update', 3, 3, 100, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1004, N'用户删除', N'system:user:delete', 3, 4, 100, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1005, N'用户导出', N'system:user:export', 3, 5, 100, N'', N'#', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1006, N'用户导入', N'system:user:import', 3, 6, 100, N'', N'#', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1007, N'重置密码', N'system:user:update-password', 3, 7, 100, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1008, N'角色查询', N'system:role:query', 3, 1, 101, N'', N'#', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1009, N'角色新增', N'system:role:create', 3, 2, 101, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1010, N'角色修改', N'system:role:update', 3, 3, 101, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1011, N'角色删除', N'system:role:delete', 3, 4, 101, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1012, N'角色导出', N'system:role:export', 3, 5, 101, N'', N'#', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1013, N'菜单查询', N'system:menu:query', 3, 1, 102, N'', N'#', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1014, N'菜单新增', N'system:menu:create', 3, 2, 102, N'', N'#', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1015, N'菜单修改', N'system:menu:update', 3, 3, 102, N'', N'#', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1016, N'菜单删除', N'system:menu:delete', 3, 4, 102, N'', N'#', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1017, N'部门查询', N'system:dept:query', 3, 1, 103, N'', N'#', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1018, N'部门新增', N'system:dept:create', 3, 2, 103, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1019, N'部门修改', N'system:dept:update', 3, 3, 103, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1020, N'部门删除', N'system:dept:delete', 3, 4, 103, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1021, N'岗位查询', N'system:post:query', 3, 1, 104, N'', N'#', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1022, N'岗位新增', N'system:post:create', 3, 2, 104, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1023, N'岗位修改', N'system:post:update', 3, 3, 104, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1024, N'岗位删除', N'system:post:delete', 3, 4, 104, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1025, N'岗位导出', N'system:post:export', 3, 5, 104, N'', N'#', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1026, N'字典查询', N'system:dict:query', 3, 1, 105, N'#', N'#', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1027, N'字典新增', N'system:dict:create', 3, 2, 105, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1028, N'字典修改', N'system:dict:update', 3, 3, 105, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1029, N'字典删除', N'system:dict:delete', 3, 4, 105, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1030, N'字典导出', N'system:dict:export', 3, 5, 105, N'#', N'#', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1031, N'配置查询', N'infra:config:query', 3, 1, 106, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1032, N'配置新增', N'infra:config:create', 3, 2, 106, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1033, N'配置修改', N'infra:config:update', 3, 3, 106, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1034, N'配置删除', N'infra:config:delete', 3, 4, 106, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1035, N'配置导出', N'infra:config:export', 3, 5, 106, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1036, N'公告查询', N'system:notice:query', 3, 1, 107, N'#', N'#', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1037, N'公告新增', N'system:notice:create', 3, 2, 107, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1038, N'公告修改', N'system:notice:update', 3, 3, 107, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1039, N'公告删除', N'system:notice:delete', 3, 4, 107, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1040, N'操作查询', N'system:operate-log:query', 3, 1, 500, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1042, N'日志导出', N'system:operate-log:export', 3, 2, 500, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1043, N'登录查询', N'system:login-log:query', 3, 1, 501, N'#', N'#', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1045, N'日志导出', N'system:login-log:export', 3, 3, 501, N'#', N'#', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1046, N'令牌列表', N'system:oauth2-token:page', 3, 1, 109, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-05-09 23:54:42', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1048, N'令牌删除', N'system:oauth2-token:delete', 3, 2, 109, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-05-09 23:54:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1050, N'任务新增', N'infra:job:create', 3, 2, 110, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1051, N'任务修改', N'infra:job:update', 3, 3, 110, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1052, N'任务删除', N'infra:job:delete', 3, 4, 110, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1053, N'状态修改', N'infra:job:update', 3, 5, 110, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1054, N'任务导出', N'infra:job:export', 3, 7, 110, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1056, N'生成修改', N'infra:codegen:update', 3, 2, 115, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1057, N'生成删除', N'infra:codegen:delete', 3, 3, 115, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1058, N'导入代码', N'infra:codegen:create', 3, 2, 115, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1059, N'预览代码', N'infra:codegen:preview', 3, 4, 115, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1060, N'生成代码', N'infra:codegen:download', 3, 5, 115, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'admin', N'2021-01-05 17:03:48', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1063, N'设置角色菜单权限', N'system:permission:assign-role-menu', 3, 6, 101, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-01-06 17:53:44', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1064, N'设置角色数据权限', N'system:permission:assign-role-data-scope', 3, 7, 101, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-01-06 17:56:31', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1065, N'设置用户角色', N'system:permission:assign-user-role', 3, 8, 101, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-01-07 10:23:28', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1066, N'获得 Redis 监控信息', N'infra:redis:get-monitor-info', 3, 1, 113, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-01-26 01:02:31', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1067, N'获得 Redis Key 列表', N'infra:redis:get-key-list', 3, 2, 113, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-01-26 01:02:52', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1070, N'代码生成案例', N'', 1, 1, 2, N'demo', N'ep:aim', N'infra/testDemo/index', NULL, 0, N'1', N'1', N'1', N'', N'2021-02-06 12:42:49', N'1', N'2023-11-15 23:45:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1075, N'任务触发', N'infra:job:trigger', 3, 8, 110, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-02-07 13:03:10', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1077, N'链路追踪', N'', 2, 4, 2740, N'skywalking', N'fa:eye', N'infra/skywalking/index', N'InfraSkyWalking', 0, N'1', N'1', N'1', N'', N'2021-02-08 20:41:31', N'1', N'2024-04-23 00:07:15', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1078, N'访问日志', N'', 2, 1, 1083, N'api-access-log', N'ep:place', N'infra/apiAccessLog/index', N'InfraApiAccessLog', 0, N'1', N'1', N'1', N'', N'2021-02-26 01:32:59', N'1', N'2024-02-29 08:54:57', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1082, N'日志导出', N'infra:api-access-log:export', 3, 2, 1078, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-02-26 01:32:59', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1083, N'API 日志', N'', 2, 4, 2, N'log', N'fa:tasks', NULL, NULL, 0, N'1', N'1', N'1', N'', N'2021-02-26 02:18:24', N'1', N'2024-04-22 23:58:36', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1084, N'错误日志', N'infra:api-error-log:query', 2, 2, 1083, N'api-error-log', N'ep:warning-filled', N'infra/apiErrorLog/index', N'InfraApiErrorLog', 0, N'1', N'1', N'1', N'', N'2021-02-26 07:53:20', N'1', N'2024-02-29 08:55:17', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1085, N'日志处理', N'infra:api-error-log:update-status', 3, 2, 1084, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-02-26 07:53:20', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1086, N'日志导出', N'infra:api-error-log:export', 3, 3, 1084, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-02-26 07:53:20', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1087, N'任务查询', N'infra:job:query', 3, 1, 110, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2021-03-10 01:26:19', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1088, N'日志查询', N'infra:api-access-log:query', 3, 1, 1078, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2021-03-10 01:28:04', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1089, N'日志查询', N'infra:api-error-log:query', 3, 1, 1084, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2021-03-10 01:29:09', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1090, N'文件列表', N'', 2, 5, 1243, N'file', N'ep:upload-filled', N'infra/file/index', N'InfraFile', 0, N'1', N'1', N'1', N'', N'2021-03-12 20:16:20', N'1', N'2024-02-29 08:53:02', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1091, N'文件查询', N'infra:file:query', 3, 1, 1090, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-03-12 20:16:20', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1092, N'文件删除', N'infra:file:delete', 3, 4, 1090, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-03-12 20:16:20', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1093, N'短信管理', N'', 1, 1, 2739, N'sms', N'ep:message', NULL, NULL, 0, N'1', N'1', N'1', N'1', N'2021-04-05 01:10:16', N'1', N'2024-04-22 23:56:03', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1094, N'短信渠道', N'', 2, 0, 1093, N'sms-channel', N'fa:stack-exchange', N'system/sms/channel/index', N'SystemSmsChannel', 0, N'1', N'1', N'1', N'', N'2021-04-01 11:07:15', N'1', N'2024-02-29 01:15:54', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1095, N'短信渠道查询', N'system:sms-channel:query', 3, 1, 1094, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-04-01 11:07:15', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1096, N'短信渠道创建', N'system:sms-channel:create', 3, 2, 1094, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-04-01 11:07:15', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1097, N'短信渠道更新', N'system:sms-channel:update', 3, 3, 1094, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-04-01 11:07:15', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1098, N'短信渠道删除', N'system:sms-channel:delete', 3, 4, 1094, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-04-01 11:07:15', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1100, N'短信模板', N'', 2, 1, 1093, N'sms-template', N'ep:connection', N'system/sms/template/index', N'SystemSmsTemplate', 0, N'1', N'1', N'1', N'', N'2021-04-01 17:35:17', N'1', N'2024-02-29 01:16:18', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1101, N'短信模板查询', N'system:sms-template:query', 3, 1, 1100, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-04-01 17:35:17', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1102, N'短信模板创建', N'system:sms-template:create', 3, 2, 1100, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-04-01 17:35:17', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1103, N'短信模板更新', N'system:sms-template:update', 3, 3, 1100, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-04-01 17:35:17', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1104, N'短信模板删除', N'system:sms-template:delete', 3, 4, 1100, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-04-01 17:35:17', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1105, N'短信模板导出', N'system:sms-template:export', 3, 5, 1100, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-04-01 17:35:17', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1106, N'发送测试短信', N'system:sms-template:send-sms', 3, 6, 1100, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2021-04-11 00:26:40', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1107, N'短信日志', N'', 2, 2, 1093, N'sms-log', N'fa:edit', N'system/sms/log/index', N'SystemSmsLog', 0, N'1', N'1', N'1', N'', N'2021-04-11 08:37:05', N'1', N'2024-02-29 08:49:02', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1108, N'短信日志查询', N'system:sms-log:query', 3, 1, 1107, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-04-11 08:37:05', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1109, N'短信日志导出', N'system:sms-log:export', 3, 5, 1107, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-04-11 08:37:05', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1117, N'支付管理', N'', 1, 30, 0, N'/pay', N'ep:money', NULL, NULL, 0, N'1', N'1', N'1', N'1', N'2021-12-25 16:43:41', N'1', N'2024-02-29 08:58:38', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1118, N'请假查询', N'', 2, 0, 5, N'leave', N'fa:leanpub', N'bpm/oa/leave/index', N'BpmOALeave', 0, N'1', N'1', N'1', N'', N'2021-09-20 08:51:03', N'1', N'2024-02-29 12:38:21', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1119, N'请假申请查询', N'bpm:oa-leave:query', 3, 1, 1118, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-09-20 08:51:03', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1120, N'请假申请创建', N'bpm:oa-leave:create', 3, 2, 1118, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-09-20 08:51:03', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1126, N'应用信息', N'', 2, 1, 1117, N'app', N'fa:apple', N'pay/app/index', N'PayApp', 0, N'1', N'1', N'1', N'', N'2021-11-10 01:13:30', N'1', N'2024-02-29 08:59:55', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1127, N'支付应用信息查询', N'pay:app:query', 3, 1, 1126, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-11-10 01:13:31', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1128, N'支付应用信息创建', N'pay:app:create', 3, 2, 1126, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-11-10 01:13:31', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1129, N'支付应用信息更新', N'pay:app:update', 3, 3, 1126, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-11-10 01:13:31', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1130, N'支付应用信息删除', N'pay:app:delete', 3, 4, 1126, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-11-10 01:13:31', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1132, N'秘钥解析', N'pay:channel:parsing', 3, 6, 1129, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2021-11-08 15:15:47', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1133, N'支付商户信息查询', N'pay:merchant:query', 3, 1, 1132, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-11-10 01:13:41', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1134, N'支付商户信息创建', N'pay:merchant:create', 3, 2, 1132, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-11-10 01:13:41', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1135, N'支付商户信息更新', N'pay:merchant:update', 3, 3, 1132, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-11-10 01:13:41', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1136, N'支付商户信息删除', N'pay:merchant:delete', 3, 4, 1132, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-11-10 01:13:41', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1137, N'支付商户信息导出', N'pay:merchant:export', 3, 5, 1132, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-11-10 01:13:41', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1138, N'租户列表', N'', 2, 0, 1224, N'list', N'ep:house', N'system/tenant/index', N'SystemTenant', 0, N'1', N'1', N'1', N'', N'2021-12-14 12:31:43', N'1', N'2024-02-29 01:01:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1139, N'租户查询', N'system:tenant:query', 3, 1, 1138, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-12-14 12:31:44', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1140, N'租户创建', N'system:tenant:create', 3, 2, 1138, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-12-14 12:31:44', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1141, N'租户更新', N'system:tenant:update', 3, 3, 1138, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-12-14 12:31:44', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1142, N'租户删除', N'system:tenant:delete', 3, 4, 1138, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-12-14 12:31:44', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1143, N'租户导出', N'system:tenant:export', 3, 5, 1138, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-12-14 12:31:44', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1150, N'秘钥解析', N'', 3, 6, 1129, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2021-11-08 15:15:47', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1161, N'退款订单', N'', 2, 3, 1117, N'refund', N'fa:registered', N'pay/refund/index', N'PayRefund', 0, N'1', N'1', N'1', N'', N'2021-12-25 08:29:07', N'1', N'2024-02-29 08:59:20', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1162, N'退款订单查询', N'pay:refund:query', 3, 1, 1161, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-12-25 08:29:07', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1163, N'退款订单创建', N'pay:refund:create', 3, 2, 1161, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-12-25 08:29:07', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1164, N'退款订单更新', N'pay:refund:update', 3, 3, 1161, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-12-25 08:29:07', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1165, N'退款订单删除', N'pay:refund:delete', 3, 4, 1161, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-12-25 08:29:07', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1166, N'退款订单导出', N'pay:refund:export', 3, 5, 1161, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-12-25 08:29:07', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1173, N'支付订单', N'', 2, 2, 1117, N'order', N'fa:cc-paypal', N'pay/order/index', N'PayOrder', 0, N'1', N'1', N'1', N'', N'2021-12-25 08:49:43', N'1', N'2024-02-29 08:59:43', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1174, N'支付订单查询', N'pay:order:query', 3, 1, 1173, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-12-25 08:49:43', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1175, N'支付订单创建', N'pay:order:create', 3, 2, 1173, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-12-25 08:49:43', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1176, N'支付订单更新', N'pay:order:update', 3, 3, 1173, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-12-25 08:49:43', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1177, N'支付订单删除', N'pay:order:delete', 3, 4, 1173, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-12-25 08:49:43', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1178, N'支付订单导出', N'pay:order:export', 3, 5, 1173, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-12-25 08:49:43', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1185, N'工作流程', N'', 1, 50, 0, N'/bpm', N'fa:medium', NULL, NULL, 0, N'1', N'1', N'1', N'1', N'2021-12-30 20:26:36', N'1', N'2024-02-29 12:43:43', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1186, N'流程管理', N'', 1, 10, 1185, N'manager', N'fa:dedent', NULL, NULL, 0, N'1', N'1', N'1', N'1', N'2021-12-30 20:28:30', N'1', N'2024-02-29 12:36:02', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1187, N'流程表单', N'', 2, 2, 1186, N'form', N'fa:hdd-o', N'bpm/form/index', N'BpmForm', 0, N'1', N'1', N'1', N'', N'2021-12-30 12:38:22', N'1', N'2024-03-19 12:25:25', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1188, N'表单查询', N'bpm:form:query', 3, 1, 1187, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-12-30 12:38:22', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1189, N'表单创建', N'bpm:form:create', 3, 2, 1187, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-12-30 12:38:22', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1190, N'表单更新', N'bpm:form:update', 3, 3, 1187, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-12-30 12:38:22', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1191, N'表单删除', N'bpm:form:delete', 3, 4, 1187, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-12-30 12:38:22', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1192, N'表单导出', N'bpm:form:export', 3, 5, 1187, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2021-12-30 12:38:22', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1193, N'流程模型', N'', 2, 1, 1186, N'model', N'fa-solid:project-diagram', N'bpm/model/index', N'BpmModel', 0, N'1', N'1', N'1', N'1', N'2021-12-31 23:24:58', N'1', N'2024-03-19 12:25:19', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1194, N'模型查询', N'bpm:model:query', 3, 1, 1193, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2022-01-03 19:01:10', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1195, N'模型创建', N'bpm:model:create', 3, 2, 1193, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2022-01-03 19:01:24', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1196, N'模型导入', N'bpm:model:import', 3, 3, 1193, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2022-01-03 19:01:35', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1197, N'模型更新', N'bpm:model:update', 3, 4, 1193, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2022-01-03 19:02:28', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1198, N'模型删除', N'bpm:model:delete', 3, 5, 1193, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2022-01-03 19:02:43', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1199, N'模型发布', N'bpm:model:deploy', 3, 6, 1193, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2022-01-03 19:03:24', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1200, N'审批中心', N'', 2, 20, 1185, N'task', N'fa:tasks', NULL, NULL, 0, N'1', N'1', N'1', N'1', N'2022-01-07 23:51:48', N'1', N'2024-03-21 00:33:15', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1201, N'我的流程', N'', 2, 1, 1200, N'my', N'fa-solid:book', N'bpm/processInstance/index', N'BpmProcessInstanceMy', 0, N'1', N'1', N'1', N'', N'2022-01-07 15:53:44', N'1', N'2024-03-21 23:52:12', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1202, N'流程实例的查询', N'bpm:process-instance:query', 3, 1, 1201, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-01-07 15:53:44', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1207, N'待办任务', N'', 2, 10, 1200, N'todo', N'fa:slack', N'bpm/task/todo/index', N'BpmTodoTask', 0, N'1', N'1', N'1', N'1', N'2022-01-08 10:33:37', N'1', N'2024-02-29 12:37:39', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1208, N'已办任务', N'', 2, 20, 1200, N'done', N'fa:delicious', N'bpm/task/done/index', N'BpmDoneTask', 0, N'1', N'1', N'1', N'1', N'2022-01-08 10:34:13', N'1', N'2024-02-29 12:37:54', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1209, N'用户分组', N'', 2, 4, 1186, N'user-group', N'fa:user-secret', N'bpm/group/index', N'BpmUserGroup', 0, N'1', N'1', N'1', N'', N'2022-01-14 02:14:20', N'1', N'2024-03-21 23:55:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1210, N'用户组查询', N'bpm:user-group:query', 3, 1, 1209, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-01-14 02:14:20', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1211, N'用户组创建', N'bpm:user-group:create', 3, 2, 1209, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-01-14 02:14:20', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1212, N'用户组更新', N'bpm:user-group:update', 3, 3, 1209, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-01-14 02:14:20', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1213, N'用户组删除', N'bpm:user-group:delete', 3, 4, 1209, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-01-14 02:14:20', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1215, N'流程定义查询', N'bpm:process-definition:query', 3, 10, 1193, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2022-01-23 00:21:43', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1216, N'流程任务分配规则查询', N'bpm:task-assign-rule:query', 3, 20, 1193, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2022-01-23 00:26:53', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1217, N'流程任务分配规则创建', N'bpm:task-assign-rule:create', 3, 21, 1193, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2022-01-23 00:28:15', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1218, N'流程任务分配规则更新', N'bpm:task-assign-rule:update', 3, 22, 1193, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2022-01-23 00:28:41', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1219, N'流程实例的创建', N'bpm:process-instance:create', 3, 2, 1201, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2022-01-23 00:36:15', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1220, N'流程实例的取消', N'bpm:process-instance:cancel', 3, 3, 1201, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2022-01-23 00:36:33', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1221, N'流程任务的查询', N'bpm:task:query', 3, 1, 1207, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2022-01-23 00:38:52', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1222, N'流程任务的更新', N'bpm:task:update', 3, 2, 1207, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2022-01-23 00:39:24', N'1', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1224, N'租户管理', N'', 2, 0, 1, N'tenant', N'fa-solid:house-user', NULL, NULL, 0, N'1', N'1', N'1', N'1', N'2022-02-20 01:41:13', N'1', N'2024-02-29 00:59:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1225, N'租户套餐', N'', 2, 0, 1224, N'package', N'fa:bars', N'system/tenantPackage/index', N'SystemTenantPackage', 0, N'1', N'1', N'1', N'', N'2022-02-19 17:44:06', N'1', N'2024-02-29 01:01:43', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1226, N'租户套餐查询', N'system:tenant-package:query', 3, 1, 1225, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-02-19 17:44:06', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1227, N'租户套餐创建', N'system:tenant-package:create', 3, 2, 1225, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-02-19 17:44:06', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1228, N'租户套餐更新', N'system:tenant-package:update', 3, 3, 1225, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-02-19 17:44:06', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1229, N'租户套餐删除', N'system:tenant-package:delete', 3, 4, 1225, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-02-19 17:44:06', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1237, N'文件配置', N'', 2, 0, 1243, N'file-config', N'fa-solid:file-signature', N'infra/fileConfig/index', N'InfraFileConfig', 0, N'1', N'1', N'1', N'', N'2022-03-15 14:35:28', N'1', N'2024-02-29 08:52:54', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1238, N'文件配置查询', N'infra:file-config:query', 3, 1, 1237, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-03-15 14:35:28', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1239, N'文件配置创建', N'infra:file-config:create', 3, 2, 1237, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-03-15 14:35:28', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1240, N'文件配置更新', N'infra:file-config:update', 3, 3, 1237, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-03-15 14:35:28', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1241, N'文件配置删除', N'infra:file-config:delete', 3, 4, 1237, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-03-15 14:35:28', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1242, N'文件配置导出', N'infra:file-config:export', 3, 5, 1237, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-03-15 14:35:28', N'', N'2022-04-20 17:03:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1243, N'文件管理', N'', 2, 6, 2, N'file', N'ep:files', NULL, N'', 0, N'1', N'1', N'1', N'1', N'2022-03-16 23:47:40', N'1', N'2024-04-23 00:02:11', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1254, N'作者动态', N'', 1, 0, 0, N'https://www.iocoder.cn', N'ep:avatar', NULL, NULL, 0, N'1', N'1', N'1', N'1', N'2022-04-23 01:03:15', N'1', N'2023-12-08 23:40:01', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1255, N'数据源配置', N'', 2, 1, 2, N'data-source-config', N'ep:data-analysis', N'infra/dataSourceConfig/index', N'InfraDataSourceConfig', 0, N'1', N'1', N'1', N'', N'2022-04-27 14:37:32', N'1', N'2024-02-29 08:51:25', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1256, N'数据源配置查询', N'infra:data-source-config:query', 3, 1, 1255, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-04-27 14:37:32', N'', N'2022-04-27 14:37:32', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1257, N'数据源配置创建', N'infra:data-source-config:create', 3, 2, 1255, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-04-27 14:37:32', N'', N'2022-04-27 14:37:32', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1258, N'数据源配置更新', N'infra:data-source-config:update', 3, 3, 1255, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-04-27 14:37:32', N'', N'2022-04-27 14:37:32', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1259, N'数据源配置删除', N'infra:data-source-config:delete', 3, 4, 1255, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-04-27 14:37:32', N'', N'2022-04-27 14:37:32', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1260, N'数据源配置导出', N'infra:data-source-config:export', 3, 5, 1255, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-04-27 14:37:32', N'', N'2022-04-27 14:37:32', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1261, N'OAuth 2.0', N'', 2, 10, 1, N'oauth2', N'fa:dashcube', NULL, NULL, 0, N'1', N'1', N'1', N'1', N'2022-05-09 23:38:17', N'1', N'2024-02-29 01:12:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1263, N'应用管理', N'', 2, 0, 1261, N'oauth2/application', N'fa:hdd-o', N'system/oauth2/client/index', N'SystemOAuth2Client', 0, N'1', N'1', N'1', N'', N'2022-05-10 16:26:33', N'1', N'2024-02-29 01:13:14', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1264, N'客户端查询', N'system:oauth2-client:query', 3, 1, 1263, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-05-10 16:26:33', N'1', N'2022-05-11 00:31:06', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1265, N'客户端创建', N'system:oauth2-client:create', 3, 2, 1263, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-05-10 16:26:33', N'1', N'2022-05-11 00:31:23', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1266, N'客户端更新', N'system:oauth2-client:update', 3, 3, 1263, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-05-10 16:26:33', N'1', N'2022-05-11 00:31:28', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1267, N'客户端删除', N'system:oauth2-client:delete', 3, 4, 1263, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-05-10 16:26:33', N'1', N'2022-05-11 00:31:33', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1281, N'报表管理', N'', 2, 40, 0, N'/report', N'ep:pie-chart', NULL, NULL, 0, N'1', N'1', N'1', N'1', N'2022-07-10 20:22:15', N'1', N'2024-02-29 12:33:03', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (1282, N'报表设计器', N'', 2, 1, 1281, N'jimu-report', N'ep:trend-charts', N'report/jmreport/index', N'GoView', 0, N'1', N'1', N'1', N'1', N'2022-07-10 20:26:36', N'1', N'2024-02-29 12:33:54', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2000, N'商品中心', N'', 1, 60, 2362, N'product', N'fa:product-hunt', NULL, NULL, 0, N'1', N'1', N'1', N'', N'2022-07-29 15:53:53', N'1', N'2023-09-30 11:52:36', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2002, N'商品分类', N'', 2, 2, 2000, N'category', N'ep:cellphone', N'mall/product/category/index', N'ProductCategory', 0, N'1', N'1', N'1', N'', N'2022-07-29 15:53:53', N'1', N'2023-08-21 10:27:15', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2003, N'分类查询', N'product:category:query', 3, 1, 2002, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-07-29 15:53:53', N'', N'2022-07-29 15:53:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2004, N'分类创建', N'product:category:create', 3, 2, 2002, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-07-29 15:53:53', N'', N'2022-07-29 15:53:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2005, N'分类更新', N'product:category:update', 3, 3, 2002, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-07-29 15:53:53', N'', N'2022-07-29 15:53:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2006, N'分类删除', N'product:category:delete', 3, 4, 2002, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-07-29 15:53:53', N'', N'2022-07-29 15:53:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2008, N'商品品牌', N'', 2, 3, 2000, N'brand', N'ep:chicken', N'mall/product/brand/index', N'ProductBrand', 0, N'1', N'1', N'1', N'', N'2022-07-30 13:52:44', N'1', N'2023-08-21 10:27:28', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2009, N'品牌查询', N'product:brand:query', 3, 1, 2008, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-07-30 13:52:44', N'', N'2022-07-30 13:52:44', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2010, N'品牌创建', N'product:brand:create', 3, 2, 2008, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-07-30 13:52:44', N'', N'2022-07-30 13:52:44', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2011, N'品牌更新', N'product:brand:update', 3, 3, 2008, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-07-30 13:52:44', N'', N'2022-07-30 13:52:44', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2012, N'品牌删除', N'product:brand:delete', 3, 4, 2008, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-07-30 13:52:44', N'', N'2022-07-30 13:52:44', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2014, N'商品列表', N'', 2, 1, 2000, N'spu', N'ep:apple', N'mall/product/spu/index', N'ProductSpu', 0, N'1', N'1', N'1', N'', N'2022-07-30 14:22:58', N'1', N'2023-08-21 10:27:01', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2015, N'商品查询', N'product:spu:query', 3, 1, 2014, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-07-30 14:22:58', N'', N'2022-07-30 14:22:58', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2016, N'商品创建', N'product:spu:create', 3, 2, 2014, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-07-30 14:22:58', N'', N'2022-07-30 14:22:58', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2017, N'商品更新', N'product:spu:update', 3, 3, 2014, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-07-30 14:22:58', N'', N'2022-07-30 14:22:58', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2018, N'商品删除', N'product:spu:delete', 3, 4, 2014, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-07-30 14:22:58', N'', N'2022-07-30 14:22:58', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2019, N'商品属性', N'', 2, 4, 2000, N'property', N'ep:cold-drink', N'mall/product/property/index', N'ProductProperty', 0, N'1', N'1', N'1', N'', N'2022-08-01 14:55:35', N'1', N'2023-08-26 11:01:05', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2020, N'规格查询', N'product:property:query', 3, 1, 2019, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-08-01 14:55:35', N'', N'2022-12-12 20:26:24', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2021, N'规格创建', N'product:property:create', 3, 2, 2019, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-08-01 14:55:35', N'', N'2022-12-12 20:26:30', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2022, N'规格更新', N'product:property:update', 3, 3, 2019, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-08-01 14:55:35', N'', N'2022-12-12 20:26:33', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2023, N'规格删除', N'product:property:delete', 3, 4, 2019, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-08-01 14:55:35', N'', N'2022-12-12 20:26:37', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2025, N'Banner', N'', 2, 100, 2387, N'banner', N'fa:bandcamp', N'mall/promotion/banner/index', NULL, 0, N'1', N'1', N'1', N'', N'2022-08-01 14:56:14', N'1', N'2023-10-24 20:20:06', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2026, N'Banner查询', N'promotion:banner:query', 3, 1, 2025, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'', N'2022-08-01 14:56:14', N'1', N'2023-10-24 20:20:18', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2027, N'Banner创建', N'promotion:banner:create', 3, 2, 2025, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'', N'2022-08-01 14:56:14', N'1', N'2023-10-24 20:20:23', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2028, N'Banner更新', N'promotion:banner:update', 3, 3, 2025, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'', N'2022-08-01 14:56:14', N'1', N'2023-10-24 20:20:28', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2029, N'Banner删除', N'promotion:banner:delete', 3, 4, 2025, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'', N'2022-08-01 14:56:14', N'1', N'2023-10-24 20:20:36', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2030, N'营销中心', N'', 1, 70, 2362, N'promotion', N'ep:present', NULL, NULL, 0, N'1', N'1', N'1', N'1', N'2022-10-31 21:25:09', N'1', N'2023-09-30 11:54:27', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2032, N'优惠劵列表', N'', 2, 1, 2365, N'template', N'ep:discount', N'mall/promotion/coupon/template/index', N'PromotionCouponTemplate', 0, N'1', N'1', N'1', N'', N'2022-10-31 22:27:14', N'1', N'2023-10-03 12:40:06', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2033, N'优惠劵模板查询', N'promotion:coupon-template:query', 3, 1, 2032, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-10-31 22:27:14', N'', N'2022-10-31 22:27:14', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2034, N'优惠劵模板创建', N'promotion:coupon-template:create', 3, 2, 2032, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-10-31 22:27:14', N'', N'2022-10-31 22:27:14', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2035, N'优惠劵模板更新', N'promotion:coupon-template:update', 3, 3, 2032, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-10-31 22:27:14', N'', N'2022-10-31 22:27:14', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2036, N'优惠劵模板删除', N'promotion:coupon-template:delete', 3, 4, 2032, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-10-31 22:27:14', N'', N'2022-10-31 22:27:14', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2038, N'领取记录', N'', 2, 2, 2365, N'list', N'ep:collection-tag', N'mall/promotion/coupon/index', N'PromotionCoupon', 0, N'1', N'1', N'1', N'', N'2022-11-03 23:21:31', N'1', N'2023-10-03 12:55:30', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2039, N'优惠劵查询', N'promotion:coupon:query', 3, 1, 2038, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-11-03 23:21:31', N'', N'2022-11-03 23:21:31', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2040, N'优惠劵删除', N'promotion:coupon:delete', 3, 4, 2038, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-11-03 23:21:31', N'', N'2022-11-03 23:21:31', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2041, N'满减送', N'', 2, 10, 2390, N'reward-activity', N'ep:goblet-square-full', N'mall/promotion/rewardActivity/index', N'PromotionRewardActivity', 0, N'1', N'1', N'1', N'', N'2022-11-04 23:47:49', N'1', N'2023-10-21 19:24:46', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2042, N'满减送活动查询', N'promotion:reward-activity:query', 3, 1, 2041, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-11-04 23:47:49', N'', N'2022-11-04 23:47:49', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2043, N'满减送活动创建', N'promotion:reward-activity:create', 3, 2, 2041, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-11-04 23:47:49', N'', N'2022-11-04 23:47:49', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2044, N'满减送活动更新', N'promotion:reward-activity:update', 3, 3, 2041, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-11-04 23:47:50', N'', N'2022-11-04 23:47:50', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2045, N'满减送活动删除', N'promotion:reward-activity:delete', 3, 4, 2041, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-11-04 23:47:50', N'', N'2022-11-04 23:47:50', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2046, N'满减送活动关闭', N'promotion:reward-activity:close', 3, 5, 2041, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2022-11-05 10:42:53', N'1', N'2022-11-05 10:42:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2047, N'限时折扣', N'', 2, 7, 2390, N'discount-activity', N'ep:timer', N'mall/promotion/discountActivity/index', N'PromotionDiscountActivity', 0, N'1', N'1', N'1', N'', N'2022-11-05 17:12:15', N'1', N'2023-10-21 19:24:21', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2048, N'限时折扣活动查询', N'promotion:discount-activity:query', 3, 1, 2047, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-11-05 17:12:15', N'', N'2022-11-05 17:12:15', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2049, N'限时折扣活动创建', N'promotion:discount-activity:create', 3, 2, 2047, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-11-05 17:12:15', N'', N'2022-11-05 17:12:15', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2050, N'限时折扣活动更新', N'promotion:discount-activity:update', 3, 3, 2047, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-11-05 17:12:16', N'', N'2022-11-05 17:12:16', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2051, N'限时折扣活动删除', N'promotion:discount-activity:delete', 3, 4, 2047, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-11-05 17:12:16', N'', N'2022-11-05 17:12:16', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2052, N'限时折扣活动关闭', N'promotion:discount-activity:close', 3, 5, 2047, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-11-05 17:12:16', N'', N'2022-11-05 17:12:16', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2059, N'秒杀商品', N'', 2, 2, 2209, N'activity', N'ep:basketball', N'mall/promotion/seckill/activity/index', N'PromotionSeckillActivity', 0, N'1', N'1', N'1', N'', N'2022-11-06 22:24:49', N'1', N'2023-06-24 18:57:25', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2060, N'秒杀活动查询', N'promotion:seckill-activity:query', 3, 1, 2059, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-11-06 22:24:49', N'', N'2022-11-06 22:24:49', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2061, N'秒杀活动创建', N'promotion:seckill-activity:create', 3, 2, 2059, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-11-06 22:24:49', N'', N'2022-11-06 22:24:49', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2062, N'秒杀活动更新', N'promotion:seckill-activity:update', 3, 3, 2059, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-11-06 22:24:49', N'', N'2022-11-06 22:24:49', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2063, N'秒杀活动删除', N'promotion:seckill-activity:delete', 3, 4, 2059, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-11-06 22:24:49', N'', N'2022-11-06 22:24:49', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2066, N'秒杀时段', N'', 2, 1, 2209, N'config', N'ep:baseball', N'mall/promotion/seckill/config/index', N'PromotionSeckillConfig', 0, N'1', N'1', N'1', N'', N'2022-11-15 19:46:50', N'1', N'2023-06-24 18:57:14', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2067, N'秒杀时段查询', N'promotion:seckill-config:query', 3, 1, 2066, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'', N'2022-11-15 19:46:51', N'1', N'2023-06-24 17:50:25', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2068, N'秒杀时段创建', N'promotion:seckill-config:create', 3, 2, 2066, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'', N'2022-11-15 19:46:51', N'1', N'2023-06-24 17:48:39', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2069, N'秒杀时段更新', N'promotion:seckill-config:update', 3, 3, 2066, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'', N'2022-11-15 19:46:51', N'1', N'2023-06-24 17:50:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2070, N'秒杀时段删除', N'promotion:seckill-config:delete', 3, 4, 2066, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'', N'2022-11-15 19:46:51', N'1', N'2023-06-24 17:50:32', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2072, N'订单中心', N'', 1, 65, 2362, N'trade', N'ep:eleme', NULL, NULL, 0, N'1', N'1', N'1', N'1', N'2022-11-19 18:57:19', N'1', N'2023-09-30 11:54:07', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2073, N'售后退款', N'', 2, 2, 2072, N'after-sale', N'ep:refrigerator', N'mall/trade/afterSale/index', N'TradeAfterSale', 0, N'1', N'1', N'1', N'', N'2022-11-19 20:15:32', N'1', N'2023-10-01 21:42:21', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2074, N'售后查询', N'trade:after-sale:query', 3, 1, 2073, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-11-19 20:15:33', N'1', N'2022-12-10 21:04:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2075, N'秒杀活动关闭', N'promotion:seckill-activity:close', 3, 5, 2059, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2022-11-28 20:20:15', N'1', N'2023-10-03 18:34:28', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2076, N'订单列表', N'', 2, 1, 2072, N'order', N'ep:list', N'mall/trade/order/index', N'TradeOrder', 0, N'1', N'1', N'1', N'1', N'2022-12-10 21:05:44', N'1', N'2023-10-01 21:42:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2083, N'地区管理', N'', 2, 14, 1, N'area', N'fa:map-marker', N'system/area/index', N'SystemArea', 0, N'1', N'1', N'1', N'1', N'2022-12-23 17:35:05', N'1', N'2024-02-29 08:50:28', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2084, N'公众号管理', N'', 1, 100, 0, N'/mp', N'ep:compass', NULL, NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-01 20:11:04', N'1', N'2024-02-29 12:39:30', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2085, N'账号管理', N'', 2, 1, 2084, N'account', N'fa:user', N'mp/account/index', N'MpAccount', 0, N'1', N'1', N'1', N'1', N'2023-01-01 20:13:31', N'1', N'2024-02-29 12:42:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2086, N'新增账号', N'mp:account:create', 3, 1, 2085, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-01 20:21:40', N'1', N'2023-01-07 17:32:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2087, N'修改账号', N'mp:account:update', 3, 2, 2085, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-07 17:32:46', N'1', N'2023-01-07 17:32:46', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2088, N'查询账号', N'mp:account:query', 3, 0, 2085, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-07 17:33:07', N'1', N'2023-01-07 17:33:07', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2089, N'删除账号', N'mp:account:delete', 3, 3, 2085, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-07 17:33:21', N'1', N'2023-01-07 17:33:21', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2090, N'生成二维码', N'mp:account:qr-code', 3, 4, 2085, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-07 17:33:58', N'1', N'2023-01-07 17:33:58', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2091, N'清空 API 配额', N'mp:account:clear-quota', 3, 5, 2085, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-07 18:20:32', N'1', N'2023-01-07 18:20:59', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2092, N'数据统计', N'mp:statistics:query', 2, 2, 2084, N'statistics', N'ep:trend-charts', N'mp/statistics/index', N'MpStatistics', 0, N'1', N'1', N'1', N'1', N'2023-01-07 20:17:36', N'1', N'2024-02-29 12:42:21', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2093, N'标签管理', N'', 2, 3, 2084, N'tag', N'ep:collection-tag', N'mp/tag/index', N'MpTag', 0, N'1', N'1', N'1', N'1', N'2023-01-08 11:37:32', N'1', N'2024-02-29 12:42:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2094, N'查询标签', N'mp:tag:query', 3, 0, 2093, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-08 11:59:03', N'1', N'2023-01-08 11:59:03', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2095, N'新增标签', N'mp:tag:create', 3, 1, 2093, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-08 11:59:23', N'1', N'2023-01-08 11:59:23', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2096, N'修改标签', N'mp:tag:update', 3, 2, 2093, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-08 11:59:41', N'1', N'2023-01-08 11:59:41', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2097, N'删除标签', N'mp:tag:delete', 3, 3, 2093, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-08 12:00:04', N'1', N'2023-01-08 12:00:13', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2098, N'同步标签', N'mp:tag:sync', 3, 4, 2093, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-08 12:00:29', N'1', N'2023-01-08 12:00:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2099, N'粉丝管理', N'', 2, 4, 2084, N'user', N'fa:user-secret', N'mp/user/index', N'MpUser', 0, N'1', N'1', N'1', N'1', N'2023-01-08 16:51:20', N'1', N'2024-02-29 12:42:39', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2100, N'查询粉丝', N'mp:user:query', 3, 0, 2099, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-08 17:16:59', N'1', N'2023-01-08 17:17:23', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2101, N'修改粉丝', N'mp:user:update', 3, 1, 2099, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-08 17:17:11', N'1', N'2023-01-08 17:17:11', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2102, N'同步粉丝', N'mp:user:sync', 3, 2, 2099, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-08 17:17:40', N'1', N'2023-01-08 17:17:40', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2103, N'消息管理', N'', 2, 5, 2084, N'message', N'ep:message', N'mp/message/index', N'MpMessage', 0, N'1', N'1', N'1', N'1', N'2023-01-08 18:44:19', N'1', N'2024-02-29 12:42:50', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2104, N'图文发表记录', N'', 2, 10, 2084, N'free-publish', N'ep:edit-pen', N'mp/freePublish/index', N'MpFreePublish', 0, N'1', N'1', N'1', N'1', N'2023-01-13 00:30:50', N'1', N'2024-02-29 12:43:31', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2105, N'查询发布列表', N'mp:free-publish:query', 3, 1, 2104, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-13 07:19:17', N'1', N'2023-01-13 07:19:17', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2106, N'发布草稿', N'mp:free-publish:submit', 3, 2, 2104, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-13 07:19:46', N'1', N'2023-01-13 07:19:46', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2107, N'删除发布记录', N'mp:free-publish:delete', 3, 3, 2104, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-13 07:20:01', N'1', N'2023-01-13 07:20:01', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2108, N'图文草稿箱', N'', 2, 9, 2084, N'draft', N'ep:edit', N'mp/draft/index', N'MpDraft', 0, N'1', N'1', N'1', N'1', N'2023-01-13 07:40:21', N'1', N'2024-02-29 12:43:26', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2109, N'新建草稿', N'mp:draft:create', 3, 1, 2108, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-13 23:15:30', N'1', N'2023-01-13 23:15:44', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2110, N'修改草稿', N'mp:draft:update', 3, 2, 2108, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-14 10:08:47', N'1', N'2023-01-14 10:08:47', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2111, N'查询草稿', N'mp:draft:query', 3, 0, 2108, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-14 10:09:01', N'1', N'2023-01-14 10:09:01', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2112, N'删除草稿', N'mp:draft:delete', 3, 3, 2108, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-14 10:09:19', N'1', N'2023-01-14 10:09:19', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2113, N'素材管理', N'', 2, 8, 2084, N'material', N'ep:basketball', N'mp/material/index', N'MpMaterial', 0, N'1', N'1', N'1', N'1', N'2023-01-14 14:12:07', N'1', N'2024-02-29 12:43:18', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2114, N'上传临时素材', N'mp:material:upload-temporary', 3, 1, 2113, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-14 15:33:55', N'1', N'2023-01-14 15:33:55', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2115, N'上传永久素材', N'mp:material:upload-permanent', 3, 2, 2113, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-14 15:34:14', N'1', N'2023-01-14 15:34:14', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2116, N'删除素材', N'mp:material:delete', 3, 3, 2113, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-14 15:35:37', N'1', N'2023-01-14 15:35:37', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2117, N'上传图文图片', N'mp:material:upload-news-image', 3, 4, 2113, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-14 15:36:31', N'1', N'2023-01-14 15:36:31', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2118, N'查询素材', N'mp:material:query', 3, 5, 2113, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-14 15:39:22', N'1', N'2023-01-14 15:39:22', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2119, N'菜单管理', N'', 2, 6, 2084, N'menu', N'ep:menu', N'mp/menu/index', N'MpMenu', 0, N'1', N'1', N'1', N'1', N'2023-01-14 17:43:54', N'1', N'2024-02-29 12:42:56', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2120, N'自动回复', N'', 2, 7, 2084, N'auto-reply', N'fa-solid:republican', N'mp/autoReply/index', N'MpAutoReply', 0, N'1', N'1', N'1', N'1', N'2023-01-15 22:13:09', N'1', N'2024-02-29 12:43:10', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2121, N'查询回复', N'mp:auto-reply:query', 3, 0, 2120, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-16 22:28:41', N'1', N'2023-01-16 22:28:41', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2122, N'新增回复', N'mp:auto-reply:create', 3, 1, 2120, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-16 22:28:54', N'1', N'2023-01-16 22:28:54', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2123, N'修改回复', N'mp:auto-reply:update', 3, 2, 2120, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-16 22:29:05', N'1', N'2023-01-16 22:29:05', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2124, N'删除回复', N'mp:auto-reply:delete', 3, 3, 2120, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-16 22:29:34', N'1', N'2023-01-16 22:29:34', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2125, N'查询菜单', N'mp:menu:query', 3, 0, 2119, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-17 23:05:41', N'1', N'2023-01-17 23:05:41', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2126, N'保存菜单', N'mp:menu:save', 3, 1, 2119, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-17 23:06:01', N'1', N'2023-01-17 23:06:01', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2127, N'删除菜单', N'mp:menu:delete', 3, 2, 2119, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-17 23:06:16', N'1', N'2023-01-17 23:06:16', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2128, N'查询消息', N'mp:message:query', 3, 0, 2103, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-17 23:07:14', N'1', N'2023-01-17 23:07:14', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2129, N'发送消息', N'mp:message:send', 3, 1, 2103, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-17 23:07:26', N'1', N'2023-01-17 23:07:26', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2130, N'邮箱管理', N'', 2, 2, 2739, N'mail', N'fa-solid:mail-bulk', NULL, NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-25 17:27:44', N'1', N'2024-04-22 23:56:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2131, N'邮箱账号', N'', 2, 0, 2130, N'mail-account', N'fa:universal-access', N'system/mail/account/index', N'SystemMailAccount', 0, N'1', N'1', N'1', N'', N'2023-01-25 09:33:48', N'1', N'2024-02-29 08:48:16', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2132, N'账号查询', N'system:mail-account:query', 3, 1, 2131, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-01-25 09:33:48', N'', N'2023-01-25 09:33:48', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2133, N'账号创建', N'system:mail-account:create', 3, 2, 2131, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-01-25 09:33:48', N'', N'2023-01-25 09:33:48', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2134, N'账号更新', N'system:mail-account:update', 3, 3, 2131, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-01-25 09:33:48', N'', N'2023-01-25 09:33:48', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2135, N'账号删除', N'system:mail-account:delete', 3, 4, 2131, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-01-25 09:33:48', N'', N'2023-01-25 09:33:48', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2136, N'邮件模版', N'', 2, 0, 2130, N'mail-template', N'fa:tag', N'system/mail/template/index', N'SystemMailTemplate', 0, N'1', N'1', N'1', N'', N'2023-01-25 12:05:31', N'1', N'2024-02-29 08:48:41', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2137, N'模版查询', N'system:mail-template:query', 3, 1, 2136, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-01-25 12:05:31', N'', N'2023-01-25 12:05:31', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2138, N'模版创建', N'system:mail-template:create', 3, 2, 2136, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-01-25 12:05:31', N'', N'2023-01-25 12:05:31', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2139, N'模版更新', N'system:mail-template:update', 3, 3, 2136, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-01-25 12:05:31', N'', N'2023-01-25 12:05:31', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2140, N'模版删除', N'system:mail-template:delete', 3, 4, 2136, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-01-25 12:05:31', N'', N'2023-01-25 12:05:31', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2141, N'邮件记录', N'', 2, 0, 2130, N'mail-log', N'fa:edit', N'system/mail/log/index', N'SystemMailLog', 0, N'1', N'1', N'1', N'', N'2023-01-26 02:16:50', N'1', N'2024-02-29 08:48:51', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2142, N'日志查询', N'system:mail-log:query', 3, 1, 2141, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-01-26 02:16:50', N'', N'2023-01-26 02:16:50', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2143, N'发送测试邮件', N'system:mail-template:send-mail', 3, 5, 2136, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-26 23:29:15', N'1', N'2023-01-26 23:29:15', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2144, N'站内信管理', N'', 1, 3, 2739, N'notify', N'ep:message-box', NULL, NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-28 10:25:18', N'1', N'2024-04-22 23:56:12', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2145, N'模板管理', N'', 2, 0, 2144, N'notify-template', N'fa:archive', N'system/notify/template/index', N'SystemNotifyTemplate', 0, N'1', N'1', N'1', N'', N'2023-01-28 02:26:42', N'1', N'2024-02-29 08:49:14', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2146, N'站内信模板查询', N'system:notify-template:query', 3, 1, 2145, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-01-28 02:26:42', N'', N'2023-01-28 02:26:42', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2147, N'站内信模板创建', N'system:notify-template:create', 3, 2, 2145, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-01-28 02:26:42', N'', N'2023-01-28 02:26:42', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2148, N'站内信模板更新', N'system:notify-template:update', 3, 3, 2145, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-01-28 02:26:42', N'', N'2023-01-28 02:26:42', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2149, N'站内信模板删除', N'system:notify-template:delete', 3, 4, 2145, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-01-28 02:26:42', N'', N'2023-01-28 02:26:42', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2150, N'发送测试站内信', N'system:notify-template:send-notify', 3, 5, 2145, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-01-28 10:54:43', N'1', N'2023-01-28 10:54:43', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2151, N'消息记录', N'', 2, 0, 2144, N'notify-message', N'fa:edit', N'system/notify/message/index', N'SystemNotifyMessage', 0, N'1', N'1', N'1', N'', N'2023-01-28 04:28:22', N'1', N'2024-02-29 08:49:22', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2152, N'站内信消息查询', N'system:notify-message:query', 3, 1, 2151, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-01-28 04:28:22', N'', N'2023-01-28 04:28:22', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2153, N'大屏设计器', N'', 2, 2, 1281, N'go-view', N'fa:area-chart', N'report/goview/index', N'JimuReport', 0, N'1', N'1', N'1', N'1', N'2023-02-07 00:03:19', N'1', N'2024-02-29 12:34:02', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2154, N'创建项目', N'report:go-view-project:create', 3, 1, 2153, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-02-07 19:25:14', N'1', N'2023-02-07 19:25:14', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2155, N'更新项目', N'report:go-view-project:update', 3, 2, 2153, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-02-07 19:25:34', N'1', N'2024-04-24 20:01:18', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2156, N'查询项目', N'report:go-view-project:query', 3, 0, 2153, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-02-07 19:25:53', N'1', N'2023-02-07 19:25:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2157, N'使用 SQL 查询数据', N'report:go-view-data:get-by-sql', 3, 3, 2153, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-02-07 19:26:15', N'1', N'2023-02-07 19:26:15', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2158, N'使用 HTTP 查询数据', N'report:go-view-data:get-by-http', 3, 4, 2153, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'1', N'2023-02-07 19:26:35', N'1', N'2023-02-07 19:26:35', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2159, N'Boot 开发文档', N'', 1, 1, 0, N'https://doc.iocoder.cn/', N'ep:document', NULL, NULL, 0, N'1', N'1', N'1', N'1', N'2023-02-10 22:46:28', N'1', N'2023-12-02 21:32:20', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2160, N'Cloud 开发文档', N'', 1, 2, 0, N'https://cloud.iocoder.cn', N'ep:document-copy', NULL, NULL, 0, N'1', N'1', N'1', N'1', N'2023-02-10 22:47:07', N'1', N'2023-12-02 21:32:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2161, N'接入示例', N'', 1, 99, 1117, N'demo', N'fa-solid:dragon', N'pay/demo/index', NULL, 0, N'1', N'1', N'1', N'', N'2023-02-11 14:21:42', N'1', N'2024-01-18 23:50:00', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2162, N'商品导出', N'product:spu:export', 3, 5, 2014, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2022-07-30 14:22:58', N'', N'2022-07-30 14:22:58', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2164, N'配送管理', N'', 1, 3, 2072, N'delivery', N'ep:shopping-cart', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-05-18 09:18:02', N'1', N'2023-09-28 10:58:09', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2165, N'快递发货', N'', 1, 0, 2164, N'express', N'ep:bicycle', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-05-18 09:22:06', N'1', N'2023-08-30 21:02:49', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2166, N'门店自提', N'', 1, 1, 2164, N'pick-up-store', N'ep:add-location', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-05-18 09:23:14', N'1', N'2023-08-30 21:03:21', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2167, N'快递公司', N'', 2, 0, 2165, N'express', N'ep:compass', N'mall/trade/delivery/express/index', N'Express', 0, N'1', N'1', N'1', N'1', N'2023-05-18 09:27:21', N'1', N'2023-08-30 21:02:59', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2168, N'快递公司查询', N'trade:delivery:express:query', 3, 1, 2167, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-05-18 09:37:53', N'', N'2023-05-18 09:37:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2169, N'快递公司创建', N'trade:delivery:express:create', 3, 2, 2167, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-05-18 09:37:53', N'', N'2023-05-18 09:37:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2170, N'快递公司更新', N'trade:delivery:express:update', 3, 3, 2167, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-05-18 09:37:53', N'', N'2023-05-18 09:37:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2171, N'快递公司删除', N'trade:delivery:express:delete', 3, 4, 2167, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-05-18 09:37:53', N'', N'2023-05-18 09:37:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2172, N'快递公司导出', N'trade:delivery:express:export', 3, 5, 2167, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-05-18 09:37:53', N'', N'2023-05-18 09:37:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2173, N'运费模版', N'trade:delivery:express-template:query', 2, 1, 2165, N'express-template', N'ep:coordinate', N'mall/trade/delivery/expressTemplate/index', N'ExpressTemplate', 0, N'1', N'1', N'1', N'1', N'2023-05-20 06:48:10', N'1', N'2023-08-30 21:03:13', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2174, N'快递运费模板查询', N'trade:delivery:express-template:query', 3, 1, 2173, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-05-20 06:49:53', N'', N'2023-05-20 06:49:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2175, N'快递运费模板创建', N'trade:delivery:express-template:create', 3, 2, 2173, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-05-20 06:49:53', N'', N'2023-05-20 06:49:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2176, N'快递运费模板更新', N'trade:delivery:express-template:update', 3, 3, 2173, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-05-20 06:49:53', N'', N'2023-05-20 06:49:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2177, N'快递运费模板删除', N'trade:delivery:express-template:delete', 3, 4, 2173, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-05-20 06:49:53', N'', N'2023-05-20 06:49:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2178, N'快递运费模板导出', N'trade:delivery:express-template:export', 3, 5, 2173, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-05-20 06:49:53', N'', N'2023-05-20 06:49:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2179, N'门店管理', N'', 2, 1, 2166, N'pick-up-store', N'ep:basketball', N'mall/trade/delivery/pickUpStore/index', N'PickUpStore', 0, N'1', N'1', N'1', N'1', N'2023-05-25 10:50:00', N'1', N'2023-08-30 21:03:28', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2180, N'自提门店查询', N'trade:delivery:pick-up-store:query', 3, 1, 2179, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-05-25 10:53:29', N'', N'2023-05-25 10:53:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2181, N'自提门店创建', N'trade:delivery:pick-up-store:create', 3, 2, 2179, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-05-25 10:53:29', N'', N'2023-05-25 10:53:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2182, N'自提门店更新', N'trade:delivery:pick-up-store:update', 3, 3, 2179, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-05-25 10:53:29', N'', N'2023-05-25 10:53:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2183, N'自提门店删除', N'trade:delivery:pick-up-store:delete', 3, 4, 2179, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-05-25 10:53:29', N'', N'2023-05-25 10:53:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2184, N'自提门店导出', N'trade:delivery:pick-up-store:export', 3, 5, 2179, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-05-25 10:53:29', N'', N'2023-05-25 10:53:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2209, N'秒杀活动', N'', 2, 3, 2030, N'seckill', N'ep:place', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-06-24 17:39:13', N'1', N'2023-06-24 18:55:15', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2262, N'会员中心', N'', 1, 55, 0, N'/member', N'ep:bicycle', NULL, NULL, 0, N'1', N'1', N'1', N'1', N'2023-06-10 00:42:03', N'1', N'2023-08-20 09:23:56', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2275, N'会员配置', N'', 2, 0, 2262, N'config', N'fa:archive', N'member/config/index', N'MemberConfig', 0, N'1', N'1', N'1', N'', N'2023-06-10 02:07:44', N'1', N'2023-10-01 23:41:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2276, N'会员配置查询', N'member:config:query', 3, 1, 2275, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'', N'2023-06-10 02:07:44', N'1', N'2024-04-24 19:48:58', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2277, N'会员配置保存', N'member:config:save', 3, 2, 2275, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'', N'2023-06-10 02:07:44', N'1', N'2024-04-24 19:49:28', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2281, N'签到配置', N'', 2, 2, 2300, N'config', N'ep:calendar', N'member/signin/config/index', N'SignInConfig', 0, N'1', N'1', N'1', N'', N'2023-06-10 03:26:12', N'1', N'2023-08-20 19:25:51', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2282, N'积分签到规则查询', N'point:sign-in-config:query', 3, 1, 2281, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-06-10 03:26:12', N'', N'2023-06-10 03:26:12', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2283, N'积分签到规则创建', N'point:sign-in-config:create', 3, 2, 2281, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-06-10 03:26:12', N'', N'2023-06-10 03:26:12', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2284, N'积分签到规则更新', N'point:sign-in-config:update', 3, 3, 2281, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-06-10 03:26:12', N'', N'2023-06-10 03:26:12', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2285, N'积分签到规则删除', N'point:sign-in-config:delete', 3, 4, 2281, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-06-10 03:26:12', N'', N'2023-06-10 03:26:12', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2287, N'会员积分', N'', 2, 10, 2262, N'record', N'fa:asterisk', N'member/point/record/index', N'PointRecord', 0, N'1', N'1', N'1', N'', N'2023-06-10 04:18:50', N'1', N'2023-10-01 23:42:11', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2288, N'用户积分记录查询', N'point:record:query', 3, 1, 2287, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-06-10 04:18:50', N'', N'2023-06-10 04:18:50', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2293, N'签到记录', N'', 2, 3, 2300, N'record', N'ep:chicken', N'member/signin/record/index', N'SignInRecord', 0, N'1', N'1', N'1', N'', N'2023-06-10 04:48:22', N'1', N'2023-08-20 19:26:02', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2294, N'用户签到积分查询', N'point:sign-in-record:query', 3, 1, 2293, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-06-10 04:48:22', N'', N'2023-06-10 04:48:22', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2297, N'用户签到积分删除', N'point:sign-in-record:delete', 3, 4, 2293, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-06-10 04:48:22', N'', N'2023-06-10 04:48:22', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2300, N'会员签到', N'', 1, 11, 2262, N'signin', N'ep:alarm-clock', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-06-27 22:49:53', N'1', N'2023-08-20 09:23:48', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2301, N'回调通知', N'', 2, 5, 1117, N'notify', N'ep:mute-notification', N'pay/notify/index', N'PayNotify', 0, N'1', N'1', N'1', N'', N'2023-07-20 04:41:32', N'1', N'2024-01-18 23:56:48', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2302, N'支付通知查询', N'pay:notify:query', 3, 1, 2301, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-07-20 04:41:32', N'', N'2023-07-20 04:41:32', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2303, N'拼团活动', N'', 2, 3, 2030, N'combination', N'fa:group', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-08-12 17:19:54', N'1', N'2023-08-12 17:20:05', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2304, N'拼团商品', N'', 2, 1, 2303, N'acitivity', N'ep:apple', N'mall/promotion/combination/activity/index', N'PromotionCombinationActivity', 0, N'1', N'1', N'1', N'1', N'2023-08-12 17:22:03', N'1', N'2023-08-12 17:22:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2305, N'拼团活动查询', N'promotion:combination-activity:query', 3, 1, 2304, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-08-12 17:54:32', N'1', N'2023-11-24 11:57:40', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2306, N'拼团活动创建', N'promotion:combination-activity:create', 3, 2, 2304, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-08-12 17:54:49', N'1', N'2023-08-12 17:54:49', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2307, N'拼团活动更新', N'promotion:combination-activity:update', 3, 3, 2304, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-08-12 17:55:04', N'1', N'2023-08-12 17:55:04', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2308, N'拼团活动删除', N'promotion:combination-activity:delete', 3, 4, 2304, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-08-12 17:55:23', N'1', N'2023-08-12 17:55:23', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2309, N'拼团活动关闭', N'promotion:combination-activity:close', 3, 5, 2304, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-08-12 17:55:37', N'1', N'2023-10-06 10:51:57', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2310, N'砍价活动', N'', 2, 4, 2030, N'bargain', N'ep:box', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-08-13 00:27:25', N'1', N'2023-08-13 00:27:25', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2311, N'砍价商品', N'', 2, 1, 2310, N'activity', N'ep:burger', N'mall/promotion/bargain/activity/index', N'PromotionBargainActivity', 0, N'1', N'1', N'1', N'1', N'2023-08-13 00:28:49', N'1', N'2023-10-05 01:16:23', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2312, N'砍价活动查询', N'promotion:bargain-activity:query', 3, 1, 2311, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-08-13 00:32:30', N'1', N'2023-08-13 00:32:30', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2313, N'砍价活动创建', N'promotion:bargain-activity:create', 3, 2, 2311, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-08-13 00:32:44', N'1', N'2023-08-13 00:32:44', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2314, N'砍价活动更新', N'promotion:bargain-activity:update', 3, 3, 2311, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-08-13 00:32:55', N'1', N'2023-08-13 00:32:55', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2315, N'砍价活动删除', N'promotion:bargain-activity:delete', 3, 4, 2311, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-08-13 00:34:50', N'1', N'2023-08-13 00:34:50', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2316, N'砍价活动关闭', N'promotion:bargain-activity:close', 3, 5, 2311, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-08-13 00:35:02', N'1', N'2023-08-13 00:35:02', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2317, N'会员管理', N'', 2, 0, 2262, N'user', N'ep:avatar', N'member/user/index', N'MemberUser', 0, N'1', N'1', N'1', N'', N'2023-08-19 04:12:15', N'1', N'2023-08-24 00:50:55', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2318, N'会员用户查询', N'member:user:query', 3, 1, 2317, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-08-19 04:12:15', N'', N'2023-08-19 04:12:15', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2319, N'会员用户更新', N'member:user:update', 3, 3, 2317, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-08-19 04:12:15', N'', N'2023-08-19 04:12:15', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2320, N'会员标签', N'', 2, 1, 2262, N'tag', N'ep:collection-tag', N'member/tag/index', N'MemberTag', 0, N'1', N'1', N'1', N'', N'2023-08-20 01:03:08', N'1', N'2023-08-20 09:23:19', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2321, N'会员标签查询', N'member:tag:query', 3, 1, 2320, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-08-20 01:03:08', N'', N'2023-08-20 01:03:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2322, N'会员标签创建', N'member:tag:create', 3, 2, 2320, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-08-20 01:03:08', N'', N'2023-08-20 01:03:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2323, N'会员标签更新', N'member:tag:update', 3, 3, 2320, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-08-20 01:03:08', N'', N'2023-08-20 01:03:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2324, N'会员标签删除', N'member:tag:delete', 3, 4, 2320, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-08-20 01:03:08', N'', N'2023-08-20 01:03:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2325, N'会员等级', N'', 2, 2, 2262, N'level', N'fa:level-up', N'member/level/index', N'MemberLevel', 0, N'1', N'1', N'1', N'', N'2023-08-22 12:41:01', N'1', N'2023-08-22 21:47:00', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2326, N'会员等级查询', N'member:level:query', 3, 1, 2325, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-08-22 12:41:02', N'', N'2023-08-22 12:41:02', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2327, N'会员等级创建', N'member:level:create', 3, 2, 2325, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-08-22 12:41:02', N'', N'2023-08-22 12:41:02', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2328, N'会员等级更新', N'member:level:update', 3, 3, 2325, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-08-22 12:41:02', N'', N'2023-08-22 12:41:02', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2329, N'会员等级删除', N'member:level:delete', 3, 4, 2325, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-08-22 12:41:02', N'', N'2023-08-22 12:41:02', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2330, N'会员分组', N'', 2, 3, 2262, N'group', N'fa:group', N'member/group/index', N'MemberGroup', 0, N'1', N'1', N'1', N'', N'2023-08-22 13:50:06', N'1', N'2023-10-01 23:42:01', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2331, N'用户分组查询', N'member:group:query', 3, 1, 2330, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-08-22 13:50:06', N'', N'2023-08-22 13:50:06', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2332, N'用户分组创建', N'member:group:create', 3, 2, 2330, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-08-22 13:50:06', N'', N'2023-08-22 13:50:06', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2333, N'用户分组更新', N'member:group:update', 3, 3, 2330, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-08-22 13:50:06', N'', N'2023-08-22 13:50:06', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2334, N'用户分组删除', N'member:group:delete', 3, 4, 2330, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-08-22 13:50:06', N'', N'2023-08-22 13:50:06', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2335, N'用户等级修改', N'member:user:update-level', 3, 5, 2317, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-08-23 16:49:05', N'', N'2023-08-23 16:50:48', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2336, N'商品评论', N'', 2, 5, 2000, N'comment', N'ep:comment', N'mall/product/comment/index', N'ProductComment', 0, N'1', N'1', N'1', N'1', N'2023-08-26 11:03:00', N'1', N'2023-08-26 11:03:38', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2337, N'评论查询', N'product:comment:query', 3, 1, 2336, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-08-26 11:04:01', N'1', N'2023-08-26 11:04:01', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2338, N'添加自评', N'product:comment:create', 3, 2, 2336, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-08-26 11:04:23', N'1', N'2023-08-26 11:08:18', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2339, N'商家回复', N'product:comment:update', 3, 3, 2336, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-08-26 11:04:37', N'1', N'2023-08-26 11:04:37', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2340, N'显隐评论', N'product:comment:update', 3, 4, 2336, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-08-26 11:04:55', N'1', N'2023-08-26 11:04:55', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2341, N'优惠劵发送', N'promotion:coupon:send', 3, 2, 2038, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-09-02 00:03:14', N'1', N'2023-09-02 00:03:14', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2342, N'交易配置', N'', 2, 0, 2072, N'config', N'ep:setting', N'mall/trade/config/index', N'TradeConfig', 0, N'1', N'1', N'1', N'', N'2023-09-28 02:46:22', N'1', N'2024-02-26 20:30:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2343, N'交易中心配置查询', N'trade:config:query', 3, 1, 2342, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-09-28 02:46:22', N'', N'2023-09-28 02:46:22', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2344, N'交易中心配置保存', N'trade:config:save', 3, 2, 2342, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-09-28 02:46:22', N'', N'2023-09-28 02:46:22', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2345, N'分销管理', N'', 1, 4, 2072, N'brokerage', N'fa-solid:project-diagram', N'', N'', 0, N'1', N'1', N'1', N'', N'2023-09-28 02:46:22', N'1', N'2023-09-28 10:58:44', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2346, N'分销用户', N'', 2, 0, 2345, N'brokerage-user', N'fa-solid:user-tie', N'mall/trade/brokerage/user/index', N'TradeBrokerageUser', 0, N'1', N'1', N'1', N'', N'2023-09-28 02:46:22', N'1', N'2024-02-26 20:33:23', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2347, N'分销用户查询', N'trade:brokerage-user:query', 3, 1, 2346, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-09-28 02:46:22', N'', N'2023-09-28 02:46:22', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2348, N'分销用户推广人查询', N'trade:brokerage-user:user-query', 3, 2, 2346, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-09-28 02:46:22', N'', N'2023-09-28 02:46:22', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2349, N'分销用户推广订单查询', N'trade:brokerage-user:order-query', 3, 3, 2346, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-09-28 02:46:22', N'', N'2023-09-28 02:46:22', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2350, N'分销用户修改推广资格', N'trade:brokerage-user:update-brokerage-enable', 3, 4, 2346, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-09-28 02:46:22', N'', N'2023-09-28 02:46:22', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2351, N'分销用户修改推广员', N'trade:brokerage-user:update-bind-user', 3, 5, 2346, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-09-28 02:46:22', N'', N'2023-09-28 02:46:22', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2352, N'分销用户清除推广员', N'trade:brokerage-user:clear-bind-user', 3, 6, 2346, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-09-28 02:46:22', N'', N'2023-09-28 02:46:22', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2353, N'佣金记录', N'', 2, 1, 2345, N'brokerage-record', N'fa:money', N'mall/trade/brokerage/record/index', N'TradeBrokerageRecord', 0, N'1', N'1', N'1', N'', N'2023-09-28 02:46:22', N'1', N'2024-02-26 20:33:30', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2354, N'佣金记录查询', N'trade:brokerage-record:query', 3, 1, 2353, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-09-28 02:46:22', N'', N'2023-09-28 02:46:22', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2355, N'佣金提现', N'', 2, 2, 2345, N'brokerage-withdraw', N'fa:credit-card', N'mall/trade/brokerage/withdraw/index', N'TradeBrokerageWithdraw', 0, N'1', N'1', N'1', N'', N'2023-09-28 02:46:22', N'1', N'2024-02-26 20:33:35', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2356, N'佣金提现查询', N'trade:brokerage-withdraw:query', 3, 1, 2355, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-09-28 02:46:22', N'', N'2023-09-28 02:46:22', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2357, N'佣金提现审核', N'trade:brokerage-withdraw:audit', 3, 2, 2355, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-09-28 02:46:22', N'', N'2023-09-28 02:46:22', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2358, N'统计中心', N'', 1, 75, 2362, N'statistics', N'ep:data-line', N'', N'', 0, N'1', N'1', N'1', N'', N'2023-09-30 03:22:40', N'1', N'2023-09-30 11:54:48', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2359, N'交易统计', N'', 2, 4, 2358, N'trade', N'fa-solid:credit-card', N'mall/statistics/trade/index', N'TradeStatistics', 0, N'1', N'1', N'1', N'', N'2023-09-30 03:22:40', N'1', N'2024-02-26 20:42:00', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2360, N'交易统计查询', N'statistics:trade:query', 3, 1, 2359, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-09-30 03:22:40', N'', N'2023-09-30 03:22:40', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2361, N'交易统计导出', N'statistics:trade:export', 3, 2, 2359, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-09-30 03:22:40', N'', N'2023-09-30 03:22:40', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2362, N'商城系统', N'', 1, 59, 0, N'/mall', N'ep:shop', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-09-30 11:52:02', N'1', N'2023-09-30 11:52:18', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2363, N'用户积分修改', N'member:user:update-point', 3, 6, 2317, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-01 14:39:43', N'', N'2023-10-01 14:39:43', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2364, N'用户余额修改', N'member:user:update-balance', 3, 7, 2317, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'', N'2023-10-01 14:39:43', N'1', N'2023-10-01 22:42:31', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2365, N'优惠劵', N'', 1, 2, 2030, N'coupon', N'fa-solid:disease', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-10-03 12:39:15', N'1', N'2023-10-05 00:16:07', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2366, N'砍价记录', N'', 2, 2, 2310, N'record', N'ep:list', N'mall/promotion/bargain/record/index', N'PromotionBargainRecord', 0, N'1', N'1', N'1', N'', N'2023-10-05 02:49:06', N'1', N'2023-10-05 10:50:38', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2367, N'砍价记录查询', N'promotion:bargain-record:query', 3, 1, 2366, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-05 02:49:06', N'', N'2023-10-05 02:49:06', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2368, N'助力记录查询', N'promotion:bargain-help:query', 3, 2, 2366, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-10-05 12:27:49', N'1', N'2023-10-05 12:27:49', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2369, N'拼团记录', N'promotion:combination-record:query', 2, 2, 2303, N'record', N'ep:avatar', N'mall/promotion/combination/record/index.vue', N'PromotionCombinationRecord', 0, N'1', N'1', N'1', N'1', N'2023-10-08 07:10:22', N'1', N'2023-10-08 07:34:11', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2374, N'会员统计', N'', 2, 2, 2358, N'member', N'ep:avatar', N'mall/statistics/member/index', N'MemberStatistics', 0, N'1', N'1', N'1', N'', N'2023-10-11 04:39:24', N'1', N'2024-02-26 20:41:46', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2375, N'会员统计查询', N'statistics:member:query', 3, 1, 2374, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-11 04:39:24', N'', N'2023-10-11 04:39:24', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2376, N'订单核销', N'trade:order:pick-up', 3, 10, 2076, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-10-14 17:11:58', N'1', N'2023-10-14 17:11:58', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2377, N'文章分类', N'', 2, 0, 2387, N'article/category', N'fa:certificate', N'mall/promotion/article/category/index', N'ArticleCategory', 0, N'1', N'1', N'1', N'', N'2023-10-16 01:26:18', N'1', N'2023-10-16 09:38:26', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2378, N'分类查询', N'promotion:article-category:query', 3, 1, 2377, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-16 01:26:18', N'', N'2023-10-16 01:26:18', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2379, N'分类创建', N'promotion:article-category:create', 3, 2, 2377, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-16 01:26:18', N'', N'2023-10-16 01:26:18', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2380, N'分类更新', N'promotion:article-category:update', 3, 3, 2377, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-16 01:26:18', N'', N'2023-10-16 01:26:18', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2381, N'分类删除', N'promotion:article-category:delete', 3, 4, 2377, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-16 01:26:18', N'', N'2023-10-16 01:26:18', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2382, N'文章列表', N'', 2, 2, 2387, N'article', N'ep:connection', N'mall/promotion/article/index', N'Article', 0, N'1', N'1', N'1', N'', N'2023-10-16 01:26:18', N'1', N'2023-10-16 09:41:19', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2383, N'文章管理查询', N'promotion:article:query', 3, 1, 2382, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-16 01:26:18', N'', N'2023-10-16 01:26:18', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2384, N'文章管理创建', N'promotion:article:create', 3, 2, 2382, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-16 01:26:18', N'', N'2023-10-16 01:26:18', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2385, N'文章管理更新', N'promotion:article:update', 3, 3, 2382, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-16 01:26:18', N'', N'2023-10-16 01:26:18', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2386, N'文章管理删除', N'promotion:article:delete', 3, 4, 2382, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-16 01:26:18', N'', N'2023-10-16 01:26:18', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2387, N'内容管理', N'', 1, 1, 2030, N'content', N'ep:collection', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-10-16 09:37:31', N'1', N'2023-10-16 09:37:31', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2388, N'商城首页', N'', 2, 1, 2362, N'home', N'ep:home-filled', N'mall/home/index', N'MallHome', 0, N'1', N'1', N'1', N'', N'2023-10-16 12:10:33', N'', N'2023-10-16 12:10:33', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2389, N'核销订单', N'', 2, 2, 2166, N'pick-up-order', N'ep:list', N'mall/trade/delivery/pickUpOrder/index', N'PickUpOrder', 0, N'1', N'1', N'1', N'', N'2023-10-19 16:09:51', N'', N'2023-10-19 16:09:51', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2390, N'优惠活动', N'', 1, 99, 2030, N'youhui', N'ep:aim', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-10-21 19:23:49', N'1', N'2023-10-21 19:23:49', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2391, N'客户管理', N'', 2, 10, 2397, N'customer', N'fa:address-book-o', N'crm/customer/index', N'CrmCustomer', 0, N'1', N'1', N'1', N'', N'2023-10-29 09:04:21', N'1', N'2024-02-17 17:13:32', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2392, N'客户查询', N'crm:customer:query', 3, 1, 2391, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 09:04:21', N'', N'2023-10-29 09:04:21', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2393, N'客户创建', N'crm:customer:create', 3, 2, 2391, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 09:04:21', N'', N'2023-10-29 09:04:21', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2394, N'客户更新', N'crm:customer:update', 3, 3, 2391, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 09:04:21', N'', N'2023-10-29 09:04:21', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2395, N'客户删除', N'crm:customer:delete', 3, 4, 2391, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 09:04:21', N'', N'2023-10-29 09:04:21', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2396, N'客户导出', N'crm:customer:export', 3, 5, 2391, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 09:04:21', N'', N'2023-10-29 09:04:21', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2397, N'CRM 系统', N'', 1, 200, 0, N'/crm', N'ep:avatar', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-10-29 17:08:30', N'1', N'2024-02-04 15:37:31', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2398, N'合同管理', N'', 2, 50, 2397, N'contract', N'ep:notebook', N'crm/contract/index', N'CrmContract', 0, N'1', N'1', N'1', N'', N'2023-10-29 10:50:41', N'1', N'2024-02-17 17:15:09', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2399, N'合同查询', N'crm:contract:query', 3, 1, 2398, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 10:50:41', N'', N'2023-10-29 10:50:41', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2400, N'合同创建', N'crm:contract:create', 3, 2, 2398, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 10:50:41', N'', N'2023-10-29 10:50:41', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2401, N'合同更新', N'crm:contract:update', 3, 3, 2398, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 10:50:41', N'', N'2023-10-29 10:50:41', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2402, N'合同删除', N'crm:contract:delete', 3, 4, 2398, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 10:50:41', N'', N'2023-10-29 10:50:41', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2403, N'合同导出', N'crm:contract:export', 3, 5, 2398, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 10:50:41', N'', N'2023-10-29 10:50:41', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2404, N'线索管理', N'', 2, 8, 2397, N'clue', N'fa:pagelines', N'crm/clue/index', N'CrmClue', 0, N'1', N'1', N'1', N'', N'2023-10-29 11:06:29', N'1', N'2024-02-17 17:15:41', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2405, N'线索查询', N'crm:clue:query', 3, 1, 2404, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:06:29', N'', N'2023-10-29 11:06:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2406, N'线索创建', N'crm:clue:create', 3, 2, 2404, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:06:29', N'', N'2023-10-29 11:06:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2407, N'线索更新', N'crm:clue:update', 3, 3, 2404, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:06:29', N'', N'2023-10-29 11:06:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2408, N'线索删除', N'crm:clue:delete', 3, 4, 2404, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:06:29', N'', N'2023-10-29 11:06:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2409, N'线索导出', N'crm:clue:export', 3, 5, 2404, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:06:29', N'', N'2023-10-29 11:06:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2410, N'商机管理', N'', 2, 40, 2397, N'business', N'fa:bus', N'crm/business/index', N'CrmBusiness', 0, N'1', N'1', N'1', N'', N'2023-10-29 11:12:35', N'1', N'2024-02-17 17:14:55', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2411, N'商机查询', N'crm:business:query', 3, 1, 2410, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:12:35', N'', N'2023-10-29 11:12:35', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2412, N'商机创建', N'crm:business:create', 3, 2, 2410, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:12:35', N'', N'2023-10-29 11:12:35', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2413, N'商机更新', N'crm:business:update', 3, 3, 2410, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:12:35', N'', N'2023-10-29 11:12:35', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2414, N'商机删除', N'crm:business:delete', 3, 4, 2410, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:12:35', N'', N'2023-10-29 11:12:35', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2415, N'商机导出', N'crm:business:export', 3, 5, 2410, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:12:35', N'', N'2023-10-29 11:12:35', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2416, N'联系人管理', N'', 2, 20, 2397, N'contact', N'fa:address-book-o', N'crm/contact/index', N'CrmContact', 0, N'1', N'1', N'1', N'', N'2023-10-29 11:14:56', N'1', N'2024-02-17 17:13:49', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2417, N'联系人查询', N'crm:contact:query', 3, 1, 2416, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:14:56', N'', N'2023-10-29 11:14:56', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2418, N'联系人创建', N'crm:contact:create', 3, 2, 2416, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:14:56', N'', N'2023-10-29 11:14:56', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2419, N'联系人更新', N'crm:contact:update', 3, 3, 2416, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:14:56', N'', N'2023-10-29 11:14:56', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2420, N'联系人删除', N'crm:contact:delete', 3, 4, 2416, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:14:56', N'', N'2023-10-29 11:14:56', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2421, N'联系人导出', N'crm:contact:export', 3, 5, 2416, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:14:56', N'', N'2023-10-29 11:14:56', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2422, N'回款管理', N'', 2, 60, 2397, N'receivable', N'ep:money', N'crm/receivable/index', N'CrmReceivable', 0, N'1', N'1', N'1', N'', N'2023-10-29 11:18:09', N'1', N'2024-02-17 17:16:18', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2423, N'回款管理查询', N'crm:receivable:query', 3, 1, 2422, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:18:09', N'', N'2023-10-29 11:18:09', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2424, N'回款管理创建', N'crm:receivable:create', 3, 2, 2422, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:18:09', N'', N'2023-10-29 11:18:09', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2425, N'回款管理更新', N'crm:receivable:update', 3, 3, 2422, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:18:09', N'', N'2023-10-29 11:18:09', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2426, N'回款管理删除', N'crm:receivable:delete', 3, 4, 2422, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:18:09', N'', N'2023-10-29 11:18:09', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2427, N'回款管理导出', N'crm:receivable:export', 3, 5, 2422, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:18:09', N'', N'2023-10-29 11:18:09', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2428, N'回款计划', N'', 2, 61, 2397, N'receivable-plan', N'fa:money', N'crm/receivable/plan/index', N'CrmReceivablePlan', 0, N'1', N'1', N'1', N'', N'2023-10-29 11:18:09', N'1', N'2024-02-17 17:16:11', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2429, N'回款计划查询', N'crm:receivable-plan:query', 3, 1, 2428, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:18:09', N'', N'2023-10-29 11:18:09', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2430, N'回款计划创建', N'crm:receivable-plan:create', 3, 2, 2428, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:18:09', N'', N'2023-10-29 11:18:09', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2431, N'回款计划更新', N'crm:receivable-plan:update', 3, 3, 2428, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:18:09', N'', N'2023-10-29 11:18:09', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2432, N'回款计划删除', N'crm:receivable-plan:delete', 3, 4, 2428, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:18:09', N'', N'2023-10-29 11:18:09', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2433, N'回款计划导出', N'crm:receivable-plan:export', 3, 5, 2428, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 11:18:09', N'', N'2023-10-29 11:18:09', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2435, N'商城装修', N'', 2, 20, 2030, N'diy-template', N'fa6-solid:brush', N'mall/promotion/diy/template/index', N'DiyTemplate', 0, N'1', N'1', N'1', N'', N'2023-10-29 14:19:25', N'', N'2023-10-29 14:19:25', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2436, N'装修模板', N'', 2, 1, 2435, N'diy-template', N'fa6-solid:brush', N'mall/promotion/diy/template/index', N'DiyTemplate', 0, N'1', N'1', N'1', N'', N'2023-10-29 14:19:25', N'', N'2023-10-29 14:19:25', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2437, N'装修模板查询', N'promotion:diy-template:query', 3, 1, 2436, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 14:19:25', N'', N'2023-10-29 14:19:25', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2438, N'装修模板创建', N'promotion:diy-template:create', 3, 2, 2436, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 14:19:25', N'', N'2023-10-29 14:19:25', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2439, N'装修模板更新', N'promotion:diy-template:update', 3, 3, 2436, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 14:19:25', N'', N'2023-10-29 14:19:25', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2440, N'装修模板删除', N'promotion:diy-template:delete', 3, 4, 2436, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 14:19:25', N'', N'2023-10-29 14:19:25', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2441, N'装修模板使用', N'promotion:diy-template:use', 3, 5, 2436, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 14:19:25', N'', N'2023-10-29 14:19:25', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2442, N'装修页面', N'', 2, 2, 2435, N'diy-page', N'foundation:page-edit', N'mall/promotion/diy/page/index', N'DiyPage', 0, N'1', N'1', N'1', N'', N'2023-10-29 14:19:25', N'', N'2023-10-29 14:19:25', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2443, N'装修页面查询', N'promotion:diy-page:query', 3, 1, 2442, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 14:19:25', N'', N'2023-10-29 14:19:25', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2444, N'装修页面创建', N'promotion:diy-page:create', 3, 2, 2442, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 14:19:26', N'', N'2023-10-29 14:19:26', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2445, N'装修页面更新', N'promotion:diy-page:update', 3, 3, 2442, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 14:19:26', N'', N'2023-10-29 14:19:26', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2446, N'装修页面删除', N'promotion:diy-page:delete', 3, 4, 2442, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-10-29 14:19:26', N'', N'2023-10-29 14:19:26', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2447, N'三方登录', N'', 1, 10, 1, N'social', N'fa:rocket', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-11-04 12:12:01', N'1', N'2024-02-29 01:14:05', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2448, N'三方应用', N'', 2, 1, 2447, N'client', N'ep:set-up', N'views/system/social/client/index.vue', N'SocialClient', 0, N'1', N'1', N'1', N'1', N'2023-11-04 12:17:19', N'1', N'2023-11-04 12:17:19', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2449, N'三方应用查询', N'system:social-client:query', 3, 1, 2448, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-11-04 12:43:12', N'1', N'2023-11-04 12:43:33', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2450, N'三方应用创建', N'system:social-client:create', 3, 2, 2448, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-11-04 12:43:58', N'1', N'2023-11-04 12:43:58', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2451, N'三方应用更新', N'system:social-client:update', 3, 3, 2448, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-11-04 12:44:27', N'1', N'2023-11-04 12:44:27', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2452, N'三方应用删除', N'system:social-client:delete', 3, 4, 2448, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-11-04 12:44:43', N'1', N'2023-11-04 12:44:43', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2453, N'三方用户', N'system:social-user:query', 2, 2, 2447, N'user', N'ep:avatar', N'system/social/user/index.vue', N'SocialUser', 0, N'1', N'1', N'1', N'1', N'2023-11-04 14:01:05', N'1', N'2023-11-04 14:01:05', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2472, N'主子表（内嵌）', N'', 2, 12, 1070, N'demo03-inner', N'fa:power-off', N'infra/demo/demo03/inner/index', N'Demo03StudentInner', 0, N'1', N'1', N'1', N'', N'2023-11-13 04:39:51', N'1', N'2023-11-16 23:53:46', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2478, N'单表（增删改查）', N'', 2, 1, 1070, N'demo01-contact', N'ep:bicycle', N'infra/demo/demo01/index', N'Demo01Contact', 0, N'1', N'1', N'1', N'', N'2023-11-15 14:42:30', N'1', N'2023-11-16 20:34:40', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2479, N'示例联系人查询', N'infra:demo01-contact:query', 3, 1, 2478, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-15 14:42:30', N'', N'2023-11-15 14:42:30', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2480, N'示例联系人创建', N'infra:demo01-contact:create', 3, 2, 2478, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-15 14:42:30', N'', N'2023-11-15 14:42:30', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2481, N'示例联系人更新', N'infra:demo01-contact:update', 3, 3, 2478, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-15 14:42:30', N'', N'2023-11-15 14:42:30', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2482, N'示例联系人删除', N'infra:demo01-contact:delete', 3, 4, 2478, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-15 14:42:30', N'', N'2023-11-15 14:42:30', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2483, N'示例联系人导出', N'infra:demo01-contact:export', 3, 5, 2478, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-15 14:42:30', N'', N'2023-11-15 14:42:30', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2484, N'树表（增删改查）', N'', 2, 2, 1070, N'demo02-category', N'fa:tree', N'infra/demo/demo02/index', N'Demo02Category', 0, N'1', N'1', N'1', N'', N'2023-11-16 12:18:27', N'1', N'2023-11-16 20:35:01', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2485, N'示例分类查询', N'infra:demo02-category:query', 3, 1, 2484, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-16 12:18:27', N'', N'2023-11-16 12:18:27', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2486, N'示例分类创建', N'infra:demo02-category:create', 3, 2, 2484, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-16 12:18:27', N'', N'2023-11-16 12:18:27', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2487, N'示例分类更新', N'infra:demo02-category:update', 3, 3, 2484, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-16 12:18:27', N'', N'2023-11-16 12:18:27', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2488, N'示例分类删除', N'infra:demo02-category:delete', 3, 4, 2484, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-16 12:18:27', N'', N'2023-11-16 12:18:27', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2489, N'示例分类导出', N'infra:demo02-category:export', 3, 5, 2484, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-16 12:18:27', N'', N'2023-11-16 12:18:27', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2490, N'主子表（标准）', N'', 2, 10, 1070, N'demo03-normal', N'fa:battery-3', N'infra/demo/demo03/normal/index', N'Demo03StudentNormal', 0, N'1', N'1', N'1', N'', N'2023-11-16 12:53:37', N'1', N'2023-11-16 23:10:03', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2491, N'学生查询', N'infra:demo03-student:query', 3, 1, 2490, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-16 12:53:37', N'', N'2023-11-16 12:53:37', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2492, N'学生创建', N'infra:demo03-student:create', 3, 2, 2490, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-16 12:53:37', N'', N'2023-11-16 12:53:37', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2493, N'学生更新', N'infra:demo03-student:update', 3, 3, 2490, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-16 12:53:37', N'', N'2023-11-16 12:53:37', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2494, N'学生删除', N'infra:demo03-student:delete', 3, 4, 2490, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-16 12:53:37', N'', N'2023-11-16 12:53:37', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2495, N'学生导出', N'infra:demo03-student:export', 3, 5, 2490, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-16 12:53:37', N'', N'2023-11-16 12:53:37', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2497, N'主子表（ERP）', N'', 2, 11, 1070, N'demo03-erp', N'ep:calendar', N'infra/demo/demo03/erp/index', N'Demo03StudentERP', 0, N'1', N'1', N'1', N'', N'2023-11-16 15:50:59', N'1', N'2023-11-17 13:19:56', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2516, N'客户公海配置', N'', 2, 0, 2524, N'customer-pool-config', N'ep:data-analysis', N'crm/customer/poolConfig/index', N'CrmCustomerPoolConfig', 0, N'1', N'1', N'1', N'', N'2023-11-18 13:33:31', N'1', N'2024-01-03 19:52:06', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2517, N'客户公海配置保存', N'crm:customer-pool-config:update', 3, 1, 2516, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-18 13:33:31', N'', N'2023-11-18 13:33:31', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2518, N'客户限制配置', N'', 2, 1, 2524, N'customer-limit-config', N'ep:avatar', N'crm/customer/limitConfig/index', N'CrmCustomerLimitConfig', 0, N'1', N'1', N'1', N'', N'2023-11-18 13:33:53', N'1', N'2024-02-24 16:43:33', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2519, N'客户限制配置查询', N'crm:customer-limit-config:query', 3, 1, 2518, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-18 13:33:53', N'', N'2023-11-18 13:33:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2520, N'客户限制配置创建', N'crm:customer-limit-config:create', 3, 2, 2518, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-18 13:33:53', N'', N'2023-11-18 13:33:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2521, N'客户限制配置更新', N'crm:customer-limit-config:update', 3, 3, 2518, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-18 13:33:53', N'', N'2023-11-18 13:33:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2522, N'客户限制配置删除', N'crm:customer-limit-config:delete', 3, 4, 2518, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-18 13:33:53', N'', N'2023-11-18 13:33:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2523, N'客户限制配置导出', N'crm:customer-limit-config:export', 3, 5, 2518, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-11-18 13:33:53', N'', N'2023-11-18 13:33:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2524, N'系统配置', N'', 1, 999, 2397, N'config', N'ep:connection', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-11-18 21:58:00', N'1', N'2024-02-17 17:14:34', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2525, N'WebSocket', N'', 2, 5, 2, N'websocket', N'ep:connection', N'infra/webSocket/index', N'InfraWebSocket', 0, N'1', N'1', N'1', N'1', N'2023-11-23 19:41:55', N'1', N'2024-04-23 00:02:00', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2526, N'产品管理', N'', 2, 80, 2397, N'product', N'fa:product-hunt', N'crm/product/index', N'CrmProduct', 0, N'1', N'1', N'1', N'1', N'2023-12-05 22:45:26', N'1', N'2024-02-20 20:36:20', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2527, N'产品查询', N'crm:product:query', 3, 1, 2526, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-12-05 22:47:16', N'1', N'2023-12-05 22:47:16', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2528, N'产品创建', N'crm:product:create', 3, 2, 2526, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-12-05 22:47:41', N'1', N'2023-12-05 22:47:48', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2529, N'产品更新', N'crm:product:update', 3, 3, 2526, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-12-05 22:48:03', N'1', N'2023-12-05 22:48:03', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2530, N'产品删除', N'crm:product:delete', 3, 4, 2526, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-12-05 22:48:17', N'1', N'2023-12-05 22:48:17', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2531, N'产品导出', N'crm:product:export', 3, 5, 2526, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-12-05 22:48:29', N'1', N'2023-12-05 22:48:29', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2532, N'产品分类配置', N'', 2, 3, 2524, N'product/category', N'fa-solid:window-restore', N'crm/product/category/index', N'CrmProductCategory', 0, N'1', N'1', N'1', N'1', N'2023-12-06 12:52:36', N'1', N'2023-12-06 12:52:51', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2533, N'产品分类查询', N'crm:product-category:query', 3, 1, 2532, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-12-06 12:53:23', N'1', N'2023-12-06 12:53:23', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2534, N'产品分类创建', N'crm:product-category:create', 3, 2, 2532, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-12-06 12:53:41', N'1', N'2023-12-06 12:53:41', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2535, N'产品分类更新', N'crm:product-category:update', 3, 3, 2532, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-12-06 12:53:59', N'1', N'2023-12-06 12:53:59', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2536, N'产品分类删除', N'crm:product-category:delete', 3, 4, 2532, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2023-12-06 12:54:14', N'1', N'2023-12-06 12:54:14', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2543, N'关联商机', N'crm:contact:create-business', 3, 10, 2416, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-01-02 17:28:25', N'1', N'2024-01-02 17:28:25', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2544, N'取关商机', N'crm:contact:delete-business', 3, 11, 2416, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-01-02 17:28:43', N'1', N'2024-01-02 17:28:51', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2545, N'商品统计', N'', 2, 3, 2358, N'product', N'fa:product-hunt', N'mall/statistics/product/index', N'ProductStatistics', 0, N'1', N'1', N'1', N'', N'2023-12-15 18:54:28', N'1', N'2024-02-26 20:41:52', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2546, N'客户公海', N'', 2, 30, 2397, N'customer/pool', N'fa-solid:swimming-pool', N'crm/customer/pool/index', N'CrmCustomerPool', 0, N'1', N'1', N'1', N'1', N'2024-01-15 21:29:34', N'1', N'2024-02-17 17:14:18', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2547, N'订单查询', N'trade:order:query', 3, 1, 2076, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-01-16 08:52:00', N'1', N'2024-01-16 08:52:00', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2548, N'订单更新', N'trade:order:update', 3, 2, 2076, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-01-16 08:52:21', N'1', N'2024-01-16 08:52:21', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2549, N'支付&退款案例', N'', 2, 1, 2161, N'order', N'fa:paypal', N'pay/demo/order/index', N'', 0, N'1', N'1', N'1', N'1', N'2024-01-18 23:45:00', N'1', N'2024-01-18 23:47:21', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2550, N'转账案例', N'', 2, 2, 2161, N'transfer', N'fa:transgender-alt', N'pay/demo/transfer/index', N'', 0, N'1', N'1', N'1', N'1', N'2024-01-18 23:51:16', N'1', N'2024-01-18 23:51:16', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2551, N'钱包管理', N'', 1, 4, 1117, N'wallet', N'ep:wallet', N'', N'', 0, N'1', N'1', N'1', N'', N'2023-12-29 02:32:54', N'1', N'2024-02-29 08:58:54', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2552, N'充值套餐', N'', 2, 2, 2551, N'wallet-recharge-package', N'fa:leaf', N'pay/wallet/rechargePackage/index', N'WalletRechargePackage', 0, N'1', N'1', N'1', N'', N'2023-12-29 02:32:54', N'', N'2023-12-29 02:32:54', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2553, N'钱包充值套餐查询', N'pay:wallet-recharge-package:query', 3, 1, 2552, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-12-29 02:32:54', N'', N'2023-12-29 02:32:54', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2554, N'钱包充值套餐创建', N'pay:wallet-recharge-package:create', 3, 2, 2552, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-12-29 02:32:54', N'', N'2023-12-29 02:32:54', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2555, N'钱包充值套餐更新', N'pay:wallet-recharge-package:update', 3, 3, 2552, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-12-29 02:32:54', N'', N'2023-12-29 02:32:54', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2556, N'钱包充值套餐删除', N'pay:wallet-recharge-package:delete', 3, 4, 2552, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-12-29 02:32:54', N'', N'2023-12-29 02:32:54', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2557, N'钱包余额', N'', 2, 1, 2551, N'wallet-balance', N'fa:leaf', N'pay/wallet/balance/index', N'WalletBalance', 0, N'1', N'1', N'1', N'', N'2023-12-29 02:32:54', N'', N'2023-12-29 02:32:54', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2558, N'钱包余额查询', N'pay:wallet:query', 3, 1, 2557, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2023-12-29 02:32:54', N'', N'2023-12-29 02:32:54', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2559, N'转账订单', N'', 2, 3, 1117, N'transfer', N'ep:credit-card', N'pay/transfer/index', N'PayTransfer', 0, N'1', N'1', N'1', N'', N'2023-12-29 02:32:54', N'', N'2023-12-29 02:32:54', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2560, N'数据统计', N'', 1, 200, 2397, N'statistics', N'ep:data-line', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-01-26 22:50:35', N'1', N'2024-02-24 20:10:07', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2561, N'排行榜', N'crm:statistics-rank:query', 2, 1, 2560, N'ranking', N'fa:area-chart', N'crm/statistics/rank/index', N'CrmStatisticsRank', 0, N'1', N'1', N'1', N'1', N'2024-01-26 22:52:09', N'1', N'2024-04-24 19:39:11', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2562, N'客户导入', N'crm:customer:import', 3, 6, 2391, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-02-01 13:09:00', N'1', N'2024-02-01 13:09:05', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2563, N'ERP 系统', N'', 1, 300, 0, N'/erp', N'fa-solid:store', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-02-04 15:37:25', N'1', N'2024-02-04 15:37:25', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2564, N'产品管理', N'', 1, 40, 2563, N'product', N'fa:product-hunt', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-02-04 15:38:43', N'1', N'2024-02-04 15:38:43', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2565, N'产品信息', N'', 2, 0, 2564, N'product', N'fa-solid:apple-alt', N'erp/product/product/index', N'ErpProduct', 0, N'1', N'1', N'1', N'', N'2024-02-04 07:52:15', N'1', N'2024-02-05 14:42:11', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2566, N'产品查询', N'erp:product:query', 3, 1, 2565, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'', N'2024-02-04 07:52:15', N'1', N'2024-02-04 17:21:57', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2567, N'产品创建', N'erp:product:create', 3, 2, 2565, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'', N'2024-02-04 07:52:15', N'1', N'2024-02-04 17:22:12', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2568, N'产品更新', N'erp:product:update', 3, 3, 2565, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'', N'2024-02-04 07:52:15', N'1', N'2024-02-04 17:22:16', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2569, N'产品删除', N'erp:product:delete', 3, 4, 2565, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'', N'2024-02-04 07:52:15', N'1', N'2024-02-04 17:22:22', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2570, N'产品导出', N'erp:product:export', 3, 5, 2565, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'', N'2024-02-04 07:52:15', N'1', N'2024-02-04 17:22:26', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2571, N'产品分类', N'', 2, 1, 2564, N'product-category', N'fa:certificate', N'erp/product/category/index', N'ErpProductCategory', 0, N'1', N'1', N'1', N'', N'2024-02-04 09:21:04', N'1', N'2024-02-04 17:24:58', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2572, N'分类查询', N'erp:product-category:query', 3, 1, 2571, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-04 09:21:04', N'', N'2024-02-04 09:21:04', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2573, N'分类创建', N'erp:product-category:create', 3, 2, 2571, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-04 09:21:04', N'', N'2024-02-04 09:21:04', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2574, N'分类更新', N'erp:product-category:update', 3, 3, 2571, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-04 09:21:04', N'', N'2024-02-04 09:21:04', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2575, N'分类删除', N'erp:product-category:delete', 3, 4, 2571, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-04 09:21:04', N'', N'2024-02-04 09:21:04', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2576, N'分类导出', N'erp:product-category:export', 3, 5, 2571, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-04 09:21:04', N'', N'2024-02-04 09:21:04', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2577, N'产品单位', N'', 2, 2, 2564, N'unit', N'ep:opportunity', N'erp/product/unit/index', N'ErpProductUnit', 0, N'1', N'1', N'1', N'', N'2024-02-04 11:54:08', N'1', N'2024-02-04 19:54:37', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2578, N'单位查询', N'erp:product-unit:query', 3, 1, 2577, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-04 11:54:08', N'', N'2024-02-04 11:54:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2579, N'单位创建', N'erp:product-unit:create', 3, 2, 2577, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-04 11:54:08', N'', N'2024-02-04 11:54:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2580, N'单位更新', N'erp:product-unit:update', 3, 3, 2577, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-04 11:54:08', N'', N'2024-02-04 11:54:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2581, N'单位删除', N'erp:product-unit:delete', 3, 4, 2577, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-04 11:54:08', N'', N'2024-02-04 11:54:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2582, N'单位导出', N'erp:product-unit:export', 3, 5, 2577, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-04 11:54:08', N'', N'2024-02-04 11:54:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2583, N'库存管理', N'', 1, 30, 2563, N'stock', N'fa:window-restore', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-02-05 00:29:37', N'1', N'2024-02-05 00:29:37', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2584, N'仓库信息', N'', 2, 0, 2583, N'warehouse', N'ep:house', N'erp/stock/warehouse/index', N'ErpWarehouse', 0, N'1', N'1', N'1', N'', N'2024-02-04 17:12:09', N'1', N'2024-02-05 01:12:53', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2585, N'仓库查询', N'erp:warehouse:query', 3, 1, 2584, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-04 17:12:09', N'', N'2024-02-04 17:12:09', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2586, N'仓库创建', N'erp:warehouse:create', 3, 2, 2584, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-04 17:12:09', N'', N'2024-02-04 17:12:09', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2587, N'仓库更新', N'erp:warehouse:update', 3, 3, 2584, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-04 17:12:09', N'', N'2024-02-04 17:12:09', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2588, N'仓库删除', N'erp:warehouse:delete', 3, 4, 2584, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-04 17:12:09', N'', N'2024-02-04 17:12:09', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2589, N'仓库导出', N'erp:warehouse:export', 3, 5, 2584, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-04 17:12:09', N'', N'2024-02-04 17:12:09', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2590, N'产品库存', N'', 2, 1, 2583, N'stock', N'ep:coffee', N'erp/stock/stock/index', N'ErpStock', 0, N'1', N'1', N'1', N'', N'2024-02-05 06:40:50', N'1', N'2024-02-05 14:42:44', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2591, N'库存查询', N'erp:stock:query', 3, 1, 2590, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 06:40:50', N'', N'2024-02-05 06:40:50', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2592, N'库存导出', N'erp:stock:export', 3, 5, 2590, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 06:40:50', N'', N'2024-02-05 06:40:50', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2593, N'出入库明细', N'', 2, 2, 2583, N'record', N'fa-solid:blog', N'erp/stock/record/index', N'ErpStockRecord', 0, N'1', N'1', N'1', N'', N'2024-02-05 10:27:21', N'1', N'2024-02-06 17:26:11', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2594, N'库存明细查询', N'erp:stock-record:query', 3, 1, 2593, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 10:27:21', N'', N'2024-02-05 10:27:21', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2595, N'库存明细导出', N'erp:stock-record:export', 3, 5, 2593, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 10:27:21', N'', N'2024-02-05 10:27:21', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2596, N'其它入库', N'', 2, 3, 2583, N'in', N'ep:zoom-in', N'erp/stock/in/index', N'ErpStockIn', 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'1', N'2024-02-07 19:06:51', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2597, N'其它入库单查询', N'erp:stock-in:query', 3, 1, 2596, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-05 16:08:56', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2598, N'其它入库单创建', N'erp:stock-in:create', 3, 2, 2596, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-05 16:08:56', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2599, N'其它入库单更新', N'erp:stock-in:update', 3, 3, 2596, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-05 16:08:56', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2600, N'其它入库单删除', N'erp:stock-in:delete', 3, 4, 2596, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-05 16:08:56', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2601, N'其它入库单导出', N'erp:stock-in:export', 3, 5, 2596, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-05 16:08:56', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2602, N'采购管理', N'', 1, 10, 2563, N'purchase', N'fa:buysellads', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-02-06 16:01:01', N'1', N'2024-02-06 16:01:23', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2603, N'供应商信息', N'', 2, 4, 2602, N'supplier', N'fa:superpowers', N'erp/purchase/supplier/index', N'ErpSupplier', 0, N'1', N'1', N'1', N'', N'2024-02-06 08:21:55', N'1', N'2024-02-06 16:22:25', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2604, N'供应商查询', N'erp:supplier:query', 3, 1, 2603, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-06 08:21:55', N'', N'2024-02-06 08:21:55', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2605, N'供应商创建', N'erp:supplier:create', 3, 2, 2603, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-06 08:21:55', N'', N'2024-02-06 08:21:55', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2606, N'供应商更新', N'erp:supplier:update', 3, 3, 2603, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-06 08:21:55', N'', N'2024-02-06 08:21:55', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2607, N'供应商删除', N'erp:supplier:delete', 3, 4, 2603, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-06 08:21:55', N'', N'2024-02-06 08:21:55', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2608, N'供应商导出', N'erp:supplier:export', 3, 5, 2603, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-06 08:21:55', N'', N'2024-02-06 08:21:55', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2609, N'其它入库单审批', N'erp:stock-in:update-status', 3, 6, 2596, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-05 16:08:56', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2610, N'其它出库', N'', 2, 4, 2583, N'out', N'ep:zoom-out', N'erp/stock/out/index', N'ErpStockOut', 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'1', N'2024-02-07 19:06:55', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2611, N'其它出库单查询', N'erp:stock-out:query', 3, 1, 2610, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 06:43:39', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2612, N'其它出库单创建', N'erp:stock-out:create', 3, 2, 2610, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 06:43:42', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2613, N'其它出库单更新', N'erp:stock-out:update', 3, 3, 2610, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 06:43:44', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2614, N'其它出库单删除', N'erp:stock-out:delete', 3, 4, 2610, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 06:43:56', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2615, N'其它出库单导出', N'erp:stock-out:export', 3, 5, 2610, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 06:43:57', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2616, N'其它出库单审批', N'erp:stock-out:update-status', 3, 6, 2610, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 06:43:58', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2617, N'销售管理', N'', 1, 20, 2563, N'sale', N'fa:sellsy', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-02-07 15:12:32', N'1', N'2024-02-07 15:12:32', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2618, N'客户信息', N'', 2, 4, 2617, N'customer', N'ep:avatar', N'erp/sale/customer/index', N'ErpCustomer', 0, N'1', N'1', N'1', N'', N'2024-02-07 07:21:45', N'1', N'2024-02-07 15:22:25', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2619, N'客户查询', N'erp:customer:query', 3, 1, 2618, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-07 07:21:45', N'', N'2024-02-07 07:21:45', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2620, N'客户创建', N'erp:customer:create', 3, 2, 2618, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-07 07:21:45', N'', N'2024-02-07 07:21:45', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2621, N'客户更新', N'erp:customer:update', 3, 3, 2618, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-07 07:21:45', N'', N'2024-02-07 07:21:45', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2622, N'客户删除', N'erp:customer:delete', 3, 4, 2618, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-07 07:21:45', N'', N'2024-02-07 07:21:45', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2623, N'客户导出', N'erp:customer:export', 3, 5, 2618, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-07 07:21:45', N'', N'2024-02-07 07:21:45', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2624, N'库存调拨', N'', 2, 5, 2583, N'move', N'ep:folder-remove', N'erp/stock/move/index', N'ErpStockMove', 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'1', N'2024-02-16 18:53:55', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2625, N'库存调度单查询', N'erp:stock-move:query', 3, 1, 2624, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:49', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2626, N'库存调度单创建', N'erp:stock-move:create', 3, 2, 2624, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:52', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2627, N'库存调度单更新', N'erp:stock-move:update', 3, 3, 2624, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:55', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2628, N'库存调度单删除', N'erp:stock-move:delete', 3, 4, 2624, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:57', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2629, N'库存调度单导出', N'erp:stock-move:export', 3, 5, 2624, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:59', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2630, N'库存调度单审批', N'erp:stock-move:update-status', 3, 6, 2624, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:13:03', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2631, N'库存盘点', N'', 2, 6, 2583, N'check', N'ep:circle-check-filled', N'erp/stock/check/index', N'ErpStockCheck', 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'1', N'2024-02-08 08:31:09', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2632, N'库存盘点单查询', N'erp:stock-check:query', 3, 1, 2631, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:49', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2633, N'库存盘点单创建', N'erp:stock-check:create', 3, 2, 2631, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:52', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2634, N'库存盘点单更新', N'erp:stock-check:update', 3, 3, 2631, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:55', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2635, N'库存盘点单删除', N'erp:stock-check:delete', 3, 4, 2631, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:57', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2636, N'库存盘点单导出', N'erp:stock-check:export', 3, 5, 2631, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:59', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2637, N'库存盘点单审批', N'erp:stock-check:update-status', 3, 6, 2631, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:13:03', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2638, N'销售订单', N'', 2, 1, 2617, N'order', N'fa:first-order', N'erp/sale/order/index', N'ErpSaleOrder', 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'1', N'2024-02-10 21:59:20', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2639, N'销售订单查询', N'erp:sale-order:query', 3, 1, 2638, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:49', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2640, N'销售订单创建', N'erp:sale-order:create', 3, 2, 2638, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:52', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2641, N'销售订单更新', N'erp:sale-order:update', 3, 3, 2638, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:55', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2642, N'销售订单删除', N'erp:sale-order:delete', 3, 4, 2638, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:57', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2643, N'销售订单导出', N'erp:sale-order:export', 3, 5, 2638, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:59', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2644, N'销售订单审批', N'erp:sale-order:update-status', 3, 6, 2638, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:13:03', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2645, N'财务管理', N'', 1, 50, 2563, N'finance', N'ep:money', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-02-10 08:05:58', N'1', N'2024-02-10 08:06:07', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2646, N'结算账户', N'', 2, 10, 2645, N'account', N'fa:universal-access', N'erp/finance/account/index', N'ErpAccount', 0, N'1', N'1', N'1', N'', N'2024-02-10 00:15:07', N'1', N'2024-02-14 08:24:31', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2647, N'结算账户查询', N'erp:account:query', 3, 1, 2646, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-10 00:15:07', N'', N'2024-02-10 00:15:07', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2648, N'结算账户创建', N'erp:account:create', 3, 2, 2646, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-10 00:15:07', N'', N'2024-02-10 00:15:07', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2649, N'结算账户更新', N'erp:account:update', 3, 3, 2646, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-10 00:15:07', N'', N'2024-02-10 00:15:07', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2650, N'结算账户删除', N'erp:account:delete', 3, 4, 2646, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-10 00:15:07', N'', N'2024-02-10 00:15:07', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2651, N'结算账户导出', N'erp:account:export', 3, 5, 2646, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-10 00:15:07', N'', N'2024-02-10 00:15:07', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2652, N'销售出库', N'', 2, 2, 2617, N'out', N'ep:sold-out', N'erp/sale/out/index', N'ErpSaleOut', 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'1', N'2024-02-10 22:02:07', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2653, N'销售出库查询', N'erp:sale-out:query', 3, 1, 2652, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:49', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2654, N'销售出库创建', N'erp:sale-out:create', 3, 2, 2652, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:52', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2655, N'销售出库更新', N'erp:sale-out:update', 3, 3, 2652, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:55', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2656, N'销售出库删除', N'erp:sale-out:delete', 3, 4, 2652, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:57', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2657, N'销售出库导出', N'erp:sale-out:export', 3, 5, 2652, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:59', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2658, N'销售出库审批', N'erp:sale-out:update-status', 3, 6, 2652, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:13:03', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2659, N'销售退货', N'', 2, 3, 2617, N'return', N'fa-solid:bone', N'erp/sale/return/index', N'ErpSaleReturn', 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'1', N'2024-02-12 06:12:58', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2660, N'销售退货查询', N'erp:sale-return:query', 3, 1, 2659, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:49', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2661, N'销售退货创建', N'erp:sale-return:create', 3, 2, 2659, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:52', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2662, N'销售退货更新', N'erp:sale-return:update', 3, 3, 2659, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:55', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2663, N'销售退货删除', N'erp:sale-return:delete', 3, 4, 2659, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:57', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2664, N'销售退货导出', N'erp:sale-return:export', 3, 5, 2659, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:12:59', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2665, N'销售退货审批', N'erp:sale-return:update-status', 3, 6, 2659, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-07 11:13:03', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2666, N'采购订单', N'', 2, 1, 2602, N'order', N'fa-solid:border-all', N'erp/purchase/order/index', N'ErpPurchaseOrder', 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'1', N'2024-02-12 08:51:49', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2667, N'采购订单查询', N'erp:purchase-order:query', 3, 1, 2666, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:45:17', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2668, N'采购订单创建', N'erp:purchase-order:create', 3, 2, 2666, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:44:54', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2669, N'采购订单更新', N'erp:purchase-order:update', 3, 3, 2666, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:44:58', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2670, N'采购订单删除', N'erp:purchase-order:delete', 3, 4, 2666, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:45:00', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2671, N'采购订单导出', N'erp:purchase-order:export', 3, 5, 2666, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:45:05', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2672, N'采购订单审批', N'erp:purchase-order:update-status', 3, 6, 2666, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:45:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2673, N'采购入库', N'', 2, 2, 2602, N'in', N'fa-solid:gopuram', N'erp/purchase/in/index', N'ErpPurchaseIn', 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'1', N'2024-02-12 11:19:27', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2674, N'采购入库查询', N'erp:purchase-in:query', 3, 1, 2673, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:45:17', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2675, N'采购入库创建', N'erp:purchase-in:create', 3, 2, 2673, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:44:54', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2676, N'采购入库更新', N'erp:purchase-in:update', 3, 3, 2673, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:44:58', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2677, N'采购入库删除', N'erp:purchase-in:delete', 3, 4, 2673, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:45:00', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2678, N'采购入库导出', N'erp:purchase-in:export', 3, 5, 2673, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:45:05', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2679, N'采购入库审批', N'erp:purchase-in:update-status', 3, 6, 2673, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:45:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2680, N'采购退货', N'', 2, 3, 2602, N'return', N'ep:minus', N'erp/purchase/return/index', N'ErpPurchaseReturn', 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'1', N'2024-02-12 20:51:02', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2681, N'采购退货查询', N'erp:purchase-return:query', 3, 1, 2680, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:45:17', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2682, N'采购退货创建', N'erp:purchase-return:create', 3, 2, 2680, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:44:54', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2683, N'采购退货更新', N'erp:purchase-return:update', 3, 3, 2680, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:44:58', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2684, N'采购退货删除', N'erp:purchase-return:delete', 3, 4, 2680, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:45:00', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2685, N'采购退货导出', N'erp:purchase-return:export', 3, 5, 2680, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:45:05', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2686, N'采购退货审批', N'erp:purchase-return:update-status', 3, 6, 2680, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:45:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2687, N'付款单', N'', 2, 1, 2645, N'payment', N'ep:caret-right', N'erp/finance/payment/index', N'ErpFinancePayment', 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'1', N'2024-02-14 08:24:23', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2688, N'付款单查询', N'erp:finance-payment:query', 3, 1, 2687, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:45:17', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2689, N'付款单创建', N'erp:finance-payment:create', 3, 2, 2687, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:44:54', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2690, N'付款单更新', N'erp:finance-payment:update', 3, 3, 2687, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:44:58', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2691, N'付款单删除', N'erp:finance-payment:delete', 3, 4, 2687, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:45:00', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2692, N'付款单导出', N'erp:finance-payment:export', 3, 5, 2687, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:45:05', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2693, N'付款单审批', N'erp:finance-payment:update-status', 3, 6, 2687, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:45:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2694, N'收款单', N'', 2, 2, 2645, N'receipt', N'ep:expand', N'erp/finance/receipt/index', N'ErpFinanceReceipt', 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'1', N'2024-02-15 19:35:45', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2695, N'收款单查询', N'erp:finance-receipt:query', 3, 1, 2694, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:45:17', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2696, N'收款单创建', N'erp:finance-receipt:create', 3, 2, 2694, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:44:54', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2697, N'收款单更新', N'erp:finance-receipt:update', 3, 3, 2694, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:44:58', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2698, N'收款单删除', N'erp:finance-receipt:delete', 3, 4, 2694, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:45:00', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2699, N'收款单导出', N'erp:finance-receipt:export', 3, 5, 2694, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:45:05', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2700, N'收款单审批', N'erp:finance-receipt:update-status', 3, 6, 2694, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-02-05 16:08:56', N'', N'2024-02-12 00:45:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2701, N'待办事项', N'', 2, 0, 2397, N'backlog', N'fa-solid:tasks', N'crm/backlog/index', N'CrmBacklog', 0, N'1', N'1', N'1', N'1', N'2024-02-17 17:17:11', N'1', N'2024-02-17 17:17:11', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2702, N'ERP 首页', N'erp:statistics:query', 2, 0, 2563, N'home', N'ep:home-filled', N'erp/home/index.vue', N'ErpHome', 0, N'1', N'1', N'1', N'1', N'2024-02-18 16:49:40', N'1', N'2024-02-26 21:12:18', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2703, N'商机状态配置', N'', 2, 4, 2524, N'business-status', N'fa-solid:charging-station', N'crm/business/status/index', N'CrmBusinessStatus', 0, N'1', N'1', N'1', N'1', N'2024-02-21 20:15:17', N'1', N'2024-02-21 20:15:17', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2704, N'商机状态查询', N'crm:business-status:query', 3, 1, 2703, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-02-21 20:35:36', N'1', N'2024-02-21 20:36:06', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2705, N'商机状态创建', N'crm:business-status:create', 3, 2, 2703, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-02-21 20:35:57', N'1', N'2024-02-21 20:35:57', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2706, N'商机状态更新', N'crm:business-status:update', 3, 3, 2703, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-02-21 20:36:21', N'1', N'2024-02-21 20:36:21', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2707, N'商机状态删除', N'crm:business-status:delete', 3, 4, 2703, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-02-21 20:36:36', N'1', N'2024-02-21 20:36:36', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2708, N'合同配置', N'', 2, 5, 2524, N'contract-config', N'ep:connection', N'crm/contract/config/index', N'CrmContractConfig', 0, N'1', N'1', N'1', N'1', N'2024-02-24 16:44:40', N'1', N'2024-02-24 16:44:48', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2709, N'客户公海配置查询', N'crm:customer-pool-config:query', 3, 2, 2516, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-02-24 16:45:19', N'1', N'2024-02-24 16:45:28', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2710, N'合同配置更新', N'crm:contract-config:update', 3, 1, 2708, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-02-24 16:45:56', N'1', N'2024-02-24 16:45:56', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2711, N'合同配置查询', N'crm:contract-config:query', 3, 2, 2708, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-02-24 16:46:16', N'1', N'2024-02-24 16:46:16', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2712, N'客户分析', N'crm:statistics-customer:query', 2, 0, 2560, N'customer', N'ep:avatar', N'views/crm/statistics/customer/index.vue', N'CrmStatisticsCustomer', 0, N'1', N'1', N'1', N'1', N'2024-03-09 16:43:56', N'1', N'2024-04-24 19:42:52', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2713, N'抄送我的', N'bpm:process-instance-cc:query', 2, 30, 1200, N'copy', N'ep:copy-document', N'bpm/task/copy/index', N'BpmProcessInstanceCopy', 0, N'1', N'1', N'1', N'1', N'2024-03-17 21:50:23', N'1', N'2024-04-24 19:55:12', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2714, N'流程分类', N'', 2, 3, 1186, N'category', N'fa:object-ungroup', N'bpm/category/index', N'BpmCategory', 0, N'1', N'1', N'1', N'', N'2024-03-08 02:00:51', N'1', N'2024-03-21 23:51:18', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2715, N'分类查询', N'bpm:category:query', 3, 1, 2714, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'', N'2024-03-08 02:00:51', N'1', N'2024-03-19 14:36:25', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2716, N'分类创建', N'bpm:category:create', 3, 2, 2714, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'', N'2024-03-08 02:00:51', N'1', N'2024-03-19 14:36:31', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2717, N'分类更新', N'bpm:category:update', 3, 3, 2714, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'', N'2024-03-08 02:00:51', N'1', N'2024-03-19 14:36:35', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2718, N'分类删除', N'bpm:category:delete', 3, 4, 2714, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'', N'2024-03-08 02:00:51', N'1', N'2024-03-19 14:36:41', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2720, N'发起流程', N'', 2, 0, 1200, N'create', N'fa-solid:grin-stars', N'bpm/processInstance/create/index', N'BpmProcessInstanceCreate', 0, N'1', N'0', N'1', N'1', N'2024-03-19 19:46:05', N'1', N'2024-03-23 19:03:42', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2721, N'流程实例', N'', 2, 10, 1186, N'process-instance/manager', N'fa:square', N'bpm/processInstance/manager/index', N'BpmProcessInstanceManager', 0, N'1', N'1', N'1', N'1', N'2024-03-21 23:57:30', N'1', N'2024-03-21 23:57:30', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2722, N'流程实例的查询（管理员）', N'bpm:process-instance:manager-query', 3, 1, 2721, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-03-22 08:18:27', N'1', N'2024-03-22 08:19:05', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2723, N'流程实例的取消（管理员）', N'bpm:process-instance:cancel-by-admin', 3, 2, 2721, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-03-22 08:19:25', N'1', N'2024-03-22 08:19:25', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2724, N'流程任务', N'', 2, 11, 1186, N'process-tasnk', N'ep:collection-tag', N'bpm/task/manager/index', N'BpmManagerTask', 0, N'1', N'1', N'1', N'1', N'2024-03-22 08:43:22', N'1', N'2024-03-22 08:43:27', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2725, N'流程任务的查询（管理员）', N'bpm:task:mananger-query', 3, 1, 2724, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-03-22 08:43:49', N'1', N'2024-03-22 08:43:49', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2726, N'流程监听器', N'', 2, 5, 1186, N'process-listener', N'fa:assistive-listening-systems', N'bpm/processListener/index', N'BpmProcessListener', 0, N'1', N'1', N'1', N'', N'2024-03-09 16:05:34', N'1', N'2024-03-23 13:13:38', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2727, N'流程监听器查询', N'bpm:process-listener:query', 3, 1, 2726, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-03-09 16:05:34', N'', N'2024-03-09 16:05:34', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2728, N'流程监听器创建', N'bpm:process-listener:create', 3, 2, 2726, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-03-09 16:05:34', N'', N'2024-03-09 16:05:34', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2729, N'流程监听器更新', N'bpm:process-listener:update', 3, 3, 2726, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-03-09 16:05:34', N'', N'2024-03-09 16:05:34', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2730, N'流程监听器删除', N'bpm:process-listener:delete', 3, 4, 2726, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-03-09 16:05:34', N'', N'2024-03-09 16:05:34', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2731, N'流程表达式', N'', 2, 6, 1186, N'process-expression', N'fa:wpexplorer', N'bpm/processExpression/index', N'BpmProcessExpression', 0, N'1', N'1', N'1', N'', N'2024-03-09 22:35:08', N'1', N'2024-03-23 19:43:05', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2732, N'流程表达式查询', N'bpm:process-expression:query', 3, 1, 2731, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-03-09 22:35:08', N'', N'2024-03-09 22:35:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2733, N'流程表达式创建', N'bpm:process-expression:create', 3, 2, 2731, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-03-09 22:35:08', N'', N'2024-03-09 22:35:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2734, N'流程表达式更新', N'bpm:process-expression:update', 3, 3, 2731, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-03-09 22:35:08', N'', N'2024-03-09 22:35:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2735, N'流程表达式删除', N'bpm:process-expression:delete', 3, 4, 2731, N'', N'', N'', NULL, 0, N'1', N'1', N'1', N'', N'2024-03-09 22:35:08', N'', N'2024-03-09 22:35:08', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2736, N'员工业绩', N'crm:statistics-performance:query', 2, 3, 2560, N'performance', N'ep:dish-dot', N'crm/statistics/performance/index', N'CrmStatisticsPerformance', 0, N'1', N'1', N'1', N'1', N'2024-04-05 13:49:20', N'1', N'2024-04-24 19:42:43', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2737, N'客户画像', N'crm:statistics-portrait:query', 2, 4, 2560, N'portrait', N'ep:picture', N'crm/statistics/portrait/index', N'CrmStatisticsPortrait', 0, N'1', N'1', N'1', N'1', N'2024-04-05 13:57:40', N'1', N'2024-04-24 19:42:24', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2738, N'销售漏斗', N'crm:statistics-funnel:query', 2, 5, 2560, N'funnel', N'ep:grape', N'crm/statistics/funnel/index', N'CrmStatisticsFunnel', 0, N'1', N'1', N'1', N'1', N'2024-04-13 10:53:26', N'1', N'2024-04-24 19:39:33', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2739, N'消息中心', N'', 1, 7, 1, N'messages', N'ep:chat-dot-round', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-04-22 23:54:30', N'1', N'2024-04-23 09:36:35', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2740, N'监控中心', N'', 1, 10, 2, N'monitors', N'ep:monitor', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-04-23 00:04:44', N'1', N'2024-04-23 00:04:44', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2741, N'领取公海客户', N'crm:customer:receive', 3, 1, 2546, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-04-24 19:47:45', N'1', N'2024-04-24 19:47:45', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2742, N'分配公海客户', N'crm:customer:distribute', 3, 2, 2546, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-04-24 19:48:05', N'1', N'2024-04-24 19:48:05', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2743, N'商品统计查询', N'statistics:product:query', 3, 1, 2545, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-04-24 19:50:05', N'1', N'2024-04-24 19:50:05', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2744, N'商品统计导出', N'statistics:product:export', 3, 2, 2545, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-04-24 19:50:26', N'1', N'2024-04-24 19:50:26', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2745, N'支付渠道查询', N'pay:channel:query', 3, 10, 1126, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-04-24 19:53:01', N'1', N'2024-04-24 19:53:01', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2746, N'支付渠道创建', N'pay:channel:create', 3, 11, 1126, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-04-24 19:53:18', N'1', N'2024-04-24 19:53:18', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2747, N'支付渠道更新', N'pay:channel:update', 3, 12, 1126, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-04-24 19:53:32', N'1', N'2024-04-24 19:53:58', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2748, N'支付渠道删除', N'pay:channel:delete', 3, 13, 1126, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-04-24 19:54:34', N'1', N'2024-04-24 19:54:34', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2749, N'商品收藏查询', N'product:favorite:query', 3, 10, 2014, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-04-24 19:55:47', N'1', N'2024-04-24 19:55:47', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2750, N'商品浏览查询', N'product:browse-history:query', 3, 20, 2014, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-04-24 19:57:43', N'1', N'2024-04-24 19:57:43', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2751, N'售后同意', N'trade:after-sale:agree', 3, 2, 2073, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-04-24 19:58:40', N'1', N'2024-04-24 19:58:40', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2752, N'售后不同意', N'trade:after-sale:disagree', 3, 3, 2073, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-04-24 19:59:03', N'1', N'2024-04-24 19:59:03', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2753, N'售后确认退货', N'trade:after-sale:receive', 3, 4, 2073, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-04-24 20:00:07', N'1', N'2024-04-24 20:00:07', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2754, N'售后确认退款', N'trade:after-sale:refund', 3, 5, 2073, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-04-24 20:00:24', N'1', N'2024-04-24 20:00:24', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2755, N'删除项目', N'report:go-view-project:delete', 3, 2, 2153, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-04-24 20:01:37', N'1', N'2024-04-24 20:01:37', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2756, N'会员等级记录查询', N'member:level-record:query', 3, 10, 2325, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-04-24 20:02:32', N'1', N'2024-04-24 20:02:32', N'0')
GO
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES (2757, N'会员经验记录查询', N'member:experience-record:query', 3, 11, 2325, N'', N'', N'', N'', 0, N'1', N'1', N'1', N'1', N'2024-04-24 20:02:51', N'1', N'2024-04-24 20:02:51', N'0')
GO
SET IDENTITY_INSERT system_menu OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for system_notice
-- ----------------------------
DROP TABLE IF EXISTS system_notice
GO
CREATE TABLE system_notice
(
    id          bigint                                 NOT NULL PRIMARY KEY IDENTITY,
    title       nvarchar(50)                           NOT NULL,
    content     nvarchar(max)                          NOT NULL,
    type        tinyint                                NOT NULL,
    status      tinyint      DEFAULT 0                 NOT NULL,
    creator     nvarchar(64) DEFAULT ''                NULL,
    create_time datetime2    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64) DEFAULT ''                NULL,
    update_time datetime2    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit          DEFAULT 0                 NOT NULL,
    tenant_id   bigint       DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'公告ID',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notice',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'公告标题',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notice',
     'COLUMN', N'title'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'公告内容',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notice',
     'COLUMN', N'content'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'公告类型（1通知 2公告）',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notice',
     'COLUMN', N'type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'公告状态（0正常 1关闭）',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notice',
     'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notice',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notice',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notice',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notice',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notice',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notice',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'通知公告表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notice'
GO

-- ----------------------------
-- Records of system_notice
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT system_notice ON
GO
INSERT INTO system_notice (id, title, content, type, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1, N'芋道的公众', N'<p>新版本内容133</p>', 1, 0, N'admin', N'2021-01-05 17:03:48', N'1', N'2022-05-04 21:00:20', N'0', 1)
GO
INSERT INTO system_notice (id, title, content, type, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2, N'维护通知：2018-07-01 系统凌晨维护', N'<p><img src="http://test.yudao.iocoder.cn/b7cb3cf49b4b3258bf7309a09dd2f4e5.jpg" alt="" data-href="" style=""/>11112222</p>', 2, 1, N'admin', N'2021-01-05 17:03:48', N'1', N'2023-12-02 20:07:26', N'0', 1)
GO
INSERT INTO system_notice (id, title, content, type, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4, N'我是测试标题', N'<p>哈哈哈哈123</p>', 1, 0, N'110', N'2022-02-22 01:01:25', N'110', N'2022-02-22 01:01:46', N'0', 121)
GO
SET IDENTITY_INSERT system_notice OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for system_notify_message
-- ----------------------------
DROP TABLE IF EXISTS system_notify_message
GO
CREATE TABLE system_notify_message
(
    id                bigint                                 NOT NULL PRIMARY KEY IDENTITY,
    user_id           bigint                                 NOT NULL,
    user_type         tinyint                                NOT NULL,
    template_id       bigint                                 NOT NULL,
    template_code     nvarchar(64)                           NOT NULL,
    template_nickname nvarchar(63)                           NOT NULL,
    template_content  nvarchar(1024)                         NOT NULL,
    template_type     int                                    NOT NULL,
    template_params   nvarchar(255)                          NOT NULL,
    read_status       varchar(1)                             NOT NULL,
    read_time         datetime2    DEFAULT NULL              NULL,
    creator           nvarchar(64) DEFAULT ''                NULL,
    create_time       datetime2    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater           nvarchar(64) DEFAULT ''                NULL,
    update_time       datetime2    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted           bit          DEFAULT 0                 NOT NULL,
    tenant_id         bigint       DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户ID',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_message',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户id',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_message',
     'COLUMN', N'user_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_message',
     'COLUMN', N'user_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模版编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_message',
     'COLUMN', N'template_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模板编码',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_message',
     'COLUMN', N'template_code'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模版发送人名称',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_message',
     'COLUMN', N'template_nickname'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模版内容',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_message',
     'COLUMN', N'template_content'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模版类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_message',
     'COLUMN', N'template_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模版参数',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_message',
     'COLUMN', N'template_params'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否已读',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_message',
     'COLUMN', N'read_status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'阅读时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_message',
     'COLUMN', N'read_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_message',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_message',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_message',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_message',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_message',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_message',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'站内信消息表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_message'
GO

-- ----------------------------
-- Records of system_notify_message
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT system_notify_message ON
GO
INSERT INTO system_notify_message (id, user_id, user_type, template_id, template_code, template_nickname, template_content, template_type, template_params, read_status, read_time, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2, 1, 2, 1, N'test', N'123', N'我是 1，我开始 2 了', 1, N'{"name":"1","what":"2"}', N'1', N'2023-02-10 00:47:04', N'1', N'2023-01-28 11:44:08', N'1', N'2023-02-10 00:47:04', N'0', 1)
GO
INSERT INTO system_notify_message (id, user_id, user_type, template_id, template_code, template_nickname, template_content, template_type, template_params, read_status, read_time, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3, 1, 2, 1, N'test', N'123', N'我是 1，我开始 2 了', 1, N'{"name":"1","what":"2"}', N'1', N'2023-02-10 00:47:04', N'1', N'2023-01-28 11:45:04', N'1', N'2023-02-10 00:47:04', N'0', 1)
GO
INSERT INTO system_notify_message (id, user_id, user_type, template_id, template_code, template_nickname, template_content, template_type, template_params, read_status, read_time, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4, 103, 2, 2, N'register', N'系统消息', N'你好，欢迎 哈哈 加入大家庭！', 2, N'{"name":"哈哈"}', N'0', NULL, N'1', N'2023-01-28 21:02:20', N'1', N'2023-01-28 21:02:20', N'0', 1)
GO
INSERT INTO system_notify_message (id, user_id, user_type, template_id, template_code, template_nickname, template_content, template_type, template_params, read_status, read_time, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (5, 1, 2, 1, N'test', N'123', N'我是 芋艿，我开始 写代码 了', 1, N'{"name":"芋艿","what":"写代码"}', N'1', N'2023-02-10 00:47:04', N'1', N'2023-01-28 22:21:42', N'1', N'2023-02-10 00:47:04', N'0', 1)
GO
INSERT INTO system_notify_message (id, user_id, user_type, template_id, template_code, template_nickname, template_content, template_type, template_params, read_status, read_time, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (6, 1, 2, 1, N'test', N'123', N'我是 芋艿，我开始 写代码 了', 1, N'{"name":"芋艿","what":"写代码"}', N'1', N'2023-01-29 10:52:06', N'1', N'2023-01-28 22:22:07', N'1', N'2023-01-29 10:52:06', N'0', 1)
GO
INSERT INTO system_notify_message (id, user_id, user_type, template_id, template_code, template_nickname, template_content, template_type, template_params, read_status, read_time, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (7, 1, 2, 1, N'test', N'123', N'我是 2，我开始 3 了', 1, N'{"name":"2","what":"3"}', N'1', N'2023-01-29 10:52:06', N'1', N'2023-01-28 23:45:21', N'1', N'2023-01-29 10:52:06', N'0', 1)
GO
INSERT INTO system_notify_message (id, user_id, user_type, template_id, template_code, template_nickname, template_content, template_type, template_params, read_status, read_time, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (8, 1, 2, 2, N'register', N'系统消息', N'你好，欢迎 123 加入大家庭！', 2, N'{"name":"123"}', N'1', N'2023-01-29 10:52:06', N'1', N'2023-01-28 23:50:21', N'1', N'2023-01-29 10:52:06', N'0', 1)
GO
INSERT INTO system_notify_message (id, user_id, user_type, template_id, template_code, template_nickname, template_content, template_type, template_params, read_status, read_time, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (9, 247, 1, 4, N'brokerage_withdraw_audit_approve', N'system', N'您在2023-09-28 08:35:46提现￥0.09元的申请已通过审核', 2, N'{"reason":null,"createTime":"2023-09-28 08:35:46","price":"0.09"}', N'0', NULL, N'1', N'2023-09-28 16:36:22', N'1', N'2023-09-28 16:36:22', N'0', 1)
GO
INSERT INTO system_notify_message (id, user_id, user_type, template_id, template_code, template_nickname, template_content, template_type, template_params, read_status, read_time, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (10, 247, 1, 4, N'brokerage_withdraw_audit_approve', N'system', N'您在2023-09-30 20:59:40提现￥1.00元的申请已通过审核', 2, N'{"reason":null,"createTime":"2023-09-30 20:59:40","price":"1.00"}', N'0', NULL, N'1', N'2023-10-03 12:11:34', N'1', N'2023-10-03 12:11:34', N'0', 1)
GO
SET IDENTITY_INSERT system_notify_message OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for system_notify_template
-- ----------------------------
DROP TABLE IF EXISTS system_notify_template
GO
CREATE TABLE system_notify_template
(
    id          bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    name        nvarchar(63)                            NOT NULL,
    code        nvarchar(64)                            NOT NULL,
    nickname    nvarchar(255)                           NOT NULL,
    content     nvarchar(1024)                          NOT NULL,
    type        tinyint                                 NOT NULL,
    params      nvarchar(255) DEFAULT NULL              NULL,
    status      tinyint                                 NOT NULL,
    remark      nvarchar(255) DEFAULT NULL              NULL,
    creator     nvarchar(64)  DEFAULT ''                NULL,
    create_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64)  DEFAULT ''                NULL,
    update_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit           DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'主键',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_template',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模板名称',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_template',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模版编码',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_template',
     'COLUMN', N'code'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'发送人名称',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_template',
     'COLUMN', N'nickname'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模版内容',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_template',
     'COLUMN', N'content'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_template',
     'COLUMN', N'type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'参数数组',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_template',
     'COLUMN', N'params'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'状态',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_template',
     'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'备注',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_template',
     'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_template',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_template',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_template',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_template',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_template',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'站内信模板表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_notify_template'
GO

-- ----------------------------
-- Table structure for system_oauth2_access_token
-- ----------------------------
DROP TABLE IF EXISTS system_oauth2_access_token
GO
CREATE TABLE system_oauth2_access_token
(
    id            bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    user_id       bigint                                  NOT NULL,
    user_type     tinyint                                 NOT NULL,
    user_info     nvarchar(512)                           NOT NULL,
    access_token  nvarchar(255)                           NOT NULL,
    refresh_token nvarchar(32)                            NOT NULL,
    client_id     nvarchar(255)                           NOT NULL,
    scopes        nvarchar(255) DEFAULT NULL              NULL,
    expires_time  datetime2                               NOT NULL,
    creator       nvarchar(64)  DEFAULT ''                NULL,
    create_time   datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater       nvarchar(64)  DEFAULT ''                NULL,
    update_time   datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted       bit           DEFAULT 0                 NOT NULL,
    tenant_id     bigint        DEFAULT 0                 NOT NULL
)
GO

CREATE INDEX idx_system_oauth2_access_token_01 ON system_oauth2_access_token (access_token)
GO
CREATE INDEX idx_system_oauth2_access_token_02 ON system_oauth2_access_token (refresh_token)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_access_token',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_access_token',
     'COLUMN', N'user_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_access_token',
     'COLUMN', N'user_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户信息',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_access_token',
     'COLUMN', N'user_info'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'访问令牌',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_access_token',
     'COLUMN', N'access_token'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'刷新令牌',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_access_token',
     'COLUMN', N'refresh_token'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'客户端编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_access_token',
     'COLUMN', N'client_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'授权范围',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_access_token',
     'COLUMN', N'scopes'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'过期时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_access_token',
     'COLUMN', N'expires_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_access_token',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_access_token',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_access_token',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_access_token',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_access_token',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_access_token',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'OAuth2 访问令牌',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_access_token'
GO

-- ----------------------------
-- Table structure for system_oauth2_approve
-- ----------------------------
DROP TABLE IF EXISTS system_oauth2_approve
GO
CREATE TABLE system_oauth2_approve
(
    id           bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    user_id      bigint                                  NOT NULL,
    user_type    tinyint                                 NOT NULL,
    client_id    nvarchar(255)                           NOT NULL,
    scope        nvarchar(255) DEFAULT ''                NOT NULL,
    approved     varchar(1)    DEFAULT '0'               NOT NULL,
    expires_time datetime2                               NOT NULL,
    creator      nvarchar(64)  DEFAULT ''                NULL,
    create_time  datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater      nvarchar(64)  DEFAULT ''                NULL,
    update_time  datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted      bit           DEFAULT 0                 NOT NULL,
    tenant_id    bigint        DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_approve',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_approve',
     'COLUMN', N'user_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_approve',
     'COLUMN', N'user_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'客户端编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_approve',
     'COLUMN', N'client_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'授权范围',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_approve',
     'COLUMN', N'scope'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否接受',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_approve',
     'COLUMN', N'approved'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'过期时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_approve',
     'COLUMN', N'expires_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_approve',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_approve',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_approve',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_approve',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_approve',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_approve',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'OAuth2 批准表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_approve'
GO

-- ----------------------------
-- Table structure for system_oauth2_client
-- ----------------------------
DROP TABLE IF EXISTS system_oauth2_client
GO
CREATE TABLE system_oauth2_client
(
    id                             bigint                                   NOT NULL PRIMARY KEY IDENTITY,
    client_id                      nvarchar(255)                            NOT NULL,
    secret                         nvarchar(255)                            NOT NULL,
    name                           nvarchar(255)                            NOT NULL,
    logo                           nvarchar(255)                            NOT NULL,
    description                    nvarchar(255)  DEFAULT NULL              NULL,
    status                         tinyint                                  NOT NULL,
    access_token_validity_seconds  int                                      NOT NULL,
    refresh_token_validity_seconds int                                      NOT NULL,
    redirect_uris                  nvarchar(255)                            NOT NULL,
    authorized_grant_types         nvarchar(255)                            NOT NULL,
    scopes                         nvarchar(255)  DEFAULT NULL              NULL,
    auto_approve_scopes            nvarchar(255)  DEFAULT NULL              NULL,
    authorities                    nvarchar(255)  DEFAULT NULL              NULL,
    resource_ids                   nvarchar(255)  DEFAULT NULL              NULL,
    additional_information         nvarchar(4000) DEFAULT NULL              NULL,
    creator                        nvarchar(64)   DEFAULT ''                NULL,
    create_time                    datetime2      DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater                        nvarchar(64)   DEFAULT ''                NULL,
    update_time                    datetime2      DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted                        bit            DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'客户端编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'client_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'客户端密钥',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'secret'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'应用名',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'应用图标',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'logo'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'应用描述',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'description'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'状态',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'访问令牌的有效期',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'access_token_validity_seconds'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'刷新令牌的有效期',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'refresh_token_validity_seconds'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'可重定向的 URI 地址',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'redirect_uris'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'授权类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'authorized_grant_types'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'授权范围',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'scopes'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'自动通过的授权范围',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'auto_approve_scopes'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'权限',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'authorities'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'资源',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'resource_ids'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'附加信息',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'additional_information'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'OAuth2 客户端表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_client'
GO

-- ----------------------------
-- Records of system_oauth2_client
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT system_oauth2_client ON
GO
INSERT INTO system_oauth2_client (id, client_id, secret, name, logo, description, status, access_token_validity_seconds, refresh_token_validity_seconds, redirect_uris, authorized_grant_types, scopes, auto_approve_scopes, authorities, resource_ids, additional_information, creator, create_time, updater, update_time, deleted) VALUES (1, N'default', N'admin123', N'芋道源码', N'http://test.yudao.iocoder.cn/a5e2e244368878a366b516805a4aabf1.png', N'我是描述', 0, 1800, 2592000, N'["https://www.iocoder.cn","https://doc.iocoder.cn"]', N'["password","authorization_code","implicit","refresh_token"]', N'["user.read","user.write"]', N'[]', N'["user.read","user.write"]', N'[]', N'{}', N'1', N'2022-05-11 21:47:12', N'1', N'2024-02-22 16:31:52', N'0')
GO
INSERT INTO system_oauth2_client (id, client_id, secret, name, logo, description, status, access_token_validity_seconds, refresh_token_validity_seconds, redirect_uris, authorized_grant_types, scopes, auto_approve_scopes, authorities, resource_ids, additional_information, creator, create_time, updater, update_time, deleted) VALUES (40, N'test', N'test2', N'biubiu', N'http://test.yudao.iocoder.cn/277a899d573723f1fcdfb57340f00379.png', N'啦啦啦啦', 0, 1800, 43200, N'["https://www.iocoder.cn"]', N'["password","authorization_code","implicit"]', N'["user_info","projects"]', N'["user_info"]', N'[]', N'[]', N'{}', N'1', N'2022-05-12 00:28:20', N'1', N'2023-12-02 21:01:01', N'0')
GO
INSERT INTO system_oauth2_client (id, client_id, secret, name, logo, description, status, access_token_validity_seconds, refresh_token_validity_seconds, redirect_uris, authorized_grant_types, scopes, auto_approve_scopes, authorities, resource_ids, additional_information, creator, create_time, updater, update_time, deleted) VALUES (41, N'yudao-sso-demo-by-code', N'test', N'基于授权码模式，如何实现 SSO 单点登录？', N'http://test.yudao.iocoder.cn/fe4ed36596adad5120036ef61a6d0153654544d44af8dd4ad3ffe8f759933d6f.png', NULL, 0, 1800, 43200, N'["http://127.0.0.1:18080"]', N'["authorization_code","refresh_token"]', N'["user.read","user.write"]', N'[]', N'[]', N'[]', NULL, N'1', N'2022-09-29 13:28:31', N'1', N'2022-09-29 13:28:31', N'0')
GO
INSERT INTO system_oauth2_client (id, client_id, secret, name, logo, description, status, access_token_validity_seconds, refresh_token_validity_seconds, redirect_uris, authorized_grant_types, scopes, auto_approve_scopes, authorities, resource_ids, additional_information, creator, create_time, updater, update_time, deleted) VALUES (42, N'yudao-sso-demo-by-password', N'test', N'基于密码模式，如何实现 SSO 单点登录？', N'http://test.yudao.iocoder.cn/604bdc695e13b3b22745be704d1f2aa8ee05c5f26f9fead6d1ca49005afbc857.jpeg', NULL, 0, 1800, 43200, N'["http://127.0.0.1:18080"]', N'["password","refresh_token"]', N'["user.read","user.write"]', N'[]', N'[]', N'[]', NULL, N'1', N'2022-10-04 17:40:16', N'1', N'2022-10-04 20:31:21', N'0')
GO
SET IDENTITY_INSERT system_oauth2_client OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for system_oauth2_code
-- ----------------------------
DROP TABLE IF EXISTS system_oauth2_code
GO
CREATE TABLE system_oauth2_code
(
    id           bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    user_id      bigint                                  NOT NULL,
    user_type    tinyint                                 NOT NULL,
    code         nvarchar(32)                            NOT NULL,
    client_id    nvarchar(255)                           NOT NULL,
    scopes       nvarchar(255) DEFAULT ''                NULL,
    expires_time datetime2                               NOT NULL,
    redirect_uri nvarchar(255) DEFAULT NULL              NULL,
    state        nvarchar(255) DEFAULT ''                NOT NULL,
    creator      nvarchar(64)  DEFAULT ''                NULL,
    create_time  datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater      nvarchar(64)  DEFAULT ''                NULL,
    update_time  datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted      bit           DEFAULT 0                 NOT NULL,
    tenant_id    bigint        DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_code',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_code',
     'COLUMN', N'user_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_code',
     'COLUMN', N'user_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'授权码',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_code',
     'COLUMN', N'code'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'客户端编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_code',
     'COLUMN', N'client_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'授权范围',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_code',
     'COLUMN', N'scopes'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'过期时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_code',
     'COLUMN', N'expires_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'可重定向的 URI 地址',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_code',
     'COLUMN', N'redirect_uri'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'状态',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_code',
     'COLUMN', N'state'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_code',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_code',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_code',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_code',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_code',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_code',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'OAuth2 授权码表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_code'
GO

-- ----------------------------
-- Table structure for system_oauth2_refresh_token
-- ----------------------------
DROP TABLE IF EXISTS system_oauth2_refresh_token
GO
CREATE TABLE system_oauth2_refresh_token
(
    id            bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    user_id       bigint                                  NOT NULL,
    refresh_token nvarchar(32)                            NOT NULL,
    user_type     tinyint                                 NOT NULL,
    client_id     nvarchar(255)                           NOT NULL,
    scopes        nvarchar(255) DEFAULT NULL              NULL,
    expires_time  datetime2                               NOT NULL,
    creator       nvarchar(64)  DEFAULT ''                NULL,
    create_time   datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater       nvarchar(64)  DEFAULT ''                NULL,
    update_time   datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted       bit           DEFAULT 0                 NOT NULL,
    tenant_id     bigint        DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_refresh_token',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_refresh_token',
     'COLUMN', N'user_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'刷新令牌',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_refresh_token',
     'COLUMN', N'refresh_token'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_refresh_token',
     'COLUMN', N'user_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'客户端编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_refresh_token',
     'COLUMN', N'client_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'授权范围',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_refresh_token',
     'COLUMN', N'scopes'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'过期时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_refresh_token',
     'COLUMN', N'expires_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_refresh_token',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_refresh_token',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_refresh_token',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_refresh_token',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_refresh_token',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_refresh_token',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'OAuth2 刷新令牌',
     'SCHEMA', N'dbo',
     'TABLE', N'system_oauth2_refresh_token'
GO

-- ----------------------------
-- Table structure for system_operate_log
-- ----------------------------
DROP TABLE IF EXISTS system_operate_log
GO
CREATE TABLE system_operate_log
(
    id             bigint                                   NOT NULL PRIMARY KEY IDENTITY,
    trace_id       nvarchar(64)   DEFAULT ''                NOT NULL,
    user_id        bigint                                   NOT NULL,
    user_type      tinyint        DEFAULT 0                 NOT NULL,
    type           nvarchar(50)                             NOT NULL,
    sub_type       nvarchar(50)                             NOT NULL,
    biz_id         bigint                                   NOT NULL,
    action         nvarchar(2000) DEFAULT ''                NOT NULL,
    extra          nvarchar(2000) DEFAULT ''                NOT NULL,
    request_method nvarchar(16)   DEFAULT ''                NULL,
    request_url    nvarchar(255)  DEFAULT ''                NULL,
    user_ip        nvarchar(50)   DEFAULT NULL              NULL,
    user_agent     nvarchar(200)  DEFAULT NULL              NULL,
    creator        nvarchar(64)   DEFAULT ''                NULL,
    create_time    datetime2      DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater        nvarchar(64)   DEFAULT ''                NULL,
    update_time    datetime2      DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted        bit            DEFAULT 0                 NOT NULL,
    tenant_id      bigint         DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'日志主键',
     'SCHEMA', N'dbo',
     'TABLE', N'system_operate_log',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'链路追踪编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_operate_log',
     'COLUMN', N'trace_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_operate_log',
     'COLUMN', N'user_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_operate_log',
     'COLUMN', N'user_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'操作模块类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_operate_log',
     'COLUMN', N'type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'操作名',
     'SCHEMA', N'dbo',
     'TABLE', N'system_operate_log',
     'COLUMN', N'sub_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'操作数据模块编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_operate_log',
     'COLUMN', N'biz_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'操作内容',
     'SCHEMA', N'dbo',
     'TABLE', N'system_operate_log',
     'COLUMN', N'action'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'拓展字段',
     'SCHEMA', N'dbo',
     'TABLE', N'system_operate_log',
     'COLUMN', N'extra'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'请求方法名',
     'SCHEMA', N'dbo',
     'TABLE', N'system_operate_log',
     'COLUMN', N'request_method'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'请求地址',
     'SCHEMA', N'dbo',
     'TABLE', N'system_operate_log',
     'COLUMN', N'request_url'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户 IP',
     'SCHEMA', N'dbo',
     'TABLE', N'system_operate_log',
     'COLUMN', N'user_ip'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'浏览器 UA',
     'SCHEMA', N'dbo',
     'TABLE', N'system_operate_log',
     'COLUMN', N'user_agent'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_operate_log',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_operate_log',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_operate_log',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_operate_log',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_operate_log',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_operate_log',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'操作日志记录 V2 版本',
     'SCHEMA', N'dbo',
     'TABLE', N'system_operate_log'
GO

-- ----------------------------
-- Table structure for system_post
-- ----------------------------
DROP TABLE IF EXISTS system_post
GO
CREATE TABLE system_post
(
    id          bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    code        nvarchar(64)                            NOT NULL,
    name        nvarchar(50)                            NOT NULL,
    sort        int                                     NOT NULL,
    status      tinyint                                 NOT NULL,
    remark      nvarchar(500) DEFAULT NULL              NULL,
    creator     nvarchar(64)  DEFAULT ''                NULL,
    create_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64)  DEFAULT ''                NULL,
    update_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit           DEFAULT 0                 NOT NULL,
    tenant_id   bigint        DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'岗位ID',
     'SCHEMA', N'dbo',
     'TABLE', N'system_post',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'岗位编码',
     'SCHEMA', N'dbo',
     'TABLE', N'system_post',
     'COLUMN', N'code'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'岗位名称',
     'SCHEMA', N'dbo',
     'TABLE', N'system_post',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'显示顺序',
     'SCHEMA', N'dbo',
     'TABLE', N'system_post',
     'COLUMN', N'sort'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'状态（0正常 1停用）',
     'SCHEMA', N'dbo',
     'TABLE', N'system_post',
     'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'备注',
     'SCHEMA', N'dbo',
     'TABLE', N'system_post',
     'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_post',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_post',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_post',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_post',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_post',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_post',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'岗位信息表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_post'
GO

-- ----------------------------
-- Records of system_post
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT system_post ON
GO
INSERT INTO system_post (id, code, name, sort, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1, N'ceo', N'董事长', 1, 0, N'', N'admin', N'2021-01-06 17:03:48', N'1', N'2023-02-11 15:19:04', N'0', 1)
GO
INSERT INTO system_post (id, code, name, sort, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2, N'se', N'项目经理', 2, 0, N'', N'admin', N'2021-01-05 17:03:48', N'1', N'2023-11-15 09:18:20', N'0', 1)
GO
INSERT INTO system_post (id, code, name, sort, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4, N'user', N'普通员工', 4, 0, N'111', N'admin', N'2021-01-05 17:03:48', N'1', N'2023-12-02 10:04:37', N'0', 1)
GO
INSERT INTO system_post (id, code, name, sort, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (5, N'HR', N'人力资源', 5, 0, N'', N'1', N'2024-03-24 20:45:40', N'1', N'2024-03-24 20:45:40', N'0', 1)
GO
SET IDENTITY_INSERT system_post OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for system_role
-- ----------------------------
DROP TABLE IF EXISTS system_role
GO
CREATE TABLE system_role
(
    id                  bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    name                nvarchar(30)                            NOT NULL,
    code                nvarchar(100)                           NOT NULL,
    sort                int                                     NOT NULL,
    data_scope          tinyint       DEFAULT 1                 NOT NULL,
    data_scope_dept_ids nvarchar(500) DEFAULT ''                NOT NULL,
    status              tinyint                                 NOT NULL,
    type                tinyint                                 NOT NULL,
    remark              nvarchar(500) DEFAULT NULL              NULL,
    creator             nvarchar(64)  DEFAULT ''                NULL,
    create_time         datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater             nvarchar(64)  DEFAULT ''                NULL,
    update_time         datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted             bit           DEFAULT 0                 NOT NULL,
    tenant_id           bigint        DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'角色ID',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'角色名称',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'角色权限字符串',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role',
     'COLUMN', N'code'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'显示顺序',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role',
     'COLUMN', N'sort'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role',
     'COLUMN', N'data_scope'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'数据范围(指定部门数组)',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role',
     'COLUMN', N'data_scope_dept_ids'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'角色状态（0正常 1停用）',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role',
     'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'角色类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role',
     'COLUMN', N'type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'备注',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role',
     'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'角色信息表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role'
GO

-- ----------------------------
-- Records of system_role
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT system_role ON
GO
INSERT INTO system_role (id, name, code, sort, data_scope, data_scope_dept_ids, status, type, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1, N'超级管理员', N'super_admin', 1, 1, N'', 0, 1, N'超级管理员', N'admin', N'2021-01-05 17:03:48', N'', N'2022-02-22 05:08:21', N'0', 1)
GO
INSERT INTO system_role (id, name, code, sort, data_scope, data_scope_dept_ids, status, type, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2, N'普通角色', N'common', 2, 2, N'', 0, 1, N'普通角色', N'admin', N'2021-01-05 17:03:48', N'', N'2022-02-22 05:08:20', N'0', 1)
GO
INSERT INTO system_role (id, name, code, sort, data_scope, data_scope_dept_ids, status, type, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3, N'CRM 管理员', N'crm_admin', 2, 1, N'', 0, 1, N'CRM 专属角色', N'1', N'2024-02-24 10:51:13', N'1', N'2024-02-24 02:51:32', N'0', 1)
GO
INSERT INTO system_role (id, name, code, sort, data_scope, data_scope_dept_ids, status, type, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (101, N'测试账号', N'test', 0, 1, N'[]', 0, 2, N'我想测试', N'', N'2021-01-06 13:49:35', N'1', N'2024-03-24 22:22:45', N'0', 1)
GO
INSERT INTO system_role (id, name, code, sort, data_scope, data_scope_dept_ids, status, type, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (109, N'租户管理员', N'tenant_admin', 0, 1, N'', 0, 1, N'系统自动生成', N'1', N'2022-02-22 00:56:14', N'1', N'2022-02-22 00:56:14', N'0', 121)
GO
INSERT INTO system_role (id, name, code, sort, data_scope, data_scope_dept_ids, status, type, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (111, N'租户管理员', N'tenant_admin', 0, 1, N'', 0, 1, N'系统自动生成', N'1', N'2022-03-07 21:37:58', N'1', N'2022-03-07 21:37:58', N'0', 122)
GO
SET IDENTITY_INSERT system_role OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for system_role_menu
-- ----------------------------
DROP TABLE IF EXISTS system_role_menu
GO
CREATE TABLE system_role_menu
(
    id          bigint                                 NOT NULL PRIMARY KEY IDENTITY,
    role_id     bigint                                 NOT NULL,
    menu_id     bigint                                 NOT NULL,
    creator     nvarchar(64) DEFAULT ''                NULL,
    create_time datetime2    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64) DEFAULT ''                NULL,
    update_time datetime2    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit          DEFAULT 0                 NOT NULL,
    tenant_id   bigint       DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'自增编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role_menu',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'角色ID',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role_menu',
     'COLUMN', N'role_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'菜单ID',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role_menu',
     'COLUMN', N'menu_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role_menu',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role_menu',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role_menu',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role_menu',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role_menu',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role_menu',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'角色和菜单关联表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_role_menu'
GO

-- ----------------------------
-- Records of system_role_menu
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT system_role_menu ON
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (263, 109, 1, N'1', N'2022-02-22 00:56:14', N'1', N'2022-02-22 00:56:14', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (434, 2, 1, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (454, 2, 1093, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (455, 2, 1094, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (460, 2, 1100, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (467, 2, 1107, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (476, 2, 1117, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (477, 2, 100, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (478, 2, 101, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (479, 2, 102, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (480, 2, 1126, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (481, 2, 103, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (483, 2, 104, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (485, 2, 105, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (488, 2, 107, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (490, 2, 108, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (492, 2, 109, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (498, 2, 1138, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (523, 2, 1224, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (524, 2, 1225, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (541, 2, 500, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (543, 2, 501, N'1', N'2022-02-22 13:09:12', N'1', N'2022-02-22 13:09:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (675, 2, 2, N'1', N'2022-02-22 13:16:57', N'1', N'2022-02-22 13:16:57', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (689, 2, 1077, N'1', N'2022-02-22 13:16:57', N'1', N'2022-02-22 13:16:57', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (690, 2, 1078, N'1', N'2022-02-22 13:16:57', N'1', N'2022-02-22 13:16:57', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (692, 2, 1083, N'1', N'2022-02-22 13:16:57', N'1', N'2022-02-22 13:16:57', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (693, 2, 1084, N'1', N'2022-02-22 13:16:57', N'1', N'2022-02-22 13:16:57', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (699, 2, 1090, N'1', N'2022-02-22 13:16:57', N'1', N'2022-02-22 13:16:57', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (703, 2, 106, N'1', N'2022-02-22 13:16:57', N'1', N'2022-02-22 13:16:57', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (704, 2, 110, N'1', N'2022-02-22 13:16:57', N'1', N'2022-02-22 13:16:57', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (705, 2, 111, N'1', N'2022-02-22 13:16:57', N'1', N'2022-02-22 13:16:57', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (706, 2, 112, N'1', N'2022-02-22 13:16:57', N'1', N'2022-02-22 13:16:57', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (707, 2, 113, N'1', N'2022-02-22 13:16:57', N'1', N'2022-02-22 13:16:57', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1296, 110, 1, N'110', N'2022-02-23 00:23:55', N'110', N'2022-02-23 00:23:55', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1578, 111, 1, N'1', N'2022-03-07 21:37:58', N'1', N'2022-03-07 21:37:58', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1604, 101, 1216, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1605, 101, 1217, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1606, 101, 1218, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1607, 101, 1219, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1608, 101, 1220, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1609, 101, 1221, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1610, 101, 5, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1611, 101, 1222, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1612, 101, 1118, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1613, 101, 1119, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1614, 101, 1120, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1615, 101, 1185, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1616, 101, 1186, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1617, 101, 1187, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1618, 101, 1188, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1619, 101, 1189, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1620, 101, 1190, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1621, 101, 1191, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1622, 101, 1192, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1623, 101, 1193, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1624, 101, 1194, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1625, 101, 1195, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1626, 101, 1196, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1627, 101, 1197, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1628, 101, 1198, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1629, 101, 1199, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1630, 101, 1200, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1631, 101, 1201, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1632, 101, 1202, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1633, 101, 1207, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1634, 101, 1208, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1635, 101, 1209, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1636, 101, 1210, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1637, 101, 1211, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1638, 101, 1212, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1639, 101, 1213, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1640, 101, 1215, N'1', N'2022-03-19 21:45:52', N'1', N'2022-03-19 21:45:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1641, 101, 2, N'1', N'2022-04-01 22:21:24', N'1', N'2022-04-01 22:21:24', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1642, 101, 1031, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1643, 101, 1032, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1644, 101, 1033, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1645, 101, 1034, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1646, 101, 1035, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1647, 101, 1050, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1648, 101, 1051, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1649, 101, 1052, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1650, 101, 1053, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1651, 101, 1054, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1652, 101, 1056, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1653, 101, 1057, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1654, 101, 1058, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1655, 101, 1059, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1656, 101, 1060, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1657, 101, 1066, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1658, 101, 1067, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1659, 101, 1070, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1664, 101, 1075, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1666, 101, 1077, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1667, 101, 1078, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1668, 101, 1082, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1669, 101, 1083, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1670, 101, 1084, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1671, 101, 1085, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1672, 101, 1086, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1673, 101, 1087, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1674, 101, 1088, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1675, 101, 1089, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1679, 101, 1237, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1680, 101, 1238, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1681, 101, 1239, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1682, 101, 1240, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1683, 101, 1241, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1684, 101, 1242, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1685, 101, 1243, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1687, 101, 106, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1688, 101, 110, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1689, 101, 111, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1690, 101, 112, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1691, 101, 113, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1692, 101, 114, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1693, 101, 115, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1694, 101, 116, N'1', N'2022-04-01 22:21:37', N'1', N'2022-04-01 22:21:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1729, 109, 100, N'1', N'2022-09-21 22:08:51', N'1', N'2022-09-21 22:08:51', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1730, 109, 101, N'1', N'2022-09-21 22:08:51', N'1', N'2022-09-21 22:08:51', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1731, 109, 1063, N'1', N'2022-09-21 22:08:51', N'1', N'2022-09-21 22:08:51', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1732, 109, 1064, N'1', N'2022-09-21 22:08:51', N'1', N'2022-09-21 22:08:51', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1733, 109, 1001, N'1', N'2022-09-21 22:08:51', N'1', N'2022-09-21 22:08:51', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1734, 109, 1065, N'1', N'2022-09-21 22:08:51', N'1', N'2022-09-21 22:08:51', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1735, 109, 1002, N'1', N'2022-09-21 22:08:51', N'1', N'2022-09-21 22:08:51', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1736, 109, 1003, N'1', N'2022-09-21 22:08:51', N'1', N'2022-09-21 22:08:51', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1737, 109, 1004, N'1', N'2022-09-21 22:08:51', N'1', N'2022-09-21 22:08:51', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1738, 109, 1005, N'1', N'2022-09-21 22:08:51', N'1', N'2022-09-21 22:08:51', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1739, 109, 1006, N'1', N'2022-09-21 22:08:51', N'1', N'2022-09-21 22:08:51', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1740, 109, 1007, N'1', N'2022-09-21 22:08:51', N'1', N'2022-09-21 22:08:51', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1741, 109, 1008, N'1', N'2022-09-21 22:08:51', N'1', N'2022-09-21 22:08:51', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1742, 109, 1009, N'1', N'2022-09-21 22:08:51', N'1', N'2022-09-21 22:08:51', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1743, 109, 1010, N'1', N'2022-09-21 22:08:51', N'1', N'2022-09-21 22:08:51', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1744, 109, 1011, N'1', N'2022-09-21 22:08:51', N'1', N'2022-09-21 22:08:51', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1745, 109, 1012, N'1', N'2022-09-21 22:08:51', N'1', N'2022-09-21 22:08:51', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1746, 111, 100, N'1', N'2022-09-21 22:08:52', N'1', N'2022-09-21 22:08:52', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1747, 111, 101, N'1', N'2022-09-21 22:08:52', N'1', N'2022-09-21 22:08:52', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1748, 111, 1063, N'1', N'2022-09-21 22:08:52', N'1', N'2022-09-21 22:08:52', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1749, 111, 1064, N'1', N'2022-09-21 22:08:52', N'1', N'2022-09-21 22:08:52', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1750, 111, 1001, N'1', N'2022-09-21 22:08:52', N'1', N'2022-09-21 22:08:52', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1751, 111, 1065, N'1', N'2022-09-21 22:08:52', N'1', N'2022-09-21 22:08:52', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1752, 111, 1002, N'1', N'2022-09-21 22:08:52', N'1', N'2022-09-21 22:08:52', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1753, 111, 1003, N'1', N'2022-09-21 22:08:52', N'1', N'2022-09-21 22:08:52', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1754, 111, 1004, N'1', N'2022-09-21 22:08:52', N'1', N'2022-09-21 22:08:52', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1755, 111, 1005, N'1', N'2022-09-21 22:08:52', N'1', N'2022-09-21 22:08:52', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1756, 111, 1006, N'1', N'2022-09-21 22:08:52', N'1', N'2022-09-21 22:08:52', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1757, 111, 1007, N'1', N'2022-09-21 22:08:52', N'1', N'2022-09-21 22:08:52', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1758, 111, 1008, N'1', N'2022-09-21 22:08:52', N'1', N'2022-09-21 22:08:52', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1759, 111, 1009, N'1', N'2022-09-21 22:08:52', N'1', N'2022-09-21 22:08:52', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1760, 111, 1010, N'1', N'2022-09-21 22:08:52', N'1', N'2022-09-21 22:08:52', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1761, 111, 1011, N'1', N'2022-09-21 22:08:52', N'1', N'2022-09-21 22:08:52', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1762, 111, 1012, N'1', N'2022-09-21 22:08:52', N'1', N'2022-09-21 22:08:52', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1763, 109, 100, N'1', N'2022-09-21 22:08:53', N'1', N'2022-09-21 22:08:53', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1764, 109, 101, N'1', N'2022-09-21 22:08:53', N'1', N'2022-09-21 22:08:53', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1765, 109, 1063, N'1', N'2022-09-21 22:08:53', N'1', N'2022-09-21 22:08:53', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1766, 109, 1064, N'1', N'2022-09-21 22:08:53', N'1', N'2022-09-21 22:08:53', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1767, 109, 1001, N'1', N'2022-09-21 22:08:53', N'1', N'2022-09-21 22:08:53', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1768, 109, 1065, N'1', N'2022-09-21 22:08:53', N'1', N'2022-09-21 22:08:53', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1769, 109, 1002, N'1', N'2022-09-21 22:08:53', N'1', N'2022-09-21 22:08:53', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1770, 109, 1003, N'1', N'2022-09-21 22:08:53', N'1', N'2022-09-21 22:08:53', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1771, 109, 1004, N'1', N'2022-09-21 22:08:53', N'1', N'2022-09-21 22:08:53', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1772, 109, 1005, N'1', N'2022-09-21 22:08:53', N'1', N'2022-09-21 22:08:53', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1773, 109, 1006, N'1', N'2022-09-21 22:08:53', N'1', N'2022-09-21 22:08:53', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1774, 109, 1007, N'1', N'2022-09-21 22:08:53', N'1', N'2022-09-21 22:08:53', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1775, 109, 1008, N'1', N'2022-09-21 22:08:53', N'1', N'2022-09-21 22:08:53', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1776, 109, 1009, N'1', N'2022-09-21 22:08:53', N'1', N'2022-09-21 22:08:53', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1777, 109, 1010, N'1', N'2022-09-21 22:08:53', N'1', N'2022-09-21 22:08:53', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1778, 109, 1011, N'1', N'2022-09-21 22:08:53', N'1', N'2022-09-21 22:08:53', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1779, 109, 1012, N'1', N'2022-09-21 22:08:53', N'1', N'2022-09-21 22:08:53', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1780, 111, 100, N'1', N'2022-09-21 22:08:54', N'1', N'2022-09-21 22:08:54', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1781, 111, 101, N'1', N'2022-09-21 22:08:54', N'1', N'2022-09-21 22:08:54', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1782, 111, 1063, N'1', N'2022-09-21 22:08:54', N'1', N'2022-09-21 22:08:54', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1783, 111, 1064, N'1', N'2022-09-21 22:08:54', N'1', N'2022-09-21 22:08:54', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1784, 111, 1001, N'1', N'2022-09-21 22:08:54', N'1', N'2022-09-21 22:08:54', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1785, 111, 1065, N'1', N'2022-09-21 22:08:54', N'1', N'2022-09-21 22:08:54', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1786, 111, 1002, N'1', N'2022-09-21 22:08:54', N'1', N'2022-09-21 22:08:54', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1787, 111, 1003, N'1', N'2022-09-21 22:08:54', N'1', N'2022-09-21 22:08:54', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1788, 111, 1004, N'1', N'2022-09-21 22:08:54', N'1', N'2022-09-21 22:08:54', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1789, 111, 1005, N'1', N'2022-09-21 22:08:54', N'1', N'2022-09-21 22:08:54', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1790, 111, 1006, N'1', N'2022-09-21 22:08:54', N'1', N'2022-09-21 22:08:54', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1791, 111, 1007, N'1', N'2022-09-21 22:08:54', N'1', N'2022-09-21 22:08:54', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1792, 111, 1008, N'1', N'2022-09-21 22:08:54', N'1', N'2022-09-21 22:08:54', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1793, 111, 1009, N'1', N'2022-09-21 22:08:54', N'1', N'2022-09-21 22:08:54', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1794, 111, 1010, N'1', N'2022-09-21 22:08:54', N'1', N'2022-09-21 22:08:54', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1795, 111, 1011, N'1', N'2022-09-21 22:08:54', N'1', N'2022-09-21 22:08:54', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1796, 111, 1012, N'1', N'2022-09-21 22:08:54', N'1', N'2022-09-21 22:08:54', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1797, 109, 100, N'1', N'2022-09-21 22:08:55', N'1', N'2022-09-21 22:08:55', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1798, 109, 101, N'1', N'2022-09-21 22:08:55', N'1', N'2022-09-21 22:08:55', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1799, 109, 1063, N'1', N'2022-09-21 22:08:55', N'1', N'2022-09-21 22:08:55', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1800, 109, 1064, N'1', N'2022-09-21 22:08:55', N'1', N'2022-09-21 22:08:55', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1801, 109, 1001, N'1', N'2022-09-21 22:08:55', N'1', N'2022-09-21 22:08:55', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1802, 109, 1065, N'1', N'2022-09-21 22:08:55', N'1', N'2022-09-21 22:08:55', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1803, 109, 1002, N'1', N'2022-09-21 22:08:55', N'1', N'2022-09-21 22:08:55', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1804, 109, 1003, N'1', N'2022-09-21 22:08:55', N'1', N'2022-09-21 22:08:55', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1805, 109, 1004, N'1', N'2022-09-21 22:08:55', N'1', N'2022-09-21 22:08:55', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1806, 109, 1005, N'1', N'2022-09-21 22:08:55', N'1', N'2022-09-21 22:08:55', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1807, 109, 1006, N'1', N'2022-09-21 22:08:55', N'1', N'2022-09-21 22:08:55', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1808, 109, 1007, N'1', N'2022-09-21 22:08:55', N'1', N'2022-09-21 22:08:55', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1809, 109, 1008, N'1', N'2022-09-21 22:08:55', N'1', N'2022-09-21 22:08:55', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1810, 109, 1009, N'1', N'2022-09-21 22:08:55', N'1', N'2022-09-21 22:08:55', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1811, 109, 1010, N'1', N'2022-09-21 22:08:55', N'1', N'2022-09-21 22:08:55', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1812, 109, 1011, N'1', N'2022-09-21 22:08:55', N'1', N'2022-09-21 22:08:55', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1813, 109, 1012, N'1', N'2022-09-21 22:08:55', N'1', N'2022-09-21 22:08:55', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1814, 111, 100, N'1', N'2022-09-21 22:08:56', N'1', N'2022-09-21 22:08:56', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1815, 111, 101, N'1', N'2022-09-21 22:08:56', N'1', N'2022-09-21 22:08:56', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1816, 111, 1063, N'1', N'2022-09-21 22:08:56', N'1', N'2022-09-21 22:08:56', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1817, 111, 1064, N'1', N'2022-09-21 22:08:56', N'1', N'2022-09-21 22:08:56', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1818, 111, 1001, N'1', N'2022-09-21 22:08:56', N'1', N'2022-09-21 22:08:56', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1819, 111, 1065, N'1', N'2022-09-21 22:08:56', N'1', N'2022-09-21 22:08:56', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1820, 111, 1002, N'1', N'2022-09-21 22:08:56', N'1', N'2022-09-21 22:08:56', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1821, 111, 1003, N'1', N'2022-09-21 22:08:56', N'1', N'2022-09-21 22:08:56', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1822, 111, 1004, N'1', N'2022-09-21 22:08:56', N'1', N'2022-09-21 22:08:56', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1823, 111, 1005, N'1', N'2022-09-21 22:08:56', N'1', N'2022-09-21 22:08:56', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1824, 111, 1006, N'1', N'2022-09-21 22:08:56', N'1', N'2022-09-21 22:08:56', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1825, 111, 1007, N'1', N'2022-09-21 22:08:56', N'1', N'2022-09-21 22:08:56', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1826, 111, 1008, N'1', N'2022-09-21 22:08:56', N'1', N'2022-09-21 22:08:56', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1827, 111, 1009, N'1', N'2022-09-21 22:08:56', N'1', N'2022-09-21 22:08:56', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1828, 111, 1010, N'1', N'2022-09-21 22:08:56', N'1', N'2022-09-21 22:08:56', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1829, 111, 1011, N'1', N'2022-09-21 22:08:56', N'1', N'2022-09-21 22:08:56', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1830, 111, 1012, N'1', N'2022-09-21 22:08:56', N'1', N'2022-09-21 22:08:56', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1831, 109, 103, N'1', N'2022-09-21 22:43:23', N'1', N'2022-09-21 22:43:23', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1832, 109, 1017, N'1', N'2022-09-21 22:43:23', N'1', N'2022-09-21 22:43:23', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1833, 109, 1018, N'1', N'2022-09-21 22:43:23', N'1', N'2022-09-21 22:43:23', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1834, 109, 1019, N'1', N'2022-09-21 22:43:23', N'1', N'2022-09-21 22:43:23', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1835, 109, 1020, N'1', N'2022-09-21 22:43:23', N'1', N'2022-09-21 22:43:23', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1836, 111, 103, N'1', N'2022-09-21 22:43:24', N'1', N'2022-09-21 22:43:24', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1837, 111, 1017, N'1', N'2022-09-21 22:43:24', N'1', N'2022-09-21 22:43:24', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1838, 111, 1018, N'1', N'2022-09-21 22:43:24', N'1', N'2022-09-21 22:43:24', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1839, 111, 1019, N'1', N'2022-09-21 22:43:24', N'1', N'2022-09-21 22:43:24', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1840, 111, 1020, N'1', N'2022-09-21 22:43:24', N'1', N'2022-09-21 22:43:24', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1841, 109, 1036, N'1', N'2022-09-21 22:48:13', N'1', N'2022-09-21 22:48:13', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1842, 109, 1037, N'1', N'2022-09-21 22:48:13', N'1', N'2022-09-21 22:48:13', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1843, 109, 1038, N'1', N'2022-09-21 22:48:13', N'1', N'2022-09-21 22:48:13', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1844, 109, 1039, N'1', N'2022-09-21 22:48:13', N'1', N'2022-09-21 22:48:13', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1845, 109, 107, N'1', N'2022-09-21 22:48:13', N'1', N'2022-09-21 22:48:13', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1846, 111, 1036, N'1', N'2022-09-21 22:48:13', N'1', N'2022-09-21 22:48:13', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1847, 111, 1037, N'1', N'2022-09-21 22:48:13', N'1', N'2022-09-21 22:48:13', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1848, 111, 1038, N'1', N'2022-09-21 22:48:13', N'1', N'2022-09-21 22:48:13', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1849, 111, 1039, N'1', N'2022-09-21 22:48:13', N'1', N'2022-09-21 22:48:13', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1850, 111, 107, N'1', N'2022-09-21 22:48:13', N'1', N'2022-09-21 22:48:13', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1991, 2, 1024, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1992, 2, 1025, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1993, 2, 1026, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1994, 2, 1027, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1995, 2, 1028, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1996, 2, 1029, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1997, 2, 1030, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1998, 2, 1031, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1999, 2, 1032, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2000, 2, 1033, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2001, 2, 1034, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2002, 2, 1035, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2003, 2, 1036, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2004, 2, 1037, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2005, 2, 1038, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2006, 2, 1039, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2007, 2, 1040, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2008, 2, 1042, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2009, 2, 1043, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2010, 2, 1045, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2011, 2, 1046, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2012, 2, 1048, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2013, 2, 1050, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2014, 2, 1051, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2015, 2, 1052, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2016, 2, 1053, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2017, 2, 1054, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2018, 2, 1056, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2019, 2, 1057, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2020, 2, 1058, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2021, 2, 2083, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2022, 2, 1059, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2023, 2, 1060, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2024, 2, 1063, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2025, 2, 1064, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2026, 2, 1065, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2027, 2, 1066, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2028, 2, 1067, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2029, 2, 1070, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2034, 2, 1075, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2036, 2, 1082, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2037, 2, 1085, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2038, 2, 1086, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2039, 2, 1087, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2040, 2, 1088, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2041, 2, 1089, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2042, 2, 1091, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2043, 2, 1092, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2044, 2, 1095, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2045, 2, 1096, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2046, 2, 1097, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2047, 2, 1098, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2048, 2, 1101, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2049, 2, 1102, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2050, 2, 1103, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2051, 2, 1104, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2052, 2, 1105, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2053, 2, 1106, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2054, 2, 1108, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2055, 2, 1109, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2061, 2, 1127, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2062, 2, 1128, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2063, 2, 1129, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2064, 2, 1130, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2066, 2, 1132, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2067, 2, 1133, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2068, 2, 1134, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2069, 2, 1135, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2070, 2, 1136, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2071, 2, 1137, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2072, 2, 114, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2073, 2, 1139, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2074, 2, 115, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2075, 2, 1140, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2076, 2, 116, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2077, 2, 1141, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2078, 2, 1142, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2079, 2, 1143, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2080, 2, 1150, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2081, 2, 1161, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2082, 2, 1162, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2083, 2, 1163, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2084, 2, 1164, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2085, 2, 1165, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2086, 2, 1166, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2087, 2, 1173, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2088, 2, 1174, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2089, 2, 1175, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2090, 2, 1176, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2091, 2, 1177, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2092, 2, 1178, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2099, 2, 1226, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2100, 2, 1227, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2101, 2, 1228, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2102, 2, 1229, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2103, 2, 1237, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2104, 2, 1238, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2105, 2, 1239, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2106, 2, 1240, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2107, 2, 1241, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2108, 2, 1242, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2109, 2, 1243, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2116, 2, 1254, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2117, 2, 1255, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2118, 2, 1256, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2119, 2, 1257, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2120, 2, 1258, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2121, 2, 1259, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2122, 2, 1260, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2123, 2, 1261, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2124, 2, 1263, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2125, 2, 1264, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2126, 2, 1265, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2127, 2, 1266, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2128, 2, 1267, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2129, 2, 1001, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2130, 2, 1002, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2131, 2, 1003, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2132, 2, 1004, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2133, 2, 1005, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2134, 2, 1006, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2135, 2, 1007, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2136, 2, 1008, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2137, 2, 1009, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2138, 2, 1010, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2139, 2, 1011, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2140, 2, 1012, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2141, 2, 1013, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2142, 2, 1014, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2143, 2, 1015, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2144, 2, 1016, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2145, 2, 1017, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2146, 2, 1018, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2147, 2, 1019, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2148, 2, 1020, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2149, 2, 1021, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2150, 2, 1022, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2151, 2, 1023, N'1', N'2023-01-25 08:42:52', N'1', N'2023-01-25 08:42:52', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2152, 2, 1281, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2153, 2, 1282, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2154, 2, 2000, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2155, 2, 2002, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2156, 2, 2003, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2157, 2, 2004, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2158, 2, 2005, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2159, 2, 2006, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2160, 2, 2008, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2161, 2, 2009, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2162, 2, 2010, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2163, 2, 2011, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2164, 2, 2012, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2170, 2, 2019, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2171, 2, 2020, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2172, 2, 2021, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2173, 2, 2022, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2174, 2, 2023, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2175, 2, 2025, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2177, 2, 2027, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2178, 2, 2028, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2179, 2, 2029, N'1', N'2023-01-25 08:42:58', N'1', N'2023-01-25 08:42:58', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2180, 2, 2014, N'1', N'2023-01-25 08:43:12', N'1', N'2023-01-25 08:43:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2181, 2, 2015, N'1', N'2023-01-25 08:43:12', N'1', N'2023-01-25 08:43:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2182, 2, 2016, N'1', N'2023-01-25 08:43:12', N'1', N'2023-01-25 08:43:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2183, 2, 2017, N'1', N'2023-01-25 08:43:12', N'1', N'2023-01-25 08:43:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2184, 2, 2018, N'1', N'2023-01-25 08:43:12', N'1', N'2023-01-25 08:43:12', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2188, 101, 1024, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2189, 101, 1, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2190, 101, 1025, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2191, 101, 1026, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2192, 101, 1027, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2193, 101, 1028, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2194, 101, 1029, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2195, 101, 1030, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2196, 101, 1036, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2197, 101, 1037, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2198, 101, 1038, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2199, 101, 1039, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2200, 101, 1040, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2201, 101, 1042, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2202, 101, 1043, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2203, 101, 1045, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2204, 101, 1046, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2205, 101, 1048, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2206, 101, 2083, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2207, 101, 1063, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2208, 101, 1064, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2209, 101, 1065, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2210, 101, 1093, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2211, 101, 1094, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2212, 101, 1095, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2213, 101, 1096, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2214, 101, 1097, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2215, 101, 1098, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2216, 101, 1100, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2217, 101, 1101, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2218, 101, 1102, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2219, 101, 1103, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2220, 101, 1104, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2221, 101, 1105, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2222, 101, 1106, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2223, 101, 2130, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2224, 101, 1107, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2225, 101, 2131, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2226, 101, 1108, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2227, 101, 2132, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2228, 101, 1109, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2229, 101, 2133, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2230, 101, 2134, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2232, 101, 2135, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2234, 101, 2136, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2236, 101, 2137, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2238, 101, 2138, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2240, 101, 2139, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2242, 101, 2140, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2243, 101, 2141, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2244, 101, 2142, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2245, 101, 2143, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2246, 101, 2144, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2247, 101, 2145, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2248, 101, 2146, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2249, 101, 2147, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2250, 101, 100, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2251, 101, 2148, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2252, 101, 101, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2253, 101, 2149, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2254, 101, 102, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2255, 101, 2150, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2256, 101, 103, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2257, 101, 2151, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2258, 101, 104, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2259, 101, 2152, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2260, 101, 105, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2261, 101, 107, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2262, 101, 108, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2263, 101, 109, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2264, 101, 1138, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2265, 101, 1139, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2266, 101, 1140, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2267, 101, 1141, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2268, 101, 1142, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2269, 101, 1143, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2270, 101, 1224, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2271, 101, 1225, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2272, 101, 1226, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2273, 101, 1227, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2274, 101, 1228, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2275, 101, 1229, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2282, 101, 1261, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2283, 101, 1263, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2284, 101, 1264, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2285, 101, 1265, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2286, 101, 1266, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2287, 101, 1267, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2288, 101, 1001, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2289, 101, 1002, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2290, 101, 1003, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2291, 101, 1004, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2292, 101, 1005, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2293, 101, 1006, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2294, 101, 1007, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2295, 101, 1008, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2296, 101, 1009, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2297, 101, 1010, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2298, 101, 1011, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2299, 101, 1012, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2300, 101, 500, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2301, 101, 1013, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2302, 101, 501, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2303, 101, 1014, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2304, 101, 1015, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2305, 101, 1016, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2306, 101, 1017, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2307, 101, 1018, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2308, 101, 1019, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2309, 101, 1020, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2310, 101, 1021, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2311, 101, 1022, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2312, 101, 1023, N'1', N'2023-02-09 23:49:46', N'1', N'2023-02-09 23:49:46', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2929, 109, 1224, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2930, 109, 1225, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2931, 109, 1226, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2932, 109, 1227, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2933, 109, 1228, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2934, 109, 1229, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2935, 109, 1138, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2936, 109, 1139, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2937, 109, 1140, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2938, 109, 1141, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2939, 109, 1142, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2940, 109, 1143, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2941, 111, 1224, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2942, 111, 1225, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2943, 111, 1226, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2944, 111, 1227, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2945, 111, 1228, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2946, 111, 1229, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2947, 111, 1138, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2948, 111, 1139, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2949, 111, 1140, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2950, 111, 1141, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2951, 111, 1142, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2952, 111, 1143, N'1', N'2023-12-02 23:19:40', N'1', N'2023-12-02 23:19:40', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2993, 109, 2, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2994, 109, 1031, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2995, 109, 1032, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2996, 109, 1033, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2997, 109, 1034, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2998, 109, 1035, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2999, 109, 1050, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3000, 109, 1051, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3001, 109, 1052, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3002, 109, 1053, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3003, 109, 1054, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3004, 109, 1056, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3005, 109, 1057, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3006, 109, 1058, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3007, 109, 1059, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3008, 109, 1060, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3009, 109, 1066, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3010, 109, 1067, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3011, 109, 1070, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3012, 109, 1075, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3013, 109, 1076, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3014, 109, 1077, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3015, 109, 1078, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3016, 109, 1082, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3017, 109, 1083, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3018, 109, 1084, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3019, 109, 1085, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3020, 109, 1086, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3021, 109, 1087, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3022, 109, 1088, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3023, 109, 1089, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3024, 109, 1090, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3025, 109, 1091, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3026, 109, 1092, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3027, 109, 106, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3028, 109, 110, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3029, 109, 111, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3030, 109, 112, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3031, 109, 113, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3032, 109, 114, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3033, 109, 115, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3034, 109, 116, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3035, 109, 2472, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3036, 109, 2478, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3037, 109, 2479, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3038, 109, 2480, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3039, 109, 2481, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3040, 109, 2482, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3041, 109, 2483, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3042, 109, 2484, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3043, 109, 2485, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3044, 109, 2486, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3045, 109, 2487, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3046, 109, 2488, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3047, 109, 2489, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3048, 109, 2490, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3049, 109, 2491, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3050, 109, 2492, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3051, 109, 2493, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3052, 109, 2494, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3053, 109, 2495, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3054, 109, 2497, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3055, 109, 1237, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3056, 109, 1238, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3057, 109, 1239, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3058, 109, 1240, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3059, 109, 1241, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3060, 109, 1242, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3061, 109, 1243, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3062, 109, 2525, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3063, 109, 1255, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3064, 109, 1256, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3065, 109, 1257, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3066, 109, 1258, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3067, 109, 1259, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3068, 109, 1260, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3069, 111, 2, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3070, 111, 1031, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3071, 111, 1032, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3072, 111, 1033, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3073, 111, 1034, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3074, 111, 1035, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3075, 111, 1050, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3076, 111, 1051, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3077, 111, 1052, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3078, 111, 1053, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3079, 111, 1054, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3080, 111, 1056, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3081, 111, 1057, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3082, 111, 1058, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3083, 111, 1059, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3084, 111, 1060, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3085, 111, 1066, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3086, 111, 1067, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3087, 111, 1070, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3088, 111, 1075, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3089, 111, 1076, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3090, 111, 1077, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3091, 111, 1078, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3092, 111, 1082, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3093, 111, 1083, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3094, 111, 1084, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3095, 111, 1085, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3096, 111, 1086, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3097, 111, 1087, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3098, 111, 1088, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3099, 111, 1089, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3100, 111, 1090, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3101, 111, 1091, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3102, 111, 1092, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3103, 111, 106, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3104, 111, 110, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3105, 111, 111, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3106, 111, 112, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3107, 111, 113, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3108, 111, 114, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3109, 111, 115, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3110, 111, 116, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3111, 111, 2472, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3112, 111, 2478, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3113, 111, 2479, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3114, 111, 2480, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3115, 111, 2481, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3116, 111, 2482, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3117, 111, 2483, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3118, 111, 2484, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3119, 111, 2485, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3120, 111, 2486, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3121, 111, 2487, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3122, 111, 2488, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3123, 111, 2489, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3124, 111, 2490, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3125, 111, 2491, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3126, 111, 2492, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3127, 111, 2493, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3128, 111, 2494, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3129, 111, 2495, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3130, 111, 2497, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3131, 111, 1237, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3132, 111, 1238, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3133, 111, 1239, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3134, 111, 1240, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3135, 111, 1241, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3136, 111, 1242, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3137, 111, 1243, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3138, 111, 2525, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3139, 111, 1255, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3140, 111, 1256, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3141, 111, 1257, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3142, 111, 1258, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3143, 111, 1259, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3144, 111, 1260, N'1', N'2023-12-02 23:41:02', N'1', N'2023-12-02 23:41:02', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3221, 109, 102, N'1', N'2023-12-30 11:42:36', N'1', N'2023-12-30 11:42:36', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3222, 109, 1013, N'1', N'2023-12-30 11:42:36', N'1', N'2023-12-30 11:42:36', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3223, 109, 1014, N'1', N'2023-12-30 11:42:36', N'1', N'2023-12-30 11:42:36', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3224, 109, 1015, N'1', N'2023-12-30 11:42:36', N'1', N'2023-12-30 11:42:36', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3225, 109, 1016, N'1', N'2023-12-30 11:42:36', N'1', N'2023-12-30 11:42:36', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3226, 111, 102, N'1', N'2023-12-30 11:42:36', N'1', N'2023-12-30 11:42:36', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3227, 111, 1013, N'1', N'2023-12-30 11:42:36', N'1', N'2023-12-30 11:42:36', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3228, 111, 1014, N'1', N'2023-12-30 11:42:36', N'1', N'2023-12-30 11:42:36', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3229, 111, 1015, N'1', N'2023-12-30 11:42:36', N'1', N'2023-12-30 11:42:36', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3230, 111, 1016, N'1', N'2023-12-30 11:42:36', N'1', N'2023-12-30 11:42:36', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4163, 109, 5, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4164, 109, 1118, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4165, 109, 1119, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4166, 109, 1120, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4167, 109, 2713, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4168, 109, 2714, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4169, 109, 2715, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4170, 109, 2716, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4171, 109, 2717, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4172, 109, 2718, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4173, 109, 2720, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4174, 109, 1185, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4175, 109, 2721, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4176, 109, 1186, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4177, 109, 2722, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4178, 109, 1187, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4179, 109, 2723, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4180, 109, 1188, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4181, 109, 2724, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4182, 109, 1189, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4183, 109, 2725, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4184, 109, 1190, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4185, 109, 2726, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4186, 109, 1191, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4187, 109, 2727, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4188, 109, 1192, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4189, 109, 2728, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4190, 109, 1193, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4191, 109, 2729, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4192, 109, 1194, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4193, 109, 2730, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4194, 109, 1195, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4195, 109, 2731, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4196, 109, 1196, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4197, 109, 2732, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4198, 109, 1197, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4199, 109, 2733, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4200, 109, 1198, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4201, 109, 2734, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4202, 109, 1199, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4203, 109, 2735, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4204, 109, 1200, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4205, 109, 1201, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4206, 109, 1202, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4207, 109, 1207, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4208, 109, 1208, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4209, 109, 1209, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4210, 109, 1210, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4211, 109, 1211, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4212, 109, 1212, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4213, 109, 1213, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4214, 109, 1215, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4215, 109, 1216, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4216, 109, 1217, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4217, 109, 1218, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4218, 109, 1219, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4219, 109, 1220, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4220, 109, 1221, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4221, 109, 1222, N'1', N'2024-03-30 17:53:17', N'1', N'2024-03-30 17:53:17', N'0', 121)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4222, 111, 5, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4223, 111, 1118, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4224, 111, 1119, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4225, 111, 1120, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4226, 111, 2713, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4227, 111, 2714, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4228, 111, 2715, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4229, 111, 2716, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4230, 111, 2717, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4231, 111, 2718, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4232, 111, 2720, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4233, 111, 1185, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4234, 111, 2721, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4235, 111, 1186, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4236, 111, 2722, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4237, 111, 1187, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4238, 111, 2723, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4239, 111, 1188, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4240, 111, 2724, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4241, 111, 1189, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4242, 111, 2725, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4243, 111, 1190, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4244, 111, 2726, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4245, 111, 1191, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4246, 111, 2727, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4247, 111, 1192, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4248, 111, 2728, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4249, 111, 1193, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4250, 111, 2729, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4251, 111, 1194, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4252, 111, 2730, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4253, 111, 1195, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4254, 111, 2731, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4255, 111, 1196, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4256, 111, 2732, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4257, 111, 1197, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4258, 111, 2733, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4259, 111, 1198, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4260, 111, 2734, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4261, 111, 1199, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4262, 111, 2735, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4263, 111, 1200, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4264, 111, 1201, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4265, 111, 1202, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4266, 111, 1207, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4267, 111, 1208, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4268, 111, 1209, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4269, 111, 1210, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4270, 111, 1211, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4271, 111, 1212, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4272, 111, 1213, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4273, 111, 1215, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4274, 111, 1216, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4275, 111, 1217, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4276, 111, 1218, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4277, 111, 1219, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4278, 111, 1220, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4279, 111, 1221, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4280, 111, 1222, N'1', N'2024-03-30 17:53:18', N'1', N'2024-03-30 17:53:18', N'0', 122)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (5777, 101, 2739, N'1', N'2024-04-30 09:38:37', N'1', N'2024-04-30 09:38:37', N'0', 1)
GO
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (5778, 101, 2740, N'1', N'2024-04-30 09:38:37', N'1', N'2024-04-30 09:38:37', N'0', 1)
GO
SET IDENTITY_INSERT system_role_menu OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for system_sms_channel
-- ----------------------------
DROP TABLE IF EXISTS system_sms_channel
GO
CREATE TABLE system_sms_channel
(
    id           bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    signature    nvarchar(12)                            NOT NULL,
    code         nvarchar(63)                            NOT NULL,
    status       tinyint                                 NOT NULL,
    remark       nvarchar(255) DEFAULT NULL              NULL,
    api_key      nvarchar(128)                           NOT NULL,
    api_secret   nvarchar(128) DEFAULT NULL              NULL,
    callback_url nvarchar(255) DEFAULT NULL              NULL,
    creator      nvarchar(64)  DEFAULT ''                NULL,
    create_time  datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater      nvarchar(64)  DEFAULT ''                NULL,
    update_time  datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted      bit           DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_channel',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'短信签名',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_channel',
     'COLUMN', N'signature'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'渠道编码',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_channel',
     'COLUMN', N'code'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'开启状态',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_channel',
     'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'备注',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_channel',
     'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'短信 API 的账号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_channel',
     'COLUMN', N'api_key'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'短信 API 的秘钥',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_channel',
     'COLUMN', N'api_secret'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'短信发送回调 URL',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_channel',
     'COLUMN', N'callback_url'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_channel',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_channel',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_channel',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_channel',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_channel',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'短信渠道',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_channel'
GO

-- ----------------------------
-- Records of system_sms_channel
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT system_sms_channel ON
GO
INSERT INTO system_sms_channel (id, signature, code, status, remark, api_key, api_secret, callback_url, creator, create_time, updater, update_time, deleted) VALUES (2, N'Ballcat', N'ALIYUN', 0, N'你要改哦，只有我可以用！！！！', N'LTAI5tCnKso2uG3kJ5gRav88', N'fGJ5SNXL7P1NHNRmJ7DJaMJGPyE55C', NULL, N'', N'2021-03-31 11:53:10', N'1', N'2023-12-02 22:10:17', N'0')
GO
INSERT INTO system_sms_channel (id, signature, code, status, remark, api_key, api_secret, callback_url, creator, create_time, updater, update_time, deleted) VALUES (4, N'测试渠道', N'DEBUG_DING_TALK', 0, N'123', N'696b5d8ead48071237e4aa5861ff08dbadb2b4ded1c688a7b7c9afc615579859', N'SEC5c4e5ff888bc8a9923ae47f59e7ccd30af1f14d93c55b4e2c9cb094e35aeed67', NULL, N'1', N'2021-04-13 00:23:14', N'1', N'2022-03-27 20:29:49', N'0')
GO
INSERT INTO system_sms_channel (id, signature, code, status, remark, api_key, api_secret, callback_url, creator, create_time, updater, update_time, deleted) VALUES (6, N'测试演示', N'DEBUG_DING_TALK', 0, N'仅测试', N'696b5d8ead48071237e4aa5861ff08dbadb2b4ded1c688a7b7c9afc615579859', N'SEC5c4e5ff888bc8a9923ae47f59e7ccd30af1f14d93c55b4e2c9cb094e35aeed67', NULL, N'1', N'2022-04-10 23:07:59', N'1', N'2023-12-02 22:10:08', N'0')
GO
SET IDENTITY_INSERT system_sms_channel OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for system_sms_code
-- ----------------------------
DROP TABLE IF EXISTS system_sms_code
GO
CREATE TABLE system_sms_code
(
    id          bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    mobile      nvarchar(11)                            NOT NULL,
    code        nvarchar(6)                             NOT NULL,
    create_ip   nvarchar(15)                            NOT NULL,
    scene       tinyint                                 NOT NULL,
    today_index tinyint                                 NOT NULL,
    used        tinyint                                 NOT NULL,
    used_time   datetime2     DEFAULT NULL              NULL,
    used_ip     nvarchar(255) DEFAULT NULL              NULL,
    creator     nvarchar(64)  DEFAULT ''                NULL,
    create_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64)  DEFAULT ''                NULL,
    update_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit           DEFAULT 0                 NOT NULL,
    tenant_id   bigint        DEFAULT 0                 NOT NULL
)
GO

CREATE INDEX idx_system_sms_code_01 ON system_sms_code (mobile)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_code',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'手机号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_code',
     'COLUMN', N'mobile'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'验证码',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_code',
     'COLUMN', N'code'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建 IP',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_code',
     'COLUMN', N'create_ip'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'发送场景',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_code',
     'COLUMN', N'scene'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'今日发送的第几条',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_code',
     'COLUMN', N'today_index'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否使用',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_code',
     'COLUMN', N'used'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'使用时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_code',
     'COLUMN', N'used_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'使用 IP',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_code',
     'COLUMN', N'used_ip'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_code',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_code',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_code',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_code',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_code',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_code',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'手机验证码',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_code'
GO

-- ----------------------------
-- Table structure for system_sms_log
-- ----------------------------
DROP TABLE IF EXISTS system_sms_log
GO
CREATE TABLE system_sms_log
(
    id               bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    channel_id       bigint                                  NOT NULL,
    channel_code     nvarchar(63)                            NOT NULL,
    template_id      bigint                                  NOT NULL,
    template_code    nvarchar(63)                            NOT NULL,
    template_type    tinyint                                 NOT NULL,
    template_content nvarchar(255)                           NOT NULL,
    template_params  nvarchar(255)                           NOT NULL,
    api_template_id  nvarchar(63)                            NOT NULL,
    mobile           nvarchar(11)                            NOT NULL,
    user_id          bigint        DEFAULT NULL              NULL,
    user_type        tinyint       DEFAULT NULL              NULL,
    send_status      tinyint       DEFAULT 0                 NOT NULL,
    send_time        datetime2     DEFAULT NULL              NULL,
    api_send_code    nvarchar(63)  DEFAULT NULL              NULL,
    api_send_msg     nvarchar(255) DEFAULT NULL              NULL,
    api_request_id   nvarchar(255) DEFAULT NULL              NULL,
    api_serial_no    nvarchar(255) DEFAULT NULL              NULL,
    receive_status   tinyint       DEFAULT 0                 NOT NULL,
    receive_time     datetime2     DEFAULT NULL              NULL,
    api_receive_code nvarchar(63)  DEFAULT NULL              NULL,
    api_receive_msg  nvarchar(255) DEFAULT NULL              NULL,
    creator          nvarchar(64)  DEFAULT ''                NULL,
    create_time      datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater          nvarchar(64)  DEFAULT ''                NULL,
    update_time      datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted          bit           DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'短信渠道编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'channel_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'短信渠道编码',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'channel_code'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模板编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'template_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模板编码',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'template_code'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'短信类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'template_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'短信内容',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'template_content'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'短信参数',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'template_params'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'短信 API 的模板编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'api_template_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'手机号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'mobile'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'user_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'user_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'发送状态',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'send_status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'发送时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'send_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'短信 API 发送结果的编码',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'api_send_code'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'短信 API 发送失败的提示',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'api_send_msg'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'短信 API 发送返回的唯一请求 ID',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'api_request_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'短信 API 发送返回的序号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'api_serial_no'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'接收状态',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'receive_status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'接收时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'receive_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'API 接收结果的编码',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'api_receive_code'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'API 接收结果的说明',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'api_receive_msg'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'短信日志',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_log'
GO

-- ----------------------------
-- Table structure for system_sms_template
-- ----------------------------
DROP TABLE IF EXISTS system_sms_template
GO
CREATE TABLE system_sms_template
(
    id              bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    type            tinyint                                 NOT NULL,
    status          tinyint                                 NOT NULL,
    code            nvarchar(63)                            NOT NULL,
    name            nvarchar(63)                            NOT NULL,
    content         nvarchar(255)                           NOT NULL,
    params          nvarchar(255)                           NOT NULL,
    remark          nvarchar(255) DEFAULT NULL              NULL,
    api_template_id nvarchar(63)                            NOT NULL,
    channel_id      bigint                                  NOT NULL,
    channel_code    nvarchar(63)                            NOT NULL,
    creator         nvarchar(64)  DEFAULT ''                NULL,
    create_time     datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater         nvarchar(64)  DEFAULT ''                NULL,
    update_time     datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted         bit           DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_template',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模板类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_template',
     'COLUMN', N'type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'开启状态',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_template',
     'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模板编码',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_template',
     'COLUMN', N'code'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模板名称',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_template',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'模板内容',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_template',
     'COLUMN', N'content'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'参数数组',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_template',
     'COLUMN', N'params'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'备注',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_template',
     'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'短信 API 的模板编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_template',
     'COLUMN', N'api_template_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'短信渠道编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_template',
     'COLUMN', N'channel_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'短信渠道编码',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_template',
     'COLUMN', N'channel_code'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_template',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_template',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_template',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_template',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_template',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'短信模板',
     'SCHEMA', N'dbo',
     'TABLE', N'system_sms_template'
GO

-- ----------------------------
-- Records of system_sms_template
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT system_sms_template ON
GO
INSERT INTO system_sms_template (id, type, status, code, name, content, params, remark, api_template_id, channel_id, channel_code, creator, create_time, updater, update_time, deleted) VALUES (2, 1, 0, N'test_01', N'测试验证码短信', N'正在进行登录操作{operation}，您的验证码是{code}', N'["operation","code"]', N'测试备注', N'4383920', 6, N'DEBUG_DING_TALK', N'', N'2021-03-31 10:49:38', N'1', N'2023-12-02 22:32:47', N'0')
GO
INSERT INTO system_sms_template (id, type, status, code, name, content, params, remark, api_template_id, channel_id, channel_code, creator, create_time, updater, update_time, deleted) VALUES (3, 1, 0, N'test_02', N'公告通知', N'您的验证码{code}，该验证码5分钟内有效，请勿泄漏于他人！', N'["code"]', NULL, N'SMS_207945135', 2, N'ALIYUN', N'', N'2021-03-31 11:56:30', N'1', N'2021-04-10 01:22:02', N'0')
GO
INSERT INTO system_sms_template (id, type, status, code, name, content, params, remark, api_template_id, channel_id, channel_code, creator, create_time, updater, update_time, deleted) VALUES (6, 3, 0, N'test-01', N'测试模板', N'哈哈哈 {name}', N'["name"]', N'f哈哈哈', N'4383920', 6, N'DEBUG_DING_TALK', N'1', N'2021-04-10 01:07:21', N'1', N'2022-12-10 21:26:09', N'0')
GO
INSERT INTO system_sms_template (id, type, status, code, name, content, params, remark, api_template_id, channel_id, channel_code, creator, create_time, updater, update_time, deleted) VALUES (7, 3, 0, N'test-04', N'测试下', N'老鸡{name}，牛逼{code}', N'["name","code"]', N'哈哈哈哈', N'suibian', 4, N'DEBUG_DING_TALK', N'1', N'2021-04-13 00:29:53', N'1', N'2023-12-02 22:35:34', N'0')
GO
INSERT INTO system_sms_template (id, type, status, code, name, content, params, remark, api_template_id, channel_id, channel_code, creator, create_time, updater, update_time, deleted) VALUES (8, 1, 0, N'user-sms-login', N'前台用户短信登录', N'您的验证码是{code}', N'["code"]', NULL, N'4372216', 6, N'DEBUG_DING_TALK', N'1', N'2021-10-11 08:10:00', N'1', N'2022-12-10 21:25:59', N'0')
GO
INSERT INTO system_sms_template (id, type, status, code, name, content, params, remark, api_template_id, channel_id, channel_code, creator, create_time, updater, update_time, deleted) VALUES (9, 2, 0, N'bpm_task_assigned', N'【工作流】任务被分配', N'您收到了一条新的待办任务：{processInstanceName}-{taskName}，申请人：{startUserNickname}，处理链接：{detailUrl}', N'["processInstanceName","taskName","startUserNickname","detailUrl"]', NULL, N'suibian', 4, N'DEBUG_DING_TALK', N'1', N'2022-01-21 22:31:19', N'1', N'2022-01-22 00:03:36', N'0')
GO
INSERT INTO system_sms_template (id, type, status, code, name, content, params, remark, api_template_id, channel_id, channel_code, creator, create_time, updater, update_time, deleted) VALUES (10, 2, 0, N'bpm_process_instance_reject', N'【工作流】流程被不通过', N'您的流程被审批不通过：{processInstanceName}，原因：{reason}，查看链接：{detailUrl}', N'["processInstanceName","reason","detailUrl"]', NULL, N'suibian', 4, N'DEBUG_DING_TALK', N'1', N'2022-01-22 00:03:31', N'1', N'2022-05-01 12:33:14', N'0')
GO
INSERT INTO system_sms_template (id, type, status, code, name, content, params, remark, api_template_id, channel_id, channel_code, creator, create_time, updater, update_time, deleted) VALUES (11, 2, 0, N'bpm_process_instance_approve', N'【工作流】流程被通过', N'您的流程被审批通过：{processInstanceName}，查看链接：{detailUrl}', N'["processInstanceName","detailUrl"]', NULL, N'suibian', 4, N'DEBUG_DING_TALK', N'1', N'2022-01-22 00:04:31', N'1', N'2022-03-27 20:32:21', N'0')
GO
INSERT INTO system_sms_template (id, type, status, code, name, content, params, remark, api_template_id, channel_id, channel_code, creator, create_time, updater, update_time, deleted) VALUES (12, 2, 0, N'demo', N'演示模板', N'我就是测试一下下', N'[]', NULL, N'biubiubiu', 6, N'DEBUG_DING_TALK', N'1', N'2022-04-10 23:22:49', N'1', N'2023-03-24 23:45:07', N'0')
GO
INSERT INTO system_sms_template (id, type, status, code, name, content, params, remark, api_template_id, channel_id, channel_code, creator, create_time, updater, update_time, deleted) VALUES (14, 1, 0, N'user-update-mobile', N'会员用户 - 修改手机', N'您的验证码{code}，该验证码 5 分钟内有效，请勿泄漏于他人！', N'["code"]', N'', N'null', 4, N'DEBUG_DING_TALK', N'1', N'2023-08-19 18:58:01', N'1', N'2023-08-19 11:34:04', N'0')
GO
INSERT INTO system_sms_template (id, type, status, code, name, content, params, remark, api_template_id, channel_id, channel_code, creator, create_time, updater, update_time, deleted) VALUES (15, 1, 0, N'user-update-password', N'会员用户 - 修改密码', N'您的验证码{code}，该验证码 5 分钟内有效，请勿泄漏于他人！', N'["code"]', N'', N'null', 4, N'DEBUG_DING_TALK', N'1', N'2023-08-19 18:58:01', N'1', N'2023-08-19 11:34:18', N'0')
GO
INSERT INTO system_sms_template (id, type, status, code, name, content, params, remark, api_template_id, channel_id, channel_code, creator, create_time, updater, update_time, deleted) VALUES (16, 1, 0, N'user-reset-password', N'会员用户 - 重置密码', N'您的验证码{code}，该验证码 5 分钟内有效，请勿泄漏于他人！', N'["code"]', N'', N'null', 4, N'DEBUG_DING_TALK', N'1', N'2023-08-19 18:58:01', N'1', N'2023-12-02 22:35:27', N'0')
GO
SET IDENTITY_INSERT system_sms_template OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for system_social_client
-- ----------------------------
DROP TABLE IF EXISTS system_social_client
GO
CREATE TABLE system_social_client
(
    id            bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    name          nvarchar(255)                           NOT NULL,
    social_type   tinyint                                 NOT NULL,
    user_type     tinyint                                 NOT NULL,
    client_id     nvarchar(255)                           NOT NULL,
    client_secret nvarchar(255)                           NOT NULL,
    agent_id      nvarchar(255) DEFAULT NULL              NULL,
    status        tinyint                                 NOT NULL,
    creator       nvarchar(64)  DEFAULT ''                NULL,
    create_time   datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater       nvarchar(64)  DEFAULT ''                NULL,
    update_time   datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted       bit           DEFAULT 0                 NOT NULL,
    tenant_id     bigint        DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_client',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'应用名',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_client',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'社交平台的类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_client',
     'COLUMN', N'social_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_client',
     'COLUMN', N'user_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'客户端编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_client',
     'COLUMN', N'client_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'客户端密钥',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_client',
     'COLUMN', N'client_secret'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'代理编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_client',
     'COLUMN', N'agent_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'状态',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_client',
     'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_client',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_client',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_client',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_client',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_client',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_client',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'社交客户端表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_client'
GO

-- ----------------------------
-- Records of system_social_client
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT system_social_client ON
GO
INSERT INTO system_social_client (id, name, social_type, user_type, client_id, client_secret, agent_id, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1, N'钉钉', 20, 2, N'dingvrnreaje3yqvzhxg', N'i8E6iZyDvZj51JIb0tYsYfVQYOks9Cq1lgryEjFRqC79P3iJcrxEwT6Qk2QvLrLI', NULL, 0, N'', N'2023-10-18 11:21:18', N'1', N'2023-12-20 21:28:26', N'1', 1)
GO
INSERT INTO system_social_client (id, name, social_type, user_type, client_id, client_secret, agent_id, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2, N'钉钉（王土豆）', 20, 2, N'dingtsu9hpepjkbmthhw', N'FP_bnSq_HAHKCSncmJjw5hxhnzs6vaVDSZZn3egj6rdqTQ_hu5tQVJyLMpgCakdP', NULL, 0, N'', N'2023-10-18 11:21:18', N'', N'2023-12-20 21:28:26', N'1', 121)
GO
INSERT INTO system_social_client (id, name, social_type, user_type, client_id, client_secret, agent_id, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3, N'微信公众号', 31, 1, N'wx5b23ba7a5589ecbb', N'2a7b3b20c537e52e74afd395eb85f61f', NULL, 0, N'', N'2023-10-18 16:07:46', N'1', N'2023-12-20 21:28:23', N'1', 1)
GO
INSERT INTO system_social_client (id, name, social_type, user_type, client_id, client_secret, agent_id, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (43, N'微信小程序', 34, 1, N'wx63c280fe3248a3e7', N'6f270509224a7ae1296bbf1c8cb97aed', NULL, 0, N'', N'2023-10-19 13:37:41', N'1', N'2023-12-20 21:28:25', N'1', 1)
GO
SET IDENTITY_INSERT system_social_client OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for system_social_user
-- ----------------------------
DROP TABLE IF EXISTS system_social_user
GO
CREATE TABLE system_social_user
(
    id             bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    type           tinyint                                 NOT NULL,
    openid         nvarchar(32)                            NOT NULL,
    token          nvarchar(256) DEFAULT NULL              NULL,
    raw_token_info nvarchar(1024)                          NOT NULL,
    nickname       nvarchar(32)                            NOT NULL,
    avatar         nvarchar(255) DEFAULT NULL              NULL,
    raw_user_info  nvarchar(1024)                          NOT NULL,
    code           nvarchar(256)                           NOT NULL,
    state          nvarchar(256) DEFAULT NULL              NULL,
    creator        nvarchar(64)  DEFAULT ''                NULL,
    create_time    datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater        nvarchar(64)  DEFAULT ''                NULL,
    update_time    datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted        bit           DEFAULT 0                 NOT NULL,
    tenant_id      bigint        DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'主键(自增策略)',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'社交平台的类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user',
     'COLUMN', N'type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'社交 openid',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user',
     'COLUMN', N'openid'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'社交 token',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user',
     'COLUMN', N'token'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'原始 Token 数据，一般是 JSON 格式',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user',
     'COLUMN', N'raw_token_info'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户昵称',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user',
     'COLUMN', N'nickname'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户头像',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user',
     'COLUMN', N'avatar'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'原始用户数据，一般是 JSON 格式',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user',
     'COLUMN', N'raw_user_info'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'最后一次的认证 code',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user',
     'COLUMN', N'code'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'最后一次的认证 state',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user',
     'COLUMN', N'state'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'社交用户表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user'
GO

-- ----------------------------
-- Table structure for system_social_user_bind
-- ----------------------------
DROP TABLE IF EXISTS system_social_user_bind
GO
CREATE TABLE system_social_user_bind
(
    id             bigint                                 NOT NULL PRIMARY KEY IDENTITY,
    user_id        bigint                                 NOT NULL,
    user_type      tinyint                                NOT NULL,
    social_type    tinyint                                NOT NULL,
    social_user_id bigint                                 NOT NULL,
    creator        nvarchar(64) DEFAULT ''                NULL,
    create_time    datetime2    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater        nvarchar(64) DEFAULT ''                NULL,
    update_time    datetime2    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted        bit          DEFAULT 0                 NOT NULL,
    tenant_id      bigint       DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'主键(自增策略)',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user_bind',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user_bind',
     'COLUMN', N'user_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user_bind',
     'COLUMN', N'user_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'社交平台的类型',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user_bind',
     'COLUMN', N'social_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'社交用户的编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user_bind',
     'COLUMN', N'social_user_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user_bind',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user_bind',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user_bind',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user_bind',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user_bind',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user_bind',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'社交绑定表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_social_user_bind'
GO

-- ----------------------------
-- Table structure for system_tenant
-- ----------------------------
DROP TABLE IF EXISTS system_tenant
GO
CREATE TABLE system_tenant
(
    id              bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    name            nvarchar(30)                            NOT NULL,
    contact_user_id bigint        DEFAULT NULL              NULL,
    contact_name    nvarchar(30)                            NOT NULL,
    contact_mobile  nvarchar(500) DEFAULT NULL              NULL,
    status          tinyint       DEFAULT 0                 NOT NULL,
    website         nvarchar(256) DEFAULT ''                NULL,
    package_id      bigint                                  NOT NULL,
    expire_time     datetime2                               NOT NULL,
    account_count   int                                     NOT NULL,
    creator         nvarchar(64)  DEFAULT ''                NOT NULL,
    create_time     datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater         nvarchar(64)  DEFAULT ''                NULL,
    update_time     datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted         bit           DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户名',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'联系人的用户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant',
     'COLUMN', N'contact_user_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'联系人',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant',
     'COLUMN', N'contact_name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'联系手机',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant',
     'COLUMN', N'contact_mobile'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户状态（0正常 1停用）',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant',
     'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'绑定域名',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant',
     'COLUMN', N'website'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户套餐编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant',
     'COLUMN', N'package_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'过期时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant',
     'COLUMN', N'expire_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'账号数量',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant',
     'COLUMN', N'account_count'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant'
GO

-- ----------------------------
-- Records of system_tenant
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT system_tenant ON
GO
INSERT INTO system_tenant (id, name, contact_user_id, contact_name, contact_mobile, status, website, package_id, expire_time, account_count, creator, create_time, updater, update_time, deleted) VALUES (1, N'芋道源码', NULL, N'芋艿', N'17321315478', 0, N'www.iocoder.cn', 0, N'2099-02-19 17:14:16', 9999, N'1', N'2021-01-05 17:03:47', N'1', N'2023-11-06 11:41:41', N'0')
GO
INSERT INTO system_tenant (id, name, contact_user_id, contact_name, contact_mobile, status, website, package_id, expire_time, account_count, creator, create_time, updater, update_time, deleted) VALUES (121, N'小租户', 110, N'小王2', N'15601691300', 0, N'zsxq.iocoder.cn', 111, N'2024-03-11 00:00:00', 20, N'1', N'2022-02-22 00:56:14', N'1', N'2023-11-06 11:41:47', N'0')
GO
INSERT INTO system_tenant (id, name, contact_user_id, contact_name, contact_mobile, status, website, package_id, expire_time, account_count, creator, create_time, updater, update_time, deleted) VALUES (122, N'测试租户', 113, N'芋道', N'15601691300', 0, N'test.iocoder.cn', 111, N'2022-04-30 00:00:00', 50, N'1', N'2022-03-07 21:37:58', N'1', N'2023-11-06 11:41:53', N'0')
GO
SET IDENTITY_INSERT system_tenant OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for system_tenant_package
-- ----------------------------
DROP TABLE IF EXISTS system_tenant_package
GO
CREATE TABLE system_tenant_package
(
    id          bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    name        nvarchar(30)                            NOT NULL,
    status      tinyint       DEFAULT 0                 NOT NULL,
    remark      nvarchar(256) DEFAULT ''                NULL,
    menu_ids    nvarchar(4000)                          NOT NULL,
    creator     nvarchar(64)  DEFAULT ''                NOT NULL,
    create_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64)  DEFAULT ''                NULL,
    update_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit           DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'套餐编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant_package',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'套餐名',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant_package',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户状态（0正常 1停用）',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant_package',
     'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'备注',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant_package',
     'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'关联的菜单编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant_package',
     'COLUMN', N'menu_ids'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant_package',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant_package',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant_package',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant_package',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant_package',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户套餐表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_tenant_package'
GO

-- ----------------------------
-- Records of system_tenant_package
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT system_tenant_package ON
GO
INSERT INTO system_tenant_package (id, name, status, remark, menu_ids, creator, create_time, updater, update_time, deleted) VALUES (111, N'普通套餐', 0, N'小功能', N'[1,2,5,1031,1032,1033,1034,1035,1036,1037,1038,1039,1050,1051,1052,1053,1054,1056,1057,1058,1059,1060,1063,1064,1065,1066,1067,1070,1075,1076,1077,1078,1082,1083,1084,1085,1086,1087,1088,1089,1090,1091,1092,1118,1119,1120,100,101,102,103,106,107,110,111,112,113,1138,114,1139,115,1140,116,1141,1142,1143,2713,2714,2715,2716,2717,2718,2720,1185,2721,1186,2722,1187,2723,1188,2724,1189,2725,1190,2726,1191,2727,2472,1192,2728,1193,2729,1194,2730,1195,2731,1196,2732,1197,2733,2478,1198,2734,2479,1199,2735,2480,1200,2481,1201,2482,1202,2483,2484,2485,2486,2487,1207,2488,1208,2489,1209,2490,1210,2491,1211,2492,1212,2493,1213,2494,2495,1215,1216,2497,1217,1218,1219,1220,1221,1222,1224,1225,1226,1227,1228,1229,1237,1238,1239,1240,1241,1242,1243,2525,1255,1256,1001,1257,1002,1258,1003,1259,1004,1260,1005,1006,1007,1008,1009,1010,1011,1012,1013,1014,1015,1016,1017,1018,1019,1020]', N'1', N'2022-02-22 00:54:00', N'1', N'2024-03-30 17:53:17', N'0')
GO
SET IDENTITY_INSERT system_tenant_package OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for system_user_post
-- ----------------------------
DROP TABLE IF EXISTS system_user_post
GO
CREATE TABLE system_user_post
(
    id          bigint                                 NOT NULL PRIMARY KEY IDENTITY,
    user_id     bigint       DEFAULT 0                 NOT NULL,
    post_id     bigint       DEFAULT 0                 NOT NULL,
    creator     nvarchar(64) DEFAULT ''                NULL,
    create_time datetime2    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64) DEFAULT ''                NULL,
    update_time datetime2    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit          DEFAULT 0                 NOT NULL,
    tenant_id   bigint       DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'id',
     'SCHEMA', N'dbo',
     'TABLE', N'system_user_post',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户ID',
     'SCHEMA', N'dbo',
     'TABLE', N'system_user_post',
     'COLUMN', N'user_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'岗位ID',
     'SCHEMA', N'dbo',
     'TABLE', N'system_user_post',
     'COLUMN', N'post_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_user_post',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_user_post',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_user_post',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_user_post',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_user_post',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_user_post',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户岗位表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_user_post'
GO

-- ----------------------------
-- Records of system_user_post
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT system_user_post ON
GO
INSERT INTO system_user_post (id, user_id, post_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (112, 1, 1, N'admin', N'2022-05-02 07:25:24', N'admin', N'2022-05-02 07:25:24', N'0', 1)
GO
INSERT INTO system_user_post (id, user_id, post_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (113, 100, 1, N'admin', N'2022-05-02 07:25:24', N'admin', N'2022-05-02 07:25:24', N'0', 1)
GO
INSERT INTO system_user_post (id, user_id, post_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (115, 104, 1, N'1', N'2022-05-16 19:36:28', N'1', N'2022-05-16 19:36:28', N'0', 1)
GO
INSERT INTO system_user_post (id, user_id, post_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (116, 117, 2, N'1', N'2022-07-09 17:40:26', N'1', N'2022-07-09 17:40:26', N'0', 1)
GO
INSERT INTO system_user_post (id, user_id, post_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (117, 118, 1, N'1', N'2022-07-09 17:44:44', N'1', N'2022-07-09 17:44:44', N'0', 1)
GO
INSERT INTO system_user_post (id, user_id, post_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (119, 114, 5, N'1', N'2024-03-24 20:45:51', N'1', N'2024-03-24 20:45:51', N'0', 1)
GO
INSERT INTO system_user_post (id, user_id, post_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (123, 115, 1, N'1', N'2024-04-04 09:37:14', N'1', N'2024-04-04 09:37:14', N'0', 1)
GO
INSERT INTO system_user_post (id, user_id, post_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (124, 115, 2, N'1', N'2024-04-04 09:37:14', N'1', N'2024-04-04 09:37:14', N'0', 1)
GO
SET IDENTITY_INSERT system_user_post OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for system_user_role
-- ----------------------------
DROP TABLE IF EXISTS system_user_role
GO
CREATE TABLE system_user_role
(
    id          bigint                                 NOT NULL PRIMARY KEY IDENTITY,
    user_id     bigint                                 NOT NULL,
    role_id     bigint                                 NOT NULL,
    creator     nvarchar(64) DEFAULT ''                NULL,
    create_time datetime2    DEFAULT CURRENT_TIMESTAMP NULL,
    updater     nvarchar(64) DEFAULT ''                NULL,
    update_time datetime2    DEFAULT CURRENT_TIMESTAMP NULL,
    deleted     bit          DEFAULT 0                 NOT NULL,
    tenant_id   bigint       DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'自增编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_user_role',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户ID',
     'SCHEMA', N'dbo',
     'TABLE', N'system_user_role',
     'COLUMN', N'user_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'角色ID',
     'SCHEMA', N'dbo',
     'TABLE', N'system_user_role',
     'COLUMN', N'role_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_user_role',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_user_role',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_user_role',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_user_role',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_user_role',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_user_role',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户和角色关联表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_user_role'
GO

-- ----------------------------
-- Records of system_user_role
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT system_user_role ON
GO
INSERT INTO system_user_role (id, user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1, 1, 1, N'', N'2022-01-11 13:19:45', N'', N'2022-05-12 12:35:17', N'0', 1)
GO
INSERT INTO system_user_role (id, user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2, 2, 2, N'', N'2022-01-11 13:19:45', N'', N'2022-05-12 12:35:13', N'0', 1)
GO
INSERT INTO system_user_role (id, user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4, 100, 101, N'', N'2022-01-11 13:19:45', N'', N'2022-05-12 12:35:13', N'0', 1)
GO
INSERT INTO system_user_role (id, user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (5, 100, 1, N'', N'2022-01-11 13:19:45', N'', N'2022-05-12 12:35:12', N'0', 1)
GO
INSERT INTO system_user_role (id, user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (6, 100, 2, N'', N'2022-01-11 13:19:45', N'', N'2022-05-12 12:35:11', N'0', 1)
GO
INSERT INTO system_user_role (id, user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (10, 103, 1, N'1', N'2022-01-11 13:19:45', N'1', N'2022-01-11 13:19:45', N'0', 1)
GO
INSERT INTO system_user_role (id, user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (14, 110, 109, N'1', N'2022-02-22 00:56:14', N'1', N'2022-02-22 00:56:14', N'0', 121)
GO
INSERT INTO system_user_role (id, user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (15, 111, 110, N'110', N'2022-02-23 13:14:38', N'110', N'2022-02-23 13:14:38', N'0', 121)
GO
INSERT INTO system_user_role (id, user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (16, 113, 111, N'1', N'2022-03-07 21:37:58', N'1', N'2022-03-07 21:37:58', N'0', 122)
GO
INSERT INTO system_user_role (id, user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (18, 1, 2, N'1', N'2022-05-12 20:39:29', N'1', N'2022-05-12 20:39:29', N'0', 1)
GO
INSERT INTO system_user_role (id, user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (20, 104, 101, N'1', N'2022-05-28 15:43:57', N'1', N'2022-05-28 15:43:57', N'0', 1)
GO
INSERT INTO system_user_role (id, user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (22, 115, 2, N'1', N'2022-07-21 22:08:30', N'1', N'2022-07-21 22:08:30', N'0', 1)
GO
INSERT INTO system_user_role (id, user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (35, 112, 1, N'1', N'2024-03-15 20:00:24', N'1', N'2024-03-15 20:00:24', N'0', 1)
GO
INSERT INTO system_user_role (id, user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (36, 118, 1, N'1', N'2024-03-17 09:12:08', N'1', N'2024-03-17 09:12:08', N'0', 1)
GO
INSERT INTO system_user_role (id, user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (38, 114, 101, N'1', N'2024-03-24 22:23:03', N'1', N'2024-03-24 22:23:03', N'0', 1)
GO
SET IDENTITY_INSERT system_user_role OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for system_users
-- ----------------------------
DROP TABLE IF EXISTS system_users
GO
CREATE TABLE system_users
(
    id          bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    username    nvarchar(30)                            NOT NULL,
    password    nvarchar(100) DEFAULT ''                NOT NULL,
    nickname    nvarchar(30)                            NOT NULL,
    remark      nvarchar(500) DEFAULT NULL              NULL,
    dept_id     bigint        DEFAULT NULL              NULL,
    post_ids    nvarchar(255) DEFAULT NULL              NULL,
    email       nvarchar(50)  DEFAULT ''                NULL,
    mobile      nvarchar(11)  DEFAULT ''                NULL,
    sex         tinyint       DEFAULT 0                 NULL,
    avatar      nvarchar(512) DEFAULT ''                NULL,
    status      tinyint       DEFAULT 0                 NOT NULL,
    login_ip    nvarchar(50)  DEFAULT ''                NULL,
    login_date  datetime2     DEFAULT NULL              NULL,
    creator     nvarchar(64)  DEFAULT ''                NULL,
    create_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64)  DEFAULT ''                NULL,
    update_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit           DEFAULT 0                 NOT NULL,
    tenant_id   bigint        DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户ID',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户账号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users',
     'COLUMN', N'username'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'密码',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users',
     'COLUMN', N'password'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户昵称',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users',
     'COLUMN', N'nickname'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'备注',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users',
     'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'部门ID',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users',
     'COLUMN', N'dept_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'岗位编号数组',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users',
     'COLUMN', N'post_ids'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户邮箱',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users',
     'COLUMN', N'email'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'手机号码',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users',
     'COLUMN', N'mobile'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户性别',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users',
     'COLUMN', N'sex'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'头像地址',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users',
     'COLUMN', N'avatar'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'帐号状态（0正常 1停用）',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users',
     'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'最后登录IP',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users',
     'COLUMN', N'login_ip'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'最后登录时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users',
     'COLUMN', N'login_date'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'用户信息表',
     'SCHEMA', N'dbo',
     'TABLE', N'system_users'
GO

-- ----------------------------
-- Records of system_users
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT system_users ON
GO
INSERT INTO system_users (id, username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1, N'admin', N'$2a$10$mRMIYLDtRHlf6.9ipiqH1.Z.bh/R9dO9d5iHiGYPigi6r5KOoR2Wm', N'芋道源码', N'管理员', 103, N'[1]', N'aoteman@126.com', N'18818260277', 2, N'http://test.yudao.iocoder.cn/96c787a2ce88bf6d0ce3cd8b6cf5314e80e7703cd41bf4af8cd2e2909dbd6b6d.png', 0, N'0:0:0:0:0:0:0:1', N'2024-04-29 21:50:32', N'admin', N'2021-01-05 17:03:47', NULL, N'2024-04-29 21:50:32', N'0', 1)
GO
INSERT INTO system_users (id, username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (100, N'yudao', N'$2a$10$11U48RhyJ5pSBYWSn12AD./ld671.ycSzJHbyrtpeoMeYiw31eo8a', N'芋道', N'不要吓我', 104, N'[1]', N'yudao@iocoder.cn', N'15601691300', 1, N'', 1, N'127.0.0.1', N'2022-07-09 23:03:33', N'', N'2021-01-07 09:07:17', NULL, N'2022-07-09 23:03:33', N'0', 1)
GO
INSERT INTO system_users (id, username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (103, N'yuanma', N'$2a$10$YMpimV4T6BtDhIaA8jSW.u8UTGBeGhc/qwXP4oxoMr4mOw9.qttt6', N'源码', NULL, 106, NULL, N'yuanma@iocoder.cn', N'15601701300', 0, N'', 0, N'0:0:0:0:0:0:0:1', N'2024-03-18 21:09:04', N'', N'2021-01-13 23:50:35', NULL, N'2024-03-18 21:09:04', N'0', 1)
GO
INSERT INTO system_users (id, username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (104, N'test', N'$2a$04$KhExCYl7lx6eWWZYKsibKOZ8IBJRyuNuCcEOLQ11RYhJKgHmlSwK.', N'测试号', NULL, 107, N'[1,2]', N'111@qq.com', N'15601691200', 1, N'', 0, N'0:0:0:0:0:0:0:1', N'2024-03-26 07:11:35', N'', N'2021-01-21 02:13:53', NULL, N'2024-03-26 07:11:35', N'0', 1)
GO
INSERT INTO system_users (id, username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (107, N'admin107', N'$2a$10$dYOOBKMO93v/.ReCqzyFg.o67Tqk.bbc2bhrpyBGkIw9aypCtr2pm', N'芋艿', NULL, NULL, NULL, N'', N'15601691300', 0, N'', 0, N'', NULL, N'1', N'2022-02-20 22:59:33', N'1', N'2022-02-27 08:26:51', N'0', 118)
GO
INSERT INTO system_users (id, username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (108, N'admin108', N'$2a$10$y6mfvKoNYL1GXWak8nYwVOH.kCWqjactkzdoIDgiKl93WN3Ejg.Lu', N'芋艿', NULL, NULL, NULL, N'', N'15601691300', 0, N'', 0, N'', NULL, N'1', N'2022-02-20 23:00:50', N'1', N'2022-02-27 08:26:53', N'0', 119)
GO
INSERT INTO system_users (id, username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (109, N'admin109', N'$2a$10$JAqvH0tEc0I7dfDVBI7zyuB4E3j.uH6daIjV53.vUS6PknFkDJkuK', N'芋艿', NULL, NULL, NULL, N'', N'15601691300', 0, N'', 0, N'', NULL, N'1', N'2022-02-20 23:11:50', N'1', N'2022-02-27 08:26:56', N'0', 120)
GO
INSERT INTO system_users (id, username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (110, N'admin110', N'$2a$10$mRMIYLDtRHlf6.9ipiqH1.Z.bh/R9dO9d5iHiGYPigi6r5KOoR2Wm', N'小王', NULL, NULL, NULL, N'', N'15601691300', 0, N'', 0, N'127.0.0.1', N'2022-09-25 22:47:33', N'1', N'2022-02-22 00:56:14', NULL, N'2022-09-25 22:47:33', N'0', 121)
GO
INSERT INTO system_users (id, username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (111, N'test', N'$2a$10$mRMIYLDtRHlf6.9ipiqH1.Z.bh/R9dO9d5iHiGYPigi6r5KOoR2Wm', N'测试用户', NULL, NULL, N'[]', N'', N'', 0, N'', 0, N'0:0:0:0:0:0:0:1', N'2023-12-30 11:42:17', N'110', N'2022-02-23 13:14:33', NULL, N'2023-12-30 11:42:17', N'0', 121)
GO
INSERT INTO system_users (id, username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (112, N'newobject', N'$2a$04$dB0z8Q819fJWz0hbaLe6B.VfHCjYgWx6LFfET5lyz3JwcqlyCkQ4C', N'新对象', NULL, 100, N'[]', N'', N'15601691235', 1, N'', 0, N'0:0:0:0:0:0:0:1', N'2024-03-16 23:11:38', N'1', N'2022-02-23 19:08:03', NULL, N'2024-03-16 23:11:38', N'0', 1)
GO
INSERT INTO system_users (id, username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (113, N'aoteman', N'$2a$10$0acJOIk2D25/oC87nyclE..0lzeu9DtQ/n3geP4fkun/zIVRhHJIO', N'芋道', NULL, NULL, NULL, N'', N'15601691300', 0, N'', 0, N'127.0.0.1', N'2022-03-19 18:38:51', N'1', N'2022-03-07 21:37:58', NULL, N'2022-03-19 18:38:51', N'0', 122)
GO
INSERT INTO system_users (id, username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (114, N'hrmgr', N'$2a$10$TR4eybBioGRhBmDBWkqWLO6NIh3mzYa8KBKDDB5woiGYFVlRAi.fu', N'hr 小姐姐', NULL, NULL, N'[5]', N'', N'15601691236', 1, N'', 0, N'0:0:0:0:0:0:0:1', N'2024-03-24 22:21:05', N'1', N'2022-03-19 21:50:58', NULL, N'2024-03-24 22:21:05', N'0', 1)
GO
INSERT INTO system_users (id, username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (115, N'aotemane', N'$2a$04$GcyP0Vyzb2F2Yni5PuIK9ueGxM0tkZGMtDwVRwrNbtMvorzbpNsV2', N'阿呆', N'11222', 102, N'[1,2]', N'7648@qq.com', N'15601691229', 2, N'', 0, N'', NULL, N'1', N'2022-04-30 02:55:43', N'1', N'2024-04-04 09:37:14', N'0', 1)
GO
INSERT INTO system_users (id, username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (117, N'admin123', N'$2a$10$WI8Gg/lpZQIrOEZMHqka7OdFaD4Nx.B/qY8ZGTTUKrOJwaHFqibaC', N'测试号', N'1111', 100, N'[2]', N'', N'15601691234', 1, N'', 0, N'', NULL, N'1', N'2022-07-09 17:40:26', N'1', N'2022-07-09 17:40:26', N'0', 1)
GO
INSERT INTO system_users (id, username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (118, N'goudan', N'$2a$04$OB1SuphCdiLVRpiYRKeqH.8NYS7UIp5vmIv1W7U4w6toiFeOAATVK', N'狗蛋', NULL, 103, N'[1]', N'', N'15601691239', 1, N'', 0, N'0:0:0:0:0:0:0:1', N'2024-03-17 09:10:27', N'1', N'2022-07-09 17:44:43', N'1', N'2024-04-04 09:48:05', N'0', 1)
GO
INSERT INTO system_users (id, username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (131, N'hh', N'$2a$04$jyH9h6.gaw8mpOjPfHIpx.8as2Rzfcmdlj5rlJFwgCw4rsv/MTb2K', N'呵呵', NULL, 100, N'[]', N'777@qq.com', N'15601882312', 1, N'', 0, N'', NULL, N'1', N'2024-04-27 08:45:56', N'1', N'2024-04-27 08:45:56', N'0', 1)
GO
SET IDENTITY_INSERT system_users OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for yudao_demo01_contact
-- ----------------------------
DROP TABLE IF EXISTS yudao_demo01_contact
GO
CREATE TABLE yudao_demo01_contact
(
    id          bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    name        nvarchar(100) DEFAULT ''                NOT NULL,
    sex         tinyint                                 NOT NULL,
    birthday    datetime2                               NOT NULL,
    description nvarchar(255)                           NOT NULL,
    avatar      nvarchar(512) DEFAULT NULL              NULL,
    creator     nvarchar(64)  DEFAULT ''                NULL,
    create_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64)  DEFAULT ''                NULL,
    update_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit           DEFAULT 0                 NOT NULL,
    tenant_id   bigint        DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo01_contact',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'名字',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo01_contact',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'性别',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo01_contact',
     'COLUMN', N'sex'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'出生年',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo01_contact',
     'COLUMN', N'birthday'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'简介',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo01_contact',
     'COLUMN', N'description'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'头像',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo01_contact',
     'COLUMN', N'avatar'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo01_contact',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo01_contact',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo01_contact',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo01_contact',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo01_contact',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo01_contact',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'示例联系人表',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo01_contact'
GO

-- ----------------------------
-- Records of yudao_demo01_contact
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT yudao_demo01_contact ON
GO
INSERT INTO yudao_demo01_contact (id, name, sex, birthday, description, avatar, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1, N'土豆', 2, N'2023-11-07 00:00:00', N'<p>天蚕土豆！呀</p>', N'http://127.0.0.1:48080/admin-api/infra/file/4/get/46f8fa1a37db3f3960d8910ff2fe3962ab3b2db87cf2f8ccb4dc8145b8bdf237.jpeg', N'1', N'2023-11-15 23:34:30', N'1', N'2023-11-15 23:47:39', N'0', 1)
GO
SET IDENTITY_INSERT yudao_demo01_contact OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for yudao_demo02_category
-- ----------------------------
DROP TABLE IF EXISTS yudao_demo02_category
GO
CREATE TABLE yudao_demo02_category
(
    id          bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    name        nvarchar(100) DEFAULT ''                NOT NULL,
    parent_id   bigint                                  NOT NULL,
    creator     nvarchar(64)  DEFAULT ''                NULL,
    create_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64)  DEFAULT ''                NULL,
    update_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit           DEFAULT 0                 NOT NULL,
    tenant_id   bigint        DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo02_category',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'名字',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo02_category',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'父级编号',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo02_category',
     'COLUMN', N'parent_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo02_category',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo02_category',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo02_category',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo02_category',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo02_category',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo02_category',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'示例分类表',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo02_category'
GO

-- ----------------------------
-- Records of yudao_demo02_category
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT yudao_demo02_category ON
GO
INSERT INTO yudao_demo02_category (id, name, parent_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1, N'土豆', 0, N'1', N'2023-11-15 23:34:30', N'1', N'2023-11-16 20:24:23', N'0', 1)
GO
INSERT INTO yudao_demo02_category (id, name, parent_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2, N'番茄', 0, N'1', N'2023-11-16 20:24:00', N'1', N'2023-11-16 20:24:15', N'0', 1)
GO
INSERT INTO yudao_demo02_category (id, name, parent_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3, N'怪怪', 0, N'1', N'2023-11-16 20:24:32', N'1', N'2023-11-16 20:24:32', N'0', 1)
GO
INSERT INTO yudao_demo02_category (id, name, parent_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4, N'小番茄', 2, N'1', N'2023-11-16 20:24:39', N'1', N'2023-11-16 20:24:39', N'0', 1)
GO
INSERT INTO yudao_demo02_category (id, name, parent_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (5, N'大番茄', 2, N'1', N'2023-11-16 20:24:46', N'1', N'2023-11-16 20:24:46', N'0', 1)
GO
INSERT INTO yudao_demo02_category (id, name, parent_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (6, N'11', 3, N'1', N'2023-11-24 19:29:34', N'1', N'2023-11-24 19:29:34', N'0', 1)
GO
SET IDENTITY_INSERT yudao_demo02_category OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for yudao_demo03_course
-- ----------------------------
DROP TABLE IF EXISTS yudao_demo03_course
GO
CREATE TABLE yudao_demo03_course
(
    id          bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    student_id  bigint                                  NOT NULL,
    name        nvarchar(100) DEFAULT ''                NOT NULL,
    score       tinyint                                 NOT NULL,
    creator     nvarchar(64)  DEFAULT ''                NULL,
    create_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64)  DEFAULT ''                NULL,
    update_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit           DEFAULT 0                 NOT NULL,
    tenant_id   bigint        DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_course',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'学生编号',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_course',
     'COLUMN', N'student_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'名字',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_course',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'分数',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_course',
     'COLUMN', N'score'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_course',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_course',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_course',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_course',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_course',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_course',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'学生课程表',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_course'
GO

-- ----------------------------
-- Records of yudao_demo03_course
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT yudao_demo03_course ON
GO
INSERT INTO yudao_demo03_course (id, student_id, name, score, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2, 2, N'语文', 66, N'1', N'2023-11-16 23:21:49', N'1', N'2023-11-16 23:21:49', N'0', 1)
GO
INSERT INTO yudao_demo03_course (id, student_id, name, score, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3, 2, N'数学', 22, N'1', N'2023-11-16 23:21:49', N'1', N'2023-11-16 23:21:49', N'0', 1)
GO
INSERT INTO yudao_demo03_course (id, student_id, name, score, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (6, 5, N'体育', 23, N'1', N'2023-11-16 23:22:46', N'1', N'2023-11-16 15:44:40', N'1', 1)
GO
INSERT INTO yudao_demo03_course (id, student_id, name, score, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (7, 5, N'计算机', 11, N'1', N'2023-11-16 23:22:46', N'1', N'2023-11-16 15:44:40', N'1', 1)
GO
INSERT INTO yudao_demo03_course (id, student_id, name, score, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (8, 5, N'体育', 23, N'1', N'2023-11-16 23:22:46', N'1', N'2023-11-16 15:47:09', N'1', 1)
GO
INSERT INTO yudao_demo03_course (id, student_id, name, score, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (9, 5, N'计算机', 11, N'1', N'2023-11-16 23:22:46', N'1', N'2023-11-16 15:47:09', N'1', 1)
GO
INSERT INTO yudao_demo03_course (id, student_id, name, score, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (10, 5, N'体育', 23, N'1', N'2023-11-16 23:22:46', N'1', N'2023-11-16 23:47:10', N'0', 1)
GO
INSERT INTO yudao_demo03_course (id, student_id, name, score, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (11, 5, N'计算机', 11, N'1', N'2023-11-16 23:22:46', N'1', N'2023-11-16 23:47:10', N'0', 1)
GO
INSERT INTO yudao_demo03_course (id, student_id, name, score, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (12, 2, N'电脑', 33, N'1', N'2023-11-17 00:20:42', N'1', N'2023-11-16 16:20:45', N'1', 1)
GO
INSERT INTO yudao_demo03_course (id, student_id, name, score, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (13, 9, N'滑雪', 12, N'1', N'2023-11-17 13:13:20', N'1', N'2023-11-17 13:13:20', N'0', 1)
GO
SET IDENTITY_INSERT yudao_demo03_course OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for yudao_demo03_grade
-- ----------------------------
DROP TABLE IF EXISTS yudao_demo03_grade
GO
CREATE TABLE yudao_demo03_grade
(
    id          bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    student_id  bigint                                  NOT NULL,
    name        nvarchar(100) DEFAULT ''                NOT NULL,
    teacher     nvarchar(255)                           NOT NULL,
    creator     nvarchar(64)  DEFAULT ''                NULL,
    create_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64)  DEFAULT ''                NULL,
    update_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit           DEFAULT 0                 NOT NULL,
    tenant_id   bigint        DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_grade',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'学生编号',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_grade',
     'COLUMN', N'student_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'名字',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_grade',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'班主任',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_grade',
     'COLUMN', N'teacher'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_grade',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_grade',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_grade',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_grade',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_grade',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_grade',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'学生班级表',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_grade'
GO

-- ----------------------------
-- Records of yudao_demo03_grade
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT yudao_demo03_grade ON
GO
INSERT INTO yudao_demo03_grade (id, student_id, name, teacher, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (7, 2, N'三年 2 班', N'周杰伦', N'1', N'2023-11-16 23:21:49', N'1', N'2023-11-16 23:21:49', N'0', 1)
GO
INSERT INTO yudao_demo03_grade (id, student_id, name, teacher, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (8, 5, N'华为', N'遥遥领先', N'1', N'2023-11-16 23:22:46', N'1', N'2023-11-16 23:47:10', N'0', 1)
GO
INSERT INTO yudao_demo03_grade (id, student_id, name, teacher, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (9, 9, N'小图', N'小娃111', N'1', N'2023-11-17 13:10:23', N'1', N'2023-11-17 13:10:23', N'0', 1)
GO
SET IDENTITY_INSERT yudao_demo03_grade OFF
GO
COMMIT
GO
-- @formatter:on

-- ----------------------------
-- Table structure for yudao_demo03_student
-- ----------------------------
DROP TABLE IF EXISTS yudao_demo03_student
GO
CREATE TABLE yudao_demo03_student
(
    id          bigint                                  NOT NULL PRIMARY KEY IDENTITY,
    name        nvarchar(100) DEFAULT ''                NOT NULL,
    sex         tinyint                                 NOT NULL,
    birthday    datetime2                               NOT NULL,
    description nvarchar(255)                           NOT NULL,
    creator     nvarchar(64)  DEFAULT ''                NULL,
    create_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updater     nvarchar(64)  DEFAULT ''                NULL,
    update_time datetime2     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted     bit           DEFAULT 0                 NOT NULL,
    tenant_id   bigint        DEFAULT 0                 NOT NULL
)
GO

EXEC sp_addextendedproperty
     'MS_Description', N'编号',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_student',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'名字',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_student',
     'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'性别',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_student',
     'COLUMN', N'sex'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'出生日期',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_student',
     'COLUMN', N'birthday'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'简介',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_student',
     'COLUMN', N'description'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建者',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_student',
     'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_student',
     'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新者',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_student',
     'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'更新时间',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_student',
     'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'是否删除',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_student',
     'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'租户编号',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_student',
     'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'学生表',
     'SCHEMA', N'dbo',
     'TABLE', N'yudao_demo03_student'
GO

-- ----------------------------
-- Records of yudao_demo03_student
-- ----------------------------
-- @formatter:off
BEGIN TRANSACTION
GO
SET IDENTITY_INSERT yudao_demo03_student ON
GO
INSERT INTO yudao_demo03_student (id, name, sex, birthday, description, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2, N'小白', 1, N'2023-11-16 00:00:00', N'<p>厉害</p>', N'1', N'2023-11-16 23:21:49', N'1', N'2023-11-17 16:49:06', N'0', 1)
GO
INSERT INTO yudao_demo03_student (id, name, sex, birthday, description, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (5, N'大黑', 2, N'2023-11-13 00:00:00', N'<p>你在教我做事?</p>', N'1', N'2023-11-16 23:22:46', N'1', N'2023-11-17 16:49:07', N'0', 1)
GO
INSERT INTO yudao_demo03_student (id, name, sex, birthday, description, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (9, N'小花', 1, N'2023-11-07 00:00:00', N'<p>哈哈哈</p>', N'1', N'2023-11-17 00:04:47', N'1', N'2023-11-17 16:49:08', N'0', 1)
GO
SET IDENTITY_INSERT yudao_demo03_student OFF
GO
COMMIT
GO
-- @formatter:on

