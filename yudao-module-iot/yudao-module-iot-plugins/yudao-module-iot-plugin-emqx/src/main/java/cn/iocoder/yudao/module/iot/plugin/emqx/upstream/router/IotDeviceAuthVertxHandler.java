package cn.iocoder.yudao.module.iot.plugin.emqx.upstream.router;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotDeviceEmqxAuthReqDTO;
import cn.iocoder.yudao.module.iot.plugin.common.util.IotPluginCommonUtils;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

/**
 * IoT EMQX 连接认证的 Vert.x Handler
 *
 * 参考：<a href="https://docs.emqx.com/zh/emqx/latest/access-control/authn/http.html">EMQX HTTP</a>
 *
 * 注意：该处理器需要返回特定格式：{"result": "allow"} 或 {"result": "deny"}，
 *      以符合 EMQX 认证插件的要求，因此不使用 IotStandardResponse 实体类
 *
 * @author haohao
 */
@RequiredArgsConstructor
@Slf4j
public class IotDeviceAuthVertxHandler implements Handler<RoutingContext> {

    public static final String PATH = "/mqtt/auth";

    private final IotDeviceUpstreamApi deviceUpstreamApi;

    @Override
    public void handle(RoutingContext routingContext) {
        try {
            // 构建认证请求 DTO
            JsonObject json = routingContext.body().asJsonObject();
            String clientId = json.getString("clientid");
            String username = json.getString("username");
            String password = json.getString("password");
            IotDeviceEmqxAuthReqDTO authReqDTO = new IotDeviceEmqxAuthReqDTO()
                    .setClientId(clientId)
                    .setUsername(username)
                    .setPassword(password);

            // 调用认证 API
            CommonResult<Boolean> authResult = deviceUpstreamApi.authenticateEmqxConnection(authReqDTO);
            if (authResult.getCode() != 0 || !authResult.getData()) {
                // 注意：这里必须返回 {"result": "deny"} 格式，以符合 EMQX 认证插件的要求
                IotPluginCommonUtils.writeJsonResponse(routingContext, Collections.singletonMap("result", "deny"));
                return;
            }

            // 响应结果
            // 注意：这里必须返回 {"result": "allow"} 格式，以符合 EMQX 认证插件的要求
            IotPluginCommonUtils.writeJsonResponse(routingContext, Collections.singletonMap("result", "allow"));
        } catch (Exception e) {
            log.error("[handle][EMQX 认证异常]", e);
            // 注意：这里必须返回 {"result": "deny"} 格式，以符合 EMQX 认证插件的要求
            IotPluginCommonUtils.writeJsonResponse(routingContext, Collections.singletonMap("result", "deny"));
        }
    }

}