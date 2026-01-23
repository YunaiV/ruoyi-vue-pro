package cn.iocoder.yudao.module.iot.core.topic.topo;

import lombok.Data;

// TODO @AI：是不是改成 IotDeviceTopoGetRespDTO
/**
 * IoT 设备拓扑关系 Response DTO
 * <p>
 * 用于 thing.topo.get 响应的子设备信息
 *
 * @author 芋道源码
 */
@Data
public class IotDeviceTopoRespDTO {

    /**
     * 子设备 ProductKey
     */
    private String productKey;

    /**
     * 子设备 DeviceName
     */
    private String deviceName;

}
