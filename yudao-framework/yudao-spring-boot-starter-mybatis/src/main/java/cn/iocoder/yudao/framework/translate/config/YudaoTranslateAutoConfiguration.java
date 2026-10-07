package cn.iocoder.yudao.framework.translate.config;

import cn.iocoder.yudao.framework.translate.core.TranslateUtils;
import org.dromara.trans.config.EasyTransMybatisPlusConfig;
import org.dromara.trans.config.TransServiceConfig;
import org.dromara.trans.service.impl.TransService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;

// 解决 Easy-Trans 自动配置顺序问题，详见 https://t.zsxq.com/nX3vi
@AutoConfiguration(after = {
        EasyTransMybatisPlusConfig.class,
        TransServiceConfig.class
})
public class YudaoTranslateAutoConfiguration {

    @Bean
    @ConditionalOnBean(TransService.class)
    @SuppressWarnings("InstantiationOfUtilityClass")
    public TranslateUtils translateUtils(TransService transService) {
        TranslateUtils.init(transService);
        return new TranslateUtils();
    }

}
