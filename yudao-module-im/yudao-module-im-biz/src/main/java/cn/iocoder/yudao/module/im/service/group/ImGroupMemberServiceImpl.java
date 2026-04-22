package cn.iocoder.yudao.module.im.service.group;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberInviteReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberUpdateReqVO;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.im.dal.mysql.group.ImGroupMemberMapper;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;

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
    public Long inviteGroupMember(ImGroupMemberInviteReqVO inviteReqVO) {
        // TODO @AI：需要群主校验下；
        // 插入
        ImGroupMemberDO groupMember = BeanUtils.toBean(inviteReqVO, ImGroupMemberDO.class);
        // TODO @AI：默认字段的设置；
        imGroupMemberMapper.insert(groupMember);
        // TODO @AI：或者调用内部的 createGroupMember 方法，设置默认字段的值；
        // 返回
        return groupMember.getId();
    }

    @Override
    public void updateGroupMember(ImGroupMemberUpdateReqVO updateReqVO) {
        // 校验存在
        validateGroupMemberExists(updateReqVO.getId());
        // 更新
        ImGroupMemberDO updateObj = BeanUtils.toBean(updateReqVO, ImGroupMemberDO.class);
        imGroupMemberMapper.updateById(updateObj);
    }

    // TODO @AI：是不是不存在删除一说？
    @Override
    public void removeGroupMember(Long id) {
        // TODO @AI：需要群主校验下；
        // 校验存在
        validateGroupMemberExists(id);

        // 删除
        imGroupMemberMapper.deleteById(id);
    }

    private void validateGroupMemberExists(Long id) {
        if (imGroupMemberMapper.selectById(id) == null) {
            throw exception(GROUP_MEMBER_NOT_EXISTS);
        }
    }

    @Override
    public ImGroupMemberDO getGroupMember(Long id) {
        return imGroupMemberMapper.selectById(id);
    }

    @Override
    public List<ImGroupMemberDO> getGroupMemberListByGroupId(Long groupId) {
        return imGroupMemberMapper.selectListByGroupId(groupId);
    }

    @Override
    public ImGroupMemberDO getGroupMember(Long groupId, Long userId) {
        return imGroupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
    }

    @Override
    public List<ImGroupMemberDO> getGroupMemberListByUserId(Long userId) {
        return imGroupMemberMapper.selectListByUserId(userId);
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
        // TODO @AI：要不要有个 initXXX 方法，设置默认字段的值；
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
    public void updateGroupMember(Long groupId, Long userId, String displayUserName, String displayGroupName) {
        // 1. 校验是群的有效成员
        ImGroupMemberDO member = validateMemberInGroup(groupId, userId);

        // 2. 更新展示信息
        imGroupMemberMapper.updateById(new ImGroupMemberDO().setId(member.getId())
                .setDisplayUserName(displayUserName).setDisplayGroupName(displayGroupName));

        // TODO @AI：需要分析下，是否需要
    }

}
