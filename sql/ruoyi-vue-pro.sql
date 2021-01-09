/*
 Navicat Premium Data Transfer

 Source Server         : local-mysql001
 Source Server Type    : MySQL
 Source Server Version : 50718
 Source Host           : localhost:33061
 Source Schema         : ruoyi-vue-pro

 Target Server Type    : MySQL
 Target Server Version : 50718
 File Encoding         : 65001

 Date: 10/01/2021 00:08:59
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table` (
  `table_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) DEFAULT '' COMMENT '表描述',
  `class_name` varchar(100) DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `package_name` varchar(100) DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) DEFAULT NULL COMMENT '生成功能作者',
  `gen_type` char(1) DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码生成业务表';

-- ----------------------------
-- Records of gen_table
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column` (
  `column_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` varchar(64) DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) DEFAULT '' COMMENT '字典类型',
  `sort` int(11) DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码生成业务表字段';

-- ----------------------------
-- Records of gen_table_column
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `config_id` int(5) NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='参数配置表';

-- ----------------------------
-- Records of sys_config
-- ----------------------------
BEGIN;
INSERT INTO `sys_config` VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2021-01-05 17:03:48', '', NULL, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow');
INSERT INTO `sys_config` VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2021-01-05 17:03:48', '', NULL, '初始化密码 123456');
INSERT INTO `sys_config` VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', '2021-01-05 17:03:48', '', NULL, '深色主题theme-dark，浅色主题theme-light');
COMMIT;

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `name` varchar(30) NOT NULL DEFAULT '' COMMENT '部门名称',
  `parent_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '父部门id',
  `ancestors` varchar(50) NOT NULL DEFAULT '' COMMENT '祖级列表',
  `sort` int(4) NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `leader` varchar(20) DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `status` tinyint(4) NOT NULL COMMENT '部门状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=200 DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
BEGIN;
INSERT INTO `sys_dept` VALUES (100, '芋道源码', 0, '0', 0, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-06 19:45:52', b'0');
INSERT INTO `sys_dept` VALUES (101, '深圳总公司', 100, '0,100', 1, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept` VALUES (102, '长沙分公司', 100, '0,100', 2, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept` VALUES (103, '研发部门', 101, '0,100,101', 1, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept` VALUES (104, '市场部门', 101, '0,100,101', 2, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept` VALUES (105, '测试部门', 101, '0,100,101', 3, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept` VALUES (106, '财务部门', 101, '0,100,101', 4, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept` VALUES (107, '运维部门', 101, '0,100,101', 5, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept` VALUES (108, '市场部门', 102, '0,100,102', 1, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept` VALUES (109, '财务部门', 102, '0,100,102', 2, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
COMMIT;

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `sort` int(4) NOT NULL DEFAULT '0' COMMENT '字典排序',
  `label` varchar(100) NOT NULL DEFAULT '' COMMENT '字典标签',
  `value` varchar(100) NOT NULL DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) NOT NULL DEFAULT '' COMMENT '字典类型',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
BEGIN;
INSERT INTO `sys_dict_data` VALUES (1, 1, '男', '1', 'sys_user_sex', 0, '性别男', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 05:48:53', b'0');
INSERT INTO `sys_dict_data` VALUES (2, 2, '女', '2', 'sys_user_sex', 0, '性别女', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 05:48:55', b'0');
INSERT INTO `sys_dict_data` VALUES (8, 1, '正常', '0', 'sys_job_status', 0, '正常状态', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (9, 2, '暂停', '1', 'sys_job_status', 0, '停用状态', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', 0, '默认分组', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', 0, '系统分组', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (12, 1, '是', 'Y', 'sys_yes_no', 0, '系统默认是', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (13, 2, '否', 'N', 'sys_yes_no', 0, '系统默认否', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (14, 1, '通知', '1', 'sys_notice_type', 0, '通知', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (15, 2, '公告', '2', 'sys_notice_type', 0, '公告', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (16, 1, '正常', '0', 'sys_notice_status', 0, '正常状态', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (17, 2, '关闭', '1', 'sys_notice_status', 0, '关闭状态', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (18, 1, '新增', '1', 'sys_oper_type', 0, '新增操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (19, 2, '修改', '2', 'sys_oper_type', 0, '修改操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (20, 3, '删除', '3', 'sys_oper_type', 0, '删除操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (21, 4, '授权', '4', 'sys_oper_type', 0, '授权操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (22, 5, '导出', '5', 'sys_oper_type', 0, '导出操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (23, 6, '导入', '6', 'sys_oper_type', 0, '导入操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (24, 7, '强退', '7', 'sys_oper_type', 0, '强退操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (25, 8, '生成代码', '8', 'sys_oper_type', 0, '生成操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (26, 9, '清空数据', '9', 'sys_oper_type', 0, '清空操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (27, 1, '开启', '0', 'sys_common_status', 0, '开启状态', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 02:57:12', b'0');
INSERT INTO `sys_dict_data` VALUES (28, 2, '关闭', '1', 'sys_common_status', 0, '关闭状态', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 05:48:32', b'0');
INSERT INTO `sys_dict_data` VALUES (29, 1, '目录', '1', 'sys_menu_type', 0, '目录', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 13:33:30', b'0');
INSERT INTO `sys_dict_data` VALUES (30, 2, '菜单', '2', 'sys_menu_type', 0, '菜单', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 13:33:35', b'0');
INSERT INTO `sys_dict_data` VALUES (31, 3, '按钮', '3', 'sys_menu_type', 0, '按钮', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 13:33:38', b'0');
INSERT INTO `sys_dict_data` VALUES (32, 1, '内置', '1', 'sys_role_type', 0, '内置角色', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 13:34:22', b'0');
INSERT INTO `sys_dict_data` VALUES (33, 2, '自定义', '2', 'sys_role_type', 0, '自定义角色', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 13:34:26', b'0');
INSERT INTO `sys_dict_data` VALUES (34, 1, '全部数据权限', '1', 'sys_data_scope', 0, '全部数据权限', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 19:38:02', b'0');
INSERT INTO `sys_dict_data` VALUES (35, 2, '指定部门数据权限', '2', 'sys_data_scope', 0, '指定部门数据权限', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 19:38:20', b'0');
INSERT INTO `sys_dict_data` VALUES (36, 3, '本部门数据权限', '3', 'sys_data_scope', 0, '本部门数据权限', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 19:38:29', b'0');
INSERT INTO `sys_dict_data` VALUES (37, 4, '本部门及以下数据权限', '4', 'sys_data_scope', 0, '本部门及以下数据权限', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 19:38:32', b'0');
INSERT INTO `sys_dict_data` VALUES (38, 5, '仅本人数据权限', '5', 'sys_data_scope', 0, '仅本人数据权限', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 19:38:38', b'0');
COMMIT;

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `name` varchar(100) DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) NOT NULL DEFAULT '' COMMENT '字典类型',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `dict_type` (`dict_type`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
BEGIN;
INSERT INTO `sys_dict_type` VALUES (1, '用户性别', 'sys_user_sex', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type` VALUES (2, '菜单状态', 'sys_show_hide', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type` VALUES (3, '系统开关', 'sys_normal_disable', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type` VALUES (4, '任务状态', 'sys_job_status', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type` VALUES (5, '任务分组', 'sys_job_group', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type` VALUES (6, '系统是否', 'sys_yes_no', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type` VALUES (7, '通知类型', 'sys_notice_type', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type` VALUES (8, '通知状态', 'sys_notice_status', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type` VALUES (9, '操作类型', 'sys_oper_type', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type` VALUES (10, '系统状态', 'sys_common_status', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
COMMIT;

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job` (
  `job_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(64) NOT NULL DEFAULT '' COMMENT '任务名称',
  `job_group` varchar(64) NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target` varchar(500) NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) DEFAULT '' COMMENT 'cron执行表达式',
  `misfire_policy` varchar(20) DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent` char(1) DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (`job_id`,`job_name`,`job_group`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='定时任务调度表';

-- ----------------------------
-- Records of sys_job
-- ----------------------------
BEGIN;
INSERT INTO `sys_job` VALUES (1, '系统默认（无参）', 'DEFAULT', 'ryTask.ryNoParams', '0/10 * * * * ?', '3', '1', '1', 'admin', '2021-01-05 17:03:48', '', NULL, '');
INSERT INTO `sys_job` VALUES (2, '系统默认（有参）', 'DEFAULT', 'ryTask.ryParams(\'ry\')', '0/15 * * * * ?', '3', '1', '1', 'admin', '2021-01-05 17:03:48', '', NULL, '');
INSERT INTO `sys_job` VALUES (3, '系统默认（多参）', 'DEFAULT', 'ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)', '0/20 * * * * ?', '3', '1', '1', 'admin', '2021-01-05 17:03:48', '', NULL, '');
COMMIT;

-- ----------------------------
-- Table structure for sys_job_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log` (
  `job_log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  `job_name` varchar(64) NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(500) NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) DEFAULT NULL COMMENT '日志信息',
  `status` char(1) DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
  `exception_info` varchar(2000) DEFAULT '' COMMENT '异常信息',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`job_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务调度日志表';

-- ----------------------------
-- Records of sys_job_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor` (
  `info_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `user_name` varchar(50) DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(50) DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) DEFAULT '' COMMENT '操作系统',
  `status` char(1) DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) DEFAULT '' COMMENT '提示消息',
  `login_time` datetime DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='系统访问记录';

-- ----------------------------
-- Records of sys_logininfor
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `name` varchar(50) NOT NULL COMMENT '菜单名称',
  `permission` varchar(100) NOT NULL DEFAULT '' COMMENT '权限标识',
  `menu_type` tinyint(4) NOT NULL COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `parent_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '父菜单ID',
  `path` varchar(200) DEFAULT '' COMMENT '路由地址',
  `icon` varchar(100) DEFAULT '#' COMMENT '菜单图标',
  `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1065 DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
BEGIN;
INSERT INTO `sys_menu` VALUES (1, '系统管理', '', 1, 1, 0, '/system', 'system', NULL, 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:34:28', b'0');
INSERT INTO `sys_menu` VALUES (2, '系统监控', '', 1, 2, 0, '/monitor', 'monitor', NULL, 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:34:28', b'0');
INSERT INTO `sys_menu` VALUES (3, '系统工具', '', 1, 3, 0, '/tool', 'tool', NULL, 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:34:28', b'0');
INSERT INTO `sys_menu` VALUES (4, '若依官网', '', 1, 4, 0, 'http://ruoyi.vip', 'guide', NULL, 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:34:28', b'0');
INSERT INTO `sys_menu` VALUES (100, '用户管理', 'system:user:list', 2, 1, 1, 'user', 'user', 'system/user/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (101, '角色管理', 'system:role:list', 2, 2, 1, 'role', 'peoples', 'system/role/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (102, '菜单管理', 'system:menu:list', 2, 3, 1, 'menu', 'tree-table', 'system/menu/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (103, '部门管理', 'system:dept:list', 2, 4, 1, 'dept', 'tree', 'system/dept/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (104, '岗位管理', 'system:post:list', 2, 5, 1, 'post', 'post', 'system/post/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (105, '字典管理', 'system:dict:list', 2, 6, 1, 'dict', 'dict', 'system/dict/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (106, '参数设置', 'system:config:list', 2, 7, 1, 'config', 'edit', 'system/config/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (107, '通知公告', 'system:notice:list', 2, 8, 1, 'notice', 'message', 'system/notice/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (108, '日志管理', '', 1, 9, 1, 'log', 'log', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:34:28', b'0');
INSERT INTO `sys_menu` VALUES (109, '在线用户', 'monitor:online:list', 2, 1, 2, 'online', 'online', 'monitor/online/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (110, '定时任务', 'monitor:job:list', 2, 2, 2, 'job', 'job', 'monitor/job/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (111, '数据监控', 'monitor:druid:list', 2, 3, 2, 'druid', 'druid', 'monitor/druid/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (112, '服务监控', 'monitor:server:list', 2, 4, 2, 'server', 'server', 'monitor/server/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (113, '缓存监控', 'monitor:cache:list', 2, 5, 2, 'cache', 'redis', 'monitor/cache/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (114, '表单构建', 'tool:build:list', 2, 1, 3, 'build', 'build', 'tool/build/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (115, '代码生成', 'tool:gen:list', 2, 2, 3, 'gen', 'code', 'tool/gen/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (116, '系统接口', 'tool:swagger:list', 2, 3, 3, 'swagger', 'swagger', 'tool/swagger/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (500, '操作日志', 'monitor:operlog:list', 2, 1, 108, 'operlog', 'form', 'monitor/operlog/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (501, '登录日志', 'monitor:logininfor:list', 2, 2, 108, 'logininfor', 'logininfor', 'monitor/logininfor/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (1001, '用户查询', 'system:user:query', 3, 1, 100, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1002, '用户新增', 'system:user:add', 3, 2, 100, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1003, '用户修改', 'system:user:edit', 3, 3, 100, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1004, '用户删除', 'system:user:remove', 3, 4, 100, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1005, '用户导出', 'system:user:export', 3, 5, 100, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1006, '用户导入', 'system:user:import', 3, 6, 100, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1007, '重置密码', 'system:user:resetPwd', 3, 7, 100, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1008, '角色查询', 'system:role:query', 3, 1, 101, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1009, '角色新增', 'system:role:add', 3, 2, 101, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1010, '角色修改', 'system:role:edit', 3, 3, 101, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1011, '角色删除', 'system:role:remove', 3, 4, 101, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1012, '角色导出', 'system:role:export', 3, 5, 101, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1013, '菜单查询', 'system:menu:query', 3, 1, 102, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1014, '菜单新增', 'system:menu:add', 3, 2, 102, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1015, '菜单修改', 'system:menu:edit', 3, 3, 102, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1016, '菜单删除', 'system:menu:remove', 3, 4, 102, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1017, '部门查询', 'system:dept:query', 3, 1, 103, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1018, '部门新增', 'system:dept:add', 3, 2, 103, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1019, '部门修改', 'system:dept:edit', 3, 3, 103, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1020, '部门删除', 'system:dept:remove', 3, 4, 103, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1021, '岗位查询', 'system:post:query', 3, 1, 104, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1022, '岗位新增', 'system:post:add', 3, 2, 104, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1023, '岗位修改', 'system:post:edit', 3, 3, 104, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1024, '岗位删除', 'system:post:remove', 3, 4, 104, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1025, '岗位导出', 'system:post:export', 3, 5, 104, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1026, '字典查询', 'system:dict:query', 3, 1, 105, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1027, '字典新增', 'system:dict:add', 3, 2, 105, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1028, '字典修改', 'system:dict:edit', 3, 3, 105, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1029, '字典删除', 'system:dict:remove', 3, 4, 105, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1030, '字典导出', 'system:dict:export', 3, 5, 105, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1031, '参数查询', 'system:config:query', 3, 1, 106, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1032, '参数新增', 'system:config:add', 3, 2, 106, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1033, '参数修改', 'system:config:edit', 3, 3, 106, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1034, '参数删除', 'system:config:remove', 3, 4, 106, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1035, '参数导出', 'system:config:export', 3, 5, 106, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1036, '公告查询', 'system:notice:query', 3, 1, 107, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1037, '公告新增', 'system:notice:add', 3, 2, 107, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1038, '公告修改', 'system:notice:edit', 3, 3, 107, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1039, '公告删除', 'system:notice:remove', 3, 4, 107, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1040, '操作查询', 'monitor:operlog:query', 3, 1, 500, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1041, '操作删除', 'monitor:operlog:remove', 3, 2, 500, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1042, '日志导出', 'monitor:operlog:export', 3, 4, 500, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1043, '登录查询', 'monitor:logininfor:query', 3, 1, 501, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1044, '登录删除', 'monitor:logininfor:remove', 3, 2, 501, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1045, '日志导出', 'monitor:logininfor:export', 3, 3, 501, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1046, '在线查询', 'monitor:online:query', 3, 1, 109, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1047, '批量强退', 'monitor:online:batchLogout', 3, 2, 109, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1048, '单条强退', 'monitor:online:forceLogout', 3, 3, 109, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1049, '任务查询', 'monitor:job:query', 3, 1, 110, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1050, '任务新增', 'monitor:job:add', 3, 2, 110, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1051, '任务修改', 'monitor:job:edit', 3, 3, 110, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1052, '任务删除', 'monitor:job:remove', 3, 4, 110, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1053, '状态修改', 'monitor:job:changeStatus', 3, 5, 110, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1054, '任务导出', 'monitor:job:export', 3, 7, 110, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1055, '生成查询', 'tool:gen:query', 3, 1, 115, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1056, '生成修改', 'tool:gen:edit', 3, 2, 115, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1057, '生成删除', 'tool:gen:remove', 3, 3, 115, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1058, '导入代码', 'tool:gen:import', 3, 2, 115, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1059, '预览代码', 'tool:gen:preview', 3, 4, 115, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1060, '生成代码', 'tool:gen:code', 3, 5, 115, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1063, '设置角色菜单权限', 'system:permission:assign-role-menu', 3, 6, 101, '', '', '', 0, '', '2021-01-06 17:53:44', '', '2021-01-06 17:55:23', b'0');
INSERT INTO `sys_menu` VALUES (1064, '设置角色数据权限', 'system:permission:assign-role-data-scope', 3, 7, 101, '', '', '', 0, '', '2021-01-06 17:56:31', '', '2021-01-06 17:56:31', b'0');
COMMIT;

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
  `notice_id` int(4) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) NOT NULL COMMENT '公告标题',
  `notice_type` char(1) NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob COMMENT '公告内容',
  `status` char(1) DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
BEGIN;
INSERT INTO `sys_notice` VALUES (1, '温馨提醒：2018-07-01 若依新版本发布啦', '2', 0xE696B0E78988E69CACE58685E5AEB9, '0', 'admin', '2021-01-05 17:03:48', '', NULL, '管理员');
INSERT INTO `sys_notice` VALUES (2, '维护通知：2018-07-01 若依系统凌晨维护', '1', 0xE7BBB4E68AA4E58685E5AEB9, '0', 'admin', '2021-01-05 17:03:48', '', NULL, '管理员');
COMMIT;

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log` (
  `oper_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) DEFAULT '' COMMENT '模块标题',
  `business_type` int(2) DEFAULT '0' COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(100) DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) DEFAULT '' COMMENT '请求方式',
  `operator_type` int(1) DEFAULT '0' COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(50) DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) DEFAULT '' COMMENT '返回参数',
  `status` int(1) DEFAULT '0' COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`oper_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='操作日志记录';

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post` (
  `post_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code` varchar(64) NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) NOT NULL COMMENT '岗位名称',
  `post_sort` int(4) NOT NULL COMMENT '显示顺序',
  `status` char(1) NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COMMENT='岗位信息表';

-- ----------------------------
-- Records of sys_post
-- ----------------------------
BEGIN;
INSERT INTO `sys_post` VALUES (1, 'ceo', '董事长', 1, '0', 'admin', '2021-01-05 17:03:48', '', NULL, '');
INSERT INTO `sys_post` VALUES (2, 'se', '项目经理', 2, '0', 'admin', '2021-01-05 17:03:48', '', NULL, '');
INSERT INTO `sys_post` VALUES (3, 'hr', '人力资源', 3, '0', 'admin', '2021-01-05 17:03:48', '', NULL, '');
INSERT INTO `sys_post` VALUES (4, 'user', '普通员工', 4, '0', 'admin', '2021-01-05 17:03:48', '', NULL, '');
COMMIT;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(30) NOT NULL COMMENT '角色名称',
  `code` varchar(100) NOT NULL COMMENT '角色权限字符串',
  `sort` int(4) NOT NULL COMMENT '显示顺序',
  `data_scope` tinyint(4) NOT NULL DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `data_scope_dept_ids` varchar(500) NOT NULL DEFAULT '' COMMENT '数据范围(指定部门数组)',
  `status` tinyint(4) NOT NULL COMMENT '角色状态（0正常 1停用）',
  `type` tinyint(4) NOT NULL COMMENT '角色类型',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=102 DEFAULT CHARSET=utf8mb4 COMMENT='角色信息表';

-- ----------------------------
-- Records of sys_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin', 1, 1, '', 0, 1, '超级管理员', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 12:40:20', b'0');
INSERT INTO `sys_role` VALUES (2, '普通角色', 'common', 2, 2, '', 0, 1, '普通角色', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 11:46:58', b'0');
INSERT INTO `sys_role` VALUES (101, '测试账号', 'test', 0, 2, '[103]', 0, 2, '132', '', '2021-01-06 13:49:35', '', '2021-01-06 20:36:02', b'0');
COMMIT;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增编号',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=169 DEFAULT CHARSET=utf8mb4 COMMENT='角色和菜单关联表';

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
BEGIN;
INSERT INTO `sys_role_menu` VALUES (1, 2, 1, '', '2021-01-06 17:28:04', '', '2021-01-06 17:48:51', b'1');
INSERT INTO `sys_role_menu` VALUES (2, 2, 2, '', '2021-01-06 17:28:04', '', '2021-01-06 17:48:51', b'1');
INSERT INTO `sys_role_menu` VALUES (3, 2, 3, '', '2021-01-06 17:28:04', '', '2021-01-06 17:48:51', b'1');
INSERT INTO `sys_role_menu` VALUES (4, 2, 4, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (5, 2, 100, '', '2021-01-06 17:28:04', '', '2021-01-06 17:48:51', b'1');
INSERT INTO `sys_role_menu` VALUES (6, 2, 101, '', '2021-01-06 17:28:04', '', '2021-01-06 17:48:51', b'1');
INSERT INTO `sys_role_menu` VALUES (7, 2, 102, '', '2021-01-06 17:28:04', '', '2021-01-06 17:48:51', b'1');
INSERT INTO `sys_role_menu` VALUES (8, 2, 103, '', '2021-01-06 17:28:04', '', '2021-01-06 17:48:51', b'1');
INSERT INTO `sys_role_menu` VALUES (9, 2, 104, '', '2021-01-06 17:28:04', '', '2021-01-06 17:48:51', b'1');
INSERT INTO `sys_role_menu` VALUES (10, 2, 105, '', '2021-01-06 17:28:04', '', '2021-01-06 17:48:51', b'1');
INSERT INTO `sys_role_menu` VALUES (11, 2, 106, '', '2021-01-06 17:28:04', '', '2021-01-06 17:48:51', b'1');
INSERT INTO `sys_role_menu` VALUES (12, 2, 107, '', '2021-01-06 17:28:04', '', '2021-01-06 17:48:51', b'1');
INSERT INTO `sys_role_menu` VALUES (13, 2, 108, '', '2021-01-06 17:28:04', '', '2021-01-06 17:48:51', b'1');
INSERT INTO `sys_role_menu` VALUES (14, 2, 109, '', '2021-01-06 17:28:04', '', '2021-01-06 17:48:51', b'1');
INSERT INTO `sys_role_menu` VALUES (15, 2, 110, '', '2021-01-06 17:28:04', '', '2021-01-06 17:48:51', b'1');
INSERT INTO `sys_role_menu` VALUES (16, 2, 111, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (17, 2, 112, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (18, 2, 113, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (19, 2, 114, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (20, 2, 115, '', '2021-01-06 17:28:04', '', '2021-01-06 17:48:51', b'1');
INSERT INTO `sys_role_menu` VALUES (21, 2, 116, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (22, 2, 500, '', '2021-01-06 17:28:04', '', '2021-01-06 17:48:51', b'1');
INSERT INTO `sys_role_menu` VALUES (23, 2, 501, '', '2021-01-06 17:28:04', '', '2021-01-06 17:48:51', b'1');
INSERT INTO `sys_role_menu` VALUES (24, 2, 1000, '', '2021-01-06 17:28:04', '', '2021-01-06 17:48:51', b'1');
INSERT INTO `sys_role_menu` VALUES (25, 2, 1001, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (26, 2, 1002, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (27, 2, 1003, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (28, 2, 1004, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (29, 2, 1005, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (30, 2, 1006, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (31, 2, 1007, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (32, 2, 1008, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (33, 2, 1009, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (34, 2, 1010, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (35, 2, 1011, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (36, 2, 1012, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (37, 2, 1013, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (38, 2, 1014, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (39, 2, 1015, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (40, 2, 1016, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (41, 2, 1017, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (42, 2, 1018, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (43, 2, 1019, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (44, 2, 1020, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (45, 2, 1021, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (46, 2, 1022, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (47, 2, 1023, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (48, 2, 1024, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (49, 2, 1025, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (50, 2, 1026, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (51, 2, 1027, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (52, 2, 1028, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (53, 2, 1029, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (54, 2, 1030, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (55, 2, 1031, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (56, 2, 1032, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (57, 2, 1033, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (58, 2, 1034, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (59, 2, 1035, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (60, 2, 1036, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (61, 2, 1037, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (62, 2, 1038, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (63, 2, 1039, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (64, 2, 1040, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (65, 2, 1041, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (66, 2, 1042, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (67, 2, 1043, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (68, 2, 1044, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (69, 2, 1045, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (70, 2, 1046, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (71, 2, 1047, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (72, 2, 1048, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (73, 2, 1049, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (74, 2, 1050, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (75, 2, 1051, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (76, 2, 1052, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (77, 2, 1053, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (78, 2, 1054, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (79, 2, 1055, '', '2021-01-06 17:28:04', '', '2021-01-06 17:48:51', b'1');
INSERT INTO `sys_role_menu` VALUES (80, 2, 1056, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (81, 2, 1057, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (82, 2, 1058, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (83, 2, 1059, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (84, 2, 1060, '', '2021-01-06 17:28:04', '', '2021-01-06 17:28:04', b'0');
INSERT INTO `sys_role_menu` VALUES (85, 101, 114, '', '2021-01-06 17:30:23', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (88, 101, 3, '', '2021-01-06 17:44:56', '', '2021-01-06 17:47:52', b'1');
INSERT INTO `sys_role_menu` VALUES (89, 101, 1056, '', '2021-01-06 17:47:52', '', '2021-01-06 17:47:59', b'1');
INSERT INTO `sys_role_menu` VALUES (90, 101, 1057, '', '2021-01-06 17:47:52', '', '2021-01-06 17:47:59', b'1');
INSERT INTO `sys_role_menu` VALUES (91, 101, 1058, '', '2021-01-06 17:47:52', '', '2021-01-06 17:47:59', b'1');
INSERT INTO `sys_role_menu` VALUES (92, 101, 1059, '', '2021-01-06 17:47:52', '', '2021-01-06 17:47:59', b'1');
INSERT INTO `sys_role_menu` VALUES (93, 101, 1060, '', '2021-01-06 17:47:52', '', '2021-01-06 17:47:59', b'1');
INSERT INTO `sys_role_menu` VALUES (94, 101, 116, '', '2021-01-06 17:47:52', '', '2021-01-06 17:49:37', b'1');
INSERT INTO `sys_role_menu` VALUES (95, 101, 1055, '', '2021-01-06 17:47:52', '', '2021-01-06 17:47:59', b'1');
INSERT INTO `sys_role_menu` VALUES (96, 101, 1055, '', '2021-01-06 17:48:04', '', '2021-01-06 17:49:37', b'1');
INSERT INTO `sys_role_menu` VALUES (97, 101, 1056, '', '2021-01-06 17:48:13', '', '2021-01-06 17:49:37', b'1');
INSERT INTO `sys_role_menu` VALUES (98, 101, 1057, '', '2021-01-06 17:48:13', '', '2021-01-06 17:49:37', b'1');
INSERT INTO `sys_role_menu` VALUES (99, 101, 1058, '', '2021-01-06 17:48:13', '', '2021-01-06 17:49:37', b'1');
INSERT INTO `sys_role_menu` VALUES (100, 101, 1059, '', '2021-01-06 17:49:29', '', '2021-01-06 17:49:37', b'1');
INSERT INTO `sys_role_menu` VALUES (101, 101, 1060, '', '2021-01-06 17:49:29', '', '2021-01-06 17:49:37', b'1');
INSERT INTO `sys_role_menu` VALUES (102, 101, 1024, '', '2021-01-06 18:00:10', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (103, 101, 1025, '', '2021-01-06 18:00:10', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (104, 101, 1026, '', '2021-01-06 18:00:10', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (105, 101, 1027, '', '2021-01-06 18:00:10', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (106, 101, 1028, '', '2021-01-06 18:00:10', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (107, 101, 4, '', '2021-01-06 18:00:10', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (108, 101, 1029, '', '2021-01-06 18:00:10', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (109, 101, 1030, '', '2021-01-06 18:00:10', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (110, 101, 1031, '', '2021-01-06 18:00:10', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (111, 101, 1032, '', '2021-01-06 18:00:10', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (112, 101, 1033, '', '2021-01-06 18:00:10', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (113, 101, 1034, '', '2021-01-06 18:00:10', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (114, 101, 1035, '', '2021-01-06 18:00:10', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (115, 101, 1036, '', '2021-01-06 18:00:10', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (116, 101, 1037, '', '2021-01-06 18:00:10', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (117, 101, 1038, '', '2021-01-06 18:00:10', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (118, 101, 1039, '', '2021-01-06 18:00:10', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (119, 101, 1040, '', '2021-01-06 18:00:10', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (120, 101, 1041, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (121, 101, 1042, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (122, 101, 1043, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (123, 101, 1044, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (124, 101, 1045, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (125, 101, 1046, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (126, 101, 1047, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (127, 101, 1048, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (128, 101, 1049, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (129, 101, 1050, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (130, 101, 1051, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (131, 101, 1052, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (132, 101, 1053, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (133, 101, 1054, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (134, 101, 1055, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (135, 101, 1056, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (136, 101, 1057, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (137, 101, 1058, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (138, 101, 1059, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (139, 101, 1060, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (140, 101, 1063, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (141, 101, 1064, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (142, 101, 1001, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (143, 101, 1002, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (144, 101, 1003, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (145, 101, 1004, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (146, 101, 1005, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (147, 101, 1006, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (148, 101, 1007, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (149, 101, 111, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (150, 101, 1008, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (151, 101, 112, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (152, 101, 1009, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (153, 101, 113, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (154, 101, 1010, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (155, 101, 1011, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (156, 101, 1012, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (157, 101, 116, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (158, 101, 1013, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (159, 101, 1014, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (160, 101, 1015, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (161, 101, 1016, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (162, 101, 1017, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (163, 101, 1018, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (164, 101, 1019, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (165, 101, 1020, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (166, 101, 1021, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (167, 101, 1022, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
INSERT INTO `sys_role_menu` VALUES (168, 101, 1023, '', '2021-01-06 18:00:11', '', '2021-01-06 18:00:16', b'1');
COMMIT;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(30) NOT NULL COMMENT '用户账号',
  `password` varchar(100) NOT NULL DEFAULT '' COMMENT '密码',
  `nickname` varchar(30) NOT NULL COMMENT '用户昵称',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '部门ID',
  `post_ids` varchar(255) DEFAULT NULL COMMENT '岗位编号数组',
  `email` varchar(50) DEFAULT '' COMMENT '用户邮箱',
  `mobile` varchar(11) DEFAULT '' COMMENT '手机号码',
  `sex` tinyint(4) DEFAULT '0' COMMENT '用户性别',
  `avatar` varchar(100) DEFAULT '' COMMENT '头像地址',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
  `login_ip` varchar(50) DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- ----------------------------
-- Records of sys_user
-- ----------------------------
BEGIN;
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '若依', '管理员', 103, '[1]', 'ry@163.com', '15888888888', 1, '', 0, '127.0.0.1', '2021-01-05 17:03:47', 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_user` VALUES (2, 'ry', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '若依', '测试员', 105, '[2]', 'ry@qq.com', '15666666666', 1, '', 0, '127.0.0.1', '2021-01-05 17:03:47', 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
COMMIT;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增编号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` bit(1) DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COMMENT='用户和角色关联表';

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_user_role` VALUES (1, 1, 1, '', NULL, '', NULL, b'0');
INSERT INTO `sys_user_role` VALUES (2, 2, 2, '', NULL, '', NULL, b'0');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
