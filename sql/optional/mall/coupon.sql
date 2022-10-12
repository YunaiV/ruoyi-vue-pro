DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon`
(
    `id`                        bigint                                                         NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `type`                      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '优惠券类型 reward-满减 discount-折扣 random-随机',
    `name`                      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci   NOT NULL COMMENT '优惠券名称',
    `coupon_type_id`            bigint UNSIGNED                                                         DEFAULT 0 COMMENT '优惠券类型id',
    `coupon_code`               varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '优惠券编码',
    `member_id`                 bigint UNSIGNED                                                NOT NULL DEFAULT 0 COMMENT '领用人',
    `use_order_id`              bigint UNSIGNED                                                NOT NULL DEFAULT 0 COMMENT '优惠券使用订单id',
    `goods_type`                tinyint(1) UNSIGNED                                            NOT NULL DEFAULT 0 COMMENT '适用商品类型1-全部商品可用；2-指定商品可用；3-指定商品不可用',
    `goods_ids`                 varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '适用商品id',
    `at_least`                  decimal(10, 2) UNSIGNED                                        NOT NULL DEFAULT 0 COMMENT '最小金额',
    `money`                     decimal(10, 2) UNSIGNED                                        NOT NULL DEFAULT 0 COMMENT '面额',
    `discount`                  decimal(10, 2) UNSIGNED                                        NOT NULL DEFAULT 0 COMMENT '1 =< 折扣 <= 9.9 当type为discount时需要添加',
    `discount_limit`            decimal(10, 2) UNSIGNED                                        NOT NULL DEFAULT 0 COMMENT '最多折扣金额 当type为discount时可选择性添加',
    `whether_forbid_preference` tinyint(1) UNSIGNED                                            NOT NULL DEFAULT 0 COMMENT '优惠叠加 0-不限制 1- 优惠券仅原价购买商品时可用',
    `whether_expire_notice`     tinyint(1) UNSIGNED                                            NOT NULL DEFAULT 0 COMMENT '是否开启过期提醒0-不开启 1-开启',
    `expire_notice_fixed_term`  int(11) UNSIGNED                                               NOT NULL DEFAULT 0 COMMENT '过期前N天提醒',
    `whether_noticed`           tinyint(1) UNSIGNED                                            NOT NULL DEFAULT 0 COMMENT '是否已提醒',
    `state`                     tinyint(4) UNSIGNED                                            NOT NULL DEFAULT 0 COMMENT '优惠券状态 1已领用（未使用） 2已使用 3已过期',
    `get_type`                  tinyint(1) UNSIGNED                                            NOT NULL DEFAULT 0 COMMENT '获取方式1订单2.直接领取3.活动领取 4转赠 5分享获取',
    `fetch_time`                datetime                                                       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    `use_time`                  datetime                                                       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '使用时间',
    `start_time`                datetime                                                       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '可使用的开始时间',
    `end_time`                  datetime                                                       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '有效期结束时间',
    `creator`                   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci   NULL     DEFAULT '' COMMENT '创建者',
    `create_time`               datetime                                                       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`                   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci   NULL     DEFAULT '' COMMENT '更新者',
    `update_time`               datetime                                                       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`                   bit(1)                                                         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`                 bigint                                                         NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 119
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '优惠券';

