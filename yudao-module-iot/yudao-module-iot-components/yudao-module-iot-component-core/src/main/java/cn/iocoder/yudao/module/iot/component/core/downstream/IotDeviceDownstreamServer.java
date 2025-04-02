package cn.iocoder.yudao.module.iot.component.core.downstream;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.*;
import cn.iocoder.yudao.module.iot.component.core.config.IotComponentCommonProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 设备下行服务，直接转发给 device 设备
 *
 * @author 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class IotDeviceDownstreamServer {

    private final IotComponentCommonProperties properties;
    private final IotDeviceDownstreamHandler deviceDownstreamHandler;

    /**
     * 调用设备服务
     *
     * @param invokeReqDTO 调用设备服务的请求
     * @return 是否成功
     */
    public CommonResult<Boolean> invokeDeviceService(IotDeviceServiceInvokeReqDTO invokeReqDTO) {
        return deviceDownstreamHandler.invokeDeviceService(invokeReqDTO);
    }

    /**
     * 获取设备属性
     *
     * @param getReqDTO 获取设备属性的请求
     * @return 是否成功
     */
    public CommonResult<Boolean> getDeviceProperty(IotDevicePropertyGetReqDTO getReqDTO) {
        return deviceDownstreamHandler.getDeviceProperty(getReqDTO);
    }

    /**
     * 设置设备属性
     *
     * @param setReqDTO 设置设备属性的请求
     * @return 是否成功
     */
    public CommonResult<Boolean> setDeviceProperty(IotDevicePropertySetReqDTO setReqDTO) {
        return deviceDownstreamHandler.setDeviceProperty(setReqDTO);
    }

    /**
     * 设置设备配置
     *
     * @param setReqDTO 设置设备配置的请求
     * @return 是否成功
     */
    public CommonResult<Boolean> setDeviceConfig(IotDeviceConfigSetReqDTO setReqDTO) {
        return deviceDownstreamHandler.setDeviceConfig(setReqDTO);
    }

    /**
     * 升级设备 OTA
     *
     * @param upgradeReqDTO 升级设备 OTA 的请求
     * @return 是否成功
     */
    public CommonResult<Boolean> upgradeDeviceOta(IotDeviceOtaUpgradeReqDTO upgradeReqDTO) {
        return deviceDownstreamHandler.upgradeDeviceOta(upgradeReqDTO);
    }

    /**
     * 获得内部组件标识
     *
     * @return 组件标识
     */
    public String getComponentId() {
        return properties.getPluginKey();
    }

}
