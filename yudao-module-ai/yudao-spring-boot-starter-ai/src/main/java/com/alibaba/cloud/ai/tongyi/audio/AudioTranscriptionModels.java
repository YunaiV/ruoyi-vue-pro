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

package com.alibaba.cloud.ai.tongyi.audio;

/**
 * @author xYLiu
 * @author yuluo
 * @since 2023.0.1.0
 */

public final class AudioTranscriptionModels {

	private AudioTranscriptionModels() {
	}

	/**
	 * Paraformer Chinese and English speech recognition model supports audio or video speech recognition with a sampling rate of 16kHz or above.
	 */
	public static final String Paraformer_V1 = "paraformer-v1";
	/**
	 * Paraformer Chinese speech recognition model, support 8kHz telephone speech recognition.
	 */
	public static final String Paraformer_8K_V1 = "paraformer-8k-v1";
	/**
	 * The Paraformer multilingual speech recognition model supports audio or video speech recognition with a sample rate of 16kHz or above.
	 */
	public static final String Paraformer_MTL_V1 = "paraformer-mtl-v1";

}
