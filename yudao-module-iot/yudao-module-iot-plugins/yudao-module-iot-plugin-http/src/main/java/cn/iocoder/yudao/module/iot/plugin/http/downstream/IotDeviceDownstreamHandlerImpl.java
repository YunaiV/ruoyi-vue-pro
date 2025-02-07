package cn.iocoder.yudao.module.iot.plugin.http.downstream;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.*;
import cn.iocoder.yudao.module.iot.plugin.common.downstream.IotDeviceDownstreamHandler;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_IMPLEMENTED;

/**
 * HTTP 插件的 {@link IotDeviceDownstreamHandler} 实现类
 *
 * 但是：由于设备通过 HTTP 短链接接入，导致其实无法下行指导给 device 设备，所以基本都是直接返回失败！！！
 * 类似 MQTT、WebSocket、TCP 插件，是可以实现下行指令的。
 *
 * @author 芋道源码
 */
public class IotDeviceDownstreamHandlerImpl implements IotDeviceDownstreamHandler {

    @Override
    public CommonResult<Boolean> invokeDeviceService(IotDeviceServiceInvokeReqDTO invokeReqDTO) {
        return CommonResult.error(NOT_IMPLEMENTED.getCode(), "HTTP 不支持调用设备服务");
    }

    @Override
    public CommonResult<Boolean> getDeviceProperty(IotDevicePropertyGetReqDTO getReqDTO) {
        return CommonResult.error(NOT_IMPLEMENTED.getCode(), "HTTP 不支持获取设备属性");
    }

    @Override
    public CommonResult<Boolean> setDeviceProperty(IotDevicePropertySetReqDTO setReqDTO) {
        return CommonResult.error(NOT_IMPLEMENTED.getCode(), "HTTP 不支持设置设备属性");
    }

    @Override
    public CommonResult<Boolean> setDeviceConfig(IotDeviceConfigSetReqDTO setReqDTO) {
        return CommonResult.error(NOT_IMPLEMENTED.getCode(), "HTTP 不支持设置设备属性");
    }

    @Override
    public CommonResult<Boolean> upgradeDeviceOta(IotDeviceOtaUpgradeReqDTO upgradeReqDTO) {
        return CommonResult.error(NOT_IMPLEMENTED.getCode(), "HTTP 不支持设置设备属性");
    }

}
