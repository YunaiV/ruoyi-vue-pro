package cn.iocoder.yudao.module.srm.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * ERP 采购退货项 DO
 *
 * @author 芋道源码
 */
@TableName("erp_purchase_return_items")
@KeySequence("erp_purchase_return_items_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SrmPurchaseReturnItemDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 采购退货编号
     * <p>
     * 关联 {@link SrmPurchaseReturnDO#getId()}
     */
    private Long returnId;

    /**
     * 入库项id
     */
    private Long inItemId;

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
     * 币种编号
     */
    private Long currencyId;
    /**
     * 币别名称
     */
    private String currencyName;
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
     * <p>
     * totalPrice = productPrice * count
     */
    private BigDecimal totalPrice;
    /**
     * 税率，百分比
     */
    private BigDecimal taxPercent;
    /**
     * 税额，单位：元
     * <p>
     * taxPrice = totalPrice * taxPercent
     */
    private BigDecimal taxPrice;

    /**
     * 备注
     */
    private String remark;

    /**
     * 含税单价
     */
    private BigDecimal actTaxPrice;

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
     * 产品sku
     */
    private String barCode;
    /**
     * 产品名称
     */
    private String productName;
}