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

package cn.iocoder.yudao.framework.ai.model.function;

/**
 * Represents a model function call handler. Implementations are registered with the
 * Models and called on prompts that trigger the function call.
 *
 * @author Christian Tzolov
 */
public interface FunctionCallback {

	/**
	 * @return Returns the Function name. Unique within the model.
	 */
	public String getName();

	/**
	 * @return Returns the function description. This description is used by the model do
	 * decide if the function should be called or not.
	 */
	public String getDescription();

	/**
	 * @return Returns the JSON schema of the function input type.
	 */
	public String getInputTypeSchema();

	/**
	 * Called when a model detects and triggers a function call. The model is responsible
	 * to pass the function arguments in the pre-configured JSON schema format.
	 * @param functionInput JSON string with the function arguments to be passed to the
	 * function. The arguments are defined as JSON schema usually registered with the the
	 * model.
	 * @return String containing the function call response.
	 */
	public String call(String functionInput);

}