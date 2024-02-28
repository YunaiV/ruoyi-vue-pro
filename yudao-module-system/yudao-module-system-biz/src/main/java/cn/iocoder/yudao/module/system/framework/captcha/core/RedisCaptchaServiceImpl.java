package cn.iocoder.yudao.module.system.framework.captcha.core;

import com.xingyuv.captcha.service.CaptchaCacheService;
import lombok.Setter;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 实现验证码的存储
 *
 * @author 星语
 */
@Setter
public class RedisCaptchaServiceImpl implements CaptchaCacheService {

    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String type() {
        return "redis";
    }

    @Override
    public void set(String key, String value, long expiresInSeconds) {
        stringRedisTemplate.opsForValue().set(key, value, expiresInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    @Override
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public Long increment(String key, long val) {
        return stringRedisTemplate.opsForValue().increment(key,val);
    }

}
