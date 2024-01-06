CREATE TABLE product_statistics
(
    id                      bigint AUTO_INCREMENT COMMENT '编号，主键自增' PRIMARY KEY,
    time                    date                                  NOT NULL COMMENT '统计日期',
    spu_id                  bigint                                NOT NULL COMMENT '商品SPU编号',
    browse_count            int         DEFAULT 0                 NOT NULL COMMENT '浏览量',
    browse_user_count       int         DEFAULT 0                 NOT NULL COMMENT '访客量',
    favorite_count          int         DEFAULT 0                 NOT NULL COMMENT '收藏数量',
    cart_count              int         DEFAULT 0                 NOT NULL COMMENT '加购数量',
    order_count             int         DEFAULT 0                 NOT NULL COMMENT '下单件数',
    order_pay_count         int         DEFAULT 0                 NOT NULL COMMENT '支付件数',
    order_pay_price         int         DEFAULT 0                 NOT NULL COMMENT '支付金额，单位：分',
    after_sale_count        int         DEFAULT 0                 NOT NULL COMMENT '退款件数',
    after_sale_refund_price int         DEFAULT 0                 NOT NULL COMMENT '退款金额，单位：分',
    browse_convert_percent  int         DEFAULT 0                 NOT NULL COMMENT '访客支付转化率（百分比）',
    creator                 varchar(64) DEFAULT ''                NULL COMMENT '创建者',
    create_time             datetime    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updater                 varchar(64) DEFAULT ''                NULL COMMENT '更新者',
    update_time             datetime    DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                 bit         DEFAULT b'0'              NOT NULL COMMENT '是否删除',
    tenant_id               bigint      DEFAULT 0                 NOT NULL COMMENT '租户编号'
)
    COMMENT '商品统计表';

CREATE INDEX idx_time
    ON product_statistics (time);

CREATE INDEX idx_spu_id
    ON product_statistics (spu_id);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES ('商品统计', '', 2, 6, 2358, 'product', 'fa:product-hunt', 'statistics/product/index', 'ProductStatistics', 0, true, true, true, '', '2023-12-15 18:54:28', '', '2023-12-15 18:54:33', false);
SELECT @parentId1 := LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES ('商品统计查询', 'statistics:product:query', 3, 1, @parentId, '', '', '', null, 0, true, true, true, '', '2023-09-30 03:22:40', '', '2023-09-30 03:22:40', false);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES ('商品统计导出', 'statistics:product:export', 3, 2, @parentId, '', '', '', null, 0, true, true, true, '', '2023-09-30 03:22:40', '', '2023-09-30 03:22:40', false);
