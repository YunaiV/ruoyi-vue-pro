package cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment;

import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeChangeReasonEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeChangeTypeEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
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
 * HRM 员工异动记录 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_employee_change_record")
@KeySequence("hrm_employee_change_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmEmployeeChangeRecordDO extends BaseDO {

    /**
     * 异动记录编号
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
     * 异动类型
     *
     * 枚举 {@link HrmEmployeeChangeTypeEnum}
     */
    private Integer type;
    /**
     * 异动原因
     *
     * 枚举 {@link HrmEmployeeChangeReasonEnum}
     */
    private Integer reason;
    /**
     * 原部门编号
     *
     * 关联 {@link DeptDO#getId()}
     */
    private Long oldDeptId;
    /**
     * 新部门编号
     *
     * 关联 {@link DeptDO#getId()}
     */
    private Long newDeptId;
    /**
     * 原岗位
     */
    private String oldPostName;
    /**
     * 新岗位
     */
    private String newPostName;
    /**
     * 原职级
     */
    private String oldPostLevel;
    /**
     * 新职级
     */
    private String newPostLevel;
    /**
     * 原工作地点
     */
    private String oldWorkAddress;
    /**
     * 新工作地点
     */
    private String newWorkAddress;
    /**
     * 原直属上级员工编号
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    private Long oldLeaderEmployeeId;
    /**
     * 新直属上级员工编号
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    private Long newLeaderEmployeeId;
    /**
     * 试用期，单位：月
     */
    private Integer probation;
    /**
     * 生效日期
     */
    private LocalDateTime effectTime;
    /**
     * 实际生效时间
     */
    private LocalDateTime appliedTime;
    /**
     * 备注
     */
    private String remark;

}
