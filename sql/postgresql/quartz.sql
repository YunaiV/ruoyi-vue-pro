-- ----------------------------
-- qrtz_blob_triggers
-- ----------------------------
CREATE TABLE qrtz_blob_triggers
(
    sched_name    varchar(120) NOT NULL,
    trigger_name  varchar(190) NOT NULL,
    trigger_group varchar(190) NOT NULL,
    blob_data     bytea        NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

CREATE INDEX idx_qrtz_blob_triggers_sched_name ON qrtz_blob_triggers (sched_name, trigger_name, trigger_group);

-- ----------------------------
-- qrtz_calendars
-- ----------------------------
CREATE TABLE qrtz_calendars
(
    sched_name    varchar(120) NOT NULL,
    calendar_name varchar(190) NOT NULL,
    calendar      bytea        NOT NULL,
    PRIMARY KEY (sched_name, calendar_name)
);


-- ----------------------------
-- qrtz_cron_triggers
-- ----------------------------
CREATE TABLE qrtz_cron_triggers
(
    sched_name      varchar(120) NOT NULL,
    trigger_name    varchar(190) NOT NULL,
    trigger_group   varchar(190) NOT NULL,
    cron_expression varchar(120) NOT NULL,
    time_zone_id    varchar(80)  NULL DEFAULT NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

-- @formatter:off
BEGIN;
INSERT INTO qrtz_cron_triggers (sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('schedulerName', 'accessLogCleanJob', 'DEFAULT', '0 0 0 * * ?', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers (sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('schedulerName', 'brokerageRecordUnfreezeJob', 'DEFAULT', '0 * * * * ?', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers (sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('schedulerName', 'demoJob', 'DEFAULT', '0 0 0 * * ?', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers (sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('schedulerName', 'errorLogCleanJob', 'DEFAULT', '0 0 0 * * ?', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers (sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('schedulerName', 'jobLogCleanJob', 'DEFAULT', '0 0 0 * * ?', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers (sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('schedulerName', 'payNotifyJob', 'DEFAULT', '* * * * * ?', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers (sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('schedulerName', 'payOrderExpireJob', 'DEFAULT', '0 0/1 * * * ?', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers (sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('schedulerName', 'payOrderSyncJob', 'DEFAULT', '0 0/1 * * * ?', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers (sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('schedulerName', 'payRefundSyncJob', 'DEFAULT', '0 0/1 * * * ?', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers (sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('schedulerName', 'tradeOrderAutoCancelJob', 'DEFAULT', '0 * * * * ?', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers (sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('schedulerName', 'tradeOrderAutoCommentJob', 'DEFAULT', '0 * * * * ?', 'Asia/Shanghai');
INSERT INTO qrtz_cron_triggers (sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) VALUES ('schedulerName', 'tradeOrderAutoReceiveJob', 'DEFAULT', '0 * * * * ?', 'Asia/Shanghai');
COMMIT;
-- @formatter:on

-- ----------------------------
-- qrtz_fired_triggers
-- ----------------------------
CREATE TABLE qrtz_fired_triggers
(
    sched_name        varchar(120) NOT NULL,
    entry_id          varchar(95)  NOT NULL,
    trigger_name      varchar(190) NOT NULL,
    trigger_group     varchar(190) NOT NULL,
    instance_name     varchar(190) NOT NULL,
    fired_time        int8         NOT NULL,
    sched_time        int8         NOT NULL,
    priority          int4         NOT NULL,
    state             varchar(16)  NOT NULL,
    job_name          varchar(190) NULL DEFAULT NULL,
    job_group         varchar(190) NULL DEFAULT NULL,
    is_nonconcurrent  varchar(1)   NULL DEFAULT NULL,
    requests_recovery varchar(1)   NULL DEFAULT NULL,
    PRIMARY KEY (sched_name, entry_id)
);

CREATE INDEX idx_qrtz_ft_trig_inst_name ON qrtz_fired_triggers (sched_name, instance_name);
CREATE INDEX idx_qrtz_ft_inst_job_req_rcvry ON qrtz_fired_triggers (sched_name, instance_name, requests_recovery);
CREATE INDEX idx_qrtz_ft_j_g ON qrtz_fired_triggers (sched_name, job_name, job_group);
CREATE INDEX idx_qrtz_ft_jg ON qrtz_fired_triggers (sched_name, job_group);
CREATE INDEX idx_qrtz_ft_t_g ON qrtz_fired_triggers (sched_name, trigger_name, trigger_group);
CREATE INDEX idx_qrtz_ft_tg ON qrtz_fired_triggers (sched_name, trigger_group);

-- ----------------------------
-- qrtz_job_details
-- ----------------------------
CREATE TABLE qrtz_job_details
(
    sched_name        varchar(120) NOT NULL,
    job_name          varchar(190) NOT NULL,
    job_group         varchar(190) NOT NULL,
    description       varchar(250) NULL DEFAULT NULL,
    job_class_name    varchar(250) NOT NULL,
    is_durable        varchar(1)   NOT NULL,
    is_nonconcurrent  varchar(1)   NOT NULL,
    is_update_data    varchar(1)   NOT NULL,
    requests_recovery varchar(1)   NOT NULL,
    job_data          bytea        NULL,
    PRIMARY KEY (sched_name, job_name, job_group)
);

CREATE INDEX idx_qrtz_j_req_recovery ON qrtz_job_details (sched_name, requests_recovery);
CREATE INDEX idx_qrtz_j_grp ON qrtz_job_details (sched_name, job_group);

-- @formatter:off
BEGIN;
INSERT INTO "qrtz_job_details" ("sched_name", "job_name", "job_group", "description", "job_class_name", "is_durable", "is_nonconcurrent", "is_update_data", "requests_recovery", "job_data") VALUES ('schedulerName', 'accessLogCleanJob', 'DEFAULT', NULL, 'cn.iocoder.yudao.framework.quartz.core.handler.JobHandlerInvoker', '0', '1', '1', '0', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\002t\\000\\006JOB_IDsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\031t\\000\\020JOB_HANDLER_NAMEt\\000\\021accessLogCleanJobx\\000');
INSERT INTO "qrtz_job_details" ("sched_name", "job_name", "job_group", "description", "job_class_name", "is_durable", "is_nonconcurrent", "is_update_data", "requests_recovery", "job_data") VALUES ('schedulerName', 'brokerageRecordUnfreezeJob', 'DEFAULT', NULL, 'cn.iocoder.yudao.framework.quartz.core.handler.JobHandlerInvoker', '0', '1', '1', '0', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\002t\\000\\006JOB_IDsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\030t\\000\\020JOB_HANDLER_NAMEt\\000\\032brokerageRecordUnfreezeJobx\\000');
INSERT INTO "qrtz_job_details" ("sched_name", "job_name", "job_group", "description", "job_class_name", "is_durable", "is_nonconcurrent", "is_update_data", "requests_recovery", "job_data") VALUES ('schedulerName', 'demoJob', 'DEFAULT', NULL, 'cn.iocoder.yudao.framework.quartz.core.handler.JobHandlerInvoker', '0', '1', '1', '0', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\002t\\000\\006JOB_IDsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\024t\\000\\020JOB_HANDLER_NAMEt\\000\\007demoJobx\\000');
INSERT INTO "qrtz_job_details" ("sched_name", "job_name", "job_group", "description", "job_class_name", "is_durable", "is_nonconcurrent", "is_update_data", "requests_recovery", "job_data") VALUES ('schedulerName', 'errorLogCleanJob', 'DEFAULT', NULL, 'cn.iocoder.yudao.framework.quartz.core.handler.JobHandlerInvoker', '0', '1', '1', '0', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\002t\\000\\006JOB_IDsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\032t\\000\\020JOB_HANDLER_NAMEt\\000\\020errorLogCleanJobx\\000');
INSERT INTO "qrtz_job_details" ("sched_name", "job_name", "job_group", "description", "job_class_name", "is_durable", "is_nonconcurrent", "is_update_data", "requests_recovery", "job_data") VALUES ('schedulerName', 'jobLogCleanJob', 'DEFAULT', NULL, 'cn.iocoder.yudao.framework.quartz.core.handler.JobHandlerInvoker', '0', '1', '1', '0', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\002t\\000\\006JOB_IDsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\033t\\000\\020JOB_HANDLER_NAMEt\\000\\016jobLogCleanJobx\\000');
INSERT INTO "qrtz_job_details" ("sched_name", "job_name", "job_group", "description", "job_class_name", "is_durable", "is_nonconcurrent", "is_update_data", "requests_recovery", "job_data") VALUES ('schedulerName', 'payNotifyJob', 'DEFAULT', NULL, 'cn.iocoder.yudao.framework.quartz.core.handler.JobHandlerInvoker', '0', '1', '1', '0', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\002t\\000\\006JOB_IDsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\005t\\000\\020JOB_HANDLER_NAMEt\\000\\014payNotifyJobx\\000');
INSERT INTO "qrtz_job_details" ("sched_name", "job_name", "job_group", "description", "job_class_name", "is_durable", "is_nonconcurrent", "is_update_data", "requests_recovery", "job_data") VALUES ('schedulerName', 'payOrderExpireJob', 'DEFAULT', NULL, 'cn.iocoder.yudao.framework.quartz.core.handler.JobHandlerInvoker', '0', '1', '1', '0', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\002t\\000\\006JOB_IDsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\022t\\000\\020JOB_HANDLER_NAMEt\\000\\021payOrderExpireJobx\\000');
INSERT INTO "qrtz_job_details" ("sched_name", "job_name", "job_group", "description", "job_class_name", "is_durable", "is_nonconcurrent", "is_update_data", "requests_recovery", "job_data") VALUES ('schedulerName', 'payOrderSyncJob', 'DEFAULT', NULL, 'cn.iocoder.yudao.framework.quartz.core.handler.JobHandlerInvoker', '0', '1', '1', '0', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\002t\\000\\006JOB_IDsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\021t\\000\\020JOB_HANDLER_NAMEt\\000\\017payOrderSyncJobx\\000');
INSERT INTO "qrtz_job_details" ("sched_name", "job_name", "job_group", "description", "job_class_name", "is_durable", "is_nonconcurrent", "is_update_data", "requests_recovery", "job_data") VALUES ('schedulerName', 'payRefundSyncJob', 'DEFAULT', NULL, 'cn.iocoder.yudao.framework.quartz.core.handler.JobHandlerInvoker', '0', '1', '1', '0', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\002t\\000\\006JOB_IDsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\023t\\000\\020JOB_HANDLER_NAMEt\\000\\020payRefundSyncJobx\\000');
INSERT INTO "qrtz_job_details" ("sched_name", "job_name", "job_group", "description", "job_class_name", "is_durable", "is_nonconcurrent", "is_update_data", "requests_recovery", "job_data") VALUES ('schedulerName', 'tradeOrderAutoCancelJob', 'DEFAULT', NULL, 'cn.iocoder.yudao.framework.quartz.core.handler.JobHandlerInvoker', '0', '1', '1', '0', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\002t\\000\\006JOB_IDsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\025t\\000\\020JOB_HANDLER_NAMEt\\000\\027tradeOrderAutoCancelJobx\\000');
INSERT INTO "qrtz_job_details" ("sched_name", "job_name", "job_group", "description", "job_class_name", "is_durable", "is_nonconcurrent", "is_update_data", "requests_recovery", "job_data") VALUES ('schedulerName', 'tradeOrderAutoCommentJob', 'DEFAULT', NULL, 'cn.iocoder.yudao.framework.quartz.core.handler.JobHandlerInvoker', '0', '1', '1', '0', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\002t\\000\\006JOB_IDsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\027t\\000\\020JOB_HANDLER_NAMEt\\000\\030tradeOrderAutoCommentJobx\\000');
INSERT INTO "qrtz_job_details" ("sched_name", "job_name", "job_group", "description", "job_class_name", "is_durable", "is_nonconcurrent", "is_update_data", "requests_recovery", "job_data") VALUES ('schedulerName', 'tradeOrderAutoReceiveJob', 'DEFAULT', NULL, 'cn.iocoder.yudao.framework.quartz.core.handler.JobHandlerInvoker', '0', '1', '1', '0', E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\002t\\000\\006JOB_IDsr\\000\\016java.lang.Long;\\213\\344\\220\\314\\217#\\337\\002\\000\\001J\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000\\000\\000\\000\\026t\\000\\020JOB_HANDLER_NAMEt\\000\\030tradeOrderAutoReceiveJobx\\000');
COMMIT;
-- @formatter:on

-- ----------------------------
-- qrtz_locks
-- ----------------------------
CREATE TABLE qrtz_locks
(
    sched_name varchar(120) NOT NULL,
    lock_name  varchar(40)  NOT NULL,
    PRIMARY KEY (sched_name, lock_name)
);

-- @formatter:off
BEGIN;
INSERT INTO qrtz_locks (sched_name, lock_name) VALUES ('schedulerName', 'STATE_ACCESS');
INSERT INTO qrtz_locks (sched_name, lock_name) VALUES ('schedulerName', 'TRIGGER_ACCESS');
COMMIT;
-- @formatter:on

-- ----------------------------
-- qrtz_paused_trigger_grps
-- ----------------------------
CREATE TABLE qrtz_paused_trigger_grps
(
    sched_name    varchar(120) NOT NULL,
    trigger_group varchar(190) NOT NULL,
    PRIMARY KEY (sched_name, trigger_group)
);

-- ----------------------------
-- qrtz_scheduler_state
-- ----------------------------
CREATE TABLE qrtz_scheduler_state
(
    sched_name        varchar(120) NOT NULL,
    instance_name     varchar(190) NOT NULL,
    last_checkin_time int8         NOT NULL,
    checkin_interval  int8         NOT NULL,
    PRIMARY KEY (sched_name, instance_name)
);

-- @formatter:off
BEGIN;
INSERT INTO qrtz_scheduler_state (sched_name, instance_name, last_checkin_time, checkin_interval) VALUES ('schedulerName', 'MacBook-Pro.local1701948801197', 1701948848400, 15000);
COMMIT;
-- @formatter:on

-- ----------------------------
-- qrtz_simple_triggers
-- ----------------------------
CREATE TABLE qrtz_simple_triggers
(
    sched_name      varchar(120) NOT NULL,
    trigger_name    varchar(190) NOT NULL,
    trigger_group   varchar(190) NOT NULL,
    repeat_count    int8         NOT NULL,
    repeat_interval int8         NOT NULL,
    times_triggered int8         NOT NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

-- ----------------------------
-- qrtz_simprop_triggers
-- ----------------------------
CREATE TABLE qrtz_simprop_triggers
(
    sched_name    varchar(120)   NOT NULL,
    trigger_name  varchar(190)   NOT NULL,
    trigger_group varchar(190)   NOT NULL,
    str_prop_1    varchar(512)   NULL DEFAULT NULL,
    str_prop_2    varchar(512)   NULL DEFAULT NULL,
    str_prop_3    varchar(512)   NULL DEFAULT NULL,
    int_prop_1    int4           NULL DEFAULT NULL,
    int_prop_2    int4           NULL DEFAULT NULL,
    long_prop_1   int8           NULL DEFAULT NULL,
    long_prop_2   int8           NULL DEFAULT NULL,
    dec_prop_1    numeric(13, 4) NULL DEFAULT NULL,
    dec_prop_2    numeric(13, 4) NULL DEFAULT NULL,
    bool_prop_1   varchar(1)     NULL DEFAULT NULL,
    bool_prop_2   varchar(1)     NULL DEFAULT NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

-- ----------------------------
-- qrtz_triggers
-- ----------------------------
CREATE TABLE qrtz_triggers
(
    sched_name     varchar(120) NOT NULL,
    trigger_name   varchar(190) NOT NULL,
    trigger_group  varchar(190) NOT NULL,
    job_name       varchar(190) NOT NULL,
    job_group      varchar(190) NOT NULL,
    description    varchar(250) NULL DEFAULT NULL,
    next_fire_time int8         NULL DEFAULT NULL,
    prev_fire_time int8         NULL DEFAULT NULL,
    priority       int4         NULL DEFAULT NULL,
    trigger_state  varchar(16)  NOT NULL,
    trigger_type   varchar(8)   NOT NULL,
    start_time     int8         NOT NULL,
    end_time       int8         NULL DEFAULT NULL,
    calendar_name  varchar(190) NULL DEFAULT NULL,
    misfire_instr  int2         NULL DEFAULT NULL,
    job_data       bytea        NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group)
);

CREATE INDEX idx_qrtz_t_j ON qrtz_triggers (sched_name, job_name, job_group);
CREATE INDEX idx_qrtz_t_jg ON qrtz_triggers (sched_name, job_group);
CREATE INDEX idx_qrtz_t_c ON qrtz_triggers (sched_name, calendar_name);
CREATE INDEX idx_qrtz_t_g ON qrtz_triggers (sched_name, trigger_group);
CREATE INDEX idx_qrtz_t_state ON qrtz_triggers (sched_name, trigger_state);
CREATE INDEX idx_qrtz_t_n_state ON qrtz_triggers (sched_name, trigger_name, trigger_group, trigger_state);
CREATE INDEX idx_qrtz_t_n_g_state ON qrtz_triggers (sched_name, trigger_group, trigger_state);
CREATE INDEX idx_qrtz_t_next_fire_time ON qrtz_triggers (sched_name, next_fire_time);
CREATE INDEX idx_qrtz_t_nft_st ON qrtz_triggers (sched_name, trigger_state, next_fire_time);
CREATE INDEX idx_qrtz_t_nft_misfire ON qrtz_triggers (sched_name, misfire_instr, next_fire_time);
CREATE INDEX idx_qrtz_t_nft_st_misfire ON qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_state);
CREATE INDEX idx_qrtz_t_nft_st_misfire_grp ON qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_group,
                                                             trigger_state);

-- @formatter:off
BEGIN;
INSERT INTO qrtz_triggers (sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('schedulerName', 'accessLogCleanJob', 'DEFAULT', 'accessLogCleanJob', 'DEFAULT', NULL, 1696348800000, -1, 5, 'PAUSED', 'CRON', 1696301981000, 0, NULL, 0, E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\003t\\000\\021JOB_HANDLER_PARAMt\\000\\000t\\000\\022JOB_RETRY_INTERVALsr\\000\\021java.lang.Integer\\022\\342\\240\\244\\367\\201\\2078\\002\\000\\001I\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000t\\000\\017JOB_RETRY_COUNTsq\\000~\\000\\012\\000\\000\\000\\003x\\000');
INSERT INTO qrtz_triggers (sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('schedulerName', 'brokerageRecordUnfreezeJob', 'DEFAULT', 'brokerageRecordUnfreezeJob', 'DEFAULT', NULL, 1695909720000, -1, 5, 'PAUSED', 'CRON', 1695909706000, 0, NULL, 0, E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\003t\\000\\021JOB_HANDLER_PARAMt\\000\\000t\\000\\022JOB_RETRY_INTERVALsr\\000\\021java.lang.Integer\\022\\342\\240\\244\\367\\201\\2078\\002\\000\\001I\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000t\\000\\017JOB_RETRY_COUNTsq\\000~\\000\\012\\000\\000\\000\\003x\\000');
INSERT INTO qrtz_triggers (sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('schedulerName', 'demoJob', 'DEFAULT', 'demoJob', 'DEFAULT', NULL, 1701964800000, 1701948834291, 5, 'PAUSED', 'CRON', 1694844083000, 0, NULL, 0, E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\003t\\000\\021JOB_HANDLER_PARAMt\\000\\000t\\000\\022JOB_RETRY_INTERVALsr\\000\\021java.lang.Integer\\022\\342\\240\\244\\367\\201\\2078\\002\\000\\001I\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\012t\\000\\017JOB_RETRY_COUNTsq\\000~\\000\\012\\000\\000\\000\\001x\\000');
INSERT INTO qrtz_triggers (sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('schedulerName', 'errorLogCleanJob', 'DEFAULT', 'errorLogCleanJob', 'DEFAULT', NULL, 1696348800000, -1, 5, 'PAUSED', 'CRON', 1696302043000, 0, NULL, 0, E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\003t\\000\\021JOB_HANDLER_PARAMt\\000\\000t\\000\\022JOB_RETRY_INTERVALsr\\000\\021java.lang.Integer\\022\\342\\240\\244\\367\\201\\2078\\002\\000\\001I\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000t\\000\\017JOB_RETRY_COUNTsq\\000~\\000\\012\\000\\000\\000\\003x\\000');
INSERT INTO qrtz_triggers (sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('schedulerName', 'jobLogCleanJob', 'DEFAULT', 'jobLogCleanJob', 'DEFAULT', NULL, 1696348800000, -1, 5, 'PAUSED', 'CRON', 1696302092000, 0, NULL, 0, E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\003t\\000\\021JOB_HANDLER_PARAMt\\000\\000t\\000\\022JOB_RETRY_INTERVALsr\\000\\021java.lang.Integer\\022\\342\\240\\244\\367\\201\\2078\\002\\000\\001I\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000t\\000\\017JOB_RETRY_COUNTsq\\000~\\000\\012\\000\\000\\000\\003x\\000');
INSERT INTO qrtz_triggers (sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('schedulerName', 'payNotifyJob', 'DEFAULT', 'payNotifyJob', 'DEFAULT', NULL, 1688907102000, 1688907101000, 5, 'PAUSED', 'CRON', 1635294882000, 0, NULL, 0, E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\003t\\000\\021JOB_HANDLER_PARAMpt\\000\\022JOB_RETRY_INTERVALsr\\000\\021java.lang.Integer\\022\\342\\240\\244\\367\\201\\2078\\002\\000\\001I\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000t\\000\\017JOB_RETRY_COUNTq\\000~\\000\\013x\\000');
INSERT INTO qrtz_triggers (sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('schedulerName', 'payOrderExpireJob', 'DEFAULT', 'payOrderExpireJob', 'DEFAULT', NULL, 1690011600000, -1, 5, 'PAUSED', 'CRON', 1690011553000, 0, NULL, 0, E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\003t\\000\\021JOB_HANDLER_PARAMpt\\000\\022JOB_RETRY_INTERVALsr\\000\\021java.lang.Integer\\022\\342\\240\\244\\367\\201\\2078\\002\\000\\001I\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000t\\000\\017JOB_RETRY_COUNTq\\000~\\000\\013x\\000');
INSERT INTO qrtz_triggers (sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('schedulerName', 'payOrderSyncJob', 'DEFAULT', 'payOrderSyncJob', 'DEFAULT', NULL, 1690011600000, 1690011540000, 5, 'PAUSED', 'CRON', 1690007785000, 0, NULL, 0, E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\003t\\000\\021JOB_HANDLER_PARAMpt\\000\\022JOB_RETRY_INTERVALsr\\000\\021java.lang.Integer\\022\\342\\240\\244\\367\\201\\2078\\002\\000\\001I\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000t\\000\\017JOB_RETRY_COUNTq\\000~\\000\\013x\\000');
INSERT INTO qrtz_triggers (sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('schedulerName', 'payRefundSyncJob', 'DEFAULT', 'payRefundSyncJob', 'DEFAULT', NULL, 1690117560000, 1690117500000, 5, 'PAUSED', 'CRON', 1690117424000, 0, NULL, 0, E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\003t\\000\\021JOB_HANDLER_PARAMpt\\000\\022JOB_RETRY_INTERVALsr\\000\\021java.lang.Integer\\022\\342\\240\\244\\367\\201\\2078\\002\\000\\001I\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000t\\000\\017JOB_RETRY_COUNTq\\000~\\000\\013x\\000');
INSERT INTO qrtz_triggers (sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('schedulerName', 'tradeOrderAutoCancelJob', 'DEFAULT', 'tradeOrderAutoCancelJob', 'DEFAULT', NULL, 1695727440000, 1695727380000, 5, 'PAUSED', 'CRON', 1695656605000, 0, NULL, 0, E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\003t\\000\\021JOB_HANDLER_PARAMt\\000\\000t\\000\\022JOB_RETRY_INTERVALsr\\000\\021java.lang.Integer\\022\\342\\240\\244\\367\\201\\2078\\002\\000\\001I\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000t\\000\\017JOB_RETRY_COUNTsq\\000~\\000\\012\\000\\000\\000\\003x\\000');
INSERT INTO qrtz_triggers (sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('schedulerName', 'tradeOrderAutoCommentJob', 'DEFAULT', 'tradeOrderAutoCommentJob', 'DEFAULT', NULL, 1695783840000, 1695783780000, 5, 'PAUSED', 'CRON', 1695742709000, 0, NULL, 0, E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\003t\\000\\021JOB_HANDLER_PARAMt\\000\\000t\\000\\022JOB_RETRY_INTERVALsr\\000\\021java.lang.Integer\\022\\342\\240\\244\\367\\201\\2078\\002\\000\\001I\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000t\\000\\017JOB_RETRY_COUNTsq\\000~\\000\\012\\000\\000\\000\\003x\\000');
INSERT INTO qrtz_triggers (sched_name, trigger_name, trigger_group, job_name, job_group, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) VALUES ('schedulerName', 'tradeOrderAutoReceiveJob', 'DEFAULT', 'tradeOrderAutoReceiveJob', 'DEFAULT', NULL, 1695742740000, 1695742680000, 5, 'PAUSED', 'CRON', 1695727433000, 0, NULL, 0, E'\\254\\355\\000\\005sr\\000\\025org.quartz.JobDataMap\\237\\260\\203\\350\\277\\251\\260\\313\\002\\000\\000xr\\000&org.quartz.utils.StringKeyDirtyFlagMap\\202\\010\\350\\303\\373\\305](\\002\\000\\001Z\\000\\023allowsTransientDataxr\\000\\035org.quartz.utils.DirtyFlagMap\\023\\346.\\255(v\\012\\316\\002\\000\\002Z\\000\\005dirtyL\\000\\003mapt\\000\\017Ljava/util/Map;xp\\001sr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\003t\\000\\021JOB_HANDLER_PARAMt\\000\\000t\\000\\022JOB_RETRY_INTERVALsr\\000\\021java.lang.Integer\\022\\342\\240\\244\\367\\201\\2078\\002\\000\\001I\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\000\\000\\000\\000t\\000\\017JOB_RETRY_COUNTsq\\000~\\000\\012\\000\\000\\000\\003x\\000');
COMMIT;
-- @formatter:on


-- ----------------------------
-- FK: qrtz_blob_triggers
-- ----------------------------
ALTER TABLE qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_ibfk_1 FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers (sched_name,
                                                                                                                             trigger_name,
                                                                                                                             trigger_group);

-- ----------------------------
-- FK: qrtz_cron_triggers
-- ----------------------------
ALTER TABLE qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_ibfk_1 FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers (sched_name, trigger_name, trigger_group);

-- ----------------------------
-- FK: qrtz_simple_triggers
-- ----------------------------
ALTER TABLE qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_ibfk_1 FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers (sched_name,
                                                                                                                               trigger_name,
                                                                                                                               trigger_group);

-- ----------------------------
-- FK: qrtz_simprop_triggers
-- ----------------------------
ALTER TABLE qrtz_simprop_triggers
    ADD CONSTRAINT qrtz_simprop_triggers_ibfk_1 FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers (sched_name, trigger_name, trigger_group);

-- ----------------------------
-- FK: qrtz_triggers
-- ----------------------------
ALTER TABLE qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_ibfk_1 FOREIGN KEY (sched_name, job_name, job_group) REFERENCES qrtz_job_details (sched_name, job_name, job_group);
