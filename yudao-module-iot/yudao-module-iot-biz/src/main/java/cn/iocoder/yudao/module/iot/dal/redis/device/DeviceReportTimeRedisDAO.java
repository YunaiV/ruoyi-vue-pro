package cn.iocoder.yudao.module.iot.dal.redis.device;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

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
        stringRedisTemplate.opsForZSet().add(RedisKeyConstants.DEVICE_REPORT_TIME, deviceKey,
                LocalDateTimeUtil.toEpochMilli(reportTime));
    }

}
