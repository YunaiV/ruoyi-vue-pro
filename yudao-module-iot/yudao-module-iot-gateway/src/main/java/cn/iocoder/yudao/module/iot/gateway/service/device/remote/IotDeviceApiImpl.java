package cn.iocoder.yudao.module.iot.gateway.service.device.remote;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;

/**
 * Iot 设备信息 Service 实现类：调用远程的 device http 接口，进行设备认证、设备获取等
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class IotDeviceApiImpl implements IotDeviceCommonApi {

    @Resource
    private IotGatewayProperties gatewayProperties;

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        IotGatewayProperties.RpcProperties rpc = gatewayProperties.getRpc();
        restTemplate = new RestTemplateBuilder()
                .rootUri(rpc.getUrl() + "/rpc-api/iot/device")
                .readTimeout(rpc.getReadTimeout())
                .connectTimeout(rpc.getConnectTimeout())
                .build();
    }

    @Override
    public CommonResult<Boolean> authDevice(IotDeviceAuthReqDTO authReqDTO) {
        return doPost("/auth", authReqDTO, new ParameterizedTypeReference<>() { });
    }

    @Override
    public CommonResult<IotDeviceRespDTO> getDevice(IotDeviceGetReqDTO getReqDTO) {
        return doPost("/get", getReqDTO, new ParameterizedTypeReference<>() { });
    }

    private <T, R> CommonResult<R> doPost(String url, T body,
                                          ParameterizedTypeReference<CommonResult<R>> responseType) {
        try {
            // 请求
            HttpEntity<T> requestEntity = new HttpEntity<>(body);
            ResponseEntity<CommonResult<R>> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, responseType);
            // 响应
            CommonResult<R> result = response.getBody();
            Assert.notNull(result, "请求结果不能为空");
            return result;
        } catch (Exception e) {
            log.error("[doPost][url({}) body({}) 发生异常]", url, body, e);
            return CommonResult.error(INTERNAL_SERVER_ERROR);
        }
    }

}
