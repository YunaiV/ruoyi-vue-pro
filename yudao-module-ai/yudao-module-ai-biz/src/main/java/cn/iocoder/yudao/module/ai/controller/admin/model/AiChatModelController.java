package cn.iocoder.yudao.module.ai.controller.admin.model;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModelAddReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModelListReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModelListRespVO;
import cn.iocoder.yudao.module.ai.service.AiChatModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

// TODO @fan：调整下接口；相关 vo 的命名等等；modal => model

/**
 * ai 模型
 *
 * @author fansili
 * @time 2024/4/24 19:42
 * @since 1.0
 */
@Tag(name = "A6-AI模型")
@RestController
@RequestMapping("/ai/chat/model")
@Slf4j
@AllArgsConstructor
public class AiChatModelController {

    private final AiChatModelService aiChatModelService;

    @Operation(summary = "ai模型 - 模型列表")
    @GetMapping("/list")
    public PageResult<AiChatModelListRespVO> list(@ModelAttribute AiChatModelListReqVO req) {
        return aiChatModelService.list(req);
    }

    @Operation(summary = "ai模型 - 添加")
    @PutMapping("/add")
    public CommonResult<Void> add(@RequestBody @Validated AiChatModelAddReqVO req) {
        aiChatModelService.add(req);
        return CommonResult.success(null);
    }

    @Operation(summary = "ai模型 - 修改")
    @PostMapping("/update")
    public CommonResult<Void> update(@RequestParam("id") Long id,
                                     @RequestBody @Validated AiChatModelAddReqVO req) {
        aiChatModelService.update(id, req);
        return CommonResult.success(null);
    }

    @Operation(summary = "ai模型 - 删除")
    @DeleteMapping("/delete")
    public CommonResult<Void> delete(@RequestParam("id") Long id) {
        aiChatModelService.delete(id);
        return CommonResult.success(null);
    }
}
