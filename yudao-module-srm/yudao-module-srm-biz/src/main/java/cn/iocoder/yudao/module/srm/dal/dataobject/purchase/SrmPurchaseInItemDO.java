package cn.iocoder.yudao.module.srm.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.*;

import java.math.BigDecimal;

/**
 * ERP 采购入库项 DO
 *
 * @author 芋道源码
 */
@TableName("srm_purchase_arrive_item")
@KeySequence("srm_purchase_arrive_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("all")
public class SrmPurchaseInItemDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    @Version
    private Integer version;
    /**
     * 采购入库编号 关联 {@link SrmPurchaseInDO#getId()}
     */
    private Long arriveId;
    /**
     * 仓库编号 关联 {@link cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO#getId()}
     */
    private Long warehouseId;
    /**
     * 产品编号 关联 {@link cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO#getId()}
     */
    private Long productId;
    /**
     * 产品单位 冗余 {@link cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO#getUnitId()}
     */
    private Long productUnitId;
    /**
     * 产品单位名称
     */
    private String productUnitName;
    /**
     * 产品单位单价，单位：元
     */
    private BigDecimal productPrice;
    /**
     * 已付款金额
     */
    private BigDecimal payPrice;
    /**
     * 到货数量
     */
    private BigDecimal qty;
    /**
     * 实际入库数量
     */
    private BigDecimal actualQty;
    /**
     * 实际入库状态
     */
    private Integer inboundStatus;
    /**
     * 总价，单位：元 totalPrice = productPrice * qty
     */
    private BigDecimal totalPrice;
    /**
     * 合计产品价格，单位：元
     */
    private BigDecimal totalProductPrice;
    /**
     * 合计税额
     */
    private BigDecimal totalGrossPrice;
    /**
     * 税率，百分比
     */
    private BigDecimal taxRate;
    /**
     * 税额，单位：元 tax = totalPrice * taxRate
     */
    private BigDecimal tax;
    /**
     * 备注
     */
    private String remark;
    /**
     * 采购订单项编号 关联 {@link SrmPurchaseOrderItemDO#getId()}
     * <p>
     * 目的：方便更新关联的采购订单项的入库数量
     */
    private Long orderItemId;
    /**
     * 采购订单编号 关联 {@link SrmPurchaseOrderDO#getCode()}
     */
    private String orderCode;
    /**
     * 付款状态
     */
    private Integer payStatus;
    /**
     * 含税单价
     */
    private BigDecimal grossPrice;
    /**
     * 价税合计
     */
    private BigDecimal grossTotalPrice;
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
     * 申请人部门id
     */
    private Long applicationDeptId;
    /**
     * 报关品名
     */
    private String declaredType;
    /**
     * 报关品名英文
     */
    private String declaredTypeEn;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * x码
     */
    private String fbaCode;
    /**
     * 箱率 关联 {@link SrmPurchaseOrderItemDO#getContainerRate()}
     */
    private String containerRate;
    /**
     * 产品sku
     */
    private String productCode;
}