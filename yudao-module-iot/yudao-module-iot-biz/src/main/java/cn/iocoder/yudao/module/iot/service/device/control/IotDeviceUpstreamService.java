package cn.iocoder.yudao.module.iot.service.device.control;

import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.*;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.control.IotDeviceUpstreamReqVO;

import javax.validation.Valid;

/**
 * IoT 设备上行 Service 接口
 *
 * 目的：设备 -> 插件 -> 服务端
 *
 * @author 芋道源码
 */
public interface IotDeviceUpstreamService {

    /**
     * 设备上行，可用于设备模拟
     *
     * @param simulatorReqVO 设备上行请求 VO
     */
    void upstreamDevice(@Valid IotDeviceUpstreamReqVO simulatorReqVO);

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

    /**
     * 注册设备
     *
     * @param registerReqDTO 注册设备 DTO
     */
    void registerDevice(IotDeviceRegisterReqDTO registerReqDTO);

    /**
     * 注册子设备
     *
     * @param registerReqDTO 注册子设备 DTO
     */
    void registerSubDevice(IotDeviceRegisterSubReqDTO registerReqDTO);

    /**
     * 添加设备拓扑
     *
     * @param addReqDTO 添加设备拓扑 DTO
     */
    void addDeviceTopology(IotDeviceTopologyAddReqDTO addReqDTO);

    /**
     * Emqx 连接认证
     *
     * @param authReqDTO Emqx 连接认证 DTO
     */
    boolean authenticateEmqxConnection(IotDeviceEmqxAuthReqDTO authReqDTO);

}
