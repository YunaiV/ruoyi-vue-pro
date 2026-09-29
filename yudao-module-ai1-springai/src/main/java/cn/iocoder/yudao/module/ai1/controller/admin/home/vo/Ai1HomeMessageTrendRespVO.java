package cn.iocoder.yudao.module.ai1.controller.admin.home.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI1 首页消息趋势 Response VO")
@Data
public class Ai1HomeMessageTrendRespVO {

    @Schema(description = "日期，格式 yyyy-MM-dd", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-09-29")
    private String date;

    @Schema(description = "当日消息数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Long count;

}
