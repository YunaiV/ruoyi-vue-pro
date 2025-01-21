package cn.iocoder.yudao.module.iot.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.api.device.DeviceDataApi;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDevicePropertyReportReqDTO;
import io.vertx.core.Handler;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpVertxHandler implements Handler<RoutingContext> {

    private final DeviceDataApi deviceDataApi;

    public HttpVertxHandler(DeviceDataApi deviceDataApi) {
        this.deviceDataApi = deviceDataApi;
    }

    @Override
    public void handle(RoutingContext ctx) {
        String productKey = ctx.pathParam("productKey");
        String deviceName = ctx.pathParam("deviceName");
        RequestBody requestBody = ctx.body();

        JSONObject jsonData;
        try {
            jsonData = JSONUtil.parseObj(requestBody.asJsonObject());
        } catch (Exception e) {
            JSONObject res = createResponseJson(
                    400,
                    new JSONObject(),
                    null,
                    "请求数据不是合法的 JSON 格式: " + e.getMessage(),
                    "thing.event.property.post",
                    "1.0");
            ctx.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json; charset=UTF-8")
                    .end(res.toString());
            return;
        }

        String id = jsonData.getStr("id", null);

        try {
            // 调用主程序的接口保存数据
            IotDevicePropertyReportReqDTO createDTO = IotDevicePropertyReportReqDTO.builder()
                    .productKey(productKey)
                    .deviceName(deviceName)
                    .params(jsonData) // TODO 芋艿：这块要优化
                    .build();
            deviceDataApi.reportDevicePropertyData(createDTO);

            // 构造成功响应内容
            JSONObject successRes = createResponseJson(
                    200,
                    new JSONObject(),
                    id,
                    "success",
                    "thing.event.property.post",
                    "1.0");
            ctx.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json; charset=UTF-8")
                    .end(successRes.toString());
        } catch (Exception e) {
            JSONObject errorRes = createResponseJson(
                    500,
                    new JSONObject(),
                    id,
                    "The format of result is error!",
                    "thing.event.property.post",
                    "1.0");
            ctx.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json; charset=UTF-8")
                    .end(errorRes.toString());
        }
    }

    /**
     * 创建标准化的响应 JSON 对象
     *
     * @param code    响应状态码（业务层面的）
     * @param data    返回的数据对象（JSON）
     * @param id      请求的 id（可选）
     * @param message 返回的提示信息
     * @param method  返回的 method 标识
     * @param version 返回的版本号
     * @return 构造好的 JSON 对象
     */
    private JSONObject createResponseJson(int code, JSONObject data, String id, String message, String method,
                                          String version) {
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
