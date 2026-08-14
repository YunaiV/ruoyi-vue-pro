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
 * FMS 现金流量表扩展数据 DO
 *
 * @author 芋道源码
 */
@TableName("fms_cash_flow_extend_data")
@KeySequence("fms_cash_flow_extend_data_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsCashFlowExtendDataDO extends BaseDO {

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
     * 备注
     */
    private String remark;
    /**
     * 现金流量表分类
     */
    private Integer category;
    /**
     * 本期金额
     */
    private BigDecimal currentAmount;
    /**
     * 本年累计金额
     */
    private BigDecimal yearAmount;
    /**
     * 开始会计期间，格式为 yyyyMM
     */
    private Integer fromPeriod;
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
     * 显示顺序
     */
    private Integer sort;
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
     */
    private Integer level;

}
