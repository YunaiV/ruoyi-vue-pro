package cn.iocoder.yudao.module.erp.dal.dataobject.sale;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * ERP 销售订单项 DO
 *
 * @author 芋道源码
 */
@TableName("erp_sale_order_items")
@KeySequence("erp_sale_order_items_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpSaleOrderItemDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 销售订单编号
     *
     * 关联 {@link ErpSaleOrderDO#getId()}
     */
    private Long orderId;
    /**
     * 产品编号
     *
     * 关联 {@link ErpProductDO#getId()}
     */
    private Long productId;
    /**
     * 产品单位单位
     *
     * 冗余 {@link ErpProductDO#getUnitId()}
     */
    private Long productUnitId;

    /**
     * 产品单位单价，单位：元
     */
    private BigDecimal productPrice;
    /**
     * 数量
     */
    private BigDecimal count;
    /**
     * 总价，单位：元
     *
     * totalPrice = productPrice * count
     */
    private BigDecimal totalPrice;
    /**
     * 税率，百分比
     */
    private BigDecimal taxPercent;
    /**
     * 税额，单位：元
     *
     * taxPrice = totalPrice * taxPercent
     */
    private BigDecimal taxPrice;

    /**
     * 备注
     */
    private String remark;

    // ========== 销售出库 ==========
    /**
     * 销售出库数量
     */
    private BigDecimal outCount;

    // ========== 销售退货（入库）） ==========
    /**
     * 销售退货数量
     */
    private BigDecimal returnCount;

}