package cn.iocoder.yudao.module.wms.dal.dataobject.order.check;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemSkuDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.warehouse.WmsWarehouseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * WMS 盘库单明细 DO
 *
 * @author 芋道源码
 */
@TableName("wms_check_order_detail")
@KeySequence("wms_check_order_detail_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsCheckOrderDetailDO extends BaseDO {

    /**
     * 主键编号
     */
    @TableId
    private Long id;
    // ========= 单据商品字段 =========

    /**
     * 盘库单编号
     *
     * 关联 {@link WmsCheckOrderDO#getId()}
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
    /**
     * 库存编号
     *
     * 关联 {@link WmsInventoryDO#getId()}
     */
    private Long inventoryId;

    /**
     * 入库时间
     */
    private LocalDateTime receiptTime;

    // ========= 数量金额字段 =========

    /**
     * 账面数量
     */
    private BigDecimal quantity;
    /**
     * 实盘数量
     */
    private BigDecimal checkQuantity;
    /**
     * 单价
     */
    private BigDecimal price;

}
