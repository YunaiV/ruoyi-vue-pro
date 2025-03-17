package cn.iocoder.yudao.module.iot.plugin.common.downstream;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.*;

/**
 * IoT 设备下行处理器
 *
 * 目的：每个 plugin 需要实现，用于处理 server 下行的指令（请求），从而实现从 server => plugin => device 的下行流程
 *
 * @author 芋道源码
 */
public interface IotDeviceDownstreamHandler {

    /**
     * 调用设备服务
     *
     * @param invokeReqDTO 调用设备服务的请求
     * @return 是否成功
     */
    CommonResult<Boolean> invokeDeviceService(IotDeviceServiceInvokeReqDTO invokeReqDTO);

    /**
     * 获取设备属性
     *
     * @param getReqDTO 获取设备属性的请求
     * @return 是否成功
     */
    CommonResult<Boolean> getDeviceProperty(IotDevicePropertyGetReqDTO getReqDTO);

    /**
     * 设置设备属性
     *
     * @param setReqDTO 设置设备属性的请求
     * @return 是否成功
     */
    CommonResult<Boolean> setDeviceProperty(IotDevicePropertySetReqDTO setReqDTO);

    /**
     * 设置设备配置
     *
     * @param setReqDTO 设置设备配置的请求
     * @return 是否成功
     */
    CommonResult<Boolean> setDeviceConfig(IotDeviceConfigSetReqDTO setReqDTO);

    /**
     * 升级设备 OTA
     *
     * @param upgradeReqDTO 升级设备 OTA 的请求
     * @return 是否成功
     */
    CommonResult<Boolean> upgradeDeviceOta(IotDeviceOtaUpgradeReqDTO upgradeReqDTO);

}
