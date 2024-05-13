package cn.iocoder.yudao.module.ai.controller.admin.chat;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.*;
import cn.iocoder.yudao.module.ai.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

// TODO @芋艿：权限标识；
@Tag(name = "管理后台 - 聊天消息")
@RestController
@RequestMapping("/ai/chat/message")
@Slf4j
public class AiChatMessageController {
    @Resource
    private AiChatService chatService;

    @Operation(summary = "发送消息（段式）", description = "一次性返回，响应较慢")
    @PostMapping("/send")
    public CommonResult<AiChatMessageRespVO> sendMessage(@Validated @RequestBody AiChatMessageSendReqVO sendReqVO) {
        // TODO done @fan：使用 static import；这样就 success 就行了；
        return success(chatService.chat(sendReqVO));
    }

    // TODO @芋艿：调用这个方法异常，Unable to handle the Spring Security Exception because the response is already committed.；可以再试试
    // TODO @fan：要不要使用 Flux 来返回；可以使用 Flux<AiChatMessageRespVO>
    @Operation(summary = "发送消息（流式）", description = "流式返回，响应较快")
    @PostMapping(value = "/send-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PermitAll // 解决 SSE 最终响应的时候，会被 Access Denied 拦截的问题
    public Flux<AiChatMessageRespVO> sendMessageStream(@Validated @RequestBody AiChatMessageSendStreamReqVO sendReqVO) {
        return chatService.chatStream(sendReqVO);
    }

    @Operation(summary = "添加/提问", description = "先创建好 message 前端才好渲染")
    @PostMapping(value = "/add")
    public CommonResult<AiChatMessageRespVO> add(@Validated @RequestBody AiChatMessageAddReqVO req) {
        return success(chatService.add(req));
    }

    @Operation(summary = "获得指定会话的消息列表")
    @GetMapping("/list-by-conversation-id")
    @Parameter(name = "conversationId", required = true, description = "会话编号", example = "1024")
    public CommonResult<List<AiChatMessageRespVO>> getMessageListByConversationId(@RequestParam("conversationId") Long conversationId) {
        return success(chatService.getMessageListByConversationId(conversationId));
    }

    @Operation(summary = "删除消息")
    @DeleteMapping("/delete")
    @Parameter(name = "id", required = true, description = "消息编号", example = "1024")
    public CommonResult<Boolean> deleteMessage(@RequestParam("id") Long id) {
        return success(chatService.deleteMessage(id));
    }

}
