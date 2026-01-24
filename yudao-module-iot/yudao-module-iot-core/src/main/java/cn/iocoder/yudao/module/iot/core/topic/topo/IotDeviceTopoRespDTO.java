package cn.iocoder.yudao.module.iot.core.topic.topo;

import lombok.Data;

// TODO @AI：IotDeviceTopoGetRespDTO
/**
 * IoT 设备拓扑关系 Response DTO
 * <p>
 * 用于 thing.topo.get 响应的子设备信息
 *
 * @author 芋道源码
 * @see <a href="https://help.aliyun.com/zh/marketplace/obtain-topological-relationship">阿里云 - 获取拓扑关系</a>
 */
@Data
public class IotDeviceTopoRespDTO {

    // TODO @AI：应该是数组；IotDeviceIdentify
    /**
     * 子设备 ProductKey
     */
    private String productKey;

    /**
     * 子设备 DeviceName
     */
    private String deviceName;

}
