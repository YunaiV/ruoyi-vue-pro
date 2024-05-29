package cn.iocoder.yudao.module.ai.controller.admin.image;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.*;
import cn.iocoder.yudao.module.ai.service.image.AiImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
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

    @Operation(summary = "获取 - 我的分页列表", description = "dall3、midjourney")
    @GetMapping("/my-page")
    public CommonResult<PageResult<AiImageListRespVO>> myPage(@Validated AiImageListReqVO req) {
        return success(aiImageService.list(req));
    }

    @Operation(summary = "获取 - 我的 image 信息", description = "...")
    @GetMapping("/get-my")
    public CommonResult<AiImageListRespVO> getMy(@RequestParam("id") Long id) {
        return CommonResult.success(aiImageService.getMy(id));
    }

    // TODO @fan：建议把 dallDrawing、midjourney 融合成一个 draw 接口，异步绘制；然后返回一个 id 给前端；前端通过 get 接口轮询，直到获取到生成成功
    @Operation(summary = "dall2/dall3绘画", description = "openAi dall3是付费的!")
    @PostMapping("/dall")
    public AiImageDallRespVO dallDrawing(@Validated @RequestBody AiImageDallReqVO req) {
        return aiImageService.dallDrawing(req);
    }

    @Operation(summary = "midjourney绘画", description = "midjourney图片绘画流程：1、提交任务 2、获取完成的任务 3、选择对应功能 4、获取最终结果")
    @PostMapping("/midjourney")
    public CommonResult<Void> midjourney(@Validated @RequestBody AiImageMidjourneyReqVO req) {
        aiImageService.midjourney(req);
        return success(null);
    }

    @Operation(summary = "midjourney绘画操作", description = "一般有选择图片、放大、换一批...")
    @PostMapping("/midjourney-operate")
    public CommonResult<Void> midjourneyOperate(@Validated @RequestBody AiImageMidjourneyOperateReqVO req) {
        aiImageService.midjourneyOperate(req);
        return success(null);
    }

    // TODO @fan：要不先不要 midjourneyOperate、cancelMidjourney 接口哈
    @Operation(summary = "取消 midjourney 绘画", description = "取消 midjourney 绘画")
    @PostMapping("/cancel-midjourney")
    public CommonResult<Void> cancelMidjourney(@RequestParam("id") Long id) {
        // @范 这里实现mj取消逻辑
        return success(null);
    }

    @Operation(summary = "删除绘画记录", description = "")
    @DeleteMapping("/delete-my")
    public CommonResult<Void> deleteMy(@RequestParam("id") Long id) {
        Long loginUserId = getLoginUserId();
        aiImageService.deleteMy(id, loginUserId);
        return success(null);
    }

}
