package cn.iocoder.yudao.module.ai.controller.admin.image;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.*;
import cn.iocoder.yudao.module.ai.service.image.AiImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

// TODO @芋艿：整理接口定义
// TODO @fan：参考 AiChatMessageController 改下 swagger 注解、注释
/**
 * ai作图
 *
 * @author fansili
 * @time 2024/4/25 15:49
 * @since 1.0
 */
@Tag(name = "A10-ai作图")
@RestController
@RequestMapping("/ai/image")
@Slf4j
@AllArgsConstructor
public class AiImageController {

    // TODO @fan：使用 @Resource 注入哈；然后 aiImageService => imageService；
    private final AiImageService aiImageService;

    // TODO @fan：分页列表，建议是 getImagePage，包括接口 path 也建议改下哈；
    // TODO @fan：@ModelAttribute 不需要哈；
    // TODO @fan：这个要不搞成 my-page？因为是我的哈
    @Operation(summary = "获取image列表", description = "dall3、midjourney")
    @GetMapping("/list")
    public CommonResult<PageResult<AiImageListRespVO>> list(@Validated @ModelAttribute AiImageListReqVO req) {
        // TODO @fan：import static，这样只要 success() 就行啦
        return CommonResult.success(aiImageService.list(req));
    }

    // TODO @fan：搞成 get-my？
    // TODO @fan：方法名改下哈。
    @Operation(summary = "获取image信息", description = "获取image信息")
    @GetMapping("/get")
    public CommonResult<AiImageListRespVO> get(@RequestParam("id") Long id) {
        return CommonResult.success(aiImageService.get(id));
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

    // TODO @fan：delete-my？需要校验是不是当前人哈
    @Operation(summary = "删除绘画记录", description = "")
    @DeleteMapping("/delete")
    public CommonResult<Void> delete(@RequestParam("id") Long id) {
        aiImageService.delete(id);
        return success(null);
    }

}
