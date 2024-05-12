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

package org.springframework.ai.model.function;

import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builder for {@link FunctionCallingOptions}. Using the {@link FunctionCallingOptions}
 * permits options portability between different AI providers that support
 * function-calling.
 *
 * @author Christian Tzolov
 * @since 0.8.1
 */
public class FunctionCallingOptionsBuilder {

	private final PortableFunctionCallingOptions options;

	public FunctionCallingOptionsBuilder() {
		this.options = new PortableFunctionCallingOptions();
	}

	public FunctionCallingOptionsBuilder withFunctionCallbacks(List<FunctionCallback> functionCallbacks) {
		this.options.setFunctionCallbacks(functionCallbacks);
		return this;
	}

	public FunctionCallingOptionsBuilder withFunctionCallback(FunctionCallback functionCallback) {
		Assert.notNull(functionCallback, "FunctionCallback must not be null");
		this.options.getFunctionCallbacks().add(functionCallback);
		return this;
	}

	public FunctionCallingOptionsBuilder withFunctions(Set<String> functions) {
		this.options.setFunctions(functions);
		return this;
	}

	public FunctionCallingOptionsBuilder withFunction(String function) {
		Assert.notNull(function, "Function must not be null");
		this.options.getFunctions().add(function);
		return this;
	}

	public FunctionCallingOptionsBuilder withTemperature(Float temperature) {
		this.options.setTemperature(temperature);
		return this;
	}

	public FunctionCallingOptionsBuilder withTopP(Float topP) {
		this.options.setTopP(topP);
		return this;
	}

	public FunctionCallingOptionsBuilder withTopK(Integer topK) {
		this.options.setTopK(topK);
		return this;
	}

	public PortableFunctionCallingOptions build() {
		return this.options;
	}

	public static class PortableFunctionCallingOptions implements FunctionCallingOptions, ChatOptions {

		private List<FunctionCallback> functionCallbacks = new ArrayList<>();

		private Set<String> functions = new HashSet<>();

		private Float temperature;

		private Float topP;

		private Integer topK;

		@Override
		public List<FunctionCallback> getFunctionCallbacks() {
			return this.functionCallbacks;
		}

		@Override
		public void setFunctionCallbacks(List<FunctionCallback> functionCallbacks) {
			Assert.notNull(functionCallbacks, "FunctionCallbacks must not be null");
			this.functionCallbacks = functionCallbacks;
		}

		@Override
		public Set<String> getFunctions() {
			return this.functions;
		}

		@Override
		public void setFunctions(Set<String> functions) {
			Assert.notNull(functions, "Functions must not be null");
			this.functions = functions;
		}

		@Override
		public Float getTemperature() {
			return this.temperature;
		}

		@Override
		public void setTemperature(Float temperature) {
			this.temperature = temperature;
		}

		@Override
		public Float getTopP() {
			return this.topP;
		}

		@Override
		public void setTopP(Float topP) {
			this.topP = topP;
		}

		@Override
		public Integer getTopK() {
			return this.topK;
		}

		@Override
		public void setTopK(Integer topK) {
			this.topK = topK;
		}

	}

}
