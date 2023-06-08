/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1 SQLServer
 Source Server Type    : SQL Server
 Source Server Version : 15004198
 Source Host           : 127.0.0.1:1433
 Source Catalog        : ruoyi-vue-pro
 Source Schema         : dbo

 Target Server Type    : SQL Server
 Target Server Version : 15004198
 File Encoding         : 65001

 Date: 15/06/2022 08:15:45
*/


-- ----------------------------
-- Table structure for QRTZ_BLOB_TRIGGERS
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_BLOB_TRIGGERS]') AND type IN ('U'))
	DROP TABLE [dbo].[QRTZ_BLOB_TRIGGERS]
GO

CREATE TABLE [dbo].[QRTZ_BLOB_TRIGGERS] (
  [SCHED_NAME] varchar(120) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [TRIGGER_NAME] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [TRIGGER_GROUP] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [BLOB_DATA] varbinary(max)  NULL
)
GO

ALTER TABLE [dbo].[QRTZ_BLOB_TRIGGERS] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Records of QRTZ_BLOB_TRIGGERS
-- ----------------------------
BEGIN TRANSACTION
GO

COMMIT
GO


-- ----------------------------
-- Table structure for QRTZ_CALENDARS
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_CALENDARS]') AND type IN ('U'))
	DROP TABLE [dbo].[QRTZ_CALENDARS]
GO

CREATE TABLE [dbo].[QRTZ_CALENDARS] (
  [SCHED_NAME] varchar(120) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [CALENDAR_NAME] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [CALENDAR] varbinary(max)  NOT NULL
)
GO

ALTER TABLE [dbo].[QRTZ_CALENDARS] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Records of QRTZ_CALENDARS
-- ----------------------------
BEGIN TRANSACTION
GO

COMMIT
GO


-- ----------------------------
-- Table structure for QRTZ_CRON_TRIGGERS
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_CRON_TRIGGERS]') AND type IN ('U'))
	DROP TABLE [dbo].[QRTZ_CRON_TRIGGERS]
GO

CREATE TABLE [dbo].[QRTZ_CRON_TRIGGERS] (
  [SCHED_NAME] varchar(120) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [TRIGGER_NAME] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [TRIGGER_GROUP] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [CRON_EXPRESSION] varchar(120) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [TIME_ZONE_ID] varchar(80) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[QRTZ_CRON_TRIGGERS] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Records of QRTZ_CRON_TRIGGERS
-- ----------------------------
BEGIN TRANSACTION
GO

INSERT INTO [dbo].[QRTZ_CRON_TRIGGERS] ([SCHED_NAME], [TRIGGER_NAME], [TRIGGER_GROUP], [CRON_EXPRESSION], [TIME_ZONE_ID]) VALUES (N'schedulerName', N'userSessionTimeoutJob', N'DEFAULT', N'0 * * * * ? *', N'Asia/Shanghai')
GO

COMMIT
GO


-- ----------------------------
-- Table structure for QRTZ_FIRED_TRIGGERS
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_FIRED_TRIGGERS]') AND type IN ('U'))
	DROP TABLE [dbo].[QRTZ_FIRED_TRIGGERS]
GO

CREATE TABLE [dbo].[QRTZ_FIRED_TRIGGERS] (
  [SCHED_NAME] varchar(120) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [ENTRY_ID] varchar(95) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [TRIGGER_NAME] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [TRIGGER_GROUP] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [INSTANCE_NAME] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [FIRED_TIME] bigint  NOT NULL,
  [SCHED_TIME] bigint  NOT NULL,
  [PRIORITY] int  NOT NULL,
  [STATE] varchar(16) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [JOB_NAME] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [JOB_GROUP] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [IS_NONCONCURRENT] varchar(1) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [REQUESTS_RECOVERY] varchar(1) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[QRTZ_FIRED_TRIGGERS] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Records of QRTZ_FIRED_TRIGGERS
-- ----------------------------
BEGIN TRANSACTION
GO

COMMIT
GO


-- ----------------------------
-- Table structure for QRTZ_JOB_DETAILS
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_JOB_DETAILS]') AND type IN ('U'))
	DROP TABLE [dbo].[QRTZ_JOB_DETAILS]
GO

CREATE TABLE [dbo].[QRTZ_JOB_DETAILS] (
  [SCHED_NAME] varchar(120) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [JOB_NAME] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [JOB_GROUP] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [DESCRIPTION] varchar(250) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [JOB_CLASS_NAME] varchar(250) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [IS_DURABLE] varchar(1) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [IS_NONCONCURRENT] varchar(1) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [IS_UPDATE_DATA] varchar(1) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [REQUESTS_RECOVERY] varchar(1) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [JOB_DATA] varbinary(max)  NULL
)
GO

ALTER TABLE [dbo].[QRTZ_JOB_DETAILS] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Records of QRTZ_JOB_DETAILS
-- ----------------------------
BEGIN TRANSACTION
GO

INSERT INTO [dbo].[QRTZ_JOB_DETAILS] ([SCHED_NAME], [JOB_NAME], [JOB_GROUP], [DESCRIPTION], [JOB_CLASS_NAME], [IS_DURABLE], [IS_NONCONCURRENT], [IS_UPDATE_DATA], [REQUESTS_RECOVERY], [JOB_DATA]) VALUES (N'schedulerName', N'userSessionTimeoutJob', N'DEFAULT', NULL, N'cn.iocoder.yudao.framework.quartz.core.handler.JobHandlerInvoker', N'0', N'1', N'1', N'0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C770800000010000000027400064A4F425F49447372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B020000787000000000000000107400104A4F425F48414E444C45525F4E414D457400157573657253657373696F6E54696D656F75744A6F627800)
GO

COMMIT
GO


-- ----------------------------
-- Table structure for QRTZ_LOCKS
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_LOCKS]') AND type IN ('U'))
	DROP TABLE [dbo].[QRTZ_LOCKS]
GO

CREATE TABLE [dbo].[QRTZ_LOCKS] (
  [SCHED_NAME] varchar(120) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [LOCK_NAME] varchar(40) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL
)
GO

ALTER TABLE [dbo].[QRTZ_LOCKS] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Records of QRTZ_LOCKS
-- ----------------------------
BEGIN TRANSACTION
GO

INSERT INTO [dbo].[QRTZ_LOCKS] ([SCHED_NAME], [LOCK_NAME]) VALUES (N'schedulerName', N'STATE_ACCESS')
GO

INSERT INTO [dbo].[QRTZ_LOCKS] ([SCHED_NAME], [LOCK_NAME]) VALUES (N'schedulerName', N'TRIGGER_ACCESS')
GO

COMMIT
GO


-- ----------------------------
-- Table structure for QRTZ_PAUSED_TRIGGER_GRPS
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_PAUSED_TRIGGER_GRPS]') AND type IN ('U'))
	DROP TABLE [dbo].[QRTZ_PAUSED_TRIGGER_GRPS]
GO

CREATE TABLE [dbo].[QRTZ_PAUSED_TRIGGER_GRPS] (
  [SCHED_NAME] varchar(120) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [TRIGGER_GROUP] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL
)
GO

ALTER TABLE [dbo].[QRTZ_PAUSED_TRIGGER_GRPS] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Records of QRTZ_PAUSED_TRIGGER_GRPS
-- ----------------------------
BEGIN TRANSACTION
GO

COMMIT
GO


-- ----------------------------
-- Table structure for QRTZ_SCHEDULER_STATE
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_SCHEDULER_STATE]') AND type IN ('U'))
	DROP TABLE [dbo].[QRTZ_SCHEDULER_STATE]
GO

CREATE TABLE [dbo].[QRTZ_SCHEDULER_STATE] (
  [SCHED_NAME] varchar(120) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [INSTANCE_NAME] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [LAST_CHECKIN_TIME] bigint  NOT NULL,
  [CHECKIN_INTERVAL] bigint  NOT NULL
)
GO

ALTER TABLE [dbo].[QRTZ_SCHEDULER_STATE] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Records of QRTZ_SCHEDULER_STATE
-- ----------------------------
BEGIN TRANSACTION
GO

INSERT INTO [dbo].[QRTZ_SCHEDULER_STATE] ([SCHED_NAME], [INSTANCE_NAME], [LAST_CHECKIN_TIME], [CHECKIN_INTERVAL]) VALUES (N'schedulerName', N'Yunai1651483828928', N'1651484588813', N'15000')
GO

COMMIT
GO


-- ----------------------------
-- Table structure for QRTZ_SIMPLE_TRIGGERS
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_SIMPLE_TRIGGERS]') AND type IN ('U'))
	DROP TABLE [dbo].[QRTZ_SIMPLE_TRIGGERS]
GO

CREATE TABLE [dbo].[QRTZ_SIMPLE_TRIGGERS] (
  [SCHED_NAME] varchar(120) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [TRIGGER_NAME] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [TRIGGER_GROUP] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [REPEAT_COUNT] bigint  NOT NULL,
  [REPEAT_INTERVAL] bigint  NOT NULL,
  [TIMES_TRIGGERED] bigint  NOT NULL
)
GO

ALTER TABLE [dbo].[QRTZ_SIMPLE_TRIGGERS] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Records of QRTZ_SIMPLE_TRIGGERS
-- ----------------------------
BEGIN TRANSACTION
GO

COMMIT
GO


-- ----------------------------
-- Table structure for QRTZ_SIMPROP_TRIGGERS
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_SIMPROP_TRIGGERS]') AND type IN ('U'))
	DROP TABLE [dbo].[QRTZ_SIMPROP_TRIGGERS]
GO

CREATE TABLE [dbo].[QRTZ_SIMPROP_TRIGGERS] (
  [SCHED_NAME] varchar(120) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [TRIGGER_NAME] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [TRIGGER_GROUP] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [STR_PROP_1] varchar(512) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [STR_PROP_2] varchar(512) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [STR_PROP_3] varchar(512) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [INT_PROP_1] int  NULL,
  [INT_PROP_2] int  NULL,
  [LONG_PROP_1] bigint  NULL,
  [LONG_PROP_2] bigint  NULL,
  [DEC_PROP_1] numeric(13,4)  NULL,
  [DEC_PROP_2] numeric(13,4)  NULL,
  [BOOL_PROP_1] varchar(1) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [BOOL_PROP_2] varchar(1) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[QRTZ_SIMPROP_TRIGGERS] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Records of QRTZ_SIMPROP_TRIGGERS
-- ----------------------------
BEGIN TRANSACTION
GO

COMMIT
GO


-- ----------------------------
-- Table structure for QRTZ_TRIGGERS
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[QRTZ_TRIGGERS]') AND type IN ('U'))
	DROP TABLE [dbo].[QRTZ_TRIGGERS]
GO

CREATE TABLE [dbo].[QRTZ_TRIGGERS] (
  [SCHED_NAME] varchar(120) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [TRIGGER_NAME] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [TRIGGER_GROUP] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [JOB_NAME] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [JOB_GROUP] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [DESCRIPTION] varchar(250) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [NEXT_FIRE_TIME] bigint  NULL,
  [PREV_FIRE_TIME] bigint  NULL,
  [PRIORITY] int  NULL,
  [TRIGGER_STATE] varchar(16) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [TRIGGER_TYPE] varchar(8) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [START_TIME] bigint  NOT NULL,
  [END_TIME] bigint  NULL,
  [CALENDAR_NAME] varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [MISFIRE_INSTR] smallint  NULL,
  [JOB_DATA] varbinary(max)  NULL
)
GO

ALTER TABLE [dbo].[QRTZ_TRIGGERS] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Records of QRTZ_TRIGGERS
-- ----------------------------
BEGIN TRANSACTION
GO

INSERT INTO [dbo].[QRTZ_TRIGGERS] ([SCHED_NAME], [TRIGGER_NAME], [TRIGGER_GROUP], [JOB_NAME], [JOB_GROUP], [DESCRIPTION], [NEXT_FIRE_TIME], [PREV_FIRE_TIME], [PRIORITY], [TRIGGER_STATE], [TRIGGER_TYPE], [START_TIME], [END_TIME], [CALENDAR_NAME], [MISFIRE_INSTR], [JOB_DATA]) VALUES (N'schedulerName', N'userSessionTimeoutJob', N'DEFAULT', N'userSessionTimeoutJob', N'DEFAULT', NULL, N'1651484640000', N'1651484580000', N'5', N'WAITING', N'CRON', N'1651483728000', N'0', NULL, N'0', 0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C770800000010000000037400114A4F425F48414E444C45525F504152414D707400124A4F425F52455452595F494E54455256414C737200116A6176612E6C616E672E496E746567657212E2A0A4F781873802000149000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B02000078700000000374000F4A4F425F52455452595F434F554E547371007E0009000007D07800)
GO

COMMIT
GO


-- ----------------------------
-- Table structure for bpm_form
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[bpm_form]') AND type IN ('U'))
	DROP TABLE [dbo].[bpm_form]
GO

CREATE TABLE [dbo].[bpm_form] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [name] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [status] tinyint  NOT NULL,
  [conf] nvarchar(1000) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [fields] nvarchar(max) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [remark] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL,
  [tenant_id] bigint DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[bpm_form] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_form',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'表单名',
'SCHEMA', N'dbo',
'TABLE', N'bpm_form',
'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'开启状态',
'SCHEMA', N'dbo',
'TABLE', N'bpm_form',
'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'表单的配置',
'SCHEMA', N'dbo',
'TABLE', N'bpm_form',
'COLUMN', N'conf'
GO

EXEC sp_addextendedproperty
'MS_Description', N'表单项的数组',
'SCHEMA', N'dbo',
'TABLE', N'bpm_form',
'COLUMN', N'fields'
GO

EXEC sp_addextendedproperty
'MS_Description', N'备注',
'SCHEMA', N'dbo',
'TABLE', N'bpm_form',
'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建者',
'SCHEMA', N'dbo',
'TABLE', N'bpm_form',
'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建时间',
'SCHEMA', N'dbo',
'TABLE', N'bpm_form',
'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新者',
'SCHEMA', N'dbo',
'TABLE', N'bpm_form',
'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新时间',
'SCHEMA', N'dbo',
'TABLE', N'bpm_form',
'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'bpm_form',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_form',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'工作流的表单定义',
'SCHEMA', N'dbo',
'TABLE', N'bpm_form'
GO


-- ----------------------------
-- Records of bpm_form
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[bpm_form] ON
GO

SET IDENTITY_INSERT [dbo].[bpm_form] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for bpm_oa_leave
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[bpm_oa_leave]') AND type IN ('U'))
	DROP TABLE [dbo].[bpm_oa_leave]
GO

CREATE TABLE [dbo].[bpm_oa_leave] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [user_id] bigint  NOT NULL,
  [type] tinyint  NOT NULL,
  [reason] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [start_time] datetime2(7)  NOT NULL,
  [end_time] datetime2(7)  NOT NULL,
  [day] tinyint  NOT NULL,
  [result] tinyint  NOT NULL,
  [process_instance_id] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint DEFAULT 0 NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[bpm_oa_leave] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'请假表单主键',
'SCHEMA', N'dbo',
'TABLE', N'bpm_oa_leave',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'申请人的用户编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_oa_leave',
'COLUMN', N'user_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'请假类型',
'SCHEMA', N'dbo',
'TABLE', N'bpm_oa_leave',
'COLUMN', N'type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'请假原因',
'SCHEMA', N'dbo',
'TABLE', N'bpm_oa_leave',
'COLUMN', N'reason'
GO

EXEC sp_addextendedproperty
'MS_Description', N'开始时间',
'SCHEMA', N'dbo',
'TABLE', N'bpm_oa_leave',
'COLUMN', N'start_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'结束时间',
'SCHEMA', N'dbo',
'TABLE', N'bpm_oa_leave',
'COLUMN', N'end_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'请假天数',
'SCHEMA', N'dbo',
'TABLE', N'bpm_oa_leave',
'COLUMN', N'day'
GO

EXEC sp_addextendedproperty
'MS_Description', N'请假结果',
'SCHEMA', N'dbo',
'TABLE', N'bpm_oa_leave',
'COLUMN', N'result'
GO

EXEC sp_addextendedproperty
'MS_Description', N'流程实例的编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_oa_leave',
'COLUMN', N'process_instance_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建者',
'SCHEMA', N'dbo',
'TABLE', N'bpm_oa_leave',
'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建时间',
'SCHEMA', N'dbo',
'TABLE', N'bpm_oa_leave',
'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新者',
'SCHEMA', N'dbo',
'TABLE', N'bpm_oa_leave',
'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新时间',
'SCHEMA', N'dbo',
'TABLE', N'bpm_oa_leave',
'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_oa_leave',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'bpm_oa_leave',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'OA 请假申请表',
'SCHEMA', N'dbo',
'TABLE', N'bpm_oa_leave'
GO


-- ----------------------------
-- Records of bpm_oa_leave
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[bpm_oa_leave] ON
GO

SET IDENTITY_INSERT [dbo].[bpm_oa_leave] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for bpm_process_definition_ext
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[bpm_process_definition_ext]') AND type IN ('U'))
	DROP TABLE [dbo].[bpm_process_definition_ext]
GO

CREATE TABLE [dbo].[bpm_process_definition_ext] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [process_definition_id] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [model_id] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [description] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [form_type] tinyint  NOT NULL,
  [form_id] bigint  NULL,
  [form_conf] nvarchar(1000) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [form_fields] nvarchar(max) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [form_custom_create_path] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [form_custom_view_path] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint DEFAULT 0 NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[bpm_process_definition_ext] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_definition_ext',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'流程定义的编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_definition_ext',
'COLUMN', N'process_definition_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'流程模型的编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_definition_ext',
'COLUMN', N'model_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'描述',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_definition_ext',
'COLUMN', N'description'
GO

EXEC sp_addextendedproperty
'MS_Description', N'表单类型',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_definition_ext',
'COLUMN', N'form_type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'表单编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_definition_ext',
'COLUMN', N'form_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'表单的配置',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_definition_ext',
'COLUMN', N'form_conf'
GO

EXEC sp_addextendedproperty
'MS_Description', N'表单项的数组',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_definition_ext',
'COLUMN', N'form_fields'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义表单的提交路径',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_definition_ext',
'COLUMN', N'form_custom_create_path'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自定义表单的查看路径',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_definition_ext',
'COLUMN', N'form_custom_view_path'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建者',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_definition_ext',
'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建时间',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_definition_ext',
'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新者',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_definition_ext',
'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新时间',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_definition_ext',
'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_definition_ext',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_definition_ext',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'Bpm 流程定义的拓展表
',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_definition_ext'
GO


-- ----------------------------
-- Records of bpm_process_definition_ext
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[bpm_process_definition_ext] ON
GO

SET IDENTITY_INSERT [dbo].[bpm_process_definition_ext] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for bpm_process_instance_ext
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[bpm_process_instance_ext]') AND type IN ('U'))
	DROP TABLE [dbo].[bpm_process_instance_ext]
GO

CREATE TABLE [dbo].[bpm_process_instance_ext] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [start_user_id] bigint  NOT NULL,
  [name] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [process_instance_id] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [process_definition_id] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [category] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [status] tinyint  NOT NULL,
  [result] tinyint  NOT NULL,
  [end_time] datetime2(7)  NULL,
  [form_variables] nvarchar(max) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[bpm_process_instance_ext] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_instance_ext',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'发起流程的用户编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_instance_ext',
'COLUMN', N'start_user_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'流程实例的名字',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_instance_ext',
'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'流程实例的编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_instance_ext',
'COLUMN', N'process_instance_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'流程定义的编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_instance_ext',
'COLUMN', N'process_definition_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'流程分类',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_instance_ext',
'COLUMN', N'category'
GO

EXEC sp_addextendedproperty
'MS_Description', N'流程实例的状态',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_instance_ext',
'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'流程实例的结果',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_instance_ext',
'COLUMN', N'result'
GO

EXEC sp_addextendedproperty
'MS_Description', N'结束时间',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_instance_ext',
'COLUMN', N'end_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'表单值',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_instance_ext',
'COLUMN', N'form_variables'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建者',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_instance_ext',
'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建时间',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_instance_ext',
'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新者',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_instance_ext',
'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新时间',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_instance_ext',
'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_instance_ext',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_instance_ext',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'工作流的流程实例的拓展',
'SCHEMA', N'dbo',
'TABLE', N'bpm_process_instance_ext'
GO


-- ----------------------------
-- Records of bpm_process_instance_ext
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[bpm_process_instance_ext] ON
GO

SET IDENTITY_INSERT [dbo].[bpm_process_instance_ext] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for bpm_task_assign_rule
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[bpm_task_assign_rule]') AND type IN ('U'))
	DROP TABLE [dbo].[bpm_task_assign_rule]
GO

CREATE TABLE [dbo].[bpm_task_assign_rule] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [model_id] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [process_definition_id] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [task_definition_key] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [type] tinyint  NOT NULL,
  [options] nvarchar(1024) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[bpm_task_assign_rule] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_assign_rule',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'流程模型的编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_assign_rule',
'COLUMN', N'model_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'流程定义的编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_assign_rule',
'COLUMN', N'process_definition_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'流程任务定义的 key',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_assign_rule',
'COLUMN', N'task_definition_key'
GO

EXEC sp_addextendedproperty
'MS_Description', N'规则类型',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_assign_rule',
'COLUMN', N'type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'规则值，JSON 数组',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_assign_rule',
'COLUMN', N'options'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建者',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_assign_rule',
'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建时间',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_assign_rule',
'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新者',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_assign_rule',
'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新时间',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_assign_rule',
'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_assign_rule',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_assign_rule',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'Bpm 任务规则表',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_assign_rule'
GO


-- ----------------------------
-- Records of bpm_task_assign_rule
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[bpm_task_assign_rule] ON
GO

SET IDENTITY_INSERT [dbo].[bpm_task_assign_rule] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for bpm_task_ext
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[bpm_task_ext]') AND type IN ('U'))
	DROP TABLE [dbo].[bpm_task_ext]
GO

CREATE TABLE [dbo].[bpm_task_ext] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [assignee_user_id] bigint  NULL,
  [name] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [task_id] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [result] tinyint  NOT NULL,
  [reason] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [end_time] datetime2(7)  NULL,
  [process_instance_id] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [process_definition_id] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[bpm_task_ext] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_ext',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'任务的审批人',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_ext',
'COLUMN', N'assignee_user_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'任务的名字',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_ext',
'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'任务的编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_ext',
'COLUMN', N'task_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'任务的结果',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_ext',
'COLUMN', N'result'
GO

EXEC sp_addextendedproperty
'MS_Description', N'审批建议',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_ext',
'COLUMN', N'reason'
GO

EXEC sp_addextendedproperty
'MS_Description', N'任务的结束时间',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_ext',
'COLUMN', N'end_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'流程实例的编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_ext',
'COLUMN', N'process_instance_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'流程定义的编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_ext',
'COLUMN', N'process_definition_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建者',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_ext',
'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建时间',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_ext',
'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新者',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_ext',
'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新时间',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_ext',
'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_ext',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_ext',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'工作流的流程任务的拓展表',
'SCHEMA', N'dbo',
'TABLE', N'bpm_task_ext'
GO


-- ----------------------------
-- Records of bpm_task_ext
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[bpm_task_ext] ON
GO

SET IDENTITY_INSERT [dbo].[bpm_task_ext] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for bpm_user_group
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[bpm_user_group]') AND type IN ('U'))
	DROP TABLE [dbo].[bpm_user_group]
GO

CREATE TABLE [dbo].[bpm_user_group] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [name] nvarchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [description] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [member_user_ids] nvarchar(1024) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [status] tinyint  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[bpm_user_group] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_user_group',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'组名',
'SCHEMA', N'dbo',
'TABLE', N'bpm_user_group',
'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'描述',
'SCHEMA', N'dbo',
'TABLE', N'bpm_user_group',
'COLUMN', N'description'
GO

EXEC sp_addextendedproperty
'MS_Description', N'成员编号数组',
'SCHEMA', N'dbo',
'TABLE', N'bpm_user_group',
'COLUMN', N'member_user_ids'
GO

EXEC sp_addextendedproperty
'MS_Description', N'状态（0正常 1停用）',
'SCHEMA', N'dbo',
'TABLE', N'bpm_user_group',
'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建者',
'SCHEMA', N'dbo',
'TABLE', N'bpm_user_group',
'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建时间',
'SCHEMA', N'dbo',
'TABLE', N'bpm_user_group',
'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新者',
'SCHEMA', N'dbo',
'TABLE', N'bpm_user_group',
'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新时间',
'SCHEMA', N'dbo',
'TABLE', N'bpm_user_group',
'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'bpm_user_group',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'bpm_user_group',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用户组',
'SCHEMA', N'dbo',
'TABLE', N'bpm_user_group'
GO


-- ----------------------------
-- Records of bpm_user_group
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[bpm_user_group] ON
GO

SET IDENTITY_INSERT [dbo].[bpm_user_group] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for dual
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[dual]') AND type IN ('U'))
	DROP TABLE [dbo].[dual]
GO

CREATE TABLE [dbo].[dual] (
  [id] int  NULL
)
GO

ALTER TABLE [dbo].[dual] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据库连接的表',
'SCHEMA', N'dbo',
'TABLE', N'dual'
GO


-- ----------------------------
-- Records of dual
-- ----------------------------
BEGIN TRANSACTION
GO

COMMIT
GO


-- ----------------------------
-- Table structure for infra_api_access_log
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[infra_api_access_log]') AND type IN ('U'))
	DROP TABLE [dbo].[infra_api_access_log]
GO

CREATE TABLE [dbo].[infra_api_access_log] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [trace_id] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [user_id] bigint DEFAULT 0 NOT NULL,
  [user_type] tinyint DEFAULT 0 NOT NULL,
  [application_name] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [request_method] nvarchar(16) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [request_url] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [request_params] nvarchar(max) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [user_ip] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [user_agent] nvarchar(512) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [begin_time] datetime2(7)  NOT NULL,
  [end_time] datetime2(7)  NOT NULL,
  [duration] int  NOT NULL,
  [result_code] int  NOT NULL,
  [result_msg] nvarchar(512) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL,
  [tenant_id] bigint  NOT NULL
)
GO

ALTER TABLE [dbo].[infra_api_access_log] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'编号',
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
-- Records of infra_api_access_log
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[infra_api_access_log] ON
GO

SET IDENTITY_INSERT [dbo].[infra_api_access_log] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for infra_api_error_log
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[infra_api_error_log]') AND type IN ('U'))
	DROP TABLE [dbo].[infra_api_error_log]
GO

CREATE TABLE [dbo].[infra_api_error_log] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [trace_id] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [user_id] int DEFAULT 0 NOT NULL,
  [user_type] tinyint DEFAULT 0 NOT NULL,
  [application_name] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [request_method] nvarchar(16) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [request_url] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [request_params] nvarchar(max) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [user_ip] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [user_agent] nvarchar(512) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [exception_time] datetime2(7)  NOT NULL,
  [exception_name] nvarchar(128) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [exception_message] nvarchar(max) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [exception_root_cause_message] nvarchar(max) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [exception_stack_trace] nvarchar(max) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [exception_class_name] nvarchar(512) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [exception_file_name] nvarchar(512) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [exception_method_name] nvarchar(512) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [exception_line_number] int  NOT NULL,
  [process_status] tinyint  NOT NULL,
  [process_time] datetime2(7)  NULL,
  [process_user_id] int  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[infra_api_error_log] SET (LOCK_ESCALATION = TABLE)
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
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'infra_api_error_log',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'infra_api_error_log',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'系统异常日志',
'SCHEMA', N'dbo',
'TABLE', N'infra_api_error_log'
GO


-- ----------------------------
-- Records of infra_api_error_log
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[infra_api_error_log] ON
GO

SET IDENTITY_INSERT [dbo].[infra_api_error_log] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for infra_codegen_column
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[infra_codegen_column]') AND type IN ('U'))
	DROP TABLE [dbo].[infra_codegen_column]
GO

CREATE TABLE [dbo].[infra_codegen_column] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [table_id] bigint  NOT NULL,
  [column_name] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [data_type] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [column_comment] nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [nullable] varchar(1) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [primary_key] varchar(1) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [auto_increment] nchar(1) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [ordinal_position] int  NOT NULL,
  [java_type] nvarchar(32) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [java_field] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [dict_type] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [example] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_operation] varchar(1) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [update_operation] varchar(1) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [list_operation] varchar(1) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [list_operation_condition] nvarchar(32) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [list_operation_result] varchar(1) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [html_type] nvarchar(32) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[infra_codegen_column] SET (LOCK_ESCALATION = TABLE)
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
'MS_Description', N'是否自增',
'SCHEMA', N'dbo',
'TABLE', N'infra_codegen_column',
'COLUMN', N'auto_increment'
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
-- Records of infra_codegen_column
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[infra_codegen_column] ON
GO

SET IDENTITY_INSERT [dbo].[infra_codegen_column] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for infra_codegen_table
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[infra_codegen_table]') AND type IN ('U'))
	DROP TABLE [dbo].[infra_codegen_table]
GO

CREATE TABLE [dbo].[infra_codegen_table] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [data_source_config_id] bigint  NOT NULL,
  [scene] tinyint  NOT NULL,
  [table_name] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [table_comment] nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [remark] nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [module_name] nvarchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [business_name] nvarchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [class_name] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [class_comment] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [author] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [template_type] tinyint  NOT NULL,
  [parent_menu_id] bigint  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[infra_codegen_table] SET (LOCK_ESCALATION = TABLE)
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
'MS_Description', N'父菜单编号',
'SCHEMA', N'dbo',
'TABLE', N'infra_codegen_table',
'COLUMN', N'parent_menu_id'
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
-- Records of infra_codegen_table
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[infra_codegen_table] ON
GO

SET IDENTITY_INSERT [dbo].[infra_codegen_table] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for infra_config
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[infra_config]') AND type IN ('U'))
	DROP TABLE [dbo].[infra_config]
GO

