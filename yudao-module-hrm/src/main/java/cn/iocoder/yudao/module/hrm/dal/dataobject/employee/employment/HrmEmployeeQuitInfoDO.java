package cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment;

import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeQuitReasonEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeQuitTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
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
 * HRM 员工离职信息 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_employee_quit_info")
@KeySequence("hrm_employee_quit_info_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmEmployeeQuitInfoDO extends BaseDO {

    /**
     * 离职信息编号
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
     * 计划离职时间
     */
    private LocalDateTime planQuitTime;
    /**
     * 申请离职时间
     */
    private LocalDateTime applyQuitTime;
    /**
     * 薪资结算时间
     */
    private LocalDateTime salarySettlementTime;
    /**
     * 离职类型
     *
     * 枚举 {@link HrmEmployeeQuitTypeEnum}
     */
    private Integer type;
    /**
     * 离职原因
     *
     * 枚举 {@link HrmEmployeeQuitReasonEnum}
     */
    private Integer reason;
    /**
     * 备注
     */
    private String remark;
    /**
     * 离职前员工状态
     *
     * 枚举 {@link HrmEmployeeStatusEnum}
     */
    private Integer oldEmployeeStatus;

}
