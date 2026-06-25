package cn.iocoder.yudao.module.im.service.message;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.privates.ImPrivateMessageManagerPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.privates.ImPrivateMessageListReqVO;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.privates.ImPrivateMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImPrivateMessageMapper;
import cn.iocoder.yudao.module.im.enums.ImConversationTypeEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageReceiptStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.enums.ImContentTypeEnum;
import cn.iocoder.yudao.module.im.framework.config.ImProperties;
import cn.iocoder.yudao.module.im.service.conversation.ImConversationReadService;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import cn.iocoder.yudao.module.im.service.message.dto.ImPrivateMessageSendDTO;
import cn.iocoder.yudao.module.im.service.sensitiveword.ImSensitiveWordService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.notification.message.ImMessageReadNotification;
import cn.iocoder.yudao.module.im.service.websocket.notification.message.ImMessageReceiptNotification;
import cn.iocoder.yudao.module.im.service.websocket.notification.message.ImPrivateMessageNotification;
import cn.iocoder.yudao.module.im.dal.dataobject.message.content.QuoteMessage;
import cn.iocoder.yudao.module.im.dal.dataobject.message.content.RecallMessage;
import cn.iocoder.yudao.module.im.util.ImMessageUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Resource
    private ImPrivateMessageMapper privateMessageMapper;

    @Resource
    private ImFriendService friendService;
    @Resource
    private ImSensitiveWordService sensitiveWordService;
    @Resource
    private ImConversationReadService conversationReadService;

    @Resource
    private ImWebSocketService imWebSocketService;

    @Resource
    private ImProperties imProperties;

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
        // 1.2 消息内容校验
        ImMessageUtils.validateUserMessageContent(reqVO.getType(), reqVO.getContent());
        // 1.3 好友校验
        friendService.validateFriend(senderId, reqVO.getReceiverId());
        // 1.4 文本消息敏感词过滤
        if (ImContentTypeEnum.TEXT.getType().equals(reqVO.getType())) {
            sensitiveWordService.validateText(reqVO.getContent());
        }

        // 2.1 引用 quote 消息规范化
        reqVO.setContent(normalizeQuoteContent(reqVO, senderId));
        // 2.2 构建并保存消息；唯一键冲突时回查已存在消息返回
        // 用户私聊消息默认需要回执（receipt 不传按 true）；系统通知走 DTO 通道，默认不回执
        Boolean receipt = reqVO.getReceipt() != null ? reqVO.getReceipt() : Boolean.TRUE;
        ImPrivateMessageDO message = BeanUtils.toBean(reqVO, ImPrivateMessageDO.class, m -> m
                .setSenderId(senderId).setStatus(ImMessageStatusEnum.NORMAL.getStatus())
                .setReceiptStatus(resolveReceiptStatus(receipt)).setSendTime(LocalDateTime.now()));
        try {
            privateMessageMapper.insert(message);
        } catch (DuplicateKeyException e) {
            log.warn("[sendPrivateMessage][senderId({}) clientMessageId({}) 并发插入冲突，回查返回]",
                    senderId, reqVO.getClientMessageId());
            return privateMessageMapper.selectBySenderIdAndClientMessageId(senderId, reqVO.getClientMessageId());
        }

        // 3. WebSocket 异步推送：接收方 + 发送方多端同步
        ImPrivateMessageNotification notification = ImPrivateMessageNotification.ofSend(message);
        imWebSocketService.sendNotificationAsync(message.getReceiverId(), ImConversationTypeEnum.PRIVATE.getType(),
                notification.getType(), notification);
        imWebSocketService.sendNotificationAsync(senderId, ImConversationTypeEnum.PRIVATE.getType(),
                notification.getType(), notification);
        return message;
    }

    @Override
    public ImPrivateMessageDO sendPrivateMessage(Long senderId, ImPrivateMessageSendDTO dto) {
        // 1.1 content 序列化：null / String 透传，POJO 走 JSON
        Object payload = dto.getContent();
        String contentString = payload == null || payload instanceof String
                ? (String) payload
                : JsonUtils.toJsonString(payload);
        // 1.2 构建消息
        ImPrivateMessageDO message = new ImPrivateMessageDO().setClientMessageId(IdUtil.fastSimpleUUID())
                .setSenderId(senderId).setReceiverId(dto.getReceiverId())
                .setType(dto.getType()).setContent(contentString)
                .setStatus(ImMessageStatusEnum.NORMAL.getStatus())
                .setReceiptStatus(resolveReceiptStatus(dto.getReceipt())).setSendTime(LocalDateTime.now());
        // 1.3 决定是否持久化：dto.persistent 优先；为 null 时按 type 默认
        boolean persistent = dto.getPersistent() != null
                ? dto.getPersistent()
                : ImContentTypeEnum.validate(dto.getType()).isPersistent();
        if (persistent) {
            privateMessageMapper.insert(message);
        }

        // 2. WebSocket 异步推送：双向（默认）；单边语义（persistent=false）下仅推 sender 多端，对方不感知
        ImPrivateMessageNotification notification = ImPrivateMessageNotification.ofSend(message);
        if (persistent) {
            imWebSocketService.sendNotificationAsync(dto.getReceiverId(), ImConversationTypeEnum.PRIVATE.getType(),
                    notification.getType(), notification);
        }
        imWebSocketService.sendNotificationAsync(senderId, ImConversationTypeEnum.PRIVATE.getType(),
                notification.getType(), notification);
        return message;
    }

    /**
     * 计算私聊消息回执 status：私聊已读关闭时强制 NO_RECEIPT，忽略发送方传入的 receipt（receipt 为 null 等价 false）
     */
    private Integer resolveReceiptStatus(Boolean receipt) {
        if (BooleanUtil.isFalse(imProperties.getMessage().isPrivateReadEnabled())) {
            return ImMessageReceiptStatusEnum.NO_RECEIPT.getStatus();
        }
        return BooleanUtil.isTrue(receipt)
                ? ImMessageReceiptStatusEnum.PENDING.getStatus()
                : ImMessageReceiptStatusEnum.NO_RECEIPT.getStatus();
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
        int recallTimeoutMinutes = imProperties.getMessage().getRecallTimeoutMinutes();
        if (message.getSendTime().plusMinutes(recallTimeoutMinutes).isBefore(LocalDateTime.now())) {
            throw exception(MESSAGE_RECALL_TIMEOUT, recallTimeoutMinutes);
        }

        // 2. 更新原消息状态为撤回
        privateMessageMapper.updateById(new ImPrivateMessageDO().setId(messageId)
                .setStatus(ImMessageStatusEnum.RECALL.getStatus()));

        // 3. 发送撤回事件
        return sendPrivateMessage(userId, new ImPrivateMessageSendDTO().setReceiverId(message.getReceiverId())
                .setType(ImContentTypeEnum.RECALL.getType()).setContent(new RecallMessage().setMessageId(messageId)));
    }

    /**
     * 私聊引用消息规范化
     *
     * @param reqVO 发送请求
     * @param senderId 发送人编号
     * @return 规范化后的 content
     */
    private String normalizeQuoteContent(ImPrivateMessageSendReqVO reqVO, Long senderId) {
        // 解析客户端 content 里的 quote.messageId
        Long quoteMessageId = ImMessageUtils.parseQuoteMessageId(reqVO.getContent());

        // 情况一：没有 quoteMessageId，直接 remove 掉 content 里可能伪造的 quote 字段
        if (quoteMessageId == null) {
            return ImMessageUtils.removeQuote(reqVO.getContent());
        }

        // 情况二：有 quoteMessageId，加载原消息并校验
        ImPrivateMessageDO original = privateMessageMapper.selectById(quoteMessageId);
        if (original == null
                || ImMessageStatusEnum.RECALL.getStatus().equals(original.getStatus())) {
            throw exception(MESSAGE_QUOTE_INVALID);
        }
        // 校验是同对话
        boolean sameConversation = (ObjUtil.equal(original.getSenderId(), senderId) // 发送人是当前用户，接收人是对方
                && ObjUtil.equal(original.getReceiverId(), reqVO.getReceiverId()))
                || (ObjUtil.equal(original.getSenderId(), reqVO.getReceiverId()) // 发送人是对方，接收人是当前用户
                        && ObjUtil.equal(original.getReceiverId(), senderId));
        if (!sameConversation) {
            throw exception(MESSAGE_QUOTE_INVALID);
        }
        // 构建 quote 对象并注入 content
        QuoteMessage quote = ImMessageUtils.buildQuote(original.getId(),
                original.getSenderId(), original.getType(), original.getContent());
        return ImMessageUtils.appendQuote(reqVO.getContent(), quote);
    }

    @Override
    public List<ImPrivateMessageDO> pullPrivateMessageList(Long userId, Long minId, Integer size) {
        int maxPullSize = imProperties.getMessage().getMaxPullSize();
        if (size > maxPullSize) {
            throw exception(MESSAGE_PULL_SIZE_EXCEEDED, maxPullSize);
        }
        // 0. 拉取时间窗；超过窗口的老消息不再通过离线通道推送
        LocalDateTime minSendTime = LocalDateTime.now().minusDays(imProperties.getMessage().getPrivatePullMaxDays());

        // 根据 minId 和 minSendTime 拉取消息，避免 minId 恰好被发出后才拉取，导致漏消息
        List<ImPrivateMessageDO> messages = privateMessageMapper.selectListByMinId(userId, minId, minSendTime, size);
        log.info("[pullPrivateMessageList][userId({}) minId({}) size({}) result({})]",
                userId, minId, size, messages.size());
        return messages;
    }

    @Override
    public void readPrivateMessages(Long userId, Long receiverId, Long messageId) {
        // 1. 全局开关校验
        if (BooleanUtil.isFalse(imProperties.getMessage().isPrivateReadEnabled())) {
            throw exception(MESSAGE_PRIVATE_READ_DISABLED);
        }
        Assert.notNull(messageId, "已读消息编号不能为空");

        // 2. 回执置 DONE：把 (receiverId → userId) 上 id <= messageId、待回执(PENDING) 的消息标记为已完成
        // status 不再表达已读（保持 NORMAL）；只翻 PENDING 行，避免覆盖 NO_RECEIPT / 已 DONE
        privateMessageMapper.updateBySenderIdAndReceiverIdAndIdLeAndReceiptStatus(
                receiverId, userId, messageId, ImMessageReceiptStatusEnum.PENDING.getStatus(),
                new ImPrivateMessageDO().setReceiptStatus(ImMessageReceiptStatusEnum.DONE.getStatus()));

        // 3. 同步写 im_conversation_read（读位置唯一权威，单调递增）；读位置前进才下发事件
        boolean advanced = conversationReadService.updateConversationReadPosition(
                userId, ImConversationTypeEnum.PRIVATE.getType(), receiverId, messageId);
        if (!advanced) {
            return;
        }

        // 4. 异步发送 READ + RECEIPT 事件（已读位置以前端上报为准，与多端 / 对方 UI 显示一致）
        imWebSocketService.sendNotificationAsync(userId, ImConversationTypeEnum.PRIVATE.getType(),
                ImContentTypeEnum.READ.getType(), ImMessageReadNotification.ofPrivate(userId, receiverId, messageId));
        imWebSocketService.sendNotificationAsync(receiverId, ImConversationTypeEnum.PRIVATE.getType(),
                ImContentTypeEnum.RECEIPT.getType(),
                ImMessageReceiptNotification.ofPrivate(userId, receiverId, messageId));
    }

    @Override
    public Long getMaxReadMessageId(Long userId, Long peerId) {
        if (BooleanUtil.isFalse(imProperties.getMessage().isPrivateReadEnabled())) {
            throw exception(MESSAGE_PRIVATE_READ_DISABLED);
        }
        // 对端 peer 在「与 userId 的会话」里的读位置 = peer 把 userId 发的消息读到哪
        return conversationReadService.getConversationReadMessageId(
                peerId, ImConversationTypeEnum.PRIVATE.getType(), userId);
    }

    @Override
    public List<ImPrivateMessageDO> getPrivateMessageList(Long userId, ImPrivateMessageListReqVO reqVO) {
        return privateMessageMapper.selectHistoryList(userId, reqVO.getReceiverId(), reqVO.getMaxId(), reqVO.getLimit());
    }

    // ==================== 管理后台 ====================

    @Override
    public PageResult<ImPrivateMessageDO> getPrivateMessagePage(ImPrivateMessageManagerPageReqVO reqVO) {
        return privateMessageMapper.selectPage(reqVO);
    }

    @Override
    public ImPrivateMessageDO getPrivateMessage(Long id) {
        return privateMessageMapper.selectById(id);
    }

}
