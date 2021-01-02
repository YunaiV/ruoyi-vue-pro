package cn.iocoder.dashboard.modules.system.dal.redis.dao.auth;

import cn.iocoder.dashboard.framework.security.core.LoginUser;
import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

import static cn.iocoder.dashboard.modules.system.dal.redis.RedisKeyConstants.LOGIN_USER;

/**
 * {@link LoginUser} 的 RedisDAO
 *
 * @author 芋道源码
 */
@Repository
public class SysLoginUserRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public LoginUser get(String sessionId) {
        String redisKey = formatKey(sessionId);
        return JSON.parseObject(stringRedisTemplate.opsForValue().get(redisKey), LoginUser.class);
    }

    public void set(String sessionId, LoginUser loginUser) {
        String redisKey = formatKey(sessionId);
        stringRedisTemplate.opsForValue().set(redisKey, JSON.toJSONString(loginUser), LOGIN_USER.getTimeout());
    }

    public void delete(String accessToken) {
        String redisKey = formatKey(accessToken);
        stringRedisTemplate.delete(redisKey);
    }

    private static String formatKey(String sessionId) {
        return String.format(LOGIN_USER.getKeyTemplate(), sessionId);
    }

}
