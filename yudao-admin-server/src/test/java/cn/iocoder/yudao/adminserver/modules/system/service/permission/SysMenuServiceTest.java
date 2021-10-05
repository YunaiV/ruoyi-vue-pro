package cn.iocoder.yudao.adminserver.modules.system.service.permission;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.adminserver.BaseDbUnitTest;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.adminserver.modules.system.controller.permission.vo.menu.SysMenuCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.permission.vo.menu.SysMenuListReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.permission.vo.menu.SysMenuUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.permission.SysMenuDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.mysql.permission.SysMenuMapper;
import cn.iocoder.yudao.adminserver.modules.system.enums.permission.MenuTypeEnum;
import cn.iocoder.yudao.adminserver.modules.system.mq.producer.permission.SysMenuProducer;
import cn.iocoder.yudao.adminserver.modules.system.service.permission.impl.SysMenuServiceImpl;
import cn.iocoder.yudao.framework.common.util.spring.SpringAopUtils;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@Import(SysMenuServiceImpl.class)
public class SysMenuServiceTest extends BaseDbUnitTest {

    @Resource
    private SysMenuServiceImpl sysMenuService;

    @MockBean
    private SysPermissionService sysPermissionService;

    @MockBean
    private SysMenuProducer sysMenuProducer;

    @Resource
    private SysMenuMapper menuMapper;

    @Test
    public void testInitLocalCache_success() throws Exception {
        SysMenuDO menuDO1 = createMenuDO(MenuTypeEnum.MENU, "xxxx", 0L);
        menuMapper.insert(menuDO1);
        SysMenuDO menuDO2 = createMenuDO(MenuTypeEnum.MENU, "xxxx", 0L);
        menuMapper.insert(menuDO2);

        // 调用
        sysMenuService.initLocalCache();

        // 获取代理对象
        SysMenuServiceImpl target = (SysMenuServiceImpl) SpringAopUtils.getTarget(sysMenuService);

        Map<Long, SysMenuDO> menuCache =
                (Map<Long, SysMenuDO>) BeanUtil.getFieldValue(target, "menuCache");
        Assert.isTrue(menuCache.size() == 2);
        assertPojoEquals(menuDO1, menuCache.get(menuDO1.getId()));
        assertPojoEquals(menuDO2, menuCache.get(menuDO2.getId()));

        Multimap<String, SysMenuDO> permissionMenuCache =
                (Multimap<String, SysMenuDO>) BeanUtil.getFieldValue(target, "permissionMenuCache");
        Assert.isTrue(permissionMenuCache.size() == 2);
        assertPojoEquals(menuDO1, permissionMenuCache.get(menuDO1.getPermission()));
        assertPojoEquals(menuDO2, permissionMenuCache.get(menuDO2.getPermission()));

        Date maxUpdateTime = (Date) BeanUtil.getFieldValue(target, "maxUpdateTime");
        assertEquals(ObjectUtils.max(menuDO1.getUpdateTime(), menuDO2.getUpdateTime()), maxUpdateTime);
    }

    @Test
    public void testCreateMenu_success() {
        //构造父目录
        SysMenuDO menuDO = createMenuDO(MenuTypeEnum.MENU, "parent", 0L);
        menuMapper.insert(menuDO);
        Long parentId = menuDO.getId();

        //调用
        SysMenuCreateReqVO vo = randomPojo(SysMenuCreateReqVO.class, o -> {
            o.setParentId(parentId);
            o.setName("testSonName");
            o.setType(MenuTypeEnum.MENU.getType());
            o.setStatus(RandomUtils.randomCommonStatus());
        });
        Long menuId = sysMenuService.createMenu(vo);

        //断言
        Assertions.assertNotNull(menuId);
        // 校验记录的属性是否正确
        SysMenuDO ret = menuMapper.selectById(menuId);
        assertPojoEquals(vo, ret);
        // 校验调用
        verify(sysMenuProducer).sendMenuRefreshMessage();
    }

    @Test
    public void testUpdateMenu_success() {
        //构造父子目录
        SysMenuDO sonMenuDO = initParentAndSonMenuDO();
        Long sonId = sonMenuDO.getId();
        Long parentId = sonMenuDO.getParentId();

        //调用
        SysMenuUpdateReqVO vo = randomPojo(SysMenuUpdateReqVO.class, o -> {
            o.setId(sonId);
            o.setParentId(parentId);
            o.setType(MenuTypeEnum.MENU.getType());
            o.setStatus(RandomUtils.randomCommonStatus());
            o.setName("pppppp"); //修改名字
        });
        sysMenuService.updateMenu(vo);

        //断言
        // 校验记录的属性是否正确
        SysMenuDO ret = menuMapper.selectById(sonId);
        assertPojoEquals(vo, ret);
        // 校验调用
        verify(sysMenuProducer).sendMenuRefreshMessage();
    }

