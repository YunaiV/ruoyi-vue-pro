package cn.iocoder.yudao.module.iot.gateway.protocol.coap.handler.upstream;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

/**
 * IoT 网关 CoAP 协议的子设备动态注册资源（/auth/register/sub-device/{productKey}/{deviceName}）
 * <p>
 * 用于子设备的动态注册，需要网关认证
 * <p>
 * 支持动态路径匹配：productKey 和 deviceName 是网关设备的标识
 *
 * @author 芋道源码
 */
@Slf4j
public class IotCoapRegisterSubResource extends CoapResource {

    public static final String PATH = "sub-device";

    private final IotCoapRegisterSubHandler registerSubHandler;

    /**
     * 创建根资源（/auth/register/sub-device）
     */
    public IotCoapRegisterSubResource(IotCoapRegisterSubHandler registerSubHandler) {
        this(PATH, registerSubHandler);
        log.info("[IotCoapRegisterSubResource][创建 CoAP 子设备动态注册资源: /auth/register/{}]", PATH);
    }

    /**
     * 创建子资源（动态路径）
     */
    private IotCoapRegisterSubResource(String name, IotCoapRegisterSubHandler registerSubHandler) {
        super(name);
        this.registerSubHandler = registerSubHandler;
    }

    @Override
    public Resource getChild(String name) {
        // 递归创建动态子资源，支持 /sub-device/{productKey}/{deviceName} 路径
        return new IotCoapRegisterSubResource(name, registerSubHandler);
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        log.debug("[handlePOST][收到子设备动态注册请求]");
        registerSubHandler.handle(exchange);
    }

}
