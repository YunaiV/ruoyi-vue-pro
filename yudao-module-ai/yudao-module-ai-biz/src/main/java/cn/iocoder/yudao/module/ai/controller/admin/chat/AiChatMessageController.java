package cn.iocoder.yudao.module.ai.controller.admin.chat;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.Utf8SseEmitter;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageRespVO;
import cn.iocoder.yudao.module.ai.service.AiChatService;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.AiChatMessageSendReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
    public CommonResult<AiChatMessageRespVO> sendMessage(@Validated @ModelAttribute AiChatMessageSendReqVO sendReqVO) {
        // TODO @fan：使用 static import；这样就 success 就行了；
        return success(chatService.chat(sendReqVO));
    }

    // TODO @芋艿：调用这个方法异常，Unable to handle the Spring Security Exception because the response is already committed.；可以再试试
    // TODO @fan：要不要使用 Flux 来返回；可以使用 Flux<AiChatMessageRespVO>
    @Operation(summary = "发送消息（流式）", description = "流式返回，响应较快")
    @PostMapping(value = "/send-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessageStream(@Validated @ModelAttribute AiChatMessageSendReqVO sendReqVO) {
        Utf8SseEmitter sseEmitter = new Utf8SseEmitter();
        chatService.chatStream(sendReqVO, sseEmitter);
        return sseEmitter;
    }

    @Operation(summary = "获得指定会话的消息列表")
    @GetMapping("/list-by-conversation-id")
    @Parameter(name = "conversationId", required = true, description = "会话编号", example = "1024")
    public CommonResult<List<AiChatMessageRespVO>> getMessageListByConversationId(@RequestParam("conversationId") Long conversationId) {
        return success(null);
    }

    @Operation(summary = "删除消息")
    @DeleteMapping("/delete")
    @Parameter(name = "id", required = true, description = "消息编号", example = "1024")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        return success(null);
    }

}
