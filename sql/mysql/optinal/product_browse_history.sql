CREATE TABLE product_browse_history
(
    id           bigint AUTO_INCREMENT COMMENT '记录编号'
        PRIMARY KEY,
    user_id      bigint                                NOT NULL COMMENT '用户编号',
    spu_id       bigint                                NOT NULL COMMENT '商品 SPU 编号',
    user_deleted bit         DEFAULT b'0'              NOT NULL COMMENT '用户是否删除',
    creator      varchar(64) DEFAULT ''                NULL COMMENT '创建者',
    create_time  datetime    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updater      varchar(64) DEFAULT ''                NULL COMMENT '更新者',
    update_time  datetime    DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      bit         DEFAULT b'0'              NOT NULL COMMENT '是否删除',
    tenant_id    bigint      DEFAULT 0                 NOT NULL COMMENT '租户编号'
)
    COMMENT '商品浏览记录表';

CREATE INDEX idx_spuId
    ON product_browse_history (spu_id);

CREATE INDEX idx_userId
    ON product_browse_history (user_id);

