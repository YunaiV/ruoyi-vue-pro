package cn.iocoder.yudao.module.iot.script.config;

import cn.iocoder.yudao.module.iot.script.engine.ScriptEngineFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
    @Primary
    public ScriptEngineFactory scriptEngineFactory() {
        return new ScriptEngineFactory();
    }
}