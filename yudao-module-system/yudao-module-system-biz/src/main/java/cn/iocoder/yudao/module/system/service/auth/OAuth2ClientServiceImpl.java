package cn.iocoder.yudao.module.system.service.auth;

import cn.iocoder.yudao.module.system.dal.dataobject.auth.OAuth2ClientDO;
import org.springframework.stereotype.Service;

/**
 * OAuth2.0 Client Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class OAuth2ClientServiceImpl implements OAuth2ClientService {

    @Override
    public OAuth2ClientDO validOAuthClientFromCache(Long id) {
        return new OAuth2ClientDO().setId(id)
                .setAccessTokenValiditySeconds(60 * 30)
                .setRefreshTokenValiditySeconds(60 * 60 * 24 * 30);
    }

}
