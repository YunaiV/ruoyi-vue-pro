package cn.iocoder.yudao.module.report.framework.jmreport.config;

import cn.iocoder.yudao.module.report.framework.jmreport.core.service.JmReportAiAssistantDisabledServiceImpl;
import org.jeecg.modules.jmreport.ai.service.IJimuAiAssistantService;
import org.jeecg.modules.jmreport.common.expetion.JimuReportException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link JmReportAiAssistantRegistryPostProcessor} 的单元测试
 *
 * @author 芋道源码
 */
class JmReportAiAssistantRegistryPostProcessorTest {

    /**
     * 积木自带 AI 助手实现的 Bean 名，见其 @Service 注解
     */
    private static final String JIMU_BEAN_NAME = "jimuSpringAiAssistantServiceImpl";

    @Test
    void testPostProcessBeanDefinitionRegistry_jimuAiUnavailable() {
        DefaultListableBeanFactory registry = scanJimuAiAssistantService();

        new JmReportAiAssistantRegistryPostProcessor(false).postProcessBeanDefinitionRegistry(registry);

        assertFalse(registry.containsBeanDefinition(JIMU_BEAN_NAME));
        assertTrue(registry.containsBeanDefinition(JmReportAiAssistantRegistryPostProcessor.DISABLED_BEAN_NAME));
        IJimuAiAssistantService service = registry.getBean(IJimuAiAssistantService.class);
        assertInstanceOf(JmReportAiAssistantDisabledServiceImpl.class, service);
        assertFalse(service.isConfigured());
        assertThrows(JimuReportException.class, () -> service.callAi("system", "user"));
        assertThrows(JimuReportException.class, () -> service.callAiService("type", "content", "ddl", "extra"));
        assertThrows(JimuReportException.class, () -> service.routeStream("type", Collections.emptyMap()));
    }

    @Test
    void testPostProcessBeanDefinitionRegistry_jimuAiAvailable() {
        DefaultListableBeanFactory registry = scanJimuAiAssistantService();

        new JmReportAiAssistantRegistryPostProcessor(true).postProcessBeanDefinitionRegistry(registry);

        assertTrue(registry.containsBeanDefinition(JIMU_BEAN_NAME));
        assertFalse(registry.containsBeanDefinition(JmReportAiAssistantRegistryPostProcessor.DISABLED_BEAN_NAME));
    }

    @Test
    void testIsJimuAiAvailable() {
        // 测试 classpath 不含 Spring AI（积木依赖已排除 org.springframework.ai）
        assertFalse(JmReportAiAssistantRegistryPostProcessor.isJimuAiAvailable(getClass().getClassLoader()));
    }

    /**
     * 与积木自身的 @ComponentScan 一样，按类元数据扫描出它的 AI 助手实现，不加载实现类
     */
    private static DefaultListableBeanFactory scanJimuAiAssistantService() {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
        scanner.setIncludeAnnotationConfig(false);
        scanner.scan("org.jeecg.modules.jmreport.ai.service");
        assertTrue(registry.containsBeanDefinition(JIMU_BEAN_NAME));
        return registry;
    }

}
