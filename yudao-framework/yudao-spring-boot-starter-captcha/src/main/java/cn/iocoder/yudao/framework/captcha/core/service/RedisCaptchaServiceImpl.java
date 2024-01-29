package cn.iocoder.yudao.framework.captcha.core.service;

import com.xingyuv.captcha.service.CaptchaCacheService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 实现验证码的存储
 *
 * @author 星语
 */
@NoArgsConstructor // 保证 aj-captcha 的 SPI 创建
@AllArgsConstructor
public class RedisCaptchaServiceImpl implements CaptchaCacheService {

    @Resource // 保证 aj-captcha 的 SPI 创建时的注入
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String type() {
        return "redis";
    }

    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
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
