DROP TABLE IF EXISTS erp_account;
CREATE TABLE erp_account(
                            `id` BIGINT AUTO_INCREMENT COMMENT '结算账户编号',
                            `name` VARCHAR (50) COMMENT '账户名称',
                            `no` VARCHAR (50) COMMENT '账户编码',
                            `remark` VARCHAR (50) COMMENT '备注',
                            `status` INT COMMENT '开启状态',
                            `sort` INT COMMENT '排序',
                            `default_status` TINYINT (3) COMMENT '是否默认',
                            `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                            `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                            PRIMARY KEY (id) USING BTREE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '结算账户';

DROP TABLE IF EXISTS erp_finance_payment;
CREATE TABLE erp_finance_payment(

                                    `id` BIGINT AUTO_INCREMENT COMMENT '编号',
                                    `no` VARCHAR(50) COMMENT '付款单号',
                                    `status` INT COMMENT '付款状态',
                                    `payment_time` DATETIME COMMENT '付款时间',
                                    `finance_user_id` BIGINT COMMENT '财务人员编号关联',
                                    `supplier_id` BIGINT COMMENT '供应商编号',
                                    `account_id` BIGINT COMMENT '付款账户编号',
                                    `total_price` DECIMAL(13,4) COMMENT '合计价格，单位：元',
                                    `discount_price` DECIMAL(13,4) COMMENT '优惠金额，单位：元',
                                    `payment_price` DECIMAL(13,4) COMMENT '实付金额，单位：分',
                                    `remark` VARCHAR (50) COMMENT '备注',

                                    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                    `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                    PRIMARY KEY (id) USING BTREE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '付款单';

DROP TABLE IF EXISTS erp_finance_payment_item;
CREATE TABLE erp_finance_payment_item(

                                         id BIGINT AUTO_INCREMENT COMMENT '入库项编号',
                                         payment_id BIGINT COMMENT '付款单编号',
                                         biz_type INT COMMENT '业务类型',
                                         biz_id BIGINT COMMENT '业务编号',
                                         biz_no VARCHAR (50) COMMENT '业务单号',
                                         total_price DECIMAL(13,4) COMMENT '应付金额，单位：分',
                                         paid_price DECIMAL(13,4) COMMENT '已付金额，单位：分',
                                         payment_price DECIMAL(13,4) COMMENT '本次付款，单位：分',
                                         remark VARCHAR(50) COMMENT '备注',

                                         `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                         `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                         `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                         PRIMARY KEY (id) USING BTREE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '付款项';

DROP TABLE IF EXISTS erp_finance_receipt;
CREATE TABLE erp_finance_receipt(
                                    `id` BIGINT AUTO_INCREMENT COMMENT '编号',
                                    `no` VARCHAR(50) COMMENT '收款单号',
                                    `status` INT  COMMENT '收款状态',
                                    `receipt_time` DATETIME COMMENT '收款时间',
                                    `finance_user_id` BIGINT COMMENT '财务人员编号',
                                    `customer_id` BIGINT COMMENT '客户编号',
                                    `account_id` BIGINT COMMENT '收款账户编号',
                                    `total_price` DECIMAL(13,4) COMMENT '合计价格，单位：元',
                                    `discount_price` DECIMAL(13,4) COMMENT '优惠金额，单位：元',
                                    `receipt_price` DECIMAL(13,4) COMMENT '实付金额，单位：分',
                                    `remark` VARCHAR(50) COMMENT '备注',

                                    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                    `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                    PRIMARY KEY (id) USING BTREE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '收款单';

DROP TABLE IF EXISTS erp_finance_receipt_item;
CREATE TABLE erp_finance_receipt_item(
                                         id BIGINT (15) AUTO_INCREMENT COMMENT '入库项编号',
                                         receipt_id BIGINT (15) COMMENT '收款单编号',
                                         biz_type INT (11) COMMENT '业务类型',
                                         biz_id BIGINT (15) COMMENT '业务编号',
                                         biz_no VARCHAR (50) COMMENT '业务单号',
                                         total_price DECIMAL (13,4) COMMENT '应收金额，单位：分',
                                         receipted_price DECIMAL (13,4) COMMENT '已收金额，单位：分',
                                         receipt_price DECIMAL (13,4) COMMENT '本次收款，单位：分',
                                         remark VARCHAR (50) COMMENT '备注',

                                         `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                         `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                         `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                         PRIMARY KEY (id) USING BTREE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '收款项';

DROP TABLE IF EXISTS erp_product_category;
CREATE TABLE erp_product_category(
                                     id BIGINT (15) AUTO_INCREMENT COMMENT '分类编号',
                                     parent_id BIGINT (15) COMMENT '父分类编号',
                                     `name` VARCHAR (50) COMMENT '分类名称',
                                     code VARCHAR (50) COMMENT '分类编码',
                                     sort INT (11) COMMENT '分类排序',
                                     `status` INT (11) COMMENT '开启状态',

                                     `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                     PRIMARY KEY (id) USING BTREE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '产品分类';

DROP TABLE IF EXISTS erp_product;
CREATE TABLE erp_product(
                            id BIGINT (15) AUTO_INCREMENT COMMENT '产品编号',
                            `name` VARCHAR (50) COMMENT '产品名称',
                            bar_code VARCHAR (50) COMMENT '产品条码',
                            category_id BIGINT (15) COMMENT '产品分类编号',
                            unit_id BIGINT (15) COMMENT '单位编号',
                            `status` INT (11) COMMENT '产品状态',
                            `standard` VARCHAR (50) COMMENT '产品规格',
                            remark VARCHAR (50) COMMENT '产品备注',
                            expiry_day INT (11) COMMENT '保质期天数',
                            weight DECIMAL (13,4) COMMENT '基础重量（kg）',
                            purchase_price DECIMAL (13,4) COMMENT '采购价格，单位：元',
                            sale_price DECIMAL (13,4) COMMENT '销售价格，单位：元',
                            min_price DECIMAL (13,4) COMMENT '最低价格，单位：元',

                            `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                            PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '产品';

DROP TABLE IF EXISTS erp_product_unit;
CREATE TABLE erp_product_unit(
                                 id BIGINT (15) NOT NULL AUTO_INCREMENT COMMENT '单位编号',
                                 `name` VARCHAR (50) NOT NULL COMMENT '单位名字',
                                 `status` INT (11) NOT NULL COMMENT '单位状态',

                                 `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                                 PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '产品单位';

DROP TABLE IF EXISTS erp_purchase_in;
CREATE TABLE erp_purchase_in(
                                id BIGINT (15) AUTO_INCREMENT COMMENT '编号',
                                `no` VARCHAR (50) COMMENT '采购入库单号',
                                `status` INT (11) COMMENT '入库状态',
                                supplier_id BIGINT (15) COMMENT '供应商编号',
                                account_id BIGINT (15) COMMENT '结算账户编号',
                                in_time DATETIME COMMENT '入库时间',
                                order_id BIGINT (15) COMMENT '采购订单编号',
                                order_no VARCHAR (50) COMMENT '采购订单号',
                                total_count DECIMAL (13,4) COMMENT '合计数量',
                                total_price DECIMAL (13,4) COMMENT '最终合计价格，单位：元 totalPrice = totalProductPrice + totalTaxPrice - discountPrice + otherPrice',
                                payment_price DECIMAL (13,4) COMMENT '已支付金额，单位：元',
                                total_product_price DECIMAL (13,4) COMMENT '合计产品价格，单位：元',
                                total_tax_price DECIMAL (13,4) COMMENT '合计税额，单位：元',
                                discount_percent DECIMAL (13,4) COMMENT '优惠率，百分比',
                                discount_price DECIMAL (13,4) COMMENT '优惠金额，单位：元 discountPrice = (totalProductPrice + totalTaxPrice) * discountPercent',
                                other_price DECIMAL (13,4) COMMENT '其它金额，单位：元',
                                file_url VARCHAR (50) COMMENT '附件地址',
                                remark VARCHAR (50) COMMENT '备注',

                                `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                                PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '采购入库';

DROP TABLE IF EXISTS erp_purchase_in_items;
CREATE TABLE erp_purchase_in_items(
                                      id BIGINT (15) AUTO_INCREMENT COMMENT '编号',
                                      in_id BIGINT (15) COMMENT '采购入库编号',
                                      order_item_id BIGINT (15) COMMENT '采购订单项编号',
                                      warehouse_id BIGINT (15) COMMENT '仓库编号',
                                      product_id BIGINT (15) COMMENT '产品编号',
                                      product_unit_id BIGINT (15) COMMENT '产品单位单位',
                                      product_price DECIMAL (13,4) COMMENT '产品单位单价，单位：元',
                                      `count` DECIMAL (13,4) COMMENT '数量',
                                      total_price DECIMAL (13,4) COMMENT '总价，单位：元 totalPrice = productPrice * count',
                                      tax_percent DECIMAL (13,4) COMMENT '税率，百分比',
                                      tax_price DECIMAL (13,4) COMMENT '税额，单位：元 taxPrice = totalPrice * taxPercent',
                                      remark VARCHAR (50) COMMENT '备注',

                                      `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                                      PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '采购入库项';

DROP TABLE IF EXISTS erp_purchase_order;
CREATE TABLE erp_purchase_order(
                                   id BIGINT (15) AUTO_INCREMENT COMMENT '编号',
                                   `no` VARCHAR (50) COMMENT '采购订单号',
                                   `status` INT (11) COMMENT '采购状态',
                                   supplier_id BIGINT (15) COMMENT '供应商编号',
                                   account_id BIGINT (15) COMMENT '结算账户编号',
                                   order_time DATETIME COMMENT '下单时间',
                                   total_count DECIMAL (13,4) COMMENT '合计数量',
                                   total_price DECIMAL (13,4) COMMENT '最终合计价格，单位：元 totalPrice = totalProductPrice + totalTaxPrice - discountPrice',
                                   total_product_price DECIMAL (13,4) COMMENT '合计产品价格，单位：元',
                                   total_tax_price DECIMAL (13,4) COMMENT '合计税额，单位：元',
                                   discount_percent DECIMAL (13,4) COMMENT '优惠率，百分比',
                                   discount_price DECIMAL (13,4) COMMENT '优惠金额，单位：元 discountPrice = (totalProductPrice + totalTaxPrice) * discountPercent',
                                   deposit_price DECIMAL (13,4) COMMENT '定金金额，单位：元',
                                   file_url VARCHAR (50) COMMENT '附件地址',
                                   remark VARCHAR (50) COMMENT '备注',
                                   in_count DECIMAL (13,4) default 0 COMMENT '采购入库数量',
                                   return_count DECIMAL (13,4) default 0 COMMENT '采购退货数量',

                                   `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                                   PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '采购订单';

DROP TABLE IF EXISTS erp_purchase_order_items;
CREATE TABLE erp_purchase_order_items(
                                         id BIGINT (15) AUTO_INCREMENT COMMENT '编号',
                                         order_id BIGINT (15) COMMENT '采购订单编号',
                                         product_id BIGINT (15) COMMENT '产品编号',
                                         product_unit_id BIGINT (15) COMMENT '产品单位单位',
                                         product_price DECIMAL (13,4) COMMENT '产品单位单价，单位：元',
                                         `count` DECIMAL (13,4) COMMENT '数量',
                                         total_price DECIMAL (13,4) COMMENT '总价，单位：元 totalPrice = productPrice * count',
                                         tax_percent DECIMAL (13,4) COMMENT '税率，百分比',
                                         tax_price DECIMAL (13,4) COMMENT '税额，单位：元 taxPrice = totalPrice * taxPercent',
                                         remark VARCHAR (50) COMMENT '备注',
                                         in_count DECIMAL (13,4) default 0 COMMENT '采购入库数量',
                                         return_count DECIMAL (13,4) default 0 COMMENT '采购退货数量',

                                         `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                         `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                                         PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '采购订单项';

DROP TABLE IF EXISTS erp_purchase_return;
CREATE TABLE erp_purchase_return(
                                    id BIGINT (15) AUTO_INCREMENT COMMENT '编号',
                                    `no` VARCHAR (50) COMMENT '采购退货单号',
                                    `status` INT (11) COMMENT '退货状态',
                                    supplier_id BIGINT (15) COMMENT '供应商编号',
                                    account_id BIGINT (15) COMMENT '结算账户编号',
                                    return_time DATETIME COMMENT '退货时间',
                                    order_id BIGINT (15) COMMENT '采购订单编号',
                                    order_no VARCHAR (50) COMMENT '采购订单号',
                                    total_count DECIMAL (13,4) COMMENT '合计数量',
                                    total_price DECIMAL (13,4) COMMENT '最终合计价格，单位：元 totalPrice = totalProductPrice + totalTaxPrice - discountPrice + otherPrice',
                                    refund_price DECIMAL (13,4) COMMENT '已退款金额',
                                    total_product_price DECIMAL (13,4) COMMENT '合计产品价格，单位：元',
                                    total_tax_price DECIMAL (13,4) COMMENT '合计税额，单位：元',
                                    discount_percent DECIMAL (13,4) COMMENT '优惠率，百分比',
                                    discount_price DECIMAL (13,4) COMMENT '优惠金额，单位：元 discountPrice = (totalProductPrice + totalTaxPrice) * discountPercent',
                                    other_price DECIMAL (13,4) COMMENT '其它金额，单位：元',
                                    file_url VARCHAR (50) COMMENT '附件地址',
                                    remark VARCHAR (50) COMMENT '备注',

                                    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                                    PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '采购退货';

DROP TABLE IF EXISTS erp_purchase_return_items;
CREATE TABLE erp_purchase_return_items(
                                          id BIGINT (15) AUTO_INCREMENT COMMENT '编号',
                                          return_id BIGINT (15) COMMENT '采购退货编号',
                                          order_item_id BIGINT (15) COMMENT '采购订单项编号',
                                          warehouse_id BIGINT (15) COMMENT '仓库编号',
                                          product_id BIGINT (15) COMMENT '产品编号',
                                          product_unit_id BIGINT (15) COMMENT '产品单位单位',
                                          product_price DECIMAL (13,4) COMMENT '产品单位单价，单位：元',
                                          `count` DECIMAL (13,4) COMMENT '数量',
                                          total_price DECIMAL (13,4) COMMENT '总价，单位：元 totalPrice = productPrice * count',
                                          tax_percent DECIMAL (13,4) COMMENT '税率，百分比',
                                          tax_price DECIMAL (13,4) COMMENT '税额，单位：元 taxPrice = totalPrice * taxPercent',
                                          remark VARCHAR (50) COMMENT '备注',

                                          `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                          `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                          `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                                          PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '采购退货项';

DROP TABLE IF EXISTS erp_supplier;
CREATE TABLE erp_supplier(
                             id BIGINT (15) AUTO_INCREMENT COMMENT '供应商编号',
                             `name` VARCHAR (50) COMMENT '供应商名称',
                             contact VARCHAR (50) COMMENT '联系人',
                             mobile VARCHAR (50) COMMENT '手机号码',
                             telephone VARCHAR (50) COMMENT '联系电话',
                             email VARCHAR (50) COMMENT '电子邮箱',
                             fax VARCHAR (50) COMMENT '传真',
                             remark VARCHAR (50) COMMENT '备注',
                             `status` INT (11) COMMENT '开启状态',
                             sort INT (11) COMMENT '排序',
                             tax_no VARCHAR (50) COMMENT '纳税人识别号',
                             tax_percent DECIMAL (13,4) COMMENT '税率',
                             bank_name VARCHAR (50) COMMENT '开户行',
                             bank_account VARCHAR (50) COMMENT '开户账号',
                             bank_address VARCHAR (50) COMMENT '开户地址',

                             `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                             PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '供应商';

DROP TABLE IF EXISTS erp_customer;
CREATE TABLE erp_customer(
                             id BIGINT (15) AUTO_INCREMENT COMMENT '客户编号',
                             `name` VARCHAR (50) COMMENT '客户名称',
                             contact VARCHAR (50) COMMENT '联系人',
                             mobile VARCHAR (50) COMMENT '手机号码',
                             telephone VARCHAR (50) COMMENT '联系电话',
                             email VARCHAR (50) COMMENT '电子邮箱',
                             fax VARCHAR (50) COMMENT '传真',
                             remark VARCHAR (50) COMMENT '备注',
                             `status` INT (11) COMMENT '开启状态',
                             sort INT (11) COMMENT '排序',
                             tax_no VARCHAR (50) COMMENT '纳税人识别号',
                             tax_percent DECIMAL (13,4) COMMENT '税率',
                             bank_name VARCHAR (50) COMMENT '开户行',
                             bank_account VARCHAR (50) COMMENT '开户账号',
                             bank_address VARCHAR (50) COMMENT '开户地址',

                             `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                             PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '客户';

DROP TABLE IF EXISTS erp_sale_order;
CREATE TABLE erp_sale_order(
                               id BIGINT (15) AUTO_INCREMENT COMMENT '编号',
                               `no` VARCHAR (50) COMMENT '销售订单号',
                               `status` INT (11) COMMENT '销售状态',
                               customer_id BIGINT (15) COMMENT '客户编号',
                               account_id BIGINT (15) COMMENT '结算账户编号',
                               sale_user_id BIGINT (15) COMMENT '销售员编号',
                               order_time DATETIME COMMENT '下单时间',
                               total_count DECIMAL (13,4) COMMENT '合计数量',
                               total_price DECIMAL (13,4) COMMENT '最终合计价格，单位：元 totalPrice = totalProductPrice + totalTaxPrice - discountPrice',
                               total_product_price DECIMAL (13,4) COMMENT '合计产品价格，单位：元',
                               total_tax_price DECIMAL (13,4) COMMENT '合计税额，单位：元',
                               discount_percent DECIMAL (13,4) COMMENT '优惠率，百分比',
                               discount_price DECIMAL (13,4) COMMENT '优惠金额，单位：元 discountPrice = (totalProductPrice + totalTaxPrice) * discountPercent',
                               deposit_price DECIMAL (13,4) COMMENT '定金金额，单位：元',
                               file_url VARCHAR (50) COMMENT '附件地址',
                               remark VARCHAR (50) COMMENT '备注',
                               out_count DECIMAL (13,4) default 0 COMMENT '销售出库数量',
                               return_count DECIMAL (13,4) default 0 COMMENT '销售退货数量',

                               `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                               PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '销售订单';

DROP TABLE IF EXISTS erp_sale_order_items;
CREATE TABLE erp_sale_order_items(
                                     id BIGINT (15) AUTO_INCREMENT COMMENT '编号',
                                     order_id BIGINT (15) COMMENT '销售订单编号',
                                     product_id BIGINT (15) COMMENT '产品编号',
                                     product_unit_id BIGINT (15) COMMENT '产品单位单位',
                                     product_price DECIMAL (13,4) COMMENT '产品单位单价，单位：元',
                                     `count` DECIMAL (13,4) COMMENT '数量',
                                     total_price DECIMAL (13,4) COMMENT '总价，单位：元 totalPrice = productPrice * count',
                                     tax_percent DECIMAL (13,4) COMMENT '税率，百分比',
                                     tax_price DECIMAL (13,4) COMMENT '税额，单位：元 taxPrice = totalPrice * taxPercent',
                                     remark VARCHAR (50) COMMENT '备注',
                                     out_count DECIMAL (13,4) default 0 COMMENT '销售出库数量',
                                     return_count DECIMAL (13,4) default 0 COMMENT '销售退货数量',

                                     `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                                     PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '销售订单项';

DROP TABLE IF EXISTS erp_sale_out;
CREATE TABLE erp_sale_out(
                             id BIGINT (15) AUTO_INCREMENT COMMENT '编号',
                             `no` VARCHAR (50) COMMENT '销售出库单号',
                             `status` INT (11) COMMENT '出库状态',
                             customer_id BIGINT (15) COMMENT '客户编号',
                             account_id BIGINT (15) COMMENT '结算账户编号',
                             sale_user_id BIGINT (15) COMMENT '销售员编号',
                             out_time DATETIME COMMENT '出库时间',
                             order_id BIGINT (15) COMMENT '销售订单编号',
                             order_no VARCHAR (50) COMMENT '销售订单号',
                             total_count DECIMAL (13,4) COMMENT '合计数量',
                             total_price DECIMAL (13,4) COMMENT '最终合计价格，单位：元 totalPrice = totalProductPrice + totalTaxPrice - discountPrice + otherPrice',
                             receipt_price DECIMAL (13,4) COMMENT '已收款金额，单位：元',
                             total_product_price DECIMAL (13,4) COMMENT '合计产品价格，单位：元',
                             total_tax_price DECIMAL (13,4) COMMENT '合计税额，单位：元',
                             discount_percent DECIMAL (13,4) COMMENT '优惠率，百分比',
                             discount_price DECIMAL (13,4) COMMENT '优惠金额，单位：元 discountPrice = (totalProductPrice + totalTaxPrice) * discountPercent',
                             other_price DECIMAL (13,4) COMMENT '其它金额，单位：元',
                             file_url VARCHAR (50) COMMENT '附件地址',
                             remark VARCHAR (50) COMMENT '备注',

                             `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                             PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '销售出库';

DROP TABLE IF EXISTS erp_sale_out_items;
CREATE TABLE erp_sale_out_items(
                                   id BIGINT (15) AUTO_INCREMENT COMMENT '编号',
                                   out_id BIGINT (15) COMMENT '销售出库编号',
                                   order_item_id BIGINT (15) COMMENT '销售订单项编号',
                                   warehouse_id BIGINT (15) COMMENT '仓库编号',
                                   product_id BIGINT (15) COMMENT '产品编号',
                                   product_unit_id BIGINT (15) COMMENT '产品单位单位',
                                   product_price DECIMAL (13,4) COMMENT '产品单位单价，单位：元',
                                   `count` DECIMAL (13,4) COMMENT '数量',
                                   total_price DECIMAL (13,4) COMMENT '总价，单位：元 totalPrice = productPrice * count',
                                   tax_percent DECIMAL (13,4) COMMENT '税率，百分比',
                                   tax_price DECIMAL (13,4) COMMENT '税额，单位：元 taxPrice = totalPrice * taxPercent',
                                   remark VARCHAR (50) COMMENT '备注',

                                   `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                                   PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '销售出库项';

DROP TABLE IF EXISTS erp_sale_return;
CREATE TABLE erp_sale_return(
                                id BIGINT (15) AUTO_INCREMENT COMMENT '编号',
                                `no` VARCHAR (50) COMMENT '销售退货单号',
                                `status` INT (11) COMMENT '退货状态',
                                customer_id BIGINT (15) COMMENT '客户编号',
                                account_id BIGINT (15) COMMENT '结算账户编号',
                                sale_user_id BIGINT (15) COMMENT '销售员编号',
                                return_time DATETIME COMMENT '退货时间',
                                order_id BIGINT (15) COMMENT '销售订单编号',
                                order_no VARCHAR (50) COMMENT '销售订单号',
                                total_count DECIMAL (13,4) COMMENT '合计数量',
                                total_price DECIMAL (13,4) COMMENT '最终合计价格，单位：元 totalPrice = totalProductPrice + totalTaxPrice - discountPrice + otherPrice',
                                refund_price DECIMAL (13,4) COMMENT '已退款金额，单位：元',
                                total_product_price DECIMAL (13,4) COMMENT '合计产品价格，单位：元',
                                total_tax_price DECIMAL (13,4) COMMENT '合计税额，单位：元',
                                discount_percent DECIMAL (13,4) COMMENT '优惠率，百分比',
                                discount_price DECIMAL (13,4) COMMENT '优惠金额，单位：元 discountPrice = (totalProductPrice + totalTaxPrice) * discountPercent',
                                other_price DECIMAL (13,4) COMMENT '其它金额，单位：元',
                                file_url VARCHAR (50) COMMENT '附件地址',
                                remark VARCHAR (50) COMMENT '备注',

                                `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                                PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '销售退货';

DROP TABLE IF EXISTS erp_sale_return_items;
CREATE TABLE erp_sale_return_items(
                                      id BIGINT (15) AUTO_INCREMENT COMMENT '编号',
                                      return_id BIGINT (15) COMMENT '销售退货编号',
                                      order_item_id BIGINT (15) COMMENT '销售订单项编号',
                                      warehouse_id BIGINT (15) COMMENT '仓库编号',
                                      product_id BIGINT (15) COMMENT '产品编号',
                                      product_unit_id BIGINT (15) COMMENT '产品单位单位',
                                      product_price DECIMAL (13,4) COMMENT '产品单位单价，单位：元',
                                      `count` DECIMAL (13,4) COMMENT '数量',
                                      total_price DECIMAL (13,4) COMMENT '总价，单位：元 totalPrice = productPrice * count',
                                      tax_percent DECIMAL (13,4) COMMENT '税率，百分比',
                                      tax_price DECIMAL (13,4) COMMENT '税额，单位：元 taxPrice = totalPrice * taxPercent',
                                      remark VARCHAR (50) COMMENT '备注',

                                      `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                                      PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '销售退货项';

DROP TABLE IF EXISTS erp_stock_check;
CREATE TABLE erp_stock_check(
                                id BIGINT (15) AUTO_INCREMENT COMMENT '盘点编号',
                                `no` VARCHAR (50) COMMENT '盘点单号',
                                check_time DATETIME COMMENT '盘点时间',
                                total_count DECIMAL (13,4) COMMENT '合计数量',
                                total_price DECIMAL (13,4) COMMENT '合计金额，单位：元',
                                `status` INT (11) COMMENT '状态',
                                remark VARCHAR (50) COMMENT '备注',
                                file_url VARCHAR (50) COMMENT '附件 URL',

                                `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                                PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '库存盘点单';

DROP TABLE IF EXISTS erp_stock_check_item;
CREATE TABLE erp_stock_check_item(
                                     id BIGINT (15) AUTO_INCREMENT COMMENT '盘点项编号',
                                     check_id BIGINT (15) COMMENT '盘点编号',
                                     warehouse_id BIGINT (15) COMMENT '仓库编号',
                                     product_id BIGINT (15) COMMENT '产品编号',
                                     product_unit_id BIGINT (15) COMMENT '产品单位编号',
                                     product_price DECIMAL (13,4) COMMENT '产品单价',
                                     stock_count DECIMAL (13,4) COMMENT '账面数量（当前库存）',
                                     actual_count DECIMAL (13,4) COMMENT '实际数量（实际库存）',
                                     `count` DECIMAL (13,4) COMMENT '盈亏数量 count = stockCount - actualCount',
                                     total_price DECIMAL (13,4) COMMENT '合计金额，单位：元',
                                     remark VARCHAR (50) COMMENT '备注',

                                     `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                                     PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '库存盘点单项';

DROP TABLE IF EXISTS erp_stock;
CREATE TABLE erp_stock(
                          id BIGINT (15) AUTO_INCREMENT COMMENT '编号',
                          product_id BIGINT (15) COMMENT '产品编号',
                          warehouse_id BIGINT (15) COMMENT '仓库编号',
                          `count` DECIMAL (13,4) COMMENT '库存数量',

                          `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                          `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                          PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '产品库存';

DROP TABLE IF EXISTS erp_stock_in;
CREATE TABLE erp_stock_in(
                             id BIGINT (15) AUTO_INCREMENT COMMENT '入库编号',
                             `no` VARCHAR (50) COMMENT '入库单号',
                             supplier_id BIGINT (15) COMMENT '供应商编号',
                             in_time DATETIME COMMENT '入库时间',
                             total_count DECIMAL (13,4) COMMENT '合计数量',
                             total_price DECIMAL (13,4) COMMENT '合计金额，单位：元',
                             `status` INT (11) COMMENT '状态',
                             remark VARCHAR (50) COMMENT '备注',
                             file_url VARCHAR (50) COMMENT '附件 URL',

                             `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                             PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '其它入库单';

DROP TABLE IF EXISTS erp_stock_in_item;
CREATE TABLE erp_stock_in_item(
                                  id BIGINT (15) AUTO_INCREMENT COMMENT '入库项编号',
                                  in_id BIGINT (15) COMMENT '入库编号',
                                  warehouse_id BIGINT (15) COMMENT '仓库编号',
                                  product_id BIGINT (15) COMMENT '产品编号',
                                  product_unit_id BIGINT (15) COMMENT '产品单位编号',
                                  product_price DECIMAL (13,4) COMMENT '产品单价',
                                  `count` DECIMAL (13,4) COMMENT '产品数量',
                                  total_price DECIMAL (13,4) COMMENT '合计金额，单位：元',
                                  remark VARCHAR (50) COMMENT '备注',

                                  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                                  PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '其它入库单项';

DROP TABLE IF EXISTS erp_stock_move;
CREATE TABLE erp_stock_move(
                               id BIGINT (15) AUTO_INCREMENT COMMENT '调拨编号',
                               `no` VARCHAR (50) COMMENT '调拨单号',
                               move_time DATETIME COMMENT '调拨时间',
                               total_count DECIMAL (13,4) COMMENT '合计数量',
                               total_price DECIMAL (13,4) COMMENT '合计金额，单位：元',
                               `status` INT (11) COMMENT '状态',
                               remark VARCHAR (50) COMMENT '备注',
                               file_url VARCHAR (50) COMMENT '附件 URL',

                               `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                               PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '库存调拨单';

DROP TABLE IF EXISTS erp_stock_move_item;
CREATE TABLE erp_stock_move_item(
                                    id BIGINT (15) AUTO_INCREMENT COMMENT '调拨项编号',
                                    move_id BIGINT (15) COMMENT '调拨编号',
                                    from_warehouse_id BIGINT (15) COMMENT '调出仓库编号',
                                    to_warehouse_id BIGINT (15) COMMENT '调入仓库编号',
                                    product_id BIGINT (15) COMMENT '产品编号',
                                    product_unit_id BIGINT (15) COMMENT '产品单位编号',
                                    product_price DECIMAL (13,4) COMMENT '产品单价',
                                    `count` DECIMAL (13,4) COMMENT '产品数量',
                                    total_price DECIMAL (13,4) COMMENT '合计金额，单位：元',
                                    remark VARCHAR (50) COMMENT '备注',

                                    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                                    PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '库存调拨单项';

DROP TABLE IF EXISTS erp_stock_out;
CREATE TABLE erp_stock_out(
                              id BIGINT (15) AUTO_INCREMENT COMMENT '出库编号',
                              `no` VARCHAR (50) COMMENT '出库单号',
                              customer_id BIGINT (15) COMMENT '客户编号',
                              out_time DATETIME COMMENT '出库时间',
                              total_count DECIMAL (13,4) COMMENT '合计数量',
                              total_price DECIMAL (13,4) COMMENT '合计金额，单位：元',
                              `status` INT (11) COMMENT '状态',
                              remark VARCHAR (50) COMMENT '备注',
                              file_url VARCHAR (50) COMMENT '附件 URL',

                              `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                              `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                              PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '其它出库单';

DROP TABLE IF EXISTS erp_stock_out_item;
CREATE TABLE erp_stock_out_item(
                                   id BIGINT (15) AUTO_INCREMENT COMMENT '出库项编号',
                                   out_id BIGINT (15) COMMENT '出库编号',
                                   warehouse_id BIGINT (15) COMMENT '仓库编号',
                                   product_id BIGINT (15) COMMENT '产品编号',
                                   product_unit_id BIGINT (15) COMMENT '产品单位编号',
                                   product_price DECIMAL (13,4) COMMENT '产品单价',
                                   `count` DECIMAL (13,4) COMMENT '产品数量',
                                   total_price DECIMAL (13,4) COMMENT '合计金额，单位：元',
                                   remark VARCHAR (50) COMMENT '备注',

                                   `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                                   PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '其它出库单项';

DROP TABLE IF EXISTS erp_stock_record;
CREATE TABLE erp_stock_record(
                                 id BIGINT (15) AUTO_INCREMENT COMMENT '编号',
                                 product_id BIGINT (15) COMMENT '产品编号',
                                 warehouse_id BIGINT (15) COMMENT '仓库编号',
                                 `count` DECIMAL (13,4) COMMENT '出入库数量',
                                 total_count DECIMAL (13,4) COMMENT '总库存量',
                                 biz_type INT (11) COMMENT '业务类型',
                                 biz_id BIGINT (15) COMMENT '业务编号',
                                 biz_item_id BIGINT (15) COMMENT '业务项编号',
                                 biz_no VARCHAR (50) COMMENT '业务单号',

                                 `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                                 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                                 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                                 PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '产品库存明细';

DROP TABLE IF EXISTS erp_warehouse;
CREATE TABLE erp_warehouse(
                              id BIGINT (15) AUTO_INCREMENT COMMENT '仓库编号',
                              `name` VARCHAR (50) COMMENT '仓库名称',
                              address VARCHAR (50) COMMENT '仓库地址',
                              sort BIGINT (15) COMMENT '排序',
                              remark VARCHAR (50) COMMENT '备注',
                              principal VARCHAR (50) COMMENT '负责人',
                              warehouse_price DECIMAL (13,4) COMMENT '仓储费，单位：元',
                              truckage_price DECIMAL (13,4) COMMENT '搬运费，单位：元',
                              `status` INT (11) COMMENT '开启状态',
                              default_status TINYINT (3) COMMENT '是否默认',

                              `creator` varchar(64) DEFAULT '' COMMENT '创建者',
                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `updater` varchar(64) DEFAULT '' COMMENT '更新者',
                              `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',`tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',

                              PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '仓库';
