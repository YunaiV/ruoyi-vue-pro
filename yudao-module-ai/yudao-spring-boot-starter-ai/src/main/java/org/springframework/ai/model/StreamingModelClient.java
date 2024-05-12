/*
 * Copyright 2024-2024 the original author or authors.
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

package org.springframework.ai.model;

import reactor.core.publisher.Flux;

/**
 * The StreamingModelClient interface provides a generic API for invoking a AI models with
 * streaming response. It abstracts the process of sending requests and receiving a
 * streaming responses. The interface uses Java generics to accommodate different types of
 * requests and responses, enhancing flexibility and adaptability across different AI
 * model implementations.
 *
 * @param <TReq> the generic type of the request to the AI model
 * @param <TResChunk> the generic type of a single item in the streaming response from the
 * AI model
 * @author Christian Tzolov
 * @since 0.8.0
 */
public interface StreamingModelClient<TReq extends ModelRequest<?>, TResChunk extends ModelResponse<?>> {

	/**
	 * Executes a method call to the AI model.
	 * @param request the request object to be sent to the AI model
	 * @return the streaming response from the AI model
	 */
	Flux<TResChunk> stream(TReq request);

}
