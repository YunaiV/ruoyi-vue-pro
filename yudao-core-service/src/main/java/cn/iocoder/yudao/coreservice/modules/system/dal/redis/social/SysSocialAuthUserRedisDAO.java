package cn.iocoder.yudao.coreservice.modules.system.dal.redis.social;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

import static cn.iocoder.yudao.coreservice.modules.system.dal.redis.SysRedisKeyCoreConstants.SOCIAL_AUTH_USER;

/**
 * 社交 {@link me.zhyd.oauth.model.AuthUser} 的 RedisDAO
 *
 * @author 芋道源码
 */
@Repository
public class SysSocialAuthUserRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public AuthUser get(Integer type, AuthCallback authCallback) {
        String redisKey = formatKey(type, authCallback.getCode());
        return JsonUtils.parseObject(stringRedisTemplate.opsForValue().get(redisKey), AuthUser.class);
    }

    public void set(Integer type, AuthCallback authCallback, AuthUser authUser) {
        String redisKey = formatKey(type, authCallback.getCode());
        stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJsonString(authUser), SOCIAL_AUTH_USER.getTimeout());
    }

    private static String formatKey(Integer type, String code) {
        return String.format(SOCIAL_AUTH_USER.getKeyTemplate(), type, code);
    }

}
