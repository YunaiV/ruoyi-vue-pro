package cn.iocoder.yudao.coreservice.modules.system.service.user.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.mysql.user.SysUserCoreMapper;
import cn.iocoder.yudao.coreservice.modules.system.service.user.SysUserCoreService;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.coreservice.modules.system.enums.SysErrorCodeConstants.USER_IS_DISABLE;
import static cn.iocoder.yudao.coreservice.modules.system.enums.SysErrorCodeConstants.USER_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 后台用户 Service Core 实现
 *
 * @author 芋道源码
 */
@Service
public class SysUserCoreServiceImpl implements SysUserCoreService {

    @Resource
    private SysUserCoreMapper userCoreMapper;

    @Override
    public SysUserDO getUser(Long id) {
        return userCoreMapper.selectById(id);
    }

    @Override
    public List<SysUserDO> getUsersByDeptIds(Collection<Long> deptIds) {
        if (CollUtil.isEmpty(deptIds)) {
            return Collections.emptyList();
        }
        return userCoreMapper.selectListByDeptIds(deptIds);
    }

    @Override
    public List<SysUserDO> getUsersByPostIds(Collection<Long> postIds) {
        if (CollUtil.isEmpty(postIds)) {
            return Collections.emptyList();
        }
        // 过滤不符合条件的
        // TODO 芋艿：暂时只能内存过滤。解决方案：1、新建一个关联表；2、基于 where + 函数；3、json 字段，适合 mysql 8+ 版本
        List<SysUserDO> users = userCoreMapper.selectList();
        users.removeIf(user -> !CollUtil.containsAny(user.getPostIds(), postIds));
        return users;
    }

    @Override
    public List<SysUserDO> getUsers(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return userCoreMapper.selectBatchIds(ids);
    }

    @Override
    public void validUsers(Set<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 获得岗位信息
        List<SysUserDO> users = userCoreMapper.selectBatchIds(ids);
        Map<Long, SysUserDO> userMap = CollectionUtils.convertMap(users, SysUserDO::getId);
        // 校验
        ids.forEach(id -> {
            SysUserDO user = userMap.get(id);
            if (user == null) {
                throw exception(USER_NOT_EXISTS);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(user.getStatus())) {
                throw exception(USER_IS_DISABLE, user.getNickname());
            }
        });
    }

}
