package cn.iocoder.yudao.module.iot.service.device.upstream;

import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceEventReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDevicePropertyReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceStateUpdateReqDTO;

/**
 * 设备上行 Service 接口
 *
 * 目的：设备 -> 插件 -> 服务端
 *
 * @author 芋道源码
 */
public interface IotDeviceUpstreamService {

    /**
     * 更新设备状态
     *
     * @param updateReqDTO 更新设备状态 DTO
     */
    void updateDeviceState(IotDeviceStateUpdateReqDTO updateReqDTO);

    /**
     * 上报设备属性数据
     *
     * @param reportReqDTO 上报设备属性数据 DTO
     */
    void reportDeviceProperty(IotDevicePropertyReportReqDTO reportReqDTO);

    /**
     * 上报设备事件数据
     *
     * @param reportReqDTO 设备事件
     */
    void reportDeviceEvent(IotDeviceEventReportReqDTO reportReqDTO);

}
