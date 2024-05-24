package cn.iocoder.yudao.module.ai.controller.admin.chat;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatMessageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.service.chat.AiChatConversationService;
import cn.iocoder.yudao.module.ai.service.chat.AiChatMessageService;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 聊天消息")
@RestController
@RequestMapping("/ai/chat/message")
@Slf4j
public class AiChatMessageController {

    @Resource
    private AiChatMessageService chatMessageService;
    @Resource
    private AiChatConversationService chatConversationService;
    @Resource
    private AiChatRoleService chatRoleService;

    @Resource
    private AdminUserApi adminUserApi;

    @Operation(summary = "发送消息（段式）", description = "一次性返回，响应较慢")
    @PostMapping("/send")
    public CommonResult<AiChatMessageRespVO> sendMessage(@Validated @RequestBody AiChatMessageSendReqVO sendReqVO) {
        return success(chatMessageService.sendMessage(sendReqVO));
    }

    @Operation(summary = "发送消息（流式）", description = "流式返回，响应较快")
    @PostMapping(value = "/send-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PermitAll // 解决 SSE 最终响应的时候，会被 Access Denied 拦截的问题
    public Flux<CommonResult<AiChatMessageSendRespVO>> sendChatMessageStream(@Validated @RequestBody AiChatMessageSendReqVO sendReqVO) {
        return chatMessageService.sendChatMessageStream(sendReqVO, getLoginUserId());
    }

    @Operation(summary = "获得指定会话的消息列表")
    @GetMapping("/list-by-conversation-id")
    @Parameter(name = "conversationId", required = true, description = "会话编号", example = "1024")
    public CommonResult<List<AiChatMessageRespVO>> getChatMessageListByConversationId(
            @RequestParam("conversationId") Long conversationId) {
        AiChatConversationDO conversation = chatConversationService.getChatConversation(conversationId);
        if (conversation == null || ObjUtil.notEqual(conversation.getUserId(), getLoginUserId())) {
            return success(Collections.emptyList());
        }
        List<AiChatMessageDO> messageList = chatMessageService.getChatMessageListByConversationId(conversationId);
        if (CollUtil.isEmpty(messageList)) {
            return success(Collections.emptyList());
        }

        // 拼接数据
        Map<Long, AiChatRoleDO> roleMap = chatRoleService.getChatRoleMap(convertSet(messageList, AiChatMessageDO::getRoleId));
        AdminUserRespDTO user = adminUserApi.getUser(getLoginUserId());
        return success(BeanUtils.toBean(messageList, AiChatMessageRespVO.class, respVO -> {
            MapUtils.findAndThen(roleMap, respVO.getRoleId(), role -> respVO.setRoleAvatar(role.getAvatar()));
            respVO.setUserAvatar(user.getAvatar());
        }));
    }

    @Operation(summary = "删除消息")
    @DeleteMapping("/delete")
    @Parameter(name = "id", required = true, description = "消息编号", example = "1024")
    public CommonResult<Boolean> deleteChatMessage(@RequestParam("id") Long id) {
        chatMessageService.deleteChatMessage(id, getLoginUserId());
        return success(true);
    }

    @Operation(summary = "删除指定会话的消息")
    @DeleteMapping("/delete-by-conversation-id")
    @Parameter(name = "conversationId", required = true, description = "会话编号", example = "1024")
    public CommonResult<Boolean> deleteChatMessageByConversationId(@RequestParam("conversationId") Long conversationId) {
        chatMessageService.deleteChatMessageByConversationId(conversationId, getLoginUserId());
        return success(true);
    }

}
