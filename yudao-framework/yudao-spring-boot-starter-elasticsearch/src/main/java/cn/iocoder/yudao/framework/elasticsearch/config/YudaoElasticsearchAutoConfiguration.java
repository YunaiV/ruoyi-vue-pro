package cn.iocoder.yudao.framework.elasticsearch.config;

import org.dromara.easyes.spring.annotation.EsMapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * Elasticsearch 配置类
 */
@AutoConfiguration
@EsMapperScan("${yudao.info.base-package}.module.*.dal.es") // Easy-ES 会加载扫描范围内的类，避免扫描整个基础包
public class YudaoElasticsearchAutoConfiguration {
}
