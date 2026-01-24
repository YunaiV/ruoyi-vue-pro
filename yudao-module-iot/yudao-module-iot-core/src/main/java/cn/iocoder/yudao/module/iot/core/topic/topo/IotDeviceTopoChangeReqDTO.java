package cn.iocoder.yudao.module.iot.core.topic.topo;

import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * IoT 设备拓扑关系变更通知 Request DTO
 * <p>
 * 用于 thing.topo.change 下行消息的 params 参数
 *
 * @author 芋道源码
 * @see <a href="https://help.aliyun.com/zh/marketplace/notify-gateway-topology-changes">阿里云 - 通知网关拓扑关系变化</a>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceTopoChangeReqDTO {

    public static final Integer STATUS_CREATE = 0;
    public static final Integer STATUS_DELETE = 1;

    /**
     * 拓扑关系状态
     */
    private Integer status;

    /**
     * 子设备列表
     */
    private List<IotDeviceIdentity> subList;

    public static IotDeviceTopoChangeReqDTO ofCreate(List<IotDeviceIdentity> subList) {
        return new IotDeviceTopoChangeReqDTO(STATUS_CREATE, subList);
    }

    public static IotDeviceTopoChangeReqDTO ofDelete(List<IotDeviceIdentity> subList) {
        return new IotDeviceTopoChangeReqDTO(STATUS_DELETE, subList);
    }

}
