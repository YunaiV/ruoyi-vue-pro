package cn.iocoder.yudao.adminserver.modules.system.dal.redis.auth;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

import static cn.iocoder.yudao.adminserver.modules.system.dal.redis.SysRedisKeyConstants.AUTH_SOCIAL_USER;

/**
 * 社交 {@link me.zhyd.oauth.model.AuthUser} 的 RedisDAO
 *
 * @author 芋道源码
 */
@Repository
public class SysAuthSocialUserRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public AuthUser get(Integer type, AuthCallback authCallback) {
        String redisKey = formatKey(type, authCallback.getCode());
        return JsonUtils.parseObject(stringRedisTemplate.opsForValue().get(redisKey), AuthUser.class);
    }

    public void set(Integer type, AuthCallback authCallback, AuthUser authUser) {
        String redisKey = formatKey(type, authCallback.getCode());
        stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJsonString(authUser), AUTH_SOCIAL_USER.getTimeout());
    }

    private static String formatKey(Integer type, String code) {
        return String.format(AUTH_SOCIAL_USER.getKeyTemplate(), type, code);
    }

}
