package cn.iocoder.yudao.module.pay.dal.redis.no;

import cn.hutool.core.date.DatePattern;import cn.hutool.core.date.DateUtil;import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;import java.time.LocalDateTime;

/**
 * 支付序号的 Redis DAO
 *
 * @author 芋道源码
 */
@Repository
public class PayNoRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成序号
     *
     * @param prefix 前缀
     * @return 序号
     */
    public String generate(String prefix) {
        String noPrefix = prefix + DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATETIME_PATTERN);
        Long no = stringRedisTemplate.opsForValue().increment(noPrefix);
        return noPrefix + no;
    }

}
