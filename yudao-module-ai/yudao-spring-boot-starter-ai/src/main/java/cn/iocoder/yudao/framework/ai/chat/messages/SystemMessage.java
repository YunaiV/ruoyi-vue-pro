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

package cn.iocoder.yudao.framework.ai.chat.messages;

import org.springframework.core.io.Resource;

/**
 * 作为输入传递的“system”类型的消息。系统消息给出高级别对话说明。
 * 此角色通常提供高级说明对话。
 * 例如，您可以使用系统消息来指示具有生成性，表现得像某个角色或以特定的方式提供答案总体安排
 *
 * A message of the type 'system' passed as input. The system message gives high level
 * instructions for the conversation. This role typically provides high-level instructions
 * for the conversation. For example, you might use a system message to instruct the
 * generative to behave like a certain character or to provide answers in a specific
 * format.
 */
public class SystemMessage extends AbstractMessage {

	public SystemMessage(String content) {
		super(MessageType.SYSTEM, content);
	}

	public SystemMessage(Resource resource) {
		super(MessageType.SYSTEM, resource);
	}

	@Override
	public String toString() {
		return "SystemMessage{" + "content='" + getContent() + '\'' + ", properties=" + properties + ", messageType="
				+ messageType + '}';
	}

}
