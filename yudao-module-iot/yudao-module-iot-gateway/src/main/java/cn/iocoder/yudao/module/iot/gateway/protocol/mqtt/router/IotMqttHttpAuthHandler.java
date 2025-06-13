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
     * 认证成功状态码
     */
    private static final int SUCCESS_STATUS_CODE = 200;

    /**
     * 参数错误状态码
     */
    private static final int BAD_REQUEST_STATUS_CODE = 400;

    /**
     * 认证失败状态码
     */
    private static final int UNAUTHORIZED_STATUS_CODE = 401;

    /**
     * 服务器错误状态码
     */
    private static final int INTERNAL_ERROR_STATUS_CODE = 500;

    /**
     * MQTT 协议实例，用于获取服务器ID
     */
    private final IotMqttUpstreamProtocol protocol;

    /**
     * 构造器
     *
     * @param protocol MQTT 协议实例
     */
    public IotMqttHttpAuthHandler(IotMqttUpstreamProtocol protocol) {
        this.protocol = protocol;
    }

    /**
     * EMQX 认证接口
     */
    public void authenticate(RoutingContext context) {
        try {
            // 解析请求体
            JsonObject body = parseRequestBody(context);
            if (body == null) {
                return;
            }

            String clientid = body.getString("clientid");
            String username = body.getString("username");
            String password = body.getString("password");

            log.debug("[authenticate][EMQX 设备认证, clientId: {}, username: {}]", clientid, username);

            // 参数校验
            if (!validateAuthParams(context, clientid, username, password)) {
                return;
            }

            // 执行设备认证
            if (!performDeviceAuth(context, clientid, username, password)) {
                return;
            }

            log.debug("[authenticate][设备认证成功, clientId: {}, username: {}]", clientid, username);
            sendSuccessResponse(context, "认证成功");

        } catch (Exception e) {
            log.error("[authenticate][设备认证异常, 详细信息: {}]", e.getMessage(), e);
            sendErrorResponse(context, INTERNAL_ERROR_STATUS_CODE, "认证服务异常");
        }
    }

    /**
     * EMQX 客户端连接事件钩子
     */
    public void connected(RoutingContext context) {
        try {
            // 解析请求体
            JsonObject body = parseRequestBody(context);
            if (body == null) {
                return;
            }

            String clientid = body.getString("clientid");
            String username = body.getString("username");
            Long timestamp = body.getLong("timestamp");

            log.debug("[connected][设备连接, clientId: {}, username: {}, timestamp: {}]",
                    clientid, username, timestamp);

            handleDeviceStateChange(username, true, "设备连接");
            sendSuccessResponse(context, "处理成功");

        } catch (Exception e) {
            log.error("[connected][处理设备连接事件失败, 详细信息: {}]", e.getMessage(), e);
            sendErrorResponse(context, INTERNAL_ERROR_STATUS_CODE, "处理失败");
        }
    }

    /**
     * EMQX 客户端断开连接事件钩子
     */
    public void disconnected(RoutingContext context) {
        try {
            // 解析请求体
            JsonObject body = parseRequestBody(context);
            if (body == null) {
                return;
            }

            String clientid = body.getString("clientid");
            String username = body.getString("username");
            String reason = body.getString("reason");
            Long timestamp = body.getLong("timestamp");

            log.debug("[disconnected][设备断开连接, clientId: {}, username: {}, reason: {}, timestamp: {}]",
                    clientid, username, reason, timestamp);

            handleDeviceStateChange(username, false, "设备断开连接，原因：" + reason);
            sendSuccessResponse(context, "处理成功");

        } catch (Exception e) {
            log.error("[disconnected][处理设备断开连接事件失败, 详细信息: {}]", e.getMessage(), e);
            sendErrorResponse(context, INTERNAL_ERROR_STATUS_CODE, "处理失败");
        }
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
                log.warn("[parseRequestBody][请求体为空]");
                sendErrorResponse(context, BAD_REQUEST_STATUS_CODE, "请求体不能为空");
                return null;
            }
            return body;
        } catch (Exception e) {
            log.error("[parseRequestBody][解析请求体失败]", e);
            sendErrorResponse(context, BAD_REQUEST_STATUS_CODE, "请求体格式错误");
            return null;
        }
    }

    /**
     * 验证认证参数
     *
     * @param context  路由上下文
     * @param clientid 客户端ID
     * @param username 用户名
     * @param password 密码
     * @return 验证是否通过
     */
    private boolean validateAuthParams(RoutingContext context, String clientid, String username, String password) {
        if (StrUtil.hasEmpty(clientid, username, password)) {
            log.warn("[validateAuthParams][认证参数不完整, clientId: {}, username: {}, password: {}]",
                    clientid, username, StrUtil.isNotEmpty(password) ? "***" : "空");
            sendErrorResponse(context, BAD_REQUEST_STATUS_CODE, "认证参数不完整");
            return false;
        }
        return true;
    }

    /**
     * 执行设备认证
     *
     * @param context  路由上下文
     * @param clientid 客户端ID
     * @param username 用户名
     * @param password 密码
     * @return 认证是否成功
     */
    private boolean performDeviceAuth(RoutingContext context, String clientid, String username, String password) {
        try {
            IotDeviceCommonApi deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
            CommonResult<Boolean> result = deviceApi.authDevice(new IotDeviceAuthReqDTO()
                    .setClientId(clientid)
                    .setUsername(username)
                    .setPassword(password));

            result.checkError();
            if (!BooleanUtil.isTrue(result.getData())) {
                log.warn("[performDeviceAuth][设备认证失败, clientId: {}, username: {}]", clientid, username);
                sendErrorResponse(context, UNAUTHORIZED_STATUS_CODE, DEVICE_AUTH_FAIL.getMsg());
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("[performDeviceAuth][设备认证异常, clientId: {}, username: {}]", clientid, username, e);
            sendErrorResponse(context, INTERNAL_ERROR_STATUS_CODE, "认证服务异常");
            return false;
        }
    }

    /**
     * 处理设备状态变化
     *
     * @param username   用户名
     * @param online     是否在线
     * @param actionDesc 操作描述
     */
    private void handleDeviceStateChange(String username, boolean online, String actionDesc) {
        if (StrUtil.isEmpty(username) || "undefined".equals(username)) {
            log.warn("[handleDeviceStateChange][用户名为空或'undefined', username: {}, action: {}]",
                    username, actionDesc);
            return;
        }

        // 解析设备信息
        IotDeviceAuthUtils.DeviceInfo deviceInfo = IotDeviceAuthUtils.parseUsername(username);
        if (deviceInfo == null) {
            log.warn("[handleDeviceStateChange][无法从 username({}) 解析设备信息, action: {}]",
                    username, actionDesc);
            return;
        }

        try {
            // 从协议实例获取服务器 ID
            String serverId = protocol.getServerId();
            if (StrUtil.isEmpty(serverId)) {
                log.error("[handleDeviceStateChange][获取服务器ID失败, username: {}, action: {}]",
                        username, actionDesc);
                return;
            }

            // 构建设备状态消息
            IotDeviceMessageService deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
            IotDeviceMessage message;
            if (online) {
                message = IotDeviceMessage.buildStateOnline();
                log.debug("[handleDeviceStateChange][发送设备上线消息, username: {}, serverId: {}]",
                        username, serverId);
            } else {
                message = IotDeviceMessage.buildStateOffline();
                log.debug("[handleDeviceStateChange][发送设备下线消息, username: {}, serverId: {}]",
                        username, serverId);
            }

            // 发送消息到消息总线
            deviceMessageService.sendDeviceMessage(message,
                    deviceInfo.getProductKey(), deviceInfo.getDeviceName(), serverId);

            log.info("[handleDeviceStateChange][{}处理成功, productKey: {}, deviceName: {}, serverId: {}]",
                    actionDesc, deviceInfo.getProductKey(), deviceInfo.getDeviceName(), serverId);

        } catch (Exception e) {
            log.error("[handleDeviceStateChange][发送设备状态消息失败, username: {}, online: {}, action: {}]",
                    username, online, actionDesc, e);
        }
    }

    /**
     * 发送成功响应
     */
    private void sendSuccessResponse(RoutingContext context, String message) {
        context.response()
                .setStatusCode(SUCCESS_STATUS_CODE)
                .putHeader("Content-Type", "text/plain; charset=utf-8")
                .end(message);
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(RoutingContext context, int statusCode, String message) {
        context.response()
                .setStatusCode(statusCode)
                .putHeader("Content-Type", "text/plain; charset=utf-8")
                .end(message);
    }
}