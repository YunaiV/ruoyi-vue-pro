package cn.iocoder.yudao.userserver.modules.member.controller.user;

import cn.iocoder.yudao.userserver.modules.member.controller.user.vo.MbrUserInfoRespVO;
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

    @PutMapping("/update-nickname")
    @ApiOperation("修改用户昵称")
    @PreAuthenticated
    public CommonResult<Boolean> updateNickname(@RequestParam("nickName") String nickName) {
        userService.updateNickname(getLoginUserId(), nickName);
        return success(true);
    }

    @PutMapping("/update-avatar")
    @ApiOperation("修改用户头像")
    @PreAuthenticated
    public CommonResult<String> updateAvatar(@RequestParam("avatarFile") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw ServiceExceptionUtil.exception(FILE_IS_EMPTY);
        }
        String avatar = userService.updateAvatar(getLoginUserId(), file.getInputStream());
        return success(avatar);
    }

    @GetMapping("/get-user-info")
    @ApiOperation("获取用户头像与昵称")
    @PreAuthenticated
    public CommonResult<MbrUserInfoRespVO> getUserInfo() {
        return success(userService.getUserInfo(getLoginUserId()));
    }

}

