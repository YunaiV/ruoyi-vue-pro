package cn.iocoder.yudao.framework.ai.config;

import cn.iocoder.yudao.framework.ai.core.model.midjourney.MidjourneyConfig;
import cn.iocoder.yudao.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置
 *
 * @author fansili
 * @time 2024/6/13 09:50
 */
@Configuration
public class YudaoMidjourneyConfiguration {

    @Bean
    @ConditionalOnProperty(value = "ai.midjourney-proxy.enable", havingValue = "true")
    public MidjourneyApi midjourneyApi(YudaoMidjourneyProperties midjourneyProperties) {
        return new MidjourneyApi(BeanUtils.toBean(midjourneyProperties, MidjourneyConfig.class));
    }
}
