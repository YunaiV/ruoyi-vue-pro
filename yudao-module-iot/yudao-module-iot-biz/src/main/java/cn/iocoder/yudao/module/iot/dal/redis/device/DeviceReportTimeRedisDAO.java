package cn.iocoder.yudao.module.iot.dal.redis.device;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

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

    public void update(String productKey, String deviceName, LocalDateTime reportTime) {
        String value = productKey + StrUtil.COMMA + deviceName; // 使用 , 分隔
        stringRedisTemplate.opsForZSet().add(RedisKeyConstants.DEVICE_REPORT_TIMES, value,
                LocalDateTimeUtil.toEpochMilli(reportTime));
    }

    public Set<String[]> range(LocalDateTime maxReportTime) {
        Set<String> values = stringRedisTemplate.opsForZSet().rangeByScore(RedisKeyConstants.DEVICE_REPORT_TIMES, 0,
                LocalDateTimeUtil.toEpochMilli(maxReportTime));
        return CollectionUtils.convertSet(values,
                value -> value.split(StrUtil.COMMA)); // 使用, 分隔
    }

}
