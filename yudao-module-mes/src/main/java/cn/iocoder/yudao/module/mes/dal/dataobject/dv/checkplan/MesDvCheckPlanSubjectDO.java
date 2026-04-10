package cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkplan;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.subject.MesDvSubjectDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 点检保养方案项目 DO
 *
 * @author 芋道源码
 */
@TableName("mes_dv_check_plan_subject")
@KeySequence("mes_dv_check_plan_subject_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesDvCheckPlanSubjectDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 方案编号
     *
     * 关联 {@link MesDvCheckPlanDO#getId()}
     */
    private Long planId;
    /**
     * 项目编号
     *
     * 关联 {@link MesDvSubjectDO#getId()}
     */
    private Long subjectId;
    /**
     * 备注
     */
    private String remark;

}
