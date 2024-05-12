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

import org.springframework.core.io.Resource;

import java.util.List;

/**
 * 作为输入传递的“user”类型的消息具有用户角色的消息来自最终用户或开发者。
 * 它们表示问题、提示或您想要的任何输入产生反应的。
 *
 * A message of the type 'user' passed as input Messages with the user role are from the
 * end-user or developer. They represent questions, prompts, or any input that you want
 * the generative to respond to.
 */
public class UserMessage extends AbstractMessage {

	public UserMessage(String message) {
		super(MessageType.USER, message);
	}

	public UserMessage(Resource resource) {
		super(MessageType.USER, resource);
	}

	public UserMessage(String textContent, List<MediaData> mediaDataList) {
		super(MessageType.USER, textContent, mediaDataList);
	}

	@Override
	public String toString() {
		return "UserMessage{" + "content='" + getContent() + '\'' + ", properties=" + properties + ", messageType="
				+ messageType + '}';
	}

}