    @Test
    public void testUpdateMenu_sonIdNotExist() {
        Long sonId = 99999L;
        Long parentId = 10000L;

        //调用
        SysMenuUpdateReqVO vo = randomPojo(SysMenuUpdateReqVO.class, o -> {
            o.setId(sonId);
            o.setParentId(parentId);
            o.setType(MenuTypeEnum.MENU.getType());
            o.setStatus(RandomUtils.randomCommonStatus());
        });
        //断言
        assertServiceException(() -> sysMenuService.updateMenu(vo), MENU_NOT_EXISTS);
    }

    @Test
    public void testDeleteMenu_success() {
        SysMenuDO sonMenuDO = initParentAndSonMenuDO();
        Long sonId = sonMenuDO.getId();

        //调用
        sysMenuService.deleteMenu(sonId);

        //断言
        SysMenuDO menuDO = menuMapper.selectById(sonId);
        Assert.isNull(menuDO);
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
        SysMenuDO sonMenu = initParentAndSonMenuDO();
        Long parentId = sonMenu.getParentId();

        assertServiceException(() -> sysMenuService.deleteMenu(parentId), MENU_EXISTS_CHILDREN);
    }

    @Test
    public void testGetMenus_success() {
        Map<Long, SysMenuDO> idMenuMap = new HashMap<>();
        SysMenuDO menuDO = createMenuDO(MenuTypeEnum.MENU, "parent", 0L);
        menuMapper.insert(menuDO);
        idMenuMap.put(menuDO.getId(), menuDO);

        SysMenuDO sonMenu = createMenuDO(MenuTypeEnum.MENU, "son", menuDO.getId());
        menuMapper.insert(sonMenu);
        idMenuMap.put(sonMenu.getId(), sonMenu);

        //调用
        List<SysMenuDO> menuDOS = sysMenuService.getMenus();

        //断言
        Assert.isTrue(menuDOS.size() == idMenuMap.size());
        menuDOS.forEach(m -> assertPojoEquals(idMenuMap.get(m.getId()), m));
    }

    @Test
    public void testGetMenusReqVo_success() {
        Map<Long, SysMenuDO> idMenuMap = new HashMap<>();
        //用于验证可以模糊搜索名称包含"name"，状态为1的menu
        SysMenuDO menu = createMenuDO(MenuTypeEnum.MENU, "name2", 0L, 1);
        menuMapper.insert(menu);
        idMenuMap.put(menu.getId(), menu);

        menu = createMenuDO(MenuTypeEnum.MENU, "11name111", 0L, 1);
        menuMapper.insert(menu);
        idMenuMap.put(menu.getId(), menu);

        menu = createMenuDO(MenuTypeEnum.MENU, "name", 0L, 1);
        menuMapper.insert(menu);
        idMenuMap.put(menu.getId(), menu);

        //以下是不符合搜索条件的的menu
        menu = createMenuDO(MenuTypeEnum.MENU, "xxxxxx", 0L, 1);
        menuMapper.insert(menu);
        menu = createMenuDO(MenuTypeEnum.MENU, "name", 0L, 2);
        menuMapper.insert(menu);

        //调用
        SysMenuListReqVO reqVO = new SysMenuListReqVO();
        reqVO.setStatus(1);
        reqVO.setName("name");
        List<SysMenuDO> menuDOS = sysMenuService.getMenus(reqVO);

        //断言
        Assert.isTrue(menuDOS.size() == idMenuMap.size());
        menuDOS.forEach(m -> assertPojoEquals(idMenuMap.get(m.getId()), m));
    }

    @Test
    public void testListMenusFromCache_success() throws Exception {
        Map<Long, SysMenuDO> mockCacheMap = new HashMap<>();
        //获取代理对象
        SysMenuServiceImpl target = (SysMenuServiceImpl) SpringAopUtils.getTarget(sysMenuService);
        BeanUtil.setFieldValue(target, "menuCache", mockCacheMap);

        Map<Long, SysMenuDO> idMenuMap = new HashMap<>();
        //用于验证搜索类型为MENU,状态为1的menu
        SysMenuDO menuDO = createMenuDO(1L, MenuTypeEnum.MENU, "name", 0L, 1);
        mockCacheMap.put(menuDO.getId(), menuDO);
        idMenuMap.put(menuDO.getId(), menuDO);

        menuDO = createMenuDO(2L, MenuTypeEnum.MENU, "name", 0L, 1);
        mockCacheMap.put(menuDO.getId(), menuDO);
        idMenuMap.put(menuDO.getId(), menuDO);

        //以下是不符合搜索条件的menu
        menuDO = createMenuDO(3L, MenuTypeEnum.BUTTON, "name", 0L, 1);
        mockCacheMap.put(menuDO.getId(), menuDO);
        menuDO = createMenuDO(4L, MenuTypeEnum.MENU, "name", 0L, 2);
        mockCacheMap.put(menuDO.getId(), menuDO);

        List<SysMenuDO> menuDOS = sysMenuService.listMenusFromCache(Collections.singletonList(MenuTypeEnum.MENU.getType()),
                Collections.singletonList(CommonStatusEnum.DISABLE.getStatus()));
        Assert.isTrue(menuDOS.size() == idMenuMap.size());
        menuDOS.forEach(m -> assertPojoEquals(idMenuMap.get(m.getId()), m));
    }

