package cn.iocoder.dashboard.modules.system.service.permission.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.permission.SysRoleMenuMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.permission.SysUserRoleMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysMenuDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysRoleDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysRoleMenuDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysUserRoleDO;
import cn.iocoder.dashboard.modules.system.service.permission.SysMenuService;
import cn.iocoder.dashboard.modules.system.service.permission.SysPermissionService;
import cn.iocoder.dashboard.modules.system.service.permission.SysRoleService;
import cn.iocoder.dashboard.util.collection.CollectionUtils;
import cn.iocoder.dashboard.util.collection.MapUtils;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 权限 Service 实现类
 *
 * @author 芋道源码
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
    public List<SysMenuDO> listRoleMenusFromCache(Collection<Long> roleIds, Collection<Integer> menuTypes,
                                                  Collection<Integer> menusStatuses) {
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
    public Set<Long> listUserRoleIds(Long userId, Collection<Integer> roleStatuses) {
        List<SysUserRoleDO> userRoleList = userRoleMapper.selectListByUserId(userId);
        // 过滤角色状态
        if (CollectionUtil.isNotEmpty(roleStatuses)) {
            userRoleList.removeIf(userRoleDO -> {
                SysRoleDO role = roleService.getRoleFromCache(userRoleDO.getRoleId());
                return role == null || !roleStatuses.contains(role.getStatus());
            });
        }
        return CollectionUtils.convertSet(userRoleList, SysUserRoleDO::getRoleId);
    }

    @Override
    public Set<Long> listRoleMenuIds(Long roleId) {
        // 如果是管理员的情况下，获取全部菜单编号
        SysRoleDO role = roleService.getRole(roleId);
        if (roleService.hasAnyAdmin(Collections.singletonList(role))) {
            return CollectionUtils.convertSet(menuService.listMenus(), SysMenuDO::getId);
        }
        // 如果是非管理员的情况下，获得拥有的菜单编号
        return CollectionUtils.convertSet(roleMenuMapper.selectListByRoleId(roleId),
                SysRoleMenuDO::getMenuId);
    }

    @Override
    @Transactional
    public void assignRoleMenu(Long roleId, Set<Long> menuIds) {
        // 获得角色拥有菜单编号
        Set<Long> dbMenuIds = CollectionUtils.convertSet(roleMenuMapper.selectListByRoleId(roleId),
                SysRoleMenuDO::getMenuId);
        // 计算新增和删除的菜单编号
        Collection<Long> createMenuIds = CollUtil.subtract(menuIds, dbMenuIds);
        Collection<Long> deleteMenuIds = CollUtil.subtract(dbMenuIds, menuIds);
        // 执行新增和删除。对于已经授权的菜单，不用做任何处理
        if (!CollectionUtil.isEmpty(createMenuIds)) {
            roleMenuMapper.insertList(roleId, createMenuIds);
        }
        if (!CollectionUtil.isEmpty(deleteMenuIds)) {
            roleMenuMapper.deleteListByRoleIdAndMenuIds(roleId, deleteMenuIds);
        }
    }

    @Override
    public void processRoleDeleted(Long roleId) {
        // TODO 实现我
//        // 标记删除 RoleResource
//        roleResourceMapper.deleteByRoleId(roleId);
//        // 标记删除 AdminRole
//        adminRoleMapper.deleteByRoleId(roleId);
    }

    @Override
    public void processMenuDeleted(Long menuId) {
        // TODO 实现我
    }

}
