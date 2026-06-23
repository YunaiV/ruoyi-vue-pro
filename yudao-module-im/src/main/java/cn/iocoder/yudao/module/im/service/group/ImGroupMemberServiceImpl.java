package cn.iocoder.yudao.module.im.service.group;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberUpdateReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.mysql.group.ImGroupMemberMapper;
import cn.iocoder.yudao.module.im.enums.group.ImGroupMemberRoleEnum;
import cn.iocoder.yudao.module.im.service.message.ImGroupMessageService;
import cn.iocoder.yudao.module.im.service.message.dto.ImGroupMessageSendDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.im.dal.redis.RedisKeyConstants.GROUP_MEMBER_IDS;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.GROUP_MEMBER_NOT_IN_GROUP;

/**
 * 群成员 Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class ImGroupMemberServiceImpl implements ImGroupMemberService {

    @Resource
    private ImGroupMemberMapper groupMemberMapper;

    @Resource
    @Lazy // 避免循环依赖
    private ImGroupMessageService groupMessageService;

    @Override
    public ImGroupMemberDO getGroupMember(Long id) {
        return groupMemberMapper.selectById(id);
    }

    @Override
    public ImGroupMemberDO getGroupMember(Long groupId, Long userId) {
        return groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
    }

    @Override
    public List<ImGroupMemberDO> getGroupMembers(Long groupId, Collection<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return groupMemberMapper.selectListByGroupIdAndUserIds(groupId, userIds);
    }

    @Override
    public List<ImGroupMemberDO> getGroupMemberListByGroupId(Long groupId) {
        return groupMemberMapper.selectListByGroupId(groupId);
    }

    @Override
    public List<ImGroupMemberDO> getActiveGroupMemberListByGroupId(Long groupId) {
        return groupMemberMapper.selectListByGroupIdAndStatus(groupId, CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public List<ImGroupMemberDO> getGroupMemberListByOwnerAndAdmin(Long groupId) {
        return groupMemberMapper.selectListByGroupIdAndStatusAndRoles(groupId, CommonStatusEnum.ENABLE.getStatus(),
                ListUtil.of(ImGroupMemberRoleEnum.OWNER.getRole(), ImGroupMemberRoleEnum.ADMIN.getRole()));
    }

    /**
     * 只缓存 userId 列表而非整个 {@link ImGroupMemberDO}，理由：
     * <ul>
     *   <li>体积小：500 人群约 4KB，失效/序列化成本低；</li>
     *   <li>失效面窄：仅 {@link #addGroupMember}/{@link #addGroupMembers}/
     *       {@link #removeGroupMember}/{@link #removeGroupMembers}/{@link #removeGroupMembersByGroupId}
     *       这类影响成员集合的写操作需要失效；
     *       {@link #updateGroupMember}（昵称/备注/免打扰）不修改集合成员，不需要失效。</li>
     * </ul>
     */
    @Override
    @Cacheable(cacheNames = GROUP_MEMBER_IDS, key = "#groupId")
    public List<Long> getActiveGroupMemberUserIdsByGroupId(Long groupId) {
        List<ImGroupMemberDO> members = groupMemberMapper.selectListByGroupIdAndStatus(
                groupId, CommonStatusEnum.ENABLE.getStatus());
        return convertList(members, ImGroupMemberDO::getUserId);
    }

    @Override
    public List<ImGroupMemberDO> getActiveGroupMemberListByUserId(Long userId) {
        return groupMemberMapper.selectListByUserIdAndStatus(userId, CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public List<ImGroupMemberDO> getGroupMemberListByUserId(Long userId) {
        return groupMemberMapper.selectListByUserId(userId);
    }

    @Override
    public ImGroupMemberDO addGroupMember(Long groupId, Long userId) {
        return addGroupMember(groupId, userId, ImGroupMemberRoleEnum.NORMAL.getRole(), null, null);
    }

    @Override
    public ImGroupMemberDO addGroupMember(Long groupId, Long userId, Integer role) {
        return addGroupMember(groupId, userId, role, null, null);
    }

    /**
     * 并发安全：依靠 im_group_member 表的唯一索引 uk_im_group_member_group_user(group_id, user_id) 保证幂等，
     * 当并发 insert 触发 {@link DuplicateKeyException} 时降级为 select + update。
     * <p>
     * 重置旧成员行时强制重置 role / addSource / inviterUserId / quitTime / muteEndTime
     */
    @Override
    @CacheEvict(cacheNames = GROUP_MEMBER_IDS, key = "#groupId")
    public ImGroupMemberDO addGroupMember(Long groupId, Long userId, Integer role,
                                          Integer addSource, Long inviterUserId) {
        LocalDateTime now = LocalDateTime.now();
        // 情况一：已存在记录 → 重置或跳过
        ImGroupMemberDO exists = groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        if (exists != null) {
            if (CommonStatusEnum.isDisable(exists.getStatus())) {
                groupMemberMapper.updateRejoinFields(exists.getId(), CommonStatusEnum.ENABLE.getStatus(), now,
                        role, addSource, inviterUserId);
                exists.setStatus(CommonStatusEnum.ENABLE.getStatus()).setJoinTime(now).setRole(role)
                        .setAddSource(addSource).setInviterUserId(inviterUserId)
                        .setQuitTime(null).setMuteEndTime(null);
            }
            return exists;
        }
        // 情况二：新增成员
        ImGroupMemberDO member = new ImGroupMemberDO()
                .setGroupId(groupId).setUserId(userId)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()).setJoinTime(now)
                .setRole(role).setAddSource(addSource).setInviterUserId(inviterUserId);
        try {
            groupMemberMapper.insert(member);
            return member;
        } catch (DuplicateKeyException e) {
            // 并发场景：另一个请求已先一步插入，且其插入的必然是 ENABLE 状态（DISABLE 场景在上方分支已处理），查询返回
            log.warn("[addGroupMember][groupId({}) userId({}) 并发插入冲突，查询返回]", groupId, userId);
            return groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        }
    }

    @Override
    public void addGroupMembers(Long groupId, Collection<Long> userIds) {
        addGroupMembers(groupId, userIds, null, null);
    }

    @Override
    @CacheEvict(cacheNames = GROUP_MEMBER_IDS, key = "#groupId")
    public void addGroupMembers(Long groupId, Collection<Long> userIds, Integer addSource, Long inviterUserId) {
        LocalDateTime now = LocalDateTime.now();
        Integer role = ImGroupMemberRoleEnum.NORMAL.getRole();
        // 1.1 查询已有记录（含已退群的 DISABLE 记录）
        List<ImGroupMemberDO> existMembers = groupMemberMapper.selectListByGroupIdAndUserIds(groupId, userIds);
        Map<Long, ImGroupMemberDO> existMap = convertMap(existMembers, ImGroupMemberDO::getUserId);
        // 1.2 分类：已有记录 → UPDATE，新成员 → INSERT
        List<ImGroupMemberDO> inserts = new ArrayList<>();
        List<ImGroupMemberDO> updates = new ArrayList<>();
        for (Long userId : userIds) {
            ImGroupMemberDO exist = existMap.get(userId);
            if (exist == null) {
                inserts.add(new ImGroupMemberDO().setGroupId(groupId).setUserId(userId)
                        .setStatus(CommonStatusEnum.ENABLE.getStatus()).setRole(role).setJoinTime(now)
                        .setAddSource(addSource).setInviterUserId(inviterUserId));
            } else if (CommonStatusEnum.DISABLE.getStatus().equals(exist.getStatus())) {
                updates.add(new ImGroupMemberDO().setId(exist.getId())
                        .setStatus(CommonStatusEnum.ENABLE.getStatus()).setRole(role).setJoinTime(now)
                        .setAddSource(addSource).setInviterUserId(inviterUserId));
            }
        }

        // 2.1 先做 update，update 没有并发冲突风险
        if (CollUtil.isNotEmpty(updates)) {
            for (ImGroupMemberDO update : updates) {
                groupMemberMapper.updateRejoinFields(update.getId(), update.getStatus(), update.getJoinTime(),
                        update.getRole(), update.getAddSource(), update.getInviterUserId());
            }
        }
        // 2.2 批量 insert。并发场景下若其它请求已先一步插入同一 (groupId, userId)，
        //     会触发唯一索引冲突，此时降级为逐个 addGroupMember（利用其兜底逻辑幂等处理）。
        if (CollUtil.isNotEmpty(inserts)) {
            try {
                groupMemberMapper.insertBatch(inserts);
            } catch (DuplicateKeyException e) {
                log.warn("[addGroupMembers][groupId({}) userIds({}) 批量插入冲突，降级为逐个处理]", groupId, userIds);
                for (ImGroupMemberDO insert : inserts) {
                    addGroupMember(groupId, insert.getUserId(), role, addSource, inviterUserId);
                }
            }
        }
    }

    @Override
    public ImGroupMemberDO validateMemberInGroup(Long groupId, Long userId) {
        ImGroupMemberDO member = groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        if (member == null || CommonStatusEnum.DISABLE.getStatus().equals(member.getStatus())) {
            throw exception(GROUP_MEMBER_NOT_IN_GROUP);
        }
        return member;
    }

    @Override
    public void validateMembersInGroup(Long groupId, Collection<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        // 一次性拉取目标 userId 的成员记录，仅保留活跃状态
        List<ImGroupMemberDO> members = groupMemberMapper.selectListByGroupIdAndUserIds(groupId, userIds);
        Set<Long> activeUserIds = convertSet(members, ImGroupMemberDO::getUserId,
                member -> CommonStatusEnum.ENABLE.getStatus().equals(member.getStatus()));
        // 任一 userId 不在活跃集合即抛
        for (Long userId : userIds) {
            if (!activeUserIds.contains(userId)) {
                throw exception(GROUP_MEMBER_NOT_IN_GROUP);
            }
        }
    }

    @Override
    public int updateGroupMemberRole(Long groupId, Collection<Long> userIds, Integer role) {
        if (CollUtil.isEmpty(userIds)) {
            return 0;
        }
        return groupMemberMapper.updateListByGroupIdAndUserIds(groupId, userIds, new ImGroupMemberDO().setRole(role));
    }

    @Override
    public Long getGroupMemberCountByRole(Long groupId, Integer role) {
        return groupMemberMapper.selectCountByGroupIdAndRoleAndStatus(
                groupId, role, CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public void updateGroupMember(Long userId, ImGroupMemberUpdateReqVO updateReqVO) {
        Long groupId = updateReqVO.getGroupId();
        // 1. 校验是群的有效成员
        ImGroupMemberDO member = validateMemberInGroup(groupId, userId);

        // 2. 更新群成员信息
        ImGroupMemberDO updateObj = BeanUtils.toBean(updateReqVO, ImGroupMemberDO.class)
                .setId(member.getId());
        groupMemberMapper.updateById(updateObj);

        // 3.1 displayUserName 是公开字段，单独走 GROUP_MEMBER_NICKNAME_UPDATE 在线同步给全员；空串视为「清空昵称」也要同步；与旧值相同跳过
        if (updateReqVO.getDisplayUserName() != null
                && ObjUtil.notEqual(updateReqVO.getDisplayUserName(), member.getDisplayUserName())) {
            groupMessageService.sendGroupMessage(userId, ImGroupMessageSendDTO.ofGroupMemberNicknameUpdate(
                    groupId, userId, updateReqVO.getDisplayUserName()));
        }
        // 3.2 silent / groupRemark 是个人字段，仅推自己做多端同步；与旧值都相同跳过
        boolean silentChanged = updateReqVO.getSilent() != null
                && ObjUtil.notEqual(updateReqVO.getSilent(), member.getSilent());
        boolean groupRemarkChanged = updateReqVO.getGroupRemark() != null
                && ObjUtil.notEqual(updateReqVO.getGroupRemark(), member.getGroupRemark());
        if (silentChanged || groupRemarkChanged) {
            groupMessageService.sendGroupMessage(userId, ListUtil.of(userId), ImGroupMessageSendDTO.ofGroupMemberSettingUpdate(
                    groupId, userId, updateReqVO.getSilent(), updateReqVO.getGroupRemark()));
        }
    }

    @Override
    @CacheEvict(cacheNames = GROUP_MEMBER_IDS, key = "#groupId")
    public void removeGroupMember(Long groupId, Long userId) {
        // 1. 校验是群的有效成员
        ImGroupMemberDO member = validateMemberInGroup(groupId, userId);
        // 2. 更新为退群状态
        groupMemberMapper.updateById(new ImGroupMemberDO().setId(member.getId())
                .setStatus(CommonStatusEnum.DISABLE.getStatus())
                .setQuitTime(LocalDateTime.now()));
    }

    @Override
    @CacheEvict(cacheNames = GROUP_MEMBER_IDS, key = "#groupId")
    public void removeGroupMembers(Long groupId, Collection<Long> userIds) {
        groupMemberMapper.updateByGroupIdAndUserIdsAndStatus(groupId, userIds,
                CommonStatusEnum.ENABLE.getStatus(),
                new ImGroupMemberDO().setStatus(CommonStatusEnum.DISABLE.getStatus())
                        .setQuitTime(LocalDateTime.now()));
    }

    @Override
    @CacheEvict(cacheNames = GROUP_MEMBER_IDS, key = "#groupId")
    public void removeGroupMembersByGroupId(Long groupId) {
        groupMemberMapper.updateByGroupIdAndStatus(groupId, CommonStatusEnum.ENABLE.getStatus(),
                new ImGroupMemberDO().setStatus(CommonStatusEnum.DISABLE.getStatus())
                        .setQuitTime(LocalDateTime.now()));
    }

    @Override
    public Map<Long, Long> getActiveMemberCountMap(Collection<Long> groupIds) {
        return groupMemberMapper.selectCountMapByGroupIdsAndStatus(groupIds, CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public void updateGroupMemberMuteEndTime(Long groupId, Long userId, LocalDateTime muteEndTime) {
        ImGroupMemberDO member = validateMemberInGroup(groupId, userId);
        if (muteEndTime != null) {
            // 禁言：直接更新到期时间
            groupMemberMapper.updateById(new ImGroupMemberDO().setId(member.getId()).setMuteEndTime(muteEndTime));
        } else {
            // 取消禁言
            groupMemberMapper.updateMuteEndTimeNull(member.getId());
        }
    }

}
