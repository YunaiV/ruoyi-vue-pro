package cn.iocoder.yudao.module.pay.controller.app.verification.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 订单数据验证 Response VO")
@Data
public class AppVerifyOrderRespVO {

    @Schema(description = "是否验证通过")
    private Boolean valid;

    @Schema(description = "验证结果说明", example = "验证通过")
    private String reason;

    @Schema(description = "链上存储的哈希")
    private String storedHash;

    @Schema(description = "提供数据的哈希")
    private String providedHash;

    @Schema(description = "链上交易哈希")
    private String txHash;

    @Schema(description = "链类型", example = "JD_CHAIN")
    private String chainType;

    @Schema(description = "验证时间（ISO）")
    private String verificationTime;

    @Schema(description = "可分享验证链接（仅 generateLink 接口返回）")
    private String shareLink;

}
