package cn.iocoder.yudao.module.ai.controller.admin.drama;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.character.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.location.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.motion.AiDramaMotionReferenceCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.visualAsset.*;
import cn.iocoder.yudao.module.ai.service.drama.AiDramaAssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - AI 短剧资产")
@RestController
@RequestMapping("/ai/drama/asset")
public class AiDramaAssetController {

    @Resource
    private AiDramaAssetService assetService;

    // ================ 角色 ================

    @PostMapping("/character/create")
    @Operation(summary = "创建角色", description = "同时创建主形象（序号 0）")
    public CommonResult<Long> createCharacter(@Valid @RequestBody AiDramaCharacterSaveReqVO createReqVO) {
        return success(assetService.createCharacterMy(getLoginUserId(), createReqVO));
    }

    @PutMapping("/character/update")
    @Operation(summary = "修改角色")
    public CommonResult<Boolean> updateCharacter(@Valid @RequestBody AiDramaCharacterSaveReqVO updateReqVO) {
        assetService.updateCharacterMy(getLoginUserId(), updateReqVO);
        return success(true);
    }

    @DeleteMapping("/character/delete")
    @Operation(summary = "删除角色", description = "一并删除形象、样音")
    @Parameter(name = "id", required = true, description = "角色编号", example = "1024")
    public CommonResult<Boolean> deleteCharacter(@RequestParam("id") Long id) {
        assetService.deleteCharacterMy(getLoginUserId(), id);
        return success(true);
    }

    // ================ 角色形象 ================

    @PostMapping("/appearance/create")
    @Operation(summary = "创建角色形象")
    public CommonResult<Long> createAppearance(@Valid @RequestBody AiDramaAppearanceSaveReqVO createReqVO) {
        return success(assetService.createAppearanceMy(getLoginUserId(), createReqVO));
    }

    @PutMapping("/appearance/update")
    @Operation(summary = "修改角色形象", description = "可修改选用的候选图")
    public CommonResult<Boolean> updateAppearance(@Valid @RequestBody AiDramaAppearanceSaveReqVO updateReqVO) {
        assetService.updateAppearanceMy(getLoginUserId(), updateReqVO);
        return success(true);
    }

    @DeleteMapping("/appearance/delete")
    @Operation(summary = "删除角色形象")
    @Parameter(name = "id", required = true, description = "角色形象编号", example = "1024")
    public CommonResult<Boolean> deleteAppearance(@RequestParam("id") Long id) {
        assetService.deleteAppearanceMy(getLoginUserId(), id);
        return success(true);
    }

    @PostMapping("/appearance/generate-image")
    @Operation(summary = "生成角色形象的候选图", description = "异步出图，前端轮询项目详情")
    public CommonResult<Boolean> generateAppearanceImage(@Valid @RequestBody AiDramaImageGenerateReqVO reqVO) {
        assetService.generateAppearanceImageMy(getLoginUserId(), reqVO);
        return success(true);
    }

    @DeleteMapping("/appearance/delete-candidate")
    @Operation(summary = "删除角色形象的一张候选图", description = "出图中不允许；删除前的候选图记为上一版，可撤销")
    @Parameters({
            @Parameter(name = "id", required = true, description = "角色形象编号", example = "1024"),
            @Parameter(name = "index", required = true, description = "候选图下标，从 0 开始", example = "0")
    })
    public CommonResult<Boolean> deleteAppearanceCandidate(@RequestParam("id") Long id, @RequestParam("index") Integer index) {
        assetService.deleteAppearanceCandidateMy(getLoginUserId(), id, index);
        return success(true);
    }

    @PutMapping("/appearance/undo-image")
    @Operation(summary = "撤销角色形象的上次候选图变更", description = "恢复为上一版候选图，只能撤销一次")
    @Parameter(name = "id", required = true, description = "角色形象编号", example = "1024")
    public CommonResult<Boolean> undoAppearanceImage(@RequestParam("id") Long id) {
        assetService.undoAppearanceImageMy(getLoginUserId(), id);
        return success(true);
    }

