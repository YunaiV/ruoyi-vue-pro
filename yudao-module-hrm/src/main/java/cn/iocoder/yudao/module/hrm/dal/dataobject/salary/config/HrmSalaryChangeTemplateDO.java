package cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * HRM 调薪模板 DO
 *
 * @author 芋道源码
 */
@TableName(value = "hrm_salary_change_template", autoResultMap = true)
@KeySequence("hrm_salary_change_template_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmSalaryChangeTemplateDO extends BaseDO {

    /**
     * 调薪模板编号
     */
    @TableId
    private Long id;
    /**
     * 模板名称
     */
    private String name;
    /**
     * 是否默认模板
     */
    private Boolean defaultStatus;
    /**
     * 调薪项配置
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<Option> options;

    /**
     * 调薪项配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {

        /**
         * 薪资项名称快照
         *
         * 关联 {@link HrmSalaryOptionDO#getName()}
         */
        private String name;
        /**
         * 薪资项编码
         *
         * 关联 {@link HrmSalaryOptionDO#getCode()}
         */
        private Integer code;

    }

}