CREATE TABLE [dbo].[infra_config] (
  [id] int  NOT NULL,
  [category] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [type] tinyint  NOT NULL,
  [name] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [config_key] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [value] nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [visible] varchar(1) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [remark] nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[infra_config] SET (LOCK_ESCALATION = TABLE)
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
BEGIN TRANSACTION
GO

INSERT INTO [dbo].[infra_config] ([id], [category], [type], [name], [config_key], [value], [visible], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1', N'ui', N'1', N'主框架页-默认皮肤样式名称', N'sys.index.skinName', N'skin-blue', N'0', N'蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-03-26 23:10:31.0000000', N'0')
GO

INSERT INTO [dbo].[infra_config] ([id], [category], [type], [name], [config_key], [value], [visible], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'2', N'biz', N'1', N'用户管理-账号初始密码', N'sys.user.init-password', N'123456', N'0', N'初始化密码 123456', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-03-20 02:25:51.0000000', N'0')
GO

INSERT INTO [dbo].[infra_config] ([id], [category], [type], [name], [config_key], [value], [visible], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'3', N'ui', N'1', N'主框架页-侧边栏主题', N'sys.index.sideTheme', N'theme-dark', N'0', N'深色主题theme-dark，浅色主题theme-light', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2021-01-19 03:05:21.0000000', N'0')
GO

INSERT INTO [dbo].[infra_config] ([id], [category], [type], [name], [config_key], [value], [visible], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'6', N'biz', N'2', N'登陆验证码的开关', N'yudao.captcha.enable', N'true', N'1', NULL, N'1', N'2022-02-17 00:03:11.0000000', N'1', N'2022-04-04 12:51:40.0000000', N'0')
GO

COMMIT
GO


-- ----------------------------
-- Table structure for infra_data_source_config
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[infra_data_source_config]') AND type IN ('U'))
	DROP TABLE [dbo].[infra_data_source_config]
GO

CREATE TABLE [dbo].[infra_data_source_config] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [name] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [url] nvarchar(1024) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [username] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [password] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[infra_data_source_config] SET (LOCK_ESCALATION = TABLE)
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
-- Records of infra_data_source_config
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[infra_data_source_config] ON
GO

INSERT INTO [dbo].[infra_data_source_config] ([id], [name], [url], [username], [password], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'8', N'test', N'jdbc:mysql://127.0.0.1:3306/testb5f4', N'root', N'3xgHTSHmF3mlgL3Ybw45ztewGDxGgEkWF3wTSYey7k+uXI/wdz45TrvYvYssQtmA', N'1', N'2022-04-27 22:48:20.0000000', N'1', N'2022-04-28 20:04:06.0000000', N'0')
GO

INSERT INTO [dbo].[infra_data_source_config] ([id], [name], [url], [username], [password], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'9', N'oracle_test', N'jdbc:oracle:thin:@127.0.0.1:1521:xe', N'root', N'vwmNAPLiEi+NX4AVdC+zNvpejPLwcFXp6dlhgNxCfDTi4vKRy76iIeFqyvpRerNC', N'1', N'2022-04-28 20:41:26.0000000', N'1', N'2022-04-28 20:41:26.0000000', N'0')
GO

SET IDENTITY_INSERT [dbo].[infra_data_source_config] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for infra_file
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[infra_file]') AND type IN ('U'))
	DROP TABLE [dbo].[infra_file]
GO

CREATE TABLE [dbo].[infra_file] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [config_id] bigint  NULL,
  [path] nvarchar(512) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [url] nvarchar(1024) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [type] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [size] int  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL,
  [name] nvarchar(512) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[infra_file] SET (LOCK_ESCALATION = TABLE)
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
'MS_Description', N'文件 MIME 类型',
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
'MS_Description', N'文件路径',
'SCHEMA', N'dbo',
'TABLE', N'infra_file',
'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'文件表',
'SCHEMA', N'dbo',
'TABLE', N'infra_file'
GO


-- ----------------------------
-- Records of infra_file
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[infra_file] ON
GO

SET IDENTITY_INSERT [dbo].[infra_file] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for infra_file_config
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[infra_file_config]') AND type IN ('U'))
	DROP TABLE [dbo].[infra_file_config]
GO

CREATE TABLE [dbo].[infra_file_config] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [name] nvarchar(63) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [storage] tinyint  NOT NULL,
  [remark] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [master] varchar(1) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [config] nvarchar(max) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[infra_file_config] SET (LOCK_ESCALATION = TABLE)
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
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[infra_file_config] ON
GO

INSERT INTO [dbo].[infra_file_config] ([id], [name], [storage], [remark], [master], [config], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'4', N'数据库', N'1', N'我是数据库', N'0', N'{"@class":"cn.iocoder.yudao.framework.file.core.client.db.DBFileClientConfig","domain":"http://127.0.0.1:48080"}', N'1', N'2022-03-15 23:56:24.0000000', N'1', N'2022-03-26 21:39:26.0000000', N'0')
GO

INSERT INTO [dbo].[infra_file_config] ([id], [name], [storage], [remark], [master], [config], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'5', N'本地磁盘', N'10', N'测试下本地存储', N'0', N'{"@class":"cn.iocoder.yudao.framework.file.core.client.local.LocalFileClientConfig","basePath":"/Users/yunai/file_test","domain":"http://127.0.0.1:48080"}', N'1', N'2022-03-15 23:57:00.0000000', N'1', N'2022-03-26 21:39:26.0000000', N'0')
GO

INSERT INTO [dbo].[infra_file_config] ([id], [name], [storage], [remark], [master], [config], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'11', N'S3 - 七牛云', N'20', NULL, N'1', N'{"@class":"cn.iocoder.yudao.framework.file.core.client.s3.S3FileClientConfig","endpoint":"s3-cn-south-1.qiniucs.com","domain":"http://test.yudao.iocoder.cn","bucket":"ruoyi-vue-pro","accessKey":"b7yvuhBSAGjmtPhMFcn9iMOxUOY_I06cA_p0ZUx8","accessSecret":"kXM1l5ia1RvSX3QaOEcwI3RLz3Y2rmNszWonKZtP"}', N'1', N'2022-03-19 18:00:03.0000000', N'1', N'2022-03-26 21:39:26.0000000', N'0')
GO

SET IDENTITY_INSERT [dbo].[infra_file_config] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for infra_file_content
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[infra_file_content]') AND type IN ('U'))
	DROP TABLE [dbo].[infra_file_content]
GO

CREATE TABLE [dbo].[infra_file_content] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [config_id] bigint  NOT NULL,
  [path] nvarchar(512) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [content] varbinary(max)  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[infra_file_content] SET (LOCK_ESCALATION = TABLE)
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
-- Records of infra_file_content
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[infra_file_content] ON
GO

SET IDENTITY_INSERT [dbo].[infra_file_content] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for infra_job
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[infra_job]') AND type IN ('U'))
	DROP TABLE [dbo].[infra_job]
GO

CREATE TABLE [dbo].[infra_job] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [name] nvarchar(32) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [status] tinyint  NOT NULL,
  [handler_name] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [handler_param] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [cron_expression] nvarchar(32) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [retry_count] int  NOT NULL,
  [retry_interval] int  NOT NULL,
  [monitor_timeout] int  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[infra_job] SET (LOCK_ESCALATION = TABLE)
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
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[infra_job] ON
GO

INSERT INTO [dbo].[infra_job] ([id], [name], [status], [handler_name], [handler_param], [cron_expression], [retry_count], [retry_interval], [monitor_timeout], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'16', N'用户 Session 超时 Job', N'1', N'userSessionTimeoutJob', NULL, N'0 * * * * ? *', N'2000', N'3', N'0', N'1', N'2022-05-02 17:28:48.8850000', N'1', N'2022-05-02 17:28:49.0240000', N'0')
GO

SET IDENTITY_INSERT [dbo].[infra_job] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for infra_job_log
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[infra_job_log]') AND type IN ('U'))
	DROP TABLE [dbo].[infra_job_log]
GO

CREATE TABLE [dbo].[infra_job_log] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [job_id] bigint  NOT NULL,
  [handler_name] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [handler_param] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [execute_index] tinyint  NOT NULL,
  [begin_time] datetime2(7)  NOT NULL,
  [end_time] datetime2(7)  NULL,
  [duration] int  NULL,
  [status] tinyint  NOT NULL,
  [result] nvarchar(4000) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[infra_job_log] SET (LOCK_ESCALATION = TABLE)
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
-- Records of infra_job_log
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[infra_job_log] ON
GO

SET IDENTITY_INSERT [dbo].[infra_job_log] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for infra_test_demo
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[infra_test_demo]') AND type IN ('U'))
	DROP TABLE [dbo].[infra_test_demo]
GO

CREATE TABLE [dbo].[infra_test_demo] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [name] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [status] tinyint  NOT NULL,
  [type] tinyint  NOT NULL,
  [category] tinyint  NOT NULL,
  [remark] nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[infra_test_demo] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'编号',
'SCHEMA', N'dbo',
'TABLE', N'infra_test_demo',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'名字',
'SCHEMA', N'dbo',
'TABLE', N'infra_test_demo',
'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'状态',
'SCHEMA', N'dbo',
'TABLE', N'infra_test_demo',
'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'类型',
'SCHEMA', N'dbo',
'TABLE', N'infra_test_demo',
'COLUMN', N'type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'分类',
'SCHEMA', N'dbo',
'TABLE', N'infra_test_demo',
'COLUMN', N'category'
GO

EXEC sp_addextendedproperty
'MS_Description', N'备注',
'SCHEMA', N'dbo',
'TABLE', N'infra_test_demo',
'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建者',
'SCHEMA', N'dbo',
'TABLE', N'infra_test_demo',
'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建时间',
'SCHEMA', N'dbo',
'TABLE', N'infra_test_demo',
'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新者',
'SCHEMA', N'dbo',
'TABLE', N'infra_test_demo',
'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新时间',
'SCHEMA', N'dbo',
'TABLE', N'infra_test_demo',
'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'infra_test_demo',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'字典类型表',
'SCHEMA', N'dbo',
'TABLE', N'infra_test_demo'
GO


-- ----------------------------
-- Records of infra_test_demo
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[infra_test_demo] ON
GO

SET IDENTITY_INSERT [dbo].[infra_test_demo] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for member_user
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[member_user]') AND type IN ('U'))
	DROP TABLE [dbo].[member_user]
GO

CREATE TABLE [dbo].[member_user] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [nickname] nvarchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [avatar] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [status] tinyint  NOT NULL,
  [mobile] nvarchar(11) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [password] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [register_ip] nvarchar(32) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [login_ip] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [login_date] datetime2(7)  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[member_user] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'编号',
'SCHEMA', N'dbo',
'TABLE', N'member_user',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用户昵称',
'SCHEMA', N'dbo',
'TABLE', N'member_user',
'COLUMN', N'nickname'
GO

EXEC sp_addextendedproperty
'MS_Description', N'头像',
'SCHEMA', N'dbo',
'TABLE', N'member_user',
'COLUMN', N'avatar'
GO

EXEC sp_addextendedproperty
'MS_Description', N'状态',
'SCHEMA', N'dbo',
'TABLE', N'member_user',
'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'手机号',
'SCHEMA', N'dbo',
'TABLE', N'member_user',
'COLUMN', N'mobile'
GO

EXEC sp_addextendedproperty
'MS_Description', N'密码',
'SCHEMA', N'dbo',
'TABLE', N'member_user',
'COLUMN', N'password'
GO

EXEC sp_addextendedproperty
'MS_Description', N'注册 IP',
'SCHEMA', N'dbo',
'TABLE', N'member_user',
'COLUMN', N'register_ip'
GO

EXEC sp_addextendedproperty
'MS_Description', N'最后登录IP',
'SCHEMA', N'dbo',
'TABLE', N'member_user',
'COLUMN', N'login_ip'
GO

EXEC sp_addextendedproperty
'MS_Description', N'最后登录时间',
'SCHEMA', N'dbo',
'TABLE', N'member_user',
'COLUMN', N'login_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建者',
'SCHEMA', N'dbo',
'TABLE', N'member_user',
'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建时间',
'SCHEMA', N'dbo',
'TABLE', N'member_user',
'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新者',
'SCHEMA', N'dbo',
'TABLE', N'member_user',
'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新时间',
'SCHEMA', N'dbo',
'TABLE', N'member_user',
'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'member_user',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'member_user',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用户',
'SCHEMA', N'dbo',
'TABLE', N'member_user'
GO


-- ----------------------------
-- Records of member_user
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[member_user] ON
GO

SET IDENTITY_INSERT [dbo].[member_user] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for pay_app
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[pay_app]') AND type IN ('U'))
	DROP TABLE [dbo].[pay_app]
GO

CREATE TABLE [dbo].[pay_app] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [name] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [status] tinyint  NOT NULL,
  [remark] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [pay_notify_url] nvarchar(1024) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [refund_notify_url] nvarchar(1024) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [merchant_id] bigint  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[pay_app] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'应用编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_app',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'应用名',
'SCHEMA', N'dbo',
'TABLE', N'pay_app',
'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'开启状态',
'SCHEMA', N'dbo',
'TABLE', N'pay_app',
'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'备注',
'SCHEMA', N'dbo',
'TABLE', N'pay_app',
'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付结果的回调地址',
'SCHEMA', N'dbo',
'TABLE', N'pay_app',
'COLUMN', N'pay_notify_url'
GO

EXEC sp_addextendedproperty
'MS_Description', N'退款结果的回调地址',
'SCHEMA', N'dbo',
'TABLE', N'pay_app',
'COLUMN', N'refund_notify_url'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商户编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_app',
'COLUMN', N'merchant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建者',
'SCHEMA', N'dbo',
'TABLE', N'pay_app',
'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_app',
'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新者',
'SCHEMA', N'dbo',
'TABLE', N'pay_app',
'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_app',
'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_app',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'pay_app',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付应用信息',
'SCHEMA', N'dbo',
'TABLE', N'pay_app'
GO


-- ----------------------------
-- Records of pay_app
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[pay_app] ON
GO

INSERT INTO [dbo].[pay_app] ([id], [name], [status], [remark], [pay_notify_url], [refund_notify_url], [merchant_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'6', N'芋道', N'0', N'我是一个公众号', N'http://127.0.0.1:28080/api/shop/order/pay-notify', N'http://127.0.0.1', N'1', N'', N'2021-10-23 08:49:25.0000000', N'', N'2022-02-27 04:14:53.0000000', N'1', N'0')
GO

SET IDENTITY_INSERT [dbo].[pay_app] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for pay_channel
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[pay_channel]') AND type IN ('U'))
	DROP TABLE [dbo].[pay_channel]
GO

CREATE TABLE [dbo].[pay_channel] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [code] nvarchar(32) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [status] tinyint  NOT NULL,
  [remark] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [fee_rate] float(53)  NOT NULL,
  [merchant_id] bigint  NOT NULL,
  [app_id] bigint  NOT NULL,
  [config] nvarchar(max) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[pay_channel] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'商户编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_channel',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'渠道编码',
'SCHEMA', N'dbo',
'TABLE', N'pay_channel',
'COLUMN', N'code'
GO

EXEC sp_addextendedproperty
'MS_Description', N'开启状态',
'SCHEMA', N'dbo',
'TABLE', N'pay_channel',
'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'备注',
'SCHEMA', N'dbo',
'TABLE', N'pay_channel',
'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
'MS_Description', N'渠道费率，单位：百分比',
'SCHEMA', N'dbo',
'TABLE', N'pay_channel',
'COLUMN', N'fee_rate'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商户编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_channel',
'COLUMN', N'merchant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'应用编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_channel',
'COLUMN', N'app_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付渠道配置',
'SCHEMA', N'dbo',
'TABLE', N'pay_channel',
'COLUMN', N'config'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建者',
'SCHEMA', N'dbo',
'TABLE', N'pay_channel',
'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_channel',
'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新者',
'SCHEMA', N'dbo',
'TABLE', N'pay_channel',
'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_channel',
'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_channel',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'pay_channel',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付渠道
',
'SCHEMA', N'dbo',
'TABLE', N'pay_channel'
GO


-- ----------------------------
-- Records of pay_channel
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[pay_channel] ON
GO

INSERT INTO [dbo].[pay_channel] ([id], [code], [status], [remark], [fee_rate], [merchant_id], [app_id], [config], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'9', N'wx_pub', N'0', NULL, N'1', N'1', N'6', N'{"@class":"cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXPayClientConfig","appId":"wx041349c6f39b268b","mchId":"1545083881","apiVersion":"v2","mchKey":"0alL64UDQdlCwiKZ73ib7ypaIjMns06p","privateKeyContent":"-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC5q2hYE3loOQoH\nl/2kh/epuj17W8VpV5vBl7ysJWAbBXux6mlq4gKTHD0QUQdiKtDEUm/bKC9Bi6VU\nuklM5Y8oCaCbhjklHRbET8jsgd9phSNGviHclYRLsQRO8oXnN89kN0y7DYKm0hYd\nmaiS12Z3v8VaImSTr4HVeHlC/z3S6mdwSr263stKt931YTcbTj/QFH7znsv9Na0u\nX6LaMBEEAsJctWdm8Ndrd1tGh9Fzf0DA5VRXsJR3kkWspy+IwiDTPV/FDKOU9NJC\nSxMmDePerTfkoZ2s1rltqBK0ykDJrXtxR+hTzEsKZ/KpNi8tyYpfNZsviHIlUsLP\nFJ5UvUhpAgMBAAECggEAd90NltazqTIxpGdeCwrwOzWNnYbIclJprlhMKIJUgf1P\nNrPTbHoOGXTAgzkcYCat8iAaMEzH/TOu/3zn92m3uqxEcEL9v1UBLqknWHAbkB6w\ngGocqDAqYUcdNe5hvbyM+fCta5C0SQgV2PQrHOlMMICwYpkTfzhtxCdreXIYMoGg\nJEIRkZWgrm/N7LTtNgizznuUjy6OURWjXaWKPcs3b3j6G1gLj9Vp++z4y0u51nqM\n4R6fcvl8M6BjlcC8zo6DbOvCW8cXtuXsnru+2TPrUnsGeybJok4fEQsfW1BvpvPo\nief38rYJn4OWxIrHcpWrhNtXtgRPeiMGFfIsEQDmVQKBgQDzXK6Nn3Nr3TFfGVTy\n8QYrzOuY2NqzH8nnsLL6qn3HoKxTv+PcFKOTPsi6f4hIYCzBP0esRrPv0ffMu9oQ\nJvFtCJvMmcKGtp0Q5hcj0y/XkbC3AWuahJtBv8lhKXVnQXSL0j3+ombljw4/8yN0\nAzgBz+j/skQQgZ3sN5h+DHGtgwKBgQDDT784/2pd4m86c/uBmrwYfqu6MJo0eHJh\n1XPtE+u8pOHyNTFk77rKobKDqN5VlrF0uEmBc/08LKhyxJ3vh/zAbcmqT1Mq778y\nAKKUtVmvcaVDrvSQHsnhj0zt4SHGmmU34U2b9hV+nocq5VszX6/jp//HJi9bs3We\ndAzfFCmaowKBgC1MmDVGc+6lCraf+X8LPFHU4Bnga70h8qxM6NPd/nG1R76DHn/t\n25DiA+0rJgwK0unZxJadxoqic9TJNssA5Lmd+5o3GM2Imm311mLVwbcHqHQ4MHZf\nrqKrd2m9lNv2hCIurVmDk1Gxsj5XHMdQfhFgSQengCHubp30r07vNA3PAoGAUEAE\nIjdQTSMs8KeXP7mEb8wcY3R05/pVhT1fVJpK0kgtTofss7yM05V88/v+3sv8Pik6\niqZN9tuimwWOn00Q3UA/DGtrkMjRlooMQ24AW8YmUZkhg9YivTtUMKnAZwopbLx2\nVw7V5iDdCRMUVheK/c+ZmQpnixZBzcmBQGfYcGECgYBjEq3Mem+Aw6pXOu6+0FwH\n9y6Xi4HhBkq0OOZZuXFtWVry7YrD3pBgzWVAZJqJCkyPKKZzCzwdbFd3u0lYBs35\nzYgx7ug4hR+wfI980a3vxjcWGOqnOUUnUJ7ucIa+KDgnYV/bBy4jqpVreXmWAJXl\nfyjG3eLWBrtrsI9YX6zeAA==\n-----END PRIVATE KEY-----\n","privateCertContent":"-----BEGIN CERTIFICATE-----\nMIID6TCCAtGgAwIBAgIUNkEHq6aQcF80NSYqWS58ybsJzI4wDQYJKoZIhvcNAQEL\nBQAwXjELMAkGA1UEBhMCQ04xEzARBgNVBAoTClRlbnBheS5jb20xHTAbBgNVBAsT\nFFRlbnBheS5jb20gQ0EgQ2VudGVyMRswGQYDVQQDExJUZW5wYXkuY29tIFJvb3Qg\nQ0EwHhcNMjExMDIxMDU0NTQxWhcNMjYxMDIwMDU0NTQxWjB7MRMwEQYDVQQDDAox\nNTQ1MDgzODgxMRswGQYDVQQKDBLlvq7kv6HllYbmiLfns7vnu58xJzAlBgNVBAsM\nHuWOhuWfjuWMuuWkp+adjuWwp+aXpeeUqOWTgeW6lzELMAkGA1UEBgwCQ04xETAP\nBgNVBAcMCFNoZW5aaGVuMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA\nuatoWBN5aDkKB5f9pIf3qbo9e1vFaVebwZe8rCVgGwV7seppauICkxw9EFEHYirQ\nxFJv2ygvQYulVLpJTOWPKAmgm4Y5JR0WxE/I7IHfaYUjRr4h3JWES7EETvKF5zfP\nZDdMuw2CptIWHZmoktdmd7/FWiJkk6+B1Xh5Qv890upncEq9ut7LSrfd9WE3G04/\n0BR+857L/TWtLl+i2jARBALCXLVnZvDXa3dbRofRc39AwOVUV7CUd5JFrKcviMIg\n0z1fxQyjlPTSQksTJg3j3q035KGdrNa5bagStMpAya17cUfoU8xLCmfyqTYvLcmK\nXzWbL4hyJVLCzxSeVL1IaQIDAQABo4GBMH8wCQYDVR0TBAIwADALBgNVHQ8EBAMC\nBPAwZQYDVR0fBF4wXDBaoFigVoZUaHR0cDovL2V2Y2EuaXRydXMuY29tLmNuL3B1\nYmxpYy9pdHJ1c2NybD9DQT0xQkQ0MjIwRTUwREJDMDRCMDZBRDM5NzU0OTg0NkMw\nMUMzRThFQkQyMA0GCSqGSIb3DQEBCwUAA4IBAQBe7XgncAY/1PLbCsnMsYt11k3V\n2cdNZ+yuCxhlOEKk3nHE6WCTL6zL0qWlTKKpnw1rE/+4OS76Tg72wWXcHfHDAOgt\n9icp62cKx1WO3QweeZpSvLDmtdLgKKrqeIWh+rL8+ZhuAOxSkaRwcsMTWDaLeDOi\n0pGeqvfG8WNhPxkkaSI8xbiTK641Yg9WT/Q4yfHS7Q6wg1dj9YQdo0dvVB0S2Nir\nX9IK6PUaHDnQeFKDmKgLkDGLaKaiijEvC91wMEE6qB8b0eNhciaxq2YhGHcFmSRP\nWUyc5CmBadt7wIOH5Z3bfuwWGxqxKjNw/baM/d+nk7hlDr01YL9c0g16B9MW\n-----END CERTIFICATE-----\n","apiV3Key":"joerVi8y5DJ3o4ttA0o1uH47Xz1u2Ase"}', NULL, N'2021-10-23 17:12:10.0000000', NULL, N'2022-02-27 04:15:13.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[pay_channel] ([id], [code], [status], [remark], [fee_rate], [merchant_id], [app_id], [config], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'10', N'wx_pub', N'0', NULL, N'1', N'1', N'6', N'{"@class":"cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXPayClientConfig","appId":"wx041349c6f39b268b","mchId":"1545083881","apiVersion":"v2","mchKey":"0alL64UDQdlCwiKZ73ib7ypaIjMns06p","privateKeyContent":"-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC5q2hYE3loOQoH\nl/2kh/epuj17W8VpV5vBl7ysJWAbBXux6mlq4gKTHD0QUQdiKtDEUm/bKC9Bi6VU\nuklM5Y8oCaCbhjklHRbET8jsgd9phSNGviHclYRLsQRO8oXnN89kN0y7DYKm0hYd\nmaiS12Z3v8VaImSTr4HVeHlC/z3S6mdwSr263stKt931YTcbTj/QFH7znsv9Na0u\nX6LaMBEEAsJctWdm8Ndrd1tGh9Fzf0DA5VRXsJR3kkWspy+IwiDTPV/FDKOU9NJC\nSxMmDePerTfkoZ2s1rltqBK0ykDJrXtxR+hTzEsKZ/KpNi8tyYpfNZsviHIlUsLP\nFJ5UvUhpAgMBAAECggEAd90NltazqTIxpGdeCwrwOzWNnYbIclJprlhMKIJUgf1P\nNrPTbHoOGXTAgzkcYCat8iAaMEzH/TOu/3zn92m3uqxEcEL9v1UBLqknWHAbkB6w\ngGocqDAqYUcdNe5hvbyM+fCta5C0SQgV2PQrHOlMMICwYpkTfzhtxCdreXIYMoGg\nJEIRkZWgrm/N7LTtNgizznuUjy6OURWjXaWKPcs3b3j6G1gLj9Vp++z4y0u51nqM\n4R6fcvl8M6BjlcC8zo6DbOvCW8cXtuXsnru+2TPrUnsGeybJok4fEQsfW1BvpvPo\nief38rYJn4OWxIrHcpWrhNtXtgRPeiMGFfIsEQDmVQKBgQDzXK6Nn3Nr3TFfGVTy\n8QYrzOuY2NqzH8nnsLL6qn3HoKxTv+PcFKOTPsi6f4hIYCzBP0esRrPv0ffMu9oQ\nJvFtCJvMmcKGtp0Q5hcj0y/XkbC3AWuahJtBv8lhKXVnQXSL0j3+ombljw4/8yN0\nAzgBz+j/skQQgZ3sN5h+DHGtgwKBgQDDT784/2pd4m86c/uBmrwYfqu6MJo0eHJh\n1XPtE+u8pOHyNTFk77rKobKDqN5VlrF0uEmBc/08LKhyxJ3vh/zAbcmqT1Mq778y\nAKKUtVmvcaVDrvSQHsnhj0zt4SHGmmU34U2b9hV+nocq5VszX6/jp//HJi9bs3We\ndAzfFCmaowKBgC1MmDVGc+6lCraf+X8LPFHU4Bnga70h8qxM6NPd/nG1R76DHn/t\n25DiA+0rJgwK0unZxJadxoqic9TJNssA5Lmd+5o3GM2Imm311mLVwbcHqHQ4MHZf\nrqKrd2m9lNv2hCIurVmDk1Gxsj5XHMdQfhFgSQengCHubp30r07vNA3PAoGAUEAE\nIjdQTSMs8KeXP7mEb8wcY3R05/pVhT1fVJpK0kgtTofss7yM05V88/v+3sv8Pik6\niqZN9tuimwWOn00Q3UA/DGtrkMjRlooMQ24AW8YmUZkhg9YivTtUMKnAZwopbLx2\nVw7V5iDdCRMUVheK/c+ZmQpnixZBzcmBQGfYcGECgYBjEq3Mem+Aw6pXOu6+0FwH\n9y6Xi4HhBkq0OOZZuXFtWVry7YrD3pBgzWVAZJqJCkyPKKZzCzwdbFd3u0lYBs35\nzYgx7ug4hR+wfI980a3vxjcWGOqnOUUnUJ7ucIa+KDgnYV/bBy4jqpVreXmWAJXl\nfyjG3eLWBrtrsI9YX6zeAA==\n-----END PRIVATE KEY-----\n","privateCertContent":"-----BEGIN CERTIFICATE-----\nMIID6TCCAtGgAwIBAgIUNkEHq6aQcF80NSYqWS58ybsJzI4wDQYJKoZIhvcNAQEL\nBQAwXjELMAkGA1UEBhMCQ04xEzARBgNVBAoTClRlbnBheS5jb20xHTAbBgNVBAsT\nFFRlbnBheS5jb20gQ0EgQ2VudGVyMRswGQYDVQQDExJUZW5wYXkuY29tIFJvb3Qg\nQ0EwHhcNMjExMDIxMDU0NTQxWhcNMjYxMDIwMDU0NTQxWjB7MRMwEQYDVQQDDAox\nNTQ1MDgzODgxMRswGQYDVQQKDBLlvq7kv6HllYbmiLfns7vnu58xJzAlBgNVBAsM\nHuWOhuWfjuWMuuWkp+adjuWwp+aXpeeUqOWTgeW6lzELMAkGA1UEBgwCQ04xETAP\nBgNVBAcMCFNoZW5aaGVuMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA\nuatoWBN5aDkKB5f9pIf3qbo9e1vFaVebwZe8rCVgGwV7seppauICkxw9EFEHYirQ\nxFJv2ygvQYulVLpJTOWPKAmgm4Y5JR0WxE/I7IHfaYUjRr4h3JWES7EETvKF5zfP\nZDdMuw2CptIWHZmoktdmd7/FWiJkk6+B1Xh5Qv890upncEq9ut7LSrfd9WE3G04/\n0BR+857L/TWtLl+i2jARBALCXLVnZvDXa3dbRofRc39AwOVUV7CUd5JFrKcviMIg\n0z1fxQyjlPTSQksTJg3j3q035KGdrNa5bagStMpAya17cUfoU8xLCmfyqTYvLcmK\nXzWbL4hyJVLCzxSeVL1IaQIDAQABo4GBMH8wCQYDVR0TBAIwADALBgNVHQ8EBAMC\nBPAwZQYDVR0fBF4wXDBaoFigVoZUaHR0cDovL2V2Y2EuaXRydXMuY29tLmNuL3B1\nYmxpYy9pdHJ1c2NybD9DQT0xQkQ0MjIwRTUwREJDMDRCMDZBRDM5NzU0OTg0NkMw\nMUMzRThFQkQyMA0GCSqGSIb3DQEBCwUAA4IBAQBe7XgncAY/1PLbCsnMsYt11k3V\n2cdNZ+yuCxhlOEKk3nHE6WCTL6zL0qWlTKKpnw1rE/+4OS76Tg72wWXcHfHDAOgt\n9icp62cKx1WO3QweeZpSvLDmtdLgKKrqeIWh+rL8+ZhuAOxSkaRwcsMTWDaLeDOi\n0pGeqvfG8WNhPxkkaSI8xbiTK641Yg9WT/Q4yfHS7Q6wg1dj9YQdo0dvVB0S2Nir\nX9IK6PUaHDnQeFKDmKgLkDGLaKaiijEvC91wMEE6qB8b0eNhciaxq2YhGHcFmSRP\nWUyc5CmBadt7wIOH5Z3bfuwWGxqxKjNw/baM/d+nk7hlDr01YL9c0g16B9MW\n-----END CERTIFICATE-----\n","apiV3Key":"joerVi8y5DJ3o4ttA0o1uH47Xz1u2Ase"}', NULL, N'2021-12-14 22:01:24.0000000', NULL, N'2022-02-27 04:15:12.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[pay_channel] ([id], [code], [status], [remark], [fee_rate], [merchant_id], [app_id], [config], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'11', N'wx_pub', N'0', NULL, N'1', N'1', N'6', N'{"@class":"cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXPayClientConfig","appId":"wx041349c6f39b268b","mchId":"1545083881","apiVersion":"v2","mchKey":"0alL64UDQdlCwiKZ73ib7ypaIjMns06p","privateKeyContent":"-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC5q2hYE3loOQoH\nl/2kh/epuj17W8VpV5vBl7ysJWAbBXux6mlq4gKTHD0QUQdiKtDEUm/bKC9Bi6VU\nuklM5Y8oCaCbhjklHRbET8jsgd9phSNGviHclYRLsQRO8oXnN89kN0y7DYKm0hYd\nmaiS12Z3v8VaImSTr4HVeHlC/z3S6mdwSr263stKt931YTcbTj/QFH7znsv9Na0u\nX6LaMBEEAsJctWdm8Ndrd1tGh9Fzf0DA5VRXsJR3kkWspy+IwiDTPV/FDKOU9NJC\nSxMmDePerTfkoZ2s1rltqBK0ykDJrXtxR+hTzEsKZ/KpNi8tyYpfNZsviHIlUsLP\nFJ5UvUhpAgMBAAECggEAd90NltazqTIxpGdeCwrwOzWNnYbIclJprlhMKIJUgf1P\nNrPTbHoOGXTAgzkcYCat8iAaMEzH/TOu/3zn92m3uqxEcEL9v1UBLqknWHAbkB6w\ngGocqDAqYUcdNe5hvbyM+fCta5C0SQgV2PQrHOlMMICwYpkTfzhtxCdreXIYMoGg\nJEIRkZWgrm/N7LTtNgizznuUjy6OURWjXaWKPcs3b3j6G1gLj9Vp++z4y0u51nqM\n4R6fcvl8M6BjlcC8zo6DbOvCW8cXtuXsnru+2TPrUnsGeybJok4fEQsfW1BvpvPo\nief38rYJn4OWxIrHcpWrhNtXtgRPeiMGFfIsEQDmVQKBgQDzXK6Nn3Nr3TFfGVTy\n8QYrzOuY2NqzH8nnsLL6qn3HoKxTv+PcFKOTPsi6f4hIYCzBP0esRrPv0ffMu9oQ\nJvFtCJvMmcKGtp0Q5hcj0y/XkbC3AWuahJtBv8lhKXVnQXSL0j3+ombljw4/8yN0\nAzgBz+j/skQQgZ3sN5h+DHGtgwKBgQDDT784/2pd4m86c/uBmrwYfqu6MJo0eHJh\n1XPtE+u8pOHyNTFk77rKobKDqN5VlrF0uEmBc/08LKhyxJ3vh/zAbcmqT1Mq778y\nAKKUtVmvcaVDrvSQHsnhj0zt4SHGmmU34U2b9hV+nocq5VszX6/jp//HJi9bs3We\ndAzfFCmaowKBgC1MmDVGc+6lCraf+X8LPFHU4Bnga70h8qxM6NPd/nG1R76DHn/t\n25DiA+0rJgwK0unZxJadxoqic9TJNssA5Lmd+5o3GM2Imm311mLVwbcHqHQ4MHZf\nrqKrd2m9lNv2hCIurVmDk1Gxsj5XHMdQfhFgSQengCHubp30r07vNA3PAoGAUEAE\nIjdQTSMs8KeXP7mEb8wcY3R05/pVhT1fVJpK0kgtTofss7yM05V88/v+3sv8Pik6\niqZN9tuimwWOn00Q3UA/DGtrkMjRlooMQ24AW8YmUZkhg9YivTtUMKnAZwopbLx2\nVw7V5iDdCRMUVheK/c+ZmQpnixZBzcmBQGfYcGECgYBjEq3Mem+Aw6pXOu6+0FwH\n9y6Xi4HhBkq0OOZZuXFtWVry7YrD3pBgzWVAZJqJCkyPKKZzCzwdbFd3u0lYBs35\nzYgx7ug4hR+wfI980a3vxjcWGOqnOUUnUJ7ucIa+KDgnYV/bBy4jqpVreXmWAJXl\nfyjG3eLWBrtrsI9YX6zeAA==\n-----END PRIVATE KEY-----\n","privateCertContent":"-----BEGIN CERTIFICATE-----\nMIID6TCCAtGgAwIBAgIUNkEHq6aQcF80NSYqWS58ybsJzI4wDQYJKoZIhvcNAQEL\nBQAwXjELMAkGA1UEBhMCQ04xEzARBgNVBAoTClRlbnBheS5jb20xHTAbBgNVBAsT\nFFRlbnBheS5jb20gQ0EgQ2VudGVyMRswGQYDVQQDExJUZW5wYXkuY29tIFJvb3Qg\nQ0EwHhcNMjExMDIxMDU0NTQxWhcNMjYxMDIwMDU0NTQxWjB7MRMwEQYDVQQDDAox\nNTQ1MDgzODgxMRswGQYDVQQKDBLlvq7kv6HllYbmiLfns7vnu58xJzAlBgNVBAsM\nHuWOhuWfjuWMuuWkp+adjuWwp+aXpeeUqOWTgeW6lzELMAkGA1UEBgwCQ04xETAP\nBgNVBAcMCFNoZW5aaGVuMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA\nuatoWBN5aDkKB5f9pIf3qbo9e1vFaVebwZe8rCVgGwV7seppauICkxw9EFEHYirQ\nxFJv2ygvQYulVLpJTOWPKAmgm4Y5JR0WxE/I7IHfaYUjRr4h3JWES7EETvKF5zfP\nZDdMuw2CptIWHZmoktdmd7/FWiJkk6+B1Xh5Qv890upncEq9ut7LSrfd9WE3G04/\n0BR+857L/TWtLl+i2jARBALCXLVnZvDXa3dbRofRc39AwOVUV7CUd5JFrKcviMIg\n0z1fxQyjlPTSQksTJg3j3q035KGdrNa5bagStMpAya17cUfoU8xLCmfyqTYvLcmK\nXzWbL4hyJVLCzxSeVL1IaQIDAQABo4GBMH8wCQYDVR0TBAIwADALBgNVHQ8EBAMC\nBPAwZQYDVR0fBF4wXDBaoFigVoZUaHR0cDovL2V2Y2EuaXRydXMuY29tLmNuL3B1\nYmxpYy9pdHJ1c2NybD9DQT0xQkQ0MjIwRTUwREJDMDRCMDZBRDM5NzU0OTg0NkMw\nMUMzRThFQkQyMA0GCSqGSIb3DQEBCwUAA4IBAQBe7XgncAY/1PLbCsnMsYt11k3V\n2cdNZ+yuCxhlOEKk3nHE6WCTL6zL0qWlTKKpnw1rE/+4OS76Tg72wWXcHfHDAOgt\n9icp62cKx1WO3QweeZpSvLDmtdLgKKrqeIWh+rL8+ZhuAOxSkaRwcsMTWDaLeDOi\n0pGeqvfG8WNhPxkkaSI8xbiTK641Yg9WT/Q4yfHS7Q6wg1dj9YQdo0dvVB0S2Nir\nX9IK6PUaHDnQeFKDmKgLkDGLaKaiijEvC91wMEE6qB8b0eNhciaxq2YhGHcFmSRP\nWUyc5CmBadt7wIOH5Z3bfuwWGxqxKjNw/baM/d+nk7hlDr01YL9c0g16B9MW\n-----END CERTIFICATE-----\n","apiV3Key":"joerVi8y5DJ3o4ttA0o1uH47Xz1u2Ase"}', NULL, N'2021-12-14 22:02:57.0000000', NULL, N'2022-02-27 04:15:11.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[pay_channel] ([id], [code], [status], [remark], [fee_rate], [merchant_id], [app_id], [config], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'12', N'wx_pub', N'0', NULL, N'1', N'1', N'6', N'{"@class":"cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXPayClientConfig","appId":"wx041349c6f39b268b","mchId":"1545083881","apiVersion":"v2","mchKey":"0alL64UDQdlCwiKZ73ib7ypaIjMns06p","privateKeyContent":"-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC5q2hYE3loOQoH\nl/2kh/epuj17W8VpV5vBl7ysJWAbBXux6mlq4gKTHD0QUQdiKtDEUm/bKC9Bi6VU\nuklM5Y8oCaCbhjklHRbET8jsgd9phSNGviHclYRLsQRO8oXnN89kN0y7DYKm0hYd\nmaiS12Z3v8VaImSTr4HVeHlC/z3S6mdwSr263stKt931YTcbTj/QFH7znsv9Na0u\nX6LaMBEEAsJctWdm8Ndrd1tGh9Fzf0DA5VRXsJR3kkWspy+IwiDTPV/FDKOU9NJC\nSxMmDePerTfkoZ2s1rltqBK0ykDJrXtxR+hTzEsKZ/KpNi8tyYpfNZsviHIlUsLP\nFJ5UvUhpAgMBAAECggEAd90NltazqTIxpGdeCwrwOzWNnYbIclJprlhMKIJUgf1P\nNrPTbHoOGXTAgzkcYCat8iAaMEzH/TOu/3zn92m3uqxEcEL9v1UBLqknWHAbkB6w\ngGocqDAqYUcdNe5hvbyM+fCta5C0SQgV2PQrHOlMMICwYpkTfzhtxCdreXIYMoGg\nJEIRkZWgrm/N7LTtNgizznuUjy6OURWjXaWKPcs3b3j6G1gLj9Vp++z4y0u51nqM\n4R6fcvl8M6BjlcC8zo6DbOvCW8cXtuXsnru+2TPrUnsGeybJok4fEQsfW1BvpvPo\nief38rYJn4OWxIrHcpWrhNtXtgRPeiMGFfIsEQDmVQKBgQDzXK6Nn3Nr3TFfGVTy\n8QYrzOuY2NqzH8nnsLL6qn3HoKxTv+PcFKOTPsi6f4hIYCzBP0esRrPv0ffMu9oQ\nJvFtCJvMmcKGtp0Q5hcj0y/XkbC3AWuahJtBv8lhKXVnQXSL0j3+ombljw4/8yN0\nAzgBz+j/skQQgZ3sN5h+DHGtgwKBgQDDT784/2pd4m86c/uBmrwYfqu6MJo0eHJh\n1XPtE+u8pOHyNTFk77rKobKDqN5VlrF0uEmBc/08LKhyxJ3vh/zAbcmqT1Mq778y\nAKKUtVmvcaVDrvSQHsnhj0zt4SHGmmU34U2b9hV+nocq5VszX6/jp//HJi9bs3We\ndAzfFCmaowKBgC1MmDVGc+6lCraf+X8LPFHU4Bnga70h8qxM6NPd/nG1R76DHn/t\n25DiA+0rJgwK0unZxJadxoqic9TJNssA5Lmd+5o3GM2Imm311mLVwbcHqHQ4MHZf\nrqKrd2m9lNv2hCIurVmDk1Gxsj5XHMdQfhFgSQengCHubp30r07vNA3PAoGAUEAE\nIjdQTSMs8KeXP7mEb8wcY3R05/pVhT1fVJpK0kgtTofss7yM05V88/v+3sv8Pik6\niqZN9tuimwWOn00Q3UA/DGtrkMjRlooMQ24AW8YmUZkhg9YivTtUMKnAZwopbLx2\nVw7V5iDdCRMUVheK/c+ZmQpnixZBzcmBQGfYcGECgYBjEq3Mem+Aw6pXOu6+0FwH\n9y6Xi4HhBkq0OOZZuXFtWVry7YrD3pBgzWVAZJqJCkyPKKZzCzwdbFd3u0lYBs35\nzYgx7ug4hR+wfI980a3vxjcWGOqnOUUnUJ7ucIa+KDgnYV/bBy4jqpVreXmWAJXl\nfyjG3eLWBrtrsI9YX6zeAA==\n-----END PRIVATE KEY-----\n","privateCertContent":"-----BEGIN CERTIFICATE-----\nMIID6TCCAtGgAwIBAgIUNkEHq6aQcF80NSYqWS58ybsJzI4wDQYJKoZIhvcNAQEL\nBQAwXjELMAkGA1UEBhMCQ04xEzARBgNVBAoTClRlbnBheS5jb20xHTAbBgNVBAsT\nFFRlbnBheS5jb20gQ0EgQ2VudGVyMRswGQYDVQQDExJUZW5wYXkuY29tIFJvb3Qg\nQ0EwHhcNMjExMDIxMDU0NTQxWhcNMjYxMDIwMDU0NTQxWjB7MRMwEQYDVQQDDAox\nNTQ1MDgzODgxMRswGQYDVQQKDBLlvq7kv6HllYbmiLfns7vnu58xJzAlBgNVBAsM\nHuWOhuWfjuWMuuWkp+adjuWwp+aXpeeUqOWTgeW6lzELMAkGA1UEBgwCQ04xETAP\nBgNVBAcMCFNoZW5aaGVuMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA\nuatoWBN5aDkKB5f9pIf3qbo9e1vFaVebwZe8rCVgGwV7seppauICkxw9EFEHYirQ\nxFJv2ygvQYulVLpJTOWPKAmgm4Y5JR0WxE/I7IHfaYUjRr4h3JWES7EETvKF5zfP\nZDdMuw2CptIWHZmoktdmd7/FWiJkk6+B1Xh5Qv890upncEq9ut7LSrfd9WE3G04/\n0BR+857L/TWtLl+i2jARBALCXLVnZvDXa3dbRofRc39AwOVUV7CUd5JFrKcviMIg\n0z1fxQyjlPTSQksTJg3j3q035KGdrNa5bagStMpAya17cUfoU8xLCmfyqTYvLcmK\nXzWbL4hyJVLCzxSeVL1IaQIDAQABo4GBMH8wCQYDVR0TBAIwADALBgNVHQ8EBAMC\nBPAwZQYDVR0fBF4wXDBaoFigVoZUaHR0cDovL2V2Y2EuaXRydXMuY29tLmNuL3B1\nYmxpYy9pdHJ1c2NybD9DQT0xQkQ0MjIwRTUwREJDMDRCMDZBRDM5NzU0OTg0NkMw\nMUMzRThFQkQyMA0GCSqGSIb3DQEBCwUAA4IBAQBe7XgncAY/1PLbCsnMsYt11k3V\n2cdNZ+yuCxhlOEKk3nHE6WCTL6zL0qWlTKKpnw1rE/+4OS76Tg72wWXcHfHDAOgt\n9icp62cKx1WO3QweeZpSvLDmtdLgKKrqeIWh+rL8+ZhuAOxSkaRwcsMTWDaLeDOi\n0pGeqvfG8WNhPxkkaSI8xbiTK641Yg9WT/Q4yfHS7Q6wg1dj9YQdo0dvVB0S2Nir\nX9IK6PUaHDnQeFKDmKgLkDGLaKaiijEvC91wMEE6qB8b0eNhciaxq2YhGHcFmSRP\nWUyc5CmBadt7wIOH5Z3bfuwWGxqxKjNw/baM/d+nk7hlDr01YL9c0g16B9MW\n-----END CERTIFICATE-----\n","apiV3Key":"joerVi8y5DJ3o4ttA0o1uH47Xz1u2Ase"}', NULL, N'2021-12-14 22:06:10.0000000', NULL, N'2022-02-27 04:15:09.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[pay_channel] ([id], [code], [status], [remark], [fee_rate], [merchant_id], [app_id], [config], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'13', N'wx_pub', N'0', NULL, N'1', N'1', N'6', N'{"@class":"cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXPayClientConfig","appId":"wx041349c6f39b268b","mchId":"1545083881","apiVersion":"v2","mchKey":"0alL64UDQdlCwiKZ73ib7ypaIjMns06p","privateKeyContent":"-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC5q2hYE3loOQoH\nl/2kh/epuj17W8VpV5vBl7ysJWAbBXux6mlq4gKTHD0QUQdiKtDEUm/bKC9Bi6VU\nuklM5Y8oCaCbhjklHRbET8jsgd9phSNGviHclYRLsQRO8oXnN89kN0y7DYKm0hYd\nmaiS12Z3v8VaImSTr4HVeHlC/z3S6mdwSr263stKt931YTcbTj/QFH7znsv9Na0u\nX6LaMBEEAsJctWdm8Ndrd1tGh9Fzf0DA5VRXsJR3kkWspy+IwiDTPV/FDKOU9NJC\nSxMmDePerTfkoZ2s1rltqBK0ykDJrXtxR+hTzEsKZ/KpNi8tyYpfNZsviHIlUsLP\nFJ5UvUhpAgMBAAECggEAd90NltazqTIxpGdeCwrwOzWNnYbIclJprlhMKIJUgf1P\nNrPTbHoOGXTAgzkcYCat8iAaMEzH/TOu/3zn92m3uqxEcEL9v1UBLqknWHAbkB6w\ngGocqDAqYUcdNe5hvbyM+fCta5C0SQgV2PQrHOlMMICwYpkTfzhtxCdreXIYMoGg\nJEIRkZWgrm/N7LTtNgizznuUjy6OURWjXaWKPcs3b3j6G1gLj9Vp++z4y0u51nqM\n4R6fcvl8M6BjlcC8zo6DbOvCW8cXtuXsnru+2TPrUnsGeybJok4fEQsfW1BvpvPo\nief38rYJn4OWxIrHcpWrhNtXtgRPeiMGFfIsEQDmVQKBgQDzXK6Nn3Nr3TFfGVTy\n8QYrzOuY2NqzH8nnsLL6qn3HoKxTv+PcFKOTPsi6f4hIYCzBP0esRrPv0ffMu9oQ\nJvFtCJvMmcKGtp0Q5hcj0y/XkbC3AWuahJtBv8lhKXVnQXSL0j3+ombljw4/8yN0\nAzgBz+j/skQQgZ3sN5h+DHGtgwKBgQDDT784/2pd4m86c/uBmrwYfqu6MJo0eHJh\n1XPtE+u8pOHyNTFk77rKobKDqN5VlrF0uEmBc/08LKhyxJ3vh/zAbcmqT1Mq778y\nAKKUtVmvcaVDrvSQHsnhj0zt4SHGmmU34U2b9hV+nocq5VszX6/jp//HJi9bs3We\ndAzfFCmaowKBgC1MmDVGc+6lCraf+X8LPFHU4Bnga70h8qxM6NPd/nG1R76DHn/t\n25DiA+0rJgwK0unZxJadxoqic9TJNssA5Lmd+5o3GM2Imm311mLVwbcHqHQ4MHZf\nrqKrd2m9lNv2hCIurVmDk1Gxsj5XHMdQfhFgSQengCHubp30r07vNA3PAoGAUEAE\nIjdQTSMs8KeXP7mEb8wcY3R05/pVhT1fVJpK0kgtTofss7yM05V88/v+3sv8Pik6\niqZN9tuimwWOn00Q3UA/DGtrkMjRlooMQ24AW8YmUZkhg9YivTtUMKnAZwopbLx2\nVw7V5iDdCRMUVheK/c+ZmQpnixZBzcmBQGfYcGECgYBjEq3Mem+Aw6pXOu6+0FwH\n9y6Xi4HhBkq0OOZZuXFtWVry7YrD3pBgzWVAZJqJCkyPKKZzCzwdbFd3u0lYBs35\nzYgx7ug4hR+wfI980a3vxjcWGOqnOUUnUJ7ucIa+KDgnYV/bBy4jqpVreXmWAJXl\nfyjG3eLWBrtrsI9YX6zeAA==\n-----END PRIVATE KEY-----\n","privateCertContent":"-----BEGIN CERTIFICATE-----\nMIID6TCCAtGgAwIBAgIUNkEHq6aQcF80NSYqWS58ybsJzI4wDQYJKoZIhvcNAQEL\nBQAwXjELMAkGA1UEBhMCQ04xEzARBgNVBAoTClRlbnBheS5jb20xHTAbBgNVBAsT\nFFRlbnBheS5jb20gQ0EgQ2VudGVyMRswGQYDVQQDExJUZW5wYXkuY29tIFJvb3Qg\nQ0EwHhcNMjExMDIxMDU0NTQxWhcNMjYxMDIwMDU0NTQxWjB7MRMwEQYDVQQDDAox\nNTQ1MDgzODgxMRswGQYDVQQKDBLlvq7kv6HllYbmiLfns7vnu58xJzAlBgNVBAsM\nHuWOhuWfjuWMuuWkp+adjuWwp+aXpeeUqOWTgeW6lzELMAkGA1UEBgwCQ04xETAP\nBgNVBAcMCFNoZW5aaGVuMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA\nuatoWBN5aDkKB5f9pIf3qbo9e1vFaVebwZe8rCVgGwV7seppauICkxw9EFEHYirQ\nxFJv2ygvQYulVLpJTOWPKAmgm4Y5JR0WxE/I7IHfaYUjRr4h3JWES7EETvKF5zfP\nZDdMuw2CptIWHZmoktdmd7/FWiJkk6+B1Xh5Qv890upncEq9ut7LSrfd9WE3G04/\n0BR+857L/TWtLl+i2jARBALCXLVnZvDXa3dbRofRc39AwOVUV7CUd5JFrKcviMIg\n0z1fxQyjlPTSQksTJg3j3q035KGdrNa5bagStMpAya17cUfoU8xLCmfyqTYvLcmK\nXzWbL4hyJVLCzxSeVL1IaQIDAQABo4GBMH8wCQYDVR0TBAIwADALBgNVHQ8EBAMC\nBPAwZQYDVR0fBF4wXDBaoFigVoZUaHR0cDovL2V2Y2EuaXRydXMuY29tLmNuL3B1\nYmxpYy9pdHJ1c2NybD9DQT0xQkQ0MjIwRTUwREJDMDRCMDZBRDM5NzU0OTg0NkMw\nMUMzRThFQkQyMA0GCSqGSIb3DQEBCwUAA4IBAQBe7XgncAY/1PLbCsnMsYt11k3V\n2cdNZ+yuCxhlOEKk3nHE6WCTL6zL0qWlTKKpnw1rE/+4OS76Tg72wWXcHfHDAOgt\n9icp62cKx1WO3QweeZpSvLDmtdLgKKrqeIWh+rL8+ZhuAOxSkaRwcsMTWDaLeDOi\n0pGeqvfG8WNhPxkkaSI8xbiTK641Yg9WT/Q4yfHS7Q6wg1dj9YQdo0dvVB0S2Nir\nX9IK6PUaHDnQeFKDmKgLkDGLaKaiijEvC91wMEE6qB8b0eNhciaxq2YhGHcFmSRP\nWUyc5CmBadt7wIOH5Z3bfuwWGxqxKjNw/baM/d+nk7hlDr01YL9c0g16B9MW\n-----END CERTIFICATE-----\n","apiV3Key":"joerVi8y5DJ3o4ttA0o1uH47Xz1u2Ase"}', NULL, N'2021-12-14 22:09:39.0000000', NULL, N'2022-02-27 04:15:08.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[pay_channel] ([id], [code], [status], [remark], [fee_rate], [merchant_id], [app_id], [config], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'14', N'wx_pub', N'0', NULL, N'1', N'1', N'6', N'{"@class":"cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXPayClientConfig","appId":"wx041349c6f39b268b","mchId":"1545083881","apiVersion":"v2","mchKey":"0alL64UDQdlCwiKZ73ib7ypaIjMns06p","privateKeyContent":"-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC5q2hYE3loOQoH\nl/2kh/epuj17W8VpV5vBl7ysJWAbBXux6mlq4gKTHD0QUQdiKtDEUm/bKC9Bi6VU\nuklM5Y8oCaCbhjklHRbET8jsgd9phSNGviHclYRLsQRO8oXnN89kN0y7DYKm0hYd\nmaiS12Z3v8VaImSTr4HVeHlC/z3S6mdwSr263stKt931YTcbTj/QFH7znsv9Na0u\nX6LaMBEEAsJctWdm8Ndrd1tGh9Fzf0DA5VRXsJR3kkWspy+IwiDTPV/FDKOU9NJC\nSxMmDePerTfkoZ2s1rltqBK0ykDJrXtxR+hTzEsKZ/KpNi8tyYpfNZsviHIlUsLP\nFJ5UvUhpAgMBAAECggEAd90NltazqTIxpGdeCwrwOzWNnYbIclJprlhMKIJUgf1P\nNrPTbHoOGXTAgzkcYCat8iAaMEzH/TOu/3zn92m3uqxEcEL9v1UBLqknWHAbkB6w\ngGocqDAqYUcdNe5hvbyM+fCta5C0SQgV2PQrHOlMMICwYpkTfzhtxCdreXIYMoGg\nJEIRkZWgrm/N7LTtNgizznuUjy6OURWjXaWKPcs3b3j6G1gLj9Vp++z4y0u51nqM\n4R6fcvl8M6BjlcC8zo6DbOvCW8cXtuXsnru+2TPrUnsGeybJok4fEQsfW1BvpvPo\nief38rYJn4OWxIrHcpWrhNtXtgRPeiMGFfIsEQDmVQKBgQDzXK6Nn3Nr3TFfGVTy\n8QYrzOuY2NqzH8nnsLL6qn3HoKxTv+PcFKOTPsi6f4hIYCzBP0esRrPv0ffMu9oQ\nJvFtCJvMmcKGtp0Q5hcj0y/XkbC3AWuahJtBv8lhKXVnQXSL0j3+ombljw4/8yN0\nAzgBz+j/skQQgZ3sN5h+DHGtgwKBgQDDT784/2pd4m86c/uBmrwYfqu6MJo0eHJh\n1XPtE+u8pOHyNTFk77rKobKDqN5VlrF0uEmBc/08LKhyxJ3vh/zAbcmqT1Mq778y\nAKKUtVmvcaVDrvSQHsnhj0zt4SHGmmU34U2b9hV+nocq5VszX6/jp//HJi9bs3We\ndAzfFCmaowKBgC1MmDVGc+6lCraf+X8LPFHU4Bnga70h8qxM6NPd/nG1R76DHn/t\n25DiA+0rJgwK0unZxJadxoqic9TJNssA5Lmd+5o3GM2Imm311mLVwbcHqHQ4MHZf\nrqKrd2m9lNv2hCIurVmDk1Gxsj5XHMdQfhFgSQengCHubp30r07vNA3PAoGAUEAE\nIjdQTSMs8KeXP7mEb8wcY3R05/pVhT1fVJpK0kgtTofss7yM05V88/v+3sv8Pik6\niqZN9tuimwWOn00Q3UA/DGtrkMjRlooMQ24AW8YmUZkhg9YivTtUMKnAZwopbLx2\nVw7V5iDdCRMUVheK/c+ZmQpnixZBzcmBQGfYcGECgYBjEq3Mem+Aw6pXOu6+0FwH\n9y6Xi4HhBkq0OOZZuXFtWVry7YrD3pBgzWVAZJqJCkyPKKZzCzwdbFd3u0lYBs35\nzYgx7ug4hR+wfI980a3vxjcWGOqnOUUnUJ7ucIa+KDgnYV/bBy4jqpVreXmWAJXl\nfyjG3eLWBrtrsI9YX6zeAA==\n-----END PRIVATE KEY-----\n","privateCertContent":"-----BEGIN CERTIFICATE-----\nMIID6TCCAtGgAwIBAgIUNkEHq6aQcF80NSYqWS58ybsJzI4wDQYJKoZIhvcNAQEL\nBQAwXjELMAkGA1UEBhMCQ04xEzARBgNVBAoTClRlbnBheS5jb20xHTAbBgNVBAsT\nFFRlbnBheS5jb20gQ0EgQ2VudGVyMRswGQYDVQQDExJUZW5wYXkuY29tIFJvb3Qg\nQ0EwHhcNMjExMDIxMDU0NTQxWhcNMjYxMDIwMDU0NTQxWjB7MRMwEQYDVQQDDAox\nNTQ1MDgzODgxMRswGQYDVQQKDBLlvq7kv6HllYbmiLfns7vnu58xJzAlBgNVBAsM\nHuWOhuWfjuWMuuWkp+adjuWwp+aXpeeUqOWTgeW6lzELMAkGA1UEBgwCQ04xETAP\nBgNVBAcMCFNoZW5aaGVuMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA\nuatoWBN5aDkKB5f9pIf3qbo9e1vFaVebwZe8rCVgGwV7seppauICkxw9EFEHYirQ\nxFJv2ygvQYulVLpJTOWPKAmgm4Y5JR0WxE/I7IHfaYUjRr4h3JWES7EETvKF5zfP\nZDdMuw2CptIWHZmoktdmd7/FWiJkk6+B1Xh5Qv890upncEq9ut7LSrfd9WE3G04/\n0BR+857L/TWtLl+i2jARBALCXLVnZvDXa3dbRofRc39AwOVUV7CUd5JFrKcviMIg\n0z1fxQyjlPTSQksTJg3j3q035KGdrNa5bagStMpAya17cUfoU8xLCmfyqTYvLcmK\nXzWbL4hyJVLCzxSeVL1IaQIDAQABo4GBMH8wCQYDVR0TBAIwADALBgNVHQ8EBAMC\nBPAwZQYDVR0fBF4wXDBaoFigVoZUaHR0cDovL2V2Y2EuaXRydXMuY29tLmNuL3B1\nYmxpYy9pdHJ1c2NybD9DQT0xQkQ0MjIwRTUwREJDMDRCMDZBRDM5NzU0OTg0NkMw\nMUMzRThFQkQyMA0GCSqGSIb3DQEBCwUAA4IBAQBe7XgncAY/1PLbCsnMsYt11k3V\n2cdNZ+yuCxhlOEKk3nHE6WCTL6zL0qWlTKKpnw1rE/+4OS76Tg72wWXcHfHDAOgt\n9icp62cKx1WO3QweeZpSvLDmtdLgKKrqeIWh+rL8+ZhuAOxSkaRwcsMTWDaLeDOi\n0pGeqvfG8WNhPxkkaSI8xbiTK641Yg9WT/Q4yfHS7Q6wg1dj9YQdo0dvVB0S2Nir\nX9IK6PUaHDnQeFKDmKgLkDGLaKaiijEvC91wMEE6qB8b0eNhciaxq2YhGHcFmSRP\nWUyc5CmBadt7wIOH5Z3bfuwWGxqxKjNw/baM/d+nk7hlDr01YL9c0g16B9MW\n-----END CERTIFICATE-----\n","apiV3Key":"joerVi8y5DJ3o4ttA0o1uH47Xz1u2Ase"}', NULL, N'2021-12-14 22:38:49.0000000', NULL, N'2022-02-27 04:15:05.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[pay_channel] ([id], [code], [status], [remark], [fee_rate], [merchant_id], [app_id], [config], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'15', N'wx_pub', N'0', NULL, N'1', N'1', N'6', N'{"@class":"cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXPayClientConfig","appId":"wx041349c6f39b268b","mchId":"1545083881","apiVersion":"v2","mchKey":"0alL64UDQdlCwiKZ73ib7ypaIjMns06p","privateKeyContent":"-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC5q2hYE3loOQoH\nl/2kh/epuj17W8VpV5vBl7ysJWAbBXux6mlq4gKTHD0QUQdiKtDEUm/bKC9Bi6VU\nuklM5Y8oCaCbhjklHRbET8jsgd9phSNGviHclYRLsQRO8oXnN89kN0y7DYKm0hYd\nmaiS12Z3v8VaImSTr4HVeHlC/z3S6mdwSr263stKt931YTcbTj/QFH7znsv9Na0u\nX6LaMBEEAsJctWdm8Ndrd1tGh9Fzf0DA5VRXsJR3kkWspy+IwiDTPV/FDKOU9NJC\nSxMmDePerTfkoZ2s1rltqBK0ykDJrXtxR+hTzEsKZ/KpNi8tyYpfNZsviHIlUsLP\nFJ5UvUhpAgMBAAECggEAd90NltazqTIxpGdeCwrwOzWNnYbIclJprlhMKIJUgf1P\nNrPTbHoOGXTAgzkcYCat8iAaMEzH/TOu/3zn92m3uqxEcEL9v1UBLqknWHAbkB6w\ngGocqDAqYUcdNe5hvbyM+fCta5C0SQgV2PQrHOlMMICwYpkTfzhtxCdreXIYMoGg\nJEIRkZWgrm/N7LTtNgizznuUjy6OURWjXaWKPcs3b3j6G1gLj9Vp++z4y0u51nqM\n4R6fcvl8M6BjlcC8zo6DbOvCW8cXtuXsnru+2TPrUnsGeybJok4fEQsfW1BvpvPo\nief38rYJn4OWxIrHcpWrhNtXtgRPeiMGFfIsEQDmVQKBgQDzXK6Nn3Nr3TFfGVTy\n8QYrzOuY2NqzH8nnsLL6qn3HoKxTv+PcFKOTPsi6f4hIYCzBP0esRrPv0ffMu9oQ\nJvFtCJvMmcKGtp0Q5hcj0y/XkbC3AWuahJtBv8lhKXVnQXSL0j3+ombljw4/8yN0\nAzgBz+j/skQQgZ3sN5h+DHGtgwKBgQDDT784/2pd4m86c/uBmrwYfqu6MJo0eHJh\n1XPtE+u8pOHyNTFk77rKobKDqN5VlrF0uEmBc/08LKhyxJ3vh/zAbcmqT1Mq778y\nAKKUtVmvcaVDrvSQHsnhj0zt4SHGmmU34U2b9hV+nocq5VszX6/jp//HJi9bs3We\ndAzfFCmaowKBgC1MmDVGc+6lCraf+X8LPFHU4Bnga70h8qxM6NPd/nG1R76DHn/t\n25DiA+0rJgwK0unZxJadxoqic9TJNssA5Lmd+5o3GM2Imm311mLVwbcHqHQ4MHZf\nrqKrd2m9lNv2hCIurVmDk1Gxsj5XHMdQfhFgSQengCHubp30r07vNA3PAoGAUEAE\nIjdQTSMs8KeXP7mEb8wcY3R05/pVhT1fVJpK0kgtTofss7yM05V88/v+3sv8Pik6\niqZN9tuimwWOn00Q3UA/DGtrkMjRlooMQ24AW8YmUZkhg9YivTtUMKnAZwopbLx2\nVw7V5iDdCRMUVheK/c+ZmQpnixZBzcmBQGfYcGECgYBjEq3Mem+Aw6pXOu6+0FwH\n9y6Xi4HhBkq0OOZZuXFtWVry7YrD3pBgzWVAZJqJCkyPKKZzCzwdbFd3u0lYBs35\nzYgx7ug4hR+wfI980a3vxjcWGOqnOUUnUJ7ucIa+KDgnYV/bBy4jqpVreXmWAJXl\nfyjG3eLWBrtrsI9YX6zeAA==\n-----END PRIVATE KEY-----\n","privateCertContent":"-----BEGIN CERTIFICATE-----\nMIID6TCCAtGgAwIBAgIUNkEHq6aQcF80NSYqWS58ybsJzI4wDQYJKoZIhvcNAQEL\nBQAwXjELMAkGA1UEBhMCQ04xEzARBgNVBAoTClRlbnBheS5jb20xHTAbBgNVBAsT\nFFRlbnBheS5jb20gQ0EgQ2VudGVyMRswGQYDVQQDExJUZW5wYXkuY29tIFJvb3Qg\nQ0EwHhcNMjExMDIxMDU0NTQxWhcNMjYxMDIwMDU0NTQxWjB7MRMwEQYDVQQDDAox\nNTQ1MDgzODgxMRswGQYDVQQKDBLlvq7kv6HllYbmiLfns7vnu58xJzAlBgNVBAsM\nHuWOhuWfjuWMuuWkp+adjuWwp+aXpeeUqOWTgeW6lzELMAkGA1UEBgwCQ04xETAP\nBgNVBAcMCFNoZW5aaGVuMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA\nuatoWBN5aDkKB5f9pIf3qbo9e1vFaVebwZe8rCVgGwV7seppauICkxw9EFEHYirQ\nxFJv2ygvQYulVLpJTOWPKAmgm4Y5JR0WxE/I7IHfaYUjRr4h3JWES7EETvKF5zfP\nZDdMuw2CptIWHZmoktdmd7/FWiJkk6+B1Xh5Qv890upncEq9ut7LSrfd9WE3G04/\n0BR+857L/TWtLl+i2jARBALCXLVnZvDXa3dbRofRc39AwOVUV7CUd5JFrKcviMIg\n0z1fxQyjlPTSQksTJg3j3q035KGdrNa5bagStMpAya17cUfoU8xLCmfyqTYvLcmK\nXzWbL4hyJVLCzxSeVL1IaQIDAQABo4GBMH8wCQYDVR0TBAIwADALBgNVHQ8EBAMC\nBPAwZQYDVR0fBF4wXDBaoFigVoZUaHR0cDovL2V2Y2EuaXRydXMuY29tLmNuL3B1\nYmxpYy9pdHJ1c2NybD9DQT0xQkQ0MjIwRTUwREJDMDRCMDZBRDM5NzU0OTg0NkMw\nMUMzRThFQkQyMA0GCSqGSIb3DQEBCwUAA4IBAQBe7XgncAY/1PLbCsnMsYt11k3V\n2cdNZ+yuCxhlOEKk3nHE6WCTL6zL0qWlTKKpnw1rE/+4OS76Tg72wWXcHfHDAOgt\n9icp62cKx1WO3QweeZpSvLDmtdLgKKrqeIWh+rL8+ZhuAOxSkaRwcsMTWDaLeDOi\n0pGeqvfG8WNhPxkkaSI8xbiTK641Yg9WT/Q4yfHS7Q6wg1dj9YQdo0dvVB0S2Nir\nX9IK6PUaHDnQeFKDmKgLkDGLaKaiijEvC91wMEE6qB8b0eNhciaxq2YhGHcFmSRP\nWUyc5CmBadt7wIOH5Z3bfuwWGxqxKjNw/baM/d+nk7hlDr01YL9c0g16B9MW\n-----END CERTIFICATE-----\n","apiV3Key":"joerVi8y5DJ3o4ttA0o1uH47Xz1u2Ase"}', NULL, N'2021-12-15 09:32:26.0000000', NULL, N'2022-02-27 04:15:04.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[pay_channel] ([id], [code], [status], [remark], [fee_rate], [merchant_id], [app_id], [config], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'16', N'wx_pub', N'0', NULL, N'1', N'1', N'6', N'{"@class":"cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXPayClientConfig","appId":"wx041349c6f39b268b","mchId":"1545083881","apiVersion":"v2","mchKey":"0alL64UDQdlCwiKZ73ib7ypaIjMns06p","privateKeyContent":"-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC5q2hYE3loOQoH\nl/2kh/epuj17W8VpV5vBl7ysJWAbBXux6mlq4gKTHD0QUQdiKtDEUm/bKC9Bi6VU\nuklM5Y8oCaCbhjklHRbET8jsgd9phSNGviHclYRLsQRO8oXnN89kN0y7DYKm0hYd\nmaiS12Z3v8VaImSTr4HVeHlC/z3S6mdwSr263stKt931YTcbTj/QFH7znsv9Na0u\nX6LaMBEEAsJctWdm8Ndrd1tGh9Fzf0DA5VRXsJR3kkWspy+IwiDTPV/FDKOU9NJC\nSxMmDePerTfkoZ2s1rltqBK0ykDJrXtxR+hTzEsKZ/KpNi8tyYpfNZsviHIlUsLP\nFJ5UvUhpAgMBAAECggEAd90NltazqTIxpGdeCwrwOzWNnYbIclJprlhMKIJUgf1P\nNrPTbHoOGXTAgzkcYCat8iAaMEzH/TOu/3zn92m3uqxEcEL9v1UBLqknWHAbkB6w\ngGocqDAqYUcdNe5hvbyM+fCta5C0SQgV2PQrHOlMMICwYpkTfzhtxCdreXIYMoGg\nJEIRkZWgrm/N7LTtNgizznuUjy6OURWjXaWKPcs3b3j6G1gLj9Vp++z4y0u51nqM\n4R6fcvl8M6BjlcC8zo6DbOvCW8cXtuXsnru+2TPrUnsGeybJok4fEQsfW1BvpvPo\nief38rYJn4OWxIrHcpWrhNtXtgRPeiMGFfIsEQDmVQKBgQDzXK6Nn3Nr3TFfGVTy\n8QYrzOuY2NqzH8nnsLL6qn3HoKxTv+PcFKOTPsi6f4hIYCzBP0esRrPv0ffMu9oQ\nJvFtCJvMmcKGtp0Q5hcj0y/XkbC3AWuahJtBv8lhKXVnQXSL0j3+ombljw4/8yN0\nAzgBz+j/skQQgZ3sN5h+DHGtgwKBgQDDT784/2pd4m86c/uBmrwYfqu6MJo0eHJh\n1XPtE+u8pOHyNTFk77rKobKDqN5VlrF0uEmBc/08LKhyxJ3vh/zAbcmqT1Mq778y\nAKKUtVmvcaVDrvSQHsnhj0zt4SHGmmU34U2b9hV+nocq5VszX6/jp//HJi9bs3We\ndAzfFCmaowKBgC1MmDVGc+6lCraf+X8LPFHU4Bnga70h8qxM6NPd/nG1R76DHn/t\n25DiA+0rJgwK0unZxJadxoqic9TJNssA5Lmd+5o3GM2Imm311mLVwbcHqHQ4MHZf\nrqKrd2m9lNv2hCIurVmDk1Gxsj5XHMdQfhFgSQengCHubp30r07vNA3PAoGAUEAE\nIjdQTSMs8KeXP7mEb8wcY3R05/pVhT1fVJpK0kgtTofss7yM05V88/v+3sv8Pik6\niqZN9tuimwWOn00Q3UA/DGtrkMjRlooMQ24AW8YmUZkhg9YivTtUMKnAZwopbLx2\nVw7V5iDdCRMUVheK/c+ZmQpnixZBzcmBQGfYcGECgYBjEq3Mem+Aw6pXOu6+0FwH\n9y6Xi4HhBkq0OOZZuXFtWVry7YrD3pBgzWVAZJqJCkyPKKZzCzwdbFd3u0lYBs35\nzYgx7ug4hR+wfI980a3vxjcWGOqnOUUnUJ7ucIa+KDgnYV/bBy4jqpVreXmWAJXl\nfyjG3eLWBrtrsI9YX6zeAA==\n-----END PRIVATE KEY-----\n","privateCertContent":"-----BEGIN CERTIFICATE-----\nMIID6TCCAtGgAwIBAgIUNkEHq6aQcF80NSYqWS58ybsJzI4wDQYJKoZIhvcNAQEL\nBQAwXjELMAkGA1UEBhMCQ04xEzARBgNVBAoTClRlbnBheS5jb20xHTAbBgNVBAsT\nFFRlbnBheS5jb20gQ0EgQ2VudGVyMRswGQYDVQQDExJUZW5wYXkuY29tIFJvb3Qg\nQ0EwHhcNMjExMDIxMDU0NTQxWhcNMjYxMDIwMDU0NTQxWjB7MRMwEQYDVQQDDAox\nNTQ1MDgzODgxMRswGQYDVQQKDBLlvq7kv6HllYbmiLfns7vnu58xJzAlBgNVBAsM\nHuWOhuWfjuWMuuWkp+adjuWwp+aXpeeUqOWTgeW6lzELMAkGA1UEBgwCQ04xETAP\nBgNVBAcMCFNoZW5aaGVuMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA\nuatoWBN5aDkKB5f9pIf3qbo9e1vFaVebwZe8rCVgGwV7seppauICkxw9EFEHYirQ\nxFJv2ygvQYulVLpJTOWPKAmgm4Y5JR0WxE/I7IHfaYUjRr4h3JWES7EETvKF5zfP\nZDdMuw2CptIWHZmoktdmd7/FWiJkk6+B1Xh5Qv890upncEq9ut7LSrfd9WE3G04/\n0BR+857L/TWtLl+i2jARBALCXLVnZvDXa3dbRofRc39AwOVUV7CUd5JFrKcviMIg\n0z1fxQyjlPTSQksTJg3j3q035KGdrNa5bagStMpAya17cUfoU8xLCmfyqTYvLcmK\nXzWbL4hyJVLCzxSeVL1IaQIDAQABo4GBMH8wCQYDVR0TBAIwADALBgNVHQ8EBAMC\nBPAwZQYDVR0fBF4wXDBaoFigVoZUaHR0cDovL2V2Y2EuaXRydXMuY29tLmNuL3B1\nYmxpYy9pdHJ1c2NybD9DQT0xQkQ0MjIwRTUwREJDMDRCMDZBRDM5NzU0OTg0NkMw\nMUMzRThFQkQyMA0GCSqGSIb3DQEBCwUAA4IBAQBe7XgncAY/1PLbCsnMsYt11k3V\n2cdNZ+yuCxhlOEKk3nHE6WCTL6zL0qWlTKKpnw1rE/+4OS76Tg72wWXcHfHDAOgt\n9icp62cKx1WO3QweeZpSvLDmtdLgKKrqeIWh+rL8+ZhuAOxSkaRwcsMTWDaLeDOi\n0pGeqvfG8WNhPxkkaSI8xbiTK641Yg9WT/Q4yfHS7Q6wg1dj9YQdo0dvVB0S2Nir\nX9IK6PUaHDnQeFKDmKgLkDGLaKaiijEvC91wMEE6qB8b0eNhciaxq2YhGHcFmSRP\nWUyc5CmBadt7wIOH5Z3bfuwWGxqxKjNw/baM/d+nk7hlDr01YL9c0g16B9MW\n-----END CERTIFICATE-----\n","apiV3Key":"joerVi8y5DJ3o4ttA0o1uH47Xz1u2Ase"}', NULL, N'2022-01-31 22:13:25.0000000', NULL, N'2022-02-27 04:15:03.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[pay_channel] ([id], [code], [status], [remark], [fee_rate], [merchant_id], [app_id], [config], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'17', N'alipay_qr', N'0', NULL, N'1', N'1', N'6', N'{"@class":"cn.iocoder.yudao.framework.pay.core.client.impl.alipay.AlipayPayClientConfig","serverUrl":"https://openapi.alipaydev.com/gateway.do","appId":"2021000118634035","signType":"RSA2","mode":null,"privateKey":"MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCHsEV1cDupwJv890x84qbppUtRIfhaKSwSVN0thCcsDCaAsGR5MZslDkO8NCT9V4r2SVXjyY7eJUZlZd1M0C8T01Tg4UOx5LUbic0O3A1uJMy6V1n9IyYwbAW3AEZhBd5bSbPgrqvmv3NeWSTQT6Anxnllf+2iDH6zyA2fPl7cYyQtbZoDJQFGqr4F+cGh2R6akzRKNoBkAeMYwoY6es2lX8sJxCVPWUmxNUoL3tScwlSpd7Bxw0q9c/X01jMwuQ0+Va358zgFiGERTE6yD01eu40OBDXOYO3z++y+TAYHlQQ2toMO63trepo88X3xV3R44/1DH+k2pAm2IF5ixiLrAgMBAAECggEAPx3SoXcseaD7rmcGcE0p4SMfbsUDdkUSmBBbtfF0GzwnqNLkWa+mgE0rWt9SmXngTQH97vByAYmLPl1s3G82ht1V7Sk7yQMe74lhFllr8eEyTjeVx3dTK1EEM4TwN+936DTXdFsr4TELJEcJJdD0KaxcCcfBLRDs2wnitEFZ9N+GoZybVmY8w0e0MI7PLObUZ2l0X4RurQnfG9ZxjXjC7PkeMVv7cGGylpNFi3BbvkRhdhLPDC2E6wqnr9e7zk+hiENivAezXrtxtwKovzCtnWJ1r0IO14Rh47H509Ic0wFnj+o5YyUL4LdmpL7yaaH6fM7zcSLFjNZPHvZCKPwYcQKBgQDQFho98QvnL8ex4v6cry4VitGpjSXm1qP3vmMQk4rTsn8iPWtcxPjqGEqOQJjdi4Mi0VZKQOLFwlH0kl95wNrD/isJ4O1yeYfX7YAXApzHqYNINzM79HemO3Yx1qLMW3okRFJ9pPRzbQ9qkTpsaegsmyX316zOBhzGRYjKbutTYwKBgQCm7phr9XdFW5Vh+XR90mVs483nrLmMiDKg7YKxSLJ8amiDjzPejCn7i95Hah08P+2MIZLIPbh2VLacczR6ltRRzN5bg5etFuqSgfkuHyxpoDmpjbe08+Q2h8JBYqcC5Nhv1AKU4iOUhVLHo/FBAQliMcGc/J3eiYTFC7EsNx382QKBgClb20doe7cttgFTXswBvaUmfFm45kmla924B7SpvrQpDD/f+VDtDZRp05fGmxuduSjYdtA3aVtpLiTwWu22OUUvZZqHDGruYOO4Hvdz23mL5b4ayqImCwoNU4bAZIc9v18p/UNf3/55NNE3oGcf/bev9rH2OjCQ4nM+Ktwhg8CFAoGACSgvbkShzUkv0ZcIf9ppu+ZnJh1AdGgINvGwaJ8vQ0nm/8h8NOoFZ4oNoGc+wU5Ubops7dUM6FjPR5e+OjdJ4E7Xp7d5O4J1TaIZlCEbo5OpdhaTDDcQvrkFu+Z4eN0qzj+YAKjDAOOrXc4tbr5q0FsgXscwtcNfaBuzFVTUrUkCgYEAwzPnMNhWG3zOWLUs2QFA2GP4Y+J8cpUYfj6pbKKzeLwyG9qBwF1NJpN8m+q9q7V9P2LY+9Lp9e1mGsGeqt5HMEA3P6vIpcqLJLqE/4PBLLRzfccTcmqb1m71+erxTRhHBRkGS+I7dZEb3olQfnS1Y1tpMBxiwYwR3LW4oXuJwj8=","alipayPublicKey":"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnq90KnF4dTnlzzmxpujbI05OYqi5WxAS6cL0gnZFv2gK51HExF8v/BaP7P979PhFMgWTqmOOI+Dtno5s+yD09XTY1WkshbLk6i4g2Xlr8fyW9ODnkU88RI2w9UdPhQU4cPPwBNlrsYhKkVK2OxwM3kFqjoBBY0CZoZCsSQ3LDH5WeZqPArlsS6xa2zqJBuuoKjMrdpELl3eXSjP8K54eDJCbeetCZNKWLL3DPahTPB7LZikfYmslb0QUvCgGapD0xkS7eVq70NaL1G57MWABs4tbfWgxike4Daj3EfUrzIVspQxj7w8HEj9WozJPgL88kSJSits0pqD3n5r8HSuseQIDAQAB","appCertContent":null,"alipayPublicCertContent":null,"rootCertContent":null}', NULL, N'2022-01-31 22:13:25.0000000', NULL, N'2022-02-27 04:15:02.0000000', N'1', N'0')
GO

SET IDENTITY_INSERT [dbo].[pay_channel] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for pay_merchant
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[pay_merchant]') AND type IN ('U'))
	DROP TABLE [dbo].[pay_merchant]
GO

CREATE TABLE [dbo].[pay_merchant] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [no] nvarchar(32) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [name] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [short_name] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [status] tinyint  NOT NULL,
  [remark] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[pay_merchant] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'商户编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_merchant',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商户号',
'SCHEMA', N'dbo',
'TABLE', N'pay_merchant',
'COLUMN', N'no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商户全称',
'SCHEMA', N'dbo',
'TABLE', N'pay_merchant',
'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商户简称',
'SCHEMA', N'dbo',
'TABLE', N'pay_merchant',
'COLUMN', N'short_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'开启状态',
'SCHEMA', N'dbo',
'TABLE', N'pay_merchant',
'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'备注',
'SCHEMA', N'dbo',
'TABLE', N'pay_merchant',
'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建者',
'SCHEMA', N'dbo',
'TABLE', N'pay_merchant',
'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_merchant',
'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新者',
'SCHEMA', N'dbo',
'TABLE', N'pay_merchant',
'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_merchant',
'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_merchant',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'pay_merchant',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付商户信息',
'SCHEMA', N'dbo',
'TABLE', N'pay_merchant'
GO


-- ----------------------------
-- Records of pay_merchant
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[pay_merchant] ON
GO

INSERT INTO [dbo].[pay_merchant] ([id], [no], [name], [short_name], [status], [remark], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'1', N'M233666999', N'芋道源码', N'芋艿', N'0', N'我是备注', N'', N'2021-10-23 08:31:14.0000000', N'', N'2022-02-27 04:15:20.0000000', N'1', N'0')
GO

SET IDENTITY_INSERT [dbo].[pay_merchant] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for pay_notify_log
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[pay_notify_log]') AND type IN ('U'))
	DROP TABLE [dbo].[pay_notify_log]
GO

CREATE TABLE [dbo].[pay_notify_log] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [task_id] bigint  NOT NULL,
  [notify_times] tinyint  NOT NULL,
  [response] nvarchar(2048) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [status] tinyint  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[pay_notify_log] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'日志编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_log',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'通知任务编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_log',
'COLUMN', N'task_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'第几次被通知',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_log',
'COLUMN', N'notify_times'
GO

EXEC sp_addextendedproperty
'MS_Description', N'请求参数',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_log',
'COLUMN', N'response'
GO

EXEC sp_addextendedproperty
'MS_Description', N'通知状态',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_log',
'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建者',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_log',
'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_log',
'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新者',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_log',
'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_log',
'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_log',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_log',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付通知 App 的日志',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_log'
GO


-- ----------------------------
-- Records of pay_notify_log
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[pay_notify_log] ON
GO

SET IDENTITY_INSERT [dbo].[pay_notify_log] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for pay_notify_task
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[pay_notify_task]') AND type IN ('U'))
	DROP TABLE [dbo].[pay_notify_task]
GO

CREATE TABLE [dbo].[pay_notify_task] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [merchant_id] bigint  NOT NULL,
  [app_id] bigint  NOT NULL,
  [type] tinyint  NOT NULL,
  [data_id] bigint  NOT NULL,
  [status] tinyint  NOT NULL,
  [merchant_order_id] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [next_notify_time] datetime2(7)  NOT NULL,
  [last_execute_time] datetime2(7)  NOT NULL,
  [notify_times] tinyint  NOT NULL,
  [max_notify_times] tinyint  NOT NULL,
  [notify_url] nvarchar(1024) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[pay_notify_task] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'任务编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_task',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商户编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_task',
'COLUMN', N'merchant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'应用编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_task',
'COLUMN', N'app_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'通知类型',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_task',
'COLUMN', N'type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_task',
'COLUMN', N'data_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'通知状态',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_task',
'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商户订单编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_task',
'COLUMN', N'merchant_order_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'下一次通知时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_task',
'COLUMN', N'next_notify_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'最后一次执行时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_task',
'COLUMN', N'last_execute_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'当前通知次数',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_task',
'COLUMN', N'notify_times'
GO

EXEC sp_addextendedproperty
'MS_Description', N'最大可通知次数',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_task',
'COLUMN', N'max_notify_times'
GO

EXEC sp_addextendedproperty
'MS_Description', N'异步通知地址',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_task',
'COLUMN', N'notify_url'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建者',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_task',
'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_task',
'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新者',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_task',
'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_task',
'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_task',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_task',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商户支付、退款等的通知
',
'SCHEMA', N'dbo',
'TABLE', N'pay_notify_task'
GO


-- ----------------------------
-- Records of pay_notify_task
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[pay_notify_task] ON
GO

SET IDENTITY_INSERT [dbo].[pay_notify_task] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for pay_order
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[pay_order]') AND type IN ('U'))
	DROP TABLE [dbo].[pay_order]
GO

CREATE TABLE [dbo].[pay_order] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [merchant_id] bigint  NOT NULL,
  [app_id] bigint  NOT NULL,
  [channel_id] bigint  NULL,
  [channel_code] nvarchar(32) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [merchant_order_id] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [subject] nvarchar(32) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [body] nvarchar(128) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [notify_url] nvarchar(1024) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [notify_status] tinyint  NOT NULL,
  [amount] bigint  NOT NULL,
  [channel_fee_rate] float(53)  NULL,
  [channel_fee_amount] bigint  NULL,
  [status] tinyint  NOT NULL,
  [user_ip] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [expire_time] datetime2(7)  NOT NULL,
  [success_time] datetime2(7)  NULL,
  [notify_time] datetime2(7)  NULL,
  [success_extension_id] bigint  NULL,
  [refund_status] tinyint  NOT NULL,
  [refund_times] tinyint  NOT NULL,
  [refund_amount] bigint  NOT NULL,
  [channel_user_id] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [channel_order_no] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[pay_order] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付订单编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商户编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'merchant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'应用编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'app_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'渠道编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'channel_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'渠道编码',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'channel_code'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商户订单编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'merchant_order_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品标题',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'subject'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品描述',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'body'
GO

EXEC sp_addextendedproperty
'MS_Description', N'异步通知地址',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'notify_url'
GO

EXEC sp_addextendedproperty
'MS_Description', N'通知商户支付结果的回调状态',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'notify_status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付金额，单位：分',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'amount'
GO

EXEC sp_addextendedproperty
'MS_Description', N'渠道手续费，单位：百分比',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'channel_fee_rate'
GO

EXEC sp_addextendedproperty
'MS_Description', N'渠道手续金额，单位：分',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'channel_fee_amount'
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付状态',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用户 IP',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'user_ip'
GO

EXEC sp_addextendedproperty
'MS_Description', N'订单失效时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'expire_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'订单支付成功时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'success_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'订单支付通知时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'notify_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付成功的订单拓展单编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'success_extension_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'退款状态',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'refund_status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'退款次数',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'refund_times'
GO

EXEC sp_addextendedproperty
'MS_Description', N'退款总金额，单位：分',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'refund_amount'
GO

EXEC sp_addextendedproperty
'MS_Description', N'渠道用户编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'channel_user_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'渠道订单号',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'channel_order_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建者',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新者',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'pay_order',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付订单
',
'SCHEMA', N'dbo',
'TABLE', N'pay_order'
GO


-- ----------------------------
-- Records of pay_order
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[pay_order] ON
GO

SET IDENTITY_INSERT [dbo].[pay_order] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for pay_order_extension
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[pay_order_extension]') AND type IN ('U'))
	DROP TABLE [dbo].[pay_order_extension]
GO

CREATE TABLE [dbo].[pay_order_extension] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [no] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [order_id] bigint  NOT NULL,
  [channel_id] bigint  NOT NULL,
  [channel_code] nvarchar(32) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [user_ip] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [status] tinyint  NOT NULL,
  [channel_extras] nvarchar(256) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [channel_notify_data] nvarchar(1024) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[pay_order_extension] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付订单编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_order_extension',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付订单号',
'SCHEMA', N'dbo',
'TABLE', N'pay_order_extension',
'COLUMN', N'no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付订单编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_order_extension',
'COLUMN', N'order_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'渠道编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_order_extension',
'COLUMN', N'channel_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'渠道编码',
'SCHEMA', N'dbo',
'TABLE', N'pay_order_extension',
'COLUMN', N'channel_code'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用户 IP',
'SCHEMA', N'dbo',
'TABLE', N'pay_order_extension',
'COLUMN', N'user_ip'
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付状态',
'SCHEMA', N'dbo',
'TABLE', N'pay_order_extension',
'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付渠道的额外参数',
'SCHEMA', N'dbo',
'TABLE', N'pay_order_extension',
'COLUMN', N'channel_extras'
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付渠道异步通知的内容',
'SCHEMA', N'dbo',
'TABLE', N'pay_order_extension',
'COLUMN', N'channel_notify_data'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建者',
'SCHEMA', N'dbo',
'TABLE', N'pay_order_extension',
'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_order_extension',
'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新者',
'SCHEMA', N'dbo',
'TABLE', N'pay_order_extension',
'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_order_extension',
'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_order_extension',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'pay_order_extension',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付订单
',
'SCHEMA', N'dbo',
'TABLE', N'pay_order_extension'
GO


-- ----------------------------
-- Records of pay_order_extension
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[pay_order_extension] ON
GO

SET IDENTITY_INSERT [dbo].[pay_order_extension] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for pay_refund
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[pay_refund]') AND type IN ('U'))
	DROP TABLE [dbo].[pay_refund]
GO

CREATE TABLE [dbo].[pay_refund] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [merchant_id] bigint  NOT NULL,
  [app_id] bigint  NOT NULL,
  [channel_id] bigint  NOT NULL,
  [channel_code] nvarchar(32) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [order_id] bigint  NOT NULL,
  [trade_no] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [merchant_order_id] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [merchant_refund_no] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [notify_url] nvarchar(1024) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [notify_status] tinyint  NOT NULL,
  [status] tinyint  NOT NULL,
  [type] tinyint  NOT NULL,
  [pay_amount] bigint  NOT NULL,
  [refund_amount] bigint  NOT NULL,
  [reason] nvarchar(256) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [user_ip] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [channel_order_no] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [channel_refund_no] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [channel_error_code] nvarchar(128) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [channel_error_msg] nvarchar(256) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [channel_extras] nvarchar(1024) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [expire_time] datetime2(7)  NULL,
  [success_time] datetime2(7)  NULL,
  [notify_time] datetime2(7)  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[pay_refund] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付退款编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商户编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'merchant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'应用编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'app_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'渠道编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'channel_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'渠道编码',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'channel_code'
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付订单编号 pay_order 表id',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'order_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'交易订单号 pay_extension 表no 字段',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'trade_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商户订单编号（商户系统生成）',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'merchant_order_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商户退款订单号（商户系统生成）',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'merchant_refund_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'异步通知商户地址',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'notify_url'
GO

EXEC sp_addextendedproperty
'MS_Description', N'通知商户退款结果的回调状态',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'notify_status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'退款状态',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'退款类型(部分退款，全部退款)',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付金额,单位分',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'pay_amount'
GO

EXEC sp_addextendedproperty
'MS_Description', N'退款金额,单位分',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'refund_amount'
GO

EXEC sp_addextendedproperty
'MS_Description', N'退款原因',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'reason'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用户 IP',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'user_ip'
GO

EXEC sp_addextendedproperty
'MS_Description', N'渠道订单号，pay_order 中的channel_order_no 对应',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'channel_order_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'渠道退款单号，渠道返回',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'channel_refund_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'渠道调用报错时，错误码',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'channel_error_code'
GO

EXEC sp_addextendedproperty
'MS_Description', N'渠道调用报错时，错误信息',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'channel_error_msg'
GO

EXEC sp_addextendedproperty
'MS_Description', N'支付渠道的额外参数',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'channel_extras'
GO

EXEC sp_addextendedproperty
'MS_Description', N'退款失效时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'expire_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'退款成功时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'success_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'退款通知时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'notify_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建者',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新者',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新时间',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'退款订单',
'SCHEMA', N'dbo',
'TABLE', N'pay_refund'
GO


-- ----------------------------
-- Records of pay_refund
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[pay_refund] ON
GO

SET IDENTITY_INSERT [dbo].[pay_refund] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_dept
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_dept]') AND type IN ('U'))
	DROP TABLE [dbo].[system_dept]
GO

CREATE TABLE [dbo].[system_dept] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [name] nvarchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [parent_id] bigint  NOT NULL,
  [sort] int  NOT NULL,
  [leader_user_id] bigint  NULL,
  [phone] nvarchar(11) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [email] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [status] tinyint  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_dept] SET (LOCK_ESCALATION = TABLE)
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
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'system_dept',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'system_dept',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'部门表',
'SCHEMA', N'dbo',
'TABLE', N'system_dept'
GO


-- ----------------------------
-- Records of system_dept
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_dept] ON
GO

INSERT INTO [dbo].[system_dept] ([id], [name], [parent_id], [sort], [leader_user_id], [phone], [email], [status], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'100', N'芋道源码', N'0', N'0', N'1', N'15888888888', N'ry@qq.com', N'0', N'admin', N'2021-01-05 17:03:47.0000000', N'103', N'2022-01-14 01:04:05.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_dept] ([id], [name], [parent_id], [sort], [leader_user_id], [phone], [email], [status], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'101', N'深圳总公司', N'100', N'1', N'104', N'15888888888', N'ry@qq.com', N'0', N'admin', N'2021-01-05 17:03:47.0000000', N'1', N'2022-02-22 19:47:48.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_dept] ([id], [name], [parent_id], [sort], [leader_user_id], [phone], [email], [status], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'102', N'长沙分公司', N'100', N'2', NULL, N'15888888888', N'ry@qq.com', N'0', N'admin', N'2021-01-05 17:03:47.0000000', N'', N'2021-12-15 05:01:40.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_dept] ([id], [name], [parent_id], [sort], [leader_user_id], [phone], [email], [status], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'103', N'研发部门', N'101', N'1', N'104', N'15888888888', N'ry@qq.com', N'0', N'admin', N'2021-01-05 17:03:47.0000000', N'103', N'2022-01-14 01:04:14.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_dept] ([id], [name], [parent_id], [sort], [leader_user_id], [phone], [email], [status], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'104', N'市场部门', N'101', N'2', NULL, N'15888888888', N'ry@qq.com', N'0', N'admin', N'2021-01-05 17:03:47.0000000', N'', N'2021-12-15 05:01:38.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_dept] ([id], [name], [parent_id], [sort], [leader_user_id], [phone], [email], [status], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'105', N'测试部门', N'101', N'3', NULL, N'15888888888', N'ry@qq.com', N'0', N'admin', N'2021-01-05 17:03:47.0000000', N'', N'2021-12-15 05:01:37.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_dept] ([id], [name], [parent_id], [sort], [leader_user_id], [phone], [email], [status], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'106', N'财务部门', N'101', N'4', N'103', N'15888888888', N'ry@qq.com', N'0', N'admin', N'2021-01-05 17:03:47.0000000', N'103', N'2022-01-15 21:32:22.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_dept] ([id], [name], [parent_id], [sort], [leader_user_id], [phone], [email], [status], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'107', N'运维部门', N'101', N'5', NULL, N'15888888888', N'ry@qq.com', N'0', N'admin', N'2021-01-05 17:03:47.0000000', N'', N'2021-12-15 05:01:33.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_dept] ([id], [name], [parent_id], [sort], [leader_user_id], [phone], [email], [status], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'108', N'市场部门', N'102', N'1', NULL, N'15888888888', N'ry@qq.com', N'0', N'admin', N'2021-01-05 17:03:47.0000000', N'1', N'2022-02-16 08:35:45.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_dept] ([id], [name], [parent_id], [sort], [leader_user_id], [phone], [email], [status], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'109', N'财务部门', N'102', N'2', NULL, N'15888888888', N'ry@qq.com', N'0', N'admin', N'2021-01-05 17:03:47.0000000', N'', N'2021-12-15 05:01:29.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_dept] ([id], [name], [parent_id], [sort], [leader_user_id], [phone], [email], [status], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'110', N'新部门', N'0', N'1', NULL, NULL, NULL, N'0', N'110', N'2022-02-23 20:46:30.0000000', N'110', N'2022-02-23 20:46:30.0000000', N'121', N'0')
GO

INSERT INTO [dbo].[system_dept] ([id], [name], [parent_id], [sort], [leader_user_id], [phone], [email], [status], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'111', N'顶级部门', N'0', N'1', NULL, NULL, NULL, N'0', N'113', N'2022-03-07 21:44:50.0000000', N'113', N'2022-03-07 21:44:50.0000000', N'122', N'0')
GO

SET IDENTITY_INSERT [dbo].[system_dept] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_dict_data
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_dict_data]') AND type IN ('U'))
	DROP TABLE [dbo].[system_dict_data]
GO

CREATE TABLE [dbo].[system_dict_data] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [sort] int  NOT NULL,
  [label] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [value] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [dict_type] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [status] tinyint  NOT NULL,
  [color_type] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [css_class] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [remark] nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_dict_data] SET (LOCK_ESCALATION = TABLE)
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
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_dict_data] ON
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1', N'1', N'男', N'1', N'system_user_sex', N'0', N'default', N'A', N'性别男', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-03-29 00:14:39.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'2', N'2', N'女', N'2', N'system_user_sex', N'1', N'success', N'', N'性别女', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-02-16 01:30:51.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'8', N'1', N'正常', N'1', N'infra_job_status', N'0', N'success', N'', N'正常状态', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-02-16 19:33:38.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'9', N'2', N'暂停', N'2', N'infra_job_status', N'0', N'danger', N'', N'停用状态', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-02-16 19:33:45.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'12', N'1', N'系统内置', N'1', N'infra_config_type', N'0', N'danger', N'', N'参数类型 - 系统内置', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-02-16 19:06:02.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'13', N'2', N'自定义', N'2', N'infra_config_type', N'0', N'primary', N'', N'参数类型 - 自定义', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-02-16 19:06:07.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'14', N'1', N'通知', N'1', N'system_notice_type', N'0', N'success', N'', N'通知', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-02-16 13:05:57.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'15', N'2', N'公告', N'2', N'system_notice_type', N'0', N'info', N'', N'公告', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-02-16 13:06:01.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'16', N'0', N'其它', N'0', N'system_operate_type', N'0', N'default', N'', N'其它操作', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-02-16 09:32:46.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'17', N'1', N'查询', N'1', N'system_operate_type', N'0', N'info', N'', N'查询操作', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-02-16 09:33:16.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'18', N'2', N'新增', N'2', N'system_operate_type', N'0', N'primary', N'', N'新增操作', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-02-16 09:33:13.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'19', N'3', N'修改', N'3', N'system_operate_type', N'0', N'warning', N'', N'修改操作', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-02-16 09:33:22.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'20', N'4', N'删除', N'4', N'system_operate_type', N'0', N'danger', N'', N'删除操作', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-02-16 09:33:27.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'22', N'5', N'导出', N'5', N'system_operate_type', N'0', N'default', N'', N'导出操作', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-02-16 09:33:32.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'23', N'6', N'导入', N'6', N'system_operate_type', N'0', N'default', N'', N'导入操作', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-02-16 09:33:35.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'27', N'1', N'开启', N'0', N'common_status', N'0', N'primary', N'', N'开启状态', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-02-16 08:00:39.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'28', N'2', N'关闭', N'1', N'common_status', N'0', N'info', N'', N'关闭状态', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-02-16 08:00:44.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'29', N'1', N'目录', N'1', N'system_menu_type', N'0', N'', N'', N'目录', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-02-01 16:43:45.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'30', N'2', N'菜单', N'2', N'system_menu_type', N'0', N'', N'', N'菜单', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-02-01 16:43:41.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'31', N'3', N'按钮', N'3', N'system_menu_type', N'0', N'', N'', N'按钮', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-02-01 16:43:39.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'32', N'1', N'内置', N'1', N'system_role_type', N'0', N'danger', N'', N'内置角色', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-02-16 13:02:08.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'33', N'2', N'自定义', N'2', N'system_role_type', N'0', N'primary', N'', N'自定义角色', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-02-16 13:02:12.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'34', N'1', N'全部数据权限', N'1', N'system_data_scope', N'0', N'', N'', N'全部数据权限', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-02-01 16:47:17.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'35', N'2', N'指定部门数据权限', N'2', N'system_data_scope', N'0', N'', N'', N'指定部门数据权限', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-02-01 16:47:18.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'36', N'3', N'本部门数据权限', N'3', N'system_data_scope', N'0', N'', N'', N'本部门数据权限', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-02-01 16:47:16.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'37', N'4', N'本部门及以下数据权限', N'4', N'system_data_scope', N'0', N'', N'', N'本部门及以下数据权限', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-02-01 16:47:21.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'38', N'5', N'仅本人数据权限', N'5', N'system_data_scope', N'0', N'', N'', N'仅本人数据权限', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-02-01 16:47:23.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'39', N'0', N'成功', N'0', N'system_login_result', N'0', N'success', N'', N'登陆结果 - 成功', N'', N'2021-01-18 06:17:36.0000000', N'1', N'2022-02-16 13:23:49.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'40', N'10', N'账号或密码不正确', N'10', N'system_login_result', N'0', N'primary', N'', N'登陆结果 - 账号或密码不正确', N'', N'2021-01-18 06:17:54.0000000', N'1', N'2022-02-16 13:24:27.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'41', N'20', N'用户被禁用', N'20', N'system_login_result', N'0', N'warning', N'', N'登陆结果 - 用户被禁用', N'', N'2021-01-18 06:17:54.0000000', N'1', N'2022-02-16 13:23:57.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'42', N'30', N'验证码不存在', N'30', N'system_login_result', N'0', N'info', N'', N'登陆结果 - 验证码不存在', N'', N'2021-01-18 06:17:54.0000000', N'1', N'2022-02-16 13:24:07.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'43', N'31', N'验证码不正确', N'31', N'system_login_result', N'0', N'info', N'', N'登陆结果 - 验证码不正确', N'', N'2021-01-18 06:17:54.0000000', N'1', N'2022-02-16 13:24:11.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'44', N'100', N'未知异常', N'100', N'system_login_result', N'0', N'danger', N'', N'登陆结果 - 未知异常', N'', N'2021-01-18 06:17:54.0000000', N'1', N'2022-02-16 13:24:23.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'45', N'1', N'是', N'true', N'infra_boolean_string', N'0', N'danger', N'', N'Boolean 是否类型 - 是', N'', N'2021-01-19 03:20:55.0000000', N'1', N'2022-03-15 23:01:45.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'46', N'1', N'否', N'false', N'infra_boolean_string', N'0', N'info', N'', N'Boolean 是否类型 - 否', N'', N'2021-01-19 03:20:55.0000000', N'1', N'2022-03-15 23:09:45.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'47', N'1', N'永不超时', N'1', N'infra_redis_timeout_type', N'0', N'primary', N'', N'Redis 未设置超时的情况', N'', N'2021-01-26 00:53:17.0000000', N'1', N'2022-02-16 19:03:35.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'48', N'1', N'动态超时', N'2', N'infra_redis_timeout_type', N'0', N'info', N'', N'程序里动态传入超时时间，无法固定', N'', N'2021-01-26 00:55:00.0000000', N'1', N'2022-02-16 19:03:41.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'49', N'3', N'固定超时', N'3', N'infra_redis_timeout_type', N'0', N'success', N'', N'Redis 设置了过期时间', N'', N'2021-01-26 00:55:26.0000000', N'1', N'2022-02-16 19:03:45.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'50', N'1', N'单表（增删改查）', N'1', N'infra_codegen_template_type', N'0', N'', N'', NULL, N'', N'2021-02-05 07:09:06.0000000', N'', N'2022-03-10 16:33:15.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'51', N'2', N'树表（增删改查）', N'2', N'infra_codegen_template_type', N'0', N'', N'', NULL, N'', N'2021-02-05 07:14:46.0000000', N'', N'2022-03-10 16:33:19.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'53', N'0', N'初始化中', N'0', N'infra_job_status', N'0', N'primary', N'', NULL, N'', N'2021-02-07 07:46:49.0000000', N'1', N'2022-02-16 19:33:29.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'57', N'0', N'运行中', N'0', N'infra_job_log_status', N'0', N'primary', N'', N'RUNNING', N'', N'2021-02-08 10:04:24.0000000', N'1', N'2022-02-16 19:07:48.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'58', N'1', N'成功', N'1', N'infra_job_log_status', N'0', N'success', N'', NULL, N'', N'2021-02-08 10:06:57.0000000', N'1', N'2022-02-16 19:07:52.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'59', N'2', N'失败', N'2', N'infra_job_log_status', N'0', N'warning', N'', N'失败', N'', N'2021-02-08 10:07:38.0000000', N'1', N'2022-02-16 19:07:56.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'60', N'1', N'会员', N'1', N'user_type', N'0', N'primary', N'', NULL, N'', N'2021-02-26 00:16:27.0000000', N'1', N'2022-02-16 10:22:19.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'61', N'2', N'管理员', N'2', N'user_type', N'0', N'success', N'', NULL, N'', N'2021-02-26 00:16:34.0000000', N'1', N'2022-02-16 10:22:22.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'62', N'0', N'未处理', N'0', N'infra_api_error_log_process_status', N'0', N'primary', N'', NULL, N'', N'2021-02-26 07:07:19.0000000', N'1', N'2022-02-16 20:14:17.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'63', N'1', N'已处理', N'1', N'infra_api_error_log_process_status', N'0', N'success', N'', NULL, N'', N'2021-02-26 07:07:26.0000000', N'1', N'2022-02-16 20:14:08.0000000', N'0')
GO



INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'66', N'2', N'阿里云', N'ALIYUN', N'system_sms_channel_code', N'0', N'primary', N'', NULL, N'1', N'2021-04-05 01:05:26.0000000', N'1', N'2022-02-16 10:09:52.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'67', N'1', N'验证码', N'1', N'system_sms_template_type', N'0', N'warning', N'', NULL, N'1', N'2021-04-05 21:50:57.0000000', N'1', N'2022-02-16 12:48:30.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'68', N'2', N'通知', N'2', N'system_sms_template_type', N'0', N'primary', N'', NULL, N'1', N'2021-04-05 21:51:08.0000000', N'1', N'2022-02-16 12:48:27.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'69', N'0', N'营销', N'3', N'system_sms_template_type', N'0', N'danger', N'', NULL, N'1', N'2021-04-05 21:51:15.0000000', N'1', N'2022-02-16 12:48:22.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'70', N'0', N'初始化', N'0', N'system_sms_send_status', N'0', N'primary', N'', NULL, N'1', N'2021-04-11 20:18:33.0000000', N'1', N'2022-02-16 10:26:07.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'71', N'1', N'发送成功', N'10', N'system_sms_send_status', N'0', N'success', N'', NULL, N'1', N'2021-04-11 20:18:43.0000000', N'1', N'2022-02-16 10:25:56.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'72', N'2', N'发送失败', N'20', N'system_sms_send_status', N'0', N'danger', N'', NULL, N'1', N'2021-04-11 20:18:49.0000000', N'1', N'2022-02-16 10:26:03.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'73', N'3', N'不发送', N'30', N'system_sms_send_status', N'0', N'info', N'', NULL, N'1', N'2021-04-11 20:19:44.0000000', N'1', N'2022-02-16 10:26:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'74', N'0', N'等待结果', N'0', N'system_sms_receive_status', N'0', N'primary', N'', NULL, N'1', N'2021-04-11 20:27:43.0000000', N'1', N'2022-02-16 10:28:24.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'75', N'1', N'接收成功', N'10', N'system_sms_receive_status', N'0', N'success', N'', NULL, N'1', N'2021-04-11 20:29:25.0000000', N'1', N'2022-02-16 10:28:28.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'76', N'2', N'接收失败', N'20', N'system_sms_receive_status', N'0', N'danger', N'', NULL, N'1', N'2021-04-11 20:29:31.0000000', N'1', N'2022-02-16 10:28:32.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'77', N'0', N'调试(钉钉)', N'DEBUG_DING_TALK', N'system_sms_channel_code', N'0', N'info', N'', NULL, N'1', N'2021-04-13 00:20:37.0000000', N'1', N'2022-02-16 10:10:00.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'78', N'1', N'自动生成', N'1', N'system_error_code_type', N'0', N'warning', N'', NULL, N'1', N'2021-04-21 00:06:48.0000000', N'1', N'2022-02-16 13:57:20.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'79', N'2', N'手动编辑', N'2', N'system_error_code_type', N'0', N'primary', N'', NULL, N'1', N'2021-04-21 00:07:14.0000000', N'1', N'2022-02-16 13:57:24.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'80', N'100', N'账号登录', N'100', N'system_login_type', N'0', N'primary', N'', N'账号登录', N'1', N'2021-10-06 00:52:02.0000000', N'1', N'2022-02-16 13:11:34.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'81', N'101', N'社交登录', N'101', N'system_login_type', N'0', N'info', N'', N'社交登录', N'1', N'2021-10-06 00:52:17.0000000', N'1', N'2022-02-16 13:11:40.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'83', N'200', N'主动登出', N'200', N'system_login_type', N'0', N'primary', N'', N'主动登出', N'1', N'2021-10-06 00:52:58.0000000', N'1', N'2022-02-16 13:11:49.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'85', N'202', N'强制登出', N'202', N'system_login_type', N'0', N'danger', N'', N'强制退出', N'1', N'2021-10-06 00:53:41.0000000', N'1', N'2022-02-16 13:11:57.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'86', N'0', N'病假', N'1', N'bpm_oa_leave_type', N'0', N'primary', N'', NULL, N'1', N'2021-09-21 22:35:28.0000000', N'1', N'2022-02-16 10:00:41.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'87', N'1', N'事假', N'2', N'bpm_oa_leave_type', N'0', N'info', N'', NULL, N'1', N'2021-09-21 22:36:11.0000000', N'1', N'2022-02-16 10:00:49.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'88', N'2', N'婚假', N'3', N'bpm_oa_leave_type', N'0', N'warning', N'', NULL, N'1', N'2021-09-21 22:36:38.0000000', N'1', N'2022-02-16 10:00:53.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'98', N'1', N'v2', N'v2', N'pay_channel_wechat_version', N'0', N'', N'', N'v2版本', N'1', N'2021-11-08 17:00:58.0000000', N'1', N'2021-11-08 17:00:58.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'99', N'2', N'v3', N'v3', N'pay_channel_wechat_version', N'0', N'', N'', N'v3版本', N'1', N'2021-11-08 17:01:07.0000000', N'1', N'2021-11-08 17:01:07.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'108', N'1', N'RSA2', N'RSA2', N'pay_channel_alipay_sign_type', N'0', N'', N'', N'RSA2', N'1', N'2021-11-18 15:39:29.0000000', N'1', N'2021-11-18 15:39:29.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'109', N'1', N'公钥模式', N'1', N'pay_channel_alipay_mode', N'0', N'', N'', N'公钥模式：privateKey + alipayPublicKey', N'1', N'2021-11-18 15:45:23.0000000', N'1', N'2021-11-18 15:45:23.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'110', N'2', N'证书模式', N'2', N'pay_channel_alipay_mode', N'0', N'', N'', N'证书模式：appCertContent + alipayPublicCertContent + rootCertContent', N'1', N'2021-11-18 15:45:40.0000000', N'1', N'2021-11-18 15:45:40.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'111', N'1', N'线上', N'https://openapi.alipay.com/gateway.do', N'pay_channel_alipay_server_type', N'0', N'', N'', N'网关地址 - 线上', N'1', N'2021-11-18 16:59:32.0000000', N'1', N'2021-11-21 17:37:29.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'112', N'2', N'沙箱', N'https://openapi.alipaydev.com/gateway.do', N'pay_channel_alipay_server_type', N'0', N'', N'', N'网关地址 - 沙箱', N'1', N'2021-11-18 16:59:48.0000000', N'1', N'2021-11-21 17:37:39.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'113', N'1', N'微信 JSAPI 支付', N'wx_pub', N'pay_channel_code_type', N'0', N'', N'', N'微信 JSAPI（公众号） 支付', N'1', N'2021-12-03 10:40:24.0000000', N'1', N'2021-12-04 16:41:00.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'114', N'2', N'微信小程序支付', N'wx_lite', N'pay_channel_code_type', N'0', N'', N'', N'微信小程序支付', N'1', N'2021-12-03 10:41:06.0000000', N'1', N'2021-12-03 10:41:06.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'115', N'3', N'微信 App 支付', N'wx_app', N'pay_channel_code_type', N'0', N'', N'', N'微信 App 支付', N'1', N'2021-12-03 10:41:20.0000000', N'1', N'2021-12-03 10:41:20.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'116', N'4', N'支付宝 PC 网站支付', N'alipay_pc', N'pay_channel_code_type', N'0', N'', N'', N'支付宝 PC 网站支付', N'1', N'2021-12-03 10:42:09.0000000', N'1', N'2021-12-03 10:42:09.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'117', N'5', N'支付宝 Wap 网站支付', N'alipay_wap', N'pay_channel_code_type', N'0', N'', N'', N'支付宝 Wap 网站支付', N'1', N'2021-12-03 10:42:26.0000000', N'1', N'2021-12-03 10:42:26.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'118', N'6', N'支付宝App 支付', N'alipay_app', N'pay_channel_code_type', N'0', N'', N'', N'支付宝App 支付', N'1', N'2021-12-03 10:42:55.0000000', N'1', N'2021-12-03 10:42:55.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'119', N'7', N'支付宝扫码支付', N'alipay_qr', N'pay_channel_code_type', N'0', N'', N'', N'支付宝扫码支付', N'1', N'2021-12-03 10:43:10.0000000', N'1', N'2021-12-03 10:43:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'120', N'1', N'通知成功', N'10', N'pay_order_notify_status', N'0', N'success', N'', N'通知成功', N'1', N'2021-12-03 11:02:41.0000000', N'1', N'2022-02-16 13:59:13.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'121', N'2', N'通知失败', N'20', N'pay_order_notify_status', N'0', N'danger', N'', N'通知失败', N'1', N'2021-12-03 11:02:59.0000000', N'1', N'2022-02-16 13:59:17.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'122', N'3', N'未通知', N'0', N'pay_order_notify_status', N'0', N'info', N'', N'未通知', N'1', N'2021-12-03 11:03:10.0000000', N'1', N'2022-02-16 13:59:23.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'123', N'1', N'支付成功', N'10', N'pay_order_status', N'0', N'success', N'', N'支付成功', N'1', N'2021-12-03 11:18:29.0000000', N'1', N'2022-02-16 15:24:25.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'124', N'2', N'支付关闭', N'20', N'pay_order_status', N'0', N'danger', N'', N'支付关闭', N'1', N'2021-12-03 11:18:42.0000000', N'1', N'2022-02-16 15:24:31.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'125', N'3', N'未支付', N'0', N'pay_order_status', N'0', N'info', N'', N'未支付', N'1', N'2021-12-03 11:18:18.0000000', N'1', N'2022-02-16 15:24:35.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'126', N'1', N'未退款', N'0', N'pay_order_refund_status', N'0', N'', N'', N'未退款', N'1', N'2021-12-03 11:30:35.0000000', N'1', N'2021-12-03 11:34:05.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'127', N'2', N'部分退款', N'10', N'pay_order_refund_status', N'0', N'', N'', N'部分退款', N'1', N'2021-12-03 11:30:44.0000000', N'1', N'2021-12-03 11:34:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'128', N'3', N'全部退款', N'20', N'pay_order_refund_status', N'0', N'', N'', N'全部退款', N'1', N'2021-12-03 11:30:52.0000000', N'1', N'2021-12-03 11:34:14.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1117', N'1', N'退款订单生成', N'0', N'pay_refund_order_status', N'0', N'primary', N'', N'退款订单生成', N'1', N'2021-12-10 16:44:44.0000000', N'1', N'2022-02-16 14:05:24.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1118', N'2', N'退款成功', N'1', N'pay_refund_order_status', N'0', N'success', N'', N'退款成功', N'1', N'2021-12-10 16:44:59.0000000', N'1', N'2022-02-16 14:05:28.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1119', N'3', N'退款失败', N'2', N'pay_refund_order_status', N'0', N'danger', N'', N'退款失败', N'1', N'2021-12-10 16:45:10.0000000', N'1', N'2022-02-16 14:05:34.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1124', N'8', N'退款关闭', N'99', N'pay_refund_order_status', N'0', N'info', N'', N'退款关闭', N'1', N'2021-12-10 16:46:26.0000000', N'1', N'2022-02-16 14:05:40.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1125', N'0', N'默认', N'1', N'bpm_model_category', N'0', N'primary', N'', N'流程分类 - 默认', N'1', N'2022-01-02 08:41:11.0000000', N'1', N'2022-02-16 20:01:42.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1126', N'0', N'OA', N'2', N'bpm_model_category', N'0', N'success', N'', N'流程分类 - OA', N'1', N'2022-01-02 08:41:22.0000000', N'1', N'2022-02-16 20:01:50.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1127', N'0', N'进行中', N'1', N'bpm_process_instance_status', N'0', N'primary', N'', N'流程实例的状态 - 进行中', N'1', N'2022-01-07 23:47:22.0000000', N'1', N'2022-02-16 20:07:49.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1128', N'2', N'已完成', N'2', N'bpm_process_instance_status', N'0', N'success', N'', N'流程实例的状态 - 已完成', N'1', N'2022-01-07 23:47:49.0000000', N'1', N'2022-02-16 20:07:54.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1129', N'1', N'处理中', N'1', N'bpm_process_instance_result', N'0', N'primary', N'', N'流程实例的结果 - 处理中', N'1', N'2022-01-07 23:48:32.0000000', N'1', N'2022-02-16 09:53:26.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1130', N'2', N'通过', N'2', N'bpm_process_instance_result', N'0', N'success', N'', N'流程实例的结果 - 通过', N'1', N'2022-01-07 23:48:45.0000000', N'1', N'2022-02-16 09:53:31.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1131', N'3', N'不通过', N'3', N'bpm_process_instance_result', N'0', N'danger', N'', N'流程实例的结果 - 不通过', N'1', N'2022-01-07 23:48:55.0000000', N'1', N'2022-02-16 09:53:38.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1132', N'4', N'已取消', N'4', N'bpm_process_instance_result', N'0', N'info', N'', N'流程实例的结果 - 撤销', N'1', N'2022-01-07 23:49:06.0000000', N'1', N'2022-02-16 09:53:42.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1133', N'10', N'流程表单', N'10', N'bpm_model_form_type', N'0', N'', N'', N'流程的表单类型 - 流程表单', N'103', N'2022-01-11 23:51:30.0000000', N'103', N'2022-01-11 23:51:30.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1134', N'20', N'业务表单', N'20', N'bpm_model_form_type', N'0', N'', N'', N'流程的表单类型 - 业务表单', N'103', N'2022-01-11 23:51:47.0000000', N'103', N'2022-01-11 23:51:47.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1135', N'10', N'角色', N'10', N'bpm_task_assign_rule_type', N'0', N'info', N'', N'任务分配规则的类型 - 角色', N'103', N'2022-01-12 23:21:22.0000000', N'1', N'2022-02-16 20:06:14.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1136', N'20', N'部门的成员', N'20', N'bpm_task_assign_rule_type', N'0', N'primary', N'', N'任务分配规则的类型 - 部门的成员', N'103', N'2022-01-12 23:21:47.0000000', N'1', N'2022-02-16 20:05:28.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1137', N'21', N'部门的负责人', N'21', N'bpm_task_assign_rule_type', N'0', N'primary', N'', N'任务分配规则的类型 - 部门的负责人', N'103', N'2022-01-12 23:33:36.0000000', N'1', N'2022-02-16 20:05:31.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1138', N'30', N'用户', N'30', N'bpm_task_assign_rule_type', N'0', N'info', N'', N'任务分配规则的类型 - 用户', N'103', N'2022-01-12 23:34:02.0000000', N'1', N'2022-02-16 20:05:50.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1139', N'40', N'用户组', N'40', N'bpm_task_assign_rule_type', N'0', N'warning', N'', N'任务分配规则的类型 - 用户组', N'103', N'2022-01-12 23:34:21.0000000', N'1', N'2022-02-16 20:05:57.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1140', N'50', N'自定义脚本', N'50', N'bpm_task_assign_rule_type', N'0', N'danger', N'', N'任务分配规则的类型 - 自定义脚本', N'103', N'2022-01-12 23:34:43.0000000', N'1', N'2022-02-16 20:06:01.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1141', N'22', N'岗位', N'22', N'bpm_task_assign_rule_type', N'0', N'success', N'', N'任务分配规则的类型 - 岗位', N'103', N'2022-01-14 18:41:55.0000000', N'1', N'2022-02-16 20:05:39.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1142', N'10', N'流程发起人', N'10', N'bpm_task_assign_script', N'0', N'', N'', N'任务分配自定义脚本 - 流程发起人', N'103', N'2022-01-15 00:10:57.0000000', N'103', N'2022-01-15 21:24:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1143', N'20', N'流程发起人的一级领导', N'20', N'bpm_task_assign_script', N'0', N'', N'', N'任务分配自定义脚本 - 流程发起人的一级领导', N'103', N'2022-01-15 21:24:31.0000000', N'103', N'2022-01-15 21:24:31.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1144', N'21', N'流程发起人的二级领导', N'21', N'bpm_task_assign_script', N'0', N'', N'', N'任务分配自定义脚本 - 流程发起人的二级领导', N'103', N'2022-01-15 21:24:46.0000000', N'103', N'2022-01-15 21:24:57.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1145', N'1', N'管理后台', N'1', N'infra_codegen_scene', N'0', N'', N'', N'代码生成的场景枚举 - 管理后台', N'1', N'2022-02-02 13:15:06.0000000', N'1', N'2022-03-10 16:32:59.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1146', N'2', N'用户 APP', N'2', N'infra_codegen_scene', N'0', N'', N'', N'代码生成的场景枚举 - 用户 APP', N'1', N'2022-02-02 13:15:19.0000000', N'1', N'2022-03-10 16:33:03.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1147', N'0', N'未退款', N'0', N'pay_refund_order_type', N'0', N'info', N'', N'退款类型 - 未退款', N'1', N'2022-02-16 14:09:01.0000000', N'1', N'2022-02-16 14:09:01.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1148', N'10', N'部分退款', N'10', N'pay_refund_order_type', N'0', N'success', N'', N'退款类型 - 部分退款', N'1', N'2022-02-16 14:09:25.0000000', N'1', N'2022-02-16 14:11:38.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1149', N'20', N'全部退款', N'20', N'pay_refund_order_type', N'0', N'warning', N'', N'退款类型 - 全部退款', N'1', N'2022-02-16 14:11:33.0000000', N'1', N'2022-02-16 14:11:33.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1150', N'1', N'数据库', N'1', N'infra_file_storage', N'0', N'default', N'', NULL, N'1', N'2022-03-15 00:25:28.0000000', N'1', N'2022-03-15 00:25:28.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1151', N'10', N'本地磁盘', N'10', N'infra_file_storage', N'0', N'default', N'', NULL, N'1', N'2022-03-15 00:25:41.0000000', N'1', N'2022-03-15 00:25:56.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1152', N'11', N'FTP 服务器', N'11', N'infra_file_storage', N'0', N'default', N'', NULL, N'1', N'2022-03-15 00:26:06.0000000', N'1', N'2022-03-15 00:26:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1153', N'12', N'SFTP 服务器', N'12', N'infra_file_storage', N'0', N'default', N'', NULL, N'1', N'2022-03-15 00:26:22.0000000', N'1', N'2022-03-15 00:26:22.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1154', N'20', N'S3 对象存储', N'20', N'infra_file_storage', N'0', N'default', N'', NULL, N'1', N'2022-03-15 00:26:31.0000000', N'1', N'2022-03-15 00:26:45.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1155', N'103', N'短信登录', N'103', N'system_login_type', N'0', N'default', N'', NULL, N'1', N'2022-05-09 23:57:58.0000000', N'1', N'2022-05-09 23:58:09.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1156', N'1', N'password', N'password', N'system_oauth2_grant_type', N'0', N'default', N'', N'密码模式', N'1', N'2022-05-12 00:22:05.0000000', N'1', N'2022-05-11 16:26:01.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1157', N'2', N'authorization_code', N'authorization_code', N'system_oauth2_grant_type', N'0', N'primary', N'', N'授权码模式', N'1', N'2022-05-12 00:22:59.0000000', N'1', N'2022-05-11 16:26:02.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1158', N'3', N'implicit', N'implicit', N'system_oauth2_grant_type', N'0', N'success', N'', N'简化模式', N'1', N'2022-05-12 00:23:40.0000000', N'1', N'2022-05-11 16:26:05.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1159', N'4', N'client_credentials', N'client_credentials', N'system_oauth2_grant_type', N'0', N'default', N'', N'客户端模式', N'1', N'2022-05-12 00:23:51.0000000', N'1', N'2022-05-11 16:26:08.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_data] ([id], [sort], [label], [value], [dict_type], [status], [color_type], [css_class], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1160', N'5', N'refresh_token', N'refresh_token', N'system_oauth2_grant_type', N'0', N'info', N'', N'刷新模式', N'1', N'2022-05-12 00:24:02.0000000', N'1', N'2022-05-11 16:26:11.0000000', N'0')
GO

SET IDENTITY_INSERT [dbo].[system_dict_data] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_dict_type
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_dict_type]') AND type IN ('U'))
	DROP TABLE [dbo].[system_dict_type]
GO

CREATE TABLE [dbo].[system_dict_type]
(
    [
    id]
    bigint
    IDENTITY
(
    1,
    1
) NOT NULL,
    [name] nvarchar
(
    100
) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [type] nvarchar
(
    100
) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    [status] tinyint NOT NULL,
    [remark] nvarchar
(
    500
) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [creator] nvarchar
(
    64
) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [create_time] datetime2
(
    7
) NOT NULL,
    [updater] nvarchar
(
    64
) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [update_time] datetime2
(
    7
) NOT NULL,
    [deleted_time] datetime2
(
    7
),
    [deleted] bit DEFAULT 0 NOT NULL
    )
GO

ALTER TABLE [dbo].[system_dict_type] SET (LOCK_ESCALATION = TABLE)
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
    'MS_Description', N'删除时间',
    'SCHEMA', N'dbo',
    'TABLE', N'system_dict_type',
    'COLUMN', N'deleted_time'
    GO
    EXEC sp_addextendedproperty
    'MS_Description', N'是否删除',
    'SCHEMA', N'dbo',
    'TABLE', N'system_dict_type',
    'COLUMN', N'deleted'
    GO
    EXEC sp_addextendedproperty
    'MS_Description', N'字典类型表',
'SCHEMA', N'dbo',
'TABLE', N'system_dict_type'
GO


-- ----------------------------
-- Records of system_dict_type
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_dict_type] ON
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1', N'用户性别', N'system_user_sex', N'0', NULL, N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-02-01 16:30:31.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'6', N'参数类型', N'infra_config_type', N'0', NULL, N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-02-01 16:36:54.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'7', N'通知类型', N'system_notice_type', N'0', NULL, N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-02-01 16:35:26.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'9', N'操作类型', N'system_operate_type', N'0', NULL, N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-02-16 09:32:21.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'10', N'系统状态', N'common_status', N'0', NULL, N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-02-01 16:21:28.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'11', N'Boolean 是否类型', N'infra_boolean_string', N'0', N'boolean 转是否', N'', N'2021-01-19 03:20:08.0000000', N'', N'2022-02-01 16:37:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'104', N'登陆结果', N'system_login_result', N'0', N'登陆结果', N'', N'2021-01-18 06:17:11.0000000', N'', N'2022-02-01 16:36:00.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'105', N'Redis 超时类型', N'infra_redis_timeout_type', N'0', N'RedisKeyDefine.TimeoutTypeEnum', N'', N'2021-01-26 00:52:50.0000000', N'', N'2022-02-01 16:50:29.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'106', N'代码生成模板类型', N'infra_codegen_template_type', N'0', NULL, N'', N'2021-02-05 07:08:06.0000000', N'', N'2022-03-10 16:33:42.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'107', N'定时任务状态', N'infra_job_status', N'0', NULL, N'', N'2021-02-07 07:44:16.0000000', N'', N'2022-02-01 16:51:11.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'108', N'定时任务日志状态', N'infra_job_log_status', N'0', NULL, N'', N'2021-02-08 10:03:51.0000000', N'', N'2022-02-01 16:50:43.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'109', N'用户类型', N'user_type', N'0', NULL, N'', N'2021-02-26 00:15:51.0000000', N'', N'2021-02-26 00:15:51.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'110', N'API 异常数据的处理状态', N'infra_api_error_log_process_status', N'0', NULL, N'', N'2021-02-26 07:07:01.0000000', N'', N'2022-02-01 16:50:53.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'111', N'短信渠道编码', N'system_sms_channel_code', N'0', NULL, N'1', N'2021-04-05 01:04:50.0000000', N'1', N'2022-02-16 02:09:08.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'112', N'短信模板的类型', N'system_sms_template_type', N'0', NULL, N'1', N'2021-04-05 21:50:43.0000000', N'1', N'2022-02-01 16:35:06.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'113', N'短信发送状态', N'system_sms_send_status', N'0', NULL, N'1', N'2021-04-11 20:18:03.0000000', N'1', N'2022-02-01 16:35:09.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'114', N'短信接收状态', N'system_sms_receive_status', N'0', NULL, N'1', N'2021-04-11 20:27:14.0000000', N'1', N'2022-02-01 16:35:14.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'115', N'错误码的类型', N'system_error_code_type', N'0', NULL, N'1', N'2021-04-21 00:06:30.0000000', N'1', N'2022-02-01 16:36:49.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'116', N'登陆日志的类型', N'system_login_type', N'0', N'登陆日志的类型', N'1', N'2021-10-06 00:50:46.0000000', N'1', N'2022-02-01 16:35:56.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'117', N'OA 请假类型', N'bpm_oa_leave_type', N'0', NULL, N'1', N'2021-09-21 22:34:33.0000000', N'1', N'2022-01-22 10:41:37.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'122', N'支付渠道微信版本', N'pay_channel_wechat_version', N'0', N'支付渠道微信版本', N'1', N'2021-11-08 17:00:26.0000000', N'1', N'2021-11-08 17:00:26.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'127', N'支付渠道支付宝算法类型', N'pay_channel_alipay_sign_type', N'0', N'支付渠道支付宝算法类型', N'1', N'2021-11-18 15:39:09.0000000', N'1', N'2021-11-18 15:39:09.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'128', N'支付渠道支付宝公钥类型', N'pay_channel_alipay_mode', N'0', N'支付渠道支付宝公钥类型', N'1', N'2021-11-18 15:44:28.0000000', N'1', N'2021-11-18 15:44:28.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'129', N'支付宝网关地址', N'pay_channel_alipay_server_type', N'0', N'支付宝网关地址', N'1', N'2021-11-18 16:58:55.0000000', N'1', N'2021-11-18 17:01:34.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'130', N'支付渠道编码类型', N'pay_channel_code_type', N'0', N'支付渠道的编码', N'1', N'2021-12-03 10:35:08.0000000', N'1', N'2021-12-03 10:35:08.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'131', N'支付订单回调状态', N'pay_order_notify_status', N'0', N'支付订单回调状态', N'1', N'2021-12-03 10:53:29.0000000', N'1', N'2021-12-03 10:53:29.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'132', N'支付订单状态', N'pay_order_status', N'0', N'支付订单状态', N'1', N'2021-12-03 11:17:50.0000000', N'1', N'2021-12-03 11:17:50.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'133', N'支付订单退款状态', N'pay_order_refund_status', N'0', N'支付订单退款状态', N'1', N'2021-12-03 11:27:31.0000000', N'1', N'2021-12-03 11:27:31.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'134', N'退款订单状态', N'pay_refund_order_status', N'0', N'退款订单状态', N'1', N'2021-12-10 16:42:50.0000000', N'1', N'2021-12-10 16:42:50.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'135', N'退款订单类别', N'pay_refund_order_type', N'0', N'退款订单类别', N'1', N'2021-12-10 17:14:53.0000000', N'1', N'2021-12-10 17:14:53.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'138', N'流程分类', N'bpm_model_category', N'0', N'流程分类', N'1', N'2022-01-02 08:40:45.0000000', N'1', N'2022-01-02 08:40:45.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'139', N'流程实例的状态', N'bpm_process_instance_status', N'0', N'流程实例的状态', N'1', N'2022-01-07 23:46:42.0000000', N'1', N'2022-01-07 23:46:42.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'140', N'流程实例的结果', N'bpm_process_instance_result', N'0', N'流程实例的结果', N'1', N'2022-01-07 23:48:10.0000000', N'1', N'2022-01-07 23:48:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'141', N'流程的表单类型', N'bpm_model_form_type', N'0', N'流程的表单类型', N'103', N'2022-01-11 23:50:45.0000000', N'103', N'2022-01-11 23:50:45.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'142', N'任务分配规则的类型', N'bpm_task_assign_rule_type', N'0', N'任务分配规则的类型', N'103', N'2022-01-12 23:21:04.0000000', N'103', N'2022-01-12 15:46:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'143', N'任务分配自定义脚本', N'bpm_task_assign_script', N'0', N'任务分配自定义脚本', N'103', N'2022-01-15 00:10:35.0000000', N'103', N'2022-01-15 00:10:35.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'144', N'代码生成的场景枚举', N'infra_codegen_scene', N'0', N'代码生成的场景枚举', N'1', N'2022-02-02 13:14:45.0000000', N'1', N'2022-03-10 16:33:46.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'145', N'角色类型', N'system_role_type', N'0', N'角色类型', N'1', N'2022-02-16 13:01:46.0000000', N'1', N'2022-02-16 13:01:46.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'146', N'文件存储器', N'infra_file_storage', N'0', N'文件存储器', N'1', N'2022-03-15 00:24:38.0000000', N'1', N'2022-03-15 00:24:38.0000000', N'0')
GO

