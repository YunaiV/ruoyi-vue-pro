package cn.iocoder.yudao.module.fms.dal.dataobject.report.income;

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
 * FMS 利润表配置 DO
 *
 * @author 芋道源码
 */
@TableName("fms_income_statement_config")
@KeySequence("fms_income_statement_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsIncomeStatementConfigDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 配置名称
     */
    private String name;
    /**
     * 行次
     */
    private Integer rowNo;
    /**
     * 公式
     */
    private String formula;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 是否可编辑
     */
    private Boolean editable;
    /**
     * 账套编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO#getId()}
     */
    private Long accountSetId;
    /**
     * 层级
     */
    private Integer level;

}
