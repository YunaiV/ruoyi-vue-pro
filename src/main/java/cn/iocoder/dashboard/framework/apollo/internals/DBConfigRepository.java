package cn.iocoder.dashboard.framework.apollo.internals;

import com.ctrip.framework.apollo.Apollo;
import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.internals.AbstractConfigRepository;
import com.ctrip.framework.apollo.internals.ConfigRepository;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.util.ConfigUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class DBConfigRepository extends AbstractConfigRepository {

    private final static ScheduledExecutorService m_executorService;

    static {
        m_executorService = Executors.newScheduledThreadPool(1,
                ApolloThreadFactory.create(DBConfigRepository.class.getSimpleName(), true));
    }

    private final ConfigUtil m_configUtil;

    private final AtomicReference<Properties> m_configCache;
    private final String m_namespace;

    public DBConfigRepository(String namespace) {
        // 初始化变量
        this.m_namespace = namespace;
        m_configCache = new AtomicReference<>();
        m_configUtil = ApolloInjector.getInstance(ConfigUtil.class);

        // 初始化加载
        this.trySync();
        // 初始化定时任务
        this.schedulePeriodicRefresh();
    }

    private AtomicInteger index = new AtomicInteger();

    @Override
    protected void sync() {
        System.out.println("我同步啦");

        index.incrementAndGet();
        Properties properties = new Properties();
        properties.setProperty("demo.test", String.valueOf(index.get()));
        m_configCache.set(properties);
        super.fireRepositoryChange(m_namespace, properties);
    }

    @Override
    public Properties getConfig() {
        // 兜底，避免可能存在配置为 null 的情况
        if (m_configCache.get() == null) {
            this.trySync();
        }
        // 返回配置
        return m_configCache.get();
    }

    @Override
    public void setUpstreamRepository(ConfigRepository upstreamConfigRepository) {
        // 啥事不做
    }

    @Override
    public ConfigSourceType getSourceType() {
        return ConfigSourceType.REMOTE;
    }

    private void schedulePeriodicRefresh() {
        log.debug("Schedule periodic refresh with interval: {} {}",
                m_configUtil.getRefreshInterval(), m_configUtil.getRefreshIntervalTimeUnit());
        m_executorService.scheduleAtFixedRate(() -> {
            Tracer.logEvent("Apollo.ConfigService", String.format("periodicRefresh: %s", m_namespace));
            log.debug("refresh config for namespace: {}", m_namespace);

            // 执行同步
            trySync();

            Tracer.logEvent("Apollo.Client.Version", Apollo.VERSION);
        }, m_configUtil.getRefreshInterval(), m_configUtil.getRefreshInterval(),
                m_configUtil.getRefreshIntervalTimeUnit());
//                TimeUnit.SECONDS);
    }

}
