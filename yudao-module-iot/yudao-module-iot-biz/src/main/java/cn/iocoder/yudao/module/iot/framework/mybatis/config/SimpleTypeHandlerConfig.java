package cn.iocoder.yudao.module.iot.framework.mybatis.config;

import cn.iocoder.yudao.module.iot.framework.mybatis.handler.SimpleObjectTypeHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.context.annotation.Configuration;

/**
 * 简单类型处理器配置
 * 注册自定义的类型处理器，避免 JSON 解析错误
 *
 * @author 芋道源码
 */
@Slf4j
@Configuration
public class SimpleTypeHandlerConfig {

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @PostConstruct
    public void registerTypeHandlers() {
        TypeHandlerRegistry registry = sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();

        // 注册简单的 Object 类型处理器，避免 JSON 解析问题
        registry.register(java.lang.Object.class, new SimpleObjectTypeHandler());

        log.info("简单类型处理器注册完成，避免 JSON 解析错误");
    }
}
