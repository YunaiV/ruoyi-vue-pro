package cn.iocoder.yudao.module.im.service.message;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.im.controller.admin.message.vo.group.ImGroupMessageSendReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import cn.iocoder.yudao.module.im.dal.mysql.message.ImGroupMessageMapper;
import cn.iocoder.yudao.module.im.dal.redis.group.GroupReadPositionRedisDAO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImGroupMessageReceiptStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.enums.message.ImWebSocketTypeConstants;
import cn.iocoder.yudao.module.im.service.group.ImGroupService;
import cn.iocoder.yudao.module.im.service.group.ImGroupMemberService;
import cn.iocoder.yudao.module.im.service.sensitiveword.ImSensitiveWordService;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
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

    private static final int MAX_PULL_SIZE = 1000;

    @Resource
    private ImGroupMessageMapper imGroupMessageMapper;
    @Resource
    private ImGroupService imGroupService;
    @Resource
    private ImGroupMemberService imGroupMemberService;
    @Resource
    private ImSensitiveWordService imSensitiveWordService;
    @Resource
    private GroupReadPositionRedisDAO groupReadPositionRedisDAO;
    @Resource
    private WebSocketMessageSender webSocketMessageSender;

    @Override
    public ImGroupMessageDO sendMessage(Long senderId, ImGroupMessageSendReqVO reqVO) {
        // 1. 幂等校验
        ImGroupMessageDO existing = imGroupMessageMapper.selectBySenderIdAndClientMessageId(
                senderId, reqVO.getClientMessageId());
        if (existing != null) {
            return existing;
        }

        // 2. 校验群存在且未解散
        ImGroupDO group = imGroupService.validateGroupExists(reqVO.getGroupId());

        // 3. 校验发送人在群中
        ImGroupMemberDO member = imGroupMemberService.getGroupMember(reqVO.getGroupId(), senderId);
        if (member == null || CommonStatusEnum.DISABLE.getStatus().equals(member.getStatus())) {
            throw exception(GROUP_MEMBER_NOT_IN_GROUP);
        }

        // 4. 文本消息敏感词过滤
        if (ImMessageTypeEnum.TEXT.getType().equals(reqVO.getType())) {
            imSensitiveWordService.validateText(reqVO.getContent());
        }

        // 5. 构建消息
        ImGroupMessageDO message = ImGroupMessageDO.builder()
                .clientMessageId(reqVO.getClientMessageId())
                .senderId(senderId)
                .groupId(reqVO.getGroupId())
                .type(reqVO.getType())
                .content(reqVO.getContent())
                .status(ImMessageStatusEnum.UNREAD.getStatus()) // 群聊正常状态 = 0
                .sendTime(LocalDateTime.now())
                .atUserIds(reqVO.getAtUserIds())
                .receiverUserIds(reqVO.getReceiverUserIds())
                .receiptStatus(Boolean.TRUE.equals(reqVO.getNeedReceipt())
                        ? ImGroupMessageReceiptStatusEnum.PENDING.getStatus()
                        : ImGroupMessageReceiptStatusEnum.NO_RECEIPT.getStatus())
                .build();
        imGroupMessageMapper.insert(message);

        // 6. WebSocket 推送给群成员
        sendGroupMessageEvent(message, reqVO.getGroupId());
        return message;
    }

    @Override
    public List<ImGroupMessageDO> pullMessages(Long userId, Long minId, Integer size) {
        if (size > MAX_PULL_SIZE) {
            throw exception(MESSAGE_PULL_SIZE_EXCEEDED);
        }
        // 获取用户所在的所有群
        List<ImGroupMemberDO> members = imGroupMemberService.getGroupMembersByUserId(userId);
        if (CollUtil.isEmpty(members)) {
            return Collections.emptyList();
        }
        List<Long> groupIds = members.stream()
                .map(ImGroupMemberDO::getGroupId)
                .collect(Collectors.toList());

        // 拉取消息
        List<ImGroupMessageDO> messages = imGroupMessageMapper.selectListByMinId(groupIds, minId, size);

        // 过滤：入群前消息不可见、定向接收过滤、退群后消息边界
        Map<Long, ImGroupMemberDO> memberMap = members.stream()
                .collect(Collectors.toMap(ImGroupMemberDO::getGroupId, m -> m, (a, b) -> a));

        return messages.stream().filter(msg -> {
            ImGroupMemberDO m = memberMap.get(msg.getGroupId());
            if (m == null) {
                return false;
            }
            // 入群前消息不可见
            if (m.getJoinTime() != null && msg.getSendTime().isBefore(m.getJoinTime())) {
                return false;
            }
            // 退群后消息边界
            if (CommonStatusEnum.DISABLE.getStatus().equals(m.getStatus())
                    && m.getQuitTime() != null && msg.getSendTime().isAfter(m.getQuitTime())) {
                return false;
            }
            // 定向接收过滤
            if (CollUtil.isNotEmpty(msg.getReceiverUserIds())) {
                if (!msg.getReceiverUserIds().contains(userId) && !msg.getSenderId().equals(userId)) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }

    @Override
    public void readMessages(Long userId, Long groupId) {
        // 1. 校验用户在群中（权限校验）
        ImGroupMemberDO member = imGroupMemberService.getGroupMember(groupId, userId);
        if (member == null || CommonStatusEnum.DISABLE.getStatus().equals(member.getStatus())) {
            throw exception(GROUP_MEMBER_NOT_IN_GROUP);
        }

        // 2. 高效获取群内最新消息 id（倒序 LIMIT 1，非全量扫描）
        Long maxMessageId = imGroupMessageMapper.selectMaxIdByGroupId(groupId);
        if (maxMessageId == null) {
            return;
        }

        // 3. 更新 Redis 群已读位置
        groupReadPositionRedisDAO.updateReadPosition(groupId, userId, maxMessageId);

        // 4. 发送 READ 事件给自己的其他终端
        Map<String, Object> readEvent = new HashMap<>();
        readEvent.put("groupId", groupId.toString());
        readEvent.put("messageScene", "group");
        webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), userId,
                ImWebSocketTypeConstants.READ, readEvent);

        // 5. 刷新群回执
        refreshGroupReceipts(groupId);
    }

    @Override
    public void recallMessage(Long userId, Long messageId) {
        // 1. 校验消息存在
        ImGroupMessageDO message = imGroupMessageMapper.selectById(messageId);
        if (message == null) {
            throw exception(MESSAGE_NOT_EXISTS);
        }
        // 2. 只能撤回自己发送的消息
        if (!message.getSenderId().equals(userId)) {
            throw exception(MESSAGE_RECALL_DENIED);
        }
        // 3. 不能重复撤回
        if (ImMessageStatusEnum.RECALL.getStatus().equals(message.getStatus())) {
            throw exception(MESSAGE_ALREADY_RECALLED);
        }
        // 4. 更新消息状态
        ImGroupMessageDO updateObj = new ImGroupMessageDO();
        updateObj.setId(messageId);
        updateObj.setStatus(ImMessageStatusEnum.RECALL.getStatus());
        imGroupMessageMapper.updateById(updateObj);

        // 5. 发送 RECALL 事件给所有群成员
        List<ImGroupMemberDO> members = imGroupMemberService.selectByGroupId(message.getGroupId());
        Map<String, Object> recallEvent = new HashMap<>();
        recallEvent.put("messageId", messageId.toString());
        recallEvent.put("groupId", message.getGroupId().toString());
        recallEvent.put("messageScene", "group");
        recallEvent.put("senderId", userId.toString());
        for (ImGroupMemberDO m : members) {
            if (CommonStatusEnum.DISABLE.getStatus().equals(m.getStatus())) {
                continue;
            }
            webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), m.getUserId(),
                    ImWebSocketTypeConstants.RECALL, recallEvent);
        }
    }

    @Override
    public List<Long> getReadUsers(Long userId, Long groupId, Long messageId) {
        // 1. 校验用户在群中（权限校验）
        ImGroupMemberDO currentMember = imGroupMemberService.getGroupMember(groupId, userId);
        if (currentMember == null || CommonStatusEnum.DISABLE.getStatus().equals(currentMember.getStatus())) {
            throw exception(GROUP_MEMBER_NOT_IN_GROUP);
        }

        // 2. 获取消息
        ImGroupMessageDO message = imGroupMessageMapper.selectById(messageId);
        if (message == null) {
            return Collections.emptyList();
        }

        // 2. 获取所有成员和已读位置
        List<ImGroupMemberDO> allMembers = imGroupMemberService.selectByGroupId(groupId);
        Map<Object, Object> allPositions = groupReadPositionRedisDAO.getAllReadPositions(groupId);

        // 3. 计算该消息的可见成员集合
        Set<Long> visibleUserIds = getVisibleUserIds(message, allMembers);

        // 4. 只返回在可见范围内且已读位置 >= messageId 的用户
        List<Long> readUserIds = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : allPositions.entrySet()) {
            Long uid = Long.parseLong(entry.getKey().toString());
            Long readMaxId = Long.parseLong(entry.getValue().toString());
            if (readMaxId >= messageId && visibleUserIds.contains(uid)
                    && !uid.equals(message.getSenderId())) { // 排除发送者自己
                readUserIds.add(uid);
            }
        }
        return readUserIds;
    }

    /**
     * 刷新群回执（仅处理 PENDING 状态的消息，无需全量扫描）
     */
    private void refreshGroupReceipts(Long groupId) {
        // 1. 只查 PENDING 状态的回执消息
        List<ImGroupMessageDO> pendingMessages = imGroupMessageMapper.selectPendingReceiptMessages(groupId);
        if (CollUtil.isEmpty(pendingMessages)) {
            return;
        }

        // 2. 加载成员和已读位置
        List<ImGroupMemberDO> allMembers = imGroupMemberService.selectByGroupId(groupId);
        Map<Object, Object> allPositions = groupReadPositionRedisDAO.getAllReadPositions(groupId);

        for (ImGroupMessageDO msg : pendingMessages) {
            // 3. 计算该消息的可见成员（排除发送者自己）
            Set<Long> visibleUserIds = getVisibleUserIds(msg, allMembers);
            visibleUserIds.remove(msg.getSenderId()); // 发送者不计入回执统计

            if (visibleUserIds.isEmpty()) {
                continue;
            }

            // 4. 统计可见成员中的已读人数
            int readCount = 0;
            for (Long uid : visibleUserIds) {
                Object val = allPositions.get(uid.toString());
                if (val != null && Long.parseLong(val.toString()) >= msg.getId()) {
                    readCount++;
                }
            }

            // 5. 判断是否全部已读
            boolean allRead = readCount >= visibleUserIds.size();
            if (allRead) {
                ImGroupMessageDO update = new ImGroupMessageDO();
                update.setId(msg.getId());
                update.setReceiptStatus(ImGroupMessageReceiptStatusEnum.DONE.getStatus());
                imGroupMessageMapper.updateById(update);
            }

            // 6. 广播 RECEIPT 事件给活跃成员
            Map<String, Object> receiptEvent = new HashMap<>();
            receiptEvent.put("messageId", msg.getId().toString());
            receiptEvent.put("groupId", groupId.toString());
            receiptEvent.put("readCount", readCount);
            receiptEvent.put("receiptStatus", allRead
                    ? ImGroupMessageReceiptStatusEnum.DONE.getStatus()
                    : ImGroupMessageReceiptStatusEnum.PENDING.getStatus());

            for (ImGroupMemberDO m : allMembers) {
                if (CommonStatusEnum.DISABLE.getStatus().equals(m.getStatus())) {
                    continue;
                }
                webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), m.getUserId(),
                        ImWebSocketTypeConstants.RECEIPT, receiptEvent);
            }
        }
    }

    /**
     * 计算一条群消息的可见成员集合
     * <p>
     * 规则：
     * 1. 成员必须是 NORMAL 状态，或虽已 QUIT 但消息发送时间在退群之前
     * 2. 成员的入群时间必须在消息发送之前（入群后才能看到消息）
     * 3. 如果消息有 receiverUserIds 定向接收，则只有列表中的人可见
     */
    private Set<Long> getVisibleUserIds(ImGroupMessageDO msg, List<ImGroupMemberDO> allMembers) {
        // 定向接收集合
        List<Long> receiverList = msg.getReceiverUserIds();

        Set<Long> visible = new HashSet<>();
        for (ImGroupMemberDO m : allMembers) {
            // 入群时间在消息发送之后 → 不可见
            if (m.getJoinTime() != null && msg.getSendTime().isBefore(m.getJoinTime())) {
                continue;
            }
            // 已退群且退群时间在消息发送之前 → 不可见
            if (CommonStatusEnum.DISABLE.getStatus().equals(m.getStatus())
                    && m.getQuitTime() != null && msg.getSendTime().isAfter(m.getQuitTime())) {
                continue;
            }
            // 定向接收过滤
            if (CollUtil.isNotEmpty(receiverList) && !receiverList.contains(m.getUserId())
                    && !m.getUserId().equals(msg.getSenderId())) {
                continue;
            }
            visible.add(m.getUserId());
        }
        return visible;
    }

    /**
     * 发送群聊消息 WebSocket 事件
     */
    private void sendGroupMessageEvent(ImGroupMessageDO message, Long groupId) {
        List<ImGroupMemberDO> members = imGroupMemberService.selectByGroupId(groupId);
        Map<String, Object> event = new HashMap<>();
        event.put("id", message.getId().toString());
        event.put("clientMessageId", message.getClientMessageId());
        event.put("senderId", message.getSenderId().toString());
        event.put("groupId", message.getGroupId().toString());
        event.put("type", message.getType());
        event.put("content", message.getContent());
        event.put("status", message.getStatus());
        event.put("sendTime", message.getSendTime().toString());
        event.put("messageScene", "group");
        if (CollUtil.isNotEmpty(message.getAtUserIds())) {
            event.put("atUserIds", message.getAtUserIds());
        }
        if (CollUtil.isNotEmpty(message.getReceiverUserIds())) {
            event.put("receiverUserIds", message.getReceiverUserIds());
        }
        event.put("receiptStatus", message.getReceiptStatus());

        // 确定推送目标
        List<Long> receiverList = message.getReceiverUserIds();

        for (ImGroupMemberDO member : members) {
            if (CommonStatusEnum.DISABLE.getStatus().equals(member.getStatus())) {
                continue;
            }
            // 定向接收过滤
            if (CollUtil.isNotEmpty(receiverList) && !receiverList.contains(member.getUserId())
                    && !member.getUserId().equals(message.getSenderId())) {
                continue;
            }
            webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), member.getUserId(),
                    ImWebSocketTypeConstants.GROUP_MESSAGE, event);
        }
    }

}
