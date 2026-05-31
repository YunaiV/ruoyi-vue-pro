package cn.iocoder.yudao.module.im.service.message;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
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
import cn.iocoder.yudao.module.im.dal.redis.message.ImGroupMessageReadRedisDAO;
import cn.iocoder.yudao.module.im.enums.group.ImGroupMemberRoleEnum;
import cn.iocoder.yudao.module.im.enums.message.ImGroupMessageReceiptStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.framework.config.ImProperties;
import cn.iocoder.yudao.module.im.service.group.ImGroupMemberService;
import cn.iocoder.yudao.module.im.service.group.ImGroupService;
import cn.iocoder.yudao.module.im.service.message.dto.ImGroupMessageSendDTO;
import cn.iocoder.yudao.module.im.service.sensitiveword.ImSensitiveWordService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImGroupMessageDTO;
import cn.iocoder.yudao.module.im.service.websocket.dto.message.QuoteMessage;
import cn.iocoder.yudao.module.im.service.websocket.dto.message.RecallMessage;
import cn.iocoder.yudao.module.im.util.ImMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * 仅用于规避群消息 pull 的"假空页"：
     * 首批消息可能因入群时间或定向接收过滤后变成空列表，但后续更大的 id 仍然存在可见消息。
     * 因此仅在过滤结果为空时，按本轮消息最大 id 向后再试几次。
     */
    private static final int PULL_GROUP_MESSAGE_EMPTY_RETRY_TIMES = 3;

    @Resource
    private ImGroupMessageMapper groupMessageMapper;
    @Resource
    private ImGroupMessageReadRedisDAO groupMessageReadRedisDAO;

    @Resource
    private ImGroupService groupService;
    @Resource
    private ImGroupMemberService groupMemberService;
    @Resource
    private ImSensitiveWordService sensitiveWordService;

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
        if (ImMessageTypeEnum.TEXT.getType().equals(reqVO.getType())) {
            sensitiveWordService.validateText(reqVO.getContent());
        }

        // 2.1 引用 quote 消息规范化
        reqVO.setContent(normalizeQuoteContent(reqVO, senderMember));
        // 2.2 构建并保存消息
        ImGroupMessageDO message = BeanUtils.toBean(reqVO, ImGroupMessageDO.class, m -> m
                .setSenderId(senderId).setStatus(ImMessageStatusEnum.UNREAD.getStatus()).setSendTime(LocalDateTime.now())
                .setReceiptStatus(resolveReceiptStatus(reqVO.getReceipt())));
        groupMessageMapper.insert(message);

        // 3. WebSocket 异步推送（群内可见成员 + 发送方多端同步）
        List<Long> memberUserIds = groupMemberService.getActiveGroupMemberUserIdsByGroupId(message.getGroupId());
        Set<Long> targetUserIds = getVisibleUserIds(message.getReceiverUserIds(), senderId, memberUserIds);
        imWebSocketService.sendGroupMessageAsync(targetUserIds, ImGroupMessageDTO.ofSend(message));
        return message;
    }

    @Override
    public ImGroupMessageDO sendGroupMessage(Long senderId, ImGroupMessageSendDTO dto) {
        List<Long> memberUserIds = groupMemberService.getActiveGroupMemberUserIdsByGroupId(dto.getGroupId());
        Set<Long> targetUserIds = getVisibleUserIds(dto.getReceiverUserIds(), senderId, memberUserIds);
        return sendGroupMessage(senderId, targetUserIds, dto);
    }

    @Override
    public ImGroupMessageDO sendGroupMessage(Long senderId, Collection<Long> targetUserIds, ImGroupMessageSendDTO dto) {
        // 1.1 content 序列化：null / String 透传，POJO 走 JSON
        Object payload = dto.getContent();
        String contentString = payload == null || payload instanceof String
                ? (String) payload
                : JsonUtils.toJsonString(payload);
        // 1.2 构建并保存消息
        ImGroupMessageDO message = new ImGroupMessageDO().setClientMessageId(IdUtil.fastSimpleUUID())
                .setSenderId(senderId).setGroupId(dto.getGroupId())
                .setType(dto.getType()).setContent(contentString)
                .setStatus(ImMessageStatusEnum.UNREAD.getStatus()).setSendTime(LocalDateTime.now())
                .setAtUserIds(dto.getAtUserIds()).setReceiverUserIds(dto.getReceiverUserIds())
                .setReceiptStatus(resolveReceiptStatus(dto.getReceipt()));
        // 1.3 按 type.persistent 决定是否入库
        if (ImMessageTypeEnum.validate(dto.getType()).isPersistent()) {
            groupMessageMapper.insert(message);
        }

        // 2. WebSocket 异步推送
        imWebSocketService.sendGroupMessageAsync(targetUserIds, ImGroupMessageDTO.ofSend(message));
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
                .setType(ImMessageTypeEnum.RECALL.getType())
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

        // 1.1 主查询：仅用"当前仍在群"的成员记录驱动；若首批消息过滤后为空，则允许内部重试
        List<ImGroupMemberDO> activeMembers = groupMemberService.getActiveGroupMemberListByUserId(userId);
        Map<Long, ImGroupMemberDO> memberMap = convertMap(activeMembers, ImGroupMemberDO::getGroupId);
        List<ImGroupMessageDO> messages = new ArrayList<>();
        if (CollUtil.isNotEmpty(activeMembers)) {
            List<Long> groupIds = convertList(activeMembers, ImGroupMemberDO::getGroupId);
            messages.addAll(pullActiveGroupMessageList(userId, groupIds, minId, size, minSendTime, memberMap));
        }
        // 1.2 补齐"退群前"的消息：
        //    - 继续基于用户原始 minId 单次查询，不能被主查询内部探测游标带着前进；
        //    - 若 minId > 0 且能查到对应消息，则进一步把下限抬到该消息的 sendTime，避免把客户端已拥有的老消息再次推送。
        messages.addAll(pullQuitGroupMessageList(userId, minId, size, minSendTime, memberMap));

        // 2. 合并后统一过滤，得到当前用户可见的结果
        List<ImGroupMessageDO> result = filterGroupMessageList(messages, memberMap, userId, size);

        // 3. 按当前用户补齐：消息已读态（相对 Redis 已读游标）、本人发送的回执消息的已读人数
        appendMessageStatusAndReceipt(userId, result);
        log.info("[pullGroupMessageList][userId({}) minId({}) size({}) result({})]", userId, minId, size, result.size());
        return result;
    }

    /**
     * 拉取当前仍在群的主路径消息。
     *
     * 仅当首批消息过滤后仍无可见消息时，才按消息最大 id 继续向后探测，
     * 直到命中可见消息或确认该来源已耗尽。
     */
    private List<ImGroupMessageDO> pullActiveGroupMessageList(Long userId, List<Long> groupIds, Long minId,
                                                              Integer size, LocalDateTime minSendTime,
                                                              Map<Long, ImGroupMemberDO> memberMap) {
        // 1. 主查询内部探测游标：仅用于向后探测，不代表客户端真实已送达边界
        Long activeMinId = minId;
        for (int retryCount = 0; retryCount <= PULL_GROUP_MESSAGE_EMPTY_RETRY_TIMES; retryCount++) {
            // 2. 查询本轮消息；若已无更多消息，则当前主路径直接结束
            List<ImGroupMessageDO> messages = groupMessageMapper.selectListByMinId(groupIds, activeMinId,
                    minSendTime, size);
            if (CollUtil.isEmpty(messages)) {
                return Collections.emptyList();
            }
            boolean hasVisibleMessage = CollUtil.anyMatch(messages,
                    message -> isMessageVisible(message, memberMap.get(message.getGroupId()), userId));
            boolean sourceExhausted = messages.size() < size;
            // 3. 本轮已命中可见消息，或主查询来源已耗尽，直接返回这一轮消息
            if (hasVisibleMessage || sourceExhausted) {
                return messages;
            }

            // 4. 按本轮消息最大 id 推进内部游标，跳过这段不可见区间；若游标未前进则直接停止
            Long maxMessageId = getMaxValue(messages, ImGroupMessageDO::getId);
            if (maxMessageId == null || maxMessageId <= activeMinId) {
                return Collections.emptyList();
            }
            activeMinId = maxMessageId;
        }
        return Collections.emptyList();
    }

    /**
     * 拉取已离开群路径的消息（退群前消息）
     */
    private List<ImGroupMessageDO> pullQuitGroupMessageList(Long userId, Long minId, Integer size,
                                                            LocalDateTime minSendTime,
                                                            Map<Long, ImGroupMemberDO> memberMap) {
        // 1. 退群补齐始终基于用户原始 minId 计算时间边界，避免被主查询内部重试游标误伤
        LocalDateTime minQuitTime = minSendTime;
        if (minId != null && minId > 0) {
            ImGroupMessageDO minMessage = groupMessageMapper.selectById(minId);
            if (minMessage != null && minMessage.getSendTime() != null
                    && minMessage.getSendTime().isAfter(minSendTime)) {
                minQuitTime = minMessage.getSendTime();
            }
        }
        // 2. 查询用户离开的群记录；若原始 minId 对应消息仍在窗口内，则用它的发送时间抬升退群筛选下限
        List<ImGroupMessageDO> messages = new ArrayList<>();
        List<ImGroupMemberDO> quitMembers = groupMemberService.getQuitGroupMemberListByUserId(userId, minQuitTime);
        for (ImGroupMemberDO quitMember : quitMembers) {
            // 3. 按原始 minId + 退群时间补齐该群退群前消息，并把成员记录写回 memberMap 供统一可见性过滤使用
            List<ImGroupMessageDO> quitGroupMessages = groupMessageMapper.selectListByGroupIdAndMinIdAndQuitTimeBefore(
                    quitMember.getGroupId(), minId, minSendTime, quitMember.getQuitTime(), size);
            if (CollUtil.isEmpty(quitGroupMessages)) {
                continue;
            }
            messages.addAll(quitGroupMessages);
            memberMap.put(quitMember.getGroupId(), quitMember);
        }
        return messages;
    }

    /**
     * 过滤一批原始群消息，得到当前用户可见的返回结果
     */
    private List<ImGroupMessageDO> filterGroupMessageList(List<ImGroupMessageDO> messages,
                                                          Map<Long, ImGroupMemberDO> memberMap,
                                                          Long userId, Integer size) {
        // 按可见性过滤（入群前不可见、定向消息排除），按 id 升序后仅取本页 size 条，
        // 避免「在群 + 退群前」多路合并时一次响应跨度过大、游标直接跳到全局最大 id 而漏拉中间消息
        return messages.stream()
                .filter(msg -> isMessageVisible(msg, memberMap.get(msg.getGroupId()), userId))
                .sorted(Comparator.comparing(ImGroupMessageDO::getId))
                .limit(size)
                .collect(Collectors.toList());
    }

    /**
     * 补全消息已读态和回执已读人数
     *
     * 1. 消息已读态（status）：根据 Redis 已读游标判断 READ / UNREAD
     * 2. 回执已读人数（readCount）：仅对本人发送的回执消息，计算可见成员中的已读人数
     */
    @SuppressWarnings({"StatementWithEmptyBody", "DataFlowIssue"})
    private void appendMessageStatusAndReceipt(Long userId, List<ImGroupMessageDO> messages) {
        if (CollUtil.isEmpty(messages)) {
            return;
        }
        // 群已读关闭：不查 Redis 已读游标，status 保持 DB 原值（含撤回），readCount 不补齐
        if (BooleanUtil.isFalse(imProperties.getMessage().isGroupReadEnabled())) {
            return;
        }
        Map<Long, Long> readMaxMessageIdsByGroup = new HashMap<>(); // 群 → 已读位置
        Map<Long, Map<Long, Long>> readPositionsByGroup = new HashMap<>(); // 群 → (用户 → 已读位置)
        Map<Long, List<ImGroupMemberDO>> membersByGroup = new HashMap<>(); // 群 → 全部成员列表
        for (ImGroupMessageDO message : messages) {
            // 消息已读态（status）：撤回 > 已读 > 未读
            Long groupId = message.getGroupId();
            long readMaxMessageId = readMaxMessageIdsByGroup.computeIfAbsent(groupId, gid -> {
                Long readMaxMsgId = groupMessageReadRedisDAO.getReadMaxMessageId(gid, userId);
                return readMaxMsgId != null ? readMaxMsgId : -1L;
            });
            if (ImMessageStatusEnum.RECALL.getStatus().equals(message.getStatus())) {
                // 保持撤回态
            } else if (readMaxMessageId >= message.getId()) {
                message.setStatus(ImMessageStatusEnum.READ.getStatus());
            } else {
                message.setStatus(ImMessageStatusEnum.UNREAD.getStatus());
            }

            // 回执消息的已读人数（readCount）：仅补齐本人发送的，其他消息不处理（回执消息才关心已读人数，且只对发送者可见）
            if (ObjUtil.notEqual(message.getSenderId(), userId)
                || ImGroupMessageReceiptStatusEnum.NO_RECEIPT.getStatus().equals(message.getReceiptStatus())) {
                continue;
            }
            Map<Long, Long> positions = readPositionsByGroup.computeIfAbsent(groupId,
                    gid -> groupMessageReadRedisDAO.getReadMaxMessageIdMap(gid));
            List<ImGroupMemberDO> allMembers = membersByGroup.computeIfAbsent(groupId,
                    groupMemberService::getGroupMemberListByGroupId);
            Set<Long> visibleUserIds = getVisibleUserIds(message, allMembers);
            visibleUserIds.remove(message.getSenderId());
            int readCount = getSumValue(visibleUserIds,
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
        // 1.1 校验用户在群中（权限校验）
        ImGroupMemberDO member = groupMemberService.validateMemberInGroup(groupId, userId);
        // 1.2 校验消息属于当前群，且对当前用户可见
        ImGroupMessageDO message = groupMessageMapper.selectById(messageId);
        if (message == null
                || ObjUtil.notEqual(message.getGroupId(), groupId)
                || !isMessageVisible(message, member, userId)) {
            throw exception(MESSAGE_NOT_IN_GROUP);
        }

        // 2. 已读位置未前进，直接返回
        Long prevMaxMessageId = groupMessageReadRedisDAO.getReadMaxMessageId(groupId, userId);
        if (prevMaxMessageId != null && prevMaxMessageId >= messageId) {
            return;
        }
        // 3. 更新 Redis 群已读位置
        groupMessageReadRedisDAO.updateReadMaxMessageId(groupId, userId, messageId);

        // 4. 异步发送 READ 事件 + 刷新范围内的群回执
        getSelf().readGroupMessageEvent(userId, groupId, prevMaxMessageId, messageId);
    }

    @Override
    public List<Long> getGroupReadUserIds(Long userId, Long groupId, Long messageId) {
        // 0. 全局开关校验
        if (BooleanUtil.isFalse(imProperties.getMessage().isGroupReadEnabled())) {
            throw exception(MESSAGE_GROUP_READ_DISABLED);
        }
        // 1.1 校验用户在群中（权限校验）
        ImGroupMemberDO operator = groupMemberService.validateMemberInGroup(groupId, userId);
        // 1.2 获取消息；并校验消息归属于该群、对调用者可见、调用者是发送方
        ImGroupMessageDO message = groupMessageMapper.selectById(messageId);
        if (message == null || ObjUtil.notEqual(message.getGroupId(), groupId)) {
            return Collections.emptyList();
        }
        if (!isMessageVisible(message, operator, userId)) {
            return Collections.emptyList();
        }
        // 1.3 仅消息发送方关心已读人数；非发送方查询直接返回空
        if (ObjUtil.notEqual(message.getSenderId(), userId)) {
            return Collections.emptyList();
        }

        // 2. 获取所有成员和已读位置
        List<ImGroupMemberDO> allMembers = groupMemberService.getGroupMemberListByGroupId(groupId);
        Map<Long, Long> allPositions = groupMessageReadRedisDAO.getReadMaxMessageIdMap(groupId);

        // 3. 计算该消息的可见成员集合（排除发送者自己）
        Set<Long> visibleUserIds = getVisibleUserIds(message, allMembers);
        visibleUserIds.remove(message.getSenderId());

        // 4. 只返回在可见范围内且已读位置 >= messageId 的用户
        List<Long> readUserIds = new ArrayList<>();
        allPositions.forEach((uid, readMaxMessageId) -> {
            if (visibleUserIds.contains(uid) && readMaxMessageId >= messageId) {
                readUserIds.add(uid);
            }
        });
        return readUserIds;
    }

    @Override
    public List<ImGroupMessageDO> getGroupMessageList(Long userId, ImGroupMessageListReqVO reqVO) {
        // 1. 校验用户在群中
        ImGroupMemberDO member = groupMemberService.validateMemberInGroup(reqVO.getGroupId(), userId);

        // 2. 查询历史消息（仅入群之后）
        List<ImGroupMessageDO> messages = groupMessageMapper.selectHistoryList(
                reqVO.getGroupId(), reqVO.getMaxId(), reqVO.getLimit(), member.getJoinTime());

        // 3. 过滤定向消息：仅保留当前用户可见的（receiverUserIds 为空 / 含当前用户 / 本人发送）
        return filterList(messages, message -> isMessageVisible(message, member, userId));
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
        imWebSocketService.sendGroupMessageAsync(userId,
                ImGroupMessageDTO.ofRead(userId, groupId, newMaxMessageId));

        // 2. 刷新 (prevMaxMessageId, newMaxMessageId] 区间内的待回执消息
        List<ImGroupMessageDO> pendingMessages = groupMessageMapper.selectListByGroupIdAndPendingReceipt(
                groupId, prevMaxMessageId, newMaxMessageId);
        if (CollUtil.isEmpty(pendingMessages)) {
            return;
        }
        List<ImGroupMemberDO> activeMembers = groupMemberService.getActiveGroupMemberListByGroupId(groupId);
        Map<Long, Long> allPositions = groupMessageReadRedisDAO.getReadMaxMessageIdMap(groupId);
        for (ImGroupMessageDO message : pendingMessages) {
            // 2.1.1 统计可见成员中的已读人数
            Set<Long> visibleUserIds = getVisibleUserIds(message, activeMembers);
            visibleUserIds.remove(message.getSenderId()); // 发送者自己不算已读
            if (CollUtil.isEmpty(visibleUserIds)) {
                continue;
            }
            int readCount = getSumValue(visibleUserIds,
                    uid -> allPositions.getOrDefault(uid, -1L) >= message.getId() ? 1 : null,
                    Integer::sum, 0);
            // 2.1.2 全部已读 → 标记回执完成
            Integer newReceiptStatus = ImGroupMessageReceiptStatusEnum.PENDING.getStatus();
            if (readCount >= visibleUserIds.size()) {
                newReceiptStatus = ImGroupMessageReceiptStatusEnum.DONE.getStatus();
                groupMessageMapper.updateById(new ImGroupMessageDO().setId(message.getId())
                        .setReceiptStatus(newReceiptStatus));
            }

            // 2.2 发送 RECEIPT 事件给消息发送方（只有 ta 关心已读进度）
            imWebSocketService.sendGroupMessageAsync(message.getSenderId(),
                    ImGroupMessageDTO.ofReceipt(message.getId(), groupId, readCount, newReceiptStatus));
        }
    }

    // ========== 私有工具方法 ==========

    /**
     * 群聊引用消息规范化
     *
     * @param reqVO 发送请求
     * @param senderMember 发送人成员
     * @return 规范化后的 content
     */
    private String normalizeQuoteContent(ImGroupMessageSendReqVO reqVO, ImGroupMemberDO senderMember) {
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
        // 拒绝定向消息（仅发送人可见的内容若被全员广播 quote.content，会泄漏给原本看不到的成员）
        if (CollUtil.isNotEmpty(original.getReceiverUserIds())) {
            throw exception(MESSAGE_QUOTE_INVALID);
        }
        // 校验对发送人可见（入群时间 / 退群时间）
        if (!isMessageVisible(original, senderMember, senderMember.getUserId())) {
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
     * 判断一条群消息对某个群成员是否可见
     *
     * @param msg 消息
     * @param member 群成员
     * @param userId 当前用户编号（用于定向消息过滤）
     * @return 是否可见
     */
    private boolean isMessageVisible(ImGroupMessageDO msg, ImGroupMemberDO member, Long userId) {
        if (member == null) {
            return false;
        }
        // 1. 入群时间晚于消息发送时间 → 不可见
        if (member.getJoinTime() != null && msg.getSendTime().isBefore(member.getJoinTime())) {
            return false;
        }
        // 2. 已退群且退群时间早于消息发送时间 → 不可见
        if (CommonStatusEnum.DISABLE.getStatus().equals(member.getStatus())
                && member.getQuitTime() != null && msg.getSendTime().isAfter(member.getQuitTime())) {
            return false;
        }
        // 3.1 无定向接收列表 → 全员可见
        if (CollUtil.isEmpty(msg.getReceiverUserIds())) {
            return true;
        }
        // 3.2 当前用户在定向列表中，或本人即发送者 → 可见
        return msg.getReceiverUserIds().contains(userId)
                || ObjUtil.equal(msg.getSenderId(), userId);
    }

    /**
     * 计算一条群消息的可见成员集合（含发送者）
     */
    private Set<Long> getVisibleUserIds(ImGroupMessageDO message, List<ImGroupMemberDO> members) {
        return convertSet(members, ImGroupMemberDO::getUserId,
                member -> isMessageVisible(message, member, member.getUserId()));
    }

    /**
     * 基于群成员 userId 列表，过滤出一条新消息的可见成员集合（含发送者）。
     * <p>
     * 仅适用于「新消息」推送场景（{@code sendTime = now}），不涉及 joinTime / quitTime 判定，
     * 只应用 {@code receiverUserIds} 定向过滤；语义与
     * {@link #isMessageVisible(ImGroupMessageDO, ImGroupMemberDO, Long)} 的第 3 步保持一致。
     */
    private Set<Long> getVisibleUserIds(List<Long> receiverUserIds, Long senderId, Collection<Long> memberUserIds) {
        if (CollUtil.isEmpty(memberUserIds)) {
            return new HashSet<>();
        }
        // 无定向接收列表 → 全员可见
        if (CollUtil.isEmpty(receiverUserIds)) {
            return new HashSet<>(memberUserIds);
        }
        // 有定向接收列表 → 仅定向用户可见；发送者自己也能看到自己的消息（多端同步）
        Set<Long> allowed = new HashSet<>(receiverUserIds);
        if (senderId != null) {
            allowed.add(senderId);
        }
        Set<Long> result = new HashSet<>();
        for (Long userId : memberUserIds) {
            if (allowed.contains(userId)) {
                result.add(userId);
            }
        }
        return result;
    }

    @Override
    public void deleteReadMaxMessageId(Long groupId, Long userId) {
        groupMessageReadRedisDAO.deleteReadMaxMessageId(groupId, userId);
    }

    @Override
    public void deleteReadMaxMessageIds(Long groupId, Collection<Long> userIds) {
        groupMessageReadRedisDAO.deleteReadMaxMessageIds(groupId, userIds);
    }

    @Override
    public void deleteReadMaxMessageIdMap(Long groupId) {
        groupMessageReadRedisDAO.deleteReadMaxMessageIdMap(groupId);
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
            return ImGroupMessageReceiptStatusEnum.NO_RECEIPT.getStatus();
        }
        return BooleanUtil.isTrue(receipt)
                ? ImGroupMessageReceiptStatusEnum.PENDING.getStatus()
                : ImGroupMessageReceiptStatusEnum.NO_RECEIPT.getStatus();
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
