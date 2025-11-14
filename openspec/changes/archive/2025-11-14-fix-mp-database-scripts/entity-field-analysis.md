# MP模块实体类字段分析

## 1. MpAccountDO (mp_account)
**继承**: TenantBaseDO
**序列**: mp_account_seq

| 字段名 | Java类型 | MySQL类型 | PostgreSQL类型 | 注释 |
|-------|---------|-----------|---------------|------|
| id | Long | bigint | BIGINT | 编号 |
| name | String | varchar(100) | VARCHAR(100) | 公众号名称 |
| account | String | varchar(100) | VARCHAR(100) | 公众号账号 |
| appId | String | varchar(100) | VARCHAR(100) | 公众号appid |
| appSecret | String | varchar(100) | VARCHAR(100) | 公众号密钥 |
| token | String | varchar(100) | VARCHAR(100) | 公众号token |
| aesKey | String | varchar(300) | VARCHAR(300) | 消息加解密密钥 |
| qrCodeUrl | String | varchar(200) | VARCHAR(200) | 二维码图片URL |
| remark | String | varchar(255) | VARCHAR(255) | 备注 |
| creator | String | varchar(64) | VARCHAR(64) | 创建者 |
| createTime | LocalDateTime | datetime | TIMESTAMP | 创建时间 |
| updater | String | varchar(64) | VARCHAR(64) | 更新者 |
| updateTime | LocalDateTime | datetime | TIMESTAMP | 更新时间 |
| deleted | Boolean | bit(1) | BOOLEAN | 是否删除 |
| tenantId | Long | bigint | BIGINT | 租户编号 |

## 2. MpUserDO (mp_user)
**继承**: BaseDO
**序列**: mp_user_seq
**特殊字段**: tag_ids (LongListTypeHandler)

| 字段名 | Java类型 | MySQL类型 | PostgreSQL类型 | 注释 |
|-------|---------|-----------|---------------|------|
| id | Long | bigint | BIGINT | 编号 |
| openid | String | varchar(100) | VARCHAR(100) | 用户标识 |
| unionId | String | varchar(100) | VARCHAR(100) | 微信生态唯一标识 |
| subscribeStatus | Integer | tinyint | SMALLINT | 关注状态 |
| subscribeTime | LocalDateTime | datetime | TIMESTAMP | 关注时间 |
| unsubscribeTime | LocalDateTime | datetime | TIMESTAMP | 取消关注时间 |
| nickname | String | varchar(64) | VARCHAR(64) | 昵称 |
| headImageUrl | String | varchar(1024) | VARCHAR(1024) | 头像地址 |
| language | String | varchar(30) | VARCHAR(30) | 语言 |
| country | String | varchar(30) | VARCHAR(30) | 国家 |
| province | String | varchar(30) | VARCHAR(30) | 省份 |
| city | String | varchar(30) | VARCHAR(30) | 城市 |
| remark | String | varchar(128) | VARCHAR(128) | 备注 |
| tagIds | List<Long> | varchar(255) | VARCHAR(255) | 标签编号数组 |
| accountId | Long | bigint | BIGINT | 微信公众号ID |
| appId | String | varchar(128) | VARCHAR(128) | 微信公众号appid |
| creator | String | varchar(64) | VARCHAR(64) | 创建者 |
| createTime | LocalDateTime | datetime | TIMESTAMP | 创建时间 |
| updater | String | varchar(64) | VARCHAR(64) | 更新者 |
| updateTime | LocalDateTime | datetime | TIMESTAMP | 更新时间 |
| deleted | Boolean | bit(1) | BOOLEAN | 是否删除 |
| tenantId | Long | bigint | BIGINT | 租户编号 |

## 3. MpTagDO (mp_tag)
**继承**: BaseDO
**序列**: mp_tag_seq
**特殊**: id使用IdType.INPUT

