package cn.iocoder.yudao.module.srm.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP采购申请单子 DO
 *
 * @author 索迈管理员
 */
@TableName("erp_purchase_request_items")
@KeySequence("erp_purchase_request_items_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpPurchaseRequestItemsDO extends TenantBaseDO {
    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * 商品id
     */
    private Long productId;

    //快照
    /**
     * 报关品名
     */
    private String declaredType;

    /**
     * 产品sku
     */
    private String barCode;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 产品单位名称
     */
    private String productUnitName;
    //快照 end
    /**
     * 申请单id
     */
    private Long requestId;
    /**
     * 申请数量//可以考虑换bigDecimal
     */
    private Integer count; //联动申请项采购数量+采购状态
    /**
     * 入库数量
     */
    private BigDecimal inCount;
    /**
     * 仓库编号
     */
    private Long warehouseId;
    /**
     * 批准数量
     */
    private Integer approveCount;
    /**
     * 含税单价
     */
    private BigDecimal actTaxPrice;
    /**
     * 关闭状态
     */
    private Integer offStatus;
    /**
     * 订购状态
     */
    private Integer orderStatus;
    /**
     * 价税合计
     */
    private BigDecimal allAmount;
    /**
     * 参考单价
     */
    private BigDecimal referenceUnitPrice;
    /**
     * 税额，单位：元
     * <p>
     * taxPrice = totalPrice * taxPercent
     */
    private BigDecimal taxPrice;
    /**
     * 税率，百分比
     */
    private BigDecimal taxPercent;
//    /**
//     * ERP 采购订单ID
//     * {@link ErpPurchaseOrderDO#getId()} ()}
//     */
//    private Long purchaseOrderId;
    /**
     * 产品已订购数量
     */
    private Integer orderedQuantity;
//    /**
//     * ERP 采购订单项ID
//     * {@link ErpPurchaseOrderItemDO#getId()} ()}
//     */
//    private Long purchaseOrderItemId;
    /**
     * 期望到货日期
     */
    private LocalDateTime expectArrivalDate;
    /**
     * 入库状态
     */
    private Integer inStatus;
}