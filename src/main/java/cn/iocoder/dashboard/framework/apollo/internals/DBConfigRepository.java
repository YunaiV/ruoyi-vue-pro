package cn.iocoder.dashboard.framework.apollo.internals;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.dashboard.framework.apollo.core.ConfigConsts;
import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.dashboard.modules.infra.dal.mysql.dataobject.config.InfConfigDO;
import com.ctrip.framework.apollo.Apollo;
import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.internals.AbstractConfigRepository;
import com.ctrip.framework.apollo.internals.ConfigRepository;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.ctrip.framework.apollo.util.factory.PropertiesFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class DBConfigRepository extends AbstractConfigRepository {

    private final static ScheduledExecutorService m_executorService;

    static {
        m_executorService = Executors.newScheduledThreadPool(1,
                ApolloThreadFactory.create(DBConfigRepository.class.getSimpleName(), true));
    }

    private final ConfigUtil m_configUtil;
    private PropertiesFactory propertiesFactory;
    private final String m_namespace;

    /**
     * 配置缓存，使用 Properties 存储
     */
    private volatile Properties m_configCache;
    /**
     * 缓存配置的最大更新时间，用于后续的增量轮询，判断是否有更新
     */
    private volatile Date maxUpdateTime;

    /**
     * Spring JDBC 操作模板
     */
    private final JdbcTemplate jdbcTemplate;

    public DBConfigRepository(String namespace) {
        // 初始化变量
        this.m_namespace = namespace;
        this.propertiesFactory = ApolloInjector.getInstance(PropertiesFactory.class);
        this.m_configUtil = ApolloInjector.getInstance(ConfigUtil.class);
        // 初始化 DB
        DataSource dataSource = new DriverManagerDataSource(System.getProperty(ConfigConsts.APOLLO_JDBC_URL),
                System.getProperty(ConfigConsts.APOLLO_JDBC_USERNAME), System.getProperty(ConfigConsts.APOLLO_JDBC_PASSWORD));
        this.jdbcTemplate = new JdbcTemplate(dataSource);

        // 初始化加载
        this.trySync();
        // 初始化定时任务
        this.schedulePeriodicRefresh();
    }

    @Override
    protected void sync() {
        // 第一步，尝试获取配置
        List<InfConfigDO> configs = this.loadConfigIfUpdate(this.maxUpdateTime);
        if (CollUtil.isEmpty(configs)) { // 如果没有更新，则返回
            return;
        }
        log.info("[sync][同步到新配置，配置数量为:{}]", configs.size());

        // 第二步，构建新的 Properties
        Properties newProperties = this.buildProperties(configs);
        this.m_configCache = newProperties;
        // 第三步，获取最大的配置时间
        this.maxUpdateTime = configs.stream().max(Comparator.comparing(BaseDO::getUpdateTime)).get().getUpdateTime();
        // 第四部，触发配置刷新！重要！！！！
        super.fireRepositoryChange(m_namespace, newProperties);
    }

    @Override
    public Properties getConfig() {
        // 兜底，避免可能存在配置为 null 的情况
        if (m_configCache == null) {
            this.trySync();
        }
        // 返回配置
        return m_configCache;
    }

    @Override
    public void setUpstreamRepository(ConfigRepository upstreamConfigRepository) {
        // 啥事不做
    }

    @Override
    public ConfigSourceType getSourceType() {
        return ConfigSourceType.REMOTE;
    }

    private Properties buildProperties(List<InfConfigDO> configs) {
        Properties properties = propertiesFactory.getPropertiesInstance();
        configs.stream().filter(config -> 0 == config.getDeleted()) // 过滤掉被删除的配置
                .forEach(config -> properties.put(config.getKey(), config.getValue()));
        return properties;
    }

    // ========== 定时器相关操作 ==========

    private void schedulePeriodicRefresh() {
        log.debug("Schedule periodic refresh with interval: {} {}",
                m_configUtil.getRefreshInterval(), m_configUtil.getRefreshIntervalTimeUnit());
        m_executorService.scheduleAtFixedRate(() -> {
            Tracer.logEvent("Apollo.ConfigService", String.format("periodicRefresh: %s", m_namespace));
            log.debug("refresh config for namespace: {}", m_namespace);

            // 执行同步. 内部已经 try catch 掉异常，无需在处理
            trySync();

            Tracer.logEvent("Apollo.Client.Version", Apollo.VERSION);
        }, m_configUtil.getRefreshInterval(), m_configUtil.getRefreshInterval(),
                m_configUtil.getRefreshIntervalTimeUnit());
//                TimeUnit.SECONDS);
    }

    // ========== 数据库相关操作 ==========

    /**
     * 如果配置发生变化，从数据库中获取最新的全量配置。
     * 如果未发生变化，则返回空
     *
     * @param maxUpdateTime 当前配置的最大更新时间
     * @return 配置列表
     */
    private List<InfConfigDO> loadConfigIfUpdate(Date maxUpdateTime) {
        // 第一步，判断是否要更新。
        boolean isUpdate = maxUpdateTime == null; // 如果更新时间为空，说明 DB 一定有新数据
        if (!isUpdate) {
            isUpdate = this.existsNewConfig(maxUpdateTime); // 判断数据库中是否有更新的配置
        }
        if (!isUpdate) {
            return null;
        }
        // 第二步，如果有更新，则从数据库加载所有配置
        return this.getSysConfigList();
    }

    private boolean existsNewConfig(Date maxUpdateTime) {
         return jdbcTemplate.query("SELECT id FROM inf_config WHERE update_time > ? LIMIT 1",
                 ResultSet::next, maxUpdateTime);
    }

    private List<InfConfigDO> getSysConfigList() {
        return jdbcTemplate.query("SELECT `key`, `value`, update_time, deleted FROM inf_config", new BeanPropertyRowMapper<>(InfConfigDO.class));
    }

}
