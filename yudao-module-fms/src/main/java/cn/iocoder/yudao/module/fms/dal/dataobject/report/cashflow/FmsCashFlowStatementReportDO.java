package cn.iocoder.yudao.module.fms.dal.dataobject.report.cashflow;

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
 * FMS 现金流量表数据 DO
 *
 * 按会计期间保存报表配置的快照：名称、行次、公式、是否可编辑、显示顺序和层级冗余自 {@link FmsCashFlowStatementConfigDO}，保证历史期间报表保留当时的公式，不受后续公式调整影响
 *
 * @author 芋道源码
 */
@TableName("fms_cash_flow_statement_report")
@KeySequence("fms_cash_flow_statement_report_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsCashFlowStatementReportDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 开始会计期间，格式为 yyyyMM
     */
    private Integer fromPeriod;
    /**
     * 配置名称
     *
     * 冗余 {@link FmsCashFlowStatementConfigDO#getName()}
     */
    private String name;
    /**
     * 行次
     *
     * 冗余 {@link FmsCashFlowStatementConfigDO#getRowNo()}
     */
    private Integer rowNo;
    /**
     * 公式
     *
     * 冗余 {@link FmsCashFlowStatementConfigDO#getFormula()}
     */
    private String formula;
    /**
     * 备注
     */
    private String remark;
    /**
     * 是否可编辑
     *
     * 冗余 {@link FmsCashFlowStatementConfigDO#getEditable()}
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
     * 显示顺序
     *
     * 冗余 {@link FmsCashFlowStatementConfigDO#getSort()}
     */
    private Integer sort;
    /**
     * 现金流量表分类
     */
    private Integer category;
    /**
     * 账套编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO#getId()}
     */
    private Long accountSetId;
    /**
     * 结束会计期间，格式为 yyyyMM
     */
    private Integer toPeriod;
    /**
     * 报表期间类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.fms.enums.report.FmsReportPeriodTypeEnum}
     */
    private Integer type;
    /**
     * 层级
     *
     * 冗余 {@link FmsCashFlowStatementConfigDO#getLevel()}
     */
    private Integer level;

}
