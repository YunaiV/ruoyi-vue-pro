package cn.iocoder.dashboard.modules.system.controller.permission;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.SysMenuCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.SysMenuListReqVO;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.SysMenuRespVO;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.SysMenuUpdateReqVO;
import cn.iocoder.dashboard.modules.system.convert.permission.SysMenuConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysMenuDO;
import cn.iocoder.dashboard.modules.system.service.permission.SysMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@Api(tags = "菜单 API")
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    @Resource
    private SysMenuService menuService;

    @ApiOperation("获取菜单列表")
//    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @GetMapping("/list")
    public CommonResult<List<SysMenuRespVO>> list(SysMenuListReqVO reqVO) {
        return success(menuService.listMenus(reqVO));
    }

    @ApiOperation("获取菜单信息")
    @GetMapping("/get")
//    @PreAuthorize("@ss.hasPermi('system:menu:query')")
    public CommonResult<SysMenuRespVO> getMenu(Long id) {
        SysMenuDO menu = menuService.getMenu(id);
        return success(SysMenuConvert.INSTANCE.convert(menu));
    }

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

    @ApiOperation("新增菜单")
//    @PreAuthorize("@ss.hasPermi('system:menu:add')")
//    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    public CommonResult<Long> createMenu(@Validated @RequestBody SysMenuCreateReqVO reqVO) {
        Long menuId = menuService.createMenu(reqVO);
        return success(menuId);
    }

    @ApiOperation("修改菜单")
//    @PreAuthorize("@ss.hasPermi('system:menu:edit')")
//    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public CommonResult<Boolean> updateMenu(@Validated @RequestBody SysMenuUpdateReqVO reqVO) {
        menuService.updateMenu(reqVO);
        return success(true);
    }

    @ApiOperation("删除菜单")
    @PostMapping("/delete")
//    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    public CommonResult<Boolean> deleteMenu(@RequestParam("id") Long id) {
        menuService.deleteMenu(id);
        return success(true);
    }

}
