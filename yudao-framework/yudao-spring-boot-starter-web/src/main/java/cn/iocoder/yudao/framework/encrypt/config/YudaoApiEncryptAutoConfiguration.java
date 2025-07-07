package cn.iocoder.yudao.framework.encrypt.config;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.enums.WebFilterOrderEnum;
import cn.iocoder.yudao.framework.encrypt.core.advice.ApiEncryptResponseBodyAdvice;
import cn.iocoder.yudao.framework.encrypt.core.filter.ApiDecryptRequestBodyFilter;
import cn.iocoder.yudao.framework.web.config.YudaoWebAutoConfiguration;
import cn.iocoder.yudao.framework.web.core.handler.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * @author Zhougang
 */
@AutoConfiguration
@EnableConfigurationProperties(EncryptProperties.class)
@ConditionalOnProperty(prefix = "yudao.encrypt", name = "enable", havingValue = "true")
public class YudaoApiEncryptAutoConfiguration {

    @Bean
    public FilterRegistrationBean<ApiDecryptRequestBodyFilter> apiDecryptRequestBodyFilter(EncryptProperties encryptProperties,
                                                                                        GlobalExceptionHandler globalExceptionHandler) {
        return YudaoWebAutoConfiguration.createFilterBean(new ApiDecryptRequestBodyFilter(encryptProperties, globalExceptionHandler), WebFilterOrderEnum.ENCRYPT_FILTER);
    }

    @Bean
    public ApiEncryptResponseBodyAdvice apiEncryptResponseBodyAdvice(EncryptProperties properties) {
        Assert.notBlank(properties.getPrivateKey(), "请配置 yudao.encrypt.privateKey");
        return new ApiEncryptResponseBodyAdvice(properties);
    }

}
