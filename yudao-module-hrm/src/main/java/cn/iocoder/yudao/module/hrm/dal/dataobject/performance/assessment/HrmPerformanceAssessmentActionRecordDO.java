package cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentActionTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentStageStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * HRM 员工绩效考核动作记录 DO
 *
 * @author 芋道源码
 */
@TableName(value = "hrm_performance_assessment_action_record", autoResultMap = true)
@KeySequence("hrm_performance_assessment_action_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HrmPerformanceAssessmentActionRecordDO extends BaseDO {

    /**
     * 动作记录编号
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
     * 操作员工编号
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    private Long employeeId;
    /**
     * 动作类型
     *
     * 枚举 {@link HrmPerformanceAssessmentActionTypeEnum}
     */
    private Integer type;
    /**
     * 动作标题
     */
    private String title;
    /**
     * 动作内容
     */
    private String content;
    /**
     * 附件地址列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> fileUrls;
    /**
     * 动作完成后的阶段状态
     *
     * 枚举 {@link HrmPerformanceAssessmentStageStatusEnum}
     */
    private Integer status;

}
