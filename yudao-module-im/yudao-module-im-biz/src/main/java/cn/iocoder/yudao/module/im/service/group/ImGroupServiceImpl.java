package cn.iocoder.yudao.module.im.service.group;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupCreateReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupUpdateReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberInviteReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberRemoveReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberUpdateReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.mysql.group.ImGroupMapper;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import cn.iocoder.yudao.module.im.service.message.ImGroupMessageService;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImGroupMessageDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.im.dal.redis.RedisKeyConstants.GROUP;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.im.enums.ImCommonConstants.*;

// TODO @芋艿：群主的调整，暂时不考虑。后续在弄；
/**
 * 群 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ImGroupServiceImpl implements ImGroupService {

    @Resource
    private ImGroupMapper groupMapper;

    @Resource
    @Lazy // 避免循环依赖
    private ImGroupMemberService groupMemberService;
    @Resource
    @Lazy // 避免循环依赖
    private ImGroupMessageService groupMessageService;
    @Resource
    private ImWebSocketService webSocketService;

    @Resource
    private ImFriendService friendService;

    @Resource
    private AdminUserApi adminUserApi;

    // ==================== 群的写操作 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImGroupDO createGroup(ImGroupCreateReqVO createReqVO, Long userId) {
        // 1. 插入群记录
        ImGroupDO group = BeanUtils.toBean(createReqVO, ImGroupDO.class)
                .setOwnerUserId(userId).setStatus(CommonStatusEnum.ENABLE.getStatus());
        groupMapper.insert(group);

        // 2. 将群主插入为群成员
        groupMemberService.addGroupMember(group.getId(), userId);

        // 3. 推送群创建事件给群主（多端同步）
        webSocketService.sendGroupMessageAsync(userId,
                ImGroupMessageDTO.ofGroupCreate(userId, group.getId()));
        return group;
    }

    @Override
    @CacheEvict(cacheNames = GROUP, key = "#updateReqVO.id")
    @Transactional(rollbackFor = Exception.class)
    public ImGroupDO updateGroup(ImGroupUpdateReqVO updateReqVO, Long userId) {
        // 1. 校验群存在
        ImGroupDO group = validateGroupExists(updateReqVO.getId());

        // 2. 更新群信息（name、avatar、notice → im_group 表）
        boolean hasGroupInfoChange = !ObjUtil.isAllEmpty(
                updateReqVO.getName(), updateReqVO.getAvatar(), updateReqVO.getNotice());
        if (hasGroupInfoChange) {
            if (ObjUtil.notEqual(group.getOwnerUserId(), userId)) {
                throw exception(GROUP_NOT_OWNER);
            }
            // 2.1 更新数据库
            ImGroupDO updateObj = BeanUtils.toBean(updateReqVO, ImGroupDO.class);
            groupMapper.updateById(updateObj);
            // 同步到内存中的 group 对象，避免再次查询
            BeanUtil.copyProperties(updateReqVO, group, CopyOptions.create().ignoreNullValue());

            // 2.2 群信息的变更，推送给所有群成员
            List<ImGroupMemberDO> members = groupMemberService.getActiveGroupMemberListByGroupId(group.getId());
            Set<Long> memberUserIds = convertSet(members, ImGroupMemberDO::getUserId);
            webSocketService.sendGroupMessageAsync(memberUserIds,
                    ImGroupMessageDTO.ofGroupUpdate(userId, group.getId()));
        }

        // 3. 更新当前用户的群成员信息（displayUserName、displayGroupName → im_group_member 表）
        if (!ObjUtil.isAllEmpty(updateReqVO.getDisplayUserName(), updateReqVO.getDisplayGroupName())) {
            groupMemberService.updateGroupMember(userId, new ImGroupMemberUpdateReqVO()
                    .setGroupId(updateReqVO.getId()).setDisplayUserName(updateReqVO.getDisplayUserName())
                    .setDisplayGroupName(updateReqVO.getDisplayGroupName()));
        }
        return group;
    }

    @Override
    @CacheEvict(cacheNames = GROUP, key = "#id")
    @Transactional(rollbackFor = Exception.class)
    public void dissolveGroup(Long id, Long userId) {
        // 1. 校验群存在 + 当前用户是群主
        validateGroupOwner(id, userId);

        // 2.1 获取群成员列表（在移除之前，后续推送需要）
        List<ImGroupMemberDO> members = groupMemberService.getActiveGroupMemberListByGroupId(id);
        Set<Long> memberUserIds = convertSet(members, ImGroupMemberDO::getUserId);
        // 2.2 更新群状态为已解散
        groupMapper.updateById(new ImGroupDO().setId(id)
                .setStatus(CommonStatusEnum.DISABLE.getStatus()).setDissolvedTime(LocalDateTime.now()));
        // 2.3 移除全部群成员
        groupMemberService.removeGroupMembersByGroupId(id);
        // 2.4 清理已读缓存
        groupMessageService.deleteReadMaxMessageIdMap(id);

        // 3.1 发送解散提示消息（TIP_TEXT）并推送
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        String tipContent = StrUtil.format(GROUP_DISSOLVE_TIP_MESSAGE, user.getNickname());
        groupMessageService.sendTipGroupMessage(userId, id, memberUserIds, tipContent);
        // 3.2 推送群删除事件给所有原群成员
        webSocketService.sendGroupMessageAsync(memberUserIds,
                ImGroupMessageDTO.ofGroupDelete(userId, id));
    }

    // ==================== 群成员的写操作 ====================

    @Override
    public void inviteGroupMember(Long userId, ImGroupMemberInviteReqVO inviteReqVO) {
        Long groupId = inviteReqVO.getGroupId();
        // 1.1 校验群存在 + 当前用户是群成员
        validateGroupExists(groupId);
        groupMemberService.validateMemberInGroup(groupId, userId);
        // 1.2 排除已在群中的用户
        List<ImGroupMemberDO> activeMembers = groupMemberService.getActiveGroupMemberListByGroupId(groupId);
        activeMembers.forEach(member -> inviteReqVO.getMemberUserIds().remove(member.getUserId()));
        if (CollUtil.isEmpty(inviteReqVO.getMemberUserIds())) {
            return;
        }
        // 1.3 校验被邀请人都是当前用户的好友
        List<Long> memberUserIds = inviteReqVO.getMemberUserIds();
        List<ImFriendDO> friends = friendService.getActiveFriendList(userId, memberUserIds);
        Set<Long> friendUserIds = convertSet(friends, ImFriendDO::getFriendUserId);
        Collection<Long> notFriendUserIds = CollUtil.subtract(memberUserIds, friendUserIds);
        if (CollUtil.isNotEmpty(notFriendUserIds)) {
            throw exception(GROUP_INVITE_NOT_FRIEND, getUserNicknames(notFriendUserIds));
        }
        // 1.4 校验群人数上限
        if (activeMembers.size() + memberUserIds.size() > MAX_GROUP_MEMBER) {
            throw exception(GROUP_MEMBER_EXCEED, MAX_GROUP_MEMBER);
        }

        // 2. 批量添加群成员
        groupMemberService.addGroupMembers(groupId, memberUserIds);

        // 3.1 推送群创建事件给被邀请人（让客户端新增群会话）
        webSocketService.sendGroupMessageAsync(new HashSet<>(memberUserIds),
                ImGroupMessageDTO.ofGroupCreate(userId, groupId));
        // 3.2 发送邀请提示消息给所有群成员
        String tipContent = StrUtil.format(GROUP_INVITE_TIP_MESSAGE,
                getUserNicknames(List.of(userId)), getUserNicknames(memberUserIds));
        List<ImGroupMemberDO> allMembers = groupMemberService.getActiveGroupMemberListByGroupId(groupId);
        Set<Long> allMemberUserIds = convertSet(allMembers, ImGroupMemberDO::getUserId);
        groupMessageService.sendTipGroupMessage(userId, groupId, allMemberUserIds, tipContent);
    }

    @Override
    public void quitGroup(Long groupId, Long userId) {
        // 1. 校验群存在
        ImGroupDO group = validateGroupExists(groupId);
        // 2. 群主不可退群
        if (ObjUtil.equal(group.getOwnerUserId(), userId)) {
            throw exception(GROUP_OWNER_CANNOT_QUIT);
        }

        // 3.1 移除群成员
        groupMemberService.removeGroupMember(groupId, userId);
        // 3.2 清理已读缓存
        groupMessageService.deleteReadMaxMessageId(groupId, userId);

        // 4.1 发送退群提示消息（TIP_TEXT）
        groupMessageService.sendTipGroupMessage(userId, groupId, Set.of(userId), GROUP_QUIT_TIP_MESSAGE);
        // 4.2 推送群删除事件
        webSocketService.sendGroupMessageAsync(Set.of(userId),
                ImGroupMessageDTO.ofGroupDelete(userId, groupId));
    }

    @Override
    public void removeGroupMember(Long userId, ImGroupMemberRemoveReqVO removeReqVO) {
        Long groupId = removeReqVO.getGroupId();
        // 1. 校验群存在 + 当前用户是群主
        validateGroupOwner(groupId, userId);
        // 2. 不能移除自己
        if (removeReqVO.getMemberUserIds().contains(userId)) {
            throw exception(GROUP_CANNOT_REMOVE_SELF);
        }

        // 3.1 批量移除群成员
        Set<Long> memberUserIds = new HashSet<>(removeReqVO.getMemberUserIds());
        groupMemberService.removeGroupMembers(groupId, memberUserIds);
        // 3.2 批量清理已读缓存
        groupMessageService.deleteReadMaxMessageIds(groupId, memberUserIds);

        // 4.1 发送踢出提示消息（TIP_TEXT）
        groupMessageService.sendTipGroupMessage(userId, groupId, memberUserIds, GROUP_REMOVE_TIP_MESSAGE);
        // 4.2 推送群删除事件
        webSocketService.sendGroupMessageAsync(memberUserIds,
                ImGroupMessageDTO.ofGroupDelete(userId, groupId));
    }

    // ==================== 群的读操作 ====================

    @Override
    public ImGroupDO validateGroupExists(Long id) {
        ImGroupDO group = getSelf().getGroup(id);
        if (group == null) {
            throw exception(GROUP_NOT_EXISTS);
        }
        if (Boolean.TRUE.equals(group.getBanned())) {
            throw exception(GROUP_BANNED);
        }
        if (CommonStatusEnum.DISABLE.getStatus().equals(group.getStatus())) {
            throw exception(GROUP_DISSOLVED);
        }
        return group;
    }

    @Override
    public ImGroupDO validateGroupOwner(Long groupId, Long userId) {
        ImGroupDO group = validateGroupExists(groupId);
        if (ObjUtil.notEqual(group.getOwnerUserId(), userId)) {
            throw exception(GROUP_NOT_OWNER);
        }
        return group;
    }

    @Override
    @Cacheable(cacheNames = GROUP, key = "#id", unless = "#result == null")
    public ImGroupDO getGroup(Long id) {
        return groupMapper.selectById(id);
    }

    @Override
    public List<ImGroupDO> getActiveGroupList(Long userId) {
        // 1. 查用户所在的、仍有效的群成员记录（仅 ENABLE 状态）
        List<ImGroupMemberDO> members = groupMemberService.getActiveGroupMemberListByUserId(userId);
        if (CollUtil.isEmpty(members)) {
            return Collections.emptyList();
        }
        Set<Long> groupIds = convertSet(members, ImGroupMemberDO::getGroupId);
        if (CollUtil.isEmpty(groupIds)) {
            return Collections.emptyList();
        }

        // 2. 批量查询群信息（仅 ENABLE 状态）
        return groupMapper.selectListByIds(groupIds, CommonStatusEnum.ENABLE.getStatus(), false);
    }

    private ImGroupServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

    /**
     * 根据用户编号集合，拼接用户昵称字符串（逗号分隔）
     *
     * @param userIds 用户编号集合
     * @return 昵称字符串，如 "张三,李四"
     */
    private String getUserNicknames(Collection<Long> userIds) {
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        return userIds.stream()
                .map(id -> userMap.containsKey(id) ? userMap.get(id).getNickname() : String.valueOf(id))
                .collect(Collectors.joining(","));
    }

}