INSERT INTO [dbo].[system_dict_type] ([id], [name], [type], [status], [remark], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'147', N'OAuth 2.0 授权类型', N'system_oauth2_grant_type', N'0', N'OAuth 2.0 授权类型（模式）', N'1', N'2022-05-12 00:20:52.0000000', N'1', N'2022-05-11 16:25:49.0000000', N'0')
GO

SET IDENTITY_INSERT [dbo].[system_dict_type] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_error_code
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_error_code]') AND type IN ('U'))
	DROP TABLE [dbo].[system_error_code]
GO

CREATE TABLE [dbo].[system_error_code] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [type] tinyint  NOT NULL,
  [application_name] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [code] int  NOT NULL,
  [message] nvarchar(512) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [memo] nvarchar(512) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_error_code] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'错误码编号',
'SCHEMA', N'dbo',
'TABLE', N'system_error_code',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'错误码类型',
'SCHEMA', N'dbo',
'TABLE', N'system_error_code',
'COLUMN', N'type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'应用名',
'SCHEMA', N'dbo',
'TABLE', N'system_error_code',
'COLUMN', N'application_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'错误码编码',
'SCHEMA', N'dbo',
'TABLE', N'system_error_code',
'COLUMN', N'code'
GO

EXEC sp_addextendedproperty
'MS_Description', N'错误码错误提示',
'SCHEMA', N'dbo',
'TABLE', N'system_error_code',
'COLUMN', N'message'
GO

