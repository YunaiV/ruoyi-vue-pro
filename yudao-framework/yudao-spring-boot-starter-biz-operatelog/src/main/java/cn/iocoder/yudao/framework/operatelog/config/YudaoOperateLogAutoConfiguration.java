package cn.iocoder.yudao.framework.operatelog.config;

import cn.iocoder.yudao.framework.operatelog.core.aop.OperateLogAspect;
import cn.iocoder.yudao.framework.operatelog.core.service.OperateLogFrameworkService;
import cn.iocoder.yudao.framework.operatelog.core.service.OperateLogFrameworkServiceImpl;
import cn.iocoder.yudao.module.system.api.logger.OperateLogApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class YudaoOperateLogAutoConfiguration {

    @Bean
    public OperateLogAspect operateLogAspect() {
        return new OperateLogAspect();
    }

    @Bean
    public OperateLogFrameworkService operateLogFrameworkService(OperateLogApi operateLogApi) {
        return new OperateLogFrameworkServiceImpl(operateLogApi);
    }

}
