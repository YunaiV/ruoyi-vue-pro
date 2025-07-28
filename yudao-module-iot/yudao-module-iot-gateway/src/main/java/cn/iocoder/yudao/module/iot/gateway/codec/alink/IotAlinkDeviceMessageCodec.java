package cn.iocoder.yudao.module.iot.gateway.codec.alink;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.codec.IotDeviceMessageCodec;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 阿里云 Alink {@link IotDeviceMessage} 的编解码器
 *
 * @author 芋道源码
 */
@Component
public class IotAlinkDeviceMessageCodec implements IotDeviceMessageCodec {

    private static final String TYPE = "Alink";

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class AlinkMessage {

        public static final String VERSION_1 = "1.0";

        /**
         * 消息 ID，且每个消息 ID 在当前设备具有唯一性
         */
        private String id;

        /**
         * 版本号
         */
        private String version;

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
         *
         * 特殊：这里阿里云是 message，为了保持和项目的 {@link CommonResult#getMsg()} 一致。
         */
        private String msg;

    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public byte[] encode(IotDeviceMessage message) {
        AlinkMessage alinkMessage = new AlinkMessage(message.getRequestId(), AlinkMessage.VERSION_1,
                message.getMethod(), message.getParams(), message.getData(), message.getCode(), message.getMsg());
        return JsonUtils.toJsonByte(alinkMessage);
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public IotDeviceMessage decode(byte[] bytes) {
        AlinkMessage alinkMessage = JsonUtils.parseObject(bytes, AlinkMessage.class);
        Assert.notNull(alinkMessage, "消息不能为空");
        Assert.equals(alinkMessage.getVersion(), AlinkMessage.VERSION_1, "消息版本号必须是 1.0");
        return IotDeviceMessage.of(alinkMessage.getId(), alinkMessage.getMethod(), alinkMessage.getParams(),
                alinkMessage.getData(), alinkMessage.getCode(), alinkMessage.getMsg());
    }

}
