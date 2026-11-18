package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI 短剧从故事想法创建项目（流式生成初版剧本） Response VO：生成中逐段返回增量文本，最后一条返回项目编号")
@Data
public class AiDramaScriptCreateRespVO {

    @Schema(description = "增量文本：生成中返回", example = "{\"title\":\"末班")
    private String content;

    @Schema(description = "项目编号：项目与剧本保存成功后返回", example = "1024")
    private Long projectId;

}
