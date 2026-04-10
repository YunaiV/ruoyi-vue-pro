package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * IoT Modbus TCP Client 协议配置
 *
 * @author 芋道源码
 */
@Data
public class IotModbusTcpClientConfig {

    /**
     * 配置刷新间隔（秒）
     */
    @NotNull(message = "配置刷新间隔不能为空")
    @Min(value = 1, message = "配置刷新间隔不能小于 1 秒")
    private Integer configRefreshInterval = 30;

}
