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
