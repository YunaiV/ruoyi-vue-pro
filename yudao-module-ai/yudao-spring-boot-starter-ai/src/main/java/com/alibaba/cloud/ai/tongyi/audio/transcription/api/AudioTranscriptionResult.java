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

import com.alibaba.cloud.ai.tongyi.metadata.audio.TongYiAudioTranscriptionMetadata;
import org.springframework.ai.model.ModelResult;
import org.springframework.ai.model.ResultMetadata;

import java.util.Objects;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */
public class AudioTranscriptionResult implements ModelResult<String> {

	private String text;

	private TongYiAudioTranscriptionMetadata transcriptionMetadata;

	public AudioTranscriptionResult(String text) {
		this.text = text;
	}

	@Override
	public String getOutput() {

		return this.text;
	}

	@Override
	public ResultMetadata getMetadata() {

		return transcriptionMetadata != null ? transcriptionMetadata : TongYiAudioTranscriptionMetadata.NULL;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AudioTranscriptionResult that = (AudioTranscriptionResult) o;
		return Objects.equals(text, that.text) && Objects.equals(transcriptionMetadata, that.transcriptionMetadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(text, transcriptionMetadata);
	}
}
