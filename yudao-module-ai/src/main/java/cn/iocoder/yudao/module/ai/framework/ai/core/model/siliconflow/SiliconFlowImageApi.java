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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.model.ApiKey;
import org.springframework.ai.model.NoopApiKey;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 硅基流动 Image API
 *
 * @see <a href= "https://docs.siliconflow.cn/cn/api-reference/images/images-generations">Images</a>
 *
 * @author zzt
 */
public class SiliconFlowImageApi {

	private final RestClient restClient;

	public SiliconFlowImageApi(String aiToken) {
		this(SiliconFlowApiConstants.DEFAULT_BASE_URL, aiToken, RestClient.builder());
	}

    public SiliconFlowImageApi(String baseUrl, String openAiToken) {
        this(baseUrl, openAiToken, RestClient.builder());
    }

	public SiliconFlowImageApi(String baseUrl, String openAiToken, RestClient.Builder restClientBuilder) {
		this(baseUrl, openAiToken, restClientBuilder, RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER);
	}

	public SiliconFlowImageApi(String baseUrl, String apiKey, RestClient.Builder restClientBuilder,
                               ResponseErrorHandler responseErrorHandler) {
		this(baseUrl, apiKey, CollectionUtils.toMultiValueMap(Map.of()), restClientBuilder, responseErrorHandler);
	}

	public SiliconFlowImageApi(String baseUrl, String apiKey, MultiValueMap<String, String> headers,
                               RestClient.Builder restClientBuilder, ResponseErrorHandler responseErrorHandler) {
		this(baseUrl, new SimpleApiKey(apiKey), headers, restClientBuilder, responseErrorHandler);
	}

	public SiliconFlowImageApi(String baseUrl, ApiKey apiKey, MultiValueMap<String, String> headers,
                               RestClient.Builder restClientBuilder, ResponseErrorHandler responseErrorHandler) {

		// @formatter:off
		this.restClient = restClientBuilder.baseUrl(baseUrl)
			.defaultHeaders(h -> {
				if(!(apiKey instanceof NoopApiKey)) {
					h.setBearerAuth(apiKey.getValue());
				}
				h.setContentType(MediaType.APPLICATION_JSON);
				h.addAll(headers);
			})
			.defaultStatusHandler(responseErrorHandler)
			.build();
		// @formatter:on
	}

	public ResponseEntity<OpenAiImageApi.OpenAiImageResponse> createImage(SiliconflowImageRequest siliconflowImageRequest) {
		Assert.notNull(siliconflowImageRequest, "Image request cannot be null.");
		Assert.hasLength(siliconflowImageRequest.prompt(), "Prompt cannot be empty.");

		return this.restClient.post()
			.uri("v1/images/generations")
			.body(siliconflowImageRequest)
			.retrieve()
			.toEntity(OpenAiImageApi.OpenAiImageResponse.class);
	}


	// @formatter:off
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record SiliconflowImageRequest (
			@JsonProperty("prompt") String prompt,
			@JsonProperty("model") String model,
			@JsonProperty("batch_size") Integer batchSize,
			@JsonProperty("negative_prompt") String negativePrompt,
			@JsonProperty("seed") Integer seed,
			@JsonProperty("num_inference_steps") Integer numInferenceSteps,
			@JsonProperty("guidance_scale") Float guidanceScale,
			@JsonProperty("image") String image) {

		public SiliconflowImageRequest(String prompt, String model) {
			this(prompt, model, null, null, null, null, null, null);
		}
	}

}
