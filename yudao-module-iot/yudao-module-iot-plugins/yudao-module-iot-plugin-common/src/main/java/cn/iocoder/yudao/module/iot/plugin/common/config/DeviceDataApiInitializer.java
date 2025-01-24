package cn.iocoder.yudao.module.iot.plugin.common.config;

import cn.iocoder.yudao.module.iot.api.device.DeviceDataApi;
import cn.iocoder.yudao.module.iot.plugin.common.api.DeviceDataApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class DeviceDataApiInitializer {

    @Value("${iot.device-data.url}")
    private String deviceDataUrl;

    @Bean
    public RestTemplate restTemplate() {
        // 如果你有更多的自定义需求，比如连接池、超时时间等，可以在这里设置
        return new RestTemplateBuilder().build();
    }

    @Bean
    public DeviceDataApi deviceDataApi(RestTemplate restTemplate) {
        // 返回我们自定义的 Client 实例
        return new DeviceDataApiClient(restTemplate, deviceDataUrl);
    }

}
