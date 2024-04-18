package cn.iocoder.yudao.module.ai.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.service.ChatConversationService;
import cn.iocoder.yudao.module.ai.vo.ChatConversationCreateReq;
import cn.iocoder.yudao.module.ai.vo.ChatConversationCreateRes;
import cn.iocoder.yudao.module.ai.vo.ChatConversationListReq;
import cn.iocoder.yudao.module.ai.vo.ChatConversationRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ia 模块
 *
 * @author fansili
 * @time 2024/4/13 17:44
 * @since 1.0
 */
@Tag(name = "A2聊天-对话")
@RestController
@RequestMapping("/ai/chat/conversation")
@Slf4j
@AllArgsConstructor
public class ChatConversationController {

    private final ChatConversationService chatConversationService;

    @Operation(summary = "创建 - 对话")
    @PostMapping("/create")
    public CommonResult<ChatConversationRes> create(@RequestBody @Validated ChatConversationCreateReq req) {
        return CommonResult.success(chatConversationService.create(req));
    }

    @Operation(summary = "获取 - 获取对话")
    @GetMapping("/getConversation")
    public CommonResult<ChatConversationRes> getConversation(@RequestParam("id") Long id) {
        return CommonResult.success(chatConversationService.getConversation(id));
    }

    @Operation(summary = "获取 - 获取对话list")
    @GetMapping("/listConversation")
    public CommonResult<List<ChatConversationRes>> listConversation(@ModelAttribute @Validated ChatConversationListReq req) {
        return CommonResult.success(chatConversationService.listConversation(req));
    }

    @Operation(summary = "删除")
    @DeleteMapping("/listConversation")
    public CommonResult<Void> delete(@RequestParam("id") Long id) {
        chatConversationService.delete(id);
        return CommonResult.success(null);
    }
}
