package cn.iocoder.yudao.module.erp.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP 采购订单项 DO
 * <p>
 * 包含采购订单的具体条目信息，例如产品、单价、数量等。
 *
 * @author wdy
 */
@TableName("erp_purchase_order_items")
@KeySequence("erp_purchase_order_items_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpPurchaseOrderItemDO extends BaseDO {
    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 供应商产品编码
     * <p>
     * 关联 {@link ErpSupplierProductDO#getId()}
     */
//    private Long supplierProductId;
    /**
     * 仓库编号
     * <p>
     * 关联  {@link cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO#getId()}
     */
    private Long warehouseId; // 仓库编号
    /**
     * 采购订单编号
     * <p>
     * 关联 {@link ErpPurchaseOrderDO#getId()}
     */
    private Long orderId;
    /**
     * 产品编号
     * <p>
     * 关联 {@link ErpProductDO#getId()}
     */
    private Long productId;
    /**
     * 产品单位单位
     * <p>
     * 冗余 {@link ErpProductDO#getUnitId()}
     */
    private Long productUnitId;
    /**
     * 备注
     */
    private String remark;
    // ========== 合计 ==========
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
     * totalPrice = productPrice * count
     */
    private BigDecimal totalPrice;
    /**
     * 税率，百分比
     */
    private BigDecimal taxPercent;
    /**
     * 税额，单位：元
     * taxPrice = totalPrice * taxPercent
     */
    private BigDecimal taxPrice;
    // ========== 采购金额和数量 ==========
    /**
     * 合计产品价格，单位：元
     */
    private BigDecimal totalProductPrice;
    /**
     * 合计产品税价，单位：元
     */
    private BigDecimal totalTaxPrice;
    /**
     * 优惠率，百分比
     */
    private BigDecimal discountPercent;
    /**
     * 优惠金额，单位：元
     */
    private BigDecimal discountPrice;
    /**
     * 定金金额，单位：元
     */
    private BigDecimal depositPrice;
    /**
     * 应付款余额，查询时显示，新增时无需填写
     */
    private BigDecimal payableBalance;
    // ========== 采购入库 ==========
    /**
     * 采购入库数量
     */
    private BigDecimal inCount;
    // ========== 采购退货（出库）） ==========
    /**
     * 采购退货数量
     */
    private BigDecimal returnCount;
    // ========== 其他 ==========
    private String xCode;
    /**
     * 交货日期
     */
    private LocalDateTime deliveryTime;
    /**
     * 关闭状态
     */
    private Integer offStatus;
    //执行状态 Integer

    //入库状态
    //付款状态
}