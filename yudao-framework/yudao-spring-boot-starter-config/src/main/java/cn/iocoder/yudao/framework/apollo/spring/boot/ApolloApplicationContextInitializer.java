package cn.iocoder.yudao.framework.apollo.spring.boot;

import cn.iocoder.yudao.framework.apollo.core.ConfigConsts;
import com.google.common.base.Strings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 对 {@link com.ctrip.framework.apollo.spring.boot.ApolloApplicationContextInitializer} 的补充，目前的目的有：
 *
 * 1. 将自定义的 apollo.jdbc 设置到 System 变量中
 *
 * @author 芋道源码
 */
public class ApolloApplicationContextInitializer implements EnvironmentPostProcessor, Ordered {

    /**
     * 优先级更高，要早于 Apollo 的 ApolloApplicationContextInitializer 的初始化
     */
    public static final int DEFAULT_ORDER = -1;

    private int order = DEFAULT_ORDER;

    private static final String[] APOLLO_SYSTEM_PROPERTIES = {ConfigConsts.APOLLO_JDBC_DAO,
            ConfigConsts.APOLLO_JDBC_URL, ConfigConsts.APOLLO_JDBC_USERNAME, ConfigConsts.APOLLO_JDBC_PASSWORD};

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        initializeSystemProperty(environment);
    }

    /**
     * To fill system properties from environment config
     */
    void initializeSystemProperty(ConfigurableEnvironment environment) {
        for (String propertyName : APOLLO_SYSTEM_PROPERTIES) {
            fillSystemPropertyFromEnvironment(environment, propertyName);
        }
    }

    private void fillSystemPropertyFromEnvironment(ConfigurableEnvironment environment, String propertyName) {
        if (System.getProperty(propertyName) != null) {
            return;
        }
        String propertyValue = environment.getProperty(propertyName);
        if (Strings.isNullOrEmpty(propertyValue)) {
            return;
        }
        System.setProperty(propertyName, propertyValue);
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

}
