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

package org.springframework.ai.chat.prompt;

public class ChatOptionsBuilder {

	private class ChatOptionsImpl implements ChatOptions {

		private Float temperature;

		private Float topP;

		private Integer topK;

		@Override
		public Float getTemperature() {
			return temperature;
		}

		@Override
		public void setTemperature(Float temperature) {
			this.temperature = temperature;
		}

		@Override
		public Float getTopP() {
			return topP;
		}

		@Override
		public void setTopP(Float topP) {
			this.topP = topP;
		}

		@Override
		public Integer getTopK() {
			return topK;
		}

		@Override
		public void setTopK(Integer topK) {
			this.topK = topK;
		}

	}

	private final ChatOptionsImpl options = new ChatOptionsImpl();

	private ChatOptionsBuilder() {
	}

	public static ChatOptionsBuilder builder() {
		return new ChatOptionsBuilder();
	}

	public ChatOptionsBuilder withTemperature(Float temperature) {
		options.setTemperature(temperature);
		return this;
	}

	public ChatOptionsBuilder withTopP(Float topP) {
		options.setTopP(topP);
		return this;
	}

	public ChatOptionsBuilder withTopK(Integer topK) {
		options.setTopK(topK);
		return this;
	}

	public ChatOptions build() {
		return options;
	}

}
