package cn.iocoder.yudao.module.im.service.rtc;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallInviteMoreReqVO;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallInviteReqVO;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcCallRespVO;
import cn.iocoder.yudao.module.im.controller.admin.rtc.vo.ImRtcGroupCallRespVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.enums.ImConversationTypeEnum;
import cn.iocoder.yudao.module.im.enums.friend.ImFriendStateEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.enums.rtc.ImCallEndReasonEnum;
import cn.iocoder.yudao.module.im.enums.rtc.ImCallStatusEnum;
import cn.iocoder.yudao.module.im.framework.config.ImProperties;
import cn.iocoder.yudao.module.im.framework.rtc.core.LiveKitTokenUtils;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import cn.iocoder.yudao.module.im.service.group.ImGroupMemberService;
import cn.iocoder.yudao.module.im.service.rtc.bo.ImCallSessionBO;
import cn.iocoder.yudao.module.im.service.rtc.bo.ImCallSessionStore;
import cn.iocoder.yudao.module.im.service.rtc.dto.*;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImPrivateMessageDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
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
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class ImRtcCallServiceImpl implements ImRtcCallService {

    // TODO @AI：后续存储到 DB 里；（晚点改）
    @Resource
    private ImCallSessionStore sessionStore;

    @Resource
    private ImGroupMemberService groupMemberService;
    @Resource
    private ImFriendService friendService;
    @Resource
    private ImWebSocketService webSocketService;

    @Resource
    private AdminUserApi adminUserApi;

    @Resource
    private ImProperties imProperties;

    @Override
    public ImRtcCallRespVO invite(Long userId, ImRtcCallInviteReqVO reqVO) {
        // TODO @AI：按照 1.1 1.2 1.3 这样搞；
        ensureEnabled();
        Integer scene = reqVO.getScene();
        // 1. 校验入参与场景
        validateInviteScene(userId, reqVO);
        // 2. 派生 roomName；同好友对 / 同群只对应一个 roomName
        String roomName = deriveRoomName(scene, userId, reqVO.getPeerUserId(), reqVO.getGroupId());
        // 3. 用户级忙线检查；当前用户已在另一通话且不是同房间则拒绝
        String selfActive = sessionStore.getActiveRoom(userId);
        // TODO @AI：notEquals 方案；
        if (selfActive != null && !selfActive.equals(roomName)) {
            throw exception(RTC_SELF_BUSY);
        }
        // 4. 私聊场景下检查被叫忙线；群通话候选成员各自决定接听，不在此拦截
        // TODO @AI：需要在看看，群聊会不会出现这个情况？感觉应该也会，可以调研下微信，以及其他 im
        if (ImConversationTypeEnum.isPrivate(scene)
                && sessionStore.isBusy(reqVO.getPeerUserId())) {
            String peerActive = sessionStore.getActiveRoom(reqVO.getPeerUserId());
            // TODO @AI：notEquals 方案；
            if (peerActive != null && !peerActive.equals(roomName)) {
                throw exception(RTC_PEER_BUSY);
            }
        }

        // 5. 原子获取或创建会话；并发同时点呼叫只会建一份
        boolean[] newCreated = {false};
        ImCallSessionBO session = sessionStore.getOrCreate(roomName, key -> {
            newCreated[0] = true;
            return buildNewSession(key, userId, reqVO);
        });
        // 6. 老 session：本次属于"加入已有通话"；新 session：发邀请信令并把发起人加入房间
        sessionStore.bindUser(userId, roomName);
        session.getJoinedUserIds().add(userId);
        if (newCreated[0]) {
            pushInviteNotifications(session, userId);
            // 群通话首次发起：向全群广播 STARTED，胶囊条立刻在所有成员客户端出现
            if (ImConversationTypeEnum.isGroup(session.getScene())) {
                pushGroupBroadcast(session, ImMessageTypeEnum.RTC_GROUP_STARTED.getType(), userId);
            }
        } else if (ImConversationTypeEnum.isGroup(session.getScene())) {
            // 群通话主动加入已有通话：广播 UPDATED 让胶囊条人数刷新
            pushGroupBroadcast(session, ImMessageTypeEnum.RTC_GROUP_UPDATED.getType(), userId);
        }
        // 7. 给当前调用方签 token
        return buildResp(session, userId, newCreated[0]);
    }

    @Override
    public ImRtcCallRespVO accept(Long userId, String roomName) {
        // TODO @AI：按照 1.1 1.2 1.3 这样搞；（注释分层一点）
        ensureEnabled();
        ImCallSessionBO session = requireSession(roomName);
        // TODO @AI：减少 ONGOING、INVITING 的混用；最好是中文（英文），大家更好理解。
        // 群通话处于 ONGOING；私聊必须 INVITING 才能接听
        if (ImConversationTypeEnum.isPrivate(session.getScene())
                && !ImCallStatusEnum.isInviting(session.getStatus())) {
            throw exception(RTC_SESSION_NOT_EXISTS);
        }
        if (!session.containsUser(userId)) {
            throw exception(RTC_NOT_PARTICIPANT);
        }
        // 私聊接听 → 切到 ONGOING；群通话已经是 ONGOING；幂等：重复 accept 不报错
        if (ImConversationTypeEnum.isPrivate(session.getScene())) {
            session.setStatus(ImCallStatusEnum.ONGOING.getType());
            // 振铃定时器在前端，后端不做 cancel 动作
        }
        session.getJoinedUserIds().add(userId);
        sessionStore.bindUser(userId, roomName);
        // 推 RTC_ACCEPT 给除自己外的全部相关方
        pushAcceptNotification(session, userId);
        // 群通话有人接听 → 全群广播 UPDATED 刷新胶囊条
        if (ImConversationTypeEnum.isGroup(session.getScene())) {
            pushGroupBroadcast(session, ImMessageTypeEnum.RTC_GROUP_UPDATED.getType(), userId);
        }
        return buildResp(session, userId, false);
    }

    @Override
    public void reject(Long userId, String roomName) {
        // TODO @AI：按照 1.1 1.2 1.3 这样搞；（注释分层一点）
        ensureEnabled(); // TODO @AI：是不是只有发起校验，其它地方先不校验了；
        ImCallSessionBO session = requireSession(roomName);
        if (!session.containsUser(userId)) {
            throw exception(RTC_NOT_PARTICIPANT);
        }
        // 群通话拒绝：不影响其他成员，房间继续存在
        if (ImConversationTypeEnum.isGroup(session.getScene())) {
            session.getRejectedUserIds().add(userId);
            sessionStore.unbindUserFromRoom(userId, roomName);
            // 仅广播 UPDATED；之前向主叫推 RTC_END 会被前端错误识别为「整个通话结束」并 reset 主叫的通话窗
            pushGroupBroadcast(session, ImMessageTypeEnum.RTC_GROUP_UPDATED.getType(), userId);
            return;
        }
        // 私聊拒绝：仅 INVITING 状态可拒；推 RTC_END 给所有相关方并关房
        if (!ImCallStatusEnum.isInviting(session.getStatus())) {
            throw exception(RTC_SESSION_NOT_EXISTS);
        }
        endSession(session, userId, ImCallEndReasonEnum.REJECT);
    }

    @Override
    public void cancel(Long userId, String roomName) {
        // TODO @AI：按照 1.1 1.2 1.3 这样搞；（注释分层一点）
        ensureEnabled();
        ImCallSessionBO session = requireSession(roomName);
        if (!Objects.equals(session.getInviterId(), userId)) {
            throw exception(RTC_NOT_PARTICIPANT);
        }
        // 仅 INVITING 状态可取消；ONGOING 应走 leave
        if (!ImCallStatusEnum.isInviting(session.getStatus())) {
            throw exception(RTC_SESSION_NOT_EXISTS);
        }
        endSession(session, userId, ImCallEndReasonEnum.CANCEL);
    }

    @Override
    public void leave(Long userId, String roomName) {
        // TODO @AI：按照 1.1 1.2 1.3 这样搞；（注释分层一点）
        ensureEnabled();
        ImCallSessionBO session = requireSession(roomName);
        if (!session.containsUser(userId)) {
            throw exception(RTC_NOT_PARTICIPANT);
        }
        session.getLeftUserIds().add(userId);
        session.getJoinedUserIds().remove(userId);
        sessionStore.unbindUserFromRoom(userId, roomName);
        // 关房条件：私聊任一方离开 = 关；群通话仅当所有被邀请人都已应答 + 在房无人时才关
        // 发起人离开后被邀请人仍可继续接听，房间不应当立即销毁
        // TODO @AI：单聊、群聊，是否可以复用一个逻辑；private 本质，只是一个特殊的方式而已；如果 private 后续支持邀请人，那 private 这个判断，就是 bug 了呀。其它地方，看看是不是也调整下；
        if (ImConversationTypeEnum.isPrivate(session.getScene())
                || shouldCloseGroupRoom(session)) {
            endSession(session, userId, ImCallEndReasonEnum.HANGUP);
            return;
        }
        // 群通话单人离开：仅广播 UPDATED；不推 RTC_END 避免主叫误关通话窗
        pushGroupBroadcast(session, ImMessageTypeEnum.RTC_GROUP_UPDATED.getType(), userId);
    }

    /**
     * 群通话是否应该关闭：在房成员空 + 没有还在响铃的被邀请人
     * <p>
     * 仅 INVITED 状态下未接受 / 拒绝 / 离开的成员视为「还在响铃」；这样发起人提前离开后，被邀请人仍能接听并独立组成通话，符合微信群通话语义
     */
    private boolean shouldCloseGroupRoom(ImCallSessionBO session) {
        // TODO @AI：CollUtil
        if (!session.getJoinedUserIds().isEmpty()) {
            return false;
        }
        // TODO @AI：CollUtil？
        if (session.getInviteeIds() == null) {
            return true;
        }
        // TODO @AI：exists 或者哪种？这样更简洁一点。
        for (Long id : session.getInviteeIds()) {
            boolean responded = session.getRejectedUserIds().contains(id)
                    || session.getLeftUserIds().contains(id);
            if (!responded) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ImRtcCallRespVO refreshToken(Long userId, String roomName) {
        ensureEnabled(); // TODO @AI：简化下；
        ImCallSessionBO session = requireSession(roomName);
        if (!session.containsUser(userId)) {
            throw exception(RTC_NOT_PARTICIPANT);
        }
        return buildResp(session, userId, false);
    }

    @Override
    public void inviteMore(Long userId, ImRtcCallInviteMoreReqVO reqVO) {
        ensureEnabled();
        ImCallSessionBO session = requireSession(reqVO.getRoomName());
        // 仅群通话支持追加邀请
        // TODO @AI：如果放开私聊的邀请，代码改动量大么？（这里只讨论，不用改）
        if (!ImConversationTypeEnum.isGroup(session.getScene())) {
            throw exception(RTC_GROUP_REQUIRED);
        }
        // 操作者必须是已在房间的参与者
        if (!session.getJoinedUserIds().contains(userId)) {
            throw exception(RTC_NOT_PARTICIPANT);
        }
        // 过滤已在被邀请池或已加入的人；仅给新成员发邀请
        Set<Long> existing = new LinkedHashSet<>();
        if (session.getInviteeIds() != null) {
            existing.addAll(session.getInviteeIds());
        }
        existing.addAll(session.getJoinedUserIds());
        // 校验被邀请人必须是该群活跃成员；防止操作者夹带任意 userId 进群房间
        // TODO @AI：如果放开私聊的邀请，代码改动量大么？（这里只讨论，不用改）
        Set<Long> validMemberIds = new LinkedHashSet<>(
                groupMemberService.getActiveGroupMemberUserIdsByGroupId(session.getGroupId()));
        Set<Long> incoming = new LinkedHashSet<>();
        for (Long id : reqVO.getInviteeIds()) {
            if (!existing.contains(id) && validMemberIds.contains(id)) {
                incoming.add(id);
            }
        }
        // TODO @AI:CollUtil isempty 判断；
        if (incoming.isEmpty()) {
            return;
        }
        if (session.getInviteeIds() == null) {
            session.setInviteeIds(new LinkedHashSet<>());
        }
        session.getInviteeIds().addAll(incoming);
        // 给新人推 RTC_INVITE；签 token 复用 invite 的逻辑
        AdminUserRespDTO inviter = adminUserApi.getUser(userId);
        for (Long inviteeId : incoming) {
            ImRtcInviteNotification payload = buildInvitePayload(session, userId, inviter, inviteeId);
            // TODO @AI：链式调用，相同的在一行；
            ImPrivateMessageDTO dto = new ImPrivateMessageDTO()
                    .setType(ImMessageTypeEnum.RTC_INVITE.getType())
                    .setSenderId(userId)
                    .setReceiverId(inviteeId)
                    .setContent(JsonUtils.toJsonString(payload))
                    .setSendTime(LocalDateTime.now());
            webSocketService.sendPrivateMessageAsync(inviteeId, dto);
        }
        // 全群广播 UPDATED；让胶囊条上「等待加入」头像同步
        pushGroupBroadcast(session, ImMessageTypeEnum.RTC_GROUP_UPDATED.getType(), userId);
    }

    @Override
    public List<ImRtcCallRespVO> getActiveSessions(Long userId) {
        // TODO @AI：报错，前端就不应该发起；
        if (!imProperties.getRtc().isEnabled()) {
            return Collections.emptyList();
        }
        String roomName = sessionStore.getActiveRoom(userId);
        if (StrUtil.isBlank(roomName)) {
            return Collections.emptyList();
        }
        ImCallSessionBO session = sessionStore.get(roomName);
        if (session == null) {
            return Collections.emptyList();
        }

        // TODO @AI：返回 ImCallSessionBO？controller 拼接？（后面在改，所有涉及到各种翻译的地方）
        // 列表场景不签 token；前端要 token 时再调 refreshToken
        ImRtcCallRespVO vo = toBaseVO(session);
        return Collections.singletonList(vo);
    }

    @Override
    public void handleLiveKitEvent(LiveKitWebhookEventDTO event) {
        // TODO @AI：需要 try catch 记录 hook + 成功 or 失败的日志么？
        if (event == null || event.getEvent() == null) {
            return;
        }
        // TODO @AI：需要检验 jdk8 语法；
        // TODO @AI：livekit webhookevent 是不是搞个枚举类？
        switch (event.getEvent()) {
            case "participant_left" -> handleParticipantLeft(event);
            case "room_finished" -> handleRoomFinished(event);
            default -> {
                // TODO @AI：仅仅 logger，后续按需搞；
                /* 其它事件忽略；participant_joined / track_published 等业务态由 LiveKit 自身分发驱动 */
            }
        }
    }

    // TODO @AI：方法注释，里面的注释，按照 1. 2. 3. 阅读性更好
    // TODO @AI：处理必要的代码空行；阅读性更好（但是不要太多空行，层次感缺少）
    /**
     * 处理 LiveKit「成员离开」事件；正常 leave 接口已经清理过的话此处幂等忽略
     * 私聊任一方离开 → 结束通话；群通话仅一人离开 → 广播；最后一人离开 → 结束通话
     */
    private void handleParticipantLeft(LiveKitWebhookEventDTO event) {
        if (event.getRoom() == null || event.getParticipant() == null) {
            return;
        }
        String roomName = event.getRoom().getName();
        ImCallSessionBO session = sessionStore.get(roomName);
        if (session == null) {
            return;
        }
        Long userId = parseUserIdFromIdentity(event.getParticipant().getIdentity());
        if (userId == null) {
            return;
        }
        // 业务态可能已被 leave 接口清掉；contains 判断保证幂等
        boolean wasInRoom = session.getJoinedUserIds().remove(userId);
        sessionStore.unbindUserFromRoom(userId, roomName);
        if (!wasInRoom) {
            return;
        }
        session.getLeftUserIds().add(userId);
        log.info("[handleParticipantLeft][roomName={} userId={} 由 LiveKit Webhook 兜底清理]", roomName, userId);
        // 关房条件与 leave 一致；群通话仅在「无人在房 + 无人响铃」时关
        if (ImConversationTypeEnum.isPrivate(session.getScene())
                || shouldCloseGroupRoom(session)) {
            endSession(session, userId, ImCallEndReasonEnum.HANGUP);
            return;
        }
        // 群通话单人异常离开：仅广播 UPDATED；同 leave 路径，避免主叫误关
        pushGroupBroadcast(session, ImMessageTypeEnum.RTC_GROUP_UPDATED.getType(), userId);
    }

    // TODO @AI：方法注释，里面的注释，按照 1. 2. 3. 阅读性更好
    // TODO @AI：处理必要的代码空行；阅读性更好（但是不要太多空行，层次感缺少）
    /** 处理「房间结束」事件；兜底从 sessionStore 移除残留会话 */
    private void handleRoomFinished(LiveKitWebhookEventDTO event) {
        if (event.getRoom() == null) {
            return;
        }
        String roomName = event.getRoom().getName();
        ImCallSessionBO session = sessionStore.get(roomName);
        if (session == null) {
            return;
        }
        log.info("[handleRoomFinished][roomName={} 由 LiveKit Webhook 兜底清理]", roomName);
        // 复用 endSession：状态切 ENDED + 推 RTC_END + 群通话广播 ENDED + 移除内存索引
        endSession(session, null, ImCallEndReasonEnum.HANGUP);
    }

    // TODO @AI：这里多端是必须的么？如果非必须，就先不用了。。。
    /**
     * 从 LiveKit identity 解析业务 userId
     * <p>
     * 当前 identity 直接是 userId 字符串；预留 userId#terminal 多端格式的解析支持
     */
    private Long parseUserIdFromIdentity(String identity) {
        if (identity == null || identity.isEmpty()) {
            return null;
        }
        int sep = identity.indexOf('#');
        String idPart = sep >= 0 ? identity.substring(0, sep) : identity;
        try {
            return Long.parseLong(idPart);
        } catch (NumberFormatException e) {
            log.warn("[parseUserIdFromIdentity][非法 identity={}]", identity);
            return null;
        }
    }

    @Override
    public ImRtcGroupCallRespVO getGroupActiveCall(Long groupId) {
        if (!imProperties.getRtc().isEnabled() || groupId == null) {
            return null;
        }
        ImCallSessionBO session = sessionStore.get("call_group_" + groupId);
        if (session == null) {
            return null;
        }
        // TODO @AI：链式调用；
        // TODO @AI：后续看看，能不能改成 controller 处理；
        ImRtcGroupCallRespVO vo = new ImRtcGroupCallRespVO();
        vo.setCallId(session.getCallId());
        vo.setRoomName(session.getRoomName());
        vo.setGroupId(session.getGroupId());
        vo.setMediaType(session.getMediaType());
        vo.setInviterId(session.getInviterId());
        vo.setJoinedUserIds(new LinkedHashSet<>(session.getJoinedUserIds()));
        if (session.getInviteeIds() != null) {
            vo.setInviteeIds(new LinkedHashSet<>(session.getInviteeIds()));
        }
        return vo;
    }

    // ========== 内部辅助 ==========

    /** 校验通话功能开关 */
    private void ensureEnabled() {
        if (!imProperties.getRtc().isEnabled()) {
            throw exception(RTC_NOT_ENABLED);
        }
    }

    /** 校验 invite 入参；按场景区分必填字段 + 群通话校验当前用户是群活跃成员 */
    private void validateInviteScene(Long userId, ImRtcCallInviteReqVO reqVO) {
        Integer scene = reqVO.getScene();
        if (ImConversationTypeEnum.isPrivate(scene)) {
            // TODO @AI：这个在 validator，参数那校验；vo 里写 asserttrue；
            if (reqVO.getPeerUserId() == null) {
                throw exception(RTC_PRIVATE_INVITEE_REQUIRED);
            }
            if (Objects.equals(userId, reqVO.getPeerUserId())) {
                throw exception(RTC_INVITE_SELF);
            }
            // 好友 / 黑名单校验：和私聊消息发送同一套语义；NONE 已删 / 未加，BLOCKED 被对方拉黑
            // TODO @AI：是不是抽一个方法？validateFriendXXX（因为有多处调用）；
            ImFriendStateEnum state = friendService.getFriendState(userId, reqVO.getPeerUserId());
            if (state == ImFriendStateEnum.NONE) {
                throw exception(FRIEND_NOT_FRIEND);
            }
            if (state == ImFriendStateEnum.BLOCKED) {
                throw exception(FRIEND_BLOCKED_BY_PEER);
            }
            return;
        }
        if (ImConversationTypeEnum.isGroup(scene)) {
            // TODO @AI：这个在 validator，参数那校验；vo 里写 asserttrue；
            if (reqVO.getGroupId() == null) {
                throw exception(RTC_GROUP_REQUIRED);
            }
            // 当前用户必须是群活跃成员（包括主动加入已有通话的场景）；非成员不允许进入群房间
            // TODO @AI：是不是校验下，被邀请人，是不是这个群里的？
            // TODO @AI：member那搞个 ids 校验？避免太大的查询。
            List<Long> activeMemberIds = groupMemberService
                    .getActiveGroupMemberUserIdsByGroupId(reqVO.getGroupId());
            if (!activeMemberIds.contains(userId)) {
                throw exception(GROUP_MEMBER_NOT_IN_GROUP);
            }
            return;
        }
        // TODO @AI：抛出异常的 scene 类型；
        throw exception(RTC_PRIVATE_INVITEE_REQUIRED);
    }

    // TODO @AI在：注释风格；
    // TODO @AI：方法名，我们一般叫 buildXXX；
    /** 派生 roomName；私聊用好友对小大 ID，双向同 key；群聊用 groupId */
    private String deriveRoomName(Integer scene, Long selfId, Long peerId, Long groupId) {
        if (ImConversationTypeEnum.isPrivate(scene)) {
            long min = Math.min(selfId, peerId);
            long max = Math.max(selfId, peerId);
            return "call_friend_" + min + "_" + max;
        }
        return "call_group_" + groupId;
    }

    // TODO @AI：注释风格？
    /** 构造新会话；私聊起始 INVITING；群聊起始 ONGOING */
    private ImCallSessionBO buildNewSession(String roomName, Long inviterId, ImRtcCallInviteReqVO reqVO) {
        Set<Long> invitees = resolveInvitees(reqVO, inviterId);
        long now = System.currentTimeMillis();
        return ImCallSessionBO.builder()
                // TODO @AI：链式调用，相同的放在一行；
                .callId(IdUtil.fastSimpleUUID())
                .roomName(roomName)
                .scene(reqVO.getScene())
                .mediaType(reqVO.getMediaType())
                .inviterId(inviterId)
                .inviteeIds(invitees)
                .groupId(reqVO.getGroupId())
                // TODO @AI：是不是
                .status(ImConversationTypeEnum.isPrivate(reqVO.getScene())
                        ? ImCallStatusEnum.INVITING.getType()
                        : ImCallStatusEnum.ONGOING.getType())
                .startTime(now)
                .joinedUserIds(new LinkedHashSet<>())
                .rejectedUserIds(new LinkedHashSet<>())
                .leftUserIds(new LinkedHashSet<>())
                .build();
    }

    /** 解析被邀请池：私聊为 peerUserId 单元素；群聊优先用前端选中的子集，否则取群活跃成员 */
    private Set<Long> resolveInvitees(ImRtcCallInviteReqVO reqVO, Long inviterId) {
        // TODO @AI：私聊。
        if (ImConversationTypeEnum.isPrivate(reqVO.getScene())) {
            Set<Long> set = new LinkedHashSet<>();
            set.add(reqVO.getPeerUserId());
            return set;
        }

        // 群通话：前端有传 inviteeIds 就用，否则回退拉群当前活跃成员
        Set<Long> initial = new LinkedHashSet<>();
        // TODO @AI：必须传递，否则报错；
        if (CollUtil.isNotEmpty(reqVO.getInviteeIds())) {
            initial.addAll(reqVO.getInviteeIds());
        } else {
            groupMemberService.getActiveGroupMemberListByGroupId(reqVO.getGroupId())
                    .forEach(member -> initial.add(member.getUserId()));
        }
        // 不论来源，发起人本人不进被邀请池
        initial.remove(inviterId);
        // 超出最大同时在房成员数；截断为 max-1 个，避免邀请整个 500 人大群
        // TODO @AI：超出，直接报错，不要裁剪；
        int max = imProperties.getRtc().getGroupMaxParticipants();
        if (initial.size() + 1 > max) {
            return initial.stream().limit(max - 1L)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return initial;
    }

    /** 推送来电信令；给除发起人外的所有被邀请人独立签 token */
    private void pushInviteNotifications(ImCallSessionBO session, Long inviterId) {
        AdminUserRespDTO inviter = adminUserApi.getUser(inviterId);
        for (Long inviteeId : session.getInviteeIds()) {
            ImRtcInviteNotification payload = buildInvitePayload(session, inviterId, inviter, inviteeId);
            // TODO @AI：链式调用；在一行，相同的；
            ImPrivateMessageDTO dto = new ImPrivateMessageDTO()
                    .setType(ImMessageTypeEnum.RTC_INVITE.getType())
                    .setSenderId(inviterId)
                    .setReceiverId(inviteeId)
                    .setContent(JsonUtils.toJsonString(payload))
                    .setSendTime(LocalDateTime.now());
            webSocketService.sendPrivateMessageAsync(inviteeId, dto);
        }
    }

    /** 组装单条来电载荷；invite / inviteMore 共用 */
    private ImRtcInviteNotification buildInvitePayload(ImCallSessionBO session, Long inviterId,
                                                      AdminUserRespDTO inviter, Long inviteeId) {
        // TODO @AI：链式调用，相同的在一行；
        ImRtcInviteNotification payload = new ImRtcInviteNotification();
        payload.setCallId(session.getCallId());
        payload.setRoomName(session.getRoomName());
        payload.setLivekitUrl(imProperties.getRtc().getLivekitUrl());
        payload.setToken(signToken(inviteeId,
                inviter == null ? null : inviter.getNickname(), session.getRoomName()));
        payload.setScene(session.getScene());
        payload.setMediaType(session.getMediaType());
        payload.setInviterId(inviterId);
        payload.setInviterNickname(inviter == null ? null : inviter.getNickname());
        payload.setInviterAvatar(inviter == null ? null : inviter.getAvatar());
        payload.setGroupId(session.getGroupId());
        return payload;
    }

    /** 推送接通信令；给除接听人外的所有相关方 */
    private void pushAcceptNotification(ImCallSessionBO session, Long acceptorId) {
        ImRtcAcceptNotification payload = new ImRtcAcceptNotification();
        // TODO @AI：链式调用，相同的在一行；
        payload.setCallId(session.getCallId());
        payload.setRoomName(session.getRoomName());
        payload.setAcceptorId(acceptorId);
        for (Long target : session.getAllUserIds()) {
            if (Objects.equals(target, acceptorId)) {
                continue;
            }
            // TODO @AI：链式调用，相同的在一行；
            ImPrivateMessageDTO dto = new ImPrivateMessageDTO()
                    .setType(ImMessageTypeEnum.RTC_ACCEPT.getType())
                    .setSenderId(acceptorId)
                    .setReceiverId(target)
                    .setContent(JsonUtils.toJsonString(payload))
                    .setSendTime(LocalDateTime.now());
            webSocketService.sendPrivateMessageAsync(target, dto);
        }
    }

    /** 推送结束信令 */
    private void pushEndNotification(ImCallSessionBO session, Long operatorId,
                                     ImCallEndReasonEnum reason, boolean roomTerminated) {
        // TODO @AI：链式调用，相同的在一行；
        ImRtcEndNotification payload = new ImRtcEndNotification();
        payload.setCallId(session.getCallId());
        payload.setRoomName(session.getRoomName());
        payload.setOperatorId(operatorId);
        payload.setReason(reason.getType());
        if (roomTerminated && session.getStartTime() != null) {
            payload.setDurationSeconds((System.currentTimeMillis() - session.getStartTime()) / 1000L);
        }
        Set<Long> targets = roomTerminated
                ? session.getAllUserIds()
                : CollUtil.newLinkedHashSet(session.getInviterId());
        for (Long target : targets) {
            // TODO @AI：链式调用，相同的在一行；
            ImPrivateMessageDTO dto = new ImPrivateMessageDTO()
                    .setType(ImMessageTypeEnum.RTC_END.getType())
                    .setSenderId(operatorId == null ? session.getInviterId() : operatorId)
                    .setReceiverId(target)
                    .setContent(JsonUtils.toJsonString(payload))
                    .setSendTime(LocalDateTime.now());
            webSocketService.sendPrivateMessageAsync(target, dto);
        }
    }

    // TODO @AI：注释风格；
    /** 关闭会话：状态切 ENDED → 推 RTC_END → 群通话再向全员广播 RTC_GROUP_ENDED → 从内存移除 */
    private void endSession(ImCallSessionBO session, Long operatorId, ImCallEndReasonEnum reason) {
        session.setStatus(ImCallStatusEnum.ENDED.getType());
        pushEndNotification(session, operatorId, reason, true);
        if (ImConversationTypeEnum.isGroup(session.getScene())) {
            pushGroupBroadcast(session, ImMessageTypeEnum.RTC_GROUP_ENDED.getType(), operatorId);
        }
        sessionStore.remove(session.getRoomName());
        log.info("[endSession][roomName={} operator={} reason={}]",
                session.getRoomName(), operatorId, reason);
    }

    /**
     * 群通话广播：给该群当前所有活跃成员推一条信号；用于胶囊条「N 人正在通话 / 开始 / 结束」实时同步
     *
     * @param session     会话；必须是群通话
     * @param messageType 信号类型；取自 RTC_GROUP_STARTED / RTC_GROUP_UPDATED / RTC_GROUP_ENDED
     * @param operatorId  触发本次广播的操作人；STARTED 为发起人，ENDED 为关房操作者，UPDATED 为加入 / 离开者
     */
    private void pushGroupBroadcast(ImCallSessionBO session, Integer messageType, Long operatorId) {
        if (!ImConversationTypeEnum.isGroup(session.getScene()) || session.getGroupId() == null) {
            return;
        }
        // TODO @AI：链式调用，相同的在一行；
        ImRtcGroupNotification payload = new ImRtcGroupNotification();
        payload.setCallId(session.getCallId());
        payload.setRoomName(session.getRoomName());
        payload.setGroupId(session.getGroupId());
        payload.setMediaType(session.getMediaType());
        payload.setInviterId(session.getInviterId());
        payload.setJoinedUserIds(new LinkedHashSet<>(session.getJoinedUserIds()));
        if (session.getInviteeIds() != null) {
            payload.setInviteeIds(new LinkedHashSet<>(session.getInviteeIds()));
        }
        Long senderId = operatorId != null ? operatorId : session.getInviterId();
        String content = JsonUtils.toJsonString(payload);
        LocalDateTime now = LocalDateTime.now();
        List<ImGroupMemberDO> members = groupMemberService
                .getActiveGroupMemberListByGroupId(session.getGroupId());
        for (ImGroupMemberDO member : members) {
            ImPrivateMessageDTO dto = new ImPrivateMessageDTO()
                    .setType(messageType)
                    .setSenderId(senderId)
                    .setReceiverId(member.getUserId())
                    .setContent(content)
                    .setSendTime(now);
            webSocketService.sendPrivateMessageAsync(member.getUserId(), dto);
        }
    }

    // TODO @AI：注释风格；
    /** 根据会话取出（不含 token 的）基础 VO */
    private ImRtcCallRespVO toBaseVO(ImCallSessionBO session) {
        return BeanUtils.toBean(session, ImRtcCallRespVO.class)
                .setLivekitUrl(imProperties.getRtc().getLivekitUrl());
    }

    /** 构造完整响应：含 token + livekitUrl + newCreated */
    // TODO @AI：controller 组装；
    private ImRtcCallRespVO buildResp(ImCallSessionBO session, Long userId, boolean newCreated) {
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        String displayName = user == null ? null : user.getNickname();
        ImRtcCallRespVO vo = toBaseVO(session);
        vo.setToken(signToken(userId, displayName, session.getRoomName()));
        vo.setNewCreated(newCreated);
        return vo;
    }

    /** 签 LiveKit Token；identity 用 userId 字符串保证多端唯一 */
    private String signToken(Long userId, String displayName, String roomName) {
        ImProperties.Rtc cfg = imProperties.getRtc();
        return LiveKitTokenUtils.signJoinToken(
                cfg.getApiKey(), cfg.getApiSecret(),
                String.valueOf(userId), displayName,
                roomName, Duration.ofHours(cfg.getTokenTtlHours()));
    }

    // TODO @AI：方法名，改成项目里，符合预期的；
    /** 拉取必存在的会话；不存在直接抛 */
    private ImCallSessionBO requireSession(String roomName) {
        ImCallSessionBO session = sessionStore.get(roomName);
        if (session == null) {
            throw exception(RTC_SESSION_NOT_EXISTS);
        }
        return session;
    }

}
