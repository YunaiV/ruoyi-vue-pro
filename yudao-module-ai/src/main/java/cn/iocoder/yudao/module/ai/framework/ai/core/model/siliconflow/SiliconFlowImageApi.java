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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.springframework.ai.model.ApiKey;
import org.springframework.ai.model.NoopApiKey;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

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
		this(baseUrl, apiKey, CollectionUtils.toMultiValueMap(Collections.emptyMap()), restClientBuilder, responseErrorHandler);
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

	public ResponseEntity<SiliconFlowImageResponse> createImage(SiliconflowImageRequest siliconflowImageRequest) {
		Assert.notNull(siliconflowImageRequest, "Image request cannot be null.");
		Assert.hasLength(siliconflowImageRequest.prompt(), "Prompt cannot be empty.");

		return this.restClient.post()
			.uri("v1/images/generations")
			.body(siliconflowImageRequest)
			.retrieve()
			.toEntity(SiliconFlowImageResponse.class);
	}


	// @formatter:off
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@Value
	@Accessors(fluent = true)
	@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
	@AllArgsConstructor
	@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
	public static class SiliconflowImageRequest {

		@JsonProperty("prompt") String prompt;
		@JsonProperty("model") String model;
		@JsonProperty("image_size") String imageSize;
		@JsonProperty("batch_size") Integer batchSize;
		@JsonProperty("negative_prompt") String negativePrompt;
		@JsonProperty("seed") Long seed;
		@JsonProperty("num_inference_steps") Integer numInferenceSteps;
		@JsonProperty("guidance_scale") Float guidanceScale;
		@JsonProperty("image") String image;

		public SiliconflowImageRequest(String prompt, String model) {
			this(prompt, model, null, null, null, null, null, null, null);
		}
	}

	/**
	 * 图片生成的响应，与 OpenAI Images 接口的响应格式一致
	 */
	@Value
	@Accessors(fluent = true)
	@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
	@AllArgsConstructor
	@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
	public static class SiliconFlowImageResponse {

		@JsonProperty("created") Long created;
		@JsonProperty("data") List<Data> data;

		@Value
		@Accessors(fluent = true)
		@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
		@AllArgsConstructor
		@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
		public static class Data {

			@JsonProperty("url") String url;
			@JsonProperty("b64_json") String b64Json;
			@JsonProperty("revised_prompt") String revisedPrompt;

		}

	}

}
