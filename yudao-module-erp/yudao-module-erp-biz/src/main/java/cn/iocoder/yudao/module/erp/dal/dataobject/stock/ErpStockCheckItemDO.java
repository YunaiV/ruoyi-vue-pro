package cn.iocoder.yudao.module.erp.dal.dataobject.stock;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * ERP 库存盘点单项 DO
 *
 * @author 芋道源码
 */
@TableName("erp_stock_check_item")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpStockCheckItemDO extends BaseDO {

    /**
     * 盘点项编号
     */
    @TableId
    private Long id;
    /**
     * 盘点编号
     *
     * 关联 {@link ErpStockCheckDO#getId()}
     */
    private Long checkId;
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
     * 产品单位编号
     *
     * 冗余 {@link ErpProductDO#getUnitId()}
     */
    private Long productUnitId;
    /**
     * 产品单价
     */
    private BigDecimal productPrice;
    /**
     * 账面数量（当前库存）
     */
    private BigDecimal stockCount;
    /**
     * 实际数量（实际库存）
     */
    private BigDecimal actualCount;
    /**
     * 盈亏数量
     *
     * count = stockCount - actualCount
     */
    private BigDecimal count;
    /**
     * 合计金额，单位：元
     */
    private BigDecimal totalPrice;
    /**
     * 备注
     */
    private String remark;

}