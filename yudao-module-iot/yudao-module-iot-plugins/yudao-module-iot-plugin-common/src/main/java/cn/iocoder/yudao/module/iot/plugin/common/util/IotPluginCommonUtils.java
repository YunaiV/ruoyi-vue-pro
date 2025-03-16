package cn.iocoder.yudao.module.iot.plugin.common.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.plugin.common.pojo.IotStandardResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import org.springframework.http.MediaType;

/**
 * IoT 插件的通用工具类
 *
 * @author 芋道源码
 */
public class IotPluginCommonUtils {

    /**
     * 流程实例的进程编号
     */
    private static String processId;

    public static String getProcessId() {
        if (StrUtil.isEmpty(processId)) {
            initProcessId();
        }
        return processId;
    }

    private synchronized static void initProcessId() {
        processId = String.format("%s@%d@%s", // IP@PID@${uuid}
                SystemUtil.getHostInfo().getAddress(), SystemUtil.getCurrentPID(), IdUtil.fastSimpleUUID());
    }

    /**
     * 将对象转换为JSON字符串后写入HTTP响应
     *
     * @param routingContext 路由上下文
     * @param data           数据对象
     */
    @SuppressWarnings("deprecation")
    public static void writeJsonResponse(RoutingContext routingContext, Object data) {
        routingContext.response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .end(JsonUtils.toJsonString(data));
    }

    /**
     * 生成标准JSON格式的响应并写入HTTP响应（基于IotStandardResponse）
     * <p>
     * 推荐使用此方法，统一MQTT和HTTP的响应格式。使用方式：
     *
     * <pre>
     * // 成功响应
     * IotStandardResponse response = IotStandardResponse.success(requestId, method, data);
     * IotPluginCommonUtils.writeJsonResponse(routingContext, response);
     *
     * // 错误响应
     * IotStandardResponse errorResponse = IotStandardResponse.error(requestId, method, code, message);
     * IotPluginCommonUtils.writeJsonResponse(routingContext, errorResponse);
     * </pre>
     *
     * @param routingContext 路由上下文
     * @param response       IotStandardResponse响应对象
     */
    @SuppressWarnings("deprecation")
    public static void writeJsonResponse(RoutingContext routingContext, IotStandardResponse response) {
        routingContext.response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .end(JsonUtils.toJsonString(response));
    }

}
