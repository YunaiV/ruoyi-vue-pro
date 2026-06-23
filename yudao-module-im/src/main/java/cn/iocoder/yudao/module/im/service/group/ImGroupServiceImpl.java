package cn.iocoder.yudao.module.im.service.group;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupAdminAddReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupAdminRemoveReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupCancelMuteMemberReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupCreateReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupMuteAllReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupMuteMemberReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupTransferOwnerReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupUpdateReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberInviteReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberRemoveReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.group.vo.ImGroupManagerBanReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.group.vo.ImGroupManagerPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import cn.iocoder.yudao.module.im.dal.mysql.group.ImGroupMapper;
import cn.iocoder.yudao.module.im.enums.group.ImGroupAddSourceEnum;
import cn.iocoder.yudao.module.im.enums.group.ImGroupMemberRoleEnum;
import cn.iocoder.yudao.module.im.enums.message.ImMessageStatusEnum;
import cn.iocoder.yudao.module.im.enums.ImContentTypeEnum;
import cn.iocoder.yudao.module.im.framework.config.ImProperties;
import cn.iocoder.yudao.module.im.service.friend.ImFriendService;
import cn.iocoder.yudao.module.im.service.message.ImGroupMessageService;
import cn.iocoder.yudao.module.im.service.message.dto.ImGroupMessageSendDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.im.dal.redis.RedisKeyConstants.GROUP;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;

