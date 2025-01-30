package cn.iocoder.yudao.module.iot.plugin.common.upstream;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotDeviceEventReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotDevicePropertyReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotDeviceStateUpdateReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.control.upstream.IotPluginInstanceHeartbeatReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 设备数据 Upstream 上行客户端
 *
 * 通过 HTTP 调用远程的 IotDeviceUpstreamApi 接口
 *
 * @author haohao
 */
@Slf4j
public class IotDeviceUpstreamClient implements IotDeviceUpstreamApi {

    public static final String URL_PREFIX = "/rpc-api/iot/device/upstream";

    private final RestTemplate restTemplate;
    private final String deviceDataUrl;

    // 可以通过构造器把 RestTemplate 和 baseUrl 注入进来
    // TODO @haohao：可以用 lombok 简化
    public IotDeviceUpstreamClient(RestTemplate restTemplate, String deviceDataUrl) {
        this.restTemplate = restTemplate;
        this.deviceDataUrl = deviceDataUrl;
    }

    // TODO @haohao：返回结果，不用 CommonResult 哈。
    @Override
    public CommonResult<Boolean> updateDeviceState(IotDeviceStateUpdateReqDTO updateReqDTO) {
        String url = deviceDataUrl + URL_PREFIX + "/update-state";
        return doPost(url, updateReqDTO, "updateDeviceState");
    }

    @Override
    public CommonResult<Boolean> reportDeviceEvent(IotDeviceEventReportReqDTO reportReqDTO) {
        String url = deviceDataUrl + URL_PREFIX + "/report-event";
        return doPost(url, reportReqDTO, "reportDeviceEventData");
    }

    @Override
    public CommonResult<Boolean> reportDeviceProperty(IotDevicePropertyReportReqDTO reportReqDTO) {
        String url = deviceDataUrl + URL_PREFIX + "/report-property";
        return doPost(url, reportReqDTO, "reportDevicePropertyData");
    }

    @Override
    public CommonResult<Boolean> heartbeatPluginInstance(IotPluginInstanceHeartbeatReqDTO heartbeatReqDTO) {
        String url = deviceDataUrl + URL_PREFIX + "/heartbeat-plugin-instance";
        return doPost(url, heartbeatReqDTO, "heartbeatPluginInstance");
    }

    // TODO @haohao：未来可能有 get 类型哈
    /**
     * 将与远程服务交互的通用逻辑抽取成一个私有方法
     */
    private <T> CommonResult<Boolean> doPost(String url, T requestBody, String actionName) {
        log.info("[{}] Sending request to URL: {}", actionName, url);
        try {
            // 这里指定返回类型为 CommonResult<?>，根据后台服务返回的实际结构做调整
            restTemplate.postForObject(url, requestBody, CommonResult.class);
            // TODO @haohao：check 结果，是否成功
            return success(true);
        } catch (Exception e) {
            log.error("[{}] Error sending request to URL: {}", actionName, url, e);
            return CommonResult.error(400, "Request error: " + e.getMessage());
        }
    }

}
