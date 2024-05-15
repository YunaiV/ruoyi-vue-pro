package cn.iocoder.yudao.framework.signature.core.aop;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.signature.core.annotation.Signature;
import cn.iocoder.yudao.framework.signature.core.redis.SignatureRedisDAO;
import cn.iocoder.yudao.framework.signature.core.service.SignatureApi;
import cn.iocoder.yudao.framework.signature.core.util.SignatureUtils;
import cn.iocoder.yudao.framework.web.core.filter.CacheRequestBodyWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * 拦截声明了 {@link Signature} 注解的方法，实现签名
 *
 * @author zhougang
 */
@Aspect
@Slf4j
public class SignatureAspect {
    private final SignatureRedisDAO signatureRedisDAO;

    public SignatureAspect(SignatureRedisDAO signatureRedisDAO) {
        this.signatureRedisDAO = signatureRedisDAO;
    }

    @Before("@annotation(signature)")
    public void beforePointCut(JoinPoint joinPoint, Signature signature) {
        if (!verifySignature(signature, Objects.requireNonNull(ServletUtils.getRequest()))) {
            log.info("[beforePointCut][方法{} 参数({}) 签名失败]", joinPoint.getSignature().toString(),
                    joinPoint.getArgs());
            String message = StrUtil.blankToDefault(signature.message(),
                    GlobalErrorCodeConstants.SIGNATURE_REQUESTS.getMsg());
            throw new ServiceException(GlobalErrorCodeConstants.SIGNATURE_REQUESTS.getCode(), message);
        }
    }

    private boolean verifySignature(Signature signature, HttpServletRequest request) {
        // 根据request 中 header值生成SignatureHeaders实体
        if (!verifyHeaders(signature, request)) {
            return false;
        }
        SignatureApi signatureApi = SpringUtil.getBean(signature.signatureApi());
        Assert.notNull(signatureApi, "找不到对应的 SignatureApi 实现");
        // 校验appId是否能获取到对应的appSecret
        String appId = request.getHeader(signature.appId());
        if (signatureApi.getAppSecret(appId) == null) {
            return false;
        }
        String appSecret = signatureApi.getAppSecret(appId);
        // 获取全部参数(包括URL和Body上的)
        SortedMap<String, String> requestParams = getRequestParams(signature, request);
        // 组装需要加签的文本
        StringBuilder signContent = new StringBuilder();
        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            signContent.append(entry.getKey()).append(entry.getValue());
        }
        // 如：/user/{id}  url带有动态参数的情况
        if (signature.urlEnable()) {
            signContent.append(request.getServletPath());
        }
        // 结尾拼接应用密钥 appSecret
        signContent.append(appSecret);

        // 生成服务端签名
        String serverSign = signatureApi.digestEncoder(signContent.toString());
        // 客户端签名
        String clientSign = request.getHeader(signature.sign());
        if (!StrUtil.equals(clientSign, serverSign)) {
            return false;
        }
        String nonce = requestParams.get(signature.nonce());
        // 将 nonce 记入缓存，防止重复使用（重点二：此处需要将 ttl 设定为允许 timestamp 时间差的值 x 2 ）
        signatureRedisDAO.setNonce(nonce, signature.expireTime(), TimeUnit.MILLISECONDS);
        return true;
    }

    /**
     * 参数校验
     * 1.appId 是否为空
     * 2.timestamp 是否为空，请求是否已经超时，默认10分钟
     * 3.nonce 是否为空，随机数是否10位以上，是否在规定时间内已经访问过了
     * 4.sign是否为空
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
     * 获取请求参数
     *
     * @param request request
     * @return SortedMap
     */
    private SortedMap<String, String> getRequestParams(Signature signature, HttpServletRequest request) {
        SortedMap<String, String> sortedMap = new TreeMap<>();
        sortedMap.put(signature.appId(), request.getHeader(signature.appId()));
        sortedMap.put(signature.timestamp(), request.getHeader(signature.timestamp()));
        sortedMap.put(signature.nonce(), request.getHeader(signature.nonce()));
        // 获取parameters（对应@RequestParam）
        if (!CollectionUtils.isEmpty(request.getParameterMap())) {
            Map<String, String[]> requestParams = request.getParameterMap();
            //获取GET请求参数,以键值对形式保存
            SortedMap<String, String> paramMap = new TreeMap<>();
            for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
                paramMap.put(entry.getKey(), entry.getValue()[0]);
            }
            sortedMap.put("param", JsonUtils.toJsonString(paramMap));
        }

        CacheRequestBodyWrapper requestWrapper = new CacheRequestBodyWrapper(request);
        // 分别获取了request input stream中的body信息、parameter信息
        String body = new String(requestWrapper.getBody(), StandardCharsets.UTF_8);
        if (StrUtil.isNotBlank(body)) {
            // body可能为JSON对象或JSON数组
            // Object parsedObject = JSON.parse(body);
            JsonNode jsonNode = JsonUtils.parseTree(body);
            if (jsonNode.isObject()) {
                // 获取POST请求的JSON参数,以键值对形式保存
                sortedMap.put("body", JsonUtils.toJsonString(SignatureUtils.traverseMap(jsonNode)));
            } else if (jsonNode.isArray()) {
                sortedMap.put("body", JsonUtils.toJsonString(SignatureUtils.traverseList(jsonNode)));
            } else {
                sortedMap.put("body", body);
            }
        }
        return sortedMap;
    }

}

