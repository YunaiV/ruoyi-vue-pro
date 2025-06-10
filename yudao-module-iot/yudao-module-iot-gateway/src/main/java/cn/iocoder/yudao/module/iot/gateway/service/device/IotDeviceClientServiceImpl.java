package cn.iocoder.yudao.module.iot.gateway.service.device;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceInfoReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceInfoRespDTO;
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

import java.util.LinkedHashMap;

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

    @Override
    public CommonResult<IotDeviceInfoRespDTO> getDeviceInfo(IotDeviceInfoReqDTO infoReqDTO) {
        return doPostForDeviceInfo("/info", infoReqDTO);
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

    @SuppressWarnings("unchecked")
    private <T> CommonResult<IotDeviceInfoRespDTO> doPostForDeviceInfo(String url, T requestBody) {
        try {
            // 使用 ParameterizedTypeReference 来处理泛型类型
            ParameterizedTypeReference<CommonResult<LinkedHashMap<String, Object>>> typeRef = new ParameterizedTypeReference<CommonResult<LinkedHashMap<String, Object>>>() {
            };

            HttpEntity<T> requestEntity = new HttpEntity<>(requestBody);
            ResponseEntity<CommonResult<LinkedHashMap<String, Object>>> response = restTemplate.exchange(url,
                    HttpMethod.POST, requestEntity, typeRef);

            CommonResult<LinkedHashMap<String, Object>> rawResult = response.getBody();
            log.info("[doPostForDeviceInfo][url({}) requestBody({}) rawResult({})]", url, requestBody, rawResult);
            Assert.notNull(rawResult, "请求结果不能为空");

            // 手动转换数据类型
            CommonResult<IotDeviceInfoRespDTO> result = new CommonResult<>();
            result.setCode(rawResult.getCode());
            result.setMsg(rawResult.getMsg());

            if (rawResult.isSuccess() && rawResult.getData() != null) {
                // 将 LinkedHashMap 转换为 IotDeviceInfoRespDTO
                IotDeviceInfoRespDTO deviceInfo = BeanUtil.toBean(rawResult.getData(), IotDeviceInfoRespDTO.class);
                result.setData(deviceInfo);
            }

            return result;
        } catch (Exception e) {
            log.error("[doPostForDeviceInfo][url({}) requestBody({}) 发生异常]", url, requestBody, e);
            return CommonResult.error(INTERNAL_SERVER_ERROR);
        }
    }

}