EXEC sp_addextendedproperty
'MS_Description', N'备注',
'SCHEMA', N'dbo',
'TABLE', N'system_error_code',
'COLUMN', N'memo'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建者',
'SCHEMA', N'dbo',
'TABLE', N'system_error_code',
'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建时间',
'SCHEMA', N'dbo',
'TABLE', N'system_error_code',
'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新者',
'SCHEMA', N'dbo',
'TABLE', N'system_error_code',
'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新时间',
'SCHEMA', N'dbo',
'TABLE', N'system_error_code',
'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'system_error_code',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'错误码表',
'SCHEMA', N'dbo',
'TABLE', N'system_error_code'
GO


-- ----------------------------
-- Records of system_error_code
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_error_code] ON
GO

SET IDENTITY_INSERT [dbo].[system_error_code] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_login_log
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_login_log]') AND type IN ('U'))
	DROP TABLE [dbo].[system_login_log]
GO

CREATE TABLE [dbo].[system_login_log] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [log_type] bigint  NOT NULL,
  [trace_id] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [user_id] bigint  NOT NULL,
  [user_type] tinyint  NOT NULL,
  [username] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [result] tinyint  NOT NULL,
  [user_ip] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [user_agent] nvarchar(512) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_login_log] SET (LOCK_ESCALATION = TABLE)
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
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'system_login_log',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'system_login_log',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'系统访问记录',
'SCHEMA', N'dbo',
'TABLE', N'system_login_log'
GO


