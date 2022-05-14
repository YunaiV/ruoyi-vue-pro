package cn.iocoder.yudao.module.system.service.auth;

import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * OAuth2 批准 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class OAuth2ApproveServiceImpl implements OAuth2ApproveService {

    @Override
    public boolean checkForPreApproval(Long userId, Integer userType, String clientId, Collection<String> scopes) {
        return false;
    }

}
