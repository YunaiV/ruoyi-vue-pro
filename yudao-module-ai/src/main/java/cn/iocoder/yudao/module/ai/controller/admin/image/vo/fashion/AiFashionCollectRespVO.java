package cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 管理后台 - 触发素材采集 Response VO
 *
 * @author deepay
 */
@Schema(description = "管理后台 - 触发素材采集 Response VO")
@Data
public class AiFashionCollectRespVO {

    @Schema(description = "采集任务 ID（async=true 时返回，用于轮询状态）")
    private String collectJobId;

    @Schema(description = "本次触发的采集源数量", example = "5")
    private Integer sourcesTriggered;

    @Schema(description = "触发的采集源 ID 列表")
    private List<String> sourceIds;

    @Schema(description = "同步采集结果（async=false 时返回）")
    private List<SourceResult> results;

    @Schema(description = "是否异步", example = "true")
    private Boolean async;

    @Data
    @Schema(description = "单个采集源结果")
    public static class SourceResult {

        @Schema(description = "采集源 ID", example = "vogue_runway")
        private String sourceId;

        @Schema(description = "采集源名称", example = "Vogue Runway")
        private String sourceName;

        @Schema(description = "采集源类型", example = "fashion_show")
        private String sourceType;

        @Schema(description = "本次新增图片数", example = "18")
        private Integer newCount;

        @Schema(description = "本次跳过（重复）图片数", example = "2")
        private Integer skipCount;

        @Schema(description = "是否成功", example = "true")
        private Boolean success;

        @Schema(description = "失败原因（success=false 时有值）")
        private String errorMessage;

    }

}
