/*
 注意：仅仅需要 Quartz 定时任务的场景，可选！！！

 Date: 30/04/2024 09:54:18
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