package cn.iocoder.yudao.module.iot.gateway.protocol.http.handler.upstream;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.http.IotHttpProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 HTTP 协议的【上行】处理器
 *
 * @author 芋道源码
 */
@Slf4j
public class IotHttpUpstreamHandler extends IotHttpAbstractHandler {

    public static final String PATH = "/topic/sys/:productKey/:deviceName/*";

    private final String serverId;

    private final IotDeviceMessageService deviceMessageService;

    public IotHttpUpstreamHandler(IotHttpProtocol protocol) {
        this.serverId = protocol.getServerId();
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
    }

    @Override
    protected CommonResult<Object> handle0(RoutingContext context) {
        // 1.1 解析通用参数
        String productKey = context.pathParam("productKey");
        String deviceName = context.pathParam("deviceName");
        String method = context.pathParam("*").replaceAll(StrPool.SLASH, StrPool.DOT);
        // 1.2 根据 Content-Type 反序列化消息
        IotDeviceMessage message = deserializeRequest(context, IotDeviceMessage.class);
        Assert.notNull(message, "请求参数不能为空");
        Assert.equals(method, message.getMethod(), "method 不匹配");

        // 2. 发送消息
        deviceMessageService.sendDeviceMessage(message,
                productKey, deviceName, serverId);

        // 3. 返回结果
        return CommonResult.success(MapUtil.of("messageId", message.getId()));
    }

}
