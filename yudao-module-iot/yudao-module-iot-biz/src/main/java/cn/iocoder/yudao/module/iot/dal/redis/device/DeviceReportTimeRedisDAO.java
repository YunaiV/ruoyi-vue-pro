package cn.iocoder.yudao.module.iot.dal.redis.device;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 设备的最后上报时间的 Redis DAO
 *
 * @author 芋道源码
 */
@Repository
public class DeviceReportTimeRedisDAO {

    /**
     * 只增不减地更新最后上报时间的脚本：ARGV 依次为设备编号、上报时间（毫秒）；不存在、或晚于已记录的时间时更新
     */
    private static final RedisScript<Long> UPDATE_IF_LATER_SCRIPT = new DefaultRedisScript<>(
            "local old = redis.call('ZSCORE', KEYS[1], ARGV[1])\n"
                    + "if old and tonumber(old) >= tonumber(ARGV[2]) then\n"
                    + "    return 0\n"
                    + "end\n"
                    + "redis.call('ZADD', KEYS[1], ARGV[2], ARGV[1])\n"
                    + "return 1\n", Long.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 更新设备的最后上报时间
     *
     * @param deviceId   设备编号
     * @param reportTime 最后上报时间
     */
    public void update(Long deviceId, LocalDateTime reportTime) {
        stringRedisTemplate.opsForZSet().add(RedisKeyConstants.DEVICE_REPORT_TIMES, String.valueOf(deviceId),
                LocalDateTimeUtil.toEpochMilli(reportTime));
    }

    /**
     * 更新设备的最后上报时间：只增不减，早于已记录的时间时不更新
     *
     * @param deviceId   设备编号
     * @param reportTime 最后上报时间
     */
    public void updateIfLater(Long deviceId, LocalDateTime reportTime) {
        stringRedisTemplate.execute(UPDATE_IF_LATER_SCRIPT, Collections.singletonList(RedisKeyConstants.DEVICE_REPORT_TIMES),
                String.valueOf(deviceId), String.valueOf(LocalDateTimeUtil.toEpochMilli(reportTime)));
    }

    /**
     * 获得最后上报时间不晚于指定时间的设备
     *
     * @param maxReportTime 最大的最后上报时间
     * @return 设备编号与最后上报时间的映射
     */
    public Map<Long, LocalDateTime> rangeWithScores(LocalDateTime maxReportTime) {
        Set<ZSetOperations.TypedTuple<String>> tuples = stringRedisTemplate.opsForZSet().rangeByScoreWithScores(
                RedisKeyConstants.DEVICE_REPORT_TIMES, 0, LocalDateTimeUtil.toEpochMilli(maxReportTime));
        Map<Long, LocalDateTime> result = new LinkedHashMap<>();
        if (tuples == null) {
            return result;
        }
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            if (tuple.getValue() == null || tuple.getScore() == null) {
                continue;
            }
            result.put(Long.parseLong(tuple.getValue()), LocalDateTimeUtil.of(tuple.getScore().longValue()));
        }
        return result;
    }

}
