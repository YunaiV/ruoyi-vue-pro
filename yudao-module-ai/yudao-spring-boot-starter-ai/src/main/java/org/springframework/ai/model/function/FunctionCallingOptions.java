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

import java.util.List;
import java.util.Set;

/**
 * @author Christian Tzolov
 */
public interface FunctionCallingOptions {

	/**
	 * Function Callbacks to be registered with the ChatClient. For Prompt Options the
	 * functionCallbacks are automatically enabled for the duration of the prompt
	 * execution. For Default Options the FunctionCallbacks are registered but disabled by
	 * default. You have to use "functions" property to list the function names from the
	 * ChatClient registry to be used in the chat completion requests.
	 * @return Return the Function Callbacks to be registered with the ChatClient.
	 */
	List<FunctionCallback> getFunctionCallbacks();

	/**
	 * Set the Function Callbacks to be registered with the ChatClient.
	 * @param functionCallbacks the Function Callbacks to be registered with the
	 * ChatClient.
	 */
	void setFunctionCallbacks(List<FunctionCallback> functionCallbacks);

	/**
	 * @return List of function names from the ChatClient registry to be used in the next
	 * chat completion requests.
	 */
	Set<String> getFunctions();

	/**
	 * Set the list of function names from the ChatClient registry to be used in the next
	 * chat completion requests.
	 * @param functions the list of function names from the ChatClient registry to be used
	 * in the next chat completion requests.
	 */
	void setFunctions(Set<String> functions);

	/**
	 * @return Returns FunctionCallingOptionsBuilder to create a new instance of
	 * FunctionCallingOptions.
	 */
	public static FunctionCallingOptionsBuilder builder() {
		return new FunctionCallingOptionsBuilder();
	}

}
