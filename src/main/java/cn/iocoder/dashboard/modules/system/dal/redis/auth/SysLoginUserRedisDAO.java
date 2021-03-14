package cn.iocoder.dashboard.modules.system.dal.redis.auth;

import cn.iocoder.dashboard.framework.security.core.LoginUser;
import cn.iocoder.dashboard.modules.system.service.auth.SysUserSessionService;
import cn.iocoder.dashboard.util.json.JsonUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.Duration;

import static cn.iocoder.dashboard.modules.system.dal.redis.SysRedisKeyConstants.LOGIN_USER;

/**
 * {@link LoginUser} 的 RedisDAO
 *
 * @author 芋道源码
 */
@Repository
public class SysLoginUserRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SysUserSessionService sysUserSessionService;

    public LoginUser get(String sessionId) {
        String redisKey = formatKey(sessionId);
        return JsonUtils.parseObject(stringRedisTemplate.opsForValue().get(redisKey), LoginUser.class);
    }

    public void set(String sessionId, LoginUser loginUser) {
        String redisKey = formatKey(sessionId);
        stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJsonString(loginUser),
                Duration.ofMillis(sysUserSessionService.getSessionTimeoutMillis()));
    }

    public void delete(String sessionId) {
        String redisKey = formatKey(sessionId);
        stringRedisTemplate.delete(redisKey);
    }

    private static String formatKey(String sessionId) {
        return String.format(LOGIN_USER.getKeyTemplate(), sessionId);
    }

}
