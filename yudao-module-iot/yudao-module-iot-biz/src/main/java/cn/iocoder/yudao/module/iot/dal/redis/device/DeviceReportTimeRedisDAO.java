package cn.iocoder.yudao.module.iot.dal.redis.device;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
