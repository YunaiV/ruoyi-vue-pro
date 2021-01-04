package cn.iocoder.dashboard.framework.mybatis.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBaits 配置类
 *
 * @author 芋道源码
 */
@Configuration
@MapperScan(value = "cn.iocoder.dashboard", annotationClass = Mapper.class)
public class MybatisConfiguration {
}
