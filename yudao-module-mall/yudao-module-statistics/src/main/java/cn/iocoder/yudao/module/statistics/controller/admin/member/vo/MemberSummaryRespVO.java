package cn.iocoder.yudao.module.statistics.controller.admin.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 会员统计 Response VO")
@Data
public class MemberSummaryRespVO {

    @Schema(description = "会员数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer userCount;

    @Schema(description = "充值会员数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "221")
    private Integer rechargeUserCount;

    @Schema(description = "充值金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer rechargePrice;

    // TODO @疯狂：要不干脆这个字段改成：orderPayPrice？？
    @Schema(description = "支出金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer expensePrice; // 只计算 mall 交易订单的支付金额，不考虑退款

}
