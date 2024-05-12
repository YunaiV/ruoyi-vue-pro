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
package org.springframework.ai.chat.messages;

public enum MessageType {

	// 用户消息
	USER("user"),

	// 之前会话的消息
	ASSISTANT("assistant"),

	// 根据注释说明：您可以使用系统消息来指示具有生成性，表现得像某个角色或以特定的方式提供答案总体安排
	// 简单理解：在对话前，发送一条具有角色的信息让模型理解（如：你现在是一个10年拍摄经验的导演，拥有丰富的经验。 这样你就可以去问他，怎么拍一个短视频可以在抖音上火）
	SYSTEM("system"),

	// 函数？根据引用现在不支持，会抛出一个异常 ---> throw new IllegalArgumentException("Tool execution results are not supported for Bedrock models");
	FUNCTION("function");

	private final String value;

	MessageType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static MessageType fromValue(String value) {
		for (MessageType messageType : MessageType.values()) {
			if (messageType.getValue().equals(value)) {
				return messageType;
			}
		}
		throw new IllegalArgumentException("Invalid MessageType value: " + value);
	}

}
