package cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.withdraw;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 佣金提现 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class BrokerageWithdrawBaseVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11436")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @Schema(description = "提现金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "18781")
    @NotNull(message = "提现金额不能为空")
    private Integer price;

    @Schema(description = "提现手续费", requiredMode = Schema.RequiredMode.REQUIRED, example = "11417")
    @NotNull(message = "提现手续费不能为空")
    private Integer feePrice;

    @Schema(description = "当前总佣金", requiredMode = Schema.RequiredMode.REQUIRED, example = "18576")
    @NotNull(message = "当前总佣金不能为空")
    private Integer totalPrice;

    @Schema(description = "提现类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "提现类型不能为空")
    private Integer type;

    @Schema(description = "真实姓名", example = "赵六")
    private String name;

    @Schema(description = "账号", example = "88677912132")
    private String accountNo;

    @Schema(description = "银行名称", example = "1")
    private String bankName;

    @Schema(description = "开户地址", example = "海淀支行")
    private String bankAddress;

    @Schema(description = "收款码", example = "https://www.iocoder.cn")
    private String accountQrCodeUrl;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "审核驳回原因", example = "不对")
    private String auditReason;

    @Schema(description = "审核时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime auditTime;

    @Schema(description = "备注", example = "随便")
    private String remark;

}
