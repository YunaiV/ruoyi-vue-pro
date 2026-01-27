package cn.iocoder.yudao.module.iot.gateway.protocol.coap.router;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.IotCoapUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.util.IotCoapUtils;
import cn.iocoder.yudao.module.iot.gateway.service.auth.IotDeviceTokenService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.coap.CoAP;
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
public class IotCoapUpstreamHandler {

    private final IotDeviceTokenService deviceTokenService;
    private final IotDeviceMessageService deviceMessageService;

    public IotCoapUpstreamHandler() {
        this.deviceTokenService = SpringUtil.getBean(IotDeviceTokenService.class);
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
    }

    /**
     * 处理 CoAP 请求
     *
     * @param exchange CoAP 交换对象
     * @param protocol 协议对象
     */
    public void handle(CoapExchange exchange, IotCoapUpstreamProtocol protocol) {
        try {
            // 1. 解析通用参数
            List<String> uriPath = exchange.getRequestOptions().getUriPath();
            String productKey = CollUtil.get(uriPath, 2);
            String deviceName = CollUtil.get(uriPath, 3);
            byte[] payload = exchange.getRequestPayload();
            if (StrUtil.isEmpty(productKey)) {
                IotCoapUtils.respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "productKey 不能为空");
                return;
            }
            if (StrUtil.isEmpty(deviceName)) {
                IotCoapUtils.respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "deviceName 不能为空");
                return;
            }
            if (ArrayUtil.isEmpty(payload)) {
                IotCoapUtils.respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "请求体不能为空");
                return;
            }

            // 2. 认证：从自定义 Option 获取 token
            String token = IotCoapUtils.getTokenFromOption(exchange, IotCoapUtils.OPTION_TOKEN);
            if (StrUtil.isEmpty(token)) {
                IotCoapUtils.respondError(exchange, CoAP.ResponseCode.UNAUTHORIZED, "token 不能为空");
                return;
            }
            // 验证 token
            IotDeviceIdentity deviceInfo = deviceTokenService.verifyToken(token);
            if (deviceInfo == null) {
                IotCoapUtils.respondError(exchange, CoAP.ResponseCode.UNAUTHORIZED, "token 无效或已过期");
                return;
            }
            // 验证设备信息匹配
            if (ObjUtil.notEqual(productKey, deviceInfo.getProductKey())
                    || ObjUtil.notEqual(deviceName, deviceInfo.getDeviceName())) {
                IotCoapUtils.respondError(exchange, CoAP.ResponseCode.FORBIDDEN, "设备信息与 token 不匹配");
                return;
            }

            // 2.1 解析 method：deviceName 后面的路径，用 . 拼接
            // 路径格式：[topic, sys, productKey, deviceName, thing, property, post]
            String method = String.join(StrPool.DOT, uriPath.subList(4, uriPath.size()));

            // 2.2 解码消息
            IotDeviceMessage message = deviceMessageService.decodeDeviceMessage(payload, productKey, deviceName);
            if (ObjUtil.notEqual(method, message.getMethod())) {
                IotCoapUtils.respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "method 不匹配");
                return;
            }
            // 2.3 发送消息到消息总线
            deviceMessageService.sendDeviceMessage(message, productKey, deviceName, protocol.getServerId());

            // 3. 返回成功响应
            IotCoapUtils.respondSuccess(exchange, MapUtil.of("messageId", message.getId()));
        } catch (Exception e) {
            log.error("[handle][CoAP 请求处理异常]", e);
            IotCoapUtils.respondError(exchange, CoAP.ResponseCode.INTERNAL_SERVER_ERROR, "服务器内部错误");
        }
    }

}
