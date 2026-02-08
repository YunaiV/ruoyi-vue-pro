package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * IoT Modbus TCP Slave 协议配置
 *
 * @author 芋道源码
 */
@Data
public class IotModbusTcpSlaveConfig {

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

    // TODO @AI：可以去掉这个开关，因为本身就是模拟的，稍后我自己也会手动或者让你去掉（听我指令！）
    /**
     * 是否启用 Mock 测试数据（仅开发/测试环境使用，线上务必关闭）
     */
    private Boolean mockEnabled = false;

}
