package cn.iocoder.yudao.module.iot.core.topic.topo;

import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * IoT 设备拓扑删除 Request DTO
 * <p>
 * 用于 thing.topo.delete 消息的 params 参数
 *
 * @author 芋道源码
 * @see <a href="https://help.aliyun.com/zh/marketplace/delete-a-topological-relationship">阿里云 - 删除拓扑关系</a>
 */
@Data
public class IotDeviceTopoDeleteReqDTO {

    /**
     * 子设备标识列表
     */
    @Valid
    @NotEmpty(message = "子设备标识列表不能为空")
    private List<IotDeviceIdentity> subDevices;

}
