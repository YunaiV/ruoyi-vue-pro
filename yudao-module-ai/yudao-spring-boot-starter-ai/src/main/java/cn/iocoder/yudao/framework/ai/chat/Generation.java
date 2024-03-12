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

package cn.iocoder.yudao.framework.ai.chat;

import cn.iocoder.yudao.framework.ai.chat.messages.AssistantMessage;
import cn.iocoder.yudao.framework.ai.chat.metadata.ChatGenerationMetadata;
import cn.iocoder.yudao.framework.ai.model.ModelResult;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * 表示AI返回的响应。
 *
 * Represents a response returned by the AI.
 */
public class Generation implements ModelResult<AssistantMessage> {

	private AssistantMessage assistantMessage;

	private ChatGenerationMetadata chatGenerationMetadata;

	public Generation(String text) {
		this.assistantMessage = new AssistantMessage(text);
	}

	public Generation(String text, Map<String, Object> properties) {
		this.assistantMessage = new AssistantMessage(text, properties);
	}

	@Override
	public AssistantMessage getOutput() {
		return this.assistantMessage;
	}

	@Override
	public ChatGenerationMetadata getMetadata() {
		ChatGenerationMetadata chatGenerationMetadata = this.chatGenerationMetadata;
		return chatGenerationMetadata != null ? chatGenerationMetadata : ChatGenerationMetadata.NULL;
	}

	public Generation withGenerationMetadata(@Nullable ChatGenerationMetadata chatGenerationMetadata) {
		this.chatGenerationMetadata = chatGenerationMetadata;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Generation that))
			return false;
		return Objects.equals(assistantMessage, that.assistantMessage)
				&& Objects.equals(chatGenerationMetadata, that.chatGenerationMetadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(assistantMessage, chatGenerationMetadata);
	}

	@Override
	public String toString() {
		return "Generation{" + "assistantMessage=" + assistantMessage + ", chatGenerationMetadata="
				+ chatGenerationMetadata + '}';
	}

}
