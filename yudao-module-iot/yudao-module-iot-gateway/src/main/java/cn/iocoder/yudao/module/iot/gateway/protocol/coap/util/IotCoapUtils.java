package cn.iocoder.yudao.module.iot.gateway.protocol.coap.util;

import cn.iocoder.yudao.module.iot.gateway.protocol.coap.handler.upstrem.IotCoapAbstractHandler;

/**
 * IoT CoAP 协议工具类
 *
 * @author 芋道源码
 */
public class IotCoapUtils {

    /**
     * 自定义 CoAP Option 编号，用于携带 Token
     * <p>
     * CoAP Option 范围 2048-65535 属于实验/自定义范围
     */
    public static final int OPTION_TOKEN = IotCoapAbstractHandler.OPTION_TOKEN;

}
