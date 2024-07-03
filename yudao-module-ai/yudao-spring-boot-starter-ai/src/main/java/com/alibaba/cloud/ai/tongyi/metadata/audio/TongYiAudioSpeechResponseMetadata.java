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

package com.alibaba.cloud.ai.tongyi.metadata.audio;

import com.alibaba.dashscope.audio.tts.SpeechSynthesisResult;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisUsage;
import com.alibaba.dashscope.audio.tts.timestamp.Sentence;
import org.springframework.ai.chat.metadata.EmptyRateLimit;
import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.model.ResponseMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.HashMap;



/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */

public class TongYiAudioSpeechResponseMetadata extends HashMap<String, Object>  implements ResponseMetadata {

	private SpeechSynthesisUsage usage;

	private String requestId;

	private Sentence time;

	protected static final String AI_METADATA_STRING = "{ @type: %1$s, requestsLimit: %2$s }";

	/**
	 * NULL objects.
	 */
	public static final TongYiAudioSpeechResponseMetadata NULL = new TongYiAudioSpeechResponseMetadata() {
	};

	public static TongYiAudioSpeechResponseMetadata from(SpeechSynthesisResult result) {

		Assert.notNull(result, "TongYi AI speech must not be null");
		TongYiAudioSpeechResponseMetadata speechResponseMetadata = new TongYiAudioSpeechResponseMetadata();



		return speechResponseMetadata;
	}

	public static TongYiAudioSpeechResponseMetadata from(String result) {

		Assert.notNull(result, "TongYi AI speech must not be null");
		TongYiAudioSpeechResponseMetadata speechResponseMetadata = new TongYiAudioSpeechResponseMetadata();

		return speechResponseMetadata;
	}

	@Nullable
	private RateLimit rateLimit;

	public TongYiAudioSpeechResponseMetadata() {

		this(null);
	}

	public TongYiAudioSpeechResponseMetadata(@Nullable RateLimit rateLimit) {

		this.rateLimit = rateLimit;
	}

	@Nullable
	public RateLimit getRateLimit() {

		RateLimit rateLimit = this.rateLimit;
		return rateLimit != null ? rateLimit : new EmptyRateLimit();
	}

	public TongYiAudioSpeechResponseMetadata withRateLimit(RateLimit rateLimit) {

		this.rateLimit = rateLimit;
		return this;
	}

	public TongYiAudioSpeechResponseMetadata withUsage(SpeechSynthesisUsage usage) {

		this.usage = usage;
		return this;
	}

	public TongYiAudioSpeechResponseMetadata withRequestId(String id) {

		this.requestId = id;
		return this;
	}

	public TongYiAudioSpeechResponseMetadata withSentence(Sentence sentence) {

		this.time = sentence;
		return this;
	}

	public SpeechSynthesisUsage getUsage() {
		return usage;
	}

	public String getRequestId() {
		return requestId;
	}

	public Sentence getTime() {
		return time;
	}

	@Override
	public String toString() {
		return AI_METADATA_STRING.formatted(getClass().getName(), getRateLimit());
	}

}
