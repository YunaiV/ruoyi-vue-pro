package cn.iocoder.yudao.module.ai.controller.admin.requestlog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI 请求日志统计 Response VO")
@Data
public class AiRequestLogStatisticsRespVO {

    @Schema(description = "请求总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long requestCount;

    @Schema(description = "成功数", requiredMode = Schema.RequiredMode.REQUIRED, example = "90")
    private Long successCount;

    @Schema(description = "失败数", requiredMode = Schema.RequiredMode.REQUIRED, example = "6")
    private Long failCount;

    @Schema(description = "取消数", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    private Long cancelCount;

    @Schema(description = "已知输入 Token 合计，没有任何已知值时为空", example = "12000")
    private Long inputTokens;

    @Schema(description = "已知输出 Token 合计，没有任何已知值时为空", example = "8000")
    private Long outputTokens;

    @Schema(description = "完整用量记录的合计 Token，没有完整用量记录时为空", example = "19500")
    private Long totalTokens;

    @Schema(description = "完整用量条数（输入、输出、合计都已知）", requiredMode = Schema.RequiredMode.REQUIRED, example = "95")
    private Long knownUsageCount;

    @Schema(description = "用量不完整或未知条数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Long unknownUsageCount;

}
