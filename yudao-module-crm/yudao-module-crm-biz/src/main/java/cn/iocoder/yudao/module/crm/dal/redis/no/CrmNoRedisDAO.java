package cn.iocoder.yudao.module.crm.dal.redis.no;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.module.crm.dal.redis.RedisKeyConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;


/**
 * Crm 订单序号的 Redis DAO
 *
 * @author HUIHUI
 */
@Repository
public class CrmNoRedisDAO {

    /**
     * 合同 {@link cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO}
     */
    public static final String CONTRACT_NO_PREFIX = "HT";

    /**
     * 回款 {@link cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivablePlanDO}
     */
    public static final String RECEIVABLE_PREFIX = "HK";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成序号，使用当前日期，格式为 {PREFIX} + yyyyMMdd + 6 位自增
     * 例如说：QTRK 202109 000001 （没有中间空格）
     *
     * @param prefix 前缀
     * @return 序号
     */
    public String generate(String prefix) {
        // 递增序号
        String noPrefix = prefix + DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN);
        String key = RedisKeyConstants.NO + noPrefix;
        Long no = stringRedisTemplate.opsForValue().increment(key);
        // 设置过期时间
        stringRedisTemplate.expire(key, Duration.ofDays(1L));
        return noPrefix + String.format("%06d", no);
    }

}
