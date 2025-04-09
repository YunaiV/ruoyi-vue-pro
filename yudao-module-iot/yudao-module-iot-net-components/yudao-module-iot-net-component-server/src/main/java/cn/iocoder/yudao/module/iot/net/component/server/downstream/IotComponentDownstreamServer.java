package cn.iocoder.yudao.module.iot.net.component.server.downstream;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.*;
import cn.iocoder.yudao.module.iot.net.component.core.downstream.IotDeviceDownstreamHandler;
import cn.iocoder.yudao.module.iot.net.component.server.config.IotNetComponentServerProperties;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 组件下行服务器，接收来自主程序的控制指令
 *
 * @author haohao
 */
@Slf4j
public class IotComponentDownstreamServer {

    public static final String SERVICE_INVOKE_PATH = "/sys/:productKey/:deviceName/thing/service/:identifier";
    public static final String PROPERTY_SET_PATH = "/sys/:productKey/:deviceName/thing/service/property/set";
    public static final String PROPERTY_GET_PATH = "/sys/:productKey/:deviceName/thing/service/property/get";
    public static final String CONFIG_SET_PATH = "/sys/:productKey/:deviceName/thing/service/config/set";
    public static final String OTA_UPGRADE_PATH = "/sys/:productKey/:deviceName/thing/service/ota/upgrade";

    private final Vertx vertx;
    private final HttpServer server;
    private final IotNetComponentServerProperties properties;
    private final IotDeviceDownstreamHandler downstreamHandler;

    public IotComponentDownstreamServer(IotNetComponentServerProperties properties,
            IotDeviceDownstreamHandler downstreamHandler) {
        this.properties = properties;
        this.downstreamHandler = downstreamHandler;
        // 创建 Vertx 实例
        this.vertx = Vertx.vertx();
        // 创建 Router 实例
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create()); // 处理 Body

        // 服务调用路由
        router.post(SERVICE_INVOKE_PATH).handler(this::handleServiceInvoke);
        // 属性设置路由
        router.post(PROPERTY_SET_PATH).handler(this::handlePropertySet);
        // 属性获取路由
        router.post(PROPERTY_GET_PATH).handler(this::handlePropertyGet);
        // 配置设置路由
        router.post(CONFIG_SET_PATH).handler(this::handleConfigSet);
        // OTA 升级路由
        router.post(OTA_UPGRADE_PATH).handler(this::handleOtaUpgrade);

