package cn.iocoder.yudao.module.iot.api.device.dto.control.upstream;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

// TODO @芋艿：要写清楚，是来自设备网关，还是设备。
/**
 * IoT 设备【拓扑】添加 Request DTO
 */
@Data
public class IotDeviceTopologyAddReqDTO extends IotDeviceUpstreamAbstractReqDTO {

    // TODO @芋艿：看看要不要优化命名
    /**
     * 子设备数组
     */
    @NotEmpty(message = "子设备不能为空")
    private List<IotDeviceRegisterSubReqDTO.Device> params;

    /**
     * 设备信息
     */
    @Data
    public static class Device {

        /**
         * 产品标识
         */
        @NotEmpty(message = "产品标识不能为空")
        private String productKey;

        /**
         * 设备名称
         */
        @NotEmpty(message = "设备名称不能为空")
        private String deviceName;

        // TODO @芋艿：阿里云还有 sign 签名

    }

}
