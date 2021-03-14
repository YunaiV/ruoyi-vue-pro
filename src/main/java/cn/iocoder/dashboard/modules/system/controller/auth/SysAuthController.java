package cn.iocoder.dashboard.modules.system.controller.auth;

import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.framework.logger.operatelog.core.annotations.OperateLog;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.auth.SysAuthLoginReqVO;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.auth.SysAuthLoginRespVO;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.auth.SysAuthMenuRespVO;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.auth.SysAuthPermissionInfoRespVO;
import cn.iocoder.dashboard.modules.system.convert.auth.SysAuthConvert;
import cn.iocoder.dashboard.modules.system.dal.dataobject.permission.SysMenuDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.permission.SysRoleDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.modules.system.enums.permission.MenuTypeEnum;
import cn.iocoder.dashboard.modules.system.service.auth.SysAuthService;
import cn.iocoder.dashboard.modules.system.service.permission.SysPermissionService;
import cn.iocoder.dashboard.modules.system.service.permission.SysRoleService;
import cn.iocoder.dashboard.modules.system.service.user.SysUserService;
import cn.iocoder.dashboard.util.collection.SetUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;
import static cn.iocoder.dashboard.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.dashboard.framework.security.core.util.SecurityFrameworkUtils.getLoginUserRoleIds;
import static cn.iocoder.dashboard.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.dashboard.util.servlet.ServletUtils.getUserAgent;

@Api(tags = "认证")
@RestController
@RequestMapping("/")
@Validated
public class SysAuthController {

    @Resource
    private SysAuthService authService;
    @Resource
    private SysUserService userService;
    @Resource
    private SysRoleService roleService;
    @Resource
    private SysPermissionService permissionService;

    @PostMapping("/login")
    @ApiOperation("使用账号密码登录")
    @OperateLog(enable = false) // 避免 Post 请求被记录操作日志
    public CommonResult<SysAuthLoginRespVO> login(@RequestBody @Valid SysAuthLoginReqVO reqVO) {
        String token = authService.login(reqVO, getClientIP(), getUserAgent());
        // 返回结果
        return success(SysAuthLoginRespVO.builder().token(token).build());
    }

    @GetMapping("/get-permission-info")
    @ApiOperation("获取登陆用户的权限信息")
    public CommonResult<SysAuthPermissionInfoRespVO> getPermissionInfo() {
        // 获得用户信息
        SysUserDO user = userService.getUser(getLoginUserId());
        if (user == null) {
            return null;
        }
        // 获得角色列表
        List<SysRoleDO> roleList = roleService.listRolesFromCache(getLoginUserRoleIds());
        // 获得菜单列表
        List<SysMenuDO> menuList = permissionService.getRoleMenusFromCache(
                getLoginUserRoleIds(), // 注意，基于登陆的角色，因为后续的权限判断也是基于它
                SetUtils.asSet(MenuTypeEnum.DIR.getType(), MenuTypeEnum.MENU.getType(), MenuTypeEnum.BUTTON.getType()),
                SetUtils.asSet(CommonStatusEnum.ENABLE.getStatus()));
        // 拼接结果返回
        return success(SysAuthConvert.INSTANCE.convert(user, roleList, menuList));
    }

    @GetMapping("list-menus")
    @ApiOperation("获得登陆用户的菜单列表")
    public CommonResult<List<SysAuthMenuRespVO>> getMenus() {
        // 获得用户拥有的菜单列表
        List<SysMenuDO> menuList = permissionService.getRoleMenusFromCache(
                getLoginUserRoleIds(), // 注意，基于登陆的角色，因为后续的权限判断也是基于它
                SetUtils.asSet(MenuTypeEnum.DIR.getType(), MenuTypeEnum.MENU.getType()), // 只要目录和菜单类型
                SetUtils.asSet(CommonStatusEnum.ENABLE.getStatus())); // 只要开启的
        // 转换成 Tree 结构返回
        return success(SysAuthConvert.INSTANCE.buildMenuTree(menuList));
    }

}