| 字段名 | Java类型 | MySQL类型 | PostgreSQL类型 | 注释 |
|-------|---------|-----------|---------------|------|
| id | Long | bigint | BIGINT | 主键 |
| tagId | Long | bigint | BIGINT | 公众号标签id |
| name | String | varchar(32) | VARCHAR(32) | 标签名称 |
| count | Integer | int | INTEGER | 此标签下粉丝数 |
| accountId | Long | bigint | BIGINT | 公众号账号的编号 |
| appId | String | varchar(128) | VARCHAR(128) | 公众号appId |
| creator | String | varchar(64) | VARCHAR(64) | 创建者 |
| createTime | LocalDateTime | datetime | TIMESTAMP | 创建时间 |
| updater | String | varchar(64) | VARCHAR(64) | 更新者 |
| updateTime | LocalDateTime | datetime | TIMESTAMP | 更新时间 |
| deleted | Boolean | bit(1) | BOOLEAN | 是否删除 |
| tenantId | Long | bigint | BIGINT | 租户编号 |

## 4. MpMenuDO (mp_menu)
**继承**: BaseDO
**序列**: mp_menu_seq
**特殊字段**: reply_articles (JacksonTypeHandler)

| 字段名 | Java类型 | MySQL类型 | PostgreSQL类型 | 注释 |
|-------|---------|-----------|---------------|------|
| id | Long | bigint | BIGINT | 编号 |
| accountId | Long | bigint | BIGINT | 公众号账号的编号 |
| appId | String | varchar(128) | VARCHAR(128) | 公众号appId |
| name | String | varchar(255) | VARCHAR(255) | 菜单名称 |
| menuKey | String | varchar(255) | VARCHAR(255) | 菜单标识 |
| parentId | Long | bigint | BIGINT | 父菜单编号 |
| type | String | varchar(32) | VARCHAR(32) | 按钮类型 |
| url | String | varchar(500) | VARCHAR(500) | 网页链接 |
| miniProgramAppId | String | varchar(32) | VARCHAR(32) | 小程序appId |
| miniProgramPagePath | String | varchar(200) | VARCHAR(200) | 小程序页面路径 |
| articleId | String | varchar(200) | VARCHAR(200) | 跳转图文的媒体编号 |
| replyMessageType | String | varchar(32) | VARCHAR(32) | 消息类型 |
| replyContent | String | varchar(1024) | VARCHAR(1024) | 回复的消息内容 |
| replyMediaId | String | varchar(128) | VARCHAR(128) | 回复的媒体文件id |
| replyMediaUrl | String | varchar(1024) | VARCHAR(1024) | 回复的媒体文件URL |
| replyTitle | String | varchar(128) | VARCHAR(128) | 回复的标题 |
| replyDescription | String | varchar(256) | VARCHAR(256) | 回复的描述 |
| replyThumbMediaId | String | varchar(128) | VARCHAR(128) | 回复的缩略图的媒体id |
| replyThumbMediaUrl | String | varchar(1024) | VARCHAR(1024) | 回复的缩略图的媒体URL |
| replyArticles | List<Article> | varchar(1024) | VARCHAR(1024) | 回复的图文消息数组 |
| replyMusicUrl | String | varchar(1024) | VARCHAR(1024) | 回复的音乐链接 |
| replyHqMusicUrl | String | varchar(1024) | VARCHAR(1024) | 回复的高质量音乐链接 |
| creator | String | varchar(64) | VARCHAR(64) | 创建者 |
| createTime | LocalDateTime | datetime | TIMESTAMP | 创建时间 |
| updater | String | varchar(64) | VARCHAR(64) | 更新者 |
| updateTime | LocalDateTime | datetime | TIMESTAMP | 更新时间 |
| deleted | Boolean | bit(1) | BOOLEAN | 是否删除 |
| tenantId | Long | bigint | BIGINT | 租户编号 |

## 5. MpMessageDO (mp_message)
**继承**: BaseDO
**序列**: mp_message_seq
**特殊字段**: articles (JacksonTypeHandler)

