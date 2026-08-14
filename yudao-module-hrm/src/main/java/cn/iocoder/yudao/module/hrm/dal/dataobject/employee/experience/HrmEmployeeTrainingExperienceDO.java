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
 * HRM 员工培训经历 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_employee_training_experience")
@KeySequence("hrm_employee_training_experience_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmEmployeeTrainingExperienceDO extends BaseDO {

    /**
     * 培训经历编号
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
     * 培训课程
     */
    private String course;
    /**
     * 培训机构名称
     */
    private String organizationName;
    /**
     * 培训开始日期
     */
    private LocalDateTime startTime;
    /**
     * 培训结束日期
     */
    private LocalDateTime endTime;
    /**
     * 培训时长
     */
    private String duration;
    /**
     * 培训成绩
     */
    private String result;
    /**
     * 培训证书名称
     */
    private String certificateName;
    /**
     * 备注
     */
    private String remark;
    /**
     * 显示顺序
     */
    private Integer sort;

}
