package cn.iocoder.yudao.module.iot.core.biz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.Map;

/**
 * IoT 网关协议实例的存活上报 Request DTO
 *
 * @author 芋道源码
 */
@Data
public class IotGatewayServerAliveReqDTO {

    /**
     * 协议实例的启动时间（毫秒）
     *
     * key：协议实例的 serverId
     * value：协议实例本次 start 前取的时间，续期期间不变
     */
    @NotEmpty(message = "启动时间不能为空")
    private Map<@NotBlank(message = "serverId 不能为空") String,
            @NotNull(message = "启动时间不能为空") @Positive(message = "启动时间必须大于 0") Long> startTimes;

}
