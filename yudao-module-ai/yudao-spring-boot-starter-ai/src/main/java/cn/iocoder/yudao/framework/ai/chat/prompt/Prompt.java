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

package cn.iocoder.yudao.framework.ai.chat.prompt;

import cn.iocoder.yudao.framework.ai.chat.ChatResponse;
import cn.iocoder.yudao.framework.ai.chat.messages.Message;
import cn.iocoder.yudao.framework.ai.chat.messages.UserMessage;
import cn.iocoder.yudao.framework.ai.model.ModelOptions;
import cn.iocoder.yudao.framework.ai.model.ModelRequest;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 文字内容
 */
public class Prompt implements ModelRequest<List<Message>> {

	private final List<Message> messages;

	private ChatOptions modelOptions;

	public Prompt(String contents) {
		this(new UserMessage(contents));
	}

	public Prompt(Message message) {
		this(Collections.singletonList(message));
	}

	public Prompt(List<Message> messages) {
		this.messages = messages;
	}

	public Prompt(String contents, ChatOptions modelOptions) {
		this(new UserMessage(contents), modelOptions);
	}

	public Prompt(Message message, ChatOptions modelOptions) {
		this(Collections.singletonList(message), modelOptions);
	}

	public Prompt(List<Message> messages, ChatOptions modelOptions) {
		this.messages = messages;
		this.modelOptions = modelOptions;
	}

	public String getContents() {
		StringBuilder sb = new StringBuilder();
		for (Message message : getInstructions()) {
			sb.append(message.getContent());
		}
		return sb.toString();
	}

	@Override
	public ModelOptions getOptions() {
		return this.modelOptions;
	}

	@Override
	public List<Message> getInstructions() {
		return this.messages;
	}

	@Override
	public String toString() {
		return "Prompt{" + "messages=" + this.messages + ", modelOptions=" + this.modelOptions + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
//		if (!(o instanceof Prompt prompt))
//			return false;
		if (!(o instanceof Prompt)) {
			return false;
		}
		Prompt prompt = (Prompt) o;
		return Objects.equals(this.messages, prompt.messages) && Objects.equals(this.modelOptions, prompt.modelOptions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.messages, this.modelOptions);
	}

}
