package cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnsales;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 销售退货单行 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_return_sales_line")
@KeySequence("mes_wm_return_sales_line_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmReturnSalesLineDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 退货单 ID
     *
     * 关联 {@link MesWmReturnSalesDO#getId()}
     */
    private Long returnId;
    /**
     * 物料 ID
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 退货数量
     */
    private BigDecimal quantity;
    /**
     * 批次 ID
     */
    private Long batchId;
    /**
     * 批次号
     */
    private String batchCode;
    /**
     * 退货检验单 ID
     */
    private Long rqcId;
    /**
     * 是否需要质检
     */
    private Boolean rqcCheckFlag;
    /**
     * 质量状态
     *
     * 字典 {@link DictTypeConstants#MES_WM_QUALITY_STATUS}
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.wm.MesWmQualityStatusEnum}
     */
    private Integer qualityStatus;
    /**
     * 备注
     */
    private String remark;

}
