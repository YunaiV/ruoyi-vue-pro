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
public class ErpPurchaseReturnItemDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 采购退货编号
     *
     * 关联 {@link ErpPurchaseReturnDO#getId()}
     */
    private Long returnId;

    /**
     * 入库项id
     */
    private Long inItemId;

    /**
     * 仓库编号
     *
     * 关联 {@link cn.iocoder.yudao.module.srm.dal.dataobject.stock.ErpWarehouseDO#getId()}
     */
    private Long warehouseId;
    /**
     * 产品编号
     *
     * 关联 {@link cn.iocoder.yudao.module.srm.dal.dataobject.product.ErpProductDO#getId()}
     */
    private Long productId;
    /**
     * 产品单位单位
     *
     * 冗余 {@link cn.iocoder.yudao.module.srm.dal.dataobject.product.ErpProductDO#getUnitId()}
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
}