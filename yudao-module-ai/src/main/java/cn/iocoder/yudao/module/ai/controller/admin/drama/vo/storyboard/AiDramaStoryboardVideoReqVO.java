package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 短剧镜头视频生成 Request VO：单个镜头或项目批量共用")
@Data
public class AiDramaStoryboardVideoReqVO {

    @Schema(description = "编号：单个生成时为分镜编号，批量生成时为项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "视频模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "视频模型不能为空")
    private Long modelId;

}
