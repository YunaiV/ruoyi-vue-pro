package cn.iocoder.yudao.module.trade.dal.redis.no;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.module.trade.dal.redis.RedisKeyConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 订单序号的 Redis DAO
 *
 * @author HUIHUI
 */
@Repository
public class TradeNoRedisDAO {

    public static final String TRADE_ORDER_NO_PREFIX = "o";

    public static final String AFTER_SALE_NO_PREFIX = "r";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成序号
     *
     * @param prefix 前缀
     * @return 序号
     */
    public String generate(String prefix) {
        // 递增序号
        String noPrefix = prefix + DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATETIME_PATTERN);
        String key = RedisKeyConstants.TRADE_NO + noPrefix;
        Long no = stringRedisTemplate.opsForValue().increment(key);
        // 设置过期时间
        stringRedisTemplate.expire(key, Duration.ofMinutes(1L));
        return noPrefix + no;
    }

}
