package cn.iocoder.yudao.module.ai.controller.admin.image;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageDallReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageListReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageMidjourneyImagineReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImagePageMyRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.service.image.AiImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - Ai 绘画")
@RestController
@RequestMapping("/ai/image")
@Slf4j
public class AiImageController {

    @Resource
    private AiImageService aiImageService;

    @Operation(summary = "获取【我的】绘图分页")
    @GetMapping("/my-page")
    public CommonResult<PageResult<AiImagePageMyRespVO>> getImagePageMy(@Validated AiImageListReqVO req) {
        // 转换 resp
        PageResult<AiImageDO> pageResult = aiImageService.getImagePageMy(getLoginUserId(), req);
        // 转换 PageResult<AiImageListRespVO> 返回
        PageResult<AiImagePageMyRespVO> result = new PageResult<>();
        result.setTotal(pageResult.getTotal());
        result.setList(BeanUtils.toBean(pageResult.getList(), AiImagePageMyRespVO.class));
        return success(result);
    }

    // TODO @fan：类似 /my-page 的建议
    @Operation(summary = "获取【我的】绘图记录", description = "...")
    @GetMapping("/get-my")
    public CommonResult<AiImagePageMyRespVO> getMy(@RequestParam("id") Long id) {
        // 获取 image 信息
        AiImageDO imageDO = aiImageService.getMy(id);
        // 转 resp 并返回
        return CommonResult.success(BeanUtils.toBean(imageDO, AiImagePageMyRespVO.class));
    }

    // TODO @fan：建议把 dallDrawing、midjourney 融合成一个 draw 接口，异步绘制；然后返回一个 id 给前端；前端通过 get 接口轮询，直到获取到生成成功
    // TODO @芋艿: 参数差异较大
    @Operation(summary = "dall2/dall3绘画", description = "openAi dall3是付费的!")
    @PostMapping("/dall")
    public CommonResult<Long> dall(@Validated @RequestBody AiImageDallReqVO req) {
        return success(aiImageService.dall(getLoginUserId(), req));
    }

    @Operation(summary = "midjourney-imagine 绘画", description = "...")
    @PostMapping("/midjourney/imagine")
    public CommonResult<Long> midjourneyImagine(@Validated @RequestBody AiImageMidjourneyImagineReqVO req) {
        return success(aiImageService.midjourneyImagine(getLoginUserId(), req));
    }

    @Operation(summary = "删除【我的】绘画记录")
    @DeleteMapping("/delete-id-my")
    @Parameter(name = "id", required = true, description = "绘画编号", example = "1024")
    public CommonResult<Boolean> deleteIdMy(@RequestParam("id") Long id) {
        return success(aiImageService.deleteIdMy(id, getLoginUserId()));
    }

    @Operation(summary = "删除【我的】绘画记录")
    @RequestMapping("/midjourney-notify")
    public CommonResult<Boolean> midjourneyNotify(HttpServletRequest request) {
        return success(true);
    }
}