    // ================ 场景 ================

    @PostMapping("/location/create")
    @Operation(summary = "创建场景")
    public CommonResult<Long> createLocation(@Valid @RequestBody AiDramaLocationSaveReqVO createReqVO) {
        return success(assetService.createLocationMy(getLoginUserId(), createReqVO));
    }

    @PutMapping("/location/update")
    @Operation(summary = "修改场景", description = "可修改选用的候选图")
    public CommonResult<Boolean> updateLocation(@Valid @RequestBody AiDramaLocationSaveReqVO updateReqVO) {
        assetService.updateLocationMy(getLoginUserId(), updateReqVO);
        return success(true);
    }

    @DeleteMapping("/location/delete")
    @Operation(summary = "删除场景")
    @Parameter(name = "id", required = true, description = "场景编号", example = "1024")
    public CommonResult<Boolean> deleteLocation(@RequestParam("id") Long id) {
        assetService.deleteLocationMy(getLoginUserId(), id);
        return success(true);
    }

    @PostMapping("/location/generate-image")
    @Operation(summary = "生成场景的候选图", description = "异步出图，前端轮询项目详情")
    public CommonResult<Boolean> generateLocationImage(@Valid @RequestBody AiDramaImageGenerateReqVO reqVO) {
        assetService.generateLocationImageMy(getLoginUserId(), reqVO);
        return success(true);
    }

    @DeleteMapping("/location/delete-candidate")
    @Operation(summary = "删除场景的一张候选图", description = "出图中不允许；删除前的候选图记为上一版，可撤销")
    @Parameters({
            @Parameter(name = "id", required = true, description = "场景编号", example = "1024"),
            @Parameter(name = "index", required = true, description = "候选图下标，从 0 开始", example = "0")
    })
    public CommonResult<Boolean> deleteLocationCandidate(@RequestParam("id") Long id, @RequestParam("index") Integer index) {
        assetService.deleteLocationCandidateMy(getLoginUserId(), id, index);
        return success(true);
    }

    @PutMapping("/location/undo-image")
    @Operation(summary = "撤销场景的上次候选图变更", description = "恢复为上一版候选图，只能撤销一次")
    @Parameter(name = "id", required = true, description = "场景编号", example = "1024")
    public CommonResult<Boolean> undoLocationImage(@RequestParam("id") Long id) {
        assetService.undoLocationImageMy(getLoginUserId(), id);
        return success(true);
    }

    // ================ 视觉资产 ================

    @PostMapping("/visual-asset/create")
    @Operation(summary = "创建道具")
    public CommonResult<Long> createVisualAsset(@Valid @RequestBody AiDramaVisualAssetSaveReqVO createReqVO) {
        return success(assetService.createVisualAssetMy(getLoginUserId(), createReqVO));
    }

    @PutMapping("/visual-asset/update")
    @Operation(summary = "修改道具")
    public CommonResult<Boolean> updateVisualAsset(@Valid @RequestBody AiDramaVisualAssetSaveReqVO updateReqVO) {
        assetService.updateVisualAssetMy(getLoginUserId(), updateReqVO);
        return success(true);
    }

    @DeleteMapping("/visual-asset/delete")
    @Operation(summary = "删除道具", description = "历史版本一并删除")
    @Parameter(name = "id", required = true, description = "视觉资产编号", example = "1024")
    public CommonResult<Boolean> deleteVisualAsset(@RequestParam("id") Long id) {
        assetService.deleteVisualAssetMy(getLoginUserId(), id);
        return success(true);
    }

