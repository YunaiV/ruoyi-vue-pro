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

 Date: 08/05/2024 18:10:05
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
  `role_id` bigint DEFAULT NULL COMMENT '聊天角色',
  `title` varchar(256) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '标题',
  `type` varchar(16) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '对话类型',
  `chat_count` int DEFAULT NULL COMMENT '聊天次数',
  `model_id` bigint DEFAULT NULL COMMENT '模型id',
  `model` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '模型',
  `pinned` blob COMMENT '是否置顶',
  `temperature` double DEFAULT NULL COMMENT '温度参数',
  `max_tokens` int DEFAULT NULL COMMENT '单条回复的最大 Token 数量',
  `max_contexts` int DEFAULT NULL COMMENT '上下文的最大 Message 数量',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `creator` bigint DEFAULT NULL COMMENT '创建用户',
  `updater` bigint DEFAULT NULL COMMENT '更新用户',
  `deleted` bit(1) DEFAULT b'0' COMMENT '删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1781604279872581650 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_bin;

-- ----------------------------
-- Records of ai_chat_conversation
-- ----------------------------
BEGIN;
INSERT INTO `ai_chat_conversation` (`id`, `user_id`, `role_id`, `title`, `type`, `chat_count`, `model_id`, `model`, `pinned`, `temperature`, `max_tokens`, `max_contexts`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (1781604279872581647, 1, NULL, '新增对话', NULL, NULL, 9, 'ERNIE-3.5-8K', 0x30, NULL, NULL, NULL, '2024-05-07 16:20:06', '2024-05-07 16:20:06', 1, 1, b'1');
INSERT INTO `ai_chat_conversation` (`id`, `user_id`, `role_id`, `title`, `type`, `chat_count`, `model_id`, `model`, `pinned`, `temperature`, `max_tokens`, `max_contexts`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (1781604279872581648, 1, 9, '新增对话', NULL, NULL, 9, 'ERNIE-3.5-8K', 0x30, NULL, NULL, NULL, '2024-05-07 16:20:35', '2024-05-07 16:20:35', 1, 1, b'0');
INSERT INTO `ai_chat_conversation` (`id`, `user_id`, `role_id`, `title`, `type`, `chat_count`, `model_id`, `model`, `pinned`, `temperature`, `max_tokens`, `max_contexts`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (1781604279872581649, 1, NULL, '新增对话', NULL, NULL, 9, 'ERNIE-3.5-8K', 0x30, NULL, NULL, NULL, '2024-05-07 16:22:37', '2024-05-07 16:22:37', 1, 1, b'0');
COMMIT;

-- ----------------------------
-- Table structure for ai_chat_message
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_message`;
CREATE TABLE `ai_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint DEFAULT NULL COMMENT '对话id',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `role_id` bigint DEFAULT NULL COMMENT '角色id',
  `type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '类型system、user、assistant\n',
  `model` varchar(32) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '模型',
  `model_id` bigint DEFAULT NULL COMMENT '模型id',
  `content` varchar(2048) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '内容',
  `tokens` int DEFAULT NULL COMMENT '消耗 Token 数量',
  `temperature` double DEFAULT NULL COMMENT '用于调整生成回复的随机性和多样性程度',
  `max_tokens` int DEFAULT NULL COMMENT '单条回复的最大 Token 数量',
  `max_contexts` int DEFAULT NULL COMMENT '上下文的最大 Message 数量',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `creator` bigint DEFAULT NULL COMMENT '创建用户',
  `updater` bigint DEFAULT NULL COMMENT '更新用户',
  `deleted` bit(1) DEFAULT b'0' COMMENT '删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_bin;

-- ----------------------------
-- Records of ai_chat_message
-- ----------------------------
BEGIN;
INSERT INTO `ai_chat_message` (`id`, `conversation_id`, `user_id`, `role_id`, `type`, `model`, `model_id`, `content`, `tokens`, `temperature`, `max_tokens`, `max_contexts`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (61, 1781604279872581649, 1, NULL, 'user', 'ERNIE-3.5-8K', 9, '苹果是什么颜色？', NULL, NULL, NULL, NULL, '2024-05-07 17:18:29', '2024-05-07 17:18:29', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `conversation_id`, `user_id`, `role_id`, `type`, `model`, `model_id`, `content`, `tokens`, `temperature`, `max_tokens`, `max_contexts`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (62, 1781604279872581649, 1, NULL, 'system', 'ERNIE-3.5-8K', 9, '苹果是一种水果，其颜色可以因品种和成熟度而异。常见的苹果颜色包括：\n\n1. 红色：许多苹果品种，如红富士、红元帅等，在成熟时会呈现出鲜艳的红色。\n2. 绿色：一些苹果品种，如青苹果、青香蕉等，在成熟时保持绿色或带有绿色条纹。\n3. 黄色：金苹果、黄元帅等品种在成熟时呈黄色。\n\n此外，还有一些苹果品种在成熟时会呈现出不同的颜色组合，如红绿相间、红黄相间等。因此，苹果的颜色并不是单一的，而是根据品种和成熟度而有所不同。', 8, NULL, NULL, NULL, '2024-05-07 17:18:38', '2024-05-07 17:18:38', NULL, NULL, b'0');
INSERT INTO `ai_chat_message` (`id`, `conversation_id`, `user_id`, `role_id`, `type`, `model`, `model_id`, `content`, `tokens`, `temperature`, `max_tokens`, `max_contexts`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (63, 1781604279872581649, 1, NULL, 'user', 'ERNIE-3.5-8K', 9, '中国好看吗？', NULL, NULL, NULL, NULL, '2024-05-07 17:18:53', '2024-05-07 17:18:53', 1, 1, b'0');
INSERT INTO `ai_chat_message` (`id`, `conversation_id`, `user_id`, `role_id`, `type`, `model`, `model_id`, `content`, `tokens`, `temperature`, `max_tokens`, `max_contexts`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (64, 1781604279872581649, 1, NULL, 'system', 'ERNIE-3.5-8K', 9, '中国是一个拥有悠久历史、灿烂文化、广袤土地和多元民族的国家，自然景观和人文景观都非常丰富。从雄伟的长城、壮丽的黄山到神秘的西藏，从繁华的上海、历史悠久的北京到充满异域风情的云南，中国各地都有独特的魅力。\n\n此外，中国还拥有丰富多彩的非物质文化遗产，如京剧、川剧、皮影戏等传统艺术形式，以及中秋节、春节等传统节日。这些文化遗产反映了中国人民的智慧和创造力，也是中国文化的重要组成部分。\n\n因此，可以说中国非常美丽，值得人们去探索和发现它的魅力。无论是自然景观还是人文景观，中国都有着独特的魅力和吸引力。', 1, NULL, NULL, NULL, '2024-05-07 17:19:03', '2024-05-07 17:19:03', 1, 1, b'0');
COMMIT;

-- ----------------------------
-- Table structure for ai_chat_model
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_model`;
CREATE TABLE `ai_chat_model` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `key_id` bigint DEFAULT NULL COMMENT 'API 秘钥编号',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '模型名字\n',
  `model` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '模型类型(自己定义qianwen、yiyan、xinghuo、openai)\n',
  `platform` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '平台',
  `sort` int DEFAULT NULL COMMENT '排序',
  `status` tinyint DEFAULT NULL COMMENT '禁用 0、正常 1、禁用\n',
  `temperature` double DEFAULT NULL COMMENT '温度参数',
  `max_tokens` int DEFAULT NULL COMMENT '单条回复的最大 Token 数量',
  `max_contexts` int DEFAULT NULL COMMENT '上下文的最大 Message 数量',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `creator` bigint DEFAULT NULL COMMENT '创建用户',
  `updater` bigint DEFAULT NULL COMMENT '更新用户',
  `deleted` bit(1) DEFAULT b'0' COMMENT '删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_bin;

-- ----------------------------
-- Records of ai_chat_model
-- ----------------------------
BEGIN;
INSERT INTO `ai_chat_model` (`id`, `key_id`, `name`, `model`, `platform`, `sort`, `status`, `temperature`, `max_tokens`, `max_contexts`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (9, 1, '小红书Ai写作大模型3.5 8k', 'ERNIE-3.5-8K', 'yiyan', 100, 0, NULL, NULL, NULL, '2024-05-07 15:08:22', '2024-05-07 15:20:32', 1, 1, b'0');
INSERT INTO `ai_chat_model` (`id`, `key_id`, `name`, `model`, `platform`, `sort`, `status`, `temperature`, `max_tokens`, `max_contexts`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (10, 1, '小红书Ai写作大模型4.0', 'ERNIE 4.0', 'yiyan', 100, 0, NULL, NULL, NULL, '2024-05-07 15:23:33', '2024-05-07 15:23:33', 1, 1, b'0');
COMMIT;

-- ----------------------------
-- Table structure for ai_chat_role
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_role`;
CREATE TABLE `ai_chat_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `model_id` bigint DEFAULT NULL COMMENT '模型编号',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '角色名，角色的显示名称\n',
  `avatar` varchar(256) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '头像',
  `category` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '分类，角色所属的类别，如娱乐、创作等\n',
  `sort` int DEFAULT NULL COMMENT '排序',
  `description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '角色描述',
  `welcome_message` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '角色欢迎语',
  `system_message` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '角色设定（消息）',
  `public_status` blob COMMENT '是否公开 true - 公开；false - 私有',
  `status` tinyint DEFAULT NULL COMMENT '状态 0、开启 1、关闭',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `creator` bigint DEFAULT NULL COMMENT '创建用户',
  `updater` bigint DEFAULT NULL COMMENT '更新用户',
  `deleted` bit(1) DEFAULT b'0' COMMENT '删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_bin;

-- ----------------------------
-- Records of ai_chat_role
-- ----------------------------
BEGIN;
INSERT INTO `ai_chat_role` (`id`, `user_id`, `model_id`, `name`, `avatar`, `category`, `sort`, `description`, `welcome_message`, `system_message`, `public_status`, `status`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (8, 1, 9, '小红书写作v2', 'http://baidu.com', 'writing', 0, '采用gpt3.5模型，拥有小红书优质作者写作经验。', '欢迎使用小红书写作模型!', '你是一名优秀的小红书人文、风光作者，你热爱旅游，每去往一个城市你都会用美妙的文字抒写着这座城市的大街小巷，描述着这座城市的美好。', 0x31, 0, '2024-05-07 15:30:30', '2024-05-07 15:35:54', 1, 1, b'1');
INSERT INTO `ai_chat_role` (`id`, `user_id`, `model_id`, `name`, `avatar`, `category`, `sort`, `description`, `welcome_message`, `system_message`, `public_status`, `status`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (9, 1, 9, '小红书写作v1', 'http://baidu.com', 'writing', 0, '采用gpt3.5模型，拥有小红书优质作者写作经验。', '欢迎使用小红书写作模型!', '你是一名优秀的小红书人文、风光作者，你热爱旅游，每去往一个城市你都会用美妙的文字抒写着这座城市的大街小巷，描述着这座城市的美好。', 0x30, 0, '2024-05-07 15:36:40', '2024-05-07 15:36:40', 1, 1, b'0');
COMMIT;

-- ----------------------------
-- Table structure for ai_image
-- ----------------------------
DROP TABLE IF EXISTS `ai_image`;
CREATE TABLE `ai_image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `prompt` varchar(2000) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '提示词\n',
  `modal` varchar(32) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '模型\n',
  `size` varchar(32) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '生成图像的尺寸大小。对于dall-e-2模型，尺寸可为256x256, 512x512, 或 1024x1024。对于dall-e-3模型，尺寸可为1024x1024, 1792x1024, 或 1024x1792。\n',
  `drawing_status` varchar(32) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '绘画状态：提交、排队、绘画中、绘画完成、绘画失败\n',
  `drawing_image_url` varchar(512) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '绘画图片地址\n',
  `drawing_error_message` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '错误信息',
  `mj_message_id` varchar(32) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '用户操作的消息编号(MJ返回)\n',
  `mj_operation_id` varchar(128) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '用户操作的操作编号(MJ返回)\n',
  `mj_operation_name` varchar(32) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '用户操作的操作名字(MJ返回)\n',
  `mj_operations` json DEFAULT NULL COMMENT 'mj图片生产成功保存的 components json 数组\n',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `creator` bigint DEFAULT NULL COMMENT '创建用户',
  `updater` bigint DEFAULT NULL COMMENT '更新用户',
  `deleted` bit(1) DEFAULT b'0' COMMENT '删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_bin;

-- ----------------------------
-- Records of ai_image
-- ----------------------------
BEGIN;
INSERT INTO `ai_image` (`id`, `user_id`, `prompt`, `modal`, `size`, `drawing_status`, `drawing_image_url`, `drawing_error_message`, `mj_message_id`, `mj_operation_id`, `mj_operation_name`, `mj_operations`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (22, 1, 'Cute cartoon style mobile game scene, a colorful camping car with an outdoor table and chairs next to it on the road in a spring forest, the simple structure of the camper van, soft lighting, C4D rendering, 3d model in the style of a cartoon, cute shape, a pastel color scheme, closeup view from the side angle, high resolution, bright colors, a happy atmosphere. --ar 1:2 --v 6.0', 'midjoureny', NULL, 'fail', NULL, 'You have reached the maximum allowed number of concurrent jobs. Don\'t worry, this job will start as soon as another one finishes!', NULL, NULL, NULL, NULL, '2024-05-08 17:26:01', '2024-05-08 17:26:04', 1, NULL, b'0');
INSERT INTO `ai_image` (`id`, `user_id`, `prompt`, `modal`, `size`, `drawing_status`, `drawing_image_url`, `drawing_error_message`, `mj_message_id`, `mj_operation_id`, `mj_operation_name`, `mj_operations`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (23, 1, 'Cute cartoon style mobile game scene, a colorful camping car with an outdoor table and chairs next to it on the road in a spring forest, the simple structure of the camper van, soft lighting, C4D rendering, 3d model in the style of a cartoon, cute shape, a pastel color scheme, closeup view from the side angle, high resolution, bright colors, a happy atmosphere. --ar 1:2 --v 6.0', 'midjoureny', NULL, 'fail', NULL, 'Your job queue is full. Please wait for a job to finish first, then resubmit this one.', '1788144718477979648', NULL, NULL, NULL, '2024-05-08 17:51:38', '2024-05-08 17:51:39', 1, NULL, b'0');
INSERT INTO `ai_image` (`id`, `user_id`, `prompt`, `modal`, `size`, `drawing_status`, `drawing_image_url`, `drawing_error_message`, `mj_message_id`, `mj_operation_id`, `mj_operation_name`, `mj_operations`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (24, 1, 'Cute cartoon style mobile game scene, a colorful camping car with an outdoor table and chairs next to it on the road in a spring forest, the simple structure of the camper van, soft lighting, C4D rendering, 3d model in the style of a cartoon, cute shape, a pastel color scheme, closeup view from the side angle, high resolution, bright colors, a happy atmosphere. --ar 1:2 --v 6.0', 'midjoureny', NULL, 'submit', NULL, NULL, '1788145293357699072', NULL, NULL, NULL, '2024-05-08 17:53:55', '2024-05-08 17:53:55', 1, 1, b'0');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
