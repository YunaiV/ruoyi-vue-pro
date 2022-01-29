package cn.iocoder.yudao.module.system.service.permission;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.menu.MenuCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.menu.MenuListReqVO;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.menu.MenuUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.SysMenuMapper;
import cn.iocoder.yudao.module.system.enums.permission.MenuTypeEnum;
import cn.iocoder.yudao.module.system.mq.producer.permission.MenuProducer;
import cn.iocoder.yudao.framework.common.util.spring.SpringAopUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.system.test.BaseDbUnitTest;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@Import(MenuServiceImpl.class)
public class MenuServiceTest extends BaseDbUnitTest {

    @Resource
    private MenuServiceImpl sysMenuService;

    @MockBean
    private PermissionService sysPermissionService;

    @MockBean
    private MenuProducer sysMenuProducer;

    @Resource
    private SysMenuMapper menuMapper;

    @Test
    public void testInitLocalCache_success() throws Exception {
        MenuDO menuDO1 = createMenuDO(MenuTypeEnum.MENU, "xxxx", 0L);
        menuMapper.insert(menuDO1);
        MenuDO menuDO2 = createMenuDO(MenuTypeEnum.MENU, "xxxx", 0L);
        menuMapper.insert(menuDO2);

        // 调用
        sysMenuService.initLocalCache();

        // 获取代理对象
        MenuServiceImpl target = (MenuServiceImpl) SpringAopUtils.getTarget(sysMenuService);

        Map<Long, MenuDO> menuCache =
                (Map<Long, MenuDO>) BeanUtil.getFieldValue(target, "menuCache");
        Assert.isTrue(menuCache.size() == 2);
        assertPojoEquals(menuDO1, menuCache.get(menuDO1.getId()));
        assertPojoEquals(menuDO2, menuCache.get(menuDO2.getId()));

        Multimap<String, MenuDO> permissionMenuCache =
                (Multimap<String, MenuDO>) BeanUtil.getFieldValue(target, "permissionMenuCache");
        Assert.isTrue(permissionMenuCache.size() == 2);
        assertPojoEquals(menuDO1, permissionMenuCache.get(menuDO1.getPermission()));
        assertPojoEquals(menuDO2, permissionMenuCache.get(menuDO2.getPermission()));

        Date maxUpdateTime = (Date) BeanUtil.getFieldValue(target, "maxUpdateTime");
        assertEquals(ObjectUtils.max(menuDO1.getUpdateTime(), menuDO2.getUpdateTime()), maxUpdateTime);
    }

    @Test
    public void testCreateMenu_success() {
        //构造父目录
        MenuDO menuDO = createMenuDO(MenuTypeEnum.MENU, "parent", 0L);
        menuMapper.insert(menuDO);
        Long parentId = menuDO.getId();

        //调用
        MenuCreateReqVO vo = randomPojo(MenuCreateReqVO.class, o -> {
            o.setParentId(parentId);
            o.setName("testSonName");
            o.setType(MenuTypeEnum.MENU.getType());
            o.setStatus(randomCommonStatus());
        });
        Long menuId = sysMenuService.createMenu(vo);

        //断言
        assertNotNull(menuId);
        // 校验记录的属性是否正确
        MenuDO ret = menuMapper.selectById(menuId);
        assertPojoEquals(vo, ret);
        // 校验调用
        verify(sysMenuProducer).sendMenuRefreshMessage();
    }

    @Test
    public void testUpdateMenu_success() {
        //构造父子目录
        MenuDO sonMenuDO = initParentAndSonMenuDO();
        Long sonId = sonMenuDO.getId();
        Long parentId = sonMenuDO.getParentId();

        //调用
        MenuUpdateReqVO vo = randomPojo(MenuUpdateReqVO.class, o -> {
            o.setId(sonId);
            o.setParentId(parentId);
            o.setType(MenuTypeEnum.MENU.getType());
            o.setStatus(randomCommonStatus());
            o.setName("pppppp"); //修改名字
        });
        sysMenuService.updateMenu(vo);

        //断言
        // 校验记录的属性是否正确
        MenuDO ret = menuMapper.selectById(sonId);
        assertPojoEquals(vo, ret);
        // 校验调用
        verify(sysMenuProducer).sendMenuRefreshMessage();
    }

    @Test
    public void testUpdateMenu_sonIdNotExist() {
        Long sonId = 99999L;
        Long parentId = 10000L;

        //调用
        MenuUpdateReqVO vo = randomPojo(MenuUpdateReqVO.class, o -> {
            o.setId(sonId);
            o.setParentId(parentId);
            o.setType(MenuTypeEnum.MENU.getType());
            o.setStatus(randomCommonStatus());
        });
        //断言
        assertServiceException(() -> sysMenuService.updateMenu(vo), MENU_NOT_EXISTS);
    }

