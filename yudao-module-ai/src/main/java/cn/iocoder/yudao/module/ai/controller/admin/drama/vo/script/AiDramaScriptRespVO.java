package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 短剧剧本 Response VO")
@Data
public class AiDramaScriptRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long projectId;

    @Schema(description = "剧本标题", example = "末班地铁")
    private String title;

    @Schema(description = "叙事基调", example = "悬疑、温情")
    private String tone;

    @Schema(description = "剧情大纲", example = "……")
    private String outline;

    @Schema(description = "剧本正文", example = "……")
    private String content;

    @Schema(description = "来源", example = "2")
    private Integer source;

    @Schema(description = "创作模式", example = "1")
    private Integer creationMode;

    @Schema(description = "创作资料", example = "……")
    private String sourceMaterial;

    @Schema(description = "世界观设定", example = "……")
    private String worldbuilding;

    @Schema(description = "修订要求", example = "结尾加一个反转")
    private String revisionNote;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
