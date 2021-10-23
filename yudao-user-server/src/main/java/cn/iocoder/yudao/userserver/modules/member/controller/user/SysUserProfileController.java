package cn.iocoder.yudao.userserver.modules.member.controller.user;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "用户个人中心")
@RestController
@RequestMapping("/system/user/profile")
@Validated
@Slf4j
public class SysUserProfileController {

    @GetMapping("/get")
    @ApiOperation("获得登录用户信息")
    @PreAuthenticated
    public CommonResult<Boolean> profile() {
        return null;
    }

}
