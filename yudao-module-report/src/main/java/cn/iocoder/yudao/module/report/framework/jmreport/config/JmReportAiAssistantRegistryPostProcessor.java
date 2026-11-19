package cn.iocoder.yudao.module.report.framework.jmreport.config;

import cn.iocoder.yudao.module.report.framework.jmreport.core.service.JmReportAiAssistantDisabledServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.jmreport.ai.service.IJimuAiAssistantService;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 积木报表内置 AI 助手的兼容处理
 *
 * 积木 boot3 starter 的 JimuAiAutoConfiguration 基于 Spring AI 1.x，条件是 ChatClient 和 OpenAiApi 同时存在。
 * Spring AI 2.x 移除了 OpenAiApi，这个配置被跳过；但积木自身 @ComponentScan 扫描到的 {@link IJimuAiAssistantService} 实现没有条件，
 * 会因为缺少该配置创建的 Bean 而启动失败。
 *
 * 只在上述条件不满足时，把积木的实现替换为 {@link JmReportAiAssistantDisabledServiceImpl}，也就是积木未配置 AI 时的状态。
 * 按 BeanDefinition 的类元数据识别，不加载积木的实现类。条件满足时不做任何处理。
 *
 * @author 芋道源码
 */
@Slf4j
public class JmReportAiAssistantRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    static final String DISABLED_BEAN_NAME = "jmReportAiAssistantDisabledService";

    private static final String JIMU_PACKAGE_PREFIX = "org.jeecg.";

    /**
     * 积木内置 AI 是否可用，即 JimuAiAutoConfiguration 的 @ConditionalOnClass 是否满足
     */
    private final boolean jimuAiAvailable;

    public JmReportAiAssistantRegistryPostProcessor(boolean jimuAiAvailable) {
        this.jimuAiAvailable = jimuAiAvailable;
    }

    public static boolean isJimuAiAvailable(ClassLoader classLoader) {
        return ClassUtils.isPresent("org.springframework.ai.chat.client.ChatClient", classLoader)
                && ClassUtils.isPresent("org.springframework.ai.openai.api.OpenAiApi", classLoader);
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        if (jimuAiAvailable) {
            return;
        }
        List<String> removedBeanNames = new ArrayList<>();
        for (String beanName : registry.getBeanDefinitionNames()) {
            if (isJimuAiAssistantService(registry.getBeanDefinition(beanName))) {
                registry.removeBeanDefinition(beanName);
                removedBeanNames.add(beanName);
            }
        }
        if (removedBeanNames.isEmpty()) {
            return;
        }
        registry.registerBeanDefinition(DISABLED_BEAN_NAME,
                new RootBeanDefinition(JmReportAiAssistantDisabledServiceImpl.class));
        log.warn("[postProcessBeanDefinitionRegistry][积木报表内置 AI 依赖的 Spring AI 1.x 不可用，" +
                "已将 {} 替换为未配置状态，普通报表、大屏不受影响]", removedBeanNames);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    }

    private static boolean isJimuAiAssistantService(BeanDefinition beanDefinition) {
        if (!(beanDefinition instanceof AnnotatedBeanDefinition)) {
            return false;
        }
        AnnotationMetadata metadata = ((AnnotatedBeanDefinition) beanDefinition).getMetadata();
        return metadata.getClassName().startsWith(JIMU_PACKAGE_PREFIX)
                && ObjectUtils.containsElement(metadata.getInterfaceNames(), IJimuAiAssistantService.class.getName());
    }

}