-- ----------------------------
-- Records of system_login_log
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_login_log] ON
GO

SET IDENTITY_INSERT [dbo].[system_login_log] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_menu
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_menu]') AND type IN ('U'))
	DROP TABLE [dbo].[system_menu]
GO

CREATE TABLE [dbo].[system_menu] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [name] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [permission] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [type] tinyint  NOT NULL,
  [sort] int  NOT NULL,
  [parent_id] bigint  NOT NULL,
  [path] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [icon] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [component] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [status] tinyint  NOT NULL,
  [visible] varchar(1) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [keep_alive] varchar(1) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_menu] SET (LOCK_ESCALATION = TABLE)
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
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_menu] ON
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1', N'系统管理', N'', N'1', N'10', N'0', N'/system', N'system', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-05-12 18:11:34.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'2', N'基础设施', N'', N'1', N'20', N'0', N'/infra', N'monitor', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-05-12 18:11:34.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'5', N'OA 示例', N'', N'1', N'40', N'1185', N'oa', N'people', N'', N'0', N'1', N'1', N'admin', N'2021-09-20 16:26:19.0000000', N'1', N'2022-05-12 18:11:34.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'100', N'用户管理', N'system:user:list', N'2', N'1', N'1', N'user', N'user', N'system/user/index', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'101', N'角色管理', N'', N'2', N'2', N'1', N'role', N'peoples', N'system/role/index', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'102', N'菜单管理', N'', N'2', N'3', N'1', N'menu', N'tree-table', N'system/menu/index', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'103', N'部门管理', N'', N'2', N'4', N'1', N'dept', N'tree', N'system/dept/index', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'104', N'岗位管理', N'', N'2', N'5', N'1', N'post', N'post', N'system/post/index', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'105', N'字典管理', N'', N'2', N'6', N'1', N'dict', N'dict', N'system/dict/index', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'106', N'配置管理', N'', N'2', N'6', N'2', N'config', N'edit', N'infra/config/index', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'107', N'通知公告', N'', N'2', N'8', N'1', N'notice', N'message', N'system/notice/index', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'108', N'审计日志', N'', N'1', N'9', N'1', N'log', N'log', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'109', N'令牌管理', N'', N'2', N'2', N'1261', N'token', N'online', N'system/oauth2/token/index', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-05-11 23:31:42.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'110', N'定时任务', N'', N'2', N'12', N'2', N'job', N'job', N'infra/job/index', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'111', N'MySQL 监控', N'', N'2', N'9', N'2', N'druid', N'druid', N'infra/druid/index', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'112', N'Java 监控', N'', N'2', N'11', N'2', N'admin-server', N'server', N'infra/server/index', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'113', N'Redis 监控', N'', N'2', N'10', N'2', N'redis', N'redis', N'infra/redis/index', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'114', N'表单构建', N'infra:build:list', N'2', N'2', N'2', N'build', N'build', N'infra/build/index', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'115', N'代码生成', N'infra:codegen:query', N'2', N'1', N'2', N'codegen', N'code', N'infra/codegen/index', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'116', N'系统接口', N'infra:swagger:list', N'2', N'3', N'2', N'swagger', N'swagger', N'infra/swagger/index', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'500', N'操作日志', N'', N'2', N'1', N'108', N'operate-log', N'form', N'system/operatelog/index', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'501', N'登录日志', N'', N'2', N'2', N'108', N'login-log', N'logininfor', N'system/loginlog/index', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1001', N'用户查询', N'system:user:query', N'3', N'1', N'100', N'', N'#', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1002', N'用户新增', N'system:user:create', N'3', N'2', N'100', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1003', N'用户修改', N'system:user:update', N'3', N'3', N'100', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1004', N'用户删除', N'system:user:delete', N'3', N'4', N'100', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1005', N'用户导出', N'system:user:export', N'3', N'5', N'100', N'', N'#', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1006', N'用户导入', N'system:user:import', N'3', N'6', N'100', N'', N'#', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1007', N'重置密码', N'system:user:update-password', N'3', N'7', N'100', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1008', N'角色查询', N'system:role:query', N'3', N'1', N'101', N'', N'#', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1009', N'角色新增', N'system:role:create', N'3', N'2', N'101', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1010', N'角色修改', N'system:role:update', N'3', N'3', N'101', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1011', N'角色删除', N'system:role:delete', N'3', N'4', N'101', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1012', N'角色导出', N'system:role:export', N'3', N'5', N'101', N'', N'#', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1013', N'菜单查询', N'system:menu:query', N'3', N'1', N'102', N'', N'#', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1014', N'菜单新增', N'system:menu:create', N'3', N'2', N'102', N'', N'#', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1015', N'菜单修改', N'system:menu:update', N'3', N'3', N'102', N'', N'#', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1016', N'菜单删除', N'system:menu:delete', N'3', N'4', N'102', N'', N'#', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1017', N'部门查询', N'system:dept:query', N'3', N'1', N'103', N'', N'#', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1018', N'部门新增', N'system:dept:create', N'3', N'2', N'103', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1019', N'部门修改', N'system:dept:update', N'3', N'3', N'103', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1020', N'部门删除', N'system:dept:delete', N'3', N'4', N'103', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1021', N'岗位查询', N'system:post:query', N'3', N'1', N'104', N'', N'#', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1022', N'岗位新增', N'system:post:create', N'3', N'2', N'104', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1023', N'岗位修改', N'system:post:update', N'3', N'3', N'104', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1024', N'岗位删除', N'system:post:delete', N'3', N'4', N'104', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1025', N'岗位导出', N'system:post:export', N'3', N'5', N'104', N'', N'#', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1026', N'字典查询', N'system:dict:query', N'3', N'1', N'105', N'#', N'#', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1027', N'字典新增', N'system:dict:create', N'3', N'2', N'105', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1028', N'字典修改', N'system:dict:update', N'3', N'3', N'105', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1029', N'字典删除', N'system:dict:delete', N'3', N'4', N'105', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1030', N'字典导出', N'system:dict:export', N'3', N'5', N'105', N'#', N'#', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1031', N'配置查询', N'infra:config:query', N'3', N'1', N'106', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1032', N'配置新增', N'infra:config:create', N'3', N'2', N'106', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1033', N'配置修改', N'infra:config:update', N'3', N'3', N'106', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1034', N'配置删除', N'infra:config:delete', N'3', N'4', N'106', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1035', N'配置导出', N'infra:config:export', N'3', N'5', N'106', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1036', N'公告查询', N'system:notice:query', N'3', N'1', N'107', N'#', N'#', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1037', N'公告新增', N'system:notice:create', N'3', N'2', N'107', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1038', N'公告修改', N'system:notice:update', N'3', N'3', N'107', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1039', N'公告删除', N'system:notice:delete', N'3', N'4', N'107', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1040', N'操作查询', N'system:operate-log:query', N'3', N'1', N'500', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1042', N'日志导出', N'system:operate-log:export', N'3', N'2', N'500', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1043', N'登录查询', N'system:login-log:query', N'3', N'1', N'501', N'#', N'#', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1045', N'日志导出', N'system:login-log:export', N'3', N'3', N'501', N'#', N'#', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1046', N'令牌列表', N'system:oauth2-token:page', N'3', N'1', N'109', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-05-09 23:54:42.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1048', N'令牌删除', N'system:oauth2-token:delete', N'3', N'2', N'109', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-05-09 23:54:53.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1050', N'任务新增', N'infra:job:create', N'3', N'2', N'110', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1051', N'任务修改', N'infra:job:update', N'3', N'3', N'110', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1052', N'任务删除', N'infra:job:delete', N'3', N'4', N'110', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1053', N'状态修改', N'infra:job:update', N'3', N'5', N'110', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1054', N'任务导出', N'infra:job:export', N'3', N'7', N'110', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1056', N'生成修改', N'infra:codegen:update', N'3', N'2', N'115', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1057', N'生成删除', N'infra:codegen:delete', N'3', N'3', N'115', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1058', N'导入代码', N'infra:codegen:create', N'3', N'2', N'115', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1059', N'预览代码', N'infra:codegen:preview', N'3', N'4', N'115', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1060', N'生成代码', N'infra:codegen:download', N'3', N'5', N'115', N'', N'', N'', N'0', N'1', N'1', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1063', N'设置角色菜单权限', N'system:permission:assign-role-menu', N'3', N'6', N'101', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-01-06 17:53:44.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1064', N'设置角色数据权限', N'system:permission:assign-role-data-scope', N'3', N'7', N'101', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-01-06 17:56:31.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1065', N'设置用户角色', N'system:permission:assign-user-role', N'3', N'8', N'101', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-01-07 10:23:28.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1066', N'获得 Redis 监控信息', N'infra:redis:get-monitor-info', N'3', N'1', N'113', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-01-26 01:02:31.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1067', N'获得 Redis Key 列表', N'infra:redis:get-key-list', N'3', N'2', N'113', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-01-26 01:02:52.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1070', N'代码生成示例', N'infra:test-demo:query', N'2', N'1', N'2', N'test-demo', N'validCode', N'infra/testDemo/index', N'0', N'1', N'1', N'', N'2021-02-06 12:42:49.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1071', N'测试示例表创建', N'infra:test-demo:create', N'3', N'1', N'1070', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-02-06 12:42:49.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1072', N'测试示例表更新', N'infra:test-demo:update', N'3', N'2', N'1070', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-02-06 12:42:49.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1073', N'测试示例表删除', N'infra:test-demo:delete', N'3', N'3', N'1070', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-02-06 12:42:49.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1074', N'测试示例表导出', N'infra:test-demo:export', N'3', N'4', N'1070', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-02-06 12:42:49.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1075', N'任务触发', N'infra:job:trigger', N'3', N'8', N'110', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-02-07 13:03:10.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1076', N'数据库文档', N'', N'2', N'4', N'2', N'db-doc', N'table', N'infra/dbDoc/index', N'0', N'1', N'1', N'', N'2021-02-08 01:41:47.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1077', N'监控平台', N'', N'2', N'13', N'2', N'skywalking', N'eye-open', N'infra/skywalking/index', N'0', N'1', N'1', N'', N'2021-02-08 20:41:31.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1078', N'访问日志', N'', N'2', N'1', N'1083', N'api-access-log', N'log', N'infra/apiAccessLog/index', N'0', N'1', N'1', N'', N'2021-02-26 01:32:59.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1082', N'日志导出', N'infra:api-access-log:export', N'3', N'2', N'1078', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-02-26 01:32:59.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1083', N'API 日志', N'', N'2', N'8', N'2', N'log', N'log', N'', N'0', N'1', N'1', N'', N'2021-02-26 02:18:24.0000000', N'1', N'2022-05-12 18:11:34.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1084', N'错误日志', N'infra:api-error-log:query', N'2', N'2', N'1083', N'api-error-log', N'log', N'infra/apiErrorLog/index', N'0', N'1', N'1', N'', N'2021-02-26 07:53:20.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1085', N'日志处理', N'infra:api-error-log:update-status', N'3', N'2', N'1084', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-02-26 07:53:20.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1086', N'日志导出', N'infra:api-error-log:export', N'3', N'3', N'1084', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-02-26 07:53:20.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1087', N'任务查询', N'infra:job:query', N'3', N'1', N'110', N'', N'', N'', N'0', N'1', N'1', N'1', N'2021-03-10 01:26:19.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1088', N'日志查询', N'infra:api-access-log:query', N'3', N'1', N'1078', N'', N'', N'', N'0', N'1', N'1', N'1', N'2021-03-10 01:28:04.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1089', N'日志查询', N'infra:api-error-log:query', N'3', N'1', N'1084', N'', N'', N'', N'0', N'1', N'1', N'1', N'2021-03-10 01:29:09.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1090', N'文件列表', N'', N'2', N'5', N'1243', N'file', N'upload', N'infra/file/index', N'0', N'1', N'1', N'', N'2021-03-12 20:16:20.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1091', N'文件查询', N'infra:file:query', N'3', N'1', N'1090', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-03-12 20:16:20.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1092', N'文件删除', N'infra:file:delete', N'3', N'4', N'1090', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-03-12 20:16:20.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1093', N'短信管理', N'', N'1', N'11', N'1', N'sms', N'validCode', N'', N'0', N'1', N'1', N'1', N'2021-04-05 01:10:16.0000000', N'1', N'2022-05-12 18:11:34.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1094', N'短信渠道', N'', N'2', N'0', N'1093', N'sms-channel', N'phone', N'system/sms/smsChannel', N'0', N'1', N'1', N'', N'2021-04-01 11:07:15.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1095', N'短信渠道查询', N'system:sms-channel:query', N'3', N'1', N'1094', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-04-01 11:07:15.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1096', N'短信渠道创建', N'system:sms-channel:create', N'3', N'2', N'1094', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-04-01 11:07:15.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1097', N'短信渠道更新', N'system:sms-channel:update', N'3', N'3', N'1094', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-04-01 11:07:15.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1098', N'短信渠道删除', N'system:sms-channel:delete', N'3', N'4', N'1094', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-04-01 11:07:15.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1100', N'短信模板', N'', N'2', N'1', N'1093', N'sms-template', N'phone', N'system/sms/smsTemplate', N'0', N'1', N'1', N'', N'2021-04-01 17:35:17.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1101', N'短信模板查询', N'system:sms-template:query', N'3', N'1', N'1100', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-04-01 17:35:17.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1102', N'短信模板创建', N'system:sms-template:create', N'3', N'2', N'1100', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-04-01 17:35:17.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1103', N'短信模板更新', N'system:sms-template:update', N'3', N'3', N'1100', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-04-01 17:35:17.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1104', N'短信模板删除', N'system:sms-template:delete', N'3', N'4', N'1100', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-04-01 17:35:17.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1105', N'短信模板导出', N'system:sms-template:export', N'3', N'5', N'1100', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-04-01 17:35:17.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1106', N'发送测试短信', N'system:sms-template:send-sms', N'3', N'6', N'1100', N'', N'', N'', N'0', N'1', N'1', N'1', N'2021-04-11 00:26:40.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1107', N'短信日志', N'', N'2', N'2', N'1093', N'sms-log', N'phone', N'system/sms/smsLog', N'0', N'1', N'1', N'', N'2021-04-11 08:37:05.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1108', N'短信日志查询', N'system:sms-log:query', N'3', N'1', N'1107', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-04-11 08:37:05.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1109', N'短信日志导出', N'system:sms-log:export', N'3', N'5', N'1107', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-04-11 08:37:05.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1110', N'错误码管理', N'', N'2', N'12', N'1', N'error-code', N'code', N'system/errorCode/index', N'0', N'1', N'1', N'', N'2021-04-13 21:46:42.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1111', N'错误码查询', N'system:error-code:query', N'3', N'1', N'1110', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-04-13 21:46:42.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1112', N'错误码创建', N'system:error-code:create', N'3', N'2', N'1110', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-04-13 21:46:42.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1113', N'错误码更新', N'system:error-code:update', N'3', N'3', N'1110', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-04-13 21:46:42.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1114', N'错误码删除', N'system:error-code:delete', N'3', N'4', N'1110', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-04-13 21:46:42.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1115', N'错误码导出', N'system:error-code:export', N'3', N'5', N'1110', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-04-13 21:46:42.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1117', N'支付管理', N'', N'1', N'11', N'0', N'/pay', N'money', N'', N'0', N'1', N'1', N'1', N'2021-12-25 16:43:41.0000000', N'1', N'2022-05-12 18:11:34.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1118', N'请假查询', N'', N'2', N'0', N'5', N'leave', N'user', N'bpm/oa/leave/index', N'0', N'1', N'1', N'', N'2021-09-20 08:51:03.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1119', N'请假申请查询', N'bpm:oa-leave:query', N'3', N'1', N'1118', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-09-20 08:51:03.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1120', N'请假申请创建', N'bpm:oa-leave:create', N'3', N'2', N'1118', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-09-20 08:51:03.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1126', N'应用信息', N'', N'2', N'1', N'1117', N'app', N'table', N'pay/app/index', N'0', N'1', N'1', N'', N'2021-11-10 01:13:30.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1127', N'支付应用信息查询', N'pay:app:query', N'3', N'1', N'1126', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-11-10 01:13:31.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1128', N'支付应用信息创建', N'pay:app:create', N'3', N'2', N'1126', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-11-10 01:13:31.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1129', N'支付应用信息更新', N'pay:app:update', N'3', N'3', N'1126', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-11-10 01:13:31.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1130', N'支付应用信息删除', N'pay:app:delete', N'3', N'4', N'1126', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-11-10 01:13:31.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1131', N'支付应用信息导出', N'pay:app:export', N'3', N'5', N'1126', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-11-10 01:13:31.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1132', N'秘钥解析', N'pay:channel:parsing', N'3', N'6', N'1129', N'', N'', N'', N'0', N'1', N'1', N'1', N'2021-11-08 15:15:47.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1133', N'支付商户信息查询', N'pay:merchant:query', N'3', N'1', N'1132', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-11-10 01:13:41.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1134', N'支付商户信息创建', N'pay:merchant:create', N'3', N'2', N'1132', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-11-10 01:13:41.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1135', N'支付商户信息更新', N'pay:merchant:update', N'3', N'3', N'1132', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-11-10 01:13:41.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1136', N'支付商户信息删除', N'pay:merchant:delete', N'3', N'4', N'1132', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-11-10 01:13:41.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1137', N'支付商户信息导出', N'pay:merchant:export', N'3', N'5', N'1132', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-11-10 01:13:41.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1138', N'租户列表', N'', N'2', N'0', N'1224', N'list', N'peoples', N'system/tenant/index', N'0', N'1', N'1', N'', N'2021-12-14 12:31:43.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1139', N'租户查询', N'system:tenant:query', N'3', N'1', N'1138', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-14 12:31:44.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1140', N'租户创建', N'system:tenant:create', N'3', N'2', N'1138', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-14 12:31:44.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1141', N'租户更新', N'system:tenant:update', N'3', N'3', N'1138', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-14 12:31:44.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1142', N'租户删除', N'system:tenant:delete', N'3', N'4', N'1138', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-14 12:31:44.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1143', N'租户导出', N'system:tenant:export', N'3', N'5', N'1138', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-14 12:31:44.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1150', N'秘钥解析', N'', N'3', N'6', N'1129', N'', N'', N'', N'0', N'1', N'1', N'1', N'2021-11-08 15:15:47.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1161', N'退款订单', N'', N'2', N'3', N'1117', N'refund', N'order', N'pay/refund/index', N'0', N'1', N'1', N'', N'2021-12-25 08:29:07.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1162', N'退款订单查询', N'pay:refund:query', N'3', N'1', N'1161', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-25 08:29:07.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1163', N'退款订单创建', N'pay:refund:create', N'3', N'2', N'1161', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-25 08:29:07.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1164', N'退款订单更新', N'pay:refund:update', N'3', N'3', N'1161', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-25 08:29:07.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1165', N'退款订单删除', N'pay:refund:delete', N'3', N'4', N'1161', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-25 08:29:07.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1166', N'退款订单导出', N'pay:refund:export', N'3', N'5', N'1161', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-25 08:29:07.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1173', N'支付订单', N'', N'2', N'2', N'1117', N'order', N'pay', N'pay/order/index', N'0', N'1', N'1', N'', N'2021-12-25 08:49:43.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1174', N'支付订单查询', N'pay:order:query', N'3', N'1', N'1173', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-25 08:49:43.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1175', N'支付订单创建', N'pay:order:create', N'3', N'2', N'1173', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-25 08:49:43.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1176', N'支付订单更新', N'pay:order:update', N'3', N'3', N'1173', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-25 08:49:43.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1177', N'支付订单删除', N'pay:order:delete', N'3', N'4', N'1173', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-25 08:49:43.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1178', N'支付订单导出', N'pay:order:export', N'3', N'5', N'1173', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-25 08:49:43.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1179', N'商户信息', N'', N'2', N'0', N'1117', N'merchant', N'merchant', N'pay/merchant/index', N'0', N'1', N'1', N'', N'2021-12-25 09:01:44.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1180', N'支付商户信息查询', N'pay:merchant:query', N'3', N'1', N'1179', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-25 09:01:44.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1181', N'支付商户信息创建', N'pay:merchant:create', N'3', N'2', N'1179', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-25 09:01:44.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1182', N'支付商户信息更新', N'pay:merchant:update', N'3', N'3', N'1179', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-25 09:01:44.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1183', N'支付商户信息删除', N'', N'3', N'4', N'1179', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-25 09:01:44.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1184', N'支付商户信息导出', N'pay:merchant:export', N'3', N'5', N'1179', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-25 09:01:44.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1185', N'工作流程', N'', N'1', N'50', N'0', N'/bpm', N'tool', N'', N'0', N'1', N'1', N'1', N'2021-12-30 20:26:36.0000000', N'103', N'2022-05-12 18:11:34.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1186', N'流程管理', N'', N'1', N'10', N'1185', N'manager', N'nested', N'', N'0', N'1', N'1', N'1', N'2021-12-30 20:28:30.0000000', N'1', N'2022-05-12 18:11:34.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1187', N'流程表单', N'', N'2', N'0', N'1186', N'form', N'form', N'bpm/form/index', N'0', N'1', N'1', N'', N'2021-12-30 12:38:22.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1188', N'表单查询', N'bpm:form:query', N'3', N'1', N'1187', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-30 12:38:22.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1189', N'表单创建', N'bpm:form:create', N'3', N'2', N'1187', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-30 12:38:22.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1190', N'表单更新', N'bpm:form:update', N'3', N'3', N'1187', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-30 12:38:22.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1191', N'表单删除', N'bpm:form:delete', N'3', N'4', N'1187', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-30 12:38:22.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1192', N'表单导出', N'bpm:form:export', N'3', N'5', N'1187', N'', N'', N'', N'0', N'1', N'1', N'', N'2021-12-30 12:38:22.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1193', N'流程模型', N'', N'2', N'5', N'1186', N'model', N'guide', N'bpm/model/index', N'0', N'1', N'1', N'1', N'2021-12-31 23:24:58.0000000', N'103', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1194', N'模型查询', N'bpm:model:query', N'3', N'1', N'1193', N'', N'', N'', N'0', N'1', N'1', N'1', N'2022-01-03 19:01:10.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1195', N'模型创建', N'bpm:model:create', N'3', N'2', N'1193', N'', N'', N'', N'0', N'1', N'1', N'1', N'2022-01-03 19:01:24.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1196', N'模型导入', N'bpm:model:import', N'3', N'3', N'1193', N'', N'', N'', N'0', N'1', N'1', N'1', N'2022-01-03 19:01:35.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1197', N'模型更新', N'bpm:model:update', N'3', N'4', N'1193', N'', N'', N'', N'0', N'1', N'1', N'1', N'2022-01-03 19:02:28.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1198', N'模型删除', N'bpm:model:delete', N'3', N'5', N'1193', N'', N'', N'', N'0', N'1', N'1', N'1', N'2022-01-03 19:02:43.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1199', N'模型发布', N'bpm:model:deploy', N'3', N'6', N'1193', N'', N'', N'', N'0', N'1', N'1', N'1', N'2022-01-03 19:03:24.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1200', N'任务管理', N'', N'1', N'20', N'1185', N'task', N'cascader', N'', N'0', N'1', N'1', N'1', N'2022-01-07 23:51:48.0000000', N'1', N'2022-05-12 18:11:34.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1201', N'我的流程', N'', N'2', N'0', N'1200', N'my', N'people', N'bpm/processInstance/index', N'0', N'1', N'1', N'', N'2022-01-07 15:53:44.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1202', N'流程实例的查询', N'bpm:process-instance:query', N'3', N'1', N'1201', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-01-07 15:53:44.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1207', N'待办任务', N'', N'2', N'10', N'1200', N'todo', N'eye-open', N'bpm/task/todo', N'0', N'1', N'1', N'1', N'2022-01-08 10:33:37.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1208', N'已办任务', N'', N'2', N'20', N'1200', N'done', N'eye', N'bpm/task/done', N'0', N'1', N'1', N'1', N'2022-01-08 10:34:13.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1209', N'用户分组', N'', N'2', N'2', N'1186', N'user-group', N'people', N'bpm/group/index', N'0', N'1', N'1', N'', N'2022-01-14 02:14:20.0000000', N'103', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1210', N'用户组查询', N'bpm:user-group:query', N'3', N'1', N'1209', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-01-14 02:14:20.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1211', N'用户组创建', N'bpm:user-group:create', N'3', N'2', N'1209', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-01-14 02:14:20.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1212', N'用户组更新', N'bpm:user-group:update', N'3', N'3', N'1209', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-01-14 02:14:20.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1213', N'用户组删除', N'bpm:user-group:delete', N'3', N'4', N'1209', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-01-14 02:14:20.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1215', N'流程定义查询', N'bpm:process-definition:query', N'3', N'10', N'1193', N'', N'', N'', N'0', N'1', N'1', N'1', N'2022-01-23 00:21:43.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1216', N'流程任务分配规则查询', N'bpm:task-assign-rule:query', N'3', N'20', N'1193', N'', N'', N'', N'0', N'1', N'1', N'1', N'2022-01-23 00:26:53.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1217', N'流程任务分配规则创建', N'bpm:task-assign-rule:create', N'3', N'21', N'1193', N'', N'', N'', N'0', N'1', N'1', N'1', N'2022-01-23 00:28:15.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1218', N'流程任务分配规则更新', N'bpm:task-assign-rule:update', N'3', N'22', N'1193', N'', N'', N'', N'0', N'1', N'1', N'1', N'2022-01-23 00:28:41.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1219', N'流程实例的创建', N'bpm:process-instance:create', N'3', N'2', N'1201', N'', N'', N'', N'0', N'1', N'1', N'1', N'2022-01-23 00:36:15.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1220', N'流程实例的取消', N'bpm:process-instance:cancel', N'3', N'3', N'1201', N'', N'', N'', N'0', N'1', N'1', N'1', N'2022-01-23 00:36:33.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1221', N'流程任务的查询', N'bpm:task:query', N'3', N'1', N'1207', N'', N'', N'', N'0', N'1', N'1', N'1', N'2022-01-23 00:38:52.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1222', N'流程任务的更新', N'bpm:task:update', N'3', N'2', N'1207', N'', N'', N'', N'0', N'1', N'1', N'1', N'2022-01-23 00:39:24.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1224', N'租户管理', N'', N'2', N'0', N'1', N'tenant', N'peoples', N'', N'0', N'1', N'1', N'1', N'2022-02-20 01:41:13.0000000', N'1', N'2022-05-12 18:11:34.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1225', N'租户套餐', N'', N'2', N'0', N'1224', N'package', N'eye', N'system/tenantPackage/index', N'0', N'1', N'1', N'', N'2022-02-19 17:44:06.0000000', N'1', N'2022-04-21 01:21:25.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1226', N'租户套餐查询', N'system:tenant-package:query', N'3', N'1', N'1225', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-02-19 17:44:06.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1227', N'租户套餐创建', N'system:tenant-package:create', N'3', N'2', N'1225', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-02-19 17:44:06.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1228', N'租户套餐更新', N'system:tenant-package:update', N'3', N'3', N'1225', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-02-19 17:44:06.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1229', N'租户套餐删除', N'system:tenant-package:delete', N'3', N'4', N'1225', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-02-19 17:44:06.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1237', N'文件配置', N'', N'2', N'0', N'1243', N'file-config', N'config', N'infra/fileConfig/index', N'0', N'1', N'1', N'', N'2022-03-15 14:35:28.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1238', N'文件配置查询', N'infra:file-config:query', N'3', N'1', N'1237', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-03-15 14:35:28.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1239', N'文件配置创建', N'infra:file-config:create', N'3', N'2', N'1237', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-03-15 14:35:28.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1240', N'文件配置更新', N'infra:file-config:update', N'3', N'3', N'1237', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-03-15 14:35:28.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1241', N'文件配置删除', N'infra:file-config:delete', N'3', N'4', N'1237', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-03-15 14:35:28.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1242', N'文件配置导出', N'infra:file-config:export', N'3', N'5', N'1237', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-03-15 14:35:28.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1243', N'文件管理', N'', N'2', N'5', N'2', N'file', N'download', N'', N'0', N'1', N'1', N'1', N'2022-03-16 23:47:40.0000000', N'1', N'2022-05-12 18:11:34.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1247', N'敏感词管理', N'', N'2', N'13', N'1', N'sensitive-word', N'education', N'system/sensitiveWord/index', N'0', N'1', N'1', N'', N'2022-04-07 16:55:03.0000000', N'1', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1248', N'敏感词查询', N'system:sensitive-word:query', N'3', N'1', N'1247', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-04-07 16:55:03.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1249', N'敏感词创建', N'system:sensitive-word:create', N'3', N'2', N'1247', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-04-07 16:55:03.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1250', N'敏感词更新', N'system:sensitive-word:update', N'3', N'3', N'1247', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-04-07 16:55:03.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1251', N'敏感词删除', N'system:sensitive-word:delete', N'3', N'4', N'1247', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-04-07 16:55:03.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1252', N'敏感词导出', N'system:sensitive-word:export', N'3', N'5', N'1247', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-04-07 16:55:03.0000000', N'', N'2022-04-20 17:03:10.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1254', N'作者动态', N'', N'1', N'0', N'0', N'https://www.iocoder.cn', N'people', N'', N'0', N'1', N'1', N'1', N'2022-04-23 01:03:15.0000000', N'1', N'2022-05-12 18:11:34.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1255', N'数据源配置', N'', N'2', N'1', N'2', N'data-source-config', N'rate', N'infra/dataSourceConfig/index', N'0', N'1', N'1', N'', N'2022-04-27 14:37:32.0000000', N'1', N'2022-04-27 22:42:06.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1256', N'数据源配置查询', N'infra:data-source-config:query', N'3', N'1', N'1255', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-04-27 14:37:32.0000000', N'', N'2022-04-27 14:37:32.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1257', N'数据源配置创建', N'infra:data-source-config:create', N'3', N'2', N'1255', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-04-27 14:37:32.0000000', N'', N'2022-04-27 14:37:32.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1258', N'数据源配置更新', N'infra:data-source-config:update', N'3', N'3', N'1255', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-04-27 14:37:32.0000000', N'', N'2022-04-27 14:37:32.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1259', N'数据源配置删除', N'infra:data-source-config:delete', N'3', N'4', N'1255', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-04-27 14:37:32.0000000', N'', N'2022-04-27 14:37:32.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1260', N'数据源配置导出', N'infra:data-source-config:export', N'3', N'5', N'1255', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-04-27 14:37:32.0000000', N'', N'2022-04-27 14:37:32.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1261', N'OAuth 2.0', N'', N'1', N'10', N'1', N'oauth2', N'people', N'', N'0', N'1', N'1', N'1', N'2022-05-09 23:38:17.0000000', N'1', N'2022-05-12 18:11:34.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1263', N'应用管理', N'', N'2', N'0', N'1261', N'oauth2/application', N'tool', N'system/oauth2/client/index', N'0', N'1', N'1', N'', N'2022-05-10 16:26:33.0000000', N'1', N'2022-05-11 23:31:36.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1264', N'客户端查询', N'system:oauth2-client:query', N'3', N'1', N'1263', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-05-10 16:26:33.0000000', N'1', N'2022-05-11 00:31:06.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1265', N'客户端创建', N'system:oauth2-client:create', N'3', N'2', N'1263', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-05-10 16:26:33.0000000', N'1', N'2022-05-11 00:31:23.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1266', N'客户端更新', N'system:oauth2-client:update', N'3', N'3', N'1263', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-05-10 16:26:33.0000000', N'1', N'2022-05-11 00:31:28.0000000', N'0')
GO

