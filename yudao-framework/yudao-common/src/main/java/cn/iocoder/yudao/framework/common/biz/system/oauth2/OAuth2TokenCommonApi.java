package cn.iocoder.yudao.framework.common.biz.system.oauth2;

import cn.iocoder.yudao.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCreateReqDTO;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenRespDTO;

import jakarta.validation.Valid;

/**
 * OAuth2.0 Token API 接口
 *
 * @author 芋道源码
 */
public interface OAuth2TokenCommonApi {

    /**
     * 创建访问令牌
     *
     * @param reqDTO 访问令牌的创建信息
     * @return 访问令牌的信息
     */
    OAuth2AccessTokenRespDTO createAccessToken(@Valid OAuth2AccessTokenCreateReqDTO reqDTO);

    /**
     * 校验访问令牌
     *
     * @param accessToken 访问令牌
     * @return 访问令牌的信息
     */
    OAuth2AccessTokenCheckRespDTO checkAccessToken(String accessToken);

    /**
     * 移除访问令牌
     *
     * @param accessToken 访问令牌
     * @return 访问令牌的信息
     */
    OAuth2AccessTokenRespDTO removeAccessToken(String accessToken);

    /**
     * 刷新访问令牌
     *
     * @param refreshToken 刷新令牌
     * @param clientId 客户端编号
     * @return 访问令牌的信息
     */
    OAuth2AccessTokenRespDTO refreshAccessToken(String refreshToken, String clientId);

}
