package cn.iocoder.dashboard.modules.system.service.permission;

import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysMenuDO;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
     * 获得用户拥有的角色编号集合
     *
     * @param userId 用户编号
     * @param roleStatuses 角色状态集合. 允许为空，为空时不过滤
     * @return 角色编号集合
     */
    Set<Long> listUserRoleIds(Long userId, @Nullable Collection<Integer> roleStatuses);

    /**
     * 处理角色删除时，删除关联授权角色
     *
     * @param roleId 角色编号
     */
    void processRoleDeleted(Long roleId);

    /**
     * 处理菜单删除时，删除关联授权数据
     *
     * @param menuId 菜单编号
     */
    void processMenuDeleted(Long menuId);

}
