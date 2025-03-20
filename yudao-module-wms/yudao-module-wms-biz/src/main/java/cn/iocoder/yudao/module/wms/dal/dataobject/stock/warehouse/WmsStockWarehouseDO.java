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
 * @table-fields : available_quantity,defective_quantity,outbound_pending_quantity,purchase_transit_quantity,product_id,shelving_pending_quantity,sellable_quantity,id,return_transit_quantity,purchase_plan_quantity,warehouse_id
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
     * 采购计划量
     */
    private Integer purchasePlanQuantity;

    /**
     * 采购在途量
     */
    private Integer purchaseTransitQuantity;

    /**
     * 退件在途数量
     */
    private Integer returnTransitQuantity;

    /**
     * 可用量，在库的良品数量
     */
    private Integer availableQuantity;

    /**
     * 可售量，未被单据占用的良品数量
     */
    private Integer sellableQuantity;

    /**
     * 不良品数量
     */
    private Integer defectiveQuantity;

    /**
     * 待上架数量
     */
    private Integer shelvingPendingQuantity;

    /**
     * 待出库量
     */
    private Integer outboundPendingQuantity;
}
