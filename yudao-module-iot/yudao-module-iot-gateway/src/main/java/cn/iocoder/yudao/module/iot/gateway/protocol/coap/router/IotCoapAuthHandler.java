package cn.iocoder.yudao.module.iot.gateway.protocol.coap.router;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.IotCoapUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.protocol.coap.util.IotCoapUtils;
import cn.iocoder.yudao.module.iot.gateway.service.auth.IotDeviceTokenService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.Map;

/**
 * IoT 网关 CoAP 协议的【认证】处理器
 *
 * 参考 {@link cn.iocoder.yudao.module.iot.gateway.protocol.http.router.IotHttpAuthHandler}
 *
 * @author 芋道源码
 */
@Slf4j
public class IotCoapAuthHandler {

    private final IotDeviceTokenService deviceTokenService;
    private final IotDeviceCommonApi deviceApi;
    private final IotDeviceMessageService deviceMessageService;

    public IotCoapAuthHandler() {
        this.deviceTokenService = SpringUtil.getBean(IotDeviceTokenService.class);
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
    }

    /**
     * 处理认证请求
     *
     * @param exchange CoAP 交换对象
     * @param protocol 协议对象
     */
    @SuppressWarnings("unchecked")
    public void handle(CoapExchange exchange, IotCoapUpstreamProtocol protocol) {
        try {
            // 1.1 解析请求体
            byte[] payload = exchange.getRequestPayload();
            if (payload == null || payload.length == 0) {
                IotCoapUtils.respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "请求体不能为空");
                return;
            }
            Map<String, Object> body;
            try {
                body = JsonUtils.parseObject(new String(payload), Map.class);
            } catch (Exception e) {
                IotCoapUtils.respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "请求体 JSON 格式错误");
                return;
            }
            // 1.2 解析参数
            String clientId = MapUtil.getStr(body, "clientId");
            if (StrUtil.isEmpty(clientId)) {
                IotCoapUtils.respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "clientId 不能为空");
                return;
            }
            String username = MapUtil.getStr(body, "username");
            if (StrUtil.isEmpty(username)) {
                IotCoapUtils.respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "username 不能为空");
                return;
            }
            String password = MapUtil.getStr(body, "password");
            if (StrUtil.isEmpty(password)) {
                IotCoapUtils.respondError(exchange, CoAP.ResponseCode.BAD_REQUEST, "password 不能为空");
                return;
            }

            // 2.1 执行认证
            CommonResult<Boolean> result = deviceApi.authDevice(new IotDeviceAuthReqDTO()
                    .setClientId(clientId).setUsername(username).setPassword(password));
            if (result.isError()) {
                log.warn("[handle][认证失败，clientId: {}, 错误: {}]", clientId, result.getMsg());
                IotCoapUtils.respondError(exchange, CoAP.ResponseCode.UNAUTHORIZED, "认证失败：" + result.getMsg());
                return;
            }
            if (!BooleanUtil.isTrue(result.getData())) {
                log.warn("[handle][认证失败，clientId: {}]", clientId);
                IotCoapUtils.respondError(exchange, CoAP.ResponseCode.UNAUTHORIZED, "认证失败");
                return;
            }
            // 2.2 生成 Token
            IotDeviceIdentity deviceInfo = deviceTokenService.parseUsername(username);
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
            IotCoapUtils.respondSuccess(exchange, MapUtil.of("token", token));
        } catch (Exception e) {
            log.error("[handle][认证处理异常]", e);
            IotCoapUtils.respondError(exchange, CoAP.ResponseCode.INTERNAL_SERVER_ERROR, "服务器内部错误");
        }
    }

}
