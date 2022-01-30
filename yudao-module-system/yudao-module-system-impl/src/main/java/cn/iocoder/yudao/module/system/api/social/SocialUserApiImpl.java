package cn.iocoder.yudao.module.system.api.social;

import cn.iocoder.yudao.module.system.api.social.dto.SocialUserBindReqDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserUnbindReqDTO;
import cn.iocoder.yudao.module.system.service.social.SocialUserService;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.SOCIAL_USER_NOT_FOUND;

/**
 * 社交用户的 API 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class SocialUserApiImpl implements SocialUserApi {

    @Resource
    private SocialUserService socialUserService;

    @Override
    public String getAuthorizeUrl(Integer type, String redirectUri) {
        return socialUserService.getAuthorizeUrl(type, redirectUri);
    }

    @Override
    public void bindSocialUser(SocialUserBindReqDTO reqDTO) {
        // 使用 code 授权
        AuthUser authUser = socialUserService.getAuthUser(reqDTO.getType(), reqDTO.getCode(),
                reqDTO.getState());
        if (authUser == null) {
            throw exception(SOCIAL_USER_NOT_FOUND);
        }

        // 绑定社交用户（新增）
        socialUserService.bindSocialUser(reqDTO.getUserId(), reqDTO.getUserType(),
                reqDTO.getType(), authUser);
    }

    @Override
    public void unbindSocialUser(SocialUserUnbindReqDTO reqDTO) {
        socialUserService.unbindSocialUser(reqDTO.getUserId(), reqDTO.getUserType(),
                reqDTO.getType(), reqDTO.getUnionId());
    }

    @Override
    public void checkSocialUser(Integer type, String code, String state) {
        AuthUser authUser = socialUserService.getAuthUser(type, code, state);
        if (authUser == null) {
            throw exception(SOCIAL_USER_NOT_FOUND);
        }
    }

    @Override
    public Long getBindUserId(Integer userType, Integer type, String code, String state) {
        AuthUser authUser = socialUserService.getAuthUser(type, code, state);
        if (authUser == null) {
            throw exception(SOCIAL_USER_NOT_FOUND);
        }

        //
        return null;
    }

}
