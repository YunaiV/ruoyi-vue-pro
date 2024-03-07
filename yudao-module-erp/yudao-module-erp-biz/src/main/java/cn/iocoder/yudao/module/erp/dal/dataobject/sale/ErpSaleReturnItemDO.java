package cn.iocoder.yudao.module.erp.dal.dataobject.sale;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * ERP 销售退货项 DO
 *
 * @author 芋道源码
 */
@TableName("erp_sale_return_items")
@KeySequence("erp_sale_return_items_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpSaleReturnItemDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 销售退货编号
     *
     * 关联 {@link ErpSaleReturnDO##getId()}
     */
    private Long returnId;
    /**
     * 销售订单项编号
     *
     * 关联 {@link ErpSaleOrderItemDO#getId()}
     * 目的：方便更新关联的销售订单项的退货数量
     */
    private Long orderItemId;
    /**
     * 仓库编号
     *
     * 关联 {@link ErpWarehouseDO#getId()}
     */
    private Long warehouseId;
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

}