| 字段名 | Java类型 | MySQL类型 | PostgreSQL类型 | 注释 |
|-------|---------|-----------|---------------|------|
| id | Long | bigint | BIGINT | 主键 |
| msgId | Long | bigint | BIGINT | 微信公众号消息id |
| accountId | Long | bigint | BIGINT | 公众号账号的编号 |
| appId | String | varchar(128) | VARCHAR(128) | 公众号appid |
| userId | Long | bigint | BIGINT | 公众号粉丝的编号 |
| openid | String | varchar(100) | VARCHAR(100) | 公众号粉丝标志 |
| type | String | varchar(32) | VARCHAR(32) | 消息类型 |
| sendFrom | Integer | tinyint | SMALLINT | 消息来源 |
| content | String | varchar(1024) | VARCHAR(1024) | 消息内容 |
| mediaId | String | varchar(128) | VARCHAR(128) | 媒体文件的编号 |
| mediaUrl | String | varchar(1024) | VARCHAR(1024) | 媒体文件的URL |
| recognition | String | varchar(1024) | VARCHAR(1024) | 语音识别后文本 |
| format | String | varchar(16) | VARCHAR(16) | 语音格式 |
| title | String | varchar(128) | VARCHAR(128) | 标题 |
| description | String | varchar(256) | VARCHAR(256) | 描述 |
| thumbMediaId | String | varchar(128) | VARCHAR(128) | 缩略图的媒体id |
| thumbMediaUrl | String | varchar(1024) | VARCHAR(1024) | 缩略图的媒体URL |
| url | String | varchar(500) | VARCHAR(500) | 点击图文消息跳转链接 |
| locationX | Double | double | DOUBLE PRECISION | 地理位置维度 |
| locationY | Double | double | DOUBLE PRECISION | 地理位置经度 |
| scale | Double | double | DOUBLE PRECISION | 地图缩放大小 |
| label | String | varchar(128) | VARCHAR(128) | 详细地址 |
| articles | List<Article> | varchar(1024) | VARCHAR(1024) | 图文消息数组 |
| musicUrl | String | varchar(1024) | VARCHAR(1024) | 音乐链接 |
| hqMusicUrl | String | varchar(1024) | VARCHAR(1024) | 高质量音乐链接 |
| event | String | varchar(64) | VARCHAR(64) | 事件类型 |
| eventKey | String | varchar(64) | VARCHAR(64) | 事件Key |
| creator | String | varchar(64) | VARCHAR(64) | 创建者 |
| createTime | LocalDateTime | datetime | TIMESTAMP | 创建时间 |
| updater | String | varchar(64) | VARCHAR(64) | 更新者 |
| updateTime | LocalDateTime | datetime | TIMESTAMP | 更新时间 |
| deleted | Boolean | bit(1) | BOOLEAN | 是否删除 |
| tenantId | Long | bigint | BIGINT | 租户编号 |

## 6. MpAutoReplyDO (mp_auto_reply)
**继承**: BaseDO
**序列**: mp_auto_reply_seq
**特殊字段**: response_articles (JacksonTypeHandler)

