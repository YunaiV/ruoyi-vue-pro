package cn.iocoder.yudao.module.iot.core.topic.topo;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

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

    // TODO @AI：应该是数组；IotDeviceIdentify
    /**
     * 子设备标识列表
     */
    private List<SubDevice> subDevices;

    /**
     * 子设备标识
     */
    @Data
    public static class SubDevice {

        /**
         * 子设备 ProductKey
         */
        @NotEmpty(message = "产品标识不能为空")
        private String productKey;

        /**
         * 子设备 DeviceName
         */
        @NotEmpty(message = "设备名称不能为空")
        private String deviceName;

    }

}
