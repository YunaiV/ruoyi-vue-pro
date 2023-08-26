-- 1.冗余 SKU 图片地址, 规格
alter table product_comment
    add column sku_pic_url varchar(256) not null comment '图片地址' after sku_id;

alter table product_comment
    add column sku_properties varchar(512) null
        comment '属性数组，JSON 格式 [{propertId: , valueId: }, {propertId: , valueId: }]' after sku_pic_url;

-- 2.修复已有数据
update product_comment pc
    join product_sku ps on pc.spu_id = ps.spu_id
set pc.sku_pic_url    = ps.pic_url,
    pc.sku_properties = ps.properties
where pc.sku_id is not null;