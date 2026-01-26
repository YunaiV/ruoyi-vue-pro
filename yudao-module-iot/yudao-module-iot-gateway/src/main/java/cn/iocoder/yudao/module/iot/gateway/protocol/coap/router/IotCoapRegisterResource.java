package cn.iocoder.yudao.module.iot.gateway.protocol.coap.router;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

/**
 * IoT 网关 CoAP 协议的设备动态注册资源（/auth/register/device）
 * <p>
 * 用于直连设备/网关的一型一密动态注册，不需要认证
 *
 * @author 芋道源码
 */
@Slf4j
public class IotCoapRegisterResource extends CoapResource {

    public static final String PATH = "device";

    private final IotCoapRegisterHandler registerHandler;

    public IotCoapRegisterResource(IotCoapRegisterHandler registerHandler) {
        super(PATH);
        this.registerHandler = registerHandler;
        log.info("[IotCoapRegisterResource][创建 CoAP 设备动态注册资源: /auth/register/{}]", PATH);
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        log.debug("[handlePOST][收到设备动态注册请求]");
        registerHandler.handle(exchange);
    }

}
