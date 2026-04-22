package cn.iocoder.yudao.module.im.service.group;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberSaveReqVO;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
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
    public Long createGroupMember(ImGroupMemberSaveReqVO createReqVO) {
        // 插入
        ImGroupMemberDO groupMember = BeanUtils.toBean(createReqVO, ImGroupMemberDO.class);
        imGroupMemberMapper.insert(groupMember);
        // 返回
        return groupMember.getId();
    }

    @Override
    public void updateGroupMember(ImGroupMemberSaveReqVO updateReqVO) {
        // 校验存在
        validateGroupMemberExists(updateReqVO.getId());
        // 更新
        ImGroupMemberDO updateObj = BeanUtils.toBean(updateReqVO, ImGroupMemberDO.class);
        imGroupMemberMapper.updateById(updateObj);
    }

    @Override
    public void deleteGroupMember(Long id) {
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
    public PageResult<ImGroupMemberDO> getGroupMemberPage(ImGroupMemberPageReqVO pageReqVO) {
        return imGroupMemberMapper.selectPage(pageReqVO);
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
        return imGroupMemberMapper.selectEnabledListByGroupId(groupId);
    }

    @Override
    public List<ImGroupMemberDO> getActiveGroupMemberListByUserId(Long userId) {
        return imGroupMemberMapper.selectEnabledListByUserId(userId);
    }

    @Override
    public List<ImGroupMemberDO> getQuitGroupMemberListByUserId(Long userId, LocalDateTime minQuitTime) {
        return imGroupMemberMapper.selectQuitListByUserId(userId, minQuitTime);
    }

    @Override
    public ImGroupMemberDO validateMemberInGroup(Long groupId, Long userId) {
        ImGroupMemberDO member = imGroupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        if (member == null || CommonStatusEnum.DISABLE.getStatus().equals(member.getStatus())) {
            throw exception(GROUP_MEMBER_NOT_IN_GROUP);
        }
        return member;
    }

}
