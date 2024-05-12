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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PromptTemplate，用于将角色指定为字符串实现及其角色不足以满足您的需求。
 *
 * A PromptTemplate that lets you specify the role as a string should the current
 * implementations and their roles not suffice for your needs.
 */
public class ChatPromptTemplate implements PromptTemplateActions, PromptTemplateChatActions {

	private final List<PromptTemplate> promptTemplates;

	public ChatPromptTemplate(List<PromptTemplate> promptTemplates) {
		this.promptTemplates = promptTemplates;
	}

	@Override
	public String render() {
		StringBuilder sb = new StringBuilder();
		for (PromptTemplate promptTemplate : promptTemplates) {
			sb.append(promptTemplate.render());
		}
		return sb.toString();
	}

	@Override
	public String render(Map<String, Object> model) {
		StringBuilder sb = new StringBuilder();
		for (PromptTemplate promptTemplate : promptTemplates) {
			sb.append(promptTemplate.render(model));
		}
		return sb.toString();
	}

	@Override
	public List<Message> createMessages() {
		List<Message> messages = new ArrayList<>();
		for (PromptTemplate promptTemplate : promptTemplates) {
			messages.add(promptTemplate.createMessage());
		}
		return messages;
	}

	@Override
	public List<Message> createMessages(Map<String, Object> model) {
		List<Message> messages = new ArrayList<>();
		for (PromptTemplate promptTemplate : promptTemplates) {
			messages.add(promptTemplate.createMessage(model));
		}
		return messages;
	}

	@Override
	public Prompt create() {
		List<Message> messages = createMessages();
		return new Prompt(messages);
	}

	@Override
	public Prompt create(Map<String, Object> model) {
		List<Message> messages = createMessages(model);
		return new Prompt(messages);
	}

}
