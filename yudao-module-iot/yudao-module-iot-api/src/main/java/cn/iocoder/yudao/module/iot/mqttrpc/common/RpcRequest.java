package cn.iocoder.yudao.module.iot.mqttrpc.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MQTT RPC 请求
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequest {

    /**
     * 方法名
     */
    private String method;

    /**
     * 参数
     */
    private Object[] params;

    /**
     * 关联 ID
     */
    private String correlationId;

    /**
     * 回复地址
     */
    private String replyTo;
}
