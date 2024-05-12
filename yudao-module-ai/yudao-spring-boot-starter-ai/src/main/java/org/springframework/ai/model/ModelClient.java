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

/**
 * The ModelClient interface provides a generic API for invoking AI models. It is designed
 * to handle the interaction with various types of AI models by abstracting the process of
 * sending requests and receiving responses. The interface uses Java generics to
 * accommodate different types of requests and responses, enhancing flexibility and
 * adaptability across different AI model implementations.
 *
 * @param <TReq> the generic type of the request to the AI model
 * @param <TRes> the generic type of the response from the AI model
 * @author Mark Pollack
 * @since 0.8.0
 */
public interface ModelClient<TReq extends ModelRequest<?>, TRes extends ModelResponse<?>> {

	/**
	 * Executes a method call to the AI model.
	 * @param request the request object to be sent to the AI model
	 * @return the response from the AI model
	 */
	TRes call(TReq request) throws Throwable;

}
