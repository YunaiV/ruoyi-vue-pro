package cn.iocoder.yudao.module.iot.service.device.property;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotDeviceMessageMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * IoT 设备日志数据 Service 实现类
 *
 * @author alwayssuper
 */
@Service
@Slf4j
@Validated
public class IotDeviceLogServiceImpl implements IotDeviceLogService {

    @Resource
    private IotDeviceMessageMapper deviceLogMapper;

    @Override
    public Long getDeviceLogCount(LocalDateTime createTime) {
        return deviceLogMapper.selectCountByCreateTime(createTime != null ? LocalDateTimeUtil.toEpochMilli(createTime) : null);
    }

    // TODO @super：加一个参数，Boolean upstream：true 上行，false 下行，null 不过滤
    @Override
    public List<Map<Long, Integer>> getDeviceLogUpCountByHour(String deviceKey, Long startTime, Long endTime) {
        // TODO @super：不能只基于数据库统计。因为有一些小时，可能出现没数据的情况，导致前端展示的图是不全的。可以参考 CrmStatisticsCustomerService 来实现
        // TODO @芋艿：这里实现，需要调整
        List<Map<String, Object>> list = deviceLogMapper.selectDeviceLogUpCountByHour(0L, startTime, endTime);
        return list.stream()
                .map(map -> {
                    // 从Timestamp获取时间戳
                    Timestamp timestamp = (Timestamp) map.get("time");
                    Long timeMillis = timestamp.getTime();
                    // 消息数量转换
                    Integer count = ((Number) map.get("data")).intValue();
                    return MapUtil.of(timeMillis, count);
                })
                .collect(Collectors.toList());
    }

    // TODO @super：getDeviceLogDownCountByHour 融合到 getDeviceLogUpCountByHour
    @Override
    public List<Map<Long, Integer>> getDeviceLogDownCountByHour(String deviceKey, Long startTime, Long endTime) {
        // TODO @芋艿：这里实现，需要调整
        List<Map<String, Object>> list = deviceLogMapper.selectDeviceLogDownCountByHour(0L, startTime, endTime);
        return list.stream()
                .map(map -> {
                    // 从Timestamp获取时间戳
                    Timestamp timestamp = (Timestamp) map.get("time");
                    Long timeMillis = timestamp.getTime();
                    // 消息数量转换
                    Integer count = ((Number) map.get("data")).intValue();
                    return MapUtil.of(timeMillis, count);
                })
                .collect(Collectors.toList());
    }

}
