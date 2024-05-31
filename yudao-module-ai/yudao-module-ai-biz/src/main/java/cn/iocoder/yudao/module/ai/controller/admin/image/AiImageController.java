package cn.iocoder.yudao.module.ai.controller.admin.image;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.client.vo.MidjourneyNotifyReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageDallReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageMidjourneyImagineReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.service.image.AiImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
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

    // TODO @fan：建议把 dallDrawing、midjourney 融合成一个 draw 接口，异步绘制；然后返回一个 id 给前端；前端通过 get 接口轮询，直到获取到生成成功
    // TODO @芋艿: 参数差异较大
    // TODO @fan：直接参数平铺？写好注释，要么？
    @Operation(summary = "dall2/dall3绘画", description = "openAi dall3是付费的!")
    @PostMapping("/dall")
    public CommonResult<Long> dall(@Validated @RequestBody AiImageDallReqVO req) {
        return success(imageService.dall(getLoginUserId(), req));
    }

    @Operation(summary = "删除【我的】绘画记录")
    @DeleteMapping("/delete-my")
    @Parameter(name = "id", required = true, description = "绘画编号", example = "1024")
    public CommonResult<Boolean> deleteImageMy(@RequestParam("id") Long id) {
        imageService.deleteImageMy(id, getLoginUserId());
        return success(true);
    }

    // ================ midjourney 接口

    @Operation(summary = "midjourney-imagine 绘画", description = "...")
    @PostMapping("/midjourney/imagine")
    public CommonResult<Long> midjourneyImagine(@Validated @RequestBody AiImageMidjourneyImagineReqVO req) {
        return success(imageService.midjourneyImagine(getLoginUserId(), req));
    }

    // TODO @fan：可以考虑，复用 AiImageDallRespVO，统一成 AIImageRespVO
    @Operation(summary = "midjourney proxy - 回调通知")
    @RequestMapping("/midjourney-notify")
    public CommonResult<Boolean> midjourneyNotify(MidjourneyNotifyReqVO notifyReqVO) {
        return success(imageService.midjourneyNotify(getLoginUserId(), notifyReqVO));
    }

}
