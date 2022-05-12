package cn.iocoder.yudao.module.system.controller.admin.auth;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
