package cn.iocoder.dashboard.framework.apollox.internals;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RemoteConfig Repository
 * <p>
 * 远程配置 Repository ，实现从 Config Service 拉取配置，并缓存在内存中。并且，定时 + 实时刷新缓存。
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class RemoteConfigRepository extends AbstractConfigRepository {

    private static final Logger logger = LoggerFactory.getLogger(RemoteConfigRepository.class);
    private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);
    private static final Joiner.MapJoiner MAP_JOINER = Joiner.on("&").withKeyValueSeparator("=");

    private static final Escaper pathEscaper = UrlEscapers.urlPathSegmentEscaper();
    private static final Escaper queryParamEscaper = UrlEscapers.urlFormParameterEscaper();

    /**
     * 远程配置长轮询服务
     */
    private RemoteConfigLongPollService remoteConfigLongPollService;
    /**
     * 指向 ApolloConfig 的 AtomicReference ，缓存配置
     */
    private volatile AtomicReference<ApolloConfig> m_configCache;
    /**
     * Namespace 名字
     */
    private final String m_namespace;
    /**
     * ScheduledExecutorService 对象
     */
    private final static ScheduledExecutorService m_executorService;
    /**
     * 指向 ServiceDTO( Config Service 信息) 的 AtomicReference
     */
    private AtomicReference<ServiceDTO> m_longPollServiceDto;
    /**
     * 指向 ApolloNotificationMessages 的 AtomicReference
     */
    private AtomicReference<ApolloNotificationMessages> m_remoteMessages;
    /**
     * 加载配置的 RateLimiter
     */
    private RateLimiter m_loadConfigRateLimiter;
    /**
     * 是否强制拉取缓存的标记
     * <p>
     * 若为 true ，则多一轮从 Config Service 拉取配置
     * 为 true 的原因，RemoteConfigRepository 知道 Config Service 有配置刷新
     */
    private AtomicBoolean m_configNeedForceRefresh;
    /**
     * 失败定时重试策略，使用 {@link ExponentialSchedulePolicy}
     */
    private SchedulePolicy m_loadConfigFailSchedulePolicy;
    private Gson gson;
    private ConfigUtil m_configUtil;
    private HttpUtil m_httpUtil;
    private ConfigServiceLocator m_serviceLocator;

    static {
        // 单线程池
        m_executorService = Executors.newScheduledThreadPool(1, ApolloThreadFactory.create("RemoteConfigRepository", true));
    }

    /**
     * Constructor.
     *
     * @param namespace the namespace
     */
    public RemoteConfigRepository(String namespace) {
        m_namespace = namespace;
        m_configCache = new AtomicReference<>();
        m_configUtil = ApolloInjector.getInstance(ConfigUtil.class);
        m_httpUtil = ApolloInjector.getInstance(HttpUtil.class);
        m_serviceLocator = ApolloInjector.getInstance(ConfigServiceLocator.class);
        remoteConfigLongPollService = ApolloInjector.getInstance(RemoteConfigLongPollService.class);
        m_longPollServiceDto = new AtomicReference<>();
        m_remoteMessages = new AtomicReference<>();
        m_loadConfigRateLimiter = RateLimiter.create(m_configUtil.getLoadConfigQPS());
        m_configNeedForceRefresh = new AtomicBoolean(true);
        m_loadConfigFailSchedulePolicy = new ExponentialSchedulePolicy(m_configUtil.getOnErrorRetryInterval(), m_configUtil.getOnErrorRetryInterval() * 8);
        gson = new Gson();
        // 尝试同步配置
        super.trySync();
        // 初始化定时刷新配置的任务
        this.schedulePeriodicRefresh();
        // 注册自己到 RemoteConfigLongPollService 中，实现配置更新的实时通知
        this.scheduleLongPollingRefresh();
    }

    @Override
    public Properties getConfig() {
        // 如果缓存为空，强制从 Config Service 拉取配置
        if (m_configCache.get() == null) {
            this.sync();
        }
        // 转换成 Properties 对象，并返回
        return transformApolloConfigToProperties(m_configCache.get());
    }

    @Override
    public void setUpstreamRepository(ConfigRepository upstreamConfigRepository) {
        // remote config doesn't need upstream
    }

    private void schedulePeriodicRefresh() {
        logger.debug("Schedule periodic refresh with interval: {} {}", m_configUtil.getRefreshInterval(), m_configUtil.getRefreshIntervalTimeUnit());
        // 创建定时任务，定时刷新配置
        m_executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // 【TODO 6001】Tracer 日志
                Tracer.logEvent("Apollo.ConfigService", String.format("periodicRefresh: %s", m_namespace));
                logger.debug("refresh config for namespace: {}", m_namespace);
                // 尝试同步配置
                trySync();
                // 【TODO 6001】Tracer 日志
                Tracer.logEvent("Apollo.Client.Version", Apollo.VERSION);
            }
        }, m_configUtil.getRefreshInterval(), m_configUtil.getRefreshInterval(), m_configUtil.getRefreshIntervalTimeUnit());
    }

    @Override
    protected synchronized void sync() {
        // 【TODO 6001】Tracer 日志
        Transaction transaction = Tracer.newTransaction("Apollo.ConfigService", "syncRemoteConfig");
        try {
            // 获得缓存的 ApolloConfig 对象
            ApolloConfig previous = m_configCache.get();
            // 从 Config Service 加载 ApolloConfig 对象
            ApolloConfig current = loadApolloConfig();

            // reference equals means HTTP 304
            // 若不相等，说明更新了，设置到缓存中
            if (previous != current) {
                logger.debug("Remote Config refreshed!");
                // 设置到缓存
                m_configCache.set(current);
                // 发布 Repository 的配置发生变化，触发对应的监听器们
                super.fireRepositoryChange(m_namespace, this.getConfig());
            }
            // 【TODO 6001】Tracer 日志
            if (current != null) {
                Tracer.logEvent(String.format("Apollo.Client.Configs.%s", current.getNamespaceName()), current.getReleaseKey());
            }
            // 【TODO 6001】Tracer 日志
            transaction.setStatus(Transaction.SUCCESS);
        } catch (Throwable ex) {
            // 【TODO 6001】Tracer 日志
            transaction.setStatus(ex);
            throw ex;
        } finally {
            // 【TODO 6001】Tracer 日志
            transaction.complete();
        }
    }

    private Properties transformApolloConfigToProperties(ApolloConfig apolloConfig) {
        Properties result = new Properties();
        result.putAll(apolloConfig.getConfigurations());
        return result;
    }

    private ApolloConfig loadApolloConfig() {
        // 限流
        if (!m_loadConfigRateLimiter.tryAcquire(5, TimeUnit.SECONDS)) {
            // wait at most 5 seconds
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
            }
        }
        // 获得 appId cluster dataCenter 配置信息
        String appId = m_configUtil.getAppId();
        String cluster = m_configUtil.getCluster();
        String dataCenter = m_configUtil.getDataCenter();
        Tracer.logEvent("Apollo.Client.ConfigMeta", STRING_JOINER.join(appId, cluster, m_namespace));
        // 计算重试次数
        int maxRetries = m_configNeedForceRefresh.get() ? 2 : 1;
        long onErrorSleepTime = 0; // 0 means no sleep
        Throwable exception = null;
        // 获得所有的 Config Service 的地址
        List<ServiceDTO> configServices = getConfigServices();
        String url = null;
        // 循环读取配置重试次数直到成功。每一次，都会循环所有的 ServiceDTO 数组。
        for (int i = 0; i < maxRetries; i++) {
            // 随机所有的 Config Service 的地址
            List<ServiceDTO> randomConfigServices = Lists.newLinkedList(configServices);
            Collections.shuffle(randomConfigServices);
            // 优先访问通知配置变更的 Config Service 的地址。并且，获取到时，需要置空，避免重复优先访问。
            // Access the server which notifies the client first
            if (m_longPollServiceDto.get() != null) {
                randomConfigServices.add(0, m_longPollServiceDto.getAndSet(null));
            }
            // 循环所有的 Config Service 的地址
            for (ServiceDTO configService : randomConfigServices) {
                // sleep 等待，下次从 Config Service 拉取配置
                if (onErrorSleepTime > 0) {
                    logger.warn("Load config failed, will retry in {} {}. appId: {}, cluster: {}, namespaces: {}", onErrorSleepTime, m_configUtil.getOnErrorRetryIntervalTimeUnit(), appId, cluster, m_namespace);
                    try {
                        m_configUtil.getOnErrorRetryIntervalTimeUnit().sleep(onErrorSleepTime);
                    } catch (InterruptedException e) {
                        //ignore
                    }
                }
                // 组装查询配置的地址
                url = assembleQueryConfigUrl(configService.getHomepageUrl(), appId, cluster, m_namespace, dataCenter, m_remoteMessages.get(), m_configCache.get());

                logger.debug("Loading config from {}", url);
                // 创建 HttpRequest 对象
                HttpRequest request = new HttpRequest(url);

                // 【TODO 6001】Tracer 日志
                Transaction transaction = Tracer.newTransaction("Apollo.ConfigService", "queryConfig");
                transaction.addData("Url", url);
                try {
                    // 发起请求，返回 HttpResponse 对象
                    HttpResponse<ApolloConfig> response = m_httpUtil.doGet(request, ApolloConfig.class);
                    // 设置 m_configNeedForceRefresh = false
                    m_configNeedForceRefresh.set(false);
                    // 标记成功
                    m_loadConfigFailSchedulePolicy.success();

                    // 【TODO 6001】Tracer 日志
                    transaction.addData("StatusCode", response.getStatusCode());
                    transaction.setStatus(Transaction.SUCCESS);

                    // 无新的配置，直接返回缓存的 ApolloConfig 对象
                    if (response.getStatusCode() == 304) {
                        logger.debug("Config server responds with 304 HTTP status code.");
                        return m_configCache.get();
                    }

                    // 有新的配置，进行返回新的 ApolloConfig 对象
                    ApolloConfig result = response.getBody();
                    logger.debug("Loaded config for {}: {}", m_namespace, result);
                    return result;
                } catch (ApolloConfigStatusCodeException ex) {
                    ApolloConfigStatusCodeException statusCodeException = ex;
                    // 若返回的状态码是 404 ，说明查询配置的 Config Service 不存在该 Namespace 。
                    // config not found
                    if (ex.getStatusCode() == 404) {
                        String message = String.format("Could not find config for namespace - appId: %s, cluster: %s, namespace: %s, " +
                                "please check whether the configs are released in Apollo!", appId, cluster, m_namespace);
                        statusCodeException = new ApolloConfigStatusCodeException(ex.getStatusCode(), message);
                    }
                    // 【TODO 6001】Tracer 日志
                    Tracer.logEvent("ApolloConfigException", ExceptionUtil.getDetailMessage(statusCodeException));
                    transaction.setStatus(statusCodeException);
                    // 设置最终的异常
                    exception = statusCodeException;
                } catch (Throwable ex) {
                    // 【TODO 6001】Tracer 日志
                    Tracer.logEvent("ApolloConfigException", ExceptionUtil.getDetailMessage(ex));
                    transaction.setStatus(ex);
                    // 设置最终的异常
                    exception = ex;
                } finally {
                    // 【TODO 6001】Tracer 日志
                    transaction.complete();
                }
                // 计算延迟时间
                // if force refresh, do normal sleep, if normal config load, do exponential sleep
                onErrorSleepTime = m_configNeedForceRefresh.get() ? m_configUtil.getOnErrorRetryInterval() : m_loadConfigFailSchedulePolicy.fail();
            }

        }
        // 若查询配置失败，抛出 ApolloConfigException 异常
        String message = String.format("Load Apollo Config failed - appId: %s, cluster: %s, namespace: %s, url: %s", appId, cluster, m_namespace, url);
        throw new ApolloConfigException(message, exception);
    }

    // 组装查询配置的地址
    String assembleQueryConfigUrl(String uri, String appId, String cluster, String namespace,
                                  String dataCenter, ApolloNotificationMessages remoteMessages, ApolloConfig previousConfig) {
        String path = "configs/%s/%s/%s"; // /configs/{appId}/{clusterName}/{namespace:.+}
        List<String> pathParams = Lists.newArrayList(pathEscaper.escape(appId), pathEscaper.escape(cluster), pathEscaper.escape(namespace));
        Map<String, String> queryParams = Maps.newHashMap();
        // releaseKey
        if (previousConfig != null) {
            queryParams.put("releaseKey", queryParamEscaper.escape(previousConfig.getReleaseKey()));
        }
        // dataCenter
        if (!Strings.isNullOrEmpty(dataCenter)) {
            queryParams.put("dataCenter", queryParamEscaper.escape(dataCenter));
        }
        // ip
        String localIp = m_configUtil.getLocalIp();
        if (!Strings.isNullOrEmpty(localIp)) {
            queryParams.put("ip", queryParamEscaper.escape(localIp));
        }
        // messages
        if (remoteMessages != null) {
            queryParams.put("messages", queryParamEscaper.escape(gson.toJson(remoteMessages)));
        }
        // 格式化 URL
        String pathExpanded = String.format(path, pathParams.toArray());
        // 拼接 Query String
        if (!queryParams.isEmpty()) {
            pathExpanded += "?" + MAP_JOINER.join(queryParams);
        }
        // 拼接最终的请求 URL
        if (!uri.endsWith("/")) {
            uri += "/";
        }
        return uri + pathExpanded;
    }

    /**
     * 注册自己到 RemoteConfigLongPollService 中，实现配置更新的实时通知
     */
    private void scheduleLongPollingRefresh() {
        remoteConfigLongPollService.submit(m_namespace, this);
    }

    /**
     * 当长轮询到配置更新时，发起同步配置的任务
     *
     * @param longPollNotifiedServiceDto ServiceDTO 对象
     * @param remoteMessages             ApolloNotificationMessages 对象
     */
    public void onLongPollNotified(ServiceDTO longPollNotifiedServiceDto, ApolloNotificationMessages remoteMessages) {
        // 提交同步任务
        m_executorService.submit(new Runnable() {

            @Override
            public void run() {
                // 设置 m_configNeedForceRefresh 为 true
                m_configNeedForceRefresh.set(true);
                // 尝试同步配置
                trySync();
            }

        });
    }


}
