package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

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
