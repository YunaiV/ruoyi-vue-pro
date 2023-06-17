package cn.iocoder.yudao.framework.trade.config;

import cn.iocoder.yudao.framework.trade.core.aop.AfterSaleLogAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

// TODO @Chopper：和 yudao-module-trade-biz 的 framework 里
@AutoConfiguration
public class YudaoAfterSaleLogAutoConfiguration {

    @Bean
    public AfterSaleLogAspect afterSaleLogAspect() {
        return new AfterSaleLogAspect();
    }

}
