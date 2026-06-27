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

package cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow;

import io.micrometer.observation.ObservationRegistry;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.image.*;
import org.springframework.ai.image.observation.DefaultImageModelObservationConvention;
import org.springframework.ai.image.observation.ImageModelObservationContext;
import org.springframework.ai.image.observation.ImageModelObservationConvention;
import org.springframework.ai.image.observation.ImageModelObservationDocumentation;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.metadata.OpenAiImageGenerationMetadata;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.ResponseEntity;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 硅基流动 {@link ImageModel} 实现类
 *
 * 参考 {@link OpenAiImageModel} 实现
 *
 * @author zzt
 */
public class SiliconFlowImageModel implements ImageModel {

	private static final Logger logger = LoggerFactory.getLogger(SiliconFlowImageModel.class);

	private static final ImageModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultImageModelObservationConvention();

	private final SiliconFlowImageOptions defaultOptions;

	private final RetryTemplate retryTemplate;

	private final SiliconFlowImageApi siliconFlowImageApi;

	private final ObservationRegistry observationRegistry;

    @Setter
	private ImageModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;

	public SiliconFlowImageModel(SiliconFlowImageApi siliconFlowImageApi) {
		this(siliconFlowImageApi, SiliconFlowImageOptions.builder().build(), RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

	public SiliconFlowImageModel(SiliconFlowImageApi siliconFlowImageApi, SiliconFlowImageOptions options, RetryTemplate retryTemplate) {
		this(siliconFlowImageApi, options, retryTemplate, ObservationRegistry.NOOP);
	}

	public SiliconFlowImageModel(SiliconFlowImageApi siliconFlowImageApi, SiliconFlowImageOptions options, RetryTemplate retryTemplate,
                                 ObservationRegistry observationRegistry) {
		Assert.notNull(siliconFlowImageApi, "SiliconFlowImageApi must not be null");
		Assert.notNull(options, "options must not be null");
		Assert.notNull(retryTemplate, "retryTemplate must not be null");
		Assert.notNull(observationRegistry, "observationRegistry must not be null");
		this.siliconFlowImageApi = siliconFlowImageApi;
		this.defaultOptions = options;
		this.retryTemplate = retryTemplate;
		this.observationRegistry = observationRegistry;
	}

	@Override
	public ImageResponse call(ImagePrompt imagePrompt) {
        SiliconFlowImageOptions requestImageOptions = mergeOptions(imagePrompt.getOptions(), this.defaultOptions);
        SiliconFlowImageApi.SiliconflowImageRequest imageRequest = createRequest(imagePrompt, requestImageOptions);

		var observationContext = ImageModelObservationContext.builder()
			.imagePrompt(imagePrompt)
			.provider(SiliconFlowApiConstants.PROVIDER_NAME)
            .imagePrompt(imagePrompt)
			.build();

		return ImageModelObservationDocumentation.IMAGE_MODEL_OPERATION
			.observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
					this.observationRegistry)
			.observe(() -> {
					ResponseEntity<SiliconFlowImageApi.SiliconFlowImageResponse> imageResponseEntity = RetryUtils.execute(
							this.retryTemplate, () -> this.siliconFlowImageApi.createImage(imageRequest));

				ImageResponse imageResponse = convertResponse(imageResponseEntity, imageRequest);

				observationContext.setResponse(imageResponse);

				return imageResponse;
			});
	}

	private SiliconFlowImageApi.SiliconflowImageRequest createRequest(ImagePrompt imagePrompt,
                                                                      SiliconFlowImageOptions requestImageOptions) {
		String instructions = imagePrompt.getInstructions().getFirst().getText();

		return new SiliconFlowImageApi.SiliconflowImageRequest(
                instructions,
                ModelOptionsUtils.mergeOption(requestImageOptions.getModel(), SiliconFlowApiConstants.DEFAULT_IMAGE_MODEL),
                requestImageOptions.getN(),
                requestImageOptions.getNegativePrompt(),
                requestImageOptions.getSeed(),
                requestImageOptions.getNumInferenceSteps(),
                requestImageOptions.getGuidanceScale(),
                requestImageOptions.getImage());
	}

	private ImageResponse convertResponse(ResponseEntity<SiliconFlowImageApi.SiliconFlowImageResponse> imageResponseEntity,
										  SiliconFlowImageApi.SiliconflowImageRequest siliconflowImageRequest) {
		SiliconFlowImageApi.SiliconFlowImageResponse imageApiResponse = imageResponseEntity.getBody();
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

    private SiliconFlowImageOptions mergeOptions(@Nullable ImageOptions runtimeOptions, SiliconFlowImageOptions defaultOptions) {
        if (runtimeOptions == null) {
            return defaultOptions;
        }
        SiliconFlowImageOptions runtimeOptionsForProvider = runtimeOptions instanceof SiliconFlowImageOptions siliconFlowImageOptions
                ? siliconFlowImageOptions
                : SiliconFlowImageOptions.builder()
                    .model(runtimeOptions.getModel())
                    .batchSize(runtimeOptions.getN())
                    .width(runtimeOptions.getWidth())
                    .height(runtimeOptions.getHeight())
                    .build();

        return SiliconFlowImageOptions.builder()
                // Handle portable image options
                .model(ModelOptionsUtils.mergeOption(runtimeOptionsForProvider.getModel(), defaultOptions.getModel()))
                .batchSize(ModelOptionsUtils.mergeOption(runtimeOptionsForProvider.getN(), defaultOptions.getN()))
                .width(ModelOptionsUtils.mergeOption(runtimeOptionsForProvider.getWidth(), defaultOptions.getWidth()))
                .height(ModelOptionsUtils.mergeOption(runtimeOptionsForProvider.getHeight(), defaultOptions.getHeight()))
                // Handle SiliconFlow specific image options
                .negativePrompt(ModelOptionsUtils.mergeOption(runtimeOptionsForProvider.getNegativePrompt(), defaultOptions.getNegativePrompt()))
                .numInferenceSteps(ModelOptionsUtils.mergeOption(runtimeOptionsForProvider.getNumInferenceSteps(), defaultOptions.getNumInferenceSteps()))
                .guidanceScale(ModelOptionsUtils.mergeOption(runtimeOptionsForProvider.getGuidanceScale(), defaultOptions.getGuidanceScale()))
                .seed(ModelOptionsUtils.mergeOption(runtimeOptionsForProvider.getSeed(), defaultOptions.getSeed()))
                .build();
    }
}
