package cn.iocoder.yudao.module.im.service.message;

import cn.hutool.core.collection.CollUtil;
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
import cn.iocoder.yudao.module.im.dal.redis.message.GroupReadPositionRedisDAO;
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
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.im.enums.ImCommonConstants.MESSAGE_MAX_PULL_SIZE;
import static cn.iocoder.yudao.module.im.enums.ImCommonConstants.MESSAGE_RECALL_TIMEOUT_MINUTES;

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
    // TODO @AI：变量名，可能要改下；
    @Resource
    private GroupReadPositionRedisDAO groupReadPositionRedisDAO;

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
        validateMemberInGroup(reqVO.getGroupId(), senderId);
        // 1.3 文本消息敏感词过滤
        if (ImMessageTypeEnum.TEXT.getType().equals(reqVO.getType())) {
            sensitiveWordService.validateText(reqVO.getContent());
        }

        // 2. 构建并保存消息
        // TODO @AI：content 要 json 下；
        ImGroupMessageDO message = BeanUtils.toBean(reqVO, ImGroupMessageDO.class, m -> m
                .setSenderId(senderId).setStatus(ImMessageStatusEnum.UNREAD.getStatus())
                .setSendTime(LocalDateTime.now())
                .setReceiptStatus(Boolean.TRUE.equals(reqVO.getNeedReceipt())
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
        // 1. 获取用户所在的所有群
        List<ImGroupMemberDO> members = groupMemberService.getGroupMembersByUserId(userId);
        if (CollUtil.isEmpty(members)) {
            return Collections.emptyList();
        }
        List<Long> groupIds = convertList(members, ImGroupMemberDO::getGroupId);

        // 2. 拉取消息
        List<ImGroupMessageDO> messages = groupMessageMapper.selectListByMinId(groupIds, minId, size);

        // 3. 过滤：入群前不可见、退群后不可见、定向接收过滤
        // TODO @AI：缺少各种逻辑；
        Map<Long, ImGroupMemberDO> memberMap = CollUtil.toMap(members, new HashMap<>(), ImGroupMemberDO::getGroupId);
        List<ImGroupMessageDO> result = messages.stream()
                .filter(msg -> isMessageVisible(msg, memberMap.get(msg.getGroupId()), userId))
                .toList();
        log.info("[pullGroupMessageList][userId({}) minId({}) size({}) result({})]",
                userId, minId, size, result.size());
        return result;
    }

    // TODO @AI：增加 messageId 参数；允许空，则查询最大的一条；
    @Override
    public void readGroupMessages(Long userId, Long groupId) {
        // 1.1 校验用户在群中（权限校验）
        validateMemberInGroup(groupId, userId);
        // 1.2 获取群内最新消息 id（倒序 LIMIT 1，避免全量扫描）
        Long newMaxId = groupMessageMapper.selectMaxIdByGroupId(groupId);
        if (newMaxId == null) {
            return;
        }
        // 1.3 已读位置未前进，直接返回
        // TODO @AI：1.3 和 2 是不是可以合并？如果 update 失败，则直接回退；不然读取两次，貌似有 cas 问题；
        Long prevMaxId = groupReadPositionRedisDAO.getReadPosition(groupId, userId);
        if (newMaxId <= prevMaxId) {
            return;
        }

        // 2. 更新 Redis 群已读位置
        groupReadPositionRedisDAO.updateReadPosition(groupId, userId, newMaxId);

        // 3. 异步发送 READ 事件 + 刷新范围内的群回执
        getSelf().readGroupMessageEvent(userId, groupId, prevMaxId, newMaxId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImGroupMessageDO recallGroupMessage(Long userId, Long messageId) {
        // 1.1 校验消息存在
        // TODO @AI：抽成一个 validateXXXExists 方法；
        ImGroupMessageDO message = groupMessageMapper.selectById(messageId);
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
        // 1.5 校验撤回人仍在群中
        validateMemberInGroup(message.getGroupId(), userId);

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

    @Override
    public List<Long> getGroupReadUsers(Long userId, Long groupId, Long messageId) {
        // 1.1 校验用户在群中（权限校验）
        validateMemberInGroup(groupId, userId);
        // 1.2 获取消息
        ImGroupMessageDO message = groupMessageMapper.selectById(messageId);
        if (message == null) {
            return Collections.emptyList();
        }

        // 2. 获取所有成员和已读位置
        List<ImGroupMemberDO> allMembers = groupMemberService.selectByGroupId(groupId);
        Map<Object, Object> allPositions = groupReadPositionRedisDAO.getAllReadPositions(groupId);

        // 3. 计算该消息的可见成员集合（排除发送者自己）
        Set<Long> visibleUserIds = getVisibleUserIds(message, allMembers);
        visibleUserIds.remove(message.getSenderId());

        // 4. 只返回在可见范围内且已读位置 >= messageId 的用户
        List<Long> readUserIds = new ArrayList<>();
        allPositions.forEach((k, v) -> {
            Long uid = Long.valueOf(k.toString());
            if (visibleUserIds.contains(uid) && Long.parseLong(v.toString()) >= messageId) {
                readUserIds.add(uid);
            }
        });
        return readUserIds;
    }

    @Override
    public List<ImGroupMessageDO> getGroupMessageList(Long userId, ImGroupMessageListReqVO reqVO) {
        // 1. 校验用户在群中
        ImGroupMemberDO member = validateMemberInGroup(reqVO.getGroupId(), userId);

        // 2. 查询历史消息（仅入群之后）
        // TODO @AI：应该 mybatis 里就进行过滤！然后 joinTime 肯定不会是空的！！！
        List<ImGroupMessageDO> messages = groupMessageMapper.selectHistoryList(
                reqVO.getGroupId(), reqVO.getMaxId(), reqVO.getLimit());
        if (member.getJoinTime() == null) {
            return messages;
        }
        return messages.stream()
                .filter(msg -> !msg.getSendTime().isBefore(member.getJoinTime()))
                .toList();
    }

    // ========== 异步 WebSocket 推送 ==========

    /**
     * 发送群聊消息 WebSocket 事件
     */
    @Async
    public void sendGroupMessageEvent(ImGroupMessageDO message) {
        ImGroupMessageDTO dto = ImGroupMessageDTO.ofSend(message);
        // 广播给群内可见成员（含发送方自己，支持多端同步）
        // TODO @AI：getGroupMemberListByGroupId()；默认过滤有效的；
        List<ImGroupMemberDO> members = groupMemberService.selectByGroupId(message.getGroupId());
        List<Long> receiverList = message.getReceiverUserIds();
        for (ImGroupMemberDO member : members) {
            if (CommonStatusEnum.DISABLE.getStatus().equals(member.getStatus())) {
                continue;
            }
            // TODO @AI：这里是不是不用过滤啊？因为一般 send 的时候，肯定带了呀。按照群友就好了；
            if (CollUtil.isNotEmpty(receiverList) && !receiverList.contains(member.getUserId())
                    && ObjUtil.notEqual(member.getUserId(), message.getSenderId())) {
                continue;
            }
            webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), member.getUserId(),
                    ImGroupMessageDTO.TYPE, dto);
        }
    }

    // TODO @AI：命名最后都是 pre 和 new 里面最好都有 messageId 的标识；包括上面的地方，更好理解吧！
    /**
     * 发送已读 + 刷新群回执 WebSocket 事件
     *
     * @param userId     当前用户编号
     * @param groupId    群编号
     * @param prevMaxId  上次已读位置
     * @param newMaxId   本次已读位置
     */
    @Async
    public void readGroupMessageEvent(Long userId, Long groupId, Long prevMaxId, Long newMaxId) {
        // 1. 发送 READ 事件给自己的其他终端（多端同步）
        ImGroupMessageDTO readDto = ImGroupMessageDTO.ofRead(userId, groupId, newMaxId);
        webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), userId,
                ImGroupMessageDTO.TYPE, readDto);

        // 2. 刷新 (prevMaxId, newMaxId] 区间内的待回执消息
        // TODO @AI：selectListByXXX；注意，prevMaxId 可能为空的情况；
        List<ImGroupMessageDO> pendingMessages = groupMessageMapper.selectPendingReceiptMessagesInRange(
                groupId, prevMaxId, newMaxId);
        if (CollUtil.isEmpty(pendingMessages)) {
            return;
        }
        // TODO @AI：是不是要过滤 status ？因为可能退出了。
        List<ImGroupMemberDO> allMembers = groupMemberService.selectByGroupId(groupId);
        // TODO @AI：变量也改下；
        Map<Object, Object> allPositions = groupReadPositionRedisDAO.getAllReadPositions(groupId);
        for (ImGroupMessageDO message : pendingMessages) {
            // 2.1 计算该消息的可见成员（排除发送者自己）
            Set<Long> visibleUserIds = getVisibleUserIds(message, allMembers);
            visibleUserIds.remove(message.getSenderId());
            // TODO @AI：CollUtil 来判断；
            if (visibleUserIds.isEmpty()) {
                continue;
            }
            // 2.2 统计可见成员中的已读人数
            int readCount = 0;
            for (Long uid : visibleUserIds) {
                // TODO @AI：根据返回结果，进行简化；应该都是 Long、Long；
                Object val = allPositions.get(uid.toString());
                if (val != null && Long.parseLong(val.toString()) >= message.getId()) {
                    readCount++;
                }
            }
            // 2.3 全部已读 → 标记回执完成
            boolean allRead = readCount >= visibleUserIds.size();
            Integer newReceiptStatus = allRead
                    ? ImGroupMessageReceiptStatusEnum.DONE.getStatus()
                    : ImGroupMessageReceiptStatusEnum.PENDING.getStatus();
            if (allRead) {
                groupMessageMapper.updateById(new ImGroupMessageDO().setId(message.getId())
                        .setReceiptStatus(newReceiptStatus));
            }
            // 2.4 发送 RECEIPT 事件给消息发送方（只有 ta 关心已读进度）
            // TODO @AI：检查下别的 Dto = 的地方，都最好改成 DTO = ；仅限 im 模块；
            ImGroupMessageDTO receiptDTO = ImGroupMessageDTO.ofReceipt(
                    message.getId(), groupId, readCount, newReceiptStatus);
            webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), message.getSenderId(),
                    ImGroupMessageDTO.TYPE, receiptDTO);
        }
    }

    // ========== 私有工具方法 ==========

    // TODO @AI：抽到对应的 GroupMemberService 中，供其他地方复用
    /**
     * 校验用户是否为群的有效成员
     *
     * @param groupId 群编号
     * @param userId  用户编号
     * @return 群成员记录
     */
    private ImGroupMemberDO validateMemberInGroup(Long groupId, Long userId) {
        ImGroupMemberDO member = groupMemberService.getGroupMember(groupId, userId);
        if (member == null || CommonStatusEnum.DISABLE.getStatus().equals(member.getStatus())) {
            throw exception(GROUP_MEMBER_NOT_IN_GROUP);
        }
        return member;
    }

    /**
     * 判断一条群消息对某个群成员是否可见
     * <p>
     * 规则：
     * 1. 入群时间晚于消息发送时间 → 不可见
     * 2. 已退群且退群时间早于消息发送时间 → 不可见
     * 3. 存在定向接收列表且当前用户既不在列表中也不是发送者 → 不可见
     */
    private boolean isMessageVisible(ImGroupMessageDO msg, ImGroupMemberDO member, Long userId) {
        if (member == null) {
            return false;
        }
        if (member.getJoinTime() != null && msg.getSendTime().isBefore(member.getJoinTime())) {
            return false;
        }
        if (CommonStatusEnum.DISABLE.getStatus().equals(member.getStatus())
                && member.getQuitTime() != null && msg.getSendTime().isAfter(member.getQuitTime())) {
            return false;
        }
        if (CollUtil.isNotEmpty(msg.getReceiverUserIds())
                && !msg.getReceiverUserIds().contains(userId)
                && ObjUtil.notEqual(msg.getSenderId(), userId)) {
            return false;
        }
        return true;
    }

    /**
     * 计算一条群消息的可见成员集合（含发送者）
     */
    private Set<Long> getVisibleUserIds(ImGroupMessageDO msg, List<ImGroupMemberDO> allMembers) {
        Set<Long> visible = new HashSet<>();
        // TODO @AI：        CollectionUtils.converset
        for (ImGroupMemberDO member : allMembers) {
            if (isMessageVisible(msg, member, member.getUserId())) {
                visible.add(member.getUserId());
            }
        }
        return visible;
    }

    private ImGroupMessageServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
