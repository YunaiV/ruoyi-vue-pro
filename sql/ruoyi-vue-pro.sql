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

 Date: 28/01/2021 01:32:22
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
-- Table structure for gen_table_column_copy1
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column_copy1`;
CREATE TABLE `gen_table_column_copy1` (
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
-- Records of gen_table_column_copy1
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for gen_table_copy1
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_copy1`;
CREATE TABLE `gen_table_copy1` (
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
-- Records of gen_table_copy1
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for inf_config
-- ----------------------------
DROP TABLE IF EXISTS `inf_config`;
CREATE TABLE `inf_config` (
  `id` int(5) NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `group` varchar(50) NOT NULL COMMENT '参数分组',
  `type` tinyint(4) NOT NULL COMMENT '参数类型',
  `name` varchar(100) NOT NULL DEFAULT '' COMMENT '参数名称',
  `key` varchar(100) NOT NULL DEFAULT '' COMMENT '参数键名',
  `value` varchar(500) NOT NULL DEFAULT '' COMMENT '参数键值',
  `sensitive` bit(1) NOT NULL COMMENT '是否敏感',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COMMENT='参数配置表';

-- ----------------------------
-- Records of inf_config
-- ----------------------------
BEGIN;
INSERT INTO `inf_config` VALUES (1, 'ui', 1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', b'0', '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow', 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `inf_config` VALUES (2, 'biz', 1, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', b'0', '初始化密码 123456', 'admin', '2021-01-05 17:03:48', '', '2021-01-21 02:13:02', b'0');
INSERT INTO `inf_config` VALUES (3, 'ui', 1, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', b'0', '深色主题theme-dark，浅色主题theme-light', 'admin', '2021-01-05 17:03:48', '', '2021-01-19 03:05:21', b'0');
INSERT INTO `inf_config` VALUES (4, '1', 2, 'xxx', 'demo.test', '10', b'0', '5', '', '2021-01-19 03:10:26', '', '2021-01-20 09:25:55', b'0');
COMMIT;

-- ----------------------------
-- Table structure for inf_config_copy1
-- ----------------------------
DROP TABLE IF EXISTS `inf_config_copy1`;
CREATE TABLE `inf_config_copy1` (
  `id` int(5) NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `group` varchar(50) NOT NULL COMMENT '参数分组',
  `type` tinyint(4) NOT NULL COMMENT '参数类型',
  `name` varchar(100) NOT NULL DEFAULT '' COMMENT '参数名称',
  `key` varchar(100) NOT NULL DEFAULT '' COMMENT '参数键名',
  `value` varchar(500) NOT NULL DEFAULT '' COMMENT '参数键值',
  `sensitive` bit(1) NOT NULL COMMENT '是否敏感',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COMMENT='参数配置表';

-- ----------------------------
-- Records of inf_config_copy1
-- ----------------------------
BEGIN;
INSERT INTO `inf_config_copy1` VALUES (1, 'ui', 1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', b'0', '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow', 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `inf_config_copy1` VALUES (2, 'biz', 1, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', b'0', '初始化密码 123456', 'admin', '2021-01-05 17:03:48', '', '2021-01-21 02:13:02', b'0');
INSERT INTO `inf_config_copy1` VALUES (3, 'ui', 1, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', b'0', '深色主题theme-dark，浅色主题theme-light', 'admin', '2021-01-05 17:03:48', '', '2021-01-19 03:05:21', b'0');
INSERT INTO `inf_config_copy1` VALUES (4, '1', 2, 'xxx', 'demo.test', '10', b'0', '5', '', '2021-01-19 03:10:26', '', '2021-01-20 09:25:55', b'0');
COMMIT;

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `name` varchar(30) NOT NULL DEFAULT '' COMMENT '部门名称',
  `parent_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '父部门id',
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
) ENGINE=InnoDB AUTO_INCREMENT=110 DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
BEGIN;
INSERT INTO `sys_dept` VALUES (100, '芋道源码', 0, 0, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-06 19:45:52', b'0');
INSERT INTO `sys_dept` VALUES (101, '深圳总公司', 100, 1, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept` VALUES (102, '长沙分公司', 100, 2, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept` VALUES (103, '研发部门', 101, 1, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept` VALUES (104, '市场部门', 101, 2, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept` VALUES (105, '测试部门', 101, 3, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept` VALUES (106, '财务部门', 101, 4, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept` VALUES (107, '运维部门', 101, 5, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept` VALUES (108, '市场部门', 102, 1, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept` VALUES (109, '财务部门', 102, 2, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
COMMIT;

-- ----------------------------
-- Table structure for sys_dept_copy1
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept_copy1`;
CREATE TABLE `sys_dept_copy1` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `name` varchar(30) NOT NULL DEFAULT '' COMMENT '部门名称',
  `parent_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '父部门id',
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
) ENGINE=InnoDB AUTO_INCREMENT=110 DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ----------------------------
-- Records of sys_dept_copy1
-- ----------------------------
BEGIN;
INSERT INTO `sys_dept_copy1` VALUES (100, '芋道源码', 0, 0, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-06 19:45:52', b'0');
INSERT INTO `sys_dept_copy1` VALUES (101, '深圳总公司', 100, 1, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept_copy1` VALUES (102, '长沙分公司', 100, 2, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept_copy1` VALUES (103, '研发部门', 101, 1, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept_copy1` VALUES (104, '市场部门', 101, 2, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept_copy1` VALUES (105, '测试部门', 101, 3, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept_copy1` VALUES (106, '财务部门', 101, 4, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept_copy1` VALUES (107, '运维部门', 101, 5, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept_copy1` VALUES (108, '市场部门', 102, 1, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_dept_copy1` VALUES (109, '财务部门', 102, 2, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
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
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

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
INSERT INTO `sys_dict_data` VALUES (12, 1, '系统内置', '1', 'sys_config_type', 0, '参数类型 - 系统内置', 'admin', '2021-01-05 17:03:48', '', '2021-01-18 07:41:59', b'0');
INSERT INTO `sys_dict_data` VALUES (13, 2, '自定义', '2', 'sys_config_type', 0, '参数类型 - 自定义', 'admin', '2021-01-05 17:03:48', '', '2021-01-18 07:41:51', b'0');
INSERT INTO `sys_dict_data` VALUES (14, 1, '通知', '1', 'sys_notice_type', 0, '通知', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (15, 2, '公告', '2', 'sys_notice_type', 0, '公告', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data` VALUES (16, 0, '其它', '0', 'sys_operate_type', 0, '其它操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-16 13:51:12', b'0');
INSERT INTO `sys_dict_data` VALUES (17, 1, '查询', '1', 'sys_operate_type', 0, '查询操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-16 13:51:10', b'0');
INSERT INTO `sys_dict_data` VALUES (18, 2, '新增', '2', 'sys_operate_type', 0, '新增操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-16 13:51:17', b'0');
INSERT INTO `sys_dict_data` VALUES (19, 3, '修改', '3', 'sys_operate_type', 0, '修改操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-16 13:51:20', b'0');
INSERT INTO `sys_dict_data` VALUES (20, 4, '删除', '4', 'sys_operate_type', 0, '删除操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-16 13:51:24', b'0');
INSERT INTO `sys_dict_data` VALUES (22, 5, '导出', '5', 'sys_operate_type', 0, '导出操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-16 13:49:20', b'0');
INSERT INTO `sys_dict_data` VALUES (23, 6, '导入', '6', 'sys_operate_type', 0, '导入操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-16 13:49:24', b'0');
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
INSERT INTO `sys_dict_data` VALUES (39, 0, '成功', '0', 'sys_login_result', 0, '登陆结果 - 成功', '', '2021-01-18 06:17:36', '', '2021-01-18 06:17:36', b'0');
INSERT INTO `sys_dict_data` VALUES (40, 10, '账号或密码不正确', '10', 'sys_login_result', 0, '登陆结果 - 账号或密码不正确', '', '2021-01-18 06:17:54', '', '2021-01-18 06:17:54', b'0');
INSERT INTO `sys_dict_data` VALUES (41, 20, '用户被禁用', '20', 'sys_login_result', 0, '登陆结果 - 用户被禁用', '', '2021-01-18 06:17:54', '', '2021-01-18 06:19:02', b'0');
INSERT INTO `sys_dict_data` VALUES (42, 30, '验证码不存在', '30', 'sys_login_result', 0, '登陆结果 - 验证码不存在', '', '2021-01-18 06:17:54', '', '2021-01-18 06:19:24', b'0');
INSERT INTO `sys_dict_data` VALUES (43, 31, '验证码不正确', '31', 'sys_login_result', 0, '登陆结果 - 验证码不正确', '', '2021-01-18 06:17:54', '', '2021-01-18 06:19:33', b'0');
INSERT INTO `sys_dict_data` VALUES (44, 100, '未知异常', '100', 'sys_login_result', 0, '登陆结果 - 未知异常', '', '2021-01-18 06:17:54', '', '2021-01-18 06:19:57', b'0');
INSERT INTO `sys_dict_data` VALUES (45, 1, '是', 'true', 'sys_boolean_string', 0, 'Boolean 是否类型 - 是', '', '2021-01-19 03:20:55', '', '2021-01-19 03:21:08', b'0');
INSERT INTO `sys_dict_data` VALUES (46, 1, '否', 'false', 'sys_boolean_string', 0, 'Boolean 是否类型 - 否', '', '2021-01-19 03:20:55', '', '2021-01-19 03:21:39', b'0');
INSERT INTO `sys_dict_data` VALUES (47, 1, '永不超时', '1', 'inf_redis_timeout_type', 0, 'Redis 未设置超时的情况', '', '2021-01-26 00:53:17', '', '2021-01-26 00:53:17', b'0');
INSERT INTO `sys_dict_data` VALUES (48, 1, '动态超时', '2', 'inf_redis_timeout_type', 0, '程序里动态传入超时时间，无法固定', '', '2021-01-26 00:55:00', '', '2021-01-26 00:55:00', b'0');
INSERT INTO `sys_dict_data` VALUES (49, 3, '固定超时', '3', 'inf_redis_timeout_type', 0, 'Redis 设置了过期时间', '', '2021-01-26 00:55:26', '', '2021-01-26 00:55:26', b'0');
COMMIT;

-- ----------------------------
-- Table structure for sys_dict_data_copy1
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data_copy1`;
CREATE TABLE `sys_dict_data_copy1` (
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
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- ----------------------------
-- Records of sys_dict_data_copy1
-- ----------------------------
BEGIN;
INSERT INTO `sys_dict_data_copy1` VALUES (1, 1, '男', '1', 'sys_user_sex', 0, '性别男', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 05:48:53', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (2, 2, '女', '2', 'sys_user_sex', 0, '性别女', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 05:48:55', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (8, 1, '正常', '0', 'sys_job_status', 0, '正常状态', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (9, 2, '暂停', '1', 'sys_job_status', 0, '停用状态', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', 0, '默认分组', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', 0, '系统分组', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (12, 1, '系统内置', '1', 'sys_config_type', 0, '参数类型 - 系统内置', 'admin', '2021-01-05 17:03:48', '', '2021-01-18 07:41:59', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (13, 2, '自定义', '2', 'sys_config_type', 0, '参数类型 - 自定义', 'admin', '2021-01-05 17:03:48', '', '2021-01-18 07:41:51', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (14, 1, '通知', '1', 'sys_notice_type', 0, '通知', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (15, 2, '公告', '2', 'sys_notice_type', 0, '公告', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 00:02:28', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (16, 0, '其它', '0', 'sys_operate_type', 0, '其它操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-16 13:51:12', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (17, 1, '查询', '1', 'sys_operate_type', 0, '查询操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-16 13:51:10', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (18, 2, '新增', '2', 'sys_operate_type', 0, '新增操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-16 13:51:17', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (19, 3, '修改', '3', 'sys_operate_type', 0, '修改操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-16 13:51:20', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (20, 4, '删除', '4', 'sys_operate_type', 0, '删除操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-16 13:51:24', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (22, 5, '导出', '5', 'sys_operate_type', 0, '导出操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-16 13:49:20', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (23, 6, '导入', '6', 'sys_operate_type', 0, '导入操作', 'admin', '2021-01-05 17:03:48', '', '2021-01-16 13:49:24', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (27, 1, '开启', '0', 'sys_common_status', 0, '开启状态', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 02:57:12', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (28, 2, '关闭', '1', 'sys_common_status', 0, '关闭状态', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 05:48:32', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (29, 1, '目录', '1', 'sys_menu_type', 0, '目录', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 13:33:30', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (30, 2, '菜单', '2', 'sys_menu_type', 0, '菜单', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 13:33:35', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (31, 3, '按钮', '3', 'sys_menu_type', 0, '按钮', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 13:33:38', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (32, 1, '内置', '1', 'sys_role_type', 0, '内置角色', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 13:34:22', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (33, 2, '自定义', '2', 'sys_role_type', 0, '自定义角色', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 13:34:26', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (34, 1, '全部数据权限', '1', 'sys_data_scope', 0, '全部数据权限', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 19:38:02', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (35, 2, '指定部门数据权限', '2', 'sys_data_scope', 0, '指定部门数据权限', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 19:38:20', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (36, 3, '本部门数据权限', '3', 'sys_data_scope', 0, '本部门数据权限', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 19:38:29', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (37, 4, '本部门及以下数据权限', '4', 'sys_data_scope', 0, '本部门及以下数据权限', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 19:38:32', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (38, 5, '仅本人数据权限', '5', 'sys_data_scope', 0, '仅本人数据权限', 'admin', '2021-01-05 17:03:48', '', '2021-01-06 19:38:38', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (39, 0, '成功', '0', 'sys_login_result', 0, '登陆结果 - 成功', '', '2021-01-18 06:17:36', '', '2021-01-18 06:17:36', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (40, 10, '账号或密码不正确', '10', 'sys_login_result', 0, '登陆结果 - 账号或密码不正确', '', '2021-01-18 06:17:54', '', '2021-01-18 06:17:54', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (41, 20, '用户被禁用', '20', 'sys_login_result', 0, '登陆结果 - 用户被禁用', '', '2021-01-18 06:17:54', '', '2021-01-18 06:19:02', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (42, 30, '验证码不存在', '30', 'sys_login_result', 0, '登陆结果 - 验证码不存在', '', '2021-01-18 06:17:54', '', '2021-01-18 06:19:24', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (43, 31, '验证码不正确', '31', 'sys_login_result', 0, '登陆结果 - 验证码不正确', '', '2021-01-18 06:17:54', '', '2021-01-18 06:19:33', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (44, 100, '未知异常', '100', 'sys_login_result', 0, '登陆结果 - 未知异常', '', '2021-01-18 06:17:54', '', '2021-01-18 06:19:57', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (45, 1, '是', 'true', 'sys_boolean_string', 0, 'Boolean 是否类型 - 是', '', '2021-01-19 03:20:55', '', '2021-01-19 03:21:08', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (46, 1, '否', 'false', 'sys_boolean_string', 0, 'Boolean 是否类型 - 否', '', '2021-01-19 03:20:55', '', '2021-01-19 03:21:39', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (47, 1, '永不超时', '1', 'inf_redis_timeout_type', 0, 'Redis 未设置超时的情况', '', '2021-01-26 00:53:17', '', '2021-01-26 00:53:17', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (48, 1, '动态超时', '2', 'inf_redis_timeout_type', 0, '程序里动态传入超时时间，无法固定', '', '2021-01-26 00:55:00', '', '2021-01-26 00:55:00', b'0');
INSERT INTO `sys_dict_data_copy1` VALUES (49, 3, '固定超时', '3', 'inf_redis_timeout_type', 0, 'Redis 设置了过期时间', '', '2021-01-26 00:55:26', '', '2021-01-26 00:55:26', b'0');
COMMIT;

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `name` varchar(100) NOT NULL DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) NOT NULL DEFAULT '' COMMENT '字典类型',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `dict_type` (`dict_type`)
) ENGINE=InnoDB AUTO_INCREMENT=106 DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
BEGIN;
INSERT INTO `sys_dict_type` VALUES (1, '用户性别', 'sys_user_sex', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type` VALUES (3, '系统开关', 'sys_normal_disable', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2021-01-07 19:47:48', b'1');
INSERT INTO `sys_dict_type` VALUES (4, '任务状态', 'sys_job_status', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type` VALUES (5, '任务分组', 'sys_job_group', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type` VALUES (6, '参数类型', 'sys_config_type', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2021-01-18 07:41:04', b'0');
INSERT INTO `sys_dict_type` VALUES (7, '通知类型', 'sys_notice_type', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type` VALUES (8, '通知状态', 'sys_notice_status', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type` VALUES (9, '操作类型', 'sys_oper_type', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type` VALUES (10, '系统状态', 'sys_common_status', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type` VALUES (11, 'Boolean 是否类型', 'sys_boolean_string', 0, 'boolean 转是否', '', '2021-01-19 03:20:08', '', '2021-01-19 03:20:08', b'0');
INSERT INTO `sys_dict_type` VALUES (104, '登陆结果', 'sys_login_result', 0, '登陆结果', '', '2021-01-18 06:17:11', '', '2021-01-18 06:17:11', b'0');
INSERT INTO `sys_dict_type` VALUES (105, 'Redis 超时类型', 'inf_redis_timeout_type', 0, 'RedisKeyDefine.TimeoutTypeEnum', '', '2021-01-26 00:52:50', '', '2021-01-26 00:52:50', b'0');
COMMIT;

-- ----------------------------
-- Table structure for sys_dict_type_copy1
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type_copy1`;
CREATE TABLE `sys_dict_type_copy1` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `name` varchar(100) NOT NULL DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) NOT NULL DEFAULT '' COMMENT '字典类型',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `dict_type` (`dict_type`)
) ENGINE=InnoDB AUTO_INCREMENT=106 DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- ----------------------------
-- Records of sys_dict_type_copy1
-- ----------------------------
BEGIN;
INSERT INTO `sys_dict_type_copy1` VALUES (1, '用户性别', 'sys_user_sex', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type_copy1` VALUES (3, '系统开关', 'sys_normal_disable', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2021-01-07 19:47:48', b'1');
INSERT INTO `sys_dict_type_copy1` VALUES (4, '任务状态', 'sys_job_status', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type_copy1` VALUES (5, '任务分组', 'sys_job_group', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type_copy1` VALUES (6, '参数类型', 'sys_config_type', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2021-01-18 07:41:04', b'0');
INSERT INTO `sys_dict_type_copy1` VALUES (7, '通知类型', 'sys_notice_type', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type_copy1` VALUES (8, '通知状态', 'sys_notice_status', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type_copy1` VALUES (9, '操作类型', 'sys_oper_type', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type_copy1` VALUES (10, '系统状态', 'sys_common_status', 0, NULL, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_dict_type_copy1` VALUES (11, 'Boolean 是否类型', 'sys_boolean_string', 0, 'boolean 转是否', '', '2021-01-19 03:20:08', '', '2021-01-19 03:20:08', b'0');
INSERT INTO `sys_dict_type_copy1` VALUES (104, '登陆结果', 'sys_login_result', 0, '登陆结果', '', '2021-01-18 06:17:11', '', '2021-01-18 06:17:11', b'0');
INSERT INTO `sys_dict_type_copy1` VALUES (105, 'Redis 超时类型', 'inf_redis_timeout_type', 0, 'RedisKeyDefine.TimeoutTypeEnum', '', '2021-01-26 00:52:50', '', '2021-01-26 00:52:50', b'0');
COMMIT;

-- ----------------------------
-- Table structure for sys_file
-- ----------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
  `id` varchar(255) NOT NULL COMMENT '文件路径',
  `content` blob NOT NULL COMMENT '文件内容',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表\n';

-- ----------------------------
-- Records of sys_file
-- ----------------------------
BEGIN;
INSERT INTO `sys_file` VALUES ('add5ec1891a7d97d2cc1d60847e16294.jpg', 0xFFD8FFE000104A46494600010101006400640000FFDB00840006040506050406060506070706080A100A0A09090A140E0F0C1017141818171416161A1D251F1A1B231C1616202C20232627292A29191F2D302D283025282928010707070A080A130A0A13281A161A2828282828282828282828282828282828282828282828282828282828282828282828282828282828282828282828282828FFC200110800EC00EC03012200021101031101FFC4001C0001000105010100000000000000000000000701020405060308FFDA0008010100000000FA14000707A3E3F659B5F2B3CB1B171B57BAFA2C000BE1ED8731AFC752DF3F2F2EF249DF79D0000A465CD59E5ACA5BE7B09BB71441BBB943DC0017447C95DB0D662E4FD0192237F3E17E8600039B8AF556E579647D0B60807A3DA49368003D7E7AC1A5B678CA3255081A4AEBC0007AFCF5834C6E8BA3DDF756902CE39200034908796C661E82C04073E003D755A4B2FC9F48A34CFA172404152C6F415C4E175B9BB879F8EAB41CEF792A807CF1B1903B7BADA5DC0737D47ADCB3CFCBCE3FD2CCBDB807CE35CAF5E8B3F0757D0FBF1BE7DD655B65965BF384B9397B80F9C6A2EDAF577F05A0F3D9499958BACF4E7F41D0654E168118657B59A8E2E43DD71B31FC73ED26EE33FB0E939E8FB135FEFBA9528007159783CAD981B8C5DDC9596A5231D6E17173476200BE26D8351AFCEB33FBED077DABE2FCFB6DB59CE463365A018B156E4A3CB7DDBC65CDE3EDF71259C945B3D7900725CFFB554C0EA3B769E32BEC92F794A4711EEE3BDEBAC0380C657CB7FD6E50F3D4E266EE0C2E2F9DE73CA6FD90237B7CB7FDB5F554B6911C8BB8A948CB039CE5E79E94230B3B6E98A82945453C225BB87D44E3DA158BB77DF56A00A1452DE6A2DB397D6FD5C562696B215002DAE1E4D29CCF8471C9DBF4E9EB03CF952A028A539DE8296E1C7DD3C2DABFA80BA3690760A5C28514A626A37F4A523DC2D7E9A772B1CC9B7733B2DA2828A2DB628EAFABA523DD1D29302BC6767EE88E46DB1414A29486A1A9CF8E99E2BD91303CF98ECE8897A9EC5428A294A70B04E5E279751D613038FECBD5485BA5911428A514530780D5E9B7B62930B93ED2D6A222EA64A28A514514ADB444FE56D7FFC4001B01000105010100000000000000000000000001020304050607FFDA0008010210000000F3600075E70AD558EA46005B9DCE33E1B7367000694AC32CD3CC00034E58A1A69A59A125A512766681ACD81C9A194D4F44E2B280D94750E8BD1383E7AE6F60E4D500BB0F4BB3CCEBAC540E7500BD5CB7D135B33E0E769342FA6DDE51AAADE56CD084D7DE780014F9CBF8EFEA2E800346D4CDC87F57654104628C38ED9DE6B841064EB5978DEAAEB155104115A7FFC4001A010002030101000000000000000000000000040102030506FFDA0008010310000000EB00043FBC939A35CE4003A76164A7AFC9800061CB22B8CAC0006EDA78C4B0B845F5B8C23981D3533A3CA742D7F0DDCD006A346783E07D9FA05BCFF7A92016EAF9EE0FABE6CE8DD4901AE84ADC1E8F5BCDE0CEC483FA72520BD62DE8D54E46B8F500019F429A318AC001259BEAF3215C8009BC1B5F4C55D2B5924B9560D55CB5AD2490B4EB3FFFC400451000010302020604090A050403010000000102030400110512061321314151203261711014222330425281D11524333440537291A1B116436292C16382E1F0357383A2FFDA0008010100013F01F4FC6B4931051908C2222ACB706690B1EAA397BE9E719811335B2B68D800AF9C4D735CB6D6A56E48036229BC3DF575B223BCDE861A84ED71E3F95AB53091D672FF00EEA270F1EA66FCE8C8863AB187F68A335A1D58C8FD2953F932DD2A628FF2DAFEDA3249FE5B5FD94F2D9BF94D3415FD236D4518C6ABE66D627A9E194AADF6047585622C3B866352E54C49533297E43C3684F61E54E105ACC946B788038D09B2E495063220A77802EAFD696EBF721C79DBF2BDABBC93DFD0346AF750424152D5B9291727DD587E8ACA9090B9EE888DFDDA76AFE02B0FC270FC3BEAB1939FEF17E52BF3AD62A9195C405B4B4B883B8A4DFEC0B4A1D694D3C80B6D42C526B12C39583FCE226672028F96DEF2DF68ECAC4594AB24A64EC3B7327876D22521D4E5988CDFEA246DA7A294A758C9D635CC784D1AC330F918A492D46B04A7E91D3B91F13D9585619170A6B2C74E678F59E575954493BFC3F25BB11CD6E1129C8CBF66FE49A8BA55221B89631D8BB0EC0F37C6A2BEC4C643B0DD4BAD9E47D38B1052B014856C20D488BF26622B84ADB11F1999278734D3CD969D520F034CBCB65576D56A25895BECCBDCFD534F34B65595C16FF346A0C473109CDC465594A815297ECA79D448CCC18A88D15395B4FEA799E88A75B43AD943A90B41DE0D3C1FD1F9499905CB344D8A4EEEEA0A0E34DB805B3A42BD3E92C432F075A9B1E7E3F9D47BAA7283A961F4FF00311E14485253915671BF655FE3952D0082B64E64F11C53DF583CD187630C495FD09F34E7603C6962C766E3D1462130904EA7F0E534EBB20A9A8F159D74B705F28DC91CCD46D185BEE25EC6A46B2DB7528DD4A3BAC2C06C03D3B7B4E53B8EC34B4EA98531F72FAD1E1357208209046E229C3981CC37EF238FBAB43315D73670D92BBBAD8BB2A3EB2797BBA3872017F32BAA819AB4559221393DC1E7652AE3B10370FB0B5D7152961C7A42C6E72438A1DD7B785C7128EB1DBC071350B02C5268CC969319BF69EDE7DD4DE8728FD63123DCDA2B0DD178906735284A7DC5B66E01B513737E824EAF0A98E0DE465FFBF9D436F5387C4680B656923F4FB0E3738C083E6B6CA7CEAD94F6F3F752C25166D06E940CA0F3EDF061B02462920B516C9423E91E56E4F6769AC2F0785860BB2DEB1FE2F39B5468A89DFD37ADF213DF8BFC8A1B59688DD907A20835227C18BF5898CA7B335E97A478524D92EBAE7E06C9AFE228C7A90A72FF00F9D0C7D9E302701FFAFF00E6938EE1C4F9C53CCF6BAD102A563586C58FAE3290E83D54B67328FBAA6CE7A64A54A90323A5391B6FEE91F13E04A1C79C6D9605DD7559103B6A2456A0446E2C74D9081B7B4F3F43193AF852637150B8AD1E942760CC281BB8D8D5AC768E9EFA992A3416F3CC7D0D0E00EF34EE9148977460F12C3EF9EDD4A87325FF00E47107560FA8DEC151F0B84C7518413CD5B68009EA803BA8D1A34F448EEAB32DA467F680B1FCEA4E1EA4254A64A9CE394EFF00CE92A0ADDDC472AD098E97A5CA9AB17D479A6FBCEF3E899714D38168DE2A338E33255270D7FC5DF59BADA5F51CA87A48CA9496B1465511FF006BD4FCE9BCAEA02D95A5C41E2937ACA7978522FDD5896901538A8B82A75AE8EB3C7AA9A630B0A735F3DC5497CEF2ADD49B0161B055EAF57A2689A346A7E30F78C29A8694D926C5445EF4EC85BEE053A84B723DB1D55F61F8D684048C04A13F4A1D59713C526FC7DDE910FAC2321F2DBF655B4530A61B5E646BE3AB9B2E5A9B9F3B66A31726DC1D40AF95B1C693B04391EEB1A5693632D9F3986B67B81A7B1BC5719CD0C3488ADFF3549BEEE550E3B71590DB42C07EB4FBEDC76CB8F2825239D3FA43B7E6D1CA87359B531A4842AD263651CD06A3496A4B41C616149357ABD5E8D71AC230886BD1790EBA91AE5A5C257C516BD4194BF18650B480D1010A17BDCF3A8921FC2DC131ABAB56723E8FBC4703DF51A435362B72A32B332E0B8F4C1453D524771A87AE7DD09D6B9946D3B69B425B4E540B0A52821254A3648173498F37185AA42595AA3A4F93C8549929662BEEB2DA17A95645051B1BD61AFB38A5D011AB780BE5E75143D86C82E37D5F593ED0A8B25B94C875955D06AF4FBEDB29CCF3894279A8D7CA71782D44730DAAD45F4C98EE884F20B994E5B1DC6B0275E6D9721C8D6A2DD64AB7281A7F4714257993E689BDEFBAA4C96A2CA4B6F1F36F2721ECEDAD1F9FF22622B8EF9F993EADBFD0AE7DD446E236A4EE3E813A1E7F9B89AFFDA9A4E8743DBAD99257EFB57F0861BF7D27FBA95A1F879EA48909F7D3FA21251730A685F24AC5AA6C79B86AAD3D8213EDA77560907E6F9DCD855B4FC29C8A35B649B0B5EB1D516F0C91CFABFAD600DB4CE09112DDB206C7FCD692869CD209EB67E8CBA77568FB4E7CA91CB69E3B7BAB1646469C58DA40D82B0D8A21450D5EEAEB28F6D3697E63C598405D3F48EABAADFC4F6541C1A24539D49D7C8E2EBBB4FBB97827E0F0A68BADA0DBBC1D6FC950AC423488032CE0A7A2DFC99281B53F8870EFAF9D048CA10F208D8A0ACA4D3985096A0E4C514AF8250760158AC44B91EE91D5163DD5A218C2B38C3272AE7F92BE7D94458FA4D2D7C96A261C9EB495E65F6213B6A23EDA59B28D88A724A8BAA523603B2B1B19B0C7EE6DB2FF00AD6172DF430194B8AD572A95804790F1790ACAA3BC54382C4004A7CA739D7D6E50B6D69A55D479AB9504392A408B1CD96A175AFEED3CFBF95458EDC5610CB09CADA770E8101408201076106BE4A5AF5AF610E06D21C524B0EED4120F03C29D5CA8DF5D82FB607AEDF9C4FE95F29C4E2EE5FC4922B15318A82E3C8403BD3955B8D68962C716C36CF2AF299D8AED1CFD1A05D429D77C771D9926F76DAF30DFBB7F87158AA99096CA1412A36DF50E0C88EC84A9E43847022DFAD66929D81827B42C56A243E7CFA836DF241BA8FBE965319901A6EFEAA109F589DC2B0F8660425EE724AAEB591EB2B977560788B8E4F991E5150505272E6EEF04B9CC450758BF2BD91BE9CD296028846AFF00326938FC89CBD461CC05BCA1D6DB64F693585C4F1282DB19B3A86D52B9A8ED27C0A48575803DE2B178EC2602C865B06E3720560EB30B4B5ACBB1124588A56C51F4531EF168129FE2DB648EFAC211920357DEBF2CFBFA4EB8865B52DD504A06F26B0684B71D4CD94828007996D5BC7F51EDF0694C53E3D16446506DE5052566DB176B581AF18C4BAB9503B75A6D5E28EBC6F31DCC9FBB46C1EFA4B6848B252903BAB01467C656B4F55A672ABBC9D9FB7431F72CDB4D73398D48F271BC29437EB2DFA8A77AE7D1697B9ABD1E75237BAA08A69391B4279003A3E3216EEA6321521EF61BDB6EF3C2B0DC1D7AE4C9C4B22DC4ED6D94ED4B7DBDA7C38AC3F1D8A50956475273215C8FC29D73C5DCD5CB4961CE4BDC7B8F1AD6200B95A2DCEF4D2D7255921365F5F31D51DE6B08802045C8559DD51CCE2FDA3D0C614A54F702BD5D83BAB16D6F8E40547FA54AC94F7D31A4B8CC43F3D8A1F6C6F396C7F31585E9161D889080B2C3E7D473E34A491DDE834CCDD186B1EDBD7E83257254530DB2F91BC8D891DEAA6B020ED8CF794E7FA4DF928F89A8EC351DB0DB0DA1B40F5522DD15A12E272B894A93C942E2BE4A819F3F8947CDCF2548C4998A754CB79B2F04EC02B0F9C8999C25252A45B37BFA12633525367537E4788A9FA3C1E19DA78EB1BBA9A047ADC298703CD058D97DE391E22B11C2DB76EB69365F2158563B3B082942FE710C6F4AB78EEAC33108B8AC7D7435DFDA41DE9E9E949CD8E618DFB282AB781D73265012A5AD46C94245CA8F6544C194F0CF89916E11D0760FC478FED484A5080942425237002C07A03B8D390714724A9866304241B78C38AF27BC0E3585406F0F881A412B513996E1DEB573E963917C46619681F357CD9DFE85FB5DC68D6251B617503F10A61C7F0F9224C0564706F1C0D6038DC7C659B27CDCA48F2DB3FB8ECE96909CDA551C704B17FDE9E73568D892B59395284EF51E558461BE2BE7E45973163CA57040F653FF0076FD89E6D0F34B6DD48536B16524F115219561B24457AFA93B1874FAC3D93DB46A5B3A978A7D5DE2BCE30FA64C45143E837045617A510254342E63896246E5A4F3ECE871AC636E972FFA628FDEB47E3090F2A7AF6A5376D81FBABFC7D931D85F2843530DB812F6F483B95DE3FCD403250B5C793B54DEC39B62D3D879F7D62A9F36857236F03B192B5E6B91D01BEB1F51563D2D2D6C756866324F6AAFF1A8ECA23B0DB2D0B21B4848FB03EFA59C80DCAD672A1237A8F431E694A845F66FAE8FE705B88E229C8AC6330997C9C8FDBC9751BD3F11D958C3722235AB9A80368CAEA7A8BF81ECE937D7AC3E5993A62DBE47997242F2F7E5B0FB0E1C7C664489AADC545A67B1037FE67A12D84C98EB6944A42B883623B6B47642E015E1F33638855BE07B8D62AB4BF065B4B48394849078D4A82B601531771AF638A7BB9D254142E9DA3A093635A4C9660CEC15D650869297944D85870BD4394DCC6CB8C056AAF64AC8B05768ECF4F2DCD54579C1BD0852BF4AC0148560B0CB7BB563F3E3D1D286836F44969EB156A17DA0EEFD69C92A5EB2FEBA520F78F03F874779C2B505051DF9556E86C00A9442509DA49E1410349E787568B6131890DF02F2B9F750000000B01E0C6F11F1169B4A7E95D5048ECEDAC35F5488895B9D6DA0FA4202810ADA0EC35A3B3061932561524D834B3AB27D9E0698D21C35F9DE2AD48BBB7CA0DBC951E40F434CDDB4161A1D653A93D2DE6B1B5AF11949C1A2A8A507CA94E0E09F6477D30CB71D9432CA421B40CA948E1E1D28CCAC563A6FB3589FDAF58565F106B29BECDBDFE974FE225296662363AA56AC914127665D878562DA4AE468AD371D00CB50DAB56D48EDA81A5188C792954B73C618BF969290081D94FE2D15A4660E67BEEB7C6B1A7D53A6C7521C4E6CF982378B749D7931A3BD217D56D2555A2ED2BC4972DEFA696B2E13D9C3A1A48ABCC2AF61D47C2B00776BAD1FC43D2E99435CBC1EED0BAD956B2DD9C6A1B49912DA6F7A6F750ECA95152C4D534D9590948B85F0A71BF20D3816E93AE56729397B36568FA72B4FEC1B17BFDDD2D2E51F929A8E8DF25E4B74CB619690DA3AA80123C24D85CEE158A2F5ADBCBE6ACDFF00EAB0755B104F6823D32224742CAD11D94ACF108158E600FC99CB950D6DDDCB67439B368E37A8BA332B5A15296C8037041BFBEBF86B1242F54DA1A5206E74AF7FBABC45386B288F9B3BBD65ABB4F4B1FDB3B0349DC645FA18B2CA2039978ECAC4C5E0BBB48D97D9583FD7D9FF00BC3EC4EAD4E3AA5ACDD4A37E87FFC40029100100010303030402030101000000000001110021314151611071812091A1B130C1D1E1F0F140FFDA0008010100013F10FCE0B032D36BD74B69C8F6137A56A00C92E87777A82C152709D05827769D2D7B9F63F9A46C9B00FBB58E2CDE53EC547DD0BE1FDB41C2099E4F34E5A46241F4524D856723E655FC52A491A36144F10CD40C4961A5C0BFF8103389AB0DE8E169ED3DA981205B99E90B6F348CE90978FE14B61C15F4914ABF205FBE8D3D0A94B3D0E3F81769770BD97397E02682183E43CB1E2294727B5459448C1F25223088FE782179644A756382AA57C9FE734AE1D0F0A701E6841A16200E50FD7B5470BDCBE9DFD05429E19C96DC9A7BA2ACC52D45BBE8705A90953D65F35AD3FEB99A590583939461F10F15A997264EE64F3F9E17411A4473255DDB9BE3538D3C6F578EB43B9A352612C991EE56128D1979DAA72568E4EC7A0FF3C04C5986AE87347DCF7BCBAC9AAEAFA548290C636AC7B65C8D3BACB3232B35930F151AC466D20FEFF3CC91C50966E4F24FB563C096344FFBF1D1A6C29CB58E56553F01BAFC6DB92D589215DF9F161A0910DE1309E91A4CDA29F33334A674442CB680DA8854C0743C2EDBC7BD0282100D0FCE0719341D4A99198FBCDA64FBA69E8D4598425190EC4041DDA3C5B82951A60E51DDAA3E3B52228E4F427CED4F18FF0071508A74B9647DCD673FF804D4A38B5059803F0D34D47E461BF60AD946AA06E1BD452466E708EEBFAAB2938A163582A6777A1B4417B4508404404130FF00C248277E82EF8177C57B207167C957A5BF1A24CFA0F83DE8309CD60E1D0E0A6EEF1EB825B909A199214F63F0E714B2520DDA2A51BE056E096A589D2F5DAD52A33D0919F769379F44C1EF4895CE0F71A1286E36727605FDE2A69EC7647BB3F36C7484A6721AF6095ED45888919D64EAADFF0009631BDD8FE42B946162DDFC47AC15012D0471C8F60CBE29652EC840344313C2BDAA752C85BC7FE2A0133A73F9A3E01C400FAA4EEFBD2A6A65A33146488BB82F4E5E9BB25DB53DE895948C8215B268D0F0109B429EF441E5A5555CBF85500DEC3C539B9C696FD37699342E895CEC3DCE6A18E24207DA93D5489913A5DE017574A0FEA8371DB47BE3BD33BB64287CE7EB8A02103004051428F502ABA8AEEEBD8331B14A11200C06CEFB7BA928269B796034B0FC8B944B27FF000F15DE8A43C9AD4025299D5E162529513F4BBBD642AC6592CDB6B71A6A54C1026E2FD19AD4FE9D56EF35A945965D8DDA621FED4050052E66C78735ACD18C8EC9A3D0D1A2A1B3BD4F34B5BCA10E9114E98389E470D4FF00E47B1F402D3C157ADC184DC4D11B27E1289EBDE9A95790A51C2F23B1B79A020917BAE5776A73848E819A57822058E3777A12C6DA4081E68776F0320E1A9FA41C6193BECD61BD6E422644D1A69C1E605290A6D53DD14EC582B905A4C9521224524A4A6F522922B4B3B54EE99E4108D4C94B898ECD23C8CFF544090121713F036F227061F745B95C4087DD76EDA1FC5679B4929F45446DA8786A51E4BC5E51984B7EF8A8C0B0846F3A781F3489EBB4DE198A24320B3903483C8D4C2A4A7CCD00A4B060A597DE69CE00BDD97A839230195B07BA5199B32B5613E2D1531C9029E2FD2F31449AC5B8EF06C780A2C416363151862F7BB7933D9A8BE618658C8659D948A121381A28DBD9A3492C7BB254BBC94292C0466187B944DCA59CBFE7DA9A24FC6498A56248B9542F760F7A04762C3ACBA525502026850E3C127540D1A046D2B454B42CA124F6A72E4227A1C5044809A72C1ECDDF053088498961EF606AF050512C64795755CAEAFA0CB140488E89519BAED8014AE5DB0516E50404E66EA9146859863DCA9B889A276AD83EA92922BEBA4BDA1FEFF1C1A9B1F9B6190EEFDF54A42870B38AB1438400D8FE454E3B10FF009A619765E0DAC03C5EA43892D4AA039573DDAB53A1BA0B0E1603F9ACD1C4C659243819B76A8767DAA22904D9477D03BD04082D2C5F21158C6E001E16580F35255E63C4C7DC3429853B50503EC0FDD36AD1A0667B50146DEACA90E39068C0E7F143BF432DF3152AC253754FA8CA2CAA029E88A344D93D1160C837BBD0AE5E121981C4B0E4A5489372FB2682C64C80ABCB2D1328C00A7B21306CD1EC9A8EA60376EC163E569366ABDE4FE684245BF12BC8C9E32CFEAA0500B670450FA1405C987F403BD4CF6F28F7AFBF834EB02C03C9074791669747BC1CD8C5A934A7A8C5065C622F3E1838A8357E2C4BB1B183D02C2003EC99F32D12B97A62200EB6D2B548D8B7163DCADBD6912F183F15756FB8FC196AC3F11FCD4CAF49A52B50B97C96F04B436BFA8F698F913B560E6C31FDFA1A6AD7922786818BB16A38A686076D63E8AC2D08B30CA09EC4F9A9A7A427A30DBB0D1B85CC0C6C5DA684C5487C95939192811733667B73C52E6B2309E593B62AF84360DB27EF1587D473B2270C3367E28A13D80DA61FBC1AD1449DD2ED0DDF047750AEB8181B018EB1D5E854861441DA94CA50A46CAEAB513914657E8D0F531347030F63D80F3DE8503721835E7F9A70B66248EA27EA99413CACFD9F4A4461B3E832536A2B57F4A8242424AF03BFC6684A9180FE006EE5758F5C7E078E92244C952415959E636E392F4444491B2351565ECD31F559963FDE6823CB5310D61A3E830A3321D4BA4FF4AB81AD22063BEA2381DFF2BE99A5A88064DF8934DB21BEF460EA448D0346C1868D8B794F09FD534A825C81AFA04939A2ED9229CC98E0978A80023E023FBE87E6C7179CC09B6C05D5B06685426CEA4D34B57E252E63F717EE14980B7EB539375AF53A83177B1BF23C539F4991382F4C8109B710E07708F7A3F24F468BB065A6BC089D4C23915EC14B4BD09FCC0E497026CC51B17AB362717045F4A0BA5FB81446803032E45CB4719A26D2613D17789DCA38AC640334FB538887699CCB7744DA7D735353D66A7A26084F09FD531AADD9CC899795A9E93D0F68921BA65DBED404EAF3B86FF153577EECA2BBC1AFA08B60AD002EAB57153A1D4B7060B7EDB1320400401B1D00ACA5ED58A4175E8081873E89A9A9A9E934B53445424371CD397D2CD97B1113FF006A2D52499E215FBD3ACD030B14386DFBA72F7A9F4020196808A198B6F7247C73446CF80054F4BB44F801C3EEA232129773254FA96A6A6A6A6A66A5FC86214473E2D5282BA08B43367DEA76ABB504090CAB36C77A304BC800B021331AD16000384BE2EB1506C402D1D5E0BDF55A5BFA6226592C4C18FD79A8F63DE31876CBE881F77D885F74D25600BE1FD749A9A5A9E9353D27AAA8B716502437B53C09D614035DAA5D36095370388A8ACDD20E56C548A3D3591858C050002B042E9063B7AAEFA600C4998F8286E0083608A9A9A1658095E299DC9DDD894C4D1AF69FD7A67A4D4F49F464C0062F98A8FF00F9260110139B5A9AB865D03BAD74D0A5F6D3000272E5344461216360FF006DD1A9EB6AA660E40FE6B4A73D1254306F0B0D43C7824E1B25081958925CB7F57AADFA36E8DBD0E29E8B03DAA75809A7AFFFC40032110002010204030506070100000000000001020300110412213105104113202232510630618191B11433425271A1C1F0FFDA0008010201013F00EFA005803589B860B6D0578F61615918EED5D98EA4D1897D2961075B576D147A5C9FE2BF0F14BF966C7D0D4913466CC3BF0CFA76726DF6AC96391BA55CAEF57BED4C5635CEF52CEF2EFB7A72EC581006F5886CB1647376F711B67456EA34E445B514F1F6C997A8DB996CAAEE3A69EE625C91853B9D68024D853CF147A6E6BF1A4795451249B9E4CA5A3751BEFDC8E1793CA2861E34FCC6B9F8566897CA9F5AED97F60FA50923BDCA0A570FE2AC4C9D9A651B9FB7776371BD3AA3EAEBAFA8A38788ECD6FE452E16306E5AE29E62DE14D052F02E20D1F682136FEFE9BD302A72B6879E178370F970CA81487B0F17C48F4DADFE5710C2BA390775D0FCBBA401BB0AF07EE1F5A319B5C548731D36AF66E34476C532E62B602FEA7AFF005A50C5BE6BB6D5ED7080CC9227988D7E3E9586C0BCE331D05370A005D5B5AC171AEC90418AF091B1E84563F1D1CF8C695366FF0005AB130766732EC7BD86F046D27CAAE6B807168B02AE92FEAB7CAD53F1BC3019CBDCFC2833F13C419A4F28FF00AD4B6B694580DEA444986561714FC2A33E52453E0F247949B8A6194907BAFE18517E7CF0D8379CFA0F5A862585422D30B1AD0520B0D79620D909352E14C84BC66F7A65286CC2DDCC568557D0561F0B24E7C234F5A8387C716ADA9AB5B99B2EC285EDAF2750E0A9A9E37C2CA452BA4EB6715340D11D76E428C071188C9D2911635CAA34F738BC3ACE963B8A40639321A5716B30B8E51AE670B58440333F527DC13D2AF47515361927B31D0D4913466CDCA30C5C64DEB0CAC175A26DDEBD1366157E43734543687970CC383E33D7EDC9F6A1CAF57ABF26B11AD48E249028DA9E219C2AD5AC4D5F9605408C7CB93EC6877C69A8ACCD7CD7D79FFFC4002E110002010302040503040300000000000001020300041121311012204105223251811330610614A1F02391C1FFDA0008010301013F00EB3B559AA88F2A7535A5647B566813524EB1FA8D3DC17D947CD3C3247B8C8A041EB5668CF325238750EBDEB7E13CE53C89BD63B9DF81914024ED52B2BBE536FB168D82C9F3C6EA3E53F507CF1BB3A2A7BFD9B55CB17F8A670832D4F72EDE8D053176F5371BC182ADD04814B148FB0C50B53DDA85A2FB9A365ECD4A81005153BF3BE3B0E9650C30C29AD31E83FEE8C128ED9AFA721D31515BAC63277A6F17B20FF4CCA33FDEFB50C30C8A0280AB8F16BF8AE1A4E60533A2FE33EFEF48EAEA255F4B6A3E7A4DDA7615FBB5F6349728DA6683A8D0D78FB3CAAB6A8DCA18124FE0634FE75A6B28D63F2EFEF5FA4DE6681D24F483A7FDA9EF121381A9A5F12D7CCBA55F7833BC867B3F306DC7706BC3EC1E1B25824DC7F1939AD54F2B6FD5127D4900A118049AFD41E152DF9478775CFCE6A0F04BD7C465703F34B1A786DB8863DCFF00734C083AD04276A46784E41C1A5F1271B8CD49389FCD8C11435E9B25C966E33DD2423DCD4B2B4ADCCD564904CBCB268C28086DD73A0AB997EB4A5C708572401455A3D185039DBA2C47F8F353DC2423CDBD4D7D249A0D07428E6DCD1C674E0AECA722A1916E2306A7B4C6A9BD03D8EFC6398416C1BBD3B973CCDBFD9B59DA17C8DAB21D79854F6E24D46FC09C0CD5C31C2A7B0FB0077E0A706A19DE1381A8A8E5590647038C6B5211A0EF4064E051D3A95720D6382EAA281236E12363845EAA61AF1C56382120E94A9CA849DE94F9799AB3A0C7190F08877A7DFAF359D318E3FFD9, '', '2021-01-13 17:15:36', '', '2021-01-13 17:15:36', b'0');
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='定时任务调度表';

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
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `log_type` bigint(4) NOT NULL COMMENT '日志类型',
  `trace_id` varchar(64) NOT NULL DEFAULT '' COMMENT '链路追踪编号',
  `username` varchar(50) NOT NULL DEFAULT '' COMMENT '用户账号',
  `result` tinyint(4) NOT NULL COMMENT '登陆结果',
  `user_ip` varchar(50) NOT NULL COMMENT '用户 IP',
  `user_agent` varchar(200) NOT NULL COMMENT '浏览器 UA',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COMMENT='系统访问记录';

-- ----------------------------
-- Records of sys_login_log
-- ----------------------------
BEGIN;
INSERT INTO `sys_login_log` VALUES (1, 1, 'c112269e-fa2c-4616-94e2-29a7349aabdc', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-17 00:53:24', '', '2021-01-17 00:53:24', b'0');
INSERT INTO `sys_login_log` VALUES (2, 1, '398ffb66-1e40-4b81-8fbb-57ef32e736c4', 'admin', 31, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-17 01:04:13', '', '2021-01-17 01:04:13', b'0');
INSERT INTO `sys_login_log` VALUES (3, 1, 'b147be38-6612-4a32-8b1a-2ed628ecc387', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-17 01:04:22', '', '2021-01-17 01:04:22', b'0');
INSERT INTO `sys_login_log` VALUES (4, 1, 'aefef075-4acd-40f4-8a40-877dc6402815', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-18 05:31:18', '', '2021-01-18 05:31:18', b'0');
INSERT INTO `sys_login_log` VALUES (5, 2, 'db0e3b18-b671-46c9-b53a-b7e59eb36cd1', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-18 06:03:11', '', '2021-01-18 06:11:03', b'0');
INSERT INTO `sys_login_log` VALUES (6, 1, 'd82b6b97-903d-48c5-a4fe-4bac9d354720', 'admin', 31, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-18 07:27:51', '', '2021-01-18 07:27:51', b'0');
INSERT INTO `sys_login_log` VALUES (7, 1, '6c1a6de3-dc37-40aa-b777-4a60bdef40ab', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-18 07:27:56', '', '2021-01-18 07:27:56', b'0');
INSERT INTO `sys_login_log` VALUES (8, 1, '023dfde8-cbed-4438-b9f8-e048416a591e', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-19 02:39:05', '', '2021-01-19 02:39:05', b'0');
INSERT INTO `sys_login_log` VALUES (9, 1, '6e7e7bf7-e1fe-401e-bc1a-1aa5057a5bb4', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-20 10:52:51', '', '2021-01-20 10:52:51', b'0');
INSERT INTO `sys_login_log` VALUES (10, 1, '2af83bf4-1c84-462c-ae5c-d0b4565fe9e4', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-20 11:17:27', '', '2021-01-20 11:17:27', b'0');
INSERT INTO `sys_login_log` VALUES (11, 1, '23a4f5c4-1edc-4fbe-8254-f52ce8d247c8', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-20 13:37:23', '', '2021-01-20 13:37:23', b'0');
INSERT INTO `sys_login_log` VALUES (12, 1, 'a0260b40-e81f-4eac-9fd7-350883f9ed72', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-20 14:10:55', '', '2021-01-20 14:10:55', b'0');
INSERT INTO `sys_login_log` VALUES (13, 1, 'c6ed4c50-933d-498f-8b5d-d68a93b88a2f', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-20 17:10:19', '', '2021-01-20 17:10:19', b'0');
INSERT INTO `sys_login_log` VALUES (14, 1, '17622ede-63e3-4b67-aea0-b525248f709d', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-20 21:41:37', '', '2021-01-20 21:41:37', b'0');
INSERT INTO `sys_login_log` VALUES (15, 1, 'dbe5faa4-c0e0-420d-b7ab-2eb943fbe1a4', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-20 23:36:39', '', '2021-01-20 23:36:39', b'0');
INSERT INTO `sys_login_log` VALUES (16, 1, '0801b6de-9406-4acf-8dbf-b797448e0006', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-21 00:37:03', '', '2021-01-21 00:37:03', b'0');
INSERT INTO `sys_login_log` VALUES (17, 1, 'c52b9ef0-9054-4733-845a-b5b094dee5e3', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-21 01:18:25', '', '2021-01-21 01:18:25', b'0');
INSERT INTO `sys_login_log` VALUES (18, 1, 'bfda0442-06f9-45f6-91f3-7285aeee1063', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-21 02:10:57', '', '2021-01-21 02:10:57', b'0');
INSERT INTO `sys_login_log` VALUES (19, 1, '6caf92e4-2b84-4185-b661-c944ce5eb05d', 'test', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-21 02:25:38', '', '2021-01-21 02:25:38', b'0');
INSERT INTO `sys_login_log` VALUES (20, 1, 'd9659885-0bfd-4662-8656-afcdeb1983ff', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-21 03:02:57', '', '2021-01-21 03:02:57', b'0');
INSERT INTO `sys_login_log` VALUES (21, 1, '02dd6057-c59f-4c1e-a234-969449f99bcc', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-21 04:56:22', '', '2021-01-21 04:56:22', b'0');
INSERT INTO `sys_login_log` VALUES (22, 1, '69c26040-e9f4-45fa-98f9-a2391625f48f', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', '', '2021-01-21 06:17:16', '', '2021-01-21 06:17:16', b'0');
INSERT INTO `sys_login_log` VALUES (23, 1, '13814413-8a87-4fbf-9474-3225b033ff44', 'admin', 30, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', '', '2021-01-21 08:30:51', '', '2021-01-21 08:30:51', b'0');
INSERT INTO `sys_login_log` VALUES (24, 1, '6d84218d-90e9-40fa-acad-e9173813a26f', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', '', '2021-01-21 08:30:51', '', '2021-01-21 08:30:51', b'0');
INSERT INTO `sys_login_log` VALUES (25, 1, '85c2a039-0e6a-47bb-bdd8-72acac8b28e2', 'admin', 31, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', '', '2021-01-21 08:30:59', '', '2021-01-21 08:30:59', b'0');
INSERT INTO `sys_login_log` VALUES (26, 1, 'f44852b5-091b-4a4c-b9f7-99d4f9850cb5', 'admin', 31, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', '', '2021-01-21 08:31:05', '', '2021-01-21 08:31:05', b'0');
INSERT INTO `sys_login_log` VALUES (27, 1, '9830c3ea-9530-45af-b284-c6f8a5c6228d', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', '', '2021-01-21 08:31:22', '', '2021-01-21 08:31:22', b'0');
INSERT INTO `sys_login_log` VALUES (28, 1, '98eae309-5661-4de5-b6fa-60ab609b4549', 'admin', 31, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', '', '2021-01-21 09:57:09', '', '2021-01-21 09:57:09', b'0');
INSERT INTO `sys_login_log` VALUES (29, 1, 'ad9e244f-4c88-4762-b280-2d46d16081ac', 'admin', 31, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', '', '2021-01-21 09:57:15', '', '2021-01-21 09:57:15', b'0');
INSERT INTO `sys_login_log` VALUES (30, 1, '5032fac5-c6dc-41e4-882b-2dbea01ef1f6', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', '', '2021-01-21 09:57:18', '', '2021-01-21 09:57:18', b'0');
INSERT INTO `sys_login_log` VALUES (31, 1, '802ca4eb-ac96-4c6b-91de-0e445dc46e78', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', '', '2021-01-25 21:29:41', '', '2021-01-25 21:29:41', b'0');
INSERT INTO `sys_login_log` VALUES (32, 1, '3aeafd34-af33-4329-8b47-8972ecc72635', 'admin', 0, '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', '', '2021-01-25 23:59:57', '', '2021-01-25 23:59:57', b'0');
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
) ENGINE=InnoDB AUTO_INCREMENT=1068 DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
BEGIN;
INSERT INTO `sys_menu` VALUES (1, '系统管理', '', 1, 1, 0, '/system', 'system', NULL, 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:34:28', b'0');
INSERT INTO `sys_menu` VALUES (2, '基础设施', '', 1, 2, 0, '/infra', 'monitor', NULL, 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-20 14:18:35', b'0');
INSERT INTO `sys_menu` VALUES (3, '系统工具', '', 1, 3, 0, '/tool', 'tool', NULL, 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:34:28', b'0');
INSERT INTO `sys_menu` VALUES (4, '若依官网', '', 1, 4, 0, 'http://ruoyi.vip', 'guide', NULL, 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-20 21:54:28', b'1');
INSERT INTO `sys_menu` VALUES (100, '用户管理', 'system:user:list', 2, 1, 1, 'user', 'user', 'system/user/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (101, '角色管理', 'system:role:list', 2, 2, 1, 'role', 'peoples', 'system/role/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (102, '菜单管理', 'system:menu:list', 2, 3, 1, 'menu', 'tree-table', 'system/menu/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (103, '部门管理', 'system:dept:list', 2, 4, 1, 'dept', 'tree', 'system/dept/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (104, '岗位管理', 'system:post:list', 2, 5, 1, 'post', 'post', 'system/post/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (105, '字典管理', 'system:dict:list', 2, 6, 1, 'dict', 'dict', 'system/dict/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (106, '配置管理', 'infra:config:list', 2, 7, 2, 'config', 'edit', 'infra/config/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-20 14:35:39', b'0');
INSERT INTO `sys_menu` VALUES (107, '通知公告', 'system:notice:list', 2, 8, 1, 'notice', 'message', 'system/notice/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (108, '日志管理', '', 1, 9, 1, 'log', 'log', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:34:28', b'0');
INSERT INTO `sys_menu` VALUES (109, '在线用户', 'monitor:online:list', 2, 1, 2, 'online', 'online', 'monitor/online/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (110, '定时任务', 'monitor:job:list', 2, 2, 2, 'job', 'job', 'monitor/job/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (111, '数据监控', 'monitor:druid:list', 2, 3, 2, 'druid', 'druid', 'monitor/druid/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (112, '服务监控', 'monitor:server:list', 2, 4, 2, 'http://127.0.0.1:8080/admin', 'server', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-21 04:58:59', b'0');
INSERT INTO `sys_menu` VALUES (113, 'Redis 监控', '', 2, 5, 2, 'redis', 'redis', 'infra/redis/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-26 00:56:30', b'0');
INSERT INTO `sys_menu` VALUES (114, '表单构建', 'tool:build:list', 2, 1, 3, 'build', 'build', 'tool/build/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (115, '代码生成', 'tool:gen:list', 2, 2, 3, 'gen', 'code', 'tool/gen/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (116, '系统接口', 'tool:swagger:list', 2, 3, 3, 'swagger', 'swagger', 'tool/swagger/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:45', b'0');
INSERT INTO `sys_menu` VALUES (500, '操作日志', 'system:operate-log:list', 2, 1, 108, 'operate-log', 'form', 'system/operatelog/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-16 18:25:45', b'0');
INSERT INTO `sys_menu` VALUES (501, '登录日志', 'system:login-log:list', 2, 2, 108, 'login-log', 'logininfor', 'system/loginlog/index', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-18 05:29:58', b'0');
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
INSERT INTO `sys_menu` VALUES (1031, '配置查询', 'infra:config:query', 3, 1, 106, '', '', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-20 14:34:00', b'0');
INSERT INTO `sys_menu` VALUES (1032, '配置新增', 'infra:config:add', 3, 2, 106, '', '', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-20 14:34:05', b'0');
INSERT INTO `sys_menu` VALUES (1033, '配置修改', 'infra:config:edit', 3, 3, 106, '', '', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-20 14:33:52', b'0');
INSERT INTO `sys_menu` VALUES (1034, '配置删除', 'infra:config:remove', 3, 4, 106, '', '', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-20 14:34:12', b'0');
INSERT INTO `sys_menu` VALUES (1035, '配置导出', 'infra:config:export', 3, 5, 106, '', '', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-20 14:34:19', b'0');
INSERT INTO `sys_menu` VALUES (1036, '公告查询', 'system:notice:query', 3, 1, 107, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1037, '公告新增', 'system:notice:add', 3, 2, 107, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1038, '公告修改', 'system:notice:edit', 3, 3, 107, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1039, '公告删除', 'system:notice:remove', 3, 4, 107, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1040, '操作查询', 'system:operate-log:query', 3, 1, 500, '', '', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-16 18:28:10', b'0');
INSERT INTO `sys_menu` VALUES (1042, '日志导出', 'system:operate-log:export', 3, 2, 500, '', '', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-16 18:28:23', b'0');
INSERT INTO `sys_menu` VALUES (1043, '登录查询', 'system:login-log:query', 3, 1, 501, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-18 05:29:26', b'0');
INSERT INTO `sys_menu` VALUES (1045, '日志导出', 'system:login-log:export', 3, 3, 501, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-18 05:29:30', b'0');
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
INSERT INTO `sys_menu` VALUES (1058, '导入代码', 'tool:gen:import', 3, 2, 115, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-20 16:55:13', b'0');
INSERT INTO `sys_menu` VALUES (1059, '预览代码', 'tool:gen:preview', 3, 4, 115, '#', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 22:36:55', b'0');
INSERT INTO `sys_menu` VALUES (1060, '生成代码', 'tool:gen:code', 3, 5, 115, '', '#', '', 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-20 16:54:18', b'0');
INSERT INTO `sys_menu` VALUES (1063, '设置角色菜单权限', 'system:permission:assign-role-menu', 3, 6, 101, '', '', '', 0, '', '2021-01-06 17:53:44', '', '2021-01-06 17:55:23', b'0');
INSERT INTO `sys_menu` VALUES (1064, '设置角色数据权限', 'system:permission:assign-role-data-scope', 3, 7, 101, '', '', '', 0, '', '2021-01-06 17:56:31', '', '2021-01-06 17:56:31', b'0');
INSERT INTO `sys_menu` VALUES (1065, '设置用户角色', 'system:permission:assign-user-role', 3, 8, 101, '', '', '', 0, '', '2021-01-07 10:23:28', '', '2021-01-07 10:23:28', b'0');
INSERT INTO `sys_menu` VALUES (1066, '获得 Redis 监控信息', 'infra:redis:get-monitor-info', 3, 1, 113, '', '', '', 0, '', '2021-01-26 01:02:31', '', '2021-01-26 01:02:31', b'0');
INSERT INTO `sys_menu` VALUES (1067, '获得 Redis Key 列表', 'infra:redis:get-key-list', 3, 2, 113, '', '', '', 0, '', '2021-01-26 01:02:52', '', '2021-01-26 01:02:52', b'0');
COMMIT;

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(50) NOT NULL COMMENT '公告标题',
  `content` text NOT NULL COMMENT '公告内容',
  `notice_type` tinyint(4) NOT NULL COMMENT '公告类型（1通知 2公告）',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
BEGIN;
INSERT INTO `sys_notice` VALUES (1, '温馨提醒：2018-07-01 若依新版本发布啦', '新版本内容', 2, 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_notice` VALUES (2, '维护通知：2018-07-01 若依系统凌晨维护', '维护内容', 1, 0, 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_notice` VALUES (3, '1133', '<p>222333</p>', 1, 0, '', '2021-01-13 05:24:52', '', '2021-01-13 05:25:01', b'1');
COMMIT;

-- ----------------------------
-- Table structure for sys_operate_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operate_log`;
CREATE TABLE `sys_operate_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `trace_id` varchar(64) NOT NULL DEFAULT '' COMMENT '链路追踪编号',
  `user_id` bigint(20) NOT NULL COMMENT '用户编号',
  `module` varchar(50) NOT NULL COMMENT '模块标题',
  `name` varchar(50) NOT NULL COMMENT '操作名',
  `operate_type` bigint(4) NOT NULL DEFAULT '0' COMMENT '操作分类',
  `content` varchar(2000) NOT NULL DEFAULT '' COMMENT '操作内容',
  `exts` varchar(512) NOT NULL DEFAULT '' COMMENT '拓展字段',
  `request_method` varchar(16) DEFAULT '' COMMENT '请求方法名',
  `request_url` varchar(255) DEFAULT '' COMMENT '请求地址',
  `user_ip` varchar(50) DEFAULT NULL COMMENT '用户 IP',
  `user_agent` varchar(200) DEFAULT NULL COMMENT '浏览器 UA',
  `java_method` varchar(512) NOT NULL DEFAULT '' COMMENT 'Java 方法名',
  `java_method_args` varchar(8000) DEFAULT '' COMMENT 'Java 方法的参数',
  `start_time` datetime NOT NULL COMMENT '操作时间',
  `duration` int(11) NOT NULL COMMENT '执行时长',
  `result_code` int(11) NOT NULL DEFAULT '0' COMMENT '结果码',
  `result_msg` varchar(512) DEFAULT '' COMMENT '结果提示',
  `result_data` varchar(4000) DEFAULT '' COMMENT '结果数据',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=85 DEFAULT CHARSET=utf8mb4 COMMENT='操作日志记录';

-- ----------------------------
-- Records of sys_operate_log
-- ----------------------------
BEGIN;
INSERT INTO `sys_operate_log` VALUES (1, 'e232606c-09a3-4934-bb24-ed834f72eca3', 1, '认证 API', '获取登陆用户的权限信息', 1, '测试内容', '{\"orderId\": 1}', 'GET', '/api/get-permission-info', '127.0.0.1', 'Apache-HttpClient/4.5.12 (Java/11.0.9)', 'CommonResult cn.iocoder.dashboard.modules.system.controller.auth.SysAuthController.getPermissionInfo()', '[]', '2021-01-17 01:19:44', 48, 0, '', '{\"permissions\":[\"\",\"system:user:resetPwd\",\"system:post:list\",\"monitor:operlog:export\",\"monitor:druid:list\",\"system:menu:query\",\"system:dept:remove\",\"system:menu:list\",\"tool:gen:edit\",\"system:permission:assign-role-menu\",\"system:dict:edit\",\"monitor:logininfor:remove\",\"monitor:job:list\",\"system:user:query\",\"system:user:add\",\"system:notice:remove\",\"system:user:export\",\"system:role:remove\",\"monitor:job:edit\",\"tool:gen:query\",\"system:dept:query\",\"system:dict:list\",\"monitor:job:query\",\"monitor:online:forceLogout\",\"system:notice:list\",\"system:dict:query\",\"monitor:online:query\",\"system:notice:query\",\"system:notice:edit\",\"monitor:online:list\",\"tool:gen:import\",\"system:post:edit\",\"monitor:job:add\",\"monitor:logininfor:list\",\"tool:gen:list\",\"system:dict:export\",\"system:post:query\",\"system:post:remove\",\"system:config:edit\",\"system:user:remove\",\"system:config:list\",\"system:menu:add\",\"system:role:list\",\"system:user:import\",\"system:dict:remove\",\"system:user:edit\",\"system:post:export\",\"system:config:export\",\"system:role:edit\",\"monitor:online:batchLogout\",\"system:dept:list\",\"system:config:query\",\"monitor:operlog:remove\",\"monitor:operlog:list\",\"system:role:add\",\"system:menu:remove\",\"system:dict:add\",\"monitor:logininfor:query\",\"monitor:server:list\",\"tool:build:list\",\"monitor:logininfor:export\",\"system:permission:assign-role-data-scope\",\"tool:swagger:list\",\"system:dept:edit\",\"system:post:add\",\"monitor:job:changeStatus\",\"tool:gen:preview\",\"monitor:operlog:query\",\"system:user:list\",\"system:notice:add\",\"monitor:job:remove\",\"system:role:export\",\"monitor:cache:list\",\"system:config:add\",\"system:permission:assign-user-role\",\"tool:gen:code\",\"monitor:job:export\",\"tool:gen:remove\",\"system:role:query\",\"system:menu:edit\",\"system:dept:add\",\"system:config:remove\"],\"roles\":[\"admin\"],\"user\":{\"avatar\":\"http://127.0.0.1:8080/api/system/file/get/add5ec1891a7d97d2cc1d60847e16294.jpg\",\"nickname\":\"若依\"}}', '', '2021-01-16 13:42:03', '', '2021-01-16 22:23:32', b'0');
INSERT INTO `sys_operate_log` VALUES (5, 'e0bee2a4-b1fa-4fcd-a961-b10c6a504b58', 1, '', '导出操作日志', 5, '', '', 'GET', '/api/system/operate-log/export', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'void cn.iocoder.dashboard.modules.system.controller.logger.SysOperateLogController.exportOperateLog(HttpServletResponse,SysOperateLogExportReqVO)', '{\"reqVO\":{},\"response\":\"[ignore]\"}', '2021-01-17 22:13:22', 33, 0, '', 'null', '', '2021-01-16 23:14:05', '', '2021-01-16 23:14:05', b'0');
INSERT INTO `sys_operate_log` VALUES (6, '8b583eec-8170-437c-bf16-0ee51b449b5a', 1, '', '导出操作日志', 5, '', '', 'GET', '/api/system/operate-log/export', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'void cn.iocoder.dashboard.modules.system.controller.logger.SysOperateLogController.exportOperateLog(HttpServletResponse,SysOperateLogExportReqVO)', '{\"reqVO\":{},\"response\":\"[ignore]\"}', '2021-01-17 22:14:29', 11069, 0, '', 'null', '', '2021-01-16 23:15:24', '', '2021-01-16 23:15:24', b'0');
INSERT INTO `sys_operate_log` VALUES (7, '9b420bf7-ad94-41bc-b4c4-226e4b89c4c4', 1, '', '导出操作日志', 5, '', '', 'GET', '/api/system/operate-log/export', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'void cn.iocoder.dashboard.modules.system.controller.logger.SysOperateLogController.exportOperateLog(HttpServletResponse,SysOperateLogExportReqVO)', '{\"reqVO\":{},\"response\":\"[ignore]\"}', '2021-01-17 22:14:50', 41, 0, '', 'null', '', '2021-01-16 23:15:34', '', '2021-01-16 23:15:34', b'0');
INSERT INTO `sys_operate_log` VALUES (8, 'f66e473c-6367-404b-84a8-cbdac96aa3bc', 1, '', '导出操作日志', 5, '', '', 'GET', '/api/system/operate-log/export', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'void cn.iocoder.dashboard.modules.system.controller.logger.SysOperateLogController.exportOperateLog(HttpServletResponse,SysOperateLogExportReqVO)', '{\"reqVO\":{},\"response\":\"[ignore]\"}', '2021-01-17 22:15:17', 9377, 0, '', 'null', '', '2021-01-16 23:16:10', '', '2021-01-16 23:16:10', b'0');
INSERT INTO `sys_operate_log` VALUES (9, '1317cefe-5e97-43a3-801b-46a4409626d0', 1, '操作日志 API', '导出操作日志', 5, '', '', 'GET', '/api/system/operate-log/export', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'void cn.iocoder.dashboard.modules.system.controller.logger.SysOperateLogController.exportOperateLog(HttpServletResponse,SysOperateLogExportReqVO)', '{\"reqVO\":{},\"response\":\"[ignore]\"}', '2021-01-17 22:17:54', 3113, 0, '', 'null', '', '2021-01-16 23:18:40', '', '2021-01-16 23:18:40', b'0');
INSERT INTO `sys_operate_log` VALUES (10, 'a1f49d12-813a-4d3d-b3a1-149d8f33d681', 1, '字典类型 API', '新增字典类型', 2, '', '', 'POST', '/api/system/dict-type/create', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.dict.SysDictTypeController.createDictType(SysDictTypeCreateReqVO)', '{\"reqVO\":{\"name\":\"登陆结果\",\"remark\":\"登陆结果\",\"status\":0,\"type\":\"sys_login_result\"}}', '2021-01-18 20:56:12', 32, 0, '', '104', '', '2021-01-18 06:17:11', '', '2021-01-18 06:17:11', b'0');
INSERT INTO `sys_operate_log` VALUES (11, '7884d51c-32a0-4add-ab15-4109065b597a', 1, '字典数据 API', '新增字典数据', 2, '', '', 'POST', '/api/system/dict-data/create', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.dict.SysDictDataController.createDictData(SysDictDataCreateReqVO)', '{\"reqVO\":{\"dictType\":\"sys_login_result\",\"label\":\"成功\",\"remark\":\"登陆结果 - 成功\",\"sort\":\"0\",\"status\":0,\"value\":\"0\"}}', '2021-01-18 20:56:37', 16, 0, '', '39', '', '2021-01-18 06:17:36', '', '2021-01-18 06:17:36', b'0');
INSERT INTO `sys_operate_log` VALUES (12, 'b5007f11-5afb-48e8-a2a6-a3ca111139dd', 1, '字典数据 API', '新增字典数据', 2, '', '', 'POST', '/api/system/dict-data/create', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.dict.SysDictDataController.createDictData(SysDictDataCreateReqVO)', '{\"reqVO\":{\"dictType\":\"sys_login_result\",\"label\":\"账号或密码不正确\",\"remark\":\"登陆结果 - 账号或密码不正确\",\"sort\":\"10\",\"status\":0,\"value\":\"10\"}}', '2021-01-18 20:56:55', 12, 0, '', '40', '', '2021-01-18 06:17:54', '', '2021-01-18 06:17:54', b'0');
INSERT INTO `sys_operate_log` VALUES (13, '98db2bd1-1bf9-4623-8eeb-79bd3235fce4', 1, '参数配置', '新增参数配置', 2, '', '', 'POST', '/api/system/config/create', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.config.SysConfigController.createConfig(SysConfigCreateReqVO)', '{\"reqVO\":{\"group\":\"1\",\"key\":\"3\",\"name\":\"2\",\"remark\":\"5\",\"sensitive\":true,\"value\":\"4\"}}', '2021-01-19 13:41:26', 96, 500, 'MySQLSyntaxErrorException: You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near \'key = \'3\')\' at line 4', 'null', '', '2021-01-19 03:09:58', '', '2021-01-19 03:09:58', b'0');
INSERT INTO `sys_operate_log` VALUES (14, '40865690-f577-4523-8530-0997df098302', 1, '参数配置', '新增参数配置', 2, '', '', 'POST', '/api/system/config/create', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.config.SysConfigController.createConfig(SysConfigCreateReqVO)', '{\"reqVO\":{\"group\":\"1\",\"key\":\"3\",\"name\":\"2\",\"remark\":\"5\",\"sensitive\":true,\"value\":\"4\"}}', '2021-01-19 13:41:54', 22, 0, '', '4', '', '2021-01-19 03:10:26', '', '2021-01-19 03:10:26', b'0');
INSERT INTO `sys_operate_log` VALUES (15, 'c400f556-a9c8-4d77-ae7c-73da552de3e4', 1, '参数配置', '修改参数配置', 3, '', '', 'PUT', '/api/system/config/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.config.SysConfigController.edit(SysConfigUpdateReqVO)', '{\"reqVO\":{\"group\":\"1\",\"id\":4,\"name\":\"2\",\"remark\":\"5\",\"sensitive\":false,\"value\":\"4\"}}', '2021-01-19 13:43:54', 1956, 0, '', 'true', '', '2021-01-19 03:12:28', '', '2021-01-19 03:12:28', b'0');
INSERT INTO `sys_operate_log` VALUES (16, '023a9567-dabb-4db3-b830-ec0e923ef3fa', 1, '参数配置', '删除参数配置', 4, '', '', 'DELETE', '/api/system/config/delete', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.config.SysConfigController.deleteConfig(Long)', '{\"id\":4}', '2021-01-19 13:44:02', 7, 0, '', 'true', '', '2021-01-19 03:12:34', '', '2021-01-19 03:12:34', b'0');
INSERT INTO `sys_operate_log` VALUES (17, '18116d9f-8058-4338-b975-c627ece30261', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"icon\":\"monitor\",\"id\":2,\"name\":\"基础设施\",\"parentId\":0,\"path\":\"/monitor\",\"permission\":\"\",\"sort\":\"2\",\"type\":1}}', '2021-01-22 01:09:46', 33, 0, '', 'true', '', '2021-01-20 14:15:18', '', '2021-01-20 14:15:18', b'0');
INSERT INTO `sys_operate_log` VALUES (18, '4217ec3d-6376-4d19-9bb7-ba6b43d2b6fe', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"infra/config/index\",\"icon\":\"edit\",\"id\":106,\"name\":\"配置管理\",\"parentId\":2,\"path\":\"config\",\"permission\":\"infra:config:list\",\"sort\":\"7\",\"type\":2}}', '2021-01-22 01:10:27', 17, 0, '', 'true', '', '2021-01-20 14:15:59', '', '2021-01-20 14:15:59', b'0');
INSERT INTO `sys_operate_log` VALUES (19, 'a3aa5f2b-20dc-4f8c-af2b-03656de1c7b9', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"icon\":\"monitor\",\"id\":2,\"name\":\"基础设施\",\"parentId\":0,\"path\":\"/infra\",\"permission\":\"\",\"sort\":\"2\",\"type\":1}}', '2021-01-22 01:13:03', 42, 0, '', 'true', '', '2021-01-20 14:18:35', '', '2021-01-20 14:18:35', b'0');
INSERT INTO `sys_operate_log` VALUES (20, 'ebef2929-2cd7-4ce5-b77b-eaeb2da80eaf', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"infra/config/index\",\"icon\":\"edit\",\"id\":106,\"name\":\"配置管理\",\"parentId\":2,\"path\":\"config\",\"permission\":\"infra:config:list\",\"sort\":\"7\",\"type\":2}}', '2021-01-22 01:17:00', 29, 0, '', 'true', '', '2021-01-20 14:22:33', '', '2021-01-20 14:22:33', b'0');
INSERT INTO `sys_operate_log` VALUES (21, '20fbd6e6-8f48-4abd-947c-131657cd93b0', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"system/config/index\",\"icon\":\"edit\",\"id\":106,\"name\":\"配置管理\",\"parentId\":2,\"path\":\"config\",\"permission\":\"infra:config:list\",\"sort\":\"7\",\"type\":2}}', '2021-01-22 01:19:40', 55, 0, '', 'true', '', '2021-01-20 14:25:12', '', '2021-01-20 14:25:12', b'0');
INSERT INTO `sys_operate_log` VALUES (22, '77fcd83d-2cca-445c-9a51-1c53fa46c75c', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"system/config/index\",\"icon\":\"edit\",\"id\":106,\"name\":\"配置管理\",\"parentId\":1,\"path\":\"config\",\"permission\":\"infra:config:list\",\"sort\":\"7\",\"type\":2}}', '2021-01-22 01:20:33', 11, 0, '', 'true', '', '2021-01-20 14:26:05', '', '2021-01-20 14:26:05', b'0');
INSERT INTO `sys_operate_log` VALUES (23, 'd3deacfe-854e-4552-80e4-c2969b5130e5', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"system/config/index\",\"icon\":\"edit\",\"id\":106,\"name\":\"配置管理\",\"parentId\":2,\"path\":\"config\",\"permission\":\"infra:config:list\",\"sort\":\"7\",\"type\":2}}', '2021-01-22 01:21:36', 43, 0, '', 'true', '', '2021-01-20 14:27:09', '', '2021-01-20 14:27:09', b'0');
INSERT INTO `sys_operate_log` VALUES (24, '04ba56f0-e64b-4b68-9783-3533754d8667', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"infra/config/index\",\"icon\":\"edit\",\"id\":106,\"name\":\"配置管理\",\"parentId\":2,\"path\":\"config\",\"permission\":\"infra:config:list\",\"sort\":\"7\",\"type\":2}}', '2021-01-22 01:23:05', 43, 0, '', 'true', '', '2021-01-20 14:28:38', '', '2021-01-20 14:28:38', b'0');
INSERT INTO `sys_operate_log` VALUES (25, '5444cc0b-8c7e-4a8c-ad45-3768d9cad08d', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"\",\"icon\":\"#\",\"id\":1031,\"name\":\"参数查询\",\"parentId\":106,\"path\":\"#\",\"permission\":\"infra:config:query\",\"sort\":\"1\",\"type\":3}}', '2021-01-22 01:28:06', 34, 0, '', 'true', '', '2021-01-20 14:33:39', '', '2021-01-20 14:33:39', b'0');
INSERT INTO `sys_operate_log` VALUES (26, '5363c982-26b4-4419-a6f4-cab7395ebbaa', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"\",\"icon\":\"#\",\"id\":1032,\"name\":\"参数新增\",\"parentId\":106,\"path\":\"#\",\"permission\":\"infra:config:add\",\"sort\":\"2\",\"type\":3}}', '2021-01-22 01:28:11', 14, 0, '', 'true', '', '2021-01-20 14:33:44', '', '2021-01-20 14:33:44', b'0');
INSERT INTO `sys_operate_log` VALUES (27, '24b2288a-b512-441d-9690-b6db74d7d76a', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"\",\"icon\":\"#\",\"id\":1033,\"name\":\"配置修改\",\"parentId\":106,\"path\":\"#\",\"permission\":\"infra:config:edit\",\"sort\":\"3\",\"type\":3}}', '2021-01-22 01:28:19', 15, 0, '', 'true', '', '2021-01-20 14:33:52', '', '2021-01-20 14:33:52', b'0');
INSERT INTO `sys_operate_log` VALUES (28, '866a915b-e60e-4ed6-a60c-ffee9d0d9fa4', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"\",\"icon\":\"\",\"id\":1031,\"name\":\"配置查询\",\"parentId\":106,\"path\":\"\",\"permission\":\"infra:config:query\",\"sort\":\"1\",\"type\":3}}', '2021-01-22 01:28:27', 16, 0, '', 'true', '', '2021-01-20 14:34:00', '', '2021-01-20 14:34:00', b'0');
INSERT INTO `sys_operate_log` VALUES (29, '6b812f9a-6b56-4965-9592-33e6bcfb789a', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"\",\"icon\":\"\",\"id\":1032,\"name\":\"配置新增\",\"parentId\":106,\"path\":\"\",\"permission\":\"infra:config:add\",\"sort\":\"2\",\"type\":3}}', '2021-01-22 01:28:32', 14, 0, '', 'true', '', '2021-01-20 14:34:05', '', '2021-01-20 14:34:05', b'0');
INSERT INTO `sys_operate_log` VALUES (30, '3d6b8177-66b9-480f-bbba-90733b15219d', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"\",\"icon\":\"#\",\"id\":1034,\"name\":\"配置删除\",\"parentId\":106,\"path\":\"#\",\"permission\":\"infra:config:remove\",\"sort\":\"4\",\"type\":3}}', '2021-01-22 01:28:39', 14, 0, '', 'true', '', '2021-01-20 14:34:13', '', '2021-01-20 14:34:13', b'0');
INSERT INTO `sys_operate_log` VALUES (31, 'b937aa5e-b18a-4dd3-a931-70302fe8c0be', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"\",\"icon\":\"#\",\"id\":1035,\"name\":\"配置导出\",\"parentId\":106,\"path\":\"#\",\"permission\":\"infra:config:export\",\"sort\":\"5\",\"type\":3}}', '2021-01-22 01:28:46', 13, 0, '', 'true', '', '2021-01-20 14:34:19', '', '2021-01-20 14:34:19', b'0');
INSERT INTO `sys_operate_log` VALUES (32, '308cd0ba-ee57-439f-b1cd-d168ce97672c', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"tool/build/index\",\"icon\":\"build\",\"id\":114,\"name\":\"表单构建\",\"parentId\":3,\"path\":\"build\",\"permission\":\"tool:build:list\",\"sort\":\"1\",\"type\":2}}', '2021-01-22 20:50:59', 32, 0, '', 'true', '', '2021-01-20 17:10:54', '', '2021-01-20 17:10:54', b'0');
INSERT INTO `sys_operate_log` VALUES (33, '90bc8b4b-a2dc-484c-b6bc-dab700f600e4', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"tool/swagger/index\",\"icon\":\"swagger\",\"id\":116,\"name\":\"系统接口\",\"parentId\":3,\"path\":\"swagger\",\"permission\":\"tool:swagger:list\",\"sort\":\"3\",\"type\":2}}', '2021-01-22 20:51:15', 5588, 0, '', 'true', '', '2021-01-20 17:11:16', '', '2021-01-20 17:11:16', b'0');
INSERT INTO `sys_operate_log` VALUES (34, '787f584d-5675-422c-b1b9-a0fcf63e9369', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"tool/build/index\",\"icon\":\"build\",\"id\":114,\"name\":\"表单构建\",\"parentId\":3,\"path\":\"build\",\"permission\":\"tool:build:list\",\"sort\":\"1\",\"type\":2}}', '2021-01-22 21:03:48', 2512, 0, '', 'true', '', '2021-01-20 17:23:46', '', '2021-01-20 17:23:46', b'0');
INSERT INTO `sys_operate_log` VALUES (35, '8593c50c-4d50-48e6-a3b1-917b83ec6972', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"tool/build/index\",\"icon\":\"build\",\"id\":114,\"name\":\"表单构建\",\"parentId\":3,\"path\":\"build\",\"permission\":\"tool:build:list\",\"sort\":\"1\",\"type\":2}}', '2021-01-22 21:14:35', 187876, 0, '', 'true', '', '2021-01-20 17:37:39', '', '2021-01-20 17:37:39', b'0');
INSERT INTO `sys_operate_log` VALUES (36, 'a5b07322-a990-47ef-a227-9af6e4bc8315', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"tool/build/index\",\"icon\":\"build\",\"id\":114,\"name\":\"表单构建\",\"parentId\":3,\"path\":\"build\",\"permission\":\"tool:build:list\",\"sort\":\"1\",\"type\":2}}', '2021-01-22 21:19:42', 74, 0, '', 'true', '', '2021-01-20 17:39:39', '', '2021-01-20 17:39:39', b'0');
INSERT INTO `sys_operate_log` VALUES (37, '02c9934e-cfba-4455-9d55-7a0600ef02ff', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"tool/build/index\",\"icon\":\"build\",\"id\":114,\"name\":\"表单构建\",\"parentId\":3,\"path\":\"build\",\"permission\":\"tool:build:list\",\"sort\":\"1\",\"type\":2}}', '2021-01-22 21:19:56', 21, 0, '', 'true', '', '2021-01-20 17:40:57', '', '2021-01-20 17:40:57', b'0');
INSERT INTO `sys_operate_log` VALUES (38, 'c4b27f76-dd8d-4da8-a10c-46f11fe1d31f', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"tool/build/index\",\"icon\":\"build\",\"id\":114,\"name\":\"表单构建\",\"parentId\":3,\"path\":\"build\",\"permission\":\"tool:build:list\",\"sort\":\"1\",\"type\":2}}', '2021-01-22 21:21:06', 17, 0, '', 'true', '', '2021-01-20 17:41:16', '', '2021-01-20 17:41:16', b'0');
INSERT INTO `sys_operate_log` VALUES (39, '0313045c-4782-4748-9085-910cf58a797e', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"tool/build/index\",\"icon\":\"build\",\"id\":114,\"name\":\"表单构建\",\"parentId\":3,\"path\":\"build\",\"permission\":\"tool:build:list\",\"sort\":\"1\",\"type\":2}}', '2021-01-22 21:21:21', 13, 0, '', 'true', '', '2021-01-20 17:42:23', '', '2021-01-20 17:42:23', b'0');
INSERT INTO `sys_operate_log` VALUES (40, '29baeaf1-4442-47c5-bd6d-8f8ed7b196df', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"tool/build/index\",\"icon\":\"build\",\"id\":114,\"name\":\"表单构建\",\"parentId\":3,\"path\":\"build\",\"permission\":\"tool:build:list\",\"sort\":\"1\",\"type\":2}}', '2021-01-22 21:42:04', 4138, 0, '', 'true', '', '2021-01-20 18:02:07', '', '2021-01-20 18:02:07', b'0');
INSERT INTO `sys_operate_log` VALUES (41, 'b3c84534-96aa-4e1b-9a25-5187fa9457c8', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"tool/build/index\",\"icon\":\"build\",\"id\":114,\"name\":\"表单构建\",\"parentId\":3,\"path\":\"build\",\"permission\":\"tool:build:list\",\"sort\":\"1\",\"type\":2}}', '2021-01-22 21:44:09', 49, 0, '', 'true', '', '2021-01-20 18:04:08', '', '2021-01-20 18:04:08', b'0');
INSERT INTO `sys_operate_log` VALUES (42, '91b5c5a5-9f27-482e-8d01-60cdb17e38fd', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"tool/build/index\",\"icon\":\"build\",\"id\":114,\"name\":\"表单构建\",\"parentId\":3,\"path\":\"build\",\"permission\":\"tool:build:list\",\"sort\":\"1\",\"type\":2}}', '2021-01-22 21:47:16', 60, 0, '', 'true', '', '2021-01-20 18:07:14', '', '2021-01-20 18:07:14', b'0');
INSERT INTO `sys_operate_log` VALUES (43, '7d4c2903-4d40-44fb-9f09-3201a016a90d', 1, '字典数据 API', '修改字典数据', 2, '', '', 'POST', '/api/system/dict-data/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.dict.SysDictDataController.updateDictData(SysDictDataUpdateReqVO)', '{\"reqVO\":{\"dictType\":\"sys_user_sex\",\"id\":1,\"label\":\"男\",\"remark\":\"性别男\",\"sort\":\"1\",\"status\":0,\"value\":\"1\"}}', '2021-01-23 10:50:51', 27, 0, '', 'true', '', '2021-01-20 21:41:59', '', '2021-01-20 21:41:59', b'0');
INSERT INTO `sys_operate_log` VALUES (44, 'de4ddd97-85e1-4cb2-9167-84256e9c743e', 1, '字典数据 API', '修改字典数据', 2, '', '', 'POST', '/api/system/dict-data/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.dict.SysDictDataController.updateDictData(SysDictDataUpdateReqVO)', '{\"reqVO\":{\"dictType\":\"sys_user_sex\",\"id\":1,\"label\":\"男\",\"remark\":\"性别男\",\"sort\":\"1\",\"status\":0,\"value\":\"1\"}}', '2021-01-23 10:53:52', 60816, 0, '', 'true', '', '2021-01-20 21:46:01', '', '2021-01-20 21:46:01', b'0');
INSERT INTO `sys_operate_log` VALUES (45, 'bd8c93f1-0a83-4e73-8c57-efb651aad67d', 1, '菜单 API', '删除菜单', 2, '', '', 'POST', '/api/system/menu/delete', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.deleteMenu(Long)', '{\"id\":4}', '2021-01-23 11:03:18', 4581, 0, '', 'true', '', '2021-01-20 21:54:32', '', '2021-01-20 21:54:32', b'0');
INSERT INTO `sys_operate_log` VALUES (46, '5e30d224-c026-4a05-82a3-0069601f4326', 1, '部门 API', '修改部门', 2, '', '', 'POST', '/api/system/dept/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.dept.SysDeptController.updateDept(SysDeptUpdateReqVO)', '{\"reqVO\":{\"email\":\"ry@qq.com\",\"id\":108,\"leader\":\"若依\",\"name\":\"市场部门\",\"parentId\":102,\"phone\":\"15888888888\",\"sort\":\"1\",\"status\":0}}', '2021-01-23 11:26:21', 34, 0, '', 'true', '', '2021-01-20 22:17:32', '', '2021-01-20 22:17:32', b'0');
INSERT INTO `sys_operate_log` VALUES (47, '766b8aa6-9414-478a-8179-ee990be2aa73', 1, '部门 API', '修改部门', 2, '', '', 'POST', '/api/system/dept/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.dept.SysDeptController.updateDept(SysDeptUpdateReqVO)', '{\"reqVO\":{\"email\":\"ry@qq.com\",\"id\":109,\"leader\":\"若依\",\"name\":\"财务部门\",\"parentId\":102,\"phone\":\"15888888888\",\"sort\":\"2\",\"status\":0}}', '2021-01-23 11:28:30', 5086, 0, '', 'true', '', '2021-01-20 22:19:46', '', '2021-01-20 22:19:46', b'0');
INSERT INTO `sys_operate_log` VALUES (48, '3cd76e05-b37b-4d89-bc7d-6252052878d3', 1, '参数配置', '修改参数配置', 3, '', '', 'PUT', '/api/infra/config/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.infra.controller.config.InfConfigController.edit(InfConfigUpdateReqVO)', '{\"reqVO\":{\"group\":\"ui\",\"id\":1,\"name\":\"主框架页-默认皮肤样式名称\",\"remark\":\"蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow\",\"sensitive\":false,\"value\":\"skin-blue\"}}', '2021-01-23 13:23:23', 31, 0, '', 'true', '', '2021-01-20 23:37:02', '', '2021-01-20 23:37:02', b'0');
INSERT INTO `sys_operate_log` VALUES (49, 'b89748ba-6f98-4abf-b26a-af9e0425f234', 1, '角色 API', '修改角色', 2, '', '', 'POST', '/api/system/role/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysRoleController.update(SysRoleUpdateReqVO)', '{\"reqVO\":{\"code\":\"test\",\"id\":101,\"name\":\"测试账号\",\"remark\":\"132\",\"sort\":\"0\",\"type\":2}}', '2021-01-23 18:16:08', 5314, 0, '', 'true', '', '2021-01-21 00:37:30', '', '2021-01-21 00:37:30', b'0');
INSERT INTO `sys_operate_log` VALUES (50, '369f88d5-b425-4918-b994-2a893165c704', 1, '角色 API', '修改角色', 2, '', '', 'POST', '/api/system/role/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysRoleController.update(SysRoleUpdateReqVO)', '{\"reqVO\":{\"code\":\"test\",\"id\":101,\"name\":\"测试账号\",\"remark\":\"132\",\"sort\":\"0\",\"type\":2}}', '2021-01-23 18:16:51', 3946, 0, '', 'true', '', '2021-01-21 00:38:11', '', '2021-01-21 00:38:11', b'0');
INSERT INTO `sys_operate_log` VALUES (51, 'ffa6bc01-3f1c-44c7-a632-087fa66c754e', 1, '用户 API', '新增用户', 2, '', '', 'POST', '/api/system/user/create', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.user.SysUserController.createUser(SysUserCreateReqVO)', '{\"reqVO\":{\"nickname\":\"测试号\",\"password\":\"buzhidao\",\"postIds\":[],\"username\":\"test\"}}', '2021-01-23 20:32:14', 135, 0, '', '104', '', '2021-01-21 02:13:53', '', '2021-01-21 02:13:53', b'0');
INSERT INTO `sys_operate_log` VALUES (52, '4b3982fc-b52c-4f72-aaf6-9a0a3cb1ad3e', 1, '用户 API', '修改用户', 2, '', '', 'POST', '/api/system/user/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.user.SysUserController.updateUser(SysUserUpdateReqVO)', '{\"reqVO\":{\"avatar\":\"\",\"deptId\":100,\"email\":\"\",\"id\":104,\"mobile\":\"15601691200\",\"nickname\":\"测试号\",\"postIds\":[],\"sex\":1,\"username\":\"test\"}}', '2021-01-23 20:32:34', 25, 0, '', 'true', '', '2021-01-21 02:14:13', '', '2021-01-21 02:14:13', b'0');
INSERT INTO `sys_operate_log` VALUES (53, 'c2390db2-3239-4c20-94d2-e92025101128', 1, '权限 API', '赋予用户角色', 2, '', '', 'POST', '/api/system/permission/assign-user-role', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysPermissionController.assignUserRole(SysPermissionAssignUserRoleReqVO)', '{\"reqVO\":{\"roleIds\":[101],\"userId\":104}}', '2021-01-23 20:32:53', 15, 0, '', 'true', '', '2021-01-21 02:14:32', '', '2021-01-21 02:14:32', b'0');
INSERT INTO `sys_operate_log` VALUES (54, 'dde4892a-a721-46c3-8fc4-3101d5b0a191', 1, '权限 API', '赋予角色菜单', 2, '', '', 'POST', '/api/system/permission/assign-role-menu', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysPermissionController.assignRoleMenu(SysPermissionAssignRoleMenuReqVO)', '{\"reqVO\":{\"menuIds\":[1001],\"roleId\":101}}', '2021-01-23 20:33:22', 27, 0, '', 'true', '', '2021-01-21 02:15:01', '', '2021-01-21 02:15:01', b'0');
INSERT INTO `sys_operate_log` VALUES (55, '36a58418-375f-4e7a-b0a9-8161358537ab', 1, '权限 API', '赋予角色数据权限', 2, '', '', 'POST', '/api/system/permission/assign-role-data-scope', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysPermissionController.assignRoleDataScope(SysPermissionAssignRoleDataScopeReqVO)', '{\"reqVO\":{\"dataScope\":2,\"dataScopeDeptIds\":[104],\"roleId\":101}}', '2021-01-23 20:33:47', 13, 0, '', 'true', '', '2021-01-21 02:15:26', '', '2021-01-21 02:15:26', b'0');
INSERT INTO `sys_operate_log` VALUES (56, 'f3b54afc-4427-4eeb-979c-c274d5566019', 1, '权限 API', '赋予角色菜单', 2, '', '', 'POST', '/api/system/permission/assign-role-menu', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysPermissionController.assignRoleMenu(SysPermissionAssignRoleMenuReqVO)', '{\"reqVO\":{\"menuIds\":[1001],\"roleId\":101}}', '2021-01-23 20:55:28', 35, 0, '', 'true', '', '2021-01-21 02:37:09', '', '2021-01-21 02:37:09', b'0');
INSERT INTO `sys_operate_log` VALUES (57, '96543ff3-30e6-4ceb-8ca1-fe39ba235bf6', 1, '权限 API', '赋予角色菜单', 2, '', '', 'POST', '/api/system/permission/assign-role-menu', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysPermissionController.assignRoleMenu(SysPermissionAssignRoleMenuReqVO)', '{\"reqVO\":{\"menuIds\":[1001],\"roleId\":101}}', '2021-01-23 20:57:48', 20, 0, '', 'true', '', '2021-01-21 02:39:29', '', '2021-01-21 02:39:29', b'0');
INSERT INTO `sys_operate_log` VALUES (58, '9a31bdda-cb5f-4b87-b30d-c5139e75f24b', 1, '权限 API', '赋予角色菜单', 2, '', '', 'POST', '/api/system/permission/assign-role-menu', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysPermissionController.assignRoleMenu(SysPermissionAssignRoleMenuReqVO)', '{\"reqVO\":{\"menuIds\":[1,100,1001],\"roleId\":101}}', '2021-01-23 20:58:05', 19, 0, '', 'true', '', '2021-01-21 02:39:45', '', '2021-01-21 02:39:45', b'0');
INSERT INTO `sys_operate_log` VALUES (59, 'f5c66c24-7aca-468d-8507-641599166c78', 1, '权限 API', '赋予角色菜单', 2, '', '', 'POST', '/api/system/permission/assign-role-menu', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysPermissionController.assignRoleMenu(SysPermissionAssignRoleMenuReqVO)', '{\"reqVO\":{\"menuIds\":[1,100,1001],\"roleId\":101}}', '2021-01-23 21:40:13', 8370, 0, '', 'true', '', '2021-01-21 03:03:21', '', '2021-01-21 03:03:21', b'0');
INSERT INTO `sys_operate_log` VALUES (60, 'b259a76d-5771-47ed-8a4a-74728ff32b96', 1, '权限 API', '赋予角色菜单', 2, '', '', 'POST', '/api/system/permission/assign-role-menu', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysPermissionController.assignRoleMenu(SysPermissionAssignRoleMenuReqVO)', '{\"reqVO\":{\"menuIds\":[1,100,1001],\"roleId\":101}}', '2021-01-23 21:40:42', 10361, 0, '', 'true', '', '2021-01-21 03:03:51', '', '2021-01-21 03:03:51', b'0');
INSERT INTO `sys_operate_log` VALUES (61, '3ff17962-16e0-412b-8b91-aaf356b13caf', 1, '权限 API', '赋予角色菜单', 2, '', '', 'POST', '/api/system/permission/assign-role-menu', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysPermissionController.assignRoleMenu(SysPermissionAssignRoleMenuReqVO)', '{\"reqVO\":{\"menuIds\":[1,100,1001],\"roleId\":101}}', '2021-01-23 21:41:25', 2828, 0, '', 'true', '', '2021-01-21 03:04:27', '', '2021-01-21 03:04:27', b'0');
INSERT INTO `sys_operate_log` VALUES (62, '70de67ba-c36a-43b3-9575-682f6e80d4b2', 1, '权限 API', '赋予角色菜单', 2, '', '', 'POST', '/api/system/permission/assign-role-menu', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysPermissionController.assignRoleMenu(SysPermissionAssignRoleMenuReqVO)', '{\"reqVO\":{\"menuIds\":[1024,1025,1,1026,1027,1028,1029,1030,1036,1037,1038,1039,1040,1042,1043,1045,100,1063,1064,1065,1007,1008,1009,1010,1011,1012,1013,1014,1015,1016,1017,1018,1019,1020,1021,1022,1023],\"roleId\":101}}', '2021-01-23 21:41:51', 1686, 0, '', 'true', '', '2021-01-21 03:04:52', '', '2021-01-21 03:04:52', b'0');
INSERT INTO `sys_operate_log` VALUES (63, 'e86ac5b7-bb6f-486a-9f98-3525bd31c62a', 1, '权限 API', '赋予角色菜单', 2, '', '', 'POST', '/api/system/permission/assign-role-menu', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysPermissionController.assignRoleMenu(SysPermissionAssignRoleMenuReqVO)', '{\"reqVO\":{\"menuIds\":[1,100,1001,1002],\"roleId\":101}}', '2021-01-23 21:44:44', 2928, 0, '', 'true', '', '2021-01-21 03:07:46', '', '2021-01-21 03:07:46', b'0');
INSERT INTO `sys_operate_log` VALUES (64, '68ee41a8-d7f4-4fbf-9234-11713cf4ea1e', 1, '权限 API', '赋予角色菜单', 2, '', '', 'POST', '/api/system/permission/assign-role-menu', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysPermissionController.assignRoleMenu(SysPermissionAssignRoleMenuReqVO)', '{\"reqVO\":{\"menuIds\":[1,100,1001],\"roleId\":101}}', '2021-01-23 21:47:08', 11, 0, '', 'true', '', '2021-01-21 03:10:08', '', '2021-01-21 03:10:08', b'0');
INSERT INTO `sys_operate_log` VALUES (65, 'e5ae7cb4-0a91-4cf6-810a-9215ccf5cf74', 1, '权限 API', '赋予角色菜单', 2, '', '', 'POST', '/api/system/permission/assign-role-menu', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysPermissionController.assignRoleMenu(SysPermissionAssignRoleMenuReqVO)', '{\"reqVO\":{\"menuIds\":[1,100,1001,1002],\"roleId\":101}}', '2021-01-23 21:48:17', 9, 0, '', 'true', '', '2021-01-21 03:11:17', '', '2021-01-21 03:11:17', b'0');
INSERT INTO `sys_operate_log` VALUES (66, '246c0e9d-8232-4944-9d5e-b245b0baea2d', 1, '权限 API', '赋予角色菜单', 2, '', '', 'POST', '/api/system/permission/assign-role-menu', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysPermissionController.assignRoleMenu(SysPermissionAssignRoleMenuReqVO)', '{\"reqVO\":{\"menuIds\":[],\"roleId\":101}}', '2021-01-23 21:50:11', 11, 0, '', 'true', '', '2021-01-21 03:13:11', '', '2021-01-21 03:13:11', b'0');
INSERT INTO `sys_operate_log` VALUES (67, 'aaa9076d-92de-4a7b-bedc-89922c9c30d0', 1, '权限 API', '赋予角色菜单', 2, '', '', 'POST', '/api/system/permission/assign-role-menu', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysPermissionController.assignRoleMenu(SysPermissionAssignRoleMenuReqVO)', '{\"reqVO\":{\"menuIds\":[1,100,1001],\"roleId\":101}}', '2021-01-23 21:50:21', 13, 0, '', 'true', '', '2021-01-21 03:13:21', '', '2021-01-21 03:13:21', b'0');
INSERT INTO `sys_operate_log` VALUES (68, '3fb4e653-ba5b-4953-85a1-30ccd0884963', 1, '权限 API', '赋予角色菜单', 2, '', '', 'POST', '/api/system/permission/assign-role-menu', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysPermissionController.assignRoleMenu(SysPermissionAssignRoleMenuReqVO)', '{\"reqVO\":{\"menuIds\":[1008,1009,1,1010,1011,1012,101,1063,1064,1065],\"roleId\":101}}', '2021-01-23 21:50:40', 28, 0, '', 'true', '', '2021-01-21 03:13:40', '', '2021-01-21 03:13:40', b'0');
INSERT INTO `sys_operate_log` VALUES (69, 'c85d8da3-2dd8-4922-9511-c60a71fc13e0', 1, '权限 API', '赋予角色菜单', 2, '', '', 'POST', '/api/system/permission/assign-role-menu', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysPermissionController.assignRoleMenu(SysPermissionAssignRoleMenuReqVO)', '{\"reqVO\":{\"menuIds\":[1008,1009,1,1010,1011,1012,101,1063,1064,1065],\"roleId\":101}}', '2021-01-23 22:00:02', 40, 0, '', 'true', '', '2021-01-21 03:23:02', '', '2021-01-21 03:23:02', b'0');
INSERT INTO `sys_operate_log` VALUES (70, 'fdb42076-927f-4274-bf0c-2e5b5c6ba51c', 1, '权限 API', '赋予角色菜单', 2, '', '', 'POST', '/api/system/permission/assign-role-menu', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysPermissionController.assignRoleMenu(SysPermissionAssignRoleMenuReqVO)', '{\"reqVO\":{\"menuIds\":[1,100,1001,1002,1003,1004,1005,1006,1007],\"roleId\":101}}', '2021-01-23 22:00:14', 41, 0, '', 'true', '', '2021-01-21 03:23:14', '', '2021-01-21 03:23:14', b'0');
INSERT INTO `sys_operate_log` VALUES (71, '60387620-e304-4180-87ab-ad5131a50dbf', 1, '权限 API', '赋予角色菜单', 2, '', '', 'POST', '/api/system/permission/assign-role-menu', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysPermissionController.assignRoleMenu(SysPermissionAssignRoleMenuReqVO)', '{\"reqVO\":{\"menuIds\":[1008,1009,1,1010,1011,1012,101,1063,1064,1065],\"roleId\":101}}', '2021-01-23 22:00:27', 44, 0, '', 'true', '', '2021-01-21 03:23:27', '', '2021-01-21 03:23:27', b'0');
INSERT INTO `sys_operate_log` VALUES (72, 'c78c1b32-66ee-456a-bf42-02a863181338', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"http://127.0.0.1:8080/admin\",\"icon\":\"server\",\"id\":112,\"name\":\"服务监控\",\"parentId\":2,\"path\":\"server\",\"permission\":\"monitor:server:list\",\"sort\":\"4\",\"type\":2}}', '2021-01-23 23:34:51', 36, 0, '', 'true', '', '2021-01-21 04:57:58', '', '2021-01-21 04:57:58', b'0');
INSERT INTO `sys_operate_log` VALUES (73, 'bdf5b5c3-6b10-4f6d-9131-bca51df74855', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"component\":\"\",\"icon\":\"server\",\"id\":112,\"name\":\"服务监控\",\"parentId\":2,\"path\":\"http://127.0.0.1:8080/admin\",\"permission\":\"monitor:server:list\",\"sort\":\"4\",\"type\":2}}', '2021-01-23 23:35:52', 14, 0, '', 'true', '', '2021-01-21 04:58:59', '', '2021-01-21 04:58:59', b'0');
INSERT INTO `sys_operate_log` VALUES (74, '8502967e-48a6-4b1b-978e-a6fab0d4dc48', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"icon\":\"tool\",\"id\":3,\"name\":\"系统工具\",\"parentId\":0,\"path\":\"/tool\",\"permission\":\"\",\"sort\":\"3\",\"type\":1}}', '2021-01-24 10:30:53', 32, 0, '', 'true', '', '2021-01-21 08:56:19', '', '2021-01-21 08:56:19', b'0');
INSERT INTO `sys_operate_log` VALUES (75, 'b1e1ac23-ff33-4d8b-ae7e-dcf58edf01eb', 1, '用户 API', '修改用户', 2, '', '', 'POST', '/api/system/user/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.user.SysUserController.updateUser(SysUserUpdateReqVO)', '{\"reqVO\":{\"username\":\"test\",\"nickname\":\"测试号\",\"remark\":null,\"deptId\":100,\"postIds\":[],\"email\":\"\",\"mobile\":\"15601691200\",\"sex\":1,\"avatar\":\"\",\"id\":104}}', '2021-01-24 12:14:07', 34, 0, '', 'true', '', '2021-01-21 10:39:40', '', '2021-01-21 10:39:40', b'0');
INSERT INTO `sys_operate_log` VALUES (76, '815f8631-e297-4a6c-b31a-8f6e54c6e7a2', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"name\":\"Redis 监控\",\"permission\":\"\",\"type\":2,\"sort\":\"5\",\"parentId\":2,\"path\":\"cache\",\"icon\":\"redis\",\"component\":\"monitor/cache/index\",\"id\":113}}', '2021-01-28 01:18:11', 52, 500, 'InvalidDefinitionException: No serializer found for class cn.iocoder.dashboard.modules.system.mq.message.permission.SysMenuRefreshMessage and no properties discovered to create BeanSerializer (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS)', 'null', '', '2021-01-26 00:51:20', '', '2021-01-26 00:51:20', b'0');
INSERT INTO `sys_operate_log` VALUES (77, 'd6f87cd1-824e-4f65-8ba3-51212fe6e4ff', 1, '字典类型 API', '新增字典类型', 2, '', '', 'POST', '/api/system/dict-type/create', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.dict.SysDictTypeController.createDictType(SysDictTypeCreateReqVO)', '{\"reqVO\":{\"name\":\"Redis 超时类型\",\"status\":0,\"remark\":\"RedisKeyDefine.TimeoutTypeEnum\",\"type\":\"inf_redis_timeout_type\"}}', '2021-01-28 01:19:41', 19, 0, '', '105', '', '2021-01-26 00:52:50', '', '2021-01-26 00:52:50', b'0');
INSERT INTO `sys_operate_log` VALUES (78, '7ed80010-a788-49c6-b41a-567930a63225', 1, '字典数据 API', '新增字典数据', 2, '', '', 'POST', '/api/system/dict-data/create', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.dict.SysDictDataController.createDictData(SysDictDataCreateReqVO)', '{\"reqVO\":{\"sort\":\"1\",\"label\":\"永不超时\",\"value\":\"1\",\"dictType\":\"inf_redis_timeout_type\",\"status\":0,\"remark\":\"Redis 未设置超时的情况\"}}', '2021-01-28 01:20:08', 18, 500, 'InvalidDefinitionException: No serializer found for class cn.iocoder.dashboard.modules.system.mq.message.dict.SysDictDataRefreshMessage and no properties discovered to create BeanSerializer (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS)', 'null', '', '2021-01-26 00:53:17', '', '2021-01-26 00:53:17', b'0');
INSERT INTO `sys_operate_log` VALUES (79, '6e92720f-7757-4cb7-99e2-f6d3b83b7bbb', 1, '字典数据 API', '新增字典数据', 2, '', '', 'POST', '/api/system/dict-data/create', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.dict.SysDictDataController.createDictData(SysDictDataCreateReqVO)', '{\"reqVO\":{\"sort\":\"1\",\"label\":\"永不超时\",\"value\":\"1\",\"dictType\":\"inf_redis_timeout_type\",\"status\":0,\"remark\":\"Redis 未设置超时的情况\"}}', '2021-01-28 01:21:26', 33, 500, 'ServiceException: 已经存在该值的字典数据', 'null', '', '2021-01-26 00:54:35', '', '2021-01-26 00:54:35', b'0');
INSERT INTO `sys_operate_log` VALUES (80, 'd204de50-32b5-4cf0-b163-768fb57ef38b', 1, '字典数据 API', '新增字典数据', 2, '', '', 'POST', '/api/system/dict-data/create', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.dict.SysDictDataController.createDictData(SysDictDataCreateReqVO)', '{\"reqVO\":{\"sort\":\"1\",\"label\":\"动态超时\",\"value\":\"2\",\"dictType\":\"inf_redis_timeout_type\",\"status\":0,\"remark\":\"程序里动态传入超时时间，无法固定\"}}', '2021-01-28 01:21:51', 24, 0, '', '48', '', '2021-01-26 00:55:00', '', '2021-01-26 00:55:00', b'0');
INSERT INTO `sys_operate_log` VALUES (81, '78c517c7-191d-46d8-bdc3-5dbeb06be26d', 1, '字典数据 API', '新增字典数据', 2, '', '', 'POST', '/api/system/dict-data/create', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.dict.SysDictDataController.createDictData(SysDictDataCreateReqVO)', '{\"reqVO\":{\"sort\":\"3\",\"label\":\"固定超时\",\"value\":\"3\",\"dictType\":\"inf_redis_timeout_type\",\"status\":0,\"remark\":\"Redis 设置了过期时间\"}}', '2021-01-28 01:22:17', 21, 0, '', '49', '', '2021-01-26 00:55:26', '', '2021-01-26 00:55:26', b'0');
INSERT INTO `sys_operate_log` VALUES (82, 'b8c971f3-fd22-4ed3-b5f9-ffc9a8d34859', 1, '菜单 API', '修改菜单', 2, '', '', 'POST', '/api/system/menu/update', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.updateMenu(SysMenuUpdateReqVO)', '{\"reqVO\":{\"name\":\"Redis 监控\",\"permission\":\"\",\"type\":2,\"sort\":\"5\",\"parentId\":2,\"path\":\"redis\",\"icon\":\"redis\",\"component\":\"infra/redis/index\",\"id\":113}}', '2021-01-28 01:23:21', 29, 0, '', 'true', '', '2021-01-26 00:56:30', '', '2021-01-26 00:56:30', b'0');
INSERT INTO `sys_operate_log` VALUES (83, 'e0922d52-0007-4105-a487-692d00d091c3', 1, '菜单 API', '创建菜单', 2, '', '', 'POST', '/api/system/menu/create', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.createMenu(SysMenuCreateReqVO)', '{\"reqVO\":{\"name\":\"获得 Redis 监控信息\",\"permission\":\"infra:redis:get-monitor-info\",\"type\":3,\"sort\":\"1\",\"parentId\":113,\"path\":null,\"icon\":null,\"component\":null}}', '2021-01-28 01:29:22', 14, 0, '', '1066', '', '2021-01-26 01:02:31', '', '2021-01-26 01:02:31', b'0');
INSERT INTO `sys_operate_log` VALUES (84, 'f44a431b-395e-45d9-9e6c-3fbdf7617155', 1, '菜单 API', '创建菜单', 2, '', '', 'POST', '/api/system/menu/create', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36', 'CommonResult cn.iocoder.dashboard.modules.system.controller.permission.SysMenuController.createMenu(SysMenuCreateReqVO)', '{\"reqVO\":{\"name\":\"获得 Redis Key 列表\",\"permission\":\"infra:redis:get-key-list\",\"type\":3,\"sort\":\"2\",\"parentId\":113,\"path\":null,\"icon\":null,\"component\":null}}', '2021-01-28 01:29:42', 11, 0, '', '1067', '', '2021-01-26 01:02:52', '', '2021-01-26 01:02:52', b'0');
COMMIT;

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `code` varchar(64) NOT NULL COMMENT '岗位编码',
  `name` varchar(50) NOT NULL COMMENT '岗位名称',
  `sort` int(4) NOT NULL COMMENT '显示顺序',
  `status` tinyint(4) NOT NULL COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COMMENT='岗位信息表';

-- ----------------------------
-- Records of sys_post
-- ----------------------------
BEGIN;
INSERT INTO `sys_post` VALUES (1, 'ceo', '董事长', 1, 0, '', 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_post` VALUES (2, 'se', '项目经理', 2, 0, '', 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_post` VALUES (3, 'hr', '人力资源', 3, 0, '', 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_post` VALUES (4, 'user', '普通员工', 4, 0, '', 'admin', '2021-01-05 17:03:48', '', '2021-01-05 17:03:48', b'0');
INSERT INTO `sys_post` VALUES (5, 'test', '测试岗位', 0, 1, '132', '', '2021-01-07 15:07:44', '', '2021-01-07 15:10:35', b'1');
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
INSERT INTO `sys_role` VALUES (101, '测试账号', 'test', 0, 2, '[104]', 0, 2, '132', '', '2021-01-06 13:49:35', '', '2021-01-21 02:15:26', b'0');
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
) ENGINE=InnoDB AUTO_INCREMENT=239 DEFAULT CHARSET=utf8mb4 COMMENT='角色和菜单关联表';

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
INSERT INTO `sys_role_menu` VALUES (169, 101, 1001, '', '2021-01-21 02:15:01', '', '2021-01-21 03:04:50', b'1');
INSERT INTO `sys_role_menu` VALUES (170, 101, 1, '', '2021-01-21 02:39:45', '', '2021-01-21 03:13:11', b'1');
INSERT INTO `sys_role_menu` VALUES (171, 101, 100, '', '2021-01-21 02:39:45', '', '2021-01-21 03:13:11', b'1');
INSERT INTO `sys_role_menu` VALUES (172, 101, 1024, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (173, 101, 1025, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (174, 101, 1026, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (175, 101, 1027, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (176, 101, 1028, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (177, 101, 1029, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (178, 101, 1030, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (179, 101, 1036, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (180, 101, 1037, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (181, 101, 1038, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (182, 101, 1039, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (183, 101, 1040, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (184, 101, 1042, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (185, 101, 1043, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (186, 101, 1045, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (187, 101, 1063, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (188, 101, 1064, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (189, 101, 1065, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (190, 101, 1007, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (191, 101, 1008, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (192, 101, 1009, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (193, 101, 1010, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (194, 101, 1011, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (195, 101, 1012, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (196, 101, 1013, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (197, 101, 1014, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (198, 101, 1015, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (199, 101, 1016, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (200, 101, 1017, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (201, 101, 1018, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (202, 101, 1019, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (203, 101, 1020, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (204, 101, 1021, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (205, 101, 1022, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (206, 101, 1023, '', '2021-01-21 03:04:50', '', '2021-01-21 03:07:43', b'1');
INSERT INTO `sys_role_menu` VALUES (207, 101, 1001, '', '2021-01-21 03:07:43', '', '2021-01-21 03:13:11', b'1');
INSERT INTO `sys_role_menu` VALUES (208, 101, 1002, '', '2021-01-21 03:07:43', '', '2021-01-21 03:10:08', b'1');
INSERT INTO `sys_role_menu` VALUES (209, 101, 1002, '', '2021-01-21 03:11:17', '', '2021-01-21 03:13:11', b'1');
INSERT INTO `sys_role_menu` VALUES (210, 101, 1, '', '2021-01-21 03:13:21', '', '2021-01-21 03:13:21', b'0');
INSERT INTO `sys_role_menu` VALUES (211, 101, 1001, '', '2021-01-21 03:13:21', '', '2021-01-21 03:13:40', b'1');
INSERT INTO `sys_role_menu` VALUES (212, 101, 100, '', '2021-01-21 03:13:21', '', '2021-01-21 03:13:40', b'1');
INSERT INTO `sys_role_menu` VALUES (213, 101, 1008, '', '2021-01-21 03:13:40', '', '2021-01-21 03:23:14', b'1');
INSERT INTO `sys_role_menu` VALUES (214, 101, 1009, '', '2021-01-21 03:13:40', '', '2021-01-21 03:23:14', b'1');
INSERT INTO `sys_role_menu` VALUES (215, 101, 1010, '', '2021-01-21 03:13:40', '', '2021-01-21 03:23:14', b'1');
INSERT INTO `sys_role_menu` VALUES (216, 101, 1011, '', '2021-01-21 03:13:40', '', '2021-01-21 03:23:14', b'1');
INSERT INTO `sys_role_menu` VALUES (217, 101, 1012, '', '2021-01-21 03:13:40', '', '2021-01-21 03:23:14', b'1');
INSERT INTO `sys_role_menu` VALUES (218, 101, 101, '', '2021-01-21 03:13:40', '', '2021-01-21 03:23:14', b'1');
INSERT INTO `sys_role_menu` VALUES (219, 101, 1063, '', '2021-01-21 03:13:40', '', '2021-01-21 03:23:14', b'1');
INSERT INTO `sys_role_menu` VALUES (220, 101, 1064, '', '2021-01-21 03:13:40', '', '2021-01-21 03:23:14', b'1');
INSERT INTO `sys_role_menu` VALUES (221, 101, 1065, '', '2021-01-21 03:13:40', '', '2021-01-21 03:23:14', b'1');
INSERT INTO `sys_role_menu` VALUES (222, 101, 100, '', '2021-01-21 03:23:14', '', '2021-01-21 03:23:27', b'1');
INSERT INTO `sys_role_menu` VALUES (223, 101, 1001, '', '2021-01-21 03:23:14', '', '2021-01-21 03:23:27', b'1');
INSERT INTO `sys_role_menu` VALUES (224, 101, 1002, '', '2021-01-21 03:23:14', '', '2021-01-21 03:23:27', b'1');
INSERT INTO `sys_role_menu` VALUES (225, 101, 1003, '', '2021-01-21 03:23:14', '', '2021-01-21 03:23:27', b'1');
INSERT INTO `sys_role_menu` VALUES (226, 101, 1004, '', '2021-01-21 03:23:14', '', '2021-01-21 03:23:27', b'1');
INSERT INTO `sys_role_menu` VALUES (227, 101, 1005, '', '2021-01-21 03:23:14', '', '2021-01-21 03:23:27', b'1');
INSERT INTO `sys_role_menu` VALUES (228, 101, 1006, '', '2021-01-21 03:23:14', '', '2021-01-21 03:23:27', b'1');
INSERT INTO `sys_role_menu` VALUES (229, 101, 1007, '', '2021-01-21 03:23:14', '', '2021-01-21 03:23:27', b'1');
INSERT INTO `sys_role_menu` VALUES (230, 101, 1008, '', '2021-01-21 03:23:27', '', '2021-01-21 03:23:27', b'0');
INSERT INTO `sys_role_menu` VALUES (231, 101, 1009, '', '2021-01-21 03:23:27', '', '2021-01-21 03:23:27', b'0');
INSERT INTO `sys_role_menu` VALUES (232, 101, 1010, '', '2021-01-21 03:23:27', '', '2021-01-21 03:23:27', b'0');
INSERT INTO `sys_role_menu` VALUES (233, 101, 1011, '', '2021-01-21 03:23:27', '', '2021-01-21 03:23:27', b'0');
INSERT INTO `sys_role_menu` VALUES (234, 101, 1012, '', '2021-01-21 03:23:27', '', '2021-01-21 03:23:27', b'0');
INSERT INTO `sys_role_menu` VALUES (235, 101, 101, '', '2021-01-21 03:23:27', '', '2021-01-21 03:23:27', b'0');
INSERT INTO `sys_role_menu` VALUES (236, 101, 1063, '', '2021-01-21 03:23:27', '', '2021-01-21 03:23:27', b'0');
INSERT INTO `sys_role_menu` VALUES (237, 101, 1064, '', '2021-01-21 03:23:27', '', '2021-01-21 03:23:27', b'0');
INSERT INTO `sys_role_menu` VALUES (238, 101, 1065, '', '2021-01-21 03:23:27', '', '2021-01-21 03:23:27', b'0');
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
) ENGINE=InnoDB AUTO_INCREMENT=105 DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- ----------------------------
-- Records of sys_user
-- ----------------------------
BEGIN;
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '若依', '管理员', 103, '[1]', 'ry@163.com', '15888888888', 1, 'http://127.0.0.1:8080/api/system/file/get/add5ec1891a7d97d2cc1d60847e16294.jpg', 0, '127.0.0.1', '2021-01-05 17:03:47', 'admin', '2021-01-05 17:03:47', '', '2021-01-13 17:51:06', b'0');
INSERT INTO `sys_user` VALUES (2, 'ry', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '若依', '测试员', 105, '[2]', 'ry@qq.com', '15666666666', 1, '', 0, '127.0.0.1', '2021-01-05 17:03:47', 'admin', '2021-01-05 17:03:47', '', '2021-01-05 17:03:47', b'0');
INSERT INTO `sys_user` VALUES (100, 'yudao', '$2a$10$11U48RhyJ5pSBYWSn12AD./ld671.ycSzJHbyrtpeoMeYiw31eo8a', '芋道', '不要吓我', 100, '[1]', 'yudao@iocoder.cn', '15601691300', 1, '', 1, '', NULL, '', '2021-01-07 09:07:17', '', '2021-01-13 23:53:12', b'0');
INSERT INTO `sys_user` VALUES (103, 'yuanma', '', '源码', NULL, 100, NULL, 'yuanma@iocoder.cn', '15601701300', 0, '', 0, '', NULL, '', '2021-01-13 23:50:35', '', '2021-01-13 23:50:35', b'0');
INSERT INTO `sys_user` VALUES (104, 'test', '$2a$10$wTJ.1LVmhxcujss2NR2SMeBo8AaFsjkoDfDafHYsdHmitAiwmnvce', '测试号', NULL, 100, '[]', '', '15601691200', 1, '', 0, '', NULL, '', '2021-01-21 02:13:53', '', '2021-01-21 02:14:13', b'0');
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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COMMENT='用户和角色关联表';

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_user_role` VALUES (1, 1, 1, '', NULL, '', NULL, b'0');
INSERT INTO `sys_user_role` VALUES (2, 2, 2, '', NULL, '', NULL, b'0');
INSERT INTO `sys_user_role` VALUES (3, 100, 1, '', NULL, '', NULL, b'1');
INSERT INTO `sys_user_role` VALUES (4, 100, 101, '', NULL, '', NULL, b'0');
INSERT INTO `sys_user_role` VALUES (5, 100, 1, '', NULL, '', NULL, b'0');
INSERT INTO `sys_user_role` VALUES (6, 100, 2, '', NULL, '', NULL, b'0');
INSERT INTO `sys_user_role` VALUES (7, 104, 101, '', NULL, '', NULL, b'0');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
