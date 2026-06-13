package cn.iocoder.yudao.module.wms.dal.dataobject.order.shipment;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemSkuDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.warehouse.WmsWarehouseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * WMS 出库单明细 DO
 *
 * @author 芋道源码
 */
@TableName("wms_shipment_order_detail")
@KeySequence("wms_shipment_order_detail_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsShipmentOrderDetailDO extends BaseDO {

    /**
     * 主键编号
     */
    @TableId
    private Long id;
    // ========= 单据商品字段 =========

    /**
     * 出库单编号
     *
     * 关联 {@link WmsShipmentOrderDO#getId()}
     */
    private Long orderId;
    /**
     * 商品 SKU 编号
     *
     * 关联 {@link WmsItemSkuDO#getId()}
     */
    private Long skuId;

    // ========= 仓库字段 =========

    /**
     * 仓库编号
     *
     * 关联 {@link WmsWarehouseDO#getId()}
     */
    private Long warehouseId;

    // ========= 数量金额字段 =========

    /**
     * 出库数量
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
