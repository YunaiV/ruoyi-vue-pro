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
package org.springframework.ai.chat;

import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.model.ModelResponse;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 人工智能提供商返回的聊天完成（例如生成）响应。
 *
 * The chat completion (e.g. generation) response returned by an AI provider.
 */
public class ChatResponse implements ModelResponse<Generation> {

	private final ChatResponseMetadata chatResponseMetadata;

	/**
	 * List of generated messages returned by the AI provider.
	 */
	private final List<Generation> generations;

	/**
	 * Construct a new {@link ChatResponse} instance without metadata.
	 * @param generations the {@link List} of {@link Generation} returned by the AI
	 * provider.
	 */
	public ChatResponse(List<Generation> generations) {
		this(generations, ChatResponseMetadata.NULL);
	}

	/**
	 * Construct a new {@link ChatResponse} instance.
	 * @param generations the {@link List} of {@link Generation} returned by the AI
	 * provider.
	 * @param chatResponseMetadata {@link ChatResponseMetadata} containing information
	 * about the use of the AI provider's API.
	 */
	public ChatResponse(List<Generation> generations, ChatResponseMetadata chatResponseMetadata) {
		this.chatResponseMetadata = chatResponseMetadata;
//		this.generations = List.copyOf(generations);
		this.generations = Collections.unmodifiableList(generations);
	}

	/**
	 * The {@link List} of {@link Generation generated outputs}.
	 * <p>
	 * It is a {@link List} of {@link List lists} because the Prompt could request
	 * multiple output {@link Generation generations}.
	 * @return the {@link List} of {@link Generation generated outputs}.
	 */

	@Override
	public List<Generation> getResults() {
		return this.generations;
	}

	/**
	 * @return Returns the first {@link Generation} in the generations list.
	 */
	public Generation getResult() {
		if (CollectionUtils.isEmpty(this.generations)) {
			return null;
		}
		return this.generations.get(0);
	}

	/**
	 * @return Returns {@link ChatResponseMetadata} containing information about the use
	 * of the AI provider's API.
	 */
	@Override
	public ChatResponseMetadata getMetadata() {
		return this.chatResponseMetadata;
	}

	@Override
	public String toString() {
		return "ChatResponse [metadata=" + chatResponseMetadata + ", generations=" + generations + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
//		if (!(o instanceof ChatResponse that))
//			return false;
		if (!(o instanceof ChatResponse)) {
			return false;
		}
		ChatResponse that = (ChatResponse) o;
		return Objects.equals(chatResponseMetadata, that.chatResponseMetadata)
				&& Objects.equals(generations, that.generations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(chatResponseMetadata, generations);
	}

}
