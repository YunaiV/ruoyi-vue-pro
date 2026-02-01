package cn.iocoder.yudao.module.iot.gateway.protocol.coap.handler.upstream;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.List;

/**
 * IoT 网关 CoAP 协议的【上行】处理器
 *
 * 处理设备通过 CoAP 协议发送的上行消息，包括：
 * 1. 属性上报：POST /topic/sys/{productKey}/{deviceName}/thing/property/post
 * 2. 事件上报：POST /topic/sys/{productKey}/{deviceName}/thing/event/post
 *
 * Token 通过自定义 CoAP Option 2088 携带
 *
 * @author 芋道源码
 */
@Slf4j
public class IotCoapUpstreamHandler extends IotCoapAbstractHandler {

    private final String serverId;

    private final IotDeviceMessageService deviceMessageService;

    public IotCoapUpstreamHandler(String serverId) {
        this.serverId = serverId;
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected CommonResult<Object> handle0(CoapExchange exchange) {
        // 1.1 解析通用参数
        List<String> uriPath = exchange.getRequestOptions().getUriPath();
        String productKey = getProductKey(uriPath);
        String deviceName = getDeviceName(uriPath);
        String method = String.join(StrPool.DOT, uriPath.subList(4, uriPath.size()));
        // 1.2 解析消息
        IotDeviceMessage message = deserializeRequest(exchange, IotDeviceMessage.class);
        Assert.notNull(message, "请求参数不能为空");
        Assert.equals(method, message.getMethod(), "method 不匹配");

        // 2. 发送消息
        deviceMessageService.sendDeviceMessage(message, productKey, deviceName, serverId);

        // 3. 返回结果
        return CommonResult.success(MapUtil.of("messageId", message.getId()));
    }

    @Override
    protected boolean requiresAuthentication() {
        return true;
    }

    @Override
    protected String getProductKey(List<String> uriPath) {
        // 路径格式：/topic/sys/{productKey}/{deviceName}/...
        return CollUtil.get(uriPath, 2);
    }

    @Override
    protected String getDeviceName(List<String> uriPath) {
        // 路径格式：/topic/sys/{productKey}/{deviceName}/...
        return CollUtil.get(uriPath, 3);
    }

}
