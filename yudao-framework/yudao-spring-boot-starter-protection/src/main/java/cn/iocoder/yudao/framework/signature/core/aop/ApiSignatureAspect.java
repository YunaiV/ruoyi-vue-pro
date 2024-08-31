package cn.iocoder.yudao.framework.signature.core.aop;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.signature.core.annotation.ApiSignature;
import cn.iocoder.yudao.framework.signature.core.redis.ApiSignatureRedisDAO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;

/**
 * 拦截声明了 {@link ApiSignature} 注解的方法，实现签名
 *
 * @author Zhougang
 */
@Aspect
@Slf4j
@AllArgsConstructor
public class ApiSignatureAspect {

    private final ApiSignatureRedisDAO signatureRedisDAO;

    @Before("@annotation(signature)")
    public void beforePointCut(JoinPoint joinPoint, ApiSignature signature) {
        // 1. 验证通过，直接结束
        if (verifySignature(signature, Objects.requireNonNull(ServletUtils.getRequest()))) {
            return;
        }

        // 2. 验证不通过，抛出异常
        log.error("[beforePointCut][方法{} 参数({}) 签名失败]", joinPoint.getSignature().toString(),
                joinPoint.getArgs());
        throw new ServiceException(BAD_REQUEST.getCode(),
                StrUtil.blankToDefault(signature.message(), BAD_REQUEST.getMsg()));
    }

    public boolean verifySignature(ApiSignature signature, HttpServletRequest request) {
        // 1.1 校验 Header
        if (!verifyHeaders(signature, request)) {
            return false;
        }
        // 1.2 校验 appId 是否能获取到对应的 appSecret
        String appId = request.getHeader(signature.appId());
        String appSecret = signatureRedisDAO.getAppSecret(appId);
        Assert.notNull(appSecret, "[appId({})] 找不到对应的 appSecret", appId);

        // 2. 校验签名【重要！】
        String clientSignature = request.getHeader(signature.sign()); // 客户端签名
        String serverSignatureString = buildSignatureString(signature, request, appSecret); // 服务端签名字符串
        String serverSignature = DigestUtil.sha256Hex(serverSignatureString); // 服务端签名
        if (ObjUtil.notEqual(clientSignature, serverSignature)) {
            return false;
        }

        // 3. 将 nonce 记入缓存，防止重复使用（重点二：此处需要将 ttl 设定为允许 timestamp 时间差的值 x 2 ）
        String nonce = request.getHeader(signature.nonce());
        signatureRedisDAO.setNonce(appId, nonce, signature.timeout() * 2, signature.timeUnit());
        return true;
    }

    /**
     * 校验请求头加签参数
     *
     * 1. appId 是否为空
     * 2. timestamp 是否为空，请求是否已经超时，默认 10 分钟
     * 3. nonce 是否为空，随机数是否 10 位以上，是否在规定时间内已经访问过了
     * 4. sign 是否为空
     *
     * @param signature signature
     * @param request   request
     * @return 是否校验 Header 通过
     */
    private boolean verifyHeaders(ApiSignature signature, HttpServletRequest request) {
        // 1. 非空校验
        String appId = request.getHeader(signature.appId());
        if (StrUtil.isBlank(appId)) {
            return false;
        }
        String timestamp = request.getHeader(signature.timestamp());
        if (StrUtil.isBlank(timestamp)) {
            return false;
        }
        String nonce = request.getHeader(signature.nonce());
        if (StrUtil.length(nonce) < 10) {
            return false;
        }
        String sign = request.getHeader(signature.sign());
        if (StrUtil.isBlank(sign)) {
            return false;
        }

        // 2. 检查 timestamp 是否超出允许的范围 （重点一：此处需要取绝对值）
        long expireTime = signature.timeUnit().toMillis(signature.timeout());
        long requestTimestamp = Long.parseLong(timestamp);
        long timestampDisparity = Math.abs(System.currentTimeMillis() - requestTimestamp);
        if (timestampDisparity > expireTime) {
            return false;
        }

        // 3. 检查 nonce 是否存在，有且仅能使用一次
        return signatureRedisDAO.getNonce(appId, nonce) == null;
    }

    /**
     * 构建签名字符串
     *
     * 格式为 = 请求参数 + 请求体 + 请求头 + 密钥
     *
     * @param signature signature
     * @param request   request
     * @param appSecret appSecret
     * @return 签名字符串
     */
    private String buildSignatureString(ApiSignature signature, HttpServletRequest request, String appSecret) {
        SortedMap<String, String> parameterMap = getRequestParameterMap(request); // 请求头
        SortedMap<String, String> headerMap = getRequestHeaderMap(signature, request); // 请求参数
        String requestBody = StrUtil.nullToDefault(ServletUtils.getBody(request), ""); // 请求体
        return MapUtil.join(parameterMap, "&", "=")
                + requestBody
                + MapUtil.join(headerMap, "&", "=")
                + appSecret;
    }

    /**
     * 获取请求头加签参数 Map
     *
     * @param request 请求
     * @param signature 签名注解
     * @return signature params
     */
    private static SortedMap<String, String> getRequestHeaderMap(ApiSignature signature, HttpServletRequest request) {
        SortedMap<String, String> sortedMap = new TreeMap<>();
        sortedMap.put(signature.appId(), request.getHeader(signature.appId()));
        sortedMap.put(signature.timestamp(), request.getHeader(signature.timestamp()));
        sortedMap.put(signature.nonce(), request.getHeader(signature.nonce()));
        return sortedMap;
    }

    /**
     * 获取请求参数 Map
     *
     * @param request 请求
     * @return queryParams
     */
    private static SortedMap<String, String> getRequestParameterMap(HttpServletRequest request) {
        SortedMap<String, String> sortedMap = new TreeMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            sortedMap.put(entry.getKey(), entry.getValue()[0]);
        }
        return sortedMap;
    }

}