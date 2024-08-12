/*
 * Copyright 2023-2024 the original author or authors.
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

package com.alibaba.cloud.ai.tongyi.audio.speech.api;

import java.util.Objects;

/**
 * The {@link SpeechMessage} class represents a single text message to
 * be converted to speech by the TongYi LLM TTS.
 *
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */

public class SpeechMessage {

	private String text;

	/**
	 * Constructs a new {@link SpeechMessage} object with the given text.
	 * @param text the text to be converted to speech
	 */
	public SpeechMessage(String text) {
		this.text = text;
	}

	/**
	 * Returns the text of this speech message.
	 * @return the text of this speech message
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text of this speech message.
	 * @param text the new text for this speech message
	 */
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o) {

			return true;
		}

		if (!(o instanceof SpeechMessage that)) {

			return false;
		}

		return Objects.equals(text, that.text);
	}

	@Override
	public int hashCode() {

		return Objects.hash(text);
	}

}
