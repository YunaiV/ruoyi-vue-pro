package cn.iocoder.yudao.module.im.service.message;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.group.ImGroupMessageManagerPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.group.ImGroupMessageListReqVO;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.group.ImGroupMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImGroupMessageMapper;
import cn.iocoder.yudao.module.im.enums.ImConversationTypeEnum;
import cn.iocoder.yudao.module.im.enums.group.ImGroupMemberRoleEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageReceiptStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.enums.ImContentTypeEnum;
import cn.iocoder.yudao.module.im.framework.config.ImProperties;
import cn.iocoder.yudao.module.im.service.conversation.ImConversationReadService;
import cn.iocoder.yudao.module.im.service.group.ImGroupMemberService;
import cn.iocoder.yudao.module.im.service.group.ImGroupService;
import cn.iocoder.yudao.module.im.service.message.dto.ImGroupMessageSendDTO;
import cn.iocoder.yudao.module.im.service.sensitiveword.ImSensitiveWordService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.notification.message.ImGroupMessageNotification;
import cn.iocoder.yudao.module.im.service.websocket.notification.message.ImMessageReadNotification;
import cn.iocoder.yudao.module.im.service.websocket.notification.message.ImMessageReceiptNotification;
import cn.iocoder.yudao.module.im.dal.dataobject.message.content.QuoteMessage;
import cn.iocoder.yudao.module.im.dal.dataobject.message.content.RecallMessage;
import cn.iocoder.yudao.module.im.util.ImMessageUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;

