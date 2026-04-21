package cn.iocoder.yudao.module.im.service.message;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.group.ImGroupMessageListReqVO;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.group.ImGroupMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.content.RecallMessage;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImGroupMessageMapper;
import cn.iocoder.yudao.module.im.dal.redis.message.GroupMessageReadRedisDAO;
import cn.iocoder.yudao.module.im.enums.message.ImGroupMessageReceiptStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.service.group.ImGroupMemberService;
import cn.iocoder.yudao.module.im.service.group.ImGroupService;
import cn.iocoder.yudao.module.im.service.sensitiveword.ImSensitiveWordService;
import cn.iocoder.yudao.module.im.websocket.ImGroupMessageDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.im.enums.ImCommonConstants.*;

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
    private GroupMessageReadRedisDAO groupMessageReadRedisDAO;

    @Resource
    private ImGroupService groupService;
    @Resource
    private ImGroupMemberService groupMemberService;
    @Resource
    private ImSensitiveWordService sensitiveWordService;

    @Resource
    private WebSocketMessageSender webSocketMessageSender;

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
        // 1.2 校验群存在、发送人仍在群中
        groupService.validateGroupExists(reqVO.getGroupId());
        groupMemberService.validateMemberInGroup(reqVO.getGroupId(), senderId);
        // 1.3 文本消息敏感词过滤
        if (ImMessageTypeEnum.TEXT.getType().equals(reqVO.getType())) {
            sensitiveWordService.validateText(reqVO.getContent());
        }

        // 2. 构建并保存消息
        ImGroupMessageDO message = BeanUtils.toBean(reqVO, ImGroupMessageDO.class, m -> m
                .setSenderId(senderId).setStatus(ImMessageStatusEnum.UNREAD.getStatus()).setSendTime(LocalDateTime.now())
                .setReceiptStatus(Boolean.TRUE.equals(reqVO.getReceipt())
                        ? ImGroupMessageReceiptStatusEnum.PENDING.getStatus()
                        : ImGroupMessageReceiptStatusEnum.NO_RECEIPT.getStatus()));
        groupMessageMapper.insert(message);

        // 3. WebSocket 推送给群成员（异步）
        getSelf().sendGroupMessageEvent(message);
        return message;
    }

    @Override
    public List<ImGroupMessageDO> pullGroupMessageList(Long userId, Long minId, Integer size) {
        if (size > MESSAGE_MAX_PULL_SIZE) {
            throw exception(MESSAGE_PULL_SIZE_EXCEEDED);
        }

        // 0. 拉取时间窗：超过窗口的老消息不再通过离线通道推送
        LocalDateTime minSendTime = LocalDateTime.now().minusDays(MESSAGE_GROUP_PULL_MAX_DAYS);

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
     * 拉取当前仍在群主路径的消息。
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
            if (maxMessageId <= activeMinId) {
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
                .toList();
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
        Assert.notNull(messageId, "已读消息编号不能为空");
        // 1. 校验用户在群中（权限校验）
        groupMemberService.validateMemberInGroup(groupId, userId);

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
    @Transactional(rollbackFor = Exception.class)
    public ImGroupMessageDO recallGroupMessage(Long userId, Long messageId) {
        // 1.1 校验消息存在
        ImGroupMessageDO message = validateGroupMessageExists(messageId);
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
        // 1.5 校验撤回人仍在群中
        groupMemberService.validateMemberInGroup(message.getGroupId(), userId);

        // 2. 更新原消息状态为撤回
        groupMessageMapper.updateById(new ImGroupMessageDO().setId(messageId)
                .setStatus(ImMessageStatusEnum.RECALL.getStatus()));

        // 3. 插入一条 TIP_TEXT 消息作为撤回提示
        RecallMessage recallContent = new RecallMessage().setMessageId(messageId);
        ImGroupMessageDO recallMessage = new ImGroupMessageDO().setClientMessageId(IdUtil.fastSimpleUUID())
                .setSenderId(userId).setGroupId(message.getGroupId())
                .setType(ImMessageTypeEnum.TIP_TEXT.getType()).setContent(JsonUtils.toJsonString(recallContent))
                .setStatus(ImMessageStatusEnum.UNREAD.getStatus()).setSendTime(LocalDateTime.now())
                .setReceiptStatus(ImGroupMessageReceiptStatusEnum.NO_RECEIPT.getStatus());
        groupMessageMapper.insert(recallMessage);

        // 4. 异步推送撤回提示消息（前端据此更新原消息状态 + 插入撤回提示）
        getSelf().sendGroupMessageEvent(recallMessage);
        return recallMessage;
    }

    // TODO @芋艿：这个方法在优化下；
    @Override
    public List<Long> getGroupReadUsers(Long userId, Long groupId, Long messageId) {
        // 1.1 校验用户在群中（权限校验）
        groupMemberService.validateMemberInGroup(groupId, userId);
        // 1.2 获取消息
        ImGroupMessageDO message = groupMessageMapper.selectById(messageId);
        if (message == null) {
            return Collections.emptyList();
        }

        // 2. 获取所有成员和已读位置
        List<ImGroupMemberDO> allMembers = groupMemberService.getGroupMemberListByGroupId(groupId);
        Map<Long, Long> allPositions = groupMessageReadRedisDAO.getReadMaxMessageIdMap(groupId);

        // 3. 计算该消息的可见成员集合（排除发送者自己）
        Set<Long> visibleUserIds = getVisibleUserIds(message, allMembers);
        visibleUserIds.remove(message.getSenderId());

        // 4. 只返回在可见范围内且已读位置 >= messageId 的用户
        // DONE @AI：根据返回结果，进行简化；应该都是 Long、Long；——RedisDAO 已返回 Map<Long, Long>，直接使用
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
        return groupMessageMapper.selectHistoryList(
                reqVO.getGroupId(), reqVO.getMaxId(), reqVO.getLimit(), member.getJoinTime());
    }

    // ========== 异步 WebSocket 推送 ==========

    /**
     * 发送群聊消息 WebSocket 事件
     */
    @Async
    public void sendGroupMessageEvent(ImGroupMessageDO message) {
        ImGroupMessageDTO dto = ImGroupMessageDTO.ofSend(message);
        // 广播给群内有效成员（含发送方自己，支持多端同步）
        List<ImGroupMemberDO> members = groupMemberService.getActiveGroupMemberListByGroupId(message.getGroupId());
        // 定向消息仍需过滤，非目标用户不应收到推送
        List<Long> receiverList = message.getReceiverUserIds();
        for (ImGroupMemberDO member : members) {
            if (CollUtil.isNotEmpty(receiverList) && !receiverList.contains(member.getUserId())
                    && ObjUtil.notEqual(member.getUserId(), message.getSenderId())) {
                continue;
            }
            webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), member.getUserId(),
                    ImGroupMessageDTO.TYPE, dto);
        }
    }

    /**
     * 发送已读 + 刷新群回执 WebSocket 事件
     *
     * @param userId            当前用户编号
     * @param groupId           群编号
     * @param prevMaxMessageId  上次已读位置
     * @param newMaxMessageId   本次已读位置
     */
    @Async
    public void readGroupMessageEvent(Long userId, Long groupId, Long prevMaxMessageId, Long newMaxMessageId) {
        // 1. 发送 READ 事件给自己的其他终端（多端同步）
        ImGroupMessageDTO readDTO = ImGroupMessageDTO.ofRead(userId, groupId, newMaxMessageId);
        webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), userId,
                ImGroupMessageDTO.TYPE, readDTO);

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
            int readCount = 0;
            for (Long uid : visibleUserIds) {
                Long readMaxMessageId = allPositions.get(uid);
                if (readMaxMessageId != null && readMaxMessageId >= message.getId()) {
                    readCount++;
                }
            }
            // 2.1.2 全部已读 → 标记回执完成
            boolean allRead = readCount >= visibleUserIds.size();
            Integer newReceiptStatus = allRead
                    ? ImGroupMessageReceiptStatusEnum.DONE.getStatus()
                    : ImGroupMessageReceiptStatusEnum.PENDING.getStatus();
            if (allRead) {
                groupMessageMapper.updateById(new ImGroupMessageDO().setId(message.getId())
                        .setReceiptStatus(newReceiptStatus));
            }

            // 224 发送 RECEIPT 事件给消息发送方（只有 ta 关心已读进度）
            ImGroupMessageDTO receiptDTO = ImGroupMessageDTO.ofReceipt(
                    message.getId(), groupId, readCount, newReceiptStatus);
            webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), message.getSenderId(),
                    ImGroupMessageDTO.TYPE, receiptDTO);
        }
    }

    // ========== 私有工具方法 ==========

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
        // 3. 存在定向接收列表且当前用户既不在列表中也不是发送者 → 不可见
        return !CollUtil.isNotEmpty(msg.getReceiverUserIds())
                || msg.getReceiverUserIds().contains(userId)
                || !ObjUtil.notEqual(msg.getSenderId(), userId);
    }

    /**
     * 计算一条群消息的可见成员集合（含发送者）
     */
    private Set<Long> getVisibleUserIds(ImGroupMessageDO msg, List<ImGroupMemberDO> allMembers) {
        return convertSet(allMembers, ImGroupMemberDO::getUserId,
                member -> isMessageVisible(msg, member, member.getUserId()));
    }

    private ImGroupMessageServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
