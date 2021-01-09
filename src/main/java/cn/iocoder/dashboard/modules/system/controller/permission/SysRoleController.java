package cn.iocoder.dashboard.modules.system.controller.permission;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.role.SysRoleCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.role.SysRolePageReqVO;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.role.SysRoleRespVO;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.role.SysRoleUpdateReqVO;
import cn.iocoder.dashboard.modules.system.convert.permission.SysRoleConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysRoleDO;
import cn.iocoder.dashboard.modules.system.service.permission.SysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@Api(tags = "角色 API")
@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    @Resource
    private SysRoleService roleService;

    @ApiOperation("获得角色分页")
    @GetMapping("/page")
//    @PreAuthorize("@ss.hasPermi('system:role:list')")
    public CommonResult<PageResult<SysRoleDO>> list(SysRolePageReqVO reqVO) {
        return success(roleService.pageRole(reqVO));
    }

    @ApiOperation("创建角色")
    @PostMapping("/create")
//    @PreAuthorize("@ss.hasPermi('system:role:add')")
//    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    public CommonResult<Long> add(@Validated @RequestBody SysRoleCreateReqVO reqVO) {
        return success(roleService.createRole(reqVO));
    }

    @ApiOperation("修改角色")
//    @PreAuthorize("@ss.hasPermi('system:role:edit')")
//    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public CommonResult<Boolean> update(@Validated @RequestBody SysRoleUpdateReqVO reqVO) {
        roleService.updateRole(reqVO);
        return success(true);
    }

    @ApiOperation("删除角色")
    @PostMapping("/delete")
    @ApiImplicitParam(name = "id", value = "角色编号", required = true, example = "1024")
//    @PreAuthorize("@ss.hasPermi('system:role:remove')")
//    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    public CommonResult<Boolean> remove(@RequestParam("id") Long id) {
        roleService.deleteRole(id);
        return success(true);
    }

    @ApiOperation("获得角色信息")
    @GetMapping("/get")
//    @PreAuthorize("@ss.hasPermi('system:role:query')")
    public CommonResult<SysRoleRespVO> getRole(@RequestParam("id") Long id) {
        SysRoleDO role = roleService.getRole(id);
        return success(SysRoleConvert.INSTANCE.convert(role));
    }

//    @Log(title = "角色管理", businessType = BusinessType.EXPORT)
//    @PreAuthorize("@ss.hasPermi('system:role:export')")
//    @GetMapping("/export")
//    public AjaxResult export(SysRole role)
//    {
//        List<SysRole> list = roleService.selectRoleList(role);
//        ExcelUtil<SysRole> util = new ExcelUtil<SysRole>(SysRole.class);
//        return util.exportExcel(list, "角色数据");
//    }

    @ApiOperation("修改角色状态")
    @PostMapping("/update-status")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "id", value = "角色编号", required = true, example = "1024"),
            @ApiImplicitParam(name = "status", value = "状态", required = true, example = "1")
    })
//    @PreAuthorize("@ss.hasPermi('system:role:edit')")
//    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    public CommonResult<Boolean> updateRoleStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        roleService.updateRoleStatus(id, status);
        return success(true);
    }

//
//    /**
//     * 修改保存数据权限
//     */
//    @PreAuthorize("@ss.hasPermi('system:role:edit')")
//    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
//    @PutMapping("/dataScope")
//    public AjaxResult dataScope(@RequestBody SysRole role)
//    {
//        roleService.checkRoleAllowed(role);
//        return toAjax(roleService.authDataScope(role));
//    }


//
//    /**
//     * 获取角色选择框列表
//     */
//    @PreAuthorize("@ss.hasPermi('system:role:query')")
//    @GetMapping("/optionselect")
//    public AjaxResult optionselect()
//    {
//        return AjaxResult.success(roleService.selectRoleAll());
//    }

}
