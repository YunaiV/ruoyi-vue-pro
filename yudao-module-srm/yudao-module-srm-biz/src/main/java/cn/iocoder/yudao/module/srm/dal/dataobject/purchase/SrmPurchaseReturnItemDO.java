package cn.iocoder.yudao.module.srm.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.*;

import java.math.BigDecimal;

/**
 * ERP 采购退货项 DO
 *
 * @author 芋道源码
 */
@TableName("srm_purchase_return_item")
@KeySequence("srm_purchase_return_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SrmPurchaseReturnItemDO extends TenantBaseDO {
    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 乐观锁
     */
    @Version
    private Integer version;
    /**
     * 采购退货编号
     * <p>
     * 关联 {@link SrmPurchaseReturnDO#getId()}
     */
    private Long returnId;
    /**
     * 到货项id
     */
    private Long arriveItemId;
    /**
     * 入库单code
     */
    private String arriveCode;
    /**
     * 仓库编号
     * <p>
     */
    private Long warehouseId;
    /**
     * 产品编号
     * <p>
     */
    private Long productId;
    /**
     * 产品单位单位
     * <p>
     */
    private Long productUnitId;
    /**
     * 产品单位单价，单位：元
     */
    private BigDecimal productPrice;
    /**
     * 产品单位名称
     */
    private String productUnitName;
    /**
     * 数量
     */
    private BigDecimal qty;
    /**
     * 总价，单位：元
     * <p>
     * totalPrice = productPrice * count
     */
    private BigDecimal totalPrice;
    /**
     * 税率，百分比
     */
    private BigDecimal taxRate;
    /**
     * 税额，单位：元
     * <p>
     * tax = totalPrice * taxRate
     */
    private BigDecimal tax;

    /**
     * 备注
     */
    private String remark;

    /**
     * 含税单价
     */
    private BigDecimal grossPrice;

    /**
     * 箱率
     */
    private String containerRate;

    /**
     * 申请人id
     */
    private Long applicantId;

    /**
     * 申请部门id
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
     * 产品sku
     */
    private String productCode;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 出库状态
     */
    private Integer outboundStatus;
    /**
     * 出库数量
     */
    private BigDecimal outboundQty;
    /**
     * 实际入库数量,取到货单item的实际入库数量
     */
    private BigDecimal actualQty;
}