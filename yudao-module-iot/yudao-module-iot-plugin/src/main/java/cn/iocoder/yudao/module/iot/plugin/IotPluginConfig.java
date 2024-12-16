package cn.iocoder.yudao.module.iot.plugin;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.context.annotation.Bean;

@Configuration
public class IoTPluginConfig {
    
    @Bean
    public IoTHttpPluginController ioTHttpPluginController() {
        return new IoTHttpPluginController();
    }
}
