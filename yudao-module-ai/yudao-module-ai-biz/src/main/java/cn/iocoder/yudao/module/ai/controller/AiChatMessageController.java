package cn.iocoder.yudao.module.ai.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.service.AiChatMessageService;
import cn.iocoder.yudao.module.ai.vo.AiChatMessageListRes;
import cn.iocoder.yudao.module.ai.vo.AiChatMessageReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * chat message
 *
 * @author fansili
 * @time 2024/4/24 17:22
 * @since 1.0
 */
@Tag(name = "A3-聊天-对话")
@RestController
@RequestMapping("/ai/chat/message")
@Slf4j
@AllArgsConstructor
public class AiChatMessageController {

    private final AiChatMessageService chatMessageService;

    @Operation(summary = "聊天记录", description = "查询个人的聊天记录")
    @GetMapping("/list")
    public PageResult<AiChatMessageListRes> list(@Validated @ModelAttribute AiChatMessageReq req) {
        return chatMessageService.list(req);
    }

    @Operation(summary = "聊天记录 - 删除", description = "删除记录")
    @DeleteMapping("/{chatConversationId}/{id}")
    public CommonResult delete(@PathVariable("chatConversationId") Long chatConversationId,
                               @PathVariable("id") Long id) {
        chatMessageService.delete(chatConversationId, id);
        return CommonResult.success(null);
    }
}
