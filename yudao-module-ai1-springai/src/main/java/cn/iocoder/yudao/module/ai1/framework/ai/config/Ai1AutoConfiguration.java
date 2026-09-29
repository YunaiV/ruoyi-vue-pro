package cn.iocoder.yudao.module.ai1.framework.ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 芋道 AI1 自动配置
 *
 * 模型、向量存储、MCP 客户端均由 framework/ai/core 与 tool 层按数据库配置程序化构建，这里只负责注册 {@link YudaoAi1Properties}
 *
 * @author 芋道源码
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(YudaoAi1Properties.class)
public class Ai1AutoConfiguration {
}
