package cn.iocoder.yudao.module.im.service.message;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.privates.ImPrivateMessageListReqVO;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.privates.ImPrivateMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImPrivateMessageMapper;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import cn.iocoder.yudao.module.im.service.sensitiveword.ImSensitiveWordService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImPrivateMessageDTO;
import cn.iocoder.yudao.module.im.service.websocket.dto.message.RecallMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.im.enums.ImCommonConstants.*;

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
    private ImWebSocketService imWebSocketService;

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
        if (!friendService.isFriend(senderId, reqVO.getReceiverId())) {
            throw exception(FRIEND_NOT_FRIEND);
        }
        // 1.3 文本消息敏感词过滤
        if (ImMessageTypeEnum.TEXT.getType().equals(reqVO.getType())) {
            sensitiveWordService.validateText(reqVO.getContent());
        }

        // 2. 构建并保存消息
        ImPrivateMessageDO message = BeanUtils.toBean(reqVO, ImPrivateMessageDO.class, m -> m
                .setSenderId(senderId).setStatus(ImMessageStatusEnum.UNREAD.getStatus()).setSendTime(LocalDateTime.now()));
        privateMessageMapper.insert(message);

        // 3. WebSocket 异步推送（接收方 + 发送方多端同步）
        ImPrivateMessageDTO websocketMessage = ImPrivateMessageDTO.ofSend(message);
        imWebSocketService.sendPrivateMessageAsync(message.getReceiverId(), websocketMessage);
        imWebSocketService.sendPrivateMessageAsync(message.getSenderId(), websocketMessage);
        return message;
    }

    @Override
    public List<ImPrivateMessageDO> pullPrivateMessageList(Long userId, Long minId, Integer size) {
        if (size > MESSAGE_MAX_PULL_SIZE) {
            throw exception(MESSAGE_PULL_SIZE_EXCEEDED, MESSAGE_MAX_PULL_SIZE);
        }
        // 0. 拉取时间窗：超过窗口的老消息不再通过离线通道推送
        LocalDateTime minSendTime = LocalDateTime.now().minusDays(MESSAGE_PRIVATE_PULL_MAX_DAYS);

        // 根据 minId 和 minSendTime 拉取消息，避免 minId 恰好被发出后才拉取，导致漏消息
        List<ImPrivateMessageDO> messages = privateMessageMapper.selectListByMinId(userId, minId, minSendTime, size);
        log.info("[pullPrivateMessageList][userId({}) minId({}) size({}) result({})]",
                userId, minId, size, messages.size());
        return messages;
    }

    // TODO DONE @AI：考虑到更稳妥，是不是前端传递 messageId？不然恰好发过来，然后读取了。【后续在优化】
    @Override
    public void readPrivateMessages(Long userId, Long receiverId, Long messageId) {
        Assert.notNull(messageId, "已读消息编号不能为空");
        // 1. 一步翻转：把 (receiverId → userId) 这条会话上、id <= messageId 的未读消息全部置为已读
        // 仅 UNREAD 行被命中，避免覆盖已撤回/已读的状态；select-then-update 拆成单条 SQL 后也消除了竞态窗口
        int updated = privateMessageMapper.updateBySenderIdAndReceiverIdAndIdLeAndStatus(
                receiverId, userId, messageId, ImMessageStatusEnum.UNREAD.getStatus(),
                new ImPrivateMessageDO().setStatus(ImMessageStatusEnum.READ.getStatus()));
        if (updated == 0) {
            return;
        }

        // 2. 异步发送 READ + RECEIPT 事件（已读位置以前端上报为准，与多端 / 对方 UI 显示一致）
        imWebSocketService.sendPrivateMessageAsync(userId,
                ImPrivateMessageDTO.ofRead(userId, receiverId, messageId));
        imWebSocketService.sendPrivateMessageAsync(receiverId,
                ImPrivateMessageDTO.ofReceipt(userId, receiverId, messageId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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

        // 3. 插入一条 RECALL 消息作为撤回信号（content 为 RecallMessage 序列化，前端据此找到原消息更新状态）
        RecallMessage recallContent = new RecallMessage().setMessageId(messageId);
        ImPrivateMessageDO recallMessage = new ImPrivateMessageDO().setClientMessageId(IdUtil.fastSimpleUUID())
                .setSenderId(userId).setReceiverId(message.getReceiverId())
                .setType(ImMessageTypeEnum.RECALL.getType()).setContent(JsonUtils.toJsonString(recallContent))
                .setStatus(ImMessageStatusEnum.UNREAD.getStatus()).setSendTime(LocalDateTime.now());
        privateMessageMapper.insert(recallMessage);

        // 4. 异步推送撤回信号（前端据此更新原消息状态 + 插入撤回提示）
        ImPrivateMessageDTO websocketMessage = ImPrivateMessageDTO.ofSend(recallMessage);
        imWebSocketService.sendPrivateMessageAsync(recallMessage.getReceiverId(), websocketMessage);
        imWebSocketService.sendPrivateMessageAsync(recallMessage.getSenderId(), websocketMessage);
        return recallMessage;
    }

    @Override
    public List<ImPrivateMessageDO> getPrivateMessageList(Long userId, ImPrivateMessageListReqVO reqVO) {
        return privateMessageMapper.selectHistoryList(userId, reqVO.getReceiverId(), reqVO.getMaxId(), reqVO.getLimit());
    }

    @Override
    public void sendTipPrivateMessage(Long senderId, Long receiverId, String content) {
        // 1. 插入 TIP_TEXT 消息
        ImPrivateMessageDO tipMessage = new ImPrivateMessageDO()
                .setClientMessageId(IdUtil.fastSimpleUUID()).setSenderId(senderId).setReceiverId(receiverId)
                .setType(ImMessageTypeEnum.TIP_TEXT.getType()).setContent(content)
                .setStatus(ImMessageStatusEnum.UNREAD.getStatus()).setSendTime(LocalDateTime.now());
        privateMessageMapper.insert(tipMessage);

        // 2. 推送给双方
        ImPrivateMessageDTO dto = ImPrivateMessageDTO.ofSend(tipMessage);
        imWebSocketService.sendPrivateMessageAsync(receiverId, dto);
        imWebSocketService.sendPrivateMessageAsync(senderId, dto);
    }

}
