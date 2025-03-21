package cn.iocoder.yudao.module.wms.dal.dataobject.pickup.item;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 拣货单详情 DO
 * @author 李方捷
 * @table-fields : inbound_id,quantity,bin_id,product_id,id,inbound_item_id,pickup_id
 */
@TableName("wms_pickup_item")
// 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@KeySequence("wms_pickup_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsPickupItemDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 拣货单ID
     */
    private Long pickupId;

    /**
     * 入库单ID
     */
    private Long inboundId;

    /**
     * 入库单明细ID
     */
    private Long inboundItemId;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 拣货数量
     */
    private Integer quantity;

    /**
     * 仓位ID，拣货到目标仓位
     */
    private Long binId;
}
