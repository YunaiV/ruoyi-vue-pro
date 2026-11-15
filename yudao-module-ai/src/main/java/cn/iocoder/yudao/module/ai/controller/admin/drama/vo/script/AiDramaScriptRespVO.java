package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script;

import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaScriptDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 芋道源码
 */
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

    @Schema(description = "打磨意见与修改记录")
    private List<AiDramaScriptDO.RevisionNote> revisionNotes;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
