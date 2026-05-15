SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


-- ----------------------------
-- Table structure for iyque_y_que_complain
-- ----------------------------
DROP TABLE IF EXISTS `iyque_y_que_complain`;
CREATE TABLE `iyque_y_que_complain` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `complain_user_phone` varchar(255) DEFAULT NULL COMMENT 'complain_user_phone',
  `complain_user_content` text DEFAULT NULL COMMENT 'complain_user_content',
  `complain_type` int DEFAULT NULL COMMENT 'complain_type'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_y_que_complaint_tip
-- ----------------------------
DROP TABLE IF EXISTS `iyque_y_que_complaint_tip`;
CREATE TABLE `iyque_y_que_complaint_tip` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `user_id` varchar(255) DEFAULT NULL COMMENT 'user_id'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_customer_info
-- ----------------------------
DROP TABLE IF EXISTS `iyque_customer_info`;
CREATE TABLE `iyque_customer_info` (
  `e_id` varchar(255) NOT NULL COMMENT '主键（externalUserid&userId）',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `customer_name` varchar(255) DEFAULT NULL COMMENT '客户名称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '客户头像',
  `type` int DEFAULT NULL COMMENT '客户类型（1=微信用户，2=企微用户）',
  `external_userid` varchar(255) DEFAULT NULL COMMENT '客户id',
  `add_way` varchar(255) DEFAULT NULL COMMENT '添加方式',
  `user_id` varchar(255) DEFAULT NULL COMMENT '添加人id',
  `tag_ids` varchar(255) DEFAULT NULL COMMENT '客户标签id',
  `state` varchar(255) DEFAULT NULL COMMENT '渠道标识',
  `add_time` datetime DEFAULT NULL COMMENT '添加时间',
  `status` int DEFAULT NULL COMMENT '状态（0正常，1流失，2员工删除）',
  PRIMARY KEY (`e_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_y_que_script
-- ----------------------------
DROP TABLE IF EXISTS `iyque_y_que_script`;
CREATE TABLE `iyque_y_que_script` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `title` varchar(255) DEFAULT NULL COMMENT 'title',
  `category_id` varchar(255) DEFAULT NULL COMMENT 'category_id',
  `update_by` varchar(255) DEFAULT NULL COMMENT 'update_by'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_y_que_script_sub
-- ----------------------------
DROP TABLE IF EXISTS `iyque_y_que_script_sub`;
CREATE TABLE `iyque_y_que_script_sub` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `script_id` bigint DEFAULT NULL COMMENT 'script_id',
  `msgtype` text DEFAULT NULL COMMENT 'msgtype',
  `del_flag` int DEFAULT NULL COMMENT 'del_flag',
  `annex_content` text DEFAULT NULL COMMENT 'annex_content'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_agent
-- ----------------------------
DROP TABLE IF EXISTS `iyque_agent`;
CREATE TABLE `iyque_agent` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `agent_id` int DEFAULT NULL COMMENT 'agent_id',
  `secret` varchar(255) DEFAULT NULL COMMENT 'secret',
  `name` varchar(255) DEFAULT NULL COMMENT 'name',
  `logo_url` varchar(255) DEFAULT NULL COMMENT 'logo_url',
  `allow_party_name` varchar(255) DEFAULT NULL COMMENT 'allow_party_name',
  `allow_userinfo_name` varchar(255) DEFAULT NULL COMMENT 'allow_userinfo_name',
  `update_by` varchar(255) DEFAULT NULL COMMENT 'update_by',
  `del_flag` int DEFAULT NULL COMMENT 'del_flag'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_agent_sub
-- ----------------------------
DROP TABLE IF EXISTS `iyque_agent_sub`;
CREATE TABLE `iyque_agent_sub` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `agent_id` bigint DEFAULT NULL COMMENT 'agent_id',
  `msg_title` text DEFAULT NULL COMMENT 'msg_title',
  `msg_type` text DEFAULT NULL COMMENT 'msg_type',
  `scope_type` int DEFAULT NULL COMMENT 'scope_type',
  `del_flag` int DEFAULT NULL COMMENT 'del_flag',
  `to_user_ids` varchar(255) DEFAULT NULL COMMENT 'to_user_ids',
  `status` int DEFAULT NULL COMMENT 'status',
  `msg_id` text DEFAULT NULL COMMENT 'msg_id',
  `send_time` datetime DEFAULT NULL COMMENT 'send_time',
  `annex_content` text DEFAULT NULL COMMENT 'annex_content'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_ai_analysis_msg_audit
-- ----------------------------
DROP TABLE IF EXISTS `iyque_ai_analysis_msg_audit`;
CREATE TABLE `iyque_ai_analysis_msg_audit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `warning` tinyint(1) DEFAULT NULL COMMENT 'warning',
  `employee_name` varchar(255) DEFAULT NULL COMMENT 'employee_name',
  `employee_id` varchar(255) DEFAULT NULL COMMENT 'employee_id',
  `customer_name` varchar(255) DEFAULT NULL COMMENT 'customer_name',
  `customer_id` varchar(255) DEFAULT NULL COMMENT 'customer_id',
  `msg_audit_type` int DEFAULT NULL COMMENT 'msg_audit_type',
  `msg` text DEFAULT NULL COMMENT 'msg',
  `start_time` datetime DEFAULT NULL COMMENT 'start_time',
  `end_time` datetime DEFAULT NULL COMMENT 'end_time'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_ai_token_record
-- ----------------------------
DROP TABLE IF EXISTS `iyque_ai_token_record`;
CREATE TABLE `iyque_ai_token_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `model` varchar(255) DEFAULT NULL COMMENT 'model',
  `ai_res_id` varchar(255) DEFAULT NULL COMMENT 'ai_res_id',
  `prompt_tokens` bigint DEFAULT NULL COMMENT 'prompt_tokens',
  `completion_tokens` bigint DEFAULT NULL COMMENT 'completion_tokens',
  `total_tokens` bigint DEFAULT NULL COMMENT 'total_tokens'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_analysis_hot_word
-- ----------------------------
DROP TABLE IF EXISTS `iyque_analysis_hot_word`;
CREATE TABLE `iyque_analysis_hot_word` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `hot_word_id` bigint DEFAULT NULL COMMENT 'hot_word_id',
  `hot_word_name` varchar(255) DEFAULT NULL COMMENT 'hot_word_name',
  `category_id` bigint DEFAULT NULL COMMENT 'category_id',
  `category_name` varchar(255) DEFAULT NULL COMMENT 'category_name',
  `msg_id` text DEFAULT NULL COMMENT 'msg_id',
  `from_id` varchar(255) DEFAULT NULL COMMENT 'from_id',
  `from_name` varchar(255) DEFAULT NULL COMMENT 'from_name',
  `accept_id` varchar(255) DEFAULT NULL COMMENT 'accept_id',
  `accept_type` int DEFAULT NULL COMMENT 'accept_type',
  `accept_name` varchar(255) DEFAULT NULL COMMENT 'accept_name',
  `content` text DEFAULT NULL COMMENT 'content'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_annex_period
-- ----------------------------
DROP TABLE IF EXISTS `iyque_annex_period`;
CREATE TABLE `iyque_annex_period` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `msg_id` bigint DEFAULT NULL COMMENT 'msg_id',
  `work_cycle` varchar(255) DEFAULT NULL COMMENT 'work_cycle',
  `begin_time` varchar(255) DEFAULT NULL COMMENT 'begin_time',
  `end_time` varchar(255) DEFAULT NULL COMMENT 'end_time',
  `weclome_msg` text DEFAULT NULL COMMENT 'weclome_msg'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_category
-- ----------------------------
DROP TABLE IF EXISTS `iyque_category`;
CREATE TABLE `iyque_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `name` varchar(255) DEFAULT NULL COMMENT 'name',
  `del_flag` int DEFAULT NULL COMMENT 'del_flag'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_chat
-- ----------------------------
DROP TABLE IF EXISTS `iyque_chat`;
CREATE TABLE `iyque_chat` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `chat_id` varchar(255) DEFAULT NULL COMMENT 'chat_id',
  `chat_name` varchar(255) DEFAULT NULL COMMENT 'chat_name',
  `owner` varchar(255) DEFAULT NULL COMMENT 'owner',
  `tag_ids` varchar(255) DEFAULT NULL COMMENT 'tag_ids'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_chat_code
-- ----------------------------
DROP TABLE IF EXISTS `iyque_chat_code`;
CREATE TABLE `iyque_chat_code` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `chat_code_name` varchar(255) DEFAULT NULL COMMENT 'chat_code_name',
  `chat_code_url` varchar(255) DEFAULT NULL COMMENT 'chat_code_url',
  `chat_code_state` varchar(255) DEFAULT NULL COMMENT 'chat_code_state',
  `config_id` varchar(255) DEFAULT NULL COMMENT 'config_id',
  `remark` text DEFAULT NULL COMMENT 'remark',
  `auto_create_room` int DEFAULT NULL COMMENT 'auto_create_room',
  `room_base_name` varchar(255) DEFAULT NULL COMMENT 'room_base_name',
  `room_base_id` int DEFAULT NULL COMMENT 'room_base_id',
  `yque_chat_list_json` text DEFAULT NULL COMMENT 'yque_chat_list_json'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_complain_annex
-- ----------------------------
DROP TABLE IF EXISTS `iyque_complain_annex`;
CREATE TABLE `iyque_complain_annex` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `complain_id` bigint DEFAULT NULL COMMENT 'complain_id',
  `annex_type` int DEFAULT NULL COMMENT 'annex_type',
  `annex_url` text DEFAULT NULL COMMENT 'annex_url'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_config
-- ----------------------------
DROP TABLE IF EXISTS `iyque_config`;
CREATE TABLE `iyque_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `corp_id` varchar(255) DEFAULT NULL COMMENT 'corp_id',
  `agent_id` varchar(255) DEFAULT NULL COMMENT 'agent_id',
  `agent_secert` varchar(255) DEFAULT NULL COMMENT 'agent_secert',
  `token` varchar(255) DEFAULT NULL COMMENT 'token',
  `encoding_aes_key` varchar(255) DEFAULT NULL COMMENT 'encoding_aes_key',
  `msg_audit_lib_path` text DEFAULT NULL COMMENT 'msg_audit_lib_path',
  `msg_audit_pri_key` text DEFAULT NULL COMMENT 'msg_audit_pri_key',
  `msg_audit_secret` text DEFAULT NULL COMMENT 'msg_audit_secret'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_customer_seas
-- ----------------------------
DROP TABLE IF EXISTS `iyque_customer_seas`;
CREATE TABLE `iyque_customer_seas` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `phone_number` varchar(255) DEFAULT NULL COMMENT 'phone_number',
  `customer_name` varchar(255) DEFAULT NULL COMMENT 'customer_name',
  `allocate_user_id` varchar(255) DEFAULT NULL COMMENT 'allocate_user_id',
  `allocate_user_name` varchar(255) DEFAULT NULL COMMENT 'allocate_user_name',
  `current_state` int DEFAULT NULL COMMENT 'current_state',
  `del_flag` int DEFAULT NULL COMMENT 'del_flag'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_default_msg
-- ----------------------------
DROP TABLE IF EXISTS `iyque_default_msg`;
CREATE TABLE `iyque_default_msg` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `start_period_annex` tinyint(1) DEFAULT NULL COMMENT 'start_period_annex'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_dept
-- ----------------------------
DROP TABLE IF EXISTS `iyque_dept`;
CREATE TABLE `iyque_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `dept_we_id` varchar(255) DEFAULT NULL COMMENT 'dept_we_id',
  `dept_name` varchar(255) DEFAULT NULL COMMENT 'dept_name'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_file_security
-- ----------------------------
DROP TABLE IF EXISTS `iyque_file_security`;
CREATE TABLE `iyque_file_security` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `operate_time` datetime DEFAULT NULL COMMENT 'operate_time',
  `user_name` varchar(255) DEFAULT NULL COMMENT 'user_name',
  `user_type` int DEFAULT NULL COMMENT 'user_type',
  `operate_type` int DEFAULT NULL COMMENT 'operate_type',
  `opreate_source` int DEFAULT NULL COMMENT 'opreate_source',
  `operate_file_info` varchar(255) DEFAULT NULL COMMENT 'operate_file_info'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_friend_circle
-- ----------------------------
DROP TABLE IF EXISTS `iyque_friend_circle`;
CREATE TABLE `iyque_friend_circle` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `name` varchar(255) DEFAULT NULL COMMENT 'name',
  `content` text DEFAULT NULL COMMENT 'content',
  `job_id` varchar(255) DEFAULT NULL COMMENT 'job_id',
  `moment_id` varchar(255) DEFAULT NULL COMMENT 'moment_id',
  `del_flag` int DEFAULT NULL COMMENT 'del_flag'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_group_msg
-- ----------------------------
DROP TABLE IF EXISTS `iyque_group_msg`;
CREATE TABLE `iyque_group_msg` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `group_msg_name` text DEFAULT NULL COMMENT 'group_msg_name',
  `chat_type` varchar(255) DEFAULT NULL COMMENT 'chat_type',
  `scope_type` int DEFAULT NULL COMMENT 'scope_type',
  `send_type` int DEFAULT NULL COMMENT 'send_type',
  `content` text DEFAULT NULL COMMENT 'content',
  `send_time` datetime DEFAULT NULL COMMENT 'send_time'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_group_msg_sub
-- ----------------------------
DROP TABLE IF EXISTS `iyque_group_msg_sub`;
CREATE TABLE `iyque_group_msg_sub` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `group_msg_id` bigint DEFAULT NULL COMMENT 'group_msg_id',
  `accept_type` int DEFAULT NULL COMMENT 'accept_type',
  `accept_id` varchar(255) DEFAULT NULL COMMENT 'accept_id',
  `accept_name` varchar(255) DEFAULT NULL COMMENT 'accept_name',
  `sender_id` varchar(255) DEFAULT NULL COMMENT 'sender_id',
  `msg_id` text DEFAULT NULL COMMENT 'msg_id',
  `status` int DEFAULT NULL COMMENT 'status',
  `status_sub` int DEFAULT NULL COMMENT 'status_sub',
  `send_time` datetime DEFAULT NULL COMMENT 'send_time'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_h5_market
-- ----------------------------
DROP TABLE IF EXISTS `iyque_h5_market`;
CREATE TABLE `iyque_h5_market` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `name` varchar(255) DEFAULT NULL COMMENT 'name',
  `type` int DEFAULT NULL COMMENT 'type',
  `control_type` int DEFAULT NULL COMMENT 'control_type',
  `control_sub_type` int DEFAULT NULL COMMENT 'control_sub_type',
  `control_url` varchar(255) DEFAULT NULL COMMENT 'control_url',
  `background_url` varchar(255) DEFAULT NULL COMMENT 'background_url',
  `title` varchar(255) DEFAULT NULL COMMENT 'title',
  `title_sub` varchar(255) DEFAULT NULL COMMENT 'title_sub',
  `guide_tip` varchar(255) DEFAULT NULL COMMENT 'guide_tip',
  `h5_url` varchar(255) DEFAULT NULL COMMENT 'h5_url',
  `h5_qr_url` varchar(255) DEFAULT NULL COMMENT 'h5_qr_url',
  `create_by` varchar(255) DEFAULT NULL COMMENT 'create_by',
  `del_flag` int DEFAULT NULL COMMENT 'del_flag'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_h5_market_record
-- ----------------------------
DROP TABLE IF EXISTS `iyque_h5_market_record`;
CREATE TABLE `iyque_h5_market_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `h5_market_id` bigint DEFAULT NULL COMMENT 'h5_market_id',
  `view_ip` varchar(255) DEFAULT NULL COMMENT 'view_ip',
  `del_flag` int DEFAULT NULL COMMENT 'del_flag'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_hot_word
-- ----------------------------
DROP TABLE IF EXISTS `iyque_hot_word`;
CREATE TABLE `iyque_hot_word` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `category_id` bigint DEFAULT NULL COMMENT 'category_id',
  `hot_word` varchar(255) DEFAULT NULL COMMENT 'hot_word',
  `update_by` varchar(255) DEFAULT NULL COMMENT 'update_by',
  `near_hot_word` varchar(255) DEFAULT NULL COMMENT 'near_hot_word',
  `del_flag` int DEFAULT NULL COMMENT 'del_flag'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_kf
-- ----------------------------
DROP TABLE IF EXISTS `iyque_kf`;
CREATE TABLE `iyque_kf` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `open_kfid` varchar(255) DEFAULT NULL COMMENT 'open_kfid',
  `kf_name` varchar(255) DEFAULT NULL COMMENT 'kf_name',
  `kf_url` varchar(255) DEFAULT NULL COMMENT 'kf_url',
  `kf_qr_url` varchar(255) DEFAULT NULL COMMENT 'kf_qr_url',
  `kf_pic_url` varchar(255) DEFAULT NULL COMMENT 'kf_pic_url',
  `kid` varchar(255) DEFAULT NULL COMMENT 'kid',
  `kname` varchar(255) DEFAULT NULL COMMENT 'kname',
  `switch_type` int DEFAULT NULL COMMENT 'switch_type',
  `switch_text` text DEFAULT NULL COMMENT 'switch_text',
  `switch_user_ids` varchar(255) DEFAULT NULL COMMENT 'switch_user_ids',
  `switch_user_names` varchar(255) DEFAULT NULL COMMENT 'switch_user_names',
  `oor_welcome` varchar(255) DEFAULT NULL COMMENT 'oor_welcome',
  `work_cycle` varchar(255) DEFAULT NULL COMMENT 'work_cycle',
  `begin_time` varchar(255) DEFAULT NULL COMMENT 'begin_time',
  `end_time` varchar(255) DEFAULT NULL COMMENT 'end_time',
  `welcome_msg` text DEFAULT NULL COMMENT 'welcome_msg',
  `swich_qr_url` varchar(255) DEFAULT NULL COMMENT 'swich_qr_url'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_kf_msg
-- ----------------------------
DROP TABLE IF EXISTS `iyque_kf_msg`;
CREATE TABLE `iyque_kf_msg` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `message_cursor` varchar(255) DEFAULT NULL COMMENT 'message_cursor',
  `open_kfid` varchar(255) DEFAULT NULL COMMENT 'open_kfid',
  `pull_time` datetime DEFAULT NULL COMMENT 'pull_time'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_kf_msg_sub
-- ----------------------------
DROP TABLE IF EXISTS `iyque_kf_msg_sub`;
CREATE TABLE `iyque_kf_msg_sub` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `kf_msg_id` bigint DEFAULT NULL COMMENT 'kf_msg_id',
  `open_kfid` varchar(255) DEFAULT NULL COMMENT 'open_kfid',
  `kf_name` varchar(255) DEFAULT NULL COMMENT 'kf_name',
  `kf_pic_url` varchar(255) DEFAULT NULL COMMENT 'kf_pic_url',
  `external_user_id` varchar(255) DEFAULT NULL COMMENT 'external_user_id',
  `switch_user_id` varchar(255) DEFAULT NULL COMMENT 'switch_user_id',
  `switch_user_name` varchar(255) DEFAULT NULL COMMENT 'switch_user_name',
  `origin` int DEFAULT NULL COMMENT 'origin',
  `msg_id` text DEFAULT NULL COMMENT 'msg_id',
  `nickname` varchar(255) DEFAULT NULL COMMENT 'nickname',
  `avatar` varchar(255) DEFAULT NULL COMMENT 'avatar',
  `unionid` varchar(255) DEFAULT NULL COMMENT 'unionid',
  `gender` int DEFAULT NULL COMMENT 'gender',
  `msg_type` text DEFAULT NULL COMMENT 'msg_type',
  `content` text DEFAULT NULL COMMENT 'content',
  `send_time` datetime DEFAULT NULL COMMENT 'send_time'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_knowledge_attach
-- ----------------------------
DROP TABLE IF EXISTS `iyque_knowledge_attach`;
CREATE TABLE `iyque_knowledge_attach` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `kid` bigint DEFAULT NULL COMMENT 'kid',
  `doc_name` varchar(255) DEFAULT NULL COMMENT 'doc_name',
  `doc_type` varchar(255) DEFAULT NULL COMMENT 'doc_type',
  `del_flag` int DEFAULT NULL COMMENT 'del_flag'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_knowledge_fragment
-- ----------------------------
DROP TABLE IF EXISTS `iyque_knowledge_fragment`;
CREATE TABLE `iyque_knowledge_fragment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `kid` bigint DEFAULT NULL COMMENT 'kid',
  `doc_id` bigint DEFAULT NULL COMMENT 'doc_id',
  `idx` int DEFAULT NULL COMMENT 'idx',
  `content` text DEFAULT NULL COMMENT 'content'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_knowledge_info
-- ----------------------------
DROP TABLE IF EXISTS `iyque_knowledge_info`;
CREATE TABLE `iyque_knowledge_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `kname` varchar(255) DEFAULT NULL COMMENT 'kname',
  `description` text DEFAULT NULL COMMENT 'description',
  `del_flag` int DEFAULT NULL COMMENT 'del_flag'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_material
-- ----------------------------
DROP TABLE IF EXISTS `iyque_material`;
CREATE TABLE `iyque_material` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `msgtype` text DEFAULT NULL COMMENT 'msgtype',
  `title` varchar(255) DEFAULT NULL COMMENT 'title',
  `category_id` varchar(255) DEFAULT NULL COMMENT 'category_id',
  `update_by` varchar(255) DEFAULT NULL COMMENT 'update_by',
  `del_flag` int DEFAULT NULL COMMENT 'del_flag',
  `annex_content` text DEFAULT NULL COMMENT 'annex_content'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_msg_annex
-- ----------------------------
DROP TABLE IF EXISTS `iyque_msg_annex`;
CREATE TABLE `iyque_msg_annex` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `msg_id` bigint DEFAULT NULL COMMENT 'msg_id',
  `msgtype` text DEFAULT NULL COMMENT 'msgtype',
  `annex_content` text DEFAULT NULL COMMENT 'annex_content'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_msg_audit
-- ----------------------------
DROP TABLE IF EXISTS `iyque_msg_audit`;
CREATE TABLE `iyque_msg_audit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `msg_id` text DEFAULT NULL COMMENT 'msg_id',
  `from_id` varchar(255) DEFAULT NULL COMMENT 'from_id',
  `from_name` varchar(255) DEFAULT NULL COMMENT 'from_name',
  `accept_id` varchar(255) DEFAULT NULL COMMENT 'accept_id',
  `accept_type` int DEFAULT NULL COMMENT 'accept_type',
  `accept_name` varchar(255) DEFAULT NULL COMMENT 'accept_name',
  `msg_type` text DEFAULT NULL COMMENT 'msg_type',
  `content` text DEFAULT NULL COMMENT 'content',
  `data_seq` bigint DEFAULT NULL COMMENT 'data_seq',
  `msg_time` datetime DEFAULT NULL COMMENT 'msg_time'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_msg_rule
-- ----------------------------
DROP TABLE IF EXISTS `iyque_msg_rule`;
CREATE TABLE `iyque_msg_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `rule_content` text DEFAULT NULL COMMENT 'rule_content',
  `rule_status` tinyint(1) DEFAULT NULL COMMENT 'rule_status',
  `default_rule` tinyint(1) DEFAULT NULL COMMENT 'default_rule'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_period_msg_annex
-- ----------------------------
DROP TABLE IF EXISTS `iyque_period_msg_annex`;
CREATE TABLE `iyque_period_msg_annex` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `annex_peroid_id` bigint DEFAULT NULL COMMENT 'annex_peroid_id',
  `msgtype` text DEFAULT NULL COMMENT 'msgtype',
  `annex_content` text DEFAULT NULL COMMENT 'annex_content'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_robot
-- ----------------------------
DROP TABLE IF EXISTS `iyque_robot`;
CREATE TABLE `iyque_robot` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `title` varchar(255) DEFAULT NULL COMMENT 'title',
  `web_hook_url` varchar(255) DEFAULT NULL COMMENT 'web_hook_url',
  `update_by` varchar(255) DEFAULT NULL COMMENT 'update_by',
  `del_flag` int DEFAULT NULL COMMENT 'del_flag'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_robot_sub
-- ----------------------------
DROP TABLE IF EXISTS `iyque_robot_sub`;
CREATE TABLE `iyque_robot_sub` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `robot_id` bigint DEFAULT NULL COMMENT 'robot_id',
  `msg_title` text DEFAULT NULL COMMENT 'msg_title',
  `msg_type` text DEFAULT NULL COMMENT 'msg_type',
  `del_flag` int DEFAULT NULL COMMENT 'del_flag',
  `send_time` datetime DEFAULT NULL COMMENT 'send_time',
  `annex_content` text DEFAULT NULL COMMENT 'annex_content'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_screen_short
-- ----------------------------
DROP TABLE IF EXISTS `iyque_screen_short`;
CREATE TABLE `iyque_screen_short` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `operate_time` datetime DEFAULT NULL COMMENT 'operate_time',
  `user_name` varchar(255) DEFAULT NULL COMMENT 'user_name',
  `user_id` varchar(255) DEFAULT NULL COMMENT 'user_id',
  `depet_id` bigint DEFAULT NULL COMMENT 'depet_id',
  `shot_type` int DEFAULT NULL COMMENT 'shot_type',
  `shot_content` text DEFAULT NULL COMMENT 'shot_content',
  `system_os` varchar(255) DEFAULT NULL COMMENT 'system_os'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_sessionntercept_rule
-- ----------------------------
DROP TABLE IF EXISTS `iyque_sessionntercept_rule`;
CREATE TABLE `iyque_sessionntercept_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `rule_name` varchar(255) DEFAULT NULL COMMENT 'rule_name',
  `sensitive_words` varchar(255) DEFAULT NULL COMMENT 'sensitive_words',
  `staff_ids` varchar(255) DEFAULT NULL COMMENT 'staff_ids',
  `intercept_type` int DEFAULT NULL COMMENT 'intercept_type',
  `semantics` varchar(255) DEFAULT NULL COMMENT 'semantics',
  `rule_id` varchar(255) DEFAULT NULL COMMENT 'rule_id',
  `del_flag` int DEFAULT NULL COMMENT 'del_flag'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_short_link
-- ----------------------------
DROP TABLE IF EXISTS `iyque_short_link`;
CREATE TABLE `iyque_short_link` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `code_name` varchar(255) DEFAULT NULL COMMENT 'code_name',
  `user_id` varchar(255) DEFAULT NULL COMMENT 'user_id',
  `user_name` varchar(255) DEFAULT NULL COMMENT 'user_name',
  `skip_verify` tinyint(1) DEFAULT NULL COMMENT 'skip_verify',
  `is_exclusive` tinyint(1) DEFAULT NULL COMMENT 'is_exclusive',
  `tag_id` varchar(255) DEFAULT NULL COMMENT 'tag_id',
  `tag_name` varchar(255) DEFAULT NULL COMMENT 'tag_name',
  `weclome_msg` text DEFAULT NULL COMMENT 'weclome_msg',
  `code_state` varchar(255) DEFAULT NULL COMMENT 'code_state',
  `code_url` varchar(255) DEFAULT NULL COMMENT 'code_url',
  `config_id` varchar(255) DEFAULT NULL COMMENT 'config_id',
  `start_period_annex` tinyint(1) DEFAULT NULL COMMENT 'start_period_annex',
  `remark_type` int DEFAULT NULL COMMENT 'remark_type'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_summary_kf_msg
-- ----------------------------
DROP TABLE IF EXISTS `iyque_summary_kf_msg`;
CREATE TABLE `iyque_summary_kf_msg` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `nickname` varchar(255) DEFAULT NULL COMMENT 'nickname',
  `avatar` varchar(255) DEFAULT NULL COMMENT 'avatar',
  `external_user_id` varchar(255) DEFAULT NULL COMMENT 'external_user_id',
  `unionid` varchar(255) DEFAULT NULL COMMENT 'unionid',
  `start_time` datetime DEFAULT NULL COMMENT 'start_time',
  `end_time` datetime DEFAULT NULL COMMENT 'end_time',
  `summary_content` text DEFAULT NULL COMMENT 'summary_content'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_synch_data_record
-- ----------------------------
DROP TABLE IF EXISTS `iyque_synch_data_record`;
CREATE TABLE `iyque_synch_data_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `synch_data_type` int DEFAULT NULL COMMENT 'synch_data_type',
  `next_cursor` varchar(255) DEFAULT NULL COMMENT 'next_cursor'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_tag
-- ----------------------------
DROP TABLE IF EXISTS `iyque_tag`;
CREATE TABLE `iyque_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tag_id` varchar(255) DEFAULT NULL COMMENT 'tag_id',
  `group_id` varchar(255) DEFAULT NULL COMMENT 'group_id',
  `name` varchar(255) DEFAULT NULL COMMENT 'name',
  `tag_type` int DEFAULT NULL COMMENT 'tag_type',
  `order_number` bigint DEFAULT NULL COMMENT 'order_number',
  `del_flag` int DEFAULT NULL COMMENT 'del_flag'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_tag_group
-- ----------------------------
DROP TABLE IF EXISTS `iyque_tag_group`;
CREATE TABLE `iyque_tag_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `group_id` varchar(255) DEFAULT NULL COMMENT 'group_id',
  `group_name` varchar(255) DEFAULT NULL COMMENT 'group_name',
  `order_number` bigint DEFAULT NULL COMMENT 'order_number',
  `group_tag_type` int DEFAULT NULL COMMENT 'group_tag_type',
  `del_flag` int DEFAULT NULL COMMENT 'del_flag'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_user
-- ----------------------------
DROP TABLE IF EXISTS `iyque_user`;
CREATE TABLE `iyque_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `name` varchar(255) DEFAULT NULL COMMENT 'name',
  `user_id` varchar(255) DEFAULT NULL COMMENT 'user_id',
  `position` varchar(255) DEFAULT NULL COMMENT 'position',
  `status` int DEFAULT NULL COMMENT 'status'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_user_code
-- ----------------------------
DROP TABLE IF EXISTS `iyque_user_code`;
CREATE TABLE `iyque_user_code` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `code_name` varchar(255) DEFAULT NULL COMMENT 'code_name',
  `user_id` varchar(255) DEFAULT NULL COMMENT 'user_id',
  `user_name` varchar(255) DEFAULT NULL COMMENT 'user_name',
  `skip_verify` tinyint(1) DEFAULT NULL COMMENT 'skip_verify',
  `is_exclusive` tinyint(1) DEFAULT NULL COMMENT 'is_exclusive',
  `tag_id` varchar(255) DEFAULT NULL COMMENT 'tag_id',
  `tag_name` varchar(255) DEFAULT NULL COMMENT 'tag_name',
  `weclome_msg` text DEFAULT NULL COMMENT 'weclome_msg',
  `code_state` varchar(255) DEFAULT NULL COMMENT 'code_state',
  `code_url` varchar(255) DEFAULT NULL COMMENT 'code_url',
  `config_id` varchar(255) DEFAULT NULL COMMENT 'config_id',
  `logo_url` varchar(255) DEFAULT NULL COMMENT 'logo_url',
  `start_period_annex` tinyint(1) DEFAULT NULL COMMENT 'start_period_annex',
  `backup_qr_url` varchar(255) DEFAULT NULL COMMENT 'backup_qr_url',
  `remark_type` int DEFAULT NULL COMMENT 'remark_type'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for iyque_yuqe_operate_log
-- ----------------------------
DROP TABLE IF EXISTS `iyque_yuqe_operate_log`;
CREATE TABLE `iyque_yuqe_operate_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `creator` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `user_id` varchar(255) DEFAULT NULL COMMENT 'user_id',
  `user_name` varchar(255) DEFAULT NULL COMMENT 'user_name',
  `operate_type` int DEFAULT NULL COMMENT 'operate_type',
  `operate_type_sub` int DEFAULT NULL COMMENT 'operate_type_sub',
  `operate_content` text DEFAULT NULL COMMENT 'operate_content',
  `operate_ip` varchar(255) DEFAULT NULL COMMENT 'operate_ip'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;