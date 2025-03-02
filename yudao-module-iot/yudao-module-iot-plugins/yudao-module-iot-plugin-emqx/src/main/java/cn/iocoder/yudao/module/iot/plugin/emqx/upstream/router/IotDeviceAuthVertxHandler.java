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
 * IoT Emqx 连接认证的 Vert.x Handler
 * <a href=
 * "https://docs.emqx.com/zh/emqx/latest/access-control/authn/http.html">...</a>
 *
 * @author haohao
 */
@RequiredArgsConstructor
@Slf4j
public class IotDeviceAuthVertxHandler implements Handler<RoutingContext> {

    public static final String PATH = "/mqtt/auth";

    private final IotDeviceUpstreamApi deviceUpstreamApi;

    @Override
    @SuppressWarnings("unchecked")
    public void handle(RoutingContext routingContext) {
        try {
            JsonObject json = routingContext.body().asJsonObject();
            String clientId = json.getString("clientid");
            String username = json.getString("username");
            String password = json.getString("password");

            // 构建认证请求DTO
            IotDeviceEmqxAuthReqDTO authReqDTO = new IotDeviceEmqxAuthReqDTO()
                    .setClientId(clientId)
                    .setUsername(username)
                    .setPassword(password);

            // 调用认证API
            CommonResult<Boolean> authResult = deviceUpstreamApi.authenticateEmqxConnection(authReqDTO);
            if (authResult.getCode() != 0 || !authResult.getData()) {
                IotPluginCommonUtils.writeJson(routingContext, Collections.singletonMap("result", "deny"));
                return;
            }

            IotPluginCommonUtils.writeJson(routingContext, Collections.singletonMap("result", "allow"));
        } catch (Exception e) {
            log.error("[handle][EMQX认证异常]", e);
            IotPluginCommonUtils.writeJson(routingContext, Collections.singletonMap("result", "deny"));
        }
    }
}