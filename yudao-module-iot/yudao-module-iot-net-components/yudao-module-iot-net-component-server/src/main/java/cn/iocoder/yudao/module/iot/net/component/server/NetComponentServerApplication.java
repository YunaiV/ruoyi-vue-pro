package cn.iocoder.yudao.module.iot.net.component.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * IoT 网络组件聚合启动服务
 *
 * @author haohao
 */
@SpringBootApplication(scanBasePackages = {"${yudao.info.base-package}.module.iot.net.component"})
public class NetComponentServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NetComponentServerApplication.class, args);
    }

} 