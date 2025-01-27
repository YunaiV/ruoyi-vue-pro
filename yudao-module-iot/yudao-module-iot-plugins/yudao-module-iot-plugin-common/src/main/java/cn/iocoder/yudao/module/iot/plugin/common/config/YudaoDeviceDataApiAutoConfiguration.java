package cn.iocoder.yudao.module.iot.plugin.common.config;

import cn.iocoder.yudao.module.iot.api.device.IotDeviceUpstreamApi;
import cn.iocoder.yudao.module.iot.plugin.common.api.DeviceDataApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * 设备数据 API 初始化器
 *
 * @author haohao
 */
@AutoConfiguration
public class YudaoDeviceDataApiAutoConfiguration {

    // TODO @haohao：这个要不搞个配置类哈
    @Value("${iot.device-data.url}")
    private String deviceDataUrl;

    /**
     * 创建 RestTemplate 实例
     *
     * @return RestTemplate 实例
     */
    @Bean
    public RestTemplate restTemplate() {
        // 如果你有更多的自定义需求，比如连接池、超时时间等，可以在这里设置
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(5000)) // 设置连接超时时间
                .setReadTimeout(Duration.ofMillis(5000)) // 设置读取超时时间
                .build();
    }

    /**
     * 创建 DeviceDataApi 实例
     *
     * @param restTemplate RestTemplate 实例
     * @return DeviceDataApi 实例
     */
    @Bean
    public IotDeviceUpstreamApi deviceDataApi(RestTemplate restTemplate) {
        return new DeviceDataApiClient(restTemplate, deviceDataUrl);
    }

}