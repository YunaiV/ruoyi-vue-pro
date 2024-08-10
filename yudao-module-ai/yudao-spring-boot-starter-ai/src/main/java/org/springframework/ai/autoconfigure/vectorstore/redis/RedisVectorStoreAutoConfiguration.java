/*
 * Copyright 2023 - 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ai.autoconfigure.vectorstore.redis;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.ai.vectorstore.RedisVectorStore.RedisVectorStoreConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPooled;

/**
 * TODO @xin 先拿 spring-ai 最新代码覆盖，1.0.0-M1 跟 redis 自动配置会冲突
 *
 * TODO 这个官方，有说啥时候 fix 哇？
 *
 * @author Christian Tzolov
 * @author Eddú Meléndez
 */
@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass({JedisPooled.class, JedisConnectionFactory.class, RedisVectorStore.class, EmbeddingModel.class})
//@ConditionalOnBean(JedisConnectionFactory.class)
@EnableConfigurationProperties(RedisVectorStoreProperties.class)
public class RedisVectorStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisVectorStore vectorStore(EmbeddingModel embeddingModel, RedisVectorStoreProperties properties,
                                        JedisConnectionFactory jedisConnectionFactory) {

        var config = RedisVectorStoreConfig.builder()
                .withIndexName(properties.getIndex())
                .withPrefix(properties.getPrefix())
                .build();

        return new RedisVectorStore(config, embeddingModel,
                new JedisPooled(jedisConnectionFactory.getHostName(), jedisConnectionFactory.getPort()),
                properties.isInitializeSchema());
    }

}
