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

import org.springframework.ai.model.ModelResult;
import org.springframework.lang.Nullable;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */

public class Speech implements ModelResult<ByteBuffer> {

	private final ByteBuffer audio;

	private SpeechMetadata speechMetadata;

	public Speech(ByteBuffer audio) {
		this.audio = audio;
	}

	@Override
	public ByteBuffer getOutput() {
		return this.audio;
	}

	@Override
	public SpeechMetadata getMetadata() {

		return speechMetadata != null ? speechMetadata : SpeechMetadata.NULL;
	}

	public Speech withSpeechMetadata(@Nullable SpeechMetadata speechMetadata) {

		this.speechMetadata = speechMetadata;
		return this;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o) {

			return true;
		}
		if (!(o instanceof Speech that)) {

			return false;
		}

		return Arrays.equals(audio.array(), that.audio.array())
				&& Objects.equals(speechMetadata, that.speechMetadata);
	}

	@Override
	public int hashCode() {

		return Objects.hash(Arrays.hashCode(audio.array()), speechMetadata);
	}

	@Override
	public String toString() {

		return "Speech{" + "text=" + audio + ", speechMetadata=" + speechMetadata + '}';
	}

}