| 字段名 | Java类型 | MySQL类型 | PostgreSQL类型 | 注释 |
|-------|---------|-----------|---------------|------|
| id | Long | bigint | BIGINT | 主键 |
| accountId | Long | bigint | BIGINT | 公众号账号的编号 |
| appId | String | varchar(128) | VARCHAR(128) | 公众号appId |
| type | Integer | tinyint | SMALLINT | 回复类型 |
| requestKeyword | String | varchar(255) | VARCHAR(255) | 请求的关键字 |
| requestMatch | Integer | tinyint | SMALLINT | 请求的关键字的匹配 |
| requestMessageType | String | varchar(32) | VARCHAR(32) | 请求的消息类型 |
| responseMessageType | String | varchar(32) | VARCHAR(32) | 回复的消息类型 |
| responseContent | String | varchar(1024) | VARCHAR(1024) | 回复的消息内容 |
| responseMediaId | String | varchar(128) | VARCHAR(128) | 回复的媒体文件id |
| responseMediaUrl | String | varchar(1024) | VARCHAR(1024) | 回复的媒体文件URL |
| responseTitle | String | varchar(128) | VARCHAR(128) | 回复的标题 |
| responseDescription | String | varchar(256) | VARCHAR(256) | 回复的描述 |
| responseThumbMediaId | String | varchar(128) | VARCHAR(128) | 回复的缩略图的媒体id |
| responseThumbMediaUrl | String | varchar(1024) | VARCHAR(1024) | 回复的缩略图的媒体URL |
| responseArticles | List<Article> | varchar(1024) | VARCHAR(1024) | 回复的图文消息数组 |
| responseMusicUrl | String | varchar(1024) | VARCHAR(1024) | 回复的音乐链接 |
| responseHqMusicUrl | String | varchar(1024) | VARCHAR(1024) | 回复的高质量音乐链接 |
| creator | String | varchar(64) | VARCHAR(64) | 创建者 |
| createTime | LocalDateTime | datetime | TIMESTAMP | 创建时间 |
| updater | String | varchar(64) | VARCHAR(64) | 更新者 |
| updateTime | LocalDateTime | datetime | TIMESTAMP | 更新时间 |
| deleted | Boolean | bit(1) | BOOLEAN | 是否删除 |
| tenantId | Long | bigint | BIGINT | 租户编号 |

## 7. MpMaterialDO (mp_material)
**继承**: BaseDO
**序列**: mp_material_seq

| 字段名 | Java类型 | MySQL类型 | PostgreSQL类型 | 注释 |
|-------|---------|-----------|---------------|------|
| id | Long | bigint | BIGINT | 主键 |
| accountId | Long | bigint | BIGINT | 公众号账号的编号 |
| appId | String | varchar(128) | VARCHAR(128) | 公众号appId |
| mediaId | String | varchar(128) | VARCHAR(128) | 公众号素材id |
| type | String | varchar(32) | VARCHAR(32) | 文件类型 |
| permanent | Boolean | bit(1) | BOOLEAN | 是否永久 |
| url | String | varchar(1024) | VARCHAR(1024) | 文件服务器的URL |
| name | String | varchar(255) | VARCHAR(255) | 名字 |
| mpUrl | String | varchar(1024) | VARCHAR(1024) | 公众号文件URL |
| title | String | varchar(255) | VARCHAR(255) | 视频素材的标题 |
| introduction | String | varchar(255) | VARCHAR(255) | 视频素材的描述 |
| creator | String | varchar(64) | VARCHAR(64) | 创建者 |
| createTime | LocalDateTime | datetime | TIMESTAMP | 创建时间 |
| updater | String | varchar(64) | VARCHAR(64) | 更新者 |
| updateTime | LocalDateTime | datetime | TIMESTAMP | 更新时间 |
| deleted | Boolean | bit(1) | BOOLEAN | 是否删除 |
| tenantId | Long | bigint | BIGINT | 租户编号 |

## 枚举类定义

### MpAutoReplyMatchEnum (请求的关键字的匹配)
- 1: 完全匹配
- 2: 半匹配

### MpAutoReplyTypeEnum (回复类型)
- 1: 关注时回复
- 2: 收到消息回复
- 3: 关键词回复

### MpMessageSendFromEnum (消息来源)
- 1: 粉丝发送给公众号
- 2: 公众号发给粉丝

## 注意事项

1. **所有表都包含tenant_id字段**，即使有些DO类继承BaseDO而非TenantBaseDO
2. **MpUserDO中的subscribeStatus**使用CommonStatusEnum，但数据库需要存储整数类型
3. **特殊TypeHandler字段**需要使用足够大的varchar类型存储JSON数据
4. **mp_user表需要设置autoResultMap = true**，因为有TypeHandler字段
5. **mp_menu和mp_message表需要设置autoResultMap = true**，因为有JacksonTypeHandler字段
6. **mp_auto_reply表需要设置autoResultMap = true**，因为有JacksonTypeHandler字段
