package cn.iocoder.dashboard.modules.system.service.permission.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.permission.SysRoleMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysRoleDO;
import cn.iocoder.dashboard.modules.system.enums.permission.RoleKeyEnum;
import cn.iocoder.dashboard.modules.system.service.permission.SysRoleService;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色 Service 实现类
 */
@Service
@Slf4j
public class SysRoleServiceImpl implements SysRoleService {

    @Resource
    private SysRoleMapper roleMapper;

    /**
     * 角色缓存
     * key：角色编号 {@link SysRoleDO#getRoleId()}
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Map<Long, SysRoleDO> roleCache;

    /**
     * 初始化 {@link #roleCache} 缓存
     */
    @Override
    @PostConstruct
    public void init() {
        // 从数据库中读取
        List<SysRoleDO> roleDOList = roleMapper.selectList(null);
        // 写入缓存
        ImmutableMap.Builder<Long, SysRoleDO> builder = ImmutableMap.builder();
        roleDOList.forEach(sysRoleDO -> builder.put(sysRoleDO.getRoleId(), sysRoleDO));
        roleCache = builder.build();
        log.info("[init][初始化 Role 数量为 {}]", roleDOList.size());
    }

    @Override
    public List<SysRoleDO> listRolesFromCache(Collection<Long> roleIds) {
        if (CollectionUtil.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        return roleCache.values().stream().filter(roleDO -> roleIds.contains(roleDO.getRoleId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasAnyAdmin(Collection<SysRoleDO> roleList) {
        if (CollectionUtil.isEmpty(roleList)) {
            return false;
        }
        return roleList.stream().anyMatch(roleDO -> RoleKeyEnum.ADMIN.getKey().equals(roleDO.getRoleKey()));
    }

}
