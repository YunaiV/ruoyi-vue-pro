package cn.iocoder.yudao.module.iot.plugin.http;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "cn.iocoder.yudao.module.iot.plugin")
public class HttpPluginSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(HttpPluginSpringbootApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }

}