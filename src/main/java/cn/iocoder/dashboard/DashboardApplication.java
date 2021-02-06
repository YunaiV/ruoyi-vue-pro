package cn.iocoder.dashboard;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAdminServer // TODO 芋艿：需要迁移出去
public class DashboardApplication {

//    static {
//        // 设置读取的配置文件
//        System.setProperty("spring.config.name", "application,db");
//    }

    public static void main(String[] args) {
        SpringApplication.run(DashboardApplication.class, args);
    }

}
