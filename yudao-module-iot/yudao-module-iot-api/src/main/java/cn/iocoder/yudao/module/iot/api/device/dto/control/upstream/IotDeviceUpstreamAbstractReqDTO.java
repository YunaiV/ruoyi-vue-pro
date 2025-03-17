package cn.iocoder.yudao.module.iot.api.device.dto.control.upstream;

import cn.iocoder.yudao.framework.common.util.json.databind.TimestampLocalDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

/**
 * IoT 设备上行的抽象 Request DTO
 *
 * @author 芋道源码
 */
@Data
public abstract class IotDeviceUpstreamAbstractReqDTO {

    /**
     * 请求编号
     */
    private String requestId;

    /**
     * 插件实例的进程编号
     */
    private String processId;

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
    @JsonSerialize(using = TimestampLocalDateTimeSerializer.class) // 解决 iot plugins 序列化 LocalDateTime 是数组，导致无法解析的问题
    private LocalDateTime reportTime;

}
