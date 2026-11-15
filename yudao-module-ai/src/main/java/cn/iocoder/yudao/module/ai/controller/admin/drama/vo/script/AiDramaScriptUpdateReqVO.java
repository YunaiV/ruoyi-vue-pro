package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 短剧剧本修改 Request VO")
@Data
public class AiDramaScriptUpdateReqVO {

    @Schema(description = "项目编号：剧本不存在时创建", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "剧本标题", example = "末班地铁")
    @Size(max = 128, message = "剧本标题不能超过 128 个字符")
    private String title;

    @Schema(description = "叙事基调", example = "悬疑、温情")
    @Size(max = 255, message = "叙事基调不能超过 255 个字符")
    private String tone;

    @Schema(description = "剧情大纲", example = "……")
    @Size(max = 5000, message = "剧情大纲不能超过 5000 个字符")
    private String outline;

    @Schema(description = "剧本正文", example = "……")
    private String content;

}
