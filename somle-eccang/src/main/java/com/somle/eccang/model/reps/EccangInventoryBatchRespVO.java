package com.somle.eccang.model.reps;

import lombok.Data;

import java.time.LocalDateTime;

@Data
//eccang-InventoryBatch 响应体
public class EccangInventoryBatchRespVO {

    /**
     * 批次库存id
     */
    private Integer ibId;

    /**
     * 仓库Id
     */
    private Integer warehouseId;

    /**
     * 产品SKU代码
     */
    private String productSku;

    /**
     * 库位
     */
    private String lcCode;

    /**
     * 可用数量
     */
    private Integer ibQuantity;

    /**
     * 待出数量
     */
    private Integer outQuantity;

    /**
     * 参考单号
     */
    private Integer referenceNo;

    /**
     * 入库单号
     */
    private Integer receivingCode;

    /**
     * 采购单号
     */
    private Integer poCode;

    /**
     * 库存状态:
     * 0已用完/不可用
     * 1可用
     */
    private Integer ibStatus;

    /**
     * 锁状态:
     * 0无
     * 1盘点锁
     * 2借领用锁
     */
    private Integer ibHoldStatus;

    /**
     * 库存类型:
     * 0标准
     * 1不良品
     */
    private Integer ibType;

    /**
     * 上架时间
     */
    private LocalDateTime ibFifoTime;

    /**
     * 更新时间
     */
    private LocalDateTime ibUpdateTime;

    /**
     * 库龄
     */
    private Integer age;

    /**
     * 是否需要报关:
     * 0否
     * 1是
     */
    private Integer isNeedDeclare;

    /**
     * 采购成本
     */
    private Float unitPrice;

    /**
     * 采购税费
     */
    private Float purchaseTaxationFee;

    /**
     * 采购运费
     */
    private Float purchaseShipFee;

    /**
     * 头程运费
     */
    private Float shippingFee;

    /**
     * 头程关税
     */
    private Float tariffFee;

    /**
     * 币种
     */
    private String currencyCode;
}
