package cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.record;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.enums.DictTypeConstants;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * HRM 考勤请假 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_attendance_leave")
@KeySequence("hrm_attendance_leave_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmAttendanceLeaveDO extends BaseDO {

    /**
     * 请假记录编号
     */
    @TableId
    private Long id;
    /**
     * 员工编号
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    private Long employeeId;
    /**
     * 请假类型
     *
     * 字典 {@link DictTypeConstants#HRM_ATTENDANCE_LEAVE_TYPE}
     */
    private String type;
    /**
     * 请假开始时间
     */
    private LocalDateTime startTime;
    /**
     * 请假结束时间
     */
    private LocalDateTime endTime;
    /**
     * 请假时长
     */
    private BigDecimal day;
    /**
     * 请假理由
     */
    private String reason;
    /**
     * 备注
     */
    private String remark;
    /**
     * 审批状态
     *
     * 枚举 {@link BpmProcessInstanceStatusEnum}
     */
    private Integer approvalStatus;
    /**
     * BPM 流程实例编号
     *
     * 关联 ProcessInstance 的 id 属性
     */
    private String processInstanceId;
    /**
     * 审批完成时间
     */
    private LocalDateTime approvalTime;
    /**
     * 审批或取消原因
     */
    private String approvalReason;

}
