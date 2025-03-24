package cn.iocoder.yudao.module.erp.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * ERP 采购入库项 DO
 *
 * @author 芋道源码
 */
@TableName("erp_purchase_in_items")
@KeySequence("erp_purchase_in_items_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("all")
public class ErpPurchaseInItemDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 采购入库编号
     * 关联 {@link cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO#getId()}
     */
    private Long inId;

    /**
     * 仓库编号
     * 关联 {@link cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO#getId()}
     */
    private Long warehouseId;
    /**
     * 产品编号
     * 关联 {@link cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO#getId()}
     */
    private Long productId;
    /**
     * 产品单位
     * 冗余 {@link cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO#getUnitId()}
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
    /**
     * 备注
     */
    private String remark;
    /**
     * 箱率
     * 关联 {@link ErpPurchaseOrderItemDO#getContainerRate()}
     */
    private String containerRate;//箱率
    /**
     * 采购订单项编号
     * 关联 {@link ErpPurchaseOrderItemDO#getId()}
     * 目的：方便更新关联的采购订单项的入库数量
     */
    private Long orderItemId;
//    /**
//     * 采购订单编号-展示用(源单单号,采购单)
//     * 关联 {@link ErpPurchaseOrderDO#getNo()}
//     */
//    private String orderNo;

    /**
     * 币别id(财务管理-币别维护)
     */
    private Long currencyId;
    /**
     * 付款状态
     */
    private Integer payStatus;
    /**
     * 含税单价
     */
    private BigDecimal actTaxPrice;
    /**
     * 价税合计
     */
    private BigDecimal allAmount;
    /**
     * 型号规格(产品带出)
     */
    private String model;
    /**
     * 单据来源
     */
    private String source;
    /**
     * 申请人id
     */
    private Long applicantId;
    /**
     * 申请部门id
     * <p>
     * 部门id
     */
    private Long applicationDeptId;
    /**
     * 汇率
     */
    private BigDecimal exchangeRate;

    private String sku;

    private String barCode;
}