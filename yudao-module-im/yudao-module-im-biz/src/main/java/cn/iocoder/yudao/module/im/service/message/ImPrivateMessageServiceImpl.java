package cn.iocoder.yudao.module.im.service.message;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.privates.ImPrivateMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImPrivateMessageMapper;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import cn.iocoder.yudao.module.im.service.sensitiveword.ImSensitiveWordService;
import cn.iocoder.yudao.module.im.websocket.ImMessageReadMessage;
import cn.iocoder.yudao.module.im.websocket.ImMessageReceiptMessage;
import cn.iocoder.yudao.module.im.websocket.ImMessageRecallMessage;
import cn.iocoder.yudao.module.im.websocket.ImPrivateMessageSendMessage;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

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
    public List<ImPrivateMessageDO> pullPrivateMessages(Long userId, Long minId, Integer size) {
        if (size > MAX_PULL_SIZE) {
            throw exception(MESSAGE_PULL_SIZE_EXCEEDED);
        }
        return privateMessageMapper.selectListByMinId(userId, minId, size);
    }

    // DONE @芋艿：已 review，逻辑正确
    @Override
    public void readPrivateMessages(Long userId, Long friendId) {
        // 1. 批量更新消息状态为已读
        int updated = privateMessageMapper.updateStatusToRead(userId, friendId);
        if (updated == 0) {
            return; // 没有需要更新的消息
        }
        // 2. 发送 READ 事件给自己的其他终端（多端同步）
        ImMessageReadMessage readMessage = new ImMessageReadMessage()
                .setFriendId(friendId.toString())
                .setMessageScene("private");
        webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), userId,
                ImMessageReadMessage.TYPE, readMessage);

        // 3. 发送 RECEIPT 事件给对方（已读回执）
        ImMessageReceiptMessage receiptMessage = new ImMessageReceiptMessage()
                .setUserId(userId.toString())
                .setMessageScene("private");
        webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), friendId,
                ImMessageReceiptMessage.TYPE, receiptMessage);
    }

    // DONE @芋艿：已 review，逻辑正确
    @Override
    public void recallPrivateMessage(Long userId, Long messageId) {
        // 1.1 校验消息存在
        ImPrivateMessageDO message = privateMessageMapper.selectById(messageId);
        if (message == null) {
            throw exception(MESSAGE_NOT_EXISTS);
        }
        // 1.2 只能撤回自己发送的消息
        // DONE @AI：Notequals。结论：这里使用 !equals() 是正确的，ObjUtil.notEqual 也可以但无必要引入
        if (!message.getSenderId().equals(userId)) {
            throw exception(MESSAGE_RECALL_DENIED);
        }
        // 1.3 不能重复撤回
        if (ImMessageStatusEnum.RECALL.getStatus().equals(message.getStatus())) {
            throw exception(MESSAGE_ALREADY_RECALLED);
        }

        // 2. 更新消息状态为撤回
        privateMessageMapper.updateById(new ImPrivateMessageDO().setId(messageId)
                .setStatus(ImMessageStatusEnum.RECALL.getStatus()));

        // 3. 发送 RECALL 事件
        ImMessageRecallMessage recallMessage = new ImMessageRecallMessage()
                .setMessageId(messageId.toString())
                .setSenderId(userId.toString())
                .setMessageScene("private");
        // 3.1 通知接收方
        webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), message.getReceiverId(),
                ImMessageRecallMessage.TYPE, recallMessage);
        // 3.2 通知发送方自己的其他终端
        webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), userId,
                ImMessageRecallMessage.TYPE, recallMessage);
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

    private ImPrivateMessageServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
