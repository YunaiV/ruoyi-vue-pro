package cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.withdraw;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 分销提现 Response VO")
@Data
public class AppBrokerageWithdrawRespVO {

    @Schema(description = "提现编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long id;

    @Schema(description = "提现类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "提现类型名", requiredMode = Schema.RequiredMode.REQUIRED, example = "微信")
    private String typeName;

    @Schema(description = "提现状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer status;

    @Schema(description = "提现状态名", requiredMode = Schema.RequiredMode.REQUIRED, example = "审核中")
    private String statusName;

    @Schema(description = "提现金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private Integer price;

    @Schema(description = "提现时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    // ========== 微信转账专属 ==========

    @Schema(description = "转账单编号", example = "1024")
    private Long payTransferId;

    @Schema(description = "渠道 package 信息")
    private String transferChannelPackageInfo;

    @Schema(description = "渠道商户号")
    private String transferChannelMchId;

}
