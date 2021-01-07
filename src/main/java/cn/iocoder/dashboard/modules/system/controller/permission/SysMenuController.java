package cn.iocoder.dashboard.modules.system.controller.permission;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.SysMenuListReqVO;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.SysMenuRespVO;
import cn.iocoder.dashboard.modules.system.service.permission.SysMenuService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@Api(tags = "菜单 API")
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    @Resource
    private SysMenuService menuService;

    /**
     * 获取菜单列表
     */
//    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @GetMapping("/list")
    public CommonResult<List<SysMenuRespVO>> list(SysMenuListReqVO reqVO) {
        return success(menuService.listMenus(reqVO));
    }
//
//    /**
//     * 根据菜单编号获取详细信息
//     */
//    @PreAuthorize("@ss.hasPermi('system:menu:query')")
//    @GetMapping(value = "/{menuId}")
//    public AjaxResult getInfo(@PathVariable Long menuId) {
//        return AjaxResult.success(menuService.selectMenuById(menuId));
//    }
//
//    /**
//     * 获取菜单下拉树列表
//     */
//    @GetMapping("/treeselect")
//    public AjaxResult treeselect(SysMenu menu) {
//        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
//        Long userId = loginUser.getUser().getUserId();
//        List<SysMenu> menus = menuService.selectMenuList(menu, userId);
//        return AjaxResult.success(menuService.buildMenuTreeSelect(menus));
//    }
//
//    /**
//     * 加载对应角色菜单列表树
//     */
//    @GetMapping(value = "/roleMenuTreeselect/{roleId}")
//    public AjaxResult roleMenuTreeselect(@PathVariable("roleId") Long roleId) {
//        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
//        List<SysMenu> menus = menuService.selectMenuList(loginUser.getUser().getUserId());
//        AjaxResult ajax = AjaxResult.success();
//        ajax.put("checkedKeys", menuService.selectMenuListByRoleId(roleId));
//        ajax.put("menus", menuService.buildMenuTreeSelect(menus));
//        return ajax;
//    }
//
//    /**
//     * 新增菜单
//     */
//    @PreAuthorize("@ss.hasPermi('system:menu:add')")
//    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
//    @PostMapping
//    public AjaxResult add(@Validated @RequestBody SysMenu menu) {
//        if (UserConstants.NOT_UNIQUE.equals(menuService.checkMenuNameUnique(menu))) {
//            return AjaxResult.error("新增菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
//        } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame())
//                && !StringUtils.startsWithAny(menu.getPath(), Constants.HTTP, Constants.HTTPS)) {
//            return AjaxResult.error("新增菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
//        }
//        menu.setCreateBy(SecurityUtils.getUsername());
//        return toAjax(menuService.insertMenu(menu));
//    }
//
//    /**
//     * 修改菜单
//     */
//    @PreAuthorize("@ss.hasPermi('system:menu:edit')")
//    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
//    @PutMapping
//    public AjaxResult edit(@Validated @RequestBody SysMenu menu) {
//        if (UserConstants.NOT_UNIQUE.equals(menuService.checkMenuNameUnique(menu))) {
//            return AjaxResult.error("修改菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
//        } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame())
//                && !StringUtils.startsWithAny(menu.getPath(), Constants.HTTP, Constants.HTTPS)) {
//            return AjaxResult.error("修改菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
//        } else if (menu.getMenuId().equals(menu.getParentId())) {
//            return AjaxResult.error("修改菜单'" + menu.getMenuName() + "'失败，上级菜单不能选择自己");
//        }
//        menu.setUpdateBy(SecurityUtils.getUsername());
//        return toAjax(menuService.updateMenu(menu));
//    }
//
//    /**
//     * 删除菜单
//     */
//    @PreAuthorize("@ss.hasPermi('system:menu:remove')")
//    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
//    @DeleteMapping("/{menuId}")
//    public AjaxResult remove(@PathVariable("menuId") Long menuId) {
//        if (menuService.hasChildByMenuId(menuId)) {
//            return AjaxResult.error("存在子菜单,不允许删除");
//        }
//        if (menuService.checkMenuExistRole(menuId)) {
//            return AjaxResult.error("菜单已分配,不允许删除");
//        }
//        return toAjax(menuService.deleteMenuById(menuId));
//    }

}
