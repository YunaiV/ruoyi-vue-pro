package cn.iocoder.yudao.module.iot.plugin.http;

import cn.iocoder.yudao.module.iot.plugin.http.config.VertxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 独立运行入口
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "cn.iocoder.yudao.module.iot.plugin") // TODO @haohao：建议不扫描 cn.iocoder.yudao.module.iot.plugin；而是通过自动配置，初始化 common 的
public class HttpPluginSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(HttpPluginSpringbootApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        ConfigurableApplicationContext context = application.run(args);

        // 手动获取 VertxService 并启动
        // TODO @haohao：可以放在 bean 的 init 里么？
        VertxService vertxService = context.getBean(VertxService.class);
        vertxService.startServer();

        log.info("[HttpPluginSpringbootApplication] 独立模式启动完成");
    }

}