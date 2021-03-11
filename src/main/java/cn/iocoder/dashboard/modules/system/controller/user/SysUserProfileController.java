package cn.iocoder.dashboard.modules.system.controller.user;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.framework.security.core.LoginUser;
import cn.iocoder.dashboard.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserProfileRespVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserProfileUpdateReqVO;
import cn.iocoder.dashboard.modules.system.convert.auth.SysAuthConvert;
import cn.iocoder.dashboard.modules.system.convert.user.SysUserConvert;
import cn.iocoder.dashboard.modules.system.dal.dataobject.permission.SysRoleDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.modules.system.service.permission.SysPermissionService;
import cn.iocoder.dashboard.modules.system.service.permission.SysRoleService;
import cn.iocoder.dashboard.modules.system.service.user.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author niudehua
 */
@Api(tags = "用户个人中心")
@RestController
@RequestMapping("/system/user/profile")
public class SysUserProfileController {

    @Resource
    private SysUserService userService;
    @Resource
    private SysPermissionService permissionService;
    @Resource
    private SysRoleService roleService;

    /**
     * 个人信息
     *
     * @return 个人信息详情
     */
    @ApiOperation("获得登录用户信息")
    @GetMapping("/get")
    public CommonResult<SysUserProfileRespVO> profile() {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        // 获取用户信息
        assert loginUser != null;
        Long userId = loginUser.getId();
        SysUserDO user = userService.getUser(userId);
        SysUserProfileRespVO userProfileRespVO = SysUserConvert.INSTANCE.convert03(user);
        List<SysRoleDO> userRoles = roleService.listRolesFromCache(permissionService.listUserRoleIs(userId));
        userProfileRespVO.setRoles(userRoles.stream().map(SysUserConvert.INSTANCE::convert).collect(Collectors.toSet()));
        return CommonResult.success(userProfileRespVO);
    }

    /**
     * 修改个人信息
     *
     * @param reqVO   个人信息更新 reqVO
     * @param request HttpServletRequest
     * @return 修改结果
     */
    @ApiOperation("修改用户个人信息")
    @PostMapping("/update")
    public CommonResult<Boolean> updateProfile(@RequestBody SysUserProfileUpdateReqVO reqVO, HttpServletRequest request) {
        if (userService.updateUserProfile(reqVO) > 0) {
            SecurityFrameworkUtils.setLoginUser(SysAuthConvert.INSTANCE.convert(reqVO), request);
            return CommonResult.success(true);
        }
        return CommonResult.success(false);
    }

    /**
     * 上传用户个人头像
     *
     * @param file 头像文件
     * @return 上传结果
     */
    @ApiOperation("上传用户个人头像")
    @PostMapping("/uploadAvatar")
    public CommonResult<Boolean> uploadAvatar(@RequestParam("avatarFile") MultipartFile file) {
        if (!file.isEmpty()) {
            LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
            assert loginUser != null;
            if (userService.updateAvatar(loginUser.getId(), file) > 0) {
                return CommonResult.success(true);
            }
        }
        return CommonResult.success(false);
    }
}
