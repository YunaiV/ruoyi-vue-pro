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

/**
 * IoT Emqx 连接认证的 Vert.x Handler
 * <a href="https://docs.emqx.com/zh/emqx/latest/access-control/authn/http.html">...</a>
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

        JsonObject json = routingContext.body().asJsonObject();
        String clientId = json.getString("clientid");
        String username = json.getString("username");
        String password = json.getString("password");

        IotDeviceEmqxAuthReqDTO authReqDTO = buildDeviceEmqxAuthReqDTO(clientId, username, password);

        CommonResult<Boolean> authResult = deviceUpstreamApi.authenticateEmqxConnection(authReqDTO);
        if (authResult.getCode() != 0 || !authResult.getData()) {
            denyAccess(routingContext);
            return;
        }
        IotPluginCommonUtils.writeJson(routingContext, "{\"result\": \"allow\"}");
    }

    private void denyAccess(RoutingContext routingContext) {
        IotPluginCommonUtils.writeJson(routingContext, "{\"result\": \"deny\"}");
    }

    private IotDeviceEmqxAuthReqDTO buildDeviceEmqxAuthReqDTO(String clientId, String username, String password) {
        return new IotDeviceEmqxAuthReqDTO().setClientId(clientId).setUsername(username).setPassword(password);
    }

}