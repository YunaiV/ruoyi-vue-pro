package cn.iocoder.dashboard.framework.apollox.spring.property;

import cn.iocoder.dashboard.framework.apollox.spring.config.ConfigPropertySource;
import cn.iocoder.dashboard.framework.apollox.spring.config.ConfigPropertySourceFactory;
import cn.iocoder.dashboard.framework.apollox.spring.util.SpringInjector;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Apollo Property Sources processor for Spring Annotation Based Application. <br /> <br />
 * <p>
 * The reason why PropertySourcesProcessor implements {@link BeanFactoryPostProcessor} instead of
 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor} is that lower versions of
 * Spring (e.g. 3.1.1) doesn't support registering BeanDefinitionRegistryPostProcessor in ImportBeanDefinitionRegistrar
 * - {@link com.ctrip.framework.apollo.spring.annotation.ApolloConfigRegistrar}
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class PropertySourcesProcessor implements BeanFactoryPostProcessor, EnvironmentAware, PriorityOrdered {

    /**
     * 是否初始化的标识
     */
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    private final ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector.getInstance(ConfigPropertySourceFactory.class);
    /**
     * Spring ConfigurableEnvironment 对象
     */
    private ConfigurableEnvironment environment;


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (INITIALIZED.compareAndSet(false, true)) {
            // 初始化 AutoUpdateConfigChangeListener 对象，实现属性的自动更新
            initializeAutoUpdatePropertiesFeature(beanFactory);
        }
    }

    private void initializeAutoUpdatePropertiesFeature(ConfigurableListableBeanFactory beanFactory) {
        // 创建 AutoUpdateConfigChangeListener 对象
        AutoUpdateConfigChangeListener autoUpdateConfigChangeListener = new AutoUpdateConfigChangeListener(environment, beanFactory);
        // 循环，向 ConfigPropertySource 注册配置变更器
        List<ConfigPropertySource> configPropertySources = configPropertySourceFactory.getAllConfigPropertySources();
        for (ConfigPropertySource configPropertySource : configPropertySources) {
            configPropertySource.addChangeListener(autoUpdateConfigChangeListener);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        //it is safe enough to cast as all known environment is derived from ConfigurableEnvironment
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public int getOrder() {
        // make it as early as possible
        return Ordered.HIGHEST_PRECEDENCE; // 最高优先级
    }

}
