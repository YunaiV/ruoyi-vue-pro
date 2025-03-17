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

package cn.iocoder.yudao.framework.ai.core.model.siliconflow;

import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.image.*;
import org.springframework.ai.image.observation.DefaultImageModelObservationConvention;
import org.springframework.ai.image.observation.ImageModelObservationContext;
import org.springframework.ai.image.observation.ImageModelObservationConvention;
import org.springframework.ai.image.observation.ImageModelObservationDocumentation;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.openai.api.common.OpenAiApiConstants;
import org.springframework.ai.openai.metadata.OpenAiImageGenerationMetadata;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

import java.util.List;

/**
 * cv openapi图片模型方法
 *
 * @author zzt
 */
public class SiliconflowImageModel implements ImageModel {

	private static final Logger logger = LoggerFactory.getLogger(SiliconflowImageModel.class);

	private static final ImageModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultImageModelObservationConvention();

	/**
	 * The default options used for the image completion requests.
	 */
	private final SiliconflowImageOptions defaultOptions;

	/**
	 * The retry template used to retry the OpenAI Image API calls.
	 */
	private final RetryTemplate retryTemplate;

	/**
	 * Low-level access to the OpenAI Image API.
	 */
	private final SiiconflowmageApi siiconflowmageApi;

	/**
	 * Observation registry used for instrumentation.
	 */
	private final ObservationRegistry observationRegistry;

	/**
	 * Conventions to use for generating observations.
	 */
	private ImageModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;

	/**
	 * Creates an instance of the OpenAiImageModel.
	 * @param siiconflowmageApi The OpenAiImageApi instance to be used for interacting with
	 * the OpenAI Image API.
	 * @throws IllegalArgumentException if openAiImageApi is null
	 */
	public SiliconflowImageModel(SiiconflowmageApi siiconflowmageApi) {
		this(siiconflowmageApi, SiliconflowImageOptions.builder().build(), RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

	/**
	 * Initializes a new instance of the OpenAiImageModel.
	 * @param siiconflowmageApi The OpenAiImageApi instance to be used for interacting with
	 * the OpenAI Image API.
	 * @param options The OpenAiImageOptions to configure the image model.
	 * @param retryTemplate The retry template.
	 */
	public SiliconflowImageModel(SiiconflowmageApi siiconflowmageApi, SiliconflowImageOptions options, RetryTemplate retryTemplate) {
		this(siiconflowmageApi, options, retryTemplate, ObservationRegistry.NOOP);
	}

	/**
	 * Initializes a new instance of the OpenAiImageModel.
	 * @param siiconflowmageApi The OpenAiImageApi instance to be used for interacting with
	 * the OpenAI Image API.
	 * @param options The OpenAiImageOptions to configure the image model.
	 * @param retryTemplate The retry template.
	 * @param observationRegistry The ObservationRegistry used for instrumentation.
	 */
	public SiliconflowImageModel(SiiconflowmageApi siiconflowmageApi, SiliconflowImageOptions options, RetryTemplate retryTemplate,
                                 ObservationRegistry observationRegistry) {
		Assert.notNull(siiconflowmageApi, "OpenAiImageApi must not be null");
		Assert.notNull(options, "options must not be null");
		Assert.notNull(retryTemplate, "retryTemplate must not be null");
		Assert.notNull(observationRegistry, "observationRegistry must not be null");
		this.siiconflowmageApi = siiconflowmageApi;
		this.defaultOptions = options;
		this.retryTemplate = retryTemplate;
		this.observationRegistry = observationRegistry;
	}

	@Override
	public ImageResponse call(ImagePrompt imagePrompt) {
		SiiconflowmageApi.SiliconflowImageRequest imageRequest = createRequest(imagePrompt);

		var observationContext = ImageModelObservationContext.builder()
			.imagePrompt(imagePrompt)
			.provider(OpenAiApiConstants.PROVIDER_NAME)
			.requestOptions(imagePrompt.getOptions())
			.build();

		return ImageModelObservationDocumentation.IMAGE_MODEL_OPERATION
			.observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
					this.observationRegistry)
			.observe(() -> {
				ResponseEntity<OpenAiImageApi.OpenAiImageResponse> imageResponseEntity = this.retryTemplate
					.execute(ctx -> this.siiconflowmageApi.createImage(imageRequest));

				ImageResponse imageResponse = convertResponse(imageResponseEntity, imageRequest);

				observationContext.setResponse(imageResponse);

				return imageResponse;
			});
	}

	private SiiconflowmageApi.SiliconflowImageRequest createRequest(ImagePrompt imagePrompt) {
		String instructions = imagePrompt.getInstructions().get(0).getText();

		SiiconflowmageApi.SiliconflowImageRequest imageRequest = new SiiconflowmageApi.SiliconflowImageRequest(instructions,
				imagePrompt.getOptions().getModel());

		return ModelOptionsUtils.merge(imagePrompt.getOptions(), imageRequest, SiiconflowmageApi.SiliconflowImageRequest.class);
	}

	private ImageResponse convertResponse(ResponseEntity<OpenAiImageApi.OpenAiImageResponse> imageResponseEntity,
										  SiiconflowmageApi.SiliconflowImageRequest siliconflowImageRequest) {
		OpenAiImageApi.OpenAiImageResponse imageApiResponse = imageResponseEntity.getBody();
		if (imageApiResponse == null) {
			logger.warn("No image response returned for request: {}", siliconflowImageRequest);
			return new ImageResponse(List.of());
		}

		List<ImageGeneration> imageGenerationList = imageApiResponse.data()
			.stream()
			.map(entry -> new ImageGeneration(new Image(entry.url(), entry.b64Json()),
					new OpenAiImageGenerationMetadata(entry.revisedPrompt())))
			.toList();

		ImageResponseMetadata openAiImageResponseMetadata = new ImageResponseMetadata(imageApiResponse.created());
		return new ImageResponse(imageGenerationList, openAiImageResponseMetadata);
	}
}
