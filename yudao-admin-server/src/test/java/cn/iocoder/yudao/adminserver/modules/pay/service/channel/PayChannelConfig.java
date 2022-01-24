package cn.iocoder.yudao.adminserver.modules.pay.service.channel;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.Validation;
import javax.validation.Validator;

/**
 * 用于初始化 validator Bean 对象
 * @author aquan
 */
@Configuration
public class PayChannelConfig {

    @Bean
    public Validator validator(){
        return Validation.buildDefaultValidatorFactory().getValidator();
    }
}
