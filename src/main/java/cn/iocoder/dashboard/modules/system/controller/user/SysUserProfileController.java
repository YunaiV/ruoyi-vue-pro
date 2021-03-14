package cn.iocoder.dashboard.modules.system.controller.user;

import cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.dashboard.common.pojo.CommonResult;
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
import cn.iocoder.dashboard.util.collection.CollectionUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;
import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.FILE_IS_EMPTY;

/**
 * @author niudehua
 */
@RestController
@RequestMapping("/system/user/profile")
@Api(tags = "用户个人中心")
@Slf4j
public class SysUserProfileController {

    @Resource
    private SysUserService userService;
    @Resource
    private SysPermissionService permissionService;
    @Resource
    private SysRoleService roleService;

    @GetMapping("/get")
    @ApiOperation("获得登录用户信息")
    public CommonResult<SysUserProfileRespVO> profile() {
        // 获取用户信息
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        SysUserDO user = userService.getUser(userId);
        SysUserProfileRespVO userProfileRespVO = SysUserConvert.INSTANCE.convert03(user);
        List<SysRoleDO> userRoles = roleService.getRolesFromCache(permissionService.listUserRoleIs(userId));
        userProfileRespVO.setRoles(CollectionUtils.convertSet(userRoles, SysUserConvert.INSTANCE::convert));
        return success(userProfileRespVO);
    }

    @PutMapping("/update")
    @ApiOperation("修改用户个人信息")
    public CommonResult<Boolean> updateUserProfile(@RequestBody SysUserProfileUpdateReqVO reqVO, HttpServletRequest request) {
        userService.updateUserProfile(reqVO);
        SecurityFrameworkUtils.setLoginUser(SysAuthConvert.INSTANCE.convert(reqVO), request);
        return success(true);
    }

    @PutMapping("/upload-avatar")
    @ApiOperation("上传用户个人头像")
    public CommonResult<Boolean> updateUserAvatar(@RequestParam("avatarFile") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw ServiceExceptionUtil.exception(FILE_IS_EMPTY);
        }
        userService.updateUserAvatar(SecurityFrameworkUtils.getLoginUserId(), file.getInputStream());
        return success(true);
    }

}
