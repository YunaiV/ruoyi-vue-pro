package cn.iocoder.dashboard.modules.system.service.permission.impl;

import cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.menu.SysMenuCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.menu.SysMenuListReqVO;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.menu.SysMenuRespVO;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.menu.SysMenuUpdateReqVO;
import cn.iocoder.dashboard.modules.system.convert.permission.SysMenuConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.permission.SysMenuMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysMenuDO;
import cn.iocoder.dashboard.modules.system.enums.permission.MenuIdEnum;
import cn.iocoder.dashboard.modules.system.enums.permission.MenuTypeEnum;
import cn.iocoder.dashboard.modules.system.service.permission.SysMenuService;
import cn.iocoder.dashboard.modules.system.service.permission.SysPermissionService;
import cn.iocoder.dashboard.util.collection.CollectionUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.*;

/**
 * 菜单 Service 实现
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class SysMenuServiceImpl implements SysMenuService {

    /**
     * 菜单缓存
     * key：菜单编号
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Map<Long, SysMenuDO> menuCache;
    /**
     * 权限与菜单缓存
     * key：权限 {@link SysMenuDO#getPermission()}
     * value：SysMenuDO 数组，因为一个权限可能对应多个 SysMenuDO 对象
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Multimap<String, SysMenuDO> permMenuCache;

    @Resource
    private SysMenuMapper menuMapper;
    @Resource
    private SysPermissionService permissionService;

    /**
     * 初始化 {@link #menuCache} 和 {@link #permMenuCache} 缓存
     */
    @Override
    @PostConstruct
    public void init() {
        List<SysMenuDO> menuList = menuMapper.selectList();
        ImmutableMap.Builder<Long, SysMenuDO> menuCacheBuilder = ImmutableMap.builder();
        ImmutableMultimap.Builder<String, SysMenuDO> permMenuCacheBuilder = ImmutableMultimap.builder();
        menuList.forEach(menuDO -> {
            menuCacheBuilder.put(menuDO.getId(), menuDO);
            permMenuCacheBuilder.put(menuDO.getPermission(), menuDO);
        });
        menuCache = menuCacheBuilder.build();
        permMenuCache = permMenuCacheBuilder.build();
        log.info("[init][初始化菜单数量为 {}]", menuList.size());
    }

    @Override
    public List<SysMenuRespVO> listMenus(SysMenuListReqVO reqVO) {
        List<SysMenuDO> list = menuMapper.selectList(reqVO);
        // TODO 排序
        return SysMenuConvert.INSTANCE.convertList(list);
    }

    @Override
    public List<SysMenuDO> listMenusFromCache(Collection<Integer> menuTypes, Collection<Integer> menusStatuses) {
        // 任一一个参数为空，则返回空
        if (CollectionUtils.isAnyEmpty(menuTypes, menusStatuses)) {
            return Collections.emptyList();
        }
        // 创建新数组，避免缓存被修改
        return menuCache.values().stream().filter(menu -> menuTypes.contains(menu.getType())
                && menusStatuses.contains(menu.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SysMenuDO> listMenusFromCache(Collection<Long> menuIds, Collection<Integer> menuTypes,
                                              Collection<Integer> menusStatuses) {
        // 任一一个参数为空，则返回空
        if (CollectionUtils.isAnyEmpty(menuIds, menuTypes, menusStatuses)) {
            return Collections.emptyList();
        }
        return menuCache.values().stream().filter(menu -> menuIds.contains(menu.getId())
                && menuTypes.contains(menu.getType())
                && menusStatuses.contains(menu.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public Long createMenu(SysMenuCreateReqVO reqVO) {
        // 校验父菜单存在
        checkParentResource(reqVO.getParentId(), null);
        // 校验菜单（自己）
        checkResource(reqVO.getParentId(), reqVO.getName(), null);
        // 插入数据库
        SysMenuDO menu = SysMenuConvert.INSTANCE.convert(reqVO);
        initMenuProperty(menu);
        menuMapper.insert(menu);
        // 返回
        return menu.getId();
    }

    @Override
    public void updateMenu(SysMenuUpdateReqVO reqVO) {
        // 校验更新的菜单是否存在
        if (menuMapper.selectById(reqVO.getId()) == null) {
            throw ServiceExceptionUtil.exception(MENU_NOT_EXISTS);
        }
        // 校验父菜单存在
        checkParentResource(reqVO.getParentId(), reqVO.getId());
        // 校验菜单（自己）
        checkResource(reqVO.getParentId(), reqVO.getName(), reqVO.getId());
        // 更新到数据库
        SysMenuDO updateObject = SysMenuConvert.INSTANCE.convert(reqVO);
        initMenuProperty(updateObject);
        menuMapper.updateById(updateObject);
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单编号
     */
    public void deleteMenu(Long menuId) {
        // 校验更新的菜单是否存在
        if (menuMapper.selectById(menuId) == null) {
            throw ServiceExceptionUtil.exception(MENU_NOT_EXISTS);
        }
        // 校验是否还有子菜单
        if (menuMapper.selectCountByParentId(menuId) > 0) {
            throw ServiceExceptionUtil.exception(MENU_EXISTS_CHILDREN);
        }
        // 校验删除的菜单是否存在
        if (menuMapper.selectById(menuId) == null) {
            throw ServiceExceptionUtil.exception(MENU_NOT_EXISTS);
        }
        // 标记删除
        menuMapper.deleteById(menuId);
        // 删除授予给角色的权限
        permissionService.processMenuDeleted(menuId);
    }

    @Override
    public SysMenuDO getMenu(Long id) {
        return menuMapper.selectById(id);
    }

//    /**
//     * 获得菜单列表
//     *
//     * @param menuIds 菜单编号列表
//     * @return 菜单列表
//     */
//    public List<ResourceBO> listResources(List<Integer> menuIds) {
//        List<ResourceDO> menuDOs = menuMapper.selectBatchIds(menuIds);
//        return ResourceConvert.INSTANCE.convertList(menuDOs);
//    }
//
//    /**
//     * 获得菜单全列表
//     *
//     * @return 菜单全列表
//     */
//    public List<ResourceBO> listResources() {
//        List<ResourceDO> menuDOs = menuMapper.selectList(null);
//        return ResourceConvert.INSTANCE.convertList(menuDOs);
//    }
//
//    /**
//     * 获得指定类型的菜单列表
//     *
//     * @param type 菜单类型，允许空
//     * @return 菜单列表
//     */
//    public List<ResourceBO> listResourcesByType(Integer type) {
//        List<ResourceDO> menuDOs = menuMapper.selectListByType(type);
//        return ResourceConvert.INSTANCE.convertList(menuDOs);
//    }
//
//    /**
//     * 获得角色拥有的菜单列表
//     *
//     * @param roleIds 角色编号
//     * @param type 菜单类型，允许空
//     * @return 菜单列表
//     */
//    public List<ResourceBO> listRoleResourcesByType(Collection<Integer> roleIds, Integer type) {
//        List<RoleResourceDO> roleResourceDOs = roleResourceMapper.selectListByRoleIds(roleIds);
//        if (CollectionUtils.isEmpty(roleResourceDOs)) {
//            return Collections.emptyList();
//        }
//        List<ResourceDO> menuDOs = menuMapper.selectListByIdsAndType(
//                CollectionUtils.convertSet(roleResourceDOs, RoleResourceDO::getResourceId), type);
//        return ResourceConvert.INSTANCE.convertList(menuDOs);
//    }
//
    /**
     * 校验父菜单是否合法
     *
     * 1. 不能设置自己为父菜单
     * 2. 父菜单不存在
     * 3. 父菜单必须是 {@link MenuTypeEnum#MENU} 菜单类型
     *
     * @param parentId 父菜单编号
     * @param childId 当前菜单编号
     */
    private void checkParentResource(Long parentId, Long childId) {
        if (parentId == null || MenuIdEnum.ROOT.getId().equals(parentId)) {
            return;
        }
        // 不能设置自己为父菜单
        if (parentId.equals(childId)) {
            throw ServiceExceptionUtil.exception(MENU_PARENT_ERROR);
        }
        SysMenuDO menu = menuMapper.selectById(parentId);
        // 父菜单不存在
        if (menu == null) {
            throw ServiceExceptionUtil.exception(MENU_PARENT_NOT_EXISTS);
        }
        // 父菜单必须是目录或者菜单类型
        if (!MenuTypeEnum.DIR.getType().equals(menu.getType())
            && !MenuTypeEnum.MENU.getType().equals(menu.getType())) {
            throw ServiceExceptionUtil.exception(MENU_PARENT_NOT_DIR_OR_MENU);
        }
    }

    /**
     * 校验菜单是否合法
     *
     * 1. 校验相同父菜单编号下，是否存在相同的菜单名
     *
     * @param name 菜单名字
     * @param parentId 父菜单编号
     * @param id 菜单编号
     */
    private void checkResource(Long parentId, String name, Long id) {
        SysMenuDO menu = menuMapper.selectByParentIdAndName(parentId, name);
        if (menu == null) {
            return;
        }
        // 如果 menuId 为空，说明不用比较是否为相同 menuId 的菜单
        if (id == null) {
            throw ServiceExceptionUtil.exception(MENU_NAME_DUPLICATE);
        }
        if (!menu.getId().equals(id)) {
            throw ServiceExceptionUtil.exception(MENU_NAME_DUPLICATE);
        }
    }

    /**
     * 初始化菜单的通用属性。
     *
     * 例如说，只有目录或者菜单类型的菜单，才设置 icon
     *
     * @param menu 菜单
     */
    private void initMenuProperty(SysMenuDO menu) {
        // 菜单为按钮类型时，无需 component、icon、path 属性，进行置空
        if (MenuTypeEnum.BUTTON.getType().equals(menu.getType())) {
            menu.setComponent("");
            menu.setIcon("");
            menu.setPath("");
        }
    }

}
