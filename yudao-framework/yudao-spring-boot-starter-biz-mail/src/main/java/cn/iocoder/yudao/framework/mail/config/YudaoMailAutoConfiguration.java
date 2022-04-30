package cn.iocoder.yudao.framework.mail.config;

import cn.iocoder.yudao.framework.mail.core.client.MailClientFactory;
import cn.iocoder.yudao.framework.mail.core.client.impl.MailClientFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 邮箱配置类
 *
 * @author 芋道源码
 */
@Configuration
public class YudaoMailAutoConfiguration {

    @Bean
    public MailClientFactory mailClientFactory() {
        return new MailClientFactoryImpl();
    }

}
