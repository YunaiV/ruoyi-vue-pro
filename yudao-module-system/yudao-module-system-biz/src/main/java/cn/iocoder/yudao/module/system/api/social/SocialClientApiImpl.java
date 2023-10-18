package cn.iocoder.yudao.module.system.api.social;

import cn.iocoder.yudao.module.system.service.social.SocialClientService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * 社交应用的 API 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class SocialClientApiImpl implements SocialClientApi {

    @Resource
    private SocialClientService socialClientService;

    @Override
    public String getAuthorizeUrl(Integer type, Integer userType, String redirectUri) {
        return socialClientService.getAuthorizeUrl(type, userType, redirectUri);
    }

}
