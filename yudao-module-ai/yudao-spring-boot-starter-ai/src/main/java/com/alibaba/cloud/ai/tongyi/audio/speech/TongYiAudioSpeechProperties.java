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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import static com.alibaba.cloud.ai.tongyi.common.constants.TongYiConstants.SCA_AI_CONFIGURATION;

/**
 * TongYi audio speech configuration properties.
 *
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */

@ConfigurationProperties(TongYiAudioSpeechProperties.CONFIG_PREFIX)
public class TongYiAudioSpeechProperties {

	/**
	 * Spring Cloud Alibaba AI configuration prefix.
	 */
	public static final String CONFIG_PREFIX = SCA_AI_CONFIGURATION + "audio.speech";
	/**
	 * Default TongYi Chat model.
	 */
	public static final String DEFAULT_AUDIO_MODEL_NAME = AudioSpeechModels.SAMBERT_ZHICHU_V1;

	/**
	 * Enable TongYiQWEN ai audio client.
	 */
	private boolean enabled = true;

	@NestedConfigurationProperty
	private TongYiAudioSpeechOptions options = TongYiAudioSpeechOptions.builder()
			.withModel(DEFAULT_AUDIO_MODEL_NAME)
			.withFormat(SpeechSynthesisAudioFormat.WAV)
			.build();

	public TongYiAudioSpeechOptions getOptions() {

		return this.options;
	}

	public void setOptions(TongYiAudioSpeechOptions options) {

		this.options = options;
	}

	public boolean isEnabled() {

		return this.enabled;
	}

	public void setEnabled(boolean enabled) {

		this.enabled = enabled;
	}

}
