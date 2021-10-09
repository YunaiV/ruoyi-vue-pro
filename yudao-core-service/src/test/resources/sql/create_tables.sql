-- inf 开头的 DB

-- sys 开头的 DB

CREATE TABLE IF NOT EXISTS `sys_user_session` (
    `id` varchar(32) NOT NULL,
    `user_id` bigint DEFAULT NULL,
    "user_type" tinyint NOT NULL,
    `username` varchar(50) NOT NULL DEFAULT '',
    `user_ip` varchar(50) DEFAULT NULL,
    `user_agent` varchar(512) DEFAULT NULL,
    `session_timeout` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '' ,
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" bit NOT NULL DEFAULT FALSE,
    PRIMARY KEY (`id`)
) COMMENT '用户在线 Session';

