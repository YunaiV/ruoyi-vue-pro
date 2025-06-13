package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.IotMqttUpstreamProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_AUTH_FAIL;

/**
 * IoT 网关 MQTT HTTP 认证处理器
 * <p>
 * 处理 EMQX 的认证请求和事件钩子，提供统一的错误处理和参数校验
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttHttpAuthHandler {

    /**
     * HTTP 成功状态码（EMQX 要求固定使用 200）
     */
    private static final int SUCCESS_STATUS_CODE = 200;

    /**
     * 认证允许结果
     */
    private static final String RESULT_ALLOW = "allow";
    /**
     * 认证拒绝结果
     */
    private static final String RESULT_DENY = "deny";
    /**
     * 认证忽略结果
     */
    private static final String RESULT_IGNORE = "ignore";

    /**
     * EMQX 事件类型常量
     */
    private static final String EVENT_CLIENT_CONNECTED = "client.connected";
    private static final String EVENT_CLIENT_DISCONNECTED = "client.disconnected";

    private final IotMqttUpstreamProtocol protocol;

    private final IotDeviceMessageService deviceMessageService;

    private final IotDeviceCommonApi deviceApi;

    public IotMqttHttpAuthHandler(IotMqttUpstreamProtocol protocol) {
        this.protocol = protocol;
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
    }

    /**
     * EMQX 认证接口
     */
    public void handleAuth(RoutingContext context) {
        try {
            // 参数校验
            JsonObject body = parseRequestBody(context);
            if (body == null) {
                return;
            }
            String clientId = body.getString("clientid");
            String username = body.getString("username");
            String password = body.getString("password");
            log.debug("[handleAuth][设备认证请求: clientId={}, username={}]", clientId, username);
            if (StrUtil.hasEmpty(clientId, username, password)) {
                log.info("[handleAuth][认证参数不完整: clientId={}, username={}]", clientId, username);
                sendAuthResponse(context, RESULT_DENY, false, "认证参数不完整");
                return;
            }

            // 执行设备认证
            boolean authResult = performDeviceAuth(clientId, username, password);
            if (authResult) {
                // TODO @haohao：是不是两条 info，直接打认证结果：authResult
                log.info("[handleAuth][设备认证成功: {}]", username);
                sendAuthResponse(context, RESULT_ALLOW, false, null);
            } else {
                log.info("[handleAuth][设备认证失败: {}]", username);
                sendAuthResponse(context, RESULT_DENY, false, DEVICE_AUTH_FAIL.getMsg());
            }
        } catch (Exception e) {
            log.error("[handleAuth][设备认证异常]", e);
            sendAuthResponse(context, RESULT_IGNORE, false, "认证服务异常");
        }
    }

    /**
     * EMQX 统一事件处理接口
     * 根据 EMQX 官方 Webhook 设计，统一处理所有客户端事件
     * 支持的事件类型：client.connected、client.disconnected 等
     */
    public void handleEvent(RoutingContext context) {
        try {
            // 解析请求体
            JsonObject body = parseRequestBody(context);
            if (body == null) {
                return;
            }
            String event = body.getString("event");
            String username = body.getString("username");
            log.debug("[handleEvent][收到事件: {} - {}]", event, username);

            // 根据事件类型进行分发处理
            switch (event) {
                case EVENT_CLIENT_CONNECTED:
                    handleClientConnected(body);
                    break;
                case EVENT_CLIENT_DISCONNECTED:
                    handleClientDisconnected(body);
                    break;
                default:
                    log.debug("[handleEvent][忽略事件: {}]", event);
                    break;
            }

            // EMQX Webhook 只需要 200 状态码，无需响应体
            context.response().setStatusCode(SUCCESS_STATUS_CODE).end();
        } catch (Exception e) {
            // TODO @haohao：body 可以打印出来
            log.error("[handleEvent][事件处理失败]", e);
            // 即使处理失败，也返回 200 避免EMQX重试
            context.response().setStatusCode(SUCCESS_STATUS_CODE).end();
        }
    }

    /**
     * 处理客户端连接事件
     */
    private void handleClientConnected(JsonObject body) {
        String username = body.getString("username");
        log.info("[handleClientConnected][设备上线: {}]", username);
        handleDeviceStateChange(username, true);
    }

    /**
     * 处理客户端断开连接事件
     */
    private void handleClientDisconnected(JsonObject body) {
        String username = body.getString("username");
        String reason = body.getString("reason");
        log.info("[handleClientDisconnected][设备下线: {} ({})]", username, reason);
        handleDeviceStateChange(username, false);
    }

    /**
     * 解析请求体
     *
     * @param context 路由上下文
     * @return 请求体JSON对象，解析失败时返回null
     */
    private JsonObject parseRequestBody(RoutingContext context) {
        try {
            JsonObject body = context.body().asJsonObject();
            if (body == null) {
                log.info("[parseRequestBody][请求体为空]");
                sendAuthResponse(context, RESULT_IGNORE, false, "请求体不能为空");
                return null;
            }
            return body;
        } catch (Exception e) {
            // TODO @haohao：最好把 body 打印出来；
            log.error("[parseRequestBody][解析请求体失败]", e);
            sendAuthResponse(context, RESULT_IGNORE, false, "请求体格式错误");
            return null;
        }
    }

    /**
     * 执行设备认证
     *
     * @param clientId 客户端ID
     * @param username 用户名
     * @param password 密码
     * @return 认证是否成功
     */
    private boolean performDeviceAuth(String clientId, String username, String password) {
        try {
            CommonResult<Boolean> result = deviceApi.authDevice(new IotDeviceAuthReqDTO()
                    .setClientId(clientId).setUsername(username).setPassword(password));
            result.checkError();
            return BooleanUtil.isTrue(result.getData());
        } catch (Exception e) {
            log.error("[performDeviceAuth][认证接口调用失败: {}]", username, e);
            throw e;
        }
    }

    /**
     * 处理设备状态变化
     *
     * @param username 用户名
     * @param online   是否在线
     */
    private void handleDeviceStateChange(String username, boolean online) {
        // 解析设备信息
        if (StrUtil.isEmpty(username) || "undefined".equals(username)) {
            return;
        }
        IotDeviceAuthUtils.DeviceInfo deviceInfo = IotDeviceAuthUtils.parseUsername(username);
        if (deviceInfo == null) {
            log.debug("[handleDeviceStateChange][跳过非设备连接: {}]", username);
            return;
        }

        try {
            // TODO @haohao：serverId 获取非空，可以忽略掉；
            String serverId = protocol.getServerId();
            if (StrUtil.isEmpty(serverId)) {
                log.error("[handleDeviceStateChange][获取服务器ID失败]");
                return;
            }

            // 构建设备状态消息
            IotDeviceMessage message = online ? IotDeviceMessage.buildStateOnline()
                    : IotDeviceMessage.buildStateOffline();
            // 发送消息到消息总线
            deviceMessageService.sendDeviceMessage(message,
                    deviceInfo.getProductKey(), deviceInfo.getDeviceName(), serverId);

            // TODO @haohao：online 不用翻译
            log.info("[handleDeviceStateChange][设备状态更新: {}/{} -> {}]",
                    deviceInfo.getProductKey(), deviceInfo.getDeviceName(),
                    online ? "在线" : "离线");
        } catch (Exception e) {
            log.error("[handleDeviceStateChange][发送设备状态消息失败: {}]", username, e);
        }
    }

    /**
     * 发送 EMQX 认证响应
     * 根据 EMQX 官方文档要求，必须返回 JSON 格式响应
     *
     * @param context     路由上下文
     * @param result      认证结果：allow、deny、ignore
     * @param isSuperuser 是否超级用户
     * @param message     日志消息（仅用于日志记录，不返回给EMQX）
     */
    private void sendAuthResponse(RoutingContext context, String result, boolean isSuperuser, String message) {
        // 构建符合 EMQX 官方规范的响应
        JsonObject response = new JsonObject()
                .put("result", result)
                .put("is_superuser", isSuperuser);

        // 可以根据业务需求添加客户端属性
        // response.put("client_attrs", new JsonObject().put("role", "device"));

        // 可以添加认证过期时间（可选）
        // response.put("expire_at", System.currentTimeMillis() / 1000 + 3600);

        // 记录详细的响应日志（message仅用于日志，不返回给EMQX）
        context.response()
                .setStatusCode(SUCCESS_STATUS_CODE)
                .putHeader("Content-Type", "application/json; charset=utf-8")
                .end(response.encode());
    }

}