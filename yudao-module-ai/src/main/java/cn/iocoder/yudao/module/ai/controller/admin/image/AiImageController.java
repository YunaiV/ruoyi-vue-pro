package cn.iocoder.yudao.module.ai.controller.admin.image;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.UserRateLimiterKeyResolver;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.*;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.enums.image.AiImageInputRoleEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.module.ai.service.image.AiImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - AI 绘画")
@RestController
@RequestMapping("/ai/image")
@Slf4j
public class AiImageController {

    @Resource
    private AiImageService imageService;

    @GetMapping("/my-page")
    @Operation(summary = "获取【我的】绘图分页")
    public CommonResult<PageResult<AiImageRespVO>> getImagePageMy(@Validated PageParam pageReqVO) {
        PageResult<AiImageDO> pageResult = imageService.getImagePageMy(getLoginUserId(), pageReqVO);
        return success(BeanUtils.toBean(pageResult, AiImageRespVO.class));
    }

    @GetMapping("/public-page")
    @Operation(summary = "获取公开的绘图分页")
    public CommonResult<PageResult<AiImageRespVO>> getImagePagePublic(@Validated AiImagePublicPageReqVO pageReqVO) {
        PageResult<AiImageDO> pageResult = imageService.getImagePagePublic(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AiImageRespVO.class, AiImageController::clearPublicResp));
    }

    @GetMapping("/get-public")
    @Operation(summary = "获取公开的绘图")
    @Parameter(name = "id", required = true, description = "绘画编号", example = "1024")
    public CommonResult<AiImageRespVO> getImagePublic(@RequestParam("id") Long id) {
        AiImageDO image = imageService.getImagePublic(id);
        return success(BeanUtils.toBean(image, AiImageRespVO.class, AiImageController::clearPublicResp));
    }

    // TODO DONE @AI：是不是不用单独的 VO？直接用原本的 VO 就行了？然后，直接设置可以么？（去掉 AiImagePublicRespVO，复用 AiImageRespVO 直接清空）
    // TODO DONE @AI：链式调用！
    /**
     * 清空公开作品不对外展示的字段；蒙版等过程图片，也不对外展示
     */
    private static void clearPublicResp(AiImageRespVO respVO) {
        respVO.setUserId(null).setApiKeyId(null).setTaskId(null).setErrorMessage(null)
                .setInputImages(filterList(respVO.getInputImages(),
                        item -> ObjUtil.notEqual(item.getRole(), AiImageInputRoleEnum.MASK.getRole())));
    }

    @GetMapping("/get-my")
    @Operation(summary = "获取【我的】绘图记录", description = "包含按钮动作、局部重绘产生的子任务")
    @Parameter(name = "id", required = true, description = "绘画编号", example = "1024")
    public CommonResult<AiImageRespVO> getImageMy(@RequestParam("id") Long id) {
        AiImageDO image = imageService.getImage(id);
        if (image == null || ObjUtil.notEqual(getLoginUserId(), image.getUserId())) {
            return success(null);
        }
        return success(buildImageDetail(image));
    }

    @GetMapping("/my-list-by-ids")
    @Operation(summary = "获取【我的】绘图记录列表")
    @Parameter(name = "ids", required = true, description = "绘画编号数组", example = "1024,2048")
    public CommonResult<List<AiImageRespVO>> getImageListMyByIds(@RequestParam("ids") List<Long> ids) {
        List<AiImageDO> imageList = imageService.getImageList(ids);
        imageList.removeIf(item -> !ObjUtil.equal(getLoginUserId(), item.getUserId()));
        return success(BeanUtils.toBean(imageList, AiImageRespVO.class));
    }

    @Operation(summary = "生成图片")
    @PostMapping("/draw")
    public CommonResult<Long> drawImage(@Valid @RequestBody AiImageDrawReqVO drawReqVO) {
        return success(imageService.drawImage(getLoginUserId(), drawReqVO));
    }

    @Operation(summary = "生成专业绘画指令", description = "使用默认的对话模型，把描述改写为专业的绘画提示词")
    @PostMapping("/optimize-prompt")
    @RateLimiter(time = 60, count = 10, keyResolver = UserRateLimiterKeyResolver.class)
    public CommonResult<String> optimizePrompt(@Valid @RequestBody AiImageOptimizePromptReqVO reqVO) {
        return success(imageService.optimizePrompt(getLoginUserId(), reqVO));
    }

    @Operation(summary = "删除【我的】绘画记录")
    @DeleteMapping("/delete-my")
    @Parameter(name = "id", required = true, description = "绘画编号", example = "1024")
    public CommonResult<Boolean> deleteImageMy(@RequestParam("id") Long id) {
        imageService.deleteImageMy(id, getLoginUserId());
        return success(true);
    }

    @Operation(summary = "更新【我的】绘画的公开状态")
    @PutMapping("/update-public-my")
    public CommonResult<Boolean> updateImagePublicMy(@Valid @RequestBody AiImageUpdateReqVO updateReqVO) {
        imageService.updateImagePublicMy(getLoginUserId(), updateReqVO);
        return success(true);
    }

    // ================ midjourney 专属 ================

    @Operation(summary = "【Midjourney】生成图片", description = "文生图、图生图、一致性")
    @PostMapping("/midjourney/imagine")
    public CommonResult<Long> midjourneyImagine(@Valid @RequestBody AiMidjourneyImagineReqVO reqVO) {
        return success(imageService.midjourneyImagine(getLoginUserId(), reqVO));
    }

    @Operation(summary = "【Midjourney】融图")
    @PostMapping("/midjourney/blend")
    public CommonResult<Long> midjourneyBlend(@Valid @RequestBody AiMidjourneyBlendReqVO reqVO) {
        return success(imageService.midjourneyBlend(getLoginUserId(), reqVO));
    }

    @Operation(summary = "【Midjourney】换脸")
    @PostMapping("/midjourney/swap-face")
    public CommonResult<Long> midjourneySwapFace(@Valid @RequestBody AiMidjourneySwapFaceReqVO reqVO) {
        return success(imageService.midjourneySwapFace(getLoginUserId(), reqVO));
    }

    @Operation(summary = "【Midjourney】通知图片进展", description = "由 Midjourney Proxy 回调")
    @PostMapping("/midjourney/notify") // 必须是 POST 方法，否则会报错
    @PermitAll
    @TenantIgnore
    public CommonResult<Boolean> midjourneyNotify(@Valid @RequestBody MidjourneyApi.Notify notify) {
        imageService.midjourneyNotify(notify);
        return success(true);
    }

    @Operation(summary = "【Midjourney】Action 操作（二次生成图片）", description = "例如说：放大、缩小、U1、U2 等")
    @PostMapping("/midjourney/action")
    public CommonResult<Long> midjourneyAction(@Valid @RequestBody AiMidjourneyActionReqVO reqVO) {
        return success(imageService.midjourneyAction(getLoginUserId(), reqVO));
    }

    @Operation(summary = "【Midjourney】局部重绘", description = "提交蒙版：黑色背景，白色为重绘区域")
    @PostMapping("/midjourney/modal")
    public CommonResult<Long> midjourneyModal(@Valid @RequestBody AiMidjourneyModalReqVO reqVO) {
        return success(imageService.midjourneyModal(getLoginUserId(), reqVO));
    }

    // ================ 绘图管理 ================

    @GetMapping("/page")
    @Operation(summary = "获得绘画分页")
    @PreAuthorize("@ss.hasPermission('ai:image:query')")
    public CommonResult<PageResult<AiImageRespVO>> getImagePage(@Valid AiImagePageReqVO pageReqVO) {
        PageResult<AiImageDO> pageResult = imageService.getImagePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AiImageRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得绘画", description = "任务详情：包含按钮动作、局部重绘产生的子任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai:image:query')")
    public CommonResult<AiImageRespVO> getImage(@RequestParam("id") Long id) {
        AiImageDO image = imageService.getImage(id);
        if (image == null) {
            return success(null);
        }
        // TODO DONE @AI：别的模块有返回 apikey name 么？如果没有，感觉可以简化；（不返回，前端按 apiKeyId 展示）
        // TODO DONE @AI：租户 id 是不是没必要返回。（不返回）
        return success(buildImageDetail(image));
    }

    private AiImageRespVO buildImageDetail(AiImageDO image) {
        // TODO DONE @AI：先 getImageListByParentId 列表。然后 beanutils；
        List<AiImageDO> children = imageService.getImageListByParentId(image.getId());
        return BeanUtils.toBean(image, AiImageRespVO.class)
                .setChildren(BeanUtils.toBean(children, AiImageRespVO.class));
    }

    @PutMapping("/update")
    @Operation(summary = "更新绘画")
    @PreAuthorize("@ss.hasPermission('ai:image:update')")
    public CommonResult<Boolean> updateImage(@Valid @RequestBody AiImageUpdateReqVO updateReqVO) {
        imageService.updateImage(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除绘画")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai:image:delete')")
    public CommonResult<Boolean> deleteImage(@RequestParam("id") Long id) {
        imageService.deleteImage(id);
        return success(true);
    }

}
