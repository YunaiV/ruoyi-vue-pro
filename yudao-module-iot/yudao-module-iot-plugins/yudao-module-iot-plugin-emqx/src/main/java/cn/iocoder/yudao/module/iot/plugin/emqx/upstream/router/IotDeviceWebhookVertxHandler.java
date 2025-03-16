package cn.iocoder.yudao.module.iot.plugin.emqx.upstream.router;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotDeviceStateUpdateReqDTO;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.plugin.common.util.IotPluginCommonUtils;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * IoT EMQX Webhook 事件处理的 Vert.x Handler
 *
 * 参考：<a href="https://docs.emqx.com/zh/emqx/latest/data-integration/webhook.html">EMQX Webhook</a>
 *
 * 注意：该处理器需要返回特定格式：{"result": "success"} 或 {"result": "error"}，
 *      以符合 EMQX Webhook 插件的要求，因此不使用 IotStandardResponse 实体类。
 *
 * @author haohao
 */
@RequiredArgsConstructor
@Slf4j
public class IotDeviceWebhookVertxHandler implements Handler<RoutingContext> {

    public static final String PATH = "/mqtt/webhook";

    private final IotDeviceUpstreamApi deviceUpstreamApi;

    @Override
    public void handle(RoutingContext routingContext) {
        try {
            // 解析请求体
            JsonObject json = routingContext.body().asJsonObject();
            String event = json.getString("event");
            String clientId = json.getString("clientid");
            String username = json.getString("username");

            // 处理不同的事件类型
            switch (event) {
                case "client.connected":
                    handleClientConnected(clientId, username);
                    break;
                case "client.disconnected":
                    handleClientDisconnected(clientId, username);
                    break;
                default:
                    log.info("[handle][未处理的 Webhook 事件] event={}, clientId={}, username={}", event, clientId, username);
                    break;
            }

            // 返回成功响应
            // 注意：这里必须返回 {"result": "success"} 格式，以符合 EMQX Webhook 插件的要求
            IotPluginCommonUtils.writeJsonResponse(routingContext, Collections.singletonMap("result", "success"));
        } catch (Exception e) {
            log.error("[handle][处理 Webhook 事件异常]", e);
            // 注意：这里必须返回 {"result": "error"} 格式，以符合 EMQX Webhook 插件的要求
            IotPluginCommonUtils.writeJsonResponse(routingContext, Collections.singletonMap("result", "error"));
        }
    }

    /**
     * 处理客户端连接事件
     *
     * @param clientId 客户端ID
     * @param username 用户名
     */
    private void handleClientConnected(String clientId, String username) {
        // 解析产品标识和设备名称
        if (StrUtil.isEmpty(username) || "undefined".equals(username)) {
            log.warn("[handleClientConnected][客户端连接事件，但用户名为空] clientId={}", clientId);
            return;
        }
        String[] parts = parseUsername(username);
        if (parts == null) {
            return;
        }

        // 更新设备状态为在线
        IotDeviceStateUpdateReqDTO updateReqDTO = new IotDeviceStateUpdateReqDTO();
        updateReqDTO.setProductKey(parts[1]);
        updateReqDTO.setDeviceName(parts[0]);
        updateReqDTO.setState(IotDeviceStateEnum.ONLINE.getState());
        updateReqDTO.setProcessId(IotPluginCommonUtils.getProcessId());
        updateReqDTO.setReportTime(LocalDateTime.now());
        CommonResult<Boolean> result = deviceUpstreamApi.updateDeviceState(updateReqDTO);
        if (result.getCode() != 0 || !result.getData()) {
            log.error("[handleClientConnected][更新设备状态为在线失败] clientId={}, username={}, code={}, msg={}",
                    clientId, username, result.getCode(), result.getMsg());
        } else {
            log.info("[handleClientConnected][更新设备状态为在线成功] clientId={}, username={}", clientId, username);
        }
    }

    /**
     * 处理客户端断开连接事件
     *
     * @param clientId 客户端ID
     * @param username 用户名
     */
    private void handleClientDisconnected(String clientId, String username) {
        // 解析产品标识和设备名称
        if (StrUtil.isEmpty(username) || "undefined".equals(username)) {
            log.warn("[handleClientDisconnected][客户端断开连接事件，但用户名为空] clientId={}", clientId);
            return;
        }
        String[] parts = parseUsername(username);
        if (parts == null) {
            return;
        }

        // 更新设备状态为离线
        IotDeviceStateUpdateReqDTO offlineReqDTO = new IotDeviceStateUpdateReqDTO();
        offlineReqDTO.setProductKey(parts[1]);
        offlineReqDTO.setDeviceName(parts[0]);
        offlineReqDTO.setState(IotDeviceStateEnum.OFFLINE.getState());
        offlineReqDTO.setProcessId(IotPluginCommonUtils.getProcessId());
        offlineReqDTO.setReportTime(LocalDateTime.now());
        CommonResult<Boolean> offlineResult = deviceUpstreamApi.updateDeviceState(offlineReqDTO);
        if (offlineResult.getCode() != 0 || !offlineResult.getData()) {
            log.error("[handleClientDisconnected][更新设备状态为离线失败] clientId={}, username={}, code={}, msg={}",
                    clientId, username, offlineResult.getCode(), offlineResult.getMsg());
        } else {
            log.info("[handleClientDisconnected][更新设备状态为离线成功] clientId={}, username={}", clientId, username);
        }
    }

    /**
     * 解析用户名，格式为 deviceName&productKey
     *
     * @param username 用户名
     * @return 解析结果，[0] 为 deviceName，[1] 为 productKey，解析失败返回 null
     */
    private String[] parseUsername(String username) {
        if (StrUtil.isEmpty(username)) {
            return null;
        }
        String[] parts = username.split("&");
        if (parts.length != 2) {
            log.warn("[parseUsername][用户名格式({})不正确，无法解析产品标识和设备名称]", username);
            return null;
        }
        return parts;
    }

}