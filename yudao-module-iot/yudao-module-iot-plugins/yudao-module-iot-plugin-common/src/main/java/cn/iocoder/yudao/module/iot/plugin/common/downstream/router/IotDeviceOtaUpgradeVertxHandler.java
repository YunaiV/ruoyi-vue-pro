package cn.iocoder.yudao.module.iot.plugin.common.downstream.router;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.dto.control.downstream.IotDeviceOtaUpgradeReqDTO;
import cn.iocoder.yudao.module.iot.plugin.common.downstream.IotDeviceDownstreamHandler;
import cn.iocoder.yudao.module.iot.plugin.common.util.IotPluginCommonUtils;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.vertx.core.json.JsonObject;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;

@Slf4j
@RequiredArgsConstructor
public class IotDeviceOtaUpgradeVertxHandler implements Handler<RoutingContext> {

    public static final String PATH = "/ota/:productKey/:deviceName/upgrade";

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
            IotPluginCommonUtils.writeJson(routingContext, CommonResult.error(BAD_REQUEST));
            return;
        }

        // 2. 调用处理器
        try {
            CommonResult<Boolean> result = deviceDownstreamHandler.upgradeDeviceOta(reqDTO);
            IotPluginCommonUtils.writeJson(routingContext, result);
        } catch (Exception e) {
            log.error("[handle][请求参数({}) OTA 升级异常]", reqDTO, e);
            IotPluginCommonUtils.writeJson(routingContext, CommonResult.error(INTERNAL_SERVER_ERROR));
        }
    }
}
