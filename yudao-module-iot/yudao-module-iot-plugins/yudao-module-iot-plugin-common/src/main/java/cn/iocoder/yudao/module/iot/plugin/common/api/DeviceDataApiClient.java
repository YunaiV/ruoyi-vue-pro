package cn.iocoder.yudao.module.iot.plugin.common.api;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.DeviceDataApi;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceEventReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDevicePropertyReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceStatusUpdateReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Slf4j
public class DeviceDataApiClient implements DeviceDataApi {

    private final RestTemplate restTemplate;
    private final String deviceDataUrl;

    // 可以通过构造器把 RestTemplate 和 baseUrl 注入进来
    public DeviceDataApiClient(RestTemplate restTemplate, String deviceDataUrl) {
        this.restTemplate = restTemplate;
        this.deviceDataUrl = deviceDataUrl;
    }

    @Override
    public CommonResult<Boolean> updateDeviceStatus(IotDeviceStatusUpdateReqDTO updateReqDTO) {
        // 示例：如果对应的远程地址是 /rpc-api/iot/device-data/update-status
        String url = deviceDataUrl + "/rpc-api/iot/device-data/update-status";
        return doPost(url, updateReqDTO, "updateDeviceStatus");
    }

    @Override
    public CommonResult<Boolean> reportDeviceEventData(IotDeviceEventReportReqDTO reportReqDTO) {
        // 示例：如果对应的远程地址是 /rpc-api/iot/device-data/report-event
        String url = deviceDataUrl + "/rpc-api/iot/device-data/report-event";
        return doPost(url, reportReqDTO, "reportDeviceEventData");
    }

    @Override
    public CommonResult<Boolean> reportDevicePropertyData(IotDevicePropertyReportReqDTO reportReqDTO) {
        // 示例：如果对应的远程地址是 /rpc-api/iot/device-data/report-property
        String url = deviceDataUrl + "/rpc-api/iot/device-data/report-property";
        return doPost(url, reportReqDTO, "reportDevicePropertyData");
    }

    /**
     * 将与远程服务交互的通用逻辑抽取成一个私有方法
     */
    private <T> CommonResult<Boolean> doPost(String url, T requestBody, String actionName) {
        log.info("[{}] Sending request to URL: {}", actionName, url);
        try {
            // 这里指定返回类型为 CommonResult<?>，根据后台服务返回的实际结构做调整
            restTemplate.postForObject(url, requestBody, CommonResult.class);
            return success(true);
        } catch (Exception e) {
            log.error("[{}] Error sending request to URL: {}", actionName, url, e);
            return CommonResult.error(400, "Request error: " + e.getMessage());
        }
    }
}
