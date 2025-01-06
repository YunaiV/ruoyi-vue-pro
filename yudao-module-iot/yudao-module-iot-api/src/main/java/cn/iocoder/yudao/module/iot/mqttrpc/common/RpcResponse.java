package cn.iocoder.yudao.module.iot.mqttrpc.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MQTT RPC 响应
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse {

    /**
     * 关联 ID
     */
    private String correlationId;

    /**
     * 结果
     */
    private Object result;

    /**
     * 错误
     */
    private String error;
}
