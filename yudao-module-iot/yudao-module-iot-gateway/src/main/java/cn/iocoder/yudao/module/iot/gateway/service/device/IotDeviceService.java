package cn.iocoder.yudao.module.iot.gateway.service.device;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;

/**
 * IoT 设备信息 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceService {

    /**
     * 根据 productKey 和 deviceName 获取设备信息
     *
     * @param productKey 产品标识
     * @param deviceName 设备名称
     * @return 设备信息
     */
    IotDeviceRespDTO getDeviceFromCache(String productKey, String deviceName);

    /**
     * 根据 id 获取设备信息
     *
     * @param id 设备编号
     * @return 设备信息
     */
    IotDeviceRespDTO getDeviceFromCache(Long id);

}