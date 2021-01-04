package cn.iocoder.dashboard.modules.system.service.permission.impl;

import cn.iocoder.dashboard.modules.system.dal.mysql.dao.permission.SysRoleDeptMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.permission.SysRoleMenuMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.permission.SysUserRoleMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.*;
import cn.iocoder.dashboard.modules.system.service.permission.SysMenuService;
import cn.iocoder.dashboard.modules.system.service.permission.SysPermissionService;
import cn.iocoder.dashboard.modules.system.service.permission.SysRoleService;
import cn.iocoder.dashboard.util.collection.CollectionUtils;
import cn.iocoder.dashboard.util.collection.MapUtils;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 权限 Service 实现类
 *
 * @author 初始化
 */
@Service
@Slf4j
public class SysPermissionServiceImpl implements SysPermissionService {

    /**
     * 角色编号与菜单编号的缓存映射
     * key：角色编号
     * value：菜单编号的数组
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Multimap<Long, Long> roleMenuCache;
    /**
     * 菜单编号与角色编号的缓存映射
     * key：菜单编号
     * value：角色编号的数组
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Multimap<Long, Long> menuRoleCache;

    @Resource
    private SysRoleMenuMapper roleMenuMapper;
    @Resource
    private SysUserRoleMapper userRoleMapper;
    @Resource
    private SysRoleDeptMapper roleDeptMapper;

    @Resource
    private SysRoleService roleService;
    @Resource
    private SysMenuService menuService;

    /**
     * 初始化 {@link #roleMenuCache} 和 {@link #menuRoleCache} 缓存
     */
    @Override
    @PostConstruct
    public void init() {
        // 初始化 roleMenuCache 和 menuRoleCache 缓存
        List<SysRoleMenuDO> roleMenuList = roleMenuMapper.selectList(null);
        ImmutableMultimap.Builder<Long, Long> roleMenuCacheBuilder = ImmutableMultimap.builder();
        ImmutableMultimap.Builder<Long, Long> menuRoleCacheBuilder = ImmutableMultimap.builder();
        roleMenuList.forEach(roleMenuDO -> {
            roleMenuCacheBuilder.put(roleMenuDO.getRoleId(), roleMenuDO.getMenuId());
            menuRoleCacheBuilder.put(roleMenuDO.getMenuId(), roleMenuDO.getRoleId());
        });
        roleMenuCache = roleMenuCacheBuilder.build();
        menuRoleCache = menuRoleCacheBuilder.build();
        log.info("[init][初始化角色与菜单的关联数量为 {}]", roleMenuList.size());
    }

    @Override
    public List<SysMenuDO> listRoleMenusFromCache(Collection<Long> roleIds, Collection<String> menuTypes,
                                                  Collection<String> menusStatuses) {
        // 任一一个参数为空时，不返回任何菜单
        if (CollectionUtils.isAnyEmpty(roleIds, menusStatuses, menusStatuses)) {
            return Collections.emptyList();
        }
        // 判断角色是否包含管理员
        List<SysRoleDO> roleList = roleService.listRolesFromCache(roleIds);
        boolean hasAdmin = roleService.hasAnyAdmin(roleList);
        // 获得角色拥有的菜单关联
        if (hasAdmin) { // 管理员，获取到全部
            return menuService.listMenusFromCache(menuTypes, menusStatuses);
        }
        List<Long> menuIds = MapUtils.getList(roleMenuCache, roleIds);
        return menuService.listMenusFromCache(menuIds, menuTypes, menusStatuses);
    }

    @Override
    public List<Long> listUserRoleIds(Long userId) {
        List<SysUserRoleDO> userRoleList = userRoleMapper.selectListByUserId(userId);
        return CollectionUtils.convertList(userRoleList, SysUserRoleDO::getRoleId);
    }

    @Override
    public Long getDeptRoleId(Long deptId) {
        SysRoleDeptDO roleDept = roleDeptMapper.selectById(deptId);
        return roleDept != null ? roleDept.getRoleId() : null;
    }

}
