package cn.iocoder.yudao.module.oms;

import cn.iocoder.yudao.module.oms.config.OnYudaoServerSaleCondition;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Conditional;

/**
 * @description:
 * @author: LaoSan
 * @create: 2025-03-18 11:19
 **/
@Conditional(OnYudaoServerSaleCondition.class)
@SpringBootApplication(scanBasePackages = {"com.somle", "cn.coder.yudao.module"})
@MapperScan(basePackages = {"com.somle.shopify.mapper","com.somle.walmart.mapper", "cn.iocoder.yudao.module.oms.mapper"})
public class SaleApplicant {

    public static void main(String[] args) {
        SpringApplication.run(SaleApplicant.class, args);
    }

}
