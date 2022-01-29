package cn.iocoder.yudao.module.system.service.permission.impl;

import cn.iocoder.yudao.module.system.dal.dataobject.permission.SysUserRoleDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.SysUserRoleCoreMapper;
import cn.iocoder.yudao.module.system.service.permission.SysPermissionCoreService;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Set;
/**
 * 权限 Core Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class SysPermissionCoreServiceImpl implements SysPermissionCoreService {

    @Resource
    private SysUserRoleCoreMapper userRoleCoreMapper;

    @Override
    public Set<Long> getUserRoleIdListByRoleIds(Collection<Long> roleIds) {
        return CollectionUtils.convertSet(userRoleCoreMapper.selectListByRoleIds(roleIds),
                SysUserRoleDO::getRoleId);
    }
}