    @Test
    public void testDeleteMenu_success() {
        MenuDO sonMenuDO = initParentAndSonMenuDO();
        Long sonId = sonMenuDO.getId();

        // 调用
        sysMenuService.deleteMenu(sonId);

        // 断言
        MenuDO menuDO = menuMapper.selectById(sonId);
        assertNull(menuDO);
        verify(sysPermissionService).processMenuDeleted(sonId);
        verify(sysMenuProducer).sendMenuRefreshMessage();
    }

    @Test
    public void testDeleteMenu_menuNotExist() {
        Long sonId = 99999L;

        assertServiceException(() -> sysMenuService.deleteMenu(sonId), MENU_NOT_EXISTS);
    }

    @Test
    public void testDeleteMenu_existChildren() {
        MenuDO sonMenu = initParentAndSonMenuDO();
        Long parentId = sonMenu.getParentId();

        assertServiceException(() -> sysMenuService.deleteMenu(parentId), MENU_EXISTS_CHILDREN);
    }

    @Test
    public void testGetMenus_success() {
        Map<Long, MenuDO> idMenuMap = new HashMap<>();
        MenuDO menuDO = createMenuDO(MenuTypeEnum.MENU, "parent", 0L);
        menuMapper.insert(menuDO);
        idMenuMap.put(menuDO.getId(), menuDO);

        MenuDO sonMenu = createMenuDO(MenuTypeEnum.MENU, "son", menuDO.getId());
        menuMapper.insert(sonMenu);
        idMenuMap.put(sonMenu.getId(), sonMenu);

        // 调用
        List<MenuDO> menuDOS = sysMenuService.getMenus();

        // 断言
        assertEquals(menuDOS.size(), idMenuMap.size());
        menuDOS.forEach(m -> assertPojoEquals(idMenuMap.get(m.getId()), m));
    }

    @Test
    public void testGetMenusReqVo_success() {
        Map<Long, MenuDO> idMenuMap = new HashMap<>();
        // 用于验证可以模糊搜索名称包含"name"，状态为1的menu
        MenuDO menu = createMenuDO(MenuTypeEnum.MENU, "name2", 0L, 1);
        menuMapper.insert(menu);
        idMenuMap.put(menu.getId(), menu);

        menu = createMenuDO(MenuTypeEnum.MENU, "11name111", 0L, 1);
        menuMapper.insert(menu);
        idMenuMap.put(menu.getId(), menu);

        menu = createMenuDO(MenuTypeEnum.MENU, "name", 0L, 1);
        menuMapper.insert(menu);
        idMenuMap.put(menu.getId(), menu);

        // 以下是不符合搜索条件的的menu
        menu = createMenuDO(MenuTypeEnum.MENU, "xxxxxx", 0L, 1);
        menuMapper.insert(menu);
        menu = createMenuDO(MenuTypeEnum.MENU, "name", 0L, 2);
        menuMapper.insert(menu);

        // 调用
        MenuListReqVO reqVO = new MenuListReqVO();
        reqVO.setStatus(1);
        reqVO.setName("name");
        List<MenuDO> menuDOS = sysMenuService.getMenus(reqVO);

        // 断言
        assertEquals(menuDOS.size(), idMenuMap.size());
        menuDOS.forEach(m -> assertPojoEquals(idMenuMap.get(m.getId()), m));
    }

    @Test
    public void testListMenusFromCache_success() throws Exception {
        Map<Long, MenuDO> mockCacheMap = new HashMap<>();
        // 获取代理对象
        MenuServiceImpl target = (MenuServiceImpl) SpringAopUtils.getTarget(sysMenuService);
        BeanUtil.setFieldValue(target, "menuCache", mockCacheMap);

        Map<Long, MenuDO> idMenuMap = new HashMap<>();
        // 用于验证搜索类型为MENU,状态为1的menu
        MenuDO menuDO = createMenuDO(1L, MenuTypeEnum.MENU, "name", 0L, 1);
        mockCacheMap.put(menuDO.getId(), menuDO);
        idMenuMap.put(menuDO.getId(), menuDO);

        menuDO = createMenuDO(2L, MenuTypeEnum.MENU, "name", 0L, 1);
        mockCacheMap.put(menuDO.getId(), menuDO);
        idMenuMap.put(menuDO.getId(), menuDO);

        // 以下是不符合搜索条件的menu
        menuDO = createMenuDO(3L, MenuTypeEnum.BUTTON, "name", 0L, 1);
        mockCacheMap.put(menuDO.getId(), menuDO);
        menuDO = createMenuDO(4L, MenuTypeEnum.MENU, "name", 0L, 2);
        mockCacheMap.put(menuDO.getId(), menuDO);

        List<MenuDO> menuDOS = sysMenuService.listMenusFromCache(Collections.singletonList(MenuTypeEnum.MENU.getType()),
                Collections.singletonList(CommonStatusEnum.DISABLE.getStatus()));
        assertEquals(menuDOS.size(), idMenuMap.size());
        menuDOS.forEach(m -> assertPojoEquals(idMenuMap.get(m.getId()), m));
    }

