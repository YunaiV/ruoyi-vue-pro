package cn.iocoder.yudao.module.system.service.permission;

import cn.iocoder.yudao.module.system.controller.admin.permission.vo.menu.MenuCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.menu.MenuListReqVO;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.menu.MenuUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;

import java.util.Collection;
import java.util.List;

/**
 * 菜单 Service 接口
 *
 * @author 芋道源码
 */
public interface MenuService {

    /**
     * 初始化菜单的本地缓存
     */
    void initLocalCache();

    /**
     * 创建菜单
     *
     * @param reqVO 菜单信息
     * @return 创建出来的菜单编号
     */
    Long createMenu(MenuCreateReqVO reqVO);

    /**
     * 更新菜单
     *
     * @param reqVO 菜单信息
     */
    void updateMenu(MenuUpdateReqVO reqVO);

    /**
     * 删除菜单
     *
     * @param id 菜单编号
     */
    void deleteMenu(Long id);

    /**
     * 获得所有菜单列表
     *
     * @return 菜单列表
     */
    List<MenuDO> getMenuList();

    /**
     * 基于租户，筛选菜单列表
     * 注意，如果是系统租户，返回的还是全菜单
     *
     * @param reqVO 筛选条件请求 VO
     * @return 菜单列表
     */
    List<MenuDO> getMenuListByTenant(MenuListReqVO reqVO);

    /**
     * 筛选菜单列表
     *
     * @param reqVO 筛选条件请求 VO
     * @return 菜单列表
     */
    List<MenuDO> getMenuList(MenuListReqVO reqVO);

    /**
     * 获得所有菜单，从缓存中
     *
     * 任一参数为空时，则返回为空
     *
     * @param menuTypes 菜单类型数组
     * @param menusStatuses 菜单状态数组
     * @return 菜单列表
     */
    List<MenuDO> getMenuListFromCache(Collection<Integer> menuTypes, Collection<Integer> menusStatuses);

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
    List<MenuDO> getMenuListFromCache(Collection<Long> menuIds, Collection<Integer> menuTypes,
                                      Collection<Integer> menusStatuses);

    /**
     * 获得权限对应的菜单数组
     *
     * @param permission 权限标识
     * @return 数组
     */
    List<MenuDO> getMenuListByPermissionFromCache(String permission);

    /**
     * 获得菜单
     *
     * @param id 菜单编号
     * @return 菜单
     */
    MenuDO getMenu(Long id);

}
