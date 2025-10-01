package cn.iocoder.yudao.framework.encrypt.core.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.AsymmetricDecryptor;
import cn.hutool.crypto.asymmetric.AsymmetricEncryptor;
import cn.hutool.crypto.symmetric.SymmetricDecryptor;
import cn.hutool.crypto.symmetric.SymmetricEncryptor;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.encrypt.config.ApiEncryptProperties;
import cn.iocoder.yudao.framework.encrypt.core.annotation.ApiEncrypt;
import cn.iocoder.yudao.framework.web.config.WebProperties;
import cn.iocoder.yudao.framework.web.core.filter.ApiRequestFilter;
import cn.iocoder.yudao.framework.web.core.handler.GlobalExceptionHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ServletRequestPathUtils;

import java.io.IOException;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;

/**
 * API 加密过滤器，处理 {@link ApiEncrypt} 注解。
 *
 * 1. 解密请求参数
 * 2. 加密响应结果
 *
 * 疑问：为什么不使用 SpringMVC 的 RequestBodyAdvice 或 ResponseBodyAdvice 机制呢？
 * 回答：考虑到项目中会记录访问日志、异常日志，以及 HTTP API 签名等场景，最好是全局级、且提前做解析！！！
 *
 * @author 芋道源码
 */
@Slf4j
public class ApiEncryptFilter extends ApiRequestFilter {

    private final ApiEncryptProperties apiEncryptProperties;

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    private final GlobalExceptionHandler globalExceptionHandler;

    private final SymmetricDecryptor requestSymmetricDecryptor;
    private final AsymmetricDecryptor requestAsymmetricDecryptor;

    private final SymmetricEncryptor responseSymmetricEncryptor;
    private final AsymmetricEncryptor responseAsymmetricEncryptor;

    public ApiEncryptFilter(WebProperties webProperties,
                            ApiEncryptProperties apiEncryptProperties,
                            RequestMappingHandlerMapping requestMappingHandlerMapping,
                            GlobalExceptionHandler globalExceptionHandler) {
        super(webProperties);
        this.apiEncryptProperties = apiEncryptProperties;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.globalExceptionHandler = globalExceptionHandler;
        if (StrUtil.equalsIgnoreCase(apiEncryptProperties.getAlgorithm(), "AES")) {
            this.requestSymmetricDecryptor = SecureUtil.aes(StrUtil.utf8Bytes(apiEncryptProperties.getRequestKey()));
            this.requestAsymmetricDecryptor = null;
            this.responseSymmetricEncryptor = SecureUtil.aes(StrUtil.utf8Bytes(apiEncryptProperties.getResponseKey()));
            this.responseAsymmetricEncryptor = null;
        } else if (StrUtil.equalsIgnoreCase(apiEncryptProperties.getAlgorithm(), "RSA")) {
            this.requestSymmetricDecryptor = null;
            this.requestAsymmetricDecryptor = SecureUtil.rsa(apiEncryptProperties.getRequestKey(), null);
            this.responseSymmetricEncryptor = null;
            this.responseAsymmetricEncryptor = SecureUtil.rsa(null, apiEncryptProperties.getResponseKey());
        } else {
            // 补充说明：如果要支持 SM2、SM4 等算法，可在此处增加对应实例的创建，并添加相应的 Maven 依赖即可。
            throw new IllegalArgumentException("不支持的加密算法：" + apiEncryptProperties.getAlgorithm());
        }
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 获取 @ApiEncrypt 注解
        ApiEncrypt apiEncrypt = getApiEncrypt(request);
        boolean requestEnable = apiEncrypt != null && apiEncrypt.request();
        boolean responseEnable = apiEncrypt != null && apiEncrypt.response();
        String encryptHeader = request.getHeader(apiEncryptProperties.getHeader());
        if (!requestEnable && !responseEnable && StrUtil.isBlank(encryptHeader))  {
            chain.doFilter(request, response);
            return;
        }

        // 1. 解密请求
        if (ObjectUtils.equalsAny(HttpMethod.valueOf(request.getMethod()),
                HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)) {
            try {
                if (StrUtil.isNotBlank(encryptHeader)) {
                    request = new ApiDecryptRequestWrapper(request,
                            requestSymmetricDecryptor, requestAsymmetricDecryptor);
                } else if (requestEnable) {
                    throw invalidParamException("请求未包含加密标头，请检查是否正确配置了加密标头");
                }
            } catch (Exception ex) {
                CommonResult<?> result = globalExceptionHandler.allExceptionHandler(request, ex);
                ServletUtils.writeJSON(response, result);
                return;
            }
        }

        // 2. 执行过滤器链
        if (responseEnable) {
            // 特殊：仅包装，最后执行。目的：Response 内容可以被重复读取！！！
            response = new ApiEncryptResponseWrapper(response);
        }
        chain.doFilter(request, response);

        // 3. 加密响应（真正执行）
        if (responseEnable) {
            ((ApiEncryptResponseWrapper) response).encrypt(apiEncryptProperties,
                    responseSymmetricEncryptor, responseAsymmetricEncryptor);
        }
    }

    /**
     * 获取 @ApiEncrypt 注解
     *
     * @param request 请求
     */
    @SuppressWarnings("PatternVariableCanBeUsed")
    private ApiEncrypt getApiEncrypt(HttpServletRequest request) {
        try {
            // 特殊：兼容 SpringBoot 2.X 版本会报错的问题 https://t.zsxq.com/kqyiB
            if (!ServletRequestPathUtils.hasParsedRequestPath(request)) {
                ServletRequestPathUtils.parseAndCache(request);
            }

            // 解析 @ApiEncrypt 注解
            HandlerExecutionChain mappingHandler = requestMappingHandlerMapping.getHandler(request);
            if (mappingHandler == null) {
                return null;
            }
            Object handler = mappingHandler.getHandler();
            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                ApiEncrypt annotation = handlerMethod.getMethodAnnotation(ApiEncrypt.class);
                if (annotation == null) {
                    annotation = handlerMethod.getBeanType().getAnnotation(ApiEncrypt.class);
                }
                return annotation;
            }
        } catch (Exception e) {
            log.error("[getApiEncrypt][url({}/{}) 获取 @ApiEncrypt 注解失败]",
                    request.getRequestURI(), request.getMethod(), e);
        }
        return null;
    }

}
