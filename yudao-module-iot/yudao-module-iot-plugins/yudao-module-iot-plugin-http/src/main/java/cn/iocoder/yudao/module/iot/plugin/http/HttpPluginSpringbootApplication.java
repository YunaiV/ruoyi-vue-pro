package cn.iocoder.yudao.module.iot.plugin.http;

import cn.iocoder.yudao.module.iot.plugin.http.upstream.IotDeviceUpstreamServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 独立运行入口
 */
@Slf4j
@SpringBootApplication
public class HttpPluginSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(HttpPluginSpringbootApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        ConfigurableApplicationContext context = application.run(args);

        // 手动获取 VertxService 并启动
        // TODO @haohao：可以放在 bean 的 init 里么？回复：会和插件模式冲突 @芋艿，测试下
        IotDeviceUpstreamServer vertxService = context.getBean(IotDeviceUpstreamServer.class);
        vertxService.start();

        log.info("[HttpPluginSpringbootApplication] 独立模式启动完成");
    }

}