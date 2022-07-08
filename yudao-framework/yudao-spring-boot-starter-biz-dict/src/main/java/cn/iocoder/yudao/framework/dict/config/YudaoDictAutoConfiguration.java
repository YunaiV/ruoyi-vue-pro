package cn.iocoder.yudao.framework.dict.config;

import cn.iocoder.yudao.framework.dict.core.util.DictFrameworkUtils;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class YudaoDictAutoConfiguration {

    @Bean
    @SuppressWarnings("InstantiationOfUtilityClass")
    public DictFrameworkUtils dictUtils(DictDataApi dictDataApi) {
        DictFrameworkUtils.init(dictDataApi);
        return new DictFrameworkUtils();
    }

}
