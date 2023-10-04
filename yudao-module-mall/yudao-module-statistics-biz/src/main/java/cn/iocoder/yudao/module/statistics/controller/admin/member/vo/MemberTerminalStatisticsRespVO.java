package cn.iocoder.yudao.module.statistics.controller.admin.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 终端性别统计 Response VO")
@Data
public class MemberTerminalStatisticsRespVO {

    @Schema(description = "终端", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer terminal;

    @Schema(description = "会员数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer userCount;

    @Schema(description = "订单创建数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer orderCreateCount;
    @Schema(description = "订单支付数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "512")
    private Integer orderPayCount;
    @Schema(description = "订单支付金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "622")
    private Integer orderPayPrice;

}
