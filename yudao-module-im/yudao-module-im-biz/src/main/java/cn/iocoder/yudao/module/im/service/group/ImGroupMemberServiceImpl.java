package cn.iocoder.yudao.module.im.service.group;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberUpdateReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.mysql.group.ImGroupMemberMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.GROUP_MEMBER_NOT_IN_GROUP;

/**
 * 群成员 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ImGroupMemberServiceImpl implements ImGroupMemberService {

    @Resource
    private ImGroupMemberMapper groupMemberMapper;

    @Override
    public ImGroupMemberDO getGroupMember(Long id) {
        return groupMemberMapper.selectById(id);
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
    public List<ImGroupMemberDO> getActiveGroupMemberListByUserId(Long userId) {
        return groupMemberMapper.selectListByUserIdAndStatus(userId, CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public List<ImGroupMemberDO> getQuitGroupMemberListByUserId(Long userId, LocalDateTime minQuitTime) {
        return groupMemberMapper.selectQuitListByUserId(userId, minQuitTime);
    }

    @Override
    public ImGroupMemberDO addGroupMember(Long groupId, Long userId) {
        ImGroupMemberDO member = new ImGroupMemberDO()
                .setGroupId(groupId).setUserId(userId)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()).setJoinTime(LocalDateTime.now());
        groupMemberMapper.insert(member);
        return member;
    }

    @Override
    public void addGroupMembers(Long groupId, Collection<Long> userIds) {
        LocalDateTime now = LocalDateTime.now();
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
                        .setStatus(CommonStatusEnum.ENABLE.getStatus()).setJoinTime(now));
            } else if (CommonStatusEnum.DISABLE.getStatus().equals(exist.getStatus())) {
                updates.add(new ImGroupMemberDO().setId(exist.getId())
                        .setStatus(CommonStatusEnum.ENABLE.getStatus()).setJoinTime(now));
            }
        }

        // 2. 执行
        if (CollUtil.isNotEmpty(inserts)) {
            groupMemberMapper.insertBatch(inserts);
        }
        if (CollUtil.isNotEmpty(updates)) {
            groupMemberMapper.updateBatch(updates);
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
    public void updateGroupMember(Long userId, ImGroupMemberUpdateReqVO updateReqVO) {
        // 1. 校验是群的有效成员
        ImGroupMemberDO member = validateMemberInGroup(updateReqVO.getGroupId(), userId);
        // 2. 更新群成员信息
        ImGroupMemberDO updateObj = BeanUtils.toBean(updateReqVO, ImGroupMemberDO.class)
                .setId(member.getId());
        groupMemberMapper.updateById(updateObj);
    }

    @Override
    public void removeGroupMember(Long groupId, Long userId) {
        // 1. 校验是群的有效成员
        ImGroupMemberDO member = validateMemberInGroup(groupId, userId);
        // 2. 更新为退群状态
        groupMemberMapper.updateById(new ImGroupMemberDO().setId(member.getId())
                .setStatus(CommonStatusEnum.DISABLE.getStatus())
                .setQuitTime(LocalDateTime.now()));
    }

    @Override
    public void removeGroupMembers(Long groupId, Collection<Long> userIds) {
        groupMemberMapper.updateByGroupIdAndUserIdsAndStatus(groupId, userIds,
                CommonStatusEnum.ENABLE.getStatus(),
                new ImGroupMemberDO().setStatus(CommonStatusEnum.DISABLE.getStatus())
                        .setQuitTime(LocalDateTime.now()));
    }

    @Override
    public void removeGroupMembersByGroupId(Long groupId) {
        groupMemberMapper.updateByGroupIdAndStatus(groupId, CommonStatusEnum.ENABLE.getStatus(),
                new ImGroupMemberDO().setStatus(CommonStatusEnum.DISABLE.getStatus())
                        .setQuitTime(LocalDateTime.now()));
    }

}
