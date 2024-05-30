package cn.iocoder.yudao.module.ai.controller.admin.image;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.*;
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
    @Operation(summary = "dall2/dall3绘画", description = "openAi dall3是付费的!")
    @PostMapping("/dall")
    public CommonResult<Long> dall(@Validated @RequestBody AiImageDallReqVO req) {
        return success(aiImageService.dall(getLoginUserId(), req));
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

    @Operation(summary = "删除【我的】绘画记录")
    @DeleteMapping("/my-delete")
    @Parameter(name = "id", required = true, description = "绘画编号", example = "1024")
    public CommonResult<Boolean> myDelete(@RequestParam("id") Long id) {
        return success(aiImageService.myDelete(id, getLoginUserId()));
    }

}
