package cn.iocoder.yudao.module.iot.net.component.server.config;

import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.net.component.server.upstream.IotComponentUpstreamClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * IoT 网络组件服务器配置类
 *
 * @author haohao
 */
@Configuration
@EnableConfigurationProperties(IotNetComponentServerProperties.class)
@EnableScheduling
public class IotNetComponentServerConfiguration {

    /**
     * 配置 RestTemplate
     *
     * @param properties 配置
     * @return RestTemplate
     */
    @Bean
    // TODO @haohao：貌似要独立一个 restTemplate 的名字？不然容易冲突；
    public RestTemplate restTemplate(IotNetComponentServerProperties properties) {
        return new RestTemplateBuilder()
                .connectTimeout(properties.getUpstreamConnectTimeout())
                .readTimeout(properties.getUpstreamReadTimeout())
                .build();
    }

    /**
     * 配置设备上行客户端
     *
     * @param properties   配置
     * @param restTemplate RestTemplate
     * @return 上行客户端
     */
    @Bean
    @Primary
    public IotDeviceUpstreamApi deviceUpstreamApi(IotNetComponentServerProperties properties,
            RestTemplate restTemplate) {
        return new IotComponentUpstreamClient(properties, restTemplate);
    }

    /**
     * 配置默认的设备上行客户端，避免在独立运行模式下的循环依赖问题
     *
     * @return 设备上行客户端
     */
    @Bean
    @ConditionalOnMissingBean(name = "serverDeviceUpstreamClient")
    public Object serverDeviceUpstreamClient() {
        // 返回一个空对象，避免找不到类的问题
        return new Object();
    }

    // TODO @haohao：这个是不是木有用呀？
    /**
     * 配置默认的组件实例注册客户端
     *
     * @return 插件实例注册客户端
     */
    @Bean
    @ConditionalOnMissingBean(name = "serverPluginInstanceRegistryClient")
    public Object serverPluginInstanceRegistryClient() {
        // 返回一个空对象，避免找不到类的问题
        return new Object();
    }
}