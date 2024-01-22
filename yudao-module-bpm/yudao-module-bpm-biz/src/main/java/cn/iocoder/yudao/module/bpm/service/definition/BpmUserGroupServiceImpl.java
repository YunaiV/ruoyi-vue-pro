package cn.iocoder.yudao.module.bpm.service.definition;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.group.BpmUserGroupCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.group.BpmUserGroupPageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.group.BpmUserGroupUpdateReqVO;
import cn.iocoder.yudao.module.bpm.convert.definition.BpmUserGroupConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmUserGroupDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.definition.BpmUserGroupMapper;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;

/**
 * 用户组 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class BpmUserGroupServiceImpl implements BpmUserGroupService {

    @Resource
    private BpmUserGroupMapper userGroupMapper;

    @Override
    public Long createUserGroup(BpmUserGroupCreateReqVO createReqVO) {
        // 插入
        BpmUserGroupDO userGroup = BpmUserGroupConvert.INSTANCE.convert(createReqVO);
        userGroupMapper.insert(userGroup);
        // 返回
        return userGroup.getId();
    }

    @Override
    public void updateUserGroup(BpmUserGroupUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateUserGroupExists(updateReqVO.getId());
        // 更新
        BpmUserGroupDO updateObj = BpmUserGroupConvert.INSTANCE.convert(updateReqVO);
        userGroupMapper.updateById(updateObj);
    }

    @Override
    public void deleteUserGroup(Long id) {
        // 校验存在
        this.validateUserGroupExists(id);
        // 删除
        userGroupMapper.deleteById(id);
    }

    private void validateUserGroupExists(Long id) {
        if (userGroupMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception(USER_GROUP_NOT_EXISTS);
        }
    }

    @Override
    public BpmUserGroupDO getUserGroup(Long id) {
        return userGroupMapper.selectById(id);
    }

    @Override
    public List<BpmUserGroupDO> getUserGroupList(Collection<Long> ids) {
        return userGroupMapper.selectBatchIds(ids);
    }


    @Override
    public List<BpmUserGroupDO> getUserGroupListByStatus(Integer status) {
        return userGroupMapper.selectListByStatus(status);
    }

    @Override
    public PageResult<BpmUserGroupDO> getUserGroupPage(BpmUserGroupPageReqVO pageReqVO) {
        return userGroupMapper.selectPage(pageReqVO);
    }

    @Override
    public void validUserGroups(Set<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 获得用户组信息
        List<BpmUserGroupDO> userGroups = userGroupMapper.selectBatchIds(ids);
        Map<Long, BpmUserGroupDO> userGroupMap = CollectionUtils.convertMap(userGroups, BpmUserGroupDO::getId);
        // 校验
        ids.forEach(id -> {
            BpmUserGroupDO userGroup = userGroupMap.get(id);
            if (userGroup == null) {
                throw ServiceExceptionUtil.exception(USER_GROUP_NOT_EXISTS);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(userGroup.getStatus())) {
                throw exception(USER_GROUP_IS_DISABLE, userGroup.getName());
            }
        });
    }

}
