package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.character;

import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio.AiDramaAudioRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 短剧角色 Response VO")
@Data
public class AiDramaCharacterRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long projectId;

    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "林夏")
    private String name;

    @Schema(description = "角色设定", example = "25 岁")
    private String description;

    @Schema(description = "角色出图提示词", example = "短发女孩")
    private String prompt;

    @Schema(description = "角色参考图", example = "https://www.iocoder.cn/1.png")
    private String referenceImageUrl;

    @Schema(description = "形象版本")
    private List<AiDramaAppearanceRespVO> appearances;

    @Schema(description = "样音")
    private List<AiDramaAudioRespVO> audios;

}
