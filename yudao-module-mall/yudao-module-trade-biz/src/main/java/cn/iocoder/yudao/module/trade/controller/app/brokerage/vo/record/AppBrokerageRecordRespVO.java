package cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 分销记录 Response VO")
@Data
public class AppBrokerageRecordRespVO {

    @Schema(description = "记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long id;

    @Schema(description = "业务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private String bizId;

    @Schema(description = "分销标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "用户下单")
    private String title;

    @Schema(description = "分销金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private Integer price;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "完成时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime finishTime;

}
