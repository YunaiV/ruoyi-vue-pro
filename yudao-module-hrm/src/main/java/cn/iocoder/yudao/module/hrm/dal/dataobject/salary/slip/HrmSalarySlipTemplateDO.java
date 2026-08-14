package cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip;

import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.enums.salary.slip.HrmSalarySlipTemplateOptionTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * HRM 工资条模板 DO
 *
 * @author 芋道源码
 */
@TableName(value = "hrm_salary_slip_template", autoResultMap = true)
@KeySequence("hrm_salary_slip_template_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmSalarySlipTemplateDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 模板名称
     */
    private String name;
    /**
     * 是否隐藏空项
     */
    private Boolean hideEmpty;
    /**
     * 是否默认模板
     */
    private Boolean defaultStatus;
    /**
     * 工资条模板项
     *
     * 模板项随工资条模板整体维护，通过 {@link Option#getChildren()} 表达父子关系
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Option> options;

    /**
     * 工资条模板项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {

        /**
         * 名称
         *
         * 关联 {@link HrmSalaryOptionDO#getName()}
         */
        private String name;
        /**
         * 类型
         *
         * 枚举 {@link HrmSalarySlipTemplateOptionTypeEnum}
         */
        private Integer type;
        /**
         * 薪资项编码
         *
         * 关联 {@link HrmSalaryOptionDO#getCode()}
         */
        private Integer code;
        /**
         * 说明
         */
        private String remark;
        /**
         * 是否隐藏
         */
        private Boolean hidden;
        /**
         * 排序
         */
        private Integer sort;
        /**
         * 子项
         */
        private List<Option> children;
    }

}
