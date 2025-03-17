package cn.iocoder.yudao.module.wms.dal.dataobject.outbound.item;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 出库单详情 DO
 *
 * @author 李方捷
 */
@TableName("wms_outbound_item")
@KeySequence("wms_outbound_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsOutboundItemDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 入库单ID
     */
    private Long outboundId;
    /**
     * 标准产品ID
     */
    private Long productId;
    /**
     * 标准产品SKU
     */
    private String productSku;
    /**
     * 预期量
     */
    private Integer expectedQuantity;
    /**
     * 实际量
     */
    private Integer actualQuantity;
    /**
     * 来源详情ID
     */
    private Long sourceItemId;

}