package cn.iocoder.yudao.module.iot.gateway.codec.tcp;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.codec.IotDeviceMessageCodec;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TCP JSON 格式 {@link IotDeviceMessage} 编解码器
 *
 * 采用纯 JSON 格式传输，格式如下：
 * {
 *     "id": "消息 ID",
 *     "method": "消息方法",
 *     "params": {...}, // 请求参数
 *     "data": {...}, // 响应结果
 *     "code": 200, // 响应错误码
 *     "msg": "success", // 响应提示
 *     "timestamp": 时间戳
 * }
 *
 * @author 芋道源码
 */
@Component
public class IotTcpJsonDeviceMessageCodec implements IotDeviceMessageCodec {

    public static final String TYPE = "TCP_JSON";

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TcpJsonMessage {

        /**
         * 消息 ID，且每个消息 ID 在当前设备具有唯一性
         */
        private String id;

        /**
         * 请求方法
         */
        private String method;

        /**
         * 请求参数
         */
        private Object params;

        /**
         * 响应结果
         */
        private Object data;

        /**
         * 响应错误码
         */
        private Integer code;

        /**
         * 响应提示
         */
        private String msg;

        /**
         * 时间戳
         */
        private Long timestamp;

    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public byte[] encode(IotDeviceMessage message) {
        TcpJsonMessage tcpJsonMessage = new TcpJsonMessage(
                message.getRequestId(),
                message.getMethod(),
                message.getParams(),
                message.getData(),
                message.getCode(),
                message.getMsg(),
                System.currentTimeMillis());
        return JsonUtils.toJsonByte(tcpJsonMessage);
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public IotDeviceMessage decode(byte[] bytes) {
        String jsonStr = StrUtil.utf8Str(bytes).trim();
        TcpJsonMessage tcpJsonMessage = JsonUtils.parseObject(jsonStr, TcpJsonMessage.class);
        Assert.notNull(tcpJsonMessage, "消息不能为空");
        Assert.notBlank(tcpJsonMessage.getMethod(), "消息方法不能为空");
        return IotDeviceMessage.of(
                tcpJsonMessage.getId(),
                tcpJsonMessage.getMethod(),
                tcpJsonMessage.getParams(),
                tcpJsonMessage.getData(),
                tcpJsonMessage.getCode(),
                tcpJsonMessage.getMsg());
    }

}
