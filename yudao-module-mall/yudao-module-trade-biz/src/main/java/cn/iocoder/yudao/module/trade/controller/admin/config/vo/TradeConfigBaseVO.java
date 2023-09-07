package cn.iocoder.yudao.module.trade.controller.admin.config.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageBindModeEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageEnabledConditionEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * 交易中心配置 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class TradeConfigBaseVO {

    // ========== 分销相关 ==========

    @Schema(description = "是否启用分佣", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否启用分佣不能为空")
    private Boolean brokerageEnabled;

    @Schema(description = "分佣模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "分佣模式不能为空")
    @InEnum(value = BrokerageEnabledConditionEnum.class, message = "分佣模式必须是 {value}")
    private Integer brokerageEnabledCondition;

    @Schema(description = "分销关系绑定模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "分销关系绑定模式不能为空")
    @InEnum(value = BrokerageBindModeEnum.class, message = "分销关系绑定模式必须是 {value}")
    private Integer brokerageBindMode;

    @Schema(description = "分销海报图地址数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "[https://www.iocoder.cn/yudao.jpg]")
    private List<String> brokeragePostUrls;

    @Schema(description = "一级返佣比例", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "一级返佣比例不能为空")
    @Range(min = 0, max = 100, message = "一级返佣比例必须在 0 - 100 之间")
    private Integer brokerageFirstPercent;

    @Schema(description = "二级返佣比例", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "二级返佣比例不能为空")
    @Range(min = 0, max = 100, message = "二级返佣比例必须在 0 - 100 之间")
    private Integer brokerageSecondPercent;

    @Schema(description = "用户提现最低金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    @NotNull(message = "用户提现最低金额不能为空")
    @PositiveOrZero(message = "用户提现最低金额不能是负数")
    private Integer brokerageWithdrawMinPrice;

    // TODO @疯狂：要不要做成字典？按道理都可以体现对哇？感觉是全局的配置哈；
    @Schema(description = "提现银行", requiredMode = Schema.RequiredMode.REQUIRED, example = "[0, 1]")
    @NotEmpty(message = "提现银行不能为空")
    private List<Integer> brokerageBankNames;

    @Schema(description = "佣金冻结时间(天)", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    @NotNull(message = "佣金冻结时间(天)不能为空")
    @PositiveOrZero(message = "佣金冻结时间不能是负数")
    private Integer brokerageFrozenDays;

    @Schema(description = "提现方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "[0, 1]")
    @NotNull(message = "提现方式不能为空")
    @InEnum(value = BrokerageWithdrawTypeEnum.class, message = "提现方式必须是 {value}")
    private List<Integer> brokerageWithdrawType;

}
