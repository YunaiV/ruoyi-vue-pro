package cn.iocoder.yudao.userserver.modules.system.controller.auth;

import cn.iocoder.yudao.coreservice.modules.system.service.social.SysSocialCoreService;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.userserver.modules.system.controller.auth.vo.*;
import cn.iocoder.yudao.userserver.modules.system.enums.sms.SysSmsSceneEnum;
import cn.iocoder.yudao.userserver.modules.system.service.auth.SysAuthService;
import cn.iocoder.yudao.userserver.modules.system.service.sms.SysSmsCodeService;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getUserAgent;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Api(tags = "认证")
@RestController
@RequestMapping("/")
@Validated
@Slf4j
public class SysAuthController {

    @Resource
    private SysAuthService authService;
    @Resource
    private SysSmsCodeService smsCodeService;
    @Resource
    private SysSocialCoreService socialService;

    @PostMapping("/login")
    @ApiOperation("使用手机 + 密码登录")
    public CommonResult<SysAuthLoginRespVO> login(@RequestBody @Valid SysAuthLoginReqVO reqVO) {
        String token = authService.login(reqVO, getClientIP(), getUserAgent());
        // 返回结果
        return success(SysAuthLoginRespVO.builder().token(token).build());
    }

    @PostMapping("/sms-login")
    @ApiOperation("使用手机 + 验证码登录")
    public CommonResult<SysAuthLoginRespVO> smsLogin(@RequestBody @Valid SysAuthSmsLoginReqVO reqVO) {
        String token = authService.smsLogin(reqVO, getClientIP(), getUserAgent());
        // 返回结果
        return success(SysAuthLoginRespVO.builder().token(token).build());
    }

    @PostMapping("/send-sms-code")
    @ApiOperation(value = "发送手机验证码",notes = "不检测该手机号是否已被注册")
    public CommonResult<Boolean> sendSmsCode(@RequestBody @Valid SysAuthSendSmsReqVO reqVO) {
        smsCodeService.sendSmsCode(reqVO.getMobile(), reqVO.getScene(), getClientIP());
        return success(true);
    }

    @PostMapping("/send-sms-new-code")
    @ApiOperation(value = "发送手机验证码",notes = "检测该手机号是否已被注册，用于修改手机时使用")
    public CommonResult<Boolean> sendSmsNewCode(@RequestBody @Valid SysAuthSendSmsReqVO reqVO) {
        smsCodeService.sendSmsNewCode(reqVO);
        return success(true);
    }

    @GetMapping("/send-sms-code-login")
    @ApiOperation(value = "向已登录用户发送验证码",notes = "修改手机时验证原手机号使用")
    public CommonResult<Boolean> sendSmsCodeLogin() {
        smsCodeService.sendSmsCodeLogin(getLoginUserId());
        return success(true);
    }

    @PostMapping("/reset-password")
    @ApiOperation(value = "重置密码", notes = "用户忘记密码时使用")
    @PreAuthenticated
    public CommonResult<Boolean> resetPassword(@RequestBody @Valid MbrAuthResetPasswordReqVO reqVO) {
        authService.resetPassword(reqVO);
        return success(true);
    }

    @PostMapping("/update-password")
    @ApiOperation(value = "修改用户密码",notes = "用户修改密码时使用")
    @PreAuthenticated
    public CommonResult<Boolean> updatePassword(@RequestBody @Valid MbrAuthUpdatePasswordReqVO reqVO) {
        authService.updatePassword(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/check-sms-code")
    @ApiOperation(value = "校验验证码是否正确")
    @PreAuthenticated
    public CommonResult<Boolean> checkSmsCode(@RequestBody @Valid SysAuthSmsLoginReqVO reqVO) {
        // TODO @宋天：check 的时候，不应该使用 useSmsCode 哈，这样验证码就直接被使用了。另外，check 开头的方法，更多是校验的逻辑，不会有 update 数据的动作。这点，在方法命名上，也是要注意的
        smsCodeService.useSmsCode(reqVO.getMobile(),SysSmsSceneEnum.CHECK_CODE_BY_SMS.getScene(),reqVO.getCode(),getClientIP());
        return success(true);
    }

    // ========== 社交登录相关 ==========

    @GetMapping("/social-auth-redirect")
    @ApiOperation("社交授权的跳转")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "社交类型", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "redirectUri", value = "回调路径", dataTypeClass = String.class)
    })
    public CommonResult<String> socialAuthRedirect(@RequestParam("type") Integer type,
                                                   @RequestParam("redirectUri") String redirectUri) {
        return CommonResult.success(socialService.getAuthorizeUrl(type, redirectUri));
    }


    @PostMapping("/social-login")
    @ApiOperation("社交登录，使用 code 授权码")
    public CommonResult<SysAuthLoginRespVO> socialLogin(@RequestBody @Valid MbrAuthSocialLoginReqVO reqVO) {
        String token = authService.socialLogin(reqVO, getClientIP(), getUserAgent());
        return success(SysAuthLoginRespVO.builder().token(token).build());
    }


    @PostMapping("/social-login2")
    @ApiOperation("社交登录，使用 手机号 + 手机验证码")
    public CommonResult<SysAuthLoginRespVO> socialLogin2(@RequestBody @Valid MbrAuthSocialLogin2ReqVO reqVO) {
        String token = authService.socialLogin2(reqVO, getClientIP(), getUserAgent());
        return success(SysAuthLoginRespVO.builder().token(token).build());
    }

    @PostMapping("/social-bind")
    @ApiOperation("社交绑定，使用 code 授权码")
    public CommonResult<Boolean> socialBind(@RequestBody @Valid MbrAuthSocialBindReqVO reqVO) {
        authService.socialBind(getLoginUserId(), reqVO);
        return CommonResult.success(true);
    }

    @DeleteMapping("/social-unbind")
    @ApiOperation("取消社交绑定")
    public CommonResult<Boolean> socialUnbind(@RequestBody MbrAuthSocialUnbindReqVO reqVO) {
        socialService.unbindSocialUser(getLoginUserId(), reqVO.getType(), reqVO.getUnionId(), UserTypeEnum.MEMBER);
        return CommonResult.success(true);
    }

}
