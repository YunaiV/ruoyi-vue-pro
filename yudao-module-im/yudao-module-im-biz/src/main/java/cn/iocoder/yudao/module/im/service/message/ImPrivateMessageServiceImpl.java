package cn.iocoder.yudao.module.im.service.message;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.privates.ImPrivateMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.content.RecallMessage;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImPrivateMessageMapper;
import cn.iocoder.yudao.module.im.enums.message.ImMessageSceneEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import cn.iocoder.yudao.module.im.service.sensitiveword.ImSensitiveWordService;
import cn.iocoder.yudao.module.im.websocket.ImMessageReadMessage;
import cn.iocoder.yudao.module.im.websocket.ImMessageReceiptMessage;
import cn.iocoder.yudao.module.im.websocket.ImPrivateMessageSendMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.im.enums.ImCommonConstants.MESSAGE_MAX_PULL_SIZE;
import static cn.iocoder.yudao.module.im.enums.ImCommonConstants.MESSAGE_RECALL_TIMEOUT_MINUTES;

/**
 * IM 私聊消息 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class ImPrivateMessageServiceImpl implements ImPrivateMessageService {

    @Resource
    private ImPrivateMessageMapper privateMessageMapper;

    @Resource
    private ImFriendService friendService;
    @Resource
    private ImSensitiveWordService sensitiveWordService;

    @Resource
    private WebSocketMessageSender webSocketMessageSender;

    @Override
    public ImPrivateMessageDO sendPrivateMessage(Long senderId, ImPrivateMessageSendReqVO reqVO) {
        // 1.1 幂等校验：根据 senderId + clientMessageId 查重
        ImPrivateMessageDO existing = privateMessageMapper.selectBySenderIdAndClientMessageId(
                senderId, reqVO.getClientMessageId());
        if (existing != null) {
            log.info("[sendPrivateMessage][幂等命中 senderId({}) clientMessageId({}) 已存在消息({})]",
                    senderId, reqVO.getClientMessageId(), existing.getId());
            return existing;
        }
        // 1.2 好友校验
        friendService.validateFriendExists(senderId, reqVO.getReceiverId());
        // 1.3 文本消息敏感词过滤
        if (ImMessageTypeEnum.TEXT.getType().equals(reqVO.getType())) {
            sensitiveWordService.validateText(reqVO.getContent());
        }

        // 2. 构建并保存消息
        ImPrivateMessageDO message = BeanUtils.toBean(reqVO, ImPrivateMessageDO.class, m -> m
                .setSenderId(senderId).setStatus(ImMessageStatusEnum.UNREAD.getStatus()).setSendTime(LocalDateTime.now()));
        privateMessageMapper.insert(message);

        // 3. WebSocket 推送给接收方（异步）
        getSelf().sendPrivateMessageEvent(message);
        return message;
    }

    @Override
    public List<ImPrivateMessageDO> pullPrivateMessageList(Long userId, Long minId, Integer size) {
        if (size > MESSAGE_MAX_PULL_SIZE) {
            throw exception(MESSAGE_PULL_SIZE_EXCEEDED);
        }
        List<ImPrivateMessageDO> messages = privateMessageMapper.selectListByMinId(userId, minId, size);
        log.info("[pullPrivateMessageList][userId({}) minId({}) size({}) result({})]",
                userId, minId, size, messages.size());
        return messages;
    }

    @Override
    public void readPrivateMessages(Long userId, Long receiverId) {
        // 1.1 查询未读消息列表
        List<ImPrivateMessageDO> unreadMessages = privateMessageMapper.selectListBySenderIdAndReceiverIdAndStatus(
                receiverId, userId, ImMessageStatusEnum.UNREAD.getStatus());
        if (CollUtil.isEmpty(unreadMessages)) {
            return;
        }
        // 1.2 根据 id in + status 条件更新，避免并发问题
        List<Long> messageIds = convertList(unreadMessages, ImPrivateMessageDO::getId);
        privateMessageMapper.updateByIdsAndStatus(messageIds, ImMessageStatusEnum.UNREAD.getStatus(),
                new ImPrivateMessageDO().setStatus(ImMessageStatusEnum.READ.getStatus()));

        // 2. 异步发送 READ + RECEIPT 事件
        getSelf().readPrivateMessageEvent(userId, receiverId);
    }

    @Override
    public ImPrivateMessageDO recallPrivateMessage(Long userId, Long messageId) {
        // 1.1 校验消息存在
        ImPrivateMessageDO message = privateMessageMapper.selectById(messageId);
        if (message == null) {
            throw exception(MESSAGE_NOT_EXISTS);
        }
        // 1.2 只能撤回自己发送的消息
        if (ObjUtil.notEqual(message.getSenderId(), userId)) {
            throw exception(MESSAGE_RECALL_DENIED);
        }
        // 1.3 不能重复撤回
        if (ImMessageStatusEnum.RECALL.getStatus().equals(message.getStatus())) {
            throw exception(MESSAGE_ALREADY_RECALLED);
        }
        // 1.4 只允许撤回限定时间内的消息
        if (message.getSendTime().plusMinutes(MESSAGE_RECALL_TIMEOUT_MINUTES).isBefore(LocalDateTime.now())) {
            throw exception(MESSAGE_RECALL_TIMEOUT, MESSAGE_RECALL_TIMEOUT_MINUTES);
        }

        // 2. 更新原消息状态为撤回
        privateMessageMapper.updateById(new ImPrivateMessageDO().setId(messageId)
                .setStatus(ImMessageStatusEnum.RECALL.getStatus()));

        // 3. 插入一条 TIP_TEXT 消息作为撤回提示
        RecallMessage recallContent = new RecallMessage()
                .setMessageId(messageId);
        ImPrivateMessageDO tipMessage = new ImPrivateMessageDO().setClientMessageId(IdUtil.fastSimpleUUID())
                .setSenderId(userId).setReceiverId(message.getReceiverId())
                .setType(ImMessageTypeEnum.TIP_TEXT.getType()).setContent(JsonUtils.toJsonString(recallContent))
                .setStatus(ImMessageStatusEnum.UNREAD.getStatus()).setSendTime(LocalDateTime.now());
        privateMessageMapper.insert(tipMessage);

        // 4. 异步推送撤回提示消息（前端据此更新原消息状态 + 插入撤回提示）
        getSelf().sendPrivateMessageEvent(tipMessage);
        return tipMessage;
    }

    /**
     * 发送私聊消息 WebSocket 事件
     */
    @Async
    public void sendPrivateMessageEvent(ImPrivateMessageDO message) {
        ImPrivateMessageSendMessage websocketMessage = BeanUtils.toBean(message, ImPrivateMessageSendMessage.class);
        // 推送给接收方
        webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), message.getReceiverId(),
                ImPrivateMessageSendMessage.TYPE, websocketMessage);

        // 推送给发送方自己的其他终端（多端同步）
        webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), message.getSenderId(),
                ImPrivateMessageSendMessage.TYPE, websocketMessage);
    }

    /**
     * 发送已读 + 已读回执 WebSocket 事件
     *
     * @param userId     当前用户编号
     * @param receiverId 对方用户编号
     */
    @Async
    public void readPrivateMessageEvent(Long userId, Long receiverId) {
        // 1. 发送 READ 事件给自己的其他终端（多端同步）
        ImMessageReadMessage readMessage = new ImMessageReadMessage()
                .setReceiverId(receiverId).setScene(ImMessageSceneEnum.PRIVATE.getScene());
        webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), userId,
                ImMessageReadMessage.TYPE, readMessage);

        // 2. 发送 RECEIPT 事件给对方（已读回执）
        ImMessageReceiptMessage receiptMessage = new ImMessageReceiptMessage()
                .setReceiverId(userId).setScene(ImMessageSceneEnum.PRIVATE.getScene());
        webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), receiverId,
                ImMessageReceiptMessage.TYPE, receiptMessage);
    }

    private ImPrivateMessageServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
