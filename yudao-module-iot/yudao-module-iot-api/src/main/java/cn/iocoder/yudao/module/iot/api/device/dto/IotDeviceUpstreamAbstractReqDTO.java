package cn.iocoder.yudao.module.iot.api.device.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * IoT 设备上行的抽象 Request DTO
 *
 * @author 芋道源码
 */
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class IotDeviceUpstreamAbstractReqDTO {

    /**
     * 请求编号
     */
    private String requestId;

    /**
     * 插件标识
     */
    private String pluginKey;

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

    /**
     * 上报时间
     */
    private LocalDateTime reportTime;

}
