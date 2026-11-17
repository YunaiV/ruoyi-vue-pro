package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.canvas;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Map;

@Schema(description = "管理后台 - AI 短剧 3D 导演工程保存 Request VO")
@Data
public class AiDramaDirectorSaveReqVO {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "画布预演节点编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "n-1f0e")
    @NotBlank(message = "画布节点编号不能为空")
    @Pattern(regexp = "[a-zA-Z0-9_-]{1,100}", message = "画布节点编号无效")
    private String nodeId;

    @Schema(description = "读取时的修订号：与当前不一致时拒绝保存", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "修订号不能为空")
    private Long revision;

    @Schema(description = "导演工程文档", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "导演工程文档不能为空")
    private Map<String, Object> document;

}
