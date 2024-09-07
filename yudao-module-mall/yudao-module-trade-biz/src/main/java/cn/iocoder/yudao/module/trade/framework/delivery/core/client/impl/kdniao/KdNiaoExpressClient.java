package cn.iocoder.yudao.module.trade.framework.delivery.core.client.impl.kdniao;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.trade.framework.delivery.config.TradeExpressProperties;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.ExpressClient;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackRespDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.kdniao.KdNiaoExpressQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.kdniao.KdNiaoExpressQueryRespDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.EXPRESS_API_QUERY_FAILED;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.EXPRESS_API_QUERY_ERROR;
import static cn.iocoder.yudao.module.trade.framework.delivery.core.client.convert.ExpressQueryConvert.INSTANCE;

/**
 * 快递鸟客户端
 *
 * @author jason
 */
@Slf4j
@AllArgsConstructor
public class KdNiaoExpressClient implements ExpressClient {

    private static final String REAL_TIME_QUERY_URL = "https://api.kdniao.com/Ebusiness/EbusinessOrderHandle.aspx";

    /**
     * 快递鸟即时查询免费版 RequestType
     */
    private static final String REAL_TIME_FREE_REQ_TYPE = "1002";

    private final RestTemplate restTemplate;
    private final TradeExpressProperties.KdNiaoConfig config;

    /**
     * 查询快递轨迹【免费版】
     *
     * 仅支持 3 家：申通快递、圆通速递、百世快递
     *
     * @see <a href="https://www.yuque.com/kdnjishuzhichi/dfcrg1/wugo6k">接口文档</a>
     *
     * @param reqDTO 查询请求参数
     * @return 快递轨迹
     */
    @Override
    public List<ExpressTrackRespDTO> getExpressTrackList(ExpressTrackQueryReqDTO reqDTO) {
        // 发起请求
        KdNiaoExpressQueryReqDTO requestDTO = INSTANCE.convert(reqDTO)
                .setExpressCode(reqDTO.getExpressCode().toUpperCase());
        if (ObjUtil.equal(requestDTO.getExpressCode(), "SF")
                && StrUtil.isBlank(reqDTO.getCustomerName())
                && StrUtil.length(reqDTO.getPhone()) >= 4) {
            requestDTO.setCustomerName(StrUtil.subSufByLength(reqDTO.getPhone(), 4));
        }
        KdNiaoExpressQueryRespDTO respDTO = httpRequest(REAL_TIME_QUERY_URL, REAL_TIME_FREE_REQ_TYPE,
                requestDTO, KdNiaoExpressQueryRespDTO.class);

        // 处理结果
        if (respDTO == null || !respDTO.getSuccess()) {
            throw exception(EXPRESS_API_QUERY_FAILED, respDTO == null ? "" : respDTO.getReason());
        }
        if (CollUtil.isEmpty(respDTO.getTracks())) {
            return Collections.emptyList();
        }
        return INSTANCE.convertList(respDTO.getTracks());
    }

    /**
     * 快递鸟 API 请求
     *
     * @param url 请求 url
     * @param requestType 对应的请求指令 (快递鸟的 RequestType)
     * @param req  对应请求的请求参数
     * @param respClass 对应请求的响应 class
     * @param <Req> 每个请求的请求结构 Req DTO
     * @param <Resp> 每个请求的响应结构 Resp DTO
     */
    private <Req, Resp> Resp httpRequest(String url, String requestType, Req req, Class<Resp> respClass) {
        // 请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // 请求体
        String reqData = JsonUtils.toJsonString(req);
        String dataSign = generateDataSign(reqData, config.getApiKey());
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("RequestData", reqData);
        requestBody.add("DataType", "2");
        requestBody.add("EBusinessID", config.getBusinessId());
        requestBody.add("DataSign", dataSign);
        requestBody.add("RequestType", requestType);
        log.debug("[httpRequest][RequestType({}) 的请求参数({})]", requestType, requestBody);

        // 发送请求
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        log.debug("[httpRequest][RequestType({}) 的响应结果({})", requestType, responseEntity);
        // 处理响应
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw exception(EXPRESS_API_QUERY_ERROR);
        }
        return JsonUtils.parseObject(responseEntity.getBody(), respClass);
    }

    /**
     * 快递鸟生成请求签名
     *
     * 参见 <a href="https://www.yuque.com/kdnjishuzhichi/dfcrg1/zes04h">签名说明</a>
     *
     * @param reqData 请求实体
     * @param apiKey  api Key
     */
    private String generateDataSign(String reqData, String apiKey) {
        String plainText = String.format("%s%s", reqData, apiKey);
        return URLEncodeUtil.encode(Base64.encode(DigestUtil.md5Hex(plainText)));
    }

}
