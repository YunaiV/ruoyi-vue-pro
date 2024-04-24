package cn.iocoder.yudao.module.ai.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.service.AiChatModalService;
import cn.iocoder.yudao.module.ai.vo.AiChatModalAddReq;
import cn.iocoder.yudao.module.ai.vo.AiChatModalListReq;
import cn.iocoder.yudao.module.ai.vo.AiChatModalListRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * ai 模型
 *
 * @author fansili
 * @time 2024/4/24 19:42
 * @since 1.0
 */
@Tag(name = "A6-AI模型")
@RestController
@RequestMapping("/ai/chat")
@Slf4j
@AllArgsConstructor
public class AiChatModalController {

    private final AiChatModalService aiChatModalService;

    @Operation(summary = "ai模型 - 模型照片上传")
    @GetMapping("/modal/list")
    public PageResult<AiChatModalListRes> list(@ModelAttribute AiChatModalListReq req) {
        return aiChatModalService.list(req);
    }

    @Operation(summary = "ai模型 - 添加")
    @PutMapping("/modal")
    public CommonResult add(@RequestBody @Validated AiChatModalAddReq req) {
        aiChatModalService.add(req);
        return CommonResult.success(null);
    }

    @Operation(summary = "ai模型 - 模型照片上传")
    @PostMapping("/modal/{id}/updateImage")
    public CommonResult updateImage(@PathVariable("id") Long id,
                                    MultipartFile file) {
        // todo yunai 文件上传这里放哪里
        return CommonResult.success(null);
    }


}
