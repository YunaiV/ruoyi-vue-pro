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

package cn.iocoder.yudao.framework.ai.chat.metadata;

/**
 * 抽象数据类型（ADT）封装关于人工智能提供商API使用的元数据根据AI请求。
 *
 * Abstract Data Type (ADT) encapsulating metadata on the usage of an AI provider's API
 * per AI request.
 *
 * @author John Blum
 * @since 0.7.0
 */
public interface Usage {

	/**
	 * 返回AI请求的｛@literal prompt｝中使用的令牌数。
	 * @返回一个｛@link Long｝，其中包含在的｛@literal提示符｝中使用的令牌数AI请求。
	 *
	 * Returns the number of tokens used in the {@literal prompt} of the AI request.
	 * @return an {@link Long} with the number of tokens used in the {@literal prompt} of
	 * the AI request.
	 * @see #getGenerationTokens()
	 */
	Long getPromptTokens();

	/**
	 * Returns the number of tokens returned in the {@literal generation (aka completion)}
	 * of the AI's response.
	 * @return an {@link Long} with the number of tokens returned in the
	 * {@literal generation (aka completion)} of the AI's response.
	 * @see #getPromptTokens()
	 */
	Long getGenerationTokens();

	/**
	 * Return the total number of tokens from both the {@literal prompt} of an AI request
	 * and {@literal generation} of the AI's response.
	 * @return the total number of tokens from both the {@literal prompt} of an AI request
	 * and {@literal generation} of the AI's response.
	 * @see #getPromptTokens()
	 * @see #getGenerationTokens()
	 */
	default Long getTotalTokens() {
		Long promptTokens = getPromptTokens();
		promptTokens = promptTokens != null ? promptTokens : 0;
		Long completionTokens = getGenerationTokens();
		completionTokens = completionTokens != null ? completionTokens : 0;
		return promptTokens + completionTokens;
	}

}
