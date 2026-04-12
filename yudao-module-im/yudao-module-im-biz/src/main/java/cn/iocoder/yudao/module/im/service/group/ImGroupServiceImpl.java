package cn.iocoder.yudao.module.im.service.group;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.module.im.controller.admin.group.vo.*;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.im.dal.mysql.group.ImGroupMapper;

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

}