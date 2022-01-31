package cn.iocoder.yudao.module.system.service.social;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserBindReqDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.social.SocialUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.social.SocialUserMapper;
import cn.iocoder.yudao.module.system.dal.redis.social.SocialAuthUserRedisDAO;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import com.google.common.annotations.VisibleForTesting;
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
import java.util.Objects;

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
    private SocialAuthUserRedisDAO authSocialUserRedisDAO;

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
    public AuthUser getAuthUser(Integer type, String code, String state) {
        AuthCallback authCallback = buildAuthCallback(code, state);
        // 从缓存中获取
        AuthUser authUser = authSocialUserRedisDAO.get(type, authCallback);
        if (authUser != null) {
            return authUser;
        }

        // 请求获取
        authUser = this.getAuthUser0(type, authCallback);
        // 缓存。原因是 code 有且可以使用一次。在社交登录时，当未绑定 User 时，需要绑定登录，此时需要 code 使用两次
        authSocialUserRedisDAO.set(type, authCallback, authUser);
        return authUser;
    }

    /**
     * 获得 unionId 对应的某个社交平台的“所有”社交用户
     * 注意，这里的“所有”，指的是类似【微信】平台，包括了小程序、公众号、PC 网站，他们的 unionId 是一致的
     *
     * @param type 社交平台的类型 {@link SocialTypeEnum}
     * @param unionId 社交平台的 unionId
     * @param userType 全局用户类型
     * @return 社交用户列表
     */
    private List<SocialUserDO> getAllSocialUserList(Integer type, String unionId, Integer userType) {
        List<Integer> types = SocialTypeEnum.getRelationTypes(type);
        return socialUserMapper.selectListByTypeAndUnionId(userType, types, unionId);
    }

    @Override
    public List<SocialUserDO> getSocialUserList(Long userId, Integer userType) {
        return socialUserMapper.selectListByUserId(userType, userId);
    }

    @Override
    public void bindSocialUser(SocialUserBindReqDTO reqDTO) {
        // 使用 code 授权
        AuthUser authUser = getAuthUser(reqDTO.getType(), reqDTO.getCode(),
                reqDTO.getState());
        if (authUser == null) {
            throw exception(SOCIAL_USER_NOT_FOUND);
        }

        // 绑定社交用户（新增）
        bindSocialUser(reqDTO.getUserId(), reqDTO.getUserType(),
                reqDTO.getType(), authUser);
    }

    /**
     * 绑定社交用户
     *  @param userId 用户编号
     * @param userType 用户类型
     * @param type 社交平台的类型 {@link SocialTypeEnum}
     * @param authUser 授权用户
     */
    @Transactional(rollbackFor = Exception.class)
    protected void bindSocialUser(Long userId, Integer userType, Integer type, AuthUser authUser) {
        // 获得 unionId 对应的 SocialUserDO 列表
        String unionId = getAuthUserUnionId(authUser);
        List<SocialUserDO> socialUsers = this.getAllSocialUserList(type, unionId, userType);

        // 逻辑一：如果 userId 之前绑定过该 type 的其它账号，需要进行解绑
        this.unbindOldSocialUser(userId, userType, type, unionId);

        // 逻辑二：如果 socialUsers 指定的 userId 改变，需要进行更新
        // 例如说，一个微信 unionId 对应了多个社交账号，结果其中有个关联了新的 userId，则其它也要跟着修改
        // 考虑到 socialUsers 一般比较少，直接 for 循环更新即可
        socialUsers.forEach(socialUser -> {
            if (Objects.equals(socialUser.getUserId(), userId)) {
                return;
            }
            socialUserMapper.updateById(new SocialUserDO().setId(socialUser.getId()).setUserId(userId));
        });

        // 逻辑三：如果 authUser 不存在于 socialUsers 中，则进行新增；否则，进行更新
        SocialUserDO socialUser = CollUtil.findOneByField(socialUsers, "openid", authUser.getUuid());
        SocialUserDO saveSocialUser = SocialUserDO.builder() // 新增和更新的通用属性
                .token(authUser.getToken().getAccessToken()).rawTokenInfo(toJsonString(authUser.getToken()))
                .nickname(authUser.getNickname()).avatar(authUser.getAvatar()).rawUserInfo(toJsonString(authUser.getRawUserInfo()))
                .build();
        if (socialUser == null) {
            saveSocialUser.setUserId(userId).setUserType(userType)
                    .setType(type).setOpenid(authUser.getUuid()).setUnionId(unionId);
            socialUserMapper.insert(saveSocialUser);
        } else {
            saveSocialUser.setId(socialUser.getId());
            socialUserMapper.updateById(saveSocialUser);
        }
    }

    @Override
    public void unbindSocialUser(Long userId, Integer userType, Integer type, String unionId) {
        // 获得 unionId 对应的所有 SocialUserDO 社交用户
        List<SocialUserDO> socialUsers = this.getAllSocialUserList(type, unionId, userType);
        if (CollUtil.isEmpty(socialUsers)) {
            return;
        }
        // 校验，是否解绑的是非自己的
        socialUsers.forEach(socialUser -> {
            if (!Objects.equals(socialUser.getUserId(), userId)) {
                throw exception(SOCIAL_USER_UNBIND_NOT_SELF);
            }
        });

        // 解绑
        socialUserMapper.deleteBatchIds(CollectionUtils.convertSet(socialUsers, SocialUserDO::getId));
    }

    @Override
    public void checkSocialUser(Integer type, String code, String state) {
        AuthUser authUser = getAuthUser(type, code, state);
        if (authUser == null) {
            throw exception(SOCIAL_USER_NOT_FOUND);
        }
    }

    @Override
    public Long getBindUserId(Integer userType, Integer type, String code, String state) {
        AuthUser authUser = getAuthUser(type, code, state);
        if (authUser == null) {
            throw exception(SOCIAL_USER_NOT_FOUND);
        }

        // 如果未绑定 SocialUserDO 用户，则无法自动登录，进行报错
        String unionId = getAuthUserUnionId(authUser);
        List<SocialUserDO> socialUsers = getAllSocialUserList(type, unionId, userType);
        if (CollUtil.isEmpty(socialUsers)) {
            throw exception(AUTH_THIRD_LOGIN_NOT_BIND);
        }
        return socialUsers.get(0).getUserId();
    }

    @VisibleForTesting
    public void unbindOldSocialUser(Long userId, Integer userType, Integer type, String newUnionId) {
        List<Integer> types = SocialTypeEnum.getRelationTypes(type);
        List<SocialUserDO> oldSocialUsers = socialUserMapper.selectListByTypeAndUserId(userType, types, userId);
        // 如果新老的 unionId 是一致的，说明无需解绑
        if (CollUtil.isEmpty(oldSocialUsers) || Objects.equals(newUnionId, oldSocialUsers.get(0).getUnionId())) {
            return;
        }

        // 解绑
        socialUserMapper.deleteBatchIds(CollectionUtils.convertSet(oldSocialUsers, SocialUserDO::getId));
    }

    /**
     * 请求社交平台，获得授权的用户
     *
     * @param type 社交平台的类型
     * @param authCallback 授权回调
     * @return 授权的用户
     */
    private AuthUser getAuthUser0(Integer type, AuthCallback authCallback) {
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
