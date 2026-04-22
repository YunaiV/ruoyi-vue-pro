package cn.iocoder.yudao.module.im.service.group;

import cn.hutool.core.collection.CollUtil;
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
    private ImGroupMapper imGroupMapper;

    @Resource
    private ImGroupMemberMapper imGroupMemberMapper;

    // TODO @AI：返回对象；
    // TODO @AI：要不要拆分 VO；ImGroupCreateReqVO（name）
    @Override
    public Long createGroup(ImGroupSaveReqVO createReqVO) {
        // 插入
        ImGroupDO group = BeanUtils.toBean(createReqVO, ImGroupDO.class);
        imGroupMapper.insert(group);

        // TODO @AI：群主 GroupMember；

        // TODO 多终端推送消息（group message）
        return group.getId();
    }

    // TODO @AI：要不要拆分 VO；ImGroupUpdateReqVO（name、avatar、notice）；我在本群的昵称/群名备注；
    // TODO 1. 需要校验群主：name、avatar、notice；如果修改群信息，是不是要群发消息？（群信息变更消息）；
    // TODO 2. 需要校验群成员：我在本群的昵称/群名备注
    // TODO @AI：返回 返回对象；
    @Override
    public void updateGroup(ImGroupSaveReqVO updateReqVO) {
        // 校验存在
        validateGroupExists(updateReqVO.getId());

        // 更新
        ImGroupDO updateObj = BeanUtils.toBean(updateReqVO, ImGroupDO.class);
        imGroupMapper.updateById(updateObj);
    }

    // TODO @AI：dissolve 解散群；
    @Override
    public void deleteGroup(Long id) {
        // 校验存在
        validateGroupExists(id);
        // TODO @AI：群主才可以；

        // TODO @AI：清理已读缓存；

        // TODO @AI：推送群解散的提示；

        // TODO @AI：推送群信息变化；

        // 删除
        imGroupMapper.deleteById(id);
    }

    // TODO @AI：quit 退群；

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

    // TODO @AI：add 加群；

    // TODO @AI：remove 踢人；

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
        if (CollUtil.isNotEmpty(members)) {
            return Collections.emptyList();
        }
        Set<Long> groupIds = CollectionUtils.convertSet(members, ImGroupMemberDO::getGroupId);
        if (CollUtil.isEmpty(groupIds)) {
            return Collections.emptyList();
        }
        // 2. 查群并过滤掉已封禁/解散的
        // TODO @AI：查询的时候，就过滤掉！不要内存过滤；
        List<ImGroupDO> groups = imGroupMapper.selectByIds(groupIds);
        groups.removeIf(g -> Boolean.TRUE.equals(g.getBanned()) || Boolean.TRUE.equals(g.getDissolved()));
        return groups;
    }

}