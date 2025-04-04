package cn.iocoder.yudao.module.iot.net.component.http.downstream;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.*;
import cn.iocoder.yudao.module.iot.net.component.core.downstream.IotDeviceDownstreamHandler;
import lombok.extern.slf4j.Slf4j;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_IMPLEMENTED;

/**
 * HTTP 网络组件的 {@link IotDeviceDownstreamHandler} 实现类
 * <p>
 * 但是：由于设备通过 HTTP 短链接接入，导致其实无法下行指导给 device 设备，所以基本都是直接返回失败！！！
 * 类似 MQTT、WebSocket、TCP 网络组件，是可以实现下行指令的。
 *
 * @author 芋道源码
 */
@Slf4j
public class IotDeviceDownstreamHandlerImpl implements IotDeviceDownstreamHandler {

    /**
     * 不支持的错误消息
     */
    private static final String NOT_SUPPORTED_MSG = "HTTP 不支持设备下行通信";

    @Override
    public CommonResult<Boolean> invokeDeviceService(IotDeviceServiceInvokeReqDTO invokeReqDTO) {
        return CommonResult.error(NOT_IMPLEMENTED.getCode(), NOT_SUPPORTED_MSG);
    }

    @Override
    public CommonResult<Boolean> getDeviceProperty(IotDevicePropertyGetReqDTO getReqDTO) {
        return CommonResult.error(NOT_IMPLEMENTED.getCode(), NOT_SUPPORTED_MSG);
    }

    @Override
    public CommonResult<Boolean> setDeviceProperty(IotDevicePropertySetReqDTO setReqDTO) {
        return CommonResult.error(NOT_IMPLEMENTED.getCode(), NOT_SUPPORTED_MSG);
    }

    @Override
    public CommonResult<Boolean> setDeviceConfig(IotDeviceConfigSetReqDTO setReqDTO) {
        return CommonResult.error(NOT_IMPLEMENTED.getCode(), NOT_SUPPORTED_MSG);
    }

    @Override
    public CommonResult<Boolean> upgradeDeviceOta(IotDeviceOtaUpgradeReqDTO upgradeReqDTO) {
        return CommonResult.error(NOT_IMPLEMENTED.getCode(), NOT_SUPPORTED_MSG);
    }
}
