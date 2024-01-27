package cn.iocoder.yudao.module.erp.dal.dataobject.sale;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * ERP 销售订单明细 DO
 *
 * @author 芋道源码
 */
@TableName("erp_sales_order_items")
@KeySequence("erp_sales_order_items_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpSalesOrderItemDO extends BaseDO {

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
     * 商品 SPU 编号
     *
     * TODO 芋艿 关联
     */
    private Long productSpuId;
    /**
     * 商品 SKU 编号
     *
     * TODO 芋艿 关联
     */
    private Long productSkuId;
    /**
     * 商品单位
     *
     * TODO 芋艿 冗余
     */
    private String productUnit;
    /**
     * 商品单价
     *
     * TODO 芋艿 冗余
     */
    private BigDecimal productPrice;

    /**
     * 数量
     */
    private Integer count;
    /**
     * 总价
     */
    private BigDecimal totalPrice;
    /**
     * 备注
     */
    private String description;
    /**
     * 税率，百分比
     */
    private BigDecimal taxPercent;
    /**
     * 税额，单位：元
     */
    private BigDecimal taxPrice;
    /**
     * 支付金额，单位：元
     */
    private BigDecimal payPrice;

}