package cn.iocoder.yudao.module.fms.dal.dataobject.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

// TODO DONE @AI：DO 字段注释、枚举引用和状态字段已对齐 FMS 现有 DO 风格。
/**
 * FMS 首页财务指标 DO
 *
 * @author 芋道源码
 */
@TableName("fms_finance_indicator")
@KeySequence("fms_finance_indicator_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FmsFinanceIndicatorDO extends BaseDO {

    // TODO DONE @AI：报表类型常量已迁移到 FmsFinanceIndicatorTypeEnum。
    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 账套编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO#getId()}
     */
    private Long accountSetId;
    /**
     * 指标名称
     */
    private String name;
    /**
     * 指标编码
     */
    private String code;
    /**
     * 取数报表类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.fms.enums.config.FmsFinanceIndicatorTypeEnum}
     */
    private Integer type;
    /**
     * 指标公式，支持报表行次公式或科目公式 JSON
     */
    private String formula;
    /**
     * 展示顺序
     */
    private Integer sort;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;
}