    @PostMapping("/visual-asset/generate-image")
    @Operation(summary = "生成道具的图片", description = "已有图片时先归档为历史版本；异步出图，前端轮询项目详情")
    public CommonResult<Boolean> generateVisualAssetImage(@Valid @RequestBody AiDramaImageGenerateReqVO reqVO) {
        assetService.generateVisualAssetImageMy(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/visual-asset/undo-image")
    @Operation(summary = "撤销视觉资产的上次出图", description = "道具、关键帧通用：与最近的历史版本互换")
    @Parameter(name = "id", required = true, description = "视觉资产编号", example = "1024")
    public CommonResult<Boolean> undoVisualAssetImage(@RequestParam("id") Long id) {
        assetService.undoVisualAssetImageMy(getLoginUserId(), id);
        return success(true);
    }

    @PostMapping("/generate-missing-images")
    @Operation(summary = "一键补齐图片", description = "id 为项目编号；没有图片、且不在生成中的资产全部提交出图")
    public CommonResult<Integer> generateMissingImages(@Valid @RequestBody AiDramaImageGenerateReqVO reqVO) {
        return success(assetService.generateMissingImagesMy(getLoginUserId(), reqVO));
    }

    // ================ 音频 ================

    @PostMapping("/audio/create")
    @Operation(summary = "上传音频", description = "角色样音、旁白、配乐、参考音频；文件先通过文件上传接口上传")
    public CommonResult<Long> createAudio(@Valid @RequestBody AiDramaAudioCreateReqVO createReqVO) {
        return success(assetService.createAudioMy(getLoginUserId(), createReqVO));
    }

    @PutMapping("/audio/update")
    @Operation(summary = "修改配乐、参考音频", description = "名称、使用的分镜、音量、起始偏移；使用的分镜为空表示候选")
    public CommonResult<Boolean> updateAudio(@Valid @RequestBody AiDramaAudioUpdateReqVO updateReqVO) {
        assetService.updateAudioMy(getLoginUserId(), updateReqVO);
        return success(true);
    }

    @PostMapping("/audio/generate")
    @Operation(summary = "生成音频", description = "使用语音模型同步合成；有角色时为样音，否则为旁白")
    public CommonResult<Long> generateAudio(@Valid @RequestBody AiDramaAudioGenerateReqVO reqVO) {
        return success(assetService.generateAudioMy(getLoginUserId(), reqVO));
    }

    @PutMapping("/audio/select")
    @Operation(summary = "选用样音", description = "记录为角色选用的样音")
    @Parameter(name = "id", required = true, description = "音频编号", example = "1024")
    public CommonResult<Boolean> selectAudio(@RequestParam("id") Long id) {
        assetService.selectAudioMy(getLoginUserId(), id);
        return success(true);
    }

    @DeleteMapping("/audio/delete")
    @Operation(summary = "删除音频")
    @Parameter(name = "id", required = true, description = "音频编号", example = "1024")
    public CommonResult<Boolean> deleteAudio(@RequestParam("id") Long id) {
        assetService.deleteAudioMy(getLoginUserId(), id);
        return success(true);
    }

    @PostMapping("/audio/create-music")
    @Operation(summary = "选用 AI 音乐为配乐", description = "AI 音乐须已生成成功")
    public CommonResult<Long> createMusicAudio(@Valid @RequestBody AiDramaAudioMusicReqVO reqVO) {
        return success(assetService.createMusicAudioMy(getLoginUserId(), reqVO));
    }

    // ================ 动作参考 ================

    @PostMapping("/motion-reference/create")
    @Operation(summary = "添加动作参考视频", description = "文件先通过文件上传接口上传；2 ~ 30 秒、24 ~ 60fps 的 H.264 / H.265")
    public CommonResult<Long> createMotionReference(@Valid @RequestBody AiDramaMotionReferenceCreateReqVO createReqVO) {
        return success(assetService.createMotionReferenceMy(getLoginUserId(), createReqVO));
    }

    @DeleteMapping("/motion-reference/delete")
    @Operation(summary = "删除动作参考视频")
    @Parameter(name = "id", required = true, description = "动作参考编号", example = "1024")
    public CommonResult<Boolean> deleteMotionReference(@RequestParam("id") Long id) {
        assetService.deleteMotionReferenceMy(getLoginUserId(), id);
        return success(true);
    }

}
