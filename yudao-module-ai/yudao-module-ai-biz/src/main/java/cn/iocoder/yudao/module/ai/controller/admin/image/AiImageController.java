package cn.iocoder.yudao.module.ai.controller.admin.image;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.*;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney.AiMidjourneyActionReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney.AiMidjourneyImagineReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
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
    public CommonResult<PageResult<AiImageRespVO>> getImagePageMy(@Validated AiImagePageReqVO pageReqVO) {
        PageResult<AiImageDO> pageResult = imageService.getImagePageMy(getLoginUserId(), pageReqVO);
        return success(BeanUtils.toBean(pageResult, AiImageRespVO.class));
    }

    @GetMapping("/public-page")
    @Operation(summary = "获取公开的绘图分页")
    public CommonResult<PageResult<AiImageRespVO>> getImagePagePublic(AiImagePublicPageReqVO pageReqVO) {
        PageResult<AiImageDO> pageResult = imageService.getImagePagePublic(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AiImageRespVO.class));
    }

    @GetMapping("/get-my")
    @Operation(summary = "获取【我的】绘图记录")
    @Parameter(name = "id", required = true, description = "绘画编号", example = "1024")
    public CommonResult<AiImageRespVO> getImageMy(@RequestParam("id") Long id) {
        AiImageDO image = imageService.getImage(id);
        if (image == null || ObjUtil.notEqual(getLoginUserId(), image.getUserId())) {
            return success(null);
        }
        return success(BeanUtils.toBean(image, AiImageRespVO.class));
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

    @Operation(summary = "删除【我的】绘画记录")
    @DeleteMapping("/delete-my")
    @Parameter(name = "id", required = true, description = "绘画编号", example = "1024")
    public CommonResult<Boolean> deleteImageMy(@RequestParam("id") Long id) {
        imageService.deleteImageMy(id, getLoginUserId());
        return success(true);
    }

    // ================ midjourney 专属 ================

    @Operation(summary = "【Midjourney】生成图片")
    @PostMapping("/midjourney/imagine")
    public CommonResult<Long> midjourneyImagine(@Valid @RequestBody AiMidjourneyImagineReqVO reqVO) {
        Long imageId = imageService.midjourneyImagine(getLoginUserId(), reqVO);
        return success(imageId);
    }

    @Operation(summary = "【Midjourney】通知图片进展", description = "由 Midjourney Proxy 回调")
    @PostMapping("/midjourney/notify") // 必须是 POST 方法，否则会报错
    @PermitAll
    public CommonResult<Boolean> midjourneyNotify(@Valid @RequestBody MidjourneyApi.Notify notify) {
        imageService.midjourneyNotify(notify);
        return success(true);
    }

    @Operation(summary = "【Midjourney】Action 操作（二次生成图片）", description = "例如说：放大、缩小、U1、U2 等")
    @PostMapping("/midjourney/action")
    public CommonResult<Long> midjourneyAction(@Valid @RequestBody AiMidjourneyActionReqVO reqVO) {
        Long imageId = imageService.midjourneyAction(getLoginUserId(), reqVO);
        return success(imageId);
    }

    // ================ 绘图管理 ================

    @GetMapping("/page")
    @Operation(summary = "获得绘画分页")
    @PreAuthorize("@ss.hasPermission('ai:image:query')")
    public CommonResult<PageResult<AiImageRespVO>> getImagePage(@Valid AiImagePageReqVO pageReqVO) {
        PageResult<AiImageDO> pageResult = imageService.getImagePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AiImageRespVO.class));
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