package cn.iocoder.yudao.module.fms.dal.dataobject.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.fms.enums.ledger.FmsLedgerBalanceModeEnum;
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
 * FMS 财务参数 DO
 *
 * @author 芋道源码
 */
@TableName("fms_finance_parameter")
@KeySequence("fms_finance_parameter_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsFinanceParameterDO extends BaseDO {

    public static final Integer DEFAULT_LEVEL = 4;

    public static final String DEFAULT_SUBJECT_CODE_RULE = "4-2-2-2";

    public static final Integer DEFAULT_LEDGER_BALANCE_MODE = FmsLedgerBalanceModeEnum.SAME_AS_SUBJECT.getMode();
    public static final Boolean DEFAULT_VOUCHER_REVIEW_REQUIRED = false;
    public static final Boolean DEFAULT_DEFICIT_CHECK = false;
    public static final Boolean DEFAULT_ASSET_PERIOD_LOCKED = false;

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
     * 科目层级
     */
    private Integer level;
    /**
     * 科目编码规则
     */
    private String subjectCodeRule;
    /**
     * 账簿余额方向模式
     */
    private Integer ledgerBalanceMode;
    /**
     * 是否检查现金及银行存款科目赤字
     */
    private Boolean deficitCheck;
    /**
     * 结账前是否要求凭证审核
     */
    private Boolean voucherReviewRequired;
    /**
     * 生成折旧凭证后是否锁定以前期间资产卡片
     */
    private Boolean assetPeriodLocked;
    /**
     * 纳税人名称
     */
    private String taxpayerName;
    /**
     * 纳税人识别号
     */
    private String taxpayerNumber;

}
