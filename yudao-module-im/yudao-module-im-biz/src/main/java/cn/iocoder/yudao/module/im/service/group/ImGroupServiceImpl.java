package cn.iocoder.yudao.module.im.service.group;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;


import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.*;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.im.dal.mysql.group.ImGroupMapper;
import cn.iocoder.yudao.module.im.dal.mysql.group.ImGroupMemberMapper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.*;

/**
 * 群 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ImGroupServiceImpl implements ImGroupService {

    @Resource
    private ImGroupMapper imGroupMapper;

    @Resource
    private ImGroupMemberMapper imGroupMemberMapper;

    @Override
    public Long createGroup(ImGroupSaveReqVO createReqVO) {
        // 插入
        ImGroupDO group = BeanUtils.toBean(createReqVO, ImGroupDO.class);
        imGroupMapper.insert(group);
        // 返回
        return group.getId();
    }

    @Override
    public void updateGroup(ImGroupSaveReqVO updateReqVO) {
        // 校验存在
        validateGroupExists(updateReqVO.getId());
        // 更新
        ImGroupDO updateObj = BeanUtils.toBean(updateReqVO, ImGroupDO.class);
        imGroupMapper.updateById(updateObj);
    }

    @Override
    public void deleteGroup(Long id) {
        // 校验存在
        validateGroupExists(id);
        // 删除
        imGroupMapper.deleteById(id);
    }

    @Override
    public ImGroupDO validateGroupExists(Long id) {
        ImGroupDO group = imGroupMapper.selectById(id);
        if (group == null) {
            throw exception(GROUP_NOT_EXISTS);
        }
        if (Boolean.TRUE.equals(group.getBanned())) {
            throw exception(GROUP_BANNED);
        }
        if (Boolean.TRUE.equals(group.getDissolved())) {
            throw exception(GROUP_DISSOLVED);
        }
        return group;
    }

    @Override
    public ImGroupDO getGroup(Long id) {
        return imGroupMapper.selectById(id);
    }

    @Override
    public PageResult<ImGroupDO> getGroupPage(ImGroupPageReqVO pageReqVO) {
        return imGroupMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ImGroupDO> getMyGroupList(Long userId) {
        // 1. 查用户所在的、仍有效的群成员记录（仅 ENABLE 状态）
        List<ImGroupMemberDO> members = imGroupMemberMapper.selectEnabledListByUserId(userId);
        if (members.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> groupIds = CollectionUtils.convertSet(members, ImGroupMemberDO::getGroupId);
        if (groupIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 2. 查群并过滤掉已封禁/解散的
        List<ImGroupDO> groups = imGroupMapper.selectByIds(groupIds);
        groups.removeIf(g -> Boolean.TRUE.equals(g.getBanned()) || Boolean.TRUE.equals(g.getDissolved()));
        return groups;
    }

}