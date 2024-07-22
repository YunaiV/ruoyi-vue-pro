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

import org.springframework.ai.model.ResultMetadata;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */

public interface SpeechMetadata extends ResultMetadata {

	/**
	 * Null Object.
	 */
	SpeechMetadata NULL = SpeechMetadata.create();

	/**
	 * Factory method used to construct a new {@link SpeechMetadata}.
	 * @return a new {@link SpeechMetadata}
	 */
	static SpeechMetadata create() {
		return new SpeechMetadata() {
		};
	}

}
