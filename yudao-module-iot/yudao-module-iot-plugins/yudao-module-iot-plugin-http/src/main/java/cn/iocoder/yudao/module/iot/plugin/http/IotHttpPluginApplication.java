package cn.iocoder.yudao.module.iot.plugin.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// TODO @芋艿：是不是搞成 cn.iocoder.yudao.module.iot.plugin？或者 common、script 要自动配置
/**
 * 独立运行入口
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {
        "cn.iocoder.yudao.module.iot.plugin.common", // common 的包
        "cn.iocoder.yudao.module.iot.plugin.http", // http 的包
        "cn.iocoder.yudao.module.iot.plugin.script" // script 的包
})
public class IotHttpPluginApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(IotHttpPluginApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
        log.info("[main][独立模式启动完成]");
    }

}