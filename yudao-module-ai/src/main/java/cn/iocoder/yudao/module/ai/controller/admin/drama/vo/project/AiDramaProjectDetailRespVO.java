package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.project;

import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio.AiDramaAudioRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.character.AiDramaCharacterRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.location.AiDramaLocationRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.visualAsset.AiDramaVisualAssetRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.script.AiDramaScriptRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard.AiDramaStoryboardRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

// DONE @AI：检查下，哪些 vo 可以直接内嵌掉，没必要单独类；（评估后不内嵌：这里引用的角色、场景、视觉资产、分镜、剧本、音频 VO 各对应一张表，按资产子包放置，剧本 VO 还被剧本接口直接返回；只有公开作品的角色 Cast 是展示用的裁剪字段，已是内部类）
/**
 * @author 芋道源码
 */
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

    @Schema(description = "视觉资产")
    private List<AiDramaVisualAssetRespVO> visualAssets;

    @Schema(description = "旁白音频")
    private List<AiDramaAudioRespVO> narrations;

    @Schema(description = "分镜")
    private List<AiDramaStoryboardRespVO> storyboards;

}
