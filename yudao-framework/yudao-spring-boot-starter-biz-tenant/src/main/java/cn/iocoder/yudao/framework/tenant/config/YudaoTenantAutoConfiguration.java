package cn.iocoder.yudao.framework.tenant.config;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.iocoder.yudao.framework.common.enums.WebFilterOrderEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.redis.config.YudaoCacheProperties;
import cn.iocoder.yudao.framework.tenant.core.TenantLineInnerCustomizer;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnoreAspect;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.framework.tenant.core.db.TenantDatabaseInterceptor;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJobHandlerDecorator;
import cn.iocoder.yudao.framework.tenant.core.mq.TenantRedisMessageInterceptor;
import cn.iocoder.yudao.framework.tenant.core.redis.TenantRedisCacheManager;
import cn.iocoder.yudao.framework.tenant.core.security.TenantSecurityWebFilter;
import cn.iocoder.yudao.framework.tenant.core.service.TenantFrameworkService;
import cn.iocoder.yudao.framework.tenant.core.service.TenantFrameworkServiceImpl;
import cn.iocoder.yudao.framework.tenant.core.web.TenantContextWebFilter;
import cn.iocoder.yudao.framework.web.config.WebProperties;
import cn.iocoder.yudao.framework.web.core.handler.GlobalExceptionHandler;
import cn.iocoder.yudao.module.system.api.tenant.TenantApi;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

@AutoConfiguration
@ConditionalOnProperty(prefix = "yudao.tenant", value = "enable", matchIfMissing = true)
// 允许使用 yudao.tenant.enable=false 禁用多租户
@EnableConfigurationProperties(TenantProperties.class)
public class YudaoTenantAutoConfiguration {

    @Bean
    public TenantFrameworkService tenantFrameworkService(TenantApi tenantApi) {
        return new TenantFrameworkServiceImpl(tenantApi);
    }

    @Bean
    public TenantDatabaseInterceptor tenantDatabaseInterceptor(TenantProperties tenantProperties) {
        return new TenantDatabaseInterceptor(tenantProperties);
    }

    // ========== AOP ==========

    @Bean
    public TenantIgnoreAspect tenantIgnoreAspect() {
        return new TenantIgnoreAspect();
    }

    // ========== DB ==========

    @Bean
    public TenantLineInnerInterceptor tenantLineInnerInterceptor(TenantDatabaseInterceptor tenantDatabaseInterceptor,
        MybatisPlusInterceptor mybatisPlusInterceptor) {
        TenantLineInnerInterceptor inner = new TenantLineInnerInterceptor(tenantDatabaseInterceptor);
        // 添加到 mybatisPlusInterceptor 中
        // 需要加在首个，主要是为了在分页插件前面。这个是 MyBatis Plus 的规定
        MyBatisUtils.addInterceptor(mybatisPlusInterceptor, inner, 0);
        return inner;
    }

    @Bean
    public TenantLineInnerCustomizer mybatisPlusTenantLineInnerCustomizer(MybatisPlusProperties properties) {
        return interceptor -> {
            try {
                // 通过 mybatis-plus 中的配置包路径，扫描所有的 DO 类
                String scanPackage = properties.getTypeAliasesPackage();

                String locationPath =
                    "classpath*:" + ClassUtils.convertClassNameToResourcePath(scanPackage) + "/**/*.class";
                PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                // 利用 spring mvc 的资源加载器，加载所有的 DO 类
                Resource[] resources = resolver.getResources(locationPath);
                for (Resource res : resources) {
                    // 先获取 resource 的元信息，然后获取 class 元信息，最后得到 class 全路径
                    String clsName =
                        new SimpleMetadataReaderFactory().getMetadataReader(res).getClassMetadata().getClassName();
                    // 通过名称加载
                    Class<?> clazz = Class.forName(clsName);
                    // 判断是否 符合 继承于 BaseDO.class 但不继承于 TenantBaseDO.class
                    if (BaseDO.class.isAssignableFrom(clazz) && !TenantBaseDO.class.isAssignableFrom(clazz)) {
                        String ignoreTable = clazz.getAnnotation(TableName.class).value();
                        interceptor.addIgnoreTable(ignoreTable);
                    }
                }
            } catch (Exception ignored) {
            }
        };
    }

    // ========== WEB ==========

    @Bean
    public FilterRegistrationBean<TenantContextWebFilter> tenantContextWebFilter() {
        FilterRegistrationBean<TenantContextWebFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TenantContextWebFilter());
        registrationBean.setOrder(WebFilterOrderEnum.TENANT_CONTEXT_FILTER);
        return registrationBean;
    }

    // ========== Security ==========

    @Bean
    public FilterRegistrationBean<TenantSecurityWebFilter> tenantSecurityWebFilter(TenantProperties tenantProperties,
        WebProperties webProperties, GlobalExceptionHandler globalExceptionHandler,
        TenantFrameworkService tenantFrameworkService) {
        FilterRegistrationBean<TenantSecurityWebFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TenantSecurityWebFilter(tenantProperties, webProperties, globalExceptionHandler,
            tenantFrameworkService));
        registrationBean.setOrder(WebFilterOrderEnum.TENANT_SECURITY_FILTER);
        return registrationBean;
    }

    // ========== MQ ==========

    @Bean
    public TenantRedisMessageInterceptor tenantRedisMessageInterceptor() {
        return new TenantRedisMessageInterceptor();
    }

    // ========== Job ==========

    @Bean
    public BeanPostProcessor jobHandlerBeanPostProcessor(TenantFrameworkService tenantFrameworkService) {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessBeforeInitialization(@Nonnull Object bean, @Nonnull String beanName)
                throws BeansException {
                if (!(bean instanceof JobHandler)) {
                    return bean;
                }
                // 有 TenantJob 注解的情况下，才会进行处理
                if (!AnnotationUtil.hasAnnotation(bean.getClass(), TenantJob.class)) {
                    return bean;
                }

                // 使用 TenantJobHandlerDecorator 装饰
                return new TenantJobHandlerDecorator(tenantFrameworkService, (JobHandler)bean);
            }

        };
    }

    // ========== Redis ==========

    @Bean
    @Primary // 引入租户时，tenantRedisCacheManager 为主 Bean
    public RedisCacheManager tenantRedisCacheManager(RedisTemplate<String, Object> redisTemplate,
        RedisCacheConfiguration redisCacheConfiguration, YudaoCacheProperties yudaoCacheProperties) {
        // 创建 RedisCacheWriter 对象
        RedisConnectionFactory connectionFactory = Objects.requireNonNull(redisTemplate.getConnectionFactory());
        RedisCacheWriter cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory,
            BatchStrategies.scan(yudaoCacheProperties.getRedisScanBatchSize()));
        // 创建 TenantRedisCacheManager 对象
        return new TenantRedisCacheManager(cacheWriter, redisCacheConfiguration);
    }

    @Configuration
    public static class TenantInitializingBean implements InitializingBean {

        private final List<TenantLineInnerCustomizer> customizers;

        private final TenantDatabaseInterceptor interceptor;

        public TenantInitializingBean(ObjectProvider<List<TenantLineInnerCustomizer>> customizers,
            TenantDatabaseInterceptor interceptor) {
            this.customizers = customizers.getIfAvailable();
            this.interceptor = interceptor;
        }

        @Override
        public void afterPropertiesSet() {
            if (!CollectionUtils.isEmpty(customizers)) {
                customizers.forEach(customizer -> customizer.customize(interceptor));
            }
        }
    }
}