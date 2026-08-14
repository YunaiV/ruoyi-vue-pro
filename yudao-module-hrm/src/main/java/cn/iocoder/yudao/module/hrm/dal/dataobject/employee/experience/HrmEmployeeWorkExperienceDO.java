package cn.iocoder.yudao.module.hrm.dal.dataobject.employee.experience;

import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * HRM 员工工作经历 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_employee_work_experience")
@KeySequence("hrm_employee_work_experience_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmEmployeeWorkExperienceDO extends BaseDO {

    /**
     * 工作经历编号
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
     * 工作单位
     */
    private String workUnit;
    /**
     * 职务
     */
    private String postName;
    /**
     * 工作开始日期
     */
    private LocalDateTime startTime;
    /**
     * 工作结束日期
     */
    private LocalDateTime endTime;
    /**
     * 离职原因
     */
    private String reason;
    /**
     * 证明人
     */
    private String witnessName;
    /**
     * 证明人手机号
     */
    private String witnessPhone;
    /**
     * 工作备注
     */
    private String remark;
    /**
     * 显示顺序
     */
    private Integer sort;

}