INSERT INTO [dbo].[system_menu] ([id], [name], [permission], [type], [sort], [parent_id], [path], [icon], [component], [status], [visible], [keep_alive], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1267', N'客户端删除', N'system:oauth2-client:delete', N'3', N'4', N'1263', N'', N'', N'', N'0', N'1', N'1', N'', N'2022-05-10 16:26:33.0000000', N'1', N'2022-05-11 00:31:33.0000000', N'0')
GO

SET IDENTITY_INSERT [dbo].[system_menu] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_notice
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_notice]') AND type IN ('U'))
	DROP TABLE [dbo].[system_notice]
GO

CREATE TABLE [dbo].[system_notice] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [title] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [content] nvarchar(max) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [type] tinyint  NOT NULL,
  [status] tinyint  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_notice] SET (LOCK_ESCALATION = TABLE)
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
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'system_notice',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'system_notice',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'通知公告表',
'SCHEMA', N'dbo',
'TABLE', N'system_notice'
GO


-- ----------------------------
-- Records of system_notice
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_notice] ON
GO

INSERT INTO [dbo].[system_notice] ([id], [title], [content], [type], [status], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'1', N'温馨提醒：2018-07-01 若依新版本发布啦', N'<p>新版本内容133</p>', N'2', N'0', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-02-15 19:47:20.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_notice] ([id], [title], [content], [type], [status], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'2', N'维护通知：2018-07-01 若依系统凌晨维护', N'维护内容', N'1', N'0', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2021-12-15 05:02:22.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_notice] ([id], [title], [content], [type], [status], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'4', N'我是测试标题', N'<p>哈哈哈哈123</p>', N'1', N'0', N'110', N'2022-02-22 01:01:25.0000000', N'110', N'2022-02-22 01:01:46.0000000', N'121', N'0')
GO

SET IDENTITY_INSERT [dbo].[system_notice] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_oauth2_access_token
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_oauth2_access_token]') AND type IN ('U'))
	DROP TABLE [dbo].[system_oauth2_access_token]
GO

CREATE TABLE [dbo].[system_oauth2_access_token] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [user_id] bigint  NOT NULL,
  [access_token] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [refresh_token] nvarchar(32) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [user_type] tinyint  NOT NULL,
  [client_id] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [expires_time] datetime2(7)  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL,
  [tenant_id] bigint DEFAULT 0 NOT NULL,
  [scopes] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT '' NULL
)
GO

ALTER TABLE [dbo].[system_oauth2_access_token] SET (LOCK_ESCALATION = TABLE)
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
'MS_Description', N'用户类型',
'SCHEMA', N'dbo',
'TABLE', N'system_oauth2_access_token',
'COLUMN', N'user_type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'客户端编号',
'SCHEMA', N'dbo',
'TABLE', N'system_oauth2_access_token',
'COLUMN', N'client_id'
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
'MS_Description', N'授权范围',
'SCHEMA', N'dbo',
'TABLE', N'system_oauth2_access_token',
'COLUMN', N'scopes'
GO

EXEC sp_addextendedproperty
'MS_Description', N'刷新令牌',
'SCHEMA', N'dbo',
'TABLE', N'system_oauth2_access_token'
GO


-- ----------------------------
-- Records of system_oauth2_access_token
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_oauth2_access_token] ON
GO

SET IDENTITY_INSERT [dbo].[system_oauth2_access_token] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_oauth2_approve
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_oauth2_approve]') AND type IN ('U'))
	DROP TABLE [dbo].[system_oauth2_approve]
GO

CREATE TABLE [dbo].[system_oauth2_approve] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [user_id] bigint  NOT NULL,
  [user_type] tinyint  NOT NULL,
  [client_id] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [scope] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [approved] varchar(1) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [expires_time] datetime2(7)  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL,
  [tenant_id] bigint  NOT NULL
)
GO

ALTER TABLE [dbo].[system_oauth2_approve] SET (LOCK_ESCALATION = TABLE)
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
-- Records of system_oauth2_approve
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_oauth2_approve] ON
GO

SET IDENTITY_INSERT [dbo].[system_oauth2_approve] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_oauth2_client
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_oauth2_client]') AND type IN ('U'))
	DROP TABLE [dbo].[system_oauth2_client]
GO

CREATE TABLE [dbo].[system_oauth2_client] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [client_id] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [secret] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [name] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [logo] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [description] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [status] tinyint  NOT NULL,
  [access_token_validity_seconds] int  NOT NULL,
  [refresh_token_validity_seconds] int  NOT NULL,
  [redirect_uris] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [auto_approve_scopes] nvarchar(max) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT '' NOT NULL,
  [authorized_grant_types] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [scopes] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT '' NULL,
  [authorities] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [resource_ids] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [additional_information] nvarchar(max) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_oauth2_client] SET (LOCK_ESCALATION = TABLE)
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
'MS_Description', N'自动通过的授权范围',
'SCHEMA', N'dbo',
'TABLE', N'system_oauth2_client',
'COLUMN', N'auto_approve_scopes'
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
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_oauth2_client] ON
GO

INSERT INTO [dbo].[system_oauth2_client] ([id], [client_id], [secret], [name], [logo], [description], [status], [access_token_validity_seconds], [refresh_token_validity_seconds], [redirect_uris], [auto_approve_scopes], [authorized_grant_types], [scopes], [authorities], [resource_ids], [additional_information], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1', N'default', N'admin123', N'芋道源码', N'http://test.yudao.iocoder.cn/a5e2e244368878a366b516805a4aabf1.png', N'我是描述', N'0', N'180', N'8640', N'["https://www.iocoder.cn","https://doc.iocoder.cn"]', N'', N'["password","authorization_code","implicit","refresh_token"]', N'["user.read", "user.write"]', N'["system:user:query"]', N'[]', N'{}', N'1', N'2022-05-11 21:47:12.0000000', N'1', N'2022-05-13 10:50:16.9620000', N'0')
GO

SET IDENTITY_INSERT [dbo].[system_oauth2_client] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_oauth2_code
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_oauth2_code]') AND type IN ('U'))
	DROP TABLE [dbo].[system_oauth2_code]
GO

CREATE TABLE [dbo].[system_oauth2_code] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [user_id] bigint  NOT NULL,
  [user_type] tinyint  NOT NULL,
  [code] nvarchar(32) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [client_id] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [scopes] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [expires_time] datetime2(7)  NOT NULL,
  [redirect_uri] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [state] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT '' NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL,
  [tenant_id] bigint  NOT NULL
)
GO

ALTER TABLE [dbo].[system_oauth2_code] SET (LOCK_ESCALATION = TABLE)
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
-- Records of system_oauth2_code
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_oauth2_code] ON
GO

SET IDENTITY_INSERT [dbo].[system_oauth2_code] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_oauth2_refresh_token
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_oauth2_refresh_token]') AND type IN ('U'))
	DROP TABLE [dbo].[system_oauth2_refresh_token]
GO

CREATE TABLE [dbo].[system_oauth2_refresh_token] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [user_id] bigint  NOT NULL,
  [refresh_token] nvarchar(32) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [user_type] tinyint  NOT NULL,
  [client_id] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [expires_time] datetime2(7)  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [scopes] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT '' NULL
)
GO

ALTER TABLE [dbo].[system_oauth2_refresh_token] SET (LOCK_ESCALATION = TABLE)
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
'MS_Description', N'授权范围',
'SCHEMA', N'dbo',
'TABLE', N'system_oauth2_refresh_token',
'COLUMN', N'scopes'
GO

EXEC sp_addextendedproperty
'MS_Description', N'刷新令牌',
'SCHEMA', N'dbo',
'TABLE', N'system_oauth2_refresh_token'
GO


-- ----------------------------
-- Records of system_oauth2_refresh_token
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_oauth2_refresh_token] ON
GO

SET IDENTITY_INSERT [dbo].[system_oauth2_refresh_token] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_operate_log
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_operate_log]') AND type IN ('U'))
	DROP TABLE [dbo].[system_operate_log]
GO

CREATE TABLE [dbo].[system_operate_log] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [trace_id] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [user_id] bigint  NOT NULL,
  [user_type] tinyint  NOT NULL,
  [module] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [name] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [type] bigint  NOT NULL,
  [content] nvarchar(2000) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT '' NOT NULL,
  [exts] nvarchar(512) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT '' NOT NULL,
  [request_method] nvarchar(16) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [request_url] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [user_ip] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [user_agent] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [java_method] nvarchar(512) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [java_method_args] nvarchar(max) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [start_time] datetime2(7)  NOT NULL,
  [duration] int  NOT NULL,
  [result_code] int  NOT NULL,
  [result_msg] nvarchar(512) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [result_data] nvarchar(4000) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_operate_log] SET (LOCK_ESCALATION = TABLE)
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
'MS_Description', N'模块标题',
'SCHEMA', N'dbo',
'TABLE', N'system_operate_log',
'COLUMN', N'module'
GO

EXEC sp_addextendedproperty
'MS_Description', N'操作名',
'SCHEMA', N'dbo',
'TABLE', N'system_operate_log',
'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'操作分类',
'SCHEMA', N'dbo',
'TABLE', N'system_operate_log',
'COLUMN', N'type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'操作内容',
'SCHEMA', N'dbo',
'TABLE', N'system_operate_log',
'COLUMN', N'content'
GO

EXEC sp_addextendedproperty
'MS_Description', N'拓展字段',
'SCHEMA', N'dbo',
'TABLE', N'system_operate_log',
'COLUMN', N'exts'
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
'MS_Description', N'Java 方法名',
'SCHEMA', N'dbo',
'TABLE', N'system_operate_log',
'COLUMN', N'java_method'
GO

EXEC sp_addextendedproperty
'MS_Description', N'Java 方法的参数',
'SCHEMA', N'dbo',
'TABLE', N'system_operate_log',
'COLUMN', N'java_method_args'
GO

EXEC sp_addextendedproperty
'MS_Description', N'操作时间',
'SCHEMA', N'dbo',
'TABLE', N'system_operate_log',
'COLUMN', N'start_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'执行时长',
'SCHEMA', N'dbo',
'TABLE', N'system_operate_log',
'COLUMN', N'duration'
GO

EXEC sp_addextendedproperty
'MS_Description', N'结果码',
'SCHEMA', N'dbo',
'TABLE', N'system_operate_log',
'COLUMN', N'result_code'
GO

EXEC sp_addextendedproperty
'MS_Description', N'结果提示',
'SCHEMA', N'dbo',
'TABLE', N'system_operate_log',
'COLUMN', N'result_msg'
GO

EXEC sp_addextendedproperty
'MS_Description', N'结果数据',
'SCHEMA', N'dbo',
'TABLE', N'system_operate_log',
'COLUMN', N'result_data'
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
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'system_operate_log',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'system_operate_log',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'操作日志记录',
'SCHEMA', N'dbo',
'TABLE', N'system_operate_log'
GO


-- ----------------------------
-- Records of system_operate_log
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_operate_log] ON
GO

SET IDENTITY_INSERT [dbo].[system_operate_log] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_post
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_post]') AND type IN ('U'))
	DROP TABLE [dbo].[system_post]
GO

CREATE TABLE [dbo].[system_post] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [code] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [name] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [sort] int  NOT NULL,
  [status] tinyint  NOT NULL,
  [remark] nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_post] SET (LOCK_ESCALATION = TABLE)
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
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'system_post',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'system_post',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'岗位信息表',
'SCHEMA', N'dbo',
'TABLE', N'system_post'
GO


