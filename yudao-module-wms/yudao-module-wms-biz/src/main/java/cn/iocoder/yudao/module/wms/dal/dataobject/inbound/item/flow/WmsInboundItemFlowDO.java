package cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 入库单库存详情扣减 DO
 * @author 李方捷
 * @table-fields : inbound_id,outbound_action_id,outbound_qty,product_id,id,inbound_item_id,outbound_id,outbound_item_id
 */
@TableName("wms_inbound_item_flow")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_inbound_item_flow_seq")
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
     * 出库单ID
     */
    private Long outboundId;

    /**
     * 出库单明细ID
     */
    private Long outboundItemId;

    /**
     * 变化的数量，出库量
     */
    private Integer outboundQty;

    /**
     * 出库动作ID
     */
    private Long outboundActionId;
}
