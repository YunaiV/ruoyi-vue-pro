package cn.iocoder.yudao.module.iot.plugin.common.api;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.api.device.DeviceDataApi;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceEventReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDevicePropertyReportReqDTO;
import cn.iocoder.yudao.module.iot.api.device.dto.IotDeviceStatusUpdateReqDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 用于通过 {@link RestTemplate} 向远程 IoT 服务发送设备数据相关的请求，
 * 包括设备状态更新、事件数据上报、属性数据上报等操作。
 */
@Slf4j
@RequiredArgsConstructor
public class DeviceDataApiClient implements DeviceDataApi {

    /**
     * 用于发送 HTTP 请求的工具
     */
    private final RestTemplate restTemplate;

    /**
     * 远程 IoT 服务的基础 URL
     * 例如：http://127.0.0.1:8080
     */
    private final String deviceDataUrl;

    // TODO @haohao：返回结果，不用 CommonResult 哈。
    @Override
    public CommonResult<Boolean> updateDeviceStatus(IotDeviceStatusUpdateReqDTO updateReqDTO) {
        String url = deviceDataUrl + "/rpc-api/iot/device-data/update-status";
        return doPost(url, updateReqDTO, "updateDeviceStatus");
    }

    @Override
    public CommonResult<Boolean> reportDeviceEventData(IotDeviceEventReportReqDTO reportReqDTO) {
        String url = deviceDataUrl + "/rpc-api/iot/device-data/report-event";
        return doPost(url, reportReqDTO, "reportDeviceEventData");
    }

    @Override
    public CommonResult<Boolean> reportDevicePropertyData(IotDevicePropertyReportReqDTO reportReqDTO) {
        String url = deviceDataUrl + "/rpc-api/iot/device-data/report-property";
        return doPost(url, reportReqDTO, "reportDevicePropertyData");
    }

    
    /**
     * 发送 GET 请求
     *
     * @param <T>         请求体类型
     * @param url         请求 URL
     * @param requestBody 请求体
     * @param actionName  操作名称
     * @return 响应结果
     */
    private <T> CommonResult<Boolean> doGet(String url, T requestBody, String actionName) {
        log.info("[{}] Sending request to URL: {}", actionName, url);
        try {
            CommonResult<?> response = restTemplate.getForObject(url, CommonResult.class);
            if (response != null && response.isSuccess()) {
                return success(true);
            } else {
                log.warn("[{}] Request to URL: {} failed with response: {}", actionName, url, response);
                return CommonResult.error(500, "Request failed");
            }
        } catch (Exception e) {
            log.error("[{}] Error sending request to URL: {}", actionName, url, e);
            return CommonResult.error(400, "Request error: " + e.getMessage());
        }
    }

    /**
     * 发送 POST 请求
     *
     * @param <T>         请求体类型
     * @param url         请求 URL
     * @param requestBody 请求体
     * @param actionName  操作名称
     * @return 响应结果
     */
    private <T> CommonResult<Boolean> doPost(String url, T requestBody, String actionName) {
        log.info("[{}] Sending request to URL: {}", actionName, url);
        try {
            CommonResult<?> response = restTemplate.postForObject(url, requestBody, CommonResult.class);
            if (response != null && response.isSuccess()) {
                return success(true);
            } else {
                log.warn("[{}] Request to URL: {} failed with response: {}", actionName, url, response);
                return CommonResult.error(500, "Request failed");
            }
        } catch (Exception e) {
            log.error("[{}] Error sending request to URL: {}", actionName, url, e);
            return CommonResult.error(400, "Request error: " + e.getMessage());
        }
    }

}
