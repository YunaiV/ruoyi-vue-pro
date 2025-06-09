package cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 入库单库存详情扣减 DO
 * @author 李方捷
 * @table-fields : bill_id,outbound_available_qty,shelve_closed_qty,inbound_id,outbound_available_delta_qty,outbound_action_id,actual_qty,bill_item_id,product_id,bill_type,id,inbound_item_id,direction
 */
@TableName("wms_item_flow")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_item_flow_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsInboundItemFlowDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 入库单ID
     */
    private Long inboundId;

    /**
     * 入库单明细ID
     */
    private Long inboundItemId;

    /**
     * 标准产品ID
     */
    private Long productId;

    /**
     * 出库动作ID
     */
    private Long outboundActionId;

    /**
     * 单据类型
     */
    private Integer billType;

    /**
     * 出库单ID
     */
    private Long billId;

    /**
     * 出库单明细ID
     */
    private Long billItemId;

    /**
     * 出入方向
     */
    private Integer direction;

    /**
     * 变化的数量，可出库量的变化量
     */
    private Integer outboundAvailableDeltaQty;

    /**
     * 可出库量
     */
    private Integer outboundAvailableQty;

    /**
     * 实际入库量
     */
    private Integer actualQty;

    /**
     * 已上架量，已经拣货到仓位的库存量
     */
    private Integer shelveClosedQty;
}
