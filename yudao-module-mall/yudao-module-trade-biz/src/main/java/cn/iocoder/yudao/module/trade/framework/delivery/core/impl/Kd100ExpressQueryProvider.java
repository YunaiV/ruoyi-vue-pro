package cn.iocoder.yudao.module.trade.framework.delivery.core.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.trade.framework.delivery.config.TradeExpressQueryProperties;
import cn.iocoder.yudao.module.trade.framework.delivery.core.ExpressQueryProvider;
import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.ExpressQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.ExpressQueryRespDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.provider.kd100.Kd100ExpressQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.provider.kd100.Kd100ExpressQueryRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.EXPRESS_API_QUERY_ERROR;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.EXPRESS_API_QUERY_FAILED;
import static cn.iocoder.yudao.module.trade.framework.delivery.core.convert.ExpressQueryConvert.INSTANCE;

// TODO @jason：可以参考 KdNiaoExpressQueryProvider 建议改改哈
/**
 * 快递 100 服务商
 *
 * @author jason
 */
@Slf4j
public class Kd100ExpressQueryProvider implements ExpressQueryProvider {

    private static final String REAL_TIME_QUERY_URL = "https://poll.kuaidi100.com/poll/query.do";

    private final RestTemplate restTemplate;
    private final TradeExpressQueryProperties.Kd100Config config;

    public Kd100ExpressQueryProvider(RestTemplate restTemplate, TradeExpressQueryProperties.Kd100Config config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    @Override
    public List<ExpressQueryRespDTO> realTimeQueryExpress(ExpressQueryReqDTO reqDTO) {
        // 发起查询
        Kd100ExpressQueryReqDTO kd100ReqParam = INSTANCE.convert2(reqDTO);
        kd100ReqParam.setExpressCompanyCode(kd100ReqParam.getExpressCompanyCode().toLowerCase()); // 快递公司编码需要转成小写
        Kd100ExpressQueryRespDTO respDTO = sendExpressQueryReq(REAL_TIME_QUERY_URL, kd100ReqParam,
                Kd100ExpressQueryRespDTO.class);
        log.debug("[realTimeQueryExpress][快递 100 接口 查询接口返回 {}]", respDTO);
        // 处理结果
        if (Objects.equals("false", respDTO.getResult())) {
            log.error("[realTimeQueryExpress][快递 100 接口 返回失败 {}]", respDTO.getMessage());
            throw exception(EXPRESS_API_QUERY_FAILED, respDTO.getMessage());
        // TODO @json：else 可以不用写哈；
        } else {
            // TODO @jason：convertList2 如果空，应该返回 list 了；
            if (CollUtil.isNotEmpty(respDTO.getTracks())) {
                return INSTANCE.convertList2(respDTO.getTracks());
            } else {
                return Collections.emptyList();
            }
        }
    }

    /**
     * 发送快递 100 实时快递查询请求，可以作为通用快递 100 通用请求接口。 目前没有其它场景需要使用。暂时放这里
     *
     * @param url 请求 url
     * @param req 对应请求的请求参数
     * @param respClass 对应请求的响应 class
     * @param <Req> 每个请求的请求结构 Req DTO
     * @param <Resp> 每个请求的响应结构 Resp DTO
     */
    // TODO @jason：可以改成 request，发起请求哈；
    private <Req, Resp> Resp sendExpressQueryReq(String url, Req req, Class<Resp> respClass) {
        // 请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // 生成签名
        String param = JsonUtils.toJsonString(req);
        String sign = generateReqSign(param, config.getKey(), config.getCustomer());
        // 请求体
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("customer", config.getCustomer());
        requestBody.add("sign", sign);
        requestBody.add("param", param);
        log.debug("[sendExpressQueryReq][快递 100 接口的请求参数: {}]", requestBody);
        // 发送请求
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        // TODO @jason：可以使用 restTemplate 的 post 方法哇？
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        log.debug("[sendExpressQueryReq][快递 100 接口响应结果 {}]", responseEntity);

        // 处理响应
        // TODO @jason：if return 原则；if (!responseEntity.getStatusCode().is2xxSuccessful()) 抛出异常；接着处理成功的
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String response = responseEntity.getBody();
            return JsonUtils.parseObject(response, respClass);
        } else {
            throw exception(EXPRESS_API_QUERY_ERROR);
        }
    }

    private String generateReqSign(String param, String key, String customer) {
        String plainText = String.format("%s%s%s", param, key, customer);
        // TODO @jason：DigestUtil.md5Hex(plainText);
        return HexUtil.encodeHexStr(DigestUtil.md5(plainText), false);
    }

}
