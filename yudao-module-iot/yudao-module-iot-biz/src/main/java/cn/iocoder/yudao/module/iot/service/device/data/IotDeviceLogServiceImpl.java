package cn.iocoder.yudao.module.iot.service.device.data;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.data.IotDeviceLogPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceLogDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotDeviceLogMapper;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

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
        Long time = null;
        if (createTime != null) {
            time = createTime.toInstant(ZoneOffset.UTC).toEpochMilli();
        }
        return deviceLogMapper.selectCountByCreateTime(time);
    }

    @Override
    public List<IotStatisticsRespVO.TimeData> getDeviceLogUpCountByHour(String deviceKey, Long startTime, Long endTime) {
        try {
            return deviceLogMapper.selectDeviceLogUpCountByHour(deviceKey, startTime, endTime);
        } catch (Exception exception) {
            if (exception.getMessage().contains("Table does not exist")) {
                return new ArrayList<>();
            }
            throw exception;
        }
    }

    @Override
    public List<IotStatisticsRespVO.TimeData> getDeviceLogDownCountByHour(String deviceKey, Long startTime, Long endTime) {
        try {
            return deviceLogMapper.selectDeviceLogDownCountByHour(deviceKey, startTime, endTime);
        } catch (Exception exception) {
            if (exception.getMessage().contains("Table does not exist")) {
                return new ArrayList<>();
            }
            throw exception;
        }
    }

}
