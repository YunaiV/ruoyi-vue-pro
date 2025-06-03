package cn.iocoder.yudao.module.iot.gateway.service.device;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
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
public class IotDeviceClientServiceImpl implements IotDeviceCommonApi {

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
        return doPost("/auth", authReqDTO);
    }

    @SuppressWarnings("unchecked")
    private <T> CommonResult<Boolean> doPost(String url, T requestBody) {
        try {
            CommonResult<Boolean> result = restTemplate.postForObject(url, requestBody,
                    (Class<CommonResult<Boolean>>) (Class<?>) CommonResult.class);
            log.info("[doPost][url({}) requestBody({}) result({})]", url, requestBody, result);
            Assert.notNull(result, "请求结果不能为空");
            return result;
        } catch (Exception e) {
            log.error("[doPost][url({}) requestBody({}) 发生异常]", url, requestBody, e);
            return CommonResult.error(INTERNAL_SERVER_ERROR);
        }
    }

}
