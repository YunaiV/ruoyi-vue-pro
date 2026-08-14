package cn.iocoder.yudao.module.fms.dal.dataobject.report.balance;

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
 * FMS 资产负债表数据 DO
 *
 * @author 芋道源码
 */
@TableName("fms_balance_sheet_report")
@KeySequence("fms_balance_sheet_report_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsBalanceSheetReportDO extends BaseDO {

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
     * 层级快照
     *
     * 冗余 {@link FmsBalanceSheetConfigDO#getLevel()}，用于保留历史期间的展示层级
     */
    private Integer level;
    /**
     * 配置名称快照
     *
     * 冗余 {@link FmsBalanceSheetConfigDO#getName()}，用于保留历史期间的项目名称
     */
    private String name;
    /**
     * 行次快照
     *
     * 冗余 {@link FmsBalanceSheetConfigDO#getRowNo()}，用于保留历史期间的报表行次
     */
    private Integer rowNo;
    /**
     * 公式快照
     *
     * 冗余 {@link FmsBalanceSheetConfigDO#getFormula()}，保证历史期间报表不受后续公式调整影响
     */
    private String formula;
    /**
     * 备注快照
     *
     * 冗余 {@link FmsBalanceSheetConfigDO#getRemark()}，用于保留历史期间的备注
     */
    private String remark;
    /**
     * 是否可编辑快照
     *
     * 冗余 {@link FmsBalanceSheetConfigDO#getEditable()}，用于保留历史期间的可编辑标识
     */
    private Boolean editable;
    /**
     * 显示顺序快照
     *
     * 冗余 {@link FmsBalanceSheetConfigDO#getSort()}，用于保留历史期间的显示顺序
     */
    private Integer sort;
    /**
     * 期初金额
     */
    private BigDecimal openingAmount;
    /**
     * 期末金额
     */
    private BigDecimal closingAmount;
    /**
     * 账套编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO#getId()}
     */
    private Long accountSetId;
    /**
     * 是否已结账
     */
    private Boolean settled;
    /**
     * 行编号快照
     *
     * 冗余 {@link FmsBalanceSheetConfigDO#getRowId()}，用于保留历史期间资产与负债项目的行配对关系
     */
    private Integer rowId;

}