        // 创建 HttpServer 实例
        this.server = vertx.createHttpServer().requestHandler(router);
    }

    /**
     * 启动服务器
     */
    public void start() {
        log.info("[start][开始启动下行服务器]");
        server.listen(properties.getDownstreamPort())
                .toCompletionStage()
                .toCompletableFuture()
                .join();
        log.info("[start][下行服务器启动完成，端口({})]", server.actualPort());
    }

    /**
     * 停止服务器
     */
    public void stop() {
        log.info("[stop][开始关闭下行服务器]");
        try {
            // 关闭 HTTP 服务器
            if (server != null) {
                server.close()
                        .toCompletionStage()
                        .toCompletableFuture()
                        .join();
            }

            // 关闭 Vertx 实例
            if (vertx != null) {
                vertx.close()
                        .toCompletionStage()
                        .toCompletableFuture()
                        .join();
            }
            log.info("[stop][下行服务器关闭完成]");
        } catch (Exception e) {
            log.error("[stop][下行服务器关闭异常]", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取服务器端口
     *
     * @return 端口号
     */
    public int getPort() {
        return server.actualPort();
    }

    /**
     * 处理服务调用请求
     */
    private void handleServiceInvoke(RoutingContext ctx) {
        try {
            // 解析路径参数
            String productKey = ctx.pathParam("productKey");
            String deviceName = ctx.pathParam("deviceName");
            String identifier = ctx.pathParam("identifier");

            // 解析请求体
            JsonObject body = ctx.body().asJsonObject();
            String requestId = body.getString("requestId", IdUtil.fastSimpleUUID());
            Object params = body.getMap().get("params");

            // 创建请求对象
            IotDeviceServiceInvokeReqDTO reqDTO = new IotDeviceServiceInvokeReqDTO();
            reqDTO.setRequestId(requestId);
            reqDTO.setProductKey(productKey);
            reqDTO.setDeviceName(deviceName);
            reqDTO.setIdentifier(identifier);
            reqDTO.setParams((Map<String, Object>) params);

            // 调用处理器
            CommonResult<Boolean> result = downstreamHandler.invokeDeviceService(reqDTO);

            // 响应结果
            ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(Json.encode(result));
        } catch (Exception e) {
            log.error("[handleServiceInvoke][处理服务调用请求失败]", e);
            ctx.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end(Json.encode(CommonResult.error(500, "处理服务调用请求失败：" + e.getMessage())));
        }
    }

    /**
     * 处理属性设置请求
     */
    private void handlePropertySet(RoutingContext ctx) {
        try {
            // 解析路径参数
            String productKey = ctx.pathParam("productKey");
            String deviceName = ctx.pathParam("deviceName");

            // 解析请求体
            JsonObject body = ctx.body().asJsonObject();
            String requestId = body.getString("requestId", IdUtil.fastSimpleUUID());
            Object properties = body.getMap().get("properties");

            // 创建请求对象
            IotDevicePropertySetReqDTO reqDTO = new IotDevicePropertySetReqDTO();
            reqDTO.setRequestId(requestId);
            reqDTO.setProductKey(productKey);
            reqDTO.setDeviceName(deviceName);
            reqDTO.setProperties((Map<String, Object>) properties);

            // 调用处理器
            CommonResult<Boolean> result = downstreamHandler.setDeviceProperty(reqDTO);

            // 响应结果
            ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(Json.encode(result));
        } catch (Exception e) {
            log.error("[handlePropertySet][处理属性设置请求失败]", e);
            ctx.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end(Json.encode(CommonResult.error(500, "处理属性设置请求失败：" + e.getMessage())));
        }
    }

    /**
     * 处理属性获取请求
     */
    private void handlePropertyGet(RoutingContext ctx) {
        try {
            // 解析路径参数
            String productKey = ctx.pathParam("productKey");
            String deviceName = ctx.pathParam("deviceName");

            // 解析请求体
            JsonObject body = ctx.body().asJsonObject();
            String requestId = body.getString("requestId", IdUtil.fastSimpleUUID());
            Object identifiers = body.getMap().get("identifiers");

            // 创建请求对象
            IotDevicePropertyGetReqDTO reqDTO = new IotDevicePropertyGetReqDTO();
            reqDTO.setRequestId(requestId);
            reqDTO.setProductKey(productKey);
            reqDTO.setDeviceName(deviceName);
            reqDTO.setIdentifiers((List<String>) identifiers);

            // 调用处理器
            CommonResult<Boolean> result = downstreamHandler.getDeviceProperty(reqDTO);

            // 响应结果
            ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(Json.encode(result));
        } catch (Exception e) {
            log.error("[handlePropertyGet][处理属性获取请求失败]", e);
            ctx.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end(Json.encode(CommonResult.error(500, "处理属性获取请求失败：" + e.getMessage())));
        }
    }

    /**
     * 处理配置设置请求
     */
    private void handleConfigSet(RoutingContext ctx) {
        try {
            // 解析路径参数
            String productKey = ctx.pathParam("productKey");
            String deviceName = ctx.pathParam("deviceName");

            // 解析请求体
            JsonObject body = ctx.body().asJsonObject();
            String requestId = body.getString("requestId", IdUtil.fastSimpleUUID());
            Object config = body.getMap().get("config");

            // 创建请求对象
            IotDeviceConfigSetReqDTO reqDTO = new IotDeviceConfigSetReqDTO();
            reqDTO.setRequestId(requestId);
            reqDTO.setProductKey(productKey);
            reqDTO.setDeviceName(deviceName);
            reqDTO.setConfig((Map<String, Object>) config);

            // 调用处理器
            CommonResult<Boolean> result = downstreamHandler.setDeviceConfig(reqDTO);

            // 响应结果
            ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(Json.encode(result));
        } catch (Exception e) {
            log.error("[handleConfigSet][处理配置设置请求失败]", e);
            ctx.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end(Json.encode(CommonResult.error(500, "处理配置设置请求失败：" + e.getMessage())));
        }
    }

    /**
     * 处理 OTA 升级请求
     */
    private void handleOtaUpgrade(RoutingContext ctx) {
        try {
            // 解析路径参数
            String productKey = ctx.pathParam("productKey");
            String deviceName = ctx.pathParam("deviceName");

            // 解析请求体
            JsonObject body = ctx.body().asJsonObject();
            String requestId = body.getString("requestId", IdUtil.fastSimpleUUID());
            Object data = body.getMap().get("data");

            // 创建请求对象
            IotDeviceOtaUpgradeReqDTO reqDTO = new IotDeviceOtaUpgradeReqDTO();
            reqDTO.setRequestId(requestId);
            reqDTO.setProductKey(productKey);
            reqDTO.setDeviceName(deviceName);

            // 数据采用 IotDeviceOtaUpgradeReqDTO.build 方法转换
            if (data instanceof Map) {
                IotDeviceOtaUpgradeReqDTO builtDTO = IotDeviceOtaUpgradeReqDTO.build((Map<?, ?>) data);
                reqDTO.setFirmwareId(builtDTO.getFirmwareId());
                reqDTO.setVersion(builtDTO.getVersion());
                reqDTO.setSignMethod(builtDTO.getSignMethod());
                reqDTO.setFileSign(builtDTO.getFileSign());
                reqDTO.setFileSize(builtDTO.getFileSize());
                reqDTO.setFileUrl(builtDTO.getFileUrl());
                reqDTO.setInformation(builtDTO.getInformation());
            }

            // 调用处理器
            CommonResult<Boolean> result = downstreamHandler.upgradeDeviceOta(reqDTO);

            // 响应结果
            ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(Json.encode(result));
        } catch (Exception e) {
            log.error("[handleOtaUpgrade][处理OTA升级请求失败]", e);
            ctx.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end(Json.encode(CommonResult.error(500, "处理OTA升级请求失败：" + e.getMessage())));
        }
    }
}