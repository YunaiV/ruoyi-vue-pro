package cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.candidate;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.LongListTypeHandler;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.enums.DictTypeConstants;
import cn.iocoder.yudao.module.hrm.enums.recruit.candidate.HrmRecruitInterviewResultEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HRM 招聘面试记录 DO
 *
 * @author 芋道源码
 */
@TableName(value = "hrm_recruit_interview", autoResultMap = true)
@KeySequence("hrm_recruit_interview_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmRecruitInterviewDO extends BaseDO {

    /**
     * 面试编号
     */
    @TableId
    private Long id;
    /**
     * 候选人编号
     *
     * 关联 {@link HrmRecruitCandidateDO#getId()}
     */
    private Long candidateId;
    /**
     * 面试方式
     *
     * 字典 {@link DictTypeConstants#HRM_RECRUIT_INTERVIEW_TYPE}
     */
    private Integer type;
    /**
     * 面试轮次
     */
    private Integer stageNumber;
    /**
     * 主面试官员工编号
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    private Long interviewEmployeeId;
    /**
     * 其他面试官员工编号数组
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> otherInterviewEmployeeIds;
    /**
     * 面试时间
     */
    private LocalDateTime interviewTime;
    /**
     * 面试地址
     */
    private String address;
    /**
     * 备注
     */
    private String remark;
    /**
     * 面试结果
     *
     * 枚举 {@link HrmRecruitInterviewResultEnum}
     * 字典 {@link DictTypeConstants#HRM_RECRUIT_INTERVIEW_RESULT}
     */
    private Integer result;
    /**
     * 面试评价
     */
    private String evaluate;
    /**
     * 取消原因
     */
    private String cancelReason;

}
