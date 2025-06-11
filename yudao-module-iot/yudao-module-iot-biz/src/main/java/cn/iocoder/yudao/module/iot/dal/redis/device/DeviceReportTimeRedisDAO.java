package cn.iocoder.yudao.module.iot.dal.redis.device;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * 设备的最后上报时间的 Redis DAO
 *
 * @author 芋道源码
 */
@Repository
public class DeviceReportTimeRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void update(Long deviceId, LocalDateTime reportTime) {
        stringRedisTemplate.opsForZSet().add(RedisKeyConstants.DEVICE_REPORT_TIMES, String.valueOf(deviceId),
                LocalDateTimeUtil.toEpochMilli(reportTime));
    }

    public Set<Long> range(LocalDateTime maxReportTime) {
        Set<String> values = stringRedisTemplate.opsForZSet().rangeByScore(RedisKeyConstants.DEVICE_REPORT_TIMES,
                0, LocalDateTimeUtil.toEpochMilli(maxReportTime));
        return convertSet(values, Long::parseLong);
    }

}
