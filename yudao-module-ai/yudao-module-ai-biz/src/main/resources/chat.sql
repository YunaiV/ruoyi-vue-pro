DROP TABLE IF EXISTS chat_role;
CREATE TABLE chat_role(
    `id` bigint   COMMENT '编号' ,
    `user_id` bigint   COMMENT '用户id' ,
    `model_id` VARCHAR(255)   COMMENT '模型编号' ,
    `role_name` VARCHAR(32)   COMMENT '角色名' ,
    `role_introduce` VARCHAR(255)   COMMENT '介绍' ,
    `role_source` VARCHAR(32)   COMMENT '角色来源(system、customer)' ,
    `classify` VARCHAR(32)   COMMENT '分类(娱乐、创作)' ,
    `publish` tinyint   COMMENT '发布(0、自己可见 1、公开 2、禁用)' ,
    `top_k` DECIMAL(4,2)   COMMENT '生成时，采样候选集的大小' ,
    `top_p` DECIMAL(24,6)   COMMENT '生成时，核采样方法的概率阈值。' ,
    `temperature` DECIMAL(24,6)   COMMENT '用于控制随机性和多样性的程度' ,
    `use_count` INT   COMMENT '使用次数' ,
    `CREATED_BY` bigint   COMMENT '创建人' ,
    `CREATED_TIME` DATETIME   COMMENT '创建时间' ,
    `UPDATED_BY` bigint   COMMENT '更新人' ,
    `UPDATED_TIME` DATETIME   COMMENT '更新时间' 
)  COMMENT = 'chat角色;';

DROP TABLE IF EXISTS chat_history;
CREATE TABLE chat_history(
    `id` bigint   COMMENT '编号' ,
    `chat_id` bigint   COMMENT '聊天id' ,
    `user_id` VARCHAR(32)   COMMENT '角色名' ,
    `message` TEXT   COMMENT '消息' ,
    `message_type` DECIMAL(4,2)   COMMENT 'system、user、assistant' ,
    `top_k` DECIMAL(4,2)   COMMENT '生成时，采样候选集的大小' ,
    `top_p` DECIMAL(24,6)   COMMENT '生成时，核采样方法的概率阈值。' ,
    `temperature` DECIMAL(24,6)   COMMENT '用于控制随机性和多样性的程度' ,
    `CREATED_BY` bigint   COMMENT '创建人' ,
    `CREATED_TIME` DATETIME   COMMENT '创建时间' ,
    `UPDATED_BY` bigint   COMMENT '更新人' ,
    `UPDATED_TIME` DATETIME   COMMENT '更新时间' 
)  COMMENT = '聊天记录;';

DROP TABLE IF EXISTS user;
CREATE TABLE user(
    `id` bigint   COMMENT '编号' ,
    `username` VARCHAR(32)   COMMENT '用户名' ,
    `nickname` VARCHAR(32)   COMMENT '昵称' ,
    `mobile` VARCHAR(32)   COMMENT '手机号' ,
    `avatar_url` VARCHAR(128)   COMMENT '头像' ,
    `password` VARCHAR(32)   COMMENT '密码' ,
    `publish` tinyint   COMMENT '发布(0、未发布 1、已发布)' ,
    `disable` VARCHAR(255)   COMMENT '禁用(0、正常 1、禁用)' ,
    `CREATED_BY` bigint   COMMENT '创建人' ,
    `CREATED_TIME` DATETIME   COMMENT '创建时间' ,
    `UPDATED_BY` bigint   COMMENT '更新人' ,
    `UPDATED_TIME` DATETIME   COMMENT '更新时间' 
)  COMMENT = '用户;';

DROP TABLE IF EXISTS chat;
CREATE TABLE chat(
    `id` bigint   COMMENT '编号' ,
    `user_id` bigint   COMMENT '用户id' ,
    `chat_role_id` VARCHAR(255)   COMMENT '模型id' ,
    `chat_title` VARCHAR(128)   COMMENT '聊天标题' ,
    `chat_count` INT   COMMENT '聊天次数' ,
    `CREATED_BY` bigint   COMMENT '创建人' ,
    `CREATED_TIME` DATETIME   COMMENT '创建时间' ,
    `UPDATED_BY` bigint   COMMENT '更人' ,
    `UPDATED_TIME` DATETIME   COMMENT '更新时间' 
)  COMMENT = '聊天;';

DROP TABLE IF EXISTS chat_model;
CREATE TABLE chat_model(
    `id` bigint   COMMENT '编号' ,
    `model_name` bigint   COMMENT '模型名字' ,
    `model_type` VARCHAR(32)   COMMENT '模型类型(qianwen、yiyan、xinghuo、openai)' ,
    `model_config` TEXT(128)   COMMENT '模型配置JSON' ,
    `model_image` VARCHAR(255)   COMMENT '模型图片' ,
    `disable` tinyint   COMMENT '禁用 0、正常 1、禁用' ,
    `CREATED_BY` bigint   COMMENT '创建人' ,
    `CREATED_TIME` DATETIME   COMMENT '创建时间' ,
    `UPDATED_BY` bigint   COMMENT '更新人' ,
    `UPDATED_TIME` DATETIME   COMMENT '更新时间' 
)  COMMENT = '聊天模型;';

