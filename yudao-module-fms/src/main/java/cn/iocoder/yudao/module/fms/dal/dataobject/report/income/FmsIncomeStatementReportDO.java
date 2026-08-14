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

import java.math.BigDecimal;

/**
 * FMS 利润表数据 DO
 *
 * 按会计期间保存报表配置的快照：名称、行次、公式、显示顺序、是否可编辑和层级冗余自 {@link FmsIncomeStatementConfigDO}，保证历史期间报表保留当时的公式，不受后续公式调整影响
 *
 * @author 芋道源码
 */
@TableName("fms_income_statement_report")
@KeySequence("fms_income_statement_report_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsIncomeStatementReportDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 报表期间类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.fms.enums.report.FmsReportPeriodTypeEnum}
     */
    private Integer type;
    /**
     * 开始会计期间，格式为 yyyyMM
     */
    private Integer fromPeriod;
    /**
     * 结束会计期间，格式为 yyyyMM
     */
    private Integer toPeriod;
    /**
     * 配置名称
     *
     * 冗余 {@link FmsIncomeStatementConfigDO#getName()}
     */
    private String name;
    /**
     * 行次
     *
     * 冗余 {@link FmsIncomeStatementConfigDO#getRowNo()}
     */
    private Integer rowNo;
    /**
     * 公式
     *
     * 冗余 {@link FmsIncomeStatementConfigDO#getFormula()}
     */
    private String formula;
    /**
     * 显示顺序
     *
     * 冗余 {@link FmsIncomeStatementConfigDO#getSort()}
     */
    private Integer sort;
    /**
     * 是否可编辑
     *
     * 冗余 {@link FmsIncomeStatementConfigDO#getEditable()}
     */
    private Boolean editable;
    /**
     * 本期金额
     */
    private BigDecimal currentAmount;
    /**
     * 本年累计金额
     */
    private BigDecimal yearAmount;
    /**
     * 账套编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO#getId()}
     */
    private Long accountSetId;
    /**
     * 层级
     *
     * 冗余 {@link FmsIncomeStatementConfigDO#getLevel()}
     */
    private Integer level;
    /**
     * 是否已结账
     */
    private Boolean settled;

}
