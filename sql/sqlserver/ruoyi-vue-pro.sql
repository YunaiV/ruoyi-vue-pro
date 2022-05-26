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

 Date: 17/05/2022 09:46:25
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

