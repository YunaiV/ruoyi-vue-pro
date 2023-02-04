package cn.iocoder.yudao.module.system.service.permission;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.menu.MenuCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.menu.MenuListReqVO;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.menu.MenuUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.MenuMapper;
import cn.iocoder.yudao.module.system.enums.permission.MenuTypeEnum;
import cn.iocoder.yudao.module.system.mq.producer.permission.MenuProducer;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO.ID_ROOT;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@Import(MenuServiceImpl.class)
public class MenuServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MenuServiceImpl menuService;

    @Resource
    private MenuMapper menuMapper;

    @MockBean
    private PermissionService permissionService;
    @MockBean
    private MenuProducer menuProducer;
    @MockBean
    private TenantService tenantService;

    @Test
    public void testInitLocalCache_success() {
        MenuDO menuDO1 = randomPojo(MenuDO.class);
        menuMapper.insert(menuDO1);
        MenuDO menuDO2 = randomPojo(MenuDO.class);
        menuMapper.insert(menuDO2);

        // 调用
        menuService.initLocalCache();
        // 校验 menuCache 缓存
        Map<Long, MenuDO> menuCache = menuService.getMenuCache();
        assertEquals(2, menuCache.size());
        assertPojoEquals(menuDO1, menuCache.get(menuDO1.getId()));
        assertPojoEquals(menuDO2, menuCache.get(menuDO2.getId()));
        // 校验 permissionMenuCache 缓存
        Multimap<String, MenuDO> permissionMenuCache = menuService.getPermissionMenuCache();
        assertEquals(2, permissionMenuCache.size());
        assertPojoEquals(menuDO1, permissionMenuCache.get(menuDO1.getPermission()));
        assertPojoEquals(menuDO2, permissionMenuCache.get(menuDO2.getPermission()));
    }

    @Test
    public void testCreateMenu_success() {
        // mock 数据（构造父菜单）
        MenuDO menuDO = createMenuDO(MenuTypeEnum.MENU,
                "parent", 0L);
        menuMapper.insert(menuDO);
        Long parentId = menuDO.getId();
        // 准备参数
        MenuCreateReqVO reqVO = randomPojo(MenuCreateReqVO.class, o -> {
            o.setParentId(parentId);
            o.setName("testSonName");
            o.setType(MenuTypeEnum.MENU.getType());
        });
        Long menuId = menuService.createMenu(reqVO);

        // 校验记录的属性是否正确
        MenuDO dbMenu = menuMapper.selectById(menuId);
        assertPojoEquals(reqVO, dbMenu);
        // 校验调用
        verify(menuProducer).sendMenuRefreshMessage();
    }

    @Test
    public void testUpdateMenu_success() {
        // mock 数据（构造父子菜单）
        MenuDO sonMenuDO = initParentAndSonMenu();
        Long sonId = sonMenuDO.getId();
        // 准备参数
        MenuUpdateReqVO reqVO = randomPojo(MenuUpdateReqVO.class, o -> {
            o.setId(sonId);
            o.setName("testSonName"); // 修改名字
            o.setParentId(sonMenuDO.getParentId());
            o.setType(MenuTypeEnum.MENU.getType());
        });

        // 调用
        menuService.updateMenu(reqVO);
        // 校验记录的属性是否正确
        MenuDO dbMenu = menuMapper.selectById(sonId);
        assertPojoEquals(reqVO, dbMenu);
        // 校验调用
        verify(menuProducer).sendMenuRefreshMessage();
    }

    @Test
    public void testUpdateMenu_sonIdNotExist() {
        // 准备参数
        MenuUpdateReqVO reqVO = randomPojo(MenuUpdateReqVO.class);
        // 调用，并断言异常
        assertServiceException(() -> menuService.updateMenu(reqVO), MENU_NOT_EXISTS);
    }

    @Test
    public void testDeleteMenu_success() {
        // mock 数据
        MenuDO menuDO = randomPojo(MenuDO.class);
        menuMapper.insert(menuDO);
        // 准备参数
        Long id = menuDO.getId();

        // 调用
        menuService.deleteMenu(id);
        // 断言
        MenuDO dbMenuDO = menuMapper.selectById(id);
        assertNull(dbMenuDO);
        verify(permissionService).processMenuDeleted(id);
        verify(menuProducer).sendMenuRefreshMessage();
    }

    @Test
    public void testDeleteMenu_menuNotExist() {
        assertServiceException(() -> menuService.deleteMenu(randomLongId()),
                MENU_NOT_EXISTS);
    }

    @Test
    public void testDeleteMenu_existChildren() {
        // mock 数据（构造父子菜单）
        MenuDO sonMenu = initParentAndSonMenu();
        // 准备参数
        Long parentId = sonMenu.getParentId();

        // 调用并断言异常
        assertServiceException(() -> menuService.deleteMenu(parentId), MENU_EXISTS_CHILDREN);
    }

    @Test
    public void testGetMenuList_all() {
        // mock 数据
        MenuDO menu100 = randomPojo(MenuDO.class);
        menuMapper.insert(menu100);
        MenuDO menu101 = randomPojo(MenuDO.class);
        menuMapper.insert(menu101);
        // 准备参数

        // 调用
        List<MenuDO> list = menuService.getMenuList();
        // 断言
        assertEquals(2, list.size());
        assertPojoEquals(menu100, list.get(0));
        assertPojoEquals(menu101, list.get(1));
    }

    @Test
    public void testGetMenuList() {
        // mock 数据
        MenuDO menuDO = randomPojo(MenuDO.class, o -> o.setName("芋艿").setStatus(CommonStatusEnum.ENABLE.getStatus()));
        menuMapper.insert(menuDO);
        // 测试 status 不匹配
        menuMapper.insert(cloneIgnoreId(menuDO, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 测试 name 不匹配
        menuMapper.insert(cloneIgnoreId(menuDO, o -> o.setName("艿")));
        // 准备参数
        MenuListReqVO reqVO = new MenuListReqVO().setName("芋").setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        List<MenuDO> result = menuService.getMenuList(reqVO);
        // 断言
        assertEquals(1, result.size());
        assertPojoEquals(menuDO, result.get(0));
    }

    @Test
    public void testGetMenuListByTenant() {
        // mock 数据
        MenuDO menu100 = randomPojo(MenuDO.class, o -> o.setId(100L).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        menuMapper.insert(menu100);
        MenuDO menu101 = randomPojo(MenuDO.class, o -> o.setId(101L).setStatus(CommonStatusEnum.DISABLE.getStatus()));
        menuMapper.insert(menu101);
        MenuDO menu102 = randomPojo(MenuDO.class, o -> o.setId(102L).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        menuMapper.insert(menu102);
        // mock 过滤菜单
        Set<Long> menuIds = asSet(100L, 101L);
        doNothing().when(tenantService).handleTenantMenu(argThat(handler -> {
            handler.handle(menuIds);
            return true;
        }));
        // 准备参数
        MenuListReqVO reqVO = new MenuListReqVO().setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        List<MenuDO> result = menuService.getMenuListByTenant(reqVO);
        // 断言
        assertEquals(1, result.size());
        assertPojoEquals(menu100, result.get(0));
    }

    @Test
    public void testListMenusFromCache_withoutId() {
        // mock 缓存
        Map<Long, MenuDO> menuCache = new HashMap<>();
        // 可被匹配
        MenuDO menuDO = randomPojo(MenuDO.class, o -> o.setId(1L)
                .setType(MenuTypeEnum.MENU.getType()).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        menuCache.put(menuDO.getId(), menuDO);
        // 测试 type 不匹配
        menuCache.put(3L, randomPojo(MenuDO.class, o -> o.setId(3L)
                .setType(MenuTypeEnum.BUTTON.getType()).setStatus(CommonStatusEnum.ENABLE.getStatus())));
        // 测试 status 不匹配
        menuCache.put(4L, randomPojo(MenuDO.class, o -> o.setId(4L)
                .setType(MenuTypeEnum.MENU.getType()).setStatus(CommonStatusEnum.DISABLE.getStatus())));
        menuService.setMenuCache(menuCache);
        // 准备参数
        Collection<Integer> menuTypes = singletonList(MenuTypeEnum.MENU.getType());
        Collection<Integer> menusStatuses = singletonList(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        List<MenuDO> list = menuService.getMenuListFromCache(menuTypes, menusStatuses);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(menuDO, list.get(0));
    }

    @Test
    public void testListMenusFromCache_withId() {
        // mock 缓存
        Map<Long, MenuDO> menuCache = new HashMap<>();
        // 可被匹配
        MenuDO menuDO = randomPojo(MenuDO.class, o -> o.setId(1L)
                .setType(MenuTypeEnum.MENU.getType()).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        menuCache.put(menuDO.getId(), menuDO);
        // 测试 id 不匹配
        menuCache.put(2L, randomPojo(MenuDO.class, o -> o.setId(2L)
                .setType(MenuTypeEnum.MENU.getType()).setStatus(CommonStatusEnum.ENABLE.getStatus())));
        // 测试 type 不匹配
        menuCache.put(3L, randomPojo(MenuDO.class, o -> o.setId(3L)
                .setType(MenuTypeEnum.BUTTON.getType()).setStatus(CommonStatusEnum.ENABLE.getStatus())));
        // 测试 status 不匹配
        menuCache.put(4L, randomPojo(MenuDO.class, o -> o.setId(4L)
                .setType(MenuTypeEnum.MENU.getType()).setStatus(CommonStatusEnum.DISABLE.getStatus())));
        menuService.setMenuCache(menuCache);
        // 准备参数
        Collection<Long> menuIds = asList(1L, 3L, 4L);
        Collection<Integer> menuTypes = singletonList(MenuTypeEnum.MENU.getType());
        Collection<Integer> menusStatuses = singletonList(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        List<MenuDO> list = menuService.getMenuListFromCache(menuIds, menuTypes, menusStatuses);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(menuDO, list.get(0));
    }

    @Test
    public void testGetMenuListByPermissionFromCache() {
        // mock 缓存
        Multimap<String, MenuDO> permissionMenuCache = LinkedListMultimap.create();
        // 可被匹配
        MenuDO menuDO01 = randomPojo(MenuDO.class, o -> o.setId(1L).setPermission("123"));
        permissionMenuCache.put(menuDO01.getPermission(), menuDO01);
        MenuDO menuDO02 = randomPojo(MenuDO.class, o -> o.setId(2L).setPermission("123"));
        permissionMenuCache.put(menuDO02.getPermission(), menuDO02);
        // 不可匹配
        permissionMenuCache.put("456", randomPojo(MenuDO.class, o -> o.setId(3L).setPermission("456")));
        menuService.setPermissionMenuCache(permissionMenuCache);
        // 准备参数
        String permission = "123";

        // 调用
        List<MenuDO> list = menuService.getMenuListByPermissionFromCache(permission);
        // 断言
        assertEquals(2, list.size());
        assertPojoEquals(menuDO01, list.get(0));
        assertPojoEquals(menuDO02, list.get(1));
    }

    @Test
    public void testGetMenu() {
        // mock 数据
        MenuDO menu = randomPojo(MenuDO.class);
        menuMapper.insert(menu);
        // 准备参数
        Long id = menu.getId();

        // 调用
        MenuDO dbMenu = menuService.getMenu(id);
        // 断言
        assertPojoEquals(menu, dbMenu);
    }

    @Test
    public void testValidateParentMenu_success() {
        // mock 数据
        MenuDO menuDO = createMenuDO(MenuTypeEnum.MENU, "parent", 0L);
        menuMapper.insert(menuDO);
        // 准备参数
        Long parentId = menuDO.getId();

        // 调用，无需断言
        menuService.validateParentMenu(parentId, null);
    }

    @Test
    public void testValidateParentMenu_canNotSetSelfToBeParent() {
        // 调用，并断言异常
        assertServiceException(() -> menuService.validateParentMenu(1L, 1L),
                MENU_PARENT_ERROR);
    }

    @Test
    public void testValidateParentMenu_parentNotExist() {
        // 调用，并断言异常
        assertServiceException(() -> menuService.validateParentMenu(randomLongId(), null),
                MENU_PARENT_NOT_EXISTS);
    }

    @Test
    public void testValidateParentMenu_parentTypeError() {
        // mock 数据
        MenuDO menuDO = createMenuDO(MenuTypeEnum.BUTTON, "parent", 0L);
        menuMapper.insert(menuDO);
        // 准备参数
        Long parentId = menuDO.getId();

        // 调用，并断言异常
        assertServiceException(() -> menuService.validateParentMenu(parentId, null),
                MENU_PARENT_NOT_DIR_OR_MENU);
    }

    @Test
    public void testValidateMenu_success() {
        // mock 父子菜单
        MenuDO sonMenu = initParentAndSonMenu();
        // 准备参数
        Long parentId = sonMenu.getParentId();
        Long otherSonMenuId = randomLongId();
        String otherSonMenuName = randomString();

        // 调用，无需断言
        menuService.validateMenu(parentId, otherSonMenuName, otherSonMenuId);
    }

    @Test
    public void testValidateMenu_sonMenuNameDuplicate() {
        // mock 父子菜单
        MenuDO sonMenu = initParentAndSonMenu();
        // 准备参数
        Long parentId = sonMenu.getParentId();
        Long otherSonMenuId = randomLongId();
        String otherSonMenuName = sonMenu.getName(); //相同名称

        // 调用，并断言异常
        assertServiceException(() -> menuService.validateMenu(parentId, otherSonMenuName, otherSonMenuId),
                MENU_NAME_DUPLICATE);
    }

    // ====================== 初始化方法 ======================

    /**
     * 构造父子菜单，返回子菜单
     *
     * @return 子菜单
     */
    private MenuDO initParentAndSonMenu() {
        // 构造父子菜单
        MenuDO parentMenuDO = createMenuDO(MenuTypeEnum.MENU, "parent", ID_ROOT);
        menuMapper.insert(parentMenuDO);
        // 构建子菜单
        MenuDO sonMenuDO = createMenuDO(MenuTypeEnum.MENU, "testSonName",
                parentMenuDO.getParentId());
        menuMapper.insert(sonMenuDO);
        return sonMenuDO;
    }

    private MenuDO createMenuDO(MenuTypeEnum type, String name, Long parentId) {
        return createMenuDO(type, name, parentId, randomCommonStatus());
    }

    private MenuDO createMenuDO(MenuTypeEnum type, String name, Long parentId, Integer status) {
        return randomPojo(MenuDO.class, o -> o.setId(null).setName(name).setParentId(parentId)
                .setType(type.getType()).setStatus(status));
    }

}
