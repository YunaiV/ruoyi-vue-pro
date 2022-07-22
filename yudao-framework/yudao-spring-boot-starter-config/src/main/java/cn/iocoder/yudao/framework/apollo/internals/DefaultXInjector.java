package cn.iocoder.yudao.framework.apollo.internals;

import cn.iocoder.yudao.framework.apollo.spi.DBConfigFactory;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.internals.*;
import com.ctrip.framework.apollo.spi.*;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.ctrip.framework.apollo.util.factory.DefaultPropertiesFactory;
import com.ctrip.framework.apollo.util.factory.PropertiesFactory;
import com.ctrip.framework.apollo.util.http.DefaultHttpClient;
import com.ctrip.framework.apollo.util.yaml.YamlParser;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Singleton;

/**
 * Guice injector
 *
 * 基于 Guice 注入器实现类
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class DefaultXInjector implements Injector {

    private final com.google.inject.Injector m_injector;

    public DefaultXInjector() {
        try {
            m_injector = Guice.createInjector(new ApolloModule());
        } catch (Throwable ex) {
            ApolloConfigException exception = new ApolloConfigException("Unable to initialize Guice Injector!", ex);
            Tracer.logError(exception);
            throw exception;
        }
    }

    @Override
    public <T> T getInstance(Class<T> clazz) {
        try {
            return m_injector.getInstance(clazz);
        } catch (Throwable ex) {
            Tracer.logError(ex);
            throw new ApolloConfigException(String.format("Unable to load instance for %s!", clazz.getName()), ex);
        }
    }

    @Override
    public <T> T getInstance(Class<T> clazz, String name) {
        // Guice does not support get instance by type and name
        return null;
    }

    private static class ApolloModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(ConfigManager.class).to(DefaultConfigManager.class).in(Singleton.class);
            bind(ConfigFactoryManager.class).to(DefaultConfigFactoryManager.class).in(Singleton.class);
            bind(ConfigRegistry.class).to(DefaultConfigRegistry.class).in(Singleton.class);

            // 自定义 ConfigFactory 实现，使用 DB 作为数据源
            bind(ConfigFactory.class).to(DBConfigFactory.class).in(Singleton.class);

            bind(ConfigUtil.class).in(Singleton.class);
            bind(DefaultHttpClient.class).in(Singleton.class);
            bind(ConfigServiceLocator.class).in(Singleton.class);
            bind(RemoteConfigLongPollService.class).in(Singleton.class);
            bind(YamlParser.class).in(Singleton.class);
            bind(PropertiesFactory.class).to(DefaultPropertiesFactory.class).in(Singleton.class);
        }

    }

}