    @Test
    public void testListMenusFromCache2_success() throws Exception {
        Map<Long, MenuDO> mockCacheMap = new HashMap<>();
        // 获取代理对象
        MenuServiceImpl target = (MenuServiceImpl) SpringAopUtils.getTarget(sysMenuService);
        BeanUtil.setFieldValue(target, "menuCache", mockCacheMap);

        Map<Long, MenuDO> idMenuMap = new HashMap<>();
        // 验证搜索id为1, 类型为MENU, 状态为1 的menu
        MenuDO menuDO = createMenuDO(1L, MenuTypeEnum.MENU, "name", 0L, 1);
        mockCacheMap.put(menuDO.getId(), menuDO);
        idMenuMap.put(menuDO.getId(), menuDO);

        // 以下是不符合搜索条件的menu
        menuDO = createMenuDO(2L, MenuTypeEnum.MENU, "name", 0L, 1);
        mockCacheMap.put(menuDO.getId(), menuDO);
        menuDO = createMenuDO(3L, MenuTypeEnum.BUTTON, "name", 0L, 1);
        mockCacheMap.put(menuDO.getId(), menuDO);
        menuDO = createMenuDO(4L, MenuTypeEnum.MENU, "name", 0L, 2);
        mockCacheMap.put(menuDO.getId(), menuDO);

        List<MenuDO> menuDOS = sysMenuService.listMenusFromCache(Collections.singletonList(1L),
                Collections.singletonList(MenuTypeEnum.MENU.getType()), Collections.singletonList(1));
        assertEquals(menuDOS.size(), idMenuMap.size());
        menuDOS.forEach(menu -> assertPojoEquals(idMenuMap.get(menu.getId()), menu));
    }

    @Test
    public void testCheckParentResource_success() {
        MenuDO menuDO = createMenuDO(MenuTypeEnum.MENU, "parent", 0L);
        menuMapper.insert(menuDO);
        Long parentId = menuDO.getId();

        sysMenuService.checkParentResource(parentId, null);
    }

    @Test
    public void testCheckParentResource_canNotSetSelfToBeParent() {
        assertServiceException(() -> sysMenuService.checkParentResource(1L, 1L), MENU_PARENT_ERROR);
    }

    @Test
    public void testCheckParentResource_parentNotExist() {
        assertServiceException(() -> sysMenuService.checkParentResource(randomLongId(), null), MENU_PARENT_NOT_EXISTS);
    }

    @Test
    public void testCheckParentResource_parentTypeError() {
        MenuDO menuDO = createMenuDO(MenuTypeEnum.BUTTON, "parent", 0L);
        menuMapper.insert(menuDO);
        Long parentId = menuDO.getId();

        assertServiceException(() -> sysMenuService.checkParentResource(parentId, null), MENU_PARENT_NOT_DIR_OR_MENU);
    }

    @Test
    public void testCheckResource_success() {
        MenuDO sonMenu = initParentAndSonMenuDO();
        Long parentId = sonMenu.getParentId();

        Long otherSonMenuId = randomLongId();
        String otherSonMenuName = randomString();

        sysMenuService.checkResource(parentId, otherSonMenuName, otherSonMenuId);
    }

    @Test
    public void testCheckResource_sonMenuNameDuplicate(){
        MenuDO sonMenu=initParentAndSonMenuDO();
        Long parentId=sonMenu.getParentId();

        Long otherSonMenuId=randomLongId();
        String otherSonMenuName=sonMenu.getName(); //相同名称

        assertServiceException(() -> sysMenuService.checkResource(parentId, otherSonMenuName, otherSonMenuId), MENU_NAME_DUPLICATE);
    }

    /**
     * 构造父子目录，返回子目录
     *
     * @return
     */
    private MenuDO initParentAndSonMenuDO() {
        //构造父子目录
        MenuDO menuDO = createMenuDO(MenuTypeEnum.MENU, "parent", 0L);
        menuMapper.insert(menuDO);
        Long parentId = menuDO.getId();

        MenuDO sonMenuDO = createMenuDO(MenuTypeEnum.MENU, "testSonName", parentId);
        menuMapper.insert(sonMenuDO);
        return sonMenuDO;
    }

    private MenuDO createMenuDO(MenuTypeEnum typeEnum, String menuName, Long parentId) {
        return createMenuDO(typeEnum, menuName, parentId, randomCommonStatus());
    }

    private MenuDO createMenuDO(MenuTypeEnum typeEnum, String menuName, Long parentId, Integer status) {
        return createMenuDO(null, typeEnum, menuName, parentId, status);
    }

    private MenuDO createMenuDO(Long id, MenuTypeEnum typeEnum, String menuName, Long parentId, Integer status) {
        return randomPojo(MenuDO.class, o -> {
            o.setId(id);
            o.setParentId(parentId);
            o.setType(typeEnum.getType());
            o.setStatus(status);
            o.setName(menuName);
        });
    }

}
