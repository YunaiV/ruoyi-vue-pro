package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router;

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
 * IoT 网关 MQTT HTTP 认证处理器
 * <p>
 * 处理 EMQX 的认证请求和事件钩子
 *
 * @author 芋道源码
 */
@Slf4j
public class IotMqttHttpAuthHandler {

    /**
     * EMQX 认证接口
     */
    public void authenticate(RoutingContext context) {
        try {
            // 解析请求体
            JsonObject body = context.body().asJsonObject();
            if (body == null) {
                sendErrorResponse(context, 400, "请求体不能为空");
                return;
            }

            String clientid = body.getString("clientid");
            String username = body.getString("username");
            String password = body.getString("password");

            log.info("[authenticate][EMQX 设备认证请求][clientId: {}][username: {}]", clientid, username);

            // 参数校验
            if (StrUtil.isEmpty(clientid) || StrUtil.isEmpty(username) || StrUtil.isEmpty(password)) {
                log.warn("[authenticate][认证参数不完整][clientId: {}][username: {}]", clientid, username);
                sendErrorResponse(context, 400, "认证参数不完整");
                return;
            }

            // 执行设备认证
            IotDeviceCommonApi deviceApi = SpringUtil.getBean(IotDeviceCommonApi.class);
            CommonResult<Boolean> result = deviceApi.authDevice(new IotDeviceAuthReqDTO()
                    .setClientId(clientid)
                    .setUsername(username)
                    .setPassword(password));

            result.checkError();
            if (!BooleanUtil.isTrue(result.getData())) {
                log.warn("[authenticate][设备认证失败][clientId: {}][username: {}]", clientid, username);
                sendErrorResponse(context, 401, "设备认证失败");
                return;
            }

            log.info("[authenticate][设备认证成功][clientId: {}][username: {}]", clientid, username);
            sendSuccessResponse(context, "认证成功");

        } catch (Exception e) {
            log.error("[authenticate][设备认证异常]", e);
            sendErrorResponse(context, 500, "认证服务异常");
        }
    }

    /**
     * EMQX 客户端连接事件钩子
     */
    public void connected(RoutingContext context) {
        try {
            // 解析请求体
            JsonObject body = context.body().asJsonObject();
            if (body == null) {
                sendErrorResponse(context, 400, "请求体不能为空");
                return;
            }

            String clientid = body.getString("clientid");
            String username = body.getString("username");
            Long timestamp = body.getLong("timestamp");

            log.info("[connected][设备连接事件][clientId: {}][username: {}]", clientid, username);

            handleDeviceStateChange(username, true);
            sendSuccessResponse(context, "处理成功");

        } catch (Exception e) {
            log.error("[connected][处理设备连接事件失败]", e);
            sendErrorResponse(context, 500, "处理失败");
        }
    }

    /**
     * EMQX 客户端断开连接事件钩子
     */
    public void disconnected(RoutingContext context) {
        try {
            // 解析请求体
            JsonObject body = context.body().asJsonObject();
            if (body == null) {
                sendErrorResponse(context, 400, "请求体不能为空");
                return;
            }

            String clientid = body.getString("clientid");
            String username = body.getString("username");
            String reason = body.getString("reason");
            Long timestamp = body.getLong("timestamp");

            log.info("[disconnected][设备断开连接事件][clientId: {}][username: {}][reason: {}]",
                    clientid, username, reason);

            handleDeviceStateChange(username, false);
            sendSuccessResponse(context, "处理成功");

        } catch (Exception e) {
            log.error("[disconnected][处理设备断开连接事件失败]", e);
            sendErrorResponse(context, 500, "处理失败");
        }
    }

    /**
     * 处理设备状态变化
     *
     * @param username 用户名
     * @param online   是否在线
     */
    private void handleDeviceStateChange(String username, boolean online) {
        if (StrUtil.isEmpty(username) || "undefined".equals(username)) {
            log.warn("[handleDeviceStateChange][用户名为空或未定义][username: {}]", username);
            return;
        }

        // 解析设备信息
        IotDeviceAuthUtils.DeviceInfo deviceInfo = IotDeviceAuthUtils.parseUsername(username);
        if (deviceInfo == null) {
            log.warn("[handleDeviceStateChange][无法解析设备信息][username: {}]", username);
            return;
        }

        try {
            // 获取服务器 ID
            String serverId = "mqtt_auth_gateway";

            // 构建设备状态消息
            IotDeviceMessageService deviceMessageService = SpringUtil.getBean(IotDeviceMessageService.class);
            IotDeviceMessage message;
            if (online) {
                message = IotDeviceMessage.buildStateOnline();
                log.info("[handleDeviceStateChange][发送设备上线消息成功][username: {}]", username);
            } else {
                message = IotDeviceMessage.buildStateOffline();
                log.info("[handleDeviceStateChange][发送设备下线消息成功][username: {}]", username);
            }

            // 发送消息到消息总线
            deviceMessageService.sendDeviceMessage(message,
                    deviceInfo.getProductKey(), deviceInfo.getDeviceName(), serverId);
        } catch (Exception e) {
            log.error("[handleDeviceStateChange][发送设备状态消息失败][username: {}][online: {}]",
                    username, online, e);
        }
    }

    /**
     * 发送成功响应
     */
    private void sendSuccessResponse(RoutingContext context, String message) {
        context.response()
                .setStatusCode(200)
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