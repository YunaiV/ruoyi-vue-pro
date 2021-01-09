package cn.iocoder.dashboard.modules.system.controller.permission;

import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.menu.*;
import cn.iocoder.dashboard.modules.system.convert.permission.SysMenuConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysMenuDO;
import cn.iocoder.dashboard.modules.system.service.permission.SysMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Comparator;
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
    public CommonResult<List<SysMenuRespVO>> listMenus(SysMenuListReqVO reqVO) {
        List<SysMenuDO> list = menuService.listMenus(reqVO);
        list.sort(Comparator.comparing(SysMenuDO::getSort));
        return success(SysMenuConvert.INSTANCE.convertList(list));
    }

    @ApiOperation(value = "获取菜单精简信息列表", notes = "只包含被开启的菜单，主要用于前端的下拉选项")
    @GetMapping("/list-all-simple")
    public CommonResult<List<SysMenuSimpleRespVO>> listSimpleMenus() {
        // 获得菜单列表，只要开启状态的
        SysMenuListReqVO reqVO = new SysMenuListReqVO();
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        List<SysMenuDO> list = menuService.listMenus(reqVO);
        // 排序后，返回个诶前端
        list.sort(Comparator.comparing(SysMenuDO::getSort));
        return success(SysMenuConvert.INSTANCE.convertList02(list));
    }

    @ApiOperation("获取菜单信息")
    @GetMapping("/get")
//    @PreAuthorize("@ss.hasPermi('system:menu:query')")
    public CommonResult<SysMenuRespVO> getMenu(Long id) {
        SysMenuDO menu = menuService.getMenu(id);
        return success(SysMenuConvert.INSTANCE.convert(menu));
    }

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

    @ApiOperation("创建菜单")
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
    @ApiImplicitParam(name = "id", value = "角色编号", required= true, example = "1024")
//    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    public CommonResult<Boolean> deleteMenu(@RequestParam("id") Long id) {
        menuService.deleteMenu(id);
        return success(true);
    }

}
