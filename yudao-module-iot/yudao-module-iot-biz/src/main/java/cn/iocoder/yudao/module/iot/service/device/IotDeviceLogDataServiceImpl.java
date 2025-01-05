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

/**
 * IoT 设备日志数据 Service 实现了
 *
 * @author alwayssuper
 */
@Service
@Slf4j
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
        IotDeviceLogDO iotDeviceLogDO = BeanUtils.toBean(simulatorReqVO, IotDeviceLogDO.class);
        iotDeviceLogDO.setTs(DateTime.now());
        iotDeviceLogDataMapper.insert(iotDeviceLogDO);
    }
}
