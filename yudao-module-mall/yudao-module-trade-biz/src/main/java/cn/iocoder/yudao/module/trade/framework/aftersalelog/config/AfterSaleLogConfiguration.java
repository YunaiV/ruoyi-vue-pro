package cn.iocoder.yudao.module.trade.framework.aftersalelog.config;

import cn.iocoder.yudao.module.trade.framework.aftersalelog.core.aop.AfterSaleLogAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * trade 模块的 afterSaleLog 组件的 Configuration
 *
 * @author 陈賝
 * @since 2023/6/18 11:09
 */
@AutoConfiguration
public class AfterSaleLogConfiguration {

    @Bean
    public AfterSaleLogAspect afterSaleLogAspect() {
        return new AfterSaleLogAspect();
    }

}
