package cn.iocoder.yudao.module.im.service.group;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberUpdateReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.mysql.group.ImGroupMemberMapper;
import cn.iocoder.yudao.module.im.enums.group.ImGroupMemberRoleEnum;
import cn.iocoder.yudao.module.im.service.websocket.ImWebSocketService;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImGroupMessageDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
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
    private ImWebSocketService webSocketService;

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
    public List<ImGroupMemberDO> getQuitGroupMemberListByUserId(Long userId, LocalDateTime minQuitTime) {
        return groupMemberMapper.selectQuitListByUserId(userId, minQuitTime);
    }

    @Override
    public ImGroupMemberDO addGroupMember(Long groupId, Long userId) {
        return addGroupMember(groupId, userId, ImGroupMemberRoleEnum.NORMAL.getRole());
    }

    /**
     * 并发安全：依靠 im_group_member 表的唯一索引 uk_im_group_member_group_user(group_id, user_id) 保证幂等，
     * 当并发 insert 触发 {@link DuplicateKeyException} 时降级为 select + update。
     * <p>
     * 重置旧成员行时强制重置 role，避免离群期间残留管理员身份在重新入群后被复用。
     */
    @Override
    @CacheEvict(cacheNames = GROUP_MEMBER_IDS, key = "#groupId")
    public ImGroupMemberDO addGroupMember(Long groupId, Long userId, Integer role) {
        LocalDateTime now = LocalDateTime.now();
        // 情况一：已存在记录 → 重置或跳过
        ImGroupMemberDO exists = groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        if (exists != null) {
            if (CommonStatusEnum.isDisable(exists.getStatus())) {
                groupMemberMapper.updateById(new ImGroupMemberDO().setId(exists.getId())
                        .setStatus(CommonStatusEnum.ENABLE.getStatus()).setJoinTime(now)
                        .setRole(role));
                exists.setStatus(CommonStatusEnum.ENABLE.getStatus()).setJoinTime(now).setRole(role);
            }
            return exists;
        }
        // 情况二：新增成员
        ImGroupMemberDO member = new ImGroupMemberDO()
                .setGroupId(groupId).setUserId(userId)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()).setJoinTime(now)
                .setRole(role);
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
    @CacheEvict(cacheNames = GROUP_MEMBER_IDS, key = "#groupId")
    public void addGroupMembers(Long groupId, Collection<Long> userIds) {
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
                        .setStatus(CommonStatusEnum.ENABLE.getStatus()).setRole(role).setJoinTime(now));
            } else if (CommonStatusEnum.DISABLE.getStatus().equals(exist.getStatus())) {
                updates.add(new ImGroupMemberDO().setId(exist.getId())
                        .setStatus(CommonStatusEnum.ENABLE.getStatus()).setRole(role).setJoinTime(now));
            }
        }

        // 2.1 先做 update，update 没有并发冲突风险
        if (CollUtil.isNotEmpty(updates)) {
            groupMemberMapper.updateBatch(updates);
        }
        // 2.2 批量 insert。并发场景下若其它请求已先一步插入同一 (groupId, userId)，
        //     会触发唯一索引冲突，此时降级为逐个 addGroupMember（利用其兜底逻辑幂等处理）。
        if (CollUtil.isNotEmpty(inserts)) {
            try {
                groupMemberMapper.insertBatch(inserts);
            } catch (DuplicateKeyException e) {
                log.warn("[addGroupMembers][groupId({}) userIds({}) 批量插入冲突，降级为逐个处理]", groupId, userIds);
                for (ImGroupMemberDO insert : inserts) {
                    addGroupMember(groupId, insert.getUserId());
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
    public void updateGroupMemberRole(Long groupId, Collection<Long> userIds, Integer role) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        groupMemberMapper.updateListByGroupIdAndUserIds(groupId, userIds, new ImGroupMemberDO().setRole(role));
    }

    @Override
    public Long getGroupMemberCountByRole(Long groupId, Integer role) {
        return groupMemberMapper.selectCountByGroupIdAndRoleAndStatus(
                groupId, role, CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public void updateGroupMember(Long userId, ImGroupMemberUpdateReqVO updateReqVO) {
        // 1. 校验是群的有效成员
        ImGroupMemberDO member = validateMemberInGroup(updateReqVO.getGroupId(), userId);

        // 2. 更新群成员信息
        ImGroupMemberDO updateObj = BeanUtils.toBean(updateReqVO, ImGroupMemberDO.class)
                .setId(member.getId());
        groupMemberMapper.updateById(updateObj);

        // 3. 推送群成员变更通知（多端同步，仅推给自己）
        webSocketService.sendGroupMessageAsync(userId,
                ImGroupMessageDTO.ofGroupMemberUpdate(userId, updateReqVO.getGroupId()));
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

}
