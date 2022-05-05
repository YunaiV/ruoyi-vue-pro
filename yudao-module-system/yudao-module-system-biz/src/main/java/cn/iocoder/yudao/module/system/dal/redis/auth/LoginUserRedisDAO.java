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

    public LoginUser get(String token) {
        String redisKey = formatKey(token);
        return JsonUtils.parseObject(stringRedisTemplate.opsForValue().get(redisKey), LoginUser.class);
    }

    public Boolean exists(String token) {
        String redisKey = formatKey(token);
        return stringRedisTemplate.hasKey(redisKey);
    }

    public void set(String token, LoginUser loginUser) {
        String redisKey = formatKey(token);
        stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJsonString(loginUser),
                securityProperties.getSessionTimeout());
    }

    public void delete(String token) {
        String redisKey = formatKey(token);
        stringRedisTemplate.delete(redisKey);
    }

    private static String formatKey(String token) {
        return LOGIN_USER.formatKey(token);
    }

}
