package cn.iocoder.yudao.module.iot.gateway.protocol.http.router;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.IotHttpUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 HTTP 协议的【上行】处理器
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotHttpUpstreamHandler extends IotHttpAbstractHandler {

    public static final String PATH = "/topic/sys/:productKey/:deviceName/*";

    private final IotHttpUpstreamProtocol protocol;

    private final IotDeviceMessageService deviceMessageService;

    public IotHttpUpstreamHandler(IotHttpUpstreamProtocol protocol) {
        this.protocol = protocol;
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
    }

    @Override
    protected CommonResult<Object> handle0(RoutingContext context) {
        // 1. 解析通用参数
        String productKey = context.pathParam("productKey");
        String deviceName = context.pathParam("deviceName");
        String method = context.pathParam("*").replaceAll(StrPool.SLASH, StrPool.DOT);

        // 2.1 解析消息
        byte[] bytes = context.body().buffer().getBytes();
        IotDeviceMessage message = deviceMessageService.decodeDeviceMessage(bytes,
                productKey, deviceName);
        Assert.equals(method, message.getMethod(), "method 不匹配");
        // 2.2 发送消息
        deviceMessageService.sendDeviceMessage(message,
                productKey, deviceName, protocol.getServerId());

        // 3. 返回结果
        return CommonResult.success(MapUtil.of("messageId", message.getId()));
    }

}