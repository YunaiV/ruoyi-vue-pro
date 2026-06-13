package cn.iocoder.yudao.module.wms.dal.dataobject.order.movement;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemSkuDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.warehouse.WmsWarehouseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * WMS 移库单明细 DO
 *
 * @author 芋道源码
 */
@TableName("wms_movement_order_detail")
@KeySequence("wms_movement_order_detail_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsMovementOrderDetailDO extends BaseDO {

    /**
     * 主键编号
     */
    @TableId
    private Long id;
    // ========= 单据商品字段 =========

    /**
     * 移库单编号
     *
     * 关联 {@link WmsMovementOrderDO#getId()}
     */
    private Long orderId;
    /**
     * 商品 SKU 编号
     *
     * 关联 {@link WmsItemSkuDO#getId()}
     */
    private Long skuId;

    // ========= 来源仓库字段 =========

    /**
     * 来源仓库编号
     *
     * 关联 {@link WmsWarehouseDO#getId()}
     */
    private Long sourceWarehouseId;

    // ========= 目标仓库字段 =========

    /**
     * 目标仓库编号
     *
     * 关联 {@link WmsWarehouseDO#getId()}
     */
    private Long targetWarehouseId;

    // ========= 数量金额字段 =========

    /**
     * 移库数量
     */
    private BigDecimal quantity;
    /**
     * 单价
     */
    private BigDecimal price;
    /**
     * 行金额（数量 * 单价）
     */
    private BigDecimal totalPrice;

}
