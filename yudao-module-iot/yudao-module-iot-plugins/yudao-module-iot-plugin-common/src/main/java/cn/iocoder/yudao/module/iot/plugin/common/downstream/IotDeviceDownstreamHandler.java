package cn.iocoder.yudao.module.iot.plugin.common.downstream;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.IotDevicePropertyGetReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.IotDevicePropertySetReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.IotDeviceServiceInvokeReqDTO;

/**
 * IoT 设备下行处理器
 *
 * 目的：每个 plugin 需要实现，用于处理 server 下行的指令（请求），从而实现从 server => plugin => device 的下行流程
 *
 * @author 芋道源码
 */
public interface IotDeviceDownstreamHandler {

    CommonResult<Boolean> invokeDeviceService(IotDeviceServiceInvokeReqDTO invokeReqDTO);

    CommonResult<Boolean> getDeviceProperty(IotDevicePropertyGetReqDTO getReqDTO);

    CommonResult<Boolean> setDeviceProperty(IotDevicePropertySetReqDTO setReqDTO);

}
