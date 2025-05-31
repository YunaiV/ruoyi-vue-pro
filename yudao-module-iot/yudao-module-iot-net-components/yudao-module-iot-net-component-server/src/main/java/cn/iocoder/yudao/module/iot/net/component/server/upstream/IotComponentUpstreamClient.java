package cn.iocoder.yudao.module.iot.net.component.server.upstream;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.net.component.server.config.IotNetComponentServerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;

/**
 * 组件上行客户端，用于向主程序上报设备数据
 * <p>
 * 通过 HTTP 调用远程的 IotDeviceUpstreamApi 接口
 *
 * @author haohao
 */
@RequiredArgsConstructor
@Slf4j
public class IotComponentUpstreamClient {

    public static final String URL_PREFIX = "/rpc-api/iot/device/upstream";

    private final IotNetComponentServerProperties properties;

    private final RestTemplate restTemplate;

//    @Override
//    public CommonResult<Boolean> updateDeviceState(IotDeviceStateUpdateReqDTO updateReqDTO) {
//        String url = properties.getUpstreamUrl() + URL_PREFIX + "/update-state";
//        return doPost(url, updateReqDTO);
//    }

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