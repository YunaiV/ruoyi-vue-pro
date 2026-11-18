package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.canvas;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Schema(description = "管理后台 - AI 短剧画布 Response VO")
@Data
public class AiDramaCanvasRespVO {

    @Schema(description = "修订号：未保存过时为 0", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Long revision;

    @Schema(description = "画布文档：节点、连线等；未保存过时为空")
    private Map<String, Object> document;

}
