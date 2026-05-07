-- 消息表 content 列长度与 MESSAGE_CONTENT_MAX_LENGTH 保持一致

ALTER TABLE `im_private_message` MODIFY COLUMN `content` varchar(8192) DEFAULT NULL COMMENT '消息内容，JSON 格式';
ALTER TABLE `im_group_message` MODIFY COLUMN `content` varchar(8192) DEFAULT NULL COMMENT '消息内容，JSON 格式';
