package cn.iocoder.yudao.module.iot.gateway.service.device.remote;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotGatewayServerAliveReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigListReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotSubDeviceRegisterFullReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterRespDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotSubDeviceRegisterRespDTO;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;

import java.util.List;
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

import java.time.Duration;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;

/**
 * Iot 设备信息 Service 实现类：调用远程的 device http 接口，进行设备认证、设备获取等
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class IotDeviceApiImpl implements IotDeviceCommonApi {

    /**
     * 存活上报的连接超时：不复用 RPC 配置的超时，避免一次上报等待过久，耽误后续的续期
     */
    private static final Duration KEEP_ALIVE_CONNECT_TIMEOUT = Duration.ofSeconds(2);
    /**
     * 存活上报的读取超时
     */
    private static final Duration KEEP_ALIVE_READ_TIMEOUT = Duration.ofSeconds(5);

    @Resource
    private IotGatewayProperties gatewayProperties;

    private RestTemplate restTemplate;
    /**
     * 存活上报专用的 RestTemplate：超时更短；超时只是网关不再等待，不会取消 biz 已经接收的请求
     */
    private RestTemplate keepAliveRestTemplate;

    @PostConstruct
    public void init() {
        IotGatewayProperties.RpcProperties rpc = gatewayProperties.getRpc();
        restTemplate = new RestTemplateBuilder()
                .rootUri(rpc.getUrl())
                .readTimeout(rpc.getReadTimeout())
                .connectTimeout(rpc.getConnectTimeout())
                .build();
        keepAliveRestTemplate = new RestTemplateBuilder()
                .rootUri(rpc.getUrl())
                .readTimeout(KEEP_ALIVE_READ_TIMEOUT)
                .connectTimeout(KEEP_ALIVE_CONNECT_TIMEOUT)
                .build();
    }

    @Override
    public CommonResult<Boolean> authDevice(IotDeviceAuthReqDTO authReqDTO) {
        return doPost("/rpc-api/iot/device/auth", authReqDTO, new ParameterizedTypeReference<>() { });
    }

    @Override
    public CommonResult<IotDeviceRespDTO> getDevice(IotDeviceGetReqDTO getReqDTO) {
        return doPost("/rpc-api/iot/device/get", getReqDTO, new ParameterizedTypeReference<>() { });
    }

    @Override
    public CommonResult<List<IotModbusDeviceConfigRespDTO>> getModbusDeviceConfigList(IotModbusDeviceConfigListReqDTO listReqDTO) {
        return doPost("/rpc-api/iot/modbus/config-list", listReqDTO, new ParameterizedTypeReference<>() { });
    }

    @Override
    public CommonResult<IotDeviceRegisterRespDTO> registerDevice(IotDeviceRegisterReqDTO reqDTO) {
        return doPost("/rpc-api/iot/device/register", reqDTO, new ParameterizedTypeReference<>() { });
    }

    @Override
    public CommonResult<List<IotSubDeviceRegisterRespDTO>> registerSubDevices(IotSubDeviceRegisterFullReqDTO reqDTO) {
        return doPost("/rpc-api/iot/device/register-sub", reqDTO, new ParameterizedTypeReference<>() { });
    }

    @Override
    public CommonResult<Boolean> keepAliveGatewayServers(IotGatewayServerAliveReqDTO reqDTO) {
        return doPost(keepAliveRestTemplate, "/rpc-api/iot/gateway-server/keep-alive", reqDTO,
                new ParameterizedTypeReference<>() { });
    }

    private <T, R> CommonResult<R> doPost(String url, T body,
                                          ParameterizedTypeReference<CommonResult<R>> responseType) {
        return doPost(restTemplate, url, body, responseType);
    }

    private <T, R> CommonResult<R> doPost(RestTemplate template, String url, T body,
                                          ParameterizedTypeReference<CommonResult<R>> responseType) {
        try {
            // 请求
            HttpEntity<T> requestEntity = new HttpEntity<>(body);
            ResponseEntity<CommonResult<R>> response = template.exchange(
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
