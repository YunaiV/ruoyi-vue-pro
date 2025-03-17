package cn.iocoder.yudao.module.iot.service.device.data;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.data.IotDeviceLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceLogDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotDeviceLogMapper;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
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
    private IotDeviceLogMapper deviceLogMapper;

    @Override
    public void defineDeviceLog() {
        if (StrUtil.isNotEmpty(deviceLogMapper.showDeviceLogSTable())) {
            log.info("[defineDeviceLog][设备日志超级表已存在，创建跳过]");
            return;
        }

        log.info("[defineDeviceLog][设备日志超级表不存在，创建开始...]");
        deviceLogMapper.createDeviceLogSTable();
        log.info("[defineDeviceLog][设备日志超级表不存在，创建成功]");
    }

    @Override
    public void createDeviceLog(IotDeviceMessage message) {
        IotDeviceLogDO log = BeanUtils.toBean(message, IotDeviceLogDO.class)
                .setId(IdUtil.fastSimpleUUID())
                .setContent(JsonUtils.toJsonString(message.getData()));
        deviceLogMapper.insert(log);
    }

    @Override
    public PageResult<IotDeviceLogDO> getDeviceLogPage(IotDeviceLogPageReqVO pageReqVO) {
        try {
            IPage<IotDeviceLogDO> page = deviceLogMapper.selectPage(
                    new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize()), pageReqVO);
            return new PageResult<>(page.getRecords(), page.getTotal());
        } catch (Exception exception) {
            if (exception.getMessage().contains("Table does not exist")) {
                return PageResult.empty();
            }
            throw exception;
        }
    }

    @Override
    public Long getDeviceLogCount(LocalDateTime createTime) {
        return deviceLogMapper.selectCountByCreateTime(createTime != null ? LocalDateTimeUtil.toEpochMilli(createTime) : null);
    }

    // TODO @super：加一个参数，Boolean upstream：true 上行，false 下行，null 不过滤
    @Override
    public List<Map<Long, Integer>> getDeviceLogUpCountByHour(String deviceKey, Long startTime, Long endTime) {
        // TODO @super：不能只基于数据库统计。因为有一些小时，可能出现没数据的情况，导致前端展示的图是不全的。可以参考 CrmStatisticsCustomerService 来实现
        List<Map<String, Object>> list = deviceLogMapper.selectDeviceLogUpCountByHour(deviceKey, startTime, endTime);
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
        List<Map<String, Object>> list = deviceLogMapper.selectDeviceLogDownCountByHour(deviceKey, startTime, endTime);
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
