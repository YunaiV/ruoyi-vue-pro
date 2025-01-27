package cn.iocoder.yudao.module.iot.plugin.common.api;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceEventReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDevicePropertyReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceStatusUpdateReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

// TODO @haohao：类注释，写一下，比较好
// TODO @haohao：类名要改下
@Slf4j
public class DeviceDataApiClient implements IotDeviceUpstreamApi {

    public static final String URL_PREFIX = "/rpc-api/iot/device/upstream";

    private final RestTemplate restTemplate;
    private final String deviceDataUrl;

    // 可以通过构造器把 RestTemplate 和 baseUrl 注入进来
    // TODO @haohao：可以用 lombok 简化
    public DeviceDataApiClient(RestTemplate restTemplate, String deviceDataUrl) {
        this.restTemplate = restTemplate;
        this.deviceDataUrl = deviceDataUrl;
    }

    // TODO @haohao：返回结果，不用 CommonResult 哈。
    @Override
    public CommonResult<Boolean> updateDeviceStatus(IotDeviceStatusUpdateReqDTO updateReqDTO) {
        String url = deviceDataUrl + URL_PREFIX + "/update-status";
        return doPost(url, updateReqDTO, "updateDeviceStatus");
    }

    @Override
    public CommonResult<Boolean> reportDeviceEventData(IotDeviceEventReportReqDTO reportReqDTO) {
        String url = deviceDataUrl + URL_PREFIX + "/report-event";
        return doPost(url, reportReqDTO, "reportDeviceEventData");
    }

    @Override
    public CommonResult<Boolean> reportDevicePropertyData(IotDevicePropertyReportReqDTO reportReqDTO) {
        String url = deviceDataUrl + URL_PREFIX + "/report-property";
        return doPost(url, reportReqDTO, "reportDevicePropertyData");
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
