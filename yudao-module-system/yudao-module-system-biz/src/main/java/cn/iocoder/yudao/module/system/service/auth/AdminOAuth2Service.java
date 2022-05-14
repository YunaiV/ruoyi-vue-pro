package cn.iocoder.yudao.module.system.service.auth;

import cn.iocoder.yudao.module.system.dal.dataobject.auth.OAuth2AccessTokenDO;

import java.util.Collection;

/**
 * 管理后台的 OAuth2 Service 接口
 *
 * 将自身的 AdminUser 用户，授权给第三方应用，采用 OAuth2.0 的协议。
 *
 * 问题：为什么自身也作为一个第三方应用，也走这套流程呢？
 * 回复：当然可以这么做，采用 Implicit 模式。考虑到大多数开发者使用不到这个特性，OAuth2.0 毕竟有一定学习成本，所以暂时没有采取这种方式。
 *
 * @author 芋道源码
 */
public interface AdminOAuth2Service {

    // ImplicitTokenGranter
    OAuth2AccessTokenDO grantImplicit(Long userId, Integer userType,
                                      String clientId, Collection<String> scopes);

    // AuthorizationCodeTokenGranter
    String grantAuthorizationCode(Long userId, Integer userType,
                                  String clientId, Collection<String> scopes,
                                  String redirectUri, String state);

}
