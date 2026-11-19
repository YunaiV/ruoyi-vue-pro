package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaScriptCreationModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - AI 短剧剧本修改 Request VO")
@Data
public class AiDramaScriptUpdateReqVO {

    @Schema(description = "剧本编号：为空时新增剧本版本", example = "1")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
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

    @Schema(description = "创作模式", example = "1")
    @InEnum(AiDramaScriptCreationModeEnum.class)
    private Integer creationMode;

    @Schema(description = "创作资料", example = "……")
    private String sourceMaterial;

    @Schema(description = "世界观设定", example = "……")
    private String worldbuilding;

    @Schema(description = "修订要求", example = "结尾加一个反转")
    @Size(max = 1000, message = "修订要求不能超过 1000 个字符")
    private String revisionNote;

}