    @Test
    public void testListMenusFromCache2_success() throws Exception {
        Map<Long, SysMenuDO> mockCacheMap = new HashMap<>();
        //获取代理对象
        SysMenuServiceImpl target = (SysMenuServiceImpl) SpringAopUtils.getTarget(sysMenuService);
        BeanUtil.setFieldValue(target, "menuCache", mockCacheMap);

        Map<Long, SysMenuDO> idMenuMap = new HashMap<>();
        //验证搜索id为1, 类型为MENU, 状态为1 的menu
        SysMenuDO menuDO = createMenuDO(1L, MenuTypeEnum.MENU, "name", 0L, 1);
        mockCacheMap.put(menuDO.getId(), menuDO);
        idMenuMap.put(menuDO.getId(), menuDO);

        //以下是不符合搜索条件的menu
        menuDO = createMenuDO(2L, MenuTypeEnum.MENU, "name", 0L, 1);
        mockCacheMap.put(menuDO.getId(), menuDO);
        menuDO = createMenuDO(3L, MenuTypeEnum.BUTTON, "name", 0L, 1);
        mockCacheMap.put(menuDO.getId(), menuDO);
        menuDO = createMenuDO(4L, MenuTypeEnum.MENU, "name", 0L, 2);
        mockCacheMap.put(menuDO.getId(), menuDO);

        List<SysMenuDO> menuDOS = sysMenuService.listMenusFromCache(Collections.singletonList(1L),
                Collections.singletonList(MenuTypeEnum.MENU.getType()), Collections.singletonList(1));
        Assert.isTrue(menuDOS.size() == idMenuMap.size());
        menuDOS.forEach(menu -> assertPojoEquals(idMenuMap.get(menu.getId()), menu));
    }

    @Test
    public void testCheckParentResource_success() {
        SysMenuDO menuDO = createMenuDO(MenuTypeEnum.MENU, "parent", 0L);
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
        SysMenuDO menuDO = createMenuDO(MenuTypeEnum.BUTTON, "parent", 0L);
        menuMapper.insert(menuDO);
        Long parentId = menuDO.getId();

        assertServiceException(() -> sysMenuService.checkParentResource(parentId, null), MENU_PARENT_NOT_DIR_OR_MENU);
    }

    @Test
    public void testCheckResource_success() {
        SysMenuDO sonMenu = initParentAndSonMenuDO();
        Long parentId = sonMenu.getParentId();

        Long otherSonMenuId = randomLongId();
        String otherSonMenuName = randomString();

        sysMenuService.checkResource(parentId, otherSonMenuName, otherSonMenuId);
    }

    @Test
    public void testCheckResource_sonMenuNameDuplicate(){
        SysMenuDO sonMenu=initParentAndSonMenuDO();
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
    private SysMenuDO initParentAndSonMenuDO() {
        //构造父子目录
        SysMenuDO menuDO = createMenuDO(MenuTypeEnum.MENU, "parent", 0L);
        menuMapper.insert(menuDO);
        Long parentId = menuDO.getId();

        SysMenuDO sonMenuDO = createMenuDO(MenuTypeEnum.MENU, "testSonName", parentId);
        menuMapper.insert(sonMenuDO);
        return sonMenuDO;
    }

    private SysMenuDO createMenuDO(MenuTypeEnum typeEnum, String menuName, Long parentId) {
        return createMenuDO(typeEnum, menuName, parentId, RandomUtils.randomCommonStatus());
    }

    private SysMenuDO createMenuDO(MenuTypeEnum typeEnum, String menuName, Long parentId, Integer status) {
        return createMenuDO(null, typeEnum, menuName, parentId, status);
    }

    private SysMenuDO createMenuDO(Long id, MenuTypeEnum typeEnum, String menuName, Long parentId, Integer status) {
        return randomPojo(SysMenuDO.class, o -> {
            o.setId(id);
            o.setParentId(parentId);
            o.setType(typeEnum.getType());
            o.setStatus(status);
            o.setName(menuName);
        });
    }

}
