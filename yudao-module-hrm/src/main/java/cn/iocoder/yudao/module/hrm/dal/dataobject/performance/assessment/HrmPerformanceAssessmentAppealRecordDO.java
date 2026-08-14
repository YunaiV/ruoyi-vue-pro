package cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAppealRecordStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * HRM 员工绩效考核申诉记录 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_performance_assessment_appeal_record")
@KeySequence("hrm_performance_assessment_appeal_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HrmPerformanceAssessmentAppealRecordDO extends BaseDO {

    /**
     * 申诉记录编号
     */
    @TableId
    private Long id;
    /**
     * 员工绩效考核编号
     *
     * 关联 {@link HrmPerformanceAssessmentDO#getId()}
     */
    private Long assessmentId;
    /**
     * 员工绩效考核阶段编号
     *
     * 关联 {@link HrmPerformanceAssessmentStageDO#getId()}
     */
    private Long stageId;
    /**
     * 处理状态
     *
     * 枚举 {@link HrmPerformanceAppealRecordStatusEnum}
     */
    private Integer status;

}
