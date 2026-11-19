package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI 短剧对话模型生成 Request VO：生成剧本、分析资产、生成分镜共用")
@Data
public class AiDramaChatReqVO {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "对话模型编号；为空时使用默认对话模型", example = "1")
    private Long modelId;

}
