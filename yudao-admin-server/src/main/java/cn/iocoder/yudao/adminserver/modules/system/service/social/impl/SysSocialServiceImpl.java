package cn.iocoder.yudao.adminserver.modules.system.service.social.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.social.SysSocialUserDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.mysql.social.SysSocialUserMapper;
import cn.iocoder.yudao.adminserver.modules.system.dal.redis.social.SysSocialAuthUserRedisDAO;
import cn.iocoder.yudao.adminserver.modules.system.enums.user.SysSocialTypeEnum;
import cn.iocoder.yudao.adminserver.modules.system.service.social.SysSocialService;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.xkcoding.justauth.AuthRequestFactory;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.SOCIAL_AUTH_FAILURE;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 社交 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Valid
@Slf4j
public class SysSocialServiceImpl implements SysSocialService {

    @Resource
    private AuthRequestFactory authRequestFactory;

    @Resource
    private SysSocialAuthUserRedisDAO authSocialUserRedisDAO;

    @Resource
    private SysSocialUserMapper socialUserMapper;

    @Override
    public String getAuthorizeUrl(Integer type, String redirectUri) {
        // 获得对应的 AuthRequest 实现
        AuthRequest authRequest = authRequestFactory.get(SysSocialTypeEnum.valueOfType(type).getSource());
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
        // 缓存。原因是 code 有且可以使用一次。在社交登录时，当未绑定 User 时，需要绑定登陆，此时需要 code 使用两次
        authSocialUserRedisDAO.set(type, authCallback, authUser);
        return authUser;
    }

    @Override
    public List<SysSocialUserDO> getAllSocialUserList(Integer type, String unionId) {
        List<Integer> types = SysSocialTypeEnum.getRelationTypes(type);
        return socialUserMapper.selectListByTypeAndUnionId(UserTypeEnum.ADMIN.getValue(), types, unionId);
    }

    @Override
    public void bindSocialUser(Long userId, Integer type, AuthUser authUser) {
        // 获得 unionId 对应的 SysSocialUserDO 列表
        String unionId = getAuthUserUnionId(authUser);
        List<SysSocialUserDO> socialUsers = this.getAllSocialUserList(type, unionId);

        // 逻辑一：如果 userId 之前绑定过该 type 的其它账号，需要进行解绑
        List<Integer> types = SysSocialTypeEnum.getRelationTypes(type);
        List<SysSocialUserDO> oldSocialUsers = socialUserMapper.selectListByTypeAndUserId(UserTypeEnum.ADMIN.getValue(),
                types, userId);
        if (CollUtil.isNotEmpty(oldSocialUsers) && !Objects.equals(unionId, oldSocialUsers.get(0).getUnionId())) {
            socialUserMapper.deleteBatchIds(CollectionUtils.convertSet(oldSocialUsers, SysSocialUserDO::getId));
        }

        // 逻辑二：如果 socialUsers 指定的 userId 改变，需要进行更新
        // 例如说，一个微信 unionId 对应了多个社交账号，结果其中有个关联了新的 userId，则其它也要跟着修改
        // 考虑到 socialUsers 一般比较少，直接 for 循环更新即可
        socialUsers.forEach(socialUser -> {
            if (Objects.equals(socialUser.getUserId(), userId)) {
                return;
            }
            socialUserMapper.updateById(new SysSocialUserDO().setUserId(socialUser.getUserId()).setUserId(userId));
        });

        // 逻辑三：如果 authUser 不存在于 socialUsers 中，则进行新增；否则，进行更新
        SysSocialUserDO saveSocialUser = CollUtil.findOneByField(socialUsers, "openid", authUser.getUuid());
        if (saveSocialUser == null) {
            saveSocialUser = new SysSocialUserDO();
            saveSocialUser.setUserId(userId).setUserType(UserTypeEnum.ADMIN.getValue());
            saveSocialUser.setType(type).setOpenid(authUser.getUuid()).setToken(authUser.getToken().getAccessToken())
                    .setUnionId(unionId).setRawTokenInfo(JsonUtils.toJsonString(authUser.getToken()));
            saveSocialUser.setNickname(authUser.getNickname()).setAvatar(authUser.getAvatar())
                    .setRawUserInfo(JsonUtils.toJsonString(authUser.getRawUserInfo()));
            socialUserMapper.insert(saveSocialUser);
        } else {
            saveSocialUser = new SysSocialUserDO().setId(saveSocialUser.getId());
            saveSocialUser.setToken(authUser.getToken().getAccessToken())
                    .setRawTokenInfo(JsonUtils.toJsonString(authUser.getToken()));
            saveSocialUser.setNickname(authUser.getNickname()).setAvatar(authUser.getAvatar())
                    .setRawUserInfo(JsonUtils.toJsonString(authUser.getRawUserInfo()));
            socialUserMapper.updateById(saveSocialUser);
        }
    }

    /**
     * 请求社交平台，获得授权的用户
     *
     * @param type 社交平台的类型
     * @param authCallback 授权回调
     * @return 授权的用户
     */
    private AuthUser getAuthUser0(Integer type, AuthCallback authCallback) {
        AuthRequest authRequest = authRequestFactory.get(SysSocialTypeEnum.valueOfType(type).getSource());
        AuthResponse<?> authResponse = authRequest.login(authCallback);
        log.info("[getAuthUser0][请求社交平台 type({}) request({}) response({})]", type, JsonUtils.toJsonString(authCallback),
                JsonUtils.toJsonString(authResponse));
        if (!authResponse.ok()) {
            throw exception(SOCIAL_AUTH_FAILURE, authResponse.getMsg());
        }
        return (AuthUser) authResponse.getData();
    }

    private static AuthCallback buildAuthCallback(String code, String state) {
        return AuthCallback.builder().code(code).state(state).build();
    }

}
