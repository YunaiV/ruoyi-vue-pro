package cn.iocoder.yudao.module.iot.plugin.common.upstream;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.*;
import cn.iocoder.yudao.module.iot.plugin.common.config.IotPluginCommonProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;

/**
 * 设备数据 Upstream 上行客户端
 *
 * 通过 HTTP 调用远程的 IotDeviceUpstreamApi 接口
 *
 * @author haohao
 */
@RequiredArgsConstructor
@Slf4j
public class IotDeviceUpstreamClient implements IotDeviceUpstreamApi {

    public static final String URL_PREFIX = "/rpc-api/iot/device/upstream";

    private final IotPluginCommonProperties properties;

    private final RestTemplate restTemplate;

    @Override
    public CommonResult<Boolean> updateDeviceState(IotDeviceStateUpdateReqDTO updateReqDTO) {
        String url = properties.getUpstreamUrl() + URL_PREFIX + "/update-state";
        return doPost(url, updateReqDTO);
    }

    @Override
    public CommonResult<Boolean> reportDeviceEvent(IotDeviceEventReportReqDTO reportReqDTO) {
        String url = properties.getUpstreamUrl() + URL_PREFIX + "/report-event";
        return doPost(url, reportReqDTO);
    }

    // TODO @芋艿：待实现
    @Override
    public CommonResult<Boolean> registerDevice(IotDeviceRegisterReqDTO registerReqDTO) {
        return null;
    }

    // TODO @芋艿：待实现
    @Override
    public CommonResult<Boolean> registerSubDevice(IotDeviceRegisterSubReqDTO registerReqDTO) {
        return null;
    }

    // TODO @芋艿：待实现
    @Override
    public CommonResult<Boolean> addDeviceTopology(IotDeviceTopologyAddReqDTO addReqDTO) {
        return null;
    }

    @Override
    public CommonResult<Boolean> authenticateEmqxConnection(IotDeviceEmqxAuthReqDTO authReqDTO) {
        String url = properties.getUpstreamUrl() + URL_PREFIX + "/authenticate-emqx-connection";
        return doPost(url, authReqDTO);
    }

    @Override
    public CommonResult<Boolean> reportDeviceProperty(IotDevicePropertyReportReqDTO reportReqDTO) {
        String url = properties.getUpstreamUrl() + URL_PREFIX + "/report-property";
        return doPost(url, reportReqDTO);
    }

    @Override
    public CommonResult<Boolean> heartbeatPluginInstance(IotPluginInstanceHeartbeatReqDTO heartbeatReqDTO) {
        String url = properties.getUpstreamUrl() + URL_PREFIX + "/heartbeat-plugin-instance";
        return doPost(url, heartbeatReqDTO);
    }

    @SuppressWarnings("unchecked")
    private <T> CommonResult<Boolean> doPost(String url, T requestBody) {
        try {
            CommonResult<Boolean> result = restTemplate.postForObject(url, requestBody,
                    (Class<CommonResult<Boolean>>) (Class<?>) CommonResult.class);
            log.info("[doPost][url({}) requestBody({}) result({})]", url, requestBody, result);
            return result;
        } catch (Exception e) {
            log.error("[doPost][url({}) requestBody({}) 发生异常]", url, requestBody, e);
            return CommonResult.error(INTERNAL_SERVER_ERROR);
        }
    }

}
