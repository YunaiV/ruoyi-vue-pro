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

import java.time.Duration;

/**
 * Abstract Data Type (ADT) encapsulating metadata from an AI provider's API rate limits
 * granted to the API key in use and the API key's current balance.
 *
 * @author John Blum
 * @since 0.7.0
 */
public interface RateLimit {

	/**
	 * Returns the maximum number of requests that are permitted before exhausting the
	 * rate limit.
	 * @return an {@link Long} with the maximum number of requests that are permitted
	 * before exhausting the rate limit.
	 * @see #getRequestsRemaining()
	 */
	Long getRequestsLimit();

	/**
	 * Returns the remaining number of requests that are permitted before exhausting the
	 * {@link #getRequestsLimit() rate limit}.
	 * @return an {@link Long} with the remaining number of requests that are permitted
	 * before exhausting the {@link #getRequestsLimit() rate limit}.
	 * @see #getRequestsLimit()
	 */
	Long getRequestsRemaining();

	/**
	 * Returns the {@link Duration time} until the rate limit (based on requests) resets
	 * to its {@link #getRequestsLimit() initial state}.
	 * @return a {@link Duration} representing the time until the rate limit (based on
	 * requests) resets to its {@link #getRequestsLimit() initial state}.
	 * @see #getRequestsLimit()
	 */
	Duration getRequestsReset();

	/**
	 * Returns the maximum number of tokens that are permitted before exhausting the rate
	 * limit.
	 * @return an {@link Long} with the maximum number of tokens that are permitted before
	 * exhausting the rate limit.
	 * @see #getTokensRemaining()
	 */
	Long getTokensLimit();

	/**
	 * Returns the remaining number of tokens that are permitted before exhausting the
	 * {@link #getTokensLimit() rate limit}.
	 * @return an {@link Long} with the remaining number of tokens that are permitted
	 * before exhausting the {@link #getTokensLimit() rate limit}.
	 * @see #getTokensLimit()
	 */
	Long getTokensRemaining();

	/**
	 * Returns the {@link Duration time} until the rate limit (based on tokens) resets to
	 * its {@link #getTokensLimit() initial state}.
	 * @return a {@link Duration} with the time until the rate limit (based on tokens)
	 * resets to its {@link #getTokensLimit() initial state}.
	 * @see #getTokensLimit()
	 */
	Duration getTokensReset();

}
