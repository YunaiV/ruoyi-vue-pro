package cn.iocoder.yudao.module.iot.core.topic.topo;

import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import lombok.Data;

import java.util.List;

/**
 * IoT 设备拓扑关系获取 Response DTO
 * <p>
 * 用于 thing.topo.get 响应
 *
 * @author 芋道源码
 * @see <a href="https://help.aliyun.com/zh/marketplace/obtain-topological-relationship">阿里云 - 获取拓扑关系</a>
 */
@Data
public class IotDeviceTopoGetRespDTO {

    /**
     * 子设备列表
     */
    private List<IotDeviceIdentity> subDevices;

}
