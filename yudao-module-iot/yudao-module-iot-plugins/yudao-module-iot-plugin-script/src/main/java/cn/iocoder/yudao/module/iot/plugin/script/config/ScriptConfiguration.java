package cn.iocoder.yudao.module.iot.plugin.script.config;

import cn.iocoder.yudao.module.iot.plugin.script.engine.ScriptEngineFactory;
import cn.iocoder.yudao.module.iot.plugin.script.service.ScriptService;
import cn.iocoder.yudao.module.iot.plugin.script.service.ScriptServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 脚本模块配置类
 */
@Configuration
public class ScriptConfiguration {

    /**
     * 创建脚本引擎工厂
     *
     * @return 脚本引擎工厂
     */
    @Bean
    public ScriptEngineFactory scriptEngineFactory() {
        return new ScriptEngineFactory();
    }

    /**
     * 创建脚本服务
     *
     * @param engineFactory 脚本引擎工厂
     * @return 脚本服务
     */
    @Bean
    public ScriptService scriptService(ScriptEngineFactory engineFactory) {
        ScriptServiceImpl service = new ScriptServiceImpl();
        // 如果有其他配置可以在这里设置
        return service;
    }
} 