package cn.iocoder.yudao.framework.mybatis.config;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.handler.DefaultDBFieldHandler;
import cn.iocoder.yudao.framework.mybatis.core.injector.YudaoSqlInjector;
import cn.iocoder.yudao.framework.mybatis.core.util.CaseInsensitiveResultMap;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.core.handlers.IJsonTypeHandler;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.extension.incrementer.*;
import com.baomidou.mybatisplus.extension.parser.JsqlParserGlobal;
import com.baomidou.mybatisplus.extension.parser.cache.JdkSerialCaffeineJsqlParseCache;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.mapping.ResultMap;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * MyBaits 配置类
 *
 * @author 芋道源码
 */
@AutoConfiguration(before = MybatisPlusAutoConfiguration.class) // 目的：先于 MyBatis Plus 自动配置，避免 @MapperScan 可能扫描不到 Mapper 打印 warn 日志
@MapperScan(value = "${yudao.info.base-package}", annotationClass = Mapper.class,
        lazyInitialization = "${mybatis.lazy-initialization:false}") // Mapper 懒加载，目前仅用于单元测试
public class YudaoMybatisAutoConfiguration {

    static {
        // 动态 SQL 智能优化支持本地缓存加速解析，更完善的租户复杂 XML 动态 SQL 支持，静态注入缓存
        JsqlParserGlobal.setJsqlParseCache(new JdkSerialCaffeineJsqlParseCache(
                (cache) -> cache.maximumSize(1024)
                        .expireAfterWrite(5, TimeUnit.SECONDS))
        );
        // 特殊：JacksonTypeHandler 先使用 JsonUtils 的 ObjectMapper（支持 LocalDateTime），避免使用 MyBatis Plus 默认的 ObjectMapper
        JacksonTypeHandler.setObjectMapper(JsonUtils.getObjectMapper());
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor()); // 分页插件
        // ↓↓↓ 按需开启，可能会影响到 updateBatch 的地方：例如说文件配置管理 ↓↓↓
        // mybatisPlusInterceptor.addInnerInterceptor(new BlockAttackInnerInterceptor()); // 拦截没有指定条件的 update 和 delete 语句
        return mybatisPlusInterceptor;
    }

    @Bean
    public MetaObjectHandler defaultMetaObjectHandler() {
        return new DefaultDBFieldHandler(); // 自动填充参数类
    }

    @Bean
    @Primary // 特殊：使用 @Primary 的原因，避免 MyBatis Plus Join 再创建 MPJSqlInjector 包装它
    public ISqlInjector yudaoSqlInjector() {
        return new YudaoSqlInjector(); // selectMaps 的结果行，使用 CaseInsensitiveResultMap
    }

    @Bean
    public ConfigurationCustomizer caseInsensitiveResultMapConfigurationCustomizer() {
        // 注册 CaseInsensitiveResultMap 的 resultMap，用于注解 SQL 的 @ResultMap
        return configuration -> configuration.addResultMap(new ResultMap.Builder(configuration,
                CaseInsensitiveResultMap.RESULT_MAP_ID, CaseInsensitiveResultMap.class, new ArrayList<>()).build());
    }

    @Bean
    @ConditionalOnProperty(prefix = "mybatis-plus.global-config.db-config", name = "id-type", havingValue = "INPUT")
    public IKeyGenerator keyGenerator(ConfigurableEnvironment environment) {
        DbType dbType = IdTypeEnvironmentPostProcessor.getDbType(environment);
        if (dbType != null) {
            switch (dbType) {
                case POSTGRE_SQL:
                    return new PostgreKeyGenerator();
                case ORACLE:
                case ORACLE_12C:
                    return new OracleKeyGenerator();
                case H2:
                    return new H2KeyGenerator();
                case KINGBASE_ES:
                    return new KingbaseKeyGenerator();
                case DM:
                    return new DmKeyGenerator();
            }
        }
        // 找不到合适的 IKeyGenerator 实现类
        throw new IllegalArgumentException(StrUtil.format("DbType{} 找不到合适的 IKeyGenerator 实现类", dbType));
    }

    @Bean
    public SmartInitializingSingleton jacksonTypeHandlerInitializer(ObjectProvider<ObjectMapper> objectMappers) {
        // 特殊：所有单例初始化后，设置 JacksonTypeHandler 的 ObjectMapper，优先唯一或 @Primary 的 ObjectMapper，否则使用 JsonUtils 的
        // 原因：避免延迟初始化未设置、多个 ObjectMapper 时取到不支持 LocalDateTime 的，且不在创建 SqlSessionFactory 时获取 ObjectMapper。https://t.zsxq.com/KmPNU
        return () -> JacksonTypeHandler.setObjectMapper(objectMappers.getIfUnique(JsonUtils::getObjectMapper));
    }

}
