package cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicatorresult;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicator.MesQcIndicatorDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 检验结果明细记录 DO
 *
 * @author 芋道源码
 */
@TableName("mes_qc_indicator_result_detail")
@KeySequence("mes_qc_indicator_result_detail_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesQcIndicatorResultDetailDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 关联检验结果 ID
     *
     * 关联 {@link MesQcIndicatorResultDO#getId()}
     */
    private Long resultId;
    /**
     * 检测指标 ID
     *
     * 关联 {@link MesQcIndicatorDO#getId()}
     */
    private Long indicatorId;
    /**
     * 检测值（统一存为字符串）
     */
    private String value;
    /**
     * 备注
     */
    private String remark;

}
