package cn.iocoder.dashboard.modules.system.service.permission;

import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysMenuDO;

import java.util.Collection;
import java.util.List;

/**
 * 菜单 Service 接口
 */
public interface SysMenuService {

    /**
     *
     */
    void init();

    /**
     * 获得所有菜单，从缓存中
     *
     * 任一参数为空时，则返回为空
     *
     * @param menuTypes 菜单类型数组
     * @param menusStatuses 菜单状态数组
     * @return 菜单列表
     */
    List<SysMenuDO> listMenusFromCache(Collection<String> menuTypes, Collection<String> menusStatuses);

    /**
     * 获得指定编号的菜单数组，从缓存中
     *
     * 任一参数为空时，则返回为空
     *
     * @param menuIds 菜单编号数组
     * @param menuTypes 菜单类型数组
     * @param menusStatuses 菜单状态数组
     * @return 菜单数组
     */
    List<SysMenuDO> listMenusFromCache(Collection<Long> menuIds, Collection<String> menuTypes,
                                       Collection<String> menusStatuses);

}
