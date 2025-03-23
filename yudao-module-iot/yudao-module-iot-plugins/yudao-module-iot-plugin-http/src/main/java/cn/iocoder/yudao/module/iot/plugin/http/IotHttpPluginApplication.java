package cn.iocoder.yudao.module.iot.plugin.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 独立运行入口
 */
@Slf4j
@SpringBootApplication
public class IotHttpPluginApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(IotHttpPluginApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
        log.info("[main][独立模式启动完成]");
    }

}