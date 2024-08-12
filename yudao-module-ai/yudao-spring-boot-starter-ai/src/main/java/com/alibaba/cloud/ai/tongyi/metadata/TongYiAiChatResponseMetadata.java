/*
 * Copyright 2023-2024 the original author or authors.
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

package com.alibaba.cloud.ai.tongyi.metadata;

import com.alibaba.dashscope.aigc.generation.GenerationResult;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.PromptMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.util.Assert;

import java.util.HashMap;



/**
 * {@link ChatResponseMetadata} implementation for {@literal Alibaba DashScope}.
 *
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */

public class TongYiAiChatResponseMetadata  extends HashMap<String, Object> implements ChatResponseMetadata {

	protected static final String AI_METADATA_STRING = "{ @type: %1$s, id: %2$s, usage: %3$s, rateLimit: %4$s }";

	@SuppressWarnings("all")
	public static TongYiAiChatResponseMetadata from(GenerationResult chatCompletions,
			PromptMetadata promptFilterMetadata) {

		Assert.notNull(chatCompletions, "Alibaba ai ChatCompletions must not be null");
		String id = chatCompletions.getRequestId();
		TongYiAiUsage usage = TongYiAiUsage.from(chatCompletions);

		return new TongYiAiChatResponseMetadata(
				id,
				usage,
				promptFilterMetadata
		);
	}

	private final String id;

	private final Usage usage;

	private final PromptMetadata promptMetadata;

	protected TongYiAiChatResponseMetadata(String id, TongYiAiUsage usage, PromptMetadata promptMetadata) {

		this.id = id;
		this.usage = usage;
		this.promptMetadata = promptMetadata;
	}

	public String getId() {
		return this.id;
	}

	@Override
	public Usage getUsage() {
		return this.usage;
	}

	@Override
	public PromptMetadata getPromptMetadata() {
		return this.promptMetadata;
	}

	@Override
	public String toString() {

		return AI_METADATA_STRING.formatted(getClass().getTypeName(), getId(), getUsage(), getRateLimit());
	}

}
