package cn.iocoder.dashboard.modules.system.controller.permission;

import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.role.*;
import cn.iocoder.dashboard.modules.system.convert.permission.SysRoleConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysRoleDO;
import cn.iocoder.dashboard.modules.system.service.permission.SysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    @ApiOperation(value = "获取角色精简信息列表", notes = "只包含被开启的角色，主要用于前端的下拉选项")
    @GetMapping("/list-all-simple")
    public CommonResult<List<SysRoleSimpleRespVO>> listSimpleRoles() {
        // 获得角色列表，只要开启状态的
        List<SysRoleDO> list = roleService.listRoles(Collections.singleton(CommonStatusEnum.ENABLE.getStatus()));
        // 排序后，返回个诶前端
        list.sort(Comparator.comparing(SysRoleDO::getSort));
        return success(SysRoleConvert.INSTANCE.convertList02(list));
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
    @ApiImplicitParam(name = "id", value = "角色编号", required = true, example = "1024", dataTypeClass = Long.class)
//    @PreAuthorize("@ss.hasPermi('system:role:remove')")
//    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    public CommonResult<Boolean> deleteRole(@RequestParam("id") Long id) {
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
//    @PreAuthorize("@ss.hasPermi('system:role:edit')")
//    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    public CommonResult<Boolean> updateRoleStatus(@Validated @RequestBody SysRoleUpdateStatusReqVO reqVO) {
        roleService.updateRoleStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

}
