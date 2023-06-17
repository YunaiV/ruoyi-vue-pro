package cn.iocoder.yudao.framework.trade.config;

import cn.iocoder.yudao.framework.trade.core.annotations.AfterSaleLog;
import cn.iocoder.yudao.framework.trade.core.aop.AfterSaleLogAspect;
import cn.iocoder.yudao.module.system.api.logger.OperateLogApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class YudaoAfterSaleLogAutoConfiguration {

    @Bean
    public AfterSaleLogAspect afterSaleLogAspect() {
        return new AfterSaleLogAspect();
    }

}
