package cn.iocoder.yudao.module.iot.service.device.control;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.control.IotDeviceSimulationUpstreamReqVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 设备下行 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotDeviceDownstreamServiceImpl implements IotDeviceDownstreamService {

    @Override
    public void simulationDeviceDownstream(IotDeviceSimulationUpstreamReqVO downstreamReqVO) {

    }

}
