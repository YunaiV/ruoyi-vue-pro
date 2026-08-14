package cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
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
 * HRM 工资表薪资项 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_salary_option")
@KeySequence("hrm_salary_option_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmSalaryOptionDO extends BaseDO {

    /**
     * 根薪资项的父编码
     */
    public static final Integer ROOT_PARENT_CODE = 0;

    /**
     * 可调整薪资项的最小分类编码
     */
    public static final Integer ADJUSTABLE_CATEGORY_MIN_CODE = 10;
    /**
     * 可调整薪资项的最大分类编码
     */
    public static final Integer ADJUSTABLE_CATEGORY_MAX_CODE = 90;

    /**
     * 企业自定义薪资项编码起始值
     *
     * <p>预置目录编码均位于低位区间，自定义项使用独立高位区间，避免未来目录升级占用同一编码。</p>
     */
    public static final Integer CUSTOM_OPTION_CODE_BASE = 1_000_000_000;

    /**
     * 薪资项编号
     */
    @TableId
    private Long id;
    /**
     * 标准薪资项目录编号（可选）
     *
     * 关联 {@link HrmSalaryOptionTemplateDO#getId()}
     */
    private Long templateId;
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
     * 是否启用
     */
    private Boolean enabled;
    /**
     * 是否计税
     */
    private Boolean taxEnabled;
    /**
     * 是否参与计算
     */
    private Boolean calculateEnabled;

}
