package cn.iocoder.dashboard.modules.system.service.permission;

import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysMenuDO;

import java.util.Collection;
import java.util.List;

/**
 * 权限 Service 接口
 *
 * 提供用户-角色、角色-菜单、角色-部门的关联权限处理
 *
 * @author 芋道源码
 */
public interface SysPermissionService {

    /**
     * 初始化
     */
    void init();

    /**
     * 获得角色们拥有的菜单列表，从缓存中获取
     *
     * 任一参数为空时，则返回为空
     *
     * @param roleIds 角色编号数组
     * @param menuTypes 菜单类型数组
     * @param menusStatuses 菜单状态数组
     * @return 菜单列表
     */
    List<SysMenuDO> listRoleMenusFromCache(Collection<Long> roleIds, Collection<Integer> menuTypes,
                                           Collection<Integer> menusStatuses);

    /**
     * 获得用户拥有的角色编号数组
     *
     * @param userId 用户编号
     * @return 角色编号数组
     */
    List<Long> listUserRoleIds(Long userId);

    /**
     * 获得部门拥有的角色编号
     *
     * @param deptId 部门编号
     * @return 角色编号
     */
    Long getDeptRoleId(Long deptId);

    /**
     * 删除授予给角色的菜单们
     *
     * @param menuId 菜单编号
     */
    void deleteRolesMenuByMenuId(Long menuId);

}
