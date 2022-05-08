package cn.iocoder.yudao.module.system.api.auth;

import cn.iocoder.yudao.module.system.api.auth.dto.OAuth2AccessTokenCheckRespDTO;
import cn.iocoder.yudao.module.system.api.auth.dto.OAuth2AccessTokenCreateReqDTO;
import cn.iocoder.yudao.module.system.api.auth.dto.OAuth2AccessTokenRespDTO;

import javax.validation.Valid;

/**
 * OAuth2.0 Token API 接口
 *
 * @author 芋道源码
 */
public interface OAuth2TokenApi {

    OAuth2AccessTokenRespDTO createAccessToken(@Valid OAuth2AccessTokenCreateReqDTO reqDTO);

    OAuth2AccessTokenCheckRespDTO checkAccessToken(String accessToken);

//    void removeToken(OAuth2RemoveTokenByUserReqDTO removeTokenDTO);

}
