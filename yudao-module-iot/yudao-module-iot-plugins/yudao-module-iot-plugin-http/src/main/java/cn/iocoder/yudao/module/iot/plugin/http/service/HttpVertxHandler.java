package cn.iocoder.yudao.module.iot.plugin.http.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDevicePropertyReportReqDTO;
import io.vertx.core.Handler;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class HttpVertxHandler implements Handler<RoutingContext> {

    private final IotDeviceUpstreamApi deviceDataApi;

    public HttpVertxHandler(IotDeviceUpstreamApi deviceDataApi) {
        this.deviceDataApi = deviceDataApi;
    }

    @Override
    public void handle(RoutingContext ctx) {
        String productKey = ctx.pathParam("productKey");
        String deviceName = ctx.pathParam("deviceName");

        // TODO @haohao：requestBody.asJsonObject() 貌似天然就是 json 对象哈？
        RequestBody requestBody = ctx.body();
        JSONObject jsonData;
        try {
            jsonData = JSONUtil.parseObj(requestBody.asJsonObject());
        } catch (Exception e) {
            log.error("[HttpVertxHandler] 请求数据解析失败", e);
            ctx.response().setStatusCode(400)
                    .putHeader("Content-Type", "application/json; charset=UTF-8")
                    .end(createResponseJson(400, null, null,
                            "请求数据不是合法的 JSON 格式: " + e.getMessage(),
                            "thing.event.property.post", "1.0").toString());
            return;
        }

        String id = jsonData.getStr("id");

        try {
            IotDevicePropertyReportReqDTO reportReqDTO = IotDevicePropertyReportReqDTO.builder()
                    .productKey(productKey)
                    .deviceName(deviceName)
                    .properties((Map<String, Object>) requestBody.asJsonObject().getMap().get("properties"))
                    .build();

            deviceDataApi.reportDeviceProperty(reportReqDTO);

            ctx.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json; charset=UTF-8")
                    .end(createResponseJson(200, new JSONObject(), id, "success",
                            "thing.event.property.post", "1.0").toString());

        } catch (Exception e) {
            log.error("[HttpVertxHandler] 上报属性数据失败", e);
            ctx.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json; charset=UTF-8")
                    .end(createResponseJson(500, new JSONObject(), id,
                            "The format of result is error!",
                            "thing.event.property.post", "1.0").toString());
        }
    }

    private JSONObject createResponseJson(int code, JSONObject data, String id,
                                          String message, String method, String version) {
        JSONObject res = new JSONObject();
        res.set("code", code);
        res.set("data", data != null ? data : new JSONObject());
        res.set("id", id);
        res.set("message", message);
        res.set("method", method);
        res.set("version", version);
        return res;
    }
}
