/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ai.chat.metadata;


import org.springframework.ai.model.ResponseMetadata;

/**
 * Abstract Data Type (ADT) modeling common AI provider metadata returned in an AI
 * response.
 *
 * 抽象数据类型（ADT）建模AI响应中返回的常见AI提供者元数据。
 *
 * @author John Blum
 * @since 0.7.0
 */
public interface ChatResponseMetadata extends ResponseMetadata {

	ChatResponseMetadata NULL = new ChatResponseMetadata() {
	};

	/**
	 * Returns AI provider specific metadata on rate limits.
	 * @return AI provider specific metadata on rate limits.
	 * @see RateLimit
	 */
	default RateLimit getRateLimit() {
		return new EmptyRateLimit();
	}

	/**
	 * Returns AI provider specific metadata on API usage.
	 * @return AI provider specific metadata on API usage.
	 * @see Usage
	 */
	default Usage getUsage() {
		return new EmptyUsage();
	}

	default PromptMetadata getPromptMetadata() {
		return PromptMetadata.empty();
	}

}
