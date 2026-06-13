package cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.plan;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmStockTakingPlanParamTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 盘点方案参数 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_stock_taking_plan_param")
@KeySequence("mes_wm_stock_taking_plan_param_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmStockTakingPlanParamDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 盘点方案编号
     *
     * 关联 {@link MesWmStockTakingPlanDO#getId()}
     */
    private Long planId;

    /**
     * 参数值类型
     *
     * 字典 {@link DictTypeConstants#MES_WM_STOCK_TAKING_PLAN_PARAM_TYPE}
     * 枚举 {@link MesWmStockTakingPlanParamTypeEnum}
     */
    private Integer type;

    /**
     * 参数值编号，例如仓库、库区、库位、物料、批次的主键 ID
     */
    private Long valueId;
    /**
     * 参数值编码，例如仓库编码、库区编码、库位编码、物料编码、批次编码
     */
    private String valueCode;
    /**
     * 参数值名称，例如仓库名称、库区名称、库位名称、物料名称、批次名称
     */
    private String valueName;

    /**
     * 备注
     */
    private String remark;

}
