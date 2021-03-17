package cn.iocoder.dashboard.modules.system.service.permission;

import cn.iocoder.dashboard.BaseDbUnitTest;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.menu.SysMenuCreateReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.permission.SysMenuDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.permission.SysMenuMapper;
import cn.iocoder.dashboard.modules.system.enums.permission.MenuTypeEnum;
import cn.iocoder.dashboard.modules.system.mq.producer.permission.SysMenuProducer;
import cn.iocoder.dashboard.modules.system.mq.producer.permission.SysPermissionProducer;
import cn.iocoder.dashboard.modules.system.service.permission.impl.SysMenuServiceImpl;
import cn.iocoder.dashboard.util.AssertUtils;
import cn.iocoder.dashboard.util.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.MENU_PARENT_NOT_DIR_OR_MENU;
import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.MENU_PARENT_NOT_EXISTS;
import static cn.iocoder.dashboard.util.RandomUtils.randomLongId;
import static cn.iocoder.dashboard.util.RandomUtils.randomPojo;
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
    public void testCreateMenu_success() {
        //构造父目录
        SysMenuDO menuDO = createParentMenuDO(MenuTypeEnum.MENU);
        menuMapper.insert(menuDO);
        Long parentId = menuDO.getId();

        //调用
        SysMenuCreateReqVO vo = createReqVO(parentId);
        Long menuId = sysMenuService.createMenu(vo);

        //断言
        Assertions.assertNotNull(menuId);
        // 校验记录的属性是否正确
        SysMenuDO ret = menuMapper.selectById(menuId);
        AssertUtils.assertPojoEquals(vo, ret);
        // 校验调用
        verify(sysMenuProducer).sendMenuRefreshMessage();
    }

    @Test
    public void testCreateMenu_checkParentExist() {
        Long parentId = RandomUtils.randomLongId();

        // 调用
        SysMenuCreateReqVO vo = createReqVO(parentId);

        // 调用, 并断言异常
        AssertUtils.assertServiceException(() -> sysMenuService.createMenu(vo), MENU_PARENT_NOT_EXISTS);
    }

    @Test
    public void testCreateMenu_checkParentIsDirOrMenu() {
        //构造父目录
        SysMenuDO menuDO = createParentMenuDO(MenuTypeEnum.BUTTON);
        menuMapper.insert(menuDO);
        Long parentId = menuDO.getId();

        // 调用
        SysMenuCreateReqVO vo = createReqVO(parentId);

        // 调用, 并断言异常
        AssertUtils.assertServiceException(() -> sysMenuService.createMenu(vo), MENU_PARENT_NOT_DIR_OR_MENU);
    }


    private SysMenuCreateReqVO createReqVO(Long parentId) {
        SysMenuCreateReqVO vo = randomPojo(SysMenuCreateReqVO.class, o -> {
            o.setParentId(parentId);
            o.setName("testSonName");
            o.setType(MenuTypeEnum.MENU.getType());
            o.setStatus(RandomUtils.randomCommonStatus());
        });
        return vo;
    }

    private SysMenuDO createParentMenuDO(MenuTypeEnum typeEnum) {
        SysMenuDO menuDO = randomPojo(SysMenuDO.class, o -> {
            o.setName("testParentName");
            o.setType(typeEnum.getType());
            o.setStatus(RandomUtils.randomCommonStatus());
        });
        return menuDO;
    }

}
