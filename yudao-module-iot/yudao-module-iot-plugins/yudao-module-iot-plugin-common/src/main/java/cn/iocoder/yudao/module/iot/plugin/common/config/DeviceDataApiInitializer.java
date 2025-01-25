package cn.iocoder.yudao.module.iot.plugin.common.config;

import cn.iocoder.yudao.module.iot.api.device.DeviceDataApi;
import cn.iocoder.yudao.module.iot.plugin.common.api.DeviceDataApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// TODO @haohao：这个最好是 autoconfiguration
@Configuration
public class DeviceDataApiInitializer {

    // TODO @haohao：这个要不搞个配置类哈
    @Value("${iot.device-data.url}")
    private String deviceDataUrl;

    @Bean
    public RestTemplate restTemplate() {
        // TODO haohao：如果你有更多的自定义需求，比如连接池、超时时间等，可以在这里设置
        return new RestTemplateBuilder().build();
    }

    // TODO @haohao：不存在时，才构建
    @Bean
    public DeviceDataApi deviceDataApi(RestTemplate restTemplate) {
        return new DeviceDataApiClient(restTemplate, deviceDataUrl);
    }

}
