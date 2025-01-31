package cn.iocoder.yudao.module.iot.service.device.control;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.control.IotDeviceSimulationDownstreamReqVO;
import jakarta.validation.Valid;

/**
 * 设备下行 Service 接口
 *
 * 目的：服务端 -> 插件 -> 设备
 *
 * @author 芋道源码
 */
public interface IotDeviceDownstreamService {

    /**
     * 模拟设备下行
     *
     * @param downstreamReqVO 设备下行请求 VO
     */
    void simulationDeviceDownstream(@Valid IotDeviceSimulationDownstreamReqVO downstreamReqVO);

}
