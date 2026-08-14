package cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment;

import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.framework.ip.core.Area;
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

/**
 * HRM 员工工资卡 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_employee_salary_card")
@KeySequence("hrm_employee_salary_card_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmEmployeeSalaryCardDO extends BaseDO {

    @TableId
    private Long id;
    /**
     * 员工编号
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    private Long employeeId;
    /**
     * 银行卡号
     */
    private String bankCardNumber;
    /**
     * 开户地区编号
     *
     * 关联 {@link Area#getId()}
     */
    private Integer bankAreaId;
    /**
     * 银行名称
     */
    private String bankName;
    /**
     * 开户支行名称
     */
    private String bankBranchName;

}
