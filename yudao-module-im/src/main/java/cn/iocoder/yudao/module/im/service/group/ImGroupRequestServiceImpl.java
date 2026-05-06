package cn.iocoder.yudao.module.im.service.group;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.request.ImGroupRequestApplyReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.group.vo.ImGroupRequestManagerPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupRequestDO;
import cn.iocoder.yudao.module.im.dal.mysql.group.ImGroupRequestMapper;
import cn.iocoder.yudao.module.im.enums.group.ImGroupAddSourceEnum;
import cn.iocoder.yudao.module.im.enums.group.ImGroupMemberRoleEnum;
import cn.iocoder.yudao.module.im.enums.group.ImGroupRequestHandleResultEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.service.message.ImGroupMessageService;
import cn.iocoder.yudao.module.im.service.message.dto.ImGroupMessageSendDTO;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImPrivateMessageDTO;
import cn.iocoder.yudao.module.im.service.websocket.dto.notification.group.BaseGroupNotification;
import cn.iocoder.yudao.module.im.service.websocket.dto.notification.group.GroupRequestApprovedNotification;
import cn.iocoder.yudao.module.im.service.websocket.dto.notification.group.GroupRequestReceivedNotification;
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
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;

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

        // 2. 情况一：群未开启审批，直接入群；写群成员留痕 + 推 1510 全员广播；不落申请记录
        if (!Boolean.TRUE.equals(group.getJoinApproval())) {
            // 入群前校验人数上限
            groupService.validateMemberCountLimit(groupId, 1);
            // 写群成员；addSource 来自前端入口（搜索 / 二维码 / 分享链接），inviterUserId=null（主动申请）
            groupMemberService.addGroupMember(groupId, userId,
                    ImGroupMemberRoleEnum.NORMAL.getRole(), reqVO.getAddSource(), null);
            // 推 1510 给全员；payload 含进群者 + 来源，前端按 entrantUserId 局部插入新成员
            groupMessageService.sendGroupMessage(userId,
                    ImGroupMessageSendDTO.ofGroupMemberEnter(groupId, userId, reqVO.getAddSource()));
            return null;
        }

        // 3. 情况二：群开启了审批；upsert 一条「主动申请」记录，inviterUserId=null
        // 已有记录（无论之前是未处理 / 已同意 / 已拒绝），覆盖申请理由 + 来源 + 重置为未处理 + 清空旧处理痕迹
        // null 字段（inviterUserId / handleUserId / handleContent / handleTime）走 LambdaUpdateWrapper.set 显式清空，updateById 默认会忽略 null
        // TODO @AI：看看是不是在 basemapperx 里，增加一个 updateXXXX；可以根据传递的 DO，深度更新的方法？应该匹配这个场景的对哇？【主要希望 service 不要出现 mapper 相关的类】
        ImGroupRequestDO request = groupRequestMapper.selectByGroupIdAndUserId(groupId, userId);
        if (request != null) {
            // update_time 显式置当前时间：update(null, wrapper) 不会触发 MetaObjectHandler.updateFill；
            // 列表查询按 update_time 倒序排，复用旧记录时必须刷新这一列才能让本次重新提交排到最前
            LocalDateTime now = LocalDateTime.now();
            groupRequestMapper.update(null, new LambdaUpdateWrapper<ImGroupRequestDO>()
                    .eq(ImGroupRequestDO::getId, request.getId())
                    .set(ImGroupRequestDO::getApplyContent, reqVO.getApplyContent())
                    .set(ImGroupRequestDO::getAddSource, reqVO.getAddSource())
                    .set(ImGroupRequestDO::getHandleResult, ImGroupRequestHandleResultEnum.UNHANDLED.getResult())
                    .set(ImGroupRequestDO::getInviterUserId, null)
                    .set(ImGroupRequestDO::getHandleUserId, null)
                    .set(ImGroupRequestDO::getHandleContent, null)
                    .set(ImGroupRequestDO::getHandleTime, null)
                    .set(ImGroupRequestDO::getUpdateTime, now));
            request.setApplyContent(reqVO.getApplyContent()).setAddSource(reqVO.getAddSource())
                    .setHandleResult(ImGroupRequestHandleResultEnum.UNHANDLED.getResult())
                    .setInviterUserId(null).setHandleUserId(null)
                    .setHandleContent(null).setHandleTime(null).setUpdateTime(now);
        } else {
            request = BeanUtils.toBean(reqVO, ImGroupRequestDO.class)
                    .setUserId(userId).setInviterUserId(null)
                    .setHandleResult(ImGroupRequestHandleResultEnum.UNHANDLED.getResult());
            groupRequestMapper.insert(request);
        }

        // 4. 1503 私聊定向推群主 + 全部管理员（多端同步）；payload 携带申请方昵称 / 头像
        AdminUserRespDTO applyUser = adminUserApi.getUser(userId);
        GroupRequestReceivedNotification payload = buildRequestNotification(group, request, applyUser);
        for (Long receiverUserId : getGroupMemberListByOwnerAndAdminUserIds(group)) {
            websocketService.sendPrivateMessageAsync(receiverUserId, ImPrivateMessageDTO.ofGroupNotification(
                    ImMessageTypeEnum.GROUP_REQUEST_RECEIVED.getType(), userId, receiverUserId, payload));
        }
        return request;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agreeGroupRequest(Long userId, Long requestId) {
        // 1. 校验申请存在 + 未处理 + 操作人是 owner / admin
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

        // 4. 写群成员；addSource / inviterUserId 沿用申请记录上的来源信息
        groupMemberService.addGroupMember(request.getGroupId(), request.getUserId(),
                ImGroupMemberRoleEnum.NORMAL.getRole(), request.getAddSource(), request.getInviterUserId());

        // 5.1 1505 私聊推送给申请人 + 群主 + 全部管理员（每端单推）
        GroupRequestApprovedNotification payload = (GroupRequestApprovedNotification) new GroupRequestApprovedNotification()
                .setRequestId(request.getId()).setGroupId(request.getGroupId()).setUserId(request.getUserId())
                .setOperatorUserId(userId);
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
        GroupRequestRejectedNotification payload = (GroupRequestRejectedNotification) new GroupRequestRejectedNotification()
                .setRequestId(request.getId()).setGroupId(request.getGroupId()).setUserId(request.getUserId())
                .setHandleContent(handleContent).setOperatorUserId(userId);
        broadcastToOwnerAdminsAndApplicant(request.getGroupId(), request.getUserId(), payload,
                ImMessageTypeEnum.GROUP_REQUEST_REJECTED.getType(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createInviteRequestList(Long groupId, Long inviterUserId, Collection<Long> invitedUserIds) {
        if (CollUtil.isEmpty(invitedUserIds)) {
            return;
        }
        ImGroupDO group = groupService.validateGroupExists(groupId);

        // 1. 每个被邀请人 upsert 一条 inviter_user_id=邀请人 的记录
        // 已有记录（同一对 group_id, user_id 唯一）：覆盖 inviter_user_id / add_source + 重置 handle_result + 清空旧处理痕迹（走 LambdaUpdateWrapper.set 显式置 null）
        // add_source 强制覆写为 INVITE，避免审批通过后把残留的旧来源回写进群成员留痕
        Integer inviteSource = ImGroupAddSourceEnum.INVITE.getSource();
        List<ImGroupRequestDO> requests = new ArrayList<>(invitedUserIds.size());
        // TODO @AI：看看是不是在 basemapperx 里，增加一个 updateXXXX；可以根据传递的 DO，深度更新的方法？应该匹配这个场景的对哇？【主要希望 service 不要出现 mapper 相关的类】
        // 同 applyJoinGroup：update(null, wrapper) 不走 MetaObjectHandler.updateFill，复用旧记录必须显式刷 update_time，否则按 update_time 倒序排不到最前
        LocalDateTime now = LocalDateTime.now();
        for (Long invitedUserId : invitedUserIds) {
            ImGroupRequestDO existing = groupRequestMapper.selectByGroupIdAndUserId(groupId, invitedUserId);
            if (existing != null) {
                groupRequestMapper.update(null, new LambdaUpdateWrapper<ImGroupRequestDO>()
                        .eq(ImGroupRequestDO::getId, existing.getId())
                        .set(ImGroupRequestDO::getInviterUserId, inviterUserId)
                        .set(ImGroupRequestDO::getAddSource, inviteSource)
                        .set(ImGroupRequestDO::getHandleResult, ImGroupRequestHandleResultEnum.UNHANDLED.getResult())
                        .set(ImGroupRequestDO::getHandleUserId, null)
                        .set(ImGroupRequestDO::getHandleContent, null)
                        .set(ImGroupRequestDO::getHandleTime, null)
                        .set(ImGroupRequestDO::getUpdateTime, now));
                existing.setInviterUserId(inviterUserId).setAddSource(inviteSource)
                        .setHandleResult(ImGroupRequestHandleResultEnum.UNHANDLED.getResult())
                        .setHandleUserId(null).setHandleContent(null).setHandleTime(null)
                        .setUpdateTime(now);
                requests.add(existing);
                continue;
            }
            ImGroupRequestDO insert = new ImGroupRequestDO()
                    .setGroupId(groupId).setUserId(invitedUserId).setInviterUserId(inviterUserId)
                    .setAddSource(inviteSource)
                    .setHandleResult(ImGroupRequestHandleResultEnum.UNHANDLED.getResult());
            groupRequestMapper.insert(insert);
            requests.add(insert);
        }

        // 2. 推 1503 给群主 + 全部管理员；多端同步；每条申请单独推一帧
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(invitedUserIds);
        List<Long> ownerAndAdmins = getGroupMemberListByOwnerAndAdminUserIds(group);
        for (ImGroupRequestDO request : requests) {
            AdminUserRespDTO applyUser = userMap.get(request.getUserId());
            GroupRequestReceivedNotification payload = buildRequestNotification(group, request, applyUser);
            for (Long receiverUserId : ownerAndAdmins) {
                websocketService.sendPrivateMessageAsync(receiverUserId, ImPrivateMessageDTO.ofGroupNotification(
                        ImMessageTypeEnum.GROUP_REQUEST_RECEIVED.getType(), inviterUserId, receiverUserId, payload));
            }
        }
    }

    @Override
    public List<ImGroupRequestDO> getUnhandledRequestListByOwnerOrAdmin(Long userId) {
        // 1. 找出当前用户作为 OWNER / ADMIN 的所有群
        List<ImGroupMemberDO> myMembers = groupMemberService.getActiveGroupMemberListByUserId(userId);
        Set<Long> ownerOrAdminGroupIds = convertSet(myMembers,
                ImGroupMemberDO::getGroupId,
                m -> ImGroupMemberRoleEnum.isOwnerOrAdmin(m.getRole()));
        if (CollUtil.isEmpty(ownerOrAdminGroupIds)) {
            return Collections.emptyList();
        }
        // 2. 一次拉所有群的未处理申请
        return groupRequestMapper.selectListByGroupIdsAndHandleResult(
                ownerOrAdminGroupIds, ImGroupRequestHandleResultEnum.UNHANDLED.getResult());
    }

    @Override
    public List<ImGroupRequestDO> getGroupRequestListByGroupId(Long userId, Long groupId) {
        // 1. 校验群存在 + 当前用户是群主 / 管理员
        groupService.validateGroupExists(groupId);
        ImGroupMemberDO operator = groupMemberService.validateMemberInGroup(groupId, userId);
        if (!ImGroupMemberRoleEnum.isOwnerOrAdmin(operator.getRole())) {
            throw exception(GROUP_REQUEST_NOT_TO_ME);
        }
        // 2. 拉取该群下全部申请（含已处理）；按 id 倒序，前端首条卡片化展示
        return groupRequestMapper.selectListByGroupId(groupId);
    }

    @Override
    public ImGroupRequestDO getGroupRequest(Long id) {
        return groupRequestMapper.selectById(id);
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
    private GroupRequestReceivedNotification buildRequestNotification(ImGroupDO group, ImGroupRequestDO request,
                                                                      AdminUserRespDTO applyUser) {
        Long operatorUserId = request.getInviterUserId() != null ? request.getInviterUserId() : request.getUserId();
        GroupRequestReceivedNotification payload = (GroupRequestReceivedNotification) new GroupRequestReceivedNotification()
                .setRequestId(request.getId()).setGroupId(group.getId()).setUserId(request.getUserId())
                .setInviterUserId(request.getInviterUserId())
                .setApplyContent(request.getApplyContent()).setAddSource(request.getAddSource())
                .setOperatorUserId(operatorUserId);
        if (applyUser != null) {
            payload.setUserNickname(applyUser.getNickname()).setUserAvatar(applyUser.getAvatar());
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
        Set<Long> receivers = new LinkedHashSet<>(getGroupMemberListByOwnerAndAdminUserIds(group));
        receivers.add(applicantUserId);
        for (Long receiverUserId : receivers) {
            websocketService.sendPrivateMessageAsync(receiverUserId, ImPrivateMessageDTO.ofGroupNotification(
                    messageType, operatorUserId, receiverUserId, payload));
        }
    }

    /**
     * 列出群主 + 全部管理员的用户编号
     *
     * @param group 群信息
     * @return 群主 + 全部管理员的用户编号列表
     */
    private List<Long> getGroupMemberListByOwnerAndAdminUserIds(ImGroupDO group) {
        return convertList(groupMemberService.getGroupMemberListByOwnerAndAdmin(group.getId()),
                ImGroupMemberDO::getUserId);
    }

}
