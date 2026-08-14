package cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryOptionTypeEnum;
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
 * HRM 标准薪资项目录 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_salary_option_template")
@KeySequence("hrm_salary_option_template_seq")
@TenantIgnore
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmSalaryOptionTemplateDO extends BaseDO {

    /**
     * 标准薪资项编号
     */
    @TableId
    private Long id;
    /**
     * 薪资项编码
     */
    private Integer code;
    /**
     * 父薪资项编码
     *
     * 关联 {@link #code}
     */
    private Integer parentCode;
    /**
     * 薪资项名称
     */
    private String name;
    /**
     * 备注
     */
    private String remark;
    /**
     * 是否系统默认项
     */
    private Boolean systemFlag;
    /**
     * 薪资项类型
     *
     * 枚举 {@link HrmSalaryOptionTypeEnum}
     */
    private Integer type;
    /**
     * 是否显示
     */
    private Boolean visible;
    /**
     * 是否计税
     */
    private Boolean taxEnabled;
    /**
     * 是否参与计算
     */
    private Boolean calculateEnabled;

}
