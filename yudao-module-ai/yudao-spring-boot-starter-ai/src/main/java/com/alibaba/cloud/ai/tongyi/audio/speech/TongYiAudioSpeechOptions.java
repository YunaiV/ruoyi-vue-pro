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

package com.alibaba.cloud.ai.tongyi.audio.speech;

import com.alibaba.cloud.ai.tongyi.audio.AudioSpeechModels;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisAudioFormat;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisTextType;
import org.springframework.ai.model.ModelOptions;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */

public class TongYiAudioSpeechOptions implements ModelOptions {


	/**
	 * Audio Speech models.
	 */
	private String model = AudioSpeechModels.SAMBERT_ZHICHU_V1;

	/**
	 * Text content.
	 */
	private String text;

	/**
	 * Input text type.
	 */
	private SpeechSynthesisTextType textType = SpeechSynthesisTextType.PLAIN_TEXT;

	/**
	 * synthesis audio format.
	 */
	private SpeechSynthesisAudioFormat format = SpeechSynthesisAudioFormat.WAV;

	/**
	 * synthesis audio sample rate.
	 */
	private Integer sampleRate = 16000;

	/**
	 * synthesis audio volume.
	 */
	private Integer volume = 50;

	/**
	 * synthesis audio speed.
	 */
	private Float rate = 1.0f;

	/**
	 * synthesis audio pitch.
	 */
	private Float pitch = 1.0f;

	/**
	 * enable word level timestamp.
	 */
	private Boolean enableWordTimestamp = false;

	/**
	 * enable phoneme level timestamp.
	 */
	private Boolean enablePhonemeTimestamp = false;

	public static Builder builder() {
		return new Builder();
	}

	public String getModel() {

		return model;
	}

	public void setModel(String model) {

		this.model = model;
	}

	public String getText() {

		return text;
	}

	public void setText(String text) {

		this.text = text;
	}

	public SpeechSynthesisTextType getTextType() {

		return textType;
	}

	public void setTextType(SpeechSynthesisTextType textType) {

		this.textType = textType;
	}

	public SpeechSynthesisAudioFormat getFormat() {

		return format;
	}

	public void setFormat(SpeechSynthesisAudioFormat format) {

		this.format = format;
	}

	public Integer getSampleRate() {

		return sampleRate;
	}

	public void setSampleRate(Integer sampleRate) {

		this.sampleRate = sampleRate;
	}

	public Integer getVolume() {

		return volume;
	}

	public void setVolume(Integer volume) {

		this.volume = volume;
	}

	public Float getRate() {

		return rate;
	}

	public void setRate(Float rate) {

		this.rate = rate;
	}

	public Float getPitch() {

		return pitch;
	}

	public void setPitch(Float pitch) {

		this.pitch = pitch;
	}

	public Boolean isEnableWordTimestamp() {

		return enableWordTimestamp;
	}

	public void setEnableWordTimestamp(Boolean enableWordTimestamp) {

		this.enableWordTimestamp = enableWordTimestamp;
	}

	public Boolean isEnablePhonemeTimestamp() {

		return enablePhonemeTimestamp;
	}

	public void setEnablePhonemeTimestamp(Boolean enablePhonemeTimestamp) {

		this.enablePhonemeTimestamp = enablePhonemeTimestamp;
	}

	/**
	 * Build a options instances.
	 */
	public static class Builder {

		private final TongYiAudioSpeechOptions options = new TongYiAudioSpeechOptions();

		public Builder withModel(String model) {

			options.model = model;
			return this;
		}

		public Builder withText(String text) {

			options.text = text;
			return this;
		}

		public Builder withTextType(SpeechSynthesisTextType textType) {

			options.textType = textType;
			return this;
		}

		public Builder withFormat(SpeechSynthesisAudioFormat format) {

			options.format = format;
			return this;
		}

		public Builder withSampleRate(Integer sampleRate) {

			options.sampleRate = sampleRate;
			return this;
		}

		public Builder withVolume(Integer volume) {

			options.volume = volume;
			return this;
		}

		public Builder withRate(Float rate) {

			options.rate = rate;
			return this;
		}

		public Builder withPitch(Float pitch) {

			options.pitch = pitch;
			return this;
		}

		public Builder withEnableWordTimestamp(Boolean enableWordTimestamp) {

			options.enableWordTimestamp = enableWordTimestamp;
			return this;
		}

		public Builder withEnablePhonemeTimestamp(Boolean enablePhonemeTimestamp) {

			options.enablePhonemeTimestamp = enablePhonemeTimestamp;
			return this;
		}

		public TongYiAudioSpeechOptions build() {

			return options;
		}

	}

}
