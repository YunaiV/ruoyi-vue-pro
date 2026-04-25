package cn.iocoder.yudao.module.deepay.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB 配置。
 *
 * <p>启用 Mongo Repositories（扫描 dal.mongodb 包），并开启审计（@CreatedDate 等）。</p>
 * <p>TTL 索引由 Spring Data MongoDB 的 @Indexed(expireAfterSeconds) 自动创建。</p>
 *
 * <p>连接配置示例（application.yml）：
 * <pre>
 * spring:
 *   data:
 *     mongodb:
 *       uri: mongodb://localhost:27017/deepay
 *       # 或分开配置：
 *       # host: localhost
 *       # port: 27017
 *       # database: deepay
 * </pre>
 * </p>
 */
@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(
        basePackages = "cn.iocoder.yudao.module.deepay.dal.mongodb"
)
public class DeepayMongoConfig {
    // Spring Data MongoDB 自动根据 @Document/@Indexed 注解创建集合和索引
    // TTL 索引（expireAfterSeconds）在 Mongo 启用 autoIndexCreation 时自动生效
}
