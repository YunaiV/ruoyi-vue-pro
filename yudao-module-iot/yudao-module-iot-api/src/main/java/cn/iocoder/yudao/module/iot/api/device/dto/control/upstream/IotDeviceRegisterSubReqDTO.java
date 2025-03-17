package cn.iocoder.yudao.module.iot.api.device.dto.control.upstream;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * IoT 设备【注册】子设备 Request DTO
 *
 * @author 芋道源码
 */
@Data
public class IotDeviceRegisterSubReqDTO extends IotDeviceUpstreamAbstractReqDTO {

    // TODO @芋艿：看看要不要优化命名
    /**
     * 子设备数组
     */
    @NotEmpty(message = "子设备不能为空")
    private List<Device> params;

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

    }

}
