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

import com.alibaba.cloud.ai.tongyi.audio.speech.TongYiAudioSpeechOptions;
import org.springframework.ai.model.ModelRequest;

import java.util.Objects;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */

public class SpeechPrompt implements ModelRequest<SpeechMessage> {

	private TongYiAudioSpeechOptions speechOptions;

	private final SpeechMessage message;

	public SpeechPrompt(String instructions) {

		this(new SpeechMessage(instructions), TongYiAudioSpeechOptions.builder().build());
	}

	public SpeechPrompt(String instructions, TongYiAudioSpeechOptions speechOptions) {

		this(new SpeechMessage(instructions), speechOptions);
	}

	public SpeechPrompt(SpeechMessage speechMessage) {
		this(speechMessage, TongYiAudioSpeechOptions.builder().build());
	}

	public SpeechPrompt(SpeechMessage speechMessage, TongYiAudioSpeechOptions speechOptions) {

		this.message = speechMessage;
		this.speechOptions = speechOptions;
	}

	@Override
	public SpeechMessage getInstructions() {
		return this.message;
	}

	@Override
	public TongYiAudioSpeechOptions getOptions() {

		return speechOptions;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o) {

			return true;
		}
		if (!(o instanceof SpeechPrompt that)) {

			return false;
		}

		return Objects.equals(speechOptions, that.speechOptions) && Objects.equals(message, that.message);
	}

	@Override
	public int hashCode() {

		return Objects.hash(speechOptions, message);
	}


}
