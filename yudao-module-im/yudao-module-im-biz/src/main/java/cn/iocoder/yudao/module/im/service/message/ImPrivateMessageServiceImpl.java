package cn.iocoder.yudao.module.im.service.message;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.privates.ImPrivateMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImPrivateMessageMapper;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.enums.message.ImWebSocketTypeConstants;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import cn.iocoder.yudao.module.im.service.sensitiveword.ImSensitiveWordService;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;

/**
 * IM 私聊消息 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class ImPrivateMessageServiceImpl implements ImPrivateMessageService {

    /**
     * pull 最大拉取数量
     */
    private static final int MAX_PULL_SIZE = 1000;

    @Resource
    private ImPrivateMessageMapper imPrivateMessageMapper;

    @Resource
    private ImFriendService imFriendService;
    @Resource
    private ImSensitiveWordService imSensitiveWordService;

    // TODO @AI：这个类貌似路径不对？！
    @Resource
    private WebSocketMessageSender webSocketMessageSender;

    @Override
    public ImPrivateMessageDO sendMessage(Long senderId, ImPrivateMessageSendReqVO reqVO) {
        // 1.1 幂等校验：根据 senderId + clientMessageId 查重
        ImPrivateMessageDO existing = imPrivateMessageMapper.selectBySenderIdAndClientMessageId(
                senderId, reqVO.getClientMessageId());
        if (existing != null) {
            return existing;
        }
        // 1.2 好友校验
        imFriendService.validateIsFriend(senderId, reqVO.getReceiverId());
        // 1.3 文本消息敏感词过滤
        if (ImMessageTypeEnum.TEXT.getType().equals(reqVO.getType())) {
            imSensitiveWordService.validateText(reqVO.getContent());
        }

        // 2. 构建并保存消息
        // TODO @AI：相同类型的字段：放在一行，避免过长；
        ImPrivateMessageDO message = ImPrivateMessageDO.builder()
                .clientMessageId(reqVO.getClientMessageId())
                .senderId(senderId)
                .receiverId(reqVO.getReceiverId())
                .type(reqVO.getType())
                .content(reqVO.getContent())
                .status(ImMessageStatusEnum.UNREAD.getStatus())
                .sendTime(LocalDateTime.now())
                .build();
        imPrivateMessageMapper.insert(message);

        // 3. WebSocket 推送给接收方
        sendPrivateMessageEvent(message);
        return message;
    }

    @Override
    public List<ImPrivateMessageDO> pullMessages(Long userId, Long minId, Integer size) {
        // pull 分页保护
        // TODO @AI：limit 这个参考校验，放在 controller 里做；
        if (size > MAX_PULL_SIZE) {
            throw exception(MESSAGE_PULL_SIZE_EXCEEDED);
        }
        // TODO @AI：可能要加一个，最大返回几个月的？！
        return imPrivateMessageMapper.selectListByMinId(userId, minId, size);
    }

    // TODO @芋艿：这里需要多 review 下；
    @Override
    public void readMessages(Long userId, Long friendId) {
        // 1. 批量更新消息状态为已读
        int updated = imPrivateMessageMapper.updateStatusToRead(userId, friendId);
        if (updated == 0) {
            return; // 没有需要更新的消息
        }
        // 2. 发送 READ 事件给自己的其他终端（多端同步）
        Map<String, Object> readEvent = new HashMap<>();
        // TODO @AI：需要整理下，相关的 websocket DTO；可维护性变强！
        readEvent.put("friendId", friendId.toString());
        readEvent.put("messageScene", "private");
        webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), userId,
                ImWebSocketTypeConstants.READ, readEvent);

        // 3. 发送 RECEIPT 事件给对方（已读回执）
        Map<String, Object> receiptEvent = new HashMap<>();
        // TODO @AI：需要整理下，相关的 websocket DTO；可维护性变强！
        receiptEvent.put("userId", userId.toString());
        receiptEvent.put("messageScene", "private");
        webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), friendId,
                ImWebSocketTypeConstants.RECEIPT, receiptEvent);
    }

    // TODO @芋艿：这里需要多 review 下；
    @Override
    public void recallMessage(Long userId, Long messageId) {
        // 1.1 校验消息存在
        ImPrivateMessageDO message = imPrivateMessageMapper.selectById(messageId);
        if (message == null) {
            throw exception(MESSAGE_NOT_EXISTS);
        }
        // 1.2 只能撤回自己发送的消息
        // TODO @AI：Notequals；
        if (!message.getSenderId().equals(userId)) {
            throw exception(MESSAGE_RECALL_DENIED);
        }
        // 1.3 不能重复撤回
        if (ImMessageStatusEnum.RECALL.getStatus().equals(message.getStatus())) {
            throw exception(MESSAGE_ALREADY_RECALLED);
        }

        // 2. 更新消息状态为撤回
        imPrivateMessageMapper.updateById(new ImPrivateMessageDO().setId(messageId)
                .setStatus(ImMessageStatusEnum.RECALL.getStatus()));

        // 3.1 发送 RECALL 事件给接收方
        // TODO @AI：需要整理下，相关的 websocket DTO；可维护性变强！
        Map<String, Object> recallEvent = new HashMap<>();
        recallEvent.put("messageId", messageId.toString());
        recallEvent.put("messageScene", "private");
        recallEvent.put("senderId", userId.toString());
        webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), message.getReceiverId(),
                ImWebSocketTypeConstants.RECALL, recallEvent);
        // 3.2 也通知发送方自己的其他终端
        // TODO @AI：需要整理下，相关的 websocket DTO；可维护性变强！
        webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), userId,
                ImWebSocketTypeConstants.RECALL, recallEvent);
    }

    /**
     * 发送私聊消息 WebSocket 事件
     */
    private void sendPrivateMessageEvent(ImPrivateMessageDO message) {
        Map<String, Object> event = new HashMap<>();
        // TODO @AI：需要整理下，相关的 websocket DTO；可维护性变强！
        event.put("id", message.getId().toString());
        event.put("clientMessageId", message.getClientMessageId());
        event.put("senderId", message.getSenderId().toString());
        event.put("receiverId", message.getReceiverId().toString());
        event.put("type", message.getType());
        event.put("content", message.getContent());
        event.put("status", message.getStatus());
        event.put("sendTime", message.getSendTime().toString());
        event.put("messageScene", "private");
        // 推送给接收方
        webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), message.getReceiverId(),
                ImWebSocketTypeConstants.PRIVATE_MESSAGE, event);
        // 推送给发送方自己的其他终端（多端同步）
        webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), message.getSenderId(),
                ImWebSocketTypeConstants.PRIVATE_MESSAGE, event);
    }

}
