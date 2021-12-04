package cn.iocoder.yudao.framework.tenant.config;

import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.tenant.core.db.TenantDatabaseInterceptor;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 多租户针对 DB 的自动配置
 *
 * @author 芋道源码
 */
@EnableConfigurationProperties(TenantProperties.class)
public class YudaoTenantDatabaseAutoConfiguration {

    @Bean
    public TenantLineInnerInterceptor tenantLineInnerInterceptor(TenantProperties properties) {
        return new TenantLineInnerInterceptor(new TenantDatabaseInterceptor(properties));
    }

    @Bean
    public BeanPostProcessor mybatisPlusInterceptorBeanPostProcessor(TenantLineInnerInterceptor tenantLineInnerInterceptor) {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                if (!(bean instanceof MybatisPlusInterceptor)) {
                    return bean;
                }
                // 将 TenantDatabaseInterceptor 添加到最前面
                MybatisPlusInterceptor interceptor = (MybatisPlusInterceptor) bean;
                MyBatisUtils.addInterceptor(interceptor, tenantLineInnerInterceptor);
                return bean;
            }

        };
    }

}
