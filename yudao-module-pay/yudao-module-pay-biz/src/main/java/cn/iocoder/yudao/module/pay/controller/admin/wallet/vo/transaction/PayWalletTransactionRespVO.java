package cn.iocoder.yudao.module.pay.controller.admin.wallet.vo.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 APP - 钱包流水分页 Response VO")
@Data
public class PayWalletTransactionRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "钱包编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Long walletId;

    @Schema(description = "业务分类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer bizType;

    @Schema(description = "交易金额，单位分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long price;

    @Schema(description = "流水标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "土豆土豆")
    private String title;

    @Schema(description = "交易后的余额，单位分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long balance;

    @Schema(description = "交易时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    // TODO @jason：merchantOrderId 字段，需要在 PayWalletTransaction 存储下；然后，前端也返回下这个字段，界面也展示下商户名

}
