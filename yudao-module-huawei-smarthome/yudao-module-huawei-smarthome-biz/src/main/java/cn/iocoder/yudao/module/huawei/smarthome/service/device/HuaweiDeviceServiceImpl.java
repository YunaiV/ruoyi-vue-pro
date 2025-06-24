package cn.iocoder.yudao.module.huawei.smarthome.service.device;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.huawei.smarthome.dto.device.DeviceControlReqDTO;
import cn.iocoder.yudao.module.huawei.smarthome.dto.device.DeviceGetReqDTO;
import cn.iocoder.yudao.module.huawei.smarthome.dto.device.DeviceListBySpaceReqDTO;
import cn.iocoder.yudao.module.huawei.smarthome.dto.device.DeviceListRespDTO;
import cn.iocoder.yudao.module.huawei.smarthome.framework.core.HuaweiSmartHomeAuthClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HuaweiDeviceServiceImpl implements HuaweiDeviceService {

    @Resource
    private HuaweiSmartHomeAuthClient huaweiSmartHomeAuthClient;

    private static final String LIST_DEVICES_BY_SPACE_PATH_FORMAT = "/openapi/asset/v1/space/%s/devices";
    private static final String GET_DEVICES_PATH = "/openapi/asset/v1/devices"; // Query param: deviceId=xx&deviceId=yy
    private static final String CONTROL_DEVICE_PATH_FORMAT = "/openapi/asset/v1/device/%s/command";

    @Override
    public DeviceListRespDTO listDevicesBySpace(DeviceListBySpaceReqDTO listReqDTO) throws IOException {
        String path = String.format(LIST_DEVICES_BY_SPACE_PATH_FORMAT, listReqDTO.getSpaceId());
        String responseBody = huaweiSmartHomeAuthClient.get(path);
        return JsonUtils.parseObject(responseBody, DeviceListRespDTO.class);
    }

    @Override
    public DeviceListRespDTO getDevices(DeviceGetReqDTO getReqDTO) throws IOException {
        if (CollectionUtils.isEmpty(getReqDTO.getDeviceIds())) {
            return new DeviceListRespDTO(); // 或者抛出参数异常
        }
        String queryParams = getReqDTO.getDeviceIds().stream()
                .map(id -> "deviceId=" + id)
                .collect(Collectors.joining("&"));
        String pathWithParams = GET_DEVICES_PATH + "?" + queryParams;
        String responseBody = huaweiSmartHomeAuthClient.get(pathWithParams);
        return JsonUtils.parseObject(responseBody, DeviceListRespDTO.class);
    }

    @Override
    public void controlDevice(DeviceControlReqDTO controlReqDTO) throws IOException {
        String path = String.format(CONTROL_DEVICE_PATH_FORMAT, controlReqDTO.getDeviceId());
        // 请求体只需要 serviceId 和 data，deviceId 在路径中
        // 构造一个只包含 serviceId 和 data 的新对象或Map进行序列化
        // 例如: Map<String, Object> bodyMap = MapUtil.of("serviceId", controlReqDTO.getServiceId(), "data", controlReqDTO.getData());
        // String requestBody = JsonUtils.toJsonString(bodyMap);
        // 或者让DeviceControlReqDTO只包含serviceId和data，deviceId通过方法参数传递到client层
        // 这里为了简单，直接序列化整个DTO，但华为API可能只期望body中有serviceId和data
        // 查阅文档，POST /openapi/asset/v1/device/{deviceId}/command
        // 请求体示例: { "deviceId": "xxx", "serviceId": "acSwitch", "data": { "on": "0" } }
        // 这表明请求体确实需要包含 deviceId, serviceId, data。
        // 但通常RESTful API中，路径参数（如deviceId）不需要在请求体中重复。这可能是华为API的一个特点。
        String requestBody = JsonUtils.toJsonString(controlReqDTO);
        huaweiSmartHomeAuthClient.post(path, requestBody);
        // 控制命令成功通常返回 200 OK，没有特定响应体需要解析
    }
}
