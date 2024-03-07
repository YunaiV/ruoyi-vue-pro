package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 快递查询的轨迹 Resp DTO
 *
 * @author jason
 */
@Schema(description = "用户 App - 快递查询的轨迹 Response VO")
@Data
public class AppOrderExpressTrackRespDTO {

    @Schema(description = "发生时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime time;

    @Schema(description = "快递状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "已签收")
    private String content;

}
