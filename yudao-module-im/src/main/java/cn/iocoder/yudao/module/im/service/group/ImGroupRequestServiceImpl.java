package cn.iocoder.yudao.module.im.service.group;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.request.ImGroupRequestApplyReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.group.vo.ImGroupRequestManagerPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupRequestDO;
import cn.iocoder.yudao.module.im.dal.mysql.group.ImGroupRequestMapper;
import cn.iocoder.yudao.module.im.enums.group.ImGroupJoinTypeEnum;
import cn.iocoder.yudao.module.im.enums.group.ImGroupMemberRoleEnum;
import cn.iocoder.yudao.module.im.enums.group.ImGroupRequestHandleResultEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.service.message.ImGroupMessageService;
import cn.iocoder.yudao.module.im.service.message.dto.ImGroupMessageSendDTO;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImPrivateMessageDTO;
import cn.iocoder.yudao.module.im.service.websocket.dto.notification.group.BaseGroupNotification;
import cn.iocoder.yudao.module.im.service.websocket.dto.notification.group.GroupRequestApprovedNotification;
import cn.iocoder.yudao.module.im.service.websocket.dto.notification.group.GroupRequestNotification;
import cn.iocoder.yudao.module.im.service.websocket.dto.notification.group.GroupRequestRejectedNotification;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;

