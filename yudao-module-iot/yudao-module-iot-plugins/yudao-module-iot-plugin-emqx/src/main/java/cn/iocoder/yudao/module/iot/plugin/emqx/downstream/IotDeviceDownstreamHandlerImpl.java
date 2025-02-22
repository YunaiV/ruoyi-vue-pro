package cn.iocoder.yudao.module.iot.plugin.emqx.downstream;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.*;
import cn.iocoder.yudao.module.iot.plugin.common.downstream.IotDeviceDownstreamHandler;

/**
 * EMQX 插件的 {@link IotDeviceDownstreamHandler} 实现类
 * <p>
 *
 * @author 芋道源码
 */
public class IotDeviceDownstreamHandlerImpl implements IotDeviceDownstreamHandler {

    @Override
    public CommonResult<Boolean> invokeDeviceService(IotDeviceServiceInvokeReqDTO invokeReqDTO) {
        return CommonResult.success(true);
    }

    @Override
    public CommonResult<Boolean> getDeviceProperty(IotDevicePropertyGetReqDTO getReqDTO) {
        return CommonResult.success(true);
    }

    @Override
    public CommonResult<Boolean> setDeviceProperty(IotDevicePropertySetReqDTO setReqDTO) {
        return CommonResult.success(true);
    }

    @Override
    public CommonResult<Boolean> setDeviceConfig(IotDeviceConfigSetReqDTO setReqDTO) {
        return CommonResult.success(true);
    }

    @Override
    public CommonResult<Boolean> upgradeDeviceOta(IotDeviceOtaUpgradeReqDTO upgradeReqDTO) {
        return CommonResult.success(true);
    }

}