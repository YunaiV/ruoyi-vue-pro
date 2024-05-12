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

package org.springframework.ai.chat.prompt;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.core.io.Resource;

import java.util.Map;

public class SystemPromptTemplate extends PromptTemplate {

	public SystemPromptTemplate(String template) {
		super(template);
	}

	public SystemPromptTemplate(Resource resource) {
		super(resource);
	}

	@Override
	public Message createMessage() {
		return new SystemMessage(render());
	}

	@Override
	public Message createMessage(Map<String, Object> model) {
		return new SystemMessage(render(model));
	}

	@Override
	public Prompt create() {
		return new Prompt(new SystemMessage(render()));
	}

	@Override
	public Prompt create(Map<String, Object> model) {
		return new Prompt(new SystemMessage(render(model)));
	}

}
