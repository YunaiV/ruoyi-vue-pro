package cn.iocoder.yudao.module.mes.dal.dataobject.wm.productsales;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.oqc.MesQcOqcDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch.MesWmBatchDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.materialstock.MesWmMaterialStockDO;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmQualityStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 销售出库单行 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_product_sales_line")
@KeySequence("mes_wm_product_sales_line_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmProductSalesLineDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 出库单ID
     *
     * 关联 {@link MesWmProductSalesDO#getId()}
     */
    private Long salesId;
    /**
     * 物料ID
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 出库数量
     */
    private BigDecimal quantity;
    /**
     * 批次 ID
     *
     * 关联 {@link MesWmBatchDO#getId()}
     */
    private Long batchId;
    /**
     * 批次号
     *
     * 关联 {@link MesWmBatchDO#getCode()}
     */
    private String batchCode;
    /**
     * 库存记录ID
     *
     * 关联 {@link MesWmMaterialStockDO#getId()}
     */
    private Long materialStockId;
    /**
     * 是否出厂检验
     */
    private Boolean oqcCheckFlag;
    /**
     * 出厂检验单 ID
     *
     * 关联 {@link MesQcOqcDO#getId()}
     */
    private Long oqcId;
    /**
     * 质量状态
     *
     * 枚举 {@link MesWmQualityStatusEnum}
     */
    private Integer qualityStatus;
    /**
     * 备注
     */
    private String remark;

}
