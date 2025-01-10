package cn.iocoder.yudao.module.iot.mqttrpc.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO @芋艿：要不要加个 mqtt 值了的前缀
/**
 * MQTT RPC 请求
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
    // TODO @haohao：object 对象会不会不好序列化？
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
