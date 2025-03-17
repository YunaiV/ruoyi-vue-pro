package cn.iocoder.yudao.module.iot.plugin.common.downstream.router;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.IotDeviceOtaUpgradeReqDTO;
import cn.iocoder.yudao.module.iot.plugin.common.downstream.IotDeviceDownstreamHandler;
import cn.iocoder.yudao.module.iot.plugin.common.pojo.IotStandardResponse;
import cn.iocoder.yudao.module.iot.plugin.common.util.IotPluginCommonUtils;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;

/**
 * IoT 设备 OTA 升级 Vertx Handler
 * <p>
 * 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class IotDeviceOtaUpgradeVertxHandler implements Handler<RoutingContext> {

    public static final String PATH = "/ota/:productKey/:deviceName/upgrade";
    public static final String METHOD = "ota.device.upgrade";

    private final IotDeviceDownstreamHandler deviceDownstreamHandler;

    @Override
    public void handle(RoutingContext routingContext) {
        // 1. 解析参数
        IotDeviceOtaUpgradeReqDTO reqDTO;
        try {
            String productKey = routingContext.pathParam("productKey");
            String deviceName = routingContext.pathParam("deviceName");
            JsonObject body = routingContext.body().asJsonObject();
            String requestId = body.getString("requestId");
            Long firmwareId = body.getLong("firmwareId");
            String version = body.getString("version");
            String signMethod = body.getString("signMethod");
            String fileSign = body.getString("fileSign");
            Long fileSize = body.getLong("fileSize");
            String fileUrl = body.getString("fileUrl");
            String information = body.getString("information");
            reqDTO = ((IotDeviceOtaUpgradeReqDTO) new IotDeviceOtaUpgradeReqDTO()
                    .setRequestId(requestId).setProductKey(productKey).setDeviceName(deviceName))
                    .setFirmwareId(firmwareId).setVersion(version)
                    .setSignMethod(signMethod).setFileSign(fileSign).setFileSize(fileSize).setFileUrl(fileUrl)
                    .setInformation(information);
        } catch (Exception e) {
            log.error("[handle][路径参数({}) 解析参数失败]", routingContext.pathParams(), e);
            IotStandardResponse errorResponse = IotStandardResponse.error(
                    null, METHOD, BAD_REQUEST.getCode(), BAD_REQUEST.getMsg());
            IotPluginCommonUtils.writeJsonResponse(routingContext, errorResponse);
            return;
        }

        // 2. 调用处理器
        try {
            CommonResult<Boolean> result = deviceDownstreamHandler.upgradeDeviceOta(reqDTO);

            // 3. 响应结果
            // TODO @haohao：可以考虑 IotStandardResponse.of(requestId, method, CommonResult)
            IotStandardResponse response = result.isSuccess() ?
                    IotStandardResponse.success(reqDTO.getRequestId(), METHOD, result.getData())
                    :IotStandardResponse.error(reqDTO.getRequestId(), METHOD, result.getCode(), result.getMsg());
            IotPluginCommonUtils.writeJsonResponse(routingContext, response);
        } catch (Exception e) {
            log.error("[handle][请求参数({}) OTA 升级异常]", reqDTO, e);
            // TODO @haohao：可以考虑 IotStandardResponse.of(requestId, method, ErrorCode)
            IotStandardResponse errorResponse = IotStandardResponse.error(
                    reqDTO.getRequestId(), METHOD, INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR.getMsg());
            IotPluginCommonUtils.writeJsonResponse(routingContext, errorResponse);
        }
    }
}
