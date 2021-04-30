package cn.iocoder.dashboard.framework.dict.config;

import cn.iocoder.dashboard.framework.dict.core.service.DictDataFrameworkService;
import cn.iocoder.dashboard.framework.dict.core.util.DictUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DictConfiguration {

    @Bean
    @SuppressWarnings("InstantiationOfUtilityClass")
    public DictUtils dictUtils(DictDataFrameworkService service) {
        DictUtils.init(service);
        return new DictUtils();
    }

}
