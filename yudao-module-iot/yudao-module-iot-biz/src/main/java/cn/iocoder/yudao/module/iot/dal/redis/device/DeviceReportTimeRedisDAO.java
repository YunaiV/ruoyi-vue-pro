package cn.iocoder.yudao.module.iot.dal.redis.device;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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

    public void update(String deviceKey, LocalDateTime reportTime) {
        stringRedisTemplate.opsForZSet().add(RedisKeyConstants.DEVICE_REPORT_TIMES, deviceKey,
                LocalDateTimeUtil.toEpochMilli(reportTime));
    }

    public Set<String> range(LocalDateTime maxReportTime) {
        return stringRedisTemplate.opsForZSet().rangeByScore(RedisKeyConstants.DEVICE_REPORT_TIMES, 0,
                LocalDateTimeUtil.toEpochMilli(maxReportTime));
    }

}
