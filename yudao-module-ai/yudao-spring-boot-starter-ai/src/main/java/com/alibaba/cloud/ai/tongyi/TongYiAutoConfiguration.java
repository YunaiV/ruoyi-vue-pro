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

package com.alibaba.cloud.ai.tongyi;

import com.alibaba.cloud.ai.tongyi.audio.speech.TongYiAudioSpeechModel;
import com.alibaba.cloud.ai.tongyi.audio.speech.TongYiAudioSpeechProperties;
import com.alibaba.cloud.ai.tongyi.audio.transcription.TongYiAudioTranscriptionModel;
import com.alibaba.cloud.ai.tongyi.audio.transcription.TongYiAudioTranscriptionProperties;
import com.alibaba.cloud.ai.tongyi.chat.TongYiChatModel;
import com.alibaba.cloud.ai.tongyi.chat.TongYiChatProperties;
import com.alibaba.cloud.ai.tongyi.common.constants.TongYiConstants;
import com.alibaba.cloud.ai.tongyi.common.exception.TongYiException;
import com.alibaba.cloud.ai.tongyi.embedding.TongYiTextEmbeddingModel;
import com.alibaba.cloud.ai.tongyi.embedding.TongYiTextEmbeddingProperties;
import com.alibaba.cloud.ai.tongyi.image.TongYiImagesModel;
import com.alibaba.cloud.ai.tongyi.image.TongYiImagesProperties;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.audio.asr.transcription.Transcription;
import com.alibaba.dashscope.audio.tts.SpeechSynthesizer;
import com.alibaba.dashscope.common.MessageManager;
import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.ApiKey;
import com.alibaba.dashscope.utils.Constants;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.util.Objects;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */

@AutoConfiguration
@ConditionalOnClass({
		MessageManager.class,
		TongYiChatModel.class,
		TongYiImagesModel.class,
		TongYiAudioSpeechModel.class,
		TongYiTextEmbeddingModel.class,
		TongYiAudioTranscriptionModel.class
})
@EnableConfigurationProperties({
		TongYiChatProperties.class,
		TongYiImagesProperties.class,
		TongYiAudioSpeechProperties.class,
		TongYiConnectionProperties.class,
		TongYiTextEmbeddingProperties.class,
		TongYiAudioTranscriptionProperties.class
})
public class TongYiAutoConfiguration {

	@Bean
	@Scope("prototype")
	@ConditionalOnMissingBean
	public Generation generation() {

		return new Generation();
	}

	@Bean
	@Scope("prototype")
	@ConditionalOnMissingBean
	public MessageManager msgManager() {

		return new MessageManager(10);
	}

	@Bean
	@Scope("prototype")
	@ConditionalOnMissingBean
	public ImageSynthesis imageSynthesis() {

		return new ImageSynthesis();
	}

	@Bean
	@Scope("prototype")
	@ConditionalOnMissingBean
	public SpeechSynthesizer speechSynthesizer() {

		return new SpeechSynthesizer();
	}

	@Bean
	@ConditionalOnMissingBean
	public Transcription transcription() {

		return new Transcription();
	}

	@Bean
	@ConditionalOnMissingBean
	public TextEmbedding textEmbedding() {

		return new TextEmbedding();
	}

	@Bean
	@ConditionalOnMissingBean
	public FunctionCallbackContext springAiFunctionManager(ApplicationContext context) {

		FunctionCallbackContext manager = new FunctionCallbackContext();
		manager.setApplicationContext(context);

		return manager;
	}

	@Bean
	@ConditionalOnProperty(
			prefix = TongYiChatProperties.CONFIG_PREFIX,
			name = "enabled",
			havingValue = "true",
			matchIfMissing = true
	)
	public TongYiChatModel tongYiChatClient(Generation generation,
			TongYiChatProperties chatOptions,
			TongYiConnectionProperties connectionProperties
	) {

		settingApiKey(connectionProperties);

		return new TongYiChatModel(generation, chatOptions.getOptions());
	}

	@Bean
	@ConditionalOnProperty(
			prefix = TongYiImagesProperties.CONFIG_PREFIX,
			name = "enabled",
			havingValue = "true",
			matchIfMissing = true
	)
	public TongYiImagesModel tongYiImagesClient(
			ImageSynthesis imageSynthesis,
			TongYiImagesProperties imagesOptions,
			TongYiConnectionProperties connectionProperties
	) {

		settingApiKey(connectionProperties);

		return new TongYiImagesModel(imageSynthesis, imagesOptions.getOptions());
	}

	@Bean
	@ConditionalOnProperty(
			prefix = TongYiAudioSpeechProperties.CONFIG_PREFIX,
			name = "enabled",
			havingValue = "true",
			matchIfMissing = true
	)
	public TongYiAudioSpeechModel tongYiAudioSpeechClient(
			SpeechSynthesizer speechSynthesizer,
			TongYiAudioSpeechProperties speechProperties,
			TongYiConnectionProperties connectionProperties
	) {

		settingApiKey(connectionProperties);

		return new TongYiAudioSpeechModel(speechSynthesizer, speechProperties.getOptions());
	}

	@Bean
	@ConditionalOnProperty(
			prefix = TongYiAudioTranscriptionProperties.CONFIG_PREFIX,
			name = "enabled",
			havingValue = "true",
			matchIfMissing = true
	)
	public TongYiAudioTranscriptionModel tongYiAudioTranscriptionClient(
			Transcription transcription,
			TongYiAudioTranscriptionProperties transcriptionProperties,
			TongYiConnectionProperties connectionProperties) {

		settingApiKey(connectionProperties);

		return new TongYiAudioTranscriptionModel(
				transcriptionProperties.getOptions(),
				transcription
		);
	}

	@Bean
	@ConditionalOnProperty(
			prefix = TongYiTextEmbeddingProperties.CONFIG_PREFIX,
			name = "enabled",
			havingValue = "true",
			matchIfMissing = true
	)
	public TongYiTextEmbeddingModel tongYiTextEmbeddingClient(
			TextEmbedding textEmbedding,
			TongYiConnectionProperties connectionProperties
	) {

		settingApiKey(connectionProperties);
		return new TongYiTextEmbeddingModel(textEmbedding);
	}

	/**
	 * Setting the API key.
	 * @param connectionProperties {@link TongYiConnectionProperties}
	 */
	private void settingApiKey(TongYiConnectionProperties connectionProperties) {

		String apiKey;

		try {
			// It is recommended to set the key by defining the api-key in an environment variable.
			var envKey = System.getenv(TongYiConstants.SCA_AI_TONGYI_API_KEY);
			if (Objects.nonNull(envKey)) {
				Constants.apiKey = envKey;
				return;
			}
			if (Objects.nonNull(connectionProperties.getApiKey())) {
				apiKey = connectionProperties.getApiKey();
			}
			else {
				apiKey = ApiKey.getApiKey(null);
			}

			Constants.apiKey = apiKey;
		}
		catch (NoApiKeyException e) {

			throw new TongYiException(e.getMessage());
		}

	}

}