-- ----------------------------
-- Records of system_post
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_post] ON
GO

INSERT INTO [dbo].[system_post] ([id], [code], [name], [sort], [status], [remark], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'1', N'ceo', N'董事长', N'1', N'0', N'', N'admin', N'2021-01-06 17:03:48.0000000', N'1', N'2022-04-19 16:53:39.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_post] ([id], [code], [name], [sort], [status], [remark], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'2', N'se', N'项目经理', N'2', N'0', N'', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2021-12-12 10:47:47.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_post] ([id], [code], [name], [sort], [status], [remark], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'4', N'user', N'普通员工', N'4', N'0', N'111', N'admin', N'2021-01-05 17:03:48.0000000', N'1', N'2022-04-20 00:59:35.0000000', N'1', N'0')
GO

SET IDENTITY_INSERT [dbo].[system_post] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_role
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_role]') AND type IN ('U'))
	DROP TABLE [dbo].[system_role]
GO

CREATE TABLE [dbo].[system_role] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [name] nvarchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [code] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [sort] int  NOT NULL,
  [data_scope] tinyint  NOT NULL,
  [data_scope_dept_ids] nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [status] tinyint  NOT NULL,
  [type] tinyint  NOT NULL,
  [remark] nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_role] SET (LOCK_ESCALATION = TABLE)
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
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'system_role',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'system_role',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'角色信息表',
'SCHEMA', N'dbo',
'TABLE', N'system_role'
GO


-- ----------------------------
-- Records of system_role
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_role] ON
GO

INSERT INTO [dbo].[system_role] ([id], [name], [code], [sort], [data_scope], [data_scope_dept_ids], [status], [type], [remark], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'1', N'超级管理员', N'super_admin', N'1', N'1', N'', N'0', N'1', N'超级管理员', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-02-22 05:08:21.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_role] ([id], [name], [code], [sort], [data_scope], [data_scope_dept_ids], [status], [type], [remark], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'2', N'普通角色', N'common', N'2', N'2', N'', N'0', N'1', N'普通角色', N'admin', N'2021-01-05 17:03:48.0000000', N'', N'2022-02-22 05:08:20.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_role] ([id], [name], [code], [sort], [data_scope], [data_scope_dept_ids], [status], [type], [remark], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'101', N'测试账号', N'test', N'0', N'1', N'[]', N'0', N'2', N'132', N'', N'2021-01-06 13:49:35.0000000', N'1', N'2022-05-02 02:32:06.6180000', N'1', N'0')
GO

INSERT INTO [dbo].[system_role] ([id], [name], [code], [sort], [data_scope], [data_scope_dept_ids], [status], [type], [remark], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'109', N'租户管理员', N'tenant_admin', N'0', N'1', N'', N'0', N'1', N'系统自动生成', N'1', N'2022-02-22 00:56:14.0000000', N'1', N'2022-02-22 00:56:14.0000000', N'121', N'0')
GO

INSERT INTO [dbo].[system_role] ([id], [name], [code], [sort], [data_scope], [data_scope_dept_ids], [status], [type], [remark], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'110', N'测试角色', N'test', N'0', N'1', N'[]', N'0', N'2', N'嘿嘿', N'110', N'2022-02-23 00:14:34.0000000', N'110', N'2022-02-23 13:14:58.0000000', N'121', N'0')
GO

INSERT INTO [dbo].[system_role] ([id], [name], [code], [sort], [data_scope], [data_scope_dept_ids], [status], [type], [remark], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'111', N'租户管理员', N'tenant_admin', N'0', N'1', N'', N'0', N'1', N'系统自动生成', N'1', N'2022-03-07 21:37:58.0000000', N'1', N'2022-03-07 21:37:58.0000000', N'122', N'0')
GO

SET IDENTITY_INSERT [dbo].[system_role] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_role_menu
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_role_menu]') AND type IN ('U'))
	DROP TABLE [dbo].[system_role_menu]
GO

CREATE TABLE [dbo].[system_role_menu] (
  [role_id] bigint  NOT NULL,
  [menu_id] bigint  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint DEFAULT 0 NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL,
  [id] bigint  IDENTITY(1,1) NOT NULL
)
GO

ALTER TABLE [dbo].[system_role_menu] SET (LOCK_ESCALATION = TABLE)
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
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'system_role_menu',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'system_role_menu',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'自增编号',
'SCHEMA', N'dbo',
'TABLE', N'system_role_menu',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'角色和菜单关联表',
'SCHEMA', N'dbo',
'TABLE', N'system_role_menu'
GO


-- ----------------------------
-- Records of system_role_menu
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_role_menu] ON
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'109', N'1', N'1', N'2022-02-22 00:56:14.0000000', N'1', N'2022-02-22 00:56:14.0000000', N'121', N'0', N'1')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'1', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'2')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'1093', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'3')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'1094', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'4')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'1100', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'5')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'1107', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'6')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'1110', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'7')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'1117', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'8')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'100', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'9')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'101', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'10')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'102', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'11')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'1126', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'12')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'103', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'13')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'104', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'14')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'105', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'15')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'107', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'16')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'108', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'17')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'109', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'18')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'1138', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'19')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'1224', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'20')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'1225', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'21')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'500', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'22')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'501', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'2022-02-22 13:09:12.0000000', N'1', N'0', N'23')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'2', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'0', N'24')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'1077', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'0', N'25')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'1078', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'0', N'26')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'1083', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'0', N'27')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'1084', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'0', N'28')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'1090', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'0', N'29')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'106', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'0', N'30')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'110', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'0', N'31')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'111', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'0', N'32')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'112', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'0', N'33')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'2', N'113', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'2022-02-22 13:16:57.0000000', N'1', N'0', N'34')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'110', N'1', N'110', N'2022-02-23 00:23:55.0000000', N'110', N'2022-02-23 00:23:55.0000000', N'121', N'0', N'35')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'109', N'103', N'1', N'2022-02-23 19:32:14.0000000', N'1', N'2022-02-23 19:32:14.0000000', N'121', N'0', N'36')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'109', N'104', N'1', N'2022-02-23 19:32:14.0000000', N'1', N'2022-02-23 19:32:14.0000000', N'121', N'0', N'37')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'1', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'38')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'2', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'39')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'1077', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'40')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'1078', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'41')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'1083', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'42')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'1084', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'43')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'1090', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'44')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'1093', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'45')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'1094', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'46')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'1100', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'47')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'1107', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'48')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'1110', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'49')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'1117', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'50')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'100', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'51')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'101', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'52')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'102', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'53')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'1126', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'54')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'103', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'55')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'104', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'56')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'105', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'57')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'106', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'58')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'107', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'59')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'108', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'60')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'109', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'61')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'110', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'62')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'111', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'63')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'112', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'64')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'113', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'65')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'1138', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'66')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'1224', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'67')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'1225', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'68')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'500', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'69')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'1', N'501', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'2022-02-23 20:03:57.0000000', N'1', N'0', N'70')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'109', N'1024', N'1', N'2022-02-23 20:30:14.0000000', N'1', N'2022-02-23 20:30:14.0000000', N'121', N'0', N'71')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'109', N'1025', N'1', N'2022-02-23 20:30:14.0000000', N'1', N'2022-02-23 20:30:14.0000000', N'121', N'0', N'72')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'109', N'1017', N'1', N'2022-02-23 20:30:14.0000000', N'1', N'2022-02-23 20:30:14.0000000', N'121', N'0', N'73')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'109', N'1018', N'1', N'2022-02-23 20:30:14.0000000', N'1', N'2022-02-23 20:30:14.0000000', N'121', N'0', N'74')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'109', N'1019', N'1', N'2022-02-23 20:30:14.0000000', N'1', N'2022-02-23 20:30:14.0000000', N'121', N'0', N'75')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'109', N'1020', N'1', N'2022-02-23 20:30:14.0000000', N'1', N'2022-02-23 20:30:14.0000000', N'121', N'0', N'76')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'109', N'1021', N'1', N'2022-02-23 20:30:14.0000000', N'1', N'2022-02-23 20:30:14.0000000', N'121', N'0', N'77')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'109', N'1022', N'1', N'2022-02-23 20:30:14.0000000', N'1', N'2022-02-23 20:30:14.0000000', N'121', N'0', N'78')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'109', N'1023', N'1', N'2022-02-23 20:30:14.0000000', N'1', N'2022-02-23 20:30:14.0000000', N'121', N'0', N'79')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'111', N'1024', N'1', N'2022-03-07 21:37:58.0000000', N'1', N'2022-03-07 21:37:58.0000000', N'122', N'0', N'80')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'111', N'1025', N'1', N'2022-03-07 21:37:58.0000000', N'1', N'2022-03-07 21:37:58.0000000', N'122', N'0', N'81')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'111', N'1', N'1', N'2022-03-07 21:37:58.0000000', N'1', N'2022-03-07 21:37:58.0000000', N'122', N'0', N'82')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'111', N'103', N'1', N'2022-03-07 21:37:58.0000000', N'1', N'2022-03-07 21:37:58.0000000', N'122', N'0', N'83')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'111', N'104', N'1', N'2022-03-07 21:37:58.0000000', N'1', N'2022-03-07 21:37:58.0000000', N'122', N'0', N'84')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'111', N'1017', N'1', N'2022-03-07 21:37:58.0000000', N'1', N'2022-03-07 21:37:58.0000000', N'122', N'0', N'85')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'111', N'1018', N'1', N'2022-03-07 21:37:58.0000000', N'1', N'2022-03-07 21:37:58.0000000', N'122', N'0', N'86')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'111', N'1019', N'1', N'2022-03-07 21:37:58.0000000', N'1', N'2022-03-07 21:37:58.0000000', N'122', N'0', N'87')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'111', N'1020', N'1', N'2022-03-07 21:37:58.0000000', N'1', N'2022-03-07 21:37:58.0000000', N'122', N'0', N'88')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'111', N'1021', N'1', N'2022-03-07 21:37:58.0000000', N'1', N'2022-03-07 21:37:58.0000000', N'122', N'0', N'89')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'111', N'1022', N'1', N'2022-03-07 21:37:58.0000000', N'1', N'2022-03-07 21:37:58.0000000', N'122', N'0', N'90')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'111', N'1023', N'1', N'2022-03-07 21:37:58.0000000', N'1', N'2022-03-07 21:37:58.0000000', N'122', N'0', N'91')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'109', N'102', N'1', N'2022-03-19 18:39:13.0000000', N'1', N'2022-03-19 18:39:13.0000000', N'121', N'0', N'92')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'109', N'1013', N'1', N'2022-03-19 18:39:13.0000000', N'1', N'2022-03-19 18:39:13.0000000', N'121', N'0', N'93')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'109', N'1014', N'1', N'2022-03-19 18:39:13.0000000', N'1', N'2022-03-19 18:39:13.0000000', N'121', N'0', N'94')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'109', N'1015', N'1', N'2022-03-19 18:39:13.0000000', N'1', N'2022-03-19 18:39:13.0000000', N'121', N'0', N'95')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'109', N'1016', N'1', N'2022-03-19 18:39:13.0000000', N'1', N'2022-03-19 18:39:13.0000000', N'121', N'0', N'96')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'111', N'102', N'1', N'2022-03-19 18:39:13.0000000', N'1', N'2022-03-19 18:39:13.0000000', N'122', N'0', N'97')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'111', N'1013', N'1', N'2022-03-19 18:39:13.0000000', N'1', N'2022-03-19 18:39:13.0000000', N'122', N'0', N'98')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'111', N'1014', N'1', N'2022-03-19 18:39:13.0000000', N'1', N'2022-03-19 18:39:13.0000000', N'122', N'0', N'99')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'111', N'1015', N'1', N'2022-03-19 18:39:13.0000000', N'1', N'2022-03-19 18:39:13.0000000', N'122', N'0', N'100')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'111', N'1016', N'1', N'2022-03-19 18:39:13.0000000', N'1', N'2022-03-19 18:39:13.0000000', N'122', N'0', N'101')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1216', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'102')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1217', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'103')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1218', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'104')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1219', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'105')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1220', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'106')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1221', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'107')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'5', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'108')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1222', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'109')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1118', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'110')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1119', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'111')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1120', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'112')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1185', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'113')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1186', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'114')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1187', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'115')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1188', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'116')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1189', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'117')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1190', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'118')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1191', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'119')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1192', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'120')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1193', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'121')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1194', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'122')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1195', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'123')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1196', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'124')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1197', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'125')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1198', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'126')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1199', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'127')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1200', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'128')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1201', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'129')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1202', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'130')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1207', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'131')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1208', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'132')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1209', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'133')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1210', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'134')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1211', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'135')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1212', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'136')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1213', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'137')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1215', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'2022-03-19 21:45:52.0000000', N'1', N'0', N'138')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'2', N'1', N'2022-04-01 22:21:24.0000000', N'1', N'2022-04-01 22:21:24.0000000', N'1', N'0', N'139')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1031', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'140')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1032', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'141')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1033', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'142')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1034', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'143')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1035', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'144')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1050', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'145')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1051', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'146')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1052', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'147')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1053', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'148')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1054', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'149')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1056', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'150')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1057', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'151')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1058', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'152')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1059', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'153')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1060', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'154')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1066', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'155')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1067', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'156')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1070', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'157')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1071', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'158')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1072', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'159')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1073', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'160')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1074', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'161')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1075', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'162')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1076', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'163')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1077', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'164')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1078', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'165')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1082', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'166')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1083', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'167')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1084', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'168')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1085', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'169')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1086', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'170')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1087', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'171')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1088', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'172')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1089', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'173')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1090', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'174')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1091', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'175')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1092', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'176')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1237', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'177')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1238', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'178')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1239', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'179')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1240', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'180')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1241', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'181')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1242', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'182')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'1243', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'183')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'106', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'184')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'110', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'185')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'111', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'186')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'112', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'187')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'113', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'188')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'114', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'189')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'115', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'190')
GO

INSERT INTO [dbo].[system_role_menu] ([role_id], [menu_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted], [id]) VALUES (N'101', N'116', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'2022-04-01 22:21:37.0000000', N'1', N'0', N'191')
GO

SET IDENTITY_INSERT [dbo].[system_role_menu] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_sensitive_word
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_sensitive_word]') AND type IN ('U'))
	DROP TABLE [dbo].[system_sensitive_word]
GO

CREATE TABLE [dbo].[system_sensitive_word] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [name] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [description] nvarchar(512) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [tags] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [status] tinyint  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_sensitive_word] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'编号',
'SCHEMA', N'dbo',
'TABLE', N'system_sensitive_word',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'敏感词',
'SCHEMA', N'dbo',
'TABLE', N'system_sensitive_word',
'COLUMN', N'name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'描述',
'SCHEMA', N'dbo',
'TABLE', N'system_sensitive_word',
'COLUMN', N'description'
GO

EXEC sp_addextendedproperty
'MS_Description', N'标签数组',
'SCHEMA', N'dbo',
'TABLE', N'system_sensitive_word',
'COLUMN', N'tags'
GO

EXEC sp_addextendedproperty
'MS_Description', N'状态',
'SCHEMA', N'dbo',
'TABLE', N'system_sensitive_word',
'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建者',
'SCHEMA', N'dbo',
'TABLE', N'system_sensitive_word',
'COLUMN', N'creator'
GO

EXEC sp_addextendedproperty
'MS_Description', N'创建时间',
'SCHEMA', N'dbo',
'TABLE', N'system_sensitive_word',
'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新者',
'SCHEMA', N'dbo',
'TABLE', N'system_sensitive_word',
'COLUMN', N'updater'
GO

EXEC sp_addextendedproperty
'MS_Description', N'更新时间',
'SCHEMA', N'dbo',
'TABLE', N'system_sensitive_word',
'COLUMN', N'update_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'system_sensitive_word',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'敏感词',
'SCHEMA', N'dbo',
'TABLE', N'system_sensitive_word'
GO


-- ----------------------------
-- Records of system_sensitive_word
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_sensitive_word] ON
GO

INSERT INTO [dbo].[system_sensitive_word] ([id], [name], [description], [tags], [status], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'3', N'土豆', N'好呀', N'蔬菜,短信', N'0', N'1', N'2022-04-08 21:07:12.0000000', N'1', N'2022-04-09 10:28:14.0000000', N'0')
GO

INSERT INTO [dbo].[system_sensitive_word] ([id], [name], [description], [tags], [status], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'4', N'XXX', NULL, N'短信', N'0', N'1', N'2022-04-08 21:27:49.0000000', N'1', N'2022-04-08 21:27:49.0000000', N'0')
GO

SET IDENTITY_INSERT [dbo].[system_sensitive_word] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_sms_channel
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_sms_channel]') AND type IN ('U'))
	DROP TABLE [dbo].[system_sms_channel]
GO

CREATE TABLE [dbo].[system_sms_channel] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [signature] nvarchar(12) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [code] nvarchar(63) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [status] tinyint  NOT NULL,
  [remark] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [api_key] nvarchar(128) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [api_secret] nvarchar(128) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [callback_url] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_sms_channel] SET (LOCK_ESCALATION = TABLE)
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
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_sms_channel] ON
GO

INSERT INTO [dbo].[system_sms_channel] ([id], [signature], [code], [status], [remark], [api_key], [api_secret], [callback_url], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'2', N'Ballcat', N'ALIYUN', N'0', N'啦啦啦', N'LTAI5tCnKso2uG3kJ5gRav88', N'fGJ5SNXL7P1NHNRmJ7DJaMJGPyE55C', NULL, N'', N'2021-03-31 11:53:10.0000000', N'1', N'2021-04-14 00:08:37.0000000', N'0')
GO

INSERT INTO [dbo].[system_sms_channel] ([id], [signature], [code], [status], [remark], [api_key], [api_secret], [callback_url], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'4', N'测试渠道', N'DEBUG_DING_TALK', N'0', N'123', N'696b5d8ead48071237e4aa5861ff08dbadb2b4ded1c688a7b7c9afc615579859', N'SEC5c4e5ff888bc8a9923ae47f59e7ccd30af1f14d93c55b4e2c9cb094e35aeed67', NULL, N'1', N'2021-04-13 00:23:14.0000000', N'1', N'2022-03-27 20:29:49.0000000', N'0')
GO

INSERT INTO [dbo].[system_sms_channel] ([id], [signature], [code], [status], [remark], [api_key], [api_secret], [callback_url], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'6', N'测试演示', N'DEBUG_DING_TALK', N'0', NULL, N'696b5d8ead48071237e4aa5861ff08dbadb2b4ded1c688a7b7c9afc615579859', N'SEC5c4e5ff888bc8a9923ae47f59e7ccd30af1f14d93c55b4e2c9cb094e35aeed67', NULL, N'1', N'2022-04-10 23:07:59.0000000', N'1', N'2022-04-10 23:07:59.0000000', N'0')
GO

SET IDENTITY_INSERT [dbo].[system_sms_channel] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_sms_code
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_sms_code]') AND type IN ('U'))
	DROP TABLE [dbo].[system_sms_code]
GO

CREATE TABLE [dbo].[system_sms_code] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [mobile] nvarchar(11) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [code] nvarchar(6) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [create_ip] nvarchar(15) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [scene] tinyint  NOT NULL,
  [today_index] tinyint  NOT NULL,
  [used] tinyint  NOT NULL,
  [used_time] datetime2(7)  NULL,
  [used_ip] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_sms_code] SET (LOCK_ESCALATION = TABLE)
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
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'system_sms_code',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'system_sms_code',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'手机验证码',
'SCHEMA', N'dbo',
'TABLE', N'system_sms_code'
GO


-- ----------------------------
-- Records of system_sms_code
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_sms_code] ON
GO

SET IDENTITY_INSERT [dbo].[system_sms_code] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_sms_log
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_sms_log]') AND type IN ('U'))
	DROP TABLE [dbo].[system_sms_log]
GO

CREATE TABLE [dbo].[system_sms_log] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [channel_id] bigint  NOT NULL,
  [channel_code] nvarchar(63) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [template_id] bigint  NOT NULL,
  [template_code] nvarchar(63) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [template_type] tinyint  NOT NULL,
  [template_content] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [template_params] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [api_template_id] nvarchar(63) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [mobile] nvarchar(11) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [user_id] bigint  NULL,
  [user_type] tinyint  NULL,
  [send_status] tinyint  NOT NULL,
  [send_time] datetime2(7)  NULL,
  [send_code] int  NULL,
  [send_msg] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [api_send_code] nvarchar(63) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [api_send_msg] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [api_request_id] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [api_serial_no] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [receive_status] tinyint  NOT NULL,
  [receive_time] datetime2(7)  NULL,
  [api_receive_code] nvarchar(63) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [api_receive_msg] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_sms_log] SET (LOCK_ESCALATION = TABLE)
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
'MS_Description', N'发送结果的编码',
'SCHEMA', N'dbo',
'TABLE', N'system_sms_log',
'COLUMN', N'send_code'
GO

EXEC sp_addextendedproperty
'MS_Description', N'发送结果的提示',
'SCHEMA', N'dbo',
'TABLE', N'system_sms_log',
'COLUMN', N'send_msg'
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
-- Records of system_sms_log
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_sms_log] ON
GO

SET IDENTITY_INSERT [dbo].[system_sms_log] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_sms_template
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_sms_template]') AND type IN ('U'))
	DROP TABLE [dbo].[system_sms_template]
GO

CREATE TABLE [dbo].[system_sms_template] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [type] tinyint  NOT NULL,
  [status] tinyint  NOT NULL,
  [code] nvarchar(63) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [name] nvarchar(63) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [content] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [params] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [remark] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [api_template_id] nvarchar(63) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [channel_id] bigint  NOT NULL,
  [channel_code] nvarchar(63) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_sms_template] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'编号',
'SCHEMA', N'dbo',
'TABLE', N'system_sms_template',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'短信签名',
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
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_sms_template] ON
GO

INSERT INTO [dbo].[system_sms_template] ([id], [type], [status], [code], [name], [content], [params], [remark], [api_template_id], [channel_id], [channel_code], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'2', N'1', N'0', N'test_01', N'测试验证码短信', N'正在进行登录操作{operation}，您的验证码是{code}', N'["operation","code"]', NULL, N'4383920', N'1', N'YUN_PIAN', N'', N'2021-03-31 10:49:38.0000000', N'1', N'2021-04-10 01:22:00.0000000', N'0')
GO

INSERT INTO [dbo].[system_sms_template] ([id], [type], [status], [code], [name], [content], [params], [remark], [api_template_id], [channel_id], [channel_code], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'3', N'1', N'0', N'test_02', N'公告通知', N'您的验证码{code}，该验证码5分钟内有效，请勿泄漏于他人！', N'["code"]', NULL, N'SMS_207945135', N'2', N'ALIYUN', N'', N'2021-03-31 11:56:30.0000000', N'1', N'2021-04-10 01:22:02.0000000', N'0')
GO

INSERT INTO [dbo].[system_sms_template] ([id], [type], [status], [code], [name], [content], [params], [remark], [api_template_id], [channel_id], [channel_code], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'6', N'3', N'0', N'test-01', N'测试模板', N'哈哈哈 {name}', N'["name"]', N'f哈哈哈', N'4383920', N'1', N'YUN_PIAN', N'1', N'2021-04-10 01:07:21.0000000', N'1', N'2021-04-10 01:22:05.0000000', N'0')
GO

INSERT INTO [dbo].[system_sms_template] ([id], [type], [status], [code], [name], [content], [params], [remark], [api_template_id], [channel_id], [channel_code], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'7', N'3', N'0', N'test-04', N'测试下', N'老鸡{name}，牛逼{code}', N'["name","code"]', NULL, N'suibian', N'4', N'DEBUG_DING_TALK', N'1', N'2021-04-13 00:29:53.0000000', N'1', N'2021-04-14 00:30:38.0000000', N'0')
GO

INSERT INTO [dbo].[system_sms_template] ([id], [type], [status], [code], [name], [content], [params], [remark], [api_template_id], [channel_id], [channel_code], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'8', N'1', N'0', N'user-sms-login', N'前台用户短信登录', N'您的验证码是{code}', N'["code"]', NULL, N'4372216', N'1', N'YUN_PIAN', N'1', N'2021-10-11 08:10:00.0000000', N'1', N'2021-10-11 08:10:00.0000000', N'0')
GO

INSERT INTO [dbo].[system_sms_template] ([id], [type], [status], [code], [name], [content], [params], [remark], [api_template_id], [channel_id], [channel_code], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'9', N'2', N'0', N'bpm_task_assigned', N'【工作流】任务被分配', N'您收到了一条新的待办任务：{processInstanceName}-{taskName}，申请人：{startUserNickname}，处理链接：{detailUrl}', N'["processInstanceName","taskName","startUserNickname","detailUrl"]', NULL, N'suibian', N'4', N'DEBUG_DING_TALK', N'1', N'2022-01-21 22:31:19.0000000', N'1', N'2022-01-22 00:03:36.0000000', N'0')
GO

INSERT INTO [dbo].[system_sms_template] ([id], [type], [status], [code], [name], [content], [params], [remark], [api_template_id], [channel_id], [channel_code], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'10', N'2', N'0', N'bpm_process_instance_reject', N'【工作流】流程被不通过', N'您的流程被审批不通过：{processInstanceName}，原因：{reason}，查看链接：{detailUrl}', N'["processInstanceName","reason","detailUrl"]', NULL, N'suibian', N'4', N'DEBUG_DING_TALK', N'1', N'2022-01-22 00:03:31.0000000', N'1', N'2022-05-01 12:33:14.0000000', N'0')
GO

INSERT INTO [dbo].[system_sms_template] ([id], [type], [status], [code], [name], [content], [params], [remark], [api_template_id], [channel_id], [channel_code], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'11', N'2', N'0', N'bpm_process_instance_approve', N'【工作流】流程被通过', N'您的流程被审批通过：{processInstanceName}，查看链接：{detailUrl}', N'["processInstanceName","detailUrl"]', NULL, N'suibian', N'4', N'DEBUG_DING_TALK', N'1', N'2022-01-22 00:04:31.0000000', N'1', N'2022-03-27 20:32:21.0000000', N'0')
GO

INSERT INTO [dbo].[system_sms_template] ([id], [type], [status], [code], [name], [content], [params], [remark], [api_template_id], [channel_id], [channel_code], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'12', N'2', N'0', N'demo', N'演示模板', N'我就是测试一下下', N'[]', NULL, N'biubiubiu', N'6', N'DEBUG_DING_TALK', N'1', N'2022-04-10 23:22:49.0000000', N'1', N'2022-04-10 23:22:49.0000000', N'0')
GO

INSERT INTO [dbo].[system_sms_template] ([id], [type], [status], [code], [name], [content], [params], [remark], [api_template_id], [channel_id], [channel_code], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'13', N'1', N'0', N'admin-sms-login', N'后台用户短信登录', N'您的验证码是{code}', N'["code"]', N'', N'4372216', N'1', N'YUN_PIAN', N'1', N'2021-10-11 08:10:00.0000000', N'1', N'2021-10-11 08:10:00.0000000', N'0')
GO

SET IDENTITY_INSERT [dbo].[system_sms_template] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_social_user
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_social_user]') AND type IN ('U'))
	DROP TABLE [dbo].[system_social_user]
GO

CREATE TABLE [dbo].[system_social_user] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [type] tinyint  NOT NULL,
  [openid] nvarchar(32) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [token] nvarchar(256) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [raw_token_info] nvarchar(1024) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [nickname] nvarchar(32) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [avatar] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [raw_user_info] nvarchar(1024) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [code] nvarchar(256) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [state] nvarchar(256) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_social_user] SET (LOCK_ESCALATION = TABLE)
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
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'system_social_user',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'system_social_user',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'社交用户表',
'SCHEMA', N'dbo',
'TABLE', N'system_social_user'
GO


-- ----------------------------
-- Records of system_social_user
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_social_user] ON
GO

SET IDENTITY_INSERT [dbo].[system_social_user] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_social_user_bind
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_social_user_bind]') AND type IN ('U'))
	DROP TABLE [dbo].[system_social_user_bind]
GO

CREATE TABLE [dbo].[system_social_user_bind] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [user_id] bigint  NOT NULL,
  [user_type] tinyint  NOT NULL,
  [social_type] tinyint  NOT NULL,
  [social_user_id] bigint  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_social_user_bind] SET (LOCK_ESCALATION = TABLE)
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
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'system_social_user_bind',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'system_social_user_bind',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'社交绑定表',
'SCHEMA', N'dbo',
'TABLE', N'system_social_user_bind'
GO


-- ----------------------------
-- Records of system_social_user_bind
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_social_user_bind] ON
GO

SET IDENTITY_INSERT [dbo].[system_social_user_bind] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_tenant
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_tenant]') AND type IN ('U'))
	DROP TABLE [dbo].[system_tenant]
GO

CREATE TABLE [dbo].[system_tenant] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [name] nvarchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [contact_user_id] bigint  NULL,
  [contact_name] nvarchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [contact_mobile] nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [status] tinyint  NOT NULL,
  [domain] nvarchar(256) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [package_id] bigint  NOT NULL,
  [expire_time] datetime2(7)  NOT NULL,
  [account_count] int  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_tenant] SET (LOCK_ESCALATION = TABLE)
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
'COLUMN', N'domain'
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
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_tenant] ON
GO

INSERT INTO [dbo].[system_tenant] ([id], [name], [contact_user_id], [contact_name], [contact_mobile], [status], [domain], [package_id], [expire_time], [account_count], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'1', N'芋道源码', NULL, N'芋艿', N'17321315478', N'0', N'https://www.iocoder.cn', N'0', N'2099-02-19 17:14:16.0000000', N'9999', N'1', N'2021-01-05 17:03:47.0000000', N'1', N'2022-02-23 12:15:11.0000000', N'0')
GO

INSERT INTO [dbo].[system_tenant] ([id], [name], [contact_user_id], [contact_name], [contact_mobile], [status], [domain], [package_id], [expire_time], [account_count], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'121', N'小租户', N'110', N'小王2', N'15601691300', N'0', N'http://www.iocoder.cn', N'111', N'2024-03-11 00:00:00.0000000', N'20', N'1', N'2022-02-22 00:56:14.0000000', N'1', N'2022-03-19 18:37:20.0000000', N'0')
GO

INSERT INTO [dbo].[system_tenant] ([id], [name], [contact_user_id], [contact_name], [contact_mobile], [status], [domain], [package_id], [expire_time], [account_count], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'122', N'测试租户', N'113', N'芋道', N'15601691300', N'0', N'https://www.iocoder.cn', N'111', N'2022-04-30 00:00:00.0000000', N'50', N'1', N'2022-03-07 21:37:58.0000000', N'1', N'2022-03-07 21:37:58.0000000', N'0')
GO

SET IDENTITY_INSERT [dbo].[system_tenant] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_tenant_package
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_tenant_package]') AND type IN ('U'))
	DROP TABLE [dbo].[system_tenant_package]
GO

CREATE TABLE [dbo].[system_tenant_package] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [name] nvarchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [status] tinyint  NOT NULL,
  [remark] nvarchar(256) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [menu_ids] nvarchar(2048) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_tenant_package] SET (LOCK_ESCALATION = TABLE)
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
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_tenant_package] ON
GO

INSERT INTO [dbo].[system_tenant_package] ([id], [name], [status], [remark], [menu_ids], [creator], [create_time], [updater], [update_time], [deleted]) VALUES (N'111', N'普通套餐', N'0', N'小功能', N'[1024,1025,1,102,103,104,1013,1014,1015,1016,1017,1018,1019,1020,1021,1022,1023]', N'1', N'2022-02-22 00:54:00.0000000', N'1', N'2022-03-19 18:39:13.0000000', N'0')
GO

SET IDENTITY_INSERT [dbo].[system_tenant_package] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_user_post
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_user_post]') AND type IN ('U'))
	DROP TABLE [dbo].[system_user_post]
GO

CREATE TABLE [dbo].[system_user_post] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [user_id] bigint  NOT NULL,
  [post_id] bigint  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL,
  [tenant_id] bigint DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_user_post] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID 主键',
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
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_user_post] ON
GO

INSERT INTO [dbo].[system_user_post] ([id], [user_id], [post_id], [creator], [create_time], [updater], [update_time], [deleted], [tenant_id]) VALUES (N'112', N'1', N'1', N'admin', N'2022-05-02 07:25:24.0000000', N'admin', N'2022-05-02 07:25:24.0000000', N'0', N'1')
GO

INSERT INTO [dbo].[system_user_post] ([id], [user_id], [post_id], [creator], [create_time], [updater], [update_time], [deleted], [tenant_id]) VALUES (N'113', N'100', N'1', N'admin', N'2022-05-02 07:25:24.0000000', N'admin', N'2022-05-02 07:25:24.0000000', N'0', N'1')
GO

INSERT INTO [dbo].[system_user_post] ([id], [user_id], [post_id], [creator], [create_time], [updater], [update_time], [deleted], [tenant_id]) VALUES (N'114', N'114', N'3', N'admin', N'2022-05-02 07:25:24.0000000', N'admin', N'2022-05-02 07:25:24.0000000', N'0', N'1')
GO

SET IDENTITY_INSERT [dbo].[system_user_post] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_user_role
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_user_role]') AND type IN ('U'))
	DROP TABLE [dbo].[system_user_role]
GO

CREATE TABLE [dbo].[system_user_role] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [user_id] bigint  NOT NULL,
  [role_id] bigint  NOT NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_user_role] SET (LOCK_ESCALATION = TABLE)
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
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'system_user_role',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'system_user_role',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用户和角色关联表',
'SCHEMA', N'dbo',
'TABLE', N'system_user_role'
GO


-- ----------------------------
-- Records of system_user_role
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_user_role] ON
GO

