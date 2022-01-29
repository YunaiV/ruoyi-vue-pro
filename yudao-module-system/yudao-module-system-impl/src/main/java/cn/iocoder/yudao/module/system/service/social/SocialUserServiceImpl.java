package cn.iocoder.yudao.module.system.service.social;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.system.dal.dataobject.social.SysSocialUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.social.SysSocialUserMapper;
import cn.iocoder.yudao.module.system.dal.redis.social.SocialAuthUserRedisDAO;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
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

import static cn.iocoder.yudao.module.system.enums.SysErrorCodeConstants.SOCIAL_AUTH_FAILURE;
import static cn.iocoder.yudao.module.system.enums.SysErrorCodeConstants.SOCIAL_UNBIND_NOT_SELF;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;

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
    private SysSocialUserMapper socialUserMapper;

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

    @Override
    public List<SysSocialUserDO> getAllSocialUserList(Integer type, String unionId,UserTypeEnum userTypeEnum) {
        List<Integer> types = SocialTypeEnum.getRelationTypes(type);
        return socialUserMapper.selectListByTypeAndUnionId(userTypeEnum.getValue(), types, unionId);
    }

    @Override
    public List<SysSocialUserDO> getSocialUserList(Long userId,UserTypeEnum userTypeEnum) {
        return socialUserMapper.selectListByUserId(userTypeEnum.getValue(), userId);
    }

    @Override
    @Transactional
    public void bindSocialUser(Long userId, Integer type, AuthUser authUser, UserTypeEnum userTypeEnum) {
        // 获得 unionId 对应的 SysSocialUserDO 列表
        String unionId = getAuthUserUnionId(authUser);
        List<SysSocialUserDO> socialUsers = this.getAllSocialUserList(type, unionId, userTypeEnum);

        // 逻辑一：如果 userId 之前绑定过该 type 的其它账号，需要进行解绑
        this.unbindOldSocialUser(userId, type, unionId, userTypeEnum);

        // 逻辑二：如果 socialUsers 指定的 userId 改变，需要进行更新
        // 例如说，一个微信 unionId 对应了多个社交账号，结果其中有个关联了新的 userId，则其它也要跟着修改
        // 考虑到 socialUsers 一般比较少，直接 for 循环更新即可
        socialUsers.forEach(socialUser -> {
            if (Objects.equals(socialUser.getUserId(), userId)) {
                return;
            }
            socialUserMapper.updateById(new SysSocialUserDO().setId(socialUser.getId()).setUserId(userId));
        });

        // 逻辑三：如果 authUser 不存在于 socialUsers 中，则进行新增；否则，进行更新
        SysSocialUserDO socialUser = CollUtil.findOneByField(socialUsers, "openid", authUser.getUuid());
        SysSocialUserDO saveSocialUser = SysSocialUserDO.builder() // 新增和更新的通用属性
                .token(authUser.getToken().getAccessToken()).rawTokenInfo(toJsonString(authUser.getToken()))
                .nickname(authUser.getNickname()).avatar(authUser.getAvatar()).rawUserInfo(toJsonString(authUser.getRawUserInfo()))
                .build();
        if (socialUser == null) {
            saveSocialUser.setUserId(userId).setUserType(userTypeEnum.getValue())
                    .setType(type).setOpenid(authUser.getUuid()).setUnionId(unionId);
            socialUserMapper.insert(saveSocialUser);
        } else {
            saveSocialUser.setId(socialUser.getId());
            socialUserMapper.updateById(saveSocialUser);
        }
    }

    @Override
    public void unbindSocialUser(Long userId, Integer type, String unionId, UserTypeEnum userTypeEnum) {
        // 获得 unionId 对应的所有 SysSocialUserDO 社交用户
        List<SysSocialUserDO> socialUsers = this.getAllSocialUserList(type, unionId, userTypeEnum);
        if (CollUtil.isEmpty(socialUsers)) {
            return;
        }
        // 校验，是否解绑的是非自己的
        socialUsers.forEach(socialUser -> {
            if (!Objects.equals(socialUser.getUserId(), userId)) {
                throw exception(SOCIAL_UNBIND_NOT_SELF);
            }
        });

        // 解绑
        socialUserMapper.deleteBatchIds(CollectionUtils.convertSet(socialUsers, SysSocialUserDO::getId));
    }

    @VisibleForTesting
    public void unbindOldSocialUser(Long userId, Integer type, String newUnionId, UserTypeEnum userTypeEnum) {
        List<Integer> types = SocialTypeEnum.getRelationTypes(type);
        List<SysSocialUserDO> oldSocialUsers = socialUserMapper.selectListByTypeAndUserId(
                userTypeEnum.getValue(), types, userId);
        // 如果新老的 unionId 是一致的，说明无需解绑
        if (CollUtil.isEmpty(oldSocialUsers) || Objects.equals(newUnionId, oldSocialUsers.get(0).getUnionId())) {
            return;
        }

        // 解绑
        socialUserMapper.deleteBatchIds(CollectionUtils.convertSet(oldSocialUsers, SysSocialUserDO::getId));
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
            throw exception(SOCIAL_AUTH_FAILURE, authResponse.getMsg());
        }
        return (AuthUser) authResponse.getData();
    }

    private static AuthCallback buildAuthCallback(String code, String state) {
        return AuthCallback.builder().code(code).state(state).build();
    }

}
