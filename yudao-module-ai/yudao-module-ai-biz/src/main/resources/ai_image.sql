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

 Date: 30/05/2024 17:20:37
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_image
-- ----------------------------
DROP TABLE IF EXISTS `ai_image`;
CREATE TABLE `ai_image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `prompt` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '提示词\n',
  `platform` varchar(32) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '平台',
  `model` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '模型 dall2/dall3、MJ、NIJI',
  `width` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '图片宽度',
  `height` varchar(32) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '图片高度',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '绘画状态：提交、排队、绘画中、绘画完成、绘画失败\n',
  `public_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '是否发布',
  `pic_url` varchar(512) COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '图片地址',
  `original_pic_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '绘画图片地址\n',
  `error_message` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin DEFAULT NULL COMMENT '错误信息',
  `draw_request` json DEFAULT NULL COMMENT '绘画request',
  `draw_response` json DEFAULT NULL COMMENT '绘画response',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `creator` bigint DEFAULT NULL COMMENT '创建用户',
  `updater` bigint DEFAULT NULL COMMENT '更新用户',
  `deleted` bit(1) DEFAULT b'0' COMMENT '删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=107 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_bin;

-- ----------------------------
-- Records of ai_image
-- ----------------------------
BEGIN;
INSERT INTO `ai_image` (`id`, `user_id`, `prompt`, `platform`, `model`, `width`, `height`, `status`, `public_status`, `pic_url`, `original_pic_url`, `error_message`, `draw_request`, `draw_response`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (91, 1, '北极企鹅', NULL, 'dall-e-2', '1024', '1024', '20', NULL, 'http://test.yudao.iocoder.cn/75b4d733222b60aafdbdcd0475562ff88149eaaff93a25c4e4c66a95bd07f01f.png', 'https://oaidalleapiprodscus.blob.core.windows.net/private/org-FttVrm20iQRlsxxFE7BLEgkT/user-5p7zykU5aS1sYXCjczkTXn8I/img-z4KBxbWtUpLBYmgaYPvWXjBh.png?st=2024-05-29T04%3A50%3A54Z&se=2024-05-29T06%3A50%3A54Z&sp=r&sv=2023-11-03&sr=b&rscd=inline&rsct=image/png&skoid=6aaadede-4fb3-4698-a8f6-684d7786b067&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skt=2024-05-28T23%3A21%3A22Z&ske=2024-05-29T23%3A21%3A22Z&sks=b&skv=2023-11-03&sig=jSw/hJfuPWJqQgjSoINtVrt4w61FsaQ6ed4pRCM8UUA%3D', NULL, '{\"style\": \"vivid\"}', NULL, '2024-05-29 13:50:43', '2024-05-29 13:51:05', 1, 1, b'0');
INSERT INTO `ai_image` (`id`, `user_id`, `prompt`, `platform`, `model`, `width`, `height`, `status`, `public_status`, `pic_url`, `original_pic_url`, `error_message`, `draw_request`, `draw_response`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (93, 1, '北极企鹅', NULL, 'dall-e-2', '1024', '1024', '20', 'private', 'http://test.yudao.iocoder.cn/ab326bbf3fae9a940770a5c36bbf39467ae539a5b94a030b6c6cc2f179d7dd31.png', 'https://oaidalleapiprodscus.blob.core.windows.net/private/org-FttVrm20iQRlsxxFE7BLEgkT/user-5p7zykU5aS1sYXCjczkTXn8I/img-CsJtCgLT9lTigTuBDJtPyO8X.png?st=2024-05-29T09%3A02%3A12Z&se=2024-05-29T11%3A02%3A12Z&sp=r&sv=2023-11-03&sr=b&rscd=inline&rsct=image/png&skoid=6aaadede-4fb3-4698-a8f6-684d7786b067&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skt=2024-05-28T23%3A21%3A54Z&ske=2024-05-29T23%3A21%3A54Z&sks=b&skv=2023-11-03&sig=tH6yFzHtcp3Dxwaua2crFYurCdE7B7%2BAXhyPNVVep1c%3D', NULL, '{\"style\": \"vivid\"}', NULL, '2024-05-29 18:02:03', '2024-05-29 18:02:17', 1, 1, b'0');
INSERT INTO `ai_image` (`id`, `user_id`, `prompt`, `platform`, `model`, `width`, `height`, `status`, `public_status`, `pic_url`, `original_pic_url`, `error_message`, `draw_request`, `draw_response`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (94, 1, '北极大熊猫', NULL, 'dall-e-2', '1024', '1024', '20', 'private', 'http://test.yudao.iocoder.cn/ed9d43a6c841c4a967700e211c998778994eeea60cff2dfeb1c3a88b0542ebdd.png', 'https://oaidalleapiprodscus.blob.core.windows.net/private/org-FttVrm20iQRlsxxFE7BLEgkT/user-5p7zykU5aS1sYXCjczkTXn8I/img-GAwShv01C2408VY5lXvLTP4Q.png?st=2024-05-30T01%3A31%3A28Z&se=2024-05-30T03%3A31%3A28Z&sp=r&sv=2023-11-03&sr=b&rscd=inline&rsct=image/png&skoid=6aaadede-4fb3-4698-a8f6-684d7786b067&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skt=2024-05-29T23%3A39%3A20Z&ske=2024-05-30T23%3A39%3A20Z&sks=b&skv=2023-11-03&sig=n/rqAnD0qJDkhbh/Qm12lz1Se70PQSdXetarmwCKoMY%3D', NULL, '{\"style\": \"vivid\"}', NULL, '2024-05-30 10:31:18', '2024-05-30 10:31:34', 1, 1, b'1');
INSERT INTO `ai_image` (`id`, `user_id`, `prompt`, `platform`, `model`, `width`, `height`, `status`, `public_status`, `pic_url`, `original_pic_url`, `error_message`, `draw_request`, `draw_response`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (102, 1, '女少侠', 'midjourney', NULL, NULL, NULL, '30', 'private', NULL, NULL, '无可用的账号实例', NULL, '{}', '2024-05-30 16:11:13', '2024-05-30 16:11:13', 1, 1, b'1');
INSERT INTO `ai_image` (`id`, `user_id`, `prompt`, `platform`, `model`, `width`, `height`, `status`, `public_status`, `pic_url`, `original_pic_url`, `error_message`, `draw_request`, `draw_response`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (103, 1, '123123', 'midjourney', NULL, NULL, NULL, '30', 'private', NULL, NULL, '无可用的账号实例', NULL, '{}', '2024-05-30 16:27:34', '2024-05-30 16:27:34', 1, 1, b'1');
INSERT INTO `ai_image` (`id`, `user_id`, `prompt`, `platform`, `model`, `width`, `height`, `status`, `public_status`, `pic_url`, `original_pic_url`, `error_message`, `draw_request`, `draw_response`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (104, 1, '123123', 'midjourney', NULL, NULL, NULL, '30', 'private', NULL, NULL, '无可用的账号实例', NULL, '{}', '2024-05-30 16:28:23', '2024-05-30 16:28:24', 1, 1, b'1');
INSERT INTO `ai_image` (`id`, `user_id`, `prompt`, `platform`, `model`, `width`, `height`, `status`, `public_status`, `pic_url`, `original_pic_url`, `error_message`, `draw_request`, `draw_response`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (105, 1, '123123', 'midjourney', NULL, NULL, NULL, '30', 'private', NULL, NULL, '无可用的账号实例', NULL, '{}', '2024-05-30 16:28:25', '2024-05-30 16:28:25', 1, 1, b'1');
INSERT INTO `ai_image` (`id`, `user_id`, `prompt`, `platform`, `model`, `width`, `height`, `status`, `public_status`, `pic_url`, `original_pic_url`, `error_message`, `draw_request`, `draw_response`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES (106, 1, '123123', 'midjourney', NULL, NULL, NULL, '30', 'private', NULL, NULL, '无可用的账号实例', NULL, '{}', '2024-05-30 16:28:34', '2024-05-30 16:28:34', 1, 1, b'1');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
