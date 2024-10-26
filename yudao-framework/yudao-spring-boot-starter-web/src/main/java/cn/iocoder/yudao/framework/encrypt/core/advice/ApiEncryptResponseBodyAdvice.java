package cn.iocoder.yudao.framework.encrypt.core.advice;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.encrypt.config.EncryptProperties;
import cn.iocoder.yudao.framework.encrypt.core.annotation.ApiEncrypt;
import cn.iocoder.yudao.framework.encrypt.core.utils.EncryptUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author Zhougang
 */
@Slf4j
@ControllerAdvice
public class ApiEncryptResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final EncryptProperties encryptProperties;

    public ApiEncryptResponseBodyAdvice(EncryptProperties encryptProperties) {
        this.encryptProperties = encryptProperties;
    }

    @Override
    @SuppressWarnings("NullableProblems") // 避免 IDEA 警告
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (returnType.getMethod() == null) {
            return false;
        }
        return returnType.getMethod().isAnnotationPresent(ApiEncrypt.class) && encryptProperties.isEnable();
    }

    @Override
    @SuppressWarnings("NullableProblems") // 避免 IDEA 警告
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null) {
            return null;
        }
        String content = body instanceof String ? (String) body : JsonUtils.toJsonString(body);
        // 获取客户端 AES 加密密钥
        String aesKey = request.getHeaders().getFirst(encryptProperties.getAesKey());
        Assert.notBlank(aesKey, () ->
                new ServiceException(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), StrUtil.format("请求头 {} 为空！", encryptProperties.getAesKey())));
        try {
            // 通过 RSA 私钥解密 AES 加密密钥
            String decryptAesKey = EncryptUtils.decryptStrByRSA(aesKey, encryptProperties.getPrivateKey());
            // 使用 AES 加密数据
            String result = EncryptUtils.encryptBase64ByAES(content, decryptAesKey);
            if (encryptProperties.isShowLog()) {
                log.info("明文字符串为：{}，加密后：{}", content, result);
            }
            return result;
        } catch (Exception e) {
            log.error("加密异常，body：{}", body, e);
            throw new ServiceException(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(), "加密异常");
        }
    }

}
