package cn.iocoder.yudao.module.iot.gateway.protocol.coap.handler.upstream;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

/**
 * IoT 网关 CoAP 协议的认证资源（/auth）
 *
 * 设备通过此资源进行认证，获取 Token
 *
 * @author 芋道源码
 */
@Slf4j
public class IotCoapAuthResource extends CoapResource {

    public static final String PATH = "auth";

    private final IotCoapAuthHandler authHandler;

    public IotCoapAuthResource(IotCoapAuthHandler authHandler) {
        super(PATH);
        this.authHandler = authHandler;
        log.info("[IotCoapAuthResource][创建 CoAP 认证资源: /{}]", PATH);
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        log.debug("[handlePOST][收到 /auth POST 请求]");
        authHandler.handle(exchange);
    }

}
