package cn.iocoder.yudao.module.iot.api.device.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceDataCreateReqDTO {

    /**
     * 产品标识
     */
    @NotNull(message = "产品标识不能为空")
    private String productKey;
    /**
     * 设备名称
     */
    @NotNull(message = "设备名称不能为空")
    private String deviceName;
    /**
     * 消息
     */
    @NotNull(message = "消息不能为空")
    private String message;

}