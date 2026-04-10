package cn.iocoder.yudao.module.mes.dal.dataobject.qc.template;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicator.MesQcIndicatorDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 质检方案-检测指标项 DO
 *
 * @author 芋道源码
 */
@TableName("mes_qc_template_indicator")
@KeySequence("mes_qc_template_indicator_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesQcTemplateIndicatorDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 质检方案编号
     *
     * 关联 {@link MesQcTemplateDO#getId()}
     */
    private Long templateId;
    /**
     * 质检指标编号
     *
     * 关联 {@link MesQcIndicatorDO#getId()}
     */
    private Long indicatorId;
    /**
     * 检测方法
     */
    private String checkMethod;
    /**
     * 标准值
     */
    private BigDecimal standardValue;
    /**
     * 计量单位编号
     *
     * 关联 {@link MesMdUnitMeasureDO#getId()}
     */
    private Long unitMeasureId;
    /**
     * 误差上限
     */
    private BigDecimal thresholdMax;
    /**
     * 误差下限
     */
    private BigDecimal thresholdMin;
    /**
     * 说明图 URL
     */
    private String docUrl;
    /**
     * 备注
     */
    private String remark;

}
