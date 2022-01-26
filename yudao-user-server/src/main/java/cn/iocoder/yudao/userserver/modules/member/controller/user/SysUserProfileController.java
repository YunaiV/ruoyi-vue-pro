package cn.iocoder.yudao.userserver.modules.member.controller.user;

import cn.iocoder.yudao.userserver.modules.member.controller.user.vo.MbrUserInfoRespVO;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.userserver.modules.member.service.user.MbrUserService;
import cn.iocoder.yudao.userserver.modules.member.controller.user.vo.MbrUserUpdateMobileReqVO;
import cn.iocoder.yudao.userserver.modules.system.enums.sms.SysSmsSceneEnum;
import cn.iocoder.yudao.userserver.modules.system.service.sms.SysSmsCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;

import java.io.IOException;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.userserver.modules.member.enums.MbrErrorCodeConstants.FILE_IS_EMPTY;

@Api(tags = "用户个人中心")
@RestController
@RequestMapping("/member/user/profile")
@Validated
@Slf4j
public class SysUserProfileController {

    @Resource
    private MbrUserService userService;

    @PutMapping("/update-nickname")
    @ApiOperation("修改用户昵称")
    @PreAuthenticated
    public CommonResult<Boolean> updateNickname(@RequestParam("nickname") String nickname) {
        userService.updateNickname(getLoginUserId(), nickname);
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

    @GetMapping("/get")
    @ApiOperation("获得基本信息")
    @PreAuthenticated
    public CommonResult<MbrUserInfoRespVO> getUserInfo() {
        return success(userService.getUserInfo(getLoginUserId()));
    }

    @PostMapping("/update-mobile")
    @ApiOperation(value = "修改用户手机")
    @PreAuthenticated
    public CommonResult<Boolean> updateMobile(@RequestBody @Valid MbrUserUpdateMobileReqVO reqVO) {
        userService.updateMobile(getLoginUserId(), reqVO);
        return success(true);
    }

}

