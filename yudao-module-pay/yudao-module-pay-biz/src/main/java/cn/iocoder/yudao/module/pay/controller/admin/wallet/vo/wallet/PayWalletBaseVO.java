package cn.iocoder.yudao.module.pay.controller.admin.wallet.vo.wallet;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 用户钱包 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class PayWalletBaseVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20020")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @Schema(description = "用户类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    @Schema(description = "余额，单位分", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "余额，单位分不能为空")
    private Integer balance;

    @Schema(description = "累计支出，单位分", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "累计支出，单位分不能为空")
    private Integer totalExpense;

    @Schema(description = "累计充值，单位分", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "累计充值，单位分不能为空")
    private Integer totalRecharge;

    @Schema(description = "冻结金额，单位分", requiredMode = Schema.RequiredMode.REQUIRED, example = "20737")
    @NotNull(message = "冻结金额，单位分不能为空")
    private Integer freezePrice;

}