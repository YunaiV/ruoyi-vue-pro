package cn.iocoder.yudao.module.im.service.rtc;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallInviteMoreReqVO;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallInviteReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcCallDO;
import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcParticipantDO;
import cn.iocoder.yudao.module.im.dal.mysql.rtc.ImRtcCallMapper;
import cn.iocoder.yudao.module.im.dal.mysql.rtc.ImRtcParticipantMapper;
import cn.iocoder.yudao.module.im.dal.redis.rtc.ImRtcCallLockRedisDAO;
import cn.iocoder.yudao.module.im.enums.ImConversationTypeEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.enums.rtc.ImRtcCallEndReasonEnum;
import cn.iocoder.yudao.module.im.enums.rtc.ImRtcParticipantRoleEnum;
import cn.iocoder.yudao.module.im.enums.rtc.ImRtcParticipantStatusEnum;
import cn.iocoder.yudao.module.im.enums.rtc.ImRtcCallStatusEnum;
import cn.iocoder.yudao.module.im.framework.config.ImProperties;
import cn.iocoder.yudao.module.im.framework.rtc.core.LiveKitClient;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import cn.iocoder.yudao.module.im.service.group.ImGroupMemberService;
import cn.iocoder.yudao.module.im.service.message.ImGroupMessageService;
import cn.iocoder.yudao.module.im.service.message.ImPrivateMessageService;
import cn.iocoder.yudao.module.im.service.message.dto.ImGroupMessageSendDTO;
import cn.iocoder.yudao.module.im.service.message.dto.ImPrivateMessageSendDTO;
import cn.iocoder.yudao.module.im.framework.rtc.core.LiveKitWebhookEventDTO;
import cn.iocoder.yudao.module.im.service.websocket.dto.notification.rtc.*;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImPrivateMessageDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;

