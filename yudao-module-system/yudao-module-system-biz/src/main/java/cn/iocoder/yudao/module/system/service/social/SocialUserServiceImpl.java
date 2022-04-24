package cn.iocoder.yudao.module.system.service.social;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserBindReqDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.social.SocialUserBindDO;
import cn.iocoder.yudao.module.system.dal.dataobject.social.SocialUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.social.SocialUserBindMapper;
import cn.iocoder.yudao.module.system.dal.mysql.social.SocialUserMapper;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import com.xkcoding.justauth.AuthRequestFactory;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;

/**
 * 社交用户 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class SocialUserServiceImpl implements SocialUserService {

    @Resource
    private AuthRequestFactory authRequestFactory;

    @Resource
    private SocialUserBindMapper socialUserBindMapper;
    @Resource
    private SocialUserMapper socialUserMapper;

    @Override
    public String getAuthorizeUrl(Integer type, String redirectUri) {
        // 获得对应的 AuthRequest 实现
        AuthRequest authRequest = authRequestFactory.get(SocialTypeEnum.valueOfType(type).getSource());
        // 生成跳转地址
        String authorizeUri = authRequest.authorize(AuthStateUtils.createState());
        return HttpUtils.replaceUrlQuery(authorizeUri, "redirect_uri", redirectUri);
    }

    @Override
    public SocialUserDO authSocialUser(Integer type, String code, String state) {
        // 优先从 DB 中获取，因为 code 有且可以使用一次。
        // 在社交登录时，当未绑定 User 时，需要绑定登录，此时需要 code 使用两次
        SocialUserDO socialUser = socialUserMapper.selectByTypeAndCodeAnState(type, code, state);
        if (socialUser != null) {
            return socialUser;
        }

        // 请求获取
        AuthUser authUser = getAuthUser(type, buildAuthCallback(code, state));
        if (authUser == null) {
            throw exception(SOCIAL_USER_NOT_FOUND);
        }

        // 保存到 DB 中
        socialUser = socialUserMapper.selectByTypeAndOpenid(type, authUser.getUuid());
        if (socialUser == null) {
            socialUser = new SocialUserDO();
        }
        socialUser.setOpenid(authUser.getUuid()).setToken(authUser.getToken().getAccessToken()).setRawTokenInfo((toJsonString(authUser.getToken())))
                .setUnionId(StrUtil.blankToDefault(authUser.getToken().getUnionId(), authUser.getUuid())) // unionId 识别多个用户
                .setNickname(authUser.getNickname()).setAvatar(authUser.getAvatar()).setRawUserInfo(toJsonString(authUser.getRawUserInfo()))
                .setCode(code).setState(state); // 需要保存 code + state 字段，保证后续可查询
        if (socialUser.getId() == null) {
            socialUserMapper.insert(socialUser);
        } else {
            socialUserMapper.updateById(socialUser);
        }
        return socialUser;
    }

    @Override
    public List<SocialUserDO> getSocialUserList(Long userId, Integer userType) {
        return socialUserMapper.selectListByUserId(userType, userId);
    }

    @Override
    @Transactional
    public void bindSocialUser(SocialUserBindReqDTO reqDTO) {
        // 获得社交用户
        SocialUserDO socialUser = authSocialUser(reqDTO.getType(), reqDTO.getCode(), reqDTO.getState());
        Assert.notNull(socialUser, "社交用户不能为空");

        // 如果 userId 之前绑定过该 type 的其它账号，需要进行解绑
        socialUserBindMapper.deleteByUserTypeAndUserIdAndPlatformAndUnionId(reqDTO.getUserType(), reqDTO.getUserId(),
                SocialTypeEnum.valueOfType(socialUser.getType()).getPlatform(), socialUser.getUnionId());

        // 绑定当前登录的社交用户
        SocialUserBindDO socialUserBind = SocialUserBindDO.builder().userId(reqDTO.getUserId()).userType(reqDTO.getUserType())
                .unionId(socialUser.getUnionId()).build();
        socialUserBindMapper.insert(socialUserBind);
    }

    @Override
    public void unbindSocialUser(Long userId, Integer userType, Integer type, String openid) {
        // 获得 openid 对应的 SocialUserDO 社交用户
        SocialUserDO socialUser = socialUserMapper.selectByTypeAndOpenid(type, openid);
        if (socialUser == null) {
            throw exception(SOCIAL_USER_NOT_FOUND);
        }

        // 获得对应的社交绑定关系
        socialUserBindMapper.deleteByUserTypeAndUserIdAndPlatformAndUnionId(userType, userId,
                SocialTypeEnum.valueOfType(socialUser.getType()).getPlatform(), socialUser.getUnionId());
    }

    @Override
    public Long getBindUserId(Integer userType, Integer type, String code, String state) {
        // 获得社交用户
        SocialUserDO socialUser = authSocialUser(type, code, state);
        Assert.notNull(socialUser, "社交用户不能为空");

        // 如果未绑定的社交用户，则无法自动登录，进行报错
        SocialUserBindDO socialUserBind = socialUserBindMapper.selectByUserTypeAndPlatformAndUnionId(userType,
                SocialTypeEnum.valueOfType(socialUser.getType()).getPlatform(), socialUser.getUnionId());
        if (socialUserBind == null) {
            throw exception(AUTH_THIRD_LOGIN_NOT_BIND);
        }
        return socialUserBind.getUserId();
    }

    /**
     * 请求社交平台，获得授权的用户
     *
     * @param type 社交平台的类型
     * @param authCallback 授权回调
     * @return 授权的用户
     */
    private AuthUser getAuthUser(Integer type, AuthCallback authCallback) {
        AuthRequest authRequest = authRequestFactory.get(SocialTypeEnum.valueOfType(type).getSource());
        AuthResponse<?> authResponse = authRequest.login(authCallback);
        log.info("[getAuthUser0][请求社交平台 type({}) request({}) response({})]", type, toJsonString(authCallback),
                toJsonString(authResponse));
        if (!authResponse.ok()) {
            throw exception(SOCIAL_USER_AUTH_FAILURE, authResponse.getMsg());
        }
        return (AuthUser) authResponse.getData();
    }

    private static AuthCallback buildAuthCallback(String code, String state) {
        return AuthCallback.builder().code(code).state(state).build();
    }

}
