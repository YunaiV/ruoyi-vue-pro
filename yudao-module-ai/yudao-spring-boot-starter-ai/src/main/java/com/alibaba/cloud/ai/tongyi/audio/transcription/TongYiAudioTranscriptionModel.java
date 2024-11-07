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

package com.alibaba.cloud.ai.tongyi.audio.transcription;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.cloud.ai.tongyi.audio.AudioTranscriptionModels;
import com.alibaba.cloud.ai.tongyi.audio.transcription.api.AudioTranscriptionPrompt;
import com.alibaba.cloud.ai.tongyi.audio.transcription.api.AudioTranscriptionResponse;
import com.alibaba.cloud.ai.tongyi.audio.transcription.api.AudioTranscriptionResult;
import com.alibaba.cloud.ai.tongyi.common.exception.TongYiException;
import com.alibaba.cloud.ai.tongyi.metadata.audio.TongYiAudioTranscriptionResponseMetadata;
import com.alibaba.dashscope.audio.asr.transcription.*;
import org.springframework.ai.model.Model;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TongYiAudioTranscriptionModel is a client for TongYi audio transcription service for
 * Spring Cloud Alibaba AI.
 * @author xYLiu
 * @author yuluo
 * @since 2023.0.1.0
 */

public class TongYiAudioTranscriptionModel
		implements Model<AudioTranscriptionPrompt, AudioTranscriptionResponse> {

	/**
	 * TongYi models options.
	 */
	private final TongYiAudioTranscriptionOptions defaultOptions;

	/**
	 * TongYi models api.
	 */
	private final Transcription transcription;

	public TongYiAudioTranscriptionModel(Transcription transcription) {
		this(null, transcription);
	}

	public TongYiAudioTranscriptionModel(TongYiAudioTranscriptionOptions defaultOptions,
			Transcription transcription) {
		Assert.notNull(transcription, "transcription must not be null");
		Assert.notNull(defaultOptions, "defaultOptions must not be null");

		this.defaultOptions = defaultOptions;
		this.transcription = transcription;
	}

	@Override
	public AudioTranscriptionResponse call(AudioTranscriptionPrompt prompt) {

		TranscriptionParam transcriptionParam;

		if (prompt.getOptions() != null) {
			var param = merge(prompt.getOptions());
			transcriptionParam = toTranscriptionParam(param);
			transcriptionParam.setFileUrls(prompt.getOptions().getFileUrls());
		}
		else {
			Resource instructions = prompt.getInstructions();
			try {
				transcriptionParam = TranscriptionParam.builder()
						.model(AudioTranscriptionModels.Paraformer_V1)
						.fileUrls(ListUtil.of(String.valueOf(instructions.getURL())))
						.build();
			}
			catch (IOException e) {
				throw new TongYiException("Failed to create resource", e);
			}
		}

		List<TranscriptionTaskResult> taskResultList;
		try {
			// Submit a transcription request
			TranscriptionResult result = transcription.asyncCall(transcriptionParam);
			// Wait for the transcription to complete
			result = transcription.wait(TranscriptionQueryParam
					.FromTranscriptionParam(transcriptionParam, result.getTaskId()));
			// Get the transcription results
			System.out.println(result.getOutput().getAsJsonObject());
			taskResultList = result.getResults();
			System.out.println(Arrays.toString(taskResultList.toArray()));

			return new AudioTranscriptionResponse(
					taskResultList.stream().map(taskResult ->
							new AudioTranscriptionResult(taskResult.getTranscriptionUrl())
					).collect(Collectors.toList()),
					TongYiAudioTranscriptionResponseMetadata.from(result)
			);
		}
		catch (Exception e) {
			throw new TongYiException("Failed to call audio transcription", e);
		}

	}

	public TongYiAudioTranscriptionOptions merge(TongYiAudioTranscriptionOptions target) {
		var mergeBuilder = TongYiAudioTranscriptionOptions.builder();

		mergeBuilder
				.withModel(defaultOptions.getModel() != null ? defaultOptions.getModel()
						: target.getModel());
		mergeBuilder.withChannelId(
				defaultOptions.getChannelId() != null ? defaultOptions.getChannelId()
						: target.getChannelId());
		mergeBuilder.withDiarizationEnabled(defaultOptions.getDiarizationEnabled() != null
				? defaultOptions.getDiarizationEnabled()
				: target.getDiarizationEnabled());
		mergeBuilder.withDisfluencyRemovalEnabled(
				defaultOptions.getDisfluencyRemovalEnabled() != null
						? defaultOptions.getDisfluencyRemovalEnabled()
						: target.getDisfluencyRemovalEnabled());
		mergeBuilder.withTimestampAlignmentEnabled(
				defaultOptions.getTimestampAlignmentEnabled() != null
						? defaultOptions.getTimestampAlignmentEnabled()
						: target.getTimestampAlignmentEnabled());
		mergeBuilder.withSpecialWordFilter(defaultOptions.getSpecialWordFilter() != null
				? defaultOptions.getSpecialWordFilter()
				: target.getSpecialWordFilter());
		mergeBuilder.withAudioEventDetectionEnabled(
				defaultOptions.getAudioEventDetectionEnabled() != null
						? defaultOptions.getAudioEventDetectionEnabled()
						: target.getAudioEventDetectionEnabled());

		return mergeBuilder.build();
	}

	public TranscriptionParam toTranscriptionParam(
			TongYiAudioTranscriptionOptions source) {
		var mergeBuilder = TranscriptionParam.builder();

		mergeBuilder.model(source.getModel() != null ? source.getModel()
				: AudioTranscriptionModels.Paraformer_V1);
		mergeBuilder.fileUrls(
				source.getFileUrls() != null ? source.getFileUrls() : new ArrayList<>());
		if (source.getPhraseId() != null) {
			mergeBuilder.phraseId(source.getPhraseId());
		}
		if (source.getChannelId() != null) {
			mergeBuilder.channelId(source.getChannelId());
		}
		if (source.getDiarizationEnabled() != null) {
			mergeBuilder.diarizationEnabled(source.getDiarizationEnabled());
		}
		if (source.getSpeakerCount() != null) {
			mergeBuilder.speakerCount(source.getSpeakerCount());
		}
		if (source.getDisfluencyRemovalEnabled() != null) {
			mergeBuilder.disfluencyRemovalEnabled(source.getDisfluencyRemovalEnabled());
		}
		if (source.getTimestampAlignmentEnabled() != null) {
			mergeBuilder.timestampAlignmentEnabled(source.getTimestampAlignmentEnabled());
		}
		if (source.getSpecialWordFilter() != null) {
			mergeBuilder.specialWordFilter(source.getSpecialWordFilter());
		}
		if (source.getAudioEventDetectionEnabled() != null) {
			mergeBuilder
					.audioEventDetectionEnabled(source.getAudioEventDetectionEnabled());
		}

		return mergeBuilder.build();
	}

}
