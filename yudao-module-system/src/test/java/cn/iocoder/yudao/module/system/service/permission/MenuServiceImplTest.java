package cn.iocoder.yudao.module.system.service.permission;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.menu.MenuListReqVO;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.menu.MenuSaveVO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.MenuMapper;
import cn.iocoder.yudao.module.system.enums.permission.MenuTypeEnum;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO.ID_ROOT;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@Import(MenuServiceImpl.class)
public class MenuServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MenuServiceImpl menuService;

    @MockitoSpyBean
    private MenuMapper menuMapper;

    @MockitoBean
    private PermissionService permissionService;
    @MockitoBean
    private TenantService tenantService;

    @Test
    public void testCreateMenu_success() {
        // mock 数据（构造父菜单）
        MenuDO menuDO = buildMenuDO(MenuTypeEnum.MENU,
                "parent", 0L);
        menuMapper.insert(menuDO);
        Long parentId = menuDO.getId();
        // 准备参数
        MenuSaveVO reqVO = randomPojo(MenuSaveVO.class, o -> {
            o.setParentId(parentId);
            o.setName("testSonName");
            o.setType(MenuTypeEnum.MENU.getType());
        }).setId(null); // 防止 id 被赋值
        Long menuId = menuService.createMenu(reqVO);

        // 校验记录的属性是否正确
        MenuDO dbMenu = menuMapper.selectById(menuId);
        assertPojoEquals(reqVO, dbMenu, "id");
    }

    @Test
    public void testUpdateMenu_success() {
        // mock 数据（构造父子菜单）
        MenuDO sonMenuDO = createParentAndSonMenu();
        Long sonId = sonMenuDO.getId();
        // 准备参数
        MenuSaveVO reqVO = randomPojo(MenuSaveVO.class, o -> {
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
    }

    @Test
    public void testUpdateMenu_sonIdNotExist() {
        // 准备参数
        MenuSaveVO reqVO = randomPojo(MenuSaveVO.class);
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
    }

    @Test
    public void testDeleteMenu_menuNotExist() {
        assertServiceException(() -> menuService.deleteMenu(randomLongId()),
                MENU_NOT_EXISTS);
    }

    @Test
    public void testDeleteMenu_existChildren() {
        // mock 数据（构造父子菜单）
        MenuDO sonMenu = createParentAndSonMenu();
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
    public void testGetMenuIdListByPermissionFromCache() {
        // mock 数据
        MenuDO menu100 = randomPojo(MenuDO.class);
        menuMapper.insert(menu100);
        MenuDO menu101 = randomPojo(MenuDO.class);
        menuMapper.insert(menu101);
        // 准备参数
        String permission = menu100.getPermission();

        // 调用
        List<Long> ids = menuService.getMenuIdListByPermissionFromCache(permission);
        // 断言
        assertEquals(1, ids.size());
        assertEquals(menu100.getId(), ids.get(0));
    }

    @Test
    public void testGetMenuList_ids() {
        // mock 数据
        MenuDO menu100 = randomPojo(MenuDO.class);
        menuMapper.insert(menu100);
        MenuDO menu101 = randomPojo(MenuDO.class);
        menuMapper.insert(menu101);
        // 准备参数
        Collection<Long> ids = Collections.singleton(menu100.getId());

        // 调用
        List<MenuDO> list = menuService.getMenuList(ids);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(menu100, list.get(0));
    }

    @Test
    public void testGetMenuList_idsEmpty() {
        // 调用，并断言
        assertTrue(menuService.getMenuList((Collection<Long>) null).isEmpty());
        assertTrue(menuService.getMenuList(Collections.<Long>emptyList()).isEmpty());
        // 断言：不查询数据库
        verify(menuMapper, never()).selectByIds(anyCollection());
    }

    @Test
    public void testGetMenuList_idsMoreThanBatchSize() {
        // mock 数据（1001 个菜单，超过 Oracle IN 的 1000 个限制）
        Set<Long> menuIds = new HashSet<>();
        for (int i = 0; i < 1001; i++) {
            MenuDO menu = buildMenuDO(MenuTypeEnum.MENU, "menu" + i, ID_ROOT);
            menuMapper.insert(menu);
            menuIds.add(menu.getId());
        }
        // mock 数据（已删除的菜单）
        MenuDO deletedMenu = buildMenuDO(MenuTypeEnum.MENU, "deleted", ID_ROOT);
        menuMapper.insert(deletedMenu);
        menuMapper.deleteById(deletedMenu.getId());
        // 准备参数（包含重复、不存在、已删除的编号）
        List<Long> ids = new ArrayList<>(menuIds);
        ids.add(ids.get(0));
        ids.add(Long.MAX_VALUE);
        ids.add(deletedMenu.getId());

        // 调用
        List<MenuDO> list = menuService.getMenuList(ids);
        // 断言：结果完整，且不重复
        assertEquals(1001, list.size());
        assertEquals(menuIds, convertSet(list, MenuDO::getId));
        // 断言：每批 IN 不超过 1000 个（Oracle 限制）
        ArgumentCaptor<Collection<Long>> batchIdsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(menuMapper, atLeastOnce()).selectByIds(batchIdsCaptor.capture());
        for (Collection<Long> batchIds : batchIdsCaptor.getAllValues()) {
            assertTrue(batchIds.size() <= 1000);
        }
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
        MenuDO menuDO = buildMenuDO(MenuTypeEnum.MENU, "parent", 0L);
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
        MenuDO menuDO = buildMenuDO(MenuTypeEnum.BUTTON, "parent", 0L);
        menuMapper.insert(menuDO);
        // 准备参数
        Long parentId = menuDO.getId();

        // 调用，并断言异常
        assertServiceException(() -> menuService.validateParentMenu(parentId, null),
                MENU_PARENT_NOT_DIR_OR_MENU);
    }

    @Test
    public void testValidateMenu_Name_success() {
        // mock 父子菜单
        MenuDO sonMenu = createParentAndSonMenu();
        // 准备参数
        Long parentId = sonMenu.getParentId();
        Long otherSonMenuId = randomLongId();
        String otherSonMenuName = randomString();

        // 调用，无需断言
        menuService.validateMenuName(parentId, otherSonMenuName, otherSonMenuId);
    }

    @Test
    public void testValidateMenu_sonMenuNameNameDuplicate() {
        // mock 父子菜单
        MenuDO sonMenu = createParentAndSonMenu();
        // 准备参数
        Long parentId = sonMenu.getParentId();
        Long otherSonMenuId = randomLongId();
        String otherSonMenuName = sonMenu.getName(); //相同名称

        // 调用，并断言异常
        assertServiceException(() -> menuService.validateMenuName(parentId, otherSonMenuName, otherSonMenuId),
                MENU_NAME_DUPLICATE);
    }

    // ====================== 初始化方法 ======================

    /**
     * 插入父子菜单，返回子菜单
     *
     * @return 子菜单
     */
    private MenuDO createParentAndSonMenu() {
        // 构造父子菜单
        MenuDO parentMenuDO = buildMenuDO(MenuTypeEnum.MENU, "parent", ID_ROOT);
        menuMapper.insert(parentMenuDO);
        // 构建子菜单
        MenuDO sonMenuDO = buildMenuDO(MenuTypeEnum.MENU, "testSonName",
                parentMenuDO.getParentId());
        menuMapper.insert(sonMenuDO);
        return sonMenuDO;
    }

    private MenuDO buildMenuDO(MenuTypeEnum type, String name, Long parentId) {
        return buildMenuDO(type, name, parentId, randomCommonStatus());
    }

    private MenuDO buildMenuDO(MenuTypeEnum type, String name, Long parentId, Integer status) {
        return randomPojo(MenuDO.class, o -> o.setId(null).setName(name).setParentId(parentId)
                .setType(type.getType()).setStatus(status));
    }

}
