package cn.iocoder.yudao.module.iot.service.device;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceDataSimulatorSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceLogDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotDeviceLogDataMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * IoT 设备日志数据 Service 实现了
 *
 * @author alwayssuper
 */
@Service
@Slf4j
@Validated
public class IotDeviceLogDataServiceImpl implements IotDeviceLogDataService{

    @Resource
    private IotDeviceLogDataMapper iotDeviceLogDataMapper;


    @Override
    public void initTDengineSTable() {
        try {
            // 创建设备日志超级表
            iotDeviceLogDataMapper.createDeviceLogSTable();
            log.info("创建设备日志超级表成功");
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public void createDeviceLog(IotDeviceDataSimulatorSaveReqVO simulatorReqVO) {
        //TODO:讨论一下，iotkit这块TS和上报时间都是外部传入的   但是看TDengine文档 他是建议对TS在SQL中直接NOW   咱们的TS数据获取是走哪一种

        // 1. 转换请求对象为 DO
        IotDeviceLogDO iotDeviceLogDO = BeanUtils.toBean(simulatorReqVO, IotDeviceLogDO.class);
        
        // 2. 处理时间字段
        long currentTime = System.currentTimeMillis();
        // 2.1 设置时序时间为当前时间
        iotDeviceLogDO.setTs(currentTime);
        
        // 2.2 处理上报时间
        if (simulatorReqVO.getReportTime() != null) {
            // 将 LocalDateTime 转换为时间戳
            iotDeviceLogDO.setReportTime(
                simulatorReqVO.getReportTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            );
        } else {
            // 如果没有上报时间，使用当前时间
            iotDeviceLogDO.setReportTime(currentTime);
        }
        
        // 3. 插入数据
        iotDeviceLogDataMapper.insert(iotDeviceLogDO);
    }
}
