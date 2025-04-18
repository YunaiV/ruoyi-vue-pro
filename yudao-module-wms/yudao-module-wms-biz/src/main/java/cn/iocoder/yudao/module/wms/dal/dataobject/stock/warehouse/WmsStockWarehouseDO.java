package cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 仓库库存 DO
 * @author 李方捷
 * @table-fields : outbound_pending_qty,product_id,shelving_pending_qty,available_qty,purchase_transit_qty,id,defective_qty,return_transit_qty,sellable_qty,purchase_plan_qty,warehouse_id
 */
@TableName("wms_stock_warehouse")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_stock_warehouse_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsStockWarehouseDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
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
}
