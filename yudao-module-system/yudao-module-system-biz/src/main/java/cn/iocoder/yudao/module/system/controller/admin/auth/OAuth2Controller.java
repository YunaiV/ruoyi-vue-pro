package cn.iocoder.yudao.module.system.controller.admin.auth;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;

@Api(tags = "管理后台 - OAuth2.0 授权")
@RestController
@RequestMapping("/system/oauth2")
@Validated
@Slf4j
public class OAuth2Controller {

//    POST oauth/token TokenEndpoint：Password、Implicit、Code、Refresh Token

//    POST oauth/check_token CheckTokenEndpoint

//    DELETE oauth/token ConsumerTokenServices#revokeToken

//    GET  oauth/authorize AuthorizationEndpoint

    @PostMapping("/authorize")
    @ApiOperation(value = "申请授权", notes = "适合 code 授权码模式，或者 implicit 简化模式；在 authorize.vue 单点登录界面被调用")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "response_type", required = true, value = "响应类型", example = "code", dataTypeClass = String.class),
            @ApiImplicitParam(name = "client_id", required = true, value = "客户端编号", example = "tudou", dataTypeClass = String.class),
            @ApiImplicitParam(name = "scope", value = "授权范围", example = "userinfo.read", dataTypeClass = String.class), // 多个使用逗号分隔
            @ApiImplicitParam(name = "redirect_uri", required = true, value = "重定向 URI", example = "https://www.iocoder.cn", dataTypeClass = String.class),
            @ApiImplicitParam(name = "state", example = "123321", dataTypeClass = String.class)
    })
    @OperateLog(enable = false) // 避免 Post 请求被记录操作日志
    // 因为前后端分离，Axios 无法很好的处理 302 重定向，所以和 Spring Security OAuth 略有不同，返回结果是重定向的 URL，剩余交给前端处理
    public CommonResult<String> authorize(@RequestParam("response_type") String responseType,
                                          @RequestParam("client_id") String clientId,
                                          @RequestParam(value = "scope", required = false) String scope,
                                          @RequestParam("redirect_uri") String redirectUri,
                                          @RequestParam(value = "state", required = false) String state) {
        // 0. 校验用户已经登录。通过 Spring Security 实现

        // 1.1 校验 responseType 是否满足 code 或者 token 值
        if (!StrUtil.equalsAny(responseType, "code", "token")) {
            throw exception0(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "response_type 参数值允许 code 和 token");
        }
        // 1.2 校验 redirectUri 重定向域名是否合法

        // 1.3 校验 scope 是否在 Client 授权范围内

        // 2.1 如果是 code 授权码模式，则发放 code 授权码，并重定向

        // 2.2 如果是 token 则是 implicit 简化模式，则发送 accessToken 访问令牌，并重定向
        // TODO 需要确认，是否要 refreshToken 生成
        return CommonResult.success("");
    }

}
