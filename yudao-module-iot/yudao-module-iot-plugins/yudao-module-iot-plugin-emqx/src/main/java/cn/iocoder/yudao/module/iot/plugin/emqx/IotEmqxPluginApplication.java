package cn.iocoder.yudao.module.iot.plugin.emqx;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * IoT Emqx 插件的独立运行入口
 */
@Slf4j
@SpringBootApplication
public class IotEmqxPluginApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(IotEmqxPluginApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
        log.info("[main][独立模式启动完成]");
    }

}