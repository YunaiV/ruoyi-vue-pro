package cn.iocoder.yudao.module.ai.controller.admin.drama;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.audio.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.character.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.location.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.asset.visualAsset.*;
import cn.iocoder.yudao.module.ai.service.drama.AiDramaAssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * @author 芋道源码
 */
@Tag(name = "管理后台 - AI 短剧资产")
@RestController
@RequestMapping("/ai/drama/asset")
public class AiDramaAssetController {

    @Resource
    private AiDramaAssetService assetService;

    // ================ 角色 ================

    @PostMapping("/character/create")
    @Operation(summary = "创建角色", description = "同时创建第 1 个形象")
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

    // ================ 视觉资产 ================

    @PostMapping("/visual-asset/create")
    @Operation(summary = "创建视觉资产")
    public CommonResult<Long> createVisualAsset(@Valid @RequestBody AiDramaVisualAssetSaveReqVO createReqVO) {
        return success(assetService.createVisualAssetMy(getLoginUserId(), createReqVO));
    }

    @PutMapping("/visual-asset/update")
    @Operation(summary = "修改视觉资产")
    public CommonResult<Boolean> updateVisualAsset(@Valid @RequestBody AiDramaVisualAssetSaveReqVO updateReqVO) {
        assetService.updateVisualAssetMy(getLoginUserId(), updateReqVO);
        return success(true);
    }

    @DeleteMapping("/visual-asset/delete")
    @Operation(summary = "删除视觉资产")
    @Parameter(name = "id", required = true, description = "视觉资产编号", example = "1024")
    public CommonResult<Boolean> deleteVisualAsset(@RequestParam("id") Long id) {
        assetService.deleteVisualAssetMy(getLoginUserId(), id);
        return success(true);
    }

    @PostMapping("/visual-asset/generate-image")
    @Operation(summary = "生成视觉资产的图片", description = "异步出图，前端轮询项目详情")
    public CommonResult<Boolean> generateVisualAssetImage(@Valid @RequestBody AiDramaImageGenerateReqVO reqVO) {
        assetService.generateVisualAssetImageMy(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/generate-missing-images")
    @Operation(summary = "一键补齐图片", description = "id 为项目编号；没有图片、且不在生成中的资产全部提交出图")
    public CommonResult<Integer> generateMissingImages(@Valid @RequestBody AiDramaImageGenerateReqVO reqVO) {
        return success(assetService.generateMissingImagesMy(getLoginUserId(), reqVO));
    }

    // ================ 音频 ================

    @PostMapping("/audio/create")
    @Operation(summary = "上传音频", description = "角色样音、旁白；文件先通过文件上传接口上传")
    public CommonResult<Long> createAudio(@Valid @RequestBody AiDramaAudioCreateReqVO createReqVO) {
        return success(assetService.createAudioMy(getLoginUserId(), createReqVO));
    }

    @PostMapping("/audio/generate")
    @Operation(summary = "生成音频", description = "使用语音模型同步合成；有角色时为样音，否则为旁白")
    public CommonResult<Long> generateAudio(@Valid @RequestBody AiDramaAudioGenerateReqVO reqVO) {
        return success(assetService.generateAudioMy(getLoginUserId(), reqVO));
    }

    @PutMapping("/audio/select")
    @Operation(summary = "选用样音", description = "同一角色只能选用一个")
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

}
