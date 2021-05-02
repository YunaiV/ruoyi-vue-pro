package cn.iocoder.yudao.framework.operatelog.config;

import cn.iocoder.yudao.framework.operatelog.core.aop.OperateLogAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class YudaoOperateLogAutoConfiguration {

    @Bean
    public OperateLogAspect operateLogAspect() {
        return new OperateLogAspect();
    }

}
