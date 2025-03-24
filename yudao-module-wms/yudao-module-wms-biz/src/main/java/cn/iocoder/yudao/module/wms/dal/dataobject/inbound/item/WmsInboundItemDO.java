package cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item;

import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.ErpProductRespSimpleVO;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 入库单详情 DO
 * @author 李方捷
 * @table-fields : inbound_id,outbound_available_quantity,source_item_id,inbound_status,actual_quantity,product_id,plan_quantity,shelved_quantity,id
 */
@TableName("wms_inbound_item")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_inbound_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsInboundItemDO extends BaseDO {

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
     * 标准产品ID
     */
    private Long productId;

    /**
     * 计划入库量
     */
    private Integer planQuantity;

    /**
     * 实际入库量
     */
    private Integer actualQuantity;

    /**
     * 来源详情ID
     */
    private Long sourceItemId;

    /**
     * 入库状态 ; InboundStatus : 0-未入库 , 1-部分入库 , 2-已入库
     */
    private Integer inboundStatus;

    /**
     * 已上架量，已经拣货到仓位的库存量
     */
    private Integer shelvedQuantity;

    /**
     * 批次剩余库存，出库后的剩余库存量
     */
    private Integer outboundAvailableQuantity;
}
