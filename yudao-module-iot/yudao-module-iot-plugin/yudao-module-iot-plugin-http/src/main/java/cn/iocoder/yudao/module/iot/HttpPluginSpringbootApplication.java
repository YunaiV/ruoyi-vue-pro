package cn.iocoder.yudao.module.iot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// TODO @haohao：建议包名：cn.iocoder.yudao.module.iot.plugin.${pluginName}，例如说 http。然后子包如下：
// config：方配置类，以及 HttpVertxPlugin 初始化
// service：放 HttpVertxHandler 逻辑；
@SpringBootApplication
public class HttpPluginSpringbootApplication {

    public static void main(String[] args) {
//        SpringApplication.run(HttpPluginSpringbootApplication.class, args);
        SpringApplication application = new SpringApplication(HttpPluginSpringbootApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }

}

// TODO @haohao：如下是 sdk 的包：cn.iocoder.yudao.module.iot.plugin.sdk
// 1. api 包：实现 DeviceDataApi 接口，通过 resttemplate 调用
// 2. config 包：初始化 DeviceDataApi 等等

// 3. 其中 resttemplate 调用的后端地址，通过每个服务的 application.yaml 进行注入。