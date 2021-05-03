package cn.iocoder.yudao.framework.idempotent.core.redis;

import cn.iocoder.yudao.framework.redis.core.RedisKeyDefine;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.redis.core.RedisKeyDefine.KeyTypeEnum.STRING;

/**
 * 幂等 Redis DAO
 *
 * @author 芋道源码
 */
@AllArgsConstructor
public class IdempotentRedisDAO {

    private static final RedisKeyDefine IDEMPOTENT = new RedisKeyDefine("幂等操作",
            "idempotent:%s", // 参数为 uuid
            STRING, String.class, RedisKeyDefine.TimeoutTypeEnum.DYNAMIC);

    private final StringRedisTemplate redisTemplate;

    public Boolean setIfAbsent(String key, long timeout, TimeUnit timeUnit) {
        String redisKey = formatKey(key);
        return redisTemplate.opsForValue().setIfAbsent(redisKey, "", timeout, timeUnit);
    }

    private static String formatKey(String key) {
        return String.format(IDEMPOTENT.getKeyTemplate(), key);
    }

}