INSERT INTO [dbo].[system_user_role] ([id], [user_id], [role_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'1', N'1', N'1', N'', N'2022-01-11 13:19:45.0000000', N'', N'2022-01-11 13:19:45.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_user_role] ([id], [user_id], [role_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'2', N'2', N'2', N'', N'2022-01-11 13:19:45.0000000', N'', N'2022-01-11 13:19:45.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_user_role] ([id], [user_id], [role_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'4', N'100', N'101', N'', N'2022-01-11 13:19:45.0000000', N'', N'2022-01-11 13:19:45.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_user_role] ([id], [user_id], [role_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'5', N'100', N'1', N'', N'2022-01-11 13:19:45.0000000', N'', N'2022-01-11 13:19:45.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_user_role] ([id], [user_id], [role_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'6', N'100', N'2', N'', N'2022-01-11 13:19:45.0000000', N'', N'2022-01-11 13:19:45.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_user_role] ([id], [user_id], [role_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'7', N'104', N'101', N'', N'2022-01-11 13:19:45.0000000', N'', N'2022-01-11 13:19:45.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_user_role] ([id], [user_id], [role_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'10', N'103', N'1', N'1', N'2022-01-11 13:19:45.0000000', N'1', N'2022-01-11 13:19:45.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_user_role] ([id], [user_id], [role_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'11', N'107', N'106', N'1', N'2022-02-20 22:59:33.0000000', N'1', N'2022-02-20 22:59:33.0000000', N'118', N'0')
GO

INSERT INTO [dbo].[system_user_role] ([id], [user_id], [role_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'12', N'108', N'107', N'1', N'2022-02-20 23:00:50.0000000', N'1', N'2022-02-20 23:00:50.0000000', N'119', N'0')
GO

INSERT INTO [dbo].[system_user_role] ([id], [user_id], [role_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'13', N'109', N'108', N'1', N'2022-02-20 23:11:50.0000000', N'1', N'2022-02-20 23:11:50.0000000', N'120', N'0')
GO

INSERT INTO [dbo].[system_user_role] ([id], [user_id], [role_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'14', N'110', N'109', N'1', N'2022-02-22 00:56:14.0000000', N'1', N'2022-02-22 00:56:14.0000000', N'121', N'0')
GO

INSERT INTO [dbo].[system_user_role] ([id], [user_id], [role_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'15', N'111', N'110', N'110', N'2022-02-23 13:14:38.0000000', N'110', N'2022-02-23 13:14:38.0000000', N'121', N'0')
GO

INSERT INTO [dbo].[system_user_role] ([id], [user_id], [role_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'16', N'113', N'111', N'1', N'2022-03-07 21:37:58.0000000', N'1', N'2022-03-07 21:37:58.0000000', N'122', N'0')
GO

INSERT INTO [dbo].[system_user_role] ([id], [user_id], [role_id], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'17', N'114', N'101', N'1', N'2022-03-19 21:51:13.0000000', N'1', N'2022-03-19 21:51:13.0000000', N'1', N'0')
GO

SET IDENTITY_INSERT [dbo].[system_user_role] OFF
GO

COMMIT
GO


-- ----------------------------
-- Table structure for system_users
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[system_users]') AND type IN ('U'))
	DROP TABLE [dbo].[system_users]
GO

CREATE TABLE [dbo].[system_users] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [username] nvarchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [password] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [nickname] nvarchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS  NOT NULL,
  [remark] nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [dept_id] bigint  NULL,
  [post_ids] nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [email] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [mobile] nvarchar(11) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [sex] tinyint  NULL,
  [avatar] nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [status] tinyint  NOT NULL,
  [login_ip] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [login_date] datetime2(7)  NULL,
  [creator] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updater] nvarchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS  NULL,
  [update_time] datetime2(7)  NOT NULL,
  [tenant_id] bigint  NOT NULL,
  [deleted] bit DEFAULT 0 NOT NULL
)
GO

ALTER TABLE [dbo].[system_users] SET (LOCK_ESCALATION = TABLE)
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
'MS_Description', N'租户编号',
'SCHEMA', N'dbo',
'TABLE', N'system_users',
'COLUMN', N'tenant_id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'是否删除',
'SCHEMA', N'dbo',
'TABLE', N'system_users',
'COLUMN', N'deleted'
GO

EXEC sp_addextendedproperty
'MS_Description', N'用户信息表',
'SCHEMA', N'dbo',
'TABLE', N'system_users'
GO


-- ----------------------------
-- Records of system_users
-- ----------------------------
BEGIN TRANSACTION
GO

SET IDENTITY_INSERT [dbo].[system_users] ON
GO

INSERT INTO [dbo].[system_users] ([id], [username], [password], [nickname], [remark], [dept_id], [post_ids], [email], [mobile], [sex], [avatar], [status], [login_ip], [login_date], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'1', N'admin', N'$2a$10$0acJOIk2D25/oC87nyclE..0lzeu9DtQ/n3geP4fkun/zIVRhHJIO', N'芋道源码', N'管理员', N'103', N'[1]', N'aoteman@126.com', N'15612345678', N'1', N'http://test.yudao.iocoder.cn/48934f2f-92d4-4250-b917-d10d2b262c6a', N'0', N'127.0.0.1', N'2022-05-26 00:51:15.3820000', N'admin', N'2021-01-05 17:03:47.0000000', NULL, N'2022-05-26 00:51:15.3870000', N'1', N'0')
GO

INSERT INTO [dbo].[system_users] ([id], [username], [password], [nickname], [remark], [dept_id], [post_ids], [email], [mobile], [sex], [avatar], [status], [login_ip], [login_date], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'100', N'yudao', N'$2a$10$11U48RhyJ5pSBYWSn12AD./ld671.ycSzJHbyrtpeoMeYiw31eo8a', N'芋道', N'不要吓我', N'104', N'[1]', N'yudao@iocoder.cn', N'15601691300', N'1', N'', N'1', N'127.0.0.1', N'2022-05-03 16:49:24.6860000', N'', N'2021-01-07 09:07:17.0000000', NULL, N'2022-05-03 16:49:24.6860000', N'1', N'0')
GO

INSERT INTO [dbo].[system_users] ([id], [username], [password], [nickname], [remark], [dept_id], [post_ids], [email], [mobile], [sex], [avatar], [status], [login_ip], [login_date], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'103', N'yuanma', N'$2a$10$wWoPT7sqriM2O1YXRL.je.GiL538OR6ZTN8aQZr9JAGdnpCH2tpYe', N'源码', NULL, N'106', NULL, N'yuanma@iocoder.cn', N'15601701300', N'0', N'', N'0', N'127.0.0.1', N'2022-01-18 00:33:40.0000000', N'', N'2021-01-13 23:50:35.0000000', NULL, N'2022-01-18 00:33:40.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_users] ([id], [username], [password], [nickname], [remark], [dept_id], [post_ids], [email], [mobile], [sex], [avatar], [status], [login_ip], [login_date], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'104', N'test', N'$2a$10$e5RpuDCC0GYSt0Hvd2.CjujIXwgGct4SnXi6dVGxdgFsnqgEryk5a', N'测试号', NULL, N'107', N'[]', N'111@qq.com', N'15601691200', N'1', N'', N'0', N'127.0.0.1', N'2022-03-19 21:46:19.0000000', N'', N'2021-01-21 02:13:53.0000000', NULL, N'2022-03-19 21:46:19.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_users] ([id], [username], [password], [nickname], [remark], [dept_id], [post_ids], [email], [mobile], [sex], [avatar], [status], [login_ip], [login_date], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'107', N'admin107', N'$2a$10$dYOOBKMO93v/.ReCqzyFg.o67Tqk.bbc2bhrpyBGkIw9aypCtr2pm', N'芋艿', NULL, NULL, NULL, N'', N'15601691300', N'0', N'', N'0', N'', NULL, N'1', N'2022-02-20 22:59:33.0000000', N'1', N'2022-02-27 08:26:51.0000000', N'118', N'0')
GO

INSERT INTO [dbo].[system_users] ([id], [username], [password], [nickname], [remark], [dept_id], [post_ids], [email], [mobile], [sex], [avatar], [status], [login_ip], [login_date], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'108', N'admin108', N'$2a$10$y6mfvKoNYL1GXWak8nYwVOH.kCWqjactkzdoIDgiKl93WN3Ejg.Lu', N'芋艿', NULL, NULL, NULL, N'', N'15601691300', N'0', N'', N'0', N'', NULL, N'1', N'2022-02-20 23:00:50.0000000', N'1', N'2022-02-27 08:26:53.0000000', N'119', N'0')
GO

INSERT INTO [dbo].[system_users] ([id], [username], [password], [nickname], [remark], [dept_id], [post_ids], [email], [mobile], [sex], [avatar], [status], [login_ip], [login_date], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'109', N'admin109', N'$2a$10$JAqvH0tEc0I7dfDVBI7zyuB4E3j.uH6daIjV53.vUS6PknFkDJkuK', N'芋艿', NULL, NULL, NULL, N'', N'15601691300', N'0', N'', N'0', N'', NULL, N'1', N'2022-02-20 23:11:50.0000000', N'1', N'2022-02-27 08:26:56.0000000', N'120', N'0')
GO

INSERT INTO [dbo].[system_users] ([id], [username], [password], [nickname], [remark], [dept_id], [post_ids], [email], [mobile], [sex], [avatar], [status], [login_ip], [login_date], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'110', N'admin110', N'$2a$10$qYxoXs0ogPHgYllyEneYde9xcCW5hZgukrxeXZ9lmLhKse8TK6IwW', N'小王', NULL, NULL, NULL, N'', N'15601691300', N'0', N'', N'0', N'127.0.0.1', N'2022-02-23 19:36:28.0000000', N'1', N'2022-02-22 00:56:14.0000000', NULL, N'2022-02-27 08:26:59.0000000', N'121', N'0')
GO

INSERT INTO [dbo].[system_users] ([id], [username], [password], [nickname], [remark], [dept_id], [post_ids], [email], [mobile], [sex], [avatar], [status], [login_ip], [login_date], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'111', N'test', N'$2a$10$mExveopHUx9Q4QiLtAzhDeH3n4/QlNLzEsM4AqgxKrU.ciUZDXZCy', N'测试用户', NULL, NULL, N'[]', N'', N'', N'0', N'', N'0', N'', NULL, N'110', N'2022-02-23 13:14:33.0000000', N'110', N'2022-02-23 13:14:33.0000000', N'121', N'0')
GO

INSERT INTO [dbo].[system_users] ([id], [username], [password], [nickname], [remark], [dept_id], [post_ids], [email], [mobile], [sex], [avatar], [status], [login_ip], [login_date], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'112', N'newobject', N'$2a$10$jh5MsR.ud/gKe3mVeUp5t.nEXGDSmHyv5OYjWQwHO8wlGmMSI9Twy', N'新对象', NULL, NULL, N'[]', N'', N'', N'0', N'', N'0', N'', NULL, N'1', N'2022-02-23 19:08:03.0000000', N'1', N'2022-02-23 19:08:03.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_users] ([id], [username], [password], [nickname], [remark], [dept_id], [post_ids], [email], [mobile], [sex], [avatar], [status], [login_ip], [login_date], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'113', N'aoteman', N'$2a$10$0acJOIk2D25/oC87nyclE..0lzeu9DtQ/n3geP4fkun/zIVRhHJIO', N'芋道', NULL, NULL, NULL, N'', N'15601691300', N'0', N'', N'0', N'127.0.0.1', N'2022-03-19 18:38:51.0000000', N'1', N'2022-03-07 21:37:58.0000000', NULL, N'2022-03-19 18:38:51.0000000', N'122', N'0')
GO

INSERT INTO [dbo].[system_users] ([id], [username], [password], [nickname], [remark], [dept_id], [post_ids], [email], [mobile], [sex], [avatar], [status], [login_ip], [login_date], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'114', N'hrmgr', N'$2a$10$TR4eybBioGRhBmDBWkqWLO6NIh3mzYa8KBKDDB5woiGYFVlRAi.fu', N'hr 小姐姐', NULL, NULL, N'[3]', N'', N'', N'0', N'', N'0', N'127.0.0.1', N'2022-03-19 22:15:43.0000000', N'1', N'2022-03-19 21:50:58.0000000', NULL, N'2022-03-19 22:15:43.0000000', N'1', N'0')
GO

INSERT INTO [dbo].[system_users] ([id], [username], [password], [nickname], [remark], [dept_id], [post_ids], [email], [mobile], [sex], [avatar], [status], [login_ip], [login_date], [creator], [create_time], [updater], [update_time], [tenant_id], [deleted]) VALUES (N'115', N'aotemane', N'$2a$10$/WCwGHu1eq0wOVDd/u8HweJ0gJCHyLS6T7ndCqI8UXZAQom1etk2e', N'1', N'11', N'100', N'[]', N'', N'', N'0', N'', N'0', N'', NULL, N'1', N'2022-04-30 02:55:43.0000000', N'1', N'2022-04-30 02:55:43.0000000', N'1', N'0')
GO

SET IDENTITY_INSERT [dbo].[system_users] OFF
GO

COMMIT
GO


-- ----------------------------
-- Primary Key structure for table QRTZ_CALENDARS
-- ----------------------------
ALTER TABLE [dbo].[QRTZ_CALENDARS] ADD CONSTRAINT [PK_QRTZ_CALENDARS] PRIMARY KEY CLUSTERED ([SCHED_NAME], [CALENDAR_NAME])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Indexes structure for table QRTZ_CRON_TRIGGERS
-- ----------------------------
CREATE NONCLUSTERED INDEX [IX_QRTZ_CRON_TRIGGERS_QRTZ_TRIGGERS]
ON [dbo].[QRTZ_CRON_TRIGGERS] (
  [SCHED_NAME] ASC,
  [TRIGGER_NAME] ASC,
  [TRIGGER_GROUP] ASC
)
GO


-- ----------------------------
-- Primary Key structure for table QRTZ_CRON_TRIGGERS
-- ----------------------------
ALTER TABLE [dbo].[QRTZ_CRON_TRIGGERS] ADD CONSTRAINT [PK_QRTZ_CRON_TRIGGERS] PRIMARY KEY CLUSTERED ([SCHED_NAME], [TRIGGER_NAME], [TRIGGER_GROUP])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table QRTZ_FIRED_TRIGGERS
-- ----------------------------
ALTER TABLE [dbo].[QRTZ_FIRED_TRIGGERS] ADD CONSTRAINT [PK_QRTZ_FIRED_TRIGGERS] PRIMARY KEY CLUSTERED ([SCHED_NAME], [ENTRY_ID])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table QRTZ_JOB_DETAILS
-- ----------------------------
ALTER TABLE [dbo].[QRTZ_JOB_DETAILS] ADD CONSTRAINT [PK_QRTZ_JOB_DETAILS] PRIMARY KEY CLUSTERED ([SCHED_NAME], [JOB_NAME], [JOB_GROUP])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table QRTZ_LOCKS
-- ----------------------------
ALTER TABLE [dbo].[QRTZ_LOCKS] ADD CONSTRAINT [PK_QRTZ_LOCKS] PRIMARY KEY CLUSTERED ([SCHED_NAME], [LOCK_NAME])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table QRTZ_PAUSED_TRIGGER_GRPS
-- ----------------------------
ALTER TABLE [dbo].[QRTZ_PAUSED_TRIGGER_GRPS] ADD CONSTRAINT [PK_QRTZ_PAUSED_TRIGGER_GRPS] PRIMARY KEY CLUSTERED ([SCHED_NAME], [TRIGGER_GROUP])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table QRTZ_SCHEDULER_STATE
-- ----------------------------
ALTER TABLE [dbo].[QRTZ_SCHEDULER_STATE] ADD CONSTRAINT [PK_QRTZ_SCHEDULER_STATE] PRIMARY KEY CLUSTERED ([SCHED_NAME], [INSTANCE_NAME])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Indexes structure for table QRTZ_SIMPLE_TRIGGERS
-- ----------------------------
CREATE NONCLUSTERED INDEX [IX_QRTZ_SIMPLE_TRIGGERS_QRTZ_TRIGGERS]
ON [dbo].[QRTZ_SIMPLE_TRIGGERS] (
  [SCHED_NAME] ASC,
  [TRIGGER_NAME] ASC,
  [TRIGGER_GROUP] ASC
)
GO


-- ----------------------------
-- Primary Key structure for table QRTZ_SIMPLE_TRIGGERS
-- ----------------------------
ALTER TABLE [dbo].[QRTZ_SIMPLE_TRIGGERS] ADD CONSTRAINT [PK_QRTZ_SIMPLE_TRIGGERS] PRIMARY KEY CLUSTERED ([SCHED_NAME], [TRIGGER_NAME], [TRIGGER_GROUP])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Indexes structure for table QRTZ_SIMPROP_TRIGGERS
-- ----------------------------
CREATE NONCLUSTERED INDEX [IX_QRTZ_SIMPROP_TRIGGERS_QRTZ_TRIGGERS]
ON [dbo].[QRTZ_SIMPROP_TRIGGERS] (
  [SCHED_NAME] ASC,
  [TRIGGER_NAME] ASC,
  [TRIGGER_GROUP] ASC
)
GO


-- ----------------------------
-- Primary Key structure for table QRTZ_SIMPROP_TRIGGERS
-- ----------------------------
ALTER TABLE [dbo].[QRTZ_SIMPROP_TRIGGERS] ADD CONSTRAINT [PK_QRTZ_SIMPROP_TRIGGERS] PRIMARY KEY CLUSTERED ([SCHED_NAME], [TRIGGER_NAME], [TRIGGER_GROUP])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Indexes structure for table QRTZ_TRIGGERS
-- ----------------------------
CREATE NONCLUSTERED INDEX [IX_QRTZ_TRIGGERS_QRTZ_JOB_DETAILS]
ON [dbo].[QRTZ_TRIGGERS] (
  [SCHED_NAME] ASC,
  [TRIGGER_NAME] ASC,
  [TRIGGER_GROUP] ASC
)
GO


-- ----------------------------
-- Primary Key structure for table QRTZ_TRIGGERS
-- ----------------------------
ALTER TABLE [dbo].[QRTZ_TRIGGERS] ADD CONSTRAINT [PK_QRTZ_TRIGGERS] PRIMARY KEY CLUSTERED ([SCHED_NAME], [TRIGGER_NAME], [TRIGGER_GROUP])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for bpm_form
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[bpm_form]', RESEED, 1)
GO


-- ----------------------------
-- Primary Key structure for table bpm_form
-- ----------------------------
ALTER TABLE [dbo].[bpm_form] ADD CONSTRAINT [PK__bpm_form__3213E83F86C2B27F] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for bpm_oa_leave
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[bpm_oa_leave]', RESEED, 1)
GO


-- ----------------------------
-- Primary Key structure for table bpm_oa_leave
-- ----------------------------
ALTER TABLE [dbo].[bpm_oa_leave] ADD CONSTRAINT [PK__bpm_oa_l__3213E83F3569F596] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for bpm_process_definition_ext
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[bpm_process_definition_ext]', RESEED, 1)
GO


-- ----------------------------
-- Primary Key structure for table bpm_process_definition_ext
-- ----------------------------
ALTER TABLE [dbo].[bpm_process_definition_ext] ADD CONSTRAINT [PK__bpm_proc__3213E83F0A8AB015] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for bpm_process_instance_ext
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[bpm_process_instance_ext]', RESEED, 1)
GO


-- ----------------------------
-- Primary Key structure for table bpm_process_instance_ext
-- ----------------------------
ALTER TABLE [dbo].[bpm_process_instance_ext] ADD CONSTRAINT [PK__bpm_proc__3213E83FFD88328F] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for bpm_task_assign_rule
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[bpm_task_assign_rule]', RESEED, 2)
GO


-- ----------------------------
-- Primary Key structure for table bpm_task_assign_rule
-- ----------------------------
ALTER TABLE [dbo].[bpm_task_assign_rule] ADD CONSTRAINT [PK__bpm_task__3213E83F474371C5] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for bpm_task_ext
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[bpm_task_ext]', RESEED, 1)
GO


-- ----------------------------
-- Primary Key structure for table bpm_task_ext
-- ----------------------------
ALTER TABLE [dbo].[bpm_task_ext] ADD CONSTRAINT [PK__bpm_task__3213E83FD8AFE1F9] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for bpm_user_group
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[bpm_user_group]', RESEED, 1)
GO


-- ----------------------------
-- Primary Key structure for table bpm_user_group
-- ----------------------------
ALTER TABLE [dbo].[bpm_user_group] ADD CONSTRAINT [PK__bpm_user__3213E83F25E4725B] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for infra_api_access_log
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[infra_api_access_log]', RESEED, 40615)
GO


-- ----------------------------
-- Primary Key structure for table infra_api_access_log
-- ----------------------------
ALTER TABLE [dbo].[infra_api_access_log] ADD CONSTRAINT [PK__infra_ap__3213E83F04F27A05] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for infra_api_error_log
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[infra_api_error_log]', RESEED, 2021)
GO


-- ----------------------------
-- Primary Key structure for table infra_api_error_log
-- ----------------------------
ALTER TABLE [dbo].[infra_api_error_log] ADD CONSTRAINT [PK__infra_ap__3213E83FCA2446D4] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for infra_codegen_column
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[infra_codegen_column]', RESEED, 1)
GO


-- ----------------------------
-- Primary Key structure for table infra_codegen_column
-- ----------------------------
ALTER TABLE [dbo].[infra_codegen_column] ADD CONSTRAINT [PK__infra_co__3213E83FA9EC5005] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for infra_codegen_table
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[infra_codegen_table]', RESEED, 1)
GO


-- ----------------------------
-- Primary Key structure for table infra_codegen_table
-- ----------------------------
ALTER TABLE [dbo].[infra_codegen_table] ADD CONSTRAINT [PK__infra_co__3213E83F555031D0] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table infra_config
-- ----------------------------
ALTER TABLE [dbo].[infra_config] ADD CONSTRAINT [PK__infra_co__3213E83FF4C71E85] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for infra_data_source_config
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[infra_data_source_config]', RESEED, 9)
GO


-- ----------------------------
-- Primary Key structure for table infra_data_source_config
-- ----------------------------
ALTER TABLE [dbo].[infra_data_source_config] ADD CONSTRAINT [PK__infra_da__3213E83F02D21AEB] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for infra_file
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[infra_file]', RESEED, 1)
GO


-- ----------------------------
-- Auto increment value for infra_file_config
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[infra_file_config]', RESEED, 11)
GO


-- ----------------------------
-- Primary Key structure for table infra_file_config
-- ----------------------------
ALTER TABLE [dbo].[infra_file_config] ADD CONSTRAINT [PK__infra_fi__3213E83F8A7903EA] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for infra_file_content
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[infra_file_content]', RESEED, 1)
GO


-- ----------------------------
-- Primary Key structure for table infra_file_content
-- ----------------------------
ALTER TABLE [dbo].[infra_file_content] ADD CONSTRAINT [PK__infra_fi__3213E83F033E6045] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for infra_job
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[infra_job]', RESEED, 16)
GO


-- ----------------------------
-- Primary Key structure for table infra_job
-- ----------------------------
ALTER TABLE [dbo].[infra_job] ADD CONSTRAINT [PK__infra_jo__3213E83F3C7DE10C] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for infra_job_log
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[infra_job_log]', RESEED, 16)
GO


-- ----------------------------
-- Primary Key structure for table infra_job_log
-- ----------------------------
ALTER TABLE [dbo].[infra_job_log] ADD CONSTRAINT [PK__infra_jo__3213E83F4CA8F353] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for infra_test_demo
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[infra_test_demo]', RESEED, 1)
GO


-- ----------------------------
-- Auto increment value for member_user
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[member_user]', RESEED, 1)
GO


-- ----------------------------
-- Primary Key structure for table member_user
-- ----------------------------
ALTER TABLE [dbo].[member_user] ADD CONSTRAINT [PK__member_u__3213E83F0A9AEC0B] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for pay_app
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[pay_app]', RESEED, 6)
GO


-- ----------------------------
-- Primary Key structure for table pay_app
-- ----------------------------
ALTER TABLE [dbo].[pay_app] ADD CONSTRAINT [PK__pay_app__3213E83FB26E0A6B] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for pay_channel
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[pay_channel]', RESEED, 17)
GO


-- ----------------------------
-- Primary Key structure for table pay_channel
-- ----------------------------
ALTER TABLE [dbo].[pay_channel] ADD CONSTRAINT [PK__pay_chan__3213E83F2556A7FC] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for pay_merchant
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[pay_merchant]', RESEED, 1)
GO


-- ----------------------------
-- Primary Key structure for table pay_merchant
-- ----------------------------
ALTER TABLE [dbo].[pay_merchant] ADD CONSTRAINT [PK__pay_merc__3213E83F010D02B8] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for pay_notify_log
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[pay_notify_log]', RESEED, 1)
GO


-- ----------------------------
-- Primary Key structure for table pay_notify_log
-- ----------------------------
ALTER TABLE [dbo].[pay_notify_log] ADD CONSTRAINT [PK__pay_noti__3213E83F5F4B3447] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for pay_notify_task
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[pay_notify_task]', RESEED, 1)
GO


-- ----------------------------
-- Primary Key structure for table pay_notify_task
-- ----------------------------
ALTER TABLE [dbo].[pay_notify_task] ADD CONSTRAINT [PK__pay_noti__3213E83FB9215103] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for pay_order
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[pay_order]', RESEED, 124)
GO


-- ----------------------------
-- Primary Key structure for table pay_order
-- ----------------------------
ALTER TABLE [dbo].[pay_order] ADD CONSTRAINT [PK__pay_orde__3213E83F34C95271] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for pay_order_extension
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[pay_order_extension]', RESEED, 1)
GO


-- ----------------------------
-- Primary Key structure for table pay_order_extension
-- ----------------------------
ALTER TABLE [dbo].[pay_order_extension] ADD CONSTRAINT [PK__pay_orde__3213E83F5ACB776F] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for pay_refund
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[pay_refund]', RESEED, 1)
GO


-- ----------------------------
-- Primary Key structure for table pay_refund
-- ----------------------------
ALTER TABLE [dbo].[pay_refund] ADD CONSTRAINT [PK__pay_refu__3213E83FBE1B54AC] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_dept
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_dept]', RESEED, 111)
GO


-- ----------------------------
-- Primary Key structure for table system_dept
-- ----------------------------
ALTER TABLE [dbo].[system_dept] ADD CONSTRAINT [PK__system_d__3213E83FFA72847C] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_dict_data
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_dict_data]', RESEED, 1160)
GO


-- ----------------------------
-- Primary Key structure for table system_dict_data
-- ----------------------------
ALTER TABLE [dbo].[system_dict_data] ADD CONSTRAINT [PK__system_d__3213E83F20407597] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_dict_type
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_dict_type]', RESEED, 147)
GO


-- ----------------------------
-- Primary Key structure for table system_dict_type
-- ----------------------------
ALTER TABLE [dbo].[system_dict_type] ADD CONSTRAINT [PK__system_d__3213E83F7C36B1FD] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_error_code
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_error_code]', RESEED, 15466)
GO


-- ----------------------------
-- Primary Key structure for table system_error_code
-- ----------------------------
ALTER TABLE [dbo].[system_error_code] ADD CONSTRAINT [PK__system_e__3213E83F68B8DFD0] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_login_log
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_login_log]', RESEED, 24)
GO


-- ----------------------------
-- Primary Key structure for table system_login_log
-- ----------------------------
ALTER TABLE [dbo].[system_login_log] ADD CONSTRAINT [PK__system_l__3213E83F717953E9] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_menu
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_menu]', RESEED, 1267)
GO


-- ----------------------------
-- Primary Key structure for table system_menu
-- ----------------------------
ALTER TABLE [dbo].[system_menu] ADD CONSTRAINT [PK__system_m__3213E83F14175801] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_notice
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_notice]', RESEED, 4)
GO


-- ----------------------------
-- Primary Key structure for table system_notice
-- ----------------------------
ALTER TABLE [dbo].[system_notice] ADD CONSTRAINT [PK__system_n__3213E83FA158BA8D] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_oauth2_access_token
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_oauth2_access_token]', RESEED, 5)
GO


-- ----------------------------
-- Auto increment value for system_oauth2_approve
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_oauth2_approve]', RESEED, 2)
GO


-- ----------------------------
-- Primary Key structure for table system_oauth2_approve
-- ----------------------------
ALTER TABLE [dbo].[system_oauth2_approve] ADD CONSTRAINT [PK__system_o__3213E83F7CC08ED6] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_oauth2_client
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_oauth2_client]', RESEED, 1)
GO


-- ----------------------------
-- Auto increment value for system_oauth2_code
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_oauth2_code]', RESEED, 4)
GO


-- ----------------------------
-- Primary Key structure for table system_oauth2_code
-- ----------------------------
ALTER TABLE [dbo].[system_oauth2_code] ADD CONSTRAINT [PK__system_o__3213E83F38C13543] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_oauth2_refresh_token
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_oauth2_refresh_token]', RESEED, 3)
GO


-- ----------------------------
-- Primary Key structure for table system_oauth2_refresh_token
-- ----------------------------
ALTER TABLE [dbo].[system_oauth2_refresh_token] ADD CONSTRAINT [PK__system_o__3213E83FCFB541CC] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_operate_log
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_operate_log]', RESEED, 19)
GO


-- ----------------------------
-- Primary Key structure for table system_operate_log
-- ----------------------------
ALTER TABLE [dbo].[system_operate_log] ADD CONSTRAINT [PK__system_o__3213E83F85EC81FD] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_post
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_post]', RESEED, 4)
GO


-- ----------------------------
-- Primary Key structure for table system_post
-- ----------------------------
ALTER TABLE [dbo].[system_post] ADD CONSTRAINT [PK__system_p__3213E83FBC098F34] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_role
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_role]', RESEED, 111)
GO


-- ----------------------------
-- Primary Key structure for table system_role
-- ----------------------------
ALTER TABLE [dbo].[system_role] ADD CONSTRAINT [PK__system_r__3213E83F209B43F2] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_role_menu
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_role_menu]', RESEED, 191)
GO


-- ----------------------------
-- Primary Key structure for table system_role_menu
-- ----------------------------
ALTER TABLE [dbo].[system_role_menu] ADD CONSTRAINT [PK__system_r__3213E83F6F1E4A9B] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_sensitive_word
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_sensitive_word]', RESEED, 4)
GO


-- ----------------------------
-- Primary Key structure for table system_sensitive_word
-- ----------------------------
ALTER TABLE [dbo].[system_sensitive_word] ADD CONSTRAINT [PK__system_s__3213E83FFFD8E555] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_sms_channel
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_sms_channel]', RESEED, 6)
GO


-- ----------------------------
-- Primary Key structure for table system_sms_channel
-- ----------------------------
ALTER TABLE [dbo].[system_sms_channel] ADD CONSTRAINT [PK__system_s__3213E83FA96B966E] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_sms_code
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_sms_code]', RESEED, 470)
GO


-- ----------------------------
-- Primary Key structure for table system_sms_code
-- ----------------------------
ALTER TABLE [dbo].[system_sms_code] ADD CONSTRAINT [PK__system_s__3213E83F825CBCB9] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_sms_log
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_sms_log]', RESEED, 6)
GO


-- ----------------------------
-- Primary Key structure for table system_sms_log
-- ----------------------------
ALTER TABLE [dbo].[system_sms_log] ADD CONSTRAINT [PK__system_s__3213E83F5F1968A9] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_sms_template
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_sms_template]', RESEED, 13)
GO


-- ----------------------------
-- Primary Key structure for table system_sms_template
-- ----------------------------
ALTER TABLE [dbo].[system_sms_template] ADD CONSTRAINT [PK__system_s__3213E83F5C91CA37] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_social_user
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_social_user]', RESEED, 1)
GO


-- ----------------------------
-- Primary Key structure for table system_social_user
-- ----------------------------
ALTER TABLE [dbo].[system_social_user] ADD CONSTRAINT [PK__system_s__3213E83F6EF3863C] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_social_user_bind
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_social_user_bind]', RESEED, 1)
GO


-- ----------------------------
-- Primary Key structure for table system_social_user_bind
-- ----------------------------
ALTER TABLE [dbo].[system_social_user_bind] ADD CONSTRAINT [PK__system_s__3213E83F21F44049] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_tenant
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_tenant]', RESEED, 122)
GO


-- ----------------------------
-- Primary Key structure for table system_tenant
-- ----------------------------
ALTER TABLE [dbo].[system_tenant] ADD CONSTRAINT [PK__system_t__3213E83FAF444092] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_tenant_package
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_tenant_package]', RESEED, 111)
GO


-- ----------------------------
-- Primary Key structure for table system_tenant_package
-- ----------------------------
ALTER TABLE [dbo].[system_tenant_package] ADD CONSTRAINT [PK__system_t__3213E83FA2213DB5] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_user_post
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_user_post]', RESEED, 115)
GO


-- ----------------------------
-- Primary Key structure for table system_user_post
-- ----------------------------
ALTER TABLE [dbo].[system_user_post] ADD CONSTRAINT [PK__system_u__3213E83F56DD4107] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_user_role
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_user_role]', RESEED, 17)
GO


-- ----------------------------
-- Primary Key structure for table system_user_role
-- ----------------------------
ALTER TABLE [dbo].[system_user_role] ADD CONSTRAINT [PK__system_u__3213E83F3593F652] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Auto increment value for system_users
-- ----------------------------
DBCC CHECKIDENT ('[dbo].[system_users]', RESEED, 115)
GO


-- ----------------------------
-- Primary Key structure for table system_users
-- ----------------------------
ALTER TABLE [dbo].[system_users] ADD CONSTRAINT [PK__system_u__3213E83F7CF2516E] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
ON [PRIMARY]
GO


-- ----------------------------
-- Foreign Keys structure for table QRTZ_BLOB_TRIGGERS
-- ----------------------------
ALTER TABLE [dbo].[QRTZ_BLOB_TRIGGERS] ADD CONSTRAINT [FK_QRTZ_BLOB_TRIGGERS_QRTZ_TRIGGERS] FOREIGN KEY ([SCHED_NAME], [TRIGGER_NAME], [TRIGGER_GROUP]) REFERENCES [dbo].[QRTZ_TRIGGERS] ([SCHED_NAME], [TRIGGER_NAME], [TRIGGER_GROUP]) ON DELETE CASCADE ON UPDATE NO ACTION
GO


-- ----------------------------
-- Foreign Keys structure for table QRTZ_CRON_TRIGGERS
-- ----------------------------
ALTER TABLE [dbo].[QRTZ_CRON_TRIGGERS] ADD CONSTRAINT [FK_QRTZ_CRON_TRIGGERS_QRTZ_TRIGGERS] FOREIGN KEY ([SCHED_NAME], [TRIGGER_NAME], [TRIGGER_GROUP]) REFERENCES [dbo].[QRTZ_TRIGGERS] ([SCHED_NAME], [TRIGGER_NAME], [TRIGGER_GROUP]) ON DELETE CASCADE ON UPDATE NO ACTION
GO


-- ----------------------------
-- Foreign Keys structure for table QRTZ_SIMPLE_TRIGGERS
-- ----------------------------
ALTER TABLE [dbo].[QRTZ_SIMPLE_TRIGGERS] ADD CONSTRAINT [FK_QRTZ_SIMPLE_TRIGGERS_QRTZ_TRIGGERS] FOREIGN KEY ([SCHED_NAME], [TRIGGER_NAME], [TRIGGER_GROUP]) REFERENCES [dbo].[QRTZ_TRIGGERS] ([SCHED_NAME], [TRIGGER_NAME], [TRIGGER_GROUP]) ON DELETE CASCADE ON UPDATE NO ACTION
GO


-- ----------------------------
-- Foreign Keys structure for table QRTZ_SIMPROP_TRIGGERS
-- ----------------------------
ALTER TABLE [dbo].[QRTZ_SIMPROP_TRIGGERS] ADD CONSTRAINT [FK_QRTZ_SIMPROP_TRIGGERS_QRTZ_TRIGGERS] FOREIGN KEY ([SCHED_NAME], [TRIGGER_NAME], [TRIGGER_GROUP]) REFERENCES [dbo].[QRTZ_TRIGGERS] ([SCHED_NAME], [TRIGGER_NAME], [TRIGGER_GROUP]) ON DELETE CASCADE ON UPDATE NO ACTION
GO


-- ----------------------------
-- Foreign Keys structure for table QRTZ_TRIGGERS
-- ----------------------------
ALTER TABLE [dbo].[QRTZ_TRIGGERS] ADD CONSTRAINT [FK_QRTZ_TRIGGERS_QRTZ_JOB_DETAILS] FOREIGN KEY ([SCHED_NAME], [JOB_NAME], [JOB_GROUP]) REFERENCES [dbo].[QRTZ_JOB_DETAILS] ([SCHED_NAME], [JOB_NAME], [JOB_GROUP]) ON DELETE NO ACTION ON UPDATE NO ACTION
GO

