package cn.iocoder.yudao.module.im.service.rtc;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.rtc.vo.ImRtcCallManagerPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallCreateReqVO;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallInviteReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcCallDO;
import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcParticipantDO;
import cn.iocoder.yudao.module.im.dal.mysql.rtc.ImRtcCallMapper;
import cn.iocoder.yudao.module.im.dal.mysql.rtc.ImRtcParticipantMapper;
import cn.iocoder.yudao.module.im.dal.redis.rtc.ImRtcCallLockRedisDAO;
import cn.iocoder.yudao.module.im.enums.ImConversationTypeEnum;
import cn.iocoder.yudao.module.im.enums.ImContentTypeEnum;
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
import cn.iocoder.yudao.module.im.service.websocket.notification.rtc.*;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

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
 *   1601 RTC_CALL（INVITING / JOINED / REJECTED / NO_ANSWER / LEFT 子类型）→ {@link ImWebSocketService#sendNotificationAsync} 仅推参与方；
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
    @Transactional(rollbackFor = Exception.class)
    public ImRtcCallDO createCall(Long userId, ImRtcCallCreateReqVO reqVO) {
        validateEnabled();
        // 1. 校验入参与场景
        validateCreateCall(userId, reqVO);

        // 2. 加锁后跑业务主体；同好友对 / 同群串行，避免并发各开一通的竞态
        if (ImConversationTypeEnum.isGroup(reqVO.getConversationType())) {
            return rtcCallLockRedisDAO.lockGroup(reqVO.getGroupId(), () -> createGroupCall(userId, reqVO));
        }
        Long peerUserId = CollUtil.getFirst(reqVO.getInviteeIds());
        return rtcCallLockRedisDAO.lockPrivate(userId, peerUserId, () -> createPrivateCall(userId, reqVO, peerUserId));
    }

    /**
     * 群通话创建锁内主体：群有活跃通话直接抛（引导走 inviteCall / joinCall）；
     * 否则走完整生命周期，若发起人自身忙线立即 end(BUSY) 留下通话记录
     *
     * @param userId 发起人编号
     * @param reqVO  创建请求
     * @return 通话主表（可能 status=ENDED 表示自身忙线）
     */
    private ImRtcCallDO createGroupCall(Long userId, ImRtcCallCreateReqVO reqVO) {
        // 1.1 同群有活跃通话 → 直接抛异常（UI 应已拦截），引导用户走 inviteCall / joinCall；避免重复开通
        ImRtcCallDO active = rtcCallMapper.selectLastOneByGroupIdAndStatusIn(
                reqVO.getGroupId(), ImRtcCallStatusEnum.ACTIVE_STATUSES);
        if (active != null) {
            throw exception(RTC_GROUP_CALL_ACTIVE);
        }
        // 1.2 先检测发起人忙线状态；不抛，留给下方 end 决定
        boolean selfBusy = rtcParticipantMapper.selectLastOneByUserIdAndStatus(
                userId, ImRtcParticipantStatusEnum.ACTIVE_STATUSES) != null;

        // 2. 完整生命周期：INSERT + INVITE × N + START 全推
        ImRtcCallDO call = createCall0(userId, reqVO);

        // 3. 自身忙线立即 end(BUSY)；END 推送给群，群成员看到完整 START + END
        if (selfBusy) {
            endSession(call, userId, ImRtcCallEndReasonEnum.BUSY);
        }
        return call;
    }

    /**
     * 私聊创建锁内主体：双方忙线时仍走完整生命周期（create + 立即 end(BUSY)），
     * 同一对正在通话视作数据异常直接抛
     *
     * @param userId     发起人编号
     * @param reqVO      创建请求
     * @param peerUserId 对端编号；来自 reqVO.inviteeIds 的唯一元素
     * @return 通话主表（可能 status=ENDED 表示忙线）
     */
    private ImRtcCallDO createPrivateCall(Long userId, ImRtcCallCreateReqVO reqVO, Long peerUserId) {
        // 1.1 双方已在同一通话 → 数据异常（UI 应已拦截），直接抛
        if (getActivePrivateCallByPair(userId, peerUserId) != null) {
            throw exception(RTC_SELF_BUSY);
        }
        // 1.2 忙线检测：self 优先（更可执行的提示）；不抛，留给下方 end 决定
        boolean selfBusy = rtcParticipantMapper.selectLastOneByUserIdAndStatus(
                userId, ImRtcParticipantStatusEnum.ACTIVE_STATUSES) != null;
        boolean peerBusy = !selfBusy && rtcParticipantMapper.selectLastOneByUserIdAndStatus(
                peerUserId, ImRtcParticipantStatusEnum.ACTIVE_STATUSES) != null;

        // 2. 完整生命周期：INSERT + INVITE 全推
        ImRtcCallDO call = createCall0(userId, reqVO);

        // 3. 忙线立即 end(BUSY)；operator 决定两端看到的文案（self busy → operator=自己；peer busy → operator=对端）
        if (selfBusy) {
            endSession(call, userId, ImRtcCallEndReasonEnum.BUSY);
        } else if (peerBusy) {
            endSession(call, peerUserId, ImRtcCallEndReasonEnum.BUSY);
        }
        return call;
    }

    @Override
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    public void inviteCall(Long userId, ImRtcCallInviteReqVO reqVO) {
        validateEnabled();
        // 1.1 校验通话存在且活跃
        ImRtcCallDO call = validateCallActive(reqVO.getRoom());
        // 1.2 仅群通话支持追加邀请
        if (!ImConversationTypeEnum.isGroup(call.getConversationType())) {
            throw exception(RTC_GROUP_REQUIRED);
        }
        // 1.3 操作者必须是房内 JOINED 参与者
        ImRtcParticipantDO operator = rtcParticipantMapper.selectByRoomAndUserId(call.getRoom(), userId);
        if (operator == null || !ImRtcParticipantStatusEnum.isJoined(operator.getStatus())) {
            throw exception(RTC_NOT_PARTICIPANT);
        }
        // 2. 加群锁后执行追加邀请；避免与新建 / 重复追加的竞态
        rtcCallLockRedisDAO.lockGroup(call.getGroupId(), () -> {
            addInvitees(call, userId, reqVO.getInviteeIds());
            return null;
        });
    }

    /**
     * 新建通话实体；INSERT 主表 + 参与表 + 推送 INVITE / START
     *
     * @param inviterId 发起人编号
     * @param reqVO     创建请求
     * @return 通话主表
     */
    private ImRtcCallDO createCall0(Long inviterId, ImRtcCallCreateReqVO reqVO) {
        // 1. 构造参数：room 用 UUID；解析被邀请池
        String room = IdUtil.fastSimpleUUID();
        LocalDateTime now = LocalDateTime.now();
        Set<Long> invitees = resolveInvitees(reqVO, inviterId);

        // 2.1 INSERT 主表；群聊发起人即时 JOINED 但通话仍处 CREATED，等首个非发起人接通才切 RUNNING
        ImRtcCallDO call = new ImRtcCallDO().setRoom(room)
                .setConversationType(reqVO.getConversationType()).setMediaType(reqVO.getMediaType())
                .setInviterUserId(inviterId).setGroupId(reqVO.getGroupId())
                .setStatus(ImRtcCallStatusEnum.CREATED.getStatus()).setStartTime(now);
        rtcCallMapper.insert(call);
        // 2.2 批量 INSERT 参与表：发起人即时 JOINED，被邀请人 INVITING 等接通
        List<ImRtcParticipantDO> participants = new ArrayList<>(invitees.size() + 1);
        participants.add(new ImRtcParticipantDO().setCallId(call.getId()).setRoom(room).setUserId(inviterId)
                .setRole(ImRtcParticipantRoleEnum.INVITER.getRole())
                .setStatus(ImRtcParticipantStatusEnum.JOINED.getStatus())
                .setInviteTime(now).setAcceptTime(now));
        for (Long inviteeId : invitees) {
            participants.add(new ImRtcParticipantDO()
                    .setCallId(call.getId()).setRoom(room).setUserId(inviteeId)
                    .setRole(ImRtcParticipantRoleEnum.INVITEE.getRole())
                    .setStatus(ImRtcParticipantStatusEnum.INVITING.getStatus())
                    .setInviteTime(now));
        }
        rtcParticipantMapper.insertBatch(participants);

        // 3.1 推送通知：RTC_CALL(INVITE) 给每个被邀请人
        AdminUserRespDTO inviterUser = adminUserApi.getUser(inviterId);
        Map<Long, AdminUserRespDTO> inviteeMap = adminUserApi.getUserMap(invitees);
        for (Long inviteeId : invitees) {
            pushCallInviteNotification(call, inviterUser, inviteeId, inviteeMap.get(inviteeId), invitees);
        }
        // 3.2 向消息流写入 RTC_CALL_START；群聊全群广播，私聊定向给被叫，作为会话列表预览的依据
        pushCallStartNotification(call, inviterUser, invitees);
        return call;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImRtcCallDO joinCall(Long userId, String room) {
        validateEnabled();
        // 1.1 校验通话存在且活跃
        ImRtcCallDO call = validateCallActive(room);
        // 1.2 仅群通话支持「旁观者加入」
        if (!ImConversationTypeEnum.isGroup(call.getConversationType())) {
            throw exception(RTC_GROUP_REQUIRED);
        }
        // 1.3 校验当前用户是该群有效成员；防止仅凭 room 就拿到 LiveKit token 越权入会
        groupMemberService.validateMemberInGroup(call.getGroupId(), userId);
        // 1.4 校验当前用户没有其它活跃通话
        validateUserNotInOtherCall(userId, call.getRoom());

        // 2. 入参与表：已有记录切回 JOINED；不在记录则以 JOINER 角色 INSERT
        LocalDateTime now = LocalDateTime.now();
        joinParticipant(call, userId, now);

        // 3. 主表 CREATED → RUNNING（首次有非发起人加入）
        maybeMarkOngoing(call, userId, now);
        return call;
    }

    /**
     * 给已存在的活跃群通话追加邀请；批量校验群成员 + 去重已在通话池 + 推送 INVITE
     *
     * @param call       活跃通话主表
     * @param inviterId  本次追加邀请的发起人；已是房内 JOINED 参与者
     * @param inviteeIds 本次追加的被邀请人编号
     */
    private void addInvitees(ImRtcCallDO call, Long inviterId, Collection<Long> inviteeIds) {
        // 1.1 校验被邀请人都是群活跃成员
        groupMemberService.validateMembersInGroup(call.getGroupId(), inviteeIds);
        // 1.2 排除已在通话池的；剩余即本次新邀请
        List<ImRtcParticipantDO> existingParticipants = rtcParticipantMapper.selectListByRoom(call.getRoom());
        Set<Long> existingUserIds = CollectionUtils.convertSet(existingParticipants, ImRtcParticipantDO::getUserId);
        Set<Long> incomingUserIds = new LinkedHashSet<>(inviteeIds);
        incomingUserIds.removeAll(existingUserIds);
        if (CollUtil.isEmpty(incomingUserIds)) {
            return;
        }
        long activeCount = existingParticipants.stream()
                .filter(participant -> ImRtcParticipantStatusEnum.ACTIVE_STATUSES.contains(participant.getStatus()))
                .count();
        if (activeCount + incomingUserIds.size() > imProperties.getRtc().getGroupMaxParticipants()) {
            throw exception(RTC_GROUP_INVITEE_OVER_LIMIT);
        }

        // 2. 批量 INSERT 新邀请人
        LocalDateTime now = LocalDateTime.now();
        List<ImRtcParticipantDO> participants = CollectionUtils.convertList(incomingUserIds, inviteeId ->
                new ImRtcParticipantDO().setCallId(call.getId()).setRoom(call.getRoom()).setUserId(inviteeId)
                        .setRole(ImRtcParticipantRoleEnum.INVITEE.getRole())
                        .setStatus(ImRtcParticipantStatusEnum.INVITING.getStatus()).setInviteTime(now));
        rtcParticipantMapper.insertBatch(participants);

        // 3. 推送通知：RTC_CALL(INVITE) 给每个新邀请人
        AdminUserRespDTO inviter = adminUserApi.getUser(inviterId);
        Map<Long, AdminUserRespDTO> inviteeMap = adminUserApi.getUserMap(incomingUserIds);
        for (Long inviteeId : incomingUserIds) {
            pushCallInviteNotification(call, inviter, inviteeId, inviteeMap.get(inviteeId), incomingUserIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        validateUserNotInOtherCall(userId, call.getRoom());
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
    @Transactional(rollbackFor = Exception.class)
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

        // 3.1 群通话拒绝：推 RTC_CALL(REJECT) 给主叫，再判定是否要关房（全员拒接 / 只剩主叫一人时收敛）
        if (ImConversationTypeEnum.isGroup(call.getConversationType())) {
            pushCallRejectNotification(call, userId);
            endSessionIfTerminal(call, userId);
            return;
        }
        // 3.2 私聊拒绝：走 endSession（推 RTC_CALL_END(reason=REJECT)）
        endSession(call, userId, ImRtcCallEndReasonEnum.REJECT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
    public void leaveCall(Long userId, String room) {
        validateEnabled();
        // 1.1 校验通话存在且活跃
        ImRtcCallDO call = validateCallActive(room);
        // 1.2 校验本人是该通话的参与者
        ImRtcParticipantDO participant = validateParticipant(call, userId);

        // 2. 当前状态 → LEFT；条件 UPDATE 防并发反复改
        LocalDateTime now = LocalDateTime.now();
        int updated = rtcParticipantMapper.updateByIdAndStatus(participant.getId(), participant.getStatus(),
                new ImRtcParticipantDO().setId(participant.getId()).setStatus(ImRtcParticipantStatusEnum.LEFT.getStatus()).setLeaveTime(now));
        if (updated == 0) {
            return;
        }

        // 3. 群通话已入会参与者离开时推送离线通知
        if (ImConversationTypeEnum.isGroup(call.getConversationType())
                && ImRtcParticipantStatusEnum.isJoined(participant.getStatus())) {
            pushParticipantDisconnectedNotification(call, userId);
        }

        // 4. 触发关房判定：私聊任一方离开必关；群通话仅在「无人在房 + 无人响铃」时关
        endSessionIfTerminal(call, userId);
    }

    /**
     * 群通话是否应该关闭
     * <p>
     * 两种终态：
     * 1. JOINED 数 = 0（房内已没人）
     * 2. JOINED 数 = 1 且 INVITING 数 = 0（只剩 1 人独守，无后续可加入者）
     * <p>
     * 关房后 endSession 会把残留 INVITING 批量改 NO_ANSWER 并推 RTC_CALL_END，响铃端 UI 自动收敛
     *
     * @param room 业务通话编号
     * @return true 表示无法继续通话，应关房
     */
    private boolean shouldCloseGroupRoom(String room) {
        int joined = 0;
        int inviting = 0;
        for (ImRtcParticipantDO p : rtcParticipantMapper.selectListByRoom(room)) {
            if (ImRtcParticipantStatusEnum.isJoined(p.getStatus())) {
                joined++;
            } else if (ImRtcParticipantStatusEnum.isInviting(p.getStatus())) {
                inviting++;
            }
        }
        return joined == 0 || (joined == 1 && inviting == 0);
    }

    /**
     * 查询两人共同所在的活跃私聊通话
     * <p>
     * 拆成 3 段简单查询：拿 A 的活跃 participant → 拿主表判私聊未结束 → 看 B 是否在同 call 活跃
     *
     * @param userIdA 用户 A 编号
     * @param userIdB 用户 B 编号
     * @return 活跃私聊通话；不存在返回 null
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
        validateEnabled();
        ImRtcCallDO call = validateCallActive(room);
        ImRtcParticipantDO participant = validateParticipant(call, userId);
        if (!ImRtcParticipantStatusEnum.isJoined(participant.getStatus())) {
            throw exception(RTC_NOT_PARTICIPANT);
        }
        return signToken(userId, resolveDisplayName(adminUserApi.getUser(userId), userId), room);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleLiveKitEvent(LiveKitWebhookEventDTO event) {
        if (event == null || event.getEvent() == null) {
            return;
        }
        switch (event.getEvent()) {
            case LiveKitWebhookEventDTO.EVENT_PARTICIPANT_JOINED:
                handleParticipantJoined(event);
                break;
            case LiveKitWebhookEventDTO.EVENT_PARTICIPANT_LEFT:
                handleParticipantLeft(event);
                break;
            case LiveKitWebhookEventDTO.EVENT_ROOM_FINISHED:
                handleRoomFinished(event);
                break;
            default:
                // 其它事件忽略；track_published 等业务态由 LiveKit 自身分发驱动
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
     *
     * @param event 事件
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
        endSessionIfTerminal(call, userId);
    }

    /**
     * 处理 LiveKit 「房间结束」事件：兜底把 call 推到 ENDED
     *
     * @param event 事件
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
    @Transactional(rollbackFor = Exception.class)
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int timeoutInvitingParticipants(int thresholdMinutes) {
        // 阈值由调用方（Job）保证 > 0；低于 1 分钟可能误杀刚发起还在响铃的合理 INVITING 态
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(thresholdMinutes);
        return noAnswerCallCheck0(rtcParticipantMapper.selectListByStatusAndInviteTimeBefore(
                ImRtcParticipantStatusEnum.INVITING.getStatus(), threshold));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void noAnswerCallCheck(Long userId, String room) {
        // 鉴权：仅该 room 参与者可触发；失败静默，不暴露错误
        ImRtcParticipantDO operator = rtcParticipantMapper.selectByRoomAndUserId(room, userId);
        if (operator == null) {
            return;
        }
        // 阈值取后端配置，避免前后端配置不一致；前端 timer 仅是触发时机
        LocalDateTime threshold = LocalDateTime.now()
                .minusMinutes(imProperties.getRtc().getInviteTimeoutMinutes());
        List<ImRtcParticipantDO> candidates = rtcParticipantMapper.selectListByRoomAndStatusAndInviteTimeBefore(
                room, ImRtcParticipantStatusEnum.INVITING.getStatus(), threshold);
        noAnswerCallCheck0(candidates);
    }

    /**
     * 批量超时处理：循环单参与者；同 room 复用 call、批量预查 user 避免 N+1；返回成功处理数
     *
     * @param candidates 已过滤的超时 INVITING 候选
     * @return 成功处理（CAS 抢占）的数量
     */
    private int noAnswerCallCheck0(List<ImRtcParticipantDO> candidates) {
        if (CollUtil.isEmpty(candidates)) {
            return 0;
        }
        Map<String, ImRtcCallDO> callCache = new HashMap<>();
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                CollectionUtils.convertSet(candidates, ImRtcParticipantDO::getUserId));
        int timedOut = 0;
        for (ImRtcParticipantDO participant : candidates) {
            if (timeoutInvitingParticipant(participant, callCache, userMap)) {
                timedOut++;
            }
        }
        return timedOut;
    }

    /**
     * 单参与者振铃超时：CAS 把 INVITING 切到 NO_ANSWER，群通话推 RTC_CALL(NO_ANSWER) + 级联关房判定；
     * 私聊场景被叫超时即整通话结束，走 endSession(NO_ANSWER) 推 RTC_CALL_END
     *
     * @param participant INVITING 状态的超时候选
     * @param callCache   按 room 缓存 call 对象，避免同批次多人重复查询
     * @param userMap     候选用户预查 map；避免逐个 adminUserApi.getUser 走 N+1
     * @return 是否成功处理（CAS 失败 / 通话主表缺失等场景返回 false）
     */
    private boolean timeoutInvitingParticipant(ImRtcParticipantDO participant,
                                               Map<String, ImRtcCallDO> callCache,
                                               Map<Long, AdminUserRespDTO> userMap) {
        // 1. CAS：INVITING → NO_ANSWER；并发已变（用户刚接 / 拒，或 endSession 整体改）跳过
        int updated = rtcParticipantMapper.updateByIdAndStatus(participant.getId(),
                ImRtcParticipantStatusEnum.INVITING.getStatus(),
                new ImRtcParticipantDO().setId(participant.getId())
                        .setStatus(ImRtcParticipantStatusEnum.NO_ANSWER.getStatus())
                        .setLeaveTime(LocalDateTime.now()));
        if (updated == 0) {
            return false;
        }
        Long userId = participant.getUserId();
        log.info("[timeoutInvitingParticipant][参与者振铃超时 room={} userId={}]", participant.getRoom(), userId);

        // 2. 查询通话主表；同 room 复用 callCache，避免同批次多人重复查询
        ImRtcCallDO call = callCache.computeIfAbsent(participant.getRoom(), rtcCallMapper::selectByRoom);
        if (call == null) {
            log.warn("[timeoutInvitingParticipant][通话主表缺失 room={} userId={}]", participant.getRoom(), userId);
            return false;
        }

        // 3.1 群通话：推 RTC_CALL(NO_ANSWER) 让前端 banner 移除该人 + 级联关房判定
        if (ImConversationTypeEnum.isGroup(call.getConversationType())) {
            pushCallNoAnswerNotification(call, userId, userMap.get(userId));
            endSessionIfTerminal(call, userId);
            return true;
        }
        // 3.2 私聊：被叫超时 = 整通话无人接听，走 endSession 推 RTC_CALL_END(NO_ANSWER)
        endSession(call, userId, ImRtcCallEndReasonEnum.NO_ANSWER);
        return true;
    }

    // ========== 内部辅助 ==========

    private void validateEnabled() {
        if (!imProperties.getRtc().isEnabled()) {
            throw exception(RTC_NOT_ENABLED);
        }
    }

    /**
     * 校验通话存在且活跃：不存在 / 已 ENDED 直接抛
     *
     * @param room 业务通话编号
     * @return 通话主表
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
     *
     * @param call   通话主表
     * @param userId 用户编号
     * @return 参与者记录
     */
    private ImRtcParticipantDO validateParticipant(ImRtcCallDO call, Long userId) {
        ImRtcParticipantDO participant = rtcParticipantMapper.selectByRoomAndUserId(call.getRoom(), userId);
        if (participant == null) {
            throw exception(RTC_NOT_PARTICIPANT);
        }
        return participant;
    }

    /**
     * 校验用户不在其它活跃通话中
     *
     * @param userId 用户编号
     * @param room   当前房间标识
     */
    private void validateUserNotInOtherCall(Long userId, String room) {
        // 查询当前房间外的活跃参与记录
        ImRtcParticipantDO participant = rtcParticipantMapper.selectLastOneByUserIdAndStatusInAndRoomNot(
                userId, ImRtcParticipantStatusEnum.ACTIVE_STATUSES, room);
        // 存在活跃参与记录，则当前用户忙线
        if (participant != null) {
            throw exception(RTC_SELF_BUSY);
        }
    }

    /**
     * 加入群通话参与者列表
     *
     * @param call   通话主表
     * @param userId 用户编号
     * @param now    当前时间
     */
    private void joinParticipant(ImRtcCallDO call, Long userId, LocalDateTime now) {
        // 1. 已有参与记录：切回 JOINED
        ImRtcParticipantDO existing = rtcParticipantMapper.selectByRoomAndUserId(call.getRoom(), userId);
        if (existing != null) {
            updateParticipantJoined(existing, now);
            return;
        }

        // 2. 无参与记录：以主动加入者身份新增
        try {
            rtcParticipantMapper.insert(new ImRtcParticipantDO()
                    .setCallId(call.getId()).setRoom(call.getRoom())
                    .setUserId(userId).setRole(ImRtcParticipantRoleEnum.JOINER.getRole())
                    .setStatus(ImRtcParticipantStatusEnum.JOINED.getStatus()).setInviteTime(now).setAcceptTime(now));
        } catch (DuplicateKeyException ex) {
            // 3. 唯一键冲突：回查并复用并发写入的记录
            existing = rtcParticipantMapper.selectByRoomAndUserId(call.getRoom(), userId);
            if (existing == null) {
                throw ex;
            }
            updateParticipantJoined(existing, now);
        }
    }

    /**
     * 将参与者更新为已加入
     *
     * @param participant 参与者记录
     * @param now         当前时间
     */
    private void updateParticipantJoined(ImRtcParticipantDO participant, LocalDateTime now) {
        // 已是 JOINED 直接返回
        if (ImRtcParticipantStatusEnum.isJoined(participant.getStatus())) {
            return;
        }
        // 更新状态和接听时间
        rtcParticipantMapper.updateById(new ImRtcParticipantDO().setId(participant.getId())
                .setStatus(ImRtcParticipantStatusEnum.JOINED.getStatus()).setAcceptTime(now));
    }

    /**
     * 关房判定收口：私聊任一方离开必关；群通话仅在「无人在房 + 无人响铃」时关
     * <p>
     * end reason 按 call.status 自动推：CREATED（没人接通过）= CANCEL；RUNNING（有人接通过）= HANGUP
     *
     * @param call       通话主表
     * @param operatorId 操作者用户编号；webhook 兜底场景可空
     */
    private void endSessionIfTerminal(ImRtcCallDO call, Long operatorId) {
        if (!ImConversationTypeEnum.isPrivate(call.getConversationType())
                && !shouldCloseGroupRoom(call.getRoom())) {
            return;
        }
        ImRtcCallEndReasonEnum reason = ImRtcCallStatusEnum.isCreated(call.getStatus())
                ? ImRtcCallEndReasonEnum.CANCEL : ImRtcCallEndReasonEnum.HANGUP;
        endSession(call, operatorId, reason);
    }

    /**
     * 校验创建通话入参；按场景区分必填字段，私聊补好友校验，群聊补群成员校验
     *
     * @param userId 发起人编号
     * @param reqVO  创建请求
     */
    private void validateCreateCall(Long userId, ImRtcCallCreateReqVO reqVO) {
        Integer conversationType = reqVO.getConversationType();
        if (ImConversationTypeEnum.isPrivate(conversationType)) {
            // 私聊必须 1 个对端
            if (CollUtil.size(reqVO.getInviteeIds()) != 1) {
                throw exception(RTC_PRIVATE_INVITEE_REQUIRED);
            }
            Long peerUserId = CollUtil.getFirst(reqVO.getInviteeIds());
            if (ObjUtil.equal(userId, peerUserId)) {
                throw exception(RTC_INVITE_SELF);
            }
            friendService.validateFriend(userId, peerUserId);
            return;
        }
        if (ImConversationTypeEnum.isGroup(conversationType)) {
            if (reqVO.getGroupId() == null) {
                throw exception(RTC_GROUP_REQUIRED);
            }
            // 群通话必须前端选中被邀请人（对齐微信）；空集合直接拒
            if (CollUtil.isEmpty(reqVO.getInviteeIds())) {
                throw exception(RTC_GROUP_INVITEE_REQUIRED);
            }
            groupMemberService.validateMemberInGroup(reqVO.getGroupId(), userId);
            return;
        }
        throw new IllegalArgumentException("非法的 conversationType: " + conversationType);
    }

    /**
     * 解析被邀请池：私聊为 peerUserId 单元素；群聊为前端选中子集（超量抛错）
     *
     * @param reqVO     创建请求
     * @param inviterId 发起人编号；自己不进被邀请池
     * @return 被邀请人 userId 集合
     */
    private Set<Long> resolveInvitees(ImRtcCallCreateReqVO reqVO, Long inviterId) {
        // 1. 私聊：inviteeIds 已在 validateCreateCall 校验仅 1 个对端，直接复用
        if (ImConversationTypeEnum.isPrivate(reqVO.getConversationType())) {
            return new LinkedHashSet<>(reqVO.getInviteeIds());
        }

        // 2. 群聊校验：被邀请人必须是该群活跃成员，防止恶意客户端塞任意 userId
        groupMemberService.validateMembersInGroup(reqVO.getGroupId(), reqVO.getInviteeIds());
        Set<Long> initial = new LinkedHashSet<>(reqVO.getInviteeIds());
        // 发起人本人不进被邀请池
        initial.remove(inviterId);
        if (CollUtil.isEmpty(initial)) {
            throw exception(RTC_GROUP_INVITEE_REQUIRED);
        }
        int max = imProperties.getRtc().getGroupMaxParticipants();
        if (initial.size() + 1 > max) {
            throw exception(RTC_GROUP_INVITEE_OVER_LIMIT);
        }
        return initial;
    }

    /**
     * 主表 CREATED → RUNNING；仅当首个非发起人加入时推进；条件 UPDATE 保幂等
     *
     * @param call       通话主表；推进成功后会同步内存字段
     * @param acceptorId 加入者用户编号；发起人加入不算「首次接通」
     * @param now        当前时间
     */
    private void maybeMarkOngoing(ImRtcCallDO call, Long acceptorId, LocalDateTime now) {
        // 1. 已 RUNNING / ENDED 直接退出；发起人加入不算「首次有人接通」
        if (!ImRtcCallStatusEnum.isCreated(call.getStatus())) {
            return;
        }
        if (ObjUtil.equal(call.getInviterUserId(), acceptorId)) {
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
     *
     * @param call       通话主表
     * @param operatorId 操作者用户编号；webhook 兜底 / Job 清理场景可空
     * @param reason     结束原因
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
        // 【特殊】同步内存 call：让 createCall 这类调用方拿到的 DO 立即反映 ENDED 终态，Controller 拼 RespVO 能直接用
        call.setStatus(ImRtcCallStatusEnum.ENDED.getStatus()).setEndReason(reason.getReason()).setEndTime(now);
        // 1.2 更新参与表为已结束：残留 INVITING 改 NO_ANSWER；残留 JOINED 改 LEFT 并写 leaveTime
        rtcParticipantMapper.updateByRoomAndStatus(call.getRoom(), ImRtcParticipantStatusEnum.INVITING.getStatus(),
                new ImRtcParticipantDO().setStatus(ImRtcParticipantStatusEnum.NO_ANSWER.getStatus()));
        rtcParticipantMapper.updateByRoomAndStatus(call.getRoom(), ImRtcParticipantStatusEnum.JOINED.getStatus(),
                new ImRtcParticipantDO().setStatus(ImRtcParticipantStatusEnum.LEFT.getStatus()).setLeaveTime(now));

        // 2. 推 RTC_CALL_END；先于 deleteRoom 异步发出，让前端按业务语义 reset（NO_ANSWER / CANCEL 等），
        // 避免随后 LiveKit Disconnected 事件抢先触发前端 "通话已断开" 兜底 toast
        Long durationSeconds = call.getAcceptTime() != null ?
                Duration.between(call.getAcceptTime(), now).getSeconds() : null;
        pushCallEndNotification(call, operatorId, reason, durationSeconds);

        // 3. 兜底删除 LiveKit 房间，强制断开异常残留客户端；失败仅记日志，不阻断业务
        try {
            liveKitClient.deleteRoom(call.getRoom());
        } catch (Exception e) {
            log.warn("[endSession][删除 LiveKit 房间失败 room={} operator={} reason={}]",
                    call.getRoom(), operatorId, reason, e);
        }
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
     *
     * @param call         通话主表
     * @param inviter      发起人；可空，缺失时 payload 的 inviterNickname / Avatar 留空
     * @param inviteeId    被邀请人用户编号
     * @param invitee      被邀请人；可空，缺失时 token 内 displayName 降级为 userId
     * @param inviteeIds   本次被邀请人列表；前端来电界面展示「邀请的其他人」用，包含 inviteeId 自身
     */
    private void pushCallInviteNotification(ImRtcCallDO call, AdminUserRespDTO inviter,
                                            Long inviteeId, AdminUserRespDTO invitee,
                                            Collection<Long> inviteeIds) {
        String token = signToken(inviteeId, resolveDisplayName(invitee, inviteeId), call.getRoom());
        ImRtcCallNotification payload = ImRtcCallNotification.ofInvite(
                call, inviter, imProperties.getRtc().getLivekitUrl(), token, inviteeIds);
        webSocketService.sendNotificationAsync(inviteeId, ImConversationTypeEnum.NONE.getType(),
                ImContentTypeEnum.RTC_CALL.getType(), payload);
    }

    /**
     * RTC_CALL(REJECT)：仅群通话场景；走 webSocketService 推给群通话受众
     * <p>
     * 私聊拒绝走 endSession → RTC_CALL_END(reason=REJECT) 入消息流，不在此推
     *
     * @param call           通话主表
     * @param operatorUserId 拒接者用户编号
     */
    private void pushCallRejectNotification(ImRtcCallDO call, Long operatorUserId) {
        AdminUserRespDTO operator = operatorUserId != null ? adminUserApi.getUser(operatorUserId) : null;
        ImRtcCallNotification payload = ImRtcCallNotification.ofReject(call, operatorUserId, operator);
        for (Long receiverUserId : getCallAudienceUserIdList(call)) {
            webSocketService.sendNotificationAsync(receiverUserId, ImConversationTypeEnum.NONE.getType(),
                    ImContentTypeEnum.RTC_CALL.getType(), payload);
        }
    }

    /**
     * RTC_CALL(NO_ANSWER)：仅群通话场景；振铃超时由 Job 触发；走 webSocketService 推给群通话受众
     * <p>
     * 私聊未接听走 endSession → RTC_CALL_END(reason=NO_ANSWER) 入消息流，不在此推
     *
     * @param call           通话主表
     * @param operatorUserId 未接听者用户编号
     * @param operator       未接听者预查结果；调用方批量查避免 N+1，可空
     */
    private void pushCallNoAnswerNotification(ImRtcCallDO call, Long operatorUserId, AdminUserRespDTO operator) {
        ImRtcCallNotification payload = ImRtcCallNotification.ofNoAnswer(call, operatorUserId, operator);
        for (Long receiverUserId : getCallAudienceUserIdList(call)) {
            webSocketService.sendNotificationAsync(receiverUserId, ImConversationTypeEnum.NONE.getType(),
                    ImContentTypeEnum.RTC_CALL.getType(), payload);
        }
    }

    /**
     * 通话参与者加入：LiveKit webhook participant_joined 触发；私聊推双方多端、群聊推全群成员（胶囊条 + 1）
     *
     * @param call    通话主表
     * @param userId  加入的参与者用户编号
     */
    private void pushParticipantConnectedNotification(ImRtcCallDO call, Long userId) {
        pushParticipantNotification(call, ImContentTypeEnum.RTC_PARTICIPANT_CONNECTED.getType(), userId,
                ImRtcParticipantConnectedNotification.of(call, userId));
    }

    /**
     * 通话参与者离开：LiveKit webhook participant_left 触发；推送范围同 {@link #pushParticipantConnectedNotification}
     *
     * @param call    通话主表
     * @param userId  离开的参与者用户编号
     */
    private void pushParticipantDisconnectedNotification(ImRtcCallDO call, Long userId) {
        pushParticipantNotification(call, ImContentTypeEnum.RTC_PARTICIPANT_DISCONNECTED.getType(), userId,
                ImRtcParticipantDisconnectedNotification.of(call, userId));
    }

    /**
     * 推送参与者事件的公共骨架；按会话类型决定收件人，单次 batch 推送扇出
     *
     * @param call        通话主表
     * @param type        消息类型；1602 / 1603
     * @param actorUserId 触发本次事件的用户编号
     * @param payload     业务 payload
     */
    private void pushParticipantNotification(ImRtcCallDO call, Integer type, Long actorUserId, Object payload) {
        Collection<Long> receivers = getCallAudienceUserIdList(call);
        if (CollUtil.isEmpty(receivers)) {
            return;
        }
        webSocketService.sendNotificationAsync(receivers, ImConversationTypeEnum.NONE.getType(), type, payload);
    }

    /**
     * RTC_CALL_START：群聊走 imGroupMessageService.send 全群广播；私聊走 imPrivateMessageService.send 定向给被叫
     * <p>
     * 私聊 peer 直接复用 createCall 解析好的 invitees，避免再查参与表；用于会话列表预览展示「[语音通话]」
     *
     * @param call     通话主表
     * @param inviter  发起人；可空
     * @param invitees 本次邀请池；私聊场景取首个作为 peer
     */
    private void pushCallStartNotification(ImRtcCallDO call, AdminUserRespDTO inviter, Set<Long> invitees) {
        ImRtcCallStartNotification payload = ImRtcCallStartNotification.of(call, inviter);
        Long peerUserId = ImConversationTypeEnum.isGroup(call.getConversationType()) ? null : CollUtil.getFirst(invitees);
        pushCallChatMessage(call, ImContentTypeEnum.RTC_CALL_START, payload, peerUserId);
    }

    /**
     * RTC_CALL_END：私聊走 imPrivateMessageService.send；群通话走 imGroupMessageService.send
     * <p>
     * senderId 始终用通话发起人，让前端按「谁发起通话」决定气泡左右；操作者从 payload.operatorUserId 拿
     *
     * @param call            通话主表
     * @param operatorId      操作者用户编号；webhook 兜底 / Job 清理场景可空
     * @param reason          结束原因
     * @param durationSeconds 通话时长（秒）；未接通时为 null
     */
    private void pushCallEndNotification(ImRtcCallDO call, Long operatorId, ImRtcCallEndReasonEnum reason,
                             Long durationSeconds) {
        AdminUserRespDTO operator = operatorId != null ? adminUserApi.getUser(operatorId) : null;
        ImRtcCallEndNotification payload = ImRtcCallEndNotification.of(call, reason, durationSeconds, operatorId, operator);
        Long peerUserId = null;
        if (!ImConversationTypeEnum.isGroup(call.getConversationType())) {
            ImRtcParticipantDO peer = CollUtil.findOne(
                    rtcParticipantMapper.selectListByRoom(call.getRoom()),
                    p -> ObjUtil.notEqual(p.getUserId(), call.getInviterUserId()));
            peerUserId = peer != null ? peer.getUserId() : null;
        }
        pushCallChatMessage(call, ImContentTypeEnum.RTC_CALL_END, payload, peerUserId);
    }

    /**
     * RTC 通话事件入消息流：群聊走 groupMessageService 全群广播，私聊走 privateMessageService 定向给 peer
     * <p>
     * senderId 固定取 inviterUserId，让前端按「谁发起」决定气泡左右；私聊 peer 缺失时降级为发给自己作兜底
     *
     * @param call       通话主表
     * @param type       消息类型；RTC_CALL_START / RTC_CALL_END
     * @param payload    推送 payload
     * @param peerUserId 私聊对端用户编号；群聊忽略，私聊缺失时回退为 senderId
     */
    private void pushCallChatMessage(ImRtcCallDO call, ImContentTypeEnum type, Object payload, Long peerUserId) {
        Long senderId = call.getInviterUserId();
        if (ImConversationTypeEnum.isGroup(call.getConversationType())) {
            ImGroupMessageSendDTO dto = new ImGroupMessageSendDTO().setGroupId(call.getGroupId())
                    .setType(type.getType()).setContent(payload);
            groupMessageService.sendGroupMessage(senderId, dto);
            return;
        }
        Long receiverId = peerUserId != null ? peerUserId : senderId;
        ImPrivateMessageSendDTO dto = new ImPrivateMessageSendDTO().setReceiverId(receiverId)
                .setType(type.getType()).setContent(payload);
        privateMessageService.sendPrivateMessage(senderId, dto);
    }

    // ========== Token / VO ==========

    /**
     * 签 LiveKit Token
     *
     * @param userId      用户编号；token 内 identity 由此派生
     * @param displayName 房内显示名
     * @param room        LiveKit 房间名
     * @return JWT 字符串
     */
    private String signToken(Long userId, String displayName, String room) {
        return liveKitClient.signJoinToken(liveKitClient.buildIdentity(userId), displayName, room);
    }

    /**
     * 解析房内显示名：优先取 user.nickname，缺失时降级为 userId 字符串（LiveKit displayName 不可空）
     *
     * @param user   用户信息；可空
     * @param userId 用户编号；displayName 兜底
     * @return 房内显示名
     */
    private static String resolveDisplayName(AdminUserRespDTO user, Long userId) {
        return StrUtil.blankToDefault(
                user == null ? null : user.getNickname(), String.valueOf(userId));
    }

    // ==================== 管理后台 ====================

    @Override
    public PageResult<ImRtcCallDO> getCallPage(ImRtcCallManagerPageReqVO reqVO) {
        return rtcCallMapper.selectPage(reqVO);
    }

    @Override
    public ImRtcCallDO getCall(Long id) {
        return rtcCallMapper.selectById(id);
    }

    @Override
    public List<ImRtcParticipantDO> getCallParticipantListByCallId(Long id) {
        return rtcParticipantMapper.selectListByCallId(id);
    }

}
