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
 * @table-fields : inbound_id,source_item_id,left_quantity,actual_quantity,product_id,plan_quantity,id
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
     * 批次剩余库存，出库后的剩余库存量
     */
    private Integer leftQuantity;

    /**
     * 来源详情ID
     */
    private Long sourceItemId;
}
