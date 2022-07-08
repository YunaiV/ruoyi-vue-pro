package cn.iocoder.yudao.framework.banner.config;

import cn.iocoder.yudao.framework.banner.core.BannerApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Banner 的自动配置类
 *
 * @author 芋道源码
 */
@Configuration
public class YudaoBannerAutoConfiguration {

    @Bean
    public BannerApplicationRunner bannerApplicationRunner() {
        return new BannerApplicationRunner();
    }

}
