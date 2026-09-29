package cn.iocoder.yudao.module.ai1.controller.admin.chat;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.chat.vo.message.Ai1ChatMessageRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.chat.vo.message.Ai1ChatMessageSendReqVO;
import cn.iocoder.yudao.module.ai1.service.chat.Ai1ChatConversationService;
import cn.iocoder.yudao.module.ai1.service.chat.Ai1ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - AI1 对话消息")
@RestController
@RequestMapping("/ai1/chat/message")
@Validated
public class Ai1ChatMessageController {

    @Resource
    private Ai1ChatMessageService chatMessageService;
    @Resource
    private Ai1ChatConversationService chatConversationService;

    // TODO @AI：分块下，哪些是 我的；哪些是管理的。类似 === === 这种噢；

    @GetMapping("/my-list")
    @Operation(summary = "获得【我的】对话消息列表")
    @Parameter(name = "conversationId", description = "对话编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai1:chat:query')")
    public CommonResult<List<Ai1ChatMessageRespVO>> getChatMessageMyList(@RequestParam("conversationId") Long conversationId) {
        chatConversationService.validateChatConversationMy(getLoginUserId(), conversationId);
        return success(BeanUtils.toBean(chatMessageService.getChatMessageListByConversationId(conversationId),
                Ai1ChatMessageRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得对话消息列表", description = "管理端查看任意用户的对话明细")
    @Parameter(name = "conversationId", description = "对话编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai1:chat-conversation:query')")
    public CommonResult<List<Ai1ChatMessageRespVO>> getChatMessageList(@RequestParam("conversationId") Long conversationId) {
        return success(BeanUtils.toBean(chatMessageService.getChatMessageListByConversationId(conversationId),
                Ai1ChatMessageRespVO.class));
    }

    @PostMapping(value = "/send-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "发送消息（流式）", description = "SSE 事件：stream（助手消息编号）、thinking、message、ping、done、error；"
            + "事件 data 均为 JSON 字符串，事件 id 为结果流条目编号，可用于续传")
    @PreAuthorize("@ss.hasPermission('ai1:chat:query')")
    public SseEmitter sendChatMessageStream(@Valid @RequestBody Ai1ChatMessageSendReqVO sendReqVO) {
        return chatMessageService.sendChatMessageStream(getLoginUserId(), sendReqVO);
    }

    @PostMapping(value = "/resume-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "断线续传（流式）", description = "从结果流 lastEventId 之后继续转发，不重新生成；事件格式同发送消息")
    @Parameter(name = "messageId", description = "助手消息编号", required = true, example = "1024")
    @Parameter(name = "lastEventId", description = "已收到的最后一个事件编号，为空时从头重放", example = "1727541234567-0")
    @PreAuthorize("@ss.hasPermission('ai1:chat:query')")
    public SseEmitter resumeChatMessageStream(@RequestParam("messageId") Long messageId,
                                              @RequestParam(value = "lastEventId", required = false) String lastEventId) {
        return chatMessageService.resumeChatMessageStream(getLoginUserId(), messageId, lastEventId);
    }

}
