package cn.iocoder.yudao.module.iot.gateway.protocol.emqx.handler.upstream;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterRespDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.emqx.IotEmqxProtocol;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.gateway.util.IotMqttTopicUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

/**
 * IoT 网关 EMQX 认证事件处理器
 * <p>
 * 为 EMQX 提供 HTTP 接口服务，包括：
 * 1. 设备认证接口 - 对应 EMQX HTTP 认证插件 {@link #handleAuth(RoutingContext)}
 * 2. 设备事件处理接口 - 对应 EMQX Webhook 事件通知 {@link #handleEvent(RoutingContext)}
 * 3. 设备 ACL 权限接口 - 对应 EMQX HTTP ACL 插件 {@link #handleAcl(RoutingContext)}
 * 4. 设备注册接口 - 集成一型一密设备注册 {@link #handleDeviceRegister(RoutingContext, String, String)}
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
     * EMQX 事件类型常量 - 客户端连接
     */
    private static final String EVENT_CLIENT_CONNECTED = "client.connected";
    /**
     * EMQX 事件类型常量 - 客户端断开连接
     */
    private static final String EVENT_CLIENT_DISCONNECTED = "client.disconnected";

    /**
     * 认证类型标识 - 设备注册
     */
    private static final String AUTH_TYPE_REGISTER = "|authType=register|";

    private final String serverId;

    private final IotEmqxProtocol protocol;

    private final IotDeviceMessageService deviceMessageService;
    private final IotDeviceCommonApi deviceApi;

    public IotEmqxAuthEventHandler(String serverId, IotEmqxProtocol protocol) {
        this.serverId = serverId;
        this.protocol = protocol;
        this.deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
        this.deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
    }

    // ========== 认证处理 ==========

    /**
     * EMQX 认证接口
     */
    public void handleAuth(RoutingContext context) {
        JsonObject body = null;
        try {
            // 1. 参数校验
            body = parseRequestBody(context);
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

            // 2.1 情况一：判断是否为注册请求
            if (StrUtil.endWith(clientId, AUTH_TYPE_REGISTER)) {
                handleDeviceRegister(context, username, password);
                return;
            }

            // 2.2 情况二：执行认证
            boolean authResult = handleDeviceAuth(clientId, username, password);
            log.info("[handleAuth][设备认证结果: {} -> {}]", username, authResult);
            if (authResult) {
                sendAuthResponse(context, RESULT_ALLOW);
            } else {
                sendAuthResponse(context, RESULT_DENY);
            }
        } catch (Exception e) {
            log.error("[handleAuth][设备认证异常][body={}]", body, e);
            sendAuthResponse(context, RESULT_IGNORE);
        }
    }

    /**
     * 解析认证接口请求体
     * <p>
     * 认证接口解析失败时返回 JSON 格式响应（包含 result 字段）
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

        // 回复响应
        context.response()
                .setStatusCode(SUCCESS_STATUS_CODE)
                .putHeader("Content-Type", "application/json; charset=utf-8")
                .end(response.encode());
    }

    // ========== ACL 处理 ==========

    /**
     * EMQX ACL 接口
     * <p>
     * 用于 EMQX 的 HTTP ACL 插件校验设备的 publish/subscribe 权限。
     * 若请求参数无法识别，则返回 ignore 交给 EMQX 自身 ACL 规则处理。
     */
    public void handleAcl(RoutingContext context) {
        JsonObject body = null;
        try {
            // 1.1 解析请求体
            body = parseRequestBody(context);
            if (body == null) {
                return;
            }
            String username = body.getString("username");
            String topic = body.getString("topic");
            if (StrUtil.hasBlank(username, topic)) {
                log.info("[handleAcl][ACL 参数不完整: username={}, topic={}]", username, topic);
                sendAuthResponse(context, RESULT_IGNORE);
                return;
            }
            // 1.2 解析设备身份
            IotDeviceIdentity deviceInfo = IotDeviceAuthUtils.parseUsername(username);
            if (deviceInfo == null) {
                sendAuthResponse(context, RESULT_IGNORE);
                return;
            }
            // 1.3 解析 ACL 动作（兼容多种 EMQX 版本/插件字段）
            Boolean subscribe = parseAclSubscribeFlag(body);
            if (subscribe == null) {
                sendAuthResponse(context, RESULT_IGNORE);
                return;
            }

            // 2. 执行 ACL 校验
            boolean allowed = subscribe
                    ? IotMqttTopicUtils.isTopicSubscribeAllowed(topic, deviceInfo.getProductKey(), deviceInfo.getDeviceName())
                    : IotMqttTopicUtils.isTopicPublishAllowed(topic, deviceInfo.getProductKey(), deviceInfo.getDeviceName());
            sendAuthResponse(context, allowed ? RESULT_ALLOW : RESULT_DENY);
        } catch (Exception e) {
            log.error("[handleAcl][ACL 处理失败][body={}]", body, e);
            sendAuthResponse(context, RESULT_IGNORE);
        }
    }

    /**
     * 解析 ACL 动作类型：订阅/发布
     *
     * @param body ACL 请求体
     * @return true 订阅；false 发布；null 不识别
     */
    private static Boolean parseAclSubscribeFlag(JsonObject body) {
        // 1. action 字段（常见为 publish/subscribe）
        String action = body.getString("action");
        if (StrUtil.isNotBlank(action)) {
            String lower = action.toLowerCase(Locale.ROOT);
            if (lower.contains("sub")) {
                return true;
            }
            if (lower.contains("pub")) {
                return false;
            }
        }

        // 2. access 字段：可能是数字或字符串
        Integer access = body.getInteger("access");
        if (access != null) {
            if (access == 1) {
                return true;
            }
            if (access == 2) {
                return false;
            }
        }
        String accessText = body.getString("access");
        if (StrUtil.isNotBlank(accessText)) {
            String lower = accessText.toLowerCase(Locale.ROOT);
            if (lower.contains("sub")) {
                return true;
            }
            if (lower.contains("pub")) {
                return false;
            }
            if (StrUtil.isNumeric(accessText)) {
                int value = Integer.parseInt(accessText);
                if (value == 1) {
                    return true;
                }
                if (value == 2) {
                    return false;
                }
            }
        }
        return null;
    }

    // ========== 事件处理 ==========

    /**
     * EMQX 统一事件处理接口：根据 EMQX 官方 Webhook 设计，统一处理所有客户端事件
     * 支持的事件类型：client.connected、client.disconnected 等
     */
    public void handleEvent(RoutingContext context) {
        JsonObject body = null;
        try {
            // 1. 解析请求体
            body = parseEventRequestBody(context);
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

            // 3. EMQX Webhook 只需要 200 状态码，无需响应体
            context.response().setStatusCode(SUCCESS_STATUS_CODE).end();
        } catch (Exception e) {
            log.error("[handleEvent][事件处理失败][body={}]", body, e);
            // 即使处理失败，也返回 200 避免 EMQX 重试
            context.response().setStatusCode(SUCCESS_STATUS_CODE).end();
        }
    }

    /**
     * 解析事件接口请求体
     * <p>
     * 事件接口解析失败时仅返回 200 状态码，无响应体（符合 EMQX Webhook 规范）
     *
     * @param context 路由上下文
     * @return 请求体JSON对象，解析失败时返回null
     */
    private JsonObject parseEventRequestBody(RoutingContext context) {
        try {
            JsonObject body = context.body().asJsonObject();
            if (body == null) {
                log.info("[parseEventRequestBody][请求体为空]");
                context.response().setStatusCode(SUCCESS_STATUS_CODE).end();
                return null;
            }
            return body;
        } catch (Exception e) {
            log.error("[parseEventRequestBody][body({}) 解析请求体失败]", context.body().asString(), e);
            context.response().setStatusCode(SUCCESS_STATUS_CODE).end();
            return null;
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
     * 处理设备状态变化
     *
     * @param username 用户名
     * @param online   是否在线 true 在线 false 离线
     */
    private void handleDeviceStateChange(String username, boolean online) {
        // 1. 解析设备信息
        IotDeviceIdentity deviceInfo = IotDeviceAuthUtils.parseUsername(username);
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

    // ========= 注册处理 =========

    /**
     * 处理设备注册请求（一型一密）
     *
     * @param context  路由上下文
     * @param username 用户名
     * @param password 密码（签名）
     */
    private void handleDeviceRegister(RoutingContext context, String username, String password) {
        try {
            // 1. 解析设备信息
            IotDeviceIdentity deviceInfo = IotDeviceAuthUtils.parseUsername(username);
            if (deviceInfo == null) {
                log.warn("[handleDeviceRegister][设备注册失败: 无法解析 username={}]", username);
                sendAuthResponse(context, RESULT_DENY);
                return;
            }

            // 2. 调用注册 API
            IotDeviceRegisterReqDTO params = new IotDeviceRegisterReqDTO()
                    .setProductKey(deviceInfo.getProductKey())
                    .setDeviceName(deviceInfo.getDeviceName())
                    .setSign(password);
            CommonResult<IotDeviceRegisterRespDTO> result = deviceApi.registerDevice(params);
            result.checkError();

            // 3. 允许连接
            log.info("[handleDeviceRegister][设备注册成功: {}]", username);
            sendAuthResponse(context, RESULT_ALLOW);

            // 4. 延迟 5 秒发送注册结果（等待设备连接成功并完成订阅）
            sendRegisterResultMessage(username, result.getData());
        } catch (Exception e) {
            log.warn("[handleDeviceRegister][设备注册失败: {}, 错误: {}]", username, e.getMessage());
            sendAuthResponse(context, RESULT_DENY);
        }
    }

    /**
     * 发送注册结果消息给设备
     * <p>
     * 注意：延迟 5 秒发送，等待设备连接成功并完成订阅。
     *
     * @param username 用户名
     * @param result   注册结果
     */
    @SuppressWarnings("DataFlowIssue")
    private void sendRegisterResultMessage(String username, IotDeviceRegisterRespDTO result) {
        IotDeviceIdentity deviceInfo = IotDeviceAuthUtils.parseUsername(username);
        Assert.notNull(deviceInfo, "设备信息不能为空");
        try {
            // 1.1 构建响应消息
            String method = IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod();
            IotDeviceMessage responseMessage = IotDeviceMessage.replyOf(null, method, result, 0, null);
            // 1.2 序列化消息
            byte[] encodedData = deviceMessageService.serializeDeviceMessage(responseMessage,
                    cn.iocoder.yudao.module.iot.core.enums.IotSerializeTypeEnum.JSON);
            // 1.3 构建响应主题
            String replyTopic = IotMqttTopicUtils.buildTopicByMethod(method,
                    deviceInfo.getProductKey(), deviceInfo.getDeviceName(), true);

            // 2. 构建响应主题，并延迟发布（等待设备连接成功并完成订阅）
            protocol.publishDelayMessage(replyTopic, encodedData, 5000);
            log.info("[sendRegisterResultMessage][发送注册结果: topic={}]", replyTopic);
        } catch (Exception e) {
            log.error("[sendRegisterResultMessage][发送注册结果失败: {}]", username, e);
        }
    }

}
