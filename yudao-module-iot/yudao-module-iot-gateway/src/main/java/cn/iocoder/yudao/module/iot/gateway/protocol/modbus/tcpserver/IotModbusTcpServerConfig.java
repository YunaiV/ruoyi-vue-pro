package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * IoT Modbus TCP Server 协议配置
 *
 * @author 芋道源码
 */
@Data
public class IotModbusTcpServerConfig {

    /**
     * 配置刷新间隔（秒）
     */
    @NotNull(message = "配置刷新间隔不能为空")
    @Min(value = 1, message = "配置刷新间隔不能小于 1 秒")
    private Integer configRefreshInterval = 30;

    /**
     * 自定义功能码（用于认证等扩展交互）
     * Modbus 协议保留 65-72 给用户自定义，默认 65
     */
    @NotNull(message = "自定义功能码不能为空")
    @Min(value = 65, message = "自定义功能码不能小于 65")
    @Max(value = 72, message = "自定义功能码不能大于 72")
    private Integer customFunctionCode = 65;

    /**
     * Pending Request 超时时间（毫秒）
     */
    @NotNull(message = "请求超时时间不能为空")
    private Integer requestTimeout = 5000;

    /**
     * Pending Request 清理间隔（毫秒）
     */
    @NotNull(message = "请求清理间隔不能为空")
    private Integer requestCleanupInterval = 10000;

}
