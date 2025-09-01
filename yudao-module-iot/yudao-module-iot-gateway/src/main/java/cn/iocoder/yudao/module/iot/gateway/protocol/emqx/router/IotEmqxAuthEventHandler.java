package cn.iocoder.yudao.module.iot.gateway.protocol.emqx.router;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 EMQX 认证事件处理器
 * <p>
 * 为 EMQX 提供 HTTP 接口服务，包括：
 * 1. 设备认证接口 - 对应 EMQX HTTP 认证插件
 * 2. 设备事件处理接口 - 对应 EMQX Webhook 事件通知
 *
 * @author 芋道源码
 */
@Slf4j
public class IotEmqxAuthEventHandler {

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

    private final String serverId;

    private final IotDeviceMessageService deviceMessageService;

    private final IotDeviceCommonApi deviceApi;

    public IotEmqxAuthEventHandler(String serverId) {
        this.serverId = serverId;
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
    }

    /**
     * EMQX 认证接口
     */
    public void handleAuth(RoutingContext context) {
        try {
            // 1. 参数校验
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
                sendAuthResponse(context, RESULT_DENY);
                return;
            }

            // 2. 执行认证
            boolean authResult = handleDeviceAuth(clientId, username, password);
            log.info("[handleAuth][设备认证结果: {} -> {}]", username, authResult);
            if (authResult) {
                sendAuthResponse(context, RESULT_ALLOW);
            } else {
                sendAuthResponse(context, RESULT_DENY);
            }
        } catch (Exception e) {
            log.error("[handleAuth][设备认证异常]", e);
            sendAuthResponse(context, RESULT_IGNORE);
        }
    }

    /**
     * EMQX 统一事件处理接口：根据 EMQX 官方 Webhook 设计，统一处理所有客户端事件
     * 支持的事件类型：client.connected、client.disconnected 等
     */
    public void handleEvent(RoutingContext context) {
        JsonObject body = null;
        try {
            // 1. 解析请求体
            body = parseRequestBody(context);
            if (body == null) {
                return;
            }
            String event = body.getString("event");
            String username = body.getString("username");
            log.debug("[handleEvent][收到事件: {} - {}]", event, username);

            // 2. 根据事件类型进行分发处理
            switch (event) {
                case EVENT_CLIENT_CONNECTED:
                    handleClientConnected(body);
                    break;
                case EVENT_CLIENT_DISCONNECTED:
                    handleClientDisconnected(body);
                    break;
                default:
                    break;
            }

            // EMQX Webhook 只需要 200 状态码，无需响应体
            context.response().setStatusCode(SUCCESS_STATUS_CODE).end();
        } catch (Exception e) {
            log.error("[handleEvent][事件处理失败][body={}]", body != null ? body.encode() : "null", e);
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
                sendAuthResponse(context, RESULT_IGNORE);
                return null;
            }
            return body;
        } catch (Exception e) {
            log.error("[parseRequestBody][body({}) 解析请求体失败]", context.body().asString(), e);
            sendAuthResponse(context, RESULT_IGNORE);
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
    private boolean handleDeviceAuth(String clientId, String username, String password) {
        try {
            CommonResult<Boolean> result = deviceApi.authDevice(new IotDeviceAuthReqDTO()
                    .setClientId(clientId).setUsername(username).setPassword(password));
            result.checkError();
            return BooleanUtil.isTrue(result.getData());
        } catch (Exception e) {
            log.error("[handleDeviceAuth][设备({}) 认证接口调用失败]", username, e);
            throw e;
        }
    }

    /**
     * 处理设备状态变化
     *
     * @param username 用户名
     * @param online   是否在线 true 在线 false 离线
     */
    private void handleDeviceStateChange(String username, boolean online) {
        // 1. 解析设备信息
        IotDeviceAuthUtils.DeviceInfo deviceInfo = IotDeviceAuthUtils.parseUsername(username);
        if (deviceInfo == null) {
            log.debug("[handleDeviceStateChange][跳过非设备({})连接]", username);
            return;
        }

        try {
            // 2. 构建设备状态消息
            IotDeviceMessage message = online ? IotDeviceMessage.buildStateUpdateOnline()
                    : IotDeviceMessage.buildStateOffline();

            // 3. 发送设备状态消息
            deviceMessageService.sendDeviceMessage(message,
                    deviceInfo.getProductKey(), deviceInfo.getDeviceName(), serverId);
        } catch (Exception e) {
            log.error("[handleDeviceStateChange][发送设备状态消息失败: {}]", username, e);
        }
    }

    /**
     * 发送 EMQX 认证响应
     * 根据 EMQX 官方文档要求，必须返回 JSON 格式响应
     *
     * @param context 路由上下文
     * @param result  认证结果：allow、deny、ignore
     */
    private void sendAuthResponse(RoutingContext context, String result) {
        // 构建符合 EMQX 官方规范的响应
        JsonObject response = new JsonObject()
                .put("result", result)
                .put("is_superuser", false);

        // 可以根据业务需求添加客户端属性
        // response.put("client_attrs", new JsonObject().put("role", "device"));

        // 可以添加认证过期时间（可选）
        // response.put("expire_at", System.currentTimeMillis() / 1000 + 3600);

        context.response()
                .setStatusCode(SUCCESS_STATUS_CODE)
                .putHeader("Content-Type", "application/json; charset=utf-8")
                .end(response.encode());
    }

}