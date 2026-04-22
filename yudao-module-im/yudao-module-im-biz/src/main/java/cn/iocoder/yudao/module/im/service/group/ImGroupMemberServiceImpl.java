package cn.iocoder.yudao.module.im.service.group;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberUpdateReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.module.im.dal.mysql.group.ImGroupMemberMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
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
    private ImGroupMemberMapper imGroupMemberMapper;

    @Override
    public ImGroupMemberDO getGroupMember(Long id) {
        return imGroupMemberMapper.selectById(id);
    }

    @Override
    public List<ImGroupMemberDO> getGroupMemberListByGroupId(Long groupId) {
        return imGroupMemberMapper.selectListByGroupId(groupId);
    }

    @Override
    public List<ImGroupMemberDO> getActiveGroupMemberListByGroupId(Long groupId) {
        return imGroupMemberMapper.selectListByGroupIdAndStatus(groupId, CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public List<ImGroupMemberDO> getActiveGroupMemberListByUserId(Long userId) {
        return imGroupMemberMapper.selectListByUserIdAndStatus(userId, CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public List<ImGroupMemberDO> getQuitGroupMemberListByUserId(Long userId, LocalDateTime minQuitTime) {
        return imGroupMemberMapper.selectQuitListByUserId(userId, minQuitTime);
    }

    @Override
    public ImGroupMemberDO addGroupMember(Long groupId, Long userId) {
        ImGroupMemberDO member = new ImGroupMemberDO()
                .setGroupId(groupId).setUserId(userId)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()).setJoinTime(LocalDateTime.now());
        imGroupMemberMapper.insert(member);
        return member;
    }

    @Override
    public ImGroupMemberDO validateMemberInGroup(Long groupId, Long userId) {
        ImGroupMemberDO member = imGroupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
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
        imGroupMemberMapper.updateById(updateObj);
    }

    @Override
    public void removeGroupMember(Long groupId, Long userId) {
        // 1. 校验是群的有效成员
        ImGroupMemberDO member = validateMemberInGroup(groupId, userId);
        // 2. 更新为退群状态
        imGroupMemberMapper.updateById(new ImGroupMemberDO().setId(member.getId())
                .setStatus(CommonStatusEnum.DISABLE.getStatus())
                .setQuitTime(LocalDateTime.now()));
    }

    @Override
    public void removeGroupMembersByGroupId(Long groupId) {
        imGroupMemberMapper.updateByGroupIdAndStatus(groupId, CommonStatusEnum.ENABLE.getStatus(),
                new ImGroupMemberDO().setStatus(CommonStatusEnum.DISABLE.getStatus())
                        .setQuitTime(LocalDateTime.now()));
    }

}
