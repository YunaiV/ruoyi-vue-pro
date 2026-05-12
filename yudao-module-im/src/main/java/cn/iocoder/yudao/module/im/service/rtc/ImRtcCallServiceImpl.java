package cn.iocoder.yudao.module.im.service.rtc;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
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
import cn.iocoder.yudao.module.im.enums.rtc.ImRtcCallStatusEnum;
import cn.iocoder.yudao.module.im.enums.rtc.ImRtcParticipantRoleEnum;
import cn.iocoder.yudao.module.im.enums.rtc.ImRtcParticipantStatusEnum;
import cn.iocoder.yudao.module.im.framework.config.ImProperties;
import cn.iocoder.yudao.module.im.framework.rtc.core.LiveKitClient;
import cn.iocoder.yudao.module.im.framework.rtc.core.LiveKitWebhookEventDTO;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import cn.iocoder.yudao.module.im.service.group.ImGroupMemberService;
import cn.iocoder.yudao.module.im.service.message.ImGroupMessageService;
import cn.iocoder.yudao.module.im.service.message.ImPrivateMessageService;
import cn.iocoder.yudao.module.im.service.message.dto.ImGroupMessageSendDTO;
import cn.iocoder.yudao.module.im.service.message.dto.ImPrivateMessageSendDTO;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImPrivateMessageDTO;
import cn.iocoder.yudao.module.im.service.websocket.dto.notification.rtc.*;
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
        validateEnabled();
        // 1. 校验入参与场景
        validateInviteScene(userId, reqVO);

        // 2. 加锁后跑业务主体；同好友对 / 同群串行，避免并发各开一通的竞态
        if (ImConversationTypeEnum.isGroup(reqVO.getScene())) {
            return rtcCallLockRedisDAO.lockGroup(reqVO.getGroupId(), () -> inviteCall0(userId, reqVO));
        }
        return rtcCallLockRedisDAO.lockPrivate(userId, reqVO.getPeerUserId(), () -> inviteCall0(userId, reqVO));
    }

    /**
     * invite 锁内主体：按场景先做活跃通话 / 对端忙线校验，再做发起人自身忙线校验，最后建通话
     */
    private ImRtcCallDO inviteCall0(Long userId, ImRtcCallInviteReqVO reqVO) {
        // 1.1 按场景检测活跃通话
        if (ImConversationTypeEnum.isGroup(reqVO.getScene())) {
            // 群通话已存在则引导走 joinCall（旁观者加入按钮），不再开新通话
            ImRtcCallDO active = rtcCallMapper.selectLastOneByGroupIdAndStatusIn(
                    reqVO.getGroupId(), ImRtcCallStatusEnum.ACTIVE_STATUSES);
            if (active != null) {
                throw exception(RTC_GROUP_CALL_ACTIVE);
            }
        } else {
            // 私聊已在通话中说明发起人自己已是参与者，当作自身忙线
            ImRtcCallDO active = getActivePrivateCallByPair(userId, reqVO.getPeerUserId());
            if (active != null) {
                throw exception(RTC_SELF_BUSY);
            }
            // 私聊额外检查对端忙线
            ImRtcParticipantDO peerActive = rtcParticipantMapper.selectLastOneByUserIdAndStatus(
                    reqVO.getPeerUserId(), ImRtcParticipantStatusEnum.ACTIVE_STATUSES);
            if (peerActive != null) {
                throw exception(RTC_PEER_BUSY);
            }
        }
        // 1.2 兜底校验：发起人在另一通通话中（跨好友 / 跨群）
        ImRtcParticipantDO selfActive = rtcParticipantMapper.selectLastOneByUserIdAndStatus(
                userId, ImRtcParticipantStatusEnum.ACTIVE_STATUSES);
        if (selfActive != null) {
            throw exception(RTC_SELF_BUSY);
        }

        // 2. 新建通话
        return createCall(userId, reqVO);
    }

    /**
     * 新建通话
     *
     * @param inviterId 发起人编号
     * @param reqVO 发起请求
     * @return 通话主表
     */
    private ImRtcCallDO createCall(Long inviterId, ImRtcCallInviteReqVO reqVO) {
        // 1. 构造参数：room 用 UUID；解析被邀请池
        String room = IdUtil.fastSimpleUUID();
        LocalDateTime now = LocalDateTime.now();
        Set<Long> invitees = resolveInvitees(reqVO, inviterId);

        // 2.1 INSERT 主表；群聊发起人即时 JOINED 但通话仍处 CREATED，等首个非发起人接通才切 RUNNING
        ImRtcCallDO call = new ImRtcCallDO().setRoom(room)
                .setConversationType(reqVO.getScene()).setMediaType(reqVO.getMediaType())
                .setInviterUserId(inviterId).setGroupId(reqVO.getGroupId())
                .setStatus(ImRtcCallStatusEnum.CREATED.getStatus()).setStartTime(now);
        rtcCallMapper.insert(call);
        // 2.2 批量 INSERT 参与表：发起人即时 JOINED，被邀请人 INVITING 等接通
        List<ImRtcParticipantDO> participants = new ArrayList<>(invitees.size() + 1);
        participants.add(new ImRtcParticipantDO().setRoom(room).setUserId(inviterId)
                .setRole(ImRtcParticipantRoleEnum.INVITER.getRole())
                .setStatus(ImRtcParticipantStatusEnum.JOINED.getStatus())
                .setInviteTime(now).setAcceptTime(now));
        for (Long inviteeId : invitees) {
            participants.add(new ImRtcParticipantDO()
                    .setRoom(room).setUserId(inviteeId)
                    .setRole(ImRtcParticipantRoleEnum.INVITEE.getRole())
                    .setStatus(ImRtcParticipantStatusEnum.INVITING.getStatus())
                    .setInviteTime(now));
        }
        rtcParticipantMapper.insertBatch(participants);

        // 3.1 推送通知：RTC_CALL(INVITE) 给每个被邀请人
        AdminUserRespDTO inviterUser = adminUserApi.getUser(inviterId);
        for (Long inviteeId : invitees) {
            pushCallInviteNotification(call, inviterUser, inviteeId);
        }
        // 3.2 群通话再向全群广播 RTC_CALL_START 入聊天流
        if (ImConversationTypeEnum.isGroup(reqVO.getScene())) {
            pushCallStartNotification(call, inviterUser);
        }
        return call;
    }

    @Override
    public ImRtcCallDO joinCall(Long userId, String room) {
        validateEnabled();
        // 1.1 校验通话存在且活跃
        ImRtcCallDO call = validateCallActive(room);
        // 1.2 仅群通话支持「旁观者加入」
        if (!ImConversationTypeEnum.isGroup(call.getConversationType())) {
            throw exception(RTC_GROUP_REQUIRED);
        }

        // 2. 入参与表：已有记录切回 JOINED；不在记录则以 ACTIVE_JOIN 角色 INSERT
        LocalDateTime now = LocalDateTime.now();
        ImRtcParticipantDO existing = rtcParticipantMapper.selectByRoomAndUserId(call.getRoom(), userId);
        if (existing != null) {
            // 非 JOINED 状态切回 JOINED + accept_time；INVITER / INVITEE 重连 / 旁观点胶囊条都走这条
            if (!ImRtcParticipantStatusEnum.isJoined(existing.getStatus())) {
                rtcParticipantMapper.updateById(new ImRtcParticipantDO().setId(existing.getId())
                        .setStatus(ImRtcParticipantStatusEnum.JOINED.getStatus()).setAcceptTime(now));
            }
        } else {
            // 旁观者主动加入：INSERT role=ACTIVE_JOIN
            rtcParticipantMapper.insert(new ImRtcParticipantDO().setRoom(call.getRoom())
                    .setUserId(userId).setRole(ImRtcParticipantRoleEnum.ACTIVE_JOIN.getRole())
                    .setStatus(ImRtcParticipantStatusEnum.JOINED.getStatus()).setInviteTime(now).setAcceptTime(now));
        }

        // 3. 主表 CREATED → RUNNING（首次有非发起人加入）
        maybeMarkOngoing(call, userId, now);
        return call;
    }

    @Override
    public void inviteMoreCall(Long userId, ImRtcCallInviteMoreReqVO reqVO) {
        validateEnabled();
        // 1.1 校验通话存在且活跃
        ImRtcCallDO call = validateCallActive(reqVO.getRoom());
        // 1.2 仅群通话支持追加邀请
        if (!ImConversationTypeEnum.isGroup(call.getConversationType())) {
            throw exception(RTC_GROUP_REQUIRED);
        }
        // 1.3 操作者必须是已在房间的参与者
        ImRtcParticipantDO operator = rtcParticipantMapper.selectByRoomAndUserId(call.getRoom(), userId);
        if (operator == null || !ImRtcParticipantStatusEnum.isJoined(operator.getStatus())) {
            throw exception(RTC_NOT_PARTICIPANT);
        }
        // 1.4 校验被邀请人必须是该群活跃成员；非本群成员直接抛
        Set<Long> validMemberIds = new HashSet<>(
                groupMemberService.getActiveGroupMemberUserIdsByGroupId(call.getGroupId()));
        for (Long inviteeId : reqVO.getInviteeIds()) {
            if (!validMemberIds.contains(inviteeId)) {
                throw exception(GROUP_MEMBER_NOT_IN_GROUP);
            }
        }
        // 1.5 排除已在通话池的；剩余即本次新邀请
        Set<Long> existingUserIds = CollectionUtils.convertSet(
                rtcParticipantMapper.selectListByRoom(call.getRoom()), ImRtcParticipantDO::getUserId);
        Set<Long> incomingUserIds = new LinkedHashSet<>(reqVO.getInviteeIds());
        incomingUserIds.removeAll(existingUserIds);
        if (CollUtil.isEmpty(incomingUserIds)) {
            return;
        }

        // 2. 批量 INSERT 新邀请人
        LocalDateTime now = LocalDateTime.now();
        // TODO @AI：convertList
        List<ImRtcParticipantDO> participants = new ArrayList<>(incomingUserIds.size());
        for (Long inviteeId : incomingUserIds) {
            participants.add(new ImRtcParticipantDO()
                    .setRoom(call.getRoom()).setUserId(inviteeId)
                    .setRole(ImRtcParticipantRoleEnum.INVITEE.getRole())
                    .setStatus(ImRtcParticipantStatusEnum.INVITING.getStatus())
                    .setInviteTime(now));
        }
        rtcParticipantMapper.insertBatch(participants);

        // 3. 推送通知：RTC_CALL(INVITE) 给每个新邀请人
        AdminUserRespDTO inviter = adminUserApi.getUser(userId);
        for (Long inviteeId : incomingUserIds) {
            pushCallInviteNotification(call, inviter, inviteeId);
        }
    }

    @Override
    public ImRtcCallDO acceptCall(Long userId, String room) {
        validateEnabled();
        // 1.1 校验通话存在且活跃
        ImRtcCallDO call = validateCallActive(room);
        // 1.2 校验本人是该通话的参与者
        ImRtcParticipantDO participant = validateParticipant(call, userId);

        // 2.1 已 JOINED 直接幂等返回
        if (ImRtcParticipantStatusEnum.isJoined(participant.getStatus())) {
            return call;
        }
        // 2.2 仅 INVITING → JOINED；其它状态拒
        if (!ImRtcParticipantStatusEnum.isInviting(participant.getStatus())) {
            throw exception(RTC_SESSION_NOT_EXISTS);
        }
        LocalDateTime now = LocalDateTime.now();
        int updated = rtcParticipantMapper.updateByIdAndStatus(participant.getId(), ImRtcParticipantStatusEnum.INVITING.getStatus(),
                new ImRtcParticipantDO().setId(participant.getId()).setStatus(ImRtcParticipantStatusEnum.JOINED.getStatus()).setAcceptTime(now));
        if (updated == 0) {
            throw exception(RTC_SESSION_NOT_EXISTS);
        }

        // 3. 主表 CREATED → RUNNING（首次有非发起人接通）
        maybeMarkOngoing(call, userId, now);
        return call;
    }

    @Override
    public void rejectCall(Long userId, String room) {
        validateEnabled();
        // 1.1 校验通话存在且活跃
        ImRtcCallDO call = validateCallActive(room);
        // 1.2 校验本人是该通话的参与者
        ImRtcParticipantDO participant = validateParticipant(call, userId);
        // 1.3 仅 INVITING 状态可拒
        if (!ImRtcParticipantStatusEnum.isInviting(participant.getStatus())) {
            throw exception(RTC_SESSION_NOT_EXISTS);
        }

        // 2. INVITING → REJECTED；并发已变更则忽略
        int updated = rtcParticipantMapper.updateByIdAndStatus(participant.getId(), ImRtcParticipantStatusEnum.INVITING.getStatus(),
                new ImRtcParticipantDO().setId(participant.getId()).setStatus(ImRtcParticipantStatusEnum.REJECTED.getStatus()).setLeaveTime(LocalDateTime.now()));
        if (updated == 0) {
            return;
        }

        // 3.1 群通话拒绝：仅推 RTC_CALL(REJECT) 给主叫；不影响通话整体
        // TODO @AI：群聊如果拒绝，需要做类似的 endSession 逻辑么？
        if (ImConversationTypeEnum.isGroup(call.getConversationType())) {
            pushCallRejectNotification(call, userId);
            return;
        }
        // 3.2 私聊拒绝：走 endSession（推 RTC_CALL_END(reason=REJECT)）
        endSession(call, userId, ImRtcCallEndReasonEnum.REJECT);
    }

    @Override
    public void cancelCall(Long userId, String room) {
        validateEnabled();
        // 1.1 校验通话存在且活跃
        ImRtcCallDO call = validateCallActive(room);
        // 1.2 仅主叫可取消
        if (ObjUtil.notEqual(call.getInviterUserId(), userId)) {
            throw exception(RTC_NOT_PARTICIPANT);
        }
        // 1.3 仅 CREATED 状态可取消（RUNNING 应走 leave）
        if (!ImRtcCallStatusEnum.isCreated(call.getStatus())) {
            throw exception(RTC_SESSION_NOT_EXISTS);
        }

        // 2. 关会话并推 RTC_CALL_END(reason=CANCEL)
        endSession(call, userId, ImRtcCallEndReasonEnum.CANCEL);
    }

    @Override
    public void leaveCall(Long userId, String room) {
        validateEnabled();
        // 1.1 校验通话存在且活跃
        ImRtcCallDO call = validateCallActive(room);
        // 1.2 校验本人是该通话的参与者
        ImRtcParticipantDO participant = validateParticipant(call, userId);

        // 2. 当前状态 → LEFT；条件 UPDATE 防并发反复改
        LocalDateTime now = LocalDateTime.now();
        rtcParticipantMapper.updateByIdAndStatus(participant.getId(), participant.getStatus(),
                new ImRtcParticipantDO().setId(participant.getId()).setStatus(ImRtcParticipantStatusEnum.LEFT.getStatus()).setLeaveTime(now));

        // 3. 触发关房判定：私聊任一方离开必关；群通话仅在「无人在房 + 无人响铃」时关
        // 群通话单人离开：LiveKit `ParticipantDisconnected` 自动通知房内成员；业务后端不另外推
        endSessionIfTerminal(call, userId, ImRtcCallEndReasonEnum.HANGUP);
    }

    /**
     * 群通话是否应该关闭：房内没有 JOINED 就关
     * <p>
     * 群通话至少需要 1 个 JOINED 在房才有意义；只剩 INVITING（响铃中）也无价值
     * <p>
     * 关房后 endSession 会把残留 INVITING 批量改 NO_ANSWER 并推 RTC_CALL_END，响铃端 UI 自动收敛
     */
    private boolean shouldCloseGroupRoom(String room) {
        List<ImRtcParticipantDO> joined = rtcParticipantMapper.selectListByRoomAndStatus(room,
                ImRtcParticipantStatusEnum.JOINED.getStatus());
        return CollUtil.isEmpty(joined);
    }

    /**
     * 查询两人共同所在的活跃私聊通话
     * <p>
     * 拆成 3 段简单查询：拿 A 的活跃 participant → 拿主表判私聊未结束 → 看 B 是否在同 call 活跃
     */
    private ImRtcCallDO getActivePrivateCallByPair(Long userIdA, Long userIdB) {
        ImRtcParticipantDO participantA = rtcParticipantMapper.selectLastOneByUserIdAndStatus(userIdA, ImRtcParticipantStatusEnum.ACTIVE_STATUSES);
        if (participantA == null) {
            return null;
        }
        ImRtcCallDO call = rtcCallMapper.selectByRoom(participantA.getRoom());
        if (call == null
                || !ImConversationTypeEnum.isPrivate(call.getConversationType())
                || ImRtcCallStatusEnum.isEnded(call.getStatus())) {
            return null;
        }
        ImRtcParticipantDO participantB = rtcParticipantMapper.selectByRoomAndUserId(call.getRoom(), userIdB);
        if (participantB == null
                || !ImRtcParticipantStatusEnum.ACTIVE_STATUSES.contains(participantB.getStatus())) {
            return null;
        }
        return call;
    }

    @Override
    public ImRtcCallDO validateCallParticipant(Long userId, String room) {
        validateEnabled();
        // 1.1 校验通话存在且活跃
        ImRtcCallDO call = validateCallActive(room);
        // 1.2 校验本人是该通话的参与者
        validateParticipant(call, userId);
        return call;
    }

    @Override
    public ImRtcCallDO getActiveCall(Long userId, Long groupId) {
        validateEnabled();
        // 1. 鉴权：仅群活跃成员能查（走单行 SQL，不依赖成员列表缓存）
        groupMemberService.validateMemberInGroup(groupId, userId);
        // 2. 查询活跃通话
        return rtcCallMapper.selectLastOneByGroupIdAndStatusIn(groupId, ImRtcCallStatusEnum.ACTIVE_STATUSES);
    }

    @Override
    public List<ImRtcParticipantDO> getCallParticipantList(String room) {
        return rtcParticipantMapper.selectListByRoom(room);
    }

    @Override
    public String signCallToken(Long userId, String room) {
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        // displayName 不能为空；nickname 缺失时降级为 userId 字符串
        String displayName = StrUtil.blankToDefault(
                user == null ? null : user.getNickname(), String.valueOf(userId));
        return signToken(userId, displayName, room);
    }

    // TODO DONE @AI：兼容 JDK8 —— 项目 Spring Boot 3 已要求 JDK17+，switch arrow case 可直接用
    @Override
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
     * 处理 LiveKit 「成员加入」事件：DB 接通态由 {@link #acceptCall(Long, String)} 接口写入，此处仅做 1602 转推
     *
     * @param event 事件
     */
    private void handleParticipantJoined(LiveKitWebhookEventDTO event) {
        if (ObjUtil.hasNull(event.getRoom(), event.getParticipant())) {
            return;
        }
        // 1. 前置检查：通话存在且活跃
        String room = event.getRoom().getName();
        ImRtcCallDO call = rtcCallMapper.selectByRoom(room);
        if (call == null || ImRtcCallStatusEnum.isEnded(call.getStatus())) {
            return;
        }
        Long userId = liveKitClient.parseUserId(event.getParticipant().getIdentity());
        if (userId == null) {
            return;
        }

        // 2. 推 1602 通知参与方 / 全群参与方
        pushParticipantConnectedNotification(call, userId);
    }

    /**
     * 处理 LiveKit 「成员离开」事件：正常 {@link #leaveCall(Long, String)} 接口已经清理过的话，条件 UPDATE 自动幂等
     */
    private void handleParticipantLeft(LiveKitWebhookEventDTO event) {
        if (ObjUtil.hasNull(event.getRoom(), event.getParticipant())) {
            return;
        }
        // 1. 前置检查：通话存在且活跃 + 参与者仍在 JOINED
        String room = event.getRoom().getName();
        ImRtcCallDO call = rtcCallMapper.selectByRoom(room);
        if (call == null || ImRtcCallStatusEnum.isEnded(call.getStatus())) {
            return;
        }
        Long userId = liveKitClient.parseUserId(event.getParticipant().getIdentity());
        if (userId == null) {
            return;
        }
        ImRtcParticipantDO participant = rtcParticipantMapper.selectByRoomAndUserId(call.getRoom(), userId);
        if (participant == null || !ImRtcParticipantStatusEnum.isJoined(participant.getStatus())) {
            return;
        }

        // 2. JOINED → LEFT；正常 leave 接口已改过则 update 影响 0 行直接退出
        int updated = rtcParticipantMapper.updateByIdAndStatus(participant.getId(), ImRtcParticipantStatusEnum.JOINED.getStatus(),
                new ImRtcParticipantDO().setId(participant.getId()).setStatus(ImRtcParticipantStatusEnum.LEFT.getStatus()).setLeaveTime(LocalDateTime.now()));
        if (updated == 0) {
            return;
        }
        log.info("[handleParticipantLeft][room={} userId={} 由 LiveKit Webhook 兜底]", room, userId);

        // 3. 推 1603 通知参与方 / 全群参与方
        pushParticipantDisconnectedNotification(call, userId);
        // 4. 触发关房判定：私聊任一方离开必关；群通话仅在「无人在房 + 无人响铃」时关
        endSessionIfTerminal(call, userId, ImRtcCallEndReasonEnum.HANGUP);
    }

    /**
     * 处理 LiveKit 「房间结束」事件：兜底把 call 推到 ENDED
     */
    private void handleRoomFinished(LiveKitWebhookEventDTO event) {
        if (event.getRoom() == null) {
            return;
        }
        // 1. 前置检查：通话存在且活跃
        String room = event.getRoom().getName();
        ImRtcCallDO call = rtcCallMapper.selectByRoom(room);
        if (call == null || ImRtcCallStatusEnum.isEnded(call.getStatus())) {
            return;
        }
        // 2. 关会话
        log.info("[handleRoomFinished][room={} 由 LiveKit Webhook 兜底]", room);
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
                count = liveKitClient.listParticipants(call.getRoom());
            } catch (Exception e) {
                log.warn("[cleanupZombieCalls][查询 LiveKit 失败 room={}]", call.getRoom(), e);
                continue;
            }
            if (count != 0) {
                continue;
            }
            log.info("[cleanupZombieCalls][清理僵尸通话 room={}]", call.getRoom());
            endSession(call, null, ImRtcCallEndReasonEnum.HANGUP);
            cleaned++;
        }
        return cleaned;
    }

    // ========== 内部辅助 ==========

    private void validateEnabled() {
        if (!imProperties.getRtc().isEnabled()) {
            throw exception(RTC_NOT_ENABLED);
        }
    }

    /**
     * 校验通话存在且活跃：不存在 / 已 ENDED 直接抛
     */
    private ImRtcCallDO validateCallActive(String room) {
        ImRtcCallDO call = rtcCallMapper.selectByRoom(room);
        if (call == null || ImRtcCallStatusEnum.isEnded(call.getStatus())) {
            throw exception(RTC_SESSION_NOT_EXISTS);
        }
        return call;
    }

    /**
     * 校验本人是该通话的参与者：accept / reject / leave / refreshToken 的共用前置
     */
    private ImRtcParticipantDO validateParticipant(ImRtcCallDO call, Long userId) {
        ImRtcParticipantDO participant = rtcParticipantMapper.selectByRoomAndUserId(call.getRoom(), userId);
        if (participant == null) {
            throw exception(RTC_NOT_PARTICIPANT);
        }
        return participant;
    }

    /**
     * 关房判定收口：私聊任一方离开必关；群通话仅在「无人在房 + 无人响铃」时关
     */
    private void endSessionIfTerminal(ImRtcCallDO call, Long operatorId, ImRtcCallEndReasonEnum reason) {
        if (ImConversationTypeEnum.isPrivate(call.getConversationType())
                || shouldCloseGroupRoom(call.getRoom())) {
            endSession(call, operatorId, reason);
        }
    }

    /**
     * 校验 invite 入参；按场景区分必填字段，私聊补好友校验，群聊补群成员校验
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
            friendService.validateFriend(userId, reqVO.getPeerUserId());
            return;
        }
        if (ImConversationTypeEnum.isGroup(scene)) {
            if (reqVO.getGroupId() == null) {
                throw exception(RTC_GROUP_REQUIRED);
            }
            // 走单行 SQL；不依赖成员列表缓存，避免新加入成员发起群通话被误判
            groupMemberService.validateMemberInGroup(reqVO.getGroupId(), userId);
            return;
        }
        throw new IllegalArgumentException("非法的 scene: " + scene);
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
        // TODO @AI：超过数量，直接抛出异常，不用裁剪，不然很奇怪；
        int max = imProperties.getRtc().getGroupMaxParticipants();
        if (initial.size() + 1 > max) {
            return initial.stream().limit(max - 1L)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return initial;
    }

    /**
     * 主表 CREATED → RUNNING；仅当首个非发起人加入时推进；条件 UPDATE 保幂等
     */
    private void maybeMarkOngoing(ImRtcCallDO call, Long acceptorId, LocalDateTime now) {
        // 1. 已 RUNNING / ENDED 直接退出；发起人加入不算「首次有人接通」
        if (!ImRtcCallStatusEnum.isCreated(call.getStatus())) {
            return;
        }
        if (Objects.equals(call.getInviterUserId(), acceptorId)) {
            return;
        }

        // 2. CREATED → RUNNING 条件 UPDATE；多人并发只有一人成功
        int updated = rtcCallMapper.updateByIdAndStatus(call.getId(), ImRtcCallStatusEnum.CREATED.getStatus(),
                new ImRtcCallDO().setStatus(ImRtcCallStatusEnum.RUNNING.getStatus()).setAcceptTime(now));
        if (updated == 0) {
            // 3. 竞争失败：reload 看真实终态；已 ENDED 抛错，否则同步内存
            ImRtcCallDO latest = rtcCallMapper.selectById(call.getId());
            if (latest == null || ImRtcCallStatusEnum.isEnded(latest.getStatus())) {
                throw exception(RTC_SESSION_NOT_EXISTS);
            }
            call.setStatus(latest.getStatus()).setAcceptTime(latest.getAcceptTime());
            return;
        }
        // 4. 推进成功：同步内存给后续判断
        call.setStatus(ImRtcCallStatusEnum.RUNNING.getStatus()).setAcceptTime(now);
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
            log.info("[endSession][已被另一路径终结，跳过 room={} operator={} reason={}]",
                    call.getRoom(), operatorId, reason);
            return;
        }
        // 1.2 更新参与表为已结束：残留 INVITING 改 NO_ANSWER；残留 JOINED 改 LEFT 并写 leaveTime
        rtcParticipantMapper.updateByRoomAndStatus(call.getRoom(), ImRtcParticipantStatusEnum.INVITING.getStatus(),
                new ImRtcParticipantDO().setStatus(ImRtcParticipantStatusEnum.NO_ANSWER.getStatus()));
        rtcParticipantMapper.updateByRoomAndStatus(call.getRoom(), ImRtcParticipantStatusEnum.JOINED.getStatus(),
                new ImRtcParticipantDO().setStatus(ImRtcParticipantStatusEnum.LEFT.getStatus()).setLeaveTime(now));

        // 2. 兜底删除 LiveKit 房间，强制断开异常残留客户端；失败仅记日志，不阻断业务
        try {
            liveKitClient.deleteRoom(call.getRoom());
        } catch (Exception e) {
            log.warn("[endSession][删除 LiveKit 房间失败 room={} operator={} reason={}]",
                    call.getRoom(), operatorId, reason, e);
        }

        // 3. 推 RTC_CALL_END
        Long durationSeconds = call.getAcceptTime() != null ?
                Duration.between(call.getAcceptTime(), now).getSeconds() : null;
        pushCallEndNotification(call, operatorId, reason, durationSeconds);
        log.info("[endSession][room={} operator={} reason={}]", call.getRoom(), operatorId, reason);
    }

    /**
     * 通话事件的收件人池：1）私聊为双方参与者；2）群聊为群活跃成员
     *
     * @param call 通话主表
     * @return 收件人 userId 集合
     */
    private Collection<Long> getCallAudienceUserIdList(ImRtcCallDO call) {
        if (ImConversationTypeEnum.isGroup(call.getConversationType())) {
            return groupMemberService.getActiveGroupMemberUserIdsByGroupId(call.getGroupId());
        }
        return CollectionUtils.convertSet(
                rtcParticipantMapper.selectListByRoom(call.getRoom()), ImRtcParticipantDO::getUserId);
    }

    // ========== 通知推送 ==========

    /**
     * RTC_CALL(INVITE)：走 webSocketService 直推到被邀请人；persistent=false 不入消息流
     */
    private void pushCallInviteNotification(ImRtcCallDO call, AdminUserRespDTO inviter, Long inviteeId) {
        String token = signToken(inviteeId, inviter == null ? null : inviter.getNickname(), call.getRoom());
        ImRtcCallNotification payload = ImRtcCallNotification.ofInvite(
                call, inviter, imProperties.getRtc().getLivekitUrl(), token);
        webSocketService.sendPrivateMessageAsync(inviteeId, ImPrivateMessageDTO.ofRtcNotification(
                ImMessageTypeEnum.RTC_CALL.getType(), call.getInviterUserId(), inviteeId, payload));
    }

    /**
     * RTC_CALL(REJECT)：仅群通话场景；走 webSocketService 直推主叫
     * <p>
     * 私聊拒绝走 endSession → RTC_CALL_END(reason=REJECT) 入消息流，不在此推
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
        Collection<Long> receivers = getCallAudienceUserIdList(call);
        if (CollUtil.isEmpty(receivers)) {
            return;
        }
        ImPrivateMessageDTO dto = ImPrivateMessageDTO.ofRtcNotification(type, actorUserId, null, payload);
        webSocketService.sendPrivateMessageAsync(receivers, dto);
    }

    /**
     * RTC_CALL_START：仅群通话场景；走 imGroupMessageService.send，入 im_group_message 全群广播；与 RTC_CALL_END 两段式配对
     */
    private void pushCallStartNotification(ImRtcCallDO call, AdminUserRespDTO inviter) {
        ImRtcCallStartNotification payload = ImRtcCallStartNotification.of(call, inviter);
        ImGroupMessageSendDTO dto = new ImGroupMessageSendDTO().setGroupId(call.getGroupId())
                .setType(ImMessageTypeEnum.RTC_CALL_START.getType()).setContent(payload);
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

        // 群聊：发起人总是 sender；receiver = 全群；走 imGroupMessageService.send 全群广播
        if (ImConversationTypeEnum.isGroup(call.getConversationType())) {
            ImGroupMessageSendDTO dto = new ImGroupMessageSendDTO().setGroupId(call.getGroupId())
                    .setType(ImMessageTypeEnum.RTC_CALL_END.getType()).setContent(payload);
            groupMessageService.sendGroupMessage(senderId, dto);
            return;
        }

        // 私聊：发起人总是 sender；receiver = 通话另一方
        ImRtcParticipantDO peer = CollUtil.findOne(
                rtcParticipantMapper.selectListByRoom(call.getRoom()),
                p -> !Objects.equals(p.getUserId(), senderId));
        Long receiverId = peer != null ? peer.getUserId() : senderId;
        ImPrivateMessageSendDTO dto = new ImPrivateMessageSendDTO().setReceiverId(receiverId)
                .setType(ImMessageTypeEnum.RTC_CALL_END.getType()).setContent(payload);
        privateMessageService.sendPrivateMessage(senderId, dto);
    }

    // ========== Token / VO ==========

    /**
     * 签 LiveKit Token
     */
    private String signToken(Long userId, String displayName, String room) {
        return liveKitClient.signJoinToken(liveKitClient.buildIdentity(userId), displayName, room);
    }

}