DROP TABLE IF EXISTS `coupon_templete`;
CREATE TABLE `coupon_templete`
(
    `id`                        bigint                                                       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `type`                      varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优惠券类型 reward-满减 discount-折扣 random-随机',
    `name`                      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优惠券名称',
    `coupon_name_remark`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '名称备注',
    `image`                     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '优惠券图片',
    `count`                     int(11)                                                      NOT NULL DEFAULT 0 COMMENT '发放数量',
    `lead_count`                int(11)                                                      NOT NULL DEFAULT 0 COMMENT '已领取数量',
    `used_count`                int(11) UNSIGNED                                             NOT NULL DEFAULT 0 COMMENT '已使用数量',
    `goods_type`                tinyint(1) UNSIGNED                                          NOT NULL DEFAULT 1 COMMENT '适用商品类型1-全部商品可用；2-指定商品可用；3-指定商品不可用',
    `product_ids`               varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '适用商品id',
    `has_use_limit`             tinyint(1) UNSIGNED                                          NOT NULL DEFAULT 0 COMMENT '使用门槛0-无门槛 1-有门槛',
    `at_least`                  decimal(10, 2)                                               NOT NULL DEFAULT 0 COMMENT '满多少元使用 0代表无限制',
    `money`                     decimal(10, 2)                                               NOT NULL DEFAULT 0 COMMENT '发放面额 当type为reward时需要添加',
    `discount`                  decimal(10, 2) UNSIGNED                                      NOT NULL DEFAULT 0 COMMENT '1 =< 折扣 <= 9.9 当type为discount时需要添加',
    `discount_limit`            decimal(10, 2)                                               NOT NULL DEFAULT 0 COMMENT '最多折扣金额 当type为discount时可选择性添加',
    `min_money`                 decimal(10, 2) UNSIGNED                                      NOT NULL DEFAULT 0 COMMENT '最低金额 当type为radom时需要添加',
    `max_money`                 decimal(10, 2) UNSIGNED                                      NOT NULL DEFAULT 0 COMMENT '最大金额 当type为radom时需要添加',
    `validity_type`             tinyint(1) UNSIGNED                                          NOT NULL DEFAULT 0 COMMENT '过期类型1-时间范围过期 2-领取之日固定日期后过期 3-领取次日固定日期后过期',
    `start_use_time`            datetime COMMENT '使用开始日期 过期类型1时必填',
    `end_use_time`              datetime COMMENT '使用结束日期 过期类型1时必填',
    `fixed_term`                int(11) UNSIGNED                                             NOT NULL DEFAULT 0 COMMENT '当validity_type为2或者3时需要添加 领取之日起或者次日N天内有效',
    `whether_limitless`         tinyint(1) UNSIGNED                                          NOT NULL DEFAULT 0 COMMENT '是否无限制0-否 1是',
    `max_fetch`                 int(11) UNSIGNED                                             NOT NULL DEFAULT 0 COMMENT '每人最大领取个数',
    `whether_expire_notice`     tinyint(1) UNSIGNED                                          NOT NULL DEFAULT 0 COMMENT '是否开启过期提醒0-不开启 1-开启',
    `expire_notice_fixed_term`  int(11) UNSIGNED                                             NOT NULL DEFAULT 0 COMMENT '过期前N天提醒',
    `whether_forbid_preference` tinyint(1) UNSIGNED                                          NOT NULL DEFAULT 0 COMMENT '优惠叠加 0-不限制 1- 优惠券仅原价购买商品时可用',
    `whether_show`              int(11) UNSIGNED                                             NOT NULL DEFAULT 0 COMMENT '是否显示',
    `discount_order_money`      decimal(10, 2) UNSIGNED                                      NOT NULL DEFAULT 0 COMMENT '订单的优惠总金额',
    `order_money`               decimal(10, 2) UNSIGNED                                      NOT NULL DEFAULT 0 COMMENT '用券总成交额',
    `whether_forbidden`         tinyint(1) UNSIGNED                                          NOT NULL DEFAULT 0 COMMENT '是否禁止发放0-否 1-是',
    `order_goods_num`           int(11) UNSIGNED                                             NOT NULL DEFAULT 0 COMMENT '使用优惠券购买的商品数量',
    `status`                    tinyint(11)                                                  NOT NULL DEFAULT 0 COMMENT '状态（1进行中2已结束-1已关闭）',
    `end_time`                  datetime COMMENT '有效日期结束时间',
    `creator`                   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL     DEFAULT '' COMMENT '创建者',
    `create_time`               datetime                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`                   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL     DEFAULT '' COMMENT '更新者',
    `update_time`               datetime                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`                   bit(1)                                                       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`                 bigint                                                       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 119
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '优惠券模板';