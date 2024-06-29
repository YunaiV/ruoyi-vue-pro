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

import org.springframework.ai.model.Model;

import java.nio.ByteBuffer;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.0.0-RC1
 */

@FunctionalInterface
public interface SpeechModel extends Model<SpeechPrompt, SpeechResponse> {

	/**
	 * Generates spoken audio from the provided text message.
	 * @param message the text message to be converted to audio.
	 * @return the resulting audio bytes.
	 */
	default ByteBuffer call(String message) {

		SpeechPrompt prompt = new SpeechPrompt(message);

		return call(prompt).getResult().getOutput();
	}

	/**
	 * Sends a speech request to the TongYi TTS API and returns the resulting speech response.
	 * @param request the speech prompt containing the input text and other parameters.
	 * @return the speech response containing the generated audio.
	 */
	SpeechResponse call(SpeechPrompt request);

}
