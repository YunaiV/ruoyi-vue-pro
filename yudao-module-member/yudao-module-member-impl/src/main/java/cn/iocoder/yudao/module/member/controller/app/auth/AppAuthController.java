package cn.iocoder.yudao.module.member.controller.app.auth;

import cn.iocoder.yudao.coreservice.modules.system.service.social.SysSocialCoreService;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.member.controller.app.auth.vo.*;
import cn.iocoder.yudao.module.member.service.auth.AuthService;
import cn.iocoder.yudao.module.member.service.sms.SysSmsCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getUserAgent;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Api(tags = "APP 端 - 认证")
@RestController
@RequestMapping("/")
@Validated
@Slf4j
public class AppAuthController {

    @Resource
    private AuthService authService;

    @Resource
    private SysSmsCodeService smsCodeService;
    @Resource
    private SysSocialCoreService socialService;

    @PostMapping("/login")
    @ApiOperation("使用手机 + 密码登录")
    @OperateLog(enable = false) // 避免 Post 请求被记录操作日志
    public CommonResult<AppAuthLoginRespVO> login(@RequestBody @Valid AppAuthLoginReqVO reqVO) {
        String token = authService.login(reqVO, getClientIP(), getUserAgent());
        // 返回结果
        return success(AppAuthLoginRespVO.builder().token(token).build());
    }

    @PostMapping("/sms-login")
    @ApiOperation("使用手机 + 验证码登录")
    @OperateLog(enable = false) // 避免 Post 请求被记录操作日志
    public CommonResult<AppAuthLoginRespVO> smsLogin(@RequestBody @Valid AppAuthSmsLoginReqVO reqVO) {
        String token = authService.smsLogin(reqVO, getClientIP(), getUserAgent());
        // 返回结果
        return success(AppAuthLoginRespVO.builder().token(token).build());
    }

    @PostMapping("/send-sms-code")
    @ApiOperation(value = "发送手机验证码")
    @OperateLog(enable = false) // 避免 Post 请求被记录操作日志
    public CommonResult<Boolean> sendSmsCode(@RequestBody @Valid AppAuthSendSmsReqVO reqVO) {
        smsCodeService.sendSmsCode(reqVO.getMobile(), reqVO.getScene(), getClientIP());
        return success(true);
    }

    @GetMapping("/send-sms-code-login") // TODO 芋艿：post 比较合理
    @ApiOperation(value = "向已登录用户发送验证码",notes = "修改手机时验证原手机号使用")
    public CommonResult<Boolean> sendSmsCodeLogin() {
        smsCodeService.sendSmsCodeLogin(getLoginUserId());
        return success(true);
    }

    @PostMapping("/reset-password")
    @ApiOperation(value = "重置密码", notes = "用户忘记密码时使用")
    @PreAuthenticated
    @OperateLog(enable = false) // 避免 Post 请求被记录操作日志
    public CommonResult<Boolean> resetPassword(@RequestBody @Valid AppAuthResetPasswordReqVO reqVO) {
        authService.resetPassword(reqVO);
        return success(true);
    }

    @PostMapping("/update-password")
    @ApiOperation(value = "修改用户密码",notes = "用户修改密码时使用")
    @PreAuthenticated
    public CommonResult<Boolean> updatePassword(@RequestBody @Valid AppAuthUpdatePasswordReqVO reqVO) {
        authService.updatePassword(getLoginUserId(), reqVO);
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
    public CommonResult<AppAuthLoginRespVO> socialLogin(@RequestBody @Valid AppAuthSocialLoginReqVO reqVO) {
        String token = authService.socialLogin(reqVO, getClientIP(), getUserAgent());
        return success(AppAuthLoginRespVO.builder().token(token).build());
    }

    @PostMapping("/social-login2")
    @ApiOperation("社交登录，使用 手机号 + 手机验证码")
    @OperateLog(enable = false) // 避免 Post 请求被记录操作日志
    public CommonResult<AppAuthLoginRespVO> socialLogin2(@RequestBody @Valid AppAuthSocialLogin2ReqVO reqVO) {
        String token = authService.socialLogin2(reqVO, getClientIP(), getUserAgent());
        return success(AppAuthLoginRespVO.builder().token(token).build());
    }

    @PostMapping("/social-bind")
    @ApiOperation("社交绑定，使用 code 授权码")
    @PreAuthenticated
    public CommonResult<Boolean> socialBind(@RequestBody @Valid AppAuthSocialBindReqVO reqVO) {
        authService.socialBind(getLoginUserId(), reqVO);
        return CommonResult.success(true);
    }

    @DeleteMapping("/social-unbind")
    @ApiOperation("取消社交绑定")
    @PreAuthenticated
    public CommonResult<Boolean> socialUnbind(@RequestBody AppAuthSocialUnbindReqVO reqVO) {
        socialService.unbindSocialUser(getLoginUserId(), reqVO.getType(), reqVO.getUnionId(), UserTypeEnum.MEMBER);
        return CommonResult.success(true);
    }

}
