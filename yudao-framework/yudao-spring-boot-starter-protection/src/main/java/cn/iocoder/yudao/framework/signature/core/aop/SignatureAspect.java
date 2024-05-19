package cn.iocoder.yudao.framework.signature.core.aop;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SignUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.signature.core.annotation.Signature;
import cn.iocoder.yudao.framework.signature.core.redis.SignatureRedisDAO;
import cn.iocoder.yudao.framework.web.core.filter.CacheRequestBodyWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * 拦截声明了 {@link Signature} 注解的方法，实现签名
 *
 * @author Zhougang
 */
@Aspect
@Slf4j
@AllArgsConstructor
public class SignatureAspect {

    private final SignatureRedisDAO signatureRedisDAO;

    @Before("@annotation(signature)")
    public void beforePointCut(JoinPoint joinPoint, Signature signature) {
        if (!verifySignature(signature, Objects.requireNonNull(ServletUtils.getRequest()))) {
            log.info("[beforePointCut][方法{} 参数({}) 签名失败]", joinPoint.getSignature().toString(),
                    joinPoint.getArgs());
            String message = StrUtil.blankToDefault(signature.message(),
                    GlobalErrorCodeConstants.BAD_REQUEST.getMsg());
            throw new ServiceException(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), message);
        }
    }

    private boolean verifySignature(Signature signature, HttpServletRequest request) {
        if (!verifyHeaders(signature, request)) {
            return false;
        }
        // 校验 appId 是否能获取到对应的 appSecret
        String appId = request.getHeader(signature.appId());
        String appSecret = signatureRedisDAO.getAppSecret(appId);
        Assert.notNull(appSecret, "找不到对应的 appSecret");
        // 请求头
        SortedMap<String, String> headersMap = getRequestHeaders(signature, request);
        // 如：/user/{id} url 带有动态参数的情况
        String urlParams = signature.urlEnable() ? request.getServletPath() : "";
        // 请求参数
        String requestParams = getRequestParams(request);
        // 请求体
        String requestBody = getRequestBody(request);
        // 生成服务端签名
        String serverSignature = SignUtil.signParamsSha256(headersMap,
                urlParams + requestParams + requestBody + appSecret);
        // 客户端签名
        String clientSignature = request.getHeader(signature.sign());
        if (!StrUtil.equals(clientSignature, serverSignature)) {
            return false;
        }
        String nonce = headersMap.get(signature.nonce());
        // 将 nonce 记入缓存，防止重复使用（重点二：此处需要将 ttl 设定为允许 timestamp 时间差的值 x 2 ）
        signatureRedisDAO.setNonce(nonce, signature.expireTime(), TimeUnit.MILLISECONDS);
        return true;
    }

    /**
     * 校验请求头加签参数
     * 1.appId 是否为空
     * 2.timestamp 是否为空，请求是否已经超时，默认 10 分钟
     * 3.nonce 是否为空，随机数是否 10 位以上，是否在规定时间内已经访问过了
     * 4.sign 是否为空
     *
     * @param signature signature
     * @param request   request
     */
    private boolean verifyHeaders(Signature signature, HttpServletRequest request) {
        String appId = request.getHeader(signature.appId());
        if (StrUtil.isBlank(appId)) {
            return false;
        }
        String timestamp = request.getHeader(signature.timestamp());
        if (StrUtil.isBlank(timestamp)) {
            return false;
        }
        String nonce = request.getHeader(signature.nonce());
        if (StrUtil.isBlank(nonce) || nonce.length() < 10) {
            return false;
        }
        String sign = request.getHeader(signature.sign());
        if (StrUtil.isBlank(sign)) {
            return false;
        }
        // 其他合法性校验
        long expireTime = signature.expireTime();
        long requestTimestamp = Long.parseLong(timestamp);
        // 检查 timestamp 是否超出允许的范围 （重点一：此处需要取绝对值）
        long timestampDisparity = Math.abs(System.currentTimeMillis() - requestTimestamp);
        if (timestampDisparity > expireTime) {
            return false;
        }
        String cacheNonce = signatureRedisDAO.getNonce(nonce);
        return StrUtil.isBlank(cacheNonce);
    }

    /**
     * 获取请求头加签参数
     *
     * @param request request
     * @return signature params
     */
    private SortedMap<String, String> getRequestHeaders(Signature signature, HttpServletRequest request) {
        SortedMap<String, String> sortedMap = new TreeMap<>();
        sortedMap.put(signature.appId(), request.getHeader(signature.appId()));
        sortedMap.put(signature.timestamp(), request.getHeader(signature.timestamp()));
        sortedMap.put(signature.nonce(), request.getHeader(signature.nonce()));
        return sortedMap;
    }

    /**
     * 获取 URL 参数
     *
     * @param request request
     * @return queryParams
     */
    private String getRequestParams(HttpServletRequest request) {
        if (CollUtil.isEmpty(request.getParameterMap())) {
            return "";
        }
        Map<String, String[]> requestParams = request.getParameterMap();
        // 获取 URL 请求参数
        SortedMap<String, String> sortParamsMap = new TreeMap<>();
        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            sortParamsMap.put(entry.getKey(), entry.getValue()[0]);
        }
        // 按 key 排序
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : sortParamsMap.entrySet()) {
            queryString.append("&").append(entry.getKey()).append("=").append(entry.getValue());
        }
        return queryString.substring(1);
    }

    /**
     * 获取请求体参数
     *
     * @param request request
     * @return body
     */
    private String getRequestBody(HttpServletRequest request) {
        CacheRequestBodyWrapper requestWrapper = new CacheRequestBodyWrapper(request);
        // 获取 body
        return new String(requestWrapper.getBody(), StandardCharsets.UTF_8);
    }

}

