package cn.iocoder.yudao.module.iot.gateway.protocol.coap.handler.upstream;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

/**
 * IoT 网关 CoAP 协议的【上行】Topic 资源
 *
 * 支持任意深度的路径匹配：
 * - /topic/sys/{productKey}/{deviceName}/thing/property/post
 * - /topic/sys/{productKey}/{deviceName}/thing/event/{eventId}/post
 *
 * @author 芋道源码
 */
@Slf4j
public class IotCoapUpstreamTopicResource extends CoapResource {

    public static final String PATH = "topic";

    private final String serverId;
    private final IotCoapUpstreamHandler upstreamHandler;

    /**
     * 创建根资源（/topic）
     */
    public IotCoapUpstreamTopicResource(String serverId,
                                         IotCoapUpstreamHandler upstreamHandler) {
        this(PATH, serverId, upstreamHandler);
        log.info("[IotCoapUpstreamTopicResource][创建 CoAP 上行 Topic 资源: /{}]", PATH);
    }

    /**
     * 创建子资源（动态路径）
     */
    private IotCoapUpstreamTopicResource(String name,
                                          String serverId,
                                          IotCoapUpstreamHandler upstreamHandler) {
        super(name);
        this.serverId = serverId;
        this.upstreamHandler = upstreamHandler;
    }

    @Override
    public Resource getChild(String name) {
        // 递归创建动态子资源，支持任意深度路径
        return new IotCoapUpstreamTopicResource(name, serverId, upstreamHandler);
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        upstreamHandler.handle(exchange);
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        upstreamHandler.handle(exchange);
    }

    @Override
    public void handlePUT(CoapExchange exchange) {
        upstreamHandler.handle(exchange);
    }

}
