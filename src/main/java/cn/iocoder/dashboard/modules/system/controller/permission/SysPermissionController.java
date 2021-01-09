package cn.iocoder.dashboard.modules.system.controller.permission;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.permission.SysPermissionAssignRoleDataScopeReqVO;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.permission.SysPermissionAssignRoleMenuReqVO;
import cn.iocoder.dashboard.modules.system.service.permission.SysPermissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Set;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

/**
 * 权限 Controller，提供赋予用户、角色的权限的 API 接口
 *
 * @author 芋道源码
 */
@Api(tags = "权限 API")
@RestController
@RequestMapping("/system/permission")
public class SysPermissionController {

    @Resource
    private SysPermissionService permissionService;

    @ApiOperation("获得角色拥有的菜单编号")
    @GetMapping("/list-role-resources")
    @ApiImplicitParam(name = "roleId", value = "角色编号", required = true)
//    @RequiresPermissions("system:permission:assign-role-menu")
    public CommonResult<Set<Long>> listRoleMenus(Long roleId) {
        return success(permissionService.listRoleMenuIds(roleId));
    }

    @PostMapping("/assign-role-menu")
    @ApiOperation("赋予角色菜单")
//    @RequiresPermissions("system:permission:assign-role-resource")
    public CommonResult<Boolean> assignRoleMenu(@Validated @RequestBody SysPermissionAssignRoleMenuReqVO reqVO) {
        permissionService.assignRoleMenu(reqVO.getRoleId(), reqVO.getMenuIds());
        return success(true);
    }

    @PostMapping("/assign-role-data-scope")
    @ApiOperation("赋予角色数据权限")
//    @RequiresPermissions("system:permission:assign-role-data-scope")
    public CommonResult<Boolean> assignRoleDataScope(
            @Validated @RequestBody SysPermissionAssignRoleDataScopeReqVO reqVO) {
        permissionService.assignRoleDataScope(reqVO.getRoleId(), reqVO.getDataScope(), reqVO.getDataScopeDeptIds());
        return success(true);
    }

//    @GetMapping("/list-admin-roles")
//    @ApiOperation("获得管理员拥有的角色编号列表")
////    @RequiresPermissions("system:permission:assign-admin-role")
//    @ApiImplicitParam(name = "adminId", value = "管理员编号", required = true)
//    public CommonResult<Set<Integer>> listAdminRoles(Integer adminId) {
//        return success(permissionManager.listAdminRoles(adminId));
//    }

//    @PostMapping("/assign-admin-role")
//    @ApiOperation("赋予用户角色")
////    @RequiresPermissions("system:permission:assign-admin-role")
//    public CommonResult<Boolean> assignAdminRole(PermissionAssignAdminRoleDTO assignAdminRoleDTO) {
//        permissionManager.assignAdminRole(assignAdminRoleDTO);
//        return success(true);
//    }

//    system:permission:assign-role-data-scope TODO

}
