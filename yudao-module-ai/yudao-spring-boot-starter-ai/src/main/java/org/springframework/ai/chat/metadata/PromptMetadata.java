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

import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Abstract Data Type (ADT) modeling metadata gathered by the AI during request
 * processing.
 *
 * @author John Blum
 * @since 0.7.0
 */
@FunctionalInterface
public interface PromptMetadata extends Iterable<PromptMetadata.PromptFilterMetadata> {

	/**
	 * Factory method used to create empty {@link PromptMetadata} when the information is
	 * not supplied by the AI provider.
	 * @return empty {@link PromptMetadata}.
	 */
	static PromptMetadata empty() {
		return of();
	}

	/**
	 * Factory method used to create a new {@link PromptMetadata} composed of an array of
	 * {@link PromptFilterMetadata}.
	 * @param array array of {@link PromptFilterMetadata} used to compose the
	 * {@link PromptMetadata}.
	 * @return a new {@link PromptMetadata} composed of an array of
	 * {@link PromptFilterMetadata}.
	 */
	static <T> PromptMetadata of(PromptFilterMetadata... array) {
		return of(Arrays.asList(array));
	}

	/**
	 * Factory method used to create a new {@link PromptMetadata} composed of an
	 * {@link Iterable} of {@link PromptFilterMetadata}.
	 * @param iterable {@link Iterable} of {@link PromptFilterMetadata} used to compose
	 * the {@link PromptMetadata}.
	 * @return a new {@link PromptMetadata} composed of an {@link Iterable} of
	 * {@link PromptFilterMetadata}.
	 */
	static PromptMetadata of(Iterable<PromptFilterMetadata> iterable) {
		Assert.notNull(iterable, "An Iterable of PromptFilterMetadata must not be null");
		return iterable::iterator;
	}

	/**
	 * Returns an {@link Optional} {@link PromptFilterMetadata} at the given index.
	 * @param promptIndex index of the {@link PromptFilterMetadata} contained in this
	 * {@link PromptMetadata}.
	 * @return {@link Optional} {@link PromptFilterMetadata} at the given index.
	 * @throws IllegalArgumentException if the prompt index is less than 0.
	 */
	default Optional<PromptFilterMetadata> findByPromptIndex(int promptIndex) {

		Assert.isTrue(promptIndex > -1, "Prompt index [%d] must be greater than equal to 0".formatted(promptIndex));

		return StreamSupport.stream(this.spliterator(), false)
			.filter(promptFilterMetadata -> promptFilterMetadata.getPromptIndex() == promptIndex)
			.findFirst();
	}

	/**
	 * Abstract Data Type (ADT) modeling filter metadata for all prompts sent during an AI
	 * request.
	 */
	interface PromptFilterMetadata {

		/**
		 * Factory method used to construct a new {@link PromptFilterMetadata} with the
		 * given prompt index and content filter metadata.
		 * @param promptIndex index of the prompt filter metadata contained in the AI
		 * response.
		 * @param contentFilterMetadata underlying AI provider metadata for filtering
		 * applied to prompt content.
		 * @return a new instance of {@link PromptFilterMetadata} with the given prompt
		 * index and content filter metadata.
		 */
		static PromptFilterMetadata from(int promptIndex, Object contentFilterMetadata) {

			return new PromptFilterMetadata() {

				@Override
				public int getPromptIndex() {
					return promptIndex;
				}

				@Override
				@SuppressWarnings("unchecked")
				public <T> T getContentFilterMetadata() {
					return (T) contentFilterMetadata;
				}
			};
		}

		/**
		 * Index of the prompt filter metadata contained in the AI response.
		 * @return an {@link Integer index} fo the prompt filter metadata contained in the
		 * AI response.
		 */
		int getPromptIndex();

		/**
		 * Returns the underlying AI provider metadata for filtering applied to prompt
		 * content.
		 * @param <T> {@link Class Type} used to cast the filtered content metadata into
		 * the AI provider-specific type.
		 * @return the underlying AI provider metadata for filtering applied to prompt
		 * content.
		 */
		<T> T getContentFilterMetadata();

	}

}