/**
 * IM 群聊消息 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class ImGroupMessageServiceImpl implements ImGroupMessageService {

    @Resource
    private ImGroupMessageMapper groupMessageMapper;

    @Resource
    private ImGroupService groupService;
    @Resource
    private ImGroupMemberService groupMemberService;
    @Resource
    private ImSensitiveWordService sensitiveWordService;
    @Resource
    private ImConversationReadService conversationReadService;

    @Resource
    private ImWebSocketService imWebSocketService;

    @Resource
    private ImProperties imProperties;

    @Override
    public ImGroupMessageDO sendGroupMessage(Long senderId, ImGroupMessageSendReqVO reqVO) {
        // 1.1 幂等校验：根据 senderId + clientMessageId 查重
        ImGroupMessageDO existing = groupMessageMapper.selectBySenderIdAndClientMessageId(
                senderId, reqVO.getClientMessageId());
        if (existing != null) {
            log.info("[sendGroupMessage][幂等命中 senderId({}) clientMessageId({}) 已存在消息({})]",
                    senderId, reqVO.getClientMessageId(), existing.getId());
            return existing;
        }
        // 1.2 消息内容校验
        ImMessageUtils.validateUserMessageContent(reqVO.getType(), reqVO.getContent());
        // 1.3 校验群存在、发送人仍在群中
        ImGroupDO group = groupService.validateGroupExists(reqVO.getGroupId());
        ImGroupMemberDO senderMember = groupMemberService.validateMemberInGroup(reqVO.getGroupId(), senderId);
        // 1.4 禁言校验
        validateMuteStatus(group, senderMember);
        // 1.5 文本消息敏感词过滤
        if (ImContentTypeEnum.TEXT.getType().equals(reqVO.getType())) {
            sensitiveWordService.validateText(reqVO.getContent());
        }

        // 2.1 固化发送当时可见成员快照：用户发送均为全员广播；getReceiverUserIds 兜底纳入发送者，钉死发送者必可见
        List<Long> memberUserIds = groupMemberService.getActiveGroupMemberUserIdsByGroupId(reqVO.getGroupId());
        Set<Long> receiverUserIds = getReceiverUserIds(null, senderId, memberUserIds);
        // 2.2 引用 quote 消息规范化（含可见性子集校验，防止定向消息内容被广播引用泄漏）
        reqVO.setContent(normalizeQuoteContent(reqVO, senderMember, receiverUserIds));
        // 2.3 构建并保存消息；唯一键冲突时回查已存在消息返回
        ImGroupMessageDO message = BeanUtils.toBean(reqVO, ImGroupMessageDO.class, m -> m
                .setSenderId(senderId).setStatus(ImMessageStatusEnum.NORMAL.getStatus()).setSendTime(LocalDateTime.now())
                .setReceiverUserIds(new ArrayList<>(receiverUserIds))
                .setReceiptStatus(resolveReceiptStatus(reqVO.getReceipt())));
        try {
            groupMessageMapper.insert(message);
        } catch (DuplicateKeyException e) {
            log.warn("[sendGroupMessage][senderId({}) clientMessageId({}) 并发插入冲突，回查返回]",
                    senderId, reqVO.getClientMessageId());
            return groupMessageMapper.selectBySenderIdAndClientMessageId(senderId, reqVO.getClientMessageId());
        }

        // 3. WebSocket 异步推送（快照内可见成员 + 发送方多端同步）
        ImGroupMessageNotification notification = ImGroupMessageNotification.ofSend(message);
        imWebSocketService.sendNotificationAsync(receiverUserIds, ImConversationTypeEnum.GROUP.getType(),
                notification.getType(), notification);
        return message;
    }

    @Override
    public ImGroupMessageDO sendGroupMessage(Long senderId, ImGroupMessageSendDTO dto) {
        List<Long> memberUserIds = groupMemberService.getActiveGroupMemberUserIdsByGroupId(dto.getGroupId());
        Set<Long> receiverUserIds = getReceiverUserIds(dto.getReceiverUserIds(), senderId, memberUserIds);
        return sendGroupMessage(senderId, receiverUserIds, dto);
    }

    @Override
    public ImGroupMessageDO sendGroupMessage(Long senderId, Collection<Long> receiverUserIds, ImGroupMessageSendDTO dto) {
        // 1.1 content 序列化：null / String 透传，POJO 走 JSON
        Object payload = dto.getContent();
        String contentString = payload == null || payload instanceof String
                ? (String) payload
                : JsonUtils.toJsonString(payload);
        // 1.2 构建并保存消息
        ImGroupMessageDO message = new ImGroupMessageDO().setClientMessageId(IdUtil.fastSimpleUUID())
                .setSenderId(senderId).setGroupId(dto.getGroupId())
                .setType(dto.getType()).setContent(contentString)
                .setStatus(ImMessageStatusEnum.NORMAL.getStatus()).setSendTime(LocalDateTime.now())
                .setAtUserIds(dto.getAtUserIds()).setReceiverUserIds(new ArrayList<>(receiverUserIds))
                .setReceiptStatus(resolveReceiptStatus(dto.getReceipt()));
        // 1.3 按 type.persistent 决定是否入库
        if (ImContentTypeEnum.validate(dto.getType()).isPersistent()) {
            groupMessageMapper.insert(message);
        }

        // 2. WebSocket 异步推送
        ImGroupMessageNotification notification = ImGroupMessageNotification.ofSend(message);
        imWebSocketService.sendNotificationAsync(receiverUserIds, ImConversationTypeEnum.GROUP.getType(),
                notification.getType(), notification);
        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImGroupMessageDO recallGroupMessage(Long userId, Long messageId) {
        // 1.1 校验消息存在
        ImGroupMessageDO message = validateGroupMessageExists(messageId);
        // 1.2 校验撤回人仍在群中，并取角色用于权限判断
        ImGroupMemberDO operator = groupMemberService.validateMemberInGroup(message.getGroupId(), userId);
        boolean isOwnerOrAdmin = ImGroupMemberRoleEnum.isOwnerOrAdmin(operator.getRole());
        // 1.3 普通成员只能撤回自己发的消息；群主 / 管理员可撤回他人违规消息
        if (ObjUtil.notEqual(message.getSenderId(), userId) && !isOwnerOrAdmin) {
            throw exception(MESSAGE_RECALL_DENIED);
        }
        // 1.4 不能重复撤回
        if (ImMessageStatusEnum.RECALL.getStatus().equals(message.getStatus())) {
            throw exception(MESSAGE_ALREADY_RECALLED);
        }
        // 1.5 撤回时间窗仅约束撤回自己的消息；群主 / 管理员治理他人违规消息不受时间限制
        if (ObjUtil.equal(message.getSenderId(), userId)) {
            int recallTimeoutMinutes = imProperties.getMessage().getRecallTimeoutMinutes();
            if (message.getSendTime().plusMinutes(recallTimeoutMinutes).isBefore(LocalDateTime.now())) {
                throw exception(MESSAGE_RECALL_TIMEOUT, recallTimeoutMinutes);
            }
        }

        // 2. 更新原消息状态为撤回
        groupMessageMapper.updateById(new ImGroupMessageDO().setId(messageId)
                .setStatus(ImMessageStatusEnum.RECALL.getStatus()));

        // 3. 发送撤回事件
        return sendGroupMessage(userId, new ImGroupMessageSendDTO().setGroupId(message.getGroupId())
                .setType(ImContentTypeEnum.RECALL.getType())
                .setReceiverUserIds(message.getReceiverUserIds())
                .setContent(new RecallMessage().setMessageId(messageId)));
    }

    @Override
    public List<ImGroupMessageDO> pullGroupMessageList(Long userId, Long minId, Integer size) {
        int maxPullSize = imProperties.getMessage().getMaxPullSize();
        if (size > maxPullSize) {
            throw exception(MESSAGE_PULL_SIZE_EXCEEDED, maxPullSize);
        }

        // 0. 拉取时间窗；超过窗口的老消息不再通过离线通道推送
        LocalDateTime minSendTime = LocalDateTime.now().minusDays(imProperties.getMessage().getGroupPullMaxDays());

        // 1. 候选群 = 用户曾经加入的所有群（含退群）；可见性与时间窗都交给 SQL，退群群最多贡献 0 条
        Set<Long> groupIds = convertSet(groupMemberService.getGroupMemberListByUserId(userId),
                ImGroupMemberDO::getGroupId);
        if (CollUtil.isEmpty(groupIds)) {
            return Collections.emptyList();
        }

        // 2. 按 receiver_user_ids 快照过滤可见性，结果按 id 升序
        List<ImGroupMessageDO> messages = groupMessageMapper.selectListByMinId(userId, groupIds, minId, minSendTime, size);

        // 3. 补齐本人发送的回执消息的已读人数
        appendMessageReceipt(userId, messages);
        log.info("[pullGroupMessageList][userId({}) minId({}) size({}) result({})]", userId, minId, size, messages.size());
        return messages;
    }

    /**
     * 补全本人发送消息的回执已读人数（readCount）
     * <p>
     * 仅对本人发送、且需要回执（非 NO_RECEIPT）的消息，按 receiver_user_ids 快照 ∩ 当前有效成员 - 发送者 算已读人数
     */
    @SuppressWarnings("DataFlowIssue")
    private void appendMessageReceipt(Long userId, List<ImGroupMessageDO> messages) {
        if (CollUtil.isEmpty(messages)) {
            return;
        }
        // 群已读关闭：不补 readCount
        if (BooleanUtil.isFalse(imProperties.getMessage().isGroupReadEnabled())) {
            return;
        }
        Map<Long, Map<Long, Long>> readPositionsByGroup = new HashMap<>(); // 群 → (用户 → 已读位置)
        Map<Long, List<ImGroupMemberDO>> membersByGroup = new HashMap<>(); // 群 → 全部成员列表
        for (ImGroupMessageDO message : messages) {
            // 仅补本人发送、且需要回执（非 NO_RECEIPT）的消息
            if (ObjUtil.notEqual(message.getSenderId(), userId)
                    || ImMessageReceiptStatusEnum.NO_RECEIPT.getStatus().equals(message.getReceiptStatus())) {
                continue;
            }
            Long groupId = message.getGroupId();
            Map<Long, Long> positions = readPositionsByGroup.computeIfAbsent(groupId,
                    gid -> conversationReadService.getUserReadMessageIdMap(ImConversationTypeEnum.GROUP.getType(), gid));
            // 应读分母取当前有效成员（剔除已退群），与异步回执刷新、前端已读弹层口径一致
            List<ImGroupMemberDO> activeMembers = membersByGroup.computeIfAbsent(groupId,
                    groupMemberService::getActiveGroupMemberListByGroupId);
            Set<Long> receiverUserIds = getReceiverUserIds(message, activeMembers);
            receiverUserIds.remove(message.getSenderId());
            int readCount = getSumValue(receiverUserIds,
                    uid -> positions.getOrDefault(uid, -1L) >= message.getId() ? 1 : null,
                    Integer::sum, 0);
            message.setReadCount(readCount);
        }
    }

    @Override
    public void readGroupMessages(Long userId, Long groupId, Long messageId) {
        // 0. 全局开关校验
        if (BooleanUtil.isFalse(imProperties.getMessage().isGroupReadEnabled())) {
            throw exception(MESSAGE_GROUP_READ_DISABLED);
        }
        Assert.notNull(messageId, "已读消息编号不能为空");
        // 1. 校验消息属于当前群，且对当前用户可见（按快照）
        ImGroupMessageDO message = groupMessageMapper.selectById(messageId);
        if (message == null
                || ObjUtil.notEqual(message.getGroupId(), groupId)
                || !isMessageReceived(message, userId)) {
            throw exception(MESSAGE_NOT_IN_GROUP);
        }

        // 2. 取旧读位置（用于回执刷新区间），再更新群已读位置；读位置未前进则不推
        Long prevMaxMessageId = conversationReadService.getConversationReadMessageId(
                userId, ImConversationTypeEnum.GROUP.getType(), groupId);
        boolean advanced = conversationReadService.updateConversationReadPosition(
                userId, ImConversationTypeEnum.GROUP.getType(), groupId, messageId);
        if (!advanced) {
            return;
        }

        // 3. 异步发送 READ 事件 + 刷新范围内的群回执
        getSelf().readGroupMessageEvent(userId, groupId, prevMaxMessageId, messageId);
    }

    @Override
    public List<Long> getGroupReadUserIds(Long userId, Long groupId, Long messageId) {
        // 0. 全局开关校验
        if (BooleanUtil.isFalse(imProperties.getMessage().isGroupReadEnabled())) {
            throw exception(MESSAGE_GROUP_READ_DISABLED);
        }
        // 1.1 校验用户在群中（权限校验）
        groupMemberService.validateMemberInGroup(groupId, userId);
        // 1.2 获取消息；并校验消息归属于该群、对调用者可见、调用者是发送方
        ImGroupMessageDO message = groupMessageMapper.selectById(messageId);
        if (message == null || ObjUtil.notEqual(message.getGroupId(), groupId)) {
            return Collections.emptyList();
        }
        if (!isMessageReceived(message, userId)) {
            return Collections.emptyList();
        }
        // 1.3 仅消息发送方关心已读人数；非发送方查询直接返回空
        if (ObjUtil.notEqual(message.getSenderId(), userId)) {
            return Collections.emptyList();
        }

        // 2. 获取当前有效成员和已读位置（剔除已退群，与回执分母口径一致）
        List<ImGroupMemberDO> activeMembers = groupMemberService.getActiveGroupMemberListByGroupId(groupId);
        Map<Long, Long> allPositions = conversationReadService.getUserReadMessageIdMap(
                ImConversationTypeEnum.GROUP.getType(), groupId);

        // 3. 计算该消息的可见成员集合（排除发送者自己）
        Set<Long> receiverUserIds = getReceiverUserIds(message, activeMembers);
        receiverUserIds.remove(message.getSenderId());

        // 4. 只返回在可见范围内且已读位置 >= messageId 的用户
        List<Long> readUserIds = new ArrayList<>();
        allPositions.forEach((uid, readMaxMessageId) -> {
            if (receiverUserIds.contains(uid) && readMaxMessageId >= messageId) {
                readUserIds.add(uid);
            }
        });
        return readUserIds;
    }

    @Override
    public List<ImGroupMessageDO> getGroupMessageList(Long userId, ImGroupMessageListReqVO reqVO) {
        // 1. 校验用户曾经在群（当前在群或已退群），与 pull 退群窗口口径一致；内容仍由 receiver_user_ids 快照过滤
        if (groupMemberService.getGroupMember(reqVO.getGroupId(), userId) == null) {
            throw exception(GROUP_MEMBER_NOT_IN_GROUP);
        }

        // 2. 查询历史消息：SQL 已按 receiver_user_ids 快照过滤当前用户可见
        return groupMessageMapper.selectHistoryListByUser(
                userId, reqVO.getGroupId(), reqVO.getMaxId(), reqVO.getLimit());
    }

    // ========== 异步 WebSocket 推送 ==========

    /**
     * 发送已读 + 刷新群回执 WebSocket 事件
     *
     * @param userId            当前用户编号
     * @param groupId           群编号
     * @param prevMaxMessageId  上次已读位置
     * @param newMaxMessageId   本次已读位置
     */
    @Async
    @SuppressWarnings("DataFlowIssue")
    public void readGroupMessageEvent(Long userId, Long groupId, Long prevMaxMessageId, Long newMaxMessageId) {
        // 1. 发送 READ 事件给自己的其他终端（多端同步）
        imWebSocketService.sendNotificationAsync(userId, ImConversationTypeEnum.GROUP.getType(),
                ImContentTypeEnum.READ.getType(),
                ImMessageReadNotification.ofGroup(userId, groupId, newMaxMessageId));

        // 2. 刷新 (prevMaxMessageId, newMaxMessageId] 区间内的待回执消息
        List<ImGroupMessageDO> pendingMessages = groupMessageMapper.selectListByGroupIdAndPendingReceipt(
                groupId, prevMaxMessageId, newMaxMessageId);
        if (CollUtil.isEmpty(pendingMessages)) {
            return;
        }
        List<ImGroupMemberDO> activeMembers = groupMemberService.getActiveGroupMemberListByGroupId(groupId);
        Map<Long, Long> allPositions = conversationReadService.getUserReadMessageIdMap(
                ImConversationTypeEnum.GROUP.getType(), groupId);
        for (ImGroupMessageDO message : pendingMessages) {
            // 2.1.1 统计可见成员中的已读人数（应读分母 = 快照 ∩ 当前有效成员 - sender）
            Set<Long> receiverUserIds = getReceiverUserIds(message, activeMembers);
            receiverUserIds.remove(message.getSenderId()); // 发送者自己不算已读
            if (CollUtil.isEmpty(receiverUserIds)) {
                continue;
            }
            int readCount = getSumValue(receiverUserIds,
                    uid -> allPositions.getOrDefault(uid, -1L) >= message.getId() ? 1 : null,
                    Integer::sum, 0);
            // 2.1.2 全部已读 → 标记回执完成
            Integer newReceiptStatus = ImMessageReceiptStatusEnum.PENDING.getStatus();
            if (readCount >= receiverUserIds.size()) {
                newReceiptStatus = ImMessageReceiptStatusEnum.DONE.getStatus();
                groupMessageMapper.updateById(new ImGroupMessageDO().setId(message.getId())
                        .setReceiptStatus(newReceiptStatus));
            }

            // 2.2 发送 RECEIPT 事件给消息发送方（只有 ta 关心已读进度）
            imWebSocketService.sendNotificationAsync(message.getSenderId(), ImConversationTypeEnum.GROUP.getType(),
                    ImContentTypeEnum.RECEIPT.getType(),
                    ImMessageReceiptNotification.ofGroup(message.getId(), groupId, readCount, newReceiptStatus));
        }
    }

    // ========== 私有工具方法 ==========

    /**
     * 群聊引用消息规范化
     *
     * @param reqVO 发送请求
     * @param senderMember 发送人成员
     * @param newAudience 新消息可见成员集合（发送当时快照）
     * @return 规范化后的 content
     */
    private String normalizeQuoteContent(ImGroupMessageSendReqVO reqVO, ImGroupMemberDO senderMember,
                                         Set<Long> newAudience) {
        // 解析客户端 content 里的 quote.messageId
        Long quoteMessageId = ImMessageUtils.parseQuoteMessageId(reqVO.getContent());

        // 情况一：没有 quoteMessageId，直接 remove 掉 content 里可能伪造的 quote 字段
        if (quoteMessageId == null) {
            return ImMessageUtils.removeQuote(reqVO.getContent());
        }

        // 情况二：有 quoteMessageId，加载原消息并校验
        ImGroupMessageDO original = groupMessageMapper.selectById(quoteMessageId);
        if (original == null
                || ImMessageStatusEnum.RECALL.getStatus().equals(original.getStatus())) {
            throw exception(MESSAGE_QUOTE_INVALID);
        }
        // 校验是同群
        if (ObjUtil.notEqual(original.getGroupId(), reqVO.getGroupId())) {
            throw exception(MESSAGE_QUOTE_INVALID);
        }
        // 校验对发送人可见（按快照）
        if (!isMessageReceived(original, senderMember.getUserId())) {
            throw exception(MESSAGE_QUOTE_INVALID);
        }
        // 防泄漏：新消息可见集合必须是被引用消息可见集合的子集，否则禁止引用；
        // 否则只对部分成员可见的定向消息内容，会随广播引用泄漏给原本看不到的成员
        if (!new HashSet<>(original.getReceiverUserIds()).containsAll(newAudience)) {
            throw exception(MESSAGE_QUOTE_INVALID);
        }
        // 构建 quote 对象并注入 content
        QuoteMessage quote = ImMessageUtils.buildQuote(original.getId(),
                original.getSenderId(), original.getType(), original.getContent());
        return ImMessageUtils.appendQuote(reqVO.getContent(), quote);
    }

    /**
     * 校验群聊消息存在
     *
     * @param messageId 消息编号
     * @return 群聊消息
     */
    private ImGroupMessageDO validateGroupMessageExists(Long messageId) {
        ImGroupMessageDO message = groupMessageMapper.selectById(messageId);
        if (message == null) {
            throw exception(MESSAGE_NOT_EXISTS);
        }
        return message;
    }

    /**
     * 判断一条群消息是否被某个用户接收到
     * <p>
     * 接收范围以发送当时固化的 receiver_user_ids 快照为准；发送者也在快照内。
     *
     * @param message 消息
     * @param userId 当前用户编号
     * @return 是否接收到
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isMessageReceived(ImGroupMessageDO message, Long userId) {
        return CollUtil.contains(message.getReceiverUserIds(), userId);
    }

    /**
     * 计算一条群消息的可见成员集合（快照与传入成员的交集，含发送者）
     */
    private Set<Long> getReceiverUserIds(ImGroupMessageDO message, List<ImGroupMemberDO> members) {
        if (CollUtil.isEmpty(message.getReceiverUserIds())) {
            return new HashSet<>();
        }
        Set<Long> snapshot = new HashSet<>(message.getReceiverUserIds());
        return convertSet(members, ImGroupMemberDO::getUserId, member -> snapshot.contains(member.getUserId()));
    }

    /**
     * 基于群成员 userId 列表，计算一条新消息的可见成员快照（含发送者）。
     * <p>
     * 仅适用于「新消息」发送场景：无定向接收列表则全员可见，否则取定向用户与当前成员的交集；
     * 发送者始终纳入快照，保证 receiver_user_ids 非空且发送方可见。
     */
    private Set<Long> getReceiverUserIds(List<Long> directedUserIds, Long senderId, Collection<Long> memberUserIds) {
        // 无定向接收列表 → 全员可见；否则取定向用户与当前成员的交集
        Set<Long> result = CollUtil.isEmpty(directedUserIds)
                ? new HashSet<>(memberUserIds)
                : new HashSet<>(CollUtil.intersection(memberUserIds, directedUserIds));
        // 发送者始终可见（多端同步），即便成员缓存暂未包含
        if (senderId != null) {
            result.add(senderId);
        }
        return result;
    }

    // ==================== 管理后台 ====================

    @Override
    public PageResult<ImGroupMessageDO> getGroupMessagePage(ImGroupMessageManagerPageReqVO reqVO) {
        return groupMessageMapper.selectPage(reqVO);
    }

    @Override
    public ImGroupMessageDO getGroupMessage(Long id) {
        return groupMessageMapper.selectById(id);
    }

    @Override
    public List<ImGroupMessageDO> getGroupMessageList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return groupMessageMapper.selectByIds(ids);
    }

    @Override
    public Map<Long, ImGroupMessageDO> getGroupMessageMap(Collection<Long> ids) {
        return convertMap(getGroupMessageList(ids), ImGroupMessageDO::getId);
    }

    private ImGroupMessageServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

    /**
     * 计算群消息回执 status：群已读关闭时强制 NO_RECEIPT，忽略发送方传入的 receipt（receipt 为 null 等价 false）
     */
    private Integer resolveReceiptStatus(Boolean receipt) {
        if (BooleanUtil.isFalse(imProperties.getMessage().isGroupReadEnabled())) {
            return ImMessageReceiptStatusEnum.NO_RECEIPT.getStatus();
        }
        return BooleanUtil.isTrue(receipt)
                ? ImMessageReceiptStatusEnum.PENDING.getStatus()
                : ImMessageReceiptStatusEnum.NO_RECEIPT.getStatus();
    }

    /**
     * 禁言状态校验：全群禁言 → 成员禁言；群主 / 管理员豁免全群禁言
     */
    private void validateMuteStatus(ImGroupDO group, ImGroupMemberDO senderMember) {
        boolean isOwnerOrAdmin = ImGroupMemberRoleEnum.isOwnerOrAdmin(senderMember.getRole());
        // 1. 全群禁言：群主 / 管理员豁免
        if (Boolean.TRUE.equals(group.getMutedAll()) && !isOwnerOrAdmin) {
            throw exception(GROUP_MUTED_CANNOT_SEND);
        }
        // 2. 成员禁言
        if (senderMember.getMuteEndTime() != null && senderMember.getMuteEndTime().isAfter(LocalDateTime.now())) {
            throw exception(GROUP_MEMBER_MUTED_CANNOT_SEND, senderMember.getMuteEndTime());
        }
    }

}
