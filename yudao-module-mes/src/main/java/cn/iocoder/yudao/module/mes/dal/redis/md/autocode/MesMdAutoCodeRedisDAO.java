package cn.iocoder.yudao.module.mes.dal.redis.md.autocode;

import cn.iocoder.yudao.module.mes.dal.redis.RedisKeyConstants;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

/**
 * MES 编码规则的 Redis DAO
 *
 * @author 芋道源码
 */
@Repository
public class MesMdAutoCodeRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 递增序号（带初始值和循环）
     *
     * @param keySuffix key 后缀（不包含 prefix）
     * @param duration 过期时间
     * @param step 步长
     * @param start 起始值（仅在 key 不存在时使用）
     * @return 递增后的值
     */
    public Long increment(String keySuffix, Duration duration,  Integer start, Integer step) {
        String key = RedisKeyConstants.AUTO_CODE + keySuffix;

        // 情况一：如果 key 不存在，初始化为 startNo
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, String.valueOf(start));
        if (Boolean.TRUE.equals(success)) {
            // 设置成功，说明是第一次，设置过期时间并返回 startNo
            if (duration != null) {
                stringRedisTemplate.expire(key, duration);
            }
            return start.longValue();
        }

        // 情况二：key 已存在，递增
        Long value = stringRedisTemplate.opsForValue().increment(key, step);
        if (duration != null) {
            stringRedisTemplate.expire(key, duration);
        }
        return value;
    }

}
