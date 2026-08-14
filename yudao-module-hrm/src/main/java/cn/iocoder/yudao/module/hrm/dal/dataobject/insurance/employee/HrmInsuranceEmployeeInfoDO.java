package cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.employee;

import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeDO;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
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
 * HRM 员工参保信息 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_insurance_employee_info")
@KeySequence("hrm_insurance_employee_info_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmInsuranceEmployeeInfoDO extends BaseDO {

    @TableId
    private Long id;
    /**
     * 员工编号
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    private Long employeeId;
    /**
     * 是否首次参保
     */
    private Boolean firstSocialSecurity;
    /**
     * 是否首次缴纳公积金
     */
    private Boolean firstAccumulationFund;
    /**
     * 社保账号
     */
    private String socialSecurityNumber;
    /**
     * 公积金账号
     */
    private String accumulationFundNumber;
    /**
     * 社保起缴月份
     */
    private LocalDateTime socialSecurityStartMonth;
    /**
     * 社保方案编号
     *
     * 关联 {@link HrmInsuranceSchemeDO#getId()}
     */
    private Long schemeId;

}
