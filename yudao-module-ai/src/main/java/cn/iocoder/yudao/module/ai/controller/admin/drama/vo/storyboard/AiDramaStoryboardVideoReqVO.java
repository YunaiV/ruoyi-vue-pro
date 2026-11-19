package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - AI 短剧镜头视频生成 Request VO：单个镜头或项目批量共用")
@Data
public class AiDramaStoryboardVideoReqVO {

    @Schema(description = "编号：单个生成时为分镜编号，批量生成时为项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "视频模型编号：镜头设置了视频模型时，优先使用镜头的", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "视频模型不能为空")
    private Long modelId;

    @Schema(description = "剧本编号：批量生成时有效，只处理该剧本版本的镜头；为空时为当前剧本", example = "1")
    private Long scriptId;

    @Schema(description = "分辨率：为空时不传，部分模型（例如 MiniMax H3）必填；镜头设置了视频模型时，使用镜头的", example = "768P")
    @Size(max = 16, message = "分辨率不能超过 16 个字符")
    private String resolution;

    @Schema(description = "请求编号：单个生成时有效，前端每次点击生成一个，网络重试时沿用；同一请求编号重复提交时不再提交服务商",
            example = "6f1c2d3e4b5a69788796a5b4c3d2e1f0")
    @Pattern(regexp = "^[a-zA-Z0-9-]{8,64}$", message = "请求编号格式不正确")
    private String requestId;

}