/**
 * 用户群 Service 实现类
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
    @Lazy // 避免循环依赖
    private ImGroupRequestService groupRequestService;

    @Resource
    private ImFriendService friendService;

    @Resource
    private AdminUserApi adminUserApi;

    @Resource
    private ImProperties imProperties;

    // ==================== 群的写操作 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImGroupDO createGroup(ImGroupCreateReqVO createReqVO, Long userId) {
        // 1.1 处理初始成员列表（去重 + 排除创建者自己）
        Set<Long> initialMemberUserIds = createReqVO.getMemberUserIds() == null
                ? new HashSet<>() : new HashSet<>(createReqVO.getMemberUserIds());
        initialMemberUserIds.remove(userId);
        // 1.2 校验初始成员都是创建者的好友
        if (CollUtil.isNotEmpty(initialMemberUserIds)) {
            List<ImFriendDO> friends = friendService.getActiveFriendList(userId, initialMemberUserIds);
            Set<Long> friendUserIds = convertSet(friends, ImFriendDO::getFriendUserId);
            Collection<Long> notFriendUserIds = CollUtil.subtract(initialMemberUserIds, friendUserIds);
            if (CollUtil.isNotEmpty(notFriendUserIds)) {
                throw exception(GROUP_INVITE_NOT_FRIEND, getUserNicknames(notFriendUserIds));
            }
        }
        // 1.3 校验群人数上限（创建者 + 初始成员 ≤ 群成员上限）
        int maxMember = imProperties.getGroup().getMaxMember();
        if (initialMemberUserIds.size() + 1 > maxMember) {
            throw exception(GROUP_MEMBER_EXCEED, maxMember);
        }

        // 2.1 插入群记录
        ImGroupDO group = BeanUtils.toBean(createReqVO, ImGroupDO.class)
                .setOwnerUserId(userId).setStatus(CommonStatusEnum.ENABLE.getStatus());
        groupMapper.insert(group);
        // 2.2 创建者作为 OWNER 入群
        groupMemberService.addGroupMember(group.getId(), userId, ImGroupMemberRoleEnum.OWNER.getRole());
        // 2.3 批量添加初始成员；标记 addSource=INVITE / inviter=创建者
        if (CollUtil.isNotEmpty(initialMemberUserIds)) {
            groupMemberService.addGroupMembers(group.getId(), initialMemberUserIds,
                    ImGroupAddSourceEnum.INVITE.getSource(), userId);
        }

        // 3. 推送 GROUP_CREATE 通知给全员（含创建者多端同步 + 初始成员）
        List<Long> allMemberUserIds = CollectionUtils.of(userId, initialMemberUserIds);
        groupMessageService.sendGroupMessage(userId, allMemberUserIds,
                ImGroupMessageSendDTO.ofGroupCreate(group.getId(), userId, allMemberUserIds));
        return group;
    }

    @Override
    @CacheEvict(cacheNames = GROUP, key = "#updateReqVO.id")
    @Transactional(rollbackFor = Exception.class)
    public ImGroupDO updateGroup(ImGroupUpdateReqVO updateReqVO, Long userId) {
        // 1.1 校验群存在：group 留作老值备份，通知里 oldXXX 字段从这里取
        ImGroupDO group = validateGroupExists(updateReqVO.getId());
        // 1.2 校验操作人是群主
        if (ObjUtil.notEqual(group.getOwnerUserId(), userId)) {
            throw exception(GROUP_NOT_OWNER);
        }

        // 2. 更新数据库（newGroup 仅含变更字段）
        ImGroupDO newGroup = BeanUtils.toBean(updateReqVO, ImGroupDO.class);
        groupMapper.updateById(newGroup);

        // 3. 按变更字段分别推送 GROUP_NAME / NOTICE / INFO_UPDATE 通知；活跃成员只查一次复用，避免 3 次 Redis GET
        // name / avatar 不允许空串（业务上必须非空），notice 允许空串（清空公告也是有效操作）
        Long groupId = group.getId();
        boolean nameChanged = StrUtil.isNotEmpty(updateReqVO.getName());
        boolean noticeChanged = updateReqVO.getNotice() != null;
        boolean avatarChanged = StrUtil.isNotEmpty(updateReqVO.getAvatar());
        boolean joinApprovalChanged = updateReqVO.getJoinApproval() != null
                && ObjUtil.notEqual(group.getJoinApproval(), updateReqVO.getJoinApproval());
        if (nameChanged || noticeChanged || avatarChanged || joinApprovalChanged) {
            List<Long> memberUserIds = groupMemberService.getActiveGroupMemberUserIdsByGroupId(groupId);
            if (nameChanged) {
                groupMessageService.sendGroupMessage(userId, memberUserIds, ImGroupMessageSendDTO.ofGroupNameUpdate(
                        groupId, userId, group.getName(), updateReqVO.getName()));
            }
            if (noticeChanged) {
                groupMessageService.sendGroupMessage(userId, memberUserIds, ImGroupMessageSendDTO.ofGroupNoticeUpdate(
                        groupId, userId, group.getNotice(), updateReqVO.getNotice()));
            }
            if (avatarChanged) {
                groupMessageService.sendGroupMessage(userId, memberUserIds, ImGroupMessageSendDTO.ofGroupInfoUpdate(
                        groupId, userId, group.getAvatar(), updateReqVO.getAvatar(), null, null));
            }
            if (joinApprovalChanged) {
                groupMessageService.sendGroupMessage(userId, memberUserIds, ImGroupMessageSendDTO.ofGroupInfoUpdate(
                        groupId, userId, null, null, group.getJoinApproval(), updateReqVO.getJoinApproval()));
            }
        }

        // 4. 返回合并后的新群信息（updateReqVO 非空字段覆盖 group）
        BeanUtil.copyProperties(updateReqVO, group, CopyOptions.create().ignoreNullValue());
        return group;
    }

    @Override
    @CacheEvict(cacheNames = GROUP, key = "#id")
    @Transactional(rollbackFor = Exception.class)
    public void dissolveGroup(Long id, Long userId) {
        // 1. 校验群存在 + 当前用户是群主
        ImGroupDO group = validateGroupNotDissolved(id);
        if (ObjUtil.notEqual(group.getOwnerUserId(), userId)) {
            throw exception(GROUP_NOT_OWNER);
        }

        // 2. 解散群
        dissolveGroup0(id, userId);
    }

    private void dissolveGroup0(Long id, Long userId) {
        // 1. 先发 GROUP_DISSOLVE 通知：放在成员移除前，sendGroupMessage 才能查到全员
        groupMessageService.sendGroupMessage(userId, ImGroupMessageSendDTO.ofGroupDissolve(id, userId));

        // 2.1 更新群状态为已解散
        groupMapper.updateById(new ImGroupDO().setId(id)
                .setStatus(CommonStatusEnum.DISABLE.getStatus()).setDissolvedTime(LocalDateTime.now()));
        // 2.2 移除全部群成员
        groupMemberService.removeGroupMembersByGroupId(id);
    }

    // ==================== 群成员的写操作 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inviteGroupMember(Long userId, ImGroupMemberInviteReqVO inviteReqVO) {
        Long groupId = inviteReqVO.getGroupId();
        // 1.1 校验群存在 + 当前用户是群成员；同时拿到 role 供下面审批分支判断
        ImGroupDO group = validateGroupExists(groupId);
        ImGroupMemberDO operator = groupMemberService.validateMemberInGroup(groupId, userId);
        // 1.2 入参去重 + 排除已在群中的用户
        List<ImGroupMemberDO> activeMembers = groupMemberService.getActiveGroupMemberListByGroupId(groupId);
        Set<Long> activeMemberUserIds = convertSet(activeMembers, ImGroupMemberDO::getUserId);
        List<Long> memberUserIds = CollUtil.subtractToList(
                CollUtil.distinct(inviteReqVO.getMemberUserIds()), activeMemberUserIds);
        if (CollUtil.isEmpty(memberUserIds)) {
            return;
        }
        // 1.3 校验被邀请人都是当前用户的好友
        List<ImFriendDO> friends = friendService.getActiveFriendList(userId, memberUserIds);
        Set<Long> friendUserIds = convertSet(friends, ImFriendDO::getFriendUserId);
        Collection<Long> notFriendUserIds = CollUtil.subtract(memberUserIds, friendUserIds);
        if (CollUtil.isNotEmpty(notFriendUserIds)) {
            throw exception(GROUP_INVITE_NOT_FRIEND, getUserNicknames(notFriendUserIds));
        }
        // 1.4 校验群人数上限
        int maxMember = imProperties.getGroup().getMaxMember();
        if (activeMembers.size() + memberUserIds.size() > maxMember) {
            throw exception(GROUP_MEMBER_EXCEED, maxMember);
        }

        // 2. 情况一：群开启审批 + 邀请人是普通成员，落 group_request 等群主 / 管理员处理
        // 群主 / 管理员邀请，直接拉人进群
        if (Boolean.TRUE.equals(group.getJoinApproval())
                && !ImGroupMemberRoleEnum.isOwnerOrAdmin(operator.getRole())) {
            groupRequestService.createInviteRequestList(groupId, userId, memberUserIds);
            return;
        }

        // 3. 情况二：未开审批 / 群主 / 管理员邀请，直进；批量添加群成员，写 addSource=INVITE / inviterUserId=操作人 留痕
        groupMemberService.addGroupMembers(groupId, memberUserIds,
                ImGroupAddSourceEnum.INVITE.getSource(), userId);

        // 4. 发 GROUP_MEMBER_INVITE 通知给全员；本地拼 receivers（已查的 active + 新邀请）避免缓存刚 evict 后强制走 DB
        Set<Long> allReceivers = new HashSet<>(memberUserIds);
        allReceivers.addAll(activeMemberUserIds);
        groupMessageService.sendGroupMessage(userId, allReceivers,
                ImGroupMessageSendDTO.ofGroupMemberInvite(groupId, userId, memberUserIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quitGroup(Long groupId, Long userId) {
        // 1.1 校验群存在且未解散
        ImGroupDO group = getSelf().getGroup(groupId);
        if (group == null) {
            throw exception(GROUP_NOT_EXISTS);
        }
        if (CommonStatusEnum.DISABLE.getStatus().equals(group.getStatus())) {
            throw exception(GROUP_DISSOLVED);
        }
        // 1.2 群主不可退群
        if (ObjUtil.equal(group.getOwnerUserId(), userId)) {
            throw exception(GROUP_OWNER_CANNOT_QUIT);
        }
        // 1.3 校验当前用户是有效群成员；防止非成员触发广播 + 后续 remove 失败时无法回滚已推送的事件
        groupMemberService.validateMemberInGroup(groupId, userId);

        // 2. 先发广播，后移成员（见类 javadoc）
        groupMessageService.sendGroupMessage(userId, ImGroupMessageSendDTO.ofGroupMemberQuit(groupId, userId));

        // 3. 移除群成员
        groupMemberService.removeGroupMember(groupId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeGroupMember(Long userId, ImGroupMemberRemoveReqVO removeReqVO) {
        Long groupId = removeReqVO.getGroupId();
        Set<Long> targetUserIds = new HashSet<>(removeReqVO.getMemberUserIds());
        // 1.1 校验群存在 + 操作者是群主或管理员
        ImGroupMemberDO operator = validateGroupOwnerOrAdmin(groupId, userId);
        // 1.2 不能移除自己
        if (targetUserIds.contains(userId)) {
            throw exception(GROUP_CANNOT_REMOVE_SELF);
        }
        // 1.3 仅保留仍有效的成员；已退群（DISABLE）/ 查无记录的目标直接跳过，只踢有效成员，不让整批失败
        List<ImGroupMemberDO> targets = filterList(groupMemberService.getGroupMembers(groupId, targetUserIds),
                target -> CommonStatusEnum.ENABLE.getStatus().equals(target.getStatus()));
        Set<Long> validTargetUserIds = convertSet(targets, ImGroupMemberDO::getUserId);
        // 1.4 目标全部已不在群：无人可踢，直接返回
        if (CollUtil.isEmpty(validTargetUserIds)) {
            return;
        }
        // 1.5 三档权限校验：群主不可被移出；管理员不能移出管理员
        boolean operatorIsAdmin = ImGroupMemberRoleEnum.isAdmin(operator.getRole());
        for (ImGroupMemberDO target : targets) {
            if (ImGroupMemberRoleEnum.isOwner(target.getRole())) {
                throw exception(GROUP_REMOVE_OWNER_DENIED);
            }
            if (operatorIsAdmin && ImGroupMemberRoleEnum.isAdmin(target.getRole())) {
                throw exception(GROUP_REMOVE_ADMIN_DENIED);
            }
        }

        // 2. 先发 GROUP_MEMBER_KICK 通知：放在被踢者移除前，sendGroupMessage 才能查到全员（含被踢者）
        groupMessageService.sendGroupMessage(userId,
                ImGroupMessageSendDTO.ofGroupMemberKick(groupId, userId, validTargetUserIds));

        // 3. 批量移除群成员；不清理读位置（保留退群前历史已读，供离线补偿）
        groupMemberService.removeGroupMembers(groupId, validTargetUserIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addGroupAdmin(Long userId, ImGroupAdminAddReqVO reqVO) {
        Long groupId = reqVO.getId();
        Set<Long> targetUserIds = new HashSet<>(reqVO.getUserIds());
        // 1.1 仅群主可操作
        validateGroupOwnerForUpdate(groupId, userId);
        // 1.2 校验目标都是有效成员且非群主
        Map<Long, ImGroupMemberDO> targetMap = convertMap(
                groupMemberService.getGroupMembers(groupId, targetUserIds), ImGroupMemberDO::getUserId);
        validateAdminTargets(targetUserIds, targetMap);
        // 1.3 幂等过滤：跳过已是 ADMIN
        Set<Long> changedUserIds = convertSet(targetUserIds,
                id -> id,
                id -> !ImGroupMemberRoleEnum.isAdmin(targetMap.get(id).getRole()));
        if (CollUtil.isEmpty(changedUserIds)) {
            return;
        }
        // 1.4 校验上限
        Long existAdminCount = groupMemberService.getGroupMemberCountByRole(
                groupId, ImGroupMemberRoleEnum.ADMIN.getRole());
        int adminMaxCount = imProperties.getGroup().getAdminMaxCount();
        if (existAdminCount + changedUserIds.size() > adminMaxCount) {
            throw exception(GROUP_ADMIN_MAX_LIMIT, adminMaxCount);
        }

        // 2. 批量更新角色
        int affected = groupMemberService.updateGroupMemberRole(groupId, changedUserIds,
                ImGroupMemberRoleEnum.ADMIN.getRole());
        if (affected != changedUserIds.size()) {
            throw exception(GROUP_ADMIN_TARGET_NOT_IN_GROUP);
        }

        // 3. 推送 GROUP_ADMIN_ADD 通知给全员
        groupMessageService.sendGroupMessage(userId,
                ImGroupMessageSendDTO.ofGroupAdminAdd(groupId, userId, changedUserIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeGroupAdmin(Long userId, ImGroupAdminRemoveReqVO reqVO) {
        Long groupId = reqVO.getId();
        Set<Long> targetUserIds = new HashSet<>(reqVO.getUserIds());
        // 1.1 仅群主可操作
        validateGroupOwnerForUpdate(groupId, userId);
        // 1.2 校验目标都是有效成员且非群主
        Map<Long, ImGroupMemberDO> targetMap = convertMap(
                groupMemberService.getGroupMembers(groupId, targetUserIds), ImGroupMemberDO::getUserId);
        validateAdminTargets(targetUserIds, targetMap);
        // 1.3 幂等过滤：跳过已是 MEMBER
        Set<Long> changedUserIds = convertSet(targetUserIds,
                id -> id,
                id -> ImGroupMemberRoleEnum.isAdmin(targetMap.get(id).getRole()));
        if (CollUtil.isEmpty(changedUserIds)) {
            return;
        }

        // 2. 批量更新角色
        int affected = groupMemberService.updateGroupMemberRole(groupId, changedUserIds,
                ImGroupMemberRoleEnum.NORMAL.getRole());
        if (affected != changedUserIds.size()) {
            throw exception(GROUP_ADMIN_TARGET_NOT_IN_GROUP);
        }

        // 3. 推送 GROUP_ADMIN_REMOVE 通知给全员
        groupMessageService.sendGroupMessage(userId,
                ImGroupMessageSendDTO.ofGroupAdminRemove(groupId, userId, changedUserIds));
    }

    /**
     * 校验管理员变更目标都是当前群的有效成员（status=ENABLE）且非群主
     */
    private void validateAdminTargets(Set<Long> targetUserIds, Map<Long, ImGroupMemberDO> targetMap) {
        for (Long targetUserId : targetUserIds) {
            ImGroupMemberDO target = targetMap.get(targetUserId);
            if (target == null || CommonStatusEnum.DISABLE.getStatus().equals(target.getStatus())) {
                throw exception(GROUP_ADMIN_TARGET_NOT_IN_GROUP);
            }
            if (ImGroupMemberRoleEnum.isOwner(target.getRole())) {
                throw exception(GROUP_ADMIN_TARGET_IS_OWNER);
            }
        }
    }

    @Override
    @CacheEvict(cacheNames = GROUP, key = "#transferReqVO.id")
    @Transactional(rollbackFor = Exception.class)
    public void transferGroupOwner(Long userId, ImGroupTransferOwnerReqVO transferReqVO) {
        Long groupId = transferReqVO.getId();
        Long newOwnerUserId = transferReqVO.getNewOwnerUserId();
        // 1.1 仅老群主可执行
        validateGroupOwnerForUpdate(groupId, userId);
        // 1.2 不能转让给自己
        if (ObjUtil.equal(userId, newOwnerUserId)) {
            throw exception(GROUP_TRANSFER_OWNER_TO_SELF);
        }
        // 1.3 新群主必须是群的有效成员
        ImGroupMemberDO newOwner = groupMemberService.validateMemberInGroup(groupId, newOwnerUserId);

        // 2.1 更新成员角色
        int newOwnerAffected = groupMemberService.updateGroupMemberRole(groupId, CollUtil.newHashSet(newOwner.getUserId()),
                ImGroupMemberRoleEnum.OWNER.getRole());
        if (newOwnerAffected != 1) {
            throw exception(GROUP_MEMBER_NOT_IN_GROUP);
        }
        int oldOwnerAffected = groupMemberService.updateGroupMemberRole(groupId, CollUtil.newHashSet(userId),
                ImGroupMemberRoleEnum.NORMAL.getRole());
        if (oldOwnerAffected != 1) {
            throw exception(GROUP_MEMBER_NOT_IN_GROUP);
        }
        // 2.2 更新群主编号
        groupMapper.updateById(new ImGroupDO().setId(groupId).setOwnerUserId(newOwnerUserId));

        // 3. 推送 GROUP_OWNER_TRANSFER 通知给全员
        groupMessageService.sendGroupMessage(userId,
                ImGroupMessageSendDTO.ofGroupOwnerTransfer(groupId, userId, newOwnerUserId));
    }

    @Override
    @CacheEvict(cacheNames = GROUP, key = "#groupId")
    @Transactional(rollbackFor = Exception.class)
    public void pinGroupMessage(Long userId, Long groupId, Long messageId) {
        // 1.1 校验群主 / 管理员；同时拿到 group 复用，避免再走一次 @Cacheable
        ImGroupDO group = validateOwnerOrAdminAndGetGroupForUpdate(groupId, userId);
        // 1.2 校验消息属于该群、是普通聊天消息（绕过前端菜单不允许置顶群事件 / 撤回事件）、且未被撤回
        ImGroupMessageDO message = groupMessageService.getGroupMessage(messageId);
        if (message == null || ObjUtil.notEqual(message.getGroupId(), groupId)) {
            throw exception(MESSAGE_NOT_IN_GROUP);
        }
        if (!ImContentTypeEnum.validate(message.getType()).isNormal()
                || ImMessageStatusEnum.RECALL.getStatus().equals(message.getStatus())) {
            throw exception(MESSAGE_NOT_IN_GROUP);
        }
        // 1.3 定向消息（receiverUserIds 非空）不允许置顶：置顶会向全群广播，泄露原本仅部分人可见的内容
        if (CollUtil.isNotEmpty(message.getReceiverUserIds())) {
            throw exception(GROUP_MESSAGE_PIN_DIRECTED_DENIED);
        }

        // 2. 幂等 + 上限校验
        List<Long> pinned = new ArrayList<>(CollUtil.emptyIfNull(group.getPinnedMessageIds()));
        if (pinned.contains(messageId)) {
            throw exception(GROUP_MESSAGE_ALREADY_PINNED);
        }
        int pinMaxCount = imProperties.getGroup().getPinMaxCount();
        if (pinned.size() >= pinMaxCount) {
            throw exception(GROUP_MESSAGE_PIN_MAX_LIMIT, pinMaxCount);
        }
        pinned.add(messageId);
        groupMapper.updateById(new ImGroupDO().setId(groupId).setPinnedMessageIds(pinned));

        // 3. 推送 GROUP_MESSAGE_PIN 通知给全员
        groupMessageService.sendGroupMessage(userId,
                ImGroupMessageSendDTO.ofGroupMessagePin(groupId, userId, message));
    }

    @Override
    @CacheEvict(cacheNames = GROUP, key = "#groupId")
    @Transactional(rollbackFor = Exception.class)
    public void unpinGroupMessage(Long userId, Long groupId, Long messageId) {
        // 1. 校验群主 / 管理员；同时拿到 group 复用，避免再走一次 @Cacheable
        ImGroupDO group = validateOwnerOrAdminAndGetGroupForUpdate(groupId, userId);
        // 2. 幂等校验
        List<Long> pinned = new ArrayList<>(CollUtil.emptyIfNull(group.getPinnedMessageIds()));
        if (!pinned.contains(messageId)) {
            throw exception(GROUP_MESSAGE_NOT_PINNED);
        }
        pinned.remove(messageId);
        groupMapper.updateById(new ImGroupDO().setId(groupId).setPinnedMessageIds(pinned));

        // 3. 推送 GROUP_MESSAGE_UNPIN 通知给全员
        groupMessageService.sendGroupMessage(userId,
                ImGroupMessageSendDTO.ofGroupMessageUnpin(groupId, userId, messageId));
    }

    /**
     * 校验登录用户是群主或管理员，同时返回群信息
     */
    private ImGroupDO validateOwnerOrAdminAndGetGroup(Long groupId, Long userId) {
        ImGroupDO group = validateGroupExists(groupId);
        ImGroupMemberDO member = groupMemberService.validateMemberInGroup(groupId, userId);
        if (!ImGroupMemberRoleEnum.isOwnerOrAdmin(member.getRole())) {
            throw exception(GROUP_NOT_OWNER_OR_ADMIN);
        }
        return group;
    }

    /**
     * 校验登录用户是群主或管理员，同时锁定群信息
     */
    private ImGroupDO validateOwnerOrAdminAndGetGroupForUpdate(Long groupId, Long userId) {
        ImGroupDO group = validateGroupExistsForUpdate(groupId);
        ImGroupMemberDO member = groupMemberService.validateMemberInGroup(groupId, userId);
        if (!ImGroupMemberRoleEnum.isOwnerOrAdmin(member.getRole())) {
            throw exception(GROUP_NOT_OWNER_OR_ADMIN);
        }
        return group;
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
    public void validateMemberCountLimit(Long groupId, int addCount) {
        // 1. 锁定群记录
        validateGroupExistsForUpdate(groupId);
        // 2. 校验成员数量上限
        int activeCount = groupMemberService.getActiveGroupMemberUserIdsByGroupId(groupId).size();
        int maxMember = imProperties.getGroup().getMaxMember();
        if (activeCount + addCount > maxMember) {
            throw exception(GROUP_MEMBER_EXCEED, maxMember);
        }
    }

    private ImGroupDO validateGroupOwnerForUpdate(Long groupId, Long userId) {
        ImGroupDO group = validateGroupExistsForUpdate(groupId);
        if (ObjUtil.notEqual(group.getOwnerUserId(), userId)) {
            throw exception(GROUP_NOT_OWNER);
        }
        return group;
    }

    private ImGroupDO validateGroupExistsForUpdate(Long groupId) {
        ImGroupDO group = groupMapper.selectByIdForUpdate(groupId);
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

    private ImGroupDO validateGroupNotDissolved(Long id) {
        ImGroupDO group = getSelf().getGroup(id);
        if (group == null) {
            throw exception(GROUP_NOT_EXISTS);
        }
        if (CommonStatusEnum.DISABLE.getStatus().equals(group.getStatus())) {
            throw exception(GROUP_DISSOLVED);
        }
        return group;
    }

    /**
     * 校验当前用户是群主或管理员，否则抛 GROUP_NOT_OWNER_OR_ADMIN
     */
    private ImGroupMemberDO validateGroupOwnerOrAdmin(Long groupId, Long userId) {
        validateGroupExists(groupId);
        ImGroupMemberDO member = groupMemberService.validateMemberInGroup(groupId, userId);
        if (!ImGroupMemberRoleEnum.isOwnerOrAdmin(member.getRole())) {
            throw exception(GROUP_NOT_OWNER_OR_ADMIN);
        }
        return member;
    }

    @Override
    @Cacheable(cacheNames = GROUP, key = "#id", unless = "#result == null")
    public ImGroupDO getGroup(Long id) {
        return groupMapper.selectById(id);
    }

    @Override
    public Map<Long, ImGroupDO> getGroupMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(groupMapper.selectByIds(ids), ImGroupDO::getId);
    }

    @Override
    public List<ImGroupDO> getMyGroupList(Long userId) {
        // 1. 查用户曾经加入的所有群（含退群，前端按需过滤）；退群前的离线消息也要能展示
        List<ImGroupMemberDO> members = groupMemberService.getGroupMemberListByUserId(userId);
        if (CollUtil.isEmpty(members)) {
            return Collections.emptyList();
        }

        // 2. 批量查询群信息（不按 status / banned 过滤，已解散 / 封禁的群也要返回，供前端展示历史消息的群名 / 头像）
        Set<Long> groupIds = convertSet(members, ImGroupMemberDO::getGroupId);
        return groupMapper.selectByIds(groupIds);
    }

    // ==================== 管理后台 ====================

    @Override
    public PageResult<ImGroupDO> getGroupPage(ImGroupManagerPageReqVO pageReqVO) {
        return groupMapper.selectPage(pageReqVO);
    }

    @Override
    @CacheEvict(cacheNames = GROUP, key = "#banReqVO.id")
    public void banGroup(Long operatorUserId, ImGroupManagerBanReqVO banReqVO) {
        // 1. 校验群存在且未解散
        ImGroupDO group = getSelf().getGroup(banReqVO.getId());
        if (group == null) {
            throw exception(GROUP_NOT_EXISTS);
        }
        if (CommonStatusEnum.DISABLE.getStatus().equals(group.getStatus())) {
            throw exception(GROUP_DISSOLVED);
        }
        // 2. 幂等：已封禁直接返回，避免重复广播封禁通知
        if (Boolean.TRUE.equals(group.getBanned())) {
            return;
        }

        // 3. 更新封禁状态
        groupMapper.updateById(new ImGroupDO().setId(banReqVO.getId())
                .setBanned(true).setBannedReason(banReqVO.getReason()).setBannedTime(LocalDateTime.now()));

        // 4. 广播通知
        groupMessageService.sendGroupMessage(operatorUserId,
                ImGroupMessageSendDTO.ofGroupBanned(banReqVO.getId(), operatorUserId, true));
    }

    @Override
    @CacheEvict(cacheNames = GROUP, key = "#id")
    public void unbanGroup(Long operatorUserId, Long id) {
        // 1. 校验群存在
        if (getSelf().getGroup(id) == null) {
            throw exception(GROUP_NOT_EXISTS);
        }

        // 2. 解封（保留 bannedReason / bannedTime 作为历史记录）
        groupMapper.updateById(new ImGroupDO().setId(id).setBanned(false));

        // 3. 广播通知
        groupMessageService.sendGroupMessage(operatorUserId,
                ImGroupMessageSendDTO.ofGroupBanned(id, operatorUserId, false));
    }

    @Override
    @CacheEvict(cacheNames = GROUP, key = "#id")
    @Transactional(rollbackFor = Exception.class)
    public void dissolveGroupByManager(Long operatorUserId, Long id) {
        // 1. 校验群存在且未解散
        validateGroupNotDissolved(id);

        // 2. 解散群
        dissolveGroup0(id, operatorUserId);
    }

    // ==================== 群禁言 ====================

    @Override
    @CacheEvict(cacheNames = GROUP, key = "#reqVO.id")
    @Transactional(rollbackFor = Exception.class)
    public void muteAll(Long userId, ImGroupMuteAllReqVO reqVO) {
        // 1. 校验群主或管理员
        validateGroupOwnerOrAdmin(reqVO.getId(), userId);

        // 2. 更新 mutedAll
        groupMapper.updateById(new ImGroupDO().setId(reqVO.getId()).setMutedAll(reqVO.getMutedAll()));

        // 3. 广播通知
        ImGroupMessageSendDTO messageSendDTO = Boolean.TRUE.equals(reqVO.getMutedAll())
                ? ImGroupMessageSendDTO.ofGroupMuted(reqVO.getId(), userId)
                : ImGroupMessageSendDTO.ofGroupCancelMuted(reqVO.getId(), userId);
        groupMessageService.sendGroupMessage(userId, messageSendDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void muteMember(Long userId, ImGroupMuteMemberReqVO reqVO) {
        // 1.1 不能禁言自己
        if (ObjUtil.equal(userId, reqVO.getUserId())) {
            throw exception(GROUP_MUTE_MEMBER_SELF);
        }
        // 1.2 校验群存在且未封禁
        validateGroupExists(reqVO.getId());
        // 1.3 校验操作人和目标都在群中
        ImGroupMemberDO operatorMember = groupMemberService.validateMemberInGroup(reqVO.getId(), userId);
        ImGroupMemberDO targetMember = groupMemberService.validateMemberInGroup(reqVO.getId(), reqVO.getUserId());
        // 1.4 三档权限校验
        validateMutePermission(operatorMember, targetMember);

        // 2. 设置 muteEndTime
        LocalDateTime muteEndTime = reqVO.getMutedSeconds() == 0
                ? ImGroupMemberDO.PERMANENT_MUTE_END_TIME : LocalDateTime.now().plusSeconds(reqVO.getMutedSeconds());
        groupMemberService.updateGroupMemberMuteEndTime(reqVO.getId(), reqVO.getUserId(), muteEndTime);

        // 3. 广播通知
        groupMessageService.sendGroupMessage(userId,
                ImGroupMessageSendDTO.ofGroupMemberMuted(reqVO.getId(), userId,
                        reqVO.getUserId(), muteEndTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelMuteMember(Long userId, ImGroupCancelMuteMemberReqVO reqVO) {
        // 1.1 校验群存在且未封禁
        validateGroupExists(reqVO.getId());
        // 1.2 校验操作人和目标都在群中
        ImGroupMemberDO operatorMember = groupMemberService.validateMemberInGroup(reqVO.getId(), userId);
        ImGroupMemberDO targetMember = groupMemberService.validateMemberInGroup(reqVO.getId(), reqVO.getUserId());
        // 1.3 三档权限校验
        validateMutePermission(operatorMember, targetMember);

        // 2. 取消禁言（清空 muteEndTime）
        groupMemberService.updateGroupMemberMuteEndTime(reqVO.getId(), reqVO.getUserId(), null);

        // 3. 广播通知
        groupMessageService.sendGroupMessage(userId,
                ImGroupMessageSendDTO.ofGroupMemberCancelMuted(reqVO.getId(), userId, reqVO.getUserId()));
    }

    /**
     * 三档分层禁言权限校验
     */
    private void validateMutePermission(ImGroupMemberDO operator, ImGroupMemberDO target) {
        // 普通成员不能禁言任何人
        if (!ImGroupMemberRoleEnum.isOwnerOrAdmin(operator.getRole())) {
            throw exception(GROUP_NOT_OWNER_OR_ADMIN);
        }
        // 群主不可被禁言
        if (ImGroupMemberRoleEnum.isOwner(target.getRole())) {
            throw exception(GROUP_MUTE_OWNER_DENIED);
        }
        // 管理员不能禁言其他管理员
        if (ImGroupMemberRoleEnum.isAdmin(target.getRole()) && !ImGroupMemberRoleEnum.isOwner(operator.getRole())) {
            throw exception(GROUP_MUTE_ADMIN_DENIED);
        }
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
