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
import com.alibaba.cloud.ai.tongyi.audio.speech.api.*;
import com.alibaba.cloud.ai.tongyi.metadata.audio.TongYiAudioSpeechResponseMetadata;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisResult;
import com.alibaba.dashscope.audio.tts.SpeechSynthesizer;
import com.alibaba.dashscope.common.ResultCallback;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.nio.ByteBuffer;

/**
 * TongYiAudioSpeechClient is a client for TongYi audio speech service for Spring Cloud Alibaba AI.
 *
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */

public class TongYiAudioSpeechModel implements SpeechModel, SpeechStreamModel {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Default speed rate.
	 */
	private static final float SPEED_RATE = 1.0f;

	/**
	 * TongYi models api.
	 */
	private final SpeechSynthesizer speechSynthesizer;

	/**
	 * TongYi models options.
	 */
	private final TongYiAudioSpeechOptions defaultOptions;

	/**
	 * TongYiAudioSpeechClient constructor.
	 * @param speechSynthesizer the speech synthesizer
	 */
	public TongYiAudioSpeechModel(SpeechSynthesizer speechSynthesizer) {

		this(speechSynthesizer, null);
	}

	/**
	 * TongYiAudioSpeechClient constructor.
	 * @param speechSynthesizer the speech synthesizer
	 * @param tongYiAudioOptions the tongYi audio options
	 */
	public TongYiAudioSpeechModel(SpeechSynthesizer speechSynthesizer, TongYiAudioSpeechOptions tongYiAudioOptions) {

		Assert.notNull(speechSynthesizer, "speechSynthesizer must not be null");
		Assert.notNull(tongYiAudioOptions, "tongYiAudioOptions must not be null");

		this.speechSynthesizer = speechSynthesizer;
		this.defaultOptions = tongYiAudioOptions;
	}

	/**
	 * Call the TongYi audio speech service.
	 * @param text the text message to be converted to audio.
	 * @return the audio byte buffer.
	 */
	@Override
	public ByteBuffer call(String text) {

		var speechRequest = new SpeechPrompt(text);

		return call(speechRequest).getResult().getOutput();
	}


	/**
	 * Call the TongYi audio speech service.
	 * @param prompt the speech prompt.
	 * @return the speech response.
	 */
	@Override
	public SpeechResponse call(SpeechPrompt prompt) {

		var SCASpeechParam = merge(prompt.getOptions());
		var speechSynthesisParams = toSpeechSynthesisParams(SCASpeechParam);
		speechSynthesisParams.setText(prompt.getInstructions().getText());
		logger.info(speechSynthesisParams.toString());

		var res = speechSynthesizer.call(speechSynthesisParams);

		return convert(res, null);
	}

	/**
	 * Call the TongYi audio speech service.
	 * @param prompt the speech prompt.
	 * @param callback the result callback.
	 * {@link SpeechSynthesizer#call(SpeechSynthesisParam, ResultCallback)}
	 */
	public void call(SpeechPrompt prompt, ResultCallback<SpeechSynthesisResult> callback) {

		var SCASpeechParam = merge(prompt.getOptions());
		var speechSynthesisParams = toSpeechSynthesisParams(SCASpeechParam);
		speechSynthesisParams.setText(prompt.getInstructions().getText());

		speechSynthesizer.call(speechSynthesisParams, callback);
	}

	/**
	 * Stream the TongYi audio speech service.
	 * @param prompt the speech prompt.
	 * @return the speech response.
	 * {@link SpeechSynthesizer#streamCall(SpeechSynthesisParam)}
	 */
	@Override
	public Flux<SpeechResponse> stream(SpeechPrompt prompt) {

		var SCASpeechParam = merge(prompt.getOptions());

		Flowable<SpeechSynthesisResult> resultFlowable = speechSynthesizer
				.streamCall(toSpeechSynthesisParams(SCASpeechParam));

		return Flux.from(resultFlowable)
				.flatMap(
						res -> Flux.just(res.getAudioFrame())
								.map(audio -> {
									var speech = new Speech(audio);
									var respMetadata = TongYiAudioSpeechResponseMetadata.from(res);
									return new SpeechResponse(speech, respMetadata);
								})
				).publishOn(Schedulers.parallel());
	}

	public TongYiAudioSpeechOptions merge(TongYiAudioSpeechOptions target) {

		var mergeBuilder = TongYiAudioSpeechOptions.builder();

		mergeBuilder.withModel(defaultOptions.getModel() != null ? defaultOptions.getModel() : target.getModel());
		mergeBuilder.withPitch(defaultOptions.getPitch() != null ? defaultOptions.getPitch() : target.getPitch());
		mergeBuilder.withRate(defaultOptions.getRate() != null ? defaultOptions.getRate() : target.getRate());
		mergeBuilder.withFormat(defaultOptions.getFormat() != null ? defaultOptions.getFormat() : target.getFormat());
		mergeBuilder.withSampleRate(defaultOptions.getSampleRate() != null ? defaultOptions.getSampleRate() : target.getSampleRate());
		mergeBuilder.withTextType(defaultOptions.getTextType() != null ? defaultOptions.getTextType() : target.getTextType());
		mergeBuilder.withVolume(defaultOptions.getVolume() != null ? defaultOptions.getVolume() : target.getVolume());
		mergeBuilder.withEnablePhonemeTimestamp(defaultOptions.isEnablePhonemeTimestamp() != null ? defaultOptions.isEnablePhonemeTimestamp() : target.isEnablePhonemeTimestamp());
		mergeBuilder.withEnableWordTimestamp(defaultOptions.isEnableWordTimestamp() != null ? defaultOptions.isEnableWordTimestamp() : target.isEnableWordTimestamp());

		return mergeBuilder.build();
	}

	public SpeechSynthesisParam toSpeechSynthesisParams(TongYiAudioSpeechOptions source) {

		var mergeBuilder = SpeechSynthesisParam.builder();

		mergeBuilder.model(source.getModel() != null ? source.getModel() : AudioSpeechModels.SAMBERT_ZHICHU_V1);
		mergeBuilder.text(source.getText() != null ? source.getText() : "");

		if (source.getFormat() != null) {
			mergeBuilder.format(source.getFormat());
		}
		if (source.getRate() != null) {
			mergeBuilder.rate(source.getRate());
		}
		if (source.getPitch() != null) {
			mergeBuilder.pitch(source.getPitch());
		}
		if (source.getTextType() != null) {
			mergeBuilder.textType(source.getTextType());
		}
		if (source.getSampleRate() != null) {
			mergeBuilder.sampleRate(source.getSampleRate());
		}
		if (source.isEnablePhonemeTimestamp() != null) {
			mergeBuilder.enablePhonemeTimestamp(source.isEnablePhonemeTimestamp());
		}
		if (source.isEnableWordTimestamp() != null) {
			mergeBuilder.enableWordTimestamp(source.isEnableWordTimestamp());
		}
		if (source.getVolume() != null) {
			mergeBuilder.volume(source.getVolume());
		}

		return mergeBuilder.build();
	}

	/**
	 * Convert the TongYi audio speech service result to the speech response.
	 * @param result the audio byte buffer.
	 * @param synthesisResult the synthesis result.
	 * @return the speech response.
	 */
	private SpeechResponse convert(ByteBuffer result, SpeechSynthesisResult synthesisResult) {

		if (synthesisResult == null) {

			return new SpeechResponse(new Speech(result));
		}

		var responseMetadata = TongYiAudioSpeechResponseMetadata.from(synthesisResult);
		var speech = new Speech(synthesisResult.getAudioFrame());

		return new SpeechResponse(speech, responseMetadata);
	}

}
