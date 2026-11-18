package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.project;

import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio.AiDramaAudioRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.character.AiDramaCharacterRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.location.AiDramaLocationRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.motion.AiDramaMotionReferenceRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.visualAsset.AiDramaVisualAssetRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 短剧项目详情 Response VO：工作台、管理端只读详情共用")
@Data
public class AiDramaProjectDetailRespVO {

    @Schema(description = "项目", requiredMode = Schema.RequiredMode.REQUIRED)
    private AiDramaProjectRespVO project;

    @Schema(description = "当前剧本（最新一条）")
    private AiDramaScriptRespVO script;

    @Schema(description = "角色：含形象版本与样音")
    private List<AiDramaCharacterRespVO> characters;

    @Schema(description = "场景")
    private List<AiDramaLocationRespVO> locations;

    @Schema(description = "视觉资产：道具、关键帧，以及它们的历史版本")
    private List<AiDramaVisualAssetRespVO> visualAssets;

    @Schema(description = "项目音频：旁白、配乐、参考音频等不属于角色的音频")
    private List<AiDramaAudioRespVO> audios;

    @Schema(description = "动作参考视频")
    private List<AiDramaMotionReferenceRespVO> motionReferences;

    @Schema(description = "分镜")
    private List<AiDramaStoryboardRespVO> storyboards;

}
