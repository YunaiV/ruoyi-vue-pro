package cn.iocoder.yudao.module.system.dal.redis.sms;

import cn.hutool.core.date.DateUtil;
import javax.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;

import static cn.hutool.core.date.DatePattern.PURE_DATE_PATTERN;
import static cn.iocoder.yudao.module.system.dal.redis.RedisKeyConstants.SMS_CODE_SEND_IP_COUNT;
import static cn.iocoder.yudao.module.system.dal.redis.RedisKeyConstants.SMS_CODE_SEND_IP_HOUR_COUNT;
import static cn.iocoder.yudao.module.system.dal.redis.RedisKeyConstants.SMS_CODE_VALIDATE_COUNT;

/**
 * 短信验证码的 RedisDAO
 *
 * 1. 校验次数：按验证码记录计数，每次校验先预占一次，校验正确时归还、错误时保留，达到上限后该验证码作废；
 *    重新发送会生成新的验证码记录，因此重新计数
 * 2. IP 发送数量：按自然日、自然小时跨手机号累计，达到任一上限后拒绝发送
 *
 * 次数通过 Lua 脚本原子预占，并发请求不会超过上限。
 *
 * @author 芋道源码
 */
@Repository
public class SmsCodeRedisDAO {

    /**
     * IP 发送数量的有效期，覆盖当日剩余时间即可
     */
    private static final Duration SEND_IP_COUNT_TIMEOUT = Duration.ofDays(1);
    /**
     * IP 每小时发送数量的有效期，覆盖当前小时剩余时间即可
     */
    private static final Duration SEND_IP_HOUR_COUNT_TIMEOUT = Duration.ofHours(1);
    /**
     * IP 每小时发送数量的时间格式，按自然小时分桶
     */
    private static final String SEND_IP_HOUR_PATTERN = "yyyyMMddHH";

    // DONE @AI：评估过 Redisson，不简化。RRateLimiter 是令牌桶，不能归还许可（正确校验不消耗次数），
    // 也无法对齐自然日、自然小时，且每次需读取或设置速率配置，状态和请求更多；RAtomicLong 的自增、设置有效期、
    // 超限回滚、归还是多次独立请求，不是原子操作，归还时还可能把已过期的 Key 以无有效期的方式重新创建。
    // 保留下面两段简短的 Lua 脚本，一次请求原子完成预占或归还
    /**
     * 预占次数的脚本：ARGV 依次为上限、有效期秒数；首次计数时设置有效期，超过上限时回滚并返回 0
     */
    private static final RedisScript<Long> RESERVE_SCRIPT = new DefaultRedisScript<>(
            "local count = redis.call('INCR', KEYS[1])\n"
                    + "if count == 1 then\n"
                    + "    redis.call('EXPIRE', KEYS[1], ARGV[2])\n"
                    + "end\n"
                    + "if count > tonumber(ARGV[1]) then\n"
                    + "    redis.call('DECR', KEYS[1])\n"
                    + "    return 0\n"
                    + "end\n"
                    + "return 1\n", Long.class);
    /**
     * 归还次数的脚本：计数大于 0 时扣减，保留原有效期；已过期或不存在时不处理
     */
    private static final RedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
            "if tonumber(redis.call('GET', KEYS[1]) or '0') > 0 then\n"
                    + "    return redis.call('DECR', KEYS[1])\n"
                    + "end\n"
                    + "return 0\n", Long.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 预占验证码的一次校验次数
     *
     * @param id 验证码编号
     * @param limit 最大校验错误次数
     * @param timeout 计数有效期，与验证码有效期一致
     * @return 是否预占成功；已达上限时返回 false
     */
    public boolean tryReserveValidateCount(Long id, int limit, Duration timeout) {
        return tryReserve(String.format(SMS_CODE_VALIDATE_COUNT, id), limit, timeout);
    }

    /**
     * 归还验证码已预占的校验次数
     *
     * @param id 验证码编号
     */
    public void releaseValidateCount(Long id) {
        release(String.format(SMS_CODE_VALIDATE_COUNT, id));
    }

    /**
     * 预占 IP 当日的一次发送数量
     *
     * @param ip 发送 IP
     * @param time 发送时间，决定所属自然日
     * @param limit 每日发送最大数量
     * @return 是否预占成功；已达上限时返回 false
     */
    public boolean tryReserveSendIpDayCount(String ip, LocalDateTime time, int limit) {
        return tryReserve(formatSendIpDayKey(ip, time), limit, SEND_IP_COUNT_TIMEOUT);
    }

    /**
     * 归还 IP 当日已预占的一次发送数量
     *
     * @param ip 发送 IP
     * @param time 预占时使用的发送时间
     */
    public void releaseSendIpDayCount(String ip, LocalDateTime time) {
        release(formatSendIpDayKey(ip, time));
    }

    /**
     * 预占 IP 当前小时的一次发送数量
     *
     * @param ip 发送 IP
     * @param time 发送时间，决定所属自然小时
     * @param limit 每小时发送最大数量
     * @return 是否预占成功；已达上限时返回 false
     */
    public boolean tryReserveSendIpHourCount(String ip, LocalDateTime time, int limit) {
        String redisKey = String.format(SMS_CODE_SEND_IP_HOUR_COUNT, DateUtil.format(time, SEND_IP_HOUR_PATTERN), ip);
        return tryReserve(redisKey, limit, SEND_IP_HOUR_COUNT_TIMEOUT);
    }

    private static String formatSendIpDayKey(String ip, LocalDateTime time) {
        return String.format(SMS_CODE_SEND_IP_COUNT, DateUtil.format(time, PURE_DATE_PATTERN), ip);
    }

    private boolean tryReserve(String redisKey, int limit, Duration timeout) {
        Long result = stringRedisTemplate.execute(RESERVE_SCRIPT, Collections.singletonList(redisKey),
                String.valueOf(limit), String.valueOf(timeout.getSeconds()));
        return result != null && result == 1;
    }

    private void release(String redisKey) {
        stringRedisTemplate.execute(RELEASE_SCRIPT, Collections.singletonList(redisKey));
    }

}