/**
 * IM 实时通话 Service 实现
 * <p>
 * 存储模型：DB 单一存储（im_rtc_call 主表 + im_rtc_participant 明细表）
 * <p>
 * 并发幂等：同好友对 / 同群活跃唯一性走 {@link ImRtcCallLockRedisDAO} 分布式锁 + 锁内 SELECT 兜底；webhook 兜底走条件 UPDATE；
 * <p>
 * 推送通道分流：
 *   1601 RTC_CALL（INVITING / JOINED / REJECTED / NO_ANSWER / LEFT 子类型）→ {@link ImWebSocketService#sendPrivateMessageAsync} 仅推参与方；
 *   1602 / 1603 PARTICIPANT_CONNECTED / DISCONNECTED → {@link ImWebSocketService} 推参与方 + 群通话场景广播全群；
 *   1610 RTC_CALL_START + 1611 RTC_CALL_END → {@link ImPrivateMessageService} / {@link ImGroupMessageService} 入消息流当聊天 tip
 *   （START 仅群通话；两者分别在 invite / cancel(leave) 事务里 INSERT，自增 id 自然保证顺序）
 * <p>
 * 职责边界：媒体协商完全交给 LiveKit；后端只做会话状态机、Token 签发、来电信令推送、通话历史落消息流；房内媒体流变化交给 LiveKit 客户端事件（TrackSubscribed 等），后端不重复推
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class ImRtcCallServiceImpl implements ImRtcCallService {

    @Resource
    private ImRtcCallMapper rtcCallMapper;
    // TODO @AI：可以搞个 ImRtcParticipantService，从而简化这个类的大小么？
    @Resource
    private ImRtcParticipantMapper rtcParticipantMapper;
    @Resource
    private ImRtcCallLockRedisDAO rtcCallLockRedisDAO;

    @Resource
    private ImGroupMemberService groupMemberService;
    @Resource
    private ImFriendService friendService;
    @Resource
    private ImWebSocketService webSocketService;
    @Resource
    private ImPrivateMessageService privateMessageService;
    @Resource
    private ImGroupMessageService groupMessageService;

    @Resource
    private LiveKitClient liveKitClient;

    @Resource
    private AdminUserApi adminUserApi;

    @Resource
    private ImProperties imProperties;

    // ========== 业务接口 ==========

    @Override
    @SneakyThrows
    public ImRtcCallDO inviteCall(Long userId, ImRtcCallInviteReqVO reqVO) {
        ensureEnabled();
        // 1. 校验入参与场景
        validateInviteScene(userId, reqVO);
        // TODO @AI：2 这个注释，有点复杂。略微重复了。应该就是加锁 + 所内逻辑。为什么加锁：xxxx
        // 2. 包锁：同好友 / 同群串行；锁内 SELECT 命中已有活跃通话改走加入分支，否则 INSERT 新通话
        if (ImConversationTypeEnum.isGroup(reqVO.getScene())) {
            return rtcCallLockRedisDAO.lockGroup(reqVO.getGroupId(), () -> inviteCall0(userId, reqVO));
        }
        return rtcCallLockRedisDAO.lockPrivate(userId, reqVO.getPeerUserId(), () -> inviteCall0(userId, reqVO));
    }

    /**
     * invite 锁内主体；SELECT 已有活跃通话 → 直接抛错（群走 joinCall，私聊用户已是参与者）；否则 INSERT 新通话
     */
    private ImRtcCallDO inviteCall0(Long userId, ImRtcCallInviteReqVO reqVO) {
        // TODO @AI：是不是可以改成：私聊的检测、群聊的检测；通用的检测；现在 if else 有点混乱。
        // 1.1 已有活跃通话：群通话提示走加入按钮；私聊一定是当前用户已在通话中（自身忙线）
        ImRtcCallDO active = ImConversationTypeEnum.isGroup(reqVO.getScene())
                // TODO @AI：selectLastOneByXXX；然后 basemapperx 也改成这样的 selectLastOne；这样整体风格对齐；
                ? rtcCallMapper.selectLastByGroupIdAndStatusIn(reqVO.getGroupId(), ImRtcCallStatusEnum.ACTIVE_STATUSES)
                : getActivePrivateCallByPair(userId, reqVO.getPeerUserId());
        if (active != null) {
            throw exception(ImConversationTypeEnum.isGroup(reqVO.getScene())
                    ? RTC_GROUP_CALL_ACTIVE : RTC_SELF_BUSY);
        }
        // 1.2 自身忙线：当前用户已在另一通话；私聊场景再额外检查对端 TODO @AI：是不是要拆成 2 段注释。就是“私聊场景再额外检查对端”下面的“ImConversationTypeEnum.isPrivate(reqVO.getScene())”
        ImRtcParticipantDO selfActive = rtcParticipantMapper.selectLastByUserIdAndStatus(userId, ImRtcParticipantStatusEnum.ACTIVE_STATUSES);
        if (selfActive != null) {
            throw exception(RTC_SELF_BUSY);
        }
        if (ImConversationTypeEnum.isPrivate(reqVO.getScene())) {
            ImRtcParticipantDO peerActive = rtcParticipantMapper.selectLastByUserIdAndStatus(reqVO.getPeerUserId(), ImRtcParticipantStatusEnum.ACTIVE_STATUSES);
            if (peerActive != null) {
                throw exception(RTC_PEER_BUSY);
            }
        }

        // 2. 新建通话
        return doInsertNewCall(userId, reqVO);
    }

    // TODO @AI：改成 createCall 方法；
    // TODO @AI：希望逻辑是：1 构造参数，2.1 插入主表、2.2 插入参与表；3. 推送通知；
    private ImRtcCallDO doInsertNewCall(Long inviterId, ImRtcCallInviteReqVO reqVO) {
        // 1. 派生 callId + roomName
        String callId = IdUtil.fastSimpleUUID();
        String roomName = ImRtcCallDO.ROOM_NAME_PREFIX + callId;
        LocalDateTime now = LocalDateTime.now();
        // 2. 构建被邀请池（私聊为 peerUserId 单元素；群聊取 invitee 子集 / 全员）
        Set<Long> invitees = resolveInvitees(reqVO, inviterId);

        // 3. INSERT 主表；群聊发起人即时 JOINED 但通话仍处 CREATED，等首个非发起人接通才切 RUNNING
        ImRtcCallDO call = new ImRtcCallDO()
                .setCallId(callId).setRoomName(roomName)
                .setConversationType(reqVO.getScene()).setMediaType(reqVO.getMediaType())
                .setInviterUserId(inviterId).setGroupId(reqVO.getGroupId())
                .setStatus(ImRtcCallStatusEnum.CREATED.getStatus())
                .setStartTime(now);
        rtcCallMapper.insert(call);
        // TODO @AI：是不是 4 和 5，改成 批量插入；
        // 4. INSERT 发起人 participant
        rtcParticipantMapper.insert(new ImRtcParticipantDO()
                .setCallId(callId).setUserId(inviterId)
                .setRole(ImRtcParticipantRoleEnum.INVITER.getRole())
                .setStatus(ImRtcParticipantStatusEnum.JOINED.getStatus())
                .setInviteTime(now).setAcceptTime(now));
        // 5. INSERT 被邀请人 participants
        for (Long inviteeId : invitees) {
            rtcParticipantMapper.insert(new ImRtcParticipantDO()
                    .setCallId(callId).setUserId(inviteeId)
                    .setRole(ImRtcParticipantRoleEnum.INVITEE.getRole())
                    .setStatus(ImRtcParticipantStatusEnum.INVITING.getStatus())
                    .setInviteTime(now));
        }
        // 6. 推送 RTC_CALL(INVITE) 给被邀请人；群通话再向全群广播 RTC_CALL_START 入聊天流
        AdminUserRespDTO inviterUser = adminUserApi.getUser(inviterId);
        for (Long inviteeId : invitees) {
            pushCallInviteNotification(call, inviterUser, inviteeId);
        }
        if (ImConversationTypeEnum.isGroup(reqVO.getScene())) {
            pushCallStartNotification(call, inviterUser);
        }
        return call;
    }

    @Override
    public ImRtcCallDO joinCall(Long userId, String roomName) {
        ensureEnabled();
        // 1.1 校验通话存在且活跃
        ImRtcCallDO call = requireActiveCall(roomName);
        // 1.2 仅群通话支持「旁观者加入」
        if (!ImConversationTypeEnum.isGroup(call.getConversationType())) {
            throw exception(RTC_GROUP_REQUIRED);
        }

        // 2. TODO 把 joinExistingCall 拿进来？避免方法太多太碎。
        return joinExistingCall(call, userId);
    }

    /**
     * 命中已有活跃通话；当前用户加入或重新进入；不再触发 INVITE / START 广播
     */
    private ImRtcCallDO joinExistingCall(ImRtcCallDO active, Long userId) {
        ImRtcParticipantDO existing = rtcParticipantMapper.selectByCallIdAndUserId(active.getCallId(), userId);
        LocalDateTime now = LocalDateTime.now();
        if (existing != null) {
            // 非 JOINED 状态切回 JOINED + accept_time；INVITER / INVITEE 重连 / 旁观点胶囊条都走这条
            if (!ImRtcParticipantStatusEnum.isJoined(existing.getStatus())) {
                rtcParticipantMapper.updateById(new ImRtcParticipantDO()
                        .setId(existing.getId())
                        .setStatus(ImRtcParticipantStatusEnum.JOINED.getStatus())
                        .setAcceptTime(now));
            }
        } else {
            // 旁观者主动加入：INSERT role=ACTIVE_JOIN
            rtcParticipantMapper.insert(new ImRtcParticipantDO()
                    .setCallId(active.getCallId()).setUserId(userId)
                    .setRole(ImRtcParticipantRoleEnum.ACTIVE_JOIN.getRole())
                    .setStatus(ImRtcParticipantStatusEnum.JOINED.getStatus())
                    .setInviteTime(now).setAcceptTime(now));
        }
        // 主表 CREATED → RUNNING（首次有非发起人加入）
        maybeMarkOngoing(active, userId, now);
        return active;
    }

    @Override
    public void inviteMoreCall(Long userId, ImRtcCallInviteMoreReqVO reqVO) {
        ensureEnabled();
        ImRtcCallDO call = requireActiveCall(reqVO.getRoomName());
        // 仅群通话支持追加邀请
        if (!ImConversationTypeEnum.isGroup(call.getConversationType())) {
            throw exception(RTC_GROUP_REQUIRED);
        }
        // 操作者必须是已在房间的参与者
        ImRtcParticipantDO operator = rtcParticipantMapper.selectByCallIdAndUserId(call.getCallId(), userId);
        if (operator == null || !ImRtcParticipantStatusEnum.isJoined(operator.getStatus())) {
            throw exception(RTC_NOT_PARTICIPANT);
        }
        // 校验被邀请人必须是该群活跃成员；且未在通话池
        Set<Long> validMemberIds = new LinkedHashSet<>(
                groupMemberService.getActiveGroupMemberUserIdsByGroupId(call.getGroupId()));
        Set<Long> existingUserIds = rtcParticipantMapper.selectListByCallId(call.getCallId()).stream()
                .map(ImRtcParticipantDO::getUserId)
                .collect(Collectors.toSet());
        List<Long> incoming = reqVO.getInviteeIds().stream()
                .filter(id -> validMemberIds.contains(id) && !existingUserIds.contains(id))
                .toList();
        if (CollUtil.isEmpty(incoming)) {
            return;
        }
        // INSERT 新邀请人 + 推 INVITE
        LocalDateTime now = LocalDateTime.now();
        for (Long inviteeId : incoming) {
            rtcParticipantMapper.insert(new ImRtcParticipantDO()
                    .setCallId(call.getCallId()).setUserId(inviteeId)
                    .setRole(ImRtcParticipantRoleEnum.INVITEE.getRole())
                    .setStatus(ImRtcParticipantStatusEnum.INVITING.getStatus())
                    .setInviteTime(now));
        }
        AdminUserRespDTO inviter = adminUserApi.getUser(userId);
        for (Long inviteeId : incoming) {
            pushCallInviteNotification(call, inviter, inviteeId);
        }
    }

    @Override
    public ImRtcCallDO acceptCall(Long userId, String roomName) {
        ensureEnabled();
        ImRtcCallDO call = requireActiveCall(roomName);
        ImRtcParticipantDO participant = rtcParticipantMapper.selectByCallIdAndUserId(call.getCallId(), userId);
        if (participant == null) {
            throw exception(RTC_NOT_PARTICIPANT);
        }
        // 已 JOINED 视为重复 accept，幂等返回
        LocalDateTime now = LocalDateTime.now();
        if (!ImRtcParticipantStatusEnum.isJoined(participant.getStatus())) {
            // 仅 INVITING → JOINED；其它状态拒
            if (!ImRtcParticipantStatusEnum.isInviting(participant.getStatus())) {
                throw exception(RTC_SESSION_NOT_EXISTS);
            }
            int updated = rtcParticipantMapper.updateByIdAndStatus(participant.getId(),
                    ImRtcParticipantStatusEnum.INVITING.getStatus(),
                    new ImRtcParticipantDO()
                            .setId(participant.getId())
                            .setStatus(ImRtcParticipantStatusEnum.JOINED.getStatus())
                            .setAcceptTime(now));
            if (updated == 0) {
                throw exception(RTC_SESSION_NOT_EXISTS);
            }
        }
        // 主表 CREATED → RUNNING（首次有非发起人接通）
        maybeMarkOngoing(call, userId, now);
        return call;
    }

    @Override
    public void rejectCall(Long userId, String roomName) {
        ensureEnabled();
        ImRtcCallDO call = requireActiveCall(roomName);
        ImRtcParticipantDO participant = rtcParticipantMapper.selectByCallIdAndUserId(call.getCallId(), userId);
        if (participant == null) {
            throw exception(RTC_NOT_PARTICIPANT);
        }
        // 仅 INVITING 状态可拒
        if (!ImRtcParticipantStatusEnum.isInviting(participant.getStatus())) {
            throw exception(RTC_SESSION_NOT_EXISTS);
        }
        int updated = rtcParticipantMapper.updateByIdAndStatus(participant.getId(),
                ImRtcParticipantStatusEnum.INVITING.getStatus(),
                new ImRtcParticipantDO()
                        .setId(participant.getId())
                        .setStatus(ImRtcParticipantStatusEnum.REJECTED.getStatus())
                        .setLeaveTime(LocalDateTime.now()));
        if (updated == 0) {
            return;
        }
        // 群通话拒绝：仅推 RTC_CALL(REJECT) 给主叫；不影响通话整体
        if (ImConversationTypeEnum.isGroup(call.getConversationType())) {
            pushCallRejectNotification(call, userId);
            return;
        }
        // 私聊拒绝：走 endSession（推 RTC_CALL_END(reason=REJECT)）
        endSession(call, userId, ImRtcCallEndReasonEnum.REJECT);
    }

    @Override
    public void cancelCall(Long userId, String roomName) {
        ensureEnabled();
        ImRtcCallDO call = requireActiveCall(roomName);
        if (!Objects.equals(call.getInviterUserId(), userId)) {
            throw exception(RTC_NOT_PARTICIPANT);
        }
        // 仅 CREATED 状态可取消（RUNNING 应走 leave）
        if (!ImRtcCallStatusEnum.isCreated(call.getStatus())) {
            throw exception(RTC_SESSION_NOT_EXISTS);
        }
        endSession(call, userId, ImRtcCallEndReasonEnum.CANCEL);
    }

    @Override
    public void leaveCall(Long userId, String roomName) {
        ensureEnabled();
        ImRtcCallDO call = requireActiveCall(roomName);
        ImRtcParticipantDO participant = rtcParticipantMapper.selectByCallIdAndUserId(call.getCallId(), userId);
        if (participant == null) {
            throw exception(RTC_NOT_PARTICIPANT);
        }
        LocalDateTime now = LocalDateTime.now();
        rtcParticipantMapper.updateByIdAndStatus(participant.getId(), participant.getStatus(),
                new ImRtcParticipantDO()
                        .setId(participant.getId())
                        .setStatus(ImRtcParticipantStatusEnum.LEFT.getStatus())
                        .setLeaveTime(now));
        // 关房条件：私聊任一方离开 = 关；群通话仅在「无人在房 + 无人响铃」时关
        if (ImConversationTypeEnum.isPrivate(call.getConversationType())
                || shouldCloseGroupRoom(call.getCallId())) {
            endSession(call, userId, ImRtcCallEndReasonEnum.HANGUP);
        }
        // 群通话单人离开：LiveKit `ParticipantDisconnected` 自动通知房内成员；业务后端不另外推
    }

    /**
     * 群通话是否应该关闭：房内没有 JOINED 就关
     * <p>
     * 群通话至少需要 1 个 JOINED 在房才有意义；如果只剩 INVITING（响铃中）的，接通后也只是 1 人独房，无价值
     * <p>
     * 关房后 endSession 会把残留 INVITING 批量改 NO_ANSWER 并推 RTC_CALL_END，响铃端 UI 自动收敛
     */
    private boolean shouldCloseGroupRoom(String callId) {
        List<ImRtcParticipantDO> joined = rtcParticipantMapper.selectListByCallIdAndStatus(callId,
                ImRtcParticipantStatusEnum.JOINED.getStatus());
        return CollUtil.isEmpty(joined);
    }

    /**
     * 查询两人共同所在的活跃私聊通话；由应用层 Redisson 锁串行化，最多一个
     * <p>
     * 拆成 3 段简单查询：拿 A 的活跃 participant → 拿主表判私聊未结束 → 看 B 是否在同 call 活跃
     */
    private ImRtcCallDO getActivePrivateCallByPair(Long userIdA, Long userIdB) {
        ImRtcParticipantDO aPart = rtcParticipantMapper.selectLastByUserIdAndStatus(userIdA, ImRtcParticipantStatusEnum.ACTIVE_STATUSES);
        if (aPart == null) {
            return null;
        }
        ImRtcCallDO call = rtcCallMapper.selectByCallId(aPart.getCallId());
        if (call == null
                || !ImConversationTypeEnum.isPrivate(call.getConversationType())
                || ImRtcCallStatusEnum.isEnded(call.getStatus())) {
            return null;
        }
        ImRtcParticipantDO bPart = rtcParticipantMapper.selectByCallIdAndUserId(call.getCallId(), userIdB);
        if (bPart == null
                || !ImRtcParticipantStatusEnum.ACTIVE_STATUSES.contains(bPart.getStatus())) {
            return null;
        }
        return call;
    }

    @Override
    public ImRtcCallDO refreshCallToken(Long userId, String roomName) {
        ensureEnabled();
        ImRtcCallDO call = requireActiveCall(roomName);
        ImRtcParticipantDO participant = rtcParticipantMapper.selectByCallIdAndUserId(call.getCallId(), userId);
        if (participant == null) {
            throw exception(RTC_NOT_PARTICIPANT);
        }
        return call;
    }

    @Override
    public ImRtcCallDO getActiveCall(Long userId, Long groupId) {
        ensureEnabled();
        // 1. 鉴权：仅群活跃成员能查（走单行 SQL，不依赖成员列表缓存）
        groupMemberService.validateMemberInGroup(groupId, userId);
        // 2. 查询活跃通话
        return rtcCallMapper.selectLastByGroupIdAndStatusIn(groupId, ImRtcCallStatusEnum.ACTIVE_STATUSES);
    }

    @Override
    public List<ImRtcParticipantDO> getCallParticipants(String callId) {
        return rtcParticipantMapper.selectListByCallId(callId);
    }

    @Override
    public String signCallToken(Long userId, String roomName) {
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        String displayName = user == null ? null : user.getNickname();
        return signToken(userId, displayName, roomName);
    }

    @Override
    // TODO @AI：这个方法的编写，需要兼容 JDK8
    public void handleLiveKitEvent(LiveKitWebhookEventDTO event) {
        if (event == null || event.getEvent() == null) {
            return;
        }
        switch (event.getEvent()) {
            case LiveKitWebhookEventDTO.EVENT_PARTICIPANT_JOINED -> handleParticipantJoined(event);
            case LiveKitWebhookEventDTO.EVENT_PARTICIPANT_LEFT -> handleParticipantLeft(event);
            case LiveKitWebhookEventDTO.EVENT_ROOM_FINISHED -> handleRoomFinished(event);
            default -> {
                /* 其它事件忽略；track_published 等业务态由 LiveKit 自身分发驱动 */
            }
        }
    }

    /**
     * 处理 LiveKit 「成员加入」事件；DB 接通态由 accept 接口写入，此处仅做 1602 转推
     */
    private void handleParticipantJoined(LiveKitWebhookEventDTO event) {
        if (event.getRoom() == null || event.getParticipant() == null) {
            return;
        }
        String roomName = event.getRoom().getName();
        ImRtcCallDO call = rtcCallMapper.selectByRoomName(roomName);
        if (call == null || ImRtcCallStatusEnum.isEnded(call.getStatus())) {
            return;
        }
        Long userId = liveKitClient.parseUserId(event.getParticipant().getIdentity());
        if (userId == null) {
            return;
        }
        pushParticipantConnectedNotification(call, userId);
    }

    /**
     * 处理 LiveKit 「成员离开」事件；正常 leave 接口已经清理过的话条件 UPDATE 自动幂等
     */
    private void handleParticipantLeft(LiveKitWebhookEventDTO event) {
        if (event.getRoom() == null || event.getParticipant() == null) {
            return;
        }
        String roomName = event.getRoom().getName();
        ImRtcCallDO call = rtcCallMapper.selectByRoomName(roomName);
        if (call == null || ImRtcCallStatusEnum.isEnded(call.getStatus())) {
            return;
        }
        Long userId = liveKitClient.parseUserId(event.getParticipant().getIdentity());
        if (userId == null) {
            return;
        }
        ImRtcParticipantDO participant = rtcParticipantMapper.selectByCallIdAndUserId(call.getCallId(), userId);
        if (participant == null || !ImRtcParticipantStatusEnum.isJoined(participant.getStatus())) {
            return;
        }
        int updated = rtcParticipantMapper.updateByIdAndStatus(participant.getId(),
                ImRtcParticipantStatusEnum.JOINED.getStatus(),
                new ImRtcParticipantDO()
                        .setId(participant.getId())
                        .setStatus(ImRtcParticipantStatusEnum.LEFT.getStatus())
                        .setLeaveTime(LocalDateTime.now()));
        if (updated == 0) {
            return;
        }
        log.info("[handleParticipantLeft][roomName={} userId={} 由 LiveKit Webhook 兜底]", roomName, userId);
        // 推 1603 通知参与方 / 全群参与方
        pushParticipantDisconnectedNotification(call, userId);
        // 关房条件与 leave 一致；群通话仅在「无人在房 + 无人响铃」时关
        if (ImConversationTypeEnum.isPrivate(call.getConversationType())
                || shouldCloseGroupRoom(call.getCallId())) {
            endSession(call, userId, ImRtcCallEndReasonEnum.HANGUP);
        }
    }

    /**
     * 处理 LiveKit 「房间结束」事件；兜底把 call 推到 ENDED
     */
    private void handleRoomFinished(LiveKitWebhookEventDTO event) {
        if (event.getRoom() == null) {
            return;
        }
        String roomName = event.getRoom().getName();
        ImRtcCallDO call = rtcCallMapper.selectByRoomName(roomName);
        if (call == null || ImRtcCallStatusEnum.isEnded(call.getStatus())) {
            return;
        }
        log.info("[handleRoomFinished][roomName={} 由 LiveKit Webhook 兜底]", roomName);
        endSession(call, null, ImRtcCallEndReasonEnum.HANGUP);
    }

    @Override
    public int cleanupZombieCalls(int thresholdMinutes) {
        // 阈值由调用方（Job）保证 > 0；低于 1 分钟会误杀刚发起的合理零人态
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(thresholdMinutes);
        List<ImRtcCallDO> candidates = rtcCallMapper.selectListByStatusInAndStartTimeBefore(
                ImRtcCallStatusEnum.ACTIVE_STATUSES, threshold);
        if (CollUtil.isEmpty(candidates)) {
            return 0;
        }

        // 2. 逐个查 LiveKit 房间真实 participant 数
        int cleaned = 0;
        for (ImRtcCallDO call : candidates) {
            int count;
            try {
                count = liveKitClient.listParticipants(call.getRoomName());
            } catch (Exception e) {
                log.warn("[cleanupZombieCalls][查询 LiveKit 失败 callId={} roomName={}]",
                        call.getCallId(), call.getRoomName(), e);
                continue;
            }
            if (count != 0) {
                continue;
            }
            log.info("[cleanupZombieCalls][清理僵尸通话 callId={} roomName={}]",
                    call.getCallId(), call.getRoomName());
            endSession(call, null, ImRtcCallEndReasonEnum.HANGUP);
            cleaned++;
        }
        return cleaned;
    }

    // ========== 内部辅助 ==========

    /**
     * 校验通话功能开关
     */
    private void ensureEnabled() {
        if (!imProperties.getRtc().isEnabled()) {
            throw exception(RTC_NOT_ENABLED);
        }
    }

    // TODO @AI：validateXXX
    /**
     * 取活跃通话；不存在 / 已 ENDED 直接抛
     */
    private ImRtcCallDO requireActiveCall(String roomName) {
        ImRtcCallDO call = rtcCallMapper.selectByRoomName(roomName);
        if (call == null || ImRtcCallStatusEnum.isEnded(call.getStatus())) {
            throw exception(RTC_SESSION_NOT_EXISTS);
        }
        return call;
    }

    /**
     * 校验 invite 入参；按场景区分必填字段；私聊补好友 / 黑名单校验，群聊补群成员校验
     */
    private void validateInviteScene(Long userId, ImRtcCallInviteReqVO reqVO) {
        Integer scene = reqVO.getScene();
        if (ImConversationTypeEnum.isPrivate(scene)) {
            if (reqVO.getPeerUserId() == null) {
                throw exception(RTC_PRIVATE_INVITEE_REQUIRED);
            }
            if (Objects.equals(userId, reqVO.getPeerUserId())) {
                throw exception(RTC_INVITE_SELF);
            }
            // TODO DONE @AI：抽到 ImFriendService#validateFriend；
            friendService.validateFriend(userId, reqVO.getPeerUserId());
            return;
        }
        if (ImConversationTypeEnum.isGroup(scene)) {
            if (reqVO.getGroupId() == null) {
                throw exception(RTC_GROUP_REQUIRED);
            }
            List<Long> activeMemberIds = groupMemberService
                    .getActiveGroupMemberUserIdsByGroupId(reqVO.getGroupId());
            if (!activeMemberIds.contains(userId)) {
                throw exception(GROUP_MEMBER_NOT_IN_GROUP);
            }
            return;
        }
        throw exception(RTC_PRIVATE_INVITEE_REQUIRED);
    }

    /**
     * 解析被邀请池：私聊为 peerUserId 单元素；群聊优先用前端选中的子集，否则取群活跃成员；超量截断
     * <p>
     * 群聊场景：被邀请人必须是该群活跃成员；防止恶意客户端塞任意 userId 进群房间
     */
    private Set<Long> resolveInvitees(ImRtcCallInviteReqVO reqVO, Long inviterId) {
        if (ImConversationTypeEnum.isPrivate(reqVO.getScene())) {
            return CollUtil.newLinkedHashSet(reqVO.getPeerUserId());
        }
        List<Long> activeMemberIds = groupMemberService
                .getActiveGroupMemberUserIdsByGroupId(reqVO.getGroupId());
        Set<Long> initial = new LinkedHashSet<>();
        if (CollUtil.isNotEmpty(reqVO.getInviteeIds())) {
            Set<Long> activeMemberSet = new HashSet<>(activeMemberIds);
            for (Long inviteeId : reqVO.getInviteeIds()) {
                if (!activeMemberSet.contains(inviteeId)) {
                    throw exception(GROUP_MEMBER_NOT_IN_GROUP);
                }
                initial.add(inviteeId);
            }
        } else {
            initial.addAll(activeMemberIds);
        }
        // 不论来源，发起人本人不进被邀请池
        initial.remove(inviterId);
        // 超出最大同时在房成员数；截断为 max-1 个，避免邀请整个 500 人大群
        int max = imProperties.getRtc().getGroupMaxParticipants();
        if (initial.size() + 1 > max) {
            return initial.stream().limit(max - 1L)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return initial;
    }

    /**
     * 主表 CREATED → RUNNING；仅当首个非发起人加入时推进；条件 UPDATE 保幂等
     * <p>
     * 与 cancel / endSession 并发时 update 可能影响 0 行；reload 看真实状态：
     *   ENDED → 通话已被另一路径终结，抛 RTC_SESSION_NOT_EXISTS 让 accept 失败而不是返回假成功 token
     *   RUNNING → 已被另一接听者抢先推进，幂等同步内存
     */
    private void maybeMarkOngoing(ImRtcCallDO call, Long acceptorId, LocalDateTime now) {
        if (!ImRtcCallStatusEnum.isCreated(call.getStatus())) {
            return;
        }
        if (Objects.equals(call.getInviterUserId(), acceptorId)) {
            return;
        }
        int updated = rtcCallMapper.updateByIdAndStatus(call.getId(),
                ImRtcCallStatusEnum.CREATED.getStatus(),
                new ImRtcCallDO()
                        .setStatus(ImRtcCallStatusEnum.RUNNING.getStatus())
                        .setAcceptTime(now));
        if (updated > 0) {
            // 内存值同步给后续判断
            call.setStatus(ImRtcCallStatusEnum.RUNNING.getStatus());
            call.setAcceptTime(now);
            return;
        }
        // updated == 0：与并发路径竞争失败；reload 看真实终态
        ImRtcCallDO latest = rtcCallMapper.selectById(call.getId());
        if (latest == null || ImRtcCallStatusEnum.isEnded(latest.getStatus())) {
            throw exception(RTC_SESSION_NOT_EXISTS);
        }
        call.setStatus(latest.getStatus());
        call.setAcceptTime(latest.getAcceptTime());
    }

    /**
     * 关闭会话：主表条件 UPDATE 推到 ENDED → 残留 INVITING 批量改 NO_ANSWER → 推 RTC_CALL_END
     */
    private void endSession(ImRtcCallDO call, Long operatorId, ImRtcCallEndReasonEnum reason) {
        // 1.1 更新通话主表为已结束；条件 UPDATE 仅在 status 还活跃时生效
        LocalDateTime now = LocalDateTime.now();
        int updated = rtcCallMapper.updateByIdAndStatusIn(call.getId(), ImRtcCallStatusEnum.ACTIVE_STATUSES,
                new ImRtcCallDO().setStatus(ImRtcCallStatusEnum.ENDED.getStatus())
                        .setEndReason(reason.getReason()).setEndTime(now));
        if (updated == 0) {
            log.info("[endSession][已被另一路径终结，跳过 callId={} roomName={} operator={} reason={}]",
                    call.getCallId(), call.getRoomName(), operatorId, reason);
            return;
        }
        // 1.2 更新参与表为已结束：残留 INVITING 改 NO_ANSWER；残留 JOINED 改 LEFT 并写 leaveTime
        rtcParticipantMapper.updateByCallIdAndStatus(call.getCallId(), ImRtcParticipantStatusEnum.INVITING.getStatus(),
                new ImRtcParticipantDO().setStatus(ImRtcParticipantStatusEnum.NO_ANSWER.getStatus()));
        rtcParticipantMapper.updateByCallIdAndStatus(call.getCallId(), ImRtcParticipantStatusEnum.JOINED.getStatus(),
                new ImRtcParticipantDO().setStatus(ImRtcParticipantStatusEnum.LEFT.getStatus()).setLeaveTime(now));

        // 2. 兜底删除 LiveKit 房间，强制断开异常残留客户端；失败仅记日志，不阻断业务
        try {
            liveKitClient.deleteRoom(call.getRoomName());
        } catch (Exception e) {
            log.warn("[endSession][删除 LiveKit 房间失败 callId={} roomName={} operator={} reason={}]",
                    call.getCallId(), call.getRoomName(), operatorId, reason, e);
        }

        // 3. 推 RTC_CALL_END
        Long durationSeconds = call.getAcceptTime() != null ?
                Duration.between(call.getAcceptTime(), now).getSeconds() : null;
        pushCallEndNotification(call, operatorId, reason, durationSeconds);
        log.info("[endSession][roomName={} operator={} reason={}]", call.getRoomName(), operatorId, reason);
    }


    /**
     * 通话事件的收件人池：私聊为双方参与者，群聊为群活跃成员
     */
    private Collection<Long> getCallAudience(ImRtcCallDO call) {
        if (ImConversationTypeEnum.isGroup(call.getConversationType())) {
            return groupMemberService.getActiveGroupMemberUserIdsByGroupId(call.getGroupId());
        }
        return CollectionUtils.convertSet(
                rtcParticipantMapper.selectListByCallId(call.getCallId()), ImRtcParticipantDO::getUserId);
    }

    // ========== 推送 ==========

    /**
     * RTC_CALL(INVITE)：走 webSocketService 直推到被邀请人
     * <p>
     * 不能走 imPrivateMessageService.send：persistent=false 时 send 仅推 sender 多端，receiver 不感知
     */
    private void pushCallInviteNotification(ImRtcCallDO call, AdminUserRespDTO inviter, Long inviteeId) {
        String token = signToken(inviteeId, inviter == null ? null : inviter.getNickname(), call.getRoomName());
        ImRtcCallNotification payload = ImRtcCallNotification.ofInvite(
                call, inviter, imProperties.getRtc().getLivekitUrl(), token);
        webSocketService.sendPrivateMessageAsync(inviteeId, ImPrivateMessageDTO.ofRtcNotification(
                ImMessageTypeEnum.RTC_CALL.getType(), call.getInviterUserId(), inviteeId, payload));
    }

    /**
     * RTC_CALL(REJECT)：仅群通话场景；走 webSocketService 直推主叫
     */
    private void pushCallRejectNotification(ImRtcCallDO call, Long operatorUserId) {
        AdminUserRespDTO operator = operatorUserId != null ? adminUserApi.getUser(operatorUserId) : null;
        ImRtcCallNotification payload = ImRtcCallNotification.ofReject(call, operatorUserId, operator);
        webSocketService.sendPrivateMessageAsync(call.getInviterUserId(), ImPrivateMessageDTO.ofRtcNotification(
                ImMessageTypeEnum.RTC_CALL.getType(), operatorUserId, call.getInviterUserId(), payload));
    }

    /**
     * 通话参与者加入：LiveKit webhook participant_joined 触发；私聊推双方多端、群聊推全群成员（胶囊条 + 1）
     *
     * @param call    通话主表
     * @param userId  加入的参与者用户编号
     */
    private void pushParticipantConnectedNotification(ImRtcCallDO call, Long userId) {
        pushParticipantNotification(call, ImMessageTypeEnum.RTC_PARTICIPANT_CONNECTED.getType(), userId,
                ImRtcParticipantConnectedNotification.of(call, userId));
    }

    /**
     * 通话参与者离开：LiveKit webhook participant_left 触发；推送范围同 {@link #pushParticipantConnectedNotification}
     *
     * @param call    通话主表
     * @param userId  离开的参与者用户编号
     */
    private void pushParticipantDisconnectedNotification(ImRtcCallDO call, Long userId) {
        pushParticipantNotification(call, ImMessageTypeEnum.RTC_PARTICIPANT_DISCONNECTED.getType(), userId,
                ImRtcParticipantDisconnectedNotification.of(call, userId));
    }

    /**
     * 推送参与者事件的公共骨架；按会话类型决定收件人，单次 batch 推送扇出
     */
    private void pushParticipantNotification(ImRtcCallDO call, Integer type, Long actorUserId, Object payload) {
        Collection<Long> receivers = getCallAudience(call);
        if (CollUtil.isEmpty(receivers)) {
            return;
        }
        ImPrivateMessageDTO dto = ImPrivateMessageDTO.ofRtcNotification(type, actorUserId, null, payload);
        webSocketService.sendPrivateMessageAsync(receivers, dto);
    }

    /**
     * RTC_CALL_START：仅群通话场景；走 imGroupMessageService.send，入 im_group_message 全群广播
     * <p>
     * 与 RTC_CALL_END 两段式配对：START 在本接口（invite）事务里 INSERT，END 在 cancel / leave 接口事务里 INSERT，
     * 自增 id 自然保证聊天流顺序
     */
    private void pushCallStartNotification(ImRtcCallDO call, AdminUserRespDTO inviter) {
        ImRtcCallStartNotification payload = ImRtcCallStartNotification.of(call, inviter);
        ImGroupMessageSendDTO dto = new ImGroupMessageSendDTO()
                .setGroupId(call.getGroupId())
                .setType(ImMessageTypeEnum.RTC_CALL_START.getType())
                .setContent(payload);
        groupMessageService.sendGroupMessage(call.getInviterUserId(), dto);
    }

    /**
     * RTC_CALL_END：私聊走 imPrivateMessageService.send；群通话走 imGroupMessageService.send
     * <p>
     * senderId 始终用通话发起人，让前端按「谁发起通话」决定气泡左右；操作者从 payload.operatorUserId 拿
     */
    private void pushCallEndNotification(ImRtcCallDO call, Long operatorId, ImRtcCallEndReasonEnum reason,
                             Long durationSeconds) {
        AdminUserRespDTO operator = operatorId != null ? adminUserApi.getUser(operatorId) : null;
        ImRtcCallEndNotification payload = ImRtcCallEndNotification.of(call, reason, durationSeconds, operatorId, operator);
        Long senderId = call.getInviterUserId();
        if (ImConversationTypeEnum.isGroup(call.getConversationType())) {
            ImGroupMessageSendDTO dto = new ImGroupMessageSendDTO()
                    .setGroupId(call.getGroupId())
                    .setType(ImMessageTypeEnum.RTC_CALL_END.getType())
                    .setContent(payload);
            groupMessageService.sendGroupMessage(senderId, dto);
            return;
        }
        // 私聊：发起人总是 sender；receiver = 通话另一方
        ImRtcParticipantDO peer = CollUtil.findOne(
                rtcParticipantMapper.selectListByCallId(call.getCallId()),
                p -> !Objects.equals(p.getUserId(), senderId));
        Long receiverId = peer != null ? peer.getUserId() : senderId;
        ImPrivateMessageSendDTO dto = new ImPrivateMessageSendDTO()
                .setReceiverId(receiverId)
                .setType(ImMessageTypeEnum.RTC_CALL_END.getType())
                .setContent(payload);
        privateMessageService.sendPrivateMessage(senderId, dto);
    }

    // ========== Token / VO ==========

    /**
     * 签 LiveKit Token
     */
    private String signToken(Long userId, String displayName, String roomName) {
        return liveKitClient.signJoinToken(liveKitClient.buildIdentity(userId), displayName, roomName);
    }

}
