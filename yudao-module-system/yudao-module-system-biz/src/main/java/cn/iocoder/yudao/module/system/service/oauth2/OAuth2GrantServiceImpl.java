package cn.iocoder.yudao.module.system.service.oauth2;

import cn.iocoder.yudao.module.system.dal.dataobject.auth.OAuth2AccessTokenDO;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * OAuth2 授予 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class OAuth2GrantServiceImpl implements OAuth2GrantService {

    @Override
    public OAuth2AccessTokenDO grantImplicit(Long userId, Integer userType,
                                             String clientId, Collection<String> scopes) {
        return null;
    }

    @Override
    public String grantAuthorizationCode(Long userId, Integer userType,
                                         String clientId, Collection<String> scopes,
                                         String redirectUri, String state) {
        return null;
    }

}
