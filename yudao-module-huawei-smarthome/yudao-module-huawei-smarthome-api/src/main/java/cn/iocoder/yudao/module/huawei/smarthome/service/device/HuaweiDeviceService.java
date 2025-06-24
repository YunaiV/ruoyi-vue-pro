package cn.iocoder.yudao.module.huawei.smarthome.service.device;

import cn.iocoder.yudao.module.huawei.smarthome.dto.device.DeviceControlReqDTO;
import cn.iocoder.yudao.module.huawei.smarthome.dto.device.DeviceGetReqDTO;
import cn.iocoder.yudao.module.huawei.smarthome.dto.device.DeviceListBySpaceReqDTO;
import cn.iocoder.yudao.module.huawei.smarthome.dto.device.DeviceListRespDTO;

import javax.validation.Valid;
import java.io.IOException;

/**
 * 华为智能家居 - 设备管理和控制 Service 接口
 *
 * @author Jules
 */
public interface HuaweiDeviceService {

    /**
     * 查询指定空间下设备信息
     *
     * @param listReqDTO 包含 spaceId 的请求 DTO
     * @return 设备信息列表
     * @throws IOException 当 API 调用失败时抛出
     */
    DeviceListRespDTO listDevicesBySpace(@Valid DeviceListBySpaceReqDTO listReqDTO) throws IOException;

    /**
     * 查询指定设备信息
     *
     * @param getReqDTO 包含 deviceIds 列表的请求 DTO
     * @return 设备信息列表
     * @throws IOException 当 API 调用失败时抛出
     */
    DeviceListRespDTO getDevices(@Valid DeviceGetReqDTO getReqDTO) throws IOException;

    /**
     * 控制设备
     *
     * @param controlReqDTO 设备控制请求 DTO
     * @throws IOException 当 API 调用失败时抛出
     */
    void controlDevice(@Valid DeviceControlReqDTO controlReqDTO) throws IOException;

}
