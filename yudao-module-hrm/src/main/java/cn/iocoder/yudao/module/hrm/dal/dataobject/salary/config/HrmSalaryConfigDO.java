package cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalarySocialSecurityMonthTypeEnum;
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
 * HRM 计薪配置 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_salary_config")
@KeySequence("hrm_salary_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmSalaryConfigDO extends BaseDO {

    @TableId
    private Long id;
    /**
     * 计薪周期开始日
     */
    private Integer cycleStartDay;
    /**
     * 计薪周期结束日
     */
    private Integer cycleEndDay;
    /**
     * 社保对应月份类型
     *
     * 枚举 {@link HrmSalarySocialSecurityMonthTypeEnum}
     */
    private Integer socialSecurityMonthType;
    /**
     * 工资开始年份
     */
    private Integer startYear;
    /**
     * 工资开始月份
     */
    private Integer startMonth;

}
