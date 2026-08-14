package cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip;

import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthRecordDO;
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
 * HRM 工资条发放记录 DO
 *
 * 同一月度工资表可以分多批次发放工资条，因此发放记录保持独立实体。
 *
 * @author 芋道源码
 */
@TableName("hrm_salary_slip_send_record")
@KeySequence("hrm_salary_slip_send_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmSalarySlipSendRecordDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 月度工资表编号
     *
     * 关联 {@link HrmSalaryMonthRecordDO#getId()}
     */
    private Long monthRecordId;
    /**
     * 工资表总人数
     */
    private Integer employeeCount;
    /**
     * 本批次发放人数
     */
    private Integer sendEmployeeCount;
    /**
     * 年份
     */
    private Integer year;
    /**
     * 月份
     */
    private Integer month;

}
