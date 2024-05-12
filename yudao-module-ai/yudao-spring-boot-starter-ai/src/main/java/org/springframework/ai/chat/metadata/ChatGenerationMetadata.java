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

import org.springframework.ai.model.ResultMetadata;
import org.springframework.lang.Nullable;

/**
 * Abstract Data Type (ADT) encapsulating information on the completion choices in the AI
 * response.
 *
 * @author John Blum
 * @since 0.7.0
 */
public interface ChatGenerationMetadata extends ResultMetadata {

	ChatGenerationMetadata NULL = ChatGenerationMetadata.from(null, null);

	/**
	 * Factory method used to construct a new {@link ChatGenerationMetadata} from the
	 * given {@link String finish reason} and content filter metadata.
	 * @param finishReason {@link String} contain the reason for the choice completion.
	 * @param contentFilterMetadata underlying AI provider metadata for filtering applied
	 * to generation content.
	 * @return a new {@link ChatGenerationMetadata} from the given {@link String finish
	 * reason} and content filter metadata.
	 */
	static ChatGenerationMetadata from(String finishReason, Object contentFilterMetadata) {
		return new ChatGenerationMetadata() {

			@Override
			@SuppressWarnings("unchecked")
			public <T> T getContentFilterMetadata() {
				return (T) contentFilterMetadata;
			}

			@Override
			public String getFinishReason() {
				return finishReason;
			}
		};
	}

	/**
	 * Returns the underlying AI provider metadata for filtering applied to generation
	 * content.
	 * @param <T> {@link Class Type} used to cast the filtered content metadata into the
	 * AI provider-specific type.
	 * @return the underlying AI provider metadata for filtering applied to generation
	 * content.
	 */
	@Nullable
	<T> T getContentFilterMetadata();

	/**
	 * Get the {@link String reason} this choice completed for the generation.
	 * @return the {@link String reason} this choice completed for the generation.
	 */
	String getFinishReason();

}
