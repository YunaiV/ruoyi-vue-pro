package cn.iocoder.yudao.module.wms.dal.dataobject.stock.flow;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 库存流水 DO
 *
 * @author 李方捷
 */
@TableName("wms_stock_flow")
@KeySequence("wms_stock_flow_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
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
     * 变更量
     */
    private Integer deltaQuantity;
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
     * 待上架数量
     */
    private Integer pendingShelvingQuantity;
    /**
     * 可用量，在库的良品数量
     */
    private Integer availableQuantity;
    /**
     * 可售量，未被单据占用的良品数量
     */
    private Integer sellableQuantity;
    /**
     * 待出库量
     */
    private Integer pendingOutboundQuantity;
    /**
     * 不良品数量
     */
    private Integer defectiveQuantity;
    /**
     * 流水发生的时间
     */
    private LocalDateTime flowTime;

}