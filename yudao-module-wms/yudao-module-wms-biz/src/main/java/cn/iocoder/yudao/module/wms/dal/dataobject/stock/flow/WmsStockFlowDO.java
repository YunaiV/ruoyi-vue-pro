package cn.iocoder.yudao.module.wms.dal.dataobject.stock.flow;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import java.sql.Timestamp;

/**
 * 库存流水 DO
 * @author 李方捷
 * @table-fields : reason,outbound_pending_qty,delta_qty,flow_time,available_qty,next_flow_id,purchase_transit_qty,stock_id,stock_type,product_id,shelving_pending_qty,id,reason_bill_id,defective_qty,return_transit_qty,sellable_qty,purchase_plan_qty,prev_flow_id,reason_item_id,warehouse_id
 */
@TableName("wms_stock_flow")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_stock_flow_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsStockFlowDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 库存类型
     */
    private Integer stockType;

    /**
     * 库存ID，分别指向三张库存表的ID
     */
    private Long stockId;

    /**
     * 流水发生的原因
     */
    private Integer reason;

    /**
     * 流水触发的单据ID
     */
    private Long reasonBillId;

    /**
     * 流水触发的单据下对应的明细ID
     */
    private Long reasonItemId;

    /**
     * 前一个流水ID
     */
    private Long prevFlowId;

    /**
     * 流水发生的时间
     */
    private Timestamp flowTime;

    /**
     * 上一个流水ID
     */
    private Long nextFlowId;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 可用量，在库的良品数量
     */
    private Integer availableQty;

    /**
     * 不良品数量
     */
    private Integer defectiveQty;

    /**
     * 变更量
     */
    private Integer deltaQty;

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
     * 待上架数量
     */
    private Integer shelvingPendingQty;
}
