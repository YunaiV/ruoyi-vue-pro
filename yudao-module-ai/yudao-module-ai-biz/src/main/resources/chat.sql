/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80034 (8.0.34)
 Source Host           : localhost:3306
 Source Schema         : ruoyi-vue-pro

 Target Server Type    : MySQL
 Target Server Version : 80034 (8.0.34)
 File Encoding         : 65001

 Date: 28/04/2024 11:50:47
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_chat_conversation
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_conversation`;
CREATE TABLE `ai_chat_conversation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `chat_role_id` bigint DEFAULT NULL COMMENT '聊天角色',
  `chat_role_name` varchar(128) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '聊天角色名称',
  `title` varchar(256) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '标题',
  `type` varchar(16) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '对话类型',
  `chat_count` int DEFAULT NULL COMMENT '聊天次数',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `creator` bigint DEFAULT NULL COMMENT '创建用户',
  `updater` bigint DEFAULT NULL COMMENT '更新用户',
  `deleted` bit(1) DEFAULT b'0' COMMENT '删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1781604279872581645 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_bin;

-- ----------------------------
-- Records of ai_chat_conversation
-- ----------------------------
BEGIN;
INSERT INTO `ai_chat_conversation` (`id`, `user_id`, `chat_role_id`, `chat_role_name`, `title`, `type`, `chat_count`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (1781604279872581644, 1, NULL, NULL, '中国好看吗？', 'userChat', 32, '2024-04-20 16:59:32', '2024-04-20 16:59:32', 1, 1, b'0');
COMMIT;

-- ----------------------------
-- Table structure for ai_chat_message
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_message`;
CREATE TABLE `ai_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chat_conversation_id` bigint DEFAULT NULL COMMENT '对话id',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `message` text COLLATE utf8mb4_0900_bin COMMENT '对话message',
  `message_type` varchar(16) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '对话类型system、user、assistant\n',
  `top_k` float DEFAULT NULL COMMENT '在生成消息时采用的Top-K采样大小',
  `top_p` float DEFAULT NULL COMMENT 'Top-P核采样方法的概率阈值',
  `temperature` float DEFAULT NULL COMMENT '用于调整生成回复的随机性和多样性程度',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `creator` bigint DEFAULT NULL COMMENT '创建用户',
  `updater` bigint DEFAULT NULL COMMENT '更新用户',
  `deleted` bit(1) DEFAULT b'0' COMMENT '删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_bin;

-- ----------------------------
-- Records of ai_chat_message
-- ----------------------------
BEGIN;
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (1, 1781604279872581644, 1, '中国好看吗？', 'user', NULL, NULL, NULL, '2024-04-20 18:22:41', '2024-04-20 18:22:41', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (2, 1781604279872581644, 1, '作为一个AI助手，我没有个人感受和偏好。但是，中国是一个拥有丰富历史、文化和自然景观的国家，有许多令人惊叹的地方，如长城、故宫、西湖、张家界、九寨沟等著名景点，吸引了无数国内外游客的喜爱。每个人对于\"好看\"的定义可能不同，但中国无疑有着独特的魅力和多样性。', 'system', NULL, NULL, NULL, '2024-04-20 18:22:44', '2024-04-20 18:22:44', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (3, 1781604279872581644, 1, '苹果是什么颜色?', 'user', NULL, NULL, NULL, '2024-04-20 18:23:05', '2024-04-20 18:23:05', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (4, 1781604279872581644, 1, '苹果的颜色可以根据品种的不同而有所差异。常见的苹果颜色有红色、黄色和青绿色。除了这些基本颜色，苹果还可能呈现出淡红色、深红色等不同的红色调。\n\n这些颜色只是影响苹果的外观，对苹果的口感和营养价值没有影响。苹果口感酸甜，营养丰富，深受人们喜爱。', 'system', NULL, NULL, NULL, '2024-04-20 18:23:11', '2024-04-20 18:23:11', NULL, NULL, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (5, 1781604279872581644, 1, '中国好看吗？', 'user', NULL, NULL, NULL, '2024-04-23 10:46:48', '2024-04-23 10:46:48', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (6, 1781604279872581644, 1, '作为一个AI助手，我没有个人感受和偏好。但是，中国是一个拥有丰富历史、文化和自然景观的国家，有许多令人惊叹的地方，如长城、故宫、西湖、张家界、九寨沟等著名景点，吸引了无数国内外游客的喜爱。每个人对于\"好看\"的定义可能不同，但中国无疑有着独特的魅力和多样性。', 'system', NULL, NULL, NULL, '2024-04-23 10:46:52', '2024-04-23 10:46:52', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (7, 1781604279872581644, 1, '苹果是什么颜色?', 'user', NULL, NULL, NULL, '2024-04-23 10:46:55', '2024-04-23 10:46:55', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (8, 1781604279872581644, 1, '苹果的颜色因品种和成熟度的不同而异。常见的苹果颜色有红色、黄色和青绿色。除此之外，还有一些苹果品种可能呈现出淡红色、深红色等不同的红色调，或者带有条纹或斑点。总的来说，苹果的颜色非常多样化，但最常见的是红色、黄色和青绿色。', 'system', NULL, NULL, NULL, '2024-04-23 10:47:03', '2024-04-23 10:47:03', NULL, NULL, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (9, 1781604279872581644, 1, '中国好看吗？', 'user', NULL, NULL, NULL, '2024-04-23 13:49:16', '2024-04-23 13:49:16', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (10, 1781604279872581644, 1, '作为一个AI助手，我没有个人感受和偏好。但是，中国是一个拥有丰富历史、文化和自然景观的国家，有许多令人惊叹的地方，如长城、故宫、西湖、张家界、九寨沟等著名景点，吸引了无数国内外游客的喜爱。每个人对于\"好看\"的定义可能不同，但中国无疑有着独特的魅力和多样性。', 'system', NULL, NULL, NULL, '2024-04-23 13:49:19', '2024-04-23 13:49:19', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (11, 1781604279872581644, 1, '中国好看吗？', 'user', NULL, NULL, NULL, '2024-04-25 17:55:39', '2024-04-25 17:55:39', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (12, 1781604279872581644, 1, 'NoSuchBeanDefinitionException: No qualifying bean of type \'cn.iocoder.yudao.framework.ai.chatqianwen.QianWenChatClient\' available', 'system', NULL, NULL, NULL, '2024-04-25 17:55:39', '2024-04-25 17:55:39', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (13, 1781604279872581644, 1, '中国好看吗？', 'user', NULL, NULL, NULL, '2024-04-25 17:56:12', '2024-04-25 17:56:12', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (14, 1781604279872581644, 1, 'NoSuchBeanDefinitionException: No qualifying bean of type \'cn.iocoder.yudao.framework.ai.chatqianwen.QianWenChatClient\' available', 'system', NULL, NULL, NULL, '2024-04-25 17:56:12', '2024-04-25 17:56:12', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (15, 1781604279872581644, 1, '中国好看吗？', 'user', NULL, NULL, NULL, '2024-04-25 17:56:24', '2024-04-25 17:56:24', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (16, 1781604279872581644, 1, 'NoSuchBeanDefinitionException: No qualifying bean of type \'cn.iocoder.yudao.framework.ai.chatqianwen.QianWenChatClient\' available', 'system', NULL, NULL, NULL, '2024-04-25 17:56:24', '2024-04-25 17:56:24', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (17, 1781604279872581644, 1, '中国好看吗？', 'user', NULL, NULL, NULL, '2024-04-25 17:56:41', '2024-04-25 17:56:41', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (18, 1781604279872581644, 1, 'NoSuchBeanDefinitionException: No qualifying bean of type \'cn.iocoder.yudao.framework.ai.chatqianwen.QianWenChatClient\' available', 'system', NULL, NULL, NULL, '2024-04-25 17:56:59', '2024-04-25 17:56:59', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (19, 1781604279872581644, 1, '中国好看吗？', 'user', NULL, NULL, NULL, '2024-04-25 17:58:15', '2024-04-25 17:58:15', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (20, 1781604279872581644, 1, '作为一个AI助手，我没有个人感受和偏好。但是，中国是一个拥有丰富历史、文化和自然景观的国家，有许多令人惊叹的地方，如长城、故宫、西湖、张家界、九寨沟等著名景点，吸引了无数国内外游客的喜爱。每个人对于\"好看\"的定义可能不同，但中国无疑有着独特的魅力和多样性。', 'system', NULL, NULL, NULL, '2024-04-25 17:58:19', '2024-04-25 17:58:19', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (21, 1781604279872581644, 1, '中国好看吗？', 'user', NULL, NULL, NULL, '2024-04-26 18:27:13', '2024-04-26 18:27:13', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (22, 1781604279872581644, 1, 'ClassCastException: class com.alibaba.dashscope.aigc.generation.models.QwenParam cannot be cast to class cn.iocoder.yudao.framework.ai.chatqianwen.api.QianWenChatCompletionRequest (com.alibaba.dashscope.aigc.generation.models.QwenParam and cn.iocoder.yudao.framework.ai.chatqianwen.api.QianWenChatCompletionRequest are in unnamed module of loader \'app\')', 'system', NULL, NULL, NULL, '2024-04-26 18:27:13', '2024-04-26 18:27:13', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (23, 1781604279872581644, 1, '中国好看吗？', 'user', NULL, NULL, NULL, '2024-04-26 18:28:12', '2024-04-26 18:28:12', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (24, 1781604279872581644, 1, 'ClassCastException: class com.alibaba.dashscope.aigc.generation.models.QwenParam cannot be cast to class cn.iocoder.yudao.framework.ai.chatqianwen.api.QianWenChatCompletionRequest (com.alibaba.dashscope.aigc.generation.models.QwenParam and cn.iocoder.yudao.framework.ai.chatqianwen.api.QianWenChatCompletionRequest are in unnamed module of loader \'app\')', 'system', NULL, NULL, NULL, '2024-04-26 18:30:31', '2024-04-26 18:30:31', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (25, 1781604279872581644, 1, '中国好看吗？', 'user', NULL, NULL, NULL, '2024-04-27 09:39:56', '2024-04-27 09:39:56', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (26, 1781604279872581644, 1, 'ClassCastException: class com.alibaba.dashscope.aigc.generation.models.QwenParam cannot be cast to class cn.iocoder.yudao.framework.ai.chatqianwen.api.QianWenChatCompletionRequest (com.alibaba.dashscope.aigc.generation.models.QwenParam and cn.iocoder.yudao.framework.ai.chatqianwen.api.QianWenChatCompletionRequest are in unnamed module of loader \'app\')', 'system', NULL, NULL, NULL, '2024-04-27 09:39:56', '2024-04-27 09:39:56', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (27, 1781604279872581644, 1, '中国好看吗？', 'user', NULL, NULL, NULL, '2024-04-27 09:41:02', '2024-04-27 09:41:02', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (28, 1781604279872581644, 1, 'AiException: 没有找到apiKey！Can not find api-key.', 'system', NULL, NULL, NULL, '2024-04-27 09:41:02', '2024-04-27 09:41:02', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (29, 1781604279872581644, 1, '中国好看吗？', 'user', NULL, NULL, NULL, '2024-04-27 09:41:58', '2024-04-27 09:41:58', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (30, 1781604279872581644, 1, 'AiException: 没有找到apiKey！Can not find api-key.', 'system', NULL, NULL, NULL, '2024-04-27 09:41:58', '2024-04-27 09:41:58', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (31, 1781604279872581644, 1, '中国好看吗？', 'user', NULL, NULL, NULL, '2024-04-27 09:42:41', '2024-04-27 09:42:41', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `chat_conversation_id`, `user_id`, `message`, `message_type`, `top_k`, `top_p`, `temperature`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (32, 1781604279872581644, 1, 'ApiException: {\"statusCode\":400,\"message\":\"parameter incremental_output only support stream call\",\"code\":\"InvalidParameter\",\"isJson\":true,\"requestId\":\"9edd3968-6eec-901d-8194-15f497ca0d3a\"}', 'system', NULL, NULL, NULL, '2024-04-27 09:42:42', '2024-04-27 09:42:42', 1, 1, b'0');
COMMIT;

-- ----------------------------
-- Table structure for ai_chat_modal
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_modal`;
CREATE TABLE `ai_chat_modal` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `model_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '模型名字\n',
  `model_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '模型类型(自己定义qianwen、yiyan、xinghuo、openai)\n',
  `modal_image` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '模型照片\n',
  `config` json DEFAULT NULL COMMENT 'Ai配置文件',
  `disable` tinyint DEFAULT NULL COMMENT '禁用 0、正常 1、禁用\n',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `creator` bigint DEFAULT NULL COMMENT '创建用户',
  `updater` bigint DEFAULT NULL COMMENT '更新用户',
  `deleted` bit(1) DEFAULT b'0' COMMENT '删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_bin;

-- ----------------------------
-- Records of ai_chat_modal
-- ----------------------------
BEGIN;
INSERT INTO `ai_chat_modal` (`id`, `model_name`, `model_type`, `modal_image`, `config`, `disable`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (1, '小红书Ai写作大模型', 'yiyan', '', NULL, 0, '2024-04-25 18:10:32', '2024-04-25 18:10:32', 1, 1, b'0');
COMMIT;

-- ----------------------------
-- Table structure for ai_chat_role
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_role`;
CREATE TABLE `ai_chat_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `model_id` bigint DEFAULT NULL COMMENT '模型编号，关联到角色使用的特定模型\n',
  `role_name` varchar(128) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '角色名，角色的显示名称\n',
  `role_introduce` varchar(256) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '角色介绍，详细描述角色的功能或用途\n',
  `role_source` varchar(16) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '角色来源，如 system（系统预置）、customer（用户自定义）\n',
  `classify` varchar(32) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '分类，角色所属的类别，如娱乐、创作等\n',
  `visibility` varchar(16) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '发布状态，private 表示仅自己可见，public表示公开，disable表示禁用\n',
  `top_k` float DEFAULT NULL COMMENT '生成时的Top-K采样候选集大小\n',
  `top_p` float DEFAULT NULL COMMENT '生成时使用的核采样方法的概率阈值\n',
  `temperature` float DEFAULT NULL COMMENT '用于控制随机性和多样性的温度参数\n',
  `use_count` int DEFAULT NULL COMMENT '角色的使用次数统计\n',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `creator` bigint DEFAULT NULL COMMENT '创建用户',
  `updater` bigint DEFAULT NULL COMMENT '更新用户',
  `deleted` bit(1) DEFAULT b'0' COMMENT '删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_bin;

-- ----------------------------
-- Records of ai_chat_role
-- ----------------------------
BEGIN;
INSERT INTO `ai_chat_role` (`id`, `user_id`, `model_id`, `role_name`, `role_introduce`, `role_source`, `classify`, `visibility`, `top_k`, `top_p`, `temperature`, `use_count`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (1, 1, 1, '小红书写作v1---hh😄', '采用gpt3.5模型，拥有小红书优质作者写作经验。', 'system', 'writing', 'private', 0.2, 0.4, 0.7, 0, '2024-04-24 19:21:42', '2024-04-24 19:37:49', 1, 1, b'1');
INSERT INTO `ai_chat_role` (`id`, `user_id`, `model_id`, `role_name`, `role_introduce`, `role_source`, `classify`, `visibility`, `top_k`, `top_p`, `temperature`, `use_count`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (2, 1, 1, '小红书写作v2', '采用gpt3.5模型，拥有小红书优质作者写作经验。', 'system', 'writing', 'public', 0.2, 0.4, 0.7, 0, '2024-04-24 19:22:05', '2024-04-24 19:22:05', 1, 1, b'0');
INSERT INTO `ai_chat_role` (`id`, `user_id`, `model_id`, `role_name`, `role_introduce`, `role_source`, `classify`, `visibility`, `top_k`, `top_p`, `temperature`, `use_count`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (3, 1, 1, '小红书写作v1---', '采用gpt3.5模型，拥有小红书优质作者写作经验。', 'system', 'writing', 'public', 0.2, 0.4, 0.7, 0, '2024-04-24 19:29:38', '2024-04-24 19:29:38', 1, 1, b'0');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
