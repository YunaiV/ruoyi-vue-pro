package cn.iocoder.dashboard.framework.web.config;

import cn.iocoder.dashboard.util.servlet.ServletUtils;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

/**
 * Web 配置类
 */
@Configuration
@EnableConfigurationProperties(WebProperties.class)
public class WebConfiguration implements WebMvcConfigurer {

    @Resource
    private WebProperties webProperties;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(webProperties.getApiPrefix(), clazz ->
                clazz.isAnnotationPresent(RestController.class)
                && clazz.getPackage().getName().startsWith(webProperties.getControllerPackage()));
    }

    // ========== MessageConverter 相关 ==========

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 创建 FastJsonHttpMessageConverter 对象
        // 重写 canRead 和 canWrite 方法，判断只处理自己写的 API 为前缀的 URL。原因是，FastJSON 和一些三方框架集成存在问题，例如说：
        //      1. 与 Spring Boot Admin 时，由于 Registration 基于 Builder 构造对象，导致它无法反序列化
        //      2. 与 Spring Boot Actuator 时，貌似也存在问题，具体还没去排查。
        // 但是，为什么不替换回 Jackson 呢？
        // 原因是，一些 Number 数值比较小时，反序列化回来是 Integer 类型，实际是 Long 类型。此时，在序列化时，会报 Integer 无法转换成 Long 的异常
        FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter() {

            @Override
            protected boolean canRead(MediaType mediaType) {
                return isApiPrefix() && super.canRead(mediaType);
            }

            @Override
            protected boolean canWrite(MediaType mediaType) {
                return isApiPrefix() && super.canWrite(mediaType);
            }

            private boolean isApiPrefix() {
                HttpServletRequest request = ServletUtils.getRequest();
                return request != null && request.getRequestURI().startsWith(webProperties.getApiPrefix());
            }

        };
        // 自定义 FastJson 配置
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setCharset(Charset.defaultCharset()); // 设置字符集
        fastJsonConfig.setSerializerFeatures(SerializerFeature.DisableCircularReferenceDetect, // 剔除循环引用
                SerializerFeature.WriteNonStringKeyAsString); // 解决 Integer 作为 Key 时，转换为 String 类型，避免浏览器报错
        fastJsonHttpMessageConverter.setFastJsonConfig(fastJsonConfig);
        // 设置支持的 MediaType
        fastJsonHttpMessageConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
        // 添加到 converters 中
        converters.add(0, fastJsonHttpMessageConverter); // 注意，添加到最开头，放在 MappingJackson2XmlHttpMessageConverter 前面
    }

    // ========== Filter 相关 ==========

    /**
     * 创建 CorsFilter Bean，解决跨域问题
     */
    @Bean
    @Order(Integer.MIN_VALUE)
    public CorsFilter corsFilter() {
        // 创建 CorsConfiguration 对象
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*"); // 设置访问源地址
        config.addAllowedHeader("*"); // 设置访问源请求头
        config.addAllowedMethod("*"); // 设置访问源请求方法
        // 创建 UrlBasedCorsConfigurationSource 对象
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 对接口配置跨域设置
        return new CorsFilter(source);
    }

}
