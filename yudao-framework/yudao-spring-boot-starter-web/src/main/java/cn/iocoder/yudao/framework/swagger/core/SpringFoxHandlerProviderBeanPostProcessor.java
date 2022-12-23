package cn.iocoder.yudao.framework.swagger.core;

import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.spring.web.plugins.WebFluxRequestHandlerProvider;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

import java.util.List;

/**
 * 解决 SpringFox 与 SpringBoot 2.6.x 不兼容的问题
 * 该问题对应的 issue 为 https://github.com/springfox/springfox/issues/3462
 *
 * @author 芋道源码
 */
public class SpringFoxHandlerProviderBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof WebMvcRequestHandlerProvider || bean instanceof WebFluxRequestHandlerProvider) {
            customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
        }
        return bean;
    }

    private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
        // 移除，只保留 patternParser
        List<T> copy = CollectionUtils.filterList(mappings, mapping -> mapping.getPatternParser() == null);
        // 添加到 mappings 中
        mappings.clear();
        mappings.addAll(copy);
    }

    @SuppressWarnings("unchecked")
    private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
        return (List<RequestMappingInfoHandlerMapping>)
                ReflectUtil.getFieldValue(bean, "handlerMappings");
    }

}
