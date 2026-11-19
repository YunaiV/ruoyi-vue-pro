package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.canvas;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Schema(description = "管理后台 - AI 短剧画布保存 Request VO")
@Data
public class AiDramaCanvasSaveReqVO {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "读取时的修订号：与当前不一致时拒绝保存", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "修订号不能为空")
    private Long revision;

    @Schema(description = "画布文档", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "画布文档不能为空")
    private Map<String, Object> document;

}
