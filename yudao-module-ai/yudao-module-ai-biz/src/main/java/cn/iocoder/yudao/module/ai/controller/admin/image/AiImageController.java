package cn.iocoder.yudao.module.ai.controller.admin.image;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.MidjourneyNotifyReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageDrawReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageMidjourneyImagineReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.service.image.AiImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - AI 绘画")
@RestController
@RequestMapping("/ai/image")
@Slf4j
public class AiImageController {

    @Resource
    private AiImageService imageService;

    @Operation(summary = "获取【我的】绘图分页")
    @GetMapping("/my-page")
    public CommonResult<PageResult<AiImageRespVO>> getImagePageMy(@Validated PageParam pageReqVO) {
        PageResult<AiImageDO> pageResult = imageService.getImagePageMy(getLoginUserId(), pageReqVO);
        return success(BeanUtils.toBean(pageResult, AiImageRespVO.class));
    }

    @Operation(summary = "获取【我的】绘图记录")
    @GetMapping("/get-my")
    public CommonResult<AiImageRespVO> getImageMy(@RequestParam("id") Long id) {
        AiImageDO image = imageService.getImage(id);
        if (image == null || ObjUtil.notEqual(getLoginUserId(), image.getUserId())) {
            return success(null);
        }
        return success(BeanUtils.toBean(image, AiImageRespVO.class));
    }

    @Operation(summary = "生成图片")
    @PostMapping("/draw")
    public CommonResult<Long> drawImage(@Validated @RequestBody AiImageDrawReqVO drawReqVO) {
        return success(imageService.drawImage(getLoginUserId(), drawReqVO));
    }

    @Operation(summary = "删除【我的】绘画记录")
    @DeleteMapping("/delete-my")
    @Parameter(name = "id", required = true, description = "绘画编号", example = "1024")
    public CommonResult<Boolean> deleteImageMy(@RequestParam("id") Long id) {
        imageService.deleteImageMy(id, getLoginUserId());
        return success(true);
    }

    // ================ midjourney 接口 ================

    @Operation(summary = "Midjourney imagine（绘画）")
    @PostMapping("/midjourney/imagine")
    public CommonResult<Long> midjourneyImagine(@Validated @RequestBody AiImageMidjourneyImagineReqVO req) {
        return success(imageService.midjourneyImagine(getLoginUserId(), req));
    }

    @Operation(summary = "Midjourney 回调通知", description = "由 Midjourney Proxy 回调")
    @PostMapping("/midjourney-notify")
    @PermitAll
    public void midjourneyNotify(@RequestBody MidjourneyNotifyReqVO notifyReqVO) {
        imageService.midjourneyNotify(notifyReqVO);
    }

    @Operation(summary = "Midjourney Action", description = "例如说：放大、缩小、U1、U2 等")
    @GetMapping("/midjourney/action")
    @Parameter(name = "id", description = "图片id", example = "1")
    @Parameter(name = "customId", description = "操作id", example = "MJ::JOB::upsample::1::85a4b4c1-8835-46c5-a15c-aea34fad1862")
    public CommonResult<Boolean> midjourneyAction(@RequestParam("id") Long imageId,
                                                  @RequestParam("customId") String customId) {
        imageService.midjourneyAction(getLoginUserId(), imageId, customId);
        return success(true);
    }

}