// TODO @AI：payload 能不能改成 notification 更像通知一点。
/**
 * IM 加群申请 Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class ImGroupRequestServiceImpl implements ImGroupRequestService {

    @Resource
    private ImGroupRequestMapper groupRequestMapper;

    @Resource
    @Lazy // 避免循环依赖
    private ImGroupService groupService;
    @Resource
    @Lazy // 避免循环依赖
    private ImGroupMemberService groupMemberService;
    @Resource
    @Lazy // 避免循环依赖
    private ImGroupMessageService groupMessageService;

    @Resource
    private ImWebSocketService websocketService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImGroupRequestDO applyJoinGroup(Long userId, ImGroupRequestApplyReqVO reqVO) {
        Long groupId = reqVO.getGroupId();
        // 1.1 校验群存在 + 未封禁 / 未解散
        ImGroupDO group = groupService.validateGroupExists(groupId);
        // 1.2 校验未在群中
        ImGroupMemberDO member = groupMemberService.getGroupMember(groupId, userId);
        if (member != null && !CommonStatusEnum.DISABLE.getStatus().equals(member.getStatus())) {
            throw exception(GROUP_REQUEST_ALREADY_MEMBER);
        }

        // 2. FREE 模式：直接入群 + 推 1510 全员广播；不落申请记录
        if (ImGroupJoinTypeEnum.isFree(group.getJoinType())) {
            // TODO @AI：这里还是写下注释；每一行的；
            groupService.validateMemberCountLimit(groupId, 1);
            groupMemberService.addGroupMember(groupId, userId,
                    ImGroupMemberRoleEnum.NORMAL.getRole(), reqVO.getAddSource(), null);
            groupMessageService.sendGroupMessage(userId,
                    ImGroupMessageSendDTO.ofGroupMemberEnter(groupId, userId, reqVO.getAddSource()));
            return null;
        }

        // 3. 非 FREE 模式：复用未处理记录或新建
        ImGroupRequestDO request = groupRequestMapper.selectLatestPendingByGroupIdAndUserIdAndInviter(
                groupId, userId, null);
        if (request != null) {
            ImGroupRequestDO updateObj = new ImGroupRequestDO()
                    .setApplyContent(reqVO.getApplyContent()).setAddSource(reqVO.getAddSource());
            int affected = groupRequestMapper.updateByIdAndHandleResult(request.getId(),
                    ImGroupRequestHandleResultEnum.UNHANDLED.getResult(), updateObj);
            if (affected == 0) {
                // 并发场景另一管理端刚处理过，提示重试
                throw exception(GROUP_REQUEST_HANDLED);
            }
            BeanUtil.copyProperties(reqVO, request, CopyOptions.create().setIgnoreNullValue(true));
        } else {
            request = BeanUtils.toBean(reqVO, ImGroupRequestDO.class)
                    .setUserId(userId).setInviterUserId(null)
                    .setHandleResult(ImGroupRequestHandleResultEnum.UNHANDLED.getResult());
            groupRequestMapper.insert(request);
        }

        // 4. 1503 私聊定向推群主 + 全部管理员（多端同步）；payload 携带申请方昵称 / 头像
        AdminUserRespDTO applyUser = adminUserApi.getUser(userId);
        GroupRequestNotification payload = buildRequestNotification(group, request, applyUser);
        for (Long receiverUserId : listOwnerAndAdminUserIds(group)) {
            // TODO @AI：能不能发群的，只发给群管理员；就是 groupMessageService；只是不 persist 呀。
            websocketService.sendPrivateMessageAsync(receiverUserId, ImPrivateMessageDTO.ofGroupNotification(
                    ImMessageTypeEnum.GROUP_REQUEST_RECEIVED.getType(), userId, receiverUserId, payload));
        }
        return request;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agreeGroupRequest(Long userId, Long requestId) {
        // 1. 校验申请存在 + 未处理 + 操作人是 owner/admin
        ImGroupRequestDO request = validateRequestForHandle(userId, requestId);
        // 2. 入群前校验人数上限；群已满抛错让操作人选择拒绝
        groupService.validateMemberCountLimit(request.getGroupId(), 1);

        // 3. 乐观锁推进状态
        LocalDateTime now = LocalDateTime.now();
        ImGroupRequestDO updateObj = new ImGroupRequestDO()
                .setHandleResult(ImGroupRequestHandleResultEnum.AGREED.getResult())
                .setHandleUserId(userId).setHandleTime(now);
        int affected = groupRequestMapper.updateByIdAndHandleResult(request.getId(),
                ImGroupRequestHandleResultEnum.UNHANDLED.getResult(), updateObj);
        if (affected == 0) {
            throw exception(GROUP_REQUEST_HANDLED);
        }
        request.setHandleResult(ImGroupRequestHandleResultEnum.AGREED.getResult())
                .setHandleUserId(userId).setHandleTime(now);

        // 4. 写群成员；带 addSource / inviterUserId
        groupMemberService.addGroupMember(request.getGroupId(), request.getUserId(),
                ImGroupMemberRoleEnum.NORMAL.getRole(), request.getAddSource(), request.getInviterUserId());

        // 5.1 1505 私聊推送给申请人 + 群主 + 全部管理员（每端单推）
        GroupRequestApprovedNotification payload = new GroupRequestApprovedNotification();
        // TODO @AI：能不能改成链式调用；
        payload.setRequestId(request.getId());
        payload.setGroupId(request.getGroupId());
        payload.setUserId(request.getUserId());
        payload.setOperatorUserId(userId);
        broadcastToOwnerAdminsAndApplicant(request.getGroupId(), request.getUserId(), payload,
                ImMessageTypeEnum.GROUP_REQUEST_APPROVED.getType(), userId);
        // 5.2 群事件：主动申请 → 1510 自由进群；被邀请 → 1509 成员加入
        if (request.getInviterUserId() == null) {
            groupMessageService.sendGroupMessage(userId,
                    ImGroupMessageSendDTO.ofGroupMemberEnter(request.getGroupId(),
                            request.getUserId(), request.getAddSource()));
        } else {
            groupMessageService.sendGroupMessage(userId,
                    ImGroupMessageSendDTO.ofGroupMemberInvite(request.getGroupId(),
                            request.getInviterUserId(), Collections.singleton(request.getUserId())));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refuseGroupRequest(Long userId, Long requestId, String handleContent) {
        // 1. 校验
        ImGroupRequestDO request = validateRequestForHandle(userId, requestId);

        // 2. 乐观锁推进
        ImGroupRequestDO updateObj = new ImGroupRequestDO()
                .setHandleResult(ImGroupRequestHandleResultEnum.REFUSED.getResult())
                .setHandleContent(handleContent)
                .setHandleUserId(userId).setHandleTime(LocalDateTime.now());
        int affected = groupRequestMapper.updateByIdAndHandleResult(request.getId(),
                ImGroupRequestHandleResultEnum.UNHANDLED.getResult(), updateObj);
        if (affected == 0) {
            throw exception(GROUP_REQUEST_HANDLED);
        }

        // 3. 1506 私聊推送给申请人 + 群主 + 全部管理员
        GroupRequestRejectedNotification payload = new GroupRequestRejectedNotification();
        payload.setRequestId(request.getId());
        payload.setGroupId(request.getGroupId());
        payload.setUserId(request.getUserId());
        payload.setHandleContent(handleContent);
        payload.setOperatorUserId(userId);
        broadcastToOwnerAdminsAndApplicant(request.getGroupId(), request.getUserId(), payload,
                ImMessageTypeEnum.GROUP_REQUEST_REJECTED.getType(), userId);
    }

    // TODO @AI：createInviteRequestList；习惯性命名；
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createInviteRequests(Long groupId, Long inviterUserId, Collection<Long> invitedUserIds) {
        if (CollUtil.isEmpty(invitedUserIds)) {
            return;
        }
        ImGroupDO group = groupService.validateGroupExists(groupId);

        // TODO @AI：= 两侧，最好有空格；看看有没别的地方，也要改的；
        // 1. 复用或新建 inviter_user_id=inviterUserId 的待审批记录
        List<ImGroupRequestDO> requests = new ArrayList<>(invitedUserIds.size());
        for (Long invitedUserId : invitedUserIds) {
            ImGroupRequestDO existing = groupRequestMapper.selectLatestPendingByGroupIdAndUserIdAndInviter(
                    groupId, invitedUserId, inviterUserId);
            if (existing != null) {
                requests.add(existing);
                continue;
            }
            ImGroupRequestDO insert = new ImGroupRequestDO()
                    .setGroupId(groupId).setUserId(invitedUserId).setInviterUserId(inviterUserId)
                    .setHandleResult(ImGroupRequestHandleResultEnum.UNHANDLED.getResult());
            groupRequestMapper.insert(insert);
            requests.add(insert);
        }

        // 2. 推 1503 给群主 + 全部管理员；多端同步；每条申请单独推一帧
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(invitedUserIds);
        List<Long> ownerAndAdmins = listOwnerAndAdminUserIds(group);
        for (ImGroupRequestDO request : requests) {
            AdminUserRespDTO applyUser = userMap.get(request.getUserId());
            // TODO @AI：GroupRequestNotification 是不是要调整下命名？因为它是 RECEIVED 的，不然显得太公用了。
            GroupRequestNotification payload = buildRequestNotification(group, request, applyUser);
            for (Long receiverUserId : ownerAndAdmins) {
                websocketService.sendPrivateMessageAsync(receiverUserId, ImPrivateMessageDTO.ofGroupNotification(
                        ImMessageTypeEnum.GROUP_REQUEST_RECEIVED.getType(), inviterUserId, receiverUserId, payload));
            }
        }
    }

    @Override
    public List<ImGroupRequestDO> getMyGroupRequestList(Long userId, Long lastRequestId, Integer limit) {
        return groupRequestMapper.selectMyList(userId, lastRequestId, limit);
    }

    @Override
    public List<ImGroupRequestDO> getPendingGroupRequestList(Long userId, Long groupId,
                                                             Long lastRequestId, Integer limit) {
        // 仅群主 / 管理员可拉
        ImGroupMemberDO member = groupMemberService.validateMemberInGroup(groupId, userId);
        if (!ImGroupMemberRoleEnum.isOwnerOrAdmin(member.getRole())) {
            throw exception(GROUP_REQUEST_NOT_TO_ME);
        }
        return groupRequestMapper.selectPendingListByGroupId(groupId, lastRequestId, limit);
    }

    @Override
    public ImGroupRequestDO getGroupRequest(Long id) {
        return groupRequestMapper.selectById(id);
    }

    // TODO @AI：【后面点改】应该批量查询；不然这样性能太差了。如果群太多；
    @Override
    public Map<Long, Long> getPendingCountMap(Collection<Long> groupIds) {
        if (CollUtil.isEmpty(groupIds)) {
            return Collections.emptyMap();
        }
        Map<Long, Long> result = new HashMap<>(groupIds.size());
        for (Long groupId : groupIds) {
            result.put(groupId, groupRequestMapper.selectPendingCountByGroupId(groupId));
        }
        return result;
    }

    @Override
    public PageResult<ImGroupRequestDO> getGroupRequestPage(ImGroupRequestManagerPageReqVO reqVO) {
        return groupRequestMapper.selectPage(reqVO);
    }

    /**
     * 校验申请可被「当前用户」处理：申请存在 + 未处理 + 操作人是群主 / 管理员
     */
    private ImGroupRequestDO validateRequestForHandle(Long userId, Long requestId) {
        ImGroupRequestDO request = groupRequestMapper.selectById(requestId);
        if (request == null) {
            throw exception(GROUP_REQUEST_NOT_EXISTS);
        }
        if (!ImGroupRequestHandleResultEnum.isUnhandled(request.getHandleResult())) {
            throw exception(GROUP_REQUEST_HANDLED);
        }
        ImGroupMemberDO operator = groupMemberService.validateMemberInGroup(request.getGroupId(), userId);
        if (!ImGroupMemberRoleEnum.isOwnerOrAdmin(operator.getRole())) {
            throw exception(GROUP_REQUEST_NOT_TO_ME);
        }
        return request;
    }

    /**
     * 构建 1503 通知 payload；聚合申请方昵称 / 头像供前端直接渲染
     */
    private GroupRequestNotification buildRequestNotification(ImGroupDO group, ImGroupRequestDO request,
                                                              AdminUserRespDTO applyUser) {
        // TODO @AI：看看能不能链式调用；
        GroupRequestNotification payload = new GroupRequestNotification();
        payload.setRequestId(request.getId());
        payload.setGroupId(group.getId());
        payload.setUserId(request.getUserId());
        payload.setInviterUserId(request.getInviterUserId());
        payload.setApplyContent(request.getApplyContent());
        payload.setAddSource(request.getAddSource());
        payload.setOperatorUserId(request.getInviterUserId() != null ? request.getInviterUserId() : request.getUserId());
        if (applyUser != null) {
            payload.setUserNickname(applyUser.getNickname());
            payload.setUserAvatar(applyUser.getAvatar());
        }
        return payload;
    }

    /**
     * 1505 / 1506 受众：申请人 + 群主 + 全部管理员；每端单独推一帧，前端按 receiver 是否申请人区分文案
     */
    private void broadcastToOwnerAdminsAndApplicant(Long groupId, Long applicantUserId, BaseGroupNotification payload,
                                                    Integer messageType, Long operatorUserId) {
        ImGroupDO group = groupService.getGroup(groupId);
        if (group == null) {
            return;
        }
        Set<Long> receivers = new LinkedHashSet<>(listOwnerAndAdminUserIds(group));
        receivers.add(applicantUserId);
        // TODO @AI：看看能不能用群的通道；
        for (Long receiverUserId : receivers) {
            websocketService.sendPrivateMessageAsync(receiverUserId, ImPrivateMessageDTO.ofGroupNotification(
                    messageType, operatorUserId, receiverUserId, payload));
        }
    }

    // TODO @AI：这个方法，搞到到 ImGroupMemberService 里去；getGroupMemberListByOwnerAndAdmin；返回 list《do》；
    /**
     * 列出群主 + 全部管理员的用户编号；activeMembers 通常 ≤ 500 + admin 上限 3，过滤成本可接受
     */
    private List<Long> listOwnerAndAdminUserIds(ImGroupDO group) {
        List<ImGroupMemberDO> activeMembers = groupMemberService.getActiveGroupMemberListByGroupId(group.getId());
        Set<Long> set = new LinkedHashSet<>();
        set.add(group.getOwnerUserId());
        for (ImGroupMemberDO member : activeMembers) {
            if (ImGroupMemberRoleEnum.isOwnerOrAdmin(member.getRole())) {
                set.add(member.getUserId());
            }
        }
        return new ArrayList<>(set);
    }

}
