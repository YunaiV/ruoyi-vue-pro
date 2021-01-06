package cn.iocoder.dashboard.modules.system.controller.auth;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.SysAuthMenuRespVO;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.SysAuthPermissionInfoRespVO;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.SysAuthLoginReqVO;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.SysAuthLoginRespVO;
import cn.iocoder.dashboard.modules.system.service.auth.SysAuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import java.util.List;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;
import static cn.iocoder.dashboard.framework.security.core.util.SecurityUtils.getLoginUserId;
import static cn.iocoder.dashboard.framework.security.core.util.SecurityUtils.getLoginUserRoleIds;

@Api(tags = "认证 API")
@RestController
@RequestMapping("/")
public class SysAuthController {

    @Resource
    private SysAuthService authService;

    @ApiOperation("使用账号密码登录")
    @PostMapping("/login")
    public CommonResult<SysAuthLoginRespVO> login(@RequestBody @Valid SysAuthLoginReqVO reqVO) {
        String token = authService.login(reqVO.getUsername(), reqVO.getPassword(), reqVO.getUuid(), reqVO.getCode());
        // 返回结果
        return success(SysAuthLoginRespVO.builder().token(token).build());
    }

    @ApiOperation("获取登陆用户的权限信息")
    @GetMapping("/get-permission-info")
    public CommonResult<SysAuthPermissionInfoRespVO> getPermissionInfo() {
        SysAuthPermissionInfoRespVO respVO = authService.getPermissionInfo(getLoginUserId(), getLoginUserRoleIds());
        return success(respVO);
    }

    @ApiOperation("获得登陆用户的菜单列表")
    @GetMapping("list-menus")
    public CommonResult<List<SysAuthMenuRespVO>> listMenus() {
        List<SysAuthMenuRespVO> respVOList = authService.listMenus(getLoginUserId(), getLoginUserRoleIds());
        return success(respVOList);
    }

}
