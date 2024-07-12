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

package com.alibaba.cloud.ai.tongyi.audio.transcription.api;

import com.alibaba.cloud.ai.tongyi.audio.transcription.TongYiAudioTranscriptionOptions;
import org.springframework.ai.model.ModelRequest;
import org.springframework.core.io.Resource;

/**
 * @author xYLiu
 * @author yuluo
 * @since 2023.0.1.0
 */

public class AudioTranscriptionPrompt implements ModelRequest<Resource> {

	private Resource audioResource;

	private TongYiAudioTranscriptionOptions transcriptionOptions;

	public AudioTranscriptionPrompt(Resource resource) {
		this.audioResource = resource;
	}

	public AudioTranscriptionPrompt(Resource resource, TongYiAudioTranscriptionOptions transcriptionOptions) {
		this.audioResource = resource;
		this.transcriptionOptions = transcriptionOptions;
	}

	@Override
	public Resource getInstructions() {

		return audioResource;
	}

	@Override
	public TongYiAudioTranscriptionOptions getOptions() {

		return transcriptionOptions;
	}

}
