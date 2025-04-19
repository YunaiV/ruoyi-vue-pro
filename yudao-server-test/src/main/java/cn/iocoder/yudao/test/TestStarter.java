package cn.iocoder.yudao.test;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;



@SpringBootApplication(scanBasePackages = {"${yudao.info.base-package}.server", "${yudao.info.base-package}.module","com.somle"})
@EnableJpaRepositories(basePackages = "com.somle")
@EntityScan(basePackages = "com.somle")
public class TestStarter {

}
