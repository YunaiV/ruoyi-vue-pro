package cn.iocoder.yudao.module.mes.dal.dataobject.qc.rqc;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicator.MesQcIndicatorDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 退货检验行 DO
 *
 * @author 芋道源码
 */
@TableName("mes_qc_rqc_line")
@KeySequence("mes_qc_rqc_line_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesQcRqcLineDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 退货检验单 ID
     *
     * 关联 {@link MesQcRqcDO#getId()}
     */
    private Long rqcId;
    /**
     * 检测指标 ID
     *
     * 关联 {@link MesQcIndicatorDO#getId()}
     */
    private Long indicatorId;
    /**
     * 检测工具
     *
     * 冗余 {@link MesQcIndicatorDO#getTool()}
     */
    private String tool;
    /**
     * 检测方法
     */
    private String checkMethod;
    /**
     * 标准值
     */
    private BigDecimal standardValue;
    /**
     * 计量单位 ID
     *
     * 关联 {@link MesMdUnitMeasureDO#getId()}
     */
    private Long unitMeasureId;
    /**
     * 误差上限
     */
    private BigDecimal maxThreshold;
    /**
     * 误差下限
     */
    private BigDecimal minThreshold;
    /**
     * 致命缺陷数量
     */
    private Integer criticalQuantity;
    /**
     * 严重缺陷数量
     */
    private Integer majorQuantity;
    /**
     * 轻微缺陷数量
     */
    private Integer minorQuantity;
    /**
     * 备注
     */
    private String remark;

}
