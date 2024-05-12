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

import org.springframework.ai.model.ModelOptions;

/**
 * 聊天选项代表了常见的选项，可在不同的聊天模式中移植。
 *
 * The ChatOptions represent the common options, portable across different chat models.
 */
public interface ChatOptions extends ModelOptions {

	Float getTemperature();

	void setTemperature(Float temperature);

	Float getTopP();

	void setTopP(Float topP);

	Integer getTopK();

	void setTopK(Integer topK);

}
