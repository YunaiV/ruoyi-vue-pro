package cn.iocoder.yudao.module.iot.gateway.protocol.coap.router;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.IotCoapUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.auth.IotDeviceTokenService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.springframework.stereotype.Component;

import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT 网关 CoAP 协议的【认证】处理器
 *
 * 参考 {@link cn.iocoder.yudao.module.iot.gateway.protocol.http.router.IotHttpAuthHandler}
 *
 * @author 芋道源码
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IotCoapAuthHandler {

    private final IotDeviceTokenService deviceTokenService;

    private final IotDeviceCommonApi deviceApi;

    private final IotDeviceMessageService deviceMessageService;

    /**
     * 处理认证请求
     *
     * @param exchange CoAP 交换对象
     * @param protocol 协议对象
     */
    public void handle(CoapExchange exchange, IotCoapUpstreamProtocol protocol) {
        try {
            // 1.1 解析请求体
            byte[] payload = exchange.getRequestPayload();
            if (payload == null || payload.length == 0) {
                respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "请求体不能为空");
                return;
            }
            Map<String, Object> body;
            try {
                body = JsonUtils.parseObject(new String(payload), Map.class);
            } catch (Exception e) {
                respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "请求体 JSON 格式错误");
                return;
            }
            // TODO @AI：通过 hutool maputil 去获取，简化下；
            // 1.2 解析参数
            String clientId = (String) body.get("clientId");
            if (StrUtil.isEmpty(clientId)) {
                respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "clientId 不能为空");
                return;
            }
            String username = (String) body.get("username");
            if (StrUtil.isEmpty(username)) {
                respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "username 不能为空");
                return;
            }
            String password = (String) body.get("password");
            if (StrUtil.isEmpty(password)) {
                respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "password 不能为空");
                return;
            }

            // 2.1 执行认证
            CommonResult<Boolean> result = deviceApi.authDevice(new IotDeviceAuthReqDTO()
                    .setClientId(clientId).setUsername(username).setPassword(password));
            if (result.isError()) {
                log.warn("[handle][认证失败，clientId: {}, 错误: {}]", clientId, result.getMsg());
                respondError(exchange, CoAP.ResponseCode.UNAUTHORIZED, "认证失败：" + result.getMsg());
                return;
            }
            if (!BooleanUtil.isTrue(result.getData())) {
                log.warn("[handle][认证失败，clientId: {}]", clientId);
                respondError(exchange, CoAP.ResponseCode.UNAUTHORIZED, "认证失败");
                return;
            }
            // 2.2 生成 Token
            IotDeviceAuthUtils.DeviceInfo deviceInfo = deviceTokenService.parseUsername(username);
            Assert.notNull(deviceInfo, "设备信息不能为空");
            String token = deviceTokenService.createToken(deviceInfo.getProductKey(), deviceInfo.getDeviceName());
            Assert.notBlank(token, "生成 token 不能为空");

            // 3. 执行上线
            IotDeviceMessage message = IotDeviceMessage.buildStateUpdateOnline();
            deviceMessageService.sendDeviceMessage(message,
                    deviceInfo.getProductKey(), deviceInfo.getDeviceName(), protocol.getServerId());

            // 4. 返回成功响应
            log.info("[handle][认证成功，productKey: {}, deviceName: {}]",
                    deviceInfo.getProductKey(), deviceInfo.getDeviceName());
            respondSuccess(exchange, MapUtil.of("token", token));
        } catch (Exception e) {
            log.error("[handle][认证处理异常]", e);
            respondError(exchange, CoAP.ResponseCode.INTERNAL_SERVER_ERROR, "服务器内部错误");
        }
    }

    // TODO @AI：抽到 coap 的 util 里；
    /**
     * 返回成功响应
     */
    private void respondSuccess(CoapExchange exchange, Object data) {
        CommonResult<Object> result = success(data);
        String json = JsonUtils.toJsonString(result);
        exchange.respond(CoAP.ResponseCode.CONTENT, json, MediaTypeRegistry.APPLICATION_JSON);
    }

    // TODO @AI：抽到 coap 的 util 里；
    /**
     * 返回错误响应
     */
    private void respondError(CoapExchange exchange, CoAP.ResponseCode code, String message) {
        CommonResult<Object> result = CommonResult.error(code.value, message);
        String json = JsonUtils.toJsonString(result);
        exchange.respond(code, json, MediaTypeRegistry.APPLICATION_JSON);
    }

}
