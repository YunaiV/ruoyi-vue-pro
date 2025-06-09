package cn.iocoder.yudao.module.wms.api.inbound.dto;

import lombok.Data;

/**
 * @table-fields : tenant_id,creator,create_time,outbound_pending_qty,available_qty,purchase_transit_qty,updater,update_time,product_id,shelving_pending_qty,id,defective_qty,return_transit_qty,sellable_qty,purchase_plan_qty,warehouse_id
 */
@Data
public class WmsStockWarehouseSimpleDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 可用量，在库的良品数量
     */
    private Integer availableQty;

    /**
     * 不良品数量
     */
    private Integer defectiveQty;

    /**
     * 待出库量
     */
    private Integer outboundPendingQty;

    /**
     * 采购计划量
     */
    private Integer purchasePlanQty;

    /**
     * 采购在途量
     */
    private Integer purchaseTransitQty;

    /**
     * 退件在途数量
     */
    private Integer returnTransitQty;

    /**
     * 可售量，未被单据占用的良品数量
     */
    private Integer sellableQty;

    /**
     * 待上架数量，上架是指从拣货区上架到货架
     */
    private Integer shelvingPendingQty;

    /**
     * 库存总量，在库的库存总量
     */
    private Integer totalQty;


}
