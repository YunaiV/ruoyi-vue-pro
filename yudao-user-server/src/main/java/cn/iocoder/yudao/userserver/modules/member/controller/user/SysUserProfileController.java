package cn.iocoder.yudao.userserver.modules.member.controller.user;

import cn.iocoder.yudao.coreservice.modules.system.controller.user.vo.SysUserCoreProfileRespVo;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.userserver.modules.member.service.user.MbrUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

import java.io.IOException;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.userserver.modules.member.enums.MbrErrorCodeConstants.FILE_IS_EMPTY;

@Api(tags = "用户个人中心")
@RestController
@RequestMapping("/system/user/profile")
@Validated
@Slf4j
public class SysUserProfileController {

    @Resource
    private MbrUserService userService;

    @GetMapping("/get")
    @ApiOperation("获得登录用户信息")
    @PreAuthenticated
    public CommonResult<Boolean> profile() {
        return null;
    }

    @PutMapping("/revise-nickname")
    @ApiOperation("修改用户昵称")
    public CommonResult<Boolean> reviseNickname(@RequestParam("nickName") String nickName) {
        userService.reviseNickname(getLoginUserId(), nickName);
        return success(true);
    }

    @PutMapping("/revise-avatar")
    @ApiOperation("修改用户头像")
    public CommonResult<String> reviseAvatar(@RequestParam("avatarFile") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw ServiceExceptionUtil.exception(FILE_IS_EMPTY);
        }
        String avatar = userService.reviseAvatar(getLoginUserId(), file.getInputStream());
        return success(avatar);
    }

    @GetMapping("/get-user-info")
    @ApiOperation("获取用户头像与昵称")
    public CommonResult<SysUserCoreProfileRespVo> getUserInfo(@RequestParam("id") Long id) {
        return success(userService.getUserInfo(id));
    }
}

