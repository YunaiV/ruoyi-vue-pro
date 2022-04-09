package cn.iocoder.yudao.module.system.dal.redis.auth;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.security.config.SecurityProperties;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

import static cn.iocoder.yudao.module.system.dal.redis.RedisKeyConstants.LOGIN_USER;

/**
 * {@link LoginUser} 的 RedisDAO
 *
 * @author 芋道源码
 */
@Repository
public class LoginUserRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SecurityProperties securityProperties;

    public LoginUser get(String sessionId) {
        String redisKey = formatKey(sessionId);
        return JsonUtils.parseObject(stringRedisTemplate.opsForValue().get(redisKey), LoginUser.class);
    }

    public void set(String sessionId, LoginUser loginUser) {
        String redisKey = formatKey(sessionId);
        stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJsonString(loginUser),
                securityProperties.getSessionTimeout());
    }

    public void delete(String sessionId) {
        String redisKey = formatKey(sessionId);
        stringRedisTemplate.delete(redisKey);
    }

    private static String formatKey(String sessionId) {
        return LOGIN_USER.formatKey(sessionId);
    }